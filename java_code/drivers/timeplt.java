/***************************************************************************

Time Pilot memory map (preliminary)

driver by Nicola Salmoria

Main processor memory map.
0000-5fff ROM
a000-a3ff Color RAM
a400-a7ff Video RAM
a800-afff RAM
b000-b7ff sprite RAM (only areas 0xb010 and 0xb410 are used).

memory mapped ports:

read:
c000      video scan line. This is used by the program to multiplex the cloud
          sprites, drawing them twice offset by 128 pixels.
c200      DSW2
c300      IN0
c320      IN1
c340      IN2
c360      DSW1

write:
c000      command for the audio CPU
c200      watchdog reset
c300      interrupt enable
c302      flip screen
c304      trigger interrupt on audio CPU
c308	  Protection ???  Stuffs in some values computed from ROM content
c30a	  coin counter 1
c30c	  coin counter 2

interrupts:
standard NMI at 0x66

SOUND BOARD:
same as Pooyan

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class timeplt
{
	
	
	
	
	
	
	public static WriteHandlerPtr timeplt_coin_counter_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		coin_counter_w(offset >> 1, data);
	} };
	
	public static ReadHandlerPtr psurge_protection_r  = new ReadHandlerPtr() { public int handler(int offset){
		return 0x80;
	} };
	
	
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new Memory_ReadAddress( 0x6004, 0x6004, psurge_protection_r ),	/* psurge only */
		new Memory_ReadAddress( 0xa000, 0xbfff, MRA_RAM ),
		new Memory_ReadAddress( 0xc000, 0xc000, timeplt_scanline_r ),
		new Memory_ReadAddress( 0xc200, 0xc200, input_port_4_r ),	/* DSW2 */
		new Memory_ReadAddress( 0xc300, 0xc300, input_port_0_r ),	/* IN0 */
		new Memory_ReadAddress( 0xc320, 0xc320, input_port_1_r ),	/* IN1 */
		new Memory_ReadAddress( 0xc340, 0xc340, input_port_2_r ),	/* IN2 */
		new Memory_ReadAddress( 0xc360, 0xc360, input_port_3_r ),	/* DSW1 */
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new Memory_WriteAddress( 0xa000, 0xa3ff, timeplt_colorram_w, timeplt_colorram ),
		new Memory_WriteAddress( 0xa400, 0xa7ff, timeplt_videoram_w, timeplt_videoram ),
		new Memory_WriteAddress( 0xa800, 0xafff, MWA_RAM ),
		new Memory_WriteAddress( 0xb010, 0xb03f, MWA_RAM, spriteram, spriteram_size ),
		new Memory_WriteAddress( 0xb410, 0xb43f, MWA_RAM, spriteram_2 ),
		new Memory_WriteAddress( 0xc000, 0xc000, soundlatch_w ),
		new Memory_WriteAddress( 0xc200, 0xc200, watchdog_reset_w ),
		new Memory_WriteAddress( 0xc300, 0xc300, interrupt_enable_w ),
		new Memory_WriteAddress( 0xc302, 0xc302, timeplt_flipscreen_w ),
		new Memory_WriteAddress( 0xc304, 0xc304, timeplt_sh_irqtrigger_w ),
		new Memory_WriteAddress( 0xc30a, 0xc30c, timeplt_coin_counter_w ),  /* c30b is not used */
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	static InputPortPtr input_ports_timeplt = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( timeplt )
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "4" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_BITX( 0,       0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x08, 0x08, "Bonus" );
		PORT_DIPSETTING(    0x08, "10000 50000" );
		PORT_DIPSETTING(    0x00, "20000 60000" );
		PORT_DIPNAME( 0x70, 0x70, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x70, "1 (Easiest"));
		PORT_DIPSETTING(    0x60, "2" );
		PORT_DIPSETTING(    0x50, "3" );
		PORT_DIPSETTING(    0x40, "4" );
		PORT_DIPSETTING(    0x30, "5 (Average"));
		PORT_DIPSETTING(    0x20, "6" );
		PORT_DIPSETTING(    0x10, "7" );
		PORT_DIPSETTING(    0x00, "8 (Hardest"));
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_psurge = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( psurge )
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();  /* DSW0 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "Initial Energy" );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_DIPSETTING(    0x08, "6" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x30, "3" );
		PORT_DIPSETTING(    0x20, "4" );
		PORT_DIPSETTING(    0x10, "5" );
		PORT_DIPSETTING(    0x00, "6" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
	
		PORT_START();  /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_5C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_5C") );
		PORT_BITX(0x10,     0x10, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Infinite Shots", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "Stop at Junctions" );
		PORT_DIPSETTING(    0x80, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		512,	/* 512 characters */
		2,	/* 2 bits per pixel */
		new int[] { 4, 0 },
		new int[] { 0, 1, 2, 3, 8*8+0,8*8+1,8*8+2,8*8+3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		16*8	/* every char takes 16 consecutive bytes */
	);
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		256,	/* 256 sprites */
		2,	/* 2 bits per pixel */
		new int[] { 4, 0 },
		new int[] { 0, 1, 2, 3,  8*8, 8*8+1, 8*8+2, 8*8+3,
				16*8+0, 16*8+1, 16*8+2, 16*8+3,  24*8+0, 24*8+1, 24*8+2, 24*8+3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				32*8, 33*8, 34*8, 35*8, 36*8, 37*8, 38*8, 39*8 },
		64*8	/* every sprite takes 64 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,        0, 32 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout,   32*4, 64 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static MACHINE_DRIVER_START( timeplt )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 3072000)	/* 3.072 MHz (?) */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_VBLANK_INT(timeplt_interrupt,256)
	
		MDRV_CPU_ADD(Z80,14318180/8)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)	/* 1.789772727 MHz */						\
		MDRV_CPU_MEMORY(timeplt_sound_readmem,timeplt_sound_writemem)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(32)
		MDRV_COLORTABLE_LENGTH(32*4+64*4)
	
		MDRV_PALETTE_INIT(timeplt)
		MDRV_VIDEO_START(timeplt)
		MDRV_VIDEO_UPDATE(timeplt)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, timeplt_ay8910_interface)
	MACHINE_DRIVER_END
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_timeplt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "tm1",          0x0000, 0x2000, CRC(1551f1b9) SHA1(c72f30988ac00cbe6549b71c3bcb414511e8b997) )
		ROM_LOAD( "tm2",          0x2000, 0x2000, CRC(58636cb5) SHA1(ab517efa93ae7be780af55faea82a6e83edd828c) )
		ROM_LOAD( "tm3",          0x4000, 0x2000, CRC(ff4e0d83) SHA1(ef98a1abb45b22d7498a0aca520f43bbee248b22) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "tm7",          0x0000, 0x1000, CRC(d66da813) SHA1(408fca4515e8af84211df3e204c8776b2f8adb23) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "tm6",          0x0000, 0x2000, CRC(c2507f40) SHA1(07221875e3f81d9def67c57a7ccd82d52ce65e01) )
	
		ROM_REGION( 0x4000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "tm4",          0x0000, 0x2000, CRC(7e437c3e) SHA1(cbe2ccd2cd503af62f009cd5aab73aa7366230b1) )
		ROM_LOAD( "tm5",          0x2000, 0x2000, CRC(e8ca87b9) SHA1(5dd30d3fb9fd8cf9e6a8e37e7ea858c7fd038a7e) )
	
		ROM_REGION( 0x0240, REGION_PROMS, 0 )
		ROM_LOAD( "timeplt.b4",   0x0000, 0x0020, CRC(34c91839) SHA1(f62e279e21fce171231d3139be7adabe1f4b8c2e) ) /* palette */
		ROM_LOAD( "timeplt.b5",   0x0020, 0x0020, CRC(463b2b07) SHA1(9ad275365eba4869f94749f39ff8705d92056a10) ) /* palette */
		ROM_LOAD( "timeplt.e9",   0x0040, 0x0100, CRC(4bbb2150) SHA1(678433b21aae1daa938e32d3293eeed529a42ef9) ) /* sprite lookup table */
		ROM_LOAD( "timeplt.e12",  0x0140, 0x0100, CRC(f7b7663e) SHA1(151bd2dff4e4ef76d6438c1ab2cae71f987b9dad) ) /* char lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_timepltc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "cd1y",         0x0000, 0x2000, CRC(83ec72c2) SHA1(f3dbc8362f6bdad1baa65cf5d95611e79de381a4) )
		ROM_LOAD( "cd2y",         0x2000, 0x2000, CRC(0dcf5287) SHA1(c36628367e81ac07f5ace72b45ebb7140b6aa116) )
		ROM_LOAD( "cd3y",         0x4000, 0x2000, CRC(c789b912) SHA1(dead7b20a40769e48738fccc3a17e2266aac445d) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "tm7",          0x0000, 0x1000, CRC(d66da813) SHA1(408fca4515e8af84211df3e204c8776b2f8adb23) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "tm6",          0x0000, 0x2000, CRC(c2507f40) SHA1(07221875e3f81d9def67c57a7ccd82d52ce65e01) )
	
		ROM_REGION( 0x4000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "tm4",          0x0000, 0x2000, CRC(7e437c3e) SHA1(cbe2ccd2cd503af62f009cd5aab73aa7366230b1) )
		ROM_LOAD( "tm5",          0x2000, 0x2000, CRC(e8ca87b9) SHA1(5dd30d3fb9fd8cf9e6a8e37e7ea858c7fd038a7e) )
	
		ROM_REGION( 0x0240, REGION_PROMS, 0 )
		ROM_LOAD( "timeplt.b4",   0x0000, 0x0020, CRC(34c91839) SHA1(f62e279e21fce171231d3139be7adabe1f4b8c2e) ) /* palette */
		ROM_LOAD( "timeplt.b5",   0x0020, 0x0020, CRC(463b2b07) SHA1(9ad275365eba4869f94749f39ff8705d92056a10) ) /* palette */
		ROM_LOAD( "timeplt.e9",   0x0040, 0x0100, CRC(4bbb2150) SHA1(678433b21aae1daa938e32d3293eeed529a42ef9) ) /* sprite lookup table */
		ROM_LOAD( "timeplt.e12",  0x0140, 0x0100, CRC(f7b7663e) SHA1(151bd2dff4e4ef76d6438c1ab2cae71f987b9dad) ) /* char lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_spaceplt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "sp1",          0x0000, 0x2000, CRC(ac8ca3ae) SHA1(9781138becd17aa70e877138e126ebb1fbff6192) )
		ROM_LOAD( "sp2",          0x2000, 0x2000, CRC(1f0308ef) SHA1(dd88378fc4cefe473f310d4730268c98354a4a44) )
		ROM_LOAD( "sp3",          0x4000, 0x2000, CRC(90aeca50) SHA1(9c6fddfeafa84f5284ec8f7c9d46216b110badc1) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "tm7",          0x0000, 0x1000, CRC(d66da813) SHA1(408fca4515e8af84211df3e204c8776b2f8adb23) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "sp6",          0x0000, 0x2000, CRC(76caa8af) SHA1(f81bb73877d415a6587a32bddaad6db8a8fd4941) )
	
		ROM_REGION( 0x4000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "sp4",          0x0000, 0x2000, CRC(3781ce7a) SHA1(68bb73f67494c3b24f7fd0d79153c9793f4b3a5b) )
		ROM_LOAD( "tm5",          0x2000, 0x2000, CRC(e8ca87b9) SHA1(5dd30d3fb9fd8cf9e6a8e37e7ea858c7fd038a7e) )
	
		ROM_REGION( 0x0240, REGION_PROMS, 0 )
		ROM_LOAD( "timeplt.b4",   0x0000, 0x0020, CRC(34c91839) SHA1(f62e279e21fce171231d3139be7adabe1f4b8c2e) ) /* palette */
		ROM_LOAD( "timeplt.b5",   0x0020, 0x0020, CRC(463b2b07) SHA1(9ad275365eba4869f94749f39ff8705d92056a10) ) /* palette */
		ROM_LOAD( "timeplt.e9",   0x0040, 0x0100, CRC(4bbb2150) SHA1(678433b21aae1daa938e32d3293eeed529a42ef9) ) /* sprite lookup table */
		ROM_LOAD( "timeplt.e12",  0x0140, 0x0100, CRC(f7b7663e) SHA1(151bd2dff4e4ef76d6438c1ab2cae71f987b9dad) ) /* char lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_psurge = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "p1",           0x0000, 0x2000, CRC(05f9ba12) SHA1(ad88838d1a0c64830281e425d4ad2498ba959098) )
		ROM_LOAD( "p2",           0x2000, 0x2000, CRC(3ff41576) SHA1(9bdbad31c65dff76942967b5a334407b0326f752) )
		ROM_LOAD( "p3",           0x4000, 0x2000, CRC(e8fe120a) SHA1(b6320c9cb1a67097692aa0de7d88b0dfb63dedd7) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "p6",           0x0000, 0x1000, CRC(b52d01fa) SHA1(9b6cf9ea51d3a87c174f34d42a4b1b5f38b48723) )
		ROM_LOAD( "p7",           0x1000, 0x1000, CRC(9db5c0ce) SHA1(b5bc1d89a7f7d7a0baae64390c37ee11f69a0e76) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "p4",           0x0000, 0x2000, CRC(26fd7f81) SHA1(eb282313a37d7d611bf90f9b0b527adee9ae283f) )
	
		ROM_REGION( 0x4000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "p5",           0x0000, 0x2000, CRC(6066ec8e) SHA1(7f1155cf8a2d63c0740a4b56f1e09e7dfc749302) )
		ROM_LOAD( "tm5",          0x2000, 0x2000, CRC(e8ca87b9) SHA1(5dd30d3fb9fd8cf9e6a8e37e7ea858c7fd038a7e) )
	
		ROM_REGION( 0x0240, REGION_PROMS, 0 )
		ROM_LOAD( "timeplt.b4",   0x0000, 0x0020, BAD_DUMP CRC(34c91839) SHA1(f62e279e21fce171231d3139be7adabe1f4b8c2e)  ) /* palette */
		ROM_LOAD( "timeplt.b5",   0x0020, 0x0020, BAD_DUMP CRC(463b2b07) SHA1(9ad275365eba4869f94749f39ff8705d92056a10)  ) /* palette */
		ROM_LOAD( "timeplt.e9",   0x0040, 0x0100, BAD_DUMP CRC(4bbb2150) SHA1(678433b21aae1daa938e32d3293eeed529a42ef9)  ) /* sprite lookup table */
		ROM_LOAD( "timeplt.e12",  0x0140, 0x0100, BAD_DUMP CRC(f7b7663e) SHA1(151bd2dff4e4ef76d6438c1ab2cae71f987b9dad)  ) /* char lookup table */
	ROM_END(); }}; 
	
	
	
	GAME( 1982, timeplt,  0,       timeplt, timeplt, 0, ROT90,  "Konami", "Time Pilot" )
	GAME( 1982, timepltc, timeplt, timeplt, timeplt, 0, ROT90,  "Konami (Centuri license)", "Time Pilot (Centuri)" )
	GAME( 1982, spaceplt, timeplt, timeplt, timeplt, 0, ROT90,  "bootleg", "Space Pilot" )
	GAME( 1988, psurge,   0,       timeplt, psurge,  0, ROT270, "<unknown>", "Power Surge" )
}
