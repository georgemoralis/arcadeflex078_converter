/***************************************************************************

	Dead Angle							(c) 1988 Seibu Kaihatsu
	Gang Hunter							(c) 1988 Seibu Kaihatsu

***************************************************************************/

/*

	TODO:

	- ghunter trackball input is broken
	- coin lockouts

*/

/*
 * ported to v0.78
 * using automatic conversion tool v0.02
 */ 
package arcadeflex.v078.drivers;

public class deadang
{
	
	
	static UINT8 *deadang_shared_ram;
	
	
	
	/* Read/Write Handlers */
	
	public static ReadHandlerPtr deadang_shared_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return deadang_shared_ram[offset];
	} };
	
	public static WriteHandlerPtr deadang_shared_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		deadang_shared_ram[offset] = data;
	} };
	
	public static ReadHandlerPtr ghunter_trackball_low_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 0:	return (readinputport(5) & 0xff);
			case 1: return (readinputport(6) & 0xff);
		}
	
		return 0;
	} };
	public static ReadHandlerPtr ghunter_trackball_high_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 0:	return (readinputport(5) & 0xff00) >> 4;
			case 1: return (readinputport(6) & 0xff00) >> 4;
		}
	
		return 0;
	} };
	
	/* Memory Maps */
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x00000, 0x03fff, MRA_RAM ),
		new Memory_ReadAddress( 0x04000, 0x04fff, deadang_shared_r ),
		new Memory_ReadAddress( 0x06000, 0x0600f, seibu_main_v30_r ),
		new Memory_ReadAddress( 0x0a000, 0x0a000, input_port_1_r ),
		new Memory_ReadAddress( 0x0a001, 0x0a001, input_port_2_r ),
		new Memory_ReadAddress( 0x0a002, 0x0a002, input_port_3_r ),
		new Memory_ReadAddress( 0x0a003, 0x0a003, input_port_4_r ),
		new Memory_ReadAddress( 0xc0000, 0xfffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x00000, 0x037ff, MWA_RAM ),
		new Memory_WriteAddress( 0x03800, 0x03fff, MWA_RAM, spriteram ),
		new Memory_WriteAddress( 0x04000, 0x04fff, deadang_shared_w, deadang_shared_ram ),
		new Memory_WriteAddress( 0x05000, 0x05fff, MWA_RAM ),
		new Memory_WriteAddress( 0x06000, 0x0600f, seibu_main_v30_w ),
		new Memory_WriteAddress( 0x06010, 0x07fff, MWA_RAM ),
		new Memory_WriteAddress( 0x08000, 0x087ff, deadang_text_w, videoram ),
		new Memory_WriteAddress( 0x08800, 0x0bfff, MWA_RAM ),
		new Memory_WriteAddress( 0x0c000, 0x0cfff, paletteram_xxxxBBBBGGGGRRRR_w, paletteram ),
		new Memory_WriteAddress( 0x0d000, 0x0dfff, MWA_RAM ),
		new Memory_WriteAddress( 0x0e000, 0x0e0ff, MWA_RAM, deadang_scroll_ram ),
		new Memory_WriteAddress( 0x0e100, 0x0ffff, MWA_RAM ),
		new Memory_WriteAddress( 0xc0000, 0xfffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress sub_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x00000, 0x037ff, MRA_RAM ),
		new Memory_ReadAddress( 0x03800, 0x03fff, MRA_RAM ),
		new Memory_ReadAddress( 0x04000, 0x04fff, deadang_shared_r ),
		new Memory_ReadAddress( 0xe0000, 0xfffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress sub_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x00000, 0x037ff, MWA_RAM ),
		new Memory_WriteAddress( 0x03800, 0x03fff, deadang_foreground_w, deadang_video_data ),
		new Memory_WriteAddress( 0x04000, 0x04fff, deadang_shared_w ),
		new Memory_WriteAddress( 0x08000, 0x08000, deadang_bank_w ),
		new Memory_WriteAddress( 0x0c000, 0x0c000, watchdog_reset_w ),
		new Memory_WriteAddress( 0xe0000, 0xfffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	/* Input Ports */
	
	static InputPortPtr input_ports_deadang = new InputPortPtr(){ public void handler() { 
		SEIBU_COIN_INPUTS	/* Must be port 0: coin inputs read through sound cpu */
	
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 	/* Dip switch A */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_5C") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_SERVICE( 0x20, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Cabinet"));
		PORT_DIPSETTING(    0x80, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
	
		PORT_START(); 	/* Dip switch B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Normal" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x08, "20K 50K" );
		PORT_DIPSETTING(    0x0c, "30K 100K" );
		PORT_DIPSETTING(    0x04, "50K 150K" );
		PORT_DIPSETTING(    0x00, "100K 200K" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "1" );
		PORT_DIPSETTING(    0x10, "2" );
		PORT_DIPSETTING(    0x30, "3" );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "User Mode" );
		PORT_DIPSETTING(    0x00, "Japan" );
		PORT_DIPSETTING(    0x80, "Overseas" );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_ghunter = new InputPortPtr(){ public void handler() { 
		SEIBU_COIN_INPUTS	/* Must be port 0: coin inputs read through sound cpu */
	
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 	/* Dip switch A */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_5C") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_SERVICE( 0x20, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Cabinet"));
		PORT_DIPSETTING(    0x80, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
	
		PORT_START(); 	/* Dip switch B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Normal" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x04, "50K 150K" );
		PORT_DIPSETTING(    0x00, "100K 200K" );
		PORT_DIPNAME( 0x08, 0x08, "Controller" );
		PORT_DIPSETTING(    0x08, "Trackball" );
		PORT_DIPSETTING(    0x00, "Joystick" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "1" );
		PORT_DIPSETTING(    0x10, "2" );
		PORT_DIPSETTING(    0x30, "3" );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "User Mode" );
		PORT_DIPSETTING(    0x00, "Japan" );
		PORT_DIPSETTING(    0x80, "Overseas" );
	
		PORT_START(); 
		PORT_ANALOG( 0x0fff, 0x0000, IPT_TRACKBALL_X | IPF_PLAYER1, 100, 30, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0x0fff, 0x0000, IPT_TRACKBALL_Y | IPF_PLAYER1, 100, 30, 0, 0 );
	INPUT_PORTS_END(); }}; 
	
	/* Graphics Layouts */
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,		/* 8*8 characters */
		RGN_FRAC(1,2),
		4,			/* 4 bits per pixel */
		new int[] { 0x4000*8, 0x4000*8+4, 0, 4 },
		new int[] { 3,2,1,0,11,10,9,8 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		128
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 tiles */
		RGN_FRAC(1,1),
		4,		/* 4 bits per pixel */
		new int[] { 8,12,0,4},
		new int[] { 3,2,1,0, 19,18,17,16, 512+3,512+2,512+1,512+0, 512+11+8,512+10+8,512+9+8,512+8+8},
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
		8*32, 9*32, 10*32, 11*32, 12*32, 13*32, 14*32, 15*32,
		},
	 	1024
	);
	
	/* Graphics Decode Information */
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x000000, charlayout,    512, 16 ),
		new GfxDecodeInfo( REGION_GFX2, 0x000000, spritelayout,  768, 16 ),
		new GfxDecodeInfo( REGION_GFX3, 0x000000, spritelayout, 1024, 16 ),
		new GfxDecodeInfo( REGION_GFX4, 0x000000, spritelayout,  256, 16 ),
		new GfxDecodeInfo( REGION_GFX5, 0x000000, spritelayout,    0, 16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/* Sound Interfaces */
	
	SEIBU_SOUND_SYSTEM_YM2203_HARDWARE(14318180/4)
	
	SEIBU_SOUND_SYSTEM_ADPCM_HARDWARE
	
	/* Interrupt Generators */
	
	public static InterruptHandlerPtr deadang_interrupt = new InterruptHandlerPtr() {public void handler()
	{
		if (cpu_getiloops() != 0)
			cpu_set_irq_line_and_vector(cpu_getactivecpu(), 0, HOLD_LINE, 0xc8/4);	/* VBL */
		else
			cpu_set_irq_line_and_vector(cpu_getactivecpu(), 0, HOLD_LINE, 0xc4/4);	/* VBL */
	} };
	
	/* Machine Drivers */
	
	static MACHINE_DRIVER_START( deadang )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(V20,16000000/2) /* Sony 8623h9 CXQ70116D-8 (V20 compatible) */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_VBLANK_INT(deadang_interrupt,2)
	
		MDRV_CPU_ADD(V20,16000000/2) /* Sony 8623h9 CXQ70116D-8 (V20 compatible) */
		MDRV_CPU_MEMORY(sub_readmem,sub_writemem)
		MDRV_CPU_VBLANK_INT(deadang_interrupt,2)
	
		SEIBU3A_SOUND_SYSTEM_CPU(14318180/4)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_INTERLEAVE(200)
	
		MDRV_MACHINE_INIT(seibu_sound_2)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_BUFFERS_SPRITERAM)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(2048)
	
		MDRV_VIDEO_START(deadang)
		MDRV_VIDEO_UPDATE(deadang)
	
		/* sound hardware */
		SEIBU_SOUND_SYSTEM_YM2203_INTERFACE
		SEIBU_SOUND_SYSTEM_ADPCM_INTERFACE
	MACHINE_DRIVER_END
	
	/* ROMs */
	
	static RomLoadPtr rom_deadang = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 ) /* v20 main cpu */
		ROM_LOAD16_BYTE("2.18h",   0x0c0000, 0x10000, CRC(1bc05b7e) SHA1(21833150a1f5ab543999a67f5b3bfbaf703e5508) )
		ROM_LOAD16_BYTE("4.22h",   0x0c0001, 0x10000, CRC(5751d4e7) SHA1(2e1a30c20199461fd876849f7563fef1d9a80c2d) )
		ROM_LOAD16_BYTE("1.18f",   0x0e0000, 0x10000, CRC(8e7b15cc) SHA1(7e4766953c1adf04be18207a2aa6f5e861ea5f6c) )
		ROM_LOAD16_BYTE("3.21f",   0x0e0001, 0x10000, CRC(e784b1fa) SHA1(3f41d31e0b36b9a2fab5e9998bb4146dfa0a97eb) )
	
		ROM_REGION( 0x100000, REGION_CPU2, 0 ) /* v20 sub cpu */
		ROM_LOAD16_BYTE("5.6bh",   0x0e0000, 0x10000, CRC(9c69eb35) SHA1(d5a9714f279b71c419b4bae0f142c4cb1cc8d30e) )
		ROM_LOAD16_BYTE("6.9b",    0x0e0001, 0x10000, CRC(34a44ce5) SHA1(621c69d8778d4c96ac3be06b033a5931a6a23da2) )
	
		ROM_REGION( 0x20000*2, REGION_CPU3, 0 ) /* sound Z80 */
		ROM_LOAD( "13.b1", 0x000000, 0x02000, CRC(13b956fb) SHA1(f7c21ad5e988ac59073659a427b1fa66ff49b0c1) ) /* Encrypted */
		ROM_LOAD( "14.c1", 0x010000, 0x10000, CRC(98837d57) SHA1(291769a11478291a65c959d119d19960b100d135) ) /* Banked */
	
		ROM_REGION( 0x08000, REGION_GFX1, ROMREGION_DISPOSE ) /* Chars */
		ROM_LOAD( "7.21j", 0x000000, 0x4000, CRC(fe615fcd) SHA1(d67ee5e877b937173f4c188829d5bcbd354ceb29) )
		ROM_LOAD( "8.21l", 0x004000, 0x4000, CRC(905d6b27) SHA1(952f1879e6c27dc87234a4dc572e0453dc2d59fa) )
	
		ROM_REGION( 0x80000, REGION_GFX2, ROMREGION_DISPOSE ) /* Sprites */
		ROM_LOAD( "l12", 0x000000, 0x80000, CRC(c94d5cd2) SHA1(25ded13faaed90886c9fe40f85969dab2f511e31) )
	
		ROM_REGION( 0x100000, REGION_GFX3, ROMREGION_DISPOSE ) /* pf1 layer */
		ROM_LOAD( "16n", 0x000000, 0x80000, CRC(fd70e1a5) SHA1(c3d1233f4dfe08f686ec99a556889f9ed6a21da3) ) // bank 0 (0x1000 tiles)
		ROM_LOAD( "16r", 0x080000, 0x80000, CRC(92f5e382) SHA1(2097b9e9bf3cd37c8613847e7aed677b5aeab7f9) ) // bank 1 (0x1000 tiles)
	
		ROM_REGION( 0x40000, REGION_GFX4, ROMREGION_DISPOSE ) // pf2 layer
		ROM_LOAD( "11m", 0x000000, 0x40000, CRC(a366659a) SHA1(e2fcd82b0b2d4e3adcdf50c710984907d26acd04) ) // fixed (0x800 tiles)
	
		ROM_REGION( 0x40000, REGION_GFX5, ROMREGION_DISPOSE ) // pf3 layer
		ROM_LOAD( "11k", 0x000000, 0x40000, CRC(9cf5bcc7) SHA1(cf96592e601fc373b1bf322d9b576668799130a5) ) // fixed (0x800 tiles)
	
		ROM_REGION( 0x10000, REGION_GFX6, 0 )	/* background map data */
		ROM_LOAD16_BYTE( "10.6l",  0x00000, 0x8000, CRC(ca99176b) SHA1(283e3769a1ff579c78a008b65cb8267e5770ba1f) )
		ROM_LOAD16_BYTE( "9.6m",   0x00001, 0x8000, CRC(51d868ca) SHA1(3e9a4e6bc4bc68773c4ba18c5f4110e6c595d0c9) )
	
		ROM_REGION( 0x10000, REGION_GFX7, 0 )	/* background map data */
		ROM_LOAD16_BYTE( "12.6j",  0x00000, 0x8000, CRC(2674d23f) SHA1(0533d80a23d917e20a703aeb833dcaccfa3a1967) )
		ROM_LOAD16_BYTE( "11.6k",  0x00001, 0x8000, CRC(3dd4d81d) SHA1(94f0a13a8d3812f6879819ca186abf3a8665f7cb) )
	
		ROM_REGION( 0x20000, REGION_SOUND1, 0 )
		ROM_LOAD( "15.b11", 0x000000, 0x10000, CRC(fabd74f2) SHA1(ac70e952a8b38287613b384cdc7ca00a7f155a13) )
		ROM_LOAD( "16.11a", 0x010000, 0x10000, CRC(a8d46fc9) SHA1(3ba51bdec4057413396a152b35015f9d95253e3f) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ghunter = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 ) /* v20 main cpu */
		ROM_LOAD16_BYTE("ggh-2.h18",   0x0c0000, 0x10000, CRC(7ccc6fee) SHA1(bccc283d82f080157f0521457b04fdd1d63caafe) )
		ROM_LOAD16_BYTE("ggh-4.h22",   0x0c0001, 0x10000, CRC(d1f23ad7) SHA1(2668729af797ccab52ac2bf519d43ab2fa9e54ce) )
		ROM_LOAD16_BYTE("ggh-1.f18",   0x0e0000, 0x10000, CRC(0d6ff111) SHA1(209d26170446b43d1d463737b447e30aaca614a7) )
		ROM_LOAD16_BYTE("ggh-3.f22",   0x0e0001, 0x10000, CRC(66dec38d) SHA1(78dd3143265c3da90d1a0ab2c4f42b4e32716af8) )
	
		ROM_REGION( 0x100000, REGION_CPU2, 0 ) /* v20 sub cpu */
		ROM_LOAD16_BYTE("ggh-5.b6",   0x0e0000, 0x10000, CRC(1f612f3b) SHA1(71840fa0e988828a819d371f082ce31d5a5e3a30) )
		ROM_LOAD16_BYTE("ggh-6.b10",  0x0e0001, 0x10000, CRC(63e18e56) SHA1(5183d0909a7c795e76540723fb710a5a75730298) )
	
		ROM_REGION( 0x20000*2, REGION_CPU3, 0 ) /* sound Z80 */
		ROM_LOAD( "13.b1", 0x000000, 0x02000, CRC(13b956fb) SHA1(f7c21ad5e988ac59073659a427b1fa66ff49b0c1) ) /* Encrypted */
		ROM_LOAD( "14.c1", 0x010000, 0x10000, CRC(98837d57) SHA1(291769a11478291a65c959d119d19960b100d135) ) /* Banked */
	
		ROM_REGION( 0x08000, REGION_GFX1, ROMREGION_DISPOSE ) /* Chars */
		ROM_LOAD( "7.21j", 0x000000, 0x4000, CRC(fe615fcd) SHA1(d67ee5e877b937173f4c188829d5bcbd354ceb29) )
		ROM_LOAD( "8.21l", 0x004000, 0x4000, CRC(905d6b27) SHA1(952f1879e6c27dc87234a4dc572e0453dc2d59fa) )
	
		ROM_REGION( 0x80000, REGION_GFX2, ROMREGION_DISPOSE ) /* Sprites */
		ROM_LOAD( "l12", 0x000000, 0x80000, CRC(c94d5cd2) SHA1(25ded13faaed90886c9fe40f85969dab2f511e31) )
	
		ROM_REGION( 0x100000, REGION_GFX3, ROMREGION_DISPOSE ) /* pf1 layer */
		ROM_LOAD( "16n", 0x000000, 0x80000, CRC(fd70e1a5) SHA1(c3d1233f4dfe08f686ec99a556889f9ed6a21da3) ) // bank 0 (0x1000 tiles)
		ROM_LOAD( "16r", 0x080000, 0x80000, CRC(92f5e382) SHA1(2097b9e9bf3cd37c8613847e7aed677b5aeab7f9) ) // bank 1 (0x1000 tiles)
	
		ROM_REGION( 0x40000, REGION_GFX4, ROMREGION_DISPOSE ) // pf2 layer
		ROM_LOAD( "11m", 0x000000, 0x40000, CRC(a366659a) SHA1(e2fcd82b0b2d4e3adcdf50c710984907d26acd04) ) // fixed (0x800 tiles)
	
		ROM_REGION( 0x40000, REGION_GFX5, ROMREGION_DISPOSE ) // pf3 layer
		ROM_LOAD( "11k", 0x000000, 0x40000, CRC(9cf5bcc7) SHA1(cf96592e601fc373b1bf322d9b576668799130a5) ) // fixed (0x800 tiles)
	
		ROM_REGION( 0x10000, REGION_GFX6, 0 )	/* background map data */
		ROM_LOAD16_BYTE( "10.6l",  0x00000, 0x8000, CRC(ca99176b) SHA1(283e3769a1ff579c78a008b65cb8267e5770ba1f) )
		ROM_LOAD16_BYTE( "9.6m",   0x00001, 0x8000, CRC(51d868ca) SHA1(3e9a4e6bc4bc68773c4ba18c5f4110e6c595d0c9) )
	
		ROM_REGION( 0x10000, REGION_GFX7, 0 )	/* background map data */
		ROM_LOAD16_BYTE( "12.6j",  0x00000, 0x8000, CRC(2674d23f) SHA1(0533d80a23d917e20a703aeb833dcaccfa3a1967) )
		ROM_LOAD16_BYTE( "11.6k",  0x00001, 0x8000, CRC(3dd4d81d) SHA1(94f0a13a8d3812f6879819ca186abf3a8665f7cb) )
	
		ROM_REGION( 0x20000, REGION_SOUND1, 0 )
		ROM_LOAD( "15.b11", 0x000000, 0x10000, CRC(fabd74f2) SHA1(ac70e952a8b38287613b384cdc7ca00a7f155a13) )
		ROM_LOAD( "16.11a", 0x010000, 0x10000, CRC(a8d46fc9) SHA1(3ba51bdec4057413396a152b35015f9d95253e3f) )
	ROM_END(); }}; 
	
	/* Driver Initialization */
	
	public static DriverInitHandlerPtr init_deadang  = new DriverInitHandlerPtr() { public void handler()
	{
		seibu_sound_decrypt(REGION_CPU3, 0x2000);
		seibu_adpcm_decrypt(REGION_SOUND1);
	} };
	
	public static DriverInitHandlerPtr init_ghunter  = new DriverInitHandlerPtr() { public void handler()
	{
		seibu_sound_decrypt(REGION_CPU3, 0x2000);
		seibu_adpcm_decrypt(REGION_SOUND1);
	
		install_mem_read_handler(0, 0x80000, 0x80001, ghunter_trackball_low_r);
		install_mem_read_handler(0, 0xb0000, 0xb0001, ghunter_trackball_high_r);
	} };
	
	/* Game Drivers */
	
	public static GameDriver driver_deadang	   = new GameDriver("1988"	,"deadang"	,"deadang.java"	,rom_deadang,null	,machine_driver_deadang	,input_ports_deadang	,init_deadang	,ROT0	,	"Seibu Kaihatsu", "Dead Angle" )
	public static GameDriver driver_ghunter	   = new GameDriver("1988"	,"ghunter"	,"deadang.java"	,rom_ghunter,driver_deadang	,machine_driver_deadang	,input_ports_ghunter	,init_ghunter	,ROT0	,	"Seibu Kaihatsu (Segasa/Sonic license)", "Gang Hunter (Spain)" )
}
