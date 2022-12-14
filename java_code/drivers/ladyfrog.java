/*
Lady Frog (c) 1990 Mondial Games
(there's  "(c) Alfa Tecnology" in the ROM)
driver by Tomasz Slanina

'N.Y. Captor' (TAITO) hardware , without sub cpu.

Sound rom is 'borrowed' from NYC.
1.115 = a80_16.i26 + a80_17.i25

PCB Layout
|-------------------------------------------------|
|18MHz                          1       M5232     |
|                                          LM3900 |
|                               6116    Z80-2     |
|                     6116                   8MHz |
|                         8MHz        N5C090-60   |
|           2148                      AY-3-8910   |
|           2148                                  |
|                                         LM3900  |
|                                                 |
|                             Z80-1               |
|                 2148         2                  |
|                 2148        6264                |
|6      3         2148                           J|
|7      4                                        A|
|8      5                                        M|
|                                                M|
|           2148                   DSWB  DSWA    A|
|           2148                                  |
|           2148                                  |
|-------------------------------------------------|

Notes:
      Z80-1 clock: 4.000MHz
      Z80-2 clock: 4.000MHz
      AY-3-8910 clock: 2.000MHz
      OKI M5232 clock: 2.000MHz
      VSync: 60Hz
      HSync: 15.68kHz

      N5C090-60: iNTEL simple PLD (PLCC44), 100% compatible with Altera EP900

*/

/* set to 1 for real screen size - two more tile columns on right side = black(title)/garbage(game) */
#define ladyfrog_scr_size 0

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class ladyfrog
{
	
	
	static int sound_nmi_enable=0,pending_nmi=0;
	static int snd_flag;
	static UINT8 snd_data;
	
	
	
	public static ReadHandlerPtr from_snd_r  = new ReadHandlerPtr() { public int handler(int offset){
		snd_flag=0;
		return snd_data;
	} };
	
	public static WriteHandlerPtr to_main_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		snd_data = data;
		snd_flag = 2;
	
	} };
	
	public static WriteHandlerPtr sound_cpu_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		cpu_set_reset_line(1, (data&1 )? ASSERT_LINE : CLEAR_LINE);
	} };
	
	static void nmi_callback(int param)
	{
		if (sound_nmi_enable) cpu_set_irq_line(1,IRQ_LINE_NMI,PULSE_LINE);
		else pending_nmi = 1;
	}
	
	public static WriteHandlerPtr sound_command_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		soundlatch_w.handler(0,data);
		timer_set(TIME_NOW,data,nmi_callback);
	} };
	
	public static WriteHandlerPtr nmi_disable_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		sound_nmi_enable = 0;
	} };
	
	public static WriteHandlerPtr nmi_enable_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		sound_nmi_enable = 1;
		if (pending_nmi)
		{
			cpu_set_irq_line(1,IRQ_LINE_NMI,PULSE_LINE);
			pending_nmi = 0;
		}
	} };
	
	public static WriteHandlerPtr unk_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	
	} };
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		1,
		8000000/4,
		new int[] { 15 },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { unk_w },
		new WriteHandlerPtr[] { unk_w }
	);
	
	static struct MSM5232interface msm5232_interface =
	{
		1,
		2000000,
		{ { 0.65e-6, 0.65e-6, 0.65e-6, 0.65e-6, 0.65e-6, 0.65e-6, 0.65e-6, 0.65e-6 } },
		{ 100 }
	};
	
	public static ReadHandlerPtr snd_flag_r  = new ReadHandlerPtr() { public int handler(int offset){
		return snd_flag | 0xfd;
	} };
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xc07f, MRA_RAM ),
		new Memory_ReadAddress( 0xc080, 0xc87f, ladyfrog_videoram_r ),
		new Memory_ReadAddress( 0xd400, 0xd400, from_snd_r ),
		new Memory_ReadAddress( 0xd401, 0xd401, snd_flag_r ),
		new Memory_ReadAddress( 0xd800, 0xd800, input_port_0_r ),
		new Memory_ReadAddress( 0xd801, 0xd801, input_port_1_r ),
		new Memory_ReadAddress( 0xd804, 0xd804, input_port_2_r ),
		new Memory_ReadAddress( 0xd806, 0xd806, input_port_3_r ),
		new Memory_ReadAddress( 0xdc00, 0xdc9f, ladyfrog_spriteram_r),
		new Memory_ReadAddress( 0xdca0, 0xdcbf, ladyfrog_scrlram_r ),
		new Memory_ReadAddress( 0xdcc0, 0xdcff, MRA_RAM ),
		new Memory_ReadAddress( 0xdd00, 0xdeff, ladyfrog_palette_r ),
		new Memory_ReadAddress( 0xd0d0, 0xd0d0, MRA_NOP ), /* code jumps to ASCII text "Alfa tecnology"  @ $b7 */
		new Memory_ReadAddress( 0xe000, 0xffff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xc07f, MWA_RAM ),
		new Memory_WriteAddress( 0xc080, 0xc87f, ladyfrog_videoram_w, videoram, videoram_size ),
		new Memory_WriteAddress( 0xd000, 0xd000, ladyfrog_gfxctrl2_w),
		new Memory_WriteAddress( 0xd400, 0xd400, sound_command_w ),
		new Memory_WriteAddress( 0xd403, 0xd403, sound_cpu_reset_w ),
		new Memory_WriteAddress( 0xdc00, 0xdc9f, ladyfrog_spriteram_w ),
		new Memory_WriteAddress( 0xdca0, 0xdcbf, ladyfrog_scrlram_w, ladyfrog_scrlram ),
		new Memory_WriteAddress( 0xdcc0, 0xdcff, MWA_RAM ),
		new Memory_WriteAddress( 0xdd00, 0xdeff, ladyfrog_palette_w ),
		new Memory_WriteAddress( 0xdf03, 0xdf03, ladyfrog_gfxctrl_w ),
		new Memory_WriteAddress( 0xe000, 0xffff, MWA_RAM),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_ReadAddress sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xc7ff, MRA_RAM ),
		new Memory_ReadAddress( 0xd000, 0xd000, soundlatch_r ),
		new Memory_ReadAddress( 0xd200, 0xd200, MRA_NOP ),
		new Memory_ReadAddress( 0xe000, 0xefff, MRA_NOP ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xc7ff, MWA_RAM ),
		new Memory_WriteAddress( 0xc800,0xc801, MWA_NOP ),
		new Memory_WriteAddress( 0xc802, 0xc802, AY8910_control_port_0_w ),
		new Memory_WriteAddress( 0xc803, 0xc803, AY8910_write_port_0_w ),
		new Memory_WriteAddress( 0xc900, 0xc90d, MSM5232_0_w ),
		new Memory_WriteAddress( 0xca00, 0xca00, MWA_NOP),
		new Memory_WriteAddress( 0xcb00, 0xcb00, MWA_NOP),
		new Memory_WriteAddress( 0xcc00, 0xcc00, MWA_NOP),
		new Memory_WriteAddress( 0xd000, 0xd000, to_main_w ),
		new Memory_WriteAddress( 0xd200, 0xd200, nmi_enable_w ),
		new Memory_WriteAddress( 0xd400, 0xd400, nmi_disable_w ),
		new Memory_WriteAddress( 0xd600, 0xd600, MWA_NOP),
		new Memory_WriteAddress( 0xe000, 0xefff, MWA_NOP ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	static InputPortPtr input_ports_ladyfrog = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( ladyfrog )
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_DIPSETTING(    0x01, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x03, "1" );
		PORT_DIPNAME( 0x04, 0x00, "Clear 'doors' after life lost" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "Allow Continue" );
		PORT_DIPSETTING(    0x20, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_3C") );
	
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x03, "Easy" );
		PORT_DIPSETTING(    0x02, "Normal" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT   | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT  | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP 	   | IPF_4WAY );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN   | IPF_4WAY );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 );
	
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,2),
		4,
		new int[] { RGN_FRAC(1,2)+0, RGN_FRAC(1,2)+4, 0, 4 },
		new int[] { 3, 2, 1, 0, 8+3, 8+2, 8+1, 8+0 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,2),
		4,
		new int[] { RGN_FRAC(1,2)+0, RGN_FRAC(1,2)+4, 0, 4 },
		new int[] { 3, 2, 1, 0, 8+3, 8+2, 8+1, 8+0,
				16*8+3, 16*8+2, 16*8+1, 16*8+0, 16*8+8+3, 16*8+8+2, 16*8+8+1, 16*8+8+0 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				16*16, 17*16, 18*16, 19*16, 20*16, 21*16, 22*16, 23*16 },
		64*8
	);
	
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,     0, 16 ),
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout, 256, 16 ),
		new GfxDecodeInfo( -1 )
	};
	
	static MACHINE_DRIVER_START( ladyfrog )
		MDRV_CPU_ADD(Z80,8000000/2)
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_CPU_ADD(Z80,8000000/2)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)
		MDRV_CPU_MEMORY(sound_readmem,sound_writemem)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,2)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
		MDRV_INTERLEAVE(100)
	
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
	#if ladyfrog_scr_size
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 29*8-1)
	#else
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 27*8-1)
	#endif
	
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(512)
	
		MDRV_VIDEO_START(ladyfrog)
		MDRV_VIDEO_UPDATE(ladyfrog)
	
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
		MDRV_SOUND_ADD(MSM5232, msm5232_interface)
	MACHINE_DRIVER_END
	
	
	static RomLoadPtr rom_ladyfrog = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )
		ROM_LOAD( "2.107",   0x0000, 0x10000, CRC(fa4466e6) SHA1(08e5cc8e1d3c845bc9c253267f2683671bffa9f2) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )
		ROM_LOAD( "1.115",   0x0000, 0x8000, CRC(b0932498) SHA1(13d90698f2682e64ff3597c9267ca9d33a6d62ba) ) /* NY Captor*/
	
		ROM_REGION( 0x60000, REGION_GFX1, ROMREGION_DISPOSE |ROMREGION_INVERT )
		ROM_LOAD( "3.32",   0x30000, 0x10000, CRC(8a27fc0a) SHA1(36e0365776e61ef830451e6351eca6b6c742086f) )
		ROM_LOAD( "4.33",   0x40000, 0x10000, CRC(e1a137d3) SHA1(add8140a9366a0d343b611ced10c804d3fb04c03) )
		ROM_LOAD( "5.34",   0x50000, 0x10000, CRC(7816925f) SHA1(037a69243b35e1739e5d7288e279d0d4289c61ed) )
		ROM_LOAD( "6.8",    0x00000, 0x10000, CRC(61b3baaa) SHA1(d65a235dbbb96c11e8307aa457d1c06f20eb8d5a) )
		ROM_LOAD( "7.9",    0x10000, 0x10000, CRC(88aaff58) SHA1(dfb143ef452dec530adf8b35a50a82d08f47d107) )
		ROM_LOAD( "8.10",   0x20000, 0x10000, CRC(8c73baa1) SHA1(50fb408be181ef3c125dee23b04daeb010c9f276) )
	ROM_END(); }}; 
	
	GAME(1990, ladyfrog, 0,       ladyfrog,  ladyfrog, 0, ORIENTATION_SWAP_XY, "Mondial Games", "Lady Frog")
}
