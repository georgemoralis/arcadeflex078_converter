/****************************************************************************

Safari Rally by SNK/Taito

Driver by Zsolt Vasvari


This hardware is a precursor to Phoenix.

----------------------------------

CPU board

76477        18MHz

              8080

Video board


 RL07  2114
       2114
       2114
       2114
       2114           RL01 RL02
       2114           RL03 RL04
       2114           RL05 RL06
 RL08  2114

11MHz

----------------------------------

TODO:

- colors (8 colors originally, see game flyer screen shots)
- SN76477 sound

****************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class safarir
{
	
	
	UINT8 *safarir_ram1, *safarir_ram2;
	size_t safarir_ram_size;
	
	static UINT8 *safarir_ram;
	
	static struct tilemap *bg_tilemap, *fg_tilemap;
	
	
	public static WriteHandlerPtr safarir_ram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (safarir_ram[offset] != data)
		{
			safarir_ram[offset] = data;
	
			if (offset < 0x400)
			{
				tilemap_mark_tile_dirty(fg_tilemap, offset);
			} 
			else
			{
				tilemap_mark_tile_dirty(bg_tilemap, offset - 0x400);
			}
		}
	} };
	
	public static ReadHandlerPtr safarir_ram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return safarir_ram[offset];
	} };
	
	public static WriteHandlerPtr safarir_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		tilemap_set_scrollx(bg_tilemap, 0, data);
	} };
	
	public static WriteHandlerPtr safarir_ram_bank_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		safarir_ram = data ? safarir_ram1 : safarir_ram2;
		tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = safarir_ram[tile_index + 0x400];
	
		SET_TILE_INFO(0, code & 0x7f, code >> 7, 0)
	} };
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = safarir_ram[tile_index];
		int flags = ((tile_index & 0x1d) && (tile_index & 0x1e)) ? 0 : TILE_IGNORE_TRANSPARENCY;
	
		SET_TILE_INFO(1, code & 0x7f, code >> 7, flags)
	} };
	
	public static VideoStartHandlerPtr video_start_safarir  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if (!bg_tilemap)
			return 1;
	
		fg_tilemap = tilemap_create(get_fg_tile_info, tilemap_scan_rows, 
			TILEMAP_TRANSPARENT, 8, 8, 32, 32);
	
		if (!fg_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap, 0);
	
		return 0;
	} };
	
	public static VideoUpdateHandlerPtr video_update_safarir  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		tilemap_draw(bitmap, Machine.visible_area, fg_tilemap, 0, 0);
	} };
	
	
	static unsigned short colortable_source[] =
	{
		0x00, 0x01,
		0x00, 0x02,
		0x00, 0x03,
		0x00, 0x04,
		0x00, 0x05,
		0x00, 0x06,
		0x00, 0x07,
	};
	
	public static PaletteInitHandlerPtr palette_init_safarir  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		palette_set_color(0, 0x00, 0x00, 0x00);
		palette_set_color(1, 0x80, 0x80, 0x80);
		palette_set_color(2, 0xff, 0xff, 0xff);
		
		palette_set_color(3, 0x00, 0x00, 0x00);
		palette_set_color(4, 0x00, 0x00, 0x00);
		palette_set_color(5, 0x00, 0x00, 0x00);
		palette_set_color(6, 0x00, 0x00, 0x00);
		palette_set_color(7, 0x00, 0x00, 0x00);
	
		memcpy(colortable, colortable_source, sizeof(colortable_source));
	} };
	
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x17ff, MRA_ROM ),
		new Memory_ReadAddress( 0x2000, 0x27ff, safarir_ram_r ),
		new Memory_ReadAddress( 0x3800, 0x38ff, input_port_0_r ),
		new Memory_ReadAddress( 0x3c00, 0x3cff, input_port_1_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x17ff, MWA_ROM ),
		new Memory_WriteAddress( 0x2000, 0x27ff, safarir_ram_w, safarir_ram1, safarir_ram_size ),
		new Memory_WriteAddress( 0x2800, 0x28ff, safarir_ram_bank_w ),
		new Memory_WriteAddress( 0x2c00, 0x2cff, safarir_scroll_w ),
		new Memory_WriteAddress( 0x3000, 0x30ff, MWA_NOP ),	/* goes to SN76477 */
	
		new Memory_WriteAddress( 0x8000, 0x87ff, MWA_NOP, safarir_ram2 ),	/* only here to initialize pointer */
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	static InputPortPtr input_ports_safarir = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( safarir )
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_2WAY );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_DIPSETTING(    0x03, "6" );
		PORT_DIPNAME( 0x0c, 0x04, "Acceleration Rate" );
		PORT_DIPSETTING(    0x00, "Slowest" );
		PORT_DIPSETTING(    0x04, "Slow" );
		PORT_DIPSETTING(    0x08, "Fast" );
		PORT_DIPSETTING(    0x0c, "Fastest" );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x60, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "3000" );
		PORT_DIPSETTING(    0x20, "5000" );
		PORT_DIPSETTING(    0x40, "7000" );
		PORT_DIPSETTING(    0x60, "9000" );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_VBLANK );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 chars */
		128,	/* 128 characters */
		1,		/* 1 bit per pixel */
		new int[] { 0 },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout, 0, 2 ),
		new GfxDecodeInfo( REGION_GFX2, 0, charlayout, 0, 2 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/* the following is copied from spaceinv */
	struct SN76477interface safarir_sn76477_interface =
	{
		1,	/* 1 chip */
		{ 25 },  /* mixing level   pin description		 */
		{ 0	/* N/C */},		/*	4  noise_res		 */
		{ 0	/* N/C */},		/*	5  filter_res		 */
		{ 0	/* N/C */},		/*	6  filter_cap		 */
		{ 0	/* N/C */},		/*	7  decay_res		 */
		{ 0	/* N/C */},		/*	8  attack_decay_cap  */
		{ RES_K(100) },		/* 10  attack_res		 */
		{ RES_K(56)  },		/* 11  amplitude_res	 */
		{ RES_K(10)  },		/* 12  feedback_res 	 */
		{ 0	/* N/C */},		/* 16  vco_voltage		 */
		{ CAP_U(0.1) },		/* 17  vco_cap			 */
		{ RES_K(8.2) },		/* 18  vco_res			 */
		{ 5.0		 },		/* 19  pitch_voltage	 */
		{ RES_K(120) },		/* 20  slf_res			 */
		{ CAP_U(1.0) },		/* 21  slf_cap			 */
		{ 0	/* N/C */},		/* 23  oneshot_cap		 */
		{ 0	/* N/C */}		/* 24  oneshot_res		 */
	};
	
	static MACHINE_DRIVER_START( safarir )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(8080, 3072000)	/* 3 MHz ? */
		MDRV_CPU_MEMORY(readmem, writemem)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 0*8, 26*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(8)
		MDRV_COLORTABLE_LENGTH(2*7)
	
		MDRV_PALETTE_INIT(safarir)
		MDRV_VIDEO_START(safarir)
		MDRV_VIDEO_UPDATE(safarir)
	
		/* sound hardware */
		MDRV_SOUND_ADD(SN76477, safarir_sn76477_interface)
	MACHINE_DRIVER_END
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_safarir = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )     /* 64k for main CPU */
		ROM_LOAD( "rl01",		0x0000, 0x0400, CRC(cf7703c9) SHA1(b4182df9332b355edaa518462217a6e31e1c07b2) )
		ROM_LOAD( "rl02",		0x0400, 0x0400, CRC(1013ecd3) SHA1(2fe367db8ca367b36c5378cb7d5ff918db243c78) )
		ROM_LOAD( "rl03",		0x0800, 0x0400, CRC(84545894) SHA1(377494ceeac5ad58b70f77b2b27b609491cb7ffd) )
		ROM_LOAD( "rl04",		0x0c00, 0x0400, CRC(5dd12f96) SHA1(a80ac0705648f0807ea33e444fdbea450bf23f85) )
		ROM_LOAD( "rl05",		0x1000, 0x0400, CRC(935ed469) SHA1(052a59df831dcc2c618e9e5e5fdfa47548550596) )
		ROM_LOAD( "rl06",		0x1400, 0x0400, CRC(24c1cd42) SHA1(fe32ecea77a3777f8137ca248b8f371db37b8b85) )
	
		ROM_REGION( 0x0400, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "rl08",		0x0000, 0x0400, CRC(d6a50aac) SHA1(0a0c2cefc556e4e15085318fcac485b82bac2416) )
	
		ROM_REGION( 0x0400, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "rl07",		0x0000, 0x0400, CRC(ba525203) SHA1(1c261cc1259787a7a248766264fefe140226e465) )
	ROM_END(); }}; 
	
	
	public static DriverInitHandlerPtr init_safarir  = new DriverInitHandlerPtr() { public void handler(){
		safarir_ram = safarir_ram1;
	} };
	
	
	GAMEX( 1979, safarir, 0, safarir, safarir, safarir, ROT90, "SNK", "Safari Rally (Japan)", GAME_NO_SOUND | GAME_WRONG_COLORS )
}
