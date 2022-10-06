/****************************************************************************

Formation Z / Aeroboto

Driver by Carlos A. Lozano


TODO:
- star field
  Uki's report:
  - The color of stars:
    at 1st title screen = neutral tints of blue and aqua (1 color only)
    at 2nd title screen and attract mode (purple surface) = light & dark aqua
    This color will not be affected by scroll. Leftmost 8pixels are light, next
    16 pixels are dark, the next 16 pixels are light, and so on.

Revisions:
- Updated starfield according to Uki's report. (AT)

*note: Holding any key at boot puts the game in MCU test. Press F3 to quit.

****************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class aeroboto
{
	
	
	extern data8_t *aeroboto_videoram;
	extern data8_t *aeroboto_hscroll, *aeroboto_vscroll, *aeroboto_tilecolor;
	extern data8_t *aeroboto_starx, *aeroboto_stary, *aeroboto_bgcolor;
	
	VIDEO_START( aeroboto );
	VIDEO_UPDATE( aeroboto );
	
	READ_HANDLER( aeroboto_in0_r );
	WRITE_HANDLER( aeroboto_3000_w );
	WRITE_HANDLER( aeroboto_videoram_w );
	WRITE_HANDLER( aeroboto_tilecolor_w );
	
	static data8_t *aeroboto_mainram;
	static int disable_irq = 0;
	
	
	static READ_HANDLER( aeroboto_201_r )
	{
		/* if you keep a button pressed during boot, the game will expect this */
		/* serie of values to be returned from 3004, and display "PASS 201" if it is */
		int res[4] = { 0xff,0x9f,0x1b,0x03};
		static int count;
		logerror("PC %04x: read 3004\n",activecpu_get_pc());
		return res[(count++)&3];
	}
	
	
	static INTERRUPT_GEN( aeroboto_interrupt )
	{
		if (!disable_irq)
			cpu_set_irq_line(0, 0, HOLD_LINE);
		else
			disable_irq--;
	}
	
	static READ_HANDLER( aeroboto_2973_r )
	{
		aeroboto_mainram[0x02be] = 0;
		return(0xff);
	}
	
	static WRITE_HANDLER ( aeroboto_1a2_w )
	{
		aeroboto_mainram[0x01a2] = data;
		if (data) disable_irq = 1;
	}
	
	static MEMORY_READ_START( readmem )
		{ 0x0000, 0x07ff, MRA_RAM }, // main RAM
		{ 0x0800, 0x08ff, MRA_RAM }, // tile color buffer; copied to 0x2000
		{ 0x1000, 0x17ff, MRA_RAM }, // tile RAM
		{ 0x1800, 0x183f, MRA_RAM }, // horizontal scroll regs
		{ 0x2000, 0x20ff, MRA_RAM }, // tile color RAM
		{ 0x2800, 0x28ff, MRA_RAM }, // sprite RAM
		{ 0x2973, 0x2973, aeroboto_2973_r }, // protection read
		{ 0x3000, 0x3000, aeroboto_in0_r },
		{ 0x3001, 0x3001, input_port_2_r },
		{ 0x3002, 0x3002, input_port_3_r },
		{ 0x3004, 0x3004, aeroboto_201_r }, // protection read
		{ 0x3800, 0x3800, MRA_NOP }, // watchdog or IRQ ack
		{ 0x4000, 0xffff, MRA_ROM }, // main ROM
	MEMORY_END
	
	static MEMORY_WRITE_START( writemem )
		{ 0x01a2, 0x01a2, aeroboto_1a2_w }, // affects IRQ line (more protection?)
		{ 0x0000, 0x07ff, MWA_RAM, &aeroboto_mainram },
		{ 0x0800, 0x08ff, MWA_RAM },
		{ 0x0900, 0x09ff, MWA_RAM }, // a backup of default tile colors
		{ 0x1000, 0x17ff, aeroboto_videoram_w, &aeroboto_videoram },
		{ 0x1800, 0x183f, MWA_RAM, &aeroboto_hscroll },
		{ 0x2000, 0x20ff, aeroboto_tilecolor_w, &aeroboto_tilecolor },
		{ 0x1840, 0x27ff, MWA_NOP }, // cleared during custom LSI test
		{ 0x2800, 0x28ff, MWA_RAM, &spriteram, &spriteram_size },
		{ 0x2900, 0x2fff, MWA_NOP }, // cleared along with sprite RAM
		{ 0x3000, 0x3000, aeroboto_3000_w },
		{ 0x3001, 0x3001, soundlatch_w },
		{ 0x3002, 0x3002, soundlatch2_w },
		{ 0x3003, 0x3003, MWA_RAM, &aeroboto_vscroll },
		{ 0x3004, 0x3004, MWA_RAM, &aeroboto_starx },
		{ 0x3005, 0x3005, MWA_RAM, &aeroboto_stary }, // usable but probably wrong
		{ 0x3006, 0x3006, MWA_RAM, &aeroboto_bgcolor },
		{ 0x4000, 0xffff, MWA_ROM },
	MEMORY_END
	
	static MEMORY_READ_START( readmem_sound )
		{ 0x0000, 0x0fff, MRA_RAM },
		{ 0x9002, 0x9002, AY8910_read_port_0_r },
		{ 0xa002, 0xa002, AY8910_read_port_1_r },
		{ 0xf000, 0xffff, MRA_ROM },
	MEMORY_END
	
	static MEMORY_WRITE_START( writemem_sound )
		{ 0x0000, 0x0fff, MWA_RAM },
		{ 0x9000, 0x9000, AY8910_control_port_0_w },
		{ 0x9001, 0x9001, AY8910_write_port_0_w },
		{ 0xa000, 0xa000, AY8910_control_port_1_w },
		{ 0xa001, 0xa001, AY8910_write_port_1_w },
		{ 0xf000, 0xffff, MWA_ROM },
	MEMORY_END
	
	
	
	INPUT_PORTS_START( formatz )
		PORT_START      /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY )
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY )
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY )
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 )
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 )
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 )
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 )
	
		PORT_START      /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL )
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL )
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL )
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL )
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL )
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )
	
		PORT_START
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
		PORT_DIPSETTING(    0x00, "3" )
		PORT_DIPSETTING(    0x01, "4" )
		PORT_DIPSETTING(    0x02, "5" )
		PORT_BITX( 0,       0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE )
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( Bonus_Life ) )
		PORT_DIPSETTING(    0x0c, "30000" )
		PORT_DIPSETTING(    0x08, "40000" )
		PORT_DIPSETTING(    0x04, "70000" )
		PORT_DIPSETTING(    0x00, "100000" )
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( Unknown ) )
		PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
		PORT_DIPSETTING(    0x10, DEF_STR( On ) )
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( Demo_Sounds ) )
		PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
		PORT_DIPSETTING(    0x20, DEF_STR( On ) )
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( Cabinet ) )
		PORT_DIPSETTING(    0x40, DEF_STR( Upright ) )
		PORT_DIPSETTING(    0x00, DEF_STR( Cocktail ) )
		/* The last dip switch is directly connected to the video hardware and
		   flips the screen. The program instead sees the coin input, which must
		   stay low for exactly 2 frames to be consistently recognized. */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 2 )
	
		PORT_START
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( Coinage ) )
		PORT_DIPSETTING(    0x07, DEF_STR( 5C_1C ) )
		PORT_DIPSETTING(    0x05, DEF_STR( 4C_1C ) )
		PORT_DIPSETTING(    0x03, DEF_STR( 3C_1C ) )
		PORT_DIPSETTING(    0x01, DEF_STR( 2C_1C ) )
		PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
		PORT_DIPSETTING(    0x02, DEF_STR( 1C_2C ) )
		PORT_DIPSETTING(    0x04, DEF_STR( 1C_3C ) )
		PORT_DIPSETTING(    0x06, DEF_STR( 1C_4C ) )
		PORT_DIPNAME( 0x18, 0x08, DEF_STR( Difficulty ) )
		PORT_DIPSETTING(    0x00, "0(Easy)" )
		PORT_DIPSETTING(    0x08, "1(Medium)" )
		PORT_DIPSETTING(    0x10, "2(Hard)" )
		PORT_DIPSETTING(    0x18, "3(Hardest)" )
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( Unknown ) )
		PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
		PORT_DIPSETTING(    0x20, DEF_STR( On ) )
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( Unknown ) )
		PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
		PORT_DIPSETTING(    0x40, DEF_STR( On ) )
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( Unknown ) )
		PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
		PORT_DIPSETTING(    0x80, DEF_STR( On ) )
	INPUT_PORTS_END
	
	
	
	static struct GfxLayout charlayout =
	{
		8,8,
		RGN_FRAC(1,2),
		2,
		{ 4, 0 },
		{ 0, 1, 2, 3, RGN_FRAC(1,2)+0, RGN_FRAC(1,2)+1, RGN_FRAC(1,2)+2, RGN_FRAC(1,2)+3 },
		{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	};
	/*
	// exact star layout unknown... could be anything
	static struct GfxLayout starlayout =
	{
		8,8,
		RGN_FRAC(1,1),
		1,
		{ 0 },
		{ 0, 1, 2, 3, 4, 5, 6, 7 },
		{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	};
	*/
	static struct GfxLayout spritelayout =
	{
		8,16,
		RGN_FRAC(1,3),
		3,
		{ RGN_FRAC(2,3), RGN_FRAC(1,3), RGN_FRAC(0,3) },
		{ 0, 1, 2, 3, 4, 5, 6, 7 },
		{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
		  8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*8
	};
	
	static struct GfxDecodeInfo gfxdecodeinfo[] =
	{
		{ REGION_GFX1, 0, &charlayout,     0,  64 },     /* chars */
	//	{ REGION_GFX2, 0, &starlayout,     0, 128 },     /* sky */
		{ REGION_GFX3, 0, &spritelayout,   0,   8 },
		{ -1 } /* end of array */
	};
	
	
	
	static struct AY8910interface ay8910_interface =
	{
		2,      /* 2 chips */
		1500000,	/* 1.5 MHz ? (hand tuned) */
		{ 25, 25 },
		{ soundlatch_r, 0 },    /* ? */
		{ soundlatch2_r, 0 },   /* ? */
		{ 0, 0 },
		{ 0, 0 }
	};
	
	static MACHINE_DRIVER_START( formatz )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M6809, 1250000) // 1.25MHz
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_VBLANK_INT(aeroboto_interrupt,1)
	
		MDRV_CPU_ADD(M6809, 640000)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)
		MDRV_CPU_MEMORY(readmem_sound,writemem_sound)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_GFXDECODE(gfxdecodeinfo)
	
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 31*8-1, 2*8, 30*8-1)
		MDRV_PALETTE_LENGTH(256)
	
		MDRV_PALETTE_INIT(RRRR_GGGG_BBBB)
		MDRV_VIDEO_START(aeroboto)
		MDRV_VIDEO_UPDATE(aeroboto)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
	MACHINE_DRIVER_END
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	ROM_START( formatz )
		ROM_REGION( 0x10000, REGION_CPU1, 0 )     /* 64k for main CPU */
		ROM_LOAD( "format_z.8",   0x4000, 0x4000, CRC(81a2416c) SHA1(d43c6bcc079847cb4c8e77fdc4d9d5bb9c2cc41a) )
		ROM_LOAD( "format_z.7",   0x8000, 0x4000, CRC(986e6052) SHA1(4d39eda38fa17695f8217b0032a750cbe71c5674) )
		ROM_LOAD( "format_z.6",   0xc000, 0x4000, CRC(baa0d745) SHA1(72b6cf31c9bbf9b5c55ef3f4ca5877ce576beda9) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )     /* 64k for sound CPU */
		ROM_LOAD( "format_z.9",   0xf000, 0x1000, CRC(6b9215ad) SHA1(3ab416d070bf6b9a8be3e19d4dbc3a399d9ab5cb) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "format_z.5",   0x0000, 0x2000, CRC(ba50be57) SHA1(aa37b644e8c1944b4c0ba81164d5a52be8ab491f) )  /* characters */
	
		ROM_REGION( 0x2000, REGION_GFX2, 0 ) // starfield data
		ROM_LOAD( "format_z.4",   0x0000, 0x2000, CRC(910375a0) SHA1(1044e0f45ce34c15986d9ab520c0e7d08fd46dde) )  /* characters */
	
		ROM_REGION( 0x3000, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "format_z.1",   0x0000, 0x1000, CRC(5739afd2) SHA1(3a645bc8a5ac69f1dc878a589c580f2bf033d3cb) )  /* sprites */
		ROM_LOAD( "format_z.2",   0x1000, 0x1000, CRC(3a821391) SHA1(476507ba5e5d64ca3729244590beadb9b3a6a018) )  /* sprites */
		ROM_LOAD( "format_z.3",   0x2000, 0x1000, CRC(7d1aec79) SHA1(bb19d6c91a14df26706226cfe22853bb8383c63d) )  /* sprites */
	
		ROM_REGION( 0x0300, REGION_PROMS, 0 )
		ROM_LOAD( "10c",          0x0000, 0x0100, CRC(b756dd6d) SHA1(ea79f87f84ded2f0a66458af24cbc792e5ff77e3) )
		ROM_LOAD( "10b",          0x0100, 0x0100, CRC(00df8809) SHA1(f4539c052a5ce8a63662db070c3f52139afef23d) )
		ROM_LOAD( "10a",          0x0200, 0x0100, CRC(e8733c8f) SHA1(105b44c9108ee173a417f8c79ec8381f824dd675) )
	ROM_END
	
	ROM_START( aeroboto )
		ROM_REGION( 0x10000, REGION_CPU1, 0 )     /* 64k for main CPU */
		ROM_LOAD( "aeroboto.8",   0x4000, 0x4000, CRC(4d3fc049) SHA1(6efb8c58c025a69ac2dce99049128861f7ede690) )
		ROM_LOAD( "aeroboto.7",   0x8000, 0x4000, CRC(522f51c1) SHA1(4ea47d0b8b65e711c99701c055dbaf70a003d441) )
		ROM_LOAD( "aeroboto.6",   0xc000, 0x4000, CRC(1a295ffb) SHA1(990b3f2f883717c180089b6ba5ae381ed9272341) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )     /* 64k for sound CPU */
		ROM_LOAD( "format_z.9",   0xf000, 0x1000, CRC(6b9215ad) SHA1(3ab416d070bf6b9a8be3e19d4dbc3a399d9ab5cb) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "aeroboto.5",   0x0000, 0x2000, CRC(32fc00f9) SHA1(fd912fe2ab0101057c15c846f0cc4259cd94b035) )  /* characters */
	
		ROM_REGION( 0x2000, REGION_GFX2, 0 ) // starfield data
		ROM_LOAD( "format_z.4",   0x0000, 0x2000, CRC(910375a0) SHA1(1044e0f45ce34c15986d9ab520c0e7d08fd46dde) )  /* characters */
	
		ROM_REGION( 0x3000, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "aeroboto.1",   0x0000, 0x1000, CRC(7820eeaf) SHA1(dedd15295bb02f417d0f51a29df686b66b94dee1) )  /* sprites */
		ROM_LOAD( "aeroboto.2",   0x1000, 0x1000, CRC(c7f81a3c) SHA1(21476a4146d5c57e2b15125c304fc61d82edf4af) )  /* sprites */
		ROM_LOAD( "aeroboto.3",   0x2000, 0x1000, CRC(5203ad04) SHA1(d16eb370de9033793a502e23c82a3119cd633aa9) )  /* sprites */
	
		ROM_REGION( 0x0300, REGION_PROMS, 0 )
		ROM_LOAD( "10c",          0x0000, 0x0100, CRC(b756dd6d) SHA1(ea79f87f84ded2f0a66458af24cbc792e5ff77e3) )
		ROM_LOAD( "10b",          0x0100, 0x0100, CRC(00df8809) SHA1(f4539c052a5ce8a63662db070c3f52139afef23d) )
		ROM_LOAD( "10a",          0x0200, 0x0100, CRC(e8733c8f) SHA1(105b44c9108ee173a417f8c79ec8381f824dd675) )
	ROM_END
	
	
	
	GAMEX( 1984, formatz,  0,       formatz, formatz, 0, ROT0, "Jaleco", "Formation Z", GAME_IMPERFECT_GRAPHICS )
	GAMEX( 1984, aeroboto, formatz, formatz, formatz, 0, ROT0, "[Jaleco] (Williams license)", "Aeroboto", GAME_IMPERFECT_GRAPHICS )
}
