/***************************************************************************

Zodiack/Dogfight Memory Map (preliminary)

driver by Zsolt Vasvari

Memory Mapped:


I/O Ports:

00-01		W   AY8910 #0


TODO:

- Verify Z80 and AY8910 clock speeds

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class zodiack
{
	
	
	
	
	int percuss_hardware;
	
	
	
	public static MachineInitHandlerPtr machine_init_zodiack  = new MachineInitHandlerPtr() { public void handler(){
		percuss_hardware = 0;
		machine_init_espial();
	} };
	
	public static MachineInitHandlerPtr machine_init_percuss  = new MachineInitHandlerPtr() { public void handler(){
		percuss_hardware = 1;
		machine_init_espial();
	} };
	
	
	public static WriteHandlerPtr zodiack_control_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* Bit 0-1 - coin counters */
		coin_counter_w(0, data & 0x02);
		coin_counter_w(1, data & 0x01);
	
		/* Bit 2 - ???? */
	} };
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x4fff, MRA_ROM ),
		new Memory_ReadAddress( 0x5800, 0x5fff, MRA_RAM ),
		new Memory_ReadAddress( 0x6081, 0x6081, input_port_0_r ),
		new Memory_ReadAddress( 0x6082, 0x6082, input_port_1_r ),
		new Memory_ReadAddress( 0x6083, 0x6083, input_port_2_r ),
		new Memory_ReadAddress( 0x6084, 0x6084, input_port_3_r ),
		new Memory_ReadAddress( 0x6090, 0x6090, soundlatch_r ),
		new Memory_ReadAddress( 0x7000, 0x7000, MRA_NOP ),  /* ??? */
		new Memory_ReadAddress( 0x9000, 0x93ff, MRA_RAM ),
		new Memory_ReadAddress( 0xa000, 0xa3ff, MRA_RAM ),
		new Memory_ReadAddress( 0xb000, 0xb3ff, MRA_RAM ),
		new Memory_ReadAddress( 0xc000, 0xcfff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x4fff, MWA_ROM ),
		new Memory_WriteAddress( 0x5800, 0x5fff, MWA_RAM ),
		new Memory_WriteAddress( 0x6081, 0x6081, zodiack_control_w ),
		new Memory_WriteAddress( 0x6090, 0x6090, zodiac_master_soundlatch_w ),
		new Memory_WriteAddress( 0x7000, 0x7000, watchdog_reset_w ),
		new Memory_WriteAddress( 0x7100, 0x7100, zodiac_master_interrupt_enable_w ),
		new Memory_WriteAddress( 0x7200, 0x7200, zodiack_flipscreen_w ),
		new Memory_WriteAddress( 0x9000, 0x903f, zodiack_attributes_w, zodiack_attributesram ),
		new Memory_WriteAddress( 0x9040, 0x905f, MWA_RAM, spriteram, spriteram_size ),
		new Memory_WriteAddress( 0x9060, 0x907f, MWA_RAM, zodiack_bulletsram, zodiack_bulletsram_size ),
		new Memory_WriteAddress( 0x9080, 0x93ff, MWA_RAM ),
		new Memory_WriteAddress( 0xa000, 0xa3ff, zodiack_videoram_w, videoram, videoram_size ),
		new Memory_WriteAddress( 0xb000, 0xb3ff, zodiack_videoram2_w, zodiack_videoram2 ),
		new Memory_WriteAddress( 0xc000, 0xcfff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new Memory_ReadAddress( 0x2000, 0x23ff, MRA_RAM ),
		new Memory_ReadAddress( 0x6000, 0x6000, soundlatch_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new Memory_WriteAddress( 0x2000, 0x23ff, MWA_RAM ),
		new Memory_WriteAddress( 0x4000, 0x4000, interrupt_enable_w ),
		new Memory_WriteAddress( 0x6000, 0x6000, soundlatch_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort sound_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x00, AY8910_control_port_0_w ),
		new IO_WritePort( 0x01, 0x01, AY8910_write_port_0_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	
	static InputPortPtr input_ports_zodiack = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( zodiack )
		PORT_START();       /* DSW0 */  /* never read in this game */
		PORT_BIT( 0xff, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_DIPSETTING(    0x03, "6" );
		PORT_DIPNAME( 0x1c, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x14, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x18, "2 Coins/1 Credit  3 Coins/2 Credits" );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x1c, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "20000 50000" );
		PORT_DIPSETTING(    0x20, "40000 70000" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_dogfight = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( dogfight )
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x05, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x38, 0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x38, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );  /* most likely unused */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );  /* most likely unused */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_DIPSETTING(    0x03, "6" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_BITX(    0x10, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "20000 50000" );
		PORT_DIPSETTING(    0x20, "40000 70000" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x80, 0x00, "Freeze" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_moguchan = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( moguchan )
		PORT_START();       /* DSW0 */
		PORT_BIT( 0xff, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_DIPSETTING(    0x03, "6" );
		PORT_DIPNAME( 0x1c, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x14, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x18, "2 Coins/1 Credit  3 Coins/2 Credits" );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x1c, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "20000 50000" );
		PORT_DIPSETTING(    0x20, "40000 70000" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);    /* these are read, but are they */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 );				/* ever used? */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_percuss = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( percuss )
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "20000 100000" );
		PORT_DIPSETTING(    0x04, "20000 200000" );
		PORT_DIPSETTING(    0x08, "40000 100000" );
		PORT_DIPSETTING(    0x0c, "40000 200000" );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON2 );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x02, "6" );
		PORT_DIPSETTING(    0x03, "7" );
		PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_bounty = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( bounty )
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON2 );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_DIPSETTING(    0x03, "6" );
		PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,    /* 8*8 chars */
		256,    /* 256 characters */
		1,      /* 1 bit per pixel */
		new int[] { 0 } , /* single bitplane */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8     /* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout charlayout_2 = new GfxLayout
	(
		8,8,    /* 8*8 chars */
		256,    /* 256 characters */
		2,      /* 2 bits per pixel */
		new int[] { 0, 512*8*8 },  /* The bitplanes are seperate */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8     /* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,  /* 16*16 sprites */
		64,     /* 64 sprites */
		2,      /* 2 bits per pixel */
		new int[] { 0, 128*32*8 },        /* the two bitplanes are separated */
		new int[] {     0,     1,     2,     3,     4,     5,     6,     7,
		  8*8+0, 8*8+1, 8*8+2, 8*8+3, 8*8+4, 8*8+5, 8*8+6, 8*8+7 },
		new int[] {  0*8,  1*8,  2*8,  3*8,  4*8,  5*8,  6*8,  7*8,
		  16*8, 17*8, 18*8, 19*8, 20*8, 21*8, 22*8, 23*8 },
		32*8    /* every sprite takes 32 consecutive bytes */
	);
	
	static GfxLayout bulletlayout = new GfxLayout
	(
		/* there is no gfx ROM for this one, it is generated by the hardware */
		7,1,	/* it's just 1 pixel, but we use 7*1 to position it correctly */
		1,	/* just one */
		1,	/* 1 bit per pixel */
		new int[] { 10*8*8 },	/* point to letter "A" */
		new int[] { 3, 7, 7, 7, 7, 7, 7 },	/* I "know" that this bit of the */
		{ 1*8 },						/* graphics ROMs is 1 */
		0	/* no use */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0000, charlayout,   8*4    , 8 ),
		new GfxDecodeInfo( REGION_GFX1, 0x0800, spritelayout, 0      , 8 ),
		new GfxDecodeInfo( REGION_GFX1, 0x0000, bulletlayout, 8*4+8*2, 1 ),
		new GfxDecodeInfo( REGION_GFX1, 0x1000, charlayout_2, 0      , 8 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		1,	/* 1 chip */
		1789750,	/* 1.78975 MHz? */
		new int[] { 50 },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	
	static MACHINE_DRIVER_START( zodiack )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 4000000)        /* 4.00 MHz??? */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_VBLANK_INT(zodiac_master_interrupt,2)
	
		MDRV_CPU_ADD(Z80, 14318000/8)	/* 1.78975 MHz??? */
		MDRV_CPU_MEMORY(sound_readmem,sound_writemem)
		MDRV_CPU_PORTS(0,sound_writeport)
		MDRV_CPU_VBLANK_INT(nmi_line_pulse,8)	/* IRQs are triggered by the main CPU */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)  /* frames per second, vblank duration */
	
		MDRV_MACHINE_INIT(zodiack)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(49)
		MDRV_COLORTABLE_LENGTH(4*8+2*8+2*1)
	
		MDRV_PALETTE_INIT(zodiack)
		MDRV_VIDEO_START(zodiack)
		MDRV_VIDEO_UPDATE(zodiack)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( percuss )
		MDRV_IMPORT_FROM(zodiack)
		MDRV_MACHINE_INIT(percuss)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( moguchan )
		MDRV_IMPORT_FROM(zodiack)
		MDRV_MACHINE_INIT(percuss)
	MACHINE_DRIVER_END
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	static RomLoadPtr rom_zodiack = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )       /* 64k for code */
		ROM_LOAD( "ovg30c.2",     0x0000, 0x2000, CRC(a2125e99) SHA1(00ae4ed2c7b6895d2dc58aa2fc51c25b6428e4ba) )
		ROM_LOAD( "ovg30c.3",     0x2000, 0x2000, CRC(aee2b77f) SHA1(2581b7a75d38663cc5ebc91a77385ca7eb9b4aba) )
		ROM_LOAD( "ovg30c.6",     0x4000, 0x0800, CRC(1debb278) SHA1(98c9f09c5f3125ba8a1392be62fd469aa0ba4d98) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "ovg20c.1",     0x0000, 0x1000, CRC(2d3c3baf) SHA1(e32937b947e591cba45da2dd456e4f655e62ddfd) )
	
		ROM_REGION( 0x2800, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "ovg40c.7",     0x0000, 0x0800, CRC(ed9d3be7) SHA1(80d5906afef8b6d68fb13d41992aea208d9e3690) )
		ROM_LOAD( "orca40c.8",    0x0800, 0x1000, CRC(88269c94) SHA1(acc27be0d27b33c2242ecf0563fe986e8dffb264) )
		ROM_LOAD( "orca40c.9",    0x1800, 0x1000, CRC(a3bd40c9) SHA1(dcf8cbb73c081a3af85da135e8278c54e9e0de7c) )
	
		ROM_REGION( 0x0040, REGION_PROMS, 0 )
		ROM_LOAD( "ovg40c.2a",    0x0000, 0x0020, CRC(703821b8) SHA1(33dcc9b0bea5e110eb4ffd3b8b8763e32e927b22) )
		ROM_LOAD( "ovg40c.2b",    0x0020, 0x0020, CRC(21f77ec7) SHA1(b1019afc4361aca98b7120b21743bfeb5ea2ff63) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_dogfight = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )       /* 64k for code */
		ROM_LOAD( "df-2",         0x0000, 0x2000, CRC(ad24b28b) SHA1(5bfc24c9d176a987525c5ad3eff3308f679d4d44) )
		ROM_LOAD( "df-3",         0x2000, 0x2000, CRC(cd172707) SHA1(9d7a494006db13cbe9c895875a18d9423a0128bc) )
		ROM_LOAD( "df-5",         0x4000, 0x1000, CRC(874dc6bf) SHA1(2c34fdfb2838c41f239171bc9a14a5cc7a94a170) )
		ROM_LOAD( "df-4",         0xc000, 0x1000, CRC(d8aa3d6d) SHA1(0863d566ff4a181ae8a8d552f9768dd028254605) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "df-1",         0x0000, 0x1000, CRC(dcbb1c5b) SHA1(51fa51ff64982455b00484d55f6a8cf89fc786f1) )
	
		ROM_REGION( 0x2800, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "df-6",         0x0000, 0x0800, CRC(3059b515) SHA1(849e99a04ddcdfcf097cc3ac17e9edf12b51cd69) )
		ROM_LOAD( "df-7",         0x0800, 0x1000, CRC(ffe05fee) SHA1(70b9d0808defd936e2c3567f8e6996a19753de81) )
		ROM_LOAD( "df-8",         0x1800, 0x1000, CRC(2cb51793) SHA1(d90177ef28730774202a04a0846281537a1883df) )
	
		ROM_REGION( 0x0040, REGION_PROMS, 0 )
		ROM_LOAD( "1.bpr",        0x0000, 0x0020, CRC(69a35aa5) SHA1(fe494ed1ff642f95834dfca92e9c4494e04f7b81) )
		ROM_LOAD( "2.bpr",        0x0020, 0x0020, CRC(596ae457) SHA1(1c1a3130d88c5fd5c66ce9f91d97a09c0a0b535f) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_moguchan = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )       /* 64k for code */
		ROM_LOAD( "2.5r",         0x0000, 0x1000, CRC(85d0cb7e) SHA1(20066f71d80161dff556bc86edf40fcc2ac3b993) )
		ROM_LOAD( "4.5m",         0x1000, 0x1000, CRC(359ef951) SHA1(93e80fcd371e8d2026919d0e046b636b7c19002e) )
		ROM_LOAD( "3.5np",        0x2000, 0x1000, CRC(c8776f77) SHA1(edc28a6a804b9573307faa25b6fdd096b7093593) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "1.7hj",        0x0000, 0x1000, CRC(1a88d35f) SHA1(d9347723c0eb6508739a6c0de4984b8244b197cf) )
	
		ROM_REGION( 0x2800, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "5.4r",         0x0000, 0x0800, CRC(1b7febd8) SHA1(4f9ce99f05e6e207bb91f831b6bee7cf72b3d05b) )
		ROM_LOAD( "6.7p",         0x0800, 0x1000, CRC(c8060ffe) SHA1(f1f975c2638e6cdf00af4a96591529c7b6684742) )
		ROM_LOAD( "7.7m",         0x1800, 0x1000, CRC(bfca00f4) SHA1(797b07bab2467fe00cd85cd4477db2367a3e5a40) )
	
		ROM_REGION( 0x0040, REGION_PROMS, 0 )
		ROM_LOAD( "moguchan.2a",  0x0000, 0x0020, CRC(e83daab3) SHA1(58b38091dfbc3f3b4ddf6c6febd98c909be89063) )
		ROM_LOAD( "moguchan.2b",  0x0020, 0x0020, CRC(9abfdf40) SHA1(44c4dcdd3d79af2c4a897cc003b5287dece0313e) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_percuss = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )       /* 64k for code */
		ROM_LOAD( "percuss.1",    0x0000, 0x1000, CRC(ff0364f7) SHA1(048963d70e513068fdb591b4bc152473fe4cc2c3) )
		ROM_LOAD( "percuss.3",    0x1000, 0x1000, CRC(7f646c59) SHA1(976210f1fed11c03e0a159c8189630a1fec63fc9) )
		ROM_LOAD( "percuss.2",    0x2000, 0x1000, CRC(6bf72dd2) SHA1(447109a508a571650501d3dd3fa018513b4e4558) )
		ROM_LOAD( "percuss.4",    0x3000, 0x1000, CRC(fb1b15ba) SHA1(778853a0fbef7dd80d1046e2b04adcd9d1241f83) )
		ROM_LOAD( "percuss.5",    0x4000, 0x1000, CRC(8e5a9692) SHA1(17db644c3cd0435c380b2080bda02c6a6d45eaf6) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "percuss.8",    0x0000, 0x0800, CRC(d63f56f3) SHA1(2cf9cca55fb612c9efae06e62bd6d33fdd38de57) )
		ROM_LOAD( "percuss.9",    0x0800, 0x0800, CRC(e08fef2f) SHA1(af41763f200c4c660ca7230eace4f443e57e809a) )
	
		ROM_REGION( 0x2800, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "percuss.10",   0x0000, 0x0800, CRC(797598aa) SHA1(6d26ed5e964b7f1b2ce651c7bf0168a730bee02f) )
		ROM_LOAD( "percuss.6",    0x0800, 0x1000, CRC(5285a580) SHA1(cd7ba64706458c67166934d0fa08894fb577810b) )
		ROM_LOAD( "percuss.7",    0x1800, 0x1000, CRC(8fc4175d) SHA1(0249f6324ae44142d7da2306674ed86afcccf5f2) )
	
		ROM_REGION( 0x0040, REGION_PROMS, 0 )
		ROM_LOAD( "percus2a.prm", 0x0000, 0x0020, CRC(e2ee9637) SHA1(e4ca064793ae1dc36b2d852448162d062a2f26f8) )
		ROM_LOAD( "percus2b.prm", 0x0020, 0x0020, CRC(e561b029) SHA1(7d21a3492a179f5ce541911d19e4816960547089) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_bounty = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )       /* 64k for code */
		ROM_LOAD( "1.4f",      0x0000, 0x1000, CRC(f495b19d) SHA1(df2de0869b10da1ee1d98d48615c9e1dce798c26) )
		ROM_LOAD( "3.4k",      0x1000, 0x1000, CRC(fa3086c3) SHA1(b8bab26a4e68e6d2e5b899e900c9affd297c22de) )
		ROM_LOAD( "2.4h",      0x2000, 0x1000, CRC(52ab5314) SHA1(ba399f8b86cc64d1dd585dde79d8036d24296475) )
		ROM_LOAD( "4.4m",      0x3000, 0x1000, CRC(5c9d3f07) SHA1(340460f38c5b23c591f92d386c0df10ed75fa16f) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 ) /* 64k for the audio CPU */
		ROM_LOAD( "7.4n",      0x0000, 0x1000, CRC(45e369b8) SHA1(9799b2ece5e3b92da435255e1b49f5097d3f7972) )
		ROM_LOAD( "8.4r",      0x1000, 0x1000, CRC(4f52c87d) SHA1(1738d798c341a54d293c70da7b6e4a3dfb00de38) )
	
		ROM_REGION( 0x2800, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "9.4r",      0x0000, 0x0800, CRC(4b4acde5) SHA1(5ed60fe50a9ab0b8d433ee9ae5787b936ddbfbdd) )
		ROM_LOAD( "5.7m",      0x0800, 0x1000, CRC(a5ce2a24) SHA1(b5469d31bda52a61cdc46349c139a7eb339ac8a7) )
		ROM_LOAD( "6.7p",      0x1800, 0x1000, CRC(43183301) SHA1(9df89479396d7847ee3325649d7264e75d413add) )
	
		ROM_REGION( 0x0040, REGION_PROMS, 0 )
		ROM_LOAD( "mb7051.2a",   0x0000, 0x0020, CRC(0de11a46) SHA1(3bc81571832dd78b29654e86479815ee5f97a4d3) )
		ROM_LOAD( "mb7051.2b",   0x0020, 0x0020, CRC(465e31d4) SHA1(d47a4aa0e8931dcd8f85017ef04c2f6ad79f5725) )
	ROM_END(); }}; 
	
	
	
	GAMEX(1983, zodiack,  0, zodiack, zodiack,  0, ROT270, "Orca (Esco Trading Co)", "Zodiack", GAME_IMPERFECT_COLORS )	/* bullet color needs to be verified */
	GAMEX(1983, dogfight, 0, zodiack, dogfight, 0, ROT270, "[Orca] Thunderbolt", "Dog Fight (Thunderbolt)", GAME_IMPERFECT_COLORS )	/* bullet color needs to be verified */
	GAMEX(1982, moguchan, 0, percuss, moguchan, 0, ROT270, "Orca (Eastern Commerce Inc. license) (bootleg?)",  /* this is in the ROM at $0b5c */ "Moguchan", GAME_WRONG_COLORS )
	GAME( 1981, percuss,  0, percuss, percuss,  0, ROT270, "Orca", "The Percussor" )
	GAME( 1982, bounty,   0, percuss, bounty,   0, ROT180, "Orca", "The Bounty" )
}
