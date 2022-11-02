/***************************************************************************

	Videa Gridlee hardware

    driver by Aaron Giles

    special thanks to Howard Delman, Roger Hector and Ed Rotberg for
    allowing distribution of the Gridlee ROMs

	Based on the Bally/Sente SAC system

	Games supported:
		* Gridlee

	Known bugs:
		* analog sound hardware is unemulated

****************************************************************************

	Memory map

****************************************************************************

	========================================================================
	CPU #1
	========================================================================
	0000-007F   R/W   xxxxxxxx    Sprite RAM (32 entries x 4 bytes)
	            R/W   xxxxxxxx       (0: image number)
	            R/W   --------       (1: unused?)
	            R/W   xxxxxxxx       (2: Y position, offset by 17 pixels)
	            R/W   xxxxxxxx       (3: X position)
	0080-07FF   R/W   xxxxxxxx    Program RAM
	0800-7FFF   R/W   xxxxxxxx    Video RAM (256x240 pixels)
	            R/W   xxxx----       (left pixel)
	            R/W   ----xxxx       (right pixel)
	9000          W   -------x    Player 1 LED
	9010          W   -------x    Player 2 LED
	9020          W   -------x    Coin counter
	9060          W   -------x    Unknown (written once at startup)
	9070          W   -------x    Cocktail flip
    9200          W   --xxxxxx    Palette base select
    9380          W   --------    Watchdog reset
    9500        R     ---xxxxx    Trackball Y position
                R     ---x----    Sign of delta
                R     ----xxxx    Cumulative magnitude
    9501        R     ---xxxxx    Trackball X position
                R     ---x----    Sign of delta
                R     ----xxxx    Cumulative magnitude
    9502        R     ------x-    Fire button 2
                R     -------x    Fire button 1
    9503        R     --xx----    Coinage switches
                R     ----x---    2 player start
                R     -----x--    1 player start
                R     ------x-    Right coin
                R     -------x    Left coin
    9600        R     x-------    Reset game data switch
                R     -x------    Reset hall of fame switch
                R     --x-----    Cocktail/upright switch
                R     ---x----    Free play switch
                R     ----xx--    Lives switches
                R     ------xx    Bonus lives switches
    9700        R     x-------    VBLANK
                R     -x------    Service advance
                R     --x-----    Service switch
    9820        R     xxxxxxxx    Random number generator
    9828-982C     W   ????????    Unknown
    9830-983F     W   ????????    Unknown (sound-related)
    9C00-9CFF   R/W   --------    NVRAM
	A000-FFFF   R     xxxxxxxx    Fixed program ROM
	========================================================================
	Interrupts:
		NMI not connected
		IRQ generated by 32L
		FIRQ generated by ??? (but should be around scanline 92)
	========================================================================

***************************************************************************/


/*
 * ported to v0.78
 * using automatic conversion tool v0.02
 */ 
package arcadeflex.v078.drivers;

public class gridlee
{
	
	
	/* constants */
	#define FIRQ_SCANLINE 92
	
	
	/* local variables */
	static UINT8 last_analog_input[2];
	static UINT8 last_analog_output[2];
	
	/* random number generator states */
	static UINT8 *poly17 = NULL;
	static UINT8 *rand17 = NULL;
	
	
	
	/* local prototypes */
	static void poly17_init(void);
	
	
	/*************************************
	 *
	 *	Interrupt handling
	 *
	 *************************************/
	
	static void irq_off(int param)
	{
		cpu_set_irq_line(0, M6809_IRQ_LINE, CLEAR_LINE);
	}
	
	
	static void irq_timer(int param)
	{
		/* next interrupt after scanline 256 is scanline 64 */
		if (param == 256)
			timer_set(cpu_getscanlinetime(64), 64, irq_timer);
		else
			timer_set(cpu_getscanlinetime(param + 64), param + 64, irq_timer);
	
		/* IRQ starts on scanline 0, 64, 128, etc. */
		cpu_set_irq_line(0, M6809_IRQ_LINE, ASSERT_LINE);
	
		/* it will turn off on the next HBLANK */
		timer_set(cpu_getscanlineperiod() * 0.9, 0, irq_off);
	}
	
	
	static void firq_off(int param)
	{
		cpu_set_irq_line(0, M6809_FIRQ_LINE, CLEAR_LINE);
	}
	
	
	static void firq_timer(int param)
	{
		/* same time next frame */
		timer_set(cpu_getscanlinetime(FIRQ_SCANLINE), 0, firq_timer);
	
		/* IRQ starts on scanline FIRQ_SCANLINE? */
		cpu_set_irq_line(0, M6809_FIRQ_LINE, ASSERT_LINE);
	
		/* it will turn off on the next HBLANK */
		timer_set(cpu_getscanlineperiod() * 0.9, 0, firq_off);
	}
	
	
	static MACHINE_INIT( gridlee )
	{
		/* start timers to generate interrupts */
		timer_set(cpu_getscanlinetime(0), 0, irq_timer);
		timer_set(cpu_getscanlinetime(FIRQ_SCANLINE), 0, firq_timer);
	
		/* create the polynomial tables */
		poly17_init();
	}
	
	
	
	/*************************************
	 *
	 *	ADC handlers
	 *
	 *************************************/
	
	public static ReadHandlerPtr analog_port_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int delta, sign, magnitude;
		UINT8 newval;
	
		/* first read the new trackball value and compute the signed delta */
		newval = readinputport(offset + 2 * gridlee_cocktail_flip);
		delta = (int)newval - (int)last_analog_input[offset];
	
		/* handle the case where we wrap around from 0x00 to 0xff, or vice versa */
		if (delta >= 0x80)
			delta -= 0x100;
		if (delta <= -0x80)
			delta += 0x100;
	
		/* just return the previous value for deltas less than 2, which are ignored */
		if (delta >= -1 && delta <= 1)
			return last_analog_output[offset];
		last_analog_input[offset] = newval;
	
		/* compute the sign and the magnitude */
		sign = (delta < 0) ? 0x10 : 0x00;
		magnitude = (delta < 0) ? -delta : delta;
	
		/* add the magnitude to the running total */
		last_analog_output[offset] += magnitude;
	
		/* or in the sign bit and return that */
		return (last_analog_output[offset] & 15) | sign;
	} };
	
	
	
	/*************************************
	 *
	 *	MM5837 noise generator
	 *
	 *	NOTE: this is stolen straight from
	 *			POKEY.c
	 *
	 *	NOTE: this is assumed to be the
	 *			same as balsente.c
	 *
	 *************************************/
	
	#define POLY17_BITS 17
	#define POLY17_SIZE ((1 << POLY17_BITS) - 1)
	#define POLY17_SHL	7
	#define POLY17_SHR	10
	#define POLY17_ADD	0x18000
	
	static void poly17_init(void)
	{
		UINT32 i, x = 0;
		UINT8 *p, *r;
	
		/* allocate memory */
		p = poly17 = auto_malloc(2 * (POLY17_SIZE + 1));
		if (poly17 == 0)
			return;
		r = rand17 = poly17 + POLY17_SIZE + 1;
	
		/* generate the polynomial */
		for (i = 0; i < POLY17_SIZE; i++)
		{
	        /* store new values */
			*p++ = x & 1;
			*r++ = x >> 3;
	
	        /* calculate next bit */
			x = ((x << POLY17_SHL) + (x >> POLY17_SHR) + POLY17_ADD) & POLY17_SIZE;
		}
	}
	
	
	
	/*************************************
	 *
	 *	Hardware random numbers
	 *
	 *************************************/
	
	public static ReadHandlerPtr random_num_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		unsigned int cc;
	
		/* CPU runs at 1.25MHz, noise source at 100kHz -. multiply by 12.5 */
		cc = activecpu_gettotalcycles();
	
		/* 12.5 = 8 + 4 + 0.5 */
		cc = (cc << 3) + (cc << 2) + (cc >> 1);
		return rand17[cc & POLY17_SIZE];
	} };
	
	
	
	/*************************************
	 *
	 *	Misc handlers
	 *
	 *************************************/
	
	public static WriteHandlerPtr led_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		set_led_status(0, data & 1);
		logerror("LED 0 %s\n", (data & 1) ? "on" : "off");
	} };
	
	
	public static WriteHandlerPtr led_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		set_led_status(1, data & 1);
		logerror("LED 1 %s\n", (data & 1) ? "on" : "off");
	} };
	
	
	public static WriteHandlerPtr gridlee_coin_counter_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		coin_counter_w(0, data & 1);
		logerror("coin counter %s\n", (data & 1) ? "on" : "off");
	} };
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	/* CPU 1 read addresses */
	public static Memory_ReadAddress readmem_cpu1[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_RAM ),
		new Memory_ReadAddress( 0x9500, 0x9501, analog_port_r ),
		new Memory_ReadAddress( 0x9502, 0x9502, input_port_4_r ),
		new Memory_ReadAddress( 0x9503, 0x9503, input_port_5_r ),
		new Memory_ReadAddress( 0x9600, 0x9600, input_port_6_r ),
		new Memory_ReadAddress( 0x9700, 0x9700, input_port_7_r ),
		new Memory_ReadAddress( 0x9820, 0x9820, random_num_r ),
		new Memory_ReadAddress( 0x9c00, 0x9cff, MRA_RAM ),
		new Memory_ReadAddress( 0xa000, 0xffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress writemem_cpu1[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x07ff, MWA_RAM, spriteram ),
		new Memory_WriteAddress( 0x0800, 0x7fff, gridlee_videoram_w, videoram, videoram_size ),
		new Memory_WriteAddress( 0x9000, 0x9000, led_0_w ),
		new Memory_WriteAddress( 0x9010, 0x9010, led_1_w ),
		new Memory_WriteAddress( 0x9020, 0x9020, gridlee_coin_counter_w ),
	/*	new Memory_WriteAddress( 0x9060, 0x9060, unknown - only written to at startup */
		{ 0x9070, 0x9070, gridlee_cocktail_flip_w },
		{ 0x9200, 0x9200, gridlee_palette_select_w },
		{ 0x9380, 0x9380, watchdog_reset_w },
		{ 0x9700, 0x9700, MWA_NOP },
		{ 0x9828, 0x993f, gridlee_sound_w },
		{ 0x9c00, 0x9cff, MWA_RAM, generic_nvram, generic_nvram_size },
		{ 0xa000, 0xffff, MWA_ROM },
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	static InputPortPtr input_ports_gridlee = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* 9500 (fake) */
	    PORT_ANALOG( 0xff, 0, IPT_TRACKBALL_Y, 20, 8, 0x00, 0xff );
	
		PORT_START(); 	/* 9501 (fake) */
	    PORT_ANALOG( 0xff, 0, IPT_TRACKBALL_X | IPF_REVERSE, 20, 8, 0x00, 0xff );
	
		PORT_START(); 	/* 9500 (fake) */
	    PORT_ANALOG( 0xff, 0, IPT_TRACKBALL_Y | IPF_COCKTAIL, 20, 8, 0x00, 0xff );
	
		PORT_START(); 	/* 9501 (fake) */
	    PORT_ANALOG( 0xff, 0, IPT_TRACKBALL_X | IPF_REVERSE | IPF_COCKTAIL, 20, 8, 0x00, 0xff );
	
		PORT_START(); 	/* 9502 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0xfc, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* 9503 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2 );
		PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_2C") );
		PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* 9600 */
		PORT_DIPNAME( 0x03, 0x01, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "8000 points" );
		PORT_DIPSETTING(    0x01, "10000 points" );
		PORT_DIPSETTING(    0x02, "12000 points" );
		PORT_DIPSETTING(    0x03, "14000 points" );
		PORT_DIPNAME( 0x0c, 0x04, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_DIPSETTING(    0x04, "3" );
		PORT_DIPSETTING(    0x08, "4" );
		PORT_DIPSETTING(    0x0c, "5" );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Cocktail") );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x40, 0x00, "Reset Hall of Fame" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x00, "Reset Game Data" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Yes") );
	
		PORT_START(); 	/* 9700 */
		PORT_BIT( 0x1f, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_SERVICE( 0x20, IP_ACTIVE_LOW );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK );
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Sound definitions
	 *
	 *************************************/
	
	static struct CustomSound_interface custom_interface =
	{
		gridlee_sh_start
	};
	
	
	static const char *sample_names[] =
	{
		"*gridlee",
		"bounce1.wav",
		"bounce2.wav",
		0	/* end of array */
	};
	
	static struct Samplesinterface samples_interface =
	{
		8,	/* 8 channels */
		40, /* volume */
		sample_names
	};
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MACHINE_DRIVER_START( gridlee )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M6809, 5000000/4)
		MDRV_CPU_MEMORY(readmem_cpu1,writemem_cpu1)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
	
		MDRV_MACHINE_INIT(gridlee)
		MDRV_NVRAM_HANDLER(generic_0fill)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_UPDATE_BEFORE_VBLANK)
		MDRV_SCREEN_SIZE(256, 240)
		MDRV_VISIBLE_AREA(0, 255, 0, 239)
		MDRV_PALETTE_LENGTH(2048)
	
		MDRV_PALETTE_INIT(gridlee)
		MDRV_VIDEO_START(gridlee)
		MDRV_VIDEO_UPDATE(gridlee)
	
		/* sound hardware */
		MDRV_SOUND_ADD(CUSTOM,  custom_interface)
		MDRV_SOUND_ADD(SAMPLES, samples_interface)
	MACHINE_DRIVER_END
	
	
	
	/*************************************
	 *
	 *	ROM definitions
	 *
	 *************************************/
	
	static RomLoadPtr rom_gridlee = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )
		ROM_LOAD( "gridfnla.bin", 0xa000, 0x1000, CRC(1c43539e) SHA1(8b4a6f5c2c22bb021937157606d2129e2b01f718) )
		ROM_LOAD( "gridfnlb.bin", 0xb000, 0x1000, CRC(c48b91b8) SHA1(651210470ddf7c14f16f6c3046a9b8e903824ab8) )
		ROM_LOAD( "gridfnlc.bin", 0xc000, 0x1000, CRC(6ad436dd) SHA1(f393b63077f249d34a8e85649aea58b27a0425b1) )
		ROM_LOAD( "gridfnld.bin", 0xd000, 0x1000, CRC(f7188ddb) SHA1(eeb3f7dd8c61689cdd9992280ee1b3b5dc79a54c) )
		ROM_LOAD( "gridfnle.bin", 0xe000, 0x1000, CRC(d5330bee) SHA1(802bb5705d4cd22d556c1bcbcf730d688ca8e8ab) )
		ROM_LOAD( "gridfnlf.bin", 0xf000, 0x1000, CRC(695d16a3) SHA1(53d22cbedbedad8c89a964b6a38b7075c43cf03b) )
	
		ROM_REGION( 0x4000, REGION_GFX1, 0 )
		ROM_LOAD( "gridpix0.bin", 0x0000, 0x1000, CRC(e6ea15ae) SHA1(2c482e25ea44aafd63ca5533b5a2e2dd8bf89365) )
		ROM_LOAD( "gridpix1.bin", 0x1000, 0x1000, CRC(d722f459) SHA1(8cad028eefbba387bdd57fb8bb3a855ae314fb32) )
		ROM_LOAD( "gridpix2.bin", 0x2000, 0x1000, CRC(1e99143c) SHA1(89c2f772cd15f2c37c8167a03dc4c7d1c923e4c3) )
		ROM_LOAD( "gridpix3.bin", 0x3000, 0x1000, CRC(274342a0) SHA1(818cfd4132183d922ff4585c73f2cd6e4546c75b) )
	
		ROM_REGION( 0x1800, REGION_PROMS, 0 )
		ROM_LOAD( "grdrprom.bin", 0x0000, 0x800, CRC(f28f87ed) SHA1(736f38c3ec5455de1266aad348ba708d7201b21a) )
		ROM_LOAD( "grdgprom.bin", 0x0800, 0x800, CRC(921b0328) SHA1(59d1a3d3a90bd680a75adca5dd1b4682236c673b) )
		ROM_LOAD( "grdbprom.bin", 0x1000, 0x800, CRC(04350348) SHA1(098fec3073143e0b8746e728d7d321f2a353286f) )
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Game drivers
	 *
	 *************************************/
	
	GAMEX( 1983, gridlee, 0,        gridlee, gridlee, 0,     ROT0, "Videa", "Gridlee", GAME_IMPERFECT_SOUND )
}
