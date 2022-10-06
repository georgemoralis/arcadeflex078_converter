/***************************************************************************
Big Event Golf (c) Taito 1986

driver by Jaroslaw Burczynski
          Tomasz Slanina


****************************************************************************


Taito 1986  M4300056B

K1100156A Sound
J1100068A
                                YM2149     2016
                                MSM5232    A67-16
                                Z80        A67-17
                                           A67-18
                                8MHz
-----------------------------------------------------
K1100215A CPU
J1100066A

2064
A67-21
A67-20      A67-03
Z80         A67-04
            A67-05
            A67-06                          2016
            A67-07
            A67-08
            A67-09
            A67-10-2          2016
                              A67-11
            10MHz             Z80
                                   A67-19-1 (68705P5)
----------------------------------------------------
K1100215A VIDEO
J1100072A

                                 41464            2148
  93422                          41464            2148
  93422                          41464            2148
  93422                          41464
  93422                          41464
                                 41464
                                 41464
                                 41464


       A67-12-1                2016
       A67-13-1
       A67-14-1     2016
       A67-15-1                      18.432MHz

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class bigevglf
{
	
	
	
	
	
	
	
	static UINT32 beg_bank=0;
	UINT8 *beg_sharedram;
	
	static int sound_nmi_enable=0,pending_nmi=0;
	static UINT8 for_sound = 0;
	static UINT8 from_sound = 0;
	static UINT8 sound_state = 0;
	
	public static WriteHandlerPtr beg_banking_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		beg_bank = data;
	
	/* d0-d3 connect to A11-A14 of the ROMs (via ls273 latch)
	   d4-d7 select one of ROMs (via ls273(above) and then ls154)
	*/
		cpu_setbank(1, memory_region(REGION_CPU1) + 0x10000 + 0x800*(beg_bank&0xff)); /* empty sockets for IC37-IC44 ROMS */
	} };
	
	static void from_sound_latch_callback(int param)
	{
		from_sound = param&0xff;
		sound_state |= 2;
	}
	static WRITE_HANDLER(beg_fromsound_w)	/* write to D800 sets bit 1 in status */
	{
		timer_set(TIME_NOW, (activecpu_get_pc()<<16)|data, from_sound_latch_callback);
	}
	
	static READ_HANDLER(beg_fromsound_r)
	{
		/* set a timer to force synchronization after the read */
		timer_set(TIME_NOW, 0, NULL);
		return from_sound;
	}
	
	static READ_HANDLER(beg_soundstate_r)
	{
		UINT8 ret = sound_state;
		/* set a timer to force synchronization after the read */
		timer_set(TIME_NOW, 0, NULL);
		sound_state &= ~2; /* read from port 21 clears bit 1 in status */
		return ret;
	}
	
	static READ_HANDLER(soundstate_r)
	{
		/* set a timer to force synchronization after the read */
		timer_set(TIME_NOW, 0, NULL);
		return sound_state;
	}
	
	static void nmi_callback(int param)
	{
		if (sound_nmi_enable) cpu_set_irq_line(2,IRQ_LINE_NMI,PULSE_LINE);
		else pending_nmi = 1;
		sound_state &= ~1;
	}
	public static WriteHandlerPtr sound_command_w = new WriteHandlerPtr() {public void handler(int offset, int data)	/* write to port 20 clears bit 0 in status */
	{
		for_sound = data;
		timer_set(TIME_NOW,data,nmi_callback);
	} };
	
	public static ReadHandlerPtr sound_command_r  = new ReadHandlerPtr() { public int handler(int offset)	/* read from D800 sets bit 0 in status */
	{
		sound_state |= 1;
		return for_sound;
	} };
	
	public static WriteHandlerPtr nmi_disable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		sound_nmi_enable = 0;
	} };
	
	public static WriteHandlerPtr nmi_enable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		sound_nmi_enable = 1;
		if (pending_nmi)
		{
			cpu_set_irq_line(2,IRQ_LINE_NMI,PULSE_LINE);
			pending_nmi = 0;
		}
	} };
	
	static UINT8 beg13_ls74[2];
	
	static void deferred_ls74_w( int param )
	{
		int offs = (param>>8) & 255;
		int data = param & 255;
		beg13_ls74[offs] = data;
	}
	
	/* do this on a timer to let the CPUs synchronize */
	static WRITE_HANDLER (beg13A_clr_w)
	{
		timer_set(TIME_NOW, (0<<8) | 0, deferred_ls74_w);
	}
	static WRITE_HANDLER (beg13B_clr_w)
	{
		timer_set(TIME_NOW, (1<<8) | 0, deferred_ls74_w);
	}
	static WRITE_HANDLER (beg13A_set_w)
	{
		timer_set(TIME_NOW, (0<<8) | 1, deferred_ls74_w);
	}
	static WRITE_HANDLER (beg13B_set_w)
	{
		timer_set(TIME_NOW, (1<<8) | 1, deferred_ls74_w);
	}
	
	public static ReadHandlerPtr beg_status_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	/* d0 = Q of 74ls74 IC13(partA)
	   d1 = Q of 74ls74 IC13(partB)
	   d2 =
	   d3 =
	   d4 =
	   d5 =
	   d6 = d7 = 10MHz/2
	
	*/
		/* set a timer to force synchronization after the read */
		timer_set(TIME_NOW, 0, NULL);
		return (beg13_ls74[0]<<0) | (beg13_ls74[1]<<1);
	} };
	
	
	public static ReadHandlerPtr beg_sharedram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return beg_sharedram[offset];
	} };
	public static WriteHandlerPtr beg_sharedram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		beg_sharedram[offset] = data;
	} };
	
	INPUT_PORTS_START( bigevglf )
	
		PORT_START	/* port 00 on sub cpu */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 )
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN4 )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 )
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 )
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 )
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START1 )
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_TILT )
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_SERVICE1 )
	
		PORT_START	/* port 04 on sub cpu - bit 0 and bit 1 are coin inputs */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 )
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 )
	
		PORT_START	/* port 05 on sub cpu */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
		PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
		PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( Flip_Screen ) )
		PORT_DIPSETTING(    0x02, DEF_STR( Off ))
		PORT_DIPSETTING(    0x00, DEF_STR( On ))
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW )
	 	PORT_DIPNAME( 0x08, 0x08, DEF_STR( Demo_Sounds ) )
	 	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
		PORT_DIPSETTING(    0x08, DEF_STR( On ) )
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( Coin_A ) )
		PORT_DIPSETTING(    0x50, DEF_STR( 2C_1C ) )
		PORT_DIPSETTING(    0xf0, DEF_STR( 1C_1C ) )
		PORT_DIPSETTING(    0x00, DEF_STR( 2C_3C ) )
		PORT_DIPSETTING(    0xa0, DEF_STR( 1C_2C ) )
	
		PORT_START	/* port 06 on sub cpu */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( Difficulty ) )
		PORT_DIPSETTING(    0x01, "Easy" )
		PORT_DIPSETTING(    0x03, "Normal" )
		PORT_DIPSETTING(    0x02, "Hard" )
		PORT_DIPSETTING(    0x00, "Difficult" )
		PORT_DIPNAME( 0x0c, 0x0c, "Holes" )
		PORT_DIPSETTING(    0x0c, "3" )
	 	PORT_DIPSETTING(    0x08, "2" )
	 	PORT_DIPSETTING(    0x04, "1" )
	 	PORT_DIPSETTING(    0x00, "4" )
		PORT_DIPNAME( 0x10, 0x00, "Title" )
		PORT_DIPSETTING(    0x00, "English" )
	 	PORT_DIPSETTING(    0x10, "Japanese" )
	 	PORT_DIPNAME( 0xe0, 0xa0, "Full game price (credits)" )
		PORT_DIPSETTING(    0xe0, "3" )
		PORT_DIPSETTING(    0xc0, "4" )
		PORT_DIPSETTING(    0xa0, "5" )
		PORT_DIPSETTING(    0x80, "6" )
		PORT_DIPSETTING(    0x60, "7" )
		PORT_DIPSETTING(    0x40, "8" )
		PORT_DIPSETTING(    0x20, "9" )
		PORT_DIPSETTING(    0x00, "10" )
	
		PORT_START  /* TRACKBALL X - port 02 on sub cpu */
		PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_X , 30, 10, 0, 0 )
	
		PORT_START  /* TRACKBALL Y - port 03 on sub cpu */
		PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_Y | IPF_REVERSE, 30, 10, 0, 0 )
	INPUT_PORTS_END
	
	
	/*****************************************************************************/
	/* Main CPU */
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xcfff, MRA_RAM ),
		new Memory_ReadAddress( 0xd000, 0xd7ff, MRA_BANK1 ),
		new Memory_ReadAddress( 0xd800, 0xdbff, beg_sharedram_r ), /* only half of the RAM is accessible, line a10 of IC73 (6116) is GNDed */
		new Memory_ReadAddress( 0xf000, 0xf0ff, bigevglf_vidram_r ), /* 41464 (64kB * 8 chips), addressed using ports 1 and 5 */
		new Memory_ReadAddress( 0xf840, 0xf8ff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xcfff, MWA_RAM ),
		new Memory_WriteAddress( 0xd000, 0xd7ff, MWA_ROM ),
		new Memory_WriteAddress( 0xd800, 0xdbff, beg_sharedram_w, beg_sharedram ),
		new Memory_WriteAddress( 0xe000, 0xe7ff, beg_palette_w, paletteram ),
		new Memory_WriteAddress( 0xe800, 0xefff, MWA_RAM, beg_spriteram1 ), /* sprite 'templates' */
		new Memory_WriteAddress( 0xf000, 0xf0ff, bigevglf_vidram_w ),
		new Memory_WriteAddress( 0xf840, 0xf8ff, MWA_RAM,beg_spriteram2 ),  /* spriteram (x,y,offset in spriteram1,palette) */
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort bigevglf_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x00, IOWP_NOP ), 	/* video ram enable ???*/
		new IO_WritePort( 0x01, 0x01, beg_gfxcontrol_w ),  /* plane select */
		new IO_WritePort( 0x02, 0x02, beg_banking_w ),
		new IO_WritePort( 0x03, 0x03, beg13A_set_w ),
		new IO_WritePort( 0x04, 0x04, beg13B_clr_w ),
		new IO_WritePort( 0x05, 0x05, bigevglf_vidram_addr_w ),	/* video banking (256 banks) for f000-f0ff area */
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort bigevglf_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x06,0x06, beg_status_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	
	/*********************************************************************************/
	/* Sub CPU */
	
	public static Memory_ReadAddress readmem_sub[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new Memory_ReadAddress( 0x4000, 0x47ff, MRA_RAM ),
		new Memory_ReadAddress( 0x8000, 0x83ff, beg_sharedram_r ), /* shared with main CPU */
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem_sub[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new Memory_WriteAddress( 0x4000, 0x47ff, MWA_RAM ),
		new Memory_WriteAddress( 0x8000, 0x83ff, beg_sharedram_w ), /* shared with main CPU */
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_WritePort bigevglf_sub_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x08, 0x08, IOWP_NOP ), /*coinlockout_w ???? watchdog ???? */
		new IO_WritePort( 0x0c, 0x0c, bigevglf_mcu_w ),
		new IO_WritePort( 0x0e, 0x0e, IOWP_NOP ), /* 0-enable MCU, 1-keep reset line ASSERTED; D0 goes to the input of ls74 and the /Q of this ls74 goes to reset line on 68705 */
		new IO_WritePort( 0x10, 0x17, beg13A_clr_w ),
		new IO_WritePort( 0x18, 0x1f, beg13B_set_w ),
		new IO_WritePort( 0x20, 0x20, sound_command_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	
	public static ReadHandlerPtr sub_cpu_mcu_coin_port_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		static int bit5=0;
		/*
				bit 0 and bit 1 = coin inputs
				bit 3 and bit 4 = MCU status
				bit 5           = must toggle, vblank ?
	
		*/
		bit5 ^= 0x20;
		return bigevglf_mcu_status_r(0) | (readinputport(1) & 3) | bit5; /* bit 0 and bit 1 - coin inputs */
	} };
	
	public static IO_ReadPort bigevglf_sub_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x00, input_port_0_r ),
		new IO_ReadPort( 0x01, 0x01, IORP_NOP ),
		new IO_ReadPort( 0x02, 0x02, input_port_4_r ),
		new IO_ReadPort( 0x03, 0x03, input_port_5_r ),
		new IO_ReadPort( 0x04, 0x04, sub_cpu_mcu_coin_port_r ),
		new IO_ReadPort( 0x05, 0x05, input_port_2_r ),
		new IO_ReadPort( 0x06, 0x06, input_port_3_r ),
		new IO_ReadPort( 0x07, 0x07, IORP_NOP ),
		new IO_ReadPort( 0x0b, 0x0b, bigevglf_mcu_r ),
		new IO_ReadPort( 0x20, 0x20, beg_fromsound_r ),
		new IO_ReadPort( 0x21, 0x21, beg_soundstate_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	
	/*********************************************************************************/
	/* Sound CPU */
	
	public static Memory_ReadAddress sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xc7ff, MRA_RAM ),
		new Memory_ReadAddress( 0xda00, 0xda00, soundstate_r ),
		new Memory_ReadAddress( 0xd800, 0xd800, sound_command_r ),	/* read from D800 sets bit 0 in status */
		new Memory_ReadAddress( 0xe000, 0xefff, MRA_NOP ),	/* space for diagnostics ROM */
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xc7ff, MWA_RAM ),
		new Memory_WriteAddress( 0xc800, 0xc800, AY8910_control_port_0_w ),
		new Memory_WriteAddress( 0xc801, 0xc801, AY8910_write_port_0_w ),
		new Memory_WriteAddress( 0xca00, 0xca0d, MSM5232_0_w ),
		new Memory_WriteAddress( 0xcc00, 0xcc00, MWA_NOP ),
		new Memory_WriteAddress( 0xce00, 0xce00, MWA_NOP ),
		new Memory_WriteAddress( 0xd800, 0xd800, beg_fromsound_w ),	/* write to D800 sets bit 1 in status */
		new Memory_WriteAddress( 0xda00, 0xda00, nmi_enable_w ),
		new Memory_WriteAddress( 0xdc00, 0xdc00, nmi_disable_w ),
		new Memory_WriteAddress( 0xde00, 0xde00, MWA_NOP ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	/*********************************************************************************/
	/* MCU */
	
	public static Memory_ReadAddress m68705_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x0000, bigevglf_68705_portA_r ),
		new Memory_ReadAddress( 0x0001, 0x0001, bigevglf_68705_portB_r ),
		new Memory_ReadAddress( 0x0002, 0x0002, bigevglf_68705_portC_r ),
		new Memory_ReadAddress( 0x0010, 0x007f, MRA_RAM ),
		new Memory_ReadAddress( 0x0080, 0x07ff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress m68705_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x0000, bigevglf_68705_portA_w ),
		new Memory_WriteAddress( 0x0001, 0x0001, bigevglf_68705_portB_w ),
		new Memory_WriteAddress( 0x0002, 0x0002, bigevglf_68705_portC_w ),
		new Memory_WriteAddress( 0x0004, 0x0004, bigevglf_68705_ddrA_w ),
		new Memory_WriteAddress( 0x0005, 0x0005, bigevglf_68705_ddrB_w ),
		new Memory_WriteAddress( 0x0006, 0x0006, bigevglf_68705_ddrC_w ),
		new Memory_WriteAddress( 0x0010, 0x007f, MWA_RAM ),
		new Memory_WriteAddress( 0x0080, 0x07ff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	static struct GfxLayout gfxlayout =
	{
		8,8,
		RGN_FRAC(1,4),
		4,
		{ 0, RGN_FRAC(1,4),RGN_FRAC(2,4),RGN_FRAC(3,4)},
		{ 0,1,2,3,4,5,6,7 },
		{ 0*8,1*8,2*8,3*8,4*8,5*8,6*8,7*8 },
		8*8
	};
	
	static struct GfxDecodeInfo gfxdecodeinfo[] =
	{
		{ REGION_GFX1, 0, &gfxlayout,   0x20*16, 16 },
		{ -1 }	/* end of array */
	};
	
	MACHINE_INIT( bigevglf )
	{
		beg13_ls74[0] = 0;
		beg13_ls74[1] = 0;
	}
	
	
	static struct AY8910interface ay8910_interface =
	{
		1,	/* 1 chip */
		8000000/4,	/* 2 MHz ? */
		{ 15 },
		{ 0 },
		{ 0 },
		{ 0 },
		{ 0 }
	};
	
	static struct MSM5232interface msm5232_interface =
	{
		1,	/* 1 chip */
		8000000/4,	/* 2 MHz ? */
		{ { 0.65e-6, 0.65e-6, 0.65e-6, 0.65e-6, 0.65e-6, 0.65e-6, 0.65e-6, 0.65e-6 } },	/* 0.65 (???) uF capacitors */
		{ 100 }	/* mixing level ??? */
	};
	
	static MACHINE_DRIVER_START( bigevglf )
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80,10000000/2)		/* 5 MHz ? */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(bigevglf_readport,bigevglf_writeport)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)	/* vblank */
	
		MDRV_CPU_ADD(Z80,10000000/2)		/* 5 MHz ? */
		MDRV_CPU_MEMORY(readmem_sub,writemem_sub)
		MDRV_CPU_PORTS(bigevglf_sub_readport,bigevglf_sub_writeport)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)	/* vblank */
	
		MDRV_CPU_ADD(Z80,8000000/2)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)		/* 4 MHz ? */
		MDRV_CPU_MEMORY(sound_readmem,sound_writemem)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,2)	/* IRQ generated by ???;
			2 irqs/frame give good music tempo but also SOUND ERROR in test mode,
			4 irqs/frame give SOUND OK in test mode but music seems to be running too fast */
	
		MDRV_CPU_ADD(M68705,2000000)	/* ??? */
		MDRV_CPU_MEMORY(m68705_readmem,m68705_writemem)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
		MDRV_INTERLEAVE(10)	/* 10 CPU slices per frame - interleaving is forced on the fly */
	
		MDRV_MACHINE_INIT(bigevglf)
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(0x800)
		MDRV_VIDEO_START(bigevglf)
		MDRV_VIDEO_UPDATE(bigevglf)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ay8910_interface) /* YM2149 really */
		MDRV_SOUND_ADD(MSM5232, msm5232_interface)
	MACHINE_DRIVER_END
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	ROM_START( bigevglf )
		ROM_REGION( 0x90000, REGION_CPU1, 0 )
		ROM_LOAD( "a67-21",   0x00000, 0x8000, CRC(2a62923d) SHA1(7b025180e203f268ae4d11baa18096e0a5704f77))
		ROM_LOAD( "a67-20",   0x08000, 0x4000, CRC(841561b1) SHA1(5d91449e135ef22508194a9543343c29e1c496cf))
		ROM_LOAD( "a67-03",   0x10000, 0x8000, CRC(695b3396) SHA1(2a3738e6bc492a4c68d2e15a2e474f7654aeb94d))
		ROM_LOAD( "a67-04",   0x18000, 0x8000, CRC(b8941902) SHA1(a03e432cbd8ea1df7223ea99ff1db220a57fc698))
		ROM_LOAD( "a67-05",   0x20000, 0x8000, CRC(681f5f4f) SHA1(2a5d8eeaf6ac697d5d4ee15164b6c4b1b81d7a29))
		ROM_LOAD( "a67-06",   0x28000, 0x8000, CRC(026f6fe5) SHA1(923b7d8363e587ef20b6518bee968d378166c76b))
		ROM_LOAD( "a67-07",   0x30000, 0x8000, CRC(27706bed) SHA1(da702c5a098eb106332996ec5d0e2c014782031e))
		ROM_LOAD( "a67-08",   0x38000, 0x8000, CRC(e922023a) SHA1(ea4c4b5e2f82ab20afb15f115e8cbc66d8471927))
		ROM_LOAD( "a67-09",   0x40000, 0x8000, CRC(a9d4263e) SHA1(8c3f2d541583e8e4b22e0beabcd04c5765508535))
		ROM_LOAD( "a67-10",   0x48000, 0x8000, CRC(7c35f4a3) SHA1(de60dc991c67fb7d48314bc8b2c1a27ad040bf1e))
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )
		ROM_LOAD( "a67-11",   0x00000, 0x4000, CRC(a2660d20) SHA1(3d8b670c071862d677d4e30655dcb6b8d830648a))
	
	 	ROM_REGION( 0x10000, REGION_CPU3, 0 )
		ROM_LOAD( "a67-16",   0x0000, 0x4000, CRC(5fb6d22e) SHA1(1701aa94b7f524187fd7213a94535bed3e8b6cc9))
		ROM_LOAD( "a67-17",   0x4000, 0x4000, CRC(9f57deae) SHA1(dbdb3d77c3de0113ef6671aec854e4e44ee162ef))
		ROM_LOAD( "a67-18",   0x8000, 0x4000, CRC(40d54fed) SHA1(bfa0922809bffafec15d3ef59ac8b8ad653860a1))
	
		ROM_REGION( 0x0800, REGION_CPU4, 0 )
		ROM_LOAD( "a67_19-1", 0x0000, 0x0800, CRC(25691658) SHA1(aabf47abac43abe2ffed18ead1cb94e587149e6e))
	
		ROM_REGION( 0x20000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "a67-12",   0x00000, 0x8000, CRC(980cc3c5) SHA1(c35c20cfff0b5cfd5b95742333ae20a2c93371b5))
		ROM_LOAD( "a67-13",   0x08000, 0x8000, CRC(ad6e04af) SHA1(4680d789cf53c4808105ad4f3c70aedb6d8bcf36))
		ROM_LOAD( "a67-14",   0x10000, 0x8000, CRC(d6708cce) SHA1(5b48f9dff2a3e28242dc2004469dc2ac2b5d0321))
		ROM_LOAD( "a67-15",   0x18000, 0x8000, CRC(1d261428) SHA1(0f3e6d83a8a462436fa414de4e1e4306db869d3e))
	ROM_END
	
	GAME( 1986, bigevglf,  0,        bigevglf,  bigevglf,  0, ROT270, "Taito America Corporation", "Big Event Golf")
}
