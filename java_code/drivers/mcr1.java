/***************************************************************************

	Midway MCR-1 system

    driver by Christopher Kirmse, Aaron Giles

	Games supported:
		* Solar Fox
		* Kick

	Known bugs:
		* none at this time

****************************************************************************

	Memory map

****************************************************************************

	========================================================================
	CPU #1
	========================================================================
	0000-6FFF   R     xxxxxxxx    Program ROM
	7000-77FF   R/W   xxxxxxxx    NVRAM
	F000-F1FF   R/W   xxxxxxxx    Sprite RAM
	F400-F41F     W   xxxxxxxx    Palette RAM blue/green
	F800-F81F     W   xxxxxxxx    Palette RAM red
	FC00-FFFF   R/W   xxxxxxxx    Background video RAM
	========================================================================
	0000        R     x-xxxxxx    Input ports
	            R     x-------    Service switch (active low)
	            R     --x-----    Tilt
	            R     ---xxx--    External inputs
	            R     ------x-    Right coin
	            R     -------x    Left coin
	0000        W     xxxxxxxx    Data latch OP0 (coin meters, 2 led's and cocktail 'flip')
	0001        R     xxxxxxxx    External inputs
	0002        R     xxxxxxxx    External inputs
	0003        R     xxxxxxxx    DIP switches
	0004        R     xxxxxxxx    External inputs
	0004        W     xxxxxxxx    Data latch OP4 (comm. with external hardware)
	0007        R     xxxxxxxx    Audio status
	001C-001F   W     xxxxxxxx    Audio latches 1-4
	00E0        W     --------    Watchdog reset
	00E8        W     xxxxxxxx    Unknown (written at initialization time)
	00F0-00F3   W     xxxxxxxx    CTC communications
	========================================================================
	Interrupts:
		NMI ???
		INT generated by CTC
	========================================================================


	========================================================================
	CPU #2 (Super Sound I/O)
	========================================================================
	0000-3FFF   R     xxxxxxxx    Program ROM
	8000-83FF   R/W   xxxxxxxx    Program RAM
	9000-9003   R     xxxxxxxx    Audio latches 1-4
	A000          W   xxxxxxxx    AY-8910 #1 control
	A001        R     xxxxxxxx    AY-8910 #1 status
	A002          W   xxxxxxxx    AY-8910 #1 data
	B000          W   xxxxxxxx    AY-8910 #2 control
	B001        R     xxxxxxxx    AY-8910 #2 status
	B002          W   xxxxxxxx    AY-8910 #2 data
	C000          W   xxxxxxxx    Audio status
	E000          W   xxxxxxxx    Unknown
	F000        R     xxxxxxxx    Audio board switches
	========================================================================
	Interrupts:
		NMI ???
		INT generated by external circuitry 780 times/second
	========================================================================

***************************************************************************/


/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class mcr1
{
	
	
	static const UINT8 *nvram_init;
	
	
	/*************************************
	 *
	 *	Kick input ports
	 *
	 *************************************/
	
	static READ_HANDLER( kick_dial_r )
	{
		return (readinputport(1) & 0x0f) | ((readinputport(6) << 4) & 0xf0);
	}
	
	
	
	/*************************************
	 *
	 *	Solar Fox input ports
	 *
	 *************************************/
	
	static READ_HANDLER( solarfox_input_0_r )
	{
		/* This is a kludge; according to the wiring diagram, the player 2 */
		/* controls are hooked up as documented below. If you go into test */
		/* mode, they will respond. However, if you try it in a 2-player   */
		/* game in cocktail mode, they don't work at all. So we fake-mux   */
		/* the controls through player 1's ports */
		if (mcr_cocktail_flip)
			return readinputport(0) | 0x08;
		else
			return ((readinputport(0) & ~0x14) | 0x08) | ((readinputport(0) & 0x08) >> 1) | ((readinputport(2) & 0x01) << 4);
	}
	
	
	static READ_HANDLER( solarfox_input_1_r )
	{
		/*  same deal as above */
		if (mcr_cocktail_flip)
			return readinputport(1) | 0xf0;
		else
			return (readinputport(1) >> 4) | 0xf0;
	}
	
	
	
	/*************************************
	 *
	 *	NVRAM save/load
	 *
	 *************************************/
	
	static NVRAM_HANDLER( mcr1 )
	{
		unsigned char *ram = memory_region(REGION_CPU1);
	
		if (read_or_write)
			mame_fwrite(file, &ram[0x7000], 0x800);
		else if (file)
			mame_fread(file, &ram[0x7000], 0x800);
		else if (nvram_init)
			memcpy(&ram[0x7000], nvram_init, 16);
	}
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x6fff, MRA_ROM ),
		new Memory_ReadAddress( 0x7000, 0x77ff, MRA_RAM ),
		new Memory_ReadAddress( 0xf000, 0xf1ff, MRA_RAM ),
		new Memory_ReadAddress( 0xfc00, 0xffff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x6fff, MWA_ROM ),
		new Memory_WriteAddress( 0x7000, 0x77ff, MWA_RAM ),
		new Memory_WriteAddress( 0xf000, 0xf1ff, MWA_RAM, &spriteram, &spriteram_size ),
		new Memory_WriteAddress( 0xf400, 0xf41f, paletteram_xxxxRRRRBBBBGGGG_split1_w, &paletteram ),
		new Memory_WriteAddress( 0xf800, 0xf81f, paletteram_xxxxRRRRBBBBGGGG_split2_w, &paletteram_2 ),
		new Memory_WriteAddress( 0xfc00, 0xffff, mcr1_videoram_w, &videoram, &videoram_size ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_ReadPort readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x00, input_port_0_r ),
		new IO_ReadPort( 0x01, 0x01, input_port_1_r ),
		new IO_ReadPort( 0x02, 0x02, input_port_2_r ),
		new IO_ReadPort( 0x03, 0x03, input_port_3_r ),
		new IO_ReadPort( 0x04, 0x04, input_port_4_r ),
		new IO_ReadPort( 0x07, 0x07, ssio_status_r ),
		new IO_ReadPort( 0x10, 0x10, input_port_0_r ),
		new IO_ReadPort( 0xf0, 0xf3, z80ctc_0_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_WritePort writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x1c, 0x1f, ssio_data_w ),
		new IO_WritePort( 0xe0, 0xe0, watchdog_reset_w ),
		new IO_WritePort( 0xe8, 0xe8, MWA_NOP ),
		new IO_WritePort( 0xf0, 0xf3, z80ctc_0_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	INPUT_PORTS_START( solarfox )
		PORT_START	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 )
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 )
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL )
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 )
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_TILT )
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN3 )
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW )
	
		PORT_START	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY )
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY )
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY )
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL )
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_COCKTAIL )
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_COCKTAIL )
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY | IPF_COCKTAIL )
	
		PORT_START	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL )
		PORT_BIT( 0xfe, IP_ACTIVE_LOW, IPT_UNUSED )
	
		PORT_START	/* IN3 -- dipswitches */
		PORT_DIPNAME( 0x03, 0x03, "Bonus" )
		PORT_DIPSETTING(    0x02, "None" )
		PORT_DIPSETTING(    0x03, "After 10 racks" )
		PORT_DIPSETTING(    0x01, "After 20 racks" )
		PORT_BIT( 0x0c, IP_ACTIVE_LOW, IPT_UNKNOWN )
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( Demo_Sounds ))
		PORT_DIPSETTING(    0x10, DEF_STR( Off ))
		PORT_DIPSETTING(    0x00, DEF_STR( On ))
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
		PORT_DIPNAME( 0x40, 0x40, "Ignore Hardware Failure" )
		PORT_DIPSETTING(    0x40, DEF_STR( Off ))
		PORT_DIPSETTING(    0x00, DEF_STR( On ))
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( Cabinet ))
		PORT_DIPSETTING(    0x80, DEF_STR( Upright ))
		PORT_DIPSETTING(    0x00, DEF_STR( Cocktail ))
	
		PORT_START	/* IN4 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED )
	
		PORT_START	/* AIN0 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN )
	INPUT_PORTS_END
	
	
	INPUT_PORTS_START( kick )
		PORT_START	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 )
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 )
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2 )
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 )
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_TILT )
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED )
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW )
	
		PORT_START	/* IN1 -- this is the Kick spinner input.  */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_REVERSE, 3, 50, 0, 0 )
	
		PORT_START	/* IN2 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED )
	
		PORT_START	/* IN3 -- dipswitches */
		PORT_DIPNAME( 0x01, 0x00, "Music" )
		PORT_DIPSETTING(    0x01, DEF_STR( Off ))
		PORT_DIPSETTING(    0x00, DEF_STR( On ))
		PORT_BIT( 0xfe, IP_ACTIVE_LOW, IPT_UNKNOWN )
	
		PORT_START	/* IN4 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED )
	
		PORT_START	/* AIN0 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN )
	
		PORT_START	/* (fake) player 2 dial */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED )
	INPUT_PORTS_END
	
	
	INPUT_PORTS_START( kicka )
		PORT_START	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 )
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 )
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2 )
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 )
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_TILT )
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED )
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW )
	
		PORT_START	/* IN1 -- this is the Kick spinner input.  */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_REVERSE, 3, 50, 0, 0 )
	
		PORT_START	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL )
		PORT_BIT( 0xfe, IP_ACTIVE_LOW, IPT_UNUSED )
	
		PORT_START	/* IN3 -- dipswitches */
		PORT_DIPNAME( 0x01, 0x00, "Music" )
		PORT_DIPSETTING(    0x01, DEF_STR( Off ))
		PORT_DIPSETTING(    0x00, DEF_STR( On ))
		PORT_BIT( 0x3e, IP_ACTIVE_LOW, IPT_UNKNOWN )
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( Cabinet ))
		PORT_DIPSETTING(    0x00, DEF_STR( Upright ))
		PORT_DIPSETTING(    0x40, DEF_STR( Cocktail ))
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )
	
		PORT_START	/* IN4 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN )
	
		PORT_START	/* AIN0 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN )
	
		PORT_START	/* (fake) player 2 dial */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_REVERSE | IPF_COCKTAIL, 3, 50, 0, 0 )
	INPUT_PORTS_END
	
	
	
	/*************************************
	 *
	 *	Graphics definitions
	 *
	 *************************************/
	
	static struct GfxDecodeInfo gfxdecodeinfo[] =
	{
		{ REGION_GFX1, 0, &mcr_bg_layout,     0, 1 },	/* colors 0-15 */
		{ REGION_GFX2, 0, &mcr_sprite_layout, 16, 1 },	/* colors 16-31 */
		{ -1 } /* end of array */
	};
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MACHINE_DRIVER_START( mcr1 )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, MAIN_OSC_MCR_I/8)
		MDRV_CPU_CONFIG(mcr_daisy_chain)
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(readport,writeport)
		MDRV_CPU_VBLANK_INT(mcr_interrupt,2)
	
		MDRV_FRAMES_PER_SECOND(30)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_30HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(mcr)
		MDRV_NVRAM_HANDLER(mcr1)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_UPDATE_BEFORE_VBLANK)
		MDRV_SCREEN_SIZE(32*16, 30*16)
		MDRV_VISIBLE_AREA(0*16, 32*16-1, 0*16, 30*16-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(32)
		
		MDRV_VIDEO_START(mcr1)
		MDRV_VIDEO_UPDATE(mcr1)
	
		/* sound hardware */
		MDRV_IMPORT_FROM(mcr_ssio)
	MACHINE_DRIVER_END
	
	
	
	/*************************************
	 *
	 *	ROM definitions
	 *
	 *************************************/
	
	ROM_START( solarfox )
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "sfcpu.3b",     0x0000, 0x1000, CRC(8c40f6eb) SHA1(a323897cfa8771edd28d58d806913e62110a2689) )
		ROM_LOAD( "sfcpu.4b",     0x1000, 0x1000, CRC(4d47bd7e) SHA1(0cfa09f2c1fe6d662c3a96abc43edf431ccf6d02) )
		ROM_LOAD( "sfcpu.5b",     0x2000, 0x1000, CRC(b52c3bd5) SHA1(bc289641830a3c6640303b1a799c378bf456bed1) )
		ROM_LOAD( "sfcpu.4d",     0x3000, 0x1000, CRC(bd5d25ba) SHA1(b7be1250dfb6af9cc0f9c6b446fb183528eab7de) )
		ROM_LOAD( "sfcpu.5d",     0x4000, 0x1000, CRC(dd57d817) SHA1(059a020313cf929130d1ae9a80f3b91c54fe7699) )
		ROM_LOAD( "sfcpu.6d",     0x5000, 0x1000, CRC(bd993cd9) SHA1(c074a6a40d0b9c0f4bf3fc5982263c89549fb338) )
		ROM_LOAD( "sfcpu.7d",     0x6000, 0x1000, CRC(8ad8731d) SHA1(ffd19c3fbad3c5a240ab27963812cc300f3d7b89) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "sfsnd.7a",     0x0000, 0x1000, CRC(cdecf83a) SHA1(5acd2709e214408d756b39916bb98cd4ecda7988) )
		ROM_LOAD( "sfsnd.8a",     0x1000, 0x1000, CRC(cb7788cb) SHA1(9e86f9131a6f0fc96dd436e21baf45e215ee65f4) )
		ROM_LOAD( "sfsnd.9a",     0x2000, 0x1000, CRC(304896ce) SHA1(00ff640eab50022da980cdc5ce8cedebaaebc9cf) )
	
		ROM_REGION( 0x02000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "sfcpu.4g",     0x0000, 0x1000, CRC(ba019a60) SHA1(81923f8c51eedfef6f5e9d6ace1d785d7afafc14) )
		ROM_LOAD( "sfcpu.5g",     0x1000, 0x1000, CRC(7ff0364e) SHA1(b04034fc076008302931a485e00762dc34660498) )
	
		ROM_REGION( 0x08000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "sfvid.1a",     0x0000, 0x2000, CRC(9d9b5d7e) SHA1(4896c532a3d5763284a4403e8558f634f7b968d8) )
		ROM_LOAD( "sfvid.1b",     0x2000, 0x2000, CRC(78801e83) SHA1(23b5811a03fe4ad576c5313d2205203577300159) )
		ROM_LOAD( "sfvid.1d",     0x4000, 0x2000, CRC(4d8445cf) SHA1(fbe427da0e758b79eb2230713f2cd12e6f8bdeb7) )
		ROM_LOAD( "sfvid.1e",     0x6000, 0x2000, CRC(3da25495) SHA1(e7b703bc8caca7497af92efc869c6f1b7dbc8bf1) )
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 )
		ROM_LOAD( "82s123.12d",   0x0000, 0x0020, CRC(e1281ee9) SHA1(9ac9b01d24affc0ee9227a4364c4fd8f8290343a) )	/* from shollow, assuming it's the same */
	ROM_END
	
	
	ROM_START( kick )
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "1200a-v2.b3",  0x0000, 0x1000, CRC(65924917) SHA1(4fbc7161f4b03bc395c775fa6239a23bf7357e89) )
		ROM_LOAD( "1300b-v2.b4",  0x1000, 0x1000, CRC(27929f52) SHA1(e03a550792df68eeb2a1f5177309fe01b5fcaa3d) )
		ROM_LOAD( "1400c-v2.b5",  0x2000, 0x1000, CRC(69107ce6) SHA1(4aedbb9f1072e315f7e5e3c8559f2995146f4b9d) )
		ROM_LOAD( "1500d-v2.d4",  0x3000, 0x1000, CRC(04a23aa1) SHA1(9a70ca3dc6db984dbb490076cf0ec11cc213f199) )
		ROM_LOAD( "1600e-v2.d5",  0x4000, 0x1000, CRC(1d2834c0) SHA1(176fad90ab14c922a575c3d12a2c8a339d1518d4) )
		ROM_LOAD( "1700f-v2.d6",  0x5000, 0x1000, CRC(ddf84ce1) SHA1(6f80b9a5cbd75b6e4af569ca4bcfcde7daaad64f) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "4200-a.a7",    0x0000, 0x1000, CRC(9e35c02e) SHA1(92afd0126dcfb2d4401927b2cf261090e186b6fa) )
		ROM_LOAD( "4300-b.a8",    0x1000, 0x1000, CRC(ca2b7c28) SHA1(fdcca3b755822c045c3c321cccc3f58112e2ad11) )
		ROM_LOAD( "4400-c.a9",    0x2000, 0x1000, CRC(d1901551) SHA1(fd7d6059f8ac59f95ae6f8ef12fbfce7ed16ec12) )
		ROM_LOAD( "4500-d.a10",   0x3000, 0x1000, CRC(d36ddcdc) SHA1(2d3ec83b9fa5a9d309c393a0c3ee45f0ba8192c9) )
	
		ROM_REGION( 0x02000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "1800g-v2.g4",  0x0000, 0x1000, CRC(b4d120f3) SHA1(e61ae236f14eb1f519e196046fe8240c10932c2e) )
		ROM_LOAD( "1900h-v2.g5",  0x1000, 0x1000, CRC(c3ba4893) SHA1(76998db55519d7f1cf0f6b51d4f8ad7161b3a92e) )
	
		ROM_REGION( 0x08000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "2600a-v2.1e",  0x0000, 0x2000, CRC(2c5d6b55) SHA1(c326b8c7bcb903ea3a1a721443a37e1e8eabe975) )
		ROM_LOAD( "2700b-v2.1d",  0x2000, 0x2000, CRC(565ea97d) SHA1(4a30a371ad407bf774cf08bf528f824675383698) )
		ROM_LOAD( "2800c-v2.1b",  0x4000, 0x2000, CRC(f3be56a1) SHA1(eb3eb0379a918a2959565572d88f9b0f021d1c2a) )
		ROM_LOAD( "2900d-v2.1a",  0x6000, 0x2000, CRC(77da795e) SHA1(ebebc8551e7a7534d89bb8458eb2576713cfbeaf) )
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 )
		ROM_LOAD( "82s123.12d",   0x0000, 0x0020, CRC(e1281ee9) SHA1(9ac9b01d24affc0ee9227a4364c4fd8f8290343a) )	/* from shollow, assuming it's the same */
	ROM_END
	
	
	ROM_START( kicka )
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "1200-a.b3",    0x0000, 0x1000, CRC(22fa42ed) SHA1(3922ce1f13e21cae9ee8d1af58f2bbe83d5eb979) )
		ROM_LOAD( "1300-b.b4",    0x1000, 0x1000, CRC(afaca819) SHA1(383f40d49e4c256e9eb83e778c140b0b97860f69) )
		ROM_LOAD( "1400-c.b5",    0x2000, 0x1000, CRC(6054ee56) SHA1(77da5a8d6209c746b233808a452dbf1917da7f3e) )
		ROM_LOAD( "1500-d.d4",    0x3000, 0x1000, CRC(263af0f3) SHA1(f8c511852f48b6fb5617d5b3666df53b08584db5) )
		ROM_LOAD( "1600-e.d5",    0x4000, 0x1000, CRC(eaaa78a7) SHA1(3c057d486f3938561fb9947e0463b1255ae04ef9) )
		ROM_LOAD( "1700-f.d6",    0x5000, 0x1000, CRC(c06c880f) SHA1(d5ac5682de316b9cb09d433e2c02746efadd2a81) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "4200-a.a7",    0x0000, 0x1000, CRC(9e35c02e) SHA1(92afd0126dcfb2d4401927b2cf261090e186b6fa) )
		ROM_LOAD( "4300-b.a8",    0x1000, 0x1000, CRC(ca2b7c28) SHA1(fdcca3b755822c045c3c321cccc3f58112e2ad11) )
		ROM_LOAD( "4400-c.a9",    0x2000, 0x1000, CRC(d1901551) SHA1(fd7d6059f8ac59f95ae6f8ef12fbfce7ed16ec12) )
		ROM_LOAD( "4500-d.a10",   0x3000, 0x1000, CRC(d36ddcdc) SHA1(2d3ec83b9fa5a9d309c393a0c3ee45f0ba8192c9) )
	
		ROM_REGION( 0x02000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "1000-g.g4",    0x0000, 0x1000, CRC(acdae4f6) SHA1(8af065a7fe07f5b444314adc0ac69073e7bd5354) )
		ROM_LOAD( "1100-h.g5",    0x1000, 0x1000, CRC(dbb18c96) SHA1(a437d5d83b0638ca498e066bab1222bb5f39d5fb) )
	
		ROM_REGION( 0x08000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "2600-a.1e",    0x0000, 0x2000, CRC(74b409d7) SHA1(13769060c02ab514d70e29a2452512f342189fa9) )
		ROM_LOAD( "2700-b.1d",    0x2000, 0x2000, CRC(78eda36c) SHA1(5cf9da6f364f586f324e7ac529db0dc273498320) )
		ROM_LOAD( "2800-c.1b",    0x4000, 0x2000, CRC(c93e0170) SHA1(a7efdb6fd13dccd8d8d10de61b87585828bde6ac) )
		ROM_LOAD( "2900-d.1a",    0x6000, 0x2000, CRC(91e59383) SHA1(bf87642cc747f1abbd80c6f529adfa60a1d9bc9e) )
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 )
		ROM_LOAD( "82s123.12d",   0x0000, 0x0020, CRC(e1281ee9) SHA1(9ac9b01d24affc0ee9227a4364c4fd8f8290343a) )	/* from shollow, assuming it's the same */
	ROM_END
	
	
	
	/*************************************
	 *
	 *	Driver initialization
	 *
	 *************************************/
	
	static DRIVER_INIT( solarfox )
	{
		static const UINT8 hiscore_init[] = { 0,0,1,1,1,1,1,3,3,3,7 };
		nvram_init = hiscore_init;
	
		MCR_CONFIGURE_SOUND(MCR_SSIO);
		install_port_read_handler(0, 0x00, 0x00, solarfox_input_0_r);
		install_port_read_handler(0, 0x01, 0x01, solarfox_input_1_r);
		install_port_write_handler(0, 0x01, 0x01, mcr_control_port_w);
	
		mcr12_sprite_xoffs = 16;
		mcr12_sprite_xoffs_flip = 0;
	}
	
	
	static DRIVER_INIT( kick )
	{
		nvram_init = NULL;
	
		MCR_CONFIGURE_SOUND(MCR_SSIO);
		install_port_read_handler(0, 0x01, 0x01, kick_dial_r);
		install_port_write_handler(0, 0x03, 0x03, mcr_control_port_w);
	
		mcr12_sprite_xoffs = 0;
		mcr12_sprite_xoffs_flip = 16;
	}
	
	
	
	/*************************************
	 *
	 *	Game drivers
	 *
	 *************************************/
	
	GAME( 1981, solarfox, 0,    mcr1, solarfox, solarfox, ROT90 ^ ORIENTATION_FLIP_Y, "Bally Midway", "Solar Fox" )
	GAME( 1981, kick,     0,    mcr1, kick,     kick,     ORIENTATION_SWAP_XY,        "Midway", "Kick (upright)" )
	GAME( 1981, kicka,    kick, mcr1, kicka,    kick,     ROT90,                      "Midway", "Kick (cocktail)" )
}
