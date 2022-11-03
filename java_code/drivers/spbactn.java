/*******************************************************************************
 Super Pinball Action (c) 1991 Tecmo
********************************************************************************
 driver by David Haywood
 inputs, dipswitches etc by Stephh

-general info-------------------------------------------------------------------

 A Pinball Game from Tecmo, the Hardware seems to be somewhere between that used
 for Tecmo's classic game Ninja Gaiden (see gaiden.c) and that used in Comad's
 Gals Pinball (see galspnbl.c) I imagine Comad took the hardware that this uses
 as a basis for writing their game on, adding a couple of features such as the
 pixel layer.

-readme file--------------------------------------------------------------------

 Super Pinball Action
 (c)1991 Tecmo

 CPU  : MC68000P12
 Sound: Z80A YM3812 Y3014B M6295
 OSC  : 12.000MHz 22.656MHz 4.00MHz

 ------
 9002-A
 ------
 ROMs:
 a-u68.1 - Main programs (27c101)
 a-u67.2 /

 a-u14.3 - Sound program (27512)

 a-u19 - Samples (27c1001)

 1 custom chip (u94, surface scrached)

 ------
 9002-B
 ------
 ROMs:
 b-u98  - Graphics (Mask, read as 27c2001)
 b-u99  |
 b-u104 |
 b-u105 /


 b-u110 - Graphics (Mask, read as 27c2001)
 b-u111 /

 Custom chips:
 U101, U102, U106, U107: surface scrached
 probably 2 pairs of TECMO-3&4
 U133: surface scrached
 probably TECMO-6
 U112: TECMO-5

 --- Team Japump!!! ---
 http://www.rainemu.com/japump/
 http://japump.i.am/
 Dumped by Noel Miruru
 17/Oct/2000

-working notes------------------------------------------------------------------

 68k interrupts
 lev 1 : 0x64 : ffff ffff - invalid
 lev 2 : 0x68 : ffff ffff - invalid
 lev 3 : 0x6c : 0000 1a0a - vblank?
 lev 4 : 0x70 : ffff ffff - invalid
 lev 5 : 0x74 : ffff ffff - invalid
 lev 6 : 0x78 : 0000 1ab2 - writes to 90031
 lev 7 : 0x7c : ffff ffff - invalid

 I can't use tilemap routines because they don't work with tiles having a
 greater height than width, which is the case on this game once its rotated.

TODO : (also check the notes from the galspnbl.c driver)

  - coin insertion is not recognized consistenly.
  - sprite/tile priority is sometimes wrong (see 1st table when ball in bumpers).
  - lots of unknown writes, what are they meant to do
  - verify some of the code which is from the other drivers such as sprite
    drawing as priorities are questionable in places
  - add support for blended sprites.

Unmapped writes (P.O.S.T.)

cpu #0 (PC=00001C3A): unmapped memory word write to 00090080 = 0F30 & FFFF
cpu #0 (PC=00001C42): unmapped memory word write to 00090090 = 0E00 & FFFF
cpu #0 (PC=00001C4A): unmapped memory word write to 000900A0 = 0F74 & FFFF
cpu #0 (PC=00001C52): unmapped memory word write to 000900B0 = 0FBA & FFFF
cpu #0 (PC=00001C5A): unmapped memory word write to 000900C0 = 0FDA & FFFF
cpu #0 (PC=00001C62): unmapped memory word write to 000900D0 = 0F20 & FFFF
cpu #0 (PC=00001C6A): unmapped memory word write to 000900E0 = 0FE7 & FFFF
cpu #0 (PC=00001C72): unmapped memory word write to 000900F0 = 0FF1 & FFFF
cpu #0 (PC=00001C7A): unmapped memory word write to 000A0110 = 0001 & FFFF
cpu #0 (PC=00001C80): unmapped memory word write to 000A0010 = 0001 & FFFF
cpu #0 (PC=00001C88): unmapped memory word write to 000A0200 = 001F & FFFF
cpu #0 (PC=00001C90): unmapped memory word write to 000A0202 = 0010 & FFFF
cpu #0 (PC=00001C98): unmapped memory word write to 000A0204 = 00E0 & FFFF
cpu #0 (PC=00001CA0): unmapped memory word write to 000A0206 = 0001 & FFFF
cpu #0 (PC=00002BFA): unmapped memory word write to 00090000 = 0000 & 00FF
cpu #0 (PC=00002C08): unmapped memory word write to 000A0100 = FF85 & FFFF
cpu #0 (PC=00002C10): unmapped memory word write to 000A0000 = FF85 & FFFF
cpu #0 (PC=00002C18): unmapped memory word write to 000A0108 = 0010 & FFFF
cpu #0 (PC=00002C20): unmapped memory word write to 000A0008 = 0010 & FFFF
cpu #0 (PC=00002C28): unmapped memory word write to 000A0104 = 0000 & FFFF
cpu #0 (PC=00002C2E): unmapped memory word write to 000A010C = 0000 & FFFF
cpu #0 (PC=00002C34): unmapped memory word write to 000A0004 = 0000 & FFFF
cpu #0 (PC=00002C3A): unmapped memory word write to 000A000C = 0000 & FFFF
cpu #0 (PC=00002C42): unmapped memory word write to 00090050 = 0004 & 00FF
cpu #0 (PC=00001A14): unmapped memory word write to 00090020 = 00AA & 00FF
cpu #0 (PC=00001A1A): unmapped memory word write to 00090030 = 0055 & 00FF

   Unmapped writes (when Dip Switches are displayed)

cpu #0 (PC=00001A14): unmapped memory word write to 00090020 = 00FF & 00FF
cpu #0 (PC=00001A1A): unmapped memory word write to 00090030 = 00FF & 00FF

   Unmapped writes (when grid is displayed)

cpu #0 (PC=0000326A): unmapped memory word write to 00090010 = 00FF & 00FF (only once)
cpu #0 (PC=00001A14): unmapped memory word write to 00090020 = 00F6 & 00FF
cpu #0 (PC=00001A1A): unmapped memory word write to 00090030 = 00F7 & 00FF

*******************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.02
 */ 
package arcadeflex.v078.drivers;

public class spbactn
{
	
	data16_t *spbactn_bgvideoram, *spbactn_fgvideoram, *spbactn_spvideoram;
	
	
	static WRITE16_HANDLER( soundcommand_w )
	{
		if (ACCESSING_LSB != 0)
		{
			soundlatch_w(offset,data & 0xff);
			cpu_set_irq_line(1,IRQ_LINE_NMI,PULSE_LINE);
		}
	}
	
	static MEMORY_READ16_START( spbactn_readmem )
		{ 0x00000, 0x3ffff, MRA16_ROM },
		{ 0x40000, 0x43fff, MRA16_RAM },
		{ 0x50000, 0x50fff, MRA16_RAM },
		{ 0x60000, 0x67fff, MRA16_RAM },
		{ 0x70000, 0x77fff, MRA16_RAM },
		{ 0x80000, 0x82fff, MRA16_RAM },
		{ 0x90000, 0x90001, input_port_0_word_r },
		{ 0x90010, 0x90011, input_port_1_word_r },
		{ 0x90020, 0x90021, input_port_2_word_r },
		{ 0x90030, 0x90031, input_port_4_word_r },
		{ 0x90040, 0x90041, input_port_3_word_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( spbactn_writemem )
		{ 0x00000, 0x3ffff, MWA16_ROM },
		{ 0x40000, 0x43fff, MWA16_RAM },	// main ram
		{ 0x50000, 0x50fff, MWA16_RAM, &spbactn_spvideoram },
		{ 0x60000, 0x67fff, MWA16_RAM, &spbactn_fgvideoram },
		{ 0x70000, 0x77fff, MWA16_RAM, &spbactn_bgvideoram },
		{ 0x80000, 0x827ff, paletteram16_xxxxBBBBGGGGRRRR_word_w, &paletteram16 },
	
		/* this is an awful lot of unknowns */
		{ 0x90000, 0x90001, MWA16_NOP },
		{ 0x90010, 0x90011, soundcommand_w },
	//	{ 0x90020, 0x90021, soundcommand_w },
		{ 0x90030, 0x90031, MWA16_NOP },
	
		{ 0x90080, 0x90081, MWA16_NOP },
		{ 0x90090, 0x90091, MWA16_NOP },
		{ 0x900a0, 0x900a1, MWA16_NOP },
		{ 0x900b0, 0x900b1, MWA16_NOP },
		{ 0x900c0, 0x900c1, MWA16_NOP },
		{ 0x900d0, 0x900d1, MWA16_NOP },
		{ 0x900e0, 0x900e1, MWA16_NOP },
		{ 0x900f0, 0x900f1, MWA16_NOP },
	
		{ 0xa0000, 0xa0001, MWA16_NOP },
		{ 0xa0004, 0xa0005, MWA16_NOP },
		{ 0xa0008, 0xa0009, MWA16_NOP },
		{ 0xa000c, 0xa000d, MWA16_NOP },
		{ 0xa0010, 0xa0011, MWA16_NOP },
	
		{ 0xa0100, 0xa0101, MWA16_NOP },
		{ 0xa0104, 0xa0105, MWA16_NOP },
		{ 0xa0108, 0xa0109, MWA16_NOP },
		{ 0xa010c, 0xa010d, MWA16_NOP },
		{ 0xa0110, 0xa0111, MWA16_NOP },
	
		{ 0xa0200, 0xa0201, MWA16_NOP },
		{ 0xa0202, 0xa0203, MWA16_NOP },
		{ 0xa0204, 0xa0205, MWA16_NOP },
		{ 0xa0206, 0xa0207, MWA16_NOP },
	MEMORY_END
	
	public static Memory_ReadAddress sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0xefff, MRA_ROM ),
		new Memory_ReadAddress( 0xf000, 0xf7ff, MRA_RAM ),
		new Memory_ReadAddress( 0xf800, 0xf800, OKIM6295_status_0_r ),
		new Memory_ReadAddress( 0xfc00, 0xfc00, MRA_NOP ),	/* irq ack ?? */
		new Memory_ReadAddress( 0xfc20, 0xfc20, soundlatch_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0xefff, MWA_ROM ),
		new Memory_WriteAddress( 0xf000, 0xf7ff, MWA_RAM ),
		new Memory_WriteAddress( 0xf800, 0xf800, OKIM6295_data_0_w ),
		new Memory_WriteAddress( 0xf810, 0xf810, YM3812_control_port_0_w ),
		new Memory_WriteAddress( 0xf811, 0xf811, YM3812_write_port_0_w ),
		new Memory_WriteAddress( 0xfc00, 0xfc00, MWA_NOP ),	/* irq ack ?? */
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	static InputPortPtr input_ports_spbactn = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );	// Left flipper
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 );	// "Shake"
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	// "Shake" (duplicated)
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );	// Right flipper
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );	// Press mulitple times for multiple players
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x01, "2 Coins/1 Credit 3/2" );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x05, "1 Coin/1 Credit 2/3" );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, "1 Coin/1 Credit 5/6" );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x08, "2 Coins/1 Credit 3/2" );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x28, "1 Coin/1 Credit 2/3" );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, "1 Coin/1 Credit 5/6" );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Lives") );		// Balls
		PORT_DIPSETTING(    0x00, "2" );
		PORT_DIPSETTING(    0xc0, "3" );
		PORT_DIPSETTING(    0x80, "4" );
		PORT_DIPSETTING(    0x40, "5" );
	
		PORT_START(); 	/* DSW2 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Normal" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Very Hard" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x04, "100k and 500k" );
		PORT_DIPSETTING(    0x0c, "200k and 800k" );
		PORT_DIPSETTING(    0x08, "200k" );
		PORT_DIPSETTING(    0x00, "None" );
		PORT_DIPNAME( 0x10, 0x10, "Hit Difficulty" );	// From .xls file - WHAT does that mean ?
		PORT_DIPSETTING(    0x10, "Normal" );
		PORT_DIPSETTING(    0x00, "Difficult" );
		PORT_DIPNAME( 0x20, 0x20, "Display Instructions" );// "Change Software" in .xls file
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Demo_Sounds") );	// To be confirmed
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Match" );			// Check code at 0x00bf8c
		PORT_DIPSETTING(    0x80, "1/20" );
		PORT_DIPSETTING(    0x00, "1/40" );
	INPUT_PORTS_END(); }}; 
	
	static GfxLayout fgtilelayout = new GfxLayout
	(
		16,8,
		RGN_FRAC(1,2),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 0*4, 1*4, RGN_FRAC(1,2)+0*4, RGN_FRAC(1,2)+1*4, 2*4, 3*4, RGN_FRAC(1,2)+2*4, RGN_FRAC(1,2)+3*4,
				16*8+0*4, 16*8+1*4, 16*8+RGN_FRAC(1,2)+0*4, 16*8+RGN_FRAC(1,2)+1*4, 16*8+2*4, 16*8+3*4, 16*8+RGN_FRAC(1,2)+2*4, 16*8+RGN_FRAC(1,2)+3*4 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		32*8
	);
	
	static GfxLayout bgtilelayout = new GfxLayout
	(
		16,8,
		RGN_FRAC(1,2),
		4,
		new int[] { 3, 2, 1, 0 },
	
		new int[] { RGN_FRAC(1,2)+1*4, RGN_FRAC(1,2)+0*4, 1*4, 0*4,
		RGN_FRAC(1,2)+3*4, RGN_FRAC(1,2)+2*4, 3*4, 2*4,
		16*8+RGN_FRAC(1,2)+1*4,16*8+RGN_FRAC(1,2)+0*4, 16*8+1*4,16*8+0*4,
		16*8+RGN_FRAC(1,2)+3*4, 16*8+RGN_FRAC(1,2)+2*4, 16*8+3*4,16*8+2*4 },
	
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		32*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,2),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 0, 4, RGN_FRAC(1,2)+0, RGN_FRAC(1,2)+4, 8+0, 8+4, 8+RGN_FRAC(1,2)+0, 8+RGN_FRAC(1,2)+4 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, fgtilelayout,   0x0200, 16384  ),
		new GfxDecodeInfo( REGION_GFX2, 0, bgtilelayout,   0x0300, 16384  ),
		new GfxDecodeInfo( REGION_GFX3, 0, spritelayout,   0x0000, 16384  ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static void irqhandler(int linestate)
	{
		cpu_set_irq_line(1,0,linestate);
	}
	
	static struct YM3812interface ym3812_interface =
	{
		1,			/* 1 chip */
		3579545,	/* 3.579545 MHz ? */
		{ 100 },	/* volume */
		{ irqhandler },
	};
	
	static struct OKIM6295interface okim6295_interface =
	{
		1,					/* 1 chip */
		{ 8000 },			/* 8000Hz frequency? */
		{ REGION_SOUND1 },	/* memory region */
		{ 50 }
	};
	
	static MACHINE_DRIVER_START( spbactn )
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 12000000)
		MDRV_CPU_MEMORY(spbactn_readmem,spbactn_writemem)
		MDRV_CPU_VBLANK_INT(irq3_line_hold,1)
	
		MDRV_CPU_ADD(Z80, 4000000)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)	/* 4 MHz ??? */
		MDRV_CPU_MEMORY(sound_readmem,sound_writemem)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_NEEDS_6BITS_PER_GUN | VIDEO_RGB_DIRECT)
		MDRV_SCREEN_SIZE(64*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 64*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(0x2800/2)
	
		MDRV_VIDEO_START(spbactn)
		MDRV_VIDEO_UPDATE(spbactn)
	
		/* sound hardware */
		MDRV_SOUND_ADD(YM3812, ym3812_interface)
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface)
	MACHINE_DRIVER_END
	
	static RomLoadPtr rom_spbactn = new RomLoadPtr(){ public void handler(){ 
		/* Board 9002-A (CPU Board) */
		ROM_REGION( 0x40000, REGION_CPU1, 0 )
		ROM_LOAD16_BYTE( "rom1.bin", 0x00000, 0x20000, CRC(6741bd3f) SHA1(844eb6465a15d339043fd6d2b6ba20ba216de493) )
		ROM_LOAD16_BYTE( "rom2.bin", 0x00001, 0x20000, CRC(488cc511) SHA1(41b4a01f35e0e93634b4843dbb894ab9840807bf) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )
		ROM_LOAD( "a-u14.3", 0x00000, 0x10000, CRC(57f4c503) SHA1(e5ddc63a43ba824bcaa4340eeba25a0d3f26cad9) )
	
		ROM_REGION( 0x20000, REGION_SOUND1, 0 )
		ROM_LOAD( "a-u19",   0x00000, 0x20000,  CRC(87427d7d) SHA1(f76b0dc3f0d87deb0f0c81084aff9756b236e867) )
	
		/* Board 9002-B (GFX Board) */
		ROM_REGION( 0x080000, REGION_GFX1, 0 ) /* 16x8 FG Tiles */
		ROM_LOAD( "b-u98",   0x00000, 0x40000, CRC(315eab4d) SHA1(6f812c85981dc649caca8b4635e3b8fd3a3c054d) )
		ROM_LOAD( "b-u99",   0x40000, 0x40000, CRC(7b76efd9) SHA1(9f23460aebe12cb5c4193776bf876d6044892979) )
	
		ROM_REGION( 0x080000, REGION_GFX2, 0 ) /* 16x8 BG Tiles */
		ROM_LOAD( "b-u104",  0x00000, 0x40000, CRC(b648a40a) SHA1(1fb756dcd027a5702596e33bbe8a0beeb3ceb22b) )
		ROM_LOAD( "b-u105",  0x40000, 0x40000, CRC(0172d79a) SHA1(7ee1faa65c85860bd81988329df516bc34940ef5) )
	
		ROM_REGION( 0x080000, REGION_GFX3, 0 ) /* 8x8 Sprite Tiles */
		ROM_LOAD( "b-u110",  0x00000, 0x40000, CRC(862ebacd) SHA1(05732e8524c50256c1db29317625d0edc19b87d2) )
		ROM_LOAD( "b-u111",  0x40000, 0x40000, CRC(1cc1379a) SHA1(44fdab8cb5ab1488688f1ac52f005454e835efee) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_spbactnj = new RomLoadPtr(){ public void handler(){ 
		/* Board 9002-A (CPU Board) */
		ROM_REGION( 0x40000, REGION_CPU1, 0 )
		ROM_LOAD16_BYTE( "a-u68.1", 0x00000, 0x20000, CRC(b5b2d824) SHA1(be04ca370a381d7396f39e31fb2680973193daee) )
		ROM_LOAD16_BYTE( "a-u67.2", 0x00001, 0x20000, CRC(9577b48b) SHA1(291d890a9d0e434455f183eb12ae6edf3156688d) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )
		ROM_LOAD( "a-u14.3", 0x00000, 0x10000, CRC(57f4c503) SHA1(e5ddc63a43ba824bcaa4340eeba25a0d3f26cad9) )
	
		ROM_REGION( 0x20000, REGION_SOUND1, 0 )
		ROM_LOAD( "a-u19",   0x00000, 0x20000,  CRC(87427d7d) SHA1(f76b0dc3f0d87deb0f0c81084aff9756b236e867) )
	
		/* Board 9002-B (GFX Board) */
		ROM_REGION( 0x080000, REGION_GFX1, 0 ) /* 16x8 FG Tiles */
		ROM_LOAD( "b-u98",   0x00000, 0x40000, CRC(315eab4d) SHA1(6f812c85981dc649caca8b4635e3b8fd3a3c054d) )
		ROM_LOAD( "b-u99",   0x40000, 0x40000, CRC(7b76efd9) SHA1(9f23460aebe12cb5c4193776bf876d6044892979) )
	
		ROM_REGION( 0x080000, REGION_GFX2, 0 ) /* 16x8 BG Tiles */
		ROM_LOAD( "b-u104",  0x00000, 0x40000, CRC(b648a40a) SHA1(1fb756dcd027a5702596e33bbe8a0beeb3ceb22b) )
		ROM_LOAD( "b-u105",  0x40000, 0x40000, CRC(0172d79a) SHA1(7ee1faa65c85860bd81988329df516bc34940ef5) )
	
		ROM_REGION( 0x080000, REGION_GFX3, 0 ) /* 8x8 Sprite Tiles */
		ROM_LOAD( "b-u110",  0x00000, 0x40000, CRC(862ebacd) SHA1(05732e8524c50256c1db29317625d0edc19b87d2) )
		ROM_LOAD( "b-u111",  0x40000, 0x40000, CRC(1cc1379a) SHA1(44fdab8cb5ab1488688f1ac52f005454e835efee) )
	ROM_END(); }}; 
	
	GAMEX( 1991, spbactn, 0,        spbactn, spbactn, 0, ROT90, "Tecmo", "Super Pinball Action (US)", GAME_IMPERFECT_GRAPHICS )
	GAMEX( 1991, spbactnj, spbactn, spbactn, spbactn, 0, ROT90, "Tecmo", "Super Pinball Action (Japan)", GAME_IMPERFECT_GRAPHICS )
}
