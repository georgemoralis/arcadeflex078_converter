/***************************************************************************

Flak Attack/MX5000 (Konami GX669)

Driver by:
	Manuel Abadia <manu@teleline.es>

TO DO:
	-What does 0x900X do? (Z80)

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class flkatck
{
	
	/* from vidhrdw/flkatck.c */
	
	
	/***************************************************************************/
	
	public static MachineInitHandlerPtr machine_init_flkatck  = new MachineInitHandlerPtr() { public void handler(){
		K007232_set_bank( 0, 0, 1 );
	} };
	
	public static InterruptHandlerPtr flkatck_interrupt = new InterruptHandlerPtr() {public void handler(){
		if (flkatck_irq_enabled)
			cpu_set_irq_line(0, HD6309_IRQ_LINE, HOLD_LINE);
	} };
	
	public static WriteHandlerPtr flkatck_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		unsigned char *RAM = memory_region(REGION_CPU1);
		int bankaddress = 0;
	
		/* bits 3-4: coin counters */
		coin_counter_w(0,data & 0x08);
		coin_counter_w(1,data & 0x10);
	
		/* bits 0-1: bank # */
		bankaddress += 0x10000 + (data & 0x03)*0x2000;
		if ((data & 0x03) != 0x03)	/* for safety */
			cpu_setbank(1,&RAM[bankaddress]);
	} };
	
	public static ReadHandlerPtr flkatck_ls138_r  = new ReadHandlerPtr() { public int handler(int offset){
		int data = 0;
	
		switch ((offset & 0x1c) >> 2){
			case 0x00:	/* inputs + DIPSW #3 + coinsw */
				if (offset & 0x02)
					data = readinputport(2 + (offset & 0x01));
				else
					data = readinputport(4 + (offset & 0x01));
				break;
			case 0x01:	/* DIPSW #1 & DIPSW #2 */
				if (offset & 0x02)
					data = readinputport(1 - (offset & 0x01));
				break;
		}
	
		return data;
	} };
	
	public static WriteHandlerPtr flkatck_ls138_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		switch ((offset & 0x1c) >> 2){
			case 0x04:	/* bankswitch */
				flkatck_bankswitch_w(0, data);
				break;
			case 0x05:	/* sound code number */
				soundlatch_w.handler(0, data);
				break;
			case 0x06:	/* Cause interrupt on audio CPU */
				cpu_set_irq_line(1,0,HOLD_LINE);
				break;
			case 0x07:	/* watchdog reset */
				watchdog_reset_w(0, data);
				break;
		}
	} };
	
	public static Memory_ReadAddress flkatck_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0400, 0x041f, flkatck_ls138_r ),			/* inputs + DIPS */
		new Memory_ReadAddress( 0x0800, 0x0bff, MRA_RAM ),		/* palette */
		new Memory_ReadAddress( 0x1000, 0x1fff, MRA_RAM ),					/* RAM */
		new Memory_ReadAddress( 0x2000, 0x3fff, MRA_RAM ),		/* Video RAM (007121) */
		new Memory_ReadAddress( 0x4000, 0x5fff, MRA_BANK1 ),					/* banked ROM */
		new Memory_ReadAddress( 0x6000, 0xffff, MRA_ROM ),					/* ROM */
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress flkatck_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x0007, flkatck_k007121_regs_w ), 	/* 007121 registers */
		new Memory_WriteAddress( 0x0400, 0x041f, flkatck_ls138_w ),			/* bankswitch + counters + sound command */
		new Memory_WriteAddress( 0x0800, 0x0bff, paletteram_xBBBBBGGGGGRRRRR_w, paletteram ),/* palette */
		new Memory_WriteAddress( 0x1000, 0x1fff, MWA_RAM ),					/* RAM */
		new Memory_WriteAddress( 0x2000, 0x3fff, flkatck_k007121_w, k007121_ram ),			/* Video RAM (007121) */
		new Memory_WriteAddress( 0x4000, 0x5fff, MWA_BANK1 ),					/* banked ROM */
		new Memory_WriteAddress( 0x6000, 0xffff, MWA_ROM ),					/* ROM */
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress flkatck_readmem_sound[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),				/* ROM */
		new Memory_ReadAddress( 0x8000, 0x87ff, MRA_RAM ),				/* RAM */
		new Memory_ReadAddress( 0x9000, 0x9000, MRA_RAM ),				/* ??? */
		new Memory_ReadAddress( 0x9001, 0x9001, MRA_RAM ),				/* ??? */
		new Memory_ReadAddress( 0x9004, 0x9004, MRA_RAM ),				/* ??? */
		new Memory_ReadAddress( 0xa000, 0xa000, soundlatch_r ),			/* soundlatch_r */
		new Memory_ReadAddress( 0xb000, 0xb00d, K007232_read_port_0_r ),	/* 007232 registers */
		new Memory_ReadAddress( 0xc001, 0xc001, YM2151_status_port_0_r ), /* YM2151 */
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress flkatck_writemem_sound[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),					/* ROM */
		new Memory_WriteAddress( 0x8000, 0x87ff, MWA_RAM ),					/* RAM */
		new Memory_WriteAddress( 0x9000, 0x9000, MWA_RAM ),					/* ??? */
		new Memory_WriteAddress( 0x9001, 0x9001, MWA_RAM ),					/* ??? */
		new Memory_WriteAddress( 0x9006, 0x9006, MWA_RAM ),					/* ??? */
		new Memory_WriteAddress( 0xb000, 0xb00d, K007232_write_port_0_w ), 	/* 007232 registers */
		new Memory_WriteAddress( 0xc000, 0xc000, YM2151_register_port_0_w ),	/* YM2151 */
		new Memory_WriteAddress( 0xc001, 0xc001, YM2151_data_port_0_w ),		/* YM2151 */
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	static InputPortPtr input_ports_flkatck = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( flkatck )
		PORT_START(); 	/* DSW #1 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(	0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(	0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(	0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(	0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(	0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(	0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(	0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(	0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(	0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(	0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(	0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(	0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(	0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(	0x90, DEF_STR( "1C_7C") );
		//PORT_DIPSETTING(	  0x00, "Invalid" );
	
		PORT_START(); 	/* DSW #2 */
		PORT_DIPNAME( 0x03, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x03, "1" );
		PORT_DIPSETTING(	0x02, "2" );
		PORT_DIPSETTING(	0x01, "3" );
		PORT_DIPSETTING(	0x00, "5" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(	0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x10, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x18, "30000 70000" );
		PORT_DIPSETTING(	0x10, "40000 80000" );
		PORT_DIPSETTING(	0x08, "30000" );
		PORT_DIPSETTING(	0x00, "40000" );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x60, "Easy" );
		PORT_DIPSETTING(	0x40, "Normal" );
		PORT_DIPSETTING(	0x20, "Difficult" );
		PORT_DIPSETTING(	0x00, "Very difficult" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW #3 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(	0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Upright Controls" );
		PORT_DIPSETTING(	0x02, "Single" );
		PORT_DIPSETTING(	0x00, "Dual" );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* COINSW & START */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );/* service */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* PLAYER 1 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP	  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* PLAYER 2 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP	  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	static GfxLayout gfxlayout = new GfxLayout
	(
		8,8,
		0x80000/32,
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, gfxlayout, 0, 32 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static struct YM2151interface ym2151_interface =
	{
		1,
		3579545,	/* 3.579545 MHz */
		{ YM3012_VOL(100,MIXER_PAN_LEFT,100,MIXER_PAN_RIGHT) },
		{ 0 },
	};
	
	static void volume_callback0(int v)
	{
		K007232_set_volume(0,0,(v >> 4) * 0x11,0);
		K007232_set_volume(0,1,0,(v & 0x0f) * 0x11);
	}
	
	static struct K007232_interface k007232_interface =
	{
		1,			/* number of chips */
		3579545,	/* clock */
		{ REGION_SOUND1 },		/* memory region */
		{ K007232_VOL(50,MIXER_PAN_CENTER,50,MIXER_PAN_CENTER) },	/* volume */
		{ volume_callback0 }	/* external port callback */
	};
	
	
	static MACHINE_DRIVER_START( flkatck )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(HD6309,3000000) /* HD63C09EP, 24/8 MHz */
		MDRV_CPU_MEMORY(flkatck_readmem,flkatck_writemem)
		MDRV_CPU_VBLANK_INT(flkatck_interrupt,1)
	
		MDRV_CPU_ADD(Z80,3579545)	/* NEC D780C-1, 3.579545 MHz */
		MDRV_CPU_MEMORY(flkatck_readmem_sound,flkatck_writemem_sound)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
		MDRV_INTERLEAVE(10)
	
		MDRV_MACHINE_INIT(flkatck)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(37*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 35*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(512)
	
		MDRV_VIDEO_START(flkatck)
		MDRV_VIDEO_UPDATE(flkatck)
	
		/* sound hardware */
		MDRV_SOUND_ATTRIBUTES(SOUND_SUPPORTS_STEREO)
		MDRV_SOUND_ADD(YM2151, ym2151_interface)
		MDRV_SOUND_ADD(K007232, k007232_interface)
	MACHINE_DRIVER_END
	
	
	
	static RomLoadPtr rom_mx5000 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x18000, REGION_CPU1, 0 )		/* 6309 code */
		ROM_LOAD( "r01",          0x010000, 0x006000, CRC(79b226fc) SHA1(3bc4d93717230fecd54bd08a0c3eeedc1c8f571d) )/* banked ROM */
		ROM_CONTINUE(			  0x006000, 0x00a000 )			/* fixed ROM */
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )		/* 64k for the SOUND CPU */
		ROM_LOAD( "m02.bin",        0x000000, 0x008000, CRC(7e11e6b9) SHA1(7a7d65a458b15842a6345388007c8f682aec20a7) )
	
		ROM_REGION( 0x080000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "mask4m.bin",     0x000000, 0x080000, CRC(ff1d718b) SHA1(d44fe3ed5a3ba1b3036264e37f9cd3500b706635) )/* tiles + sprites */
	
		ROM_REGION( 0x040000, REGION_SOUND1, 0 )	/* 007232 data (chip 1) */
		ROM_LOAD( "mask2m.bin",     0x000000, 0x040000, CRC(6d1ea61c) SHA1(9e6eb9ac61838df6e1f74e74bb72f3edf1274aed) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_flkatck = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x18000, REGION_CPU1, 0 )		/* 6309 code */
		ROM_LOAD( "gx669_p1.16c", 0x010000, 0x006000, CRC(c5cd2807) SHA1(22ddd911a23954ff2d52552e07323f5f0ddaeead) )/* banked ROM */
		ROM_CONTINUE(			  0x006000, 0x00a000 )			/* fixed ROM */
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )		/* 64k for the SOUND CPU */
		ROM_LOAD( "m02.bin",        0x000000, 0x008000, CRC(7e11e6b9) SHA1(7a7d65a458b15842a6345388007c8f682aec20a7) )
	
		ROM_REGION( 0x080000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "mask4m.bin",     0x000000, 0x080000, CRC(ff1d718b) SHA1(d44fe3ed5a3ba1b3036264e37f9cd3500b706635) )/* tiles + sprites */
	
		ROM_REGION( 0x040000, REGION_SOUND1, 0 )	/* 007232 data (chip 1) */
		ROM_LOAD( "mask2m.bin",     0x000000, 0x040000, CRC(6d1ea61c) SHA1(9e6eb9ac61838df6e1f74e74bb72f3edf1274aed) )
	ROM_END(); }}; 
	
	
	
	GAME( 1987, mx5000,  0, 	 flkatck, flkatck, 0, ROT90, "Konami", "MX5000" )
	GAME( 1987, flkatck, mx5000, flkatck, flkatck, 0, ROT90, "Konami", "Flak Attack (Japan)" )
	
}
