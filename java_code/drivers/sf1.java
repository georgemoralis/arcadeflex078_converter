/***************************************************************************

  Street Fighter 1

  driver by Olivier Galibert

TODO:
- is there a third coin input?

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.02
 */ 
package arcadeflex.v078.drivers;

public class sf1
{
	
	
	
	WRITE16_HANDLER( sf1_bg_scroll_w );
	WRITE16_HANDLER( sf1_fg_scroll_w );
	WRITE16_HANDLER( sf1_videoram_w );
	WRITE16_HANDLER( sf1_gfxctrl_w );
	
	
	static READ16_HANDLER( dummy_r )
	{
		return 0xffff;
	}
	
	
	static WRITE16_HANDLER( sf1_coin_w )
	{
		if (ACCESSING_LSB != 0)
		{
			coin_counter_w(0,data & 0x01);
			coin_counter_w(1,data & 0x02);
			coin_lockout_w(0,~data & 0x10);
			coin_lockout_w(1,~data & 0x20);
			coin_lockout_w(2,~data & 0x40);	/* is there a third coin input? */
		}
	}
	
	
	static WRITE16_HANDLER( soundcmd_w )
	{
		if (ACCESSING_LSB != 0)
		{
			soundlatch_w(offset,data & 0xff);
			cpu_set_irq_line(1,IRQ_LINE_NMI,PULSE_LINE);
		}
	}
	
	
	/* The protection of the japanese version */
	/* I'd love to see someone dump the 68705 rom */
	
	static void write_dword(offs_t offset,UINT32 data)
	{
		cpu_writemem24bew_word(offset,data >> 16);
		cpu_writemem24bew_word(offset+2,data);
	}
	
	static WRITE16_HANDLER( protection_w )
	{
		static int maplist[4][10] = {
			{ 1, 0, 3, 2, 4, 5, 6, 7, 8, 9 },
			{ 4, 5, 6, 7, 1, 0, 3, 2, 8, 9 },
			{ 3, 2, 1, 0, 6, 7, 4, 5, 8, 9 },
			{ 6, 7, 4, 5, 3, 2, 1, 0, 8, 9 }
		};
		int map;
	
		map = maplist
			[cpu_readmem24bew(0xffc006)]
			[(cpu_readmem24bew(0xffc003)<<1) + (cpu_readmem24bew_word(0xffc004)>>8)];
	
		switch(cpu_readmem24bew(0xffc684)) {
		case 1:
			{
				int base;
	
				base = 0x1b6e8+0x300e*map;
	
				write_dword(0xffc01c, 0x16bfc+0x270*map);
				write_dword(0xffc020, base+0x80);
				write_dword(0xffc024, base);
				write_dword(0xffc028, base+0x86);
				write_dword(0xffc02c, base+0x8e);
				write_dword(0xffc030, base+0x20e);
				write_dword(0xffc034, base+0x30e);
				write_dword(0xffc038, base+0x38e);
				write_dword(0xffc03c, base+0x40e);
				write_dword(0xffc040, base+0x80e);
				write_dword(0xffc044, base+0xc0e);
				write_dword(0xffc048, base+0x180e);
				write_dword(0xffc04c, base+0x240e);
				write_dword(0xffc050, 0x19548+0x60*map);
				write_dword(0xffc054, 0x19578+0x60*map);
				break;
			}
		case 2:
			{
				static int delta1[10] = {
					0x1f80, 0x1c80, 0x2700, 0x2400, 0x2b80, 0x2e80, 0x3300, 0x3600, 0x3a80, 0x3d80
				};
				static int delta2[10] = {
					0x2180, 0x1800, 0x3480, 0x2b00, 0x3e00, 0x4780, 0x5100, 0x5a80, 0x6400, 0x6d80
				};
	
				int d1 = delta1[map] + 0xc0;
				int d2 = delta2[map];
	
				cpu_writemem24bew_word(0xffc680, d1);
				cpu_writemem24bew_word(0xffc682, d2);
				cpu_writemem24bew_word(0xffc00c, 0xc0);
				cpu_writemem24bew_word(0xffc00e, 0);
	
				sf1_fg_scroll_w(0, d1, 0);
				sf1_bg_scroll_w(0, d2, 0);
				break;
			}
		case 4:
			{
				int pos = cpu_readmem24bew(0xffc010);
				pos = (pos+1) & 3;
				cpu_writemem24bew(0xffc010, pos);
				if (pos == 0) {
					int d1 = cpu_readmem24bew_word(0xffc682);
					int off = cpu_readmem24bew_word(0xffc00e);
					if(off!=512) {
						off++;
						d1++;
					} else {
						off = 0;
						d1 -= 512;
					}
					cpu_writemem24bew_word(0xffc682, d1);
					cpu_writemem24bew_word(0xffc00e, off);
					sf1_bg_scroll_w(0, d1, 0);
				}
				break;
			}
		default:
			{
				logerror("Write protection at %06x (%04x)\n", activecpu_get_pc(), data&0xffff);
				logerror("*** Unknown protection %d\n", cpu_readmem24bew(0xffc684));
				break;
			}
		}
	}
	
	
	/* The world version has analog buttons */
	/* We simulate them with 3 buttons the same way the other versions
	   internally do */
	
	static int scale[8] = { 0x00, 0x40, 0xe0, 0xfe, 0xfe, 0xfe, 0xfe, 0xfe };
	
	static READ16_HANDLER( button1_r )
	{
		return (scale[input_port_7_r(0)]<<8)|scale[input_port_5_r(0)];
	}
	
	static READ16_HANDLER( button2_r )
	{
		return (scale[input_port_8_r(0)]<<8)|scale[input_port_6_r(0)];
	}
	
	
	public static WriteHandlerPtr sound2_bank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cpu_setbank(1,memory_region(REGION_CPU3)+0x8000*(data+1));
	} };
	
	
	public static WriteHandlerPtr msm5205_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		MSM5205_reset_w(offset,(data>>7)&1);
		/* ?? bit 6?? */
		MSM5205_data_w(offset,data);
		MSM5205_vclk_w(offset,1);
		MSM5205_vclk_w(offset,0);
	} };
	
	
	
	static MEMORY_READ16_START( readmem )
		{ 0x000000, 0x04ffff, MRA16_ROM },
		{ 0x800000, 0x800fff, MRA16_RAM },
		{ 0xc00000, 0xc00001, input_port_3_word_r },
		{ 0xc00002, 0xc00003, input_port_4_word_r },
		{ 0xc00004, 0xc00005, button1_r },
		{ 0xc00006, 0xc00007, button2_r },
		{ 0xc00008, 0xc00009, input_port_0_word_r },
		{ 0xc0000a, 0xc0000b, input_port_1_word_r },
		{ 0xc0000c, 0xc0000d, input_port_2_word_r },
		{ 0xc0000e, 0xc0000f, dummy_r },
		{ 0xff8000, 0xffdfff, MRA16_RAM },
		{ 0xffe000, 0xffffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_READ16_START( readmemus )
		{ 0x000000, 0x04ffff, MRA16_ROM },
		{ 0x800000, 0x800fff, MRA16_RAM },
		{ 0xc00000, 0xc00001, input_port_3_word_r },
		{ 0xc00002, 0xc00003, input_port_4_word_r },
		{ 0xc00004, 0xc00005, dummy_r },
		{ 0xc00006, 0xc00007, dummy_r },
		{ 0xc00008, 0xc00009, input_port_0_word_r },
		{ 0xc0000a, 0xc0000b, input_port_1_word_r },
		{ 0xc0000c, 0xc0000d, input_port_2_word_r },
		{ 0xc0000e, 0xc0000f, dummy_r },
		{ 0xff8000, 0xffdfff, MRA16_RAM },
		{ 0xffe000, 0xffffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_READ16_START( readmemjp )
		{ 0x000000, 0x04ffff, MRA16_ROM },
		{ 0x800000, 0x800fff, MRA16_RAM },
		{ 0xc00000, 0xc00001, input_port_3_word_r },
		{ 0xc00002, 0xc00003, input_port_4_word_r },
		{ 0xc00004, 0xc00005, input_port_5_word_r },
		{ 0xc00006, 0xc00007, dummy_r },
		{ 0xc00008, 0xc00009, input_port_0_word_r },
		{ 0xc0000a, 0xc0000b, input_port_1_word_r },
		{ 0xc0000c, 0xc0000d, input_port_2_word_r },
		{ 0xc0000e, 0xc0000f, dummy_r },
		{ 0xff8000, 0xffdfff, MRA16_RAM },
		{ 0xffe000, 0xffffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( writemem )
		{ 0x000000, 0x04ffff, MWA16_ROM },
		{ 0x800000, 0x800fff, sf1_videoram_w, &sf1_videoram, &videoram_size },
		{ 0xb00000, 0xb007ff, paletteram16_xxxxRRRRGGGGBBBB_word_w, &paletteram16 },
		{ 0xc00010, 0xc00011, sf1_coin_w },
		{ 0xc00014, 0xc00015, sf1_fg_scroll_w },
		{ 0xc00018, 0xc00019, sf1_bg_scroll_w },
		{ 0xc0001a, 0xc0001b, sf1_gfxctrl_w },
		{ 0xc0001c, 0xc0001d, soundcmd_w },
		{ 0xc0001e, 0xc0001f, protection_w },
		{ 0xff8000, 0xffdfff, MWA16_RAM },
		{ 0xffe000, 0xffffff, MWA16_RAM, &sf1_objectram },
	MEMORY_END
	
	public static Memory_ReadAddress sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xc7ff, MRA_RAM ),
		new Memory_ReadAddress( 0xc800, 0xc800, soundlatch_r ),
		new Memory_ReadAddress( 0xe001, 0xe001, YM2151_status_port_0_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xc7ff, MWA_RAM ),
		new Memory_WriteAddress( 0xe000, 0xe000, YM2151_register_port_0_w ),
		new Memory_WriteAddress( 0xe001, 0xe001, YM2151_data_port_0_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_ReadAddress sound2_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0xffff, MRA_BANK1 ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	/* Yes, _no_ ram */
	public static Memory_WriteAddress sound2_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
	/*	new Memory_WriteAddress( 0x0000, 0xffff, MWA_ROM ), avoid cluttering up error.log */
		new Memory_WriteAddress( 0x0000, 0xffff, MWA_NOP ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort sound2_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x01, 0x01, soundlatch_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_WritePort sound2_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x01, msm5205_w ),
		new IO_WritePort( 0x02, 0x02, sound2_bank_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	
	static InputPortPtr input_ports_sf1 = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		PORT_DIPNAME( 0x0007, 0x0007, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0002, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0007, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0006, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0005, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0003, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x0038, 0x0038, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0038, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0030, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0028, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0018, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x0040, 0x0040, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0040, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080, 0x0080, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0080, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0100, 0x0100, DEF_STR( "Unknown") );	/* Flip Screen not available */
		PORT_DIPSETTING(      0x0100, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0200, 0x0200, "Attract Music" );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0200, DEF_STR( "On") );
		PORT_DIPNAME( 0x0400, 0x0400, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0400, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0800, 0x0800, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0800, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x1000, 0x1000, "Speed" );
		PORT_DIPSETTING(      0x0000, "Slow" );
		PORT_DIPSETTING(      0x1000, "Normal" );
		PORT_DIPNAME( 0x2000, 0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x4000, 0x4000, "Freeze" );
		PORT_DIPSETTING(      0x4000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_SERVICE( 0x8000, IP_ACTIVE_LOW );
	
		PORT_START(); 
		PORT_DIPNAME( 0x0007, 0x0007, "Continuation max stage" );
		PORT_DIPSETTING(      0x0007, "5th" );
		PORT_DIPSETTING(      0x0006, "4th" );
		PORT_DIPSETTING(      0x0005, "3rd" );
		PORT_DIPSETTING(      0x0004, "2nd" );
		PORT_DIPSETTING(      0x0003, "1st" );
		PORT_DIPSETTING(      0x0002, "No continuation" );
		PORT_DIPNAME( 0x0018, 0x0018, "Round time" );
		PORT_DIPSETTING(      0x0018, "100" );
		PORT_DIPSETTING(      0x0010, "150" );
		PORT_DIPSETTING(      0x0008, "200" );
		PORT_DIPSETTING(      0x0000, "250" );
		PORT_DIPNAME( 0x0060, 0x0060, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(      0x0060, "Normal" );
		PORT_DIPSETTING(      0x0040, "Easy" );
		PORT_DIPSETTING(      0x0020, "Difficult" );
		PORT_DIPSETTING(      0x0000, "Very difficult" );
		PORT_DIPNAME( 0x0380, 0x0380, "Buy-in max stage" );
		PORT_DIPSETTING(      0x0380, "5th" );
		PORT_DIPSETTING(      0x0300, "4th" );
		PORT_DIPSETTING(      0x0280, "3rd" );
		PORT_DIPSETTING(      0x0200, "2nd" );
		PORT_DIPSETTING(      0x0180, "1st" );
		PORT_DIPSETTING(      0x0080, "No buy-in" );
		PORT_DIPNAME( 0x0400, 0x0400, "Number of start countries" );
		PORT_DIPSETTING(      0x0400, "4" );
		PORT_DIPSETTING(      0x0000, "2" );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* Freezes the game ? */
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON5 | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON6 | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON5 | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON6 | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_sf1us = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		PORT_DIPNAME( 0x0007, 0x0007, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0002, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0007, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0006, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0005, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0003, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x0038, 0x0038, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0038, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0030, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0028, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0018, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x0040, 0x0040, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0040, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080, 0x0080, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0080, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0100, 0x0100, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(      0x0100, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0200, 0x0200, "Attract Music" );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0200, DEF_STR( "On") );
		PORT_DIPNAME( 0x0400, 0x0400, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0400, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0800, 0x0800, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0800, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x1000, 0x1000, "Speed" );
		PORT_DIPSETTING(      0x0000, "Slow" );
		PORT_DIPSETTING(      0x1000, "Normal" );
		PORT_DIPNAME( 0x2000, 0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x4000, 0x4000, "Freeze" );
		PORT_DIPSETTING(      0x4000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_SERVICE( 0x8000, IP_ACTIVE_LOW );
	
		PORT_START(); 
		PORT_DIPNAME( 0x0007, 0x0007, "Continuation max stage" );
		PORT_DIPSETTING(      0x0007, "5th" );
		PORT_DIPSETTING(      0x0006, "4th" );
		PORT_DIPSETTING(      0x0005, "3rd" );
		PORT_DIPSETTING(      0x0004, "2nd" );
		PORT_DIPSETTING(      0x0003, "1st" );
		PORT_DIPSETTING(      0x0002, "No continuation" );
		PORT_DIPNAME( 0x0018, 0x0018, "Round time" );
		PORT_DIPSETTING(      0x0018, "100" );
		PORT_DIPSETTING(      0x0010, "150" );
		PORT_DIPSETTING(      0x0008, "200" );
		PORT_DIPSETTING(      0x0000, "250" );
		PORT_DIPNAME( 0x0060, 0x0060, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(      0x0060, "Normal" );
		PORT_DIPSETTING(      0x0040, "Easy" );
		PORT_DIPSETTING(      0x0020, "Difficult" );
		PORT_DIPSETTING(      0x0000, "Very difficult" );
		PORT_DIPNAME( 0x0380, 0x0380, "Buy-in max stage" );
		PORT_DIPSETTING(      0x0380, "5th" );
		PORT_DIPSETTING(      0x0300, "4th" );
		PORT_DIPSETTING(      0x0280, "3rd" );
		PORT_DIPSETTING(      0x0200, "2nd" );
		PORT_DIPSETTING(      0x0180, "1st" );
		PORT_DIPSETTING(      0x0080, "No buy-in" );
		PORT_DIPNAME( 0x0400, 0x0000, "Number of start countries" );
		PORT_DIPSETTING(      0x0000, "4" );
		PORT_DIPSETTING(      0x0400, "2" );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* Freezes the game ? */
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_sf1jp = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		PORT_DIPNAME( 0x0007, 0x0007, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0002, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0007, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0006, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0005, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0003, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x0038, 0x0038, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0038, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0030, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0028, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0018, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x0040, 0x0040, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0040, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080, 0x0080, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0080, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0100, 0x0100, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(      0x0100, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0200, 0x0200, "Attract Music" );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0200, DEF_STR( "On") );
		PORT_DIPNAME( 0x0400, 0x0400, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0400, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0800, 0x0800, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0800, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x1000, 0x1000, "Speed" );
		PORT_DIPSETTING(      0x0000, "Slow" );
		PORT_DIPSETTING(      0x1000, "Normal" );
		PORT_DIPNAME( 0x2000, 0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x4000, 0x4000, "Freeze" );
		PORT_DIPSETTING(      0x4000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_SERVICE( 0x8000, IP_ACTIVE_LOW );
	
		PORT_START(); 
		PORT_DIPNAME( 0x0007, 0x0007, "Continuation max stage" );
		PORT_DIPSETTING(      0x0007, "5th" );
		PORT_DIPSETTING(      0x0006, "4th" );
		PORT_DIPSETTING(      0x0005, "3rd" );
		PORT_DIPSETTING(      0x0004, "2nd" );
		PORT_DIPSETTING(      0x0003, "1st" );
		PORT_DIPSETTING(      0x0002, "No continuation" );
		PORT_DIPNAME( 0x0018, 0x0018, "Round time" );
		PORT_DIPSETTING(      0x0018, "100" );
		PORT_DIPSETTING(      0x0010, "150" );
		PORT_DIPSETTING(      0x0008, "200" );
		PORT_DIPSETTING(      0x0000, "250" );
		PORT_DIPNAME( 0x0060, 0x0060, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(      0x0060, "Normal" );
		PORT_DIPSETTING(      0x0040, "Easy" );
		PORT_DIPSETTING(      0x0020, "Difficult" );
		PORT_DIPSETTING(      0x0000, "Very difficult" );
		PORT_DIPNAME( 0x0380, 0x0380, "Buy-in max stage" );
		PORT_DIPSETTING(      0x0380, "5th" );
		PORT_DIPSETTING(      0x0300, "4th" );
		PORT_DIPSETTING(      0x0280, "3rd" );
		PORT_DIPSETTING(      0x0200, "2nd" );
		PORT_DIPSETTING(      0x0180, "1st" );
		PORT_DIPSETTING(      0x0080, "No buy-in" );
		PORT_DIPNAME( 0x0400, 0x0000, "Number of start countries" );
		PORT_DIPSETTING(      0x0000, "4" );
		PORT_DIPSETTING(      0x0400, "2" );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* Freezes the game ? */
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER1 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER2 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout char_layout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		2,
		new int[] { 4, 0 },
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8
	);
	
	static GfxLayout sprite_layout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,2),
		4,
		new int[] { 4, 0, RGN_FRAC(1,2)+4, RGN_FRAC(1,2) },
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3,
				16*16+0, 16*16+1, 16*16+2, 16*16+3, 16*16+8+0, 16*16+8+1, 16*16+8+2, 16*16+8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
		64*8
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, sprite_layout,   0, 16 ),
		new GfxDecodeInfo( REGION_GFX2, 0, sprite_layout, 256, 16 ),
		new GfxDecodeInfo( REGION_GFX3, 0, sprite_layout, 512, 16 ),
		new GfxDecodeInfo( REGION_GFX4, 0, char_layout,   768, 16 ),
		new GfxDecodeInfo( -1 )
	};
	
	
	
	static void irq_handler(int irq)
	{
		cpu_set_irq_line(1,0,irq ? ASSERT_LINE : CLEAR_LINE);
	}
	
	static struct YM2151interface ym2151_interface =
	{
		1,	/* 1 chip */
		3579545,	/* ? xtal is 3.579545MHz */
		{ YM3012_VOL(60,MIXER_PAN_LEFT,60,MIXER_PAN_RIGHT) },
		{ irq_handler }
	};
	
	static struct MSM5205interface msm5205_interface =
	{
		2,		/* 2 chips */
		384000,				/* 384KHz ?           */
		{ 0, 0 },/* interrupt function */
		{ MSM5205_SEX_4B,MSM5205_SEX_4B},	/* 8KHz playback ?    */
		{ 100, 100 }
	};
	
	public static MachineHandlerPtr machine_driver_sf1 = new MachineHandlerPtr() {
        public void handler(InternalMachineDriver machine) {
	
		/* basic machine hardware */
		MDRV_CPU_ADD_TAG("main", M68000, 8000000)	/* 8 MHz ? (xtal is 16MHz) */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_VBLANK_INT(irq1_line_hold,1)
	
		MDRV_CPU_ADD(Z80, 3579545)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)	/* ? xtal is 3.579545MHz */
		MDRV_CPU_MEMORY(sound_readmem,sound_writemem)
									/* NMIs are caused by the main CPU */
		MDRV_CPU_ADD(Z80, 3579545)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)	/* ? xtal is 3.579545MHz */
		MDRV_CPU_MEMORY(sound2_readmem,sound2_writemem)
		MDRV_CPU_PORTS(sound2_readport,sound2_writeport)
		MDRV_CPU_PERIODIC_INT(irq0_line_hold,8000)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(64*8, 32*8)
		MDRV_VISIBLE_AREA(8*8, (64-8)*8-1, 2*8, 30*8-1 )
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(sf1)
		MDRV_VIDEO_UPDATE(sf1)
	
		/* sound hardware */
		MDRV_SOUND_ATTRIBUTES(SOUND_SUPPORTS_STEREO)
		MDRV_SOUND_ADD(YM2151, ym2151_interface)
		MDRV_SOUND_ADD(MSM5205, msm5205_interface)
	MACHINE_DRIVER_END();
 }
};
	
	
	public static MachineHandlerPtr machine_driver_sf1us = new MachineHandlerPtr() {
        public void handler(InternalMachineDriver machine) {
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(sf1)
		MDRV_CPU_MODIFY("main")
		MDRV_CPU_MEMORY(readmemus,writemem)
	MACHINE_DRIVER_END();
 }
};
	
	
	public static MachineHandlerPtr machine_driver_sf1jp = new MachineHandlerPtr() {
        public void handler(InternalMachineDriver machine) {
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(sf1)
		MDRV_CPU_MODIFY("main")
		MDRV_CPU_MEMORY(readmemjp,writemem)
	MACHINE_DRIVER_END();
 }
};
	
	
	public static MachineHandlerPtr machine_driver_sf1p = new MachineHandlerPtr() {
        public void handler(InternalMachineDriver machine) {
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(sf1)
		MDRV_CPU_MODIFY("main")
		MDRV_CPU_VBLANK_INT(irq6_line_hold,1)
	MACHINE_DRIVER_END();
 }
};
	
	
	static RomLoadPtr rom_sf1 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x60000, REGION_CPU1, 0 )
		ROM_LOAD16_BYTE("sfe-19", 0x00000, 0x10000, CRC(8346c3ca) SHA1(404e26d210e453ef0f03b092d70c770106eed1d1) )
		ROM_LOAD16_BYTE("sfe-22", 0x00001, 0x10000, CRC(3a4bfaa8) SHA1(6a6fc8d967838eca7d2973de987bb350c25628d5) )
		ROM_LOAD16_BYTE("sfe-20", 0x20000, 0x10000, CRC(b40e67ee) SHA1(394987dc4c306351b1657d10528ecb665700c4db) )
		ROM_LOAD16_BYTE("sfe-23", 0x20001, 0x10000, CRC(477c3d5b) SHA1(6443334b3546550e5d97cf4057b279ec7b3cd758) )
		ROM_LOAD16_BYTE("sfe-21", 0x40000, 0x10000, CRC(2547192b) SHA1(aaf07c613a6c42ec1dc82ffa86d00044b4ea27fc) )
		ROM_LOAD16_BYTE("sfe-24", 0x40001, 0x10000, CRC(79680f4e) SHA1(df596fa5b49a336fe462c2be7b454e695f5382db) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the music CPU */
		ROM_LOAD( "sf-02.bin", 0x0000, 0x8000, CRC(4a9ac534) SHA1(933645f8db4756aa2a35a843c3ac6f93cb8d565d) )
	
		ROM_REGION( 0x40000, REGION_CPU3, 0 )	/* 256k for the samples CPU */
		ROM_LOAD( "sfu-00",    0x00000, 0x20000, CRC(a7cce903) SHA1(76f521c9a00abd95a3491ab95e8eccd0fc7ea0e5) )
		ROM_LOAD( "sf-01.bin", 0x20000, 0x20000, CRC(86e0f0d5) SHA1(7cef8056f83dac15f1b47d7be705d26170858337) )
	
		ROM_REGION( 0x080000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "sf-39.bin", 0x000000, 0x020000, CRC(cee3d292) SHA1(a8c22f1dc81976e8dd5d6c70361c61fa3f9f89d6) ) /* Background b planes 0-1*/
		ROM_LOAD( "sf-38.bin", 0x020000, 0x020000, CRC(2ea99676) SHA1(5f3eb77e75f0ee27fb8fc7bab2819b3fdd480206) )
		ROM_LOAD( "sf-41.bin", 0x040000, 0x020000, CRC(e0280495) SHA1(e52c79feed590535b9a0b71ccadd0ed27d04ff45) ) /* planes 2-3 */
		ROM_LOAD( "sf-40.bin", 0x060000, 0x020000, CRC(c70b30de) SHA1(26112ee1720b6ad0e2e29e2d25ee2ec76fca0e3a) )
	
		ROM_REGION( 0x100000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "sf-25.bin", 0x000000, 0x020000, CRC(7f23042e) SHA1(a355fd7047fb1a71ab5cd08e1afd82c2558494c1) )	/* Background m planes 0-1 */
		ROM_LOAD( "sf-28.bin", 0x020000, 0x020000, CRC(92f8b91c) SHA1(6d958bc45131810d7b0af02be939ce37a39c35e8) )
		ROM_LOAD( "sf-30.bin", 0x040000, 0x020000, CRC(b1399856) SHA1(7c956d49b2e73291182ea1ec4cebd3411d1322a1) )
		ROM_LOAD( "sf-34.bin", 0x060000, 0x020000, CRC(96b6ae2e) SHA1(700e050463b7a29a1eb08007a2add045afdcd8a0) )
		ROM_LOAD( "sf-26.bin", 0x080000, 0x020000, CRC(54ede9f5) SHA1(c2cb354a6b32047759945fa3ecafc70ba7d1dda1) ) /* planes 2-3 */
		ROM_LOAD( "sf-29.bin", 0x0a0000, 0x020000, CRC(f0649a67) SHA1(eeda256527f7a2ee2d5e0688c505a01de548bc54) )
		ROM_LOAD( "sf-31.bin", 0x0c0000, 0x020000, CRC(8f4dd71a) SHA1(28b82c540df04c91a2dd6cbbc9a95bbebda6643b) )
		ROM_LOAD( "sf-35.bin", 0x0e0000, 0x020000, CRC(70c00fb4) SHA1(7c5504a5aedd3be7b663c5090eb22243e3fa669b) )
	
		ROM_REGION( 0x1c0000, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "sf-15.bin", 0x000000, 0x020000, CRC(fc0113db) SHA1(7c19603129be5f6e1ccd07fd8b7ee1cbf86468db) ) /* Sprites planes 1-2 */
		ROM_LOAD( "sf-16.bin", 0x020000, 0x020000, CRC(82e4a6d3) SHA1(5ec519c2740c66f5da27ced1db99e19fe38fdad7) )
		ROM_LOAD( "sf-11.bin", 0x040000, 0x020000, CRC(e112df1b) SHA1(3f9856f69b457d79fe085bf51dfb2efcd98f883d) )
		ROM_LOAD( "sf-12.bin", 0x060000, 0x020000, CRC(42d52299) SHA1(6560c38f5fd5a47db7728cc7df83d2169157174f) )
		ROM_LOAD( "sf-07.bin", 0x080000, 0x020000, CRC(49f340d9) SHA1(65822efefa198791a632ef851a5ce06a71b4ed0f) )
		ROM_LOAD( "sf-08.bin", 0x0a0000, 0x020000, CRC(95ece9b1) SHA1(f0a15fce5cd9617fa5d4dd43bd5b6ea190dace85) )
		ROM_LOAD( "sf-03.bin", 0x0c0000, 0x020000, CRC(5ca05781) SHA1(004f5ad34798471b39bd4612c797f0913ed0fb4a) )
		ROM_LOAD( "sf-17.bin", 0x0e0000, 0x020000, CRC(69fac48e) SHA1(c9272217256c73cb8ddb4fbbfb5905ce1122c746) ) /* planes 2-3 */
		ROM_LOAD( "sf-18.bin", 0x100000, 0x020000, CRC(71cfd18d) SHA1(4c17e2124f3456d6b13ede8ad3ae916b53f9bb7e) )
		ROM_LOAD( "sf-13.bin", 0x120000, 0x020000, CRC(fa2eb24b) SHA1(96f3bd54c340771577cc232ebde93965421f2557) )
		ROM_LOAD( "sf-14.bin", 0x140000, 0x020000, CRC(ad955c95) SHA1(549d6a5125432aa45d03f15e76f6c2c8ab2e05a3) )
		ROM_LOAD( "sf-09.bin", 0x160000, 0x020000, CRC(41b73a31) SHA1(aaa7a53e29fe23a1ca8ec4430f7efcbd774a8cbf) )
		ROM_LOAD( "sf-10.bin", 0x180000, 0x020000, CRC(91c41c50) SHA1(b03fb9b3c553fb4aae45ad6997eeb7bb95fdcce3) )
		ROM_LOAD( "sf-05.bin", 0x1a0000, 0x020000, CRC(538c7cbe) SHA1(f030a9562fbb93d1534b91343ca3f429cdbd0136) )
	
		ROM_REGION( 0x004000, REGION_GFX4, ROMREGION_DISPOSE )
		ROM_LOAD( "sf-27.bin", 0x000000, 0x004000, CRC(2b09b36d) SHA1(9fe1dd3a9396fbb06f30247cfe526653553beca1) ) /* Characters planes 1-2 */
	
		ROM_REGION( 0x40000, REGION_GFX5, 0 )	/* background tilemaps */
		ROM_LOAD( "sf-37.bin", 0x000000, 0x010000, CRC(23d09d3d) SHA1(a0c71abc49c5fe59487a63b502e3d03021bfef13) )
		ROM_LOAD( "sf-36.bin", 0x010000, 0x010000, CRC(ea16df6c) SHA1(68709a314b775c500817fc17d40a80204b2ae06c) )
		ROM_LOAD( "sf-32.bin", 0x020000, 0x010000, CRC(72df2bd9) SHA1(9a0da618139673738b6b3302207255e44c5491a2) )
		ROM_LOAD( "sf-33.bin", 0x030000, 0x010000, CRC(3e99d3d5) SHA1(9168a977e80f8c23c6126b9e64eb176290cf941a) )
	
		ROM_REGION( 0x0320, REGION_PROMS, 0 )
		ROM_LOAD( "mb7114h.12k",  0x0000, 0x0100, CRC(75af3553) SHA1(14da009592877a6097b34ea844fa897ceda7465e) )	/* unknown */
		ROM_LOAD( "mb7114h.11h",  0x0100, 0x0100, CRC(c0e56586) SHA1(2abf93aef48af34f869b30f63c130513a97f86a3) )	/* unknown */
		ROM_LOAD( "mb7114h.12j",  0x0200, 0x0100, CRC(4c734b64) SHA1(7a122b643bad3e3586821980efff023a63e5a029) )	/* unknown */
		ROM_LOAD( "mmi-7603.13h", 0x0300, 0x0020, CRC(06bcda53) SHA1(fa69b77697bb12aa6012d82ef5b504d3a1d20232) )	/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_sf1us = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x60000, REGION_CPU1, 0 )
		ROM_LOAD16_BYTE("sfd-19", 0x00000, 0x10000, CRC(faaf6255) SHA1(f6d0186c6109780839576c141fc6b557c170c182) )
		ROM_LOAD16_BYTE("sfd-22", 0x00001, 0x10000, CRC(e1fe3519) SHA1(5c59343a8acaaa4f36636d8e28a4ca7854110dad) )
		ROM_LOAD16_BYTE("sfd-20", 0x20000, 0x10000, CRC(44b915bd) SHA1(85772fb89712f97bb0489a7e43f8b1a5037c8081) )
		ROM_LOAD16_BYTE("sfd-23", 0x20001, 0x10000, CRC(79c43ff8) SHA1(450fb75b6f36e08788d7a806122e4e1b0a87746c) )
		ROM_LOAD16_BYTE("sfd-21", 0x40000, 0x10000, CRC(e8db799b) SHA1(8443ba6a9b9ad29d5985d434658e685fd46d8f1e) )
		ROM_LOAD16_BYTE("sfd-24", 0x40001, 0x10000, CRC(466a3440) SHA1(689823763bfdbc12ac11ff176acfd22f352e2658) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the music CPU */
		ROM_LOAD( "sf-02.bin", 0x0000, 0x8000, CRC(4a9ac534) SHA1(933645f8db4756aa2a35a843c3ac6f93cb8d565d) )
	
		ROM_REGION( 0x40000, REGION_CPU3, 0 )	/* 256k for the samples CPU */
		ROM_LOAD( "sfu-00",    0x00000, 0x20000, CRC(a7cce903) SHA1(76f521c9a00abd95a3491ab95e8eccd0fc7ea0e5) )
		ROM_LOAD( "sf-01.bin", 0x20000, 0x20000, CRC(86e0f0d5) SHA1(7cef8056f83dac15f1b47d7be705d26170858337) )
	
		ROM_REGION( 0x080000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "sf-39.bin", 0x000000, 0x020000, CRC(cee3d292) SHA1(a8c22f1dc81976e8dd5d6c70361c61fa3f9f89d6) ) /* Background b planes 0-1*/
		ROM_LOAD( "sf-38.bin", 0x020000, 0x020000, CRC(2ea99676) SHA1(5f3eb77e75f0ee27fb8fc7bab2819b3fdd480206) )
		ROM_LOAD( "sf-41.bin", 0x040000, 0x020000, CRC(e0280495) SHA1(e52c79feed590535b9a0b71ccadd0ed27d04ff45) ) /* planes 2-3 */
		ROM_LOAD( "sf-40.bin", 0x060000, 0x020000, CRC(c70b30de) SHA1(26112ee1720b6ad0e2e29e2d25ee2ec76fca0e3a) )
	
		ROM_REGION( 0x100000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "sf-25.bin", 0x000000, 0x020000, CRC(7f23042e) SHA1(a355fd7047fb1a71ab5cd08e1afd82c2558494c1) )	/* Background m planes 0-1 */
		ROM_LOAD( "sf-28.bin", 0x020000, 0x020000, CRC(92f8b91c) SHA1(6d958bc45131810d7b0af02be939ce37a39c35e8) )
		ROM_LOAD( "sf-30.bin", 0x040000, 0x020000, CRC(b1399856) SHA1(7c956d49b2e73291182ea1ec4cebd3411d1322a1) )
		ROM_LOAD( "sf-34.bin", 0x060000, 0x020000, CRC(96b6ae2e) SHA1(700e050463b7a29a1eb08007a2add045afdcd8a0) )
		ROM_LOAD( "sf-26.bin", 0x080000, 0x020000, CRC(54ede9f5) SHA1(c2cb354a6b32047759945fa3ecafc70ba7d1dda1) ) /* planes 2-3 */
		ROM_LOAD( "sf-29.bin", 0x0a0000, 0x020000, CRC(f0649a67) SHA1(eeda256527f7a2ee2d5e0688c505a01de548bc54) )
		ROM_LOAD( "sf-31.bin", 0x0c0000, 0x020000, CRC(8f4dd71a) SHA1(28b82c540df04c91a2dd6cbbc9a95bbebda6643b) )
		ROM_LOAD( "sf-35.bin", 0x0e0000, 0x020000, CRC(70c00fb4) SHA1(7c5504a5aedd3be7b663c5090eb22243e3fa669b) )
	
		ROM_REGION( 0x1c0000, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "sf-15.bin", 0x000000, 0x020000, CRC(fc0113db) SHA1(7c19603129be5f6e1ccd07fd8b7ee1cbf86468db) ) /* Sprites planes 1-2 */
		ROM_LOAD( "sf-16.bin", 0x020000, 0x020000, CRC(82e4a6d3) SHA1(5ec519c2740c66f5da27ced1db99e19fe38fdad7) )
		ROM_LOAD( "sf-11.bin", 0x040000, 0x020000, CRC(e112df1b) SHA1(3f9856f69b457d79fe085bf51dfb2efcd98f883d) )
		ROM_LOAD( "sf-12.bin", 0x060000, 0x020000, CRC(42d52299) SHA1(6560c38f5fd5a47db7728cc7df83d2169157174f) )
		ROM_LOAD( "sf-07.bin", 0x080000, 0x020000, CRC(49f340d9) SHA1(65822efefa198791a632ef851a5ce06a71b4ed0f) )
		ROM_LOAD( "sf-08.bin", 0x0a0000, 0x020000, CRC(95ece9b1) SHA1(f0a15fce5cd9617fa5d4dd43bd5b6ea190dace85) )
		ROM_LOAD( "sf-03.bin", 0x0c0000, 0x020000, CRC(5ca05781) SHA1(004f5ad34798471b39bd4612c797f0913ed0fb4a) )
		ROM_LOAD( "sf-17.bin", 0x0e0000, 0x020000, CRC(69fac48e) SHA1(c9272217256c73cb8ddb4fbbfb5905ce1122c746) ) /* planes 2-3 */
		ROM_LOAD( "sf-18.bin", 0x100000, 0x020000, CRC(71cfd18d) SHA1(4c17e2124f3456d6b13ede8ad3ae916b53f9bb7e) )
		ROM_LOAD( "sf-13.bin", 0x120000, 0x020000, CRC(fa2eb24b) SHA1(96f3bd54c340771577cc232ebde93965421f2557) )
		ROM_LOAD( "sf-14.bin", 0x140000, 0x020000, CRC(ad955c95) SHA1(549d6a5125432aa45d03f15e76f6c2c8ab2e05a3) )
		ROM_LOAD( "sf-09.bin", 0x160000, 0x020000, CRC(41b73a31) SHA1(aaa7a53e29fe23a1ca8ec4430f7efcbd774a8cbf) )
		ROM_LOAD( "sf-10.bin", 0x180000, 0x020000, CRC(91c41c50) SHA1(b03fb9b3c553fb4aae45ad6997eeb7bb95fdcce3) )
		ROM_LOAD( "sf-05.bin", 0x1a0000, 0x020000, CRC(538c7cbe) SHA1(f030a9562fbb93d1534b91343ca3f429cdbd0136) )
	
		ROM_REGION( 0x004000, REGION_GFX4, ROMREGION_DISPOSE )
		ROM_LOAD( "sf-27.bin", 0x000000, 0x004000, CRC(2b09b36d) SHA1(9fe1dd3a9396fbb06f30247cfe526653553beca1) ) /* Characters planes 1-2 */
	
		ROM_REGION( 0x40000, REGION_GFX5, 0 )	/* background tilemaps */
		ROM_LOAD( "sf-37.bin", 0x000000, 0x010000, CRC(23d09d3d) SHA1(a0c71abc49c5fe59487a63b502e3d03021bfef13) )
		ROM_LOAD( "sf-36.bin", 0x010000, 0x010000, CRC(ea16df6c) SHA1(68709a314b775c500817fc17d40a80204b2ae06c) )
		ROM_LOAD( "sf-32.bin", 0x020000, 0x010000, CRC(72df2bd9) SHA1(9a0da618139673738b6b3302207255e44c5491a2) )
		ROM_LOAD( "sf-33.bin", 0x030000, 0x010000, CRC(3e99d3d5) SHA1(9168a977e80f8c23c6126b9e64eb176290cf941a) )
	
		ROM_REGION( 0x0320, REGION_PROMS, 0 )
		ROM_LOAD( "mb7114h.12k",  0x0000, 0x0100, CRC(75af3553) SHA1(14da009592877a6097b34ea844fa897ceda7465e) )	/* unknown */
		ROM_LOAD( "mb7114h.11h",  0x0100, 0x0100, CRC(c0e56586) SHA1(2abf93aef48af34f869b30f63c130513a97f86a3) )	/* unknown */
		ROM_LOAD( "mb7114h.12j",  0x0200, 0x0100, CRC(4c734b64) SHA1(7a122b643bad3e3586821980efff023a63e5a029) )	/* unknown */
		ROM_LOAD( "mmi-7603.13h", 0x0300, 0x0020, CRC(06bcda53) SHA1(fa69b77697bb12aa6012d82ef5b504d3a1d20232) )	/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_sf1jp = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x60000, REGION_CPU1, 0 )
		ROM_LOAD16_BYTE("sf-19.bin", 0x00000, 0x10000, CRC(116027d7) SHA1(6bcb117ee415aff4d8ea962d4eff4088ca94c251) )
		ROM_LOAD16_BYTE("sf-22.bin", 0x00001, 0x10000, CRC(d3cbd09e) SHA1(7274c603100132102de09e10d2129cfeb6c06369) )
		ROM_LOAD16_BYTE("sf-20.bin", 0x20000, 0x10000, CRC(fe07e83f) SHA1(252dd592c31e594103ac1eabd734d10748655701) )
		ROM_LOAD16_BYTE("sf-23.bin", 0x20001, 0x10000, CRC(1e435d33) SHA1(2022a4368aa63cb036e77cb5739810030db469ff) )
		ROM_LOAD16_BYTE("sf-21.bin", 0x40000, 0x10000, CRC(e086bc4c) SHA1(782134978ff0a7133768d9cc8050bc3b5016580b) )
		ROM_LOAD16_BYTE("sf-24.bin", 0x40001, 0x10000, CRC(13a6696b) SHA1(c01f9b700928e427bc9914c61beeaa6bcbde4546) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the music CPU */
		ROM_LOAD( "sf-02.bin", 0x0000, 0x8000, CRC(4a9ac534) SHA1(933645f8db4756aa2a35a843c3ac6f93cb8d565d) )
	
		ROM_REGION( 0x40000, REGION_CPU3, 0 )	/* 256k for the samples CPU */
		ROM_LOAD( "sf-00.bin", 0x00000, 0x20000, CRC(4b733845) SHA1(f7ff46e02f8ce6682d6e573588271bae2edfa90f) )
		ROM_LOAD( "sf-01.bin", 0x20000, 0x20000, CRC(86e0f0d5) SHA1(7cef8056f83dac15f1b47d7be705d26170858337) )
	
		ROM_REGION( 0x080000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "sf-39.bin", 0x000000, 0x020000, CRC(cee3d292) SHA1(a8c22f1dc81976e8dd5d6c70361c61fa3f9f89d6) ) /* Background b planes 0-1*/
		ROM_LOAD( "sf-38.bin", 0x020000, 0x020000, CRC(2ea99676) SHA1(5f3eb77e75f0ee27fb8fc7bab2819b3fdd480206) )
		ROM_LOAD( "sf-41.bin", 0x040000, 0x020000, CRC(e0280495) SHA1(e52c79feed590535b9a0b71ccadd0ed27d04ff45) ) /* planes 2-3 */
		ROM_LOAD( "sf-40.bin", 0x060000, 0x020000, CRC(c70b30de) SHA1(26112ee1720b6ad0e2e29e2d25ee2ec76fca0e3a) )
	
		ROM_REGION( 0x100000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "sf-25.bin", 0x000000, 0x020000, CRC(7f23042e) SHA1(a355fd7047fb1a71ab5cd08e1afd82c2558494c1) )	/* Background m planes 0-1 */
		ROM_LOAD( "sf-28.bin", 0x020000, 0x020000, CRC(92f8b91c) SHA1(6d958bc45131810d7b0af02be939ce37a39c35e8) )
		ROM_LOAD( "sf-30.bin", 0x040000, 0x020000, CRC(b1399856) SHA1(7c956d49b2e73291182ea1ec4cebd3411d1322a1) )
		ROM_LOAD( "sf-34.bin", 0x060000, 0x020000, CRC(96b6ae2e) SHA1(700e050463b7a29a1eb08007a2add045afdcd8a0) )
		ROM_LOAD( "sf-26.bin", 0x080000, 0x020000, CRC(54ede9f5) SHA1(c2cb354a6b32047759945fa3ecafc70ba7d1dda1) ) /* planes 2-3 */
		ROM_LOAD( "sf-29.bin", 0x0a0000, 0x020000, CRC(f0649a67) SHA1(eeda256527f7a2ee2d5e0688c505a01de548bc54) )
		ROM_LOAD( "sf-31.bin", 0x0c0000, 0x020000, CRC(8f4dd71a) SHA1(28b82c540df04c91a2dd6cbbc9a95bbebda6643b) )
		ROM_LOAD( "sf-35.bin", 0x0e0000, 0x020000, CRC(70c00fb4) SHA1(7c5504a5aedd3be7b663c5090eb22243e3fa669b) )
	
		ROM_REGION( 0x1c0000, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "sf-15.bin", 0x000000, 0x020000, CRC(fc0113db) SHA1(7c19603129be5f6e1ccd07fd8b7ee1cbf86468db) ) /* Sprites planes 1-2 */
		ROM_LOAD( "sf-16.bin", 0x020000, 0x020000, CRC(82e4a6d3) SHA1(5ec519c2740c66f5da27ced1db99e19fe38fdad7) )
		ROM_LOAD( "sf-11.bin", 0x040000, 0x020000, CRC(e112df1b) SHA1(3f9856f69b457d79fe085bf51dfb2efcd98f883d) )
		ROM_LOAD( "sf-12.bin", 0x060000, 0x020000, CRC(42d52299) SHA1(6560c38f5fd5a47db7728cc7df83d2169157174f) )
		ROM_LOAD( "sf-07.bin", 0x080000, 0x020000, CRC(49f340d9) SHA1(65822efefa198791a632ef851a5ce06a71b4ed0f) )
		ROM_LOAD( "sf-08.bin", 0x0a0000, 0x020000, CRC(95ece9b1) SHA1(f0a15fce5cd9617fa5d4dd43bd5b6ea190dace85) )
		ROM_LOAD( "sf-03.bin", 0x0c0000, 0x020000, CRC(5ca05781) SHA1(004f5ad34798471b39bd4612c797f0913ed0fb4a) )
		ROM_LOAD( "sf-17.bin", 0x0e0000, 0x020000, CRC(69fac48e) SHA1(c9272217256c73cb8ddb4fbbfb5905ce1122c746) ) /* planes 2-3 */
		ROM_LOAD( "sf-18.bin", 0x100000, 0x020000, CRC(71cfd18d) SHA1(4c17e2124f3456d6b13ede8ad3ae916b53f9bb7e) )
		ROM_LOAD( "sf-13.bin", 0x120000, 0x020000, CRC(fa2eb24b) SHA1(96f3bd54c340771577cc232ebde93965421f2557) )
		ROM_LOAD( "sf-14.bin", 0x140000, 0x020000, CRC(ad955c95) SHA1(549d6a5125432aa45d03f15e76f6c2c8ab2e05a3) )
		ROM_LOAD( "sf-09.bin", 0x160000, 0x020000, CRC(41b73a31) SHA1(aaa7a53e29fe23a1ca8ec4430f7efcbd774a8cbf) )
		ROM_LOAD( "sf-10.bin", 0x180000, 0x020000, CRC(91c41c50) SHA1(b03fb9b3c553fb4aae45ad6997eeb7bb95fdcce3) )
		ROM_LOAD( "sf-05.bin", 0x1a0000, 0x020000, CRC(538c7cbe) SHA1(f030a9562fbb93d1534b91343ca3f429cdbd0136) )
	
		ROM_REGION( 0x004000, REGION_GFX4, ROMREGION_DISPOSE )
		ROM_LOAD( "sf-27.bin", 0x000000, 0x004000, CRC(2b09b36d) SHA1(9fe1dd3a9396fbb06f30247cfe526653553beca1) ) /* Characters planes 1-2 */
	
		ROM_REGION( 0x40000, REGION_GFX5, 0 )	/* background tilemaps */
		ROM_LOAD( "sf-37.bin", 0x000000, 0x010000, CRC(23d09d3d) SHA1(a0c71abc49c5fe59487a63b502e3d03021bfef13) )
		ROM_LOAD( "sf-36.bin", 0x010000, 0x010000, CRC(ea16df6c) SHA1(68709a314b775c500817fc17d40a80204b2ae06c) )
		ROM_LOAD( "sf-32.bin", 0x020000, 0x010000, CRC(72df2bd9) SHA1(9a0da618139673738b6b3302207255e44c5491a2) )
		ROM_LOAD( "sf-33.bin", 0x030000, 0x010000, CRC(3e99d3d5) SHA1(9168a977e80f8c23c6126b9e64eb176290cf941a) )
	
		ROM_REGION( 0x0320, REGION_PROMS, 0 )
		ROM_LOAD( "sfb05.bin",    0x0000, 0x0100, CRC(864199ad) SHA1(b777df20b19fa7b7536120191df1875101e9d7af) )	/* unknown */
		ROM_LOAD( "sfb00.bin",    0x0100, 0x0100, CRC(bd3f8c5d) SHA1(c31ee9f466f05a21612f5ea29fb8c7c25dc9e011) )	/* unknown */
		ROM_LOAD( "mb7114h.12j",  0x0200, 0x0100, CRC(4c734b64) SHA1(7a122b643bad3e3586821980efff023a63e5a029) )	/* unknown */
		ROM_LOAD( "mmi-7603.13h", 0x0300, 0x0020, CRC(06bcda53) SHA1(fa69b77697bb12aa6012d82ef5b504d3a1d20232) )	/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_sf1p = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x60000, REGION_CPU1, 0 )
		ROM_LOAD16_BYTE("prg8.2a", 0x00000, 0x20000, CRC(d48d06a3) SHA1(d899771c66c1e7a5caa11f67a1122adb6f0f4d28) )
		ROM_LOAD16_BYTE("prg0.2c", 0x00001, 0x20000, CRC(e8606c1a) SHA1(be94203cba733e337993e6f386ff5ce1e76d8913) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the music CPU */
		ROM_LOAD( "sound.9j", 0x0000, 0x8000, CRC(43cd32ae) SHA1(42e59becde5761eb5d5bc310d2bc690f6f16882a) )
	
		ROM_REGION( 0x10000, REGION_CPU3, 0 )	/* 256k for the samples CPU */
		ROM_LOAD( "voice.1g", 0x00000, 0x10000, CRC(3f23c180) SHA1(fb4e3bb835d94a733eacc0b1df9fe19fa1120997) )
	
		ROM_REGION( 0x080000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "bkchr.2k", 0x000000, 0x020000, CRC(e4d47aca) SHA1(597ed03e5c8328ec7209282247080c171eaedf86) ) /* Background b planes 0-1*/
		ROM_LOAD( "bkchr.1k", 0x020000, 0x020000, CRC(5a1cbc1b) SHA1(ad7bf117a7d1c0ef2aa47e133b0889092a009ae5) )
		ROM_LOAD( "bkchr.4k", 0x040000, 0x020000, CRC(c351bd48) SHA1(58131974d378a91f03f8c0bbd2ea384bd4fe501a) ) /* planes 2-3 */
		ROM_LOAD( "bkchr.3k", 0x060000, 0x020000, CRC(6bb2b050) SHA1(d36419dabdc0a90b76e295b746928d9e1e69674a) )
	
		ROM_REGION( 0x100000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "mchr.1d", 0x000000, 0x020000, CRC(ab06a60b) SHA1(44febaa2ac8f060ed297b69af1fd258164ff565d) )	/* Background m planes 0-1 */
		ROM_LOAD( "mchr.1e", 0x020000, 0x020000, CRC(d221387d) SHA1(012dc8c646a6a4b8bf905d859e3465b4bcaaed67) )
		ROM_LOAD( "mchr.1g", 0x040000, 0x020000, CRC(1e4c1712) SHA1(543b47a865d11dd91331c0236c5578dbe7549881) )
		ROM_LOAD( "mchr.1h", 0x060000, 0x020000, CRC(a381f529) SHA1(7e427894f8440c23c92ce5d1f118b7a1d70b0282) )
		ROM_LOAD( "mchr.2d", 0x080000, 0x020000, CRC(e52303c4) SHA1(1ae4979c53e589d9a5e7c0dbbf33b980d10274ac) ) /* planes 2-3 */
		ROM_LOAD( "mchr.2e", 0x0a0000, 0x020000, CRC(23b9a6a1) SHA1(bf7f67d97cfaa1f4c78f290c7c18e099566709c7) )
		ROM_LOAD( "mchr.2g", 0x0c0000, 0x020000, CRC(1283ac09) SHA1(229a507e0a1c46b451d8879e690e8557d21d588d) )
		ROM_LOAD( "mchr.2h", 0x0e0000, 0x020000, CRC(cc6bf05c) SHA1(4e83dd55c88d5b539ab1dcae5bfd16195bcd2565) )
	
		ROM_REGION( 0xc0000, REGION_GFX3, ROMREGION_DISPOSE )
		ROM_LOAD( "b1m.bin", 0x000000, 0x010000, CRC(64758232) SHA1(20d21677b791a7f96afed54b286ee92adb80456d) ) /* Sprites planes 1-2 */
		ROM_LOAD( "b2m.bin", 0x010000, 0x010000, CRC(d958f5ad) SHA1(0e5c98a24814f5e1e6346dba4cfbd3a3a72ed724) )
		ROM_LOAD( "b1k.bin", 0x020000, 0x010000, CRC(e766f5fe) SHA1(ad48a543507a981d844f0e2d5cceb689775b9ad6) )
		ROM_LOAD( "b2k.bin", 0x030000, 0x010000, CRC(e71572d3) SHA1(752540bbabf56c883208b132e285b485d4b5b4ee) )
		ROM_LOAD( "b1h.bin", 0x040000, 0x010000, CRC(8494f38c) SHA1(8d99ae088bd5b479f10e69b0a960f07d10adc23b) )
		ROM_LOAD( "b2h.bin", 0x050000, 0x010000, CRC(1fc5f049) SHA1(bb6d5622247ec32ad044cde856cf67dddc3c732f) )
		ROM_LOAD( "b3m.bin", 0x060000, 0x010000, CRC(d136802e) SHA1(84c2a6b2a8bad7e9249b6dce9cbf5301526aa6af) ) /* planes 2-3 */
		ROM_LOAD( "b4m.bin", 0x070000, 0x010000, CRC(b4fa85d3) SHA1(c15e36000bf68a838eb34c3872e342acbb9c140a) )
		ROM_LOAD( "b3k.bin", 0x080000, 0x010000, CRC(40e11cc8) SHA1(ed469a8629080da88ce6faeb232633f94e2816c3) )
		ROM_LOAD( "b4k.bin", 0x090000, 0x010000, CRC(5ca9716e) SHA1(87620083aa6a7697f6faf742ac0e47115af3e0f3) )
		ROM_LOAD( "b3h.bin", 0x0a0000, 0x010000, CRC(8c3d9173) SHA1(08df92d962852f88b42e76dfaf6bb23a80d84657) )
		ROM_LOAD( "b4h.bin", 0x0b0000, 0x010000, CRC(a2df66f8) SHA1(9349704fdb7b0919813cb48d4deacdbbdebb2fee) )
	
		ROM_REGION( 0x004000, REGION_GFX4, ROMREGION_DISPOSE )
		ROM_LOAD( "vram.4d", 0x000000, 0x004000, CRC(bfadfb32) SHA1(8443ad9f02da5fb032017fc0c657b1bdc15e4f27) ) /* Characters planes 1-2 */
	
		ROM_REGION( 0x40000, REGION_GFX5, 0 )	/* background tilemaps */
		ROM_LOAD( "bks1j10.5h", 0x000000, 0x010000, CRC(4934aacd) SHA1(15274ae8b26799e15c7a66ff89ffd386de1659d3) )
		ROM_LOAD( "bks1j18.3h", 0x010000, 0x010000, CRC(551ffc88) SHA1(4f9213f4e80033f910dd8aae44b2c6d9ba760d61) )
		ROM_LOAD( "ms1j10.3g",  0x020000, 0x010000, CRC(f92958b8) SHA1(da8fa64ea9ad27c737225681c49f7c57cc7afeed) )
		ROM_LOAD( "ms1j18.5g",  0x030000, 0x010000, CRC(89e35dc1) SHA1(368d0cce3bc39b3762d79df0c023242018fbbcb8) )
	
		ROM_REGION( 0x0320, REGION_PROMS, 0 )
		ROM_LOAD( "sfb05.bin",    0x0000, 0x0100, CRC(864199ad) SHA1(b777df20b19fa7b7536120191df1875101e9d7af) )	/* unknown */
		ROM_LOAD( "sfb00.bin",    0x0100, 0x0100, CRC(bd3f8c5d) SHA1(c31ee9f466f05a21612f5ea29fb8c7c25dc9e011) )	/* unknown */
		ROM_LOAD( "mb7114h.12j",  0x0200, 0x0100, CRC(4c734b64) SHA1(7a122b643bad3e3586821980efff023a63e5a029) )	/* unknown */
		ROM_LOAD( "mmi-7603.13h", 0x0300, 0x0020, CRC(06bcda53) SHA1(fa69b77697bb12aa6012d82ef5b504d3a1d20232) )	/* unknown */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_sf1	   = new GameDriver("1987"	,"sf1"	,"sf1.java"	,rom_sf1,null	,machine_driver_sf1	,input_ports_sf1	,null	,ROT0	,	"Capcom", "Street Fighter (World)" )
	public static GameDriver driver_sf1us	   = new GameDriver("1987"	,"sf1us"	,"sf1.java"	,rom_sf1us,driver_sf1	,machine_driver_sf1us	,input_ports_sf1us	,null	,ROT0	,	"Capcom", "Street Fighter (US)" )
	public static GameDriver driver_sf1jp	   = new GameDriver("1987"	,"sf1jp"	,"sf1.java"	,rom_sf1jp,driver_sf1	,machine_driver_sf1jp	,input_ports_sf1jp	,null	,ROT0	,	"Capcom", "Street Fighter (Japan)" )
	public static GameDriver driver_sf1p	   = new GameDriver("1987"	,"sf1p"	,"sf1.java"	,rom_sf1p,driver_sf1	,machine_driver_sf1p	,input_ports_sf1	,null	,ROT0	,	"Capcom", "Street Fighter (prototype)" )
}
