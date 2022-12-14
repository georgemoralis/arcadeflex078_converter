/***************************************************************************

Atari Wolf Pack (prototype) driver

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class wolfpack
{
	
	
	
	
	
	
	static void periodic_callback(int scanline)
	{
		cpu_set_nmi_line(0, PULSE_LINE);
	
		scanline += 64;
	
		if (scanline >= 262)
		{
			scanline = 0;
		}
	
		timer_set(cpu_getscanlinetime(scanline), scanline, periodic_callback);
	}
	
	
	public static MachineInitHandlerPtr machine_init_wolfpack  = new MachineInitHandlerPtr() { public void handler(){
		timer_set(cpu_getscanlinetime(0), 0, periodic_callback);
	} };
	
	
	public static PaletteInitHandlerPtr palette_init_wolfpack  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
		palette_set_color(0, 0x00, 0x00, 0x00);
		palette_set_color(1, 0xc1, 0xc1, 0xc1);
		palette_set_color(2, 0x81, 0x81, 0x81);
		palette_set_color(3, 0x48, 0x48, 0x48);
	
		for (i = 0; i < 4; i++)
		{
			UINT8 r, g, b;
	
			palette_get_color(i, &r, &g, &b);
	
			palette_set_color(4 + i,
				r < 0xb8 ? r + 0x48 : 0xff,
				g < 0xb8 ? g + 0x48 : 0xff,
				b < 0xb8 ? b + 0x48 : 0xff);
		}
	
		colortable[0] = 0;
		colortable[1] = 1;
		colortable[2] = 1;
		colortable[3] = 0;
		colortable[4] = 0;
		colortable[5] = 2;
		colortable[6] = 0;
		colortable[7] = 3;
	} };
	
	
	public static ReadHandlerPtr wolfpack_zeropage_r  = new ReadHandlerPtr() { public int handler(int offset){
		return memory_region(REGION_CPU1)[offset & 0xff];
	} };
	
	
	public static ReadHandlerPtr wolfpack_input_r  = new ReadHandlerPtr() { public int handler(int offset){
		UINT8 val = readinputport(0);
	
		if (((readinputport(2) + 0) / 2) & 1)
		{
			val |= 1;
		}
		if (((readinputport(2) + 1) / 2) & 1)
		{
			val |= 2;
		}
	
		return val;
	} };
	
	
	public static ReadHandlerPtr wolfpack_misc_r  = new ReadHandlerPtr() { public int handler(int offset){
		UINT8 val = 0;
	
		/* BIT0 => SPEECH BUSY */
		/* BIT1 => COMP SIREN  */
		/* BIT2 => SPARE       */
		/* BIT3 => SPARE       */
		/* BIT4 => COL DETECT  */
		/* BIT5 => UNUSED      */
		/* BIT6 => UNUSED      */
		/* BIT7 => VBLANK      */
	
		if (!wolfpack_collision)
		{
			val |= 0x10;
		}
		if (cpu_getscanline() >= 240)
		{
			val |= 0x80;
		}
	
		return val;
	} };
	
	
	public static WriteHandlerPtr wolfpack_zeropage_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		memory_region(REGION_CPU1)[offset & 0xff] = data;
	} };
	
	
	public static WriteHandlerPtr wolfpack_high_explo_w = new WriteHandlerPtr() {public void handler(int offset, int data) }
	public static WriteHandlerPtr wolfpack_sonar_ping_w = new WriteHandlerPtr() {public void handler(int offset, int data)}
	public static WriteHandlerPtr wolfpack_sirlat_w = new WriteHandlerPtr() {public void handler(int offset, int data)}
	public static WriteHandlerPtr wolfpack_pt_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)}
	public static WriteHandlerPtr wolfpack_start_speech_w = new WriteHandlerPtr() {public void handler(int offset, int data)}
	public static WriteHandlerPtr wolfpack_launch_torpedo_w = new WriteHandlerPtr() {public void handler(int offset, int data)}
	public static WriteHandlerPtr wolfpack_low_explo_w = new WriteHandlerPtr() {public void handler(int offset, int data)}
	public static WriteHandlerPtr wolfpack_screw_cont_w = new WriteHandlerPtr() {public void handler(int offset, int data)}
	public static WriteHandlerPtr wolfpack_lamp_flash_w = new WriteHandlerPtr() {public void handler(int offset, int data)}
	public static WriteHandlerPtr wolfpack_warning_light_w = new WriteHandlerPtr() {public void handler(int offset, int data)}
	public static WriteHandlerPtr wolfpack_audamp_w = new WriteHandlerPtr() {public void handler(int offset, int data)}
	public static WriteHandlerPtr wolfpack_word_w = new WriteHandlerPtr() {public void handler(int offset, int data)}
	
	
	public static WriteHandlerPtr wolfpack_attract_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		coin_lockout_global_w(!(data & 1));
	} };
	
	
	public static WriteHandlerPtr wolfpack_credit_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		set_led_status(0, !(data & 1));
	} };
	
	
	public static WriteHandlerPtr wolfpack_coldetres_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		wolfpack_collision = 0;
	} };
	
	
	public static Memory_ReadAddress wolfpack_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x00ff, MRA_RAM ),
		new Memory_ReadAddress( 0x0100, 0x01ff, wolfpack_zeropage_r ),
		new Memory_ReadAddress( 0x1000, 0x1000, wolfpack_input_r ),
		new Memory_ReadAddress( 0x2000, 0x2000, wolfpack_misc_r ),
		new Memory_ReadAddress( 0x3000, 0x3000, input_port_1_r ),
		new Memory_ReadAddress( 0x7000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x9000, 0x9000, MRA_NOP ), /* debugger ROM location? */
		new Memory_ReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress wolfpack_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x00ff, MWA_RAM ),
		new Memory_WriteAddress( 0x0100, 0x01ff, wolfpack_zeropage_w ),
		new Memory_WriteAddress( 0x1000, 0x10ff, MWA_RAM, wolfpack_alpha_num_ram ),
		new Memory_WriteAddress( 0x2000, 0x2000, wolfpack_high_explo_w ),
		new Memory_WriteAddress( 0x2001, 0x2001, wolfpack_sonar_ping_w ),
		new Memory_WriteAddress( 0x2002, 0x2002, wolfpack_sirlat_w ),
		new Memory_WriteAddress( 0x2003, 0x2003, wolfpack_pt_sound_w ),
		new Memory_WriteAddress( 0x2004, 0x2004, wolfpack_start_speech_w ),
		new Memory_WriteAddress( 0x2005, 0x2005, wolfpack_launch_torpedo_w ),
		new Memory_WriteAddress( 0x2006, 0x2006, wolfpack_low_explo_w ),
		new Memory_WriteAddress( 0x2007, 0x2007, wolfpack_screw_cont_w ),
		new Memory_WriteAddress( 0x2008, 0x2008, wolfpack_video_invert_w ),
		new Memory_WriteAddress( 0x2009, 0x2009, wolfpack_ship_reflect_w ),
		new Memory_WriteAddress( 0x200a, 0x200a, wolfpack_lamp_flash_w ),
		new Memory_WriteAddress( 0x200c, 0x200c, wolfpack_credit_w ),
		new Memory_WriteAddress( 0x200d, 0x200d, wolfpack_attract_w ),
		new Memory_WriteAddress( 0x200e, 0x200e, wolfpack_pt_pos_select_w ),
		new Memory_WriteAddress( 0x200f, 0x200f, wolfpack_warning_light_w ),
		new Memory_WriteAddress( 0x3000, 0x3000, wolfpack_audamp_w ),
		new Memory_WriteAddress( 0x3001, 0x3001, wolfpack_pt_horz_w ),
		new Memory_WriteAddress( 0x3003, 0x3003, wolfpack_pt_pic_w ),
		new Memory_WriteAddress( 0x3004, 0x3004, wolfpack_word_w ),
		new Memory_WriteAddress( 0x3007, 0x3007, wolfpack_coldetres_w ),
		new Memory_WriteAddress( 0x4000, 0x4000, wolfpack_ship_h_w ),
		new Memory_WriteAddress( 0x4001, 0x4001, wolfpack_torpedo_pic_w ),
		new Memory_WriteAddress( 0x4002, 0x4002, wolfpack_ship_size_w ),
		new Memory_WriteAddress( 0x4003, 0x4003, wolfpack_ship_h_precess_w ),
		new Memory_WriteAddress( 0x4004, 0x4004, wolfpack_ship_pic_w ),
		new Memory_WriteAddress( 0x4005, 0x4005, wolfpack_torpedo_h_w ),
		new Memory_WriteAddress( 0x4006, 0x4006, wolfpack_torpedo_v_w ),
		new Memory_WriteAddress( 0x5000, 0x5fff, watchdog_reset_w ),
		new Memory_WriteAddress( 0x7000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	static InputPortPtr input_ports_wolfpack = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( wolfpack )
	
		PORT_START(); 
		PORT_BIT ( 0x03, IP_ACTIVE_HIGH, IPT_UNUSED );/* dial connects here */
		PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_SERVICE( 0x10, IP_ACTIVE_HIGH );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
	
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x01, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x03, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x04, 0x00, "Extended Play" );
		PORT_DIPSETTING(    0x04, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x18, 0x08, "Play Time" );
		PORT_DIPSETTING(    0x00, "65 seconds" );
		PORT_DIPSETTING(    0x08, "97 seconds" );
		PORT_DIPSETTING(    0x10, "130 seconds" );
		PORT_DIPSETTING(    0x18, "160 seconds" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") ); /* demo sound? */
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0xc0, 0x80, "Score for Extended Play" );
		PORT_DIPSETTING(    0x00, "8000" );
		PORT_DIPSETTING(    0x40, "12000" );
		PORT_DIPSETTING(    0x80, "16000" );
		PORT_DIPSETTING(    0xc0, "20000" );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_DIAL, 30, 5, 0, 255);
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout tile_layout = new GfxLayout
	(
		16, 8,
		64,
		1,
		new int[] { 0 },
		new int[] {
			0x7, 0x7, 0x6, 0x6, 0x5, 0x5, 0x4, 0x4,
			0xf, 0xf, 0xe, 0xe, 0xd, 0xd, 0xc, 0xc
		},
		new int[] {
			0x70, 0x60, 0x50, 0x40, 0x30, 0x20, 0x10, 0x00
		},
		0x80
	);
	
	
	static GfxLayout ship_layout = new GfxLayout
	(
		64, 16,
		16,
		1,
		new int[] { 0 },
		new int[] {
			0x04, 0x05, 0x06, 0x07, 0x0c, 0x0d, 0x0e, 0x0f,
			0x14, 0x15, 0x16, 0x17, 0x1c, 0x1d, 0x1e, 0x1f,
			0x24, 0x25, 0x26, 0x27, 0x2c, 0x2d, 0x2e, 0x2f,
			0x34, 0x35, 0x36, 0x37, 0x3c, 0x3d, 0x3e, 0x3f,
			0x44, 0x45, 0x46, 0x47, 0x4c, 0x4d, 0x4e, 0x4f,
			0x54, 0x55, 0x56, 0x57, 0x5c, 0x5d, 0x5e, 0x5f,
			0x64, 0x65, 0x66, 0x67, 0x6c, 0x6d, 0x6e, 0x6f,
			0x74, 0x75, 0x76, 0x77, 0x7c, 0x7d, 0x7e, 0x7f
		},
		new int[] {
			0x780, 0x700, 0x680, 0x600, 0x580, 0x500, 0x480, 0x400,
			0x380, 0x300, 0x280, 0x200, 0x180, 0x100, 0x080, 0x000
		},
		0x800
	);
	
	
	static GfxLayout pt_layout = new GfxLayout
	(
		64, 8,
		16,
		1,
		new int[] { 0 },
		new int[] {
			0x3f, 0x3f, 0x3e, 0x3e, 0x3d, 0x3d, 0x3c, 0x3c,
			0x37, 0x37, 0x36, 0x36, 0x35, 0x35, 0x34, 0x34,
			0x2f, 0x2f, 0x2e, 0x2e, 0x2d, 0x2d, 0x2c, 0x2c,
			0x27, 0x27, 0x26, 0x26, 0x25, 0x25, 0x24, 0x24,
			0x1f, 0x1f, 0x1e, 0x1e, 0x1d, 0x1d, 0x1c, 0x1c,
			0x17, 0x17, 0x16, 0x16, 0x15, 0x15, 0x14, 0x14,
			0x0f, 0x0f, 0x0e, 0x0e, 0x0d, 0x0d, 0x0c, 0x0c,
			0x07, 0x07, 0x06, 0x06, 0x05, 0x05, 0x04, 0x04
		},
		new int[] {
			0x000, 0x040, 0x080, 0x0c0, 0x100, 0x140, 0x180, 0x1c0
		},
		0x200
	);
	
	
	static GfxLayout torpedo_layout = new GfxLayout
	(
		16, 32,
		16,
		1,
		new int[] { 0 },
		new int[] {
			0x4, 0x4, 0x5, 0x5, 0x6, 0x6, 0x7, 0x7,
			0xc, 0xc, 0xd, 0xd, 0xe, 0xe, 0xf, 0xf
		},
		new int[] {
			0x000, 0x010, 0x020, 0x030, 0x040, 0x050, 0x060, 0x070,
			0x080, 0x090, 0x0a0, 0x0b0, 0x0c0, 0x0d0, 0x0e0, 0x0f0,
			0x100, 0x110, 0x120, 0x130, 0x140, 0x150, 0x160, 0x170,
			0x180, 0x190, 0x1a0, 0x1b0, 0x1c0, 0x1d0, 0x1e0, 0x1f0
		},
		0x0200
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tile_layout, 0, 2 ),
		new GfxDecodeInfo( REGION_GFX2, 0, ship_layout, 6, 1 ),
		new GfxDecodeInfo( REGION_GFX3, 0, pt_layout, 0, 1 ),
		new GfxDecodeInfo( REGION_GFX4, 0, torpedo_layout, 4, 1 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	static MACHINE_DRIVER_START(wolfpack)
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M6502, 12096000 / 16)
		MDRV_CPU_MEMORY(wolfpack_readmem, wolfpack_writemem)
	
		MDRV_FRAMES_PER_SECOND(60)
	
		/* video hardware */
		MDRV_MACHINE_INIT(wolfpack)
	
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 262)
		MDRV_VISIBLE_AREA(0, 511, 16, 239)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(8)
		MDRV_COLORTABLE_LENGTH(8)
		MDRV_PALETTE_INIT(wolfpack)
		MDRV_VIDEO_START(wolfpack)
		MDRV_VIDEO_UPDATE(wolfpack)
		MDRV_VIDEO_EOF(wolfpack)
	
		/* sound hardware */
	MACHINE_DRIVER_END
	
	
	static RomLoadPtr rom_wolfpack = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )
		ROM_LOAD_NIB_LOW ( "30285.e3", 0x7000, 0x0800, CRC(b4d30b33) SHA1(46645c227828632b57244bdccad455e1831b5273) )
		ROM_RELOAD       (             0xF000, 0x0800 )
		ROM_LOAD_NIB_HIGH( "30287.g3", 0x7000, 0x0800, CRC(c6300dc9) SHA1(6a0ec0bfa6ad4c870aa6f21bfde094da6975b58b) )
		ROM_RELOAD       (             0xF000, 0x0800 )
		ROM_LOAD_NIB_LOW ( "30286.f3", 0x7800, 0x0800, CRC(17dce9e8) SHA1(9c7bac1aa676548dc7908f1518efd58c72645ab7) )
		ROM_RELOAD       (             0xF800, 0x0800 )
		ROM_LOAD_NIB_HIGH( "30288.h3", 0x7800, 0x0800, CRC(b80ab7b6) SHA1(f2ede98ac5337064499ae2262a8a81f83505bd66) )
		ROM_RELOAD       (             0xF800, 0x0800 )
	
		ROM_REGION( 0x0400, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "30291.c1", 0x0000, 0x0400, CRC(7e3d22cf) SHA1(92e6bbe049dc8fcd674f2ff96cde3786f714508d) )
	
		ROM_REGION( 0x1000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "30289.j6", 0x0000, 0x0800, CRC(f63e5629) SHA1(d64f19fc62060d395df5bb8663a7694a23b0aa2e) )
		ROM_LOAD( "30290.k6", 0x0800, 0x0800, CRC(70d5430e) SHA1(d512fc3bb0cf0816a1c987f7188c4b331303347f) )
	
		ROM_REGION( 0x0400, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "30294.p4", 0x0000, 0x0400, CRC(ea93f4b9) SHA1(48b4e0136f5349eb53fea7127a969d87457d70f9) )
	
		ROM_REGION( 0x0400, REGION_GFX4, ROMREGION_DISPOSE )
		ROM_LOAD( "30293.m6", 0x0000, 0x0400, CRC(11900d47) SHA1(2dcb3c3488a5e9ed7f1751649f8dc25696f0f57a) )
	
		ROM_REGION( 0x0800, REGION_SOUND1, ROMREGION_DISPOSE ) /* voice data */
		ROM_LOAD_NIB_LOW ( "30863.r1", 0x0000, 0x0800, CRC(3f779f13) SHA1(8ed8a1bf680e8277066416f467388e3875e8cbbd) )
		ROM_LOAD_NIB_HIGH( "30864.r3", 0x0000, 0x0800, CRC(c4a58d1d) SHA1(a2ba9354b99c739bbfa94458d671c109be163ca0) )
	ROM_END(); }}; 
	
	
	GAMEX( 1978, wolfpack, 0, wolfpack, wolfpack, 0, ORIENTATION_FLIP_Y, "Atari", "Wolf Pack (prototype)", GAME_NO_SOUND )
}
