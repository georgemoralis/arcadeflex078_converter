/*
Car Jamboree
Omori Electric CAD (OEC) 1981

c14                c.d19
c13                c.d18           c10
c12                                c9
c11         2125   2125
            2125   2125
            2125   2125  2114 2114
            2125   2125  2114 2114
            2125   2125            c8
            2125   2125            c7
                                   c6
                                   c5
                                   c4
                                   c3
5101                               c2
5101                               c1
                                   6116
18.432MHz
          6116
Z80A      c15
                                   Z80A
       8910         SW
       8910

Notes:

- sound cpu speed chosen from coin error countdown, 1.536 MHz is too fast
  as it loses synchronisation with the onscreen timer

- some sprite glitches from sprite number/colour changes happening on
  different frames, possibly original behaviour. eg cars changing colour
  just before exploding, animals displaying as the wrong sprite for one
  frame when entering the arena

- colours are wrong, sprites and characters only using one of the proms

- background colour calculation is a guess
*/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class carjmbre
{
	
	
	
	
	public static Memory_ReadAddress carjmbre_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new Memory_ReadAddress( 0x8800, 0x8800, MRA_NOP ),			//?? possibly watchdog
		new Memory_ReadAddress( 0x9000, 0x97ff, videoram_r ),
		new Memory_ReadAddress( 0xa000, 0xa000, input_port_0_r ),
		new Memory_ReadAddress( 0xa800, 0xa800, input_port_1_r ),
		new Memory_ReadAddress( 0xb800, 0xb800, input_port_2_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress carjmbre_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new Memory_WriteAddress( 0x8803, 0x8803, interrupt_enable_w ),
		new Memory_WriteAddress( 0x8805, 0x8806, carjmbre_bgcolor_w ),	//guess
		new Memory_WriteAddress( 0x8807, 0x8807, carjmbre_flipscreen_w ),
		new Memory_WriteAddress( 0x8fc1, 0x8fc1, MWA_NOP ),			//overrun during initial screen clear
		new Memory_WriteAddress( 0x8fe1, 0x8fe1, MWA_NOP ),			//overrun during initial screen clear
		new Memory_WriteAddress( 0x9000, 0x97ff, carjmbre_videoram_w, videoram ),
		new Memory_WriteAddress( 0x9800, 0x985f, spriteram_w, spriteram, spriteram_size ),
		new Memory_WriteAddress( 0x9880, 0x98df, MWA_RAM ),			//spriteram mirror
		new Memory_WriteAddress( 0xb800, 0xb800, soundlatch_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress carjmbre_sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x0fff, MRA_ROM ),
		new Memory_ReadAddress( 0x1000, 0x10ff, MRA_NOP ),			//look to be stray reads from 10/12/14/16/18xx
		new Memory_ReadAddress( 0x1200, 0x12ff, MRA_NOP ),
		new Memory_ReadAddress( 0x1400, 0x14ff, MRA_NOP ),
		new Memory_ReadAddress( 0x1600, 0x16ff, MRA_NOP ),
		new Memory_ReadAddress( 0x1800, 0x18ff, MRA_NOP ),
		new Memory_ReadAddress( 0x2000, 0x27ff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress carjmbre_sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x0fff, MWA_ROM ),
		new Memory_WriteAddress( 0x2000, 0x27ff, MWA_RAM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort carjmbre_sound_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x00, soundlatch_r ),
		new IO_ReadPort( 0x24, 0x24, IORP_NOP ),				//??
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort carjmbre_sound_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x10, 0x10, IOWP_NOP ),				//?? written on init/0xff sound command reset
		new IO_WritePort( 0x20, 0x20, AY8910_control_port_0_w ),
		new IO_WritePort( 0x21, 0x21, AY8910_write_port_0_w ),
		new IO_WritePort( 0x22, 0x22, IOWP_NOP ),				//?? written before and after 0x21 with same value
		new IO_WritePort( 0x30, 0x30, AY8910_control_port_1_w ),
		new IO_WritePort( 0x31, 0x31, AY8910_write_port_1_w ),
		new IO_WritePort( 0x32, 0x32, IOWP_NOP ),				//?? written before and after 0x31 with same value
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	static InputPortPtr input_ports_carjmbre = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( carjmbre )
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );	//coin error if held high for 1s
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );	//or if many coins inserted quickly
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
	
		PORT_START(); 	/* IN2 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x18, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x08, "4" );
		PORT_DIPSETTING(    0x10, "5" );
		PORT_BITX( 0,       0x18, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "250", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "10k, then every 100k" );
		PORT_DIPSETTING(    0x20, "20k, then every 100k" );
		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
	INPUT_PORTS_END(); }}; 
	
	static GfxLayout carjmbre_charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(2,4),
		2,
		new int[] { RGN_FRAC(0,4), RGN_FRAC(2,4) },
		new int[] { STEP8(0,1) },
		new int[] { STEP8(0,8) },
		8*8
	);
	
	static GfxLayout carjmbre_spritelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,4),
		2,
		new int[] { RGN_FRAC(2,4), RGN_FRAC(0,4) },
		new int[] { STEP8(0,1), STEP8(256*16*8,1) },
		new int[] { STEP16(0,8) },
		16*8
	);
	
	static GfxDecodeInfo carjmbre_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, carjmbre_charlayout,   0, 16 ),
		new GfxDecodeInfo( REGION_GFX2, 0, carjmbre_spritelayout, 0, 16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static AY8910interface carjmbre_ay8910_interface = new AY8910interface
	(
		2,	/* 2 chips */
		1500000,
		new int[] { 12, 12 },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	static MACHINE_DRIVER_START( carjmbre )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80,18432000/6)
		MDRV_CPU_MEMORY(carjmbre_readmem,carjmbre_writemem)
		MDRV_CPU_VBLANK_INT(nmi_line_pulse,1)
	
		MDRV_CPU_ADD(Z80, 1500000)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)
		MDRV_CPU_MEMORY(carjmbre_sound_readmem,carjmbre_sound_writemem)
		MDRV_CPU_PORTS(carjmbre_sound_readport,carjmbre_sound_writeport)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(carjmbre_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(64)
		MDRV_COLORTABLE_LENGTH(64)
	
		MDRV_PALETTE_INIT(carjmbre)
		MDRV_VIDEO_START(carjmbre)
		MDRV_VIDEO_UPDATE(carjmbre)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, carjmbre_ay8910_interface)
	MACHINE_DRIVER_END
	
	static RomLoadPtr rom_carjmbre = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )
		ROM_LOAD( "c1",      0x0000, 0x1000, CRC(62b21739) SHA1(710e5c52f27603aa8f864f6f28d7272f21271d60) )
		ROM_LOAD( "c2",      0x1000, 0x1000, CRC(9ab1a0fa) SHA1(519cf67b98e62b2b42232788ba01ab6637880afc) )
		ROM_LOAD( "c3",      0x2000, 0x1000, CRC(bb29e100) SHA1(93e3cfcf7f8b0b36327f402d9a64c04c3b2c7549) )
		ROM_LOAD( "c4",      0x3000, 0x1000, CRC(c63d8f97) SHA1(9f08fd1cd24a1fb4011864c06580985e009d9af4) )
		ROM_LOAD( "c5",      0x4000, 0x1000, CRC(4d593942) SHA1(30cc649a4be3d7f3705f55d8d0dadb0b63d59ec9) )
		ROM_LOAD( "c6",      0x5000, 0x1000, CRC(fb576963) SHA1(5bf5c54a7c12aa55272629c12b414bf49cda0f1f) )
		ROM_LOAD( "c7",      0x6000, 0x1000, CRC(2b8c4511) SHA1(428a48d6b14455d66720a115bc5f35293dc50de7) )
		ROM_LOAD( "c8",      0x7000, 0x1000, CRC(51cc22a7) SHA1(f614368bfee04f084c70bf145801ac46e5631acb) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )
		ROM_LOAD( "c15",     0x0000, 0x1000, CRC(7d7779d1) SHA1(f8f5246be4cc9632076d3330fc3d3343b911dfee) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "c9",     0x0000, 0x1000, CRC(2accb821) SHA1(ce2804536fc1abd3377dc864c8c9976ca28c1b6e) )
		ROM_LOAD( "c10",    0x1000, 0x1000, CRC(75ddbe56) SHA1(5e1363967a822265618793ccb74bf3ef5e0e00b5) )
	
		ROM_REGION( 0x4000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "c11",    0x0000, 0x1000, CRC(d90cd126) SHA1(7ee110cf19b45ee654016ba0ce92f3db6ea2ed92) )
		ROM_LOAD( "c12",    0x1000, 0x1000, CRC(b3bb39d7) SHA1(89c901be6fae2356ce4d2653e94bf28d6bcf41fe) )
		ROM_LOAD( "c13",    0x2000, 0x1000, CRC(3004010b) SHA1(00d5d2185014159112eb90d8ed50092a3b4ab664) )
		ROM_LOAD( "c14",    0x3000, 0x1000, CRC(fb5f0d31) SHA1(7a27af91efc836bb48c6ed3b283b7c5f7b31c4b5) )
	
		ROM_REGION( 0x0040, REGION_PROMS, 0 )
		ROM_LOAD( "c.d19",  0x0000, 0x0020, CRC(220bceeb) SHA1(46b9f867d014596e2aa7503f104dc721965f0ed5) )
		ROM_LOAD( "c.d18",  0x0020, 0x0020, CRC(7b9ed1b0) SHA1(ec5e1f56e5a2fc726083866c08ac0e1de0ed6ace) )
	ROM_END(); }}; 
	
	GAMEX(1983, carjmbre, 0, carjmbre, carjmbre, 0, ROT90, "Omori Electric Co., Ltd.", "Car Jamboree", GAME_IMPERFECT_COLORS )
}
