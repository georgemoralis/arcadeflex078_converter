/*
	"Gladiator"
	(C) 1984 SNK


	known issues:
	sound/music doesn't sound good (but it might be correct)
	cocktail support is missing

Change Log
----------

AT08XX03:
 - fixed music tempo, shadows and reduced sprite lag

   The chips should be in good sync but quite a bit of lag
   remains without overclocking CPUB. I guess you can't beat
   a game with barrel-rolling bad guys in plate armors who
   can outrun a horse.
*/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class sgladiat
{
	
	static unsigned char *shared_ram, *shared_ram2;
	
	static AY8910interface ay8910_interface = new AY8910interface(
		2,	/* number of chips */
		2000000, /* 2 MHz? */
		new int[] { 25,25 }, /* volume */
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	static GfxLayout tile_layout = new GfxLayout
	(
		8,8,
		256,
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 4, 0, 12, 8, 20, 16, 28, 24},
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		256
	);
	
	static GfxLayout sprite_layout = new GfxLayout
	(
		16,16,
		256,
		3,
		new int[] { 2*0x2000*8,1*0x2000*8,0*0x2000*8},
		new int[] { 7,6,5,4,3,2,1,0, 15,14,13,12,11,10,9,8 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
			8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
		256
	);
	
	static GfxDecodeInfo tnk3_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0, tile_layout,	128*3,  8 ),
		new GfxDecodeInfo( REGION_GFX2, 0x0, tile_layout,	128*1, 16 ),
		new GfxDecodeInfo( REGION_GFX3, 0x0, sprite_layout,	128*0, 16 ),
		new GfxDecodeInfo( -1 )
	};
	
	/************************************************************************/
	
	public static ReadHandlerPtr shared_ram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return shared_ram[offset];
	} };
	public static WriteHandlerPtr shared_ram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		shared_ram[offset] = data;
	} };
	
	public static ReadHandlerPtr shared_ram2_r  = new ReadHandlerPtr() { public int handler(int offset){
		return shared_ram2[offset];
	} };
	public static WriteHandlerPtr shared_ram2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		shared_ram2[offset] = data;
	} };
	
	/************************************************************************/
	
	public static WriteHandlerPtr sgladiat_soundlatch_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		snk_sound_busy_bit = 0x20;
		soundlatch_w.handler( offset, data );
	
		/* trigger NMI on sound CPU */
	//	cpu_set_nmi_line(2, PULSE_LINE);
		cpu_set_nmi_line(2, PULSE_LINE);	// safer because NMI can be lost in rare occations
	} };
	
	public static ReadHandlerPtr sgladiat_soundlatch_r  = new ReadHandlerPtr() { public int handler(int offset){
		snk_sound_busy_bit = 0;
		return(soundlatch_r(0));
	} };
	
	public static ReadHandlerPtr sgladiat_sound_nmi_ack_r  = new ReadHandlerPtr() { public int handler(int offset){
	//	cpu_set_nmi_line(2, CLEAR_LINE);
		return 0;
	} };
	
	/************************************************************************/
	
	public static ReadHandlerPtr sgladiat_inp0_r  = new ReadHandlerPtr() { public int handler(int offset){
		return(readinputport(0) | snk_sound_busy_bit);
	} };
	
	public static WriteHandlerPtr sglatiat_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* 0xa006 */
		/* x-------	screen is flipped */
	} };
	
	public static Memory_ReadAddress sgladiat_readmem_cpuA[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x9fff, MRA_ROM ),
		new Memory_ReadAddress( 0xa000, 0xa000, sgladiat_inp0_r ),
		new Memory_ReadAddress( 0xa100, 0xa100, input_port_1_r ), /* joy1 */
		new Memory_ReadAddress( 0xa200, 0xa200, input_port_2_r ), /* joy2 */
		new Memory_ReadAddress( 0xa400, 0xa400, input_port_3_r ), /* dsw1 */
		new Memory_ReadAddress( 0xa500, 0xa500, input_port_4_r ), /* dsw2 */
		new Memory_ReadAddress( 0xa700, 0xa700, snk_cpuB_nmi_trigger_r ),
		new Memory_ReadAddress( 0xd800, 0xdfff, MRA_RAM ), /* spriteram */
		new Memory_ReadAddress( 0xe000, 0xe7ff, MRA_RAM ), /* videoram */
		new Memory_ReadAddress( 0xe800, 0xefff, MRA_RAM ), /* work ram */
		new Memory_ReadAddress( 0xf000, 0xf7ff, MRA_RAM ), /* shareram */
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress sgladiat_writemem_cpuA[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x9fff, MWA_ROM ),
		new Memory_WriteAddress( 0xa300, 0xa300, sgladiat_soundlatch_w ),
		new Memory_WriteAddress( 0xa600, 0xa600, sglatiat_flipscreen_w ),
		new Memory_WriteAddress( 0xa700, 0xa700, snk_cpuA_nmi_ack_w ),
		new Memory_WriteAddress( 0xd000, 0xd7ff, MWA_RAM, shared_ram2 ),
	//		new Memory_WriteAddress( 0xd200, 0xd200, MWA_RAM ), /* ?0x24 */
	//		new Memory_WriteAddress( 0xd300, 0xd300, MWA_RAM ), /* ------xx: msb scrollx */
	//		new Memory_WriteAddress( 0xd400, 0xd400, MWA_RAM ), /* xscroll (sprite) */
	//		new Memory_WriteAddress( 0xd500, 0xd500, MWA_RAM ), /* yscroll (sprite) */
	//		new Memory_WriteAddress( 0xd600, 0xd600, MWA_RAM ), /* xscroll (bg) */
	//		new Memory_WriteAddress( 0xd700, 0xd700, MWA_RAM ), /* yscroll (bg) */
		new Memory_WriteAddress( 0xd800, 0xdfff, MWA_RAM, spriteram ),
		new Memory_WriteAddress( 0xe000, 0xe7ff, MWA_RAM, videoram ),
		new Memory_WriteAddress( 0xe800, 0xefff, MWA_RAM ),
		new Memory_WriteAddress( 0xf000, 0xf7ff, MWA_RAM, shared_ram ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress sgladiat_readmem_cpuB[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0xa000, 0xa000, snk_cpuA_nmi_trigger_r ),
		new Memory_ReadAddress( 0xc000, 0xcfff, spriteram_r ), /* 0xc800..0xcfff is videoram */
		new Memory_ReadAddress( 0xd800, 0xdfff, shared_ram2_r ),
		new Memory_ReadAddress( 0xe000, 0xe7ff, shared_ram_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress sgladiat_writemem_cpuB[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x9fff, MWA_ROM ),
		new Memory_WriteAddress( 0xa000, 0xa000, snk_cpuB_nmi_ack_w ),
		new Memory_WriteAddress( 0xa600, 0xa600, sglatiat_flipscreen_w ),
		new Memory_WriteAddress( 0xc000, 0xcfff, spriteram_w ), /* 0xc800..0xcfff is videoram */
		new Memory_WriteAddress( 0xd800, 0xdfff, shared_ram2_w ),
		new Memory_WriteAddress( 0xe000, 0xe7ff, shared_ram_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress sgladiat_readmem_sound[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new Memory_ReadAddress( 0xa000, 0xa000, sgladiat_soundlatch_r ),
		new Memory_ReadAddress( 0xc000, 0xc000, sgladiat_sound_nmi_ack_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress sgladiat_writemem_sound[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new Memory_WriteAddress( 0xe000, 0xe000, AY8910_control_port_0_w ),
		new Memory_WriteAddress( 0xe001, 0xe001, AY8910_write_port_0_w ),
		new Memory_WriteAddress( 0xe002, 0xe003, MWA_NOP ),	// leftover wave generator ports?
		new Memory_WriteAddress( 0xe004, 0xe004, AY8910_control_port_1_w ),
		new Memory_WriteAddress( 0xe005, 0xe005, AY8910_write_port_1_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort sgladiat_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x00, MRA_NOP ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	static MACHINE_DRIVER_START( sgladiat )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 4000000)
		MDRV_CPU_MEMORY(sgladiat_readmem_cpuA,sgladiat_writemem_cpuA)
	//	MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_CPU_ADD(Z80, 5000000)
		MDRV_CPU_MEMORY(sgladiat_readmem_cpuB,sgladiat_writemem_cpuB)
		MDRV_CPU_VBLANK_INT(snk_irq_BA,1)
	
		MDRV_CPU_ADD(Z80, 4000000)
		MDRV_CPU_MEMORY(sgladiat_readmem_sound,sgladiat_writemem_sound)
		MDRV_CPU_PORTS(sgladiat_readport,0)
		MDRV_CPU_PERIODIC_INT(irq0_line_hold, 244)	// Marvin's frequency, sounds ok
	
		MDRV_FRAMES_PER_SECOND(60.606060)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_INTERLEAVE(300)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_HAS_SHADOWS)
		MDRV_SCREEN_SIZE(36*8, 28*8)
		MDRV_VISIBLE_AREA(0*8+16, 36*8-1-16, 1*8, 28*8-1)
		MDRV_GFXDECODE(tnk3_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_PALETTE_INIT(aso)
		MDRV_VIDEO_START(sgladiat)
		MDRV_VIDEO_UPDATE(sgladiat)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
	MACHINE_DRIVER_END
	
	static RomLoadPtr rom_sgladiat = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for cpuA code */
		ROM_LOAD( "glad.005",	0x0000, 0x4000, CRC(4bc60f0b) SHA1(19baf7533b8fc6bab372f1d35603068a6b93627c) )
		ROM_LOAD( "glad.004",	0x4000, 0x4000, CRC(db557f46) SHA1(dc3565096c95a20d2c64dd4f5d0b465fbd85e041) )
		ROM_LOAD( "glad.003",	0x8000, 0x2000, CRC(55ce82b4) SHA1(703f017a8501e4dd5166b5717b244aa6b1e7dc0a) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for cpuB code */
		ROM_LOAD( "glad.002",	0x0000, 0x4000, CRC(8350261c) SHA1(046099128d5d941b3a37dce559ff5f0aa7f61683) )
		ROM_LOAD( "glad.001",	0x4000, 0x4000, CRC(5ee9d3fb) SHA1(6c8d8db7a966d3d3a2e8c46fd779e12e1f1e3716) )
	
		ROM_REGION( 0x10000, REGION_CPU3, 0 )	/* 64k for sound code */
		ROM_LOAD( "glad.007",  0x0000, 0x2000, CRC(c25b6731) SHA1(519c6844bfec958b9bb65f148b3527b41fe38b99) )
		ROM_LOAD( "glad.006",  0x2000, 0x2000, CRC(2024d716) SHA1(6ff069fc53524d13c386e8e714ba3056509adc4d) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "glad.011",	0x0000, 0x2000, CRC(305bb294) SHA1(e148571a581b12ff9502a65ec428e4d19bc757cb) ) /* foreground tiles */
	
		ROM_REGION( 0x2000, REGION_GFX2, ROMREGION_DISPOSE ) /* background tiles */
		ROM_LOAD( "glad.012",	0x0000, 0x2000, CRC(b7dd519f) SHA1(7bd760e54712648105d1049e678fa6b9effa600b) )
	
		ROM_REGION( 0x6000, REGION_GFX3, ROMREGION_DISPOSE ) /* 16x16 sprites */
		ROM_LOAD( "glad.008", 0x0000, 0x2000, CRC(bcf42587) SHA1(1546fe903fbc6dc0b410c83ab51887c33c91ec2d) )
		ROM_LOAD( "glad.009", 0x2000, 0x2000, CRC(912a20e0) SHA1(9621b955bc00b7c52ed8363bb441b568efb55863) )
		ROM_LOAD( "glad.010", 0x4000, 0x2000, CRC(8b1db3a5) SHA1(5ca403d40071ab13deb7fdb04cb0e055e6b30b05) )
	
		ROM_REGION( 0xc00, REGION_PROMS, 0 )
		ROM_LOAD( "82s137.001",  0x000, 0x400, CRC(d9184823) SHA1(455c6a437d54c29673dddb8248ca78d000c7f354) )
		ROM_LOAD( "82s137.002",  0x400, 0x400, CRC(1a6b0953) SHA1(552ac2897abe507f2fd9ca11c8128a0314af215c) )
		ROM_LOAD( "82s137.003",  0x800, 0x400, CRC(c0e70308) SHA1(d7dbc500bc9991c2d1b95850f3723a2a224fbfbb) )
	ROM_END(); }}; 
	
	static InputPortPtr input_ports_sgladiat = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( sgladiat )
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* sound CPU status */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW1 - copied from TNK3! */
		PORT_BITX( 0x01,    0x01, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Walk everywhere", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x04, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x20, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0xc0, "20k 60k" );
		PORT_DIPSETTING(    0x80, "40k 90k" );
		PORT_DIPSETTING(    0x40, "50k 120k" );
		PORT_DIPSETTING(    0x00, "None" );
	
		PORT_START(); 	/* DSW2 - copied from TNK3!  */
		PORT_DIPNAME( 0x01, 0x01, "Bonus Occurrence" );
		PORT_DIPSETTING(    0x01, "1st & every 2nd" );
		PORT_DIPSETTING(    0x00, "1st & 2nd only" );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x06, "Easy?" );
		PORT_DIPSETTING(    0x04, "Normal?" );
		PORT_DIPSETTING(    0x02, "Hard?" );
		PORT_DIPSETTING(    0x00, "Hardest?" );
		PORT_DIPNAME( 0x18, 0x10, "Game Mode" );
		PORT_DIPSETTING(    0x18, "Demo Sounds Off" );
		PORT_DIPSETTING(    0x10, "Demo Sounds On" );
		PORT_DIPSETTING(    0x00, "Freeze" );
		PORT_BITX( 0,       0x08, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite Lives", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "Allow Continue" );
		PORT_DIPSETTING(    0x80, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
	INPUT_PORTS_END(); }}; 
	
	public static DriverInitHandlerPtr init_sgladiat  = new DriverInitHandlerPtr() { public void handler(){
		snk_irq_delay = 2000;
	} };
	
	GAMEX( 1984, sgladiat, 0, sgladiat, sgladiat, sgladiat, 0, "SNK", "Gladiator 1984", GAME_NO_COCKTAIL )
}
