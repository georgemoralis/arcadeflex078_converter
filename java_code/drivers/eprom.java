/***************************************************************************

	Atari Escape hardware

	driver by Aaron Giles

	Games supported:
		* Escape From The Planet Of The Robot Monsters (1989) [2 sets]
		* Klax prototypes [2 sets]

	Known bugs:
		* none at this time

****************************************************************************

	Memory map (TBA)

***************************************************************************/


/*
 * ported to v0.78
 * using automatic conversion tool v0.02
 */ 
package arcadeflex.v078.drivers;

public class eprom
{
	
	
	
	/*************************************
	 *
	 *	Statics
	 *
	 *************************************/
	
	static data16_t *sync_data;
	
	
	
	/*************************************
	 *
	 *	Initialization
	 *
	 *************************************/
	
	static void update_interrupts(void)
	{
		int newstate = 0;
		int newstate2 = 0;
	
		if (atarigen_video_int_state != 0)
			newstate |= 4, newstate2 |= 4;
		if (atarigen_sound_int_state != 0)
			newstate |= 6;
	
		if (newstate != 0)
			cpu_set_irq_line(0, newstate, ASSERT_LINE);
		else
			cpu_set_irq_line(0, 7, CLEAR_LINE);
	
		if (newstate2 != 0)
			cpu_set_irq_line(1, newstate2, ASSERT_LINE);
		else
			cpu_set_irq_line(1, 7, CLEAR_LINE);
	}
	
	
	public static MachineInitHandlerPtr machine_init_eprom  = new MachineInitHandlerPtr() { public void handler()
	{
		atarigen_eeprom_reset();
		atarigen_interrupt_reset(update_interrupts);
		atarigen_scanline_timer_reset(eprom_scanline_update, 8);
		atarijsa_reset();
	} };
	
	
	
	/*************************************
	 *
	 *	I/O handling
	 *
	 *************************************/
	
	static READ16_HANDLER( special_port1_r )
	{
		int result = readinputport(1);
	
		if (atarigen_sound_to_cpu_ready != 0) result ^= 0x0004;
		if (atarigen_cpu_to_sound_ready != 0) result ^= 0x0008;
		result ^= 0x0010;
	
		return result;
	}
	
	
	static READ16_HANDLER( adc_r )
	{
		static int last_offset;
		int result = readinputport(2 + (last_offset & 3));
		last_offset = offset;
		return result;
	}
	
	
	
	/*************************************
	 *
	 *	Latch write handler
	 *
	 *************************************/
	
	static WRITE16_HANDLER( eprom_latch_w )
	{
		/* reset extra CPU */
		if (ACCESSING_LSB != 0)
		{
			if ((data & 1) != 0)
				cpu_set_reset_line(1, CLEAR_LINE);
			else
				cpu_set_reset_line(1, ASSERT_LINE);
		}
	}
	
	
	
	/*************************************
	 *
	 *	Synchronization
	 *
	 *************************************/
	
	static READ16_HANDLER( sync_r )
	{
		return sync_data[offset];
	}
	
	
	static WRITE16_HANDLER( sync_w )
	{
		int oldword = sync_data[offset];
		int newword = oldword;
		COMBINE_DATA(&newword);
	
		sync_data[offset] = newword;
		if ((oldword & 0xff00) != (newword & 0xff00))
			cpu_yield();
	}
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	static MEMORY_READ16_START( main_readmem )
		{ 0x000000, 0x09ffff, MRA16_ROM },
		{ 0x0e0000, 0x0e0fff, atarigen_eeprom_r },
		{ 0x160000, 0x16ffff, MRA16_BANK1 },
		{ 0x260000, 0x26000f, input_port_0_word_r },
		{ 0x260010, 0x26001f, special_port1_r },
		{ 0x260020, 0x26002f, adc_r },
		{ 0x260030, 0x260031, atarigen_sound_r },
		{ 0x3e0000, 0x3e0fff, MRA16_RAM },
		{ 0x3f0000, 0x3f9fff, MRA16_RAM },
	MEMORY_END
	
	
	static MEMORY_WRITE16_START( main_writemem )
		{ 0x000000, 0x09ffff, MWA16_ROM },
		{ 0x0e0000, 0x0e0fff, atarigen_eeprom_w, &atarigen_eeprom, &atarigen_eeprom_size },
		{ 0x160000, 0x16ffff, MWA16_BANK1 },	/* shared */
		{ 0x1f0000, 0x1fffff, atarigen_eeprom_enable_w },
		{ 0x2e0000, 0x2e0001, watchdog_reset16_w },
		{ 0x360000, 0x360001, atarigen_video_int_ack_w },
		{ 0x360010, 0x360011, eprom_latch_w },
		{ 0x360020, 0x360021, atarigen_sound_reset_w },
		{ 0x360030, 0x360031, atarigen_sound_w },
		{ 0x3e0000, 0x3e0fff, paletteram16_IIIIRRRRGGGGBBBB_word_w, &paletteram16 },
		{ 0x3f0000, 0x3f1fff, atarigen_playfield_w, &atarigen_playfield },
		{ 0x3f2000, 0x3f3fff, atarimo_0_spriteram_w, &atarimo_0_spriteram },
		{ 0x3f4000, 0x3f4f7f, atarigen_alpha_w, &atarigen_alpha },
		{ 0x3f4f80, 0x3f4fff, atarimo_0_slipram_w, &atarimo_0_slipram },
		{ 0x3f5000, 0x3f7fff, MWA16_RAM },
		{ 0x3f8000, 0x3f9fff, atarigen_playfield_upper_w, &atarigen_playfield_upper },
	MEMORY_END
	
	
	
	/*************************************
	 *
	 *	Extra CPU memory handlers
	 *
	 *************************************/
	
	static MEMORY_READ16_START( extra_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0x16cc00, 0x16cc01, sync_r },
		{ 0x160000, 0x16ffff, MRA16_BANK1 },
		{ 0x260000, 0x26000f, input_port_0_word_r },
		{ 0x260010, 0x26001f, special_port1_r },
		{ 0x260020, 0x26002f, adc_r },
		{ 0x260030, 0x260031, atarigen_sound_r },
	MEMORY_END
	
	
	static MEMORY_WRITE16_START( extra_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x16cc00, 0x16cc01, sync_w, &sync_data },
		{ 0x160000, 0x16ffff, MWA16_BANK1 },	/* shared */
		{ 0x360000, 0x360001, atarigen_video_int_ack_w },
		{ 0x360010, 0x360011, eprom_latch_w },
		{ 0x360020, 0x360021, atarigen_sound_reset_w },
		{ 0x360030, 0x360031, atarigen_sound_w },
	MEMORY_END
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	static InputPortPtr input_ports_eprom = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* 26000 */
		PORT_BIT( 0x00ff, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0xf000, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 		/* 26010 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_SERVICE( 0x0002, IP_ACTIVE_LOW );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_UNUSED );/* Input buffer full (@260030) */
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_UNUSED );/* Output buffer full (@360030) */
		PORT_BIT( 0x0010, IP_ACTIVE_HIGH, IPT_UNUSED );/* ADEOC, end of conversion */
		PORT_BIT( 0x00e0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0xf000, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* ADC0 @ 0x260020 */
		PORT_ANALOG( 0x00ff, 0x0080, IPT_AD_STICK_Y | IPF_PLAYER1, 100, 10, 0x10, 0xf0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* ADC1 @ 0x260022 */
		PORT_ANALOG( 0x00ff, 0x0080, IPT_AD_STICK_X | IPF_REVERSE | IPF_PLAYER1, 100, 10, 0x10, 0xf0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* ADC0 @ 0x260024 */
		PORT_ANALOG( 0x00ff, 0x0080, IPT_AD_STICK_Y | IPF_PLAYER2, 100, 10, 0x10, 0xf0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* ADC1 @ 0x260026 */
		PORT_ANALOG( 0x00ff, 0x0080, IPT_AD_STICK_X | IPF_REVERSE | IPF_PLAYER2, 100, 10, 0x10, 0xf0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		JSA_I_PORT	/* audio board port */
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_klaxp = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* 26000 */
		PORT_BIT( 0x00ff, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 );
	
		PORT_START(); 		/* 26010 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_SERVICE( 0x0002, IP_ACTIVE_LOW );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_UNUSED );/* Input buffer full (@260030) */
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_UNUSED );/* Output buffer full (@360030) */
		PORT_BIT( 0x0010, IP_ACTIVE_HIGH, IPT_UNUSED );/* ADEOC, end of conversion */
		PORT_BIT( 0x00e0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 );
	
		JSA_II_PORT	/* audio board port */
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
	
	
	static GfxLayout pfmolayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(0,4), RGN_FRAC(1,4), RGN_FRAC(2,4), RGN_FRAC(3,4) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, pfmolayout,  256, 32 ),	/* sprites  playfield */
		new GfxDecodeInfo( REGION_GFX2, 0, anlayout,      0, 64 ),	/* characters 8x8 */
		new GfxDecodeInfo( -1 )
	};
	
	
	
	/*************************************
	 *
	 *	Machine drivers
	 *
	 *************************************/
	
	public static MachineHandlerPtr machine_driver_eprom = new MachineHandlerPtr() {
        public void handler(InternalMachineDriver machine) {
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, ATARI_CLOCK_14MHz/2)
		MDRV_CPU_MEMORY(main_readmem,main_writemem)
		MDRV_CPU_VBLANK_INT(atarigen_video_int_gen,1)
		
		MDRV_CPU_ADD(M68000, ATARI_CLOCK_14MHz/2)
		MDRV_CPU_MEMORY(extra_readmem,extra_writemem)
		
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_INTERLEAVE(10)
		
		MDRV_MACHINE_INIT(eprom)
		MDRV_NVRAM_HANDLER(atarigen)
		
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_NEEDS_6BITS_PER_GUN | VIDEO_UPDATE_BEFORE_VBLANK)
		MDRV_SCREEN_SIZE(42*8, 30*8)
		MDRV_VISIBLE_AREA(0*8, 42*8-1, 0*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(2048)
		
		MDRV_VIDEO_START(eprom)
		MDRV_VIDEO_UPDATE(eprom)
		
		/* sound hardware */
		MDRV_IMPORT_FROM(jsa_i_mono_speech)
	MACHINE_DRIVER_END();
 }
};
	
	
	public static MachineHandlerPtr machine_driver_klaxp = new MachineHandlerPtr() {
        public void handler(InternalMachineDriver machine) {
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, ATARI_CLOCK_14MHz/2)
		MDRV_CPU_MEMORY(main_readmem,main_writemem)
		MDRV_CPU_VBLANK_INT(atarigen_video_int_gen,1)
		
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_INTERLEAVE(10)
		
		MDRV_MACHINE_INIT(eprom)
		MDRV_NVRAM_HANDLER(atarigen)
		
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_NEEDS_6BITS_PER_GUN | VIDEO_UPDATE_BEFORE_VBLANK)
		MDRV_SCREEN_SIZE(42*8, 30*8)
		MDRV_VISIBLE_AREA(0*8, 42*8-1, 0*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(2048)
		
		MDRV_VIDEO_START(eprom)
		MDRV_VIDEO_UPDATE(eprom)
		
		/* sound hardware */
		MDRV_IMPORT_FROM(jsa_ii_mono)
	MACHINE_DRIVER_END();
 }
};
	
	
	
	/*************************************
	 *
	 *	ROM definition(s)
	 *
	 *************************************/
	
	static RomLoadPtr rom_eprom = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xa0000, REGION_CPU1, 0 )	/* 10*64k for 68000 code */
		ROM_LOAD16_BYTE( "136069.50a",   0x00000, 0x10000, CRC(08888dec) SHA1(a0a137828b9e1efbdbc0e5ddaf4d73d24b36948a) )
		ROM_LOAD16_BYTE( "136069.40a",   0x00001, 0x10000, CRC(29cb1e97) SHA1(ccf7024dccbd61983d61450f15c805422e4eee09) )
		ROM_LOAD16_BYTE( "136069.50b",   0x20000, 0x10000, CRC(702241c9) SHA1(cba27e92f64fd201c16aed6a8f2dc64c4f887e4f) )
		ROM_LOAD16_BYTE( "136069.40b",   0x20001, 0x10000, CRC(fecbf9e2) SHA1(cd06bfab296e9496564fc2716b26874b55dc2188) )
		ROM_LOAD16_BYTE( "136069.50d",   0x40000, 0x10000, CRC(0f2f1502) SHA1(2aa65c03d4cd94839a2c2ba338177202bc1185ee) )
		ROM_LOAD16_BYTE( "136069.40d",   0x40001, 0x10000, CRC(bc6f6ae8) SHA1(43a947cf9db7cda825924e167529305f63bb2a5c) )
		ROM_LOAD16_BYTE( "136069.40k",   0x60000, 0x10000, CRC(130650f6) SHA1(bea7780d54a4e1f3e93f14494c82446a4bb48e19) )
		ROM_LOAD16_BYTE( "136069.50k",   0x60001, 0x10000, CRC(1da21ed8) SHA1(3b00e3cf5a25918c1f3158d8b2192158f77cb521) )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 )	/* 8*64k for 68000 code */
		ROM_LOAD16_BYTE( "136069.10s",   0x00000, 0x10000, CRC(deff6469) SHA1(2fe4d42d60965581579e8edad49b86fbd321d1db) )
		ROM_LOAD16_BYTE( "136069.10u",   0x00001, 0x10000, CRC(5d7afca2) SHA1(a37ecd2909049dd0b3ddbe602f0173c44b065f6f) )
		ROM_COPY( REGION_CPU1, 0x60000,  0x60000, 0x20000 )
	
		ROM_REGION( 0x14000, REGION_CPU3, 0 )	/* 64k + 16k for 6502 code */
		ROM_LOAD( "136069.7b",    0x10000, 0x4000, CRC(86e93695) SHA1(63ddab02df139dd41a8260c303798b2a550b9fe6) )
		ROM_CONTINUE(             0x04000, 0xc000 )
	
		ROM_REGION( 0x100000, REGION_GFX1, ROMREGION_DISPOSE | ROMREGION_INVERT )
		ROM_LOAD( "136069.47s",   0x00000, 0x10000, CRC(0de9d98d) SHA1(c2f963a8a4573e135a2825929cbc5535ce3b0215) )
		ROM_LOAD( "136069.43s",   0x10000, 0x10000, CRC(8eb106ad) SHA1(ece0ddba8fafe6e720f843c4d3f69ae654ae9d92) )
		ROM_LOAD( "136069.38s",   0x20000, 0x10000, CRC(bf3d0e18) SHA1(c81dacd06ce2580e37ff480d1182ab6c7e74d600) )
		ROM_LOAD( "136069.32s",   0x30000, 0x10000, CRC(48fb2e42) SHA1(480edc87f7ca547c3d8e09bf1a98e04ac464f4c6) )
		ROM_LOAD( "136069.76s",   0x40000, 0x10000, CRC(602d939d) SHA1(2ce9899f4cf0786df8c5f0e8cc63ce5206ea514f) )
		ROM_LOAD( "136069.70s",   0x50000, 0x10000, CRC(f6c973af) SHA1(048d5a9b89cb83186ca594e71521675248970735) )
		ROM_LOAD( "136069.64s",   0x60000, 0x10000, CRC(9cd52e30) SHA1(59233a87f2b50e9390f297abe7489864222f98e2) )
		ROM_LOAD( "136069.57s",   0x70000, 0x10000, CRC(4e2c2e7e) SHA1(6bf203e8c029d955634dcbaef9a6932d42035b25) )
		ROM_LOAD( "136069.47u",   0x80000, 0x10000, CRC(e7edcced) SHA1(4c19ea8b15332681bfc73a3d2b063985c1bbac1d) )
		ROM_LOAD( "136069.43u",   0x90000, 0x10000, CRC(9d3e144d) SHA1(7f4c7ee14d10a733f8b4169b41023bda1b5702c8) )
		ROM_LOAD( "136069.38u",   0xa0000, 0x10000, CRC(23f40437) SHA1(567aa09a986dd8765c54f413f906e1cb323568c6) )
		ROM_LOAD( "136069.32u",   0xb0000, 0x10000, CRC(2a47ff7b) SHA1(89935eac8fbeed87668fe1dcb4645c96a9df2c03) )
		ROM_LOAD( "136069.76u",   0xc0000, 0x10000, CRC(b0cead58) SHA1(b50b0125bedc1740d02c50e0547a2d2e25b2c42e) )
		ROM_LOAD( "136069.70u",   0xd0000, 0x10000, CRC(fbc3934b) SHA1(08c581359a005df4d63fa07733bb343c5ab653a9) )
		ROM_LOAD( "136069.64u",   0xe0000, 0x10000, CRC(0e07493b) SHA1(c5839ac4824b6fedb5397779cd30f6b1eff962d5) )
		ROM_LOAD( "136069.57u",   0xf0000, 0x10000, CRC(34f8f0ed) SHA1(9096aa2a188a15c2e78acf48d33def0c9f2a419f) )
	
		ROM_REGION( 0x04000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "1360691.25d",  0x00000, 0x04000, CRC(409d818e) SHA1(63dcde3ce87c1a9d5afef8089432c499cc70f8f0) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_eprom2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xa0000, REGION_CPU1, 0 )	/* 10*64k for 68000 code */
		ROM_LOAD16_BYTE( "1025.50a",   0x00000, 0x10000, CRC(b0c9a476) SHA1(6d0edeeb9458e92191f6623307eddc9b2f830d4d) )
		ROM_LOAD16_BYTE( "1024.40a",   0x00001, 0x10000, CRC(4cc2c50c) SHA1(088908cc57b07d71a5d664674e38fa02c55bb4fc) )
		ROM_LOAD16_BYTE( "1027.50b",   0x20000, 0x10000, CRC(84f533ea) SHA1(c1da671be5149bff26acd19b14cd18db0df695b7) )
		ROM_LOAD16_BYTE( "1026.40b",   0x20001, 0x10000, CRC(506396ce) SHA1(9457d346ab3aabec17f2c9ea32b9058aabdce831) )
		ROM_LOAD16_BYTE( "1029.50d",   0x40000, 0x10000, CRC(99810b9b) SHA1(f744afa559798e58b0d7ad5c7f02746e5ef94524) )
		ROM_LOAD16_BYTE( "1028.40d",   0x40001, 0x10000, CRC(08ab41f2) SHA1(1801c01efbeca64c1beecc9ca31ec12e02000a6c) )
		ROM_LOAD16_BYTE( "1033.40k",   0x60000, 0x10000, CRC(395fc203) SHA1(5f5ceb286f5e4efd88c9a9368b0486da9f318365) )
		ROM_LOAD16_BYTE( "1032.50k",   0x60001, 0x10000, CRC(a19c8acb) SHA1(77405d1e9ca82f7967ea7e54ffa81b74d81f5b56) )
		ROM_LOAD16_BYTE( "1037.50e",   0x80000, 0x10000, CRC(ad39a3dd) SHA1(00dcdcb30b7f8441df4216f9be4de15791ac5fc8) )
		ROM_LOAD16_BYTE( "1036.40e",   0x80001, 0x10000, CRC(34fc8895) SHA1(0c167c3a778e064a37517b52fd7a52f16d844f77) )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 )	/* 8*64k for 68000 code */
		ROM_LOAD16_BYTE( "1035.10s",    0x00000, 0x10000, CRC(ffeb5647) SHA1(fbd9217a96e51dd0c0cbc0ba9dfdaaa36fbc1ae9) )
		ROM_LOAD16_BYTE( "1034.10u",    0x00001, 0x10000, CRC(c68f58dd) SHA1(0ec300f32e67b710ac33efb60b8eccceb43faca6) )
		ROM_COPY( REGION_CPU1, 0x60000, 0x60000, 0x20000 )
	
		ROM_REGION( 0x14000, REGION_CPU3, 0 )	/* 64k + 16k for 6502 code */
		ROM_LOAD( "136069.7b",    0x10000, 0x4000, CRC(86e93695) SHA1(63ddab02df139dd41a8260c303798b2a550b9fe6) )
		ROM_CONTINUE(             0x04000, 0xc000 )
	
		ROM_REGION( 0x100000, REGION_GFX1, ROMREGION_DISPOSE | ROMREGION_INVERT )
		ROM_LOAD( "136069.47s",   0x00000, 0x10000, CRC(0de9d98d) SHA1(c2f963a8a4573e135a2825929cbc5535ce3b0215) )
		ROM_LOAD( "136069.43s",   0x10000, 0x10000, CRC(8eb106ad) SHA1(ece0ddba8fafe6e720f843c4d3f69ae654ae9d92) )
		ROM_LOAD( "136069.38s",   0x20000, 0x10000, CRC(bf3d0e18) SHA1(c81dacd06ce2580e37ff480d1182ab6c7e74d600) )
		ROM_LOAD( "136069.32s",   0x30000, 0x10000, CRC(48fb2e42) SHA1(480edc87f7ca547c3d8e09bf1a98e04ac464f4c6) )
		ROM_LOAD( "136069.76s",   0x40000, 0x10000, CRC(602d939d) SHA1(2ce9899f4cf0786df8c5f0e8cc63ce5206ea514f) )
		ROM_LOAD( "136069.70s",   0x50000, 0x10000, CRC(f6c973af) SHA1(048d5a9b89cb83186ca594e71521675248970735) )
		ROM_LOAD( "136069.64s",   0x60000, 0x10000, CRC(9cd52e30) SHA1(59233a87f2b50e9390f297abe7489864222f98e2) )
		ROM_LOAD( "136069.57s",   0x70000, 0x10000, CRC(4e2c2e7e) SHA1(6bf203e8c029d955634dcbaef9a6932d42035b25) )
		ROM_LOAD( "136069.47u",   0x80000, 0x10000, CRC(e7edcced) SHA1(4c19ea8b15332681bfc73a3d2b063985c1bbac1d) )
		ROM_LOAD( "136069.43u",   0x90000, 0x10000, CRC(9d3e144d) SHA1(7f4c7ee14d10a733f8b4169b41023bda1b5702c8) )
		ROM_LOAD( "136069.38u",   0xa0000, 0x10000, CRC(23f40437) SHA1(567aa09a986dd8765c54f413f906e1cb323568c6) )
		ROM_LOAD( "136069.32u",   0xb0000, 0x10000, CRC(2a47ff7b) SHA1(89935eac8fbeed87668fe1dcb4645c96a9df2c03) )
		ROM_LOAD( "136069.76u",   0xc0000, 0x10000, CRC(b0cead58) SHA1(b50b0125bedc1740d02c50e0547a2d2e25b2c42e) )
		ROM_LOAD( "136069.70u",   0xd0000, 0x10000, CRC(fbc3934b) SHA1(08c581359a005df4d63fa07733bb343c5ab653a9) )
		ROM_LOAD( "136069.64u",   0xe0000, 0x10000, CRC(0e07493b) SHA1(c5839ac4824b6fedb5397779cd30f6b1eff962d5) )
		ROM_LOAD( "136069.57u",   0xf0000, 0x10000, CRC(34f8f0ed) SHA1(9096aa2a188a15c2e78acf48d33def0c9f2a419f) )
	
		ROM_REGION( 0x04000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "1360691.25d",  0x00000, 0x04000, CRC(409d818e) SHA1(63dcde3ce87c1a9d5afef8089432c499cc70f8f0) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_klaxp1 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xa0000, REGION_CPU1, 0 )	/* 10*64k for 68000 code */
		ROM_LOAD16_BYTE( "klax_ft1.50a",   0x00000, 0x10000, CRC(87ee72d1) SHA1(39ae6f8406f0768480bcc80d395a14d9c2c65dca) )
		ROM_LOAD16_BYTE( "klax_ft1.40a",   0x00001, 0x10000, CRC(ba139fdb) SHA1(98a8ac5e0349b934f55d0d9de85abacd3fd0d77d) )
	
		ROM_REGION( 0x14000, REGION_CPU2, 0 )	/* 64k + 16k for 6502 code */
		ROM_LOAD( "klaxsnd.10c",  0x10000, 0x4000, CRC(744734cb) SHA1(3630428d69ddd2a4d5dd76bb4ee9485c943129e9) )
		ROM_CONTINUE(             0x04000, 0xc000 )
	
		ROM_REGION( 0x40000, REGION_GFX1, ROMREGION_DISPOSE | ROMREGION_INVERT )
		ROM_LOAD( "klaxprot.43s",   0x00000, 0x10000, CRC(a523c966) SHA1(8e284901cd1c68b25aa9dec1c87374b93cceeb40) )
		ROM_LOAD( "klaxprot.76s",   0x10000, 0x10000, CRC(dbc678cd) SHA1(4e6db153d29300e8d5960937d3bfebbd1ae2e78a) )
		ROM_LOAD( "klaxprot.47u",   0x20000, 0x10000, CRC(af184754) SHA1(4567337e1af1f748b1663e0b4c3e8ea746aac56c) )
		ROM_LOAD( "klaxprot.76u",   0x30000, 0x10000, CRC(7a56ffab) SHA1(96c491e51931c6641e63e55da173ecd41df7c7b3) )
	
		ROM_REGION( 0x04000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "klax125d",  0x00000, 0x04000, CRC(409d818e) SHA1(63dcde3ce87c1a9d5afef8089432c499cc70f8f0) )
	
		ROM_REGION( 0x20000, REGION_SOUND1, 0 )	/* ADPCM data */
		ROM_LOAD( "klaxadp0.1f", 0x00000, 0x10000, CRC(ba1e864f) SHA1(7c45e9040701b54c8be398c6e5cdf9201dc37c17) )
		ROM_LOAD( "klaxadp1.1e", 0x10000, 0x10000, CRC(dec9a5ac) SHA1(8039d946ac3613fa6193b557cc8775c81871831d) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_klaxp2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xa0000, REGION_CPU1, 0 )	/* 10*64k for 68000 code */
		ROM_LOAD16_BYTE( "klax_ft2.50a",   0x00000, 0x10000, CRC(7d401937) SHA1(8db0560528a86b9cb01c4598a49694bd44b00dba) )
		ROM_LOAD16_BYTE( "klax_ft2.40a",   0x00001, 0x10000, CRC(c5ca33a9) SHA1(c2e2948f987ba43f61c043baed06ffea8787be43) )
	
		ROM_REGION( 0x14000, REGION_CPU2, 0 )	/* 64k + 16k for 6502 code */
		ROM_LOAD( "klaxsnd.10c",  0x10000, 0x4000, CRC(744734cb) SHA1(3630428d69ddd2a4d5dd76bb4ee9485c943129e9) )
		ROM_CONTINUE(             0x04000, 0xc000 )
	
		ROM_REGION( 0x40000, REGION_GFX1, ROMREGION_DISPOSE | ROMREGION_INVERT )
		ROM_LOAD( "klaxprot.43s",   0x00000, 0x10000, CRC(a523c966) SHA1(8e284901cd1c68b25aa9dec1c87374b93cceeb40) )
		ROM_LOAD( "klaxprot.76s",   0x10000, 0x10000, CRC(dbc678cd) SHA1(4e6db153d29300e8d5960937d3bfebbd1ae2e78a) )
		ROM_LOAD( "klaxprot.47u",   0x20000, 0x10000, CRC(af184754) SHA1(4567337e1af1f748b1663e0b4c3e8ea746aac56c) )
		ROM_LOAD( "klaxprot.76u",   0x30000, 0x10000, CRC(7a56ffab) SHA1(96c491e51931c6641e63e55da173ecd41df7c7b3) )
	
		ROM_REGION( 0x04000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "klax125d",  0x00000, 0x04000, CRC(409d818e) SHA1(63dcde3ce87c1a9d5afef8089432c499cc70f8f0) )
	
		ROM_REGION( 0x20000, REGION_SOUND1, 0 )	/* ADPCM data */
		ROM_LOAD( "klaxadp0.1f", 0x00000, 0x10000, CRC(ba1e864f) SHA1(7c45e9040701b54c8be398c6e5cdf9201dc37c17) )
		ROM_LOAD( "klaxadp1.1e", 0x10000, 0x10000, CRC(dec9a5ac) SHA1(8039d946ac3613fa6193b557cc8775c81871831d) )
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Driver initialization
	 *
	 *************************************/
	
	public static DriverInitHandlerPtr init_eprom  = new DriverInitHandlerPtr() { public void handler()
	{
		atarigen_eeprom_default = NULL;
		atarijsa_init(2, 6, 1, 0x0002);
		atarigen_init_6502_speedup(2, 0x4158, 0x4170);
	
		/* install CPU synchronization handlers */
		sync_data = install_mem_read16_handler(0, 0x16cc00, 0x16cc01, sync_r);
		sync_data = install_mem_read16_handler(1, 0x16cc00, 0x16cc01, sync_r);
		sync_data = install_mem_write16_handler(0, 0x16cc00, 0x16cc01, sync_w);
		sync_data = install_mem_write16_handler(1, 0x16cc00, 0x16cc01, sync_w);
	} };
	
	
	public static DriverInitHandlerPtr init_klaxp  = new DriverInitHandlerPtr() { public void handler()
	{
		atarigen_eeprom_default = NULL;
		atarijsa_init(1, 2, 1, 0x0002);
		atarigen_init_6502_speedup(1, 0x4159, 0x4171);
	} };
	
	
	
	/*************************************
	 *
	 *	Game driver(s)
	 *
	 *************************************/
	
	public static GameDriver driver_eprom	   = new GameDriver("1989"	,"eprom"	,"eprom.java"	,rom_eprom,null	,machine_driver_eprom	,input_ports_eprom	,init_eprom	,ROT0	,	"Atari Games", "Escape from the Planet of the Robot Monsters (set 1)" )
	public static GameDriver driver_eprom2	   = new GameDriver("1989"	,"eprom2"	,"eprom.java"	,rom_eprom2,driver_eprom	,machine_driver_eprom	,input_ports_eprom	,init_eprom	,ROT0	,	"Atari Games", "Escape from the Planet of the Robot Monsters (set 2)" )
	public static GameDriver driver_klaxp1	   = new GameDriver("1989"	,"klaxp1"	,"eprom.java"	,rom_klaxp1,driver_klax	,machine_driver_klaxp	,input_ports_klaxp	,init_klaxp	,ROT0	,	"Atari Games", "Klax (prototype set 1)" )
	public static GameDriver driver_klaxp2	   = new GameDriver("1989"	,"klaxp2"	,"eprom.java"	,rom_klaxp2,driver_klax	,machine_driver_klaxp	,input_ports_klaxp	,init_klaxp	,ROT0	,	"Atari Games", "Klax (prototype set 2)" )
}
