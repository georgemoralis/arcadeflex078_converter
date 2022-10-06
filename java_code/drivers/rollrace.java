/***************************************************************************

Issues:
-sound effects missing
-cpu speeds are guessed
-colours might be wrong in the night stage
***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class rollrace
{
	
	
	VIDEO_UPDATE( rollrace );
	PALETTE_INIT( wiz );
	VIDEO_START( rollrace );
	
	WRITE_HANDLER( rollrace_charbank_w );
	WRITE_HANDLER( rollrace_backgroundpage_w);
	WRITE_HANDLER( rollrace_backgroundcolor_w);
	WRITE_HANDLER( rollrace_bkgpen_w );
	WRITE_HANDLER( rollrace_flipy_w );
	WRITE_HANDLER( rollrace_spritebank_w );
	WRITE_HANDLER( rollrace_flipx_w );
	
	READ_HANDLER( rollrace_bkgpen_r );
	
	READ_HANDLER( ra_fake_d800_r )
	{
		return 0x51;
	}
	
	WRITE_HANDLER( ra_fake_d800_w )
	{
	/*	logerror("d900: %02X\n",data);*/
	}
	
	static MEMORY_READ_START (readmem)
		{ 0x0000, 0x7fff, MRA_ROM },
		{ 0x8000, 0x9fff, MRA_ROM }, /* only rollace2 */
		{ 0xc000, 0xcfff, MRA_RAM },
		{ 0xd900, 0xd900, ra_fake_d800_r }, /* protection ??*/
		{ 0xe000, 0xe3ff, MRA_RAM },
		{ 0xe400, 0xe47f, MRA_RAM },
		{ 0xec00, 0xec0f, MRA_NOP }, /* Analog sound effects ??*/
		{ 0xf000, 0xf0ff, MRA_RAM },
		{ 0xf800, 0xf800, input_port_0_r },		/* player1*/
		{ 0xf801, 0xf801, input_port_1_r },		/* player2*/
		{ 0xf804, 0xf804, input_port_3_r },
		{ 0xf802, 0xf802, input_port_2_r },
		{ 0xf805, 0xf805, input_port_4_r },
		{ 0xd806, 0xd806, MRA_NOP }, /* looks like a watchdog, bit4 checked*/
	MEMORY_END
	
	static MEMORY_WRITE_START (writemem)
		{ 0x0000, 0x7fff, MWA_ROM },
		{ 0x8000, 0x9fff, MWA_ROM },
		{ 0xc000, 0xcfff, MWA_RAM },
		{ 0xd900, 0xd900, ra_fake_d800_w }, /* protection ?? */
		{ 0xe000, 0xe3ff, videoram_w, &videoram, &videoram_size },
		{ 0xe400, 0xe47f, colorram_w, &colorram },
		{ 0xe800, 0xe800, soundlatch_w },
		{ 0xec00, 0xec0f, MWA_NOP }, /* Analog sound effects ?? ec00 sound enable ?*/
		{ 0xf000, 0xf0ff, MWA_RAM , &spriteram, &spriteram_size },
		{ 0xfc00, 0xfc00, rollrace_flipx_w },
		{ 0xfc01, 0xfc01, interrupt_enable_w },
		{ 0xfc02, 0xfc03, MWA_NOP }, /* coin counters */
		{ 0xfc04, 0xfc05, rollrace_charbank_w },
		{ 0xfc06, 0xfc06, rollrace_spritebank_w },
		{ 0xf400, 0xf400, rollrace_backgroundcolor_w },
		{ 0xf801, 0xf801, rollrace_bkgpen_w },
		{ 0xf802, 0xf802, rollrace_backgroundpage_w },
		{ 0xf803, 0xf803, rollrace_flipy_w },
	MEMORY_END
	
	static MEMORY_READ_START (readmem_snd)
			{ 0x0000, 0x0fff, MRA_ROM },
			{ 0x2000, 0x2fff, MRA_RAM },
			{ 0x3000, 0x3000, soundlatch_r },
	MEMORY_END
	
	static MEMORY_WRITE_START (writemem_snd)
			{ 0x0000, 0x0fff, MWA_ROM },
			{ 0x2000, 0x2fff, MWA_RAM },
			{ 0x3000, 0x3000, interrupt_enable_w },
			{ 0x4000, 0x4000, AY8910_control_port_0_w },
			{ 0x4001, 0x4001, AY8910_write_port_0_w },
			{ 0x5000, 0x5000, AY8910_control_port_1_w },
			{ 0x5001, 0x5001, AY8910_write_port_1_w },
			{ 0x6000, 0x6000, AY8910_control_port_2_w },
			{ 0x6001, 0x6001, AY8910_write_port_2_w },
	MEMORY_END
	
	
	INPUT_PORTS_START( rollrace )
		PORT_START
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY )
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT| IPF_8WAY )
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP   | IPF_8WAY )
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY )
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 ) // Jump
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 ) // Punch
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )
		PORT_START
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_COCKTAIL |IPF_8WAY )
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL |IPF_8WAY )
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_COCKTAIL |IPF_8WAY )
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_COCKTAIL |IPF_8WAY )
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL ) // Jump
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL ) // Punch
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	
		PORT_START
		PORT_BIT( 0x01, IP_ACTIVE_HIGH,IPT_COIN1 )
		PORT_BIT( 0x02, IP_ACTIVE_HIGH,IPT_COIN2 )
		PORT_BIT( 0x04, IP_ACTIVE_HIGH,IPT_SERVICE1 )
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( Unknown ) )
		PORT_DIPSETTING( 0x00, DEF_STR( Off ) )
		PORT_DIPSETTING( 0x08, DEF_STR( On ) )
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_START1 )
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_START2 )
		PORT_SERVICE( 0x40, IP_ACTIVE_HIGH )
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( Unknown ) )
		PORT_DIPSETTING( 0x00, DEF_STR( Off ) )
		PORT_DIPSETTING( 0x80, DEF_STR( On ) )
	
		PORT_START
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( Coin_A ) )
		PORT_DIPSETTING( 	0x07, DEF_STR( 6C_1C ) )
		PORT_DIPSETTING( 	0x06, DEF_STR( 3C_1C ) )
		PORT_DIPSETTING( 	0x04, DEF_STR( 2C_1C ) )
		PORT_DIPSETTING( 	0x00, DEF_STR( 1C_1C ) )
		PORT_DIPSETTING( 	0x05, DEF_STR( 2C_3C ) )
		PORT_DIPSETTING( 	0x01, DEF_STR( 1C_2C ) )
		PORT_DIPSETTING( 	0x02, DEF_STR( 1C_3C ) )
		PORT_DIPSETTING( 	0x03, DEF_STR( 1C_6C ) )
	
		PORT_DIPNAME( 0x38, 0x00, DEF_STR( Coin_B ) )
		PORT_DIPSETTING( 	0x38, DEF_STR( 6C_1C ) )
		PORT_DIPSETTING( 	0x30, DEF_STR( 3C_1C ) )
		PORT_DIPSETTING( 	0x20, DEF_STR( 2C_1C ) )
		PORT_DIPSETTING( 	0x00, DEF_STR( 1C_1C ) )
		PORT_DIPSETTING( 	0x28, DEF_STR( 2C_3C ) )
		PORT_DIPSETTING( 	0x08, DEF_STR( 1C_2C ) )
		PORT_DIPSETTING( 	0x10, DEF_STR( 1C_3C ) )
		PORT_DIPSETTING( 	0x18, DEF_STR( 1C_6C ) )
	
	/*	PORT_BIT( 0x40, IP_ACTIVE_HIGH , IPT_VBLANK )  freezes frame, could be vblank ?*/
		PORT_DIPNAME( 0x40, 0x00, "Freeze" )
		PORT_DIPSETTING( 	0x00, DEF_STR( Off ) )
		PORT_DIPSETTING( 	0x40, DEF_STR( On ) )
	/*	PORT_DIPNAME( 0x80, 0x00, "Free Run" ) */
		PORT_BITX(    0x80, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", 0, 0 )
		PORT_DIPSETTING( 	0x00, DEF_STR( Off ) ) /* test mode, you are invulnerable */
		PORT_DIPSETTING( 	0x80, DEF_STR( On ) )	/* to 'static' objects */
	
		PORT_START
		PORT_DIPNAME( 0x03, 0x01, DEF_STR( Lives ) )
		PORT_DIPSETTING( 	0x00, "2" )
		PORT_DIPSETTING( 	0x01, "3" )
		PORT_DIPSETTING( 	0x02, "5" )
		PORT_DIPSETTING( 	0x03, "7" )
		PORT_DIPNAME( 0x0c, 0x00, DEF_STR( Bonus_Life ) )
		PORT_DIPSETTING( 	0x04, "20000" )
		PORT_DIPSETTING( 	0x08, "50000" )
		PORT_DIPSETTING( 	0x0c, "100000" )
		PORT_DIPSETTING( 	0x00, "None" )
		PORT_DIPNAME( 0x30, 0x00, DEF_STR( Difficulty ) )
		PORT_DIPSETTING( 	0x00, "A" )
		PORT_DIPSETTING( 	0x10, "B" )
		PORT_DIPSETTING( 	0x20, "C" )
		PORT_DIPSETTING( 	0x30, "D" )
		PORT_DIPNAME( 0x40, 0x00, DEF_STR(Cabinet) )
		PORT_DIPSETTING( 	0x00, DEF_STR( Upright ) )
		PORT_DIPSETTING( 	0x40, DEF_STR( Cocktail ) )
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( Unknown ) )
		PORT_DIPSETTING( 0x00, DEF_STR( Off ) )
		PORT_DIPSETTING( 0x80, DEF_STR( On ) )
	INPUT_PORTS_END
	
	static struct GfxLayout charlayout =
	{
		8,8,	/* 8*8 characters */
			256,	/* 256 characters */
			3,	  /* 3 bits per pixel */
			{ 0,1024*8*8, 2*1024*8*8 }, /* the two bitplanes are separated */
		{ 0,1,2,3,4,5,6,7 },
		{ 7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8 },
	
	
		8*8	/* every char takes 8 consecutive bytes */
	};
	static struct GfxLayout charlayout2 =
	{
		8,8,	/* 8*8 characters */
			1024,	/* 1024 characters */
			3,	  /* 3 bits per pixel */
			{ 0,1024*8*8, 2*1024*8*8 }, /* the two bitplanes are separated */
		{ 0, 1, 2, 3, 4, 5, 6, 7 },
		{ 7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8 },
	
	//	{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
	
		8*8	/* every char takes 8 consecutive bytes */
	};
	
	static struct GfxLayout spritelayout =
	{
			32,32,  /* 32*32 sprites */
			64,	/* 64 sprites */
		3,	  /* 3 bits per pixel */
			{ 0x4000*8, 0x2000*8, 0 }, /* the three bitplanes are separated */
		{ 0, 1, 2, 3, 4, 5, 6, 7,
			 8*8+0, 8*8+1, 8*8+2, 8*8+3, 8*8+4, 8*8+5, 8*8+6, 8*8+7,16*8,16*8+1,16*8+2,16*8+3,16*8+4,16*8+5,16*8+6,16*8+7,24*8,24*8+1,24*8+2,24*8+3,24*8+4,24*8+5,24*8+6,24*8+7},
	
		{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
			  32*8, 33*8, 34*8, 35*8, 36*8, 37*8, 38*8, 39*8,
			  64*8,65*8,66*8,67*8,68*8,69*8,70*8,71*8, 96*8,97*8,98*8,99*8,100*8,101*8,102*8,103*8 },
	
			32*32	 /* every sprite takes 128 consecutive bytes */
	};
	
	static struct GfxDecodeInfo gfxdecodeinfo[] =
	{
			{ REGION_GFX1, 0x0000, &charlayout,	0,	32 }, /* foreground */
			{ REGION_GFX1, 0x0800, &charlayout,	0,	32 },
			{ REGION_GFX1, 0x1000, &charlayout,	0,	32 },
			{ REGION_GFX1, 0x1800, &charlayout,	0,	32 },
			{ REGION_GFX2, 0x0000, &charlayout2,	0, 	32 }, /* for the road */
			{ REGION_GFX3, 0x0000, &spritelayout,	0, 	32 }, /* sprites */
			{ REGION_GFX4, 0x0000, &spritelayout,	0,	32 },
			{ REGION_GFX5, 0x0000, &spritelayout,	0,	32 },
	
		{ -1 } /* end of array */
	};
	
	static struct AY8910interface ra_ay8910_interface =
	{
		3,	  	/* 3 chips */
		14318000/8,	/* 1.78975 MHz */
		{ 10,10,10 },
		{ 0 },
		{ 0 },
		{ 0 },
		{ 0 }
	};
	
	static MACHINE_DRIVER_START( rollrace )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80,18432000/6)			/* ?? */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_VBLANK_INT(nmi_line_pulse,1)
	
		MDRV_CPU_ADD(Z80,14318000/6)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)			/* ?? */
		MDRV_CPU_MEMORY(readmem_snd,writemem_snd)
		MDRV_CPU_VBLANK_INT(nmi_line_pulse,3)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(16,255,16, 255-16)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(256)
		MDRV_COLORTABLE_LENGTH(32*8)
	
		MDRV_PALETTE_INIT(wiz)
		MDRV_VIDEO_START(generic)
		MDRV_VIDEO_UPDATE(rollrace)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ra_ay8910_interface)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( rollace2 )
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(rollrace)
	
		MDRV_VISIBLE_AREA(0,255-24,16, 255-16)
	MACHINE_DRIVER_END
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	ROM_START( fightrol )
		ROM_REGION( 0x10000, REGION_CPU1,0 )	/* 64k for code */
		ROM_LOAD( "4.8k", 0x0000, 0x2000, CRC(efa2f430) SHA1(6aeb2a41e4fba97a0ac1b24fe5437e25b6c6b6c5) )
		ROM_LOAD( "5.8h", 0x2000, 0x2000, CRC(2497d9f6) SHA1(4f4cfed47efc603bf057dd24b761beecf5b929f4) )
		ROM_LOAD( "6.8f", 0x4000, 0x2000, CRC(f39727b9) SHA1(08a1300172b4100cb80c9a5d8942408255d8e330) )
		ROM_LOAD( "7.8d", 0x6000, 0x2000, CRC(ee65b728) SHA1(871918d505ad8bab60c55bbb95fe37556a204dc9) )
	
		ROM_REGION( 0x6000,REGION_GFX1,ROMREGION_DISPOSE )	/* characters */
		ROM_LOAD( "3.7m", 0x0000, 0x2000, CRC(ca4f353c) SHA1(754838c6ad6886052a018966d55f40a7ed4b684d) )
		ROM_LOAD( "2.8m", 0x2000, 0x2000, CRC(93786171) SHA1(3928aad8bc43adeaad5e53c1d4e9df64f1d23704) )
		ROM_LOAD( "1.9m", 0x4000, 0x2000, CRC(dc072be1) SHA1(94d379a4c5a53050a18cd572cc82edb337182f3b) )
	
		ROM_REGION( 0x6000, REGION_GFX2,ROMREGION_DISPOSE )	/* road graphics */
		ROM_LOAD ( "6.20k", 0x0000, 0x2000, CRC(003d7515) SHA1(d8d84d690478cad16101f2ef9a1ae1ae74d01c88) )
		ROM_LOAD ( "7.18k", 0x2000, 0x2000, CRC(27843afa) SHA1(81d3031a2c06086461110696a0ee11d32992ecac) )
		ROM_LOAD ( "5.20f", 0x4000, 0x2000, CRC(51dd0108) SHA1(138c0aba6c952204e794216193def17b390c4ba2) )
	
		ROM_REGION( 0x6000, REGION_GFX3,ROMREGION_DISPOSE ) 	/* sprite bank 0*/
		ROM_LOAD ( "8.17m",  0x0000, 0x2000, CRC(08ad783e) SHA1(fea91e41916cfc7b29c5f9a578e2c82a54f66829) )
		ROM_LOAD ( "9.17r",  0x2000, 0x2000, CRC(69b23461) SHA1(73eca5e721425f37df311454bd5b4e632b096eba) )
		ROM_LOAD ( "10.17t", 0x4000, 0x2000, CRC(ba6ccd8c) SHA1(29a13e3161aba4db080434685869f8b79ad7997c) )
	
		ROM_REGION( 0x6000, REGION_GFX4,ROMREGION_DISPOSE ) 	/* sprite bank 1*/
		ROM_LOAD ( "11.18m", 0x0000, 0x2000, CRC(06a5d849) SHA1(b9f604edf4fdc053b738041493aef91dd730fe6b) )
		ROM_LOAD ( "12.18r", 0x2000, 0x2000, CRC(569815ef) SHA1(db261799892f60b2274b73fb25cde58219bb44db) )
		ROM_LOAD ( "13.18t", 0x4000, 0x2000, CRC(4f8af872) SHA1(6c07ff0733b8d8440309c9ae0db0876587b740a6) )
	
		ROM_REGION( 0x6000, REGION_GFX5,ROMREGION_DISPOSE )	/* sprite bank 2*/
		ROM_LOAD ( "14.19m", 0x0000, 0x2000, CRC(93f3c649) SHA1(38d6bb4b6108a67b135ae1a145532f4a0c2568b8) )
		ROM_LOAD ( "15.19r", 0x2000, 0x2000, CRC(5b3d87e4) SHA1(e47f7b62bf7101afba8d5e181f4bd8f8eb6eeb08) )
		ROM_LOAD ( "16.19u", 0x4000, 0x2000, CRC(a2c24b64) SHA1(e76558785ea337ab902fb6f94dc1a4bdfcd6335e) )
	
		ROM_REGION( 0x8000, REGION_USER1,ROMREGION_NODISPOSE )	/* road layout */
		ROM_LOAD ( "1.17a", 0x0000, 0x2000, CRC(f0fa72fc) SHA1(b73e794df635630f29a79adfe2951dc8f1d17e20) )
		ROM_LOAD ( "3.18b", 0x2000, 0x2000, CRC(954268f7) SHA1(07057296e0281f90b18dfe4223aad18bff7cfa6e) )
		ROM_LOAD ( "2.17d", 0x4000, 0x2000, CRC(2e38bb0e) SHA1(684f14a06ff957e40780be21c0ad5f10088a55ed) )
		ROM_LOAD ( "4.18d", 0x6000, 0x2000, CRC(3d9e16ab) SHA1(e99628ffc54e3ff4818313a287ca111617120910) )
	
		ROM_REGION( 0x300, REGION_PROMS,ROMREGION_NODISPOSE )	/* colour */
		ROM_LOAD("tbp24s10.7u", 0x0000, 0x0100, CRC(9d199d33) SHA1(b8982f7da2b85f10d117177e4e73cbb486931cf5) )
		ROM_LOAD("tbp24s10.7t", 0x0100, 0x0100, CRC(c0426582) SHA1(8e3e4d1e76243cce272aa099d2d6ad4fa6c99f7c) )
		ROM_LOAD("tbp24s10.6t", 0x0200, 0x0100, CRC(c096e05c) SHA1(cb5b509e6124453f381a683ba446f8f4493d4610) )
	
		ROM_REGION( 0x10000,REGION_CPU2,0 )
		ROM_LOAD( "8.6f", 0x0000, 0x1000, CRC(6ec3c545) SHA1(1a2477b9e1563734195b0743f5dbbb005e06022e) )
	ROM_END
	
	ROM_START( rollace )
		ROM_REGION( 0x10000, REGION_CPU1,0 )	/* 64k for code */
		ROM_LOAD( "w1.8k", 0x0000, 0x2000, CRC(c0bd3cf3) SHA1(a44d69b8c3249b5093261a32d0e0404992fa7f7a) )
		ROM_LOAD( "w2.8h", 0x2000, 0x2000, CRC(c1900a75) SHA1(f7ec968b6bcb6ee6db98628cdf566ae0a501edba) )
		ROM_LOAD( "w3.8f", 0x4000, 0x2000, CRC(16ceced6) SHA1(241119959ffdf26780258bcc5651eca0c6a6128f) )
		ROM_LOAD( "w4.8d", 0x6000, 0x2000, CRC(ae826a96) SHA1(47979343c9fa7629ba6d62630c7c3fdfa2c8c28a) )
	
		ROM_REGION( 0x6000,REGION_GFX1,ROMREGION_DISPOSE )	/* characters */
		ROM_LOAD( "w3.7m", 0x0000, 0x2000, CRC(f9970aae) SHA1(ccb806cab3d3817c779e048f995d1f6fbe163679) )
		ROM_LOAD( "w2.8m", 0x2000, 0x2000, CRC(80573091) SHA1(ea352abebc428db9e89eda5f369a3b1086aa8970) )
		ROM_LOAD( "w1.9m", 0x4000, 0x2000, CRC(b37effd8) SHA1(d77d56d734834812b8d9b3c156577dbbcb2deac8) )
	
		ROM_REGION( 0x6000, REGION_GFX2,ROMREGION_DISPOSE )	/* road graphics */
		ROM_LOAD ( "6.20k", 0x0000, 0x2000, CRC(003d7515) SHA1(d8d84d690478cad16101f2ef9a1ae1ae74d01c88) )
		ROM_LOAD ( "7.18k", 0x2000, 0x2000, CRC(27843afa) SHA1(81d3031a2c06086461110696a0ee11d32992ecac) )
		ROM_LOAD ( "5.20f", 0x4000, 0x2000, CRC(51dd0108) SHA1(138c0aba6c952204e794216193def17b390c4ba2) )
	
		ROM_REGION( 0x6000, REGION_GFX3,ROMREGION_DISPOSE ) 	/* sprite bank 0*/
		ROM_LOAD ( "w8.17m",  0x0000, 0x2000, CRC(e2afe3a3) SHA1(a83a12c0c6c62e45add916a6993f0ad06840c4d9) )
		ROM_LOAD ( "w9.17p",  0x2000, 0x2000, CRC(8a8e6b62) SHA1(6e7d4a84b7c78e009bce0641e357f74c8ac9e5ac) )
		ROM_LOAD ( "w10.17t", 0x4000, 0x2000, CRC(70bf7b23) SHA1(6774eceb0bfea66156ecd837f9d0adbdf8dec8ee) )
	
		ROM_REGION( 0x6000, REGION_GFX4,ROMREGION_DISPOSE ) 	/* sprite bank 1*/
		ROM_LOAD ( "11.18m", 0x0000, 0x2000, CRC(06a5d849) SHA1(b9f604edf4fdc053b738041493aef91dd730fe6b) )
		ROM_LOAD ( "12.18r", 0x2000, 0x2000, CRC(569815ef) SHA1(db261799892f60b2274b73fb25cde58219bb44db) )
		ROM_LOAD ( "13.18t", 0x4000, 0x2000, CRC(4f8af872) SHA1(6c07ff0733b8d8440309c9ae0db0876587b740a6) )
	
		ROM_REGION( 0x6000, REGION_GFX5,ROMREGION_DISPOSE )	/* sprite bank 2*/
		ROM_LOAD ( "14.19m", 0x0000, 0x2000, CRC(93f3c649) SHA1(38d6bb4b6108a67b135ae1a145532f4a0c2568b8) )
		ROM_LOAD ( "15.19r", 0x2000, 0x2000, CRC(5b3d87e4) SHA1(e47f7b62bf7101afba8d5e181f4bd8f8eb6eeb08) )
		ROM_LOAD ( "16.19u", 0x4000, 0x2000, CRC(a2c24b64) SHA1(e76558785ea337ab902fb6f94dc1a4bdfcd6335e) )
	
		ROM_REGION( 0x8000, REGION_USER1,ROMREGION_NODISPOSE )	/* road layout */
		ROM_LOAD ( "1.17a", 0x0000, 0x2000, CRC(f0fa72fc) SHA1(b73e794df635630f29a79adfe2951dc8f1d17e20) )
		ROM_LOAD ( "3.18b", 0x2000, 0x2000, CRC(954268f7) SHA1(07057296e0281f90b18dfe4223aad18bff7cfa6e) )
		ROM_LOAD ( "2.17d", 0x4000, 0x2000, CRC(2e38bb0e) SHA1(684f14a06ff957e40780be21c0ad5f10088a55ed) )
		ROM_LOAD ( "4.18d", 0x6000, 0x2000, CRC(3d9e16ab) SHA1(e99628ffc54e3ff4818313a287ca111617120910) )
	
		ROM_REGION( 0x300, REGION_PROMS,ROMREGION_NODISPOSE )	/* colour */
		ROM_LOAD("tbp24s10.7u", 0x0000, 0x0100, CRC(9d199d33) SHA1(b8982f7da2b85f10d117177e4e73cbb486931cf5) )
		ROM_LOAD("tbp24s10.7t", 0x0100, 0x0100, CRC(c0426582) SHA1(8e3e4d1e76243cce272aa099d2d6ad4fa6c99f7c) )
		ROM_LOAD("tbp24s10.6t", 0x0200, 0x0100, CRC(c096e05c) SHA1(cb5b509e6124453f381a683ba446f8f4493d4610) )
	
		ROM_REGION( 0x10000,REGION_CPU2,0 )
		ROM_LOAD( "8.6f", 0x0000, 0x1000, CRC(6ec3c545) SHA1(1a2477b9e1563734195b0743f5dbbb005e06022e) )
	ROM_END
	
	ROM_START( rollace2 )
		ROM_REGION( 0x10000, REGION_CPU1,0 )	/* 64k for code */
		ROM_LOAD( "8k.764", 0x0000, 0x2000, CRC(a7abff82) SHA1(d49635f98b28b2b5e2833d25b0961addac2c3e6f) )
		ROM_LOAD( "8h.764", 0x2000, 0x2000, CRC(9716ba03) SHA1(8a7bfc1dce3b1b0c634690e0637e0a30776c0334) )
		ROM_LOAD( "8f.764", 0x4000, 0x2000, CRC(3eadb0e8) SHA1(6ff5b76360597f3a6a9718e505295c8557e569ae) )
		ROM_LOAD( "8d.764", 0x6000, 0x2000, CRC(baac14db) SHA1(9707b59a6506eb11c0a6b88364a784469ccdbb96) )
		ROM_LOAD( "8c.764", 0x8000, 0x2000, CRC(b418ce84) SHA1(876be297a671328138a9238d42871f22bb568cda) )
	
		ROM_REGION( 0x6000,REGION_GFX1,ROMREGION_DISPOSE )	/* characters */
		ROM_LOAD( "7m.764", 0x0000, 0x2000, CRC(8b9b27af) SHA1(a52894adb739f14a5949b6d15dd7b03ce5716d9a) )
		ROM_LOAD( "8m.764", 0x2000, 0x2000, CRC(2dfc38f2) SHA1(c0ad3a7d1f5249c159c355d709cc3039fbb7a3b2) )
		ROM_LOAD( "9m.764", 0x4000, 0x2000, CRC(2e3a825b) SHA1(d0d25d9a0fe31d46cb6cc999da3d9fc14f23251f) )
	
		ROM_REGION( 0x6000, REGION_GFX2,ROMREGION_DISPOSE )	/* road graphics */
		ROM_LOAD ( "6.20k", 0x0000, 0x2000, CRC(003d7515) SHA1(d8d84d690478cad16101f2ef9a1ae1ae74d01c88) )
		ROM_LOAD ( "7.18k", 0x2000, 0x2000, CRC(27843afa) SHA1(81d3031a2c06086461110696a0ee11d32992ecac) )
		ROM_LOAD ( "5.20f", 0x4000, 0x2000, CRC(51dd0108) SHA1(138c0aba6c952204e794216193def17b390c4ba2) )
	
		ROM_REGION( 0x6000, REGION_GFX3,ROMREGION_DISPOSE ) 	/* sprite bank 0*/
		ROM_LOAD ( "17n.764",0x0000, 0x2000, CRC(3365703c) SHA1(7cf374ba25f4fd163a66c0aea74ddfd3003c7992) )
		ROM_LOAD ( "9.17r",  0x2000, 0x2000, CRC(69b23461) SHA1(73eca5e721425f37df311454bd5b4e632b096eba) )
		ROM_LOAD ( "17t.764",0x4000, 0x2000, CRC(5e84cc9b) SHA1(33cdf7b756ade8c0dd1dcdad583af4de02cd51eb) )
	
		ROM_REGION( 0x6000, REGION_GFX4,ROMREGION_DISPOSE ) 	/* sprite bank 1*/
		ROM_LOAD ( "11.18m", 0x0000, 0x2000, CRC(06a5d849) SHA1(b9f604edf4fdc053b738041493aef91dd730fe6b) )
		ROM_LOAD ( "12.18r", 0x2000, 0x2000, CRC(569815ef) SHA1(db261799892f60b2274b73fb25cde58219bb44db) )
		ROM_LOAD ( "13.18t", 0x4000, 0x2000, CRC(4f8af872) SHA1(6c07ff0733b8d8440309c9ae0db0876587b740a6) )
	
		ROM_REGION( 0x6000, REGION_GFX5,ROMREGION_DISPOSE )	/* sprite bank 2*/
		ROM_LOAD ( "14.19m", 0x0000, 0x2000, CRC(93f3c649) SHA1(38d6bb4b6108a67b135ae1a145532f4a0c2568b8) )
		ROM_LOAD ( "15.19r", 0x2000, 0x2000, CRC(5b3d87e4) SHA1(e47f7b62bf7101afba8d5e181f4bd8f8eb6eeb08) )
		ROM_LOAD ( "16.19u", 0x4000, 0x2000, CRC(a2c24b64) SHA1(e76558785ea337ab902fb6f94dc1a4bdfcd6335e) )
	
		ROM_REGION( 0x8000, REGION_USER1,ROMREGION_NODISPOSE )	/* road layout */
		ROM_LOAD ( "1.17a",  0x0000, 0x2000, CRC(f0fa72fc) SHA1(b73e794df635630f29a79adfe2951dc8f1d17e20) )
		ROM_LOAD ( "3.18b",  0x2000, 0x2000, CRC(954268f7) SHA1(07057296e0281f90b18dfe4223aad18bff7cfa6e) )
		ROM_LOAD ( "17d.764",0x4000, 0x2000, CRC(32e69320) SHA1(d399a8c3b0319178d75f68f1a9b65b3efd91e00a) )
		ROM_LOAD ( "4.18d",  0x6000, 0x2000, CRC(3d9e16ab) SHA1(e99628ffc54e3ff4818313a287ca111617120910) )
	
		ROM_REGION( 0x300, REGION_PROMS,ROMREGION_NODISPOSE )	/* colour */
		ROM_LOAD("tbp24s10.7u", 0x0000, 0x0100, CRC(9d199d33) SHA1(b8982f7da2b85f10d117177e4e73cbb486931cf5) )
		ROM_LOAD("tbp24s10.7t", 0x0100, 0x0100, CRC(c0426582) SHA1(8e3e4d1e76243cce272aa099d2d6ad4fa6c99f7c) )
		ROM_LOAD("tbp24s10.6t", 0x0200, 0x0100, CRC(c096e05c) SHA1(cb5b509e6124453f381a683ba446f8f4493d4610) )
	
		ROM_REGION( 0x10000,REGION_CPU2,0 )
		ROM_LOAD( "8.6f", 0x0000, 0x1000, CRC(6ec3c545) SHA1(1a2477b9e1563734195b0743f5dbbb005e06022e) )
	ROM_END
	
	GAMEX( 1983, fightrol, 0,        rollrace, rollrace, 0, ROT270, "[Kaneko] (Taito license)", "Fighting Roller", GAME_IMPERFECT_SOUND|GAME_IMPERFECT_COLORS )
	GAMEX( 1983, rollace,  fightrol, rollrace, rollrace, 0, ROT270, "[Kaneko] (Williams license)", "Roller Aces (set 1)", GAME_IMPERFECT_SOUND|GAME_IMPERFECT_COLORS )
	GAMEX( 1983, rollace2, fightrol, rollace2, rollrace, 0, ROT90,  "[Kaneko] (Williams license)", "Roller Aces (set 2)", GAME_IMPERFECT_SOUND|GAME_IMPERFECT_COLORS )
}
