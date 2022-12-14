/***************************************************************************

Atari Sky Raider driver

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class skyraid
{
	
	
	
	
	static int analog_range;
	static int analog_offset;
	
	
	public static PaletteInitHandlerPtr palette_init_skyraid  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		palette_set_color( 0, 0x00, 0x00, 0x00);	/* terrain */
		palette_set_color( 1, 0x18, 0x18, 0x18);
		palette_set_color( 2, 0x30, 0x30, 0x30);
		palette_set_color( 3, 0x48, 0x48, 0x48);
		palette_set_color( 4, 0x60, 0x60, 0x60);
		palette_set_color( 5, 0x78, 0x78, 0x78);
		palette_set_color( 6, 0x90, 0x90, 0x90);
		palette_set_color( 7, 0xA8, 0xA8, 0xA8);
		palette_set_color( 8, 0x10, 0x10, 0x10);	/* sprites */
		palette_set_color( 9, 0xE0, 0xE0, 0xE0);
		palette_set_color(10, 0xA0, 0xA0, 0xA0);
		palette_set_color(11, 0x48, 0x48, 0x48);
		palette_set_color(12, 0x10, 0x10, 0x10);
		palette_set_color(13, 0x48, 0x48, 0x48);
		palette_set_color(14, 0xA0, 0xA0, 0xA0);
		palette_set_color(15, 0xE0, 0xE0, 0xE0);
		palette_set_color(16, 0x00, 0x00, 0x00);	/* missiles */
		palette_set_color(17, 0xFF, 0xFF, 0xFF);
		palette_set_color(18, 0x00, 0x00, 0x00);	/* text */
		palette_set_color(19, 0xE0, 0xE0, 0xE0);
	} };
	
	
	public static ReadHandlerPtr skyraid_zeropage_r  = new ReadHandlerPtr() { public int handler(int offset){
		return memory_region(REGION_CPU1)[offset & 0xff];
	} };
	
	
	public static ReadHandlerPtr skyraid_alpha_num_r  = new ReadHandlerPtr() { public int handler(int offset){
		return skyraid_alpha_num_ram[offset & 0x7f];
	} };
	
	
	public static ReadHandlerPtr skyraid_port_0_r  = new ReadHandlerPtr() { public int handler(int offset){
		UINT8 val = readinputport(0);
	
		if (readinputport(4) > analog_range)
			val |= 0x40;
		if (readinputport(5) > analog_range)
			val |= 0x80;
	
		return val;
	} };
	
	
	public static WriteHandlerPtr skyraid_zeropage_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		memory_region(REGION_CPU1)[offset & 0xff] = data;
	} };
	
	
	public static WriteHandlerPtr skyraid_alpha_num_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		skyraid_alpha_num_ram[offset & 0x7f] = data;
	} };
	
	
	public static WriteHandlerPtr skyraid_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* BIT0 => PLANE SWEEP */
		/* BIT1 => MISSILE     */
		/* BIT2 => EXPLOSION   */
		/* BIT3 => START LAMP  */
		/* BIT4 => PLANE ON    */
		/* BIT5 => ATTRACT     */
	
		set_led_status(0, !(data & 0x08));
	} };
	
	
	public static WriteHandlerPtr skyraid_range_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		analog_range = data & 0x3f;
	} };
	
	
	public static WriteHandlerPtr skyraid_offset_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		analog_offset = data & 0x3f;
	} };
	
	
	public static WriteHandlerPtr skyraid_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		skyraid_scroll = data;
	} };
	
	
	public static Memory_ReadAddress skyraid_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x00ff, MRA_RAM ),
		new Memory_ReadAddress( 0x0100, 0x03ff, skyraid_zeropage_r ),
		new Memory_ReadAddress( 0x0800, 0x087f, MRA_RAM ),
		new Memory_ReadAddress( 0x0880, 0x0bff, skyraid_alpha_num_r ),
		new Memory_ReadAddress( 0x1000, 0x1000, skyraid_port_0_r ),
		new Memory_ReadAddress( 0x1000, 0x1001, input_port_1_r ),
		new Memory_ReadAddress( 0x1400, 0x1400, input_port_2_r ),
		new Memory_ReadAddress( 0x1400, 0x1401, input_port_3_r ),
		new Memory_ReadAddress( 0x7000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress skyraid_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x00ff, MWA_RAM ),
		new Memory_WriteAddress( 0x0100, 0x03ff, skyraid_zeropage_w ),
		new Memory_WriteAddress( 0x0400, 0x040f, MWA_RAM, skyraid_pos_ram ),
		new Memory_WriteAddress( 0x0800, 0x087f, MWA_RAM, skyraid_alpha_num_ram ),
		new Memory_WriteAddress( 0x0880, 0x0bff, skyraid_alpha_num_w ),
		new Memory_WriteAddress( 0x1c00, 0x1c0f, MWA_RAM, skyraid_obj_ram ),
		new Memory_WriteAddress( 0x4000, 0x4000, skyraid_scroll_w ),
		new Memory_WriteAddress( 0x4400, 0x4400, skyraid_sound_w ),
		new Memory_WriteAddress( 0x4800, 0x4800, skyraid_range_w ),
		new Memory_WriteAddress( 0x5000, 0x5000, watchdog_reset_w ),
		new Memory_WriteAddress( 0x5800, 0x5800, skyraid_offset_w ),
		new Memory_WriteAddress( 0x7000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	static InputPortPtr input_ports_skyraid = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( skyraid )
		PORT_START(); 
		PORT_DIPNAME( 0x30, 0x00, "Language" );
		PORT_DIPSETTING(    0x00, "English" );
		PORT_DIPSETTING(    0x10, "French" );
		PORT_DIPSETTING(    0x20, "German" );
		PORT_DIPSETTING(    0x30, "Spanish" );
		PORT_BIT (0x40, IP_ACTIVE_HIGH, IPT_UNUSED);/* POT1 */
		PORT_BIT (0x80, IP_ACTIVE_HIGH, IPT_UNUSED);/* POT0 */
	
		PORT_START(); 
		PORT_DIPNAME( 0x30, 0x10, "Play Time" );
		PORT_DIPSETTING(    0x00, "60 Seconds" );
		PORT_DIPSETTING(    0x10, "80 Seconds" );
		PORT_DIPSETTING(    0x20, "100 Seconds" );
		PORT_DIPSETTING(    0x30, "120 Seconds" );
		PORT_DIPNAME( 0x40, 0x40, "DIP #5" );/* must be OFF */
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "Extended Play" );
		PORT_DIPSETTING(    0x80, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
	
		/* coinage settings are insane, refer to the manual */
	
		PORT_START(); 
		PORT_DIPNAME( 0x0F, 0x01, DEF_STR( "Coinage") ); /* dial */
		PORT_DIPSETTING(    0x00, "Mode 0" );
		PORT_DIPSETTING(    0x01, "Mode 1" );
		PORT_DIPSETTING(    0x02, "Mode 2" );
		PORT_DIPSETTING(    0x03, "Mode 3" );
		PORT_DIPSETTING(    0x04, "Mode 4" );
		PORT_DIPSETTING(    0x05, "Mode 5" );
		PORT_DIPSETTING(    0x06, "Mode 6" );
		PORT_DIPSETTING(    0x07, "Mode 7" );
		PORT_DIPSETTING(    0x08, "Mode 8" );
		PORT_DIPSETTING(    0x09, "Mode 9" );
		PORT_DIPSETTING(    0x0A, "Mode A" );
		PORT_DIPSETTING(    0x0B, "Mode B" );
		PORT_DIPSETTING(    0x0C, "Mode C" );
		PORT_DIPSETTING(    0x0D, "Mode D" );
		PORT_DIPSETTING(    0x0E, "Mode E" );
		PORT_DIPSETTING(    0x0F, "Mode F" );
		PORT_DIPNAME( 0x10, 0x10, "Score for Extended Play" );
		PORT_DIPSETTING(    0x00, "Low" );
		PORT_DIPSETTING(    0x10, "High" );
		PORT_BIT (0x20, IP_ACTIVE_LOW, IPT_BUTTON1);
		PORT_BIT (0x40, IP_ACTIVE_HIGH, IPT_COIN1);
		PORT_BIT (0x80, IP_ACTIVE_HIGH, IPT_COIN2);
	
		PORT_START(); 
		PORT_BIT (0x10, IP_ACTIVE_LOW, IPT_TILT);
		PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_BUTTON7, "Hiscore Reset", KEYCODE_H, IP_JOY_DEFAULT);
		PORT_BIT (0x40, IP_ACTIVE_LOW, IPT_START1);
		PORT_SERVICE(0x80, IP_ACTIVE_LOW);
	
		PORT_START(); 
		PORT_ANALOG( 0x3f, 0x20, IPT_AD_STICK_Y | IPF_REVERSE, 10, 10, 0, 63 );
	
		PORT_START(); 
		PORT_ANALOG( 0x3f, 0x20, IPT_AD_STICK_X, 10, 10, 0, 63 );
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout skyraid_text_layout = new GfxLayout
	(
		16, 8,  /* width, height */
		64,     /* total         */
		1,      /* planes        */
		new int[] { 0 },  /* plane offsets */
		new int[] {
			0, 0, 1, 1, 2, 2, 3, 3,
			4, 4, 5, 5, 6, 6, 7, 7
		},
		new int[] {
			0x38, 0x30, 0x28, 0x20, 0x18, 0x10, 0x08, 0x00
		},
		0x40
	);
	
	
	static GfxLayout skyraid_sprite_layout = new GfxLayout
	(
		32, 32, /* width, height */
		8,      /* total         */
		2,      /* planes        */
		        /* plane offsets */
		new int[] { 0, 1 },
		new int[] {
			0x00, 0x02, 0x04, 0x06, 0x08, 0x0A, 0x0C, 0x0E,
			0x10, 0x12, 0x14, 0x16, 0x18, 0x1A, 0x1C, 0x1E,
			0x20, 0x22, 0x24, 0x26, 0x28, 0x2A, 0x2C, 0x2E,
			0x30, 0x32, 0x34, 0x36, 0x38, 0x3A, 0x3C, 0x3E
		},
		new int[] {
			0x000, 0x040, 0x080, 0x0C0, 0x100, 0x140, 0x180, 0x1C0,
			0x200, 0x240, 0x280, 0x2C0, 0x300, 0x340, 0x380, 0x3C0,
			0x400, 0x440, 0x480, 0x4C0, 0x500, 0x540, 0x580, 0x5C0,
			0x600, 0x640, 0x680, 0x6C0, 0x700, 0x740, 0x780, 0x7C0
		},
		0x800
	);
	
	
	static GfxLayout skyraid_missile_layout = new GfxLayout
	(
		16, 16, /* width, height */
		8,      /* total         */
		1,      /* planes        */
		new int[] { 0 },  /* plane offsets */
		new int[] {
			0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
			0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F
		},
		new int[] {
			0x00, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70,
			0x80, 0x90, 0xA0, 0xB0, 0xC0, 0xD0, 0xE0, 0xF0
		},
		0x100
	);
	
	
	static GfxDecodeInfo skyraid_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, skyraid_text_layout, 18, 1 ),
		new GfxDecodeInfo( REGION_GFX2, 0, skyraid_sprite_layout, 8, 2 ),
		new GfxDecodeInfo( REGION_GFX3, 0, skyraid_missile_layout, 16, 1 ),
		new GfxDecodeInfo( -1 )
	};
	
	
	static MACHINE_DRIVER_START( skyraid )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M6502, 12096000 / 12)
		MDRV_CPU_MEMORY(skyraid_readmem, skyraid_writemem)
		MDRV_CPU_VBLANK_INT(irq0_line_hold, 1)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(22 * 1000000 / 15750)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 240)
		MDRV_VISIBLE_AREA(0, 511, 0, 239)
		MDRV_GFXDECODE(skyraid_gfxdecodeinfo)
	
		MDRV_PALETTE_INIT(skyraid)
		MDRV_PALETTE_LENGTH(20)
	
		MDRV_VIDEO_START(skyraid)
		MDRV_VIDEO_UPDATE(skyraid)
	
		/* sound hardware */
	MACHINE_DRIVER_END
	
	
	static RomLoadPtr rom_skyraid = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )
		ROM_LOAD( "030595.e1", 0x7000, 0x800, CRC(c6cb3a2b) SHA1(e4cb8d259446d0614c0c8f097f97dcf21869782e) )
		ROM_RELOAD(            0xF000, 0x800 )
		ROM_LOAD( "030594.d1", 0x7800, 0x800, CRC(27979e96) SHA1(55ffe3094c6764e6b99ee148e3dd730ca263fa3a) )
		ROM_RELOAD(            0xF800, 0x800 )
	
		ROM_REGION( 0x0200, REGION_GFX1, ROMREGION_DISPOSE ) /* alpha numerics */
		ROM_LOAD( "030598.h2", 0x0000, 0x200, CRC(2a7c5fa0) SHA1(93a79e5948dfcd9b6c2ff390e85a43f7a8cac327) )
	
		ROM_REGION( 0x0800, REGION_GFX2, ROMREGION_DISPOSE ) /* sprites */
		ROM_LOAD( "030599.m7", 0x0000, 0x800, CRC(0cd179ea) SHA1(e3c763f76e6103e5909e7b5a979206b262d6e96a) )
	
		ROM_REGION( 0x0100, REGION_GFX3, ROMREGION_DISPOSE ) /* missiles */
		ROM_LOAD_NIB_LOW ( "030597.n5", 0x0000, 0x100, CRC(319ff49c) SHA1(ff4d8b20436179910bf30c720d98df4678f683a9) )
		ROM_LOAD_NIB_HIGH( "030596.m4", 0x0000, 0x100, CRC(30454ed0) SHA1(4216a54c13d9c4803f88f2de35cdee31290bb15e) )
	
		ROM_REGION( 0x0800, REGION_USER1, 0 ) /* terrain */
		ROM_LOAD_NIB_LOW ( "030584.j5", 0x0000, 0x800, CRC(81f6e8a5) SHA1(ad77b469ed0c9d5dfaa221ecf47d0db4a7f7ac91) )
		ROM_LOAD_NIB_HIGH( "030585.k5", 0x0000, 0x800, CRC(b49bec3f) SHA1(b55d25230ec11c52e7b47d2c10194a49adbeb50a) )
	
		ROM_REGION( 0x0100, REGION_USER2, 0 ) /* trapezoid */
		ROM_LOAD_NIB_LOW ( "030582.a6", 0x0000, 0x100, CRC(0eacd595) SHA1(5469e312a1f522ce0a61054b50895a5b1a3f19ba) )
		ROM_LOAD_NIB_HIGH( "030583.b6", 0x0000, 0x100, CRC(3edd6fbc) SHA1(0418ea78cf51e18c51087b43a41cd9e13aac0a16) )
	
		ROM_REGION( 0x0300, REGION_PROMS, 0 )
		ROM_LOAD( "006559.c4", 0x0200, 0x100, CRC(5a8d0e42) SHA1(772220c4c24f18769696ddba26db2bc2e5b0909d) ) /* sync */
	ROM_END(); }}; 
	
	
	GAMEX( 1978, skyraid, 0, skyraid, skyraid, 0, ORIENTATION_FLIP_Y, "Atari", "Sky Raider", GAME_NO_SOUND | GAME_IMPERFECT_COLORS )
}
