/***************************************************************************

Vastar memory map (preliminary)

driver by Allard Van Der Bas

CPU #1:

0000-7fff ROM
8000-83ff bg #1 attribute RAM
8800-8bff bg #1 video RAM
8c00-8fff bg #1 color RAM
9000-93ff bg #2 attribute RAM
9800-9bff bg #2 video RAM
9c00-9fff bg #2 color RAM
a000-a3ff used only during startup - it's NOT a part of the RAM test
c400-c7ff fg color RAM
c800-cbff fg attribute RAM
cc00-cfff fg video RAM
f000-f7ff RAM (f000-f0ff is shared with CPU #2)

read:
e000      ???

write:
c410-c41f sprites
c430-c43f sprites
c7c0-c7df bg #2 scroll
c7e0-c7ff bg #1 scroll
c810-c81f sprites
c830-c83f sprites
cc10-cc1f sprites
cc30-cc3f sprites
e000      ???

I/O:
read:

write:
02        0 = hold CPU #2?

CPU #2:

0000-1fff ROM
4000-43ff RAM (shared with CPU #1)

read:
8000      IN1
8040      IN0
8080      IN2

write:

I/O:
read:
02        8910 read (port A = DSW0 port B = DSW1)

write:
00        8910 control
01        8910 write

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class vastar
{
	
	
	
	
	
	static unsigned char *vastar_sharedram;
	
	
	
	public static MachineInitHandlerPtr machine_init_vastar  = new MachineInitHandlerPtr() { public void handler(){
		/* we must start with the second CPU halted */
		cpu_set_reset_line(1,ASSERT_LINE);
	} };
	
	public static WriteHandlerPtr vastar_hold_cpu2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* I'm not sure that this works exactly like this */
		if (data & 1)
			cpu_set_reset_line(1,CLEAR_LINE);
		else
			cpu_set_reset_line(1,ASSERT_LINE);
	} };
	
	public static ReadHandlerPtr vastar_sharedram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return vastar_sharedram[offset];
	} };
	
	public static WriteHandlerPtr vastar_sharedram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		vastar_sharedram[offset] = data;
	} };
	
	public static WriteHandlerPtr flip_screen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		flip_screen_set(data);
	} };
	
	
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0x8fff, vastar_bg2videoram_r ),
		new Memory_ReadAddress( 0x9000, 0x9fff, vastar_bg1videoram_r ),
		new Memory_ReadAddress( 0xa000, 0xafff, vastar_bg2videoram_r ),	/* mirror address */
		new Memory_ReadAddress( 0xb000, 0xbfff, vastar_bg1videoram_r ),	/* mirror address */
		new Memory_ReadAddress( 0xc400, 0xcfff, MRA_RAM ),
		new Memory_ReadAddress( 0xe000, 0xe000, watchdog_reset_r ),
		new Memory_ReadAddress( 0xf000, 0xf0ff, vastar_sharedram_r ),
		new Memory_ReadAddress( 0xf100, 0xf7ff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x8fff, vastar_bg2videoram_w, vastar_bg2videoram ),
		new Memory_WriteAddress( 0x9000, 0x9fff, vastar_bg1videoram_w, vastar_bg1videoram ),
		new Memory_WriteAddress( 0xa000, 0xafff, vastar_bg2videoram_w ),				/* mirror address */
		new Memory_WriteAddress( 0xb000, 0xbfff, vastar_bg1videoram_w ),  				/* mirror address */
		new Memory_WriteAddress( 0xc000, 0xc000, MWA_RAM, vastar_sprite_priority ),	/* sprite/BG priority */
		new Memory_WriteAddress( 0xc400, 0xcfff, vastar_fgvideoram_w, vastar_fgvideoram ),
		new Memory_WriteAddress( 0xe000, 0xe000, watchdog_reset_w ),
		new Memory_WriteAddress( 0xf000, 0xf0ff, vastar_sharedram_w, vastar_sharedram ),
		new Memory_WriteAddress( 0xf100, 0xf7ff, MWA_RAM ),
	
		/* in hidden portions of video RAM: */
		new Memory_WriteAddress( 0xc400, 0xc43f, MWA_RAM, spriteram, spriteram_size ),	/* actually c410-c41f and c430-c43f */
		new Memory_WriteAddress( 0xc7c0, 0xc7df, MWA_RAM, vastar_bg1_scroll ),
		new Memory_WriteAddress( 0xc7e0, 0xc7ff, MWA_RAM, vastar_bg2_scroll ),
		new Memory_WriteAddress( 0xc800, 0xc83f, MWA_RAM, spriteram_2 ),	/* actually c810-c81f and c830-c83f */
		new Memory_WriteAddress( 0xcc00, 0xcc3f, MWA_RAM, spriteram_3 ),	/* actually cc10-cc1f and cc30-cc3f */
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x00, interrupt_enable_w ),
		new IO_WritePort( 0x01, 0x01, flip_screen_w ),
		new IO_WritePort( 0x02, 0x02, vastar_hold_cpu2_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress cpu2_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new Memory_ReadAddress( 0x4000, 0x40ff, vastar_sharedram_r ),
		new Memory_ReadAddress( 0x8000, 0x8000, input_port_1_r ),
		new Memory_ReadAddress( 0x8040, 0x8040, input_port_0_r ),
		new Memory_ReadAddress( 0x8080, 0x8080, input_port_2_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress cpu2_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new Memory_WriteAddress( 0x4000, 0x40ff, vastar_sharedram_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort cpu2_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x02, 0x02, AY8910_read_port_0_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort cpu2_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x00, AY8910_control_port_0_w ),
		new IO_WritePort( 0x01, 0x01, AY8910_write_port_0_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	
	static InputPortPtr input_ports_vastar = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vastar )
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "4" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "6" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "Show Author Credits" );
		PORT_DIPSETTING(    0x08, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_BITX(    0x10, 0x10, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Slow Motion", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x20, "20000 50000" );
		PORT_DIPSETTING(	0x00, "40000 70000" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x80, 0x80, "Freeze" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		2,
		new int[] { 0, 4 },
		new int[] { 0, 1, 2, 3, 8*8+0, 8*8+1, 8*8+2, 8*8+3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		16*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,1),
		2,
		new int[] { 0, 4 },
		new int[] { 0, 1, 2, 3, 8*8+0, 8*8+1, 8*8+2, 8*8+3,
				16*8+0, 16*8+1, 16*8+2, 16*8+3, 24*8+0, 24*8+1, 24*8+2, 24*8+3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				32*8, 33*8, 34*8, 35*8, 36*8, 37*8, 38*8, 39*8 },
		64*8
	);
	
	static GfxLayout spritelayoutdw = new GfxLayout
	(
		16,32,
		RGN_FRAC(1,1),
		2,
		new int[] { 0, 4 },
		new int[] { 0, 1, 2, 3, 8*8+0, 8*8+1, 8*8+2, 8*8+3,
				16*8+0, 16*8+1, 16*8+2, 16*8+3, 24*8+0, 24*8+1, 24*8+2, 24*8+3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				32*8, 33*8, 34*8, 35*8, 36*8, 37*8, 38*8, 39*8,
				64*8, 65*8, 66*8, 67*8, 68*8, 69*8, 70*8, 71*8,
				96*8, 97*8, 98*8, 99*8, 100*8, 101*8, 102*8, 103*8 },
		128*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,     0, 64 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout,   0, 64 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayoutdw, 0, 64 ),
		new GfxDecodeInfo( REGION_GFX3, 0, charlayout,     0, 64 ),
		new GfxDecodeInfo( REGION_GFX4, 0, charlayout,     0, 64 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		1,	/* 1 chip */
		1500000,	/* 1.5 MHz??????? */
		new int[] { 50 },
		new ReadHandlerPtr[] { input_port_3_r },
		new ReadHandlerPtr[] { input_port_4_r },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	
	
	static MACHINE_DRIVER_START( vastar )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 3072000)	/* 3.072 MHz ???? */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(0,writeport)
		MDRV_CPU_VBLANK_INT(nmi_line_pulse,1)
	
		MDRV_CPU_ADD(Z80, 3072000)	/* 3.072 MHz ???? */
		MDRV_CPU_MEMORY(cpu2_readmem,cpu2_writemem)
		MDRV_CPU_PORTS(cpu2_readport,cpu2_writeport)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,4)	/* ??? */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
		MDRV_INTERLEAVE(10)	/* 10 CPU slices per frame - seems enough to ensure proper */
							/* synchronization of the CPUs */
		MDRV_MACHINE_INIT(vastar)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(256)
	
		MDRV_PALETTE_INIT(RRRR_GGGG_BBBB)
		MDRV_VIDEO_START(vastar)
		MDRV_VIDEO_UPDATE(vastar)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
	MACHINE_DRIVER_END
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_vastar = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "e_f4.rom",     0x0000, 0x1000, CRC(45fa5075) SHA1(99c3d7414f3bc3a84430067a71dd00d260bbcdab) )
		ROM_LOAD( "e_k4.rom",     0x1000, 0x1000, CRC(84531982) SHA1(bf2fd92d821734f64ad72e13f4e1aae8e055aa43) )
		ROM_LOAD( "e_h4.rom",     0x2000, 0x1000, CRC(94a4f778) SHA1(d52b3d6ed4953cff6dde1884dec9f9cc94847cb2) )
		ROM_LOAD( "e_l4.rom",     0x3000, 0x1000, CRC(40e4d57b) SHA1(3f073574f430791518283314ce325e48d8daa246) )
		ROM_LOAD( "e_j4.rom",     0x4000, 0x1000, CRC(bd607651) SHA1(23d3c7d2a0c17a780286a01a93e480aafcdb4b05) )
		ROM_LOAD( "e_n4.rom",     0x5000, 0x1000, CRC(7a3779a4) SHA1(98e7092ed4eaec1ab129a7bede6ea1cf16e329f0) )
		ROM_LOAD( "e_n7.rom",     0x6000, 0x1000, CRC(31b6be39) SHA1(be0d03db9c6c8982b2f38ad534a6e213bbde1802) )
		ROM_LOAD( "e_n5.rom",     0x7000, 0x1000, CRC(f63f0e78) SHA1(a029e340b11b358dbe0dcf2d1a0e6c6c093bbc9d) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the second CPU */
		ROM_LOAD( "e_f2.rom",     0x0000, 0x1000, CRC(713478d8) SHA1(9cbd1fb689d93a8964f48e59d4effaa4878b2945) )
		ROM_LOAD( "e_j2.rom",     0x1000, 0x1000, CRC(e4535442) SHA1(280d93bec5cf6183250827ce70ed5ddff968bba5) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "c_c9.rom",     0x0000, 0x2000, CRC(34f067b6) SHA1(45d7f8be5bd1dc9e5e511aa2e99c216c5ff12273) )
	
		ROM_REGION( 0x4000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "c_f7.rom",     0x0000, 0x2000, CRC(edbf3b13) SHA1(9d6ddf16e83c68c831fec28607584471b5cbcbd2) )
		ROM_LOAD( "c_f9.rom",     0x2000, 0x2000, CRC(8f309e22) SHA1(f5bbc5cf70687415061a0674e273e20fbfcc1f8f) )
	
		ROM_REGION( 0x2000, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "c_n4.rom",     0x0000, 0x2000, CRC(b5f9c866) SHA1(17fc38cd40638e4f5d25c0cae70df3b8f03425dd) )
	
		ROM_REGION( 0x2000, REGION_GFX4, ROMREGION_DISPOSE )
		ROM_LOAD( "c_s4.rom",     0x0000, 0x2000, CRC(c9fbbfc9) SHA1(7c6ace0e2eae8420a31d9054ad5dd94924273d5f) )
	
		ROM_REGION( 0x0400, REGION_PROMS, 0 )
		ROM_LOAD( "tbp24s10.6p",  0x0000, 0x0100, CRC(a712d73a) SHA1(a65fa5928431d8631fb04e01ad0a0d2de849bf1d) )	/* red component */
		ROM_LOAD( "tbp24s10.6s",  0x0100, 0x0100, CRC(0a7d48ec) SHA1(400e0b271c241712e7b7502e96e4f8a609e078e1) )	/* green component */
		ROM_LOAD( "tbp24s10.6m",  0x0200, 0x0100, CRC(4c3db907) SHA1(03bcbc4763dcf49f4a06f499042e36183aa8b762) )	/* blue component */
		ROM_LOAD( "tbp24s10.8n",  0x0300, 0x0100, CRC(b5297a3b) SHA1(a5a512f86097b7d892f6d11e8492e8a379c07f60) )	/* ???? */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vastar2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "3.4f",         0x0000, 0x1000, CRC(6741ff9c) SHA1(d83e8233626845962b4cf9302d4aa75915017f36) )
		ROM_LOAD( "6.4k",         0x1000, 0x1000, CRC(5027619b) SHA1(5fa1d53f6ee125048d4ef3bc3bff5655648c5bd6) )
		ROM_LOAD( "4.4h",         0x2000, 0x1000, CRC(fdaa44e6) SHA1(7e4dbd924d001d1d3ffb86dd0e88d363ef32fa5f) )
		ROM_LOAD( "7.4l",         0x3000, 0x1000, CRC(29bef91c) SHA1(bc8eacac39c73b92ee84ea20c32e6987c4dd450b) )
		ROM_LOAD( "5.4j",         0x4000, 0x1000, CRC(c17c2458) SHA1(585022ca6df8568d0bf6fc4dc2e77909b3c8ab54) )
		ROM_LOAD( "8.4n",         0x5000, 0x1000, CRC(8ca25c37) SHA1(c8307a8453c426075927a4a8a20edd48c6c74f05) )
		ROM_LOAD( "10.6n",        0x6000, 0x1000, CRC(80df74ba) SHA1(5cbc75fb96ad6d63186ec42a5e9af6aae209d78f) )
		ROM_LOAD( "9.5n",         0x7000, 0x1000, CRC(239ec84e) SHA1(8b516c63d858d5c4acc3701a9abf9c3d53ddf7ff) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the second CPU */
		ROM_LOAD( "e_f2.rom",     0x0000, 0x1000, CRC(713478d8) SHA1(9cbd1fb689d93a8964f48e59d4effaa4878b2945) )
		ROM_LOAD( "e_j2.rom",     0x1000, 0x1000, CRC(e4535442) SHA1(280d93bec5cf6183250827ce70ed5ddff968bba5) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "c_c9.rom",     0x0000, 0x2000, CRC(34f067b6) SHA1(45d7f8be5bd1dc9e5e511aa2e99c216c5ff12273) )
	
		ROM_REGION( 0x4000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "c_f7.rom",     0x0000, 0x2000, CRC(edbf3b13) SHA1(9d6ddf16e83c68c831fec28607584471b5cbcbd2) )
		ROM_LOAD( "c_f9.rom",     0x2000, 0x2000, CRC(8f309e22) SHA1(f5bbc5cf70687415061a0674e273e20fbfcc1f8f) )
	
		ROM_REGION( 0x2000, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "c_n4.rom",     0x0000, 0x2000, CRC(b5f9c866) SHA1(17fc38cd40638e4f5d25c0cae70df3b8f03425dd) )
	
		ROM_REGION( 0x2000, REGION_GFX4, ROMREGION_DISPOSE )
		ROM_LOAD( "c_s4.rom",     0x0000, 0x2000, CRC(c9fbbfc9) SHA1(7c6ace0e2eae8420a31d9054ad5dd94924273d5f) )
	
		ROM_REGION( 0x0400, REGION_PROMS, 0 )
		ROM_LOAD( "tbp24s10.6p",  0x0000, 0x0100, CRC(a712d73a) SHA1(a65fa5928431d8631fb04e01ad0a0d2de849bf1d) )	/* red component */
		ROM_LOAD( "tbp24s10.6s",  0x0100, 0x0100, CRC(0a7d48ec) SHA1(400e0b271c241712e7b7502e96e4f8a609e078e1) )	/* green component */
		ROM_LOAD( "tbp24s10.6m",  0x0200, 0x0100, CRC(4c3db907) SHA1(03bcbc4763dcf49f4a06f499042e36183aa8b762) )	/* blue component */
		ROM_LOAD( "tbp24s10.8n",  0x0300, 0x0100, CRC(b5297a3b) SHA1(a5a512f86097b7d892f6d11e8492e8a379c07f60) )	/* ???? */
	ROM_END(); }}; 
	
	
	
	GAME( 1983, vastar,  0,      vastar, vastar, 0, ROT90, "Sesame Japan", "Vastar (set 1)" )
	GAME( 1983, vastar2, vastar, vastar, vastar, 0, ROT90, "Sesame Japan", "Vastar (set 2)" )
}
