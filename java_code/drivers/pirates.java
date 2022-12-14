/*---

Pirates      (c)1994 NIX  (DEC 14 1994 17:32:29) displayed in cabinet test mode
Genix Family (c)1994 NIX  (MAY 10 1994 14:21:20) displayed in cabinet test mode
driver by David Haywood and Nicola Salmoria

TODO:
- EEPROM doesn't work. I'm not sure what the program is trying to do.
  The EEPROM handling might actually be related to the protection which
  makes the game hang.
  See pirates_in1_r() for code which would work around the protection,
  but makes the game periodically hang for a couple of seconds; therefore,
  for now I'm just patching out the protection check.

- Protection is the same in Genix Family

-----

Here's some info about the dump:

Name:            Pirates
Manufacturer:    NIX
Year:            1994
Date Dumped:     14-07-2002 (DD-MM-YYYY)

CPU:             68000, possibly at 12mhz (prototype board does have a 16mhz one)
SOUND:           OKIM6295
GFX:             Unknown

CPU Roms at least are the same on the Prototype board (the rest of the roms probably are too)

-----

Program Roms are Scrambled (Data + Address Lines)
P Graphic Roms (Tilemap Tiles) are Scrambled (Data + Address Lines)
S Graphic Roms (Sprite Tiles) are Scrambled (Data + Address Lines)
OKI Samples Rom is Scrambled (Data + Address Lines)

68k interrupts (pirates)
lev 1 : 0x64 : 0000 bf84 - vbl?
lev 2 : 0x68 : 0000 4bc6 -
lev 3 : 0x6c : 0000 3bda -
lev 4 : 0x70 : 0000 3bf0 -
lev 5 : 0x74 : 0000 3c06 -
lev 6 : 0x78 : 0000 3c1c -
lev 7 : 0x7c : 0000 3c32 -

Inputs mapped by Stephh

The game hanging is an interesting issue, the board owner has 2 copies of this game, one a prototype,
on the final released version.  The roms on both boards are the same, however the prototype locks up
just as it does in Mame at the moment.  The final board does not.  It would appear the prototype
board does not have the protection hardware correctly in place


PCB Layout (Genix Family) (by Guru)
----------

|------------------------------------------------|
|     0.31       6116  6116             3.72     |
|         M6295  6116  6116             4.71     |
|                                       5.70     |
|         6264         6116             6.69     |
|         6264         6116                      |
|                                                |
|                                                |
| 93C46       Altera  24MHz   Altera             |
|             EPM7064         EPM7064            |
|                                                |
|                                                |
|             Altera          Altera  7.34  9.35 |
|             EPM7064         EPM7064 8.48 10.49 |
|                                                |
|        PAL                                     |
|                                                |
|        68000  1.15  62256  6264                |
| 32MHz         2.16  62256  6264                |
|               *                                |
|------------------------------------------------|

Notes:
      68000 clock: 16.000MHz
      M6295 clock: 1.33333MHz, Sample Rate: /165
      VSync: 60Hz
      HSync: 15.69kHz
      *    : unknown IC (18 pin DIP, surface scratched off)

---*/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class pirates
{
	
	
	WRITE16_HANDLER( pirates_tx_tileram_w );
	WRITE16_HANDLER( pirates_fg_tileram_w );
	WRITE16_HANDLER( pirates_bg_tileram_w );
	
	
	
	static struct EEPROM_interface eeprom_interface =
	{
		6,				/* address bits */
		16,				/* data bits */
		"*110",			/*  read command */
		"*101",			/* write command */
		0,				/* erase command */
		"*10000xxxx",	/* lock command */
		"*10011xxxx"	/* unlock command */
	};
	
	public static NVRAMHandlerPtr nvram_handler_pirates  = new NVRAMHandlerPtr() { public void handler(mame_file file, int read_or_write){
		if (read_or_write) EEPROM_save(file);
		else
		{
			EEPROM_init(&eeprom_interface);
			if (file) EEPROM_load(file);
		}
	} };
	
	static WRITE16_HANDLER( pirates_out_w )
	{
		if (ACCESSING_LSB)
		{
			/* bits 0-2 control EEPROM */
			EEPROM_write_bit(data & 0x04);
			EEPROM_set_cs_line((data & 0x01) ? CLEAR_LINE : ASSERT_LINE);
			EEPROM_set_clock_line((data & 0x02) ? ASSERT_LINE : CLEAR_LINE);
	
			/* bit 6 selects oki bank */
			OKIM6295_set_bank_base(0, (data & 0x40) ? 0x40000 : 0x00000);
	
			/* bit 7 used (function unknown) */
		}
	
	//	logerror("%06x: out_w %04x\n",activecpu_get_pc(),data);
	}
	
	static READ16_HANDLER( pirates_in1_r )
	{
	//	static int prot = 0xa3;
		int bit;
	
	//	logerror("%06x: IN1_r\n",activecpu_get_pc());
	
	#if 0
		/* Pirates protection workaround. It more complicated than this... see code at
		   602e and 62a6 */
		/* For Genix, see 6576 for setting values and 67c2,d3b4 and dbc2 for tests. */
	
		if (activecpu_get_pc() == 0x6134)
		{
			bit = prot & 1;
			prot = (prot >> 1) | (bit << 7);
		}
		else if (activecpu_get_pc() == 0x6020)
			bit = 0;
		else if (activecpu_get_pc() == 0x6168)
			bit = 0;
		else if (activecpu_get_pc() == 0x61cc)
			bit = 1;
		else
	#endif
			bit = 1;
	
		/* bit 4 is EEPROM data, bit 7 is protection */
		return input_port_1_word_r(0,0) | (EEPROM_read_bit() << 4) | (bit << 7);
	}
	
	
	
	/* Memory Maps */
	
	static MEMORY_READ16_START( pirates_readmem )
		{ 0x000000, 0x0fffff, MRA16_ROM },
		{ 0x100000, 0x10ffff, MRA16_RAM },
		{ 0x300000, 0x300001, input_port_0_word_r },
		{ 0x400000, 0x400001, pirates_in1_r },
	//	{ 0x500000, 0x5007ff, MRA16_RAM },
		{ 0x800000, 0x803fff, MRA16_RAM },
	//	{ 0x900000, 0x903fff, MRA16_RAM },
		{ 0xa00000, 0xa00001, OKIM6295_status_0_lsb_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( pirates_writemem )
		{ 0x000000, 0x0fffff, MWA16_ROM },
		{ 0x100000, 0x10ffff, MWA16_RAM }, // main ram
		{ 0x500000, 0x5007ff, MWA16_RAM, &pirates_spriteram },
	//	{ 0x500800, 0x50080f, MWA16_RAM },
		{ 0x600000, 0x600001, pirates_out_w },
		{ 0x700000, 0x700001, MWA16_RAM, &pirates_scroll },	// scroll reg
		{ 0x800000, 0x803fff, paletteram16_xRRRRRGGGGGBBBBB_word_w, &paletteram16 },
		{ 0x900000, 0x90017f, MWA16_RAM },  // more of tilemaps ?
		{ 0x900180, 0x90137f, pirates_tx_tileram_w, &pirates_tx_tileram },
		{ 0x901380, 0x902a7f, pirates_fg_tileram_w, &pirates_fg_tileram },
	//	{ 0x902580, 0x902a7f, MWA16_RAM },  // more of tilemaps ?
		{ 0x902a80, 0x904187, pirates_bg_tileram_w, &pirates_bg_tileram },
	//	{ 0x903c80, 0x904187, MWA16_RAM },  // more of tilemaps ?
		{ 0xa00000, 0xa00001, OKIM6295_data_0_lsb_w },
	MEMORY_END
	
	
	
	/* Input Ports */
	
	static InputPortPtr input_ports_pirates = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( pirates )
		PORT_START(); 	// IN0 - 0x300000.w
		PORT_BIT(  0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 );
		PORT_BIT(  0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );
		PORT_BIT(  0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );
		PORT_BIT(  0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
		PORT_BIT(  0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT(  0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT(  0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT(  0x0080, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 	// IN1 - 0x400000.w
		PORT_BIT(  0x0001, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT(  0x0002, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT(  0x0004, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BITX( 0x0008, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
		PORT_BIT(  0x0010, IP_ACTIVE_HIGH,IPT_SPECIAL );	// EEPROM data
		PORT_BIT(  0x0020, IP_ACTIVE_HIGH, IPT_UNKNOWN );	// seems checked in "test mode"
		PORT_BIT(  0x0040, IP_ACTIVE_HIGH, IPT_UNKNOWN );	// seems checked in "test mode"
		PORT_BIT(  0x0080, IP_ACTIVE_HIGH,IPT_SPECIAL );	// protection (see pirates_in1_r)
		/* What do these bits do ? */
		PORT_BIT(  0x0100, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT(  0x0200, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT(  0x0400, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT(  0x0800, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT(  0x1000, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT(  0x2000, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT(  0x4000, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT(  0x8000, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(3,4), RGN_FRAC(2,4), RGN_FRAC(1,4), RGN_FRAC(0,4) },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0 },
		new int[] { 8*0, 8*1, 8*2, 8*3, 8*4, 8*5, 8*6, 8*7 },
		8*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(3,4), RGN_FRAC(2,4), RGN_FRAC(1,4), RGN_FRAC(0,4) },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0,
		 15,14,13,12,11,10, 9, 8 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
		  8*16, 9*16,10*16,11*16,12*16,13*16,14*16,15*16 },
		16*16
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
	
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   0x0000, 3*128 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 0x1800,   128 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/* Machine Driver + Related bits */
	
	static struct OKIM6295interface okim6295_interface =
	{
		1,                  /* 1 chip */
		{ 1333333/165 },     /* measured frequency */
		{ REGION_SOUND1 },	/* memory region */
		{ 100 }
	};
	
	
	
	static MACHINE_DRIVER_START( pirates )
		MDRV_CPU_ADD(M68000, 16000000) /* 16mhz */
		MDRV_CPU_MEMORY(pirates_readmem,pirates_writemem)
		MDRV_CPU_VBLANK_INT(irq1_line_hold,1)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		MDRV_NVRAM_HANDLER(pirates)
	
		MDRV_GFXDECODE(gfxdecodeinfo)
	
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(36*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 36*8-1, 2*8, 30*8-1)
		MDRV_PALETTE_LENGTH(0x2000)
	
		MDRV_VIDEO_START(pirates)
		MDRV_VIDEO_UPDATE(pirates)
	
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface)
	MACHINE_DRIVER_END
	
	
	
	
	/* Rom Loading */
	
	static RomLoadPtr rom_pirates = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 ) /* 68000 Code (encrypted) */
		ROM_LOAD16_BYTE( "r_449b.bin",  0x00000, 0x80000, CRC(224aeeda) SHA1(5b7e47a106af0debf8b07f120571f437ad6ab5c3) )
		ROM_LOAD16_BYTE( "l_5c1e.bin",  0x00001, 0x80000, CRC(46740204) SHA1(6f1da3b2cbea25bbfdec74c625c5fb23459b83b6) )
	
		ROM_REGION( 0x200000, REGION_GFX1, 0 ) /* GFX (encrypted) */
		ROM_LOAD( "p4_4d48.bin", 0x000000, 0x080000, CRC(89fda216) SHA1(ea31e750460e67a24972b04171230633eb2b6d9d) )
		ROM_LOAD( "p2_5d74.bin", 0x080000, 0x080000, CRC(40e069b4) SHA1(515d12cbb29bdbf3f3016e5bbe14941209978095) )
		ROM_LOAD( "p1_7b30.bin", 0x100000, 0x080000, CRC(26d78518) SHA1(c293f1194f8ef38241d149cf1db1a511a7fb4936) )
		ROM_LOAD( "p8_9f4f.bin", 0x180000, 0x080000, CRC(f31696ea) SHA1(f5ab59e441317b02b615a1cdc6d075c5bdcdea73) )
	
		ROM_REGION( 0x200000, REGION_GFX2, 0 ) /* GFX (encrypted) */
		ROM_LOAD( "s1_6e89.bin", 0x000000, 0x080000, CRC(c78a276f) SHA1(d5127593e68f9e8f2878803c652a35a1c6d82b2c) )
		ROM_LOAD( "s2_6df3.bin", 0x080000, 0x080000, CRC(9f0bad96) SHA1(b8f910aa259192e261815392f5d7c9c7dabe0b4d) )
		ROM_LOAD( "s4_fdcc.bin", 0x100000, 0x080000, CRC(8916ddb5) SHA1(f4f7da831ef929eb7575bbe69eae317f15cfd648) )
		ROM_LOAD( "s8_4b7c.bin", 0x180000, 0x080000, CRC(1c41bd2c) SHA1(fba264a3c195f303337223a74cbad5eec5c457ec) )
	
		ROM_REGION( 0x080000, REGION_SOUND1, 0) /* OKI samples (encrypted) */
		ROM_LOAD( "s89_49d4.bin", 0x000000, 0x080000, CRC(63a739ec) SHA1(c57f657225e62b3c9c5f0c7185ad7a87794d55f4) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_genix = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 ) /* 68000 Code (encrypted) */
		ROM_LOAD16_BYTE( "1.15",  0x00000, 0x80000, CRC(d26abfb0) SHA1(4a89ba7504f86cb612796c376f359ab61ec3d902) )
		ROM_LOAD16_BYTE( "2.16",  0x00001, 0x80000, CRC(a14a25b4) SHA1(9fa64c6514bdee56b5654b001f8367283b461e8a) )
	
		ROM_REGION( 0x200000, REGION_GFX1, 0 ) /* GFX (encrypted) */
		ROM_LOAD( "7.34", 0x000000, 0x040000, CRC(58da8aac) SHA1(bfc8449ba842f8ceac62ebdf6005d8f19d96afa6) )
		ROM_LOAD( "9.35", 0x080000, 0x040000, CRC(96bad9a8) SHA1(4e757cca0ab157f0c935087c9702c88741bf7a79) )
		ROM_LOAD( "8.48", 0x100000, 0x040000, CRC(0ddc58b6) SHA1(d52437607695ddebfe8494fd214efd20ba72d549) )
		ROM_LOAD( "10.49",0x180000, 0x040000, CRC(2be308c5) SHA1(22fc0991557643c22f6763f186b74900a33a39e0) )
	
		ROM_REGION( 0x200000, REGION_GFX2, 0 ) /* GFX (encrypted) */
		ROM_LOAD( "6.69", 0x000000, 0x040000, CRC(b8422af7) SHA1(d3290fc6ea2670c445731e2b493205874dc4b319) )
		ROM_LOAD( "5.70", 0x080000, 0x040000, CRC(e46125c5) SHA1(73d9a51f30a9c1a8397145d2a4397696ef37f4e5) )
		ROM_LOAD( "4.71", 0x100000, 0x040000, CRC(7a8ed21b) SHA1(f380156c44de2fc316f390adee09b6a3cd404dec) )
		ROM_LOAD( "3.72", 0x180000, 0x040000, CRC(f78bd6ca) SHA1(c70857b8053f9a6e3e15bbc9f7d13354b0966b30) )
	
		ROM_REGION( 0x080000, REGION_SOUND1, 0) /* OKI samples (encrypted) */
		ROM_LOAD( "0.31", 0x000000, 0x080000, CRC(80d087bc) SHA1(04d1aacc273c7ffa57b48bd043d55b5b3d993f74) )
	ROM_END(); }}; 
	
	/* Init */
	
	static void pirates_decrypt_68k(void)
	{
	    int rom_size;
	    UINT16 *buf, *rom;
	    int i;
	
	    rom_size = memory_region_length(REGION_CPU1);
	
	    buf = malloc(rom_size);
	
	    if (!buf) return;
	
	    rom = (UINT16 *)memory_region(REGION_CPU1);
	    memcpy (buf, rom, rom_size);
	
	    for (i=0; i<rom_size/2; i++)
	    {
	        int adrl, adrr;
	        unsigned char vl, vr;
	
	        adrl = BITSWAP24(i,23,22,21,20,19,18,4,8,3,14,2,15,17,0,9,13,10,5,16,7,12,6,1,11);
	        vl = BITSWAP8(buf[adrl],    4,2,7,1,6,5,0,3);
	
	        adrr = BITSWAP24(i,23,22,21,20,19,18,4,10,1,11,12,5,9,17,14,0,13,6,15,8,3,16,7,2);
	        vr = BITSWAP8(buf[adrr]>>8, 1,4,7,0,3,5,6,2);
	
	        rom[i] = (vr<<8) | vl;
	    }
	    free (buf);
	}
	
	static void pirates_decrypt_p(void)
	{
	    int rom_size;
	    UINT8 *buf, *rom;
	    int i;
	
	    rom_size = memory_region_length(REGION_GFX1);
	
	    buf = malloc(rom_size);
	
	    if (!buf) return;
	
	    rom = memory_region(REGION_GFX1);
	    memcpy (buf, rom, rom_size);
	
	    for (i=0; i<rom_size/4; i++)
	    {
			int adr = BITSWAP24(i,23,22,21,20,19,18,10,2,5,9,7,13,16,14,11,4,1,6,12,17,3,0,15,8);
			rom[adr+0*(rom_size/4)] = BITSWAP8(buf[i+0*(rom_size/4)], 2,3,4,0,7,5,1,6);
			rom[adr+1*(rom_size/4)] = BITSWAP8(buf[i+1*(rom_size/4)], 4,2,7,1,6,5,0,3);
			rom[adr+2*(rom_size/4)] = BITSWAP8(buf[i+2*(rom_size/4)], 1,4,7,0,3,5,6,2);
			rom[adr+3*(rom_size/4)] = BITSWAP8(buf[i+3*(rom_size/4)], 2,3,4,0,7,5,1,6);
	    }
	    free (buf);
	}
	
	static void pirates_decrypt_s(void)
	{
	    int rom_size;
	    UINT8 *buf, *rom;
	    int i;
	
	    rom_size = memory_region_length(REGION_GFX2);
	
	    buf = malloc(rom_size);
	
	    if (!buf) return;
	
	    rom = memory_region(REGION_GFX2);
	    memcpy (buf, rom, rom_size);
	
	    for (i=0; i<rom_size/4; i++)
	    {
			int adr = BITSWAP24(i,23,22,21,20,19,18,17,5,12,14,8,3,0,7,9,16,4,2,6,11,13,1,10,15);
			rom[adr+0*(rom_size/4)] = BITSWAP8(buf[i+0*(rom_size/4)], 4,2,7,1,6,5,0,3);
			rom[adr+1*(rom_size/4)] = BITSWAP8(buf[i+1*(rom_size/4)], 1,4,7,0,3,5,6,2);
			rom[adr+2*(rom_size/4)] = BITSWAP8(buf[i+2*(rom_size/4)], 2,3,4,0,7,5,1,6);
			rom[adr+3*(rom_size/4)] = BITSWAP8(buf[i+3*(rom_size/4)], 4,2,7,1,6,5,0,3);
	    }
	    free (buf);
	}
	
	
	static void pirates_decrypt_oki(void)
	{
	    int rom_size;
	    UINT8 *buf, *rom;
	    int i;
	
	    rom_size = memory_region_length(REGION_SOUND1);
	
	    buf = malloc(rom_size);
	
	    if (!buf) return;
	
	    rom = memory_region(REGION_SOUND1);
	    memcpy (buf, rom, rom_size);
	
	    for (i=0; i<rom_size; i++)
	    {
			int adr = BITSWAP24(i,23,22,21,20,19,10,16,13,8,4,7,11,14,17,12,6,2,0,5,18,15,3,1,9);
			rom[adr] = BITSWAP8(buf[i], 2,3,4,0,7,5,1,6);
	    }
	    free (buf);
	}
	
	
	public static DriverInitHandlerPtr init_pirates  = new DriverInitHandlerPtr() { public void handler(){
		data16_t *rom = (data16_t *)memory_region(REGION_CPU1);
	
		pirates_decrypt_68k();
		pirates_decrypt_p();
		pirates_decrypt_s();
		pirates_decrypt_oki();
	
		/* patch out protection check */
		rom[0x62c0/2] = 0x6006; // beq -> bra
	} };
	
	static READ16_HANDLER( genix_prot_r ) {	if(!offset)	return 0x0004; else	return 0x0000; }
	
	public static DriverInitHandlerPtr init_genix  = new DriverInitHandlerPtr() { public void handler(){
		pirates_decrypt_68k();
		pirates_decrypt_p();
		pirates_decrypt_s();
		pirates_decrypt_oki();
	
		/* If this value is increased then something has gone wrong and the protection failed */
		/* Write-protect it for now */
		install_mem_read16_handler (0, 0x109e98, 0x109e9b, genix_prot_r );
	} };
	
	
	/* GAME */
	
	GAME( 1994, pirates, 0, pirates, pirates, pirates, 0, "NIX", "Pirates" )
	GAME( 1994, genix,   0, pirates, pirates, genix,   0, "NIX", "Genix Family" )
}
