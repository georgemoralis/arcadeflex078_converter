/***************************************************************************

Mario Bros memory map (preliminary):

driver by Mirko Buffoni


0000-5fff ROM
6000-6fff RAM
7000-73ff ?
7400-77ff Video RAM
f000-ffff ROM

read:
7c00      IN0
7c80      IN1
7f80      DSW

*
 * IN0 (bits NOT inverted)
 * bit 7 : TEST
 * bit 6 : START 2
 * bit 5 : START 1
 * bit 4 : JUMP player 1
 * bit 3 : ? DOWN player 1 ?
 * bit 2 : ? UP player 1 ?
 * bit 1 : LEFT player 1
 * bit 0 : RIGHT player 1
 *
*
 * IN1 (bits NOT inverted)
 * bit 7 : ?
 * bit 6 : COIN 2
 * bit 5 : COIN 1
 * bit 4 : JUMP player 2
 * bit 3 : ? DOWN player 2 ?
 * bit 2 : ? UP player 2 ?
 * bit 1 : LEFT player 2
 * bit 0 : RIGHT player 2
 *
*
 * DSW (bits NOT inverted)
 * bit 7 : \ difficulty
 * bit 6 : / 00 = easy  01 = medium  10 = hard  11 = hardest
 * bit 5 : \ bonus
 * bit 4 : / 00 = 20000  01 = 30000  10 = 40000  11 = none
 * bit 3 : \ coins per play
 * bit 2 : /
 * bit 1 : \ 00 = 3 lives  01 = 4 lives
 * bit 0 : / 10 = 5 lives  11 = 6 lives
 *

write:
7d00      vertical scroll (pow)
7d80      ?
7e00      sound
7e80-7e82 ?
7e83      sprite palette bank select
7e84      interrupt enable
7e85      ?
7f00-7f07 sound triggers


I/O ports

write:
00        ?

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class mario
{
	
	static int p[8] = { 0,0xf0,0,0,0,0,0,0 };
	static int t[2] = { 0,0 };
	
	
	
	/*
	 *  from sndhrdw/mario.c
	 */
	
	
	#define ACTIVELOW_PORT_BIT(P,A,D)   ((P & (~(1 << A))) | ((D ^ 1) << A))
	#define ACTIVEHIGH_PORT_BIT(P,A,D)   ((P & (~(1 << A))) | (D << A))
	
	
	public static WriteHandlerPtr mario_sh_getcoin_w = new WriteHandlerPtr() {public void handler(int offset, int data)  { t[0] = data; } };
	public static WriteHandlerPtr mario_sh_crab_w = new WriteHandlerPtr() {public void handler(int offset, int data)     { p[1] = ACTIVEHIGH_PORT_BIT(p[1],0,data); } };
	public static WriteHandlerPtr mario_sh_turtle_w = new WriteHandlerPtr() {public void handler(int offset, int data)   { p[1] = ACTIVEHIGH_PORT_BIT(p[1],1,data); } };
	public static WriteHandlerPtr mario_sh_fly_w = new WriteHandlerPtr() {public void handler(int offset, int data)      { p[1] = ACTIVEHIGH_PORT_BIT(p[1],2,data); } };
	public static WriteHandlerPtr mario_sh_tuneselect_w = new WriteHandlerPtr() {public void handler(int offset, int data) soundlatch_w.handler(offset,data); }
	
	public static ReadHandlerPtr mario_sh_p1_r  = new ReadHandlerPtr() { public int handler(int offset) { return p[1]; } };
	public static ReadHandlerPtr mario_sh_p2_r  = new ReadHandlerPtr() { public int handler(int offset) { return p[2]; } };
	public static ReadHandlerPtr mario_sh_t0_r  = new ReadHandlerPtr() { public int handler(int offset) { return t[0]; } };
	public static ReadHandlerPtr mario_sh_t1_r  = new ReadHandlerPtr() { public int handler(int offset) { return t[1]; } };
	public static ReadHandlerPtr mario_sh_tune_r  = new ReadHandlerPtr() { public int handler(int offset) return soundlatch_r(offset); }
	
	public static WriteHandlerPtr mario_sh_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		DAC_data_w(0,data);
	} };
	public static WriteHandlerPtr mario_sh_p1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		p[1] = data;
	} };
	public static WriteHandlerPtr mario_sh_p2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		p[2] = data;
	} };
	public static WriteHandlerPtr masao_sh_irqtrigger_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		static int last;
	
	
		if (last == 1 && data == 0)
		{
			/* setting bit 0 high then low triggers IRQ on the sound CPU */
			cpu_set_irq_line_and_vector(1,0,HOLD_LINE,0xff);
		}
	
		last = data;
	} };
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new Memory_ReadAddress( 0x6000, 0x6fff, MRA_RAM ),
		new Memory_ReadAddress( 0x7400, 0x77ff, MRA_RAM ),	/* video RAM */
		new Memory_ReadAddress( 0x7c00, 0x7c00, input_port_0_r ),	/* IN0 */
		new Memory_ReadAddress( 0x7c80, 0x7c80, input_port_1_r ),	/* IN1 */
		new Memory_ReadAddress( 0x7f80, 0x7f80, input_port_2_r ),	/* DSW */
		new Memory_ReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new Memory_WriteAddress( 0x6000, 0x68ff, MWA_RAM ),
		new Memory_WriteAddress( 0x6a80, 0x6fff, MWA_RAM ),
		new Memory_WriteAddress( 0x6900, 0x6a7f, MWA_RAM, spriteram, spriteram_size ),
		new Memory_WriteAddress( 0x7400, 0x77ff, mario_videoram_w, videoram ),
		new Memory_WriteAddress( 0x7c00, 0x7c00, mario_sh1_w ), /* Mario run sample */
		new Memory_WriteAddress( 0x7c80, 0x7c80, mario_sh2_w ), /* Luigi run sample */
		new Memory_WriteAddress( 0x7d00, 0x7d00, mario_scroll_w ),
		new Memory_WriteAddress( 0x7e80, 0x7e80, mario_gfxbank_w ),
		new Memory_WriteAddress( 0x7e83, 0x7e83, mario_palettebank_w ),
		new Memory_WriteAddress( 0x7e84, 0x7e84, interrupt_enable_w ),
		new Memory_WriteAddress( 0x7f00, 0x7f00, mario_sh_w ),	/* death */
		new Memory_WriteAddress( 0x7f01, 0x7f01, mario_sh_getcoin_w ),
		new Memory_WriteAddress( 0x7f03, 0x7f03, mario_sh_crab_w ),
		new Memory_WriteAddress( 0x7f04, 0x7f04, mario_sh_turtle_w ),
		new Memory_WriteAddress( 0x7f05, 0x7f05, mario_sh_fly_w ),
		new Memory_WriteAddress( 0x7f00, 0x7f07, mario_sh3_w ), /* Misc discrete samples */
		new Memory_WriteAddress( 0x7e00, 0x7e00, mario_sh_tuneselect_w ),
		new Memory_WriteAddress( 0x7000, 0x73ff, MWA_NOP ),	/* ??? */
	//	new Memory_WriteAddress( 0x7e85, 0x7e85, MWA_RAM ),	/* Sets alternative 1 and 0 */
		new Memory_WriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress masao_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new Memory_WriteAddress( 0x6000, 0x68ff, MWA_RAM ),
		new Memory_WriteAddress( 0x6a80, 0x6fff, MWA_RAM ),
		new Memory_WriteAddress( 0x6900, 0x6a7f, MWA_RAM, spriteram, spriteram_size ),
		new Memory_WriteAddress( 0x7400, 0x77ff, mario_videoram_w, videoram ),
		new Memory_WriteAddress( 0x7d00, 0x7d00, mario_scroll_w ),
		new Memory_WriteAddress( 0x7e00, 0x7e00, soundlatch_w ),
		new Memory_WriteAddress( 0x7e80, 0x7e80, mario_gfxbank_w ),
		new Memory_WriteAddress( 0x7e83, 0x7e83, mario_palettebank_w ),
		new Memory_WriteAddress( 0x7e84, 0x7e84, interrupt_enable_w ),
		new Memory_WriteAddress( 0x7000, 0x73ff, MWA_NOP ),	/* ??? */
		new Memory_WriteAddress( 0x7f00, 0x7f00, masao_sh_irqtrigger_w ),
		new Memory_WriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort mario_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00,   0x00,   IOWP_NOP ),  /* unknown... is this a trigger? */
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress readmem_sound[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x0fff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress writemem_sound[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x0fff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	public static IO_ReadPort readport_sound[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00,     0xff,     mario_sh_tune_r ),
		new IO_ReadPort( I8039_p1, I8039_p1, mario_sh_p1_r ),
		new IO_ReadPort( I8039_p2, I8039_p2, mario_sh_p2_r ),
		new IO_ReadPort( I8039_t0, I8039_t0, mario_sh_t0_r ),
		new IO_ReadPort( I8039_t1, I8039_t1, mario_sh_t1_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	public static IO_WritePort writeport_sound[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00,     0xff,     mario_sh_sound_w ),
		new IO_WritePort( I8039_p1, I8039_p1, mario_sh_p1_w ),
		new IO_WritePort( I8039_p2, I8039_p2, mario_sh_p2_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	
	static InputPortPtr input_ports_mario = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( mario )
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BITX(0x80, IP_ACTIVE_HIGH, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_DIPSETTING(    0x03, "6" );
		PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "20000" );
		PORT_DIPSETTING(    0x10, "30000" );
		PORT_DIPSETTING(    0x20, "40000" );
		PORT_DIPSETTING(    0x30, "None" );
		PORT_DIPNAME( 0xc0, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "Easy" );
		PORT_DIPSETTING(    0x40, "Medium" );
		PORT_DIPSETTING(    0x80, "Hard" );
		PORT_DIPSETTING(    0xc0, "Hardest" );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_mariojp = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( mariojp )
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BITX(0x80, IP_ACTIVE_HIGH, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN2 );/* doesn't work in game, but does in service mode */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_DIPSETTING(    0x03, "6" );
		PORT_DIPNAME( 0x1c, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x14, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x1c, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x20, 0x20, "2 Players Game" );
		PORT_DIPSETTING(    0x00, "1 Credit" );
		PORT_DIPSETTING(    0x20, "2 Credits" );
		PORT_DIPNAME( 0xc0, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "20000" );
		PORT_DIPSETTING(    0x40, "30000" );
		PORT_DIPSETTING(    0x80, "40000" );
		PORT_DIPSETTING(    0xc0, "None" );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		512,	/* 512 characters */
		2,	/* 2 bits per pixel */
		new int[] { 512*8*8, 0 },	/* the bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },	/* pretty straightforward layout */
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		256,	/* 256 sprites */
		3,	/* 3 bits per pixel */
		new int[] { 2*256*16*16, 256*16*16, 0 },	/* the bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,		/* the two halves of the sprite are separated */
				256*16*8+0, 256*16*8+1, 256*16*8+2, 256*16*8+3, 256*16*8+4, 256*16*8+5, 256*16*8+6, 256*16*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*8	/* every sprite takes 16 consecutive bytes */
	);
	
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,      0, 16 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 16*4, 32 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static DACinterface dac_interface = new DACinterface
	(
		1,
		new int[] { 100 }
	);
	
	static const char *mario_sample_names[] =
	{
		"*mario",
	
		/* 7f01 - 7f07 sounds */
		"ice.wav",    /* 0x02 ice appears (formerly effect0.wav) */
		"coin.wav",   /* 0x06 coin appears (formerly effect1.wav) */
		"skid.wav",   /* 0x07 skid */
	
		/* 7c00 */
		"run.wav",        /* 03, 02, 01 - 0x1b */
	
		/* 7c80 */
		"luigirun.wav",   /* 03, 02, 01 - 0x1c */
	
	    0	/* end of array */
	};
	
	static Samplesinterface samples_interface = new Samplesinterface
	(
		3,	/* 3 channels */
		25,	/* volume */
		mario_sample_names
	);
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		1,      /* 1 chip */
		14318000/6,	/* ? */
		new int[] { 50 },
		new ReadHandlerPtr[] { soundlatch_r },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	public static Memory_ReadAddress masao_sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x0fff, MRA_ROM ),
		new Memory_ReadAddress( 0x2000, 0x23ff, MRA_RAM ),
		new Memory_ReadAddress( 0x4000, 0x4000, AY8910_read_port_0_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress masao_sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x0fff, MWA_ROM ),
		new Memory_WriteAddress( 0x2000, 0x23ff, MWA_RAM ),
		new Memory_WriteAddress( 0x6000, 0x6000, AY8910_control_port_0_w ),
		new Memory_WriteAddress( 0x4000, 0x4000, AY8910_write_port_0_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	static MACHINE_DRIVER_START( mario )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 3072000)	/* 3.072 MHz (?) */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(0,mario_writeport)
		MDRV_CPU_VBLANK_INT(nmi_line_pulse,1)
	
		MDRV_CPU_ADD(I8039, 730000)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)         /* 730 kHz */
		MDRV_CPU_MEMORY(readmem_sound,writemem_sound)
		MDRV_CPU_PORTS(readport_sound,writeport_sound)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(256)
		MDRV_COLORTABLE_LENGTH(16*4+32*8)
	
		MDRV_PALETTE_INIT(mario)
		MDRV_VIDEO_START(mario)
		MDRV_VIDEO_UPDATE(mario)
	
		/* sound hardware */
		MDRV_SOUND_ADD(DAC, dac_interface)
		MDRV_SOUND_ADD(SAMPLES, samples_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( masao )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 4000000)        /* 4.000 MHz (?) */
		MDRV_CPU_MEMORY(readmem,masao_writemem)
		MDRV_CPU_VBLANK_INT(nmi_line_pulse,1)
	
		MDRV_CPU_ADD(Z80,24576000/16)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)	/* ???? */
		MDRV_CPU_MEMORY(masao_sound_readmem,masao_sound_writemem)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(256)
		MDRV_COLORTABLE_LENGTH(16*4+32*8)
	
		MDRV_PALETTE_INIT(mario)
		MDRV_VIDEO_START(mario)
		MDRV_VIDEO_UPDATE(mario)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
	MACHINE_DRIVER_END
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_mario = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "mario.7f",     0x0000, 0x2000, CRC(c0c6e014) SHA1(36a04f9ca1c2a583477cb8a6f2ef94e044e08296) )
		ROM_LOAD( "mario.7e",     0x2000, 0x2000, CRC(116b3856) SHA1(e372f846d0e5a2b9b47ebd0330293fcc8a12363f) )
		ROM_LOAD( "mario.7d",     0x4000, 0x2000, CRC(dcceb6c1) SHA1(b19804e69ce2c98cf276c6055c3a250316b96b45) )
		ROM_LOAD( "mario.7c",     0xf000, 0x1000, CRC(4a63d96b) SHA1(b09060b2c84ab77cc540a27b8f932cb60ec8d442) )
	
		ROM_REGION( 0x1000, REGION_CPU2, 0 )	/* sound */
		ROM_LOAD( "tma1c-a.6k",   0x0000, 0x1000, CRC(06b9ff85) SHA1(111a29bcb9cda0d935675fa26eca6b099a88427f) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "mario.3f",     0x0000, 0x1000, CRC(28b0c42c) SHA1(46749568aff88a28c3b6a1ac423abd1b90742a4d) )
		ROM_LOAD( "mario.3j",     0x1000, 0x1000, CRC(0c8cc04d) SHA1(15fae47d701dc1ef15c943cee6aa991776ecffdf) )
	
		ROM_REGION( 0x6000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "mario.7m",     0x0000, 0x1000, CRC(22b7372e) SHA1(4a1c1e239cb6d483e76f50d7a3b941025963c6a3) )
		ROM_LOAD( "mario.7n",     0x1000, 0x1000, CRC(4f3a1f47) SHA1(0747d693b9482f6dd28b0bc484fd1d3e29d35654) )
		ROM_LOAD( "mario.7p",     0x2000, 0x1000, CRC(56be6ccd) SHA1(15a6e16c189d45f72761ebcbe9db5001bdecd659) )
		ROM_LOAD( "mario.7s",     0x3000, 0x1000, CRC(56f1d613) SHA1(9af6844dbaa3615433d0595e9e85e72493e31a54) )
		ROM_LOAD( "mario.7t",     0x4000, 0x1000, CRC(641f0008) SHA1(589fe108c7c11278fd897f2ded8f0498bc149cfd) )
		ROM_LOAD( "mario.7u",     0x5000, 0x1000, CRC(7baf5309) SHA1(d9194ff7b89a18273d37b47228fc7fb7e2a0ed1f) )
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 )
		ROM_LOAD( "mario.4p",     0x0000, 0x0200, CRC(afc9bd41) SHA1(90b739c4c7f24a88b6ac5ca29b06c032906a2801) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mariojp = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 ) /* 64k for code */
		ROM_LOAD( "tma1c-a1.7f",  0x0000, 0x2000, CRC(b64b6330) SHA1(f7084251ac325bbfa3fb804da16a50622e1fd213) )
		ROM_LOAD( "tma1c-a2.7e",  0x2000, 0x2000, CRC(290c4977) SHA1(5af266be0ddc883c6548c90e4a9084024a1e91a0) )
		ROM_LOAD( "tma1c-a1.7d",  0x4000, 0x2000, CRC(f8575f31) SHA1(710d0e72fcfce700ed2a22fb9c7c392cc76b250b) )
		ROM_LOAD( "tma1c-a2.7c",  0xf000, 0x1000, CRC(a3c11e9e) SHA1(d0612b0f8c2ea4e798f551922a04a324f4ed5f3d) )
	
		ROM_REGION( 0x1000, REGION_CPU2, 0 )	/* sound */
		ROM_LOAD( "tma1c-a.6k",   0x0000, 0x1000, CRC(06b9ff85) SHA1(111a29bcb9cda0d935675fa26eca6b099a88427f) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "tma1v-a.3f",   0x0000, 0x1000, CRC(adf49ee0) SHA1(11fc2cd197bfe3ecb6af55c3c7a326c94988d2bd) )
		ROM_LOAD( "tma1v-a.3j",   0x1000, 0x1000, CRC(a5318f2d) SHA1(e42f5e51804195c64a56addb18b7ad12c57bb09a) )
	
		ROM_REGION( 0x6000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "tma1v-a.7m",   0x0000, 0x1000, CRC(186762f8) SHA1(711fdd37392656bdd5027e020d51d083ccd7c407) )
		ROM_LOAD( "tma1v-a.7n",   0x1000, 0x1000, CRC(e0e08bba) SHA1(315eba2c10d426c9c0bb4e36987bf8ebed7df9a0) )
		ROM_LOAD( "tma1v-a.7p",   0x2000, 0x1000, CRC(7b27c8c1) SHA1(3fb2613ce19e353fbcc77b6817927794fb35810f) )
		ROM_LOAD( "tma1v-a.7s",   0x3000, 0x1000, CRC(912ba80a) SHA1(351fb5b160216eb10e281815d05a7165ca0e5909) )
		ROM_LOAD( "tma1v-a.7t",   0x4000, 0x1000, CRC(5cbb92a5) SHA1(a78a378e6d3060143dc456e9c33a5068da648331) )
		ROM_LOAD( "tma1v-a.7u",   0x5000, 0x1000, CRC(13afb9ed) SHA1(b29dcd91cf5e639ee50b734afc7a3afce79634df) )
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 )
		ROM_LOAD( "mario.4p",     0x0000, 0x0200, CRC(afc9bd41) SHA1(90b739c4c7f24a88b6ac5ca29b06c032906a2801) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_masao = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 ) /* 64k for code */
		ROM_LOAD( "masao-4.rom",  0x0000, 0x2000, CRC(07a75745) SHA1(acc760242a8862d177e3cff90aa32c4f3dac4e65) )
		ROM_LOAD( "masao-3.rom",  0x2000, 0x2000, CRC(55c629b6) SHA1(1f5b5699821871aadacc511663cb4bd4e357e215) )
		ROM_LOAD( "masao-2.rom",  0x4000, 0x2000, CRC(42e85240) SHA1(bc8cdf867b743c5ee58fcacb63a44f826c8f8c1a) )
		ROM_LOAD( "masao-1.rom",  0xf000, 0x1000, CRC(b2817af9) SHA1(95e83752e544671a68df2107fae1010b187f04a6) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 ) /* 64k for sound */
		ROM_LOAD( "masao-5.rom",  0x0000, 0x1000, CRC(bd437198) SHA1(ebae88461984afc97bbc103fc6d95bc3c1865eec) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "masao-6.rom",  0x0000, 0x1000, CRC(1c9e0be2) SHA1(b4a650412dad90c6f6d79e93cde49055703b7f3e) )
		ROM_LOAD( "masao-7.rom",  0x1000, 0x1000, CRC(747c1349) SHA1(54674f78edf86953b7d500b66393483d1a5ce8ab) )
	
		ROM_REGION( 0x6000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "tma1v-a.7m",   0x0000, 0x1000, CRC(186762f8) SHA1(711fdd37392656bdd5027e020d51d083ccd7c407) )
		ROM_LOAD( "masao-9.rom",  0x1000, 0x1000, CRC(50be3918) SHA1(73e22eee67a03732ff57e523f900f20c6aee0491) )
		ROM_LOAD( "mario.7p",     0x2000, 0x1000, CRC(56be6ccd) SHA1(15a6e16c189d45f72761ebcbe9db5001bdecd659) )
		ROM_LOAD( "tma1v-a.7s",   0x3000, 0x1000, CRC(912ba80a) SHA1(351fb5b160216eb10e281815d05a7165ca0e5909) )
		ROM_LOAD( "tma1v-a.7t",   0x4000, 0x1000, CRC(5cbb92a5) SHA1(a78a378e6d3060143dc456e9c33a5068da648331) )
		ROM_LOAD( "tma1v-a.7u",   0x5000, 0x1000, CRC(13afb9ed) SHA1(b29dcd91cf5e639ee50b734afc7a3afce79634df) )
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 )
		ROM_LOAD( "mario.4p",     0x0000, 0x0200, CRC(afc9bd41) SHA1(90b739c4c7f24a88b6ac5ca29b06c032906a2801) )
	ROM_END(); }}; 
	
	
	
	GAME( 1983, mario,   0,     mario, mario,   0, ROT180, "Nintendo of America", "Mario Bros. (US)" )
	GAME( 1983, mariojp, mario, mario, mariojp, 0, ROT180, "Nintendo", "Mario Bros. (Japan)" )
	GAME( 1983, masao,   mario, masao, mario,   0, ROT180, "bootleg", "Masao" )
}
