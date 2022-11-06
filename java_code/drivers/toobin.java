/***************************************************************************

	Atari Toobin' hardware

	driver by Aaron Giles

	Games supported:
		* Toobin' (1988) [6 sets]

	Known bugs:
		* none at this time

****************************************************************************

	Memory map (TBA)

***************************************************************************/


/*
 * ported to v0.78
 * using automatic conversion tool v0.04
 */ 
package arcadeflex.v078.drivers;

public class toobin
{
	
	
	
	/*************************************
	 *
	 *	Statics
	 *
	 *************************************/
	
	static data16_t *interrupt_scan;
	
	
	
	/*************************************
	 *
	 *	Initialization & interrupts
	 *
	 *************************************/
	
	static void update_interrupts(void)
	{
		int newstate = 0;
	
		if (atarigen_scanline_int_state)
			newstate |= 1;
		if (atarigen_sound_int_state)
			newstate |= 2;
	
		if (newstate)
			cpu_set_irq_line(0, newstate, ASSERT_LINE);
		else
			cpu_set_irq_line(0, 7, CLEAR_LINE);
	}
	
	
	public static MachineInitHandlerPtr machine_init_toobin  = new MachineInitHandlerPtr() { public void handler(){
		atarigen_eeprom_reset();
		atarigen_interrupt_reset(update_interrupts);
		atarijsa_reset();
	} };
	
	
	
	/*************************************
	 *
	 *	Interrupt handlers
	 *
	 *************************************/
	
	static WRITE16_HANDLER( interrupt_scan_w )
	{
		int oldword = interrupt_scan[offset];
		int newword = oldword;
		COMBINE_DATA(&newword);
	
		/* if something changed, update the word in memory */
		if (oldword != newword)
		{
			interrupt_scan[offset] = newword;
			atarigen_scanline_int_set(newword & 0x1ff);
		}
	}
	
	
	
	/*************************************
	 *
	 *	I/O read dispatch
	 *
	 *************************************/
	
	static READ16_HANDLER( special_port1_r )
	{
		int result = readinputport(1);
		if (atarigen_get_hblank()) result ^= 0x8000;
		if (atarigen_cpu_to_sound_ready) result ^= 0x2000;
		return result;
	}
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	static MEMORY_READ16_START( main_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0xc00000, 0xc09fff, MRA16_RAM },
		{ 0xc10000, 0xc107ff, MRA16_RAM },
		{ 0xff6000, 0xff6001, MRA16_NOP },		/* who knows? read at controls time */
		{ 0xff8800, 0xff8801, input_port_0_word_r },
		{ 0xff9000, 0xff9001, special_port1_r },
		{ 0xff9800, 0xff9801, atarigen_sound_r },
		{ 0xffa000, 0xffafff, atarigen_eeprom_r },
		{ 0xffc000, 0xffffff, MRA16_RAM },
	MEMORY_END
	
	
	static MEMORY_WRITE16_START( main_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0xc00000, 0xc07fff, atarigen_playfield_large_w, &atarigen_playfield },
		{ 0xc08000, 0xc097ff, atarigen_alpha_w, &atarigen_alpha },
		{ 0xc09800, 0xc09fff, atarimo_0_spriteram_w, &atarimo_0_spriteram },
		{ 0xc10000, 0xc107ff, toobin_paletteram_w, &paletteram16 },
		{ 0xff8000, 0xff8001, watchdog_reset16_w },
		{ 0xff8100, 0xff8101, atarigen_sound_w },
		{ 0xff8300, 0xff8301, toobin_intensity_w },
		{ 0xff8340, 0xff8341, interrupt_scan_w, &interrupt_scan },
		{ 0xff8380, 0xff8381, toobin_slip_w, &atarimo_0_slipram },
		{ 0xff83c0, 0xff83c1, atarigen_scanline_int_ack_w },
		{ 0xff8400, 0xff8401, atarigen_sound_reset_w },
		{ 0xff8500, 0xff8501, atarigen_eeprom_enable_w },
		{ 0xff8600, 0xff8601, toobin_xscroll_w, &atarigen_xscroll },
		{ 0xff8700, 0xff8701, toobin_yscroll_w, &atarigen_yscroll },
		{ 0xffa000, 0xffafff, atarigen_eeprom_w, &atarigen_eeprom, &atarigen_eeprom_size },
		{ 0xffc000, 0xffffff, MWA16_RAM },
	MEMORY_END
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	static InputPortPtr input_ports_toobin = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( toobin )
		PORT_START(); 	/* ff8800 */
		PORT_BITX(0x0001, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2, "P2 R Paddle Forward", KEYCODE_L, IP_JOY_DEFAULT );
		PORT_BITX(0x0002, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2, "P2 L Paddle Forward", KEYCODE_J, IP_JOY_DEFAULT );
		PORT_BITX(0x0004, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2, "P2 L Paddle Backward", KEYCODE_U, IP_JOY_DEFAULT );
		PORT_BITX(0x0008, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2, "P2 R Paddle Backward", KEYCODE_O, IP_JOY_DEFAULT );
		PORT_BITX(0x0010, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "P1 R Paddle Forward", KEYCODE_D, IP_JOY_DEFAULT );
		PORT_BITX(0x0020, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1, "P1 L Paddle Forward", KEYCODE_A, IP_JOY_DEFAULT );
		PORT_BITX(0x0040, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1, "P1 L Paddle Backward", KEYCODE_Q, IP_JOY_DEFAULT );
		PORT_BITX(0x0080, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1, "P1 R Paddle Backward", KEYCODE_E, IP_JOY_DEFAULT );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BITX(0x0100, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "P1 Throw", KEYCODE_LCONTROL, IP_JOY_DEFAULT );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BITX(0x0200, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2, "P2 Throw", KEYCODE_RCONTROL, IP_JOY_DEFAULT );
		PORT_BIT( 0xfc00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* ff9000 */
		PORT_BIT( 0x03ff, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_SERVICE( 0x1000, IP_ACTIVE_LOW );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNUSED );
	
		JSA_I_PORT	/* audio board port */
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Graphics definitions
	 *
	 *************************************/
	
	static GfxLayout anlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		2,
		new int[] { 0, 4 },
		new int[] { 0, 1, 2, 3, 8, 9, 10, 11 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		8*16
	);
	
	
	static GfxLayout pflayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,2),
		4,
		new int[] { RGN_FRAC(1,2)+0, RGN_FRAC(1,2)+4, 0, 4 },
		new int[] { 0, 1, 2, 3, 8, 9, 10, 11 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		8*16
	);
	
	
	static GfxLayout molayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,2),
		4,
		new int[] { RGN_FRAC(1,2)+0, RGN_FRAC(1,2)+4, 0, 4 },
		new int[] { 0, 1, 2, 3, 8, 9, 10, 11, 16, 17, 18, 19, 24, 25, 26, 27 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32, 8*32, 9*32, 10*32, 11*32, 12*32, 13*32, 14*32, 15*32 },
		8*64
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, pflayout,     0, 16 ),
		new GfxDecodeInfo( REGION_GFX2, 0, molayout,   256, 16 ),
		new GfxDecodeInfo( REGION_GFX3, 0, anlayout,   512, 64 ),
		new GfxDecodeInfo( -1 )
	};
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MACHINE_DRIVER_START( toobin )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68010, ATARI_CLOCK_32MHz/4)
		MDRV_CPU_MEMORY(main_readmem,main_writemem)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
	
		MDRV_MACHINE_INIT(toobin)
		MDRV_NVRAM_HANDLER(atarigen)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_UPDATE_BEFORE_VBLANK)
		MDRV_SCREEN_SIZE(64*8, 48*8)
		MDRV_VISIBLE_AREA(0*8, 64*8-1, 0*8, 48*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(toobin)
		MDRV_VIDEO_UPDATE(toobin)
	
		/* sound hardware */
		MDRV_IMPORT_FROM(jsa_i_stereo_pokey)
	MACHINE_DRIVER_END
	
	
	
	/*************************************
	 *
	 *	ROM definition(s)
	 *
	 *************************************/
	
	static RomLoadPtr rom_toobin = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 )	/* 8*64k for 68000 code */
		ROM_LOAD16_BYTE( "3133-1j.061",  0x000000, 0x010000, CRC(79a92d02) SHA1(72eebb96a3963f94558bb204e0afe08f2b4c1864) )
		ROM_LOAD16_BYTE( "3137-1f.061",  0x000001, 0x010000, CRC(e389ef60) SHA1(24861fe5eb49de852987993a905fefe4dd43b204) )
		ROM_LOAD16_BYTE( "3134-2j.061",  0x020000, 0x010000, CRC(3dbe9a48) SHA1(37fe2534fed5708a63995e53ea0cb1d2d23fc1b9) )
		ROM_LOAD16_BYTE( "3138-2f.061",  0x020001, 0x010000, CRC(a17fb16c) SHA1(ae0a2c675a88dfaafffe47971c46c83dc7552148) )
		ROM_LOAD16_BYTE( "3135-4j.061",  0x040000, 0x010000, CRC(dc90b45c) SHA1(78c648be8e0aec6d1be45f909f2e468f3b572957) )
		ROM_LOAD16_BYTE( "3139-4f.061",  0x040001, 0x010000, CRC(6f8a719a) SHA1(bca7280155a4c44f55b402aed59927343c651acc) )
		ROM_LOAD16_BYTE( "1136-5j.061",  0x060000, 0x010000, CRC(5ae3eeac) SHA1(583b6c3f61e8ad4d98449205fedecf3e21ee993c) )
		ROM_LOAD16_BYTE( "1140-5f.061",  0x060001, 0x010000, CRC(dacbbd94) SHA1(0e3a93f439ff9f3dd57ee13604be02e9c74c8eec) )
	
		ROM_REGION( 0x14000, REGION_CPU2, 0 )	/* 64k for 6502 code */
		ROM_LOAD( "1141-2k.061",  0x010000, 0x004000, CRC(c0dcce1a) SHA1(285c13f08020cf5827eca2afcc2fa8a3a0a073e0) )
		ROM_CONTINUE(             0x004000, 0x00c000 )
	
		ROM_REGION( 0x080000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "1101-1a.061",  0x000000, 0x010000, CRC(02696f15) SHA1(51856c331c45d287e574e2e4013b62a6472ad720) )
		ROM_LOAD( "1102-2a.061",  0x010000, 0x010000, CRC(4bed4262) SHA1(eda16ece14cb60012edbe006b2839986d082822e) )
		ROM_LOAD( "1103-4a.061",  0x020000, 0x010000, CRC(e62b037f) SHA1(9a2341b822265269c07b65c4bc0fbc760c1bd456) )
		ROM_LOAD( "1104-5a.061",  0x030000, 0x010000, CRC(fa05aee6) SHA1(db0dbf94ba1f2c1bb3ad55df2f38a71b4ecb38e4) )
		ROM_LOAD( "1105-1b.061",  0x040000, 0x010000, CRC(ab1c5578) SHA1(e80a1c7d2f279a523dcc9d943bd5a1ce75045d2e) )
		ROM_LOAD( "1106-2b.061",  0x050000, 0x010000, CRC(4020468e) SHA1(fa83e3d903d254c598fcbf120492ac77777ae31f) )
		ROM_LOAD( "1107-4b.061",  0x060000, 0x010000, CRC(fe6f6aed) SHA1(11bd17be3c9fe409db8268cb17515040bfd92ee2) )
		ROM_LOAD( "1108-5b.061",  0x070000, 0x010000, CRC(26fe71e1) SHA1(cac22f969c943e184a58d7bb62072f93273638de) )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "1143-10a.061", 0x000000, 0x020000, CRC(211c1049) SHA1(fcf4d9321b2871723a10b7607069da83d3402723) )
		ROM_LOAD( "1144-13a.061", 0x020000, 0x020000, CRC(ef62ed2c) SHA1(c2c21023b2f559b8a63e6ae9c59c33a3055cc465) )
		ROM_LOAD( "1145-16a.061", 0x040000, 0x020000, CRC(067ecb8a) SHA1(a42e4602e1de1cc83a30a901a9adb5519f426cff) )
		ROM_LOAD( "1146-18a.061", 0x060000, 0x020000, CRC(fea6bc92) SHA1(c502ab022fdafdef71a720237094fe95c3137d69) )
		ROM_LOAD( "1125-21a.061", 0x080000, 0x010000, CRC(c37f24ac) SHA1(341fab8244d8063a516a4a25d7ee778f708cd386) )
		ROM_RELOAD(               0x0c0000, 0x010000 )
		ROM_LOAD( "1126-23a.061", 0x090000, 0x010000, CRC(015257f0) SHA1(c5ae6a43b95ecb06a04ea877f435b1f925cff136) )
		ROM_RELOAD(               0x0d0000, 0x010000 )
		ROM_LOAD( "1127-24a.061", 0x0a0000, 0x010000, CRC(d05417cb) SHA1(5cbd54050364e82fe443ca2150c34fca84c42419) )
		ROM_RELOAD(               0x0e0000, 0x010000 )
		ROM_LOAD( "1128-25a.061", 0x0b0000, 0x010000, CRC(fba3e203) SHA1(5951571e6e64061e5448cae27af0cedd5bda2b1e) )
		ROM_RELOAD(               0x0f0000, 0x010000 )
		ROM_LOAD( "1147-10b.061", 0x100000, 0x020000, CRC(ca4308cf) SHA1(966970524915a0a5a77e3525e446b50ecde5b119) )
		ROM_LOAD( "1148-13b.061", 0x120000, 0x020000, CRC(23ddd45c) SHA1(8ee19e8982a928b56d6010f283fb2f720dc71cd6) )
		ROM_LOAD( "1149-16b.061", 0x140000, 0x020000, CRC(d77cd1d0) SHA1(148fa17c9b7a2453adf059325cb608073d1ef195) )
		ROM_LOAD( "1150-18b.061", 0x160000, 0x020000, CRC(a37157b8) SHA1(347cea600f28709fc3d942feb5cadce7def72dbb) )
		ROM_LOAD( "1129-21b.061", 0x180000, 0x010000, CRC(294aaa02) SHA1(69b42dfc444b2c9f2bb0fdcb96e2becb0df6226a) )
		ROM_RELOAD(               0x1c0000, 0x010000 )
		ROM_LOAD( "1130-23b.061", 0x190000, 0x010000, CRC(dd610817) SHA1(25f542ae4e7e77399d6df328edbc4cceb390db04) )
		ROM_RELOAD(               0x1d0000, 0x010000 )
		ROM_LOAD( "1131-24b.061", 0x1a0000, 0x010000, CRC(e8e2f919) SHA1(292dacb60db867cb2ae69942c7502af6526ab550) )
		ROM_RELOAD(               0x1e0000, 0x010000 )
		ROM_LOAD( "1132-25b.061", 0x1b0000, 0x010000, CRC(c79f8ffc) SHA1(6e90044755097387011e7cc04548bedb399b7cc3) )
		ROM_RELOAD(               0x1f0000, 0x010000 )
	
		ROM_REGION( 0x004000, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "1142-20h.061", 0x000000, 0x004000, CRC(a6ab551f) SHA1(6a11e16f3965416c81737efcb81e751484ba5ace) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_toobine = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 )	/* 8*64k for 68000 code */
		ROM_LOAD16_BYTE( "3733-1j.061",  0x000000, 0x010000, CRC(286c7fad) SHA1(1f06168327bdc356f1bc4cf9a951f914932c491a) )
		ROM_LOAD16_BYTE( "3737-1f.061",  0x000001, 0x010000, CRC(965c161d) SHA1(30d959a945cb7dc7f00ad4ca9db027a377024030) )
		ROM_LOAD16_BYTE( "3134-2j.061",  0x020000, 0x010000, CRC(3dbe9a48) SHA1(37fe2534fed5708a63995e53ea0cb1d2d23fc1b9) )
		ROM_LOAD16_BYTE( "3138-2f.061",  0x020001, 0x010000, CRC(a17fb16c) SHA1(ae0a2c675a88dfaafffe47971c46c83dc7552148) )
		ROM_LOAD16_BYTE( "3135-4j.061",  0x040000, 0x010000, CRC(dc90b45c) SHA1(78c648be8e0aec6d1be45f909f2e468f3b572957) )
		ROM_LOAD16_BYTE( "3139-4f.061",  0x040001, 0x010000, CRC(6f8a719a) SHA1(bca7280155a4c44f55b402aed59927343c651acc) )
		ROM_LOAD16_BYTE( "1136-5j.061",  0x060000, 0x010000, CRC(5ae3eeac) SHA1(583b6c3f61e8ad4d98449205fedecf3e21ee993c) )
		ROM_LOAD16_BYTE( "1140-5f.061",  0x060001, 0x010000, CRC(dacbbd94) SHA1(0e3a93f439ff9f3dd57ee13604be02e9c74c8eec) )
	
		ROM_REGION( 0x14000, REGION_CPU2, 0 )	/* 64k for 6502 code */
		ROM_LOAD( "1141-2k.061",  0x010000, 0x004000, CRC(c0dcce1a) SHA1(285c13f08020cf5827eca2afcc2fa8a3a0a073e0) )
		ROM_CONTINUE(             0x004000, 0x00c000 )
	
		ROM_REGION( 0x080000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "1101-1a.061",  0x000000, 0x010000, CRC(02696f15) SHA1(51856c331c45d287e574e2e4013b62a6472ad720) )
		ROM_LOAD( "1102-2a.061",  0x010000, 0x010000, CRC(4bed4262) SHA1(eda16ece14cb60012edbe006b2839986d082822e) )
		ROM_LOAD( "1103-4a.061",  0x020000, 0x010000, CRC(e62b037f) SHA1(9a2341b822265269c07b65c4bc0fbc760c1bd456) )
		ROM_LOAD( "1104-5a.061",  0x030000, 0x010000, CRC(fa05aee6) SHA1(db0dbf94ba1f2c1bb3ad55df2f38a71b4ecb38e4) )
		ROM_LOAD( "1105-1b.061",  0x040000, 0x010000, CRC(ab1c5578) SHA1(e80a1c7d2f279a523dcc9d943bd5a1ce75045d2e) )
		ROM_LOAD( "1106-2b.061",  0x050000, 0x010000, CRC(4020468e) SHA1(fa83e3d903d254c598fcbf120492ac77777ae31f) )
		ROM_LOAD( "1107-4b.061",  0x060000, 0x010000, CRC(fe6f6aed) SHA1(11bd17be3c9fe409db8268cb17515040bfd92ee2) )
		ROM_LOAD( "1108-5b.061",  0x070000, 0x010000, CRC(26fe71e1) SHA1(cac22f969c943e184a58d7bb62072f93273638de) )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "1143-10a.061", 0x000000, 0x020000, CRC(211c1049) SHA1(fcf4d9321b2871723a10b7607069da83d3402723) )
		ROM_LOAD( "1144-13a.061", 0x020000, 0x020000, CRC(ef62ed2c) SHA1(c2c21023b2f559b8a63e6ae9c59c33a3055cc465) )
		ROM_LOAD( "1145-16a.061", 0x040000, 0x020000, CRC(067ecb8a) SHA1(a42e4602e1de1cc83a30a901a9adb5519f426cff) )
		ROM_LOAD( "1146-18a.061", 0x060000, 0x020000, CRC(fea6bc92) SHA1(c502ab022fdafdef71a720237094fe95c3137d69) )
		ROM_LOAD( "1125-21a.061", 0x080000, 0x010000, CRC(c37f24ac) SHA1(341fab8244d8063a516a4a25d7ee778f708cd386) )
		ROM_RELOAD(               0x0c0000, 0x010000 )
		ROM_LOAD( "1126-23a.061", 0x090000, 0x010000, CRC(015257f0) SHA1(c5ae6a43b95ecb06a04ea877f435b1f925cff136) )
		ROM_RELOAD(               0x0d0000, 0x010000 )
		ROM_LOAD( "1127-24a.061", 0x0a0000, 0x010000, CRC(d05417cb) SHA1(5cbd54050364e82fe443ca2150c34fca84c42419) )
		ROM_RELOAD(               0x0e0000, 0x010000 )
		ROM_LOAD( "1128-25a.061", 0x0b0000, 0x010000, CRC(fba3e203) SHA1(5951571e6e64061e5448cae27af0cedd5bda2b1e) )
		ROM_RELOAD(               0x0f0000, 0x010000 )
		ROM_LOAD( "1147-10b.061", 0x100000, 0x020000, CRC(ca4308cf) SHA1(966970524915a0a5a77e3525e446b50ecde5b119) )
		ROM_LOAD( "1148-13b.061", 0x120000, 0x020000, CRC(23ddd45c) SHA1(8ee19e8982a928b56d6010f283fb2f720dc71cd6) )
		ROM_LOAD( "1149-16b.061", 0x140000, 0x020000, CRC(d77cd1d0) SHA1(148fa17c9b7a2453adf059325cb608073d1ef195) )
		ROM_LOAD( "1150-18b.061", 0x160000, 0x020000, CRC(a37157b8) SHA1(347cea600f28709fc3d942feb5cadce7def72dbb) )
		ROM_LOAD( "1129-21b.061", 0x180000, 0x010000, CRC(294aaa02) SHA1(69b42dfc444b2c9f2bb0fdcb96e2becb0df6226a) )
		ROM_RELOAD(               0x1c0000, 0x010000 )
		ROM_LOAD( "1130-23b.061", 0x190000, 0x010000, CRC(dd610817) SHA1(25f542ae4e7e77399d6df328edbc4cceb390db04) )
		ROM_RELOAD(               0x1d0000, 0x010000 )
		ROM_LOAD( "1131-24b.061", 0x1a0000, 0x010000, CRC(e8e2f919) SHA1(292dacb60db867cb2ae69942c7502af6526ab550) )
		ROM_RELOAD(               0x1e0000, 0x010000 )
		ROM_LOAD( "1132-25b.061", 0x1b0000, 0x010000, CRC(c79f8ffc) SHA1(6e90044755097387011e7cc04548bedb399b7cc3) )
		ROM_RELOAD(               0x1f0000, 0x010000 )
	
		ROM_REGION( 0x004000, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "1142-20h.061", 0x000000, 0x004000, CRC(a6ab551f) SHA1(6a11e16f3965416c81737efcb81e751484ba5ace) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_toobing = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 )	/* 8*64k for 68000 code */
		ROM_LOAD16_BYTE( "3233-1j.061",  0x000000, 0x010000, CRC(b04eb760) SHA1(760525b4f72fad47cfc457e14db70ade30a9ddac) )
		ROM_LOAD16_BYTE( "3237-1f.061",  0x000001, 0x010000, CRC(4e41a470) SHA1(3a4c9b0d93cf4cff80978c0568bb9ef9eeb878dd) )
		ROM_LOAD16_BYTE( "3234-2j.061",  0x020000, 0x010000, CRC(8c60f1b4) SHA1(0ff3f4fede83410d73027b6e7445e83044e4b21e) )
		ROM_LOAD16_BYTE( "3238-2f.061",  0x020001, 0x010000, CRC(c251b3a2) SHA1(a12add64541dc300611cd5beea642037dc8eb4d0) )
		ROM_LOAD16_BYTE( "3235-4j.061",  0x040000, 0x010000, CRC(1121b5f4) SHA1(54ef54b7104366626ffe9b9f86793de24dc5e5d4) )
		ROM_LOAD16_BYTE( "3239-4f.061",  0x040001, 0x010000, CRC(385c5a80) SHA1(e0cab1b6ac178b90f4f95d28cdf9470aad1ac92f) )
		ROM_LOAD16_BYTE( "1136-5j.061",  0x060000, 0x010000, CRC(5ae3eeac) SHA1(583b6c3f61e8ad4d98449205fedecf3e21ee993c) )
		ROM_LOAD16_BYTE( "1140-5f.061",  0x060001, 0x010000, CRC(dacbbd94) SHA1(0e3a93f439ff9f3dd57ee13604be02e9c74c8eec) )
	
		ROM_REGION( 0x14000, REGION_CPU2, 0 )	/* 64k for 6502 code */
		ROM_LOAD( "1141-2k.061",  0x010000, 0x004000, CRC(c0dcce1a) SHA1(285c13f08020cf5827eca2afcc2fa8a3a0a073e0) )
		ROM_CONTINUE(             0x004000, 0x00c000 )
	
		ROM_REGION( 0x080000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "1101-1a.061",  0x000000, 0x010000, CRC(02696f15) SHA1(51856c331c45d287e574e2e4013b62a6472ad720) )
		ROM_LOAD( "1102-2a.061",  0x010000, 0x010000, CRC(4bed4262) SHA1(eda16ece14cb60012edbe006b2839986d082822e) )
		ROM_LOAD( "1103-4a.061",  0x020000, 0x010000, CRC(e62b037f) SHA1(9a2341b822265269c07b65c4bc0fbc760c1bd456) )
		ROM_LOAD( "1104-5a.061",  0x030000, 0x010000, CRC(fa05aee6) SHA1(db0dbf94ba1f2c1bb3ad55df2f38a71b4ecb38e4) )
		ROM_LOAD( "1105-1b.061",  0x040000, 0x010000, CRC(ab1c5578) SHA1(e80a1c7d2f279a523dcc9d943bd5a1ce75045d2e) )
		ROM_LOAD( "1106-2b.061",  0x050000, 0x010000, CRC(4020468e) SHA1(fa83e3d903d254c598fcbf120492ac77777ae31f) )
		ROM_LOAD( "1107-4b.061",  0x060000, 0x010000, CRC(fe6f6aed) SHA1(11bd17be3c9fe409db8268cb17515040bfd92ee2) )
		ROM_LOAD( "1108-5b.061",  0x070000, 0x010000, CRC(26fe71e1) SHA1(cac22f969c943e184a58d7bb62072f93273638de) )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "1143-10a.061", 0x000000, 0x020000, CRC(211c1049) SHA1(fcf4d9321b2871723a10b7607069da83d3402723) )
		ROM_LOAD( "1144-13a.061", 0x020000, 0x020000, CRC(ef62ed2c) SHA1(c2c21023b2f559b8a63e6ae9c59c33a3055cc465) )
		ROM_LOAD( "1145-16a.061", 0x040000, 0x020000, CRC(067ecb8a) SHA1(a42e4602e1de1cc83a30a901a9adb5519f426cff) )
		ROM_LOAD( "1146-18a.061", 0x060000, 0x020000, CRC(fea6bc92) SHA1(c502ab022fdafdef71a720237094fe95c3137d69) )
		ROM_LOAD( "1125-21a.061", 0x080000, 0x010000, CRC(c37f24ac) SHA1(341fab8244d8063a516a4a25d7ee778f708cd386) )
		ROM_RELOAD(               0x0c0000, 0x010000 )
		ROM_LOAD( "1126-23a.061", 0x090000, 0x010000, CRC(015257f0) SHA1(c5ae6a43b95ecb06a04ea877f435b1f925cff136) )
		ROM_RELOAD(               0x0d0000, 0x010000 )
		ROM_LOAD( "1127-24a.061", 0x0a0000, 0x010000, CRC(d05417cb) SHA1(5cbd54050364e82fe443ca2150c34fca84c42419) )
		ROM_RELOAD(               0x0e0000, 0x010000 )
		ROM_LOAD( "1128-25a.061", 0x0b0000, 0x010000, CRC(fba3e203) SHA1(5951571e6e64061e5448cae27af0cedd5bda2b1e) )
		ROM_RELOAD(               0x0f0000, 0x010000 )
		ROM_LOAD( "1147-10b.061", 0x100000, 0x020000, CRC(ca4308cf) SHA1(966970524915a0a5a77e3525e446b50ecde5b119) )
		ROM_LOAD( "1148-13b.061", 0x120000, 0x020000, CRC(23ddd45c) SHA1(8ee19e8982a928b56d6010f283fb2f720dc71cd6) )
		ROM_LOAD( "1149-16b.061", 0x140000, 0x020000, CRC(d77cd1d0) SHA1(148fa17c9b7a2453adf059325cb608073d1ef195) )
		ROM_LOAD( "1150-18b.061", 0x160000, 0x020000, CRC(a37157b8) SHA1(347cea600f28709fc3d942feb5cadce7def72dbb) )
		ROM_LOAD( "1129-21b.061", 0x180000, 0x010000, CRC(294aaa02) SHA1(69b42dfc444b2c9f2bb0fdcb96e2becb0df6226a) )
		ROM_RELOAD(               0x1c0000, 0x010000 )
		ROM_LOAD( "1130-23b.061", 0x190000, 0x010000, CRC(dd610817) SHA1(25f542ae4e7e77399d6df328edbc4cceb390db04) )
		ROM_RELOAD(               0x1d0000, 0x010000 )
		ROM_LOAD( "1131-24b.061", 0x1a0000, 0x010000, CRC(e8e2f919) SHA1(292dacb60db867cb2ae69942c7502af6526ab550) )
		ROM_RELOAD(               0x1e0000, 0x010000 )
		ROM_LOAD( "1132-25b.061", 0x1b0000, 0x010000, CRC(c79f8ffc) SHA1(6e90044755097387011e7cc04548bedb399b7cc3) )
		ROM_RELOAD(               0x1f0000, 0x010000 )
	
		ROM_REGION( 0x004000, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "1142-20h.061", 0x000000, 0x004000, CRC(a6ab551f) SHA1(6a11e16f3965416c81737efcb81e751484ba5ace) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_toobin2e = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 )	/* 8*64k for 68000 code */
		ROM_LOAD16_BYTE( "2733-1j.061",  0x000000, 0x010000, CRC(a6334cf7) SHA1(39e540619c24af65bda44160a5bdaebf3600b64b) )
		ROM_LOAD16_BYTE( "2737-1f.061",  0x000001, 0x010000, CRC(9a52dd20) SHA1(a370ae3e4c7af55ea61b57a203a900f2be3ce6b9) )
		ROM_LOAD16_BYTE( "2134-2j.061",  0x020000, 0x010000, CRC(2b8164c8) SHA1(aeeaff9df9fda23b295b59efadf52160f084d256) )
		ROM_LOAD16_BYTE( "2138-2f.061",  0x020001, 0x010000, CRC(c09cadbd) SHA1(93598a512d17664c111e3d88397fde37a492b4a6) )
		ROM_LOAD16_BYTE( "2135-4j.061",  0x040000, 0x010000, CRC(90477c4a) SHA1(69b4bcf5c329d8710d0985ce3e45bd40a7102a91) )
		ROM_LOAD16_BYTE( "2139-4f.061",  0x040001, 0x010000, CRC(47936958) SHA1(ac7c99272f3b21d15e5673d2e8f206d60c32f4f9) )
		ROM_LOAD16_BYTE( "1136-5j.061",  0x060000, 0x010000, CRC(5ae3eeac) SHA1(583b6c3f61e8ad4d98449205fedecf3e21ee993c) )
		ROM_LOAD16_BYTE( "1140-5f.061",  0x060001, 0x010000, CRC(dacbbd94) SHA1(0e3a93f439ff9f3dd57ee13604be02e9c74c8eec) )
	
		ROM_REGION( 0x14000, REGION_CPU2, 0 )	/* 64k for 6502 code */
		ROM_LOAD( "1141-2k.061",  0x010000, 0x004000, CRC(c0dcce1a) SHA1(285c13f08020cf5827eca2afcc2fa8a3a0a073e0) )
		ROM_CONTINUE(             0x004000, 0x00c000 )
	
		ROM_REGION( 0x080000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "1101-1a.061",  0x000000, 0x010000, CRC(02696f15) SHA1(51856c331c45d287e574e2e4013b62a6472ad720) )
		ROM_LOAD( "1102-2a.061",  0x010000, 0x010000, CRC(4bed4262) SHA1(eda16ece14cb60012edbe006b2839986d082822e) )
		ROM_LOAD( "1103-4a.061",  0x020000, 0x010000, CRC(e62b037f) SHA1(9a2341b822265269c07b65c4bc0fbc760c1bd456) )
		ROM_LOAD( "1104-5a.061",  0x030000, 0x010000, CRC(fa05aee6) SHA1(db0dbf94ba1f2c1bb3ad55df2f38a71b4ecb38e4) )
		ROM_LOAD( "1105-1b.061",  0x040000, 0x010000, CRC(ab1c5578) SHA1(e80a1c7d2f279a523dcc9d943bd5a1ce75045d2e) )
		ROM_LOAD( "1106-2b.061",  0x050000, 0x010000, CRC(4020468e) SHA1(fa83e3d903d254c598fcbf120492ac77777ae31f) )
		ROM_LOAD( "1107-4b.061",  0x060000, 0x010000, CRC(fe6f6aed) SHA1(11bd17be3c9fe409db8268cb17515040bfd92ee2) )
		ROM_LOAD( "1108-5b.061",  0x070000, 0x010000, CRC(26fe71e1) SHA1(cac22f969c943e184a58d7bb62072f93273638de) )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "1143-10a.061", 0x000000, 0x020000, CRC(211c1049) SHA1(fcf4d9321b2871723a10b7607069da83d3402723) )
		ROM_LOAD( "1144-13a.061", 0x020000, 0x020000, CRC(ef62ed2c) SHA1(c2c21023b2f559b8a63e6ae9c59c33a3055cc465) )
		ROM_LOAD( "1145-16a.061", 0x040000, 0x020000, CRC(067ecb8a) SHA1(a42e4602e1de1cc83a30a901a9adb5519f426cff) )
		ROM_LOAD( "1146-18a.061", 0x060000, 0x020000, CRC(fea6bc92) SHA1(c502ab022fdafdef71a720237094fe95c3137d69) )
		ROM_LOAD( "1125-21a.061", 0x080000, 0x010000, CRC(c37f24ac) SHA1(341fab8244d8063a516a4a25d7ee778f708cd386) )
		ROM_RELOAD(               0x0c0000, 0x010000 )
		ROM_LOAD( "1126-23a.061", 0x090000, 0x010000, CRC(015257f0) SHA1(c5ae6a43b95ecb06a04ea877f435b1f925cff136) )
		ROM_RELOAD(               0x0d0000, 0x010000 )
		ROM_LOAD( "1127-24a.061", 0x0a0000, 0x010000, CRC(d05417cb) SHA1(5cbd54050364e82fe443ca2150c34fca84c42419) )
		ROM_RELOAD(               0x0e0000, 0x010000 )
		ROM_LOAD( "1128-25a.061", 0x0b0000, 0x010000, CRC(fba3e203) SHA1(5951571e6e64061e5448cae27af0cedd5bda2b1e) )
		ROM_RELOAD(               0x0f0000, 0x010000 )
		ROM_LOAD( "1147-10b.061", 0x100000, 0x020000, CRC(ca4308cf) SHA1(966970524915a0a5a77e3525e446b50ecde5b119) )
		ROM_LOAD( "1148-13b.061", 0x120000, 0x020000, CRC(23ddd45c) SHA1(8ee19e8982a928b56d6010f283fb2f720dc71cd6) )
		ROM_LOAD( "1149-16b.061", 0x140000, 0x020000, CRC(d77cd1d0) SHA1(148fa17c9b7a2453adf059325cb608073d1ef195) )
		ROM_LOAD( "1150-18b.061", 0x160000, 0x020000, CRC(a37157b8) SHA1(347cea600f28709fc3d942feb5cadce7def72dbb) )
		ROM_LOAD( "1129-21b.061", 0x180000, 0x010000, CRC(294aaa02) SHA1(69b42dfc444b2c9f2bb0fdcb96e2becb0df6226a) )
		ROM_RELOAD(               0x1c0000, 0x010000 )
		ROM_LOAD( "1130-23b.061", 0x190000, 0x010000, CRC(dd610817) SHA1(25f542ae4e7e77399d6df328edbc4cceb390db04) )
		ROM_RELOAD(               0x1d0000, 0x010000 )
		ROM_LOAD( "1131-24b.061", 0x1a0000, 0x010000, CRC(e8e2f919) SHA1(292dacb60db867cb2ae69942c7502af6526ab550) )
		ROM_RELOAD(               0x1e0000, 0x010000 )
		ROM_LOAD( "1132-25b.061", 0x1b0000, 0x010000, CRC(c79f8ffc) SHA1(6e90044755097387011e7cc04548bedb399b7cc3) )
		ROM_RELOAD(               0x1f0000, 0x010000 )
	
		ROM_REGION( 0x004000, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "1142-20h.061", 0x000000, 0x004000, CRC(a6ab551f) SHA1(6a11e16f3965416c81737efcb81e751484ba5ace) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_toobin2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 )	/* 8*64k for 68000 code */
		ROM_LOAD16_BYTE( "2133-1j.061",  0x000000, 0x010000, CRC(2c3382e4) SHA1(39919e9b5b586b630e0581adabfe25d83b2bfaef) )
		ROM_LOAD16_BYTE( "2137-1f.061",  0x000001, 0x010000, CRC(891c74b1) SHA1(2f39d0e4934ccf48bb5fc0737f34fc5a65cfd903) )
		ROM_LOAD16_BYTE( "2134-2j.061",  0x020000, 0x010000, CRC(2b8164c8) SHA1(aeeaff9df9fda23b295b59efadf52160f084d256) )
		ROM_LOAD16_BYTE( "2138-2f.061",  0x020001, 0x010000, CRC(c09cadbd) SHA1(93598a512d17664c111e3d88397fde37a492b4a6) )
		ROM_LOAD16_BYTE( "2135-4j.061",  0x040000, 0x010000, CRC(90477c4a) SHA1(69b4bcf5c329d8710d0985ce3e45bd40a7102a91) )
		ROM_LOAD16_BYTE( "2139-4f.061",  0x040001, 0x010000, CRC(47936958) SHA1(ac7c99272f3b21d15e5673d2e8f206d60c32f4f9) )
		ROM_LOAD16_BYTE( "1136-5j.061",  0x060000, 0x010000, CRC(5ae3eeac) SHA1(583b6c3f61e8ad4d98449205fedecf3e21ee993c) )
		ROM_LOAD16_BYTE( "1140-5f.061",  0x060001, 0x010000, CRC(dacbbd94) SHA1(0e3a93f439ff9f3dd57ee13604be02e9c74c8eec) )
	
		ROM_REGION( 0x14000, REGION_CPU2, 0 )	/* 64k for 6502 code */
		ROM_LOAD( "1141-2k.061",  0x010000, 0x004000, CRC(c0dcce1a) SHA1(285c13f08020cf5827eca2afcc2fa8a3a0a073e0) )
		ROM_CONTINUE(             0x004000, 0x00c000 )
	
		ROM_REGION( 0x080000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "1101-1a.061",  0x000000, 0x010000, CRC(02696f15) SHA1(51856c331c45d287e574e2e4013b62a6472ad720) )
		ROM_LOAD( "1102-2a.061",  0x010000, 0x010000, CRC(4bed4262) SHA1(eda16ece14cb60012edbe006b2839986d082822e) )
		ROM_LOAD( "1103-4a.061",  0x020000, 0x010000, CRC(e62b037f) SHA1(9a2341b822265269c07b65c4bc0fbc760c1bd456) )
		ROM_LOAD( "1104-5a.061",  0x030000, 0x010000, CRC(fa05aee6) SHA1(db0dbf94ba1f2c1bb3ad55df2f38a71b4ecb38e4) )
		ROM_LOAD( "1105-1b.061",  0x040000, 0x010000, CRC(ab1c5578) SHA1(e80a1c7d2f279a523dcc9d943bd5a1ce75045d2e) )
		ROM_LOAD( "1106-2b.061",  0x050000, 0x010000, CRC(4020468e) SHA1(fa83e3d903d254c598fcbf120492ac77777ae31f) )
		ROM_LOAD( "1107-4b.061",  0x060000, 0x010000, CRC(fe6f6aed) SHA1(11bd17be3c9fe409db8268cb17515040bfd92ee2) )
		ROM_LOAD( "1108-5b.061",  0x070000, 0x010000, CRC(26fe71e1) SHA1(cac22f969c943e184a58d7bb62072f93273638de) )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "1143-10a.061", 0x000000, 0x020000, CRC(211c1049) SHA1(fcf4d9321b2871723a10b7607069da83d3402723) )
		ROM_LOAD( "1144-13a.061", 0x020000, 0x020000, CRC(ef62ed2c) SHA1(c2c21023b2f559b8a63e6ae9c59c33a3055cc465) )
		ROM_LOAD( "1145-16a.061", 0x040000, 0x020000, CRC(067ecb8a) SHA1(a42e4602e1de1cc83a30a901a9adb5519f426cff) )
		ROM_LOAD( "1146-18a.061", 0x060000, 0x020000, CRC(fea6bc92) SHA1(c502ab022fdafdef71a720237094fe95c3137d69) )
		ROM_LOAD( "1125-21a.061", 0x080000, 0x010000, CRC(c37f24ac) SHA1(341fab8244d8063a516a4a25d7ee778f708cd386) )
		ROM_RELOAD(               0x0c0000, 0x010000 )
		ROM_LOAD( "1126-23a.061", 0x090000, 0x010000, CRC(015257f0) SHA1(c5ae6a43b95ecb06a04ea877f435b1f925cff136) )
		ROM_RELOAD(               0x0d0000, 0x010000 )
		ROM_LOAD( "1127-24a.061", 0x0a0000, 0x010000, CRC(d05417cb) SHA1(5cbd54050364e82fe443ca2150c34fca84c42419) )
		ROM_RELOAD(               0x0e0000, 0x010000 )
		ROM_LOAD( "1128-25a.061", 0x0b0000, 0x010000, CRC(fba3e203) SHA1(5951571e6e64061e5448cae27af0cedd5bda2b1e) )
		ROM_RELOAD(               0x0f0000, 0x010000 )
		ROM_LOAD( "1147-10b.061", 0x100000, 0x020000, CRC(ca4308cf) SHA1(966970524915a0a5a77e3525e446b50ecde5b119) )
		ROM_LOAD( "1148-13b.061", 0x120000, 0x020000, CRC(23ddd45c) SHA1(8ee19e8982a928b56d6010f283fb2f720dc71cd6) )
		ROM_LOAD( "1149-16b.061", 0x140000, 0x020000, CRC(d77cd1d0) SHA1(148fa17c9b7a2453adf059325cb608073d1ef195) )
		ROM_LOAD( "1150-18b.061", 0x160000, 0x020000, CRC(a37157b8) SHA1(347cea600f28709fc3d942feb5cadce7def72dbb) )
		ROM_LOAD( "1129-21b.061", 0x180000, 0x010000, CRC(294aaa02) SHA1(69b42dfc444b2c9f2bb0fdcb96e2becb0df6226a) )
		ROM_RELOAD(               0x1c0000, 0x010000 )
		ROM_LOAD( "1130-23b.061", 0x190000, 0x010000, CRC(dd610817) SHA1(25f542ae4e7e77399d6df328edbc4cceb390db04) )
		ROM_RELOAD(               0x1d0000, 0x010000 )
		ROM_LOAD( "1131-24b.061", 0x1a0000, 0x010000, CRC(e8e2f919) SHA1(292dacb60db867cb2ae69942c7502af6526ab550) )
		ROM_RELOAD(               0x1e0000, 0x010000 )
		ROM_LOAD( "1132-25b.061", 0x1b0000, 0x010000, CRC(c79f8ffc) SHA1(6e90044755097387011e7cc04548bedb399b7cc3) )
		ROM_RELOAD(               0x1f0000, 0x010000 )
	
		ROM_REGION( 0x004000, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "1142-20h.061", 0x000000, 0x004000, CRC(a6ab551f) SHA1(6a11e16f3965416c81737efcb81e751484ba5ace) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_toobin1 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 )	/* 8*64k for 68000 code */
		ROM_LOAD16_BYTE( "1133-1j.061",  0x000000, 0x010000, CRC(caeb5d1b) SHA1(8036871a04b5206fd383ac0fd9a9d3218128088b) )
		ROM_LOAD16_BYTE( "1137-1f.061",  0x000001, 0x010000, CRC(9713d9d3) SHA1(55791150312de201bdd330bfd4cbb132cb3959e4) )
		ROM_LOAD16_BYTE( "1134-2j.061",  0x020000, 0x010000, CRC(119f5d7b) SHA1(edd0b1ab29bb9c15c3b80037635c3b6d5fb434dc) )
		ROM_LOAD16_BYTE( "1138-2f.061",  0x020001, 0x010000, CRC(89664841) SHA1(4ace8e4fd0026d0d73726d339a71d841652fdc87) )
		ROM_LOAD16_BYTE( "1135-4j.061",  0x040000, 0x010000, CRC(90477c4a) SHA1(69b4bcf5c329d8710d0985ce3e45bd40a7102a91) )
		ROM_LOAD16_BYTE( "1139-4f.061",  0x040001, 0x010000, CRC(a9f082a9) SHA1(b1d45e528d466efa3f7562c80d2ee0c8913a33a6) )
		ROM_LOAD16_BYTE( "1136-5j.061",  0x060000, 0x010000, CRC(5ae3eeac) SHA1(583b6c3f61e8ad4d98449205fedecf3e21ee993c) )
		ROM_LOAD16_BYTE( "1140-5f.061",  0x060001, 0x010000, CRC(dacbbd94) SHA1(0e3a93f439ff9f3dd57ee13604be02e9c74c8eec) )
	
		ROM_REGION( 0x14000, REGION_CPU2, 0 )	/* 64k for 6502 code */
		ROM_LOAD( "1141-2k.061",  0x010000, 0x004000, CRC(c0dcce1a) SHA1(285c13f08020cf5827eca2afcc2fa8a3a0a073e0) )
		ROM_CONTINUE(             0x004000, 0x00c000 )
	
		ROM_REGION( 0x080000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "1101-1a.061",  0x000000, 0x010000, CRC(02696f15) SHA1(51856c331c45d287e574e2e4013b62a6472ad720) )
		ROM_LOAD( "1102-2a.061",  0x010000, 0x010000, CRC(4bed4262) SHA1(eda16ece14cb60012edbe006b2839986d082822e) )
		ROM_LOAD( "1103-4a.061",  0x020000, 0x010000, CRC(e62b037f) SHA1(9a2341b822265269c07b65c4bc0fbc760c1bd456) )
		ROM_LOAD( "1104-5a.061",  0x030000, 0x010000, CRC(fa05aee6) SHA1(db0dbf94ba1f2c1bb3ad55df2f38a71b4ecb38e4) )
		ROM_LOAD( "1105-1b.061",  0x040000, 0x010000, CRC(ab1c5578) SHA1(e80a1c7d2f279a523dcc9d943bd5a1ce75045d2e) )
		ROM_LOAD( "1106-2b.061",  0x050000, 0x010000, CRC(4020468e) SHA1(fa83e3d903d254c598fcbf120492ac77777ae31f) )
		ROM_LOAD( "1107-4b.061",  0x060000, 0x010000, CRC(fe6f6aed) SHA1(11bd17be3c9fe409db8268cb17515040bfd92ee2) )
		ROM_LOAD( "1108-5b.061",  0x070000, 0x010000, CRC(26fe71e1) SHA1(cac22f969c943e184a58d7bb62072f93273638de) )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "1143-10a.061", 0x000000, 0x020000, CRC(211c1049) SHA1(fcf4d9321b2871723a10b7607069da83d3402723) )
		ROM_LOAD( "1144-13a.061", 0x020000, 0x020000, CRC(ef62ed2c) SHA1(c2c21023b2f559b8a63e6ae9c59c33a3055cc465) )
		ROM_LOAD( "1145-16a.061", 0x040000, 0x020000, CRC(067ecb8a) SHA1(a42e4602e1de1cc83a30a901a9adb5519f426cff) )
		ROM_LOAD( "1146-18a.061", 0x060000, 0x020000, CRC(fea6bc92) SHA1(c502ab022fdafdef71a720237094fe95c3137d69) )
		ROM_LOAD( "1125-21a.061", 0x080000, 0x010000, CRC(c37f24ac) SHA1(341fab8244d8063a516a4a25d7ee778f708cd386) )
		ROM_RELOAD(               0x0c0000, 0x010000 )
		ROM_LOAD( "1126-23a.061", 0x090000, 0x010000, CRC(015257f0) SHA1(c5ae6a43b95ecb06a04ea877f435b1f925cff136) )
		ROM_RELOAD(               0x0d0000, 0x010000 )
		ROM_LOAD( "1127-24a.061", 0x0a0000, 0x010000, CRC(d05417cb) SHA1(5cbd54050364e82fe443ca2150c34fca84c42419) )
		ROM_RELOAD(               0x0e0000, 0x010000 )
		ROM_LOAD( "1128-25a.061", 0x0b0000, 0x010000, CRC(fba3e203) SHA1(5951571e6e64061e5448cae27af0cedd5bda2b1e) )
		ROM_RELOAD(               0x0f0000, 0x010000 )
		ROM_LOAD( "1147-10b.061", 0x100000, 0x020000, CRC(ca4308cf) SHA1(966970524915a0a5a77e3525e446b50ecde5b119) )
		ROM_LOAD( "1148-13b.061", 0x120000, 0x020000, CRC(23ddd45c) SHA1(8ee19e8982a928b56d6010f283fb2f720dc71cd6) )
		ROM_LOAD( "1149-16b.061", 0x140000, 0x020000, CRC(d77cd1d0) SHA1(148fa17c9b7a2453adf059325cb608073d1ef195) )
		ROM_LOAD( "1150-18b.061", 0x160000, 0x020000, CRC(a37157b8) SHA1(347cea600f28709fc3d942feb5cadce7def72dbb) )
		ROM_LOAD( "1129-21b.061", 0x180000, 0x010000, CRC(294aaa02) SHA1(69b42dfc444b2c9f2bb0fdcb96e2becb0df6226a) )
		ROM_RELOAD(               0x1c0000, 0x010000 )
		ROM_LOAD( "1130-23b.061", 0x190000, 0x010000, CRC(dd610817) SHA1(25f542ae4e7e77399d6df328edbc4cceb390db04) )
		ROM_RELOAD(               0x1d0000, 0x010000 )
		ROM_LOAD( "1131-24b.061", 0x1a0000, 0x010000, CRC(e8e2f919) SHA1(292dacb60db867cb2ae69942c7502af6526ab550) )
		ROM_RELOAD(               0x1e0000, 0x010000 )
		ROM_LOAD( "1132-25b.061", 0x1b0000, 0x010000, CRC(c79f8ffc) SHA1(6e90044755097387011e7cc04548bedb399b7cc3) )
		ROM_RELOAD(               0x1f0000, 0x010000 )
	
		ROM_REGION( 0x004000, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "1142-20h.061", 0x000000, 0x004000, CRC(a6ab551f) SHA1(6a11e16f3965416c81737efcb81e751484ba5ace) )
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Driver initialization
	 *
	 *************************************/
	
	public static DriverInitHandlerPtr init_toobin  = new DriverInitHandlerPtr() { public void handler(){
		atarigen_eeprom_default = NULL;
		atarijsa_init(1, 2, 1, 0x1000);
	} };
	
	
	
	/*************************************
	 *
	 *	Game driver(s)
	 *
	 *************************************/
	
	GAME( 1988, toobin,   0,      toobin, toobin, toobin, ROT270, "Atari Games", "Toobin' (rev 3)" )
	GAME( 1988, toobine,  toobin, toobin, toobin, toobin, ROT270, "Atari Games", "Toobin' (Europe, rev 3)" )
	GAME( 1988, toobing,  toobin, toobin, toobin, toobin, ROT270, "Atari Games", "Toobin' (German, rev 3)" )
	GAME( 1988, toobin2,  toobin, toobin, toobin, toobin, ROT270, "Atari Games", "Toobin' (rev 2)" )
	GAME( 1988, toobin2e, toobin, toobin, toobin, toobin, ROT270, "Atari Games", "Toobin' (Europe, rev 2)" )
	GAME( 1988, toobin1,  toobin, toobin, toobin, toobin, ROT270, "Atari Games", "Toobin' (rev 1)" )
}
