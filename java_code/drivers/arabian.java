/***************************************************************************

	Sun Electronics Arabian hardware

	driver by Dan Boris

	Games supported:
		* Arabian

	Known bugs:
		* none at this time

****************************************************************************

	Memory map

****************************************************************************

	========================================================================
	CPU #1 (Arabian)
	========================================================================
	0000-7FFF   R     xxxxxxxx    Program ROM
	8000-BFFF   R/W   xxxxxxxx    Bitmap RAM
	C000        R     ----xxxx    Coin inputs
	C200        R     ----xxxx    Option switches
	D000-DFFF   R/W   xxxxxxxx    Custom microprocessor RAM
	E000          W   ----xxxx    BSEL Bank select
	E001          W   xxxxxxxx    DMA ROM start address low
	E002          W   xxxxxxxx    DMA ROM start address high
	E003          W   xxxxxxxx    DMA RAM start address low
	E004          W   xxxxxxxx    DMA RAM start address high
	E005          W   xxxxxxxx    Picture size/DMA start low
	E006          W   xxxxxxxx    Picture size/DMA start high
	========================================================================
	C800          W   xxxxxxxx    Sound chip address
	CA00        R/W   xxxxxxxx    Sound chip data
	========================================================================
	Interrupts:
		NMI not connected
		IRQ generated by VBLANK
	========================================================================

	========================================================================
	CPU #1 (Kangaroo)
	========================================================================
	0000-7FFF   R     xxxxxxxx    Program ROM
	8000-BFFF   R/W   xxxxxxxx    Bitmap RAM
	C000        R     ----xxxx    Coin inputs
	C200        R     ----xxxx    Option switches
	D000-DFFF   R/W   xxxxxxxx    Custom microprocessor RAM
	E000          W   ----xxxx    BSEL Bank select
	E001          W   xxxxxxxx    DMA ROM start address low
	E002          W   xxxxxxxx    DMA ROM start address high
	E003          W   xxxxxxxx    DMA RAM start address low
	E004          W   xxxxxxxx    DMA RAM start address high
	E005          W   xxxxxxxx    Picture size/DMA start low
	E006          W   xxxxxxxx    Picture size/DMA start high
	========================================================================
	C800          W   xxxxxxxx    Sound chip address
	CA00        R/W   xxxxxxxx    Sound chip data
	========================================================================
	Interrupts:
		NMI not connected
		IRQ generated by VBLANK
	========================================================================

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class arabian
{
	
	
	/* constants */
	#define MAIN_OSC		12000000
	
	
	/* local variables */
	static UINT8 custom_cpu_reset;
	static UINT8 custom_cpu_busy;
	static UINT8 *custom_cpu_ram;
	
	
	
	/*************************************
	 *
	 *	Audio chip output ports
	 *
	 *************************************/
	
	static WRITE_HANDLER( ay8910_porta_w )
	{
		/*
			bit 7 = ENA
			bit 6 = ENB
			bit 5 = /ABHF
			bit 4 = /AGHF
			bit 3 = /ARHF
		*/
		arabian_video_control = data;
	}
	
	
	static WRITE_HANDLER( ay8910_portb_w )
	{
		/*
			bit 5 = /IREQ to custom CPU
			bit 4 = /SRES to custom CPU
			bit 1 = coin 2 counter
			bit 0 = coin 1 counter
		*/
	
		/* track the custom CPU reset */
		custom_cpu_reset = ~data & 0x10;
	
		/* clock the coin counters */
		coin_counter_w(1, ~data & 0x02);
		coin_counter_w(0, ~data & 0x01);
	}
	
	
	
	/*************************************
	 *
	 *	Custom CPU RAM snooping
	 *
	 *************************************/
	
	static READ_HANDLER( custom_cpu_r )
	{
		/* since we don't have a simulator for the Fujitsu 8841 4-bit microprocessor */
		/* we have to simulate its behavior; it looks like Arabian reads out of the  */
		/* alternate CPU's RAM space while the CPU is running. If the CPU is not     */
		/* running (i.e., the /SRES line is low), it needs to look like RAM to pass  */
		/* the self-tests */
	
		/* if the CPU reset line is being held down, just return RAM */
		if (custom_cpu_reset)
			return custom_cpu_ram[0x7f0 + offset];
	
		/* otherwise, assume the custom CPU is live */
		switch (offset)
		{
			/* 4-bit input ports from the custom */
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				return readinputport(2 + offset);
	
			/* busy flag; this is polled to check the custom CPU's readiness */
			/* we just toggle it on and off until the main CPU gets the result */
			/* it wants. There appears to be a number of different ways to make */
			/* the custom turn this on. */
			case 6:
				return custom_cpu_busy ^= 1;
	
			/* handshake read; the main CPU writes to the previous memory location */
			/* and waits for the custom to copy that value here */
			case 8:
				return custom_cpu_ram[0x7f0 + offset - 1];
	
			/* error cases */
			default:
				logerror("Input Port %04X read.  PC=%04X\n", offset+0xd7f0, activecpu_get_pc());
				return 0;
		}
		return 0;
	}
	
	
	static void update_flip_state(void)
	{
		/* the custom CPU also controls the video flip control line; unfortunately,    */
		/* it appears that the custom is smart enough to flip the screen itself, based */
		/* on the information stored at $d400 and $d401. The value at $d400 specifies  */
		/* the active player number, and the value at $d401 is a copy of the input     */
		/* port from $c200. Also, the value at $d34b controls the global flip screen   */
		/* state. */
	
		/* initial state is based on the flip screen flag */
		arabian_flip_screen = custom_cpu_ram[0x34b];
	
		/* flip if not player 1 and cocktail mode */
		if (custom_cpu_ram[0x400] != 0 && !(custom_cpu_ram[0x401] & 0x02))
			arabian_flip_screen = !arabian_flip_screen;
	}
	
	
	static WRITE_HANDLER( custom_flip_w )
	{
		custom_cpu_ram[0x34b + offset] = data;
		update_flip_state();
	}
	
	
	static WRITE_HANDLER( custom_cocktail_w )
	{
		custom_cpu_ram[0x400 + offset] = data;
		update_flip_state();
	}
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	static MEMORY_READ_START( readmem )
		{ 0x0000, 0x7fff, MRA_ROM },
		{ 0xc000, 0xc000, input_port_0_r },
		{ 0xc200, 0xc200, input_port_1_r },
		{ 0xd000, 0xd7ef, MRA_RAM },
		{ 0xd7f0, 0xd7ff, custom_cpu_r },
	MEMORY_END
	
	
	static MEMORY_WRITE_START( writemem )
		{ 0x0000, 0x7fff, MWA_ROM },
		{ 0x8000, 0xbfff, arabian_videoram_w, &videoram },
		{ 0xd000, 0xd7ff, MWA_RAM, &custom_cpu_ram },
		{ 0xe000, 0xe07f, arabian_blitter_w, &spriteram },
	MEMORY_END
	
	
	
	/*************************************
	 *
	 *	Main CPU port handlers
	 *
	 *************************************/
	
	static PORT_WRITE_START( writeport )
		{ 0xc800, 0xc800, AY8910_control_port_0_w },
		{ 0xca00, 0xca00, AY8910_write_port_0_w },
	PORT_END
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	INPUT_PORTS_START( arabian )
		PORT_START      /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 )
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 )
		PORT_SERVICE( 0x04, IP_ACTIVE_HIGH )
		PORT_BIT( 0xf8, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	
		PORT_START      /* DSW1 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( Lives ))
		PORT_DIPSETTING(    0x00, "3" )
		PORT_DIPSETTING(    0x01, "5" )
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( Cabinet ))
		PORT_DIPSETTING(    0x02, DEF_STR( Upright ))
		PORT_DIPSETTING(    0x00, DEF_STR( Cocktail ))
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( Flip_Screen ))
		PORT_DIPSETTING(    0x04, DEF_STR( Off ))
		PORT_DIPSETTING(    0x00, DEF_STR( On ))
		PORT_DIPNAME( 0x08, 0x00, "Carry Bowls to Next Life" )
		PORT_DIPSETTING(    0x08, DEF_STR( No ))
		PORT_DIPSETTING(    0x00, DEF_STR( Yes ))
		PORT_DIPNAME( 0xf0, 0x00, DEF_STR( Coinage ))
		PORT_DIPSETTING(    0x10, "A 2/1 B 2/1" )
		PORT_DIPSETTING(    0x20, "A 2/1 B 1/3" )
		PORT_DIPSETTING(    0x00, "A 1/1 B 1/1" )
		PORT_DIPSETTING(    0x30, "A 1/1 B 1/2" )
		PORT_DIPSETTING(    0x40, "A 1/1 B 1/3" )
		PORT_DIPSETTING(    0x50, "A 1/1 B 1/4" )
		PORT_DIPSETTING(    0x60, "A 1/1 B 1/5" )
		PORT_DIPSETTING(    0x70, "A 1/1 B 1/6" )
		PORT_DIPSETTING(    0x80, "A 1/2 B 1/2" )
		PORT_DIPSETTING(    0x90, "A 1/2 B 1/4" )
		PORT_DIPSETTING(    0xa0, "A 1/2 B 1/6" )
		PORT_DIPSETTING(    0xb0, "A 1/2 B 1/10" )
		PORT_DIPSETTING(    0xc0, "A 1/2 B 1/11" )
		PORT_DIPSETTING(    0xd0, "A 1/2 B 1/12" )
		PORT_DIPSETTING(    0xf0, DEF_STR( Free_Play ))
		/* 0xe0 gives A 1/2 B 1/6 */
	
		PORT_START      /* COM0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED )
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START1 )
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START2 )
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_COIN3 )		/* IN3 */
	
		PORT_START      /* COM1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY )
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY )
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY )
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY )
	
		PORT_START      /* COM2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 )
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )	/* IN9 */
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )	/* IN10 */
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )	/* IN11 */
	
		PORT_START      /* COM3 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL )
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL )
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL )
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL )
	
		PORT_START      /* COM4 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL )
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )	/* IN17 */
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )	/* IN18 */
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )	/* IN19 */
	
		PORT_START      /* COM5 */
		PORT_DIPNAME( 0x01, 0x00, "Coin Chutes" )
		PORT_DIPSETTING(    0x01, "1" )
		PORT_DIPSETTING(    0x00, "2" )
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( Demo_Sounds ))
		PORT_DIPSETTING(    0x02, DEF_STR( Off ))
		PORT_DIPSETTING(    0x00, DEF_STR( On ))
		PORT_DIPNAME( 0x0c, 0x04, DEF_STR( Bonus_Life ))
		PORT_DIPSETTING(    0x04, "20000" )
		PORT_DIPSETTING(    0x08, "40000" )
		PORT_DIPSETTING(    0x0c, "20000 50000 +100K" )
		PORT_DIPSETTING(    0x00, "None" )
	INPUT_PORTS_END
	
	
	
	/*************************************
	 *
	 *	Sound definitions
	 *
	 *************************************/
	
	static struct AY8910interface ay8910_interface =
	{
		1,
		MAIN_OSC/4/2,	/* 1.5 MHz */
		{ 50 },
		{ 0 },
		{ 0 },
		{ ay8910_porta_w },
		{ ay8910_portb_w }
	};
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MACHINE_DRIVER_START( arabian )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, MAIN_OSC/4)
		MDRV_CPU_FLAGS(CPU_16BIT_PORT)
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(0,writeport)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0, 255, 11, 244)
		MDRV_PALETTE_LENGTH(64)
		MDRV_COLORTABLE_LENGTH(256*32)
		
		MDRV_PALETTE_INIT(arabian)
		MDRV_VIDEO_START(arabian)
		MDRV_VIDEO_UPDATE(arabian)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
	MACHINE_DRIVER_END
	
	
	
	/*************************************
	 *
	 *	ROM definitions
	 *
	 *************************************/
	
	ROM_START( arabian )
		ROM_REGION( 0x10000, REGION_CPU1, 0 )
		ROM_LOAD( "ic1rev2.87", 0x0000, 0x2000, CRC(5e1c98b8) SHA1(1775b7b125dde3502aefcf6221662e82f55b3f2a) )
		ROM_LOAD( "ic2rev2.88", 0x2000, 0x2000, CRC(092f587e) SHA1(a722a61d35629ff4087c7a5e4c98b3ab51d6322b) )
		ROM_LOAD( "ic3rev2.89", 0x4000, 0x2000, CRC(15145f23) SHA1(ae250116b57455ed84948cd9a6bdda86b2ac3e16) )
		ROM_LOAD( "ic4rev2.90", 0x6000, 0x2000, CRC(32b77b44) SHA1(9d7951e723bc65e3d607f89836f1436b99f2585b) )
	
		ROM_REGION( 0x10000, REGION_GFX1, 0 )
		ROM_LOAD( "ic84.91",    0x0000, 0x2000, CRC(c4637822) SHA1(0c73d9a4db925421a535784780ad93bb0f091051) )
		ROM_LOAD( "ic85.92",    0x2000, 0x2000, CRC(f7c6866d) SHA1(34f545c5f7c152cd59f7be0a72105f739852cd6a) )
		ROM_LOAD( "ic86.93",    0x4000, 0x2000, CRC(71acd48d) SHA1(cd0bffed351b14c9aebbfc1d3d4d232a5b91a68f) )
		ROM_LOAD( "ic87.94",    0x6000, 0x2000, CRC(82160b9a) SHA1(03511f6ebcf22ba709a80a565e71acf5bdecbabb) )
	ROM_END
	
	
	ROM_START( arabiana )
		ROM_REGION( 0x10000, REGION_CPU1, 0 )
		ROM_LOAD( "ic1.87",     0x0000, 0x2000, CRC(51e9a6b1) SHA1(a2e6beab5380eed56972f5625be21b01c7e2082a) )
		ROM_LOAD( "ic2.88",     0x2000, 0x2000, CRC(1cdcc1ab) SHA1(46886d53cc8a1c1d540fd0e1ddf1811fb256c1f3) )
		ROM_LOAD( "ic3.89",     0x4000, 0x2000, CRC(b7b7faa0) SHA1(719418b7b7c057acb6d3060cf7061ffacf00798c) )
		ROM_LOAD( "ic4.90",     0x6000, 0x2000, CRC(dbded961) SHA1(ecc09fa95f6dd58c4ac0e095a89ffd3aae681da4) )
	
		ROM_REGION( 0x10000, REGION_GFX1, 0 )
		ROM_LOAD( "ic84.91",    0x0000, 0x2000, CRC(c4637822) SHA1(0c73d9a4db925421a535784780ad93bb0f091051) )
		ROM_LOAD( "ic85.92",    0x2000, 0x2000, CRC(f7c6866d) SHA1(34f545c5f7c152cd59f7be0a72105f739852cd6a) )
		ROM_LOAD( "ic86.93",    0x4000, 0x2000, CRC(71acd48d) SHA1(cd0bffed351b14c9aebbfc1d3d4d232a5b91a68f) )
		ROM_LOAD( "ic87.94",    0x6000, 0x2000, CRC(82160b9a) SHA1(03511f6ebcf22ba709a80a565e71acf5bdecbabb) )
	ROM_END
	
	
	
	/*************************************
	 *
	 *	Driver initialization
	 *
	 *************************************/
	
	static DRIVER_INIT( arabian )
	{
		install_mem_write_handler(0, 0xd34b, 0xd34b, custom_flip_w);
		install_mem_write_handler(0, 0xd400, 0xd401, custom_cocktail_w);
	}
	
	
	
	/*************************************
	 *
	 *	Game drivers
	 *
	 *************************************/
	
	GAME( 1983, arabian,  0,       arabian, arabian, arabian, ROT270, "Sun Electronics", "Arabian" )
	GAME( 1983, arabiana, arabian, arabian, arabian, arabian, ROT270, "[Sun Electronics] (Atari license)", "Arabian (Atari)" )
}
