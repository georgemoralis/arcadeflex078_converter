/***************************************************************************

	Cinemat/Leland driver

	driver by Aaron Giles and Paul Leaman

	Games supported:
		* Cerberus
		* Mayhem 2002
		* Power Play
		* World Series: The Season
		* Alley Master
		* Danger Zone
		* Baseball The Season II
		* Super Baseball Double Play Home Run Derby
		* Strike Zone Baseball
		* Redline Racer
		* Quarterback
		* Viper
		* John Elway's Team Quarterback
		* All American Football
		* Ironman Stewart's Super Off-Road
		* Pigout

	Known bugs:
		* none at this time

****************************************************************************

	To enter service mode in most games, press 1P start and then press
	the service switch (F2).

	For Redline Racer, hold the service switch down and reset the machine.

	For Super Offroad, press the blue nitro button (3P button 1) and then
	press the service switch.

***************************************************************************/


/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class leland
{
	
	
	/*************************************
	 *
	 *	Master CPU memory handlers
	 *
	 *************************************/
	
	public static Memory_ReadAddress master_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new Memory_ReadAddress( 0x2000, 0x9fff, MRA_BANK1 ),
		new Memory_ReadAddress( 0xa000, 0xdfff, MRA_BANK2 ),
		new Memory_ReadAddress( 0xe000, 0xefff, MRA_RAM ),
		new Memory_ReadAddress( 0xf000, 0xf3ff, leland_gated_paletteram_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress master_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x9fff, MWA_ROM ),
		new Memory_WriteAddress( 0xa000, 0xdfff, leland_battery_ram_w ),
		new Memory_WriteAddress( 0xe000, 0xefff, MWA_RAM ),
		new Memory_WriteAddress( 0xf000, 0xf3ff, leland_gated_paletteram_w, paletteram ),
		new Memory_WriteAddress( 0xf800, 0xf801, leland_master_video_addr_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_ReadPort master_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
	    new IO_ReadPort( 0xf2, 0xf2, leland_i86_response_r ),
	    new IO_ReadPort( 0xfd, 0xff, leland_master_analog_key_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_WritePort master_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0xf0, 0xf0, leland_master_alt_bankswitch_w ),
		new IO_WritePort( 0xf2, 0xf2, leland_i86_command_lo_w ),
		new IO_WritePort( 0xf4, 0xf4, leland_i86_command_hi_w ),
	    new IO_WritePort( 0xfd, 0xff, leland_master_analog_key_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	
	/*************************************
	 *
	 *	Slave CPU memory handlers
	 *
	 *************************************/
	
	public static Memory_ReadAddress slave_small_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new Memory_ReadAddress( 0x2000, 0xdfff, MRA_BANK3 ),
		new Memory_ReadAddress( 0xe000, 0xefff, MRA_RAM ),
		new Memory_ReadAddress( 0xf802, 0xf802, leland_raster_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress slave_small_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0xdfff, MWA_ROM ),
		new Memory_WriteAddress( 0xe000, 0xefff, MWA_RAM ),
		new Memory_WriteAddress( 0xf800, 0xf801, leland_slave_video_addr_w ),
		new Memory_WriteAddress( 0xf803, 0xf803, leland_slave_small_banksw_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_ReadAddress slave_large_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new Memory_ReadAddress( 0x4000, 0xbfff, MRA_BANK3 ),
		new Memory_ReadAddress( 0xe000, 0xefff, MRA_RAM ),
		new Memory_ReadAddress( 0xf802, 0xf802, leland_raster_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress slave_large_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xc000, leland_slave_large_banksw_w ),
		new Memory_WriteAddress( 0xe000, 0xefff, MWA_RAM ),
		new Memory_WriteAddress( 0xf800, 0xf801, leland_slave_video_addr_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_ReadPort slave_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x1f, leland_svram_port_r ),
		new IO_ReadPort( 0x40, 0x5f, leland_svram_port_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_WritePort slave_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x1f, leland_svram_port_w ),
		new IO_WritePort( 0x40, 0x5f, leland_svram_port_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	/* Helps document the input ports. */
	#define IPT_SLAVEHALT 	IPT_SPECIAL
	#define IPT_EEPROM_DATA	IPT_SPECIAL
	
	
	static InputPortPtr input_ports_cerberus = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( cerberus )		/* complete, verified from code */
		PORT_START();       /* 0x80 */
	    PORT_BIT( 0x3f, IP_ACTIVE_LOW, IPT_SPECIAL | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
	
		PORT_START();       /* 0x81 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT );
		PORT_SERVICE_NO_TOGGLE( 0x02, IP_ACTIVE_LOW )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x90 */
	    PORT_BIT( 0x3f, IP_ACTIVE_LOW, IPT_SPECIAL | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
	
		PORT_START();       /* 0x91 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_EEPROM_DATA );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 | IPF_PLAYER1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* Analog joystick 1 */
	    PORT_ANALOG( 0xff, 0, IPT_DIAL | IPF_PLAYER1, 50, 10, 0, 0 );
		PORT_START(); 
	    PORT_ANALOG( 0xff, 0, IPT_DIAL | IPF_PLAYER2, 50, 10, 0, 0 );
		PORT_START();       /* Analog joystick 2 */
		PORT_START(); 
		PORT_START();       /* Analog joystick 3 */
		PORT_START(); 
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_mayhem = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( mayhem )		/* complete, verified from code */
		PORT_START();       /* 0xC0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
	
		PORT_START();       /* 0xC1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT );
		PORT_SERVICE_NO_TOGGLE( 0x02, IP_ACTIVE_LOW )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0xD0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
	
		PORT_START();       /* 0xD1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_EEPROM_DATA );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* Analog joystick 1 */
		PORT_START(); 
		PORT_START();       /* Analog joystick 2 */
		PORT_START(); 
		PORT_START();       /* Analog joystick 3 */
		PORT_START(); 
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_wseries = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( wseries )		/* complete, verified from code */
		PORT_START();       /* 0x80 */
		PORT_BIT( 0x3f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "Extra Base", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "Go Back", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
	
		PORT_START();       /* 0x81 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT );
		PORT_SERVICE_NO_TOGGLE( 0x02, IP_ACTIVE_LOW )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x90 */
		PORT_BIT( 0x7f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1, "Aim", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
	
		PORT_START();       /* 0x91 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_EEPROM_DATA );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 | IPF_PLAYER1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* Analog joystick 1 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START();       /* Analog joystick 2 */
		PORT_START(); 
		PORT_START();       /* Analog joystick 3 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_PLAYER2, 100, 10, 0, 255 );
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER2, 100, 10, 0, 255 );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_alleymas = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( alleymas )		/* complete, verified from code */
		PORT_START();       /* 0xC0 */
		PORT_BIT( 0x3f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 );
	
		PORT_START();       /* 0xC1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT );
		PORT_SERVICE_NO_TOGGLE( 0x02, IP_ACTIVE_LOW )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0xD0 */
		PORT_BIT( 0x3f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 );	/* redundant inputs */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON2 );	/* redundant inputs */
	
		PORT_START();       /* 0xD1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_EEPROM_DATA );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 | IPF_PLAYER1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* Analog joystick 1 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_PLAYER1, 100, 10, 0, 224 );
		PORT_START();       /* Analog joystick 2 */
		PORT_START(); 
		PORT_START();       /* Analog joystick 3 */
		PORT_START(); 
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_dangerz = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( dangerz )		/* complete, verified from code */
		PORT_START();       /* 0x80 */
		PORT_BIT( 0x1f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x81 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT );
		PORT_SERVICE_NO_TOGGLE( 0x02, IP_ACTIVE_LOW )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x90 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x91 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_EEPROM_DATA );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* Analog 1 */
		PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_Y | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START();       /* Analog 2 */
		PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER1, 100, 10, 0, 255 );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_basebal2 = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( basebal2 )		/* complete, verified from code */
		PORT_START();       /* 0x40/C0 */
		PORT_BIT( 0x0f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x30, IP_ACTIVE_LOW, IPT_UNKNOWN );/* read by strkzone, but never referenced */
		PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "Extra Base", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "Go Back", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
	
		PORT_START();       /* 0x41/C1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT );
		PORT_SERVICE_NO_TOGGLE( 0x02, IP_ACTIVE_LOW )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x50/D0 */
		PORT_BIT( 0x0f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1, "R Run/Steal", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1, "L Run/Steal", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER1, "Run/Aim", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1, "Run/Cutoff", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
	
		PORT_START();       /* 0x51/D1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_EEPROM_DATA );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 | IPF_PLAYER1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* Analog joystick 1 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START();       /* Analog joystick 2 */
		PORT_START(); 
		PORT_START();       /* Analog joystick 3 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_PLAYER2, 100, 10, 0, 255 );
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER2, 100, 10, 0, 255 );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_redline = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( redline )		/* complete, verified in code */
		PORT_START();       /* 0xC0 */
		PORT_BIT( 0x1f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_ANALOG( 0xe0, 0xe0, IPT_PEDAL | IPF_PLAYER1, 100, 64, 0x00, 0xff );
	
		PORT_START();       /* 0xC1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT );
		PORT_SERVICE_NO_TOGGLE( 0x02, IP_ACTIVE_LOW )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x70, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* used, but for what purpose? */
	
		PORT_START();       /* 0xD0 */
		PORT_BIT( 0x1f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_ANALOG( 0xe0, 0xe0, IPT_PEDAL | IPF_PLAYER2, 100, 64, 0x00, 0xff );
	
		PORT_START();       /* 0xD1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_EEPROM_DATA );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* Analog wheel 1 */
		PORT_ANALOG( 0xff, 0x80, IPT_DIAL | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START();       /* Analog wheel 2 */
		PORT_ANALOG( 0xff, 0x80, IPT_DIAL | IPF_PLAYER2, 100, 10, 0, 255 );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_quarterb = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( quarterb )		/* complete, verified in code */
		PORT_START();       /* 0x80 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x0e, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x81 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT );
		PORT_SERVICE_NO_TOGGLE( 0x02, IP_ACTIVE_LOW )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x90 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 );
	
		PORT_START();       /* 0x91 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_EEPROM_DATA );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* Analog spring stick 1 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START();       /* Analog spring stick 2 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START();       /* Analog spring stick 3 */
		PORT_START();       /* Analog spring stick 4 */
		PORT_START();       /* Analog spring stick 5 */
		PORT_START();       /* Analog spring stick 6 */
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_teamqb = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( teamqb )		/* complete, verified in code */
		PORT_START();       /* 0x80 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x0e, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x81 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT );
		PORT_SERVICE_NO_TOGGLE( 0x02, IP_ACTIVE_LOW )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x90 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 );
	
		PORT_START();       /* 0x91 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_EEPROM_DATA );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* Analog spring stick 1 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START();       /* Analog spring stick 2 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START();       /* Analog spring stick 3 */
		PORT_START();       /* Analog spring stick 4 */
		PORT_START();       /* Analog spring stick 5 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER3, 100, 10, 0, 255 );
		PORT_START();       /* Analog spring stick 6 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_PLAYER3, 100, 10, 0, 255 );
	
		PORT_START();       /* 0x7C */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );
		PORT_BIT( 0x0e, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );
	
		PORT_START();       /* 0x7F */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER4 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER4 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER4 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER4 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER3 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER3 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER3 );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_aafb2p = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( aafb2p )		/* complete, verified in code */
		PORT_START();       /* 0x80 */
		PORT_BIT( 0x0f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x81 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT );
		PORT_SERVICE_NO_TOGGLE( 0x02, IP_ACTIVE_LOW )
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x90 */
		PORT_BIT( 0x0f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 );
	
		PORT_START();       /* 0x91 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_EEPROM_DATA );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* Analog spring stick 1 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START();       /* Analog spring stick 2 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START();       /* Analog spring stick 3 */
		PORT_START();       /* Analog spring stick 4 */
		PORT_START();       /* Analog spring stick 5 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER2, 100, 10, 0, 255 );
		PORT_START();       /* Analog spring stick 6 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_PLAYER2, 100, 10, 0, 255 );
	
		PORT_START();       /* 0x7C */
		PORT_BIT( 0x0f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x7F */
		PORT_BIT( 0x0f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_offroad = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( offroad )		/* complete, verified from code */
		PORT_START();       /* 0xC0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );/* read */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );/* read */
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );/* read */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0xC1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0xD0 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0xD1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_EEPROM_DATA );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_SERVICE_NO_TOGGLE( 0x08, IP_ACTIVE_LOW )
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* Analog pedal 1 */
		PORT_ANALOG( 0xff, 0x00, IPT_PEDAL | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START();       /* Analog pedal 2 */
		PORT_ANALOG( 0xff, 0x00, IPT_PEDAL | IPF_PLAYER2, 100, 10, 0, 255 );
		PORT_START();       /* Analog pedal 3 */
		PORT_ANALOG( 0xff, 0x00, IPT_PEDAL | IPF_PLAYER3, 100, 10, 0, 255 );
		PORT_START();       /* Analog wheel 1 */
		PORT_ANALOG( 0xff, 0x80, IPT_DIAL | IPF_PLAYER1, 100, 10, 0, 255 );
		PORT_START();       /* Analog wheel 2 */
		PORT_ANALOG( 0xff, 0x80, IPT_DIAL | IPF_PLAYER2, 100, 10, 0, 255 );
		PORT_START();       /* Analog wheel 3 */
		PORT_ANALOG( 0xff, 0x80, IPT_DIAL | IPF_PLAYER3, 100, 10, 0, 255 );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_pigout = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( pigout )		/* complete, verified from code */
		PORT_START();       /* 0x40 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP   | IPF_8WAY | IPF_PLAYER2 );
	
		PORT_START();       /* 0x41 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );/* read, but never referenced */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x50 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP   | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
	
		PORT_START();       /* 0x51 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_EEPROM_DATA );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_SERVICE_NO_TOGGLE( 0x04, IP_ACTIVE_LOW )
		PORT_BIT( 0xf8, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* 0x7F */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Graphics definitions
	 *
	 *************************************/
	
	static GfxLayout bklayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,3),
		3,
		new int[] { RGN_FRAC(0,3), RGN_FRAC(1,3), RGN_FRAC(2,3) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, bklayout, 0, 8 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/*************************************
	 *
	 *	Sound definitions
	 *
	 *************************************/
	
	/*
	   2 AY8910 chips - Actually, one of these is an 8912
	   (8910 with only 1 output port)
	
	   Port A of both chips is connected to a banking control
	   register.
	*/
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		2,
		10000000/6, /* 1.666 MHz */
		new int[] { 25, 25 },
	    new ReadHandlerPtr[] { leland_sound_port_r, leland_sound_port_r },
		new ReadHandlerPtr[] { 0 },
	    new WriteHandlerPtr[] { leland_sound_port_w, leland_sound_port_w },
		new WriteHandlerPtr[] { 0 }
	);
	
	
	static struct CustomSound_interface dac_custom_interface =
	{
	    leland_sh_start,
	    leland_sh_stop
	};
	
	
	static struct CustomSound_interface i186_custom_interface =
	{
	    leland_i186_sh_start
	};
	
	
	static struct CustomSound_interface redline_custom_interface =
	{
	  	redline_i186_sh_start
	};
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MACHINE_DRIVER_START( leland )
	
		/* basic machine hardware */
		MDRV_CPU_ADD_TAG("master", Z80, 6000000)
		MDRV_CPU_MEMORY(master_readmem,master_writemem)
		MDRV_CPU_PORTS(master_readport,master_writeport)
		MDRV_CPU_VBLANK_INT(leland_master_interrupt,1)
	
		MDRV_CPU_ADD_TAG("slave", Z80, 6000000)
		MDRV_CPU_MEMORY(slave_small_readmem,slave_small_writemem)
		MDRV_CPU_PORTS(slave_readport,slave_writeport)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION((1000000*16)/(256*60))
		
		MDRV_MACHINE_INIT(leland)
		MDRV_NVRAM_HANDLER(leland)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(40*8, 30*8)
		MDRV_VISIBLE_AREA(0*8, 40*8-1, 0*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(leland)
		MDRV_VIDEO_EOF(leland)
		MDRV_VIDEO_UPDATE(leland)
	
		/* sound hardware */
		MDRV_SOUND_ADD_TAG("ay8910", AY8910, ay8910_interface)
		MDRV_SOUND_ADD_TAG("custom", CUSTOM, dac_custom_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( redline )
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(leland)
		MDRV_CPU_ADD_TAG("sound", I186, 16000000/2)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)
		MDRV_CPU_MEMORY(leland_i86_readmem,leland_i86_writemem)
		MDRV_CPU_PORTS(leland_i86_readport,redline_i86_writeport)
		
		/* sound hardware */
		MDRV_SOUND_REPLACE("custom", CUSTOM, redline_custom_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( quarterb )
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(redline)
		MDRV_CPU_MODIFY("sound")
		MDRV_CPU_PORTS(leland_i86_readport,leland_i86_writeport)
		
		/* sound hardware */
		MDRV_SOUND_REPLACE("custom", CUSTOM, i186_custom_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( lelandi )
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(quarterb)
		MDRV_CPU_MODIFY("slave")
		MDRV_CPU_MEMORY(slave_large_readmem,slave_large_writemem)
	MACHINE_DRIVER_END
	
	
	
	/*************************************
	 *
	 *	ROM definitions
	 *
	 *************************************/
	
	static RomLoadPtr rom_cerberus = new RomLoadPtr(){ public void handler(){ 
	    ROM_REGION( 0x10000, REGION_CPU1, 0 )
		ROM_LOAD( "3-23u101", 0x00000, 0x02000, CRC(d78210df) SHA1(7557bc9da7d7347073cebcc080ff2040184ee77b) )
		ROM_LOAD( "3-23u102", 0x02000, 0x02000, CRC(eed121ef) SHA1(862c4fee6c4483569aec7969ce797a5c3fbae336) )
		ROM_LOAD( "3-23u103", 0x04000, 0x02000, CRC(45b82bf7) SHA1(ca239fcb96754c9e388d55eea4974824e6ce4d75) )
		ROM_LOAD( "3-23u104", 0x06000, 0x02000, CRC(e133d6bf) SHA1(7afe4883d7b072277fab8b383ad3a247c7045403) )
		ROM_LOAD( "3-23u105", 0x08000, 0x02000, CRC(a12c2c79) SHA1(1a36405a8f9bc4422f01c2bb1361061fb8d76b51) )
		ROM_LOAD( "3-23u106", 0x0a000, 0x02000, CRC(d64110d2) SHA1(3bd8cda21e848357c84f5064f38e0b9da35051db) )
		ROM_LOAD( "3-23u107", 0x0c000, 0x02000, CRC(24e41c34) SHA1(b38462593320bd004a24392e0cce7b36fe12434e) )
	
	    ROM_REGION( 0x10000, REGION_CPU2, 0 )
		ROM_LOAD( "3-23u3",  0x00000, 0x02000, CRC(b0579138) SHA1(b79888d0c8cc4ecb015e3865df379859e02e2846) )
		ROM_LOAD( "3-23u4",  0x02000, 0x02000, CRC(ba0dc990) SHA1(836eef85e31b81a4b6f84529ecbe64167a5059dd) )
		ROM_LOAD( "3-23u5",  0x04000, 0x02000, CRC(f8d6cc5d) SHA1(5b82c722aa6a055d1955f654985b43e114792704) )
		ROM_LOAD( "3-23u6",  0x06000, 0x02000, CRC(42cdd393) SHA1(3d2a803cb90ec25af0b34de1ae549408fc0292c3) )
		ROM_LOAD( "3-23u7",  0x08000, 0x02000, CRC(c020148a) SHA1(5ed0211526f0dc04ed010b9103bb7992dc17766f) )
		ROM_LOAD( "3-23u8",  0x0a000, 0x02000, CRC(dbabdbde) SHA1(906ff8f91eaf01f0435d7ac1291af62073568d2f) )
		ROM_LOAD( "3-23u9",  0x0c000, 0x02000, CRC(eb992385) SHA1(0951d6fb5ff8508ef7184e9c26be6c20b85bad72) )
	
		ROM_REGION( 0x06000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "3-23u93", 0x00000, 0x02000, CRC(14a1a4b0) SHA1(aad63e368a09497188f8112d1ca0ac0d0366ac61) )
		ROM_LOAD( "3-23u94", 0x02000, 0x02000, CRC(207a1709) SHA1(c7fbb80a83a5684b6b35750df68d51091e8747e4) )
		ROM_LOAD( "3-23u95", 0x04000, 0x02000, CRC(e9c86267) SHA1(c7f3a4725824da1e2793160409821017bd0bd956) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "3-23u70",  0x02000, 0x2000, CRC(96499983) SHA1(202c9d74fe4bbce7b93fcbb6352c35eb480d8297) )
		ROM_LOAD( "3-23_u92", 0x06000, 0x2000, CRC(497bb717) SHA1(748ac9f22d896b493cdf182ec9deb3e07e2ffb48) )
		ROM_LOAD( "3-23u69",  0x0a000, 0x2000, CRC(ebd14d9e) SHA1(8eb061d43eb60eea01b122e0b4e937bfc00146cc) )
		ROM_LOAD( "3-23u91",  0x0e000, 0x2000, CRC(b592d2e5) SHA1(bbacbd772b6fc683dfec4f13bdf9a1746f3ea1e6) )
		ROM_LOAD( "3-23u68",  0x12000, 0x2000, CRC(cfa7b8bf) SHA1(7f38f8148cddc93baedfaa28a8c72918eb5d3b98) )
		ROM_LOAD( "3-23u90",  0x16000, 0x2000, CRC(b7566f8a) SHA1(a0128b3bf4803947050a75df0607e4886f5ed931) )
		ROM_LOAD( "3-23u67",  0x1a000, 0x2000, CRC(02b079a8) SHA1(2ad76641831a391d9acefe8e42515e16dd056868) )
		ROM_LOAD( "3-23u89",  0x1e000, 0x2000, CRC(7e5e82bb) SHA1(ccbb583689d420a0b7413c0a221a3f57a5ab0e63) )
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_mayhem = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1, 0 )
		ROM_LOAD( "13208.101",   0x00000, 0x04000, CRC(04306973) SHA1(83e35fa7f2b2c6c1a65ee2f76223e12234eb69ad) )
		ROM_LOAD( "13215.102",   0x10000, 0x02000, CRC(06e689ae) SHA1(1bf4ae82809eaaf06608d2015bdeceae57a345a1) )
		ROM_CONTINUE(            0x1c000, 0x02000 )
		ROM_LOAD( "13216.103",   0x12000, 0x02000, CRC(6452a82c) SHA1(8008238359fbf1c138f4fa9fce5580d63db978f2) )
		ROM_CONTINUE(            0x1e000, 0x02000 )
		ROM_LOAD( "13217.104",   0x14000, 0x02000, CRC(62f6036e) SHA1(3e88e3f4390236b0a4623678a1a6e160c30ff747) )
		ROM_CONTINUE(            0x20000, 0x02000 )
		ROM_LOAD( "13218.105",   0x16000, 0x02000, CRC(162f5eb1) SHA1(9658b8bae35ea1d55e147a5a43ec00a25e102f54) )
		ROM_CONTINUE(            0x22000, 0x02000 )
		ROM_LOAD( "13219.106",   0x18000, 0x02000, CRC(c0a74d6f) SHA1(c47ff4dc47bea79c76198a677181c92026e8c3db) )
		ROM_CONTINUE(            0x24000, 0x02000 )
	
		ROM_REGION( 0x28000, REGION_CPU2, 0 )
		ROM_LOAD( "13207.3",  0x00000, 0x04000, CRC(be1df6aa) SHA1(72e8782a96d598580a13b2183fbdc434f68d490b) ) /* DO NOT TRIM THIS ROM */
		ROM_LOAD( "13209.4",  0x10000, 0x02000, CRC(39fcd7c6) SHA1(2064a7caec0753d38a39095492f705a20482eb83) )
		ROM_CONTINUE(         0x1c000, 0x02000 )
		ROM_LOAD( "13210.5",  0x12000, 0x02000, CRC(630ed136) SHA1(fc9bc18ec18a57b8d45adcab737e29512fc62d3a) )
		ROM_CONTINUE(         0x1e000, 0x02000 )
		ROM_LOAD( "13211.6",  0x14000, 0x02000, CRC(28b4aecd) SHA1(66bfcdc66efec6e8537b29382b9702f713455826) )
		ROM_CONTINUE(         0x20000, 0x02000 )
		ROM_LOAD( "13212.7",  0x16000, 0x02000, CRC(1d6b39ab) SHA1(094e3b7e2b933c5e00722f889a75e4d76569f6fb) )
		ROM_CONTINUE(         0x22000, 0x02000 )
		ROM_LOAD( "13213.8",  0x18000, 0x02000, CRC(f3b2ea05) SHA1(ee916b903ce6891e7ea98848d559362c0e0ac8d2) )
		ROM_CONTINUE(         0x24000, 0x02000 )
		ROM_LOAD( "13214.9",  0x1a000, 0x02000, CRC(96f3e8d9) SHA1(e0a663c3c9dc77f2ec10c71a9d227ec3ea765c6e) )
		ROM_CONTINUE(         0x26000, 0x02000 )
	
		ROM_REGION( 0x0c000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "13204.93", 0x00000, 0x04000, CRC(de183518) SHA1(080cd45c2c7d81b8edd5170aa6a587ae6e7e54fb) )
		ROM_LOAD( "13205.94", 0x04000, 0x04000, CRC(c61f63ac) SHA1(c52fe331391720796556a7eab7d145fd1dacf6ed) )
		ROM_LOAD( "13206.95", 0x08000, 0x04000, CRC(8e7bd2fd) SHA1(ccd97ef604be6d4479a8a91fccecb5d71a4d82af) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		/* U70 = Empty */
		ROM_LOAD( "13203.92",  0x04000, 0x4000, CRC(121ed5bf) SHA1(691b09a3bad3d1fd13ec38a81a15436b8baba0a1) )
		ROM_LOAD( "13201.69",  0x08000, 0x4000, CRC(90283e29) SHA1(36b71e2df455758b139a503968b80112a65c347a) )
		/* U91 = Empty */
		/* U68 = Empty */
		/* U90 = Empty */
		/* U67 = Empty */
		ROM_LOAD( "13202.89",  0x1c000, 0x4000, CRC(c5eaa4e3) SHA1(007a526543d06b8f39e4e93da6ad19725ec6aa2d) )
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_powrplay = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1, 0 )
		ROM_LOAD( "13306.101",   0x00000, 0x02000, CRC(981fc215) SHA1(c2ae1ff12f96c713d0dc6f6503ce0ba18ac342c4) )
		ROM_LOAD( "13307.102",   0x10000, 0x02000, CRC(38a6ddfe) SHA1(a4a4372697e14584c3a6a9a8c94e5a4ee58b3ee6) )
		ROM_CONTINUE(            0x1c000, 0x02000 )
		ROM_LOAD( "13308.103",   0x12000, 0x02000, CRC(7fa2ab9e) SHA1(d774f3a32d799f845805e88e21e1687aa35a390e) )
		ROM_CONTINUE(            0x1e000, 0x02000 )
		ROM_LOAD( "13309.104",   0x14000, 0x02000, CRC(bd9e6fa8) SHA1(4530d449d9e1cee0e346f8915e3b727b396a399d) )
		ROM_CONTINUE(            0x20000, 0x02000 )
		ROM_LOAD( "13310.105",   0x16000, 0x02000, CRC(b6df3a5a) SHA1(b968c47ecceb8be7f3b21f1f35f1a13840821f32) )
		ROM_CONTINUE(            0x22000, 0x02000 )
		ROM_LOAD( "13311.106",   0x18000, 0x02000, CRC(5e17fe84) SHA1(8f53de9acc08f17dd2bc5a81489d8da86ad3c690) )
		ROM_CONTINUE(            0x24000, 0x02000 )
	
		ROM_REGION( 0x28000, REGION_CPU2, 0 )
		ROM_LOAD( "13305.003",  0x00000, 0x02000, CRC(df8fbeed) SHA1(2b5ec692cf90fe66d06a2261d9d56cb88528750d) )
		ROM_LOAD( "13313.004",  0x10000, 0x02000, CRC(081eb88f) SHA1(97700d9dba05a459fb85911db8f4b4fe1283776b) )
		ROM_CONTINUE(           0x1c000, 0x02000 )
		ROM_LOAD( "13314.005",  0x12000, 0x02000, CRC(b8e61f8c) SHA1(0ae3439510ad8a15f9f9c1981b2278aa950cc0b4) )
		ROM_CONTINUE(           0x1e000, 0x02000 )
		ROM_LOAD( "13315.006",  0x14000, 0x02000, CRC(776d3c40) SHA1(7fc68f16dc148c860c1ae12fb8e12d3adbe3d7c1) )
		ROM_CONTINUE(           0x20000, 0x02000 )
		ROM_LOAD( "13316.007",  0x16000, 0x02000, CRC(9b3ec2a1) SHA1(a8cc461124c93019310a0cd6de5faf83f13060d6) )
		ROM_CONTINUE(           0x22000, 0x02000 )
		ROM_LOAD( "13317.008",  0x18000, 0x02000, CRC(a081a031) SHA1(c7eef2022bc623bb3399895e092d6cb56c50b5e3) )
		ROM_CONTINUE(           0x24000, 0x02000 )
	
		ROM_REGION( 0x06000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "13302.093", 0x00000, 0x02000, CRC(9beaa403) SHA1(02af1fb98f61b3e7758524978deba094224c8a5d) )
		ROM_LOAD( "13303.094", 0x02000, 0x02000, CRC(2bf711d0) SHA1(bf20177e1b07b12b4ef833072b313a2917d1b65e) )
		ROM_LOAD( "13304.095", 0x04000, 0x02000, CRC(06b8675b) SHA1(8b25a473c03f8210f5d8542c0dc6643c499a0afa) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "13301.070", 0x00000, 0x2000, CRC(aa6d3b9d) SHA1(cb1f148495b029b73f5a32c5162dcc54c0387b4e) )
		/* U92 = Empty */
		/* U69 = Empty */
		/* U91 = Empty */
		/* U68 = Empty */
		/* U90 = Empty */
		/* U67 = Empty */
		/* U89 = Empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_wseries = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1, 0 )
		ROM_LOAD( "13409-01.101",   0x00000, 0x02000, CRC(b5eccf5c) SHA1(1ca781245292399d1b573e6be2edbb79daf9b5d6) )
		ROM_LOAD( "13410-01.102",   0x10000, 0x02000, CRC(dd1ec091) SHA1(ef644c49bbe1cc30ecafab928a0715ea3461a1bd) )
		ROM_CONTINUE(               0x1c000, 0x02000 )
		ROM_LOAD( "13411-01.103",   0x12000, 0x02000, CRC(ec867a0e) SHA1(7b0e390e234056fcc8e6ae9605d633b6ed874e32) )
		ROM_CONTINUE(               0x1e000, 0x02000 )
		ROM_LOAD( "13412-01.104",   0x14000, 0x02000, CRC(2977956d) SHA1(24c8317f10710a5ae4d4e43bc1321a815e47c78f) )
		ROM_CONTINUE(               0x20000, 0x02000 )
		ROM_LOAD( "13413-01.105",   0x16000, 0x02000, CRC(569468a6) SHA1(311257c3b7575cbf442c3afbb42ae3603c03807a) )
		ROM_CONTINUE(               0x22000, 0x02000 )
		ROM_LOAD( "13414-01.106",   0x18000, 0x02000, CRC(b178632d) SHA1(c764e9e69bbd9fd9eb8e950abfd869b8bef71325) )
		ROM_CONTINUE(               0x24000, 0x02000 )
		ROM_LOAD( "13415-01.107",   0x1a000, 0x02000, CRC(20b92eff) SHA1(02156fb36cae6c47b6ae9afcbc27f8f5e9074bbe) )
		ROM_CONTINUE(               0x26000, 0x02000 )
	
		ROM_REGION( 0x28000, REGION_CPU2, 0 )
		ROM_LOAD( "13416-00.u3",  0x00000, 0x02000, CRC(37c960cf) SHA1(e18c72cdbd642e8dfa1184814b65770535a469cb) )
		ROM_LOAD( "13417-00.u4",  0x10000, 0x02000, CRC(97f044b5) SHA1(289a9e19ce46dd039c7edc4d78bd07c355da6dad) )
		ROM_CONTINUE(             0x1c000, 0x02000 )
		ROM_LOAD( "13418-00.u5",  0x12000, 0x02000, CRC(0931cfc0) SHA1(13adb7caf6b1dcf3918277352545fe03e27da3c1) )
		ROM_CONTINUE(             0x1e000, 0x02000 )
		ROM_LOAD( "13419-00.u6",  0x14000, 0x02000, CRC(a7962b5a) SHA1(857c05395b8a1d4aeb3cbac394b673d3bc551b7f) )
		ROM_CONTINUE(             0x20000, 0x02000 )
		ROM_LOAD( "13420-00.u7",  0x16000, 0x02000, CRC(3c275262) SHA1(3a352c184ef3ab87bc7f926eb1af2bef7befcfb6) )
		ROM_CONTINUE(             0x22000, 0x02000 )
		ROM_LOAD( "13421-00.u8",  0x18000, 0x02000, CRC(86f57c80) SHA1(460fb2e1d432840797edafcf4643e23072006c2e) )
		ROM_CONTINUE(             0x24000, 0x02000 )
		ROM_LOAD( "13422-00.u9",  0x1a000, 0x02000, CRC(222e8405) SHA1(a1cc700e06df43847b635858d21ff2e45d8e00ab) )
		ROM_CONTINUE(             0x26000, 0x02000 )
	
		ROM_REGION( 0x0c000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "13401-00.u93", 0x00000, 0x04000, CRC(4ea3e641) SHA1(7628fbf25b5e36d06818d2f9cdc66e2fb15cba4f) )
		ROM_LOAD( "13402-00.u94", 0x04000, 0x04000, CRC(71a8a56c) SHA1(b793a9641dd5d4cd122fb8f5cf1eef5dc3fd475c) )
		ROM_LOAD( "13403-00.u95", 0x08000, 0x04000, CRC(8077ae25) SHA1(15bb1f99e8aea67b9057ef5ef8570f33470a24a3) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		/* U70 = Empty */
		ROM_LOAD( "13404-00.u92",  0x04000, 0x4000, CRC(22da40aa) SHA1(a0306f795f1300d9ab88356ab44117764f6f22a4) )
		ROM_LOAD( "13405-00.u69",  0x08000, 0x4000, CRC(6f65b313) SHA1(2ae85686f679eaa8be15f0cd7d5af61af966c4bd) )
		/* U91 = Empty */
		ROM_LOAD( "13406-00.u68",  0x12000, 0x2000, CRC(bb568693) SHA1(f7f3af505ba5caa330a36cde77b1c2c3cbf83398) )
		ROM_LOAD( "13407-00.u90",  0x14000, 0x4000, CRC(e46ca57f) SHA1(771b43c4a2bcedc6a5bdde14a3c04701032b5713) )
		ROM_LOAD( "13408-00.u67",  0x18000, 0x4000, CRC(be637305) SHA1(a13cbc1644dc06ec52faa0a18340b679c03dc902) )
		/* 89 = Empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_alleymas = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1, 0 )
		ROM_LOAD( "101",   0x00000, 0x02000, CRC(4273e260) SHA1(1b2a726e0a6fe6a60d447c987471a6e1a9e78479) )
		ROM_LOAD( "102",   0x10000, 0x02000, CRC(eb6575aa) SHA1(0876c83d13565937610b5af52aacee1ae6fd59ba) )
		ROM_CONTINUE(      0x1c000, 0x02000 )
		ROM_LOAD( "103",   0x12000, 0x02000, CRC(cc9d778c) SHA1(293ac75d45be4531af1982c0b99597d18bab6a05) )
		ROM_CONTINUE(      0x1e000, 0x02000 )
		ROM_LOAD( "104",   0x14000, 0x02000, CRC(8edb129b) SHA1(f1268617cf18c1c3fd5fb324e882db14cced3d8c) )
		ROM_CONTINUE(      0x20000, 0x02000 )
		ROM_LOAD( "105",   0x16000, 0x02000, CRC(a342dc8e) SHA1(9a6657d66fba5cb1ae3d11e940467b85d47472ea) )
		ROM_CONTINUE(      0x22000, 0x02000 )
		ROM_LOAD( "106",   0x18000, 0x02000, CRC(b396c254) SHA1(06b118ae07d3018209b7ae831f7667cc23d23abd) )
		ROM_CONTINUE(      0x24000, 0x02000 )
		ROM_LOAD( "107",   0x1a000, 0x02000, CRC(3ca13e8c) SHA1(34e00a17ce305c8327674bd79347f01cda14bc8b) )
		ROM_CONTINUE(      0x26000, 0x02000 )
	
		ROM_REGION( 0x28000, REGION_CPU2, 0 )
		ROM_LOAD( "003",  0x00000, 0x02000, CRC(3fee63ae) SHA1(519fe4981dc2c6d025fc2f27af6682103c99dd5e) )
		ROM_LOAD( "004",  0x10000, 0x02000, CRC(d302b5d1) SHA1(77263944d7b4e335fbc3b91d69def6cc85648ec6) )
		ROM_CONTINUE(     0x1c000, 0x02000 )
		ROM_LOAD( "005",  0x12000, 0x02000, CRC(79bdb24d) SHA1(f64c3c5a715d5f4a27e01aeb31e1c43f1f3d5b17) )
		ROM_CONTINUE(     0x1e000, 0x02000 )
		ROM_LOAD( "006",  0x14000, 0x02000, CRC(f0b15d68) SHA1(8017fce4b30e2f3bee74fba82d2a0154b3a1ab6d) )
		ROM_CONTINUE(     0x20000, 0x02000 )
		ROM_LOAD( "007",  0x16000, 0x02000, CRC(6974036c) SHA1(222dd4d8c6d69f6b44b76681a508ff2cfafe1acc) )
		ROM_CONTINUE(     0x22000, 0x02000 )
		ROM_LOAD( "008",  0x18000, 0x02000, CRC(a4357b5a) SHA1(c58505e1ef66641f4da5f29edbb197c5a09a367b) )
		ROM_CONTINUE(     0x24000, 0x02000 )
		ROM_LOAD( "009",  0x1a000, 0x02000, CRC(6d74274e) SHA1(10bb04243eabeb8178884b4e0691c5e1765a1dc4) )
		ROM_CONTINUE(     0x26000, 0x02000 )
	
		ROM_REGION( 0x06000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "093", 0x00000, 0x02000, CRC(54456e6f) SHA1(be41711f57b5b9bd6651399f0df00c538ca1a3a5) )
		ROM_LOAD( "094", 0x02000, 0x02000, CRC(edc240da) SHA1(a812ab0cccb20cd68e9dbe283d4aab92f540af24) )
		ROM_LOAD( "095", 0x04000, 0x02000, CRC(19793ed0) SHA1(2a3cb81726977b29c88d47c90d6e15a7e287c836) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		/* U70 = Empty */
		ROM_LOAD( "092",  0x04000, 0x2000, CRC(a020eab5) SHA1(2f4f51f0eff8a042bf23d5f3ff42166db56e7822) )
		ROM_LOAD( "069",  0x08000, 0x2000, CRC(79abb979) SHA1(dfff8ea4d13dd0db2836e75b6b57f5f3ddac0201) )
		/* U91 = Empty */
		ROM_LOAD( "068",  0x10000, 0x2000, CRC(0c583385) SHA1(4bf5648991441470c4427c88ce17265b447d30d0) )
		ROM_LOAD( "090",  0x14000, 0x2000, CRC(0e1769e3) SHA1(7ca5e3205e790d90e0a39dc88766c582f25147b7) )
		/* U67 = Empty */
		/* U89 = Empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_dangerz = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1, 0 )
		ROM_LOAD( "13823.12t",   0x00000, 0x10000, CRC(31604634) SHA1(0b3d4fea91faf41519888954a21a82827eae6e2a) )
		ROM_LOAD( "13824.13t",   0x10000, 0x10000, CRC(381026c6) SHA1(16c810d162789154e3b5ad38545855370f73b679) )
	
		ROM_REGION( 0x28000, REGION_CPU2, 0 )
		ROM_LOAD( "13818.3",   0x00000, 0x04000, CRC(71863c5b) SHA1(18fdae631d0990815b07934d9cce73a41df9152f) )
		ROM_LOAD( "13817.4",   0x10000, 0x02000, CRC(924bead3) SHA1(ba8dd39db9992b426968e5584c94a8b5ed7c0535) )
		ROM_CONTINUE(          0x1c000, 0x02000 )
		ROM_LOAD( "13818.5",   0x12000, 0x02000, CRC(403bdfea) SHA1(71b959c674e7583670e638ebbd44c75784f565c8) )
		ROM_CONTINUE(          0x1e000, 0x02000 )
		ROM_LOAD( "13819.6",   0x14000, 0x02000, CRC(1fee5f10) SHA1(0aee1e139e13528ec328a8a949f576bfca1892a1) )
		ROM_CONTINUE(          0x20000, 0x02000 )
		ROM_LOAD( "13820.7",   0x16000, 0x02000, CRC(42657a1e) SHA1(d5bb6b6a4bc121fea39809b3b2c891345b12f4d7) )
		ROM_CONTINUE(          0x22000, 0x02000 )
		ROM_LOAD( "13821.8",   0x18000, 0x02000, CRC(92f3e006) SHA1(134a2412ddc700473b70aec6331b1a65db3c7e29) )
		ROM_CONTINUE(          0x24000, 0x02000 )
	
		ROM_REGION( 0x0c000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "13801.93", 0x00000, 0x04000, CRC(f9ff55ec) SHA1(2eab55b3708def97f22a1f13d1faa0bfe19c18e9) )
		ROM_LOAD( "13802.94", 0x04000, 0x04000, CRC(d4adbcbb) SHA1(dfd427d5a0db309cc7e056857c3b63a1b6e7769b) )
		ROM_LOAD( "13803.95", 0x08000, 0x04000, CRC(9178ed76) SHA1(f05568eea53c38f46b16217e63b73194d3a3c500) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "13809.70",  0x00000, 0x4000, CRC(e44eb9f5) SHA1(f15e4262eb96989cbd13a4cbf0b4a0ab390005aa) )
		ROM_LOAD( "13804.92",  0x04000, 0x4000, CRC(6c23f1a5) SHA1(0de32ba7b5796bfe37b142fb892beb223f27c381) )
		ROM_LOAD( "13805.69",  0x08000, 0x4000, CRC(e9c9f38b) SHA1(6a03cf9ab4d06f05d4fb846f14eab22467c79661) )
		ROM_LOAD( "13808.91",  0x0c000, 0x4000, CRC(035534ad) SHA1(e4759992c479d039d6810f129fa2267e0e9527a2) )
		ROM_LOAD( "13806.68",  0x10000, 0x4000, CRC(2dbd64d2) SHA1(eaa015c92daa9562f58e5ed1d153ecd3f1403546) )
		ROM_LOAD( "13808.90",  0x14000, 0x4000, CRC(d5b4985d) SHA1(d9a5e331f6cf9b4abf9f5d739fadf0d6216fe994) )
		ROM_LOAD( "13822.67",  0x18000, 0x4000, CRC(00ff3033) SHA1(ca183f28cb4732ebfc41b6c1651405fee28a9ec6) )
		ROM_LOAD( "13810.89",  0x1c000, 0x4000, CRC(4f645973) SHA1(94bf12db53dc08eb917c17f1ba0d5a40922ff22c) )
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_basebal2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x38000, REGION_CPU1, 0 )
		ROM_LOAD( "14115-00.101",   0x00000, 0x02000, CRC(05231fee) SHA1(d2f4f81309d344576aadb39c209240b901476ac2) )
		ROM_LOAD( "14116-00.102",   0x10000, 0x02000, CRC(e1482ea3) SHA1(a55b8c99428fefd033ac481944b370a4c82ac134) )
		ROM_CONTINUE(               0x1c000, 0x02000 )
		ROM_LOAD( "14117-01.103",   0x12000, 0x02000, CRC(677181dd) SHA1(afc3f33c50551efe5087a3a90f672fe95e3b9087) )
		ROM_CONTINUE(               0x1e000, 0x02000 )
		ROM_LOAD( "14118-01.104",   0x14000, 0x02000, CRC(5f570264) SHA1(09bf8ec7e40292e3764d51988d5ed613920869ec) )
		ROM_CONTINUE(               0x20000, 0x02000 )
		ROM_LOAD( "14119-01.105",   0x16000, 0x02000, CRC(90822145) SHA1(52c872e69055589936d5804334255ffc70a5892e) )
		ROM_CONTINUE(               0x22000, 0x02000 )
		ROM_LOAD( "14120-00.106",   0x18000, 0x02000, CRC(4d2b7217) SHA1(c67cd8361077653f04fc02e8218fd933591d1e45) )
		ROM_CONTINUE(               0x24000, 0x02000 )
		ROM_LOAD( "14121-01.107",   0x1a000, 0x02000, CRC(b987b97c) SHA1(d9fb7142cbb29ce4389f38416584037a398d3fe2) )
		ROM_CONTINUE(               0x26000, 0x02000 )
		/* Extra banks ( referred to as the "top" board). Probably an add-on */
		ROM_LOAD( "14122-01.u2t",   0x28000, 0x02000, CRC(a89882d8) SHA1(fb17b527c65f5de271fa756d7e682449c76bd4ad) )
		ROM_RELOAD(                 0x30000, 0x02000 )
		ROM_LOAD( "14123-01.u3t",   0x2a000, 0x02000, CRC(f9c51e5a) SHA1(a4ed976b9490457b54f2ac6528cf9f4d04732808) )
		ROM_RELOAD(                 0x32000, 0x02000 )
	
		ROM_REGION( 0x28000, REGION_CPU2, 0 )
		ROM_LOAD( "14100-01.u3",  0x00000, 0x02000, CRC(1dffbdaf) SHA1(15630a84c6034a13449cd481bcb6a93bdf009d1f) )
		ROM_LOAD( "14101-01.u4",  0x10000, 0x02000, CRC(c585529c) SHA1(208807c1f8761675903fcf3c590ba3920e980a8b) )
		ROM_CONTINUE(             0x1c000, 0x02000 )
		ROM_LOAD( "14102-01.u5",  0x12000, 0x02000, CRC(ace3f918) SHA1(d393f28d0b8c6faf4d76180208deb023f94277fc) )
		ROM_CONTINUE(             0x1e000, 0x02000 )
		ROM_LOAD( "14103-01.u6",  0x14000, 0x02000, CRC(cd41cf7a) SHA1(bed00824399cea55017d3cc026ae65ddf7edf5e5) )
		ROM_CONTINUE(             0x20000, 0x02000 )
		ROM_LOAD( "14104-01.u7",  0x16000, 0x02000, CRC(9b169e78) SHA1(16ced9610cef997d21668230a5eed6bdfc1df4bd) )
		ROM_CONTINUE(             0x22000, 0x02000 )
		ROM_LOAD( "14105-01.u8",  0x18000, 0x02000, CRC(ec596b43) SHA1(230cdfe0ab4dfd837b3fd66acc961a93e196ce2d) )
		ROM_CONTINUE(             0x24000, 0x02000 )
		ROM_LOAD( "14106-01.u9",  0x1a000, 0x02000, CRC(b9656baa) SHA1(41b25ee6127981b703859c07f730e94f5694faff) )
		ROM_CONTINUE(             0x26000, 0x02000 )
	
		ROM_REGION( 0x0c000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "14112-00.u93", 0x00000, 0x04000, CRC(8ccb1404) SHA1(28ef5a7da1b9edf7ecbba0fd473599df5d181602) )
		ROM_LOAD( "14113-00.u94", 0x04000, 0x04000, CRC(9941a55b) SHA1(6917b70bb2a7a23c0517fde43e9375a7dbd64c18) )
		ROM_LOAD( "14114-00.u95", 0x08000, 0x04000, CRC(b68baf47) SHA1(ea1d5efe696af56ef5b9161c00957b2a9c7ce372) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		/* U70 = Empty */
		ROM_LOAD( "14111-01.u92",  0x04000, 0x4000, CRC(2508a9ad) SHA1(f0a56d1b8dbe57b16dc1b3d21980149bbdcd0068) )
		ROM_LOAD( "14109-00.u69",  0x08000, 0x4000, CRC(b123a28e) SHA1(8d244db422aee9117e901e7d150cdefcbf96dd53) )
		/* U91 = Empty */
		ROM_LOAD( "14108-01.u68",  0x10000, 0x4000, CRC(a1a51383) SHA1(6b734c5d82fb8159768f8849a26f5569cab2f074) )
		ROM_LOAD( "14110-01.u90",  0x14000, 0x4000, CRC(ef01d997) SHA1(693bc42b0aaa436f2734efbe2cfb8c98ad4858c6) )
		ROM_LOAD( "14107-00.u67",  0x18000, 0x4000, CRC(976334e6) SHA1(5b2534f5ba697bd5bad0aef9cefbb7d1c421c06b) )
		/* 89 = Empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_dblplay = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x38000, REGION_CPU1, 0 )
		ROM_LOAD( "15018-01.101",   0x00000, 0x02000, CRC(17b6af29) SHA1(00865927d74f735ed9bbe635bb554d408bf7f856) )
		ROM_LOAD( "15019-01.102",   0x10000, 0x02000, CRC(9fc8205e) SHA1(2b783c406539a3d06adacd6b15c8edd86b994561) )
		ROM_CONTINUE(               0x1c000, 0x02000 )
		ROM_LOAD( "15020-01.103",   0x12000, 0x02000, CRC(4edcc091) SHA1(5db2641fb92eeba22b731074e2818484aaa247a0) )
		ROM_CONTINUE(               0x1e000, 0x02000 )
		ROM_LOAD( "15021-01.104",   0x14000, 0x02000, CRC(a0eba1c7) SHA1(5d1afd2e6f694416ab64aec334ce6f7803dac93e) )
		ROM_CONTINUE(               0x20000, 0x02000 )
		ROM_LOAD( "15022-01.105",   0x16000, 0x02000, CRC(7bbfe0b7) SHA1(551e4d48ffc8f3660d59bb4e59f73d438f4eb20d) )
		ROM_CONTINUE(               0x22000, 0x02000 )
		ROM_LOAD( "15023-01.106",   0x18000, 0x02000, CRC(bbedae34) SHA1(4c15f63ea6ac822a6c9bc5c3b9f9e5a62e57b88c) )
		ROM_CONTINUE(               0x24000, 0x02000 )
		ROM_LOAD( "15024-01.107",   0x1a000, 0x02000, CRC(02afcf52) SHA1(686332740733d92f87fb004de85be4cb9cbaabc0) )
		ROM_CONTINUE(               0x26000, 0x02000 )
		/* Extra banks ( referred to as the "top" board). Probably an add-on */
		ROM_LOAD( "15025-01.u2t",   0x28000, 0x02000, CRC(1c959895) SHA1(efd40c1775f8283162602fdb490bfc18ee784a12) )
		ROM_RELOAD(                 0x30000, 0x02000 )
		ROM_LOAD( "15026-01.u3t",   0x2a000, 0x02000, CRC(ed5196d6) SHA1(03dbc4fa30cee9e2cc132d1fa1e45ac9f503705a) )
		ROM_RELOAD(                 0x32000, 0x02000 )
		ROM_LOAD( "15027-01.u4t",   0x2c000, 0x02000, CRC(9b1e72e9) SHA1(09609835b6951d3dc271e48c8bf91cbff99b6f50) )
		ROM_CONTINUE(               0x34000, 0x02000 )
	
		ROM_REGION( 0x28000, REGION_CPU2, 0 )
		ROM_LOAD( "15000-01.u03",  0x00000, 0x02000, CRC(208a920a) SHA1(3544bd79e008f696a9ff400aad8bf0eb52a42451) )
		ROM_LOAD( "15001-01.u04",  0x10000, 0x02000, CRC(751c40d6) SHA1(00e0cba02916c641c85748a1b15af192aca5d60f) )
		ROM_CONTINUE(              0x1c000, 0x02000 )
		ROM_LOAD( "14402-01.u05",  0x12000, 0x02000, CRC(5ffaec36) SHA1(edb36f3f480f6a6ed3b030f7b90e6622b451d086) )
		ROM_CONTINUE(              0x1e000, 0x02000 )
		ROM_LOAD( "14403-01.u06",  0x14000, 0x02000, CRC(48d6d9d3) SHA1(6208f16883867448f478eb49155cd5dbcd25236b) )
		ROM_CONTINUE(              0x20000, 0x02000 )
		ROM_LOAD( "15004-01.u07",  0x16000, 0x02000, CRC(6a7acebc) SHA1(133258a78fdb7b8dc08312e8619767fa694f175e) )
		ROM_CONTINUE(              0x22000, 0x02000 )
		ROM_LOAD( "15005-01.u08",  0x18000, 0x02000, CRC(69d487c9) SHA1(217560f9cbb196970fb9ccbe32c640b376321b7e) )
		ROM_CONTINUE(              0x24000, 0x02000 )
		ROM_LOAD( "15006-01.u09",  0x1a000, 0x02000, CRC(ab3aac49) SHA1(699a6a66e6b35f1b287ff1ab3a12365dbdc16041) )
		ROM_CONTINUE(              0x26000, 0x02000 )
	
		ROM_REGION( 0x0c000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "15015-01.u93", 0x00000, 0x04000, CRC(8ccb1404) SHA1(28ef5a7da1b9edf7ecbba0fd473599df5d181602) )
		ROM_LOAD( "15016-01.u94", 0x04000, 0x04000, CRC(9941a55b) SHA1(6917b70bb2a7a23c0517fde43e9375a7dbd64c18) )
		ROM_LOAD( "15017-01.u95", 0x08000, 0x04000, CRC(b68baf47) SHA1(ea1d5efe696af56ef5b9161c00957b2a9c7ce372) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		/* U70 = Empty */
		ROM_LOAD( "15014-01.u92",  0x04000, 0x4000, CRC(2508a9ad) SHA1(f0a56d1b8dbe57b16dc1b3d21980149bbdcd0068) )
		ROM_LOAD( "15009-01.u69",  0x08000, 0x4000, CRC(b123a28e) SHA1(8d244db422aee9117e901e7d150cdefcbf96dd53) )
		/* U91 = Empty */
		ROM_LOAD( "15008-01.u68",  0x10000, 0x4000, CRC(a1a51383) SHA1(6b734c5d82fb8159768f8849a26f5569cab2f074) )
		ROM_LOAD( "15012-01.u90",  0x14000, 0x4000, CRC(ef01d997) SHA1(693bc42b0aaa436f2734efbe2cfb8c98ad4858c6) )
		ROM_LOAD( "15007-01.u67",  0x18000, 0x4000, CRC(976334e6) SHA1(5b2534f5ba697bd5bad0aef9cefbb7d1c421c06b) )
		/* 89 = Empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_strkzone = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x38000, REGION_CPU1, 0 )
		ROM_LOAD( "strkzone.101",   0x00000, 0x04000, CRC(8d83a611) SHA1(d17114559c8d60e3107895bdcb1886cc843b624c) )
		ROM_LOAD( "strkzone.102",   0x10000, 0x02000, CRC(3859e67d) SHA1(0a0d18c16fa5becae2ecc147dfafadc16dee8d2f) )
		ROM_CONTINUE(               0x1c000, 0x02000 )
		ROM_LOAD( "strkzone.103",   0x12000, 0x02000, CRC(cdd83bfb) SHA1(6d5c1e9e951a0bfdd79fd54b06e2e4f1bf8e37b4) )
		ROM_CONTINUE(               0x1e000, 0x02000 )
		ROM_LOAD( "strkzone.104",   0x14000, 0x02000, CRC(be280212) SHA1(f48f2edd41bd4f2729ee6c15fb228c758da40ea1) )
		ROM_CONTINUE(               0x20000, 0x02000 )
		ROM_LOAD( "strkzone.105",   0x16000, 0x02000, CRC(afb63390) SHA1(42df802ca2a247b971ae274bd6f7d1f1e5893fe3) )
		ROM_CONTINUE(               0x22000, 0x02000 )
		ROM_LOAD( "strkzone.106",   0x18000, 0x02000, CRC(e853b9f6) SHA1(07cc7bd0053422d68526a6e1b68165db60af6429) )
		ROM_CONTINUE(               0x24000, 0x02000 )
		ROM_LOAD( "strkzone.107",   0x1a000, 0x02000, CRC(1b4b6c2d) SHA1(9cd5e5ce7bc3088f14b6cbbd7c2d5b5e69a7bc11) )
		ROM_CONTINUE(               0x26000, 0x02000 )
		/* Extra banks ( referred to as the "top" board). Probably an add-on */
		ROM_LOAD( "strkzone.u2t",   0x28000, 0x02000, CRC(8e0af06f) SHA1(ad277433a2c97c388e626a0ce9119466dff85d37) )
		ROM_RELOAD(                 0x30000, 0x02000 )
		ROM_LOAD( "strkzone.u3t",   0x2a000, 0x02000, CRC(909d35f3) SHA1(2ec51b1591990cf13b71d6c343bfe9540d3c2b69) )
		ROM_RELOAD(                 0x32000, 0x02000 )
		ROM_LOAD( "strkzone.u4t",   0x2c000, 0x02000, CRC(9b1e72e9) SHA1(09609835b6951d3dc271e48c8bf91cbff99b6f50) )
		ROM_CONTINUE(               0x34000, 0x02000 )
	
		ROM_REGION( 0x28000, REGION_CPU2, 0 )
		ROM_LOAD( "strkzone.u3",  0x00000, 0x02000, CRC(40258fbe) SHA1(4a68dbf050455bf15fadef20f615ab1de194a1c2) )
		ROM_LOAD( "strkzone.u4",  0x10000, 0x02000, CRC(df7f2604) SHA1(4aed43905fedf84de683dea9785a73d6d9f89713) )
		ROM_CONTINUE(             0x1c000, 0x02000 )
		ROM_LOAD( "strkzone.u5",  0x12000, 0x02000, CRC(37885206) SHA1(60428a4ad16c452e7a90c6d2617c9905cef8ed0b) )
		ROM_CONTINUE(             0x1e000, 0x02000 )
		ROM_LOAD( "strkzone.u6",  0x14000, 0x02000, CRC(6892dc4f) SHA1(be0c8c0afed925e2e373e10b42c00f5ab6cfed40) )
		ROM_CONTINUE(             0x20000, 0x02000 )
		ROM_LOAD( "strkzone.u7",  0x16000, 0x02000, CRC(6ac8f87c) SHA1(cf820922f09d503bdd73e20f9e5e786910ab2ab8) )
		ROM_CONTINUE(             0x22000, 0x02000 )
		ROM_LOAD( "strkzone.u8",  0x18000, 0x02000, CRC(4b6d3725) SHA1(e7d1d31df3fd10dd51a6969a0ca688a4b7e3d3f1) )
		ROM_CONTINUE(             0x24000, 0x02000 )
		ROM_LOAD( "strkzone.u9",  0x1a000, 0x02000, CRC(ab3aac49) SHA1(699a6a66e6b35f1b287ff1ab3a12365dbdc16041) )
		ROM_CONTINUE(             0x26000, 0x02000 )
	
		ROM_REGION( 0x0c000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "strkzone.u93", 0x00000, 0x04000, CRC(8ccb1404) SHA1(28ef5a7da1b9edf7ecbba0fd473599df5d181602) )
		ROM_LOAD( "strkzone.u94", 0x04000, 0x04000, CRC(9941a55b) SHA1(6917b70bb2a7a23c0517fde43e9375a7dbd64c18) )
		ROM_LOAD( "strkzone.u95", 0x08000, 0x04000, CRC(b68baf47) SHA1(ea1d5efe696af56ef5b9161c00957b2a9c7ce372) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		/* U70 = Empty */
		ROM_LOAD( "strkzone.u92",  0x04000, 0x4000, CRC(2508a9ad) SHA1(f0a56d1b8dbe57b16dc1b3d21980149bbdcd0068) )
		ROM_LOAD( "strkzone.u69",  0x08000, 0x4000, CRC(b123a28e) SHA1(8d244db422aee9117e901e7d150cdefcbf96dd53) )
		/* U91 = Empty */
		ROM_LOAD( "strkzone.u68",  0x10000, 0x4000, CRC(a1a51383) SHA1(6b734c5d82fb8159768f8849a26f5569cab2f074) )
		ROM_LOAD( "strkzone.u90",  0x14000, 0x4000, CRC(ef01d997) SHA1(693bc42b0aaa436f2734efbe2cfb8c98ad4858c6) )
		ROM_LOAD( "strkzone.u67",  0x18000, 0x4000, CRC(976334e6) SHA1(5b2534f5ba697bd5bad0aef9cefbb7d1c421c06b) )
		/* 89 = Empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_redlin2p = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1, 0 )
		ROM_LOAD( "13932-01.23t", 0x00000, 0x10000, CRC(ecdf0fbe) SHA1(186e1cecadb84b5085d9ccbf6512553a80b9ebfb) )
		ROM_LOAD( "13931-01.22t", 0x10000, 0x10000, CRC(16d01978) SHA1(6882eac35a54a91f12a8d37a4f83d7ca0dc65ef5) )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 )
		ROM_LOAD( "13907-01.u3",  0x00000, 0x04000, CRC(b760d63e) SHA1(b117fd1f96b861abefedd049a305b60c964aadad) )
		ROM_LOAD( "13908-01.u4",  0x10000, 0x02000, CRC(a30739d3) SHA1(eefce1f11ead0ca4c7fc3ed3102fbdb8bfbf3cbc) )
		ROM_CONTINUE(             0x1c000, 0x02000 )
		ROM_LOAD( "13909-01.u5",  0x12000, 0x02000, CRC(aaf16ad7) SHA1(d08d224ecb824204143e9fd1b1657dc2dd6035e6) )
		ROM_CONTINUE(             0x1e000, 0x02000 )
		ROM_LOAD( "13910-01.u6",  0x14000, 0x02000, CRC(d03469eb) SHA1(78bda66821cc458be58ae179c0d39879b9f02282) )
		ROM_CONTINUE(             0x20000, 0x02000 )
		ROM_LOAD( "13911-01.u7",  0x16000, 0x02000, CRC(8ee1f547) SHA1(05ef34786f0e26f5d891f5b25c007956b92bf0cb) )
		ROM_CONTINUE(             0x22000, 0x02000 )
		ROM_LOAD( "13912-01.u8",  0x18000, 0x02000, CRC(e5b57eac) SHA1(b31f38ddfdf896cc90703df486b840214ed16a7f) )
		ROM_CONTINUE(             0x24000, 0x02000 )
		ROM_LOAD( "13913-01.u9",  0x1a000, 0x02000, CRC(02886071) SHA1(699f13677a3e76e8ec2ec73e62d4da4038f9f85d) )
		ROM_CONTINUE(             0x26000, 0x02000 )
	
		ROM_REGION( 0x100000, REGION_CPU3, 0 )
		ROM_LOAD16_BYTE( "17t",    0x0e0001, 0x10000, CRC(8d26f221) SHA1(cd5b1d88fec0ff1ab7af554a9fcffc43d33a12e7) )
		ROM_LOAD16_BYTE( "28t",    0x0e0000, 0x10000, CRC(7aa21b2c) SHA1(5fd9f49d4bb1dc28393b9df76dfa19e28677639b) )
	
		ROM_REGION( 0x0c000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "13930-01.u93", 0x00000, 0x04000, CRC(0721f42e) SHA1(fe3d447316b1e6c7c1b6849319fad1aebe5e6aa1) )
		ROM_LOAD( "13929-01.u94", 0x04000, 0x04000, CRC(1522e7b2) SHA1(540fc55013a22a5afb32a89b42ef9b11dbe36d97) )
		ROM_LOAD( "13928-01.u95", 0x08000, 0x04000, CRC(c321b5d1) SHA1(d1524165e71fe200cab6fd6f6327da0e6efc6868) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "13920-01.u70",  0x00000, 0x4000, CRC(f343d34a) SHA1(161348e082afeb69862c3752f4dd536166edad21) )
		ROM_LOAD( "13921-01.u92",  0x04000, 0x4000, CRC(c9ba8d41) SHA1(777a504e3ffe6c3da94f71eb1b14e05dc861db66) )
		ROM_LOAD( "13922-01.u69",  0x08000, 0x4000, CRC(276cfba0) SHA1(4b252f21e2d1314801cf9329ed9383ff9158c382) )
		ROM_LOAD( "13923-01.u91",  0x0c000, 0x4000, CRC(4a88ea34) SHA1(e79cc404f435789ef8f62c6bef03af1b9b89caeb) )
		ROM_LOAD( "13924-01.u68",  0x10000, 0x4000, CRC(3995cb7e) SHA1(4a77d3c71e2a8240a21a82ac946804895f4af959) )
		/* 90 = empty / missing */
		ROM_LOAD( "13926-01.u67",  0x18000, 0x4000, CRC(daa30add) SHA1(e9c066406c2d50ab3fc8eea8a97a181ad8c950c7) )
		ROM_LOAD( "13927-01.u89",  0x1c000, 0x4000, CRC(30e60fb5) SHA1(374c84358d2b7ae7c74321996797af9adbc2a155) )
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_quarterb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1, 0 )
		ROM_LOAD( "15219-05.49t", 0x00000, 0x10000, CRC(ff653e4f) SHA1(761e18ccbdc1c559648c47d06ee21a8a4710c5a0) )
		ROM_LOAD( "15218-05.48t", 0x10000, 0x10000, CRC(34b83d81) SHA1(0425638063872ff562939439871631f7aa642182) )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 )
		ROM_LOAD( "15200-01.u3",  0x00000, 0x04000, CRC(83297861) SHA1(9d836f647491af945de021fbf8c75159b32c22c9) )
		ROM_LOAD( "15201-01.u4",  0x10000, 0x02000, CRC(af8dbdab) SHA1(663a32b55ef0337074a223288e59b53c4a10b616) )
		ROM_CONTINUE(             0x1c000, 0x02000 )
		ROM_LOAD( "15202-01.u5",  0x12000, 0x02000, CRC(3eeecb3d) SHA1(ee2a7a2dba8137c6e414f74300b445db9141a49d) )
		ROM_CONTINUE(             0x1e000, 0x02000 )
		ROM_LOAD( "15203-01.u6",  0x14000, 0x02000, CRC(b9c5b663) SHA1(5948f77301446dcab64d787ae6f2c49bee666a7b) )
		ROM_CONTINUE(             0x20000, 0x02000 )
		ROM_LOAD( "15204-01.u7",  0x16000, 0x02000, CRC(c68821b7) SHA1(bd68282453ab2752a31681a2c5f31361a704bc07) )
		ROM_CONTINUE(             0x22000, 0x02000 )
		ROM_LOAD( "15205-01.u8",  0x18000, 0x02000, CRC(2be843a9) SHA1(a77c84ab95e20dfef09ff2c34b302b2c4ec87f02) )
		ROM_CONTINUE(             0x24000, 0x02000 )
		ROM_LOAD( "15206-01.u9",  0x1a000, 0x02000, CRC(6bf8d4ab) SHA1(cc9b3f1e651b2a667f17553aac655f0039983890) )
		ROM_CONTINUE(             0x26000, 0x02000 )
	
		ROM_REGION( 0x100000, REGION_CPU3, 0 )
		ROM_LOAD16_BYTE( "15222-02.45t", 0x040001, 0x10000, CRC(710bdc76) SHA1(610f7baa17adf2d16c9494b05556b49ae376fe81) )
		ROM_LOAD16_BYTE( "15225-02.62t", 0x040000, 0x10000, CRC(041cecde) SHA1(91556a393d61979d3e92f75142832353e9081a15) )
		ROM_LOAD16_BYTE( "15221-02.44t", 0x060001, 0x10000, CRC(e0459ddb) SHA1(811896fe3398ecc322ca20c2376b715b2d44992e) )
		ROM_LOAD16_BYTE( "15224-02.61t", 0x060000, 0x10000, CRC(9027c579) SHA1(47177f9c42d134ec44a8b1aad17012dd971cf1fd) )
		ROM_LOAD16_BYTE( "15220-02.43t", 0x0e0001, 0x10000, CRC(48a8a018) SHA1(f50d66feeab32f1edc47f4b3f33e579c06fd979e) )
		ROM_LOAD16_BYTE( "15223-02.60t", 0x0e0000, 0x10000, CRC(6a299766) SHA1(4e5b1f930f668302496a314bbe8876a21012fb20) )
	
		ROM_REGION( 0x0c000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "15215-01.u93", 0x00000, 0x04000, CRC(4fb678d7) SHA1(ca729ca8d2ba1e22a7b650ddfc330e85e294e48f) )
		ROM_LOAD( "lelqb.94",     0x04000, 0x04000, CRC(7b57a44c) SHA1(b28ecdc8b1579e677a58a4b5257d5d754783148f) )
		ROM_LOAD( "lelqb.95",     0x08000, 0x04000, CRC(29bc33fd) SHA1(e85d20b24144c5b0f6ffa6dc96f1abb35bce437a) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "15210-01.u70",  0x00000, 0x4000, CRC(a5aea20e) SHA1(c5b40bdb63cd29386f73e69b814c37eb43dadbac) )
		ROM_LOAD( "15214-01.u92",  0x04000, 0x4000, CRC(36f261ca) SHA1(d42868c9ace5bec75b74268393755340ccafea59) )
		ROM_LOAD( "15209-01.u69",  0x08000, 0x4000, CRC(0f5d74a4) SHA1(76bd78153a5f986ffdd0db606a1e2a0b895b4832) )
		/* 91 = empty */
		ROM_LOAD( "15208-01.u68",  0x10000, 0x4000, CRC(0319aec7) SHA1(e4f14ce9b4712c1cee69141165d187e9068101fc) )
		ROM_LOAD( "15212-01.u90",  0x14000, 0x4000, CRC(38b298d6) SHA1(fa22d8d5fa66f1f7f052541f21408a6d755a1317) )
		ROM_LOAD( "15207-01.u67",  0x18000, 0x4000, CRC(5ff86aad) SHA1(6c2704dc4a934270e7080c610181018c9c5e10c5) )
		/* 89 = empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_quartrba = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1, 0 )
		ROM_LOAD( "15219-02.49t",   0x00000, 0x10000, CRC(7fbe1e5a) SHA1(a4af54328935e348f2903fe7f7dea8612660b899) )
		ROM_LOAD( "15218-02.48t",   0x10000, 0x10000, CRC(6fbd4b27) SHA1(8146f276af5e3ef968851fa95c8f979b8b969ef6) )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 )
		ROM_LOAD( "15200-01.u3",  0x00000, 0x04000, CRC(83297861) SHA1(9d836f647491af945de021fbf8c75159b32c22c9) )
		ROM_LOAD( "15201-01.u4",  0x10000, 0x02000, CRC(af8dbdab) SHA1(663a32b55ef0337074a223288e59b53c4a10b616) )
		ROM_CONTINUE(             0x1c000, 0x02000 )
		ROM_LOAD( "15202-01.u5",  0x12000, 0x02000, CRC(3eeecb3d) SHA1(ee2a7a2dba8137c6e414f74300b445db9141a49d) )
		ROM_CONTINUE(             0x1e000, 0x02000 )
		ROM_LOAD( "15203-01.u6",  0x14000, 0x02000, CRC(b9c5b663) SHA1(5948f77301446dcab64d787ae6f2c49bee666a7b) )
		ROM_CONTINUE(             0x20000, 0x02000 )
		ROM_LOAD( "15204-01.u7",  0x16000, 0x02000, CRC(c68821b7) SHA1(bd68282453ab2752a31681a2c5f31361a704bc07) )
		ROM_CONTINUE(             0x22000, 0x02000 )
		ROM_LOAD( "15205-01.u8",  0x18000, 0x02000, CRC(2be843a9) SHA1(a77c84ab95e20dfef09ff2c34b302b2c4ec87f02) )
		ROM_CONTINUE(             0x24000, 0x02000 )
		ROM_LOAD( "15206-01.u9",  0x1a000, 0x02000, CRC(6bf8d4ab) SHA1(cc9b3f1e651b2a667f17553aac655f0039983890) )
		ROM_CONTINUE(             0x26000, 0x02000 )
	
		ROM_REGION( 0x100000, REGION_CPU3, 0 )
		ROM_LOAD16_BYTE( "15222-01.45t", 0x040001, 0x10000, CRC(722d1a19) SHA1(b7c7c606798c4357cee58b64d95f2f6a6172d72e) )
		ROM_LOAD16_BYTE( "15225-01.62t", 0x040000, 0x10000, CRC(f8c20496) SHA1(5f948a56743127e19d9fbd888b546ce82c0b05f6) )
		ROM_LOAD16_BYTE( "15221-01.44t", 0x060001, 0x10000, CRC(bc6abaaf) SHA1(2ca9116c1861d7089679de034c2255bc51179338) )
		ROM_LOAD16_BYTE( "15224-01.61t", 0x060000, 0x10000, CRC(7ce3c3b7) SHA1(fa85a9159895e26dff03cc6955fdd880213a0dec) )
		ROM_LOAD16_BYTE( "15220-01.43t", 0x0e0001, 0x10000, CRC(ccb6c8d7) SHA1(bafe1ba6259f396cfa91fc6d2ff7832199763f3e) )
		ROM_LOAD16_BYTE( "15223-01.60t", 0x0e0000, 0x10000, CRC(c0ee425d) SHA1(4edbd62b8bb7f814e7ffa3111e6fb1e8b6615ae8) )
	
		ROM_REGION( 0x0c000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "15215-01.u93", 0x00000, 0x04000, CRC(4fb678d7) SHA1(ca729ca8d2ba1e22a7b650ddfc330e85e294e48f) )
		ROM_LOAD( "lelqb.94",     0x04000, 0x04000, CRC(7b57a44c) SHA1(b28ecdc8b1579e677a58a4b5257d5d754783148f) )
		ROM_LOAD( "lelqb.95",     0x08000, 0x04000, CRC(29bc33fd) SHA1(e85d20b24144c5b0f6ffa6dc96f1abb35bce437a) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "15210-01.u70",  0x00000, 0x4000, CRC(a5aea20e) SHA1(c5b40bdb63cd29386f73e69b814c37eb43dadbac) )
		ROM_LOAD( "15214-01.u92",  0x04000, 0x4000, CRC(36f261ca) SHA1(d42868c9ace5bec75b74268393755340ccafea59) )
		ROM_LOAD( "15209-01.u69",  0x08000, 0x4000, CRC(0f5d74a4) SHA1(76bd78153a5f986ffdd0db606a1e2a0b895b4832) )
		/* 91 = empty */
		ROM_LOAD( "15208-01.u68",  0x10000, 0x4000, CRC(0319aec7) SHA1(e4f14ce9b4712c1cee69141165d187e9068101fc) )
		ROM_LOAD( "15212-01.u90",  0x14000, 0x4000, CRC(38b298d6) SHA1(fa22d8d5fa66f1f7f052541f21408a6d755a1317) )
		ROM_LOAD( "15207-01.u67",  0x18000, 0x4000, CRC(5ff86aad) SHA1(6c2704dc4a934270e7080c610181018c9c5e10c5) )
		/* 89 = empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_viper = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1, 0 )
		ROM_LOAD( "15617-03.49t",   0x00000, 0x10000, CRC(7e4688a6) SHA1(282f98d22447b2f93d6f328a351ce1613a33069b) )
		ROM_LOAD( "15616-03.48t",   0x10000, 0x10000, CRC(3fe2f0bf) SHA1(2a1a7d1654e5f45a5b30374596865006e80928f5) )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 )
		ROM_LOAD( "15600-02.u3", 0x00000, 0x02000, CRC(0f57f68a) SHA1(2cb132eb41da5f8a90f83e54a6a8c00a62b66949) )
		ROM_LOAD( "viper.u2t",   0x10000, 0x10000, CRC(4043d4ee) SHA1(70ebb98444f13a25cdcd8d31ee47a20af7df5613) )
		ROM_LOAD( "viper.u3t",   0x20000, 0x10000, CRC(213bc02b) SHA1(53fadd81a0138525d3d39fd9c2ea258f90b2e6e7) )
		ROM_LOAD( "viper.u4t",   0x30000, 0x10000, CRC(ce0b95b4) SHA1(1a322714ce1e9e5589da9966f2e684e9a2c22592) )
	
		ROM_REGION( 0x100000, REGION_CPU3, 0 )
		ROM_LOAD16_BYTE( "15620-02.45t", 0x040001, 0x10000, CRC(7380ece1) SHA1(c131c80c67503785ba1ec5b31366cd72f0f7e0e3) )
		ROM_LOAD16_BYTE( "15623-02.62t", 0x040000, 0x10000, CRC(2921d8f9) SHA1(5ce6752ef3928b40263efdcd81fae376e2d86e36) )
		ROM_LOAD16_BYTE( "15619-02.44t", 0x060001, 0x10000, CRC(c8507cc2) SHA1(aae9f19b3bc6790a137d94e3c4bb3e61e8670b42) )
		ROM_LOAD16_BYTE( "15622-02.61t", 0x060000, 0x10000, CRC(32dfda37) SHA1(bbd643239add553e61735c2997bb4ffdbe67d9e1) )
		ROM_LOAD16_BYTE( "15618-02.43t", 0x0e0001, 0x10000, CRC(5562e0c3) SHA1(4c7b0cedc5adc4e24a1cd6010591205ddb16d554) )
		ROM_LOAD16_BYTE( "15621-02.60t", 0x0e0000, 0x10000, CRC(cb468f2b) SHA1(f37596c781b1d7c49d8f62d289c15a2ae0d752cc) )
	
		ROM_REGION( 0x0c000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "15609-01.u93", 0x00000, 0x04000, CRC(08ad92e9) SHA1(6eaffd58f03db3a67871ce7390b01754842e2574) )
		ROM_LOAD( "15610-01.u94", 0x04000, 0x04000, CRC(d4e56dfb) SHA1(0fc83847b8629534b15f9366f197c87e3c81c61a) )
		ROM_LOAD( "15611-01.u95", 0x08000, 0x04000, CRC(3a2c46fb) SHA1(e96849447852a9922e72f7f1908c76fea3c603c4) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "15604-01.u70",  0x00000, 0x4000, CRC(7e3b0cce) SHA1(d9002df27e3de28d40a9cfb081512526987377b2) )
		ROM_LOAD( "15608-01.u92",  0x04000, 0x4000, CRC(a9bde0ef) SHA1(84f55bc62fc49ae0232ada2ac192c5c8a2519703) )
		ROM_LOAD( "15603-01.u69",  0x08000, 0x4000, CRC(aecc9516) SHA1(513ae810d62d5df29a96a567a7c024f12c6837d5) )
		ROM_LOAD( "15607-01.u91",  0x0c000, 0x4000, CRC(14f06f88) SHA1(7e76b5b7d74635dff2dd2245d345beee5c0ee46e) )
		ROM_LOAD( "15602-01.u68",  0x10000, 0x4000, CRC(4ef613ad) SHA1(b08445056038fdef90bd9de0a4effdfd18f81e15) )
		ROM_LOAD( "15606-01.u90",  0x14000, 0x4000, CRC(3c2e8e76) SHA1(f526240df82e14102854de8e391571f747dfa405) )
		ROM_LOAD( "15601-01.u67",  0x18000, 0x4000, CRC(dc7006cd) SHA1(d828b9c7a43c1b37aa55d1c5891fe0744ea78595) )
		ROM_LOAD( "15605-01.u89",  0x1c000, 0x4000, CRC(4aa9c788) SHA1(77095d7ce4949db3c39c19d131d2902e4099b6d4) )
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_teamqb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1, 0 )
		ROM_LOAD( "15618-03.58t",   0x00000, 0x10000, CRC(b32568dc) SHA1(92fb8dd89cc7838129e7b106bc0e35107372904f) )
		ROM_LOAD( "15619-03.59t",   0x10000, 0x10000, CRC(40b3319f) SHA1(26c6e26cd440fc7e1ab5ee7536e17a1c00b83f44) )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 )
		ROM_LOAD( "15600-01.u3",   0x00000, 0x02000, CRC(46615844) SHA1(cb73dd73d389d1f6a5da91d0935b5461649ba706) )
		ROM_LOAD( "15601-01.u2t",  0x10000, 0x10000, CRC(8e523c58) SHA1(7f1133144177c39849fd6355bf9250895b2d597f) )
		ROM_LOAD( "15602-01.u3t",  0x20000, 0x10000, CRC(545b27a1) SHA1(1e8beebc1384cf6513bff7c2381ca214967ff135) )
		ROM_LOAD( "15603-01.u4t",  0x30000, 0x10000, CRC(cdc9c09d) SHA1(8641312638507d027948c17c042417b0d0362714) )
		ROM_LOAD( "15604-01.u5t",  0x40000, 0x10000, CRC(3c03e92e) SHA1(7cd9b02bbf1d30a8432632d902c4ea6c8108210b) )
		ROM_LOAD( "15605-01.u6t",  0x50000, 0x10000, CRC(cdf7d19c) SHA1(577c8bf5964d77dbfef4840c53ae40cda68bf4f3) )
		ROM_LOAD( "15606-01.u7t",  0x60000, 0x10000, CRC(8eeb007c) SHA1(6f9d4132c7e5e6502108cb3e8eab9114f07848b4) )
		ROM_LOAD( "15607-01.u8t",  0x70000, 0x10000, CRC(57cb6d2d) SHA1(56e364aedca25935a5cd7ab4460d9213fcc58b4a) )
	
		ROM_REGION( 0x100000, REGION_CPU3, 0 )
		ROM_LOAD16_BYTE( "15623-01.25t", 0x040001, 0x10000, CRC(710bdc76) SHA1(610f7baa17adf2d16c9494b05556b49ae376fe81) )
		ROM_LOAD16_BYTE( "15620-01.13t", 0x040000, 0x10000, CRC(7e5cb8ad) SHA1(aaff4e93053638955b95951dceea3b35e842e80f) )
		ROM_LOAD16_BYTE( "15624-01.26t", 0x060001, 0x10000, CRC(dd090d33) SHA1(09a3fa4fa3a50c6692be2bc5fec2c4e9a5072d5d) )
		ROM_LOAD16_BYTE( "15621-01.14t", 0x060000, 0x10000, CRC(f68c68c9) SHA1(a7d77c36831d455a8c36d2156460287cf28c9694) )
		ROM_LOAD16_BYTE( "15625-01.27t", 0x0e0001, 0x10000, CRC(ac442523) SHA1(d05dcc413eb39b0938890ef80ec7b636773bb1a3) )
		ROM_LOAD16_BYTE( "15622-01.15t", 0x0e0000, 0x10000, CRC(9e84509a) SHA1(4c3a3e5192ba6c38d8391eedf817350795bddb8f) )
	
		ROM_REGION( 0x0c000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "15615-01.u93", 0x00000, 0x04000, CRC(a7ea6a87) SHA1(5cfd9ed6a5ffc8e86d18b7d8496761b9086b6368) )
		ROM_LOAD( "15616-01.u94", 0x04000, 0x04000, CRC(4a9b3900) SHA1(00398cc5056c999673604e414c9c0338d83b13d4) )
		ROM_LOAD( "15617-01.u95", 0x08000, 0x04000, CRC(2cd95edb) SHA1(939ff97562535b05f427186b085a74a8fe5a332a) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "15611-01.u70",  0x00000, 0x4000, CRC(bf2695fb) SHA1(58a6d1e9e83912f7567eabdf54278db85061c284) )
		ROM_LOAD( "15614-01.u92",  0x04000, 0x4000, CRC(c93fd870) SHA1(1086334496a4d1900a2d697cbd2575a77df89d65) )
		ROM_LOAD( "15610-01.u69",  0x08000, 0x4000, CRC(3e5b786f) SHA1(13d2ab7b6a1182933272b597718d3e715b547a10) )
		/* 91 = empty */
		ROM_LOAD( "15609-01.u68",  0x10000, 0x4000, CRC(0319aec7) SHA1(e4f14ce9b4712c1cee69141165d187e9068101fc) )
		ROM_LOAD( "15613-01.u90",  0x14000, 0x4000, CRC(4805802e) SHA1(a121aec2b0340773288687baccf85519c0ef3160) )
		ROM_LOAD( "15608-01.u67",  0x18000, 0x4000, CRC(78f0fd2b) SHA1(e83b1106411bb03c64a985a08c5f20c2eb397140) )
		/* 89 = empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_teamqb2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1, 0 )
		ROM_LOAD( "15618-03.58t",   0x00000, 0x10000, CRC(b32568dc) SHA1(92fb8dd89cc7838129e7b106bc0e35107372904f) )
		ROM_LOAD( "15619-02.59t",   0x10000, 0x10000, CRC(6d533714) SHA1(ab177aaa5b034250c85bde0c2441902f72d44f42) )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 )
		ROM_LOAD( "15600-01.u3",   0x00000, 0x02000, CRC(46615844) SHA1(cb73dd73d389d1f6a5da91d0935b5461649ba706) )
		ROM_LOAD( "15601-01.u2t",  0x10000, 0x10000, CRC(8e523c58) SHA1(7f1133144177c39849fd6355bf9250895b2d597f) )
		ROM_LOAD( "15602-01.u3t",  0x20000, 0x10000, CRC(545b27a1) SHA1(1e8beebc1384cf6513bff7c2381ca214967ff135) )
		ROM_LOAD( "15603-01.u4t",  0x30000, 0x10000, CRC(cdc9c09d) SHA1(8641312638507d027948c17c042417b0d0362714) )
		ROM_LOAD( "15604-01.u5t",  0x40000, 0x10000, CRC(3c03e92e) SHA1(7cd9b02bbf1d30a8432632d902c4ea6c8108210b) )
		ROM_LOAD( "15605-01.u6t",  0x50000, 0x10000, CRC(cdf7d19c) SHA1(577c8bf5964d77dbfef4840c53ae40cda68bf4f3) )
		ROM_LOAD( "15606-01.u7t",  0x60000, 0x10000, CRC(8eeb007c) SHA1(6f9d4132c7e5e6502108cb3e8eab9114f07848b4) )
		ROM_LOAD( "15607-01.u8t",  0x70000, 0x10000, CRC(57cb6d2d) SHA1(56e364aedca25935a5cd7ab4460d9213fcc58b4a) )
	
		ROM_REGION( 0x100000, REGION_CPU3, 0 )
		ROM_LOAD16_BYTE( "15623-01.25t", 0x040001, 0x10000, CRC(710bdc76) SHA1(610f7baa17adf2d16c9494b05556b49ae376fe81) )
		ROM_LOAD16_BYTE( "15620-01.13t", 0x040000, 0x10000, CRC(7e5cb8ad) SHA1(aaff4e93053638955b95951dceea3b35e842e80f) )
		ROM_LOAD16_BYTE( "15624-01.26t", 0x060001, 0x10000, CRC(dd090d33) SHA1(09a3fa4fa3a50c6692be2bc5fec2c4e9a5072d5d) )
		ROM_LOAD16_BYTE( "15621-01.14t", 0x060000, 0x10000, CRC(f68c68c9) SHA1(a7d77c36831d455a8c36d2156460287cf28c9694) )
		ROM_LOAD16_BYTE( "15625-01.27t", 0x0e0001, 0x10000, CRC(ac442523) SHA1(d05dcc413eb39b0938890ef80ec7b636773bb1a3) )
		ROM_LOAD16_BYTE( "15622-01.15t", 0x0e0000, 0x10000, CRC(9e84509a) SHA1(4c3a3e5192ba6c38d8391eedf817350795bddb8f) )
	
		ROM_REGION( 0x0c000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "15615-01.u93", 0x00000, 0x04000, CRC(a7ea6a87) SHA1(5cfd9ed6a5ffc8e86d18b7d8496761b9086b6368) )
		ROM_LOAD( "15616-01.u94", 0x04000, 0x04000, CRC(4a9b3900) SHA1(00398cc5056c999673604e414c9c0338d83b13d4) )
		ROM_LOAD( "15617-01.u95", 0x08000, 0x04000, CRC(2cd95edb) SHA1(939ff97562535b05f427186b085a74a8fe5a332a) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "15611-01.u70",  0x00000, 0x4000, CRC(bf2695fb) SHA1(58a6d1e9e83912f7567eabdf54278db85061c284) )
		ROM_LOAD( "15614-01.u92",  0x04000, 0x4000, CRC(c93fd870) SHA1(1086334496a4d1900a2d697cbd2575a77df89d65) )
		ROM_LOAD( "15610-01.u69",  0x08000, 0x4000, CRC(3e5b786f) SHA1(13d2ab7b6a1182933272b597718d3e715b547a10) )
		/* 91 = empty */
		ROM_LOAD( "15609-01.u68",  0x10000, 0x4000, CRC(0319aec7) SHA1(e4f14ce9b4712c1cee69141165d187e9068101fc) )
		ROM_LOAD( "15613-01.u90",  0x14000, 0x4000, CRC(4805802e) SHA1(a121aec2b0340773288687baccf85519c0ef3160) )
		ROM_LOAD( "15608-01.u67",  0x18000, 0x4000, CRC(78f0fd2b) SHA1(e83b1106411bb03c64a985a08c5f20c2eb397140) )
		/* 89 = empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_aafb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1, 0 )
		ROM_LOAD( "03-28011.u58",   0x00000, 0x10000, CRC(fa75a4a0) SHA1(ff37d71d8f2776a8ae9b15f95f105f282b079c5b) )
		ROM_LOAD( "03-28012.u59",   0x10000, 0x10000, CRC(ab6a606f) SHA1(6c8872c73b26760517884b2996a0f3834b16b480) )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 )
		ROM_LOAD( "03-28000.u3",   0x00000, 0x02000, CRC(cb531986) SHA1(e3bc3fdd1471719e0489d9990302a267a2cedc23) )
		ROM_LOAD( "26001-01.2t",   0x10000, 0x10000, CRC(f118b9b4) SHA1(95d0ae9055cf60e2d0b414ab64632fed1f48bf99) )
		ROM_LOAD( "24002-02.u3t",  0x20000, 0x10000, CRC(bbb92184) SHA1(d9890d1c95fb19e9fff6465c977cabf71e4528b4) )
		ROM_LOAD( "15603-01.u4t",  0x30000, 0x10000, CRC(cdc9c09d) SHA1(8641312638507d027948c17c042417b0d0362714) )
		ROM_LOAD( "15604-01.u5t",  0x40000, 0x10000, CRC(3c03e92e) SHA1(7cd9b02bbf1d30a8432632d902c4ea6c8108210b) )
		ROM_LOAD( "15605-01.u6t",  0x50000, 0x10000, CRC(cdf7d19c) SHA1(577c8bf5964d77dbfef4840c53ae40cda68bf4f3) )
		ROM_LOAD( "15606-01.u7t",  0x60000, 0x10000, CRC(8eeb007c) SHA1(6f9d4132c7e5e6502108cb3e8eab9114f07848b4) )
		ROM_LOAD( "03-28002.u8",   0x70000, 0x10000, CRC(c3e09811) SHA1(9b6e036a53000c9bcb104677d9c71743f02fd841) )
	
		ROM_REGION( 0x100000, REGION_CPU3, 0 )
	    ROM_LOAD16_BYTE( "24019-01.u25", 0x040001, 0x10000, CRC(9e344768) SHA1(7f16d29c52f3d7f0046f414185c4d889f6128597) )
	    ROM_LOAD16_BYTE( "24016-01.u13", 0x040000, 0x10000, CRC(6997025f) SHA1(5eda3bcae896933385fe97a4e1396ae2da7576cb) )
	    ROM_LOAD16_BYTE( "24020-01.u26", 0x060001, 0x10000, CRC(0788f2a5) SHA1(75eb1ab00185f8efa71f1d46197b5f6d20d721f2) )
	    ROM_LOAD16_BYTE( "24017-01.u14", 0x060000, 0x10000, CRC(a48bd721) SHA1(e099074165594a7c289a25c522005db7e9554ca1) )
	    ROM_LOAD16_BYTE( "24021-01.u27", 0x0e0001, 0x10000, CRC(94081899) SHA1(289eb2f494d1110d169552e8898296e4a47fcb1d) )
	    ROM_LOAD16_BYTE( "24018-01.u15", 0x0e0000, 0x10000, CRC(76eb6077) SHA1(255731c63f4a846bb01d4203a786eb34a4734e66) )
	
		ROM_REGION( 0x0c000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "03-28008.u93", 0x00000, 0x04000, NO_DUMP )
		ROM_LOAD( "03-28009.u94", 0x04000, 0x04000, CRC(669791ac) SHA1(e8b7bdec313ea9d40f89f13499a31f0b125951a8) )
		ROM_LOAD( "03-28010.u95", 0x08000, 0x04000, CRC(bd62aa8a) SHA1(c8a177a11ec94671bb3bd5883b40692495c049a2) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "03-28005.u70",  0x00000, 0x4000, CRC(5ca6f4e2) SHA1(76c86d432fac27d0b30f38e12d340b013baf0dd4) )
		ROM_LOAD( "03-28007.u92",  0x04000, 0x4000, CRC(1d9e33c2) SHA1(0b05d1dc20eb9dd803056113265ac6a43291711b) )
		ROM_LOAD( "03-28004.u69",  0x08000, 0x4000, CRC(d4b8a471) SHA1(a9940f749a756409da303c1ebbd2382f635e9a3f) )
		/* 91 = empty */
		/* 68 = empty */
		ROM_LOAD( "03-28006.u90",  0x14000, 0x4000, CRC(e68c8b6e) SHA1(94f2774d1713fadf0e644641bc0226fd03040bf8) )
		ROM_LOAD( "03-28003.u67",  0x18000, 0x4000, CRC(c92f6357) SHA1(07fa8f9e07aafbe844e11cd6f9a0cbe65625dc53) )
		/* 89 = empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_aafbb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1, 0 )
		ROM_LOAD( "24014-02.u58",   0x00000, 0x10000, CRC(5db4a3d0) SHA1(f759ab16de48562db1640bc5df68be188725aecf) )
		ROM_LOAD( "24015-02.u59",   0x10000, 0x10000, NO_DUMP )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 )
		ROM_LOAD( "24000-02.u3",   0x00000, 0x02000, CRC(52df0354) SHA1(a39a2538b733e336eac5a1491c42c89fd4f4d1aa) )
		ROM_LOAD( "24001-02.u2t",  0x10000, 0x10000, CRC(9b20697d) SHA1(ccb9851e5db4360731f19e5446a0ef9205110860) )
		ROM_LOAD( "24002-02.u3t",  0x20000, 0x10000, CRC(bbb92184) SHA1(d9890d1c95fb19e9fff6465c977cabf71e4528b4) )
		ROM_LOAD( "15603-01.u4t",  0x30000, 0x10000, CRC(cdc9c09d) SHA1(8641312638507d027948c17c042417b0d0362714) )
		ROM_LOAD( "15604-01.u5t",  0x40000, 0x10000, CRC(3c03e92e) SHA1(7cd9b02bbf1d30a8432632d902c4ea6c8108210b) )
		ROM_LOAD( "15605-01.u6t",  0x50000, 0x10000, CRC(cdf7d19c) SHA1(577c8bf5964d77dbfef4840c53ae40cda68bf4f3) )
		ROM_LOAD( "15606-01.u7t",  0x60000, 0x10000, CRC(8eeb007c) SHA1(6f9d4132c7e5e6502108cb3e8eab9114f07848b4) )
		ROM_LOAD( "24002-02.u8t",  0x70000, 0x10000, CRC(3d9747c9) SHA1(4624ac39ff5336b0fd8c70bf35685041d5c38b1c) )
	
		ROM_REGION( 0x100000, REGION_CPU3, 0 )
	    ROM_LOAD16_BYTE( "24019-01.u25", 0x040001, 0x10000, CRC(9e344768) SHA1(7f16d29c52f3d7f0046f414185c4d889f6128597) )
	    ROM_LOAD16_BYTE( "24016-01.u13", 0x040000, 0x10000, CRC(6997025f) SHA1(5eda3bcae896933385fe97a4e1396ae2da7576cb) )
	    ROM_LOAD16_BYTE( "24020-01.u26", 0x060001, 0x10000, CRC(0788f2a5) SHA1(75eb1ab00185f8efa71f1d46197b5f6d20d721f2) )
	    ROM_LOAD16_BYTE( "24017-01.u14", 0x060000, 0x10000, CRC(a48bd721) SHA1(e099074165594a7c289a25c522005db7e9554ca1) )
	    ROM_LOAD16_BYTE( "24021-01.u27", 0x0e0001, 0x10000, CRC(94081899) SHA1(289eb2f494d1110d169552e8898296e4a47fcb1d) )
	    ROM_LOAD16_BYTE( "24018-01.u15", 0x0e0000, 0x10000, CRC(76eb6077) SHA1(255731c63f4a846bb01d4203a786eb34a4734e66) )
	
		ROM_REGION( 0x18000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "24011-02.u93", 0x00000, 0x08000, CRC(71f4425b) SHA1(074c79d709bf9e927f538932ef05b13e5e649197) )
		ROM_LOAD( "24012-02.u94", 0x08000, 0x08000, CRC(b2499547) SHA1(cf5979e56cc307133cbdbfdba448cdf3087eaf8c) )
		ROM_LOAD( "24013-02.u95", 0x10000, 0x08000, CRC(0a604e0d) SHA1(08917c3e9fb408b8e128fe2e3617c8c17d964d66) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "24007-01.u70",  0x00000, 0x4000, CRC(40e46aa4) SHA1(e8a27c9007218906683eac29affdd748f64cc6e6) )
		ROM_LOAD( "24010-01.u92",  0x04000, 0x4000, CRC(78705f42) SHA1(4b941df0690a8ce4e390b0488a7ce7e083f52ff3) )
		ROM_LOAD( "24006-01.u69",  0x08000, 0x4000, CRC(6a576aa9) SHA1(8849929c66012de6d2d8c1b4faefe71f11133aac) )
		ROM_LOAD( "24009-02.u91",  0x0c000, 0x4000, CRC(b857a1ad) SHA1(40aeb6afb115af14530177f05100b7cf4baf330a) )
		ROM_LOAD( "24005-02.u68",  0x10000, 0x4000, CRC(8ea75319) SHA1(8651346030e51f19bd77d0ddd76a2544e951b12e) )
		ROM_LOAD( "24008-01.u90",  0x14000, 0x4000, CRC(4538bc58) SHA1(a568e392771398f60b2aa0f83425935fc7198f72) )
		ROM_LOAD( "24004-02.u67",  0x18000, 0x4000, CRC(cd7a3338) SHA1(c91d277578ad9d039f2febdd15d977d7259e5fc8) )
		/* 89 = empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_aafbc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1, 0 )
		ROM_LOAD( "u58t.bin",   0x00000, 0x10000, CRC(25cc4ccc) SHA1(0fe02e23942a10bb9a46524e75705f10fbb0a79a) )
		ROM_LOAD( "u59t.bin",   0x10000, 0x10000, CRC(bfa1b56f) SHA1(b5dba27bfcd47cfeebdcf99e9d5f978d5d7f4fb3) )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 )
		ROM_LOAD( "24000-02.u3",   0x00000, 0x02000, CRC(52df0354) SHA1(a39a2538b733e336eac5a1491c42c89fd4f4d1aa) )
		ROM_LOAD( "24001-02.u2t",  0x10000, 0x10000, CRC(9b20697d) SHA1(ccb9851e5db4360731f19e5446a0ef9205110860) )
		ROM_LOAD( "24002-02.u3t",  0x20000, 0x10000, CRC(bbb92184) SHA1(d9890d1c95fb19e9fff6465c977cabf71e4528b4) )
		ROM_LOAD( "15603-01.u4t",  0x30000, 0x10000, CRC(cdc9c09d) SHA1(8641312638507d027948c17c042417b0d0362714) )
		ROM_LOAD( "15604-01.u5t",  0x40000, 0x10000, CRC(3c03e92e) SHA1(7cd9b02bbf1d30a8432632d902c4ea6c8108210b) )
		ROM_LOAD( "15605-01.u6t",  0x50000, 0x10000, CRC(cdf7d19c) SHA1(577c8bf5964d77dbfef4840c53ae40cda68bf4f3) )
		ROM_LOAD( "15606-01.u7t",  0x60000, 0x10000, CRC(8eeb007c) SHA1(6f9d4132c7e5e6502108cb3e8eab9114f07848b4) )
		ROM_LOAD( "24002-02.u8t",  0x70000, 0x10000, CRC(3d9747c9) SHA1(4624ac39ff5336b0fd8c70bf35685041d5c38b1c) )
	
		ROM_REGION( 0x100000, REGION_CPU3, 0 )
	    ROM_LOAD16_BYTE( "24019-01.u25", 0x040001, 0x10000, CRC(9e344768) SHA1(7f16d29c52f3d7f0046f414185c4d889f6128597) )
	    ROM_LOAD16_BYTE( "24016-01.u13", 0x040000, 0x10000, CRC(6997025f) SHA1(5eda3bcae896933385fe97a4e1396ae2da7576cb) )
	    ROM_LOAD16_BYTE( "24020-01.u26", 0x060001, 0x10000, CRC(0788f2a5) SHA1(75eb1ab00185f8efa71f1d46197b5f6d20d721f2) )
	    ROM_LOAD16_BYTE( "24017-01.u14", 0x060000, 0x10000, CRC(a48bd721) SHA1(e099074165594a7c289a25c522005db7e9554ca1) )
	    ROM_LOAD16_BYTE( "24021-01.u27", 0x0e0001, 0x10000, CRC(94081899) SHA1(289eb2f494d1110d169552e8898296e4a47fcb1d) )
	    ROM_LOAD16_BYTE( "24018-01.u15", 0x0e0000, 0x10000, CRC(76eb6077) SHA1(255731c63f4a846bb01d4203a786eb34a4734e66) )
	
		ROM_REGION( 0x18000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "24011-02.u93", 0x00000, 0x08000, CRC(71f4425b) SHA1(074c79d709bf9e927f538932ef05b13e5e649197) )
		ROM_LOAD( "24012-02.u94", 0x08000, 0x08000, CRC(b2499547) SHA1(cf5979e56cc307133cbdbfdba448cdf3087eaf8c) )
		ROM_LOAD( "24013-02.u95", 0x10000, 0x08000, CRC(0a604e0d) SHA1(08917c3e9fb408b8e128fe2e3617c8c17d964d66) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "24007-01.u70",  0x00000, 0x4000, CRC(40e46aa4) SHA1(e8a27c9007218906683eac29affdd748f64cc6e6) )
		ROM_LOAD( "24010-01.u92",  0x04000, 0x4000, CRC(78705f42) SHA1(4b941df0690a8ce4e390b0488a7ce7e083f52ff3) )
		ROM_LOAD( "24006-01.u69",  0x08000, 0x4000, CRC(6a576aa9) SHA1(8849929c66012de6d2d8c1b4faefe71f11133aac) )
		ROM_LOAD( "24009-02.u91",  0x0c000, 0x4000, CRC(b857a1ad) SHA1(40aeb6afb115af14530177f05100b7cf4baf330a) )
		ROM_LOAD( "24005-02.u68",  0x10000, 0x4000, CRC(8ea75319) SHA1(8651346030e51f19bd77d0ddd76a2544e951b12e) )
		ROM_LOAD( "24008-01.u90",  0x14000, 0x4000, CRC(4538bc58) SHA1(a568e392771398f60b2aa0f83425935fc7198f72) )
		ROM_LOAD( "24004-02.u67",  0x18000, 0x4000, CRC(cd7a3338) SHA1(c91d277578ad9d039f2febdd15d977d7259e5fc8) )
		/* 89 = empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_aafbd2p = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1, 0 )
		ROM_LOAD( "26014-01.58t", 0x00000, 0x10000, CRC(79fd14cd) SHA1(1dd75bcecd51d414194ca19381bee0b9f70a8007) )
		ROM_LOAD( "26015-01.59t", 0x10000, 0x10000, CRC(3b0382f0) SHA1(1b01af999201f202e76da8e445ff986d096103cd) )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 )
		ROM_LOAD( "26000-01.u3",   0x00000, 0x02000, CRC(98c06c63) SHA1(91922f7e2d4f30018ee7302f3d9b7b6793b43dba) )
		ROM_LOAD( "26001-01.2t",   0x10000, 0x10000, CRC(f118b9b4) SHA1(95d0ae9055cf60e2d0b414ab64632fed1f48bf99) )
		ROM_LOAD( "24002-02.u3t",  0x20000, 0x10000, CRC(bbb92184) SHA1(d9890d1c95fb19e9fff6465c977cabf71e4528b4) )
		ROM_LOAD( "15603-01.u4t",  0x30000, 0x10000, CRC(cdc9c09d) SHA1(8641312638507d027948c17c042417b0d0362714) )
		ROM_LOAD( "15604-01.u5t",  0x40000, 0x10000, CRC(3c03e92e) SHA1(7cd9b02bbf1d30a8432632d902c4ea6c8108210b) )
		ROM_LOAD( "15605-01.u6t",  0x50000, 0x10000, CRC(cdf7d19c) SHA1(577c8bf5964d77dbfef4840c53ae40cda68bf4f3) )
		ROM_LOAD( "15606-01.u7t",  0x60000, 0x10000, CRC(8eeb007c) SHA1(6f9d4132c7e5e6502108cb3e8eab9114f07848b4) )
		ROM_LOAD( "24002-02.u8t",  0x70000, 0x10000, CRC(3d9747c9) SHA1(4624ac39ff5336b0fd8c70bf35685041d5c38b1c) )
	
		ROM_REGION( 0x100000, REGION_CPU3, 0 )
	    ROM_LOAD16_BYTE( "24019-01.u25", 0x040001, 0x10000, CRC(9e344768) SHA1(7f16d29c52f3d7f0046f414185c4d889f6128597) )
	    ROM_LOAD16_BYTE( "24016-01.u13", 0x040000, 0x10000, CRC(6997025f) SHA1(5eda3bcae896933385fe97a4e1396ae2da7576cb) )
	    ROM_LOAD16_BYTE( "24020-01.u26", 0x060001, 0x10000, CRC(0788f2a5) SHA1(75eb1ab00185f8efa71f1d46197b5f6d20d721f2) )
	    ROM_LOAD16_BYTE( "24017-01.u14", 0x060000, 0x10000, CRC(a48bd721) SHA1(e099074165594a7c289a25c522005db7e9554ca1) )
	    ROM_LOAD16_BYTE( "24021-01.u27", 0x0e0001, 0x10000, CRC(94081899) SHA1(289eb2f494d1110d169552e8898296e4a47fcb1d) )
	    ROM_LOAD16_BYTE( "24018-01.u15", 0x0e0000, 0x10000, CRC(76eb6077) SHA1(255731c63f4a846bb01d4203a786eb34a4734e66) )
	
		ROM_REGION( 0x18000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "24011-02.u93", 0x00000, 0x08000, CRC(71f4425b) SHA1(074c79d709bf9e927f538932ef05b13e5e649197) )
		ROM_LOAD( "24012-02.u94", 0x08000, 0x08000, CRC(b2499547) SHA1(cf5979e56cc307133cbdbfdba448cdf3087eaf8c) )
		ROM_LOAD( "24013-02.u95", 0x10000, 0x08000, CRC(0a604e0d) SHA1(08917c3e9fb408b8e128fe2e3617c8c17d964d66) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "24007-01.u70",  0x00000, 0x4000, CRC(40e46aa4) SHA1(e8a27c9007218906683eac29affdd748f64cc6e6) )
		ROM_LOAD( "24010-01.u92",  0x04000, 0x4000, CRC(78705f42) SHA1(4b941df0690a8ce4e390b0488a7ce7e083f52ff3) )
		ROM_LOAD( "24006-01.u69",  0x08000, 0x4000, CRC(6a576aa9) SHA1(8849929c66012de6d2d8c1b4faefe71f11133aac) )
		ROM_LOAD( "24009-02.u91",  0x0c000, 0x4000, CRC(b857a1ad) SHA1(40aeb6afb115af14530177f05100b7cf4baf330a) )
		ROM_LOAD( "24005-02.u68",  0x10000, 0x4000, CRC(8ea75319) SHA1(8651346030e51f19bd77d0ddd76a2544e951b12e) )
		ROM_LOAD( "24008-01.u90",  0x14000, 0x4000, CRC(4538bc58) SHA1(a568e392771398f60b2aa0f83425935fc7198f72) )
		ROM_LOAD( "24004-02.u67",  0x18000, 0x4000, CRC(cd7a3338) SHA1(c91d277578ad9d039f2febdd15d977d7259e5fc8) )
		/* 89 = empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_offroad = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 )
		ROM_LOAD( "22121-04.u58",   0x00000, 0x10000, CRC(c5790988) SHA1(a6bae6b024d86b49a23805037b77d15a3c7913ef) )
		ROM_LOAD( "22122-03.u59",   0x10000, 0x10000, CRC(ae862fdc) SHA1(ac31630cff5850409f87bfa5a7303eeedf8a895d) )
		ROM_LOAD( "22120-01.u57",   0x20000, 0x10000, CRC(e9f0f175) SHA1(db8c55015d1e8230f1fb27dfac6b8b364b0718a2) )
		ROM_LOAD( "22119-02.u56",   0x30000, 0x10000, CRC(38642f22) SHA1(9167bbc7ed8a8a0b913ead3b8b5a7749a29f15cb) )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 )
		ROM_LOAD( "22100-01.u2",  0x00000, 0x02000, CRC(08c96a4b) SHA1(7d93dd918a5733c190b2668811d161f5f6339cf0) )
		ROM_LOAD( "22108-02.u4",  0x30000, 0x10000, CRC(0d72780a) SHA1(634b87e7afff4ac2e8e3b98554364c5f3c4d9176) )
		ROM_LOAD( "22109-02.u5",  0x40000, 0x10000, CRC(5429ce2c) SHA1(73e543796629ac719928f4fe48442f1975db5092) )
		ROM_LOAD( "22110-02.u6",  0x50000, 0x10000, CRC(f97bad5c) SHA1(c68f8022c86bfc5c0480e5ce426fe2f985dc255f) )
		ROM_LOAD( "22111-01.u7",  0x60000, 0x10000, CRC(f79157a1) SHA1(a5731aa92f805123cb00c6ef93a0aed3dc84dae4) )
		ROM_LOAD( "22112-01.u8",  0x70000, 0x10000, CRC(3eef38d3) SHA1(9131960592a44c8567ab483f72955d2cc8898445) )
	
		ROM_REGION( 0x100000, REGION_CPU3, 0 )
	    ROM_LOAD16_BYTE( "22116-03.u25", 0x040001, 0x10000, CRC(95bb31d3) SHA1(e7bc43b63126fd33663865b2e41bacc58e962628) )
	    ROM_LOAD16_BYTE( "22113-03.u13", 0x040000, 0x10000, CRC(71b28df6) SHA1(caf8e4c98a1650dbaedf83f4d38da920d0976f78) )
	    ROM_LOAD16_BYTE( "22117-03.u26", 0x060001, 0x10000, CRC(703d81ce) SHA1(caf5363fb468a461a260e0ec636b0a7a8dc9cd3d) )
	    ROM_LOAD16_BYTE( "22114-03.u14", 0x060000, 0x10000, CRC(f8b31bf8) SHA1(cb8133effe5484c5b4c40b77769f6ec72441c333) )
	    ROM_LOAD16_BYTE( "22118-03.u27", 0x0e0001, 0x10000, CRC(806ccf8b) SHA1(7335a85fc84d5c2f7537548c3856c9cd2f267609) )
	    ROM_LOAD16_BYTE( "22115-03.u15", 0x0e0000, 0x10000, CRC(c8439a7a) SHA1(9a8bb1fca8d3414dcfd4839bc0c4289e4d810943) )
	
		ROM_REGION( 0x18000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "22105-01.u93", 0x00000, 0x08000, CRC(4426e367) SHA1(298203112d724feb9a75a7bfc34b3dbb4d7fffe7) )
		ROM_LOAD( "22106-02.u94", 0x08000, 0x08000, CRC(687dc1fc) SHA1(876c72561d942ebc5f3a148d3d3efdceb39c9e2e) )
		ROM_LOAD( "22107-02.u95", 0x10000, 0x08000, CRC(cee6ee5f) SHA1(3f1c6e8d9eb9de207cabca7c9d6d8d633bd69681) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		/* 70 = empty */
		ROM_LOAD( "22104-01.u92",  0x04000, 0x4000, CRC(03e0497d) SHA1(bffd870251d51cce262961b77f1953f360f9607b) )
		ROM_LOAD( "22102-01.u69",  0x08000, 0x4000, CRC(c3f2e443) SHA1(82f22dabc99b3aaa94acaa303735a155ac13e592) )
		/* 91 = empty */
		/* 68 = empty */
		ROM_LOAD( "22103-02.u90",  0x14000, 0x4000, CRC(2266757a) SHA1(22aaf4b14f11198ffd14c9830c7997fd47ee14b6) )
		ROM_LOAD( "22101-01.u67",  0x18000, 0x4000, CRC(ecab0527) SHA1(6bbf8243d9b2ea775897719592212b51998f1b01) )
		/* 89 = empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_offroadt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x040000, REGION_CPU1, 0 )
		ROM_LOAD( "ortpu58.bin",   0x00000, 0x10000, CRC(adbc6211) SHA1(cb3181a0dd64754d9a65a7a557e4a183b8d539a2) )
		ROM_LOAD( "ortpu59.bin",   0x10000, 0x10000, CRC(296dd3b6) SHA1(01ae1f2976e2fecc8237fc7b4cf4fb86dd170a70) )
		ROM_LOAD( "ortpu57.bin",   0x20000, 0x10000, CRC(e9f0f175) SHA1(db8c55015d1e8230f1fb27dfac6b8b364b0718a2) )
		ROM_LOAD( "ortpu56.bin",   0x30000, 0x10000, CRC(2c1a22b3) SHA1(fb18af5ec873968beab47d163d9ef23532c40771) )
	
		ROM_REGION( 0x90000, REGION_CPU2, 0 )
		ROM_LOAD( "ortpu3b.bin", 0x00000, 0x02000, CRC(95abb9f1) SHA1(98e9e8f388047d6992a664ae87c50ca65a5db0b1) )
		ROM_LOAD( "ortpu2.bin",  0x10000, 0x10000, CRC(c46c1627) SHA1(1e911bc774cbc0a66b2feb68b600aa5ad272daa6) )
		ROM_LOAD( "ortpu3.bin",  0x20000, 0x10000, CRC(2276546f) SHA1(d19335504a71ccf74864c8e9896347709bf794e4) )
		ROM_LOAD( "ortpu4.bin",  0x30000, 0x10000, CRC(aa4b5975) SHA1(7d695957c283aae4c7e6fb90dab117add65571b4) )
		ROM_LOAD( "ortpu5.bin",  0x40000, 0x10000, CRC(69100b06) SHA1(c25d1273d08fd20651d1873ce412bb1e18eff06f) )
		ROM_LOAD( "ortpu6.bin",  0x50000, 0x10000, CRC(b75015b8) SHA1(2bb6b4422e087502cfeb9bce0d3e3ffe18192fe0) )
		ROM_LOAD( "ortpu7.bin",  0x60000, 0x10000, CRC(a5af5b4f) SHA1(e4992bfbf628d034a879bf9317377348ee4c24e9) )
		ROM_LOAD( "ortpu8.bin",  0x70000, 0x10000, CRC(0f735078) SHA1(cb59b11fbed672cb372759384e5916418e6c3dc7) )
	
		ROM_REGION( 0x100000, REGION_CPU3, 0 )
		ROM_LOAD16_BYTE( "ortpu25.bin", 0x040001, 0x10000, CRC(f952f800) SHA1(0f1fc837b0b5f5495a666b0a42adb6068e58a57a) )
		ROM_LOAD16_BYTE( "ortpu13.bin", 0x040000, 0x10000, CRC(7beec9fc) SHA1(b03b4a28217a8c7c02dc0314db97fef1d4ab6f20) )
		ROM_LOAD16_BYTE( "ortpu26.bin", 0x060001, 0x10000, CRC(6227ea94) SHA1(26384af82f73452b7be8a0eeac9f8a3b464068f6) )
		ROM_LOAD16_BYTE( "ortpu14.bin", 0x060000, 0x10000, CRC(0a44331d) SHA1(1a52da64c44bc91c2fc9499d1c41191725f9f2be) )
		ROM_LOAD16_BYTE( "ortpu27.bin", 0x0e0001, 0x10000, CRC(b80c5f99) SHA1(6b0657db870fb4e14e20cbd955655d0990dd7bda) )
		ROM_LOAD16_BYTE( "ortpu15.bin", 0x0e0000, 0x10000, CRC(2a1a1c3c) SHA1(990328240a2dba7264bb5add5ea8cae2752327d9) )
	
		ROM_REGION( 0x18000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "ortpu93b.bin", 0x00000, 0x08000, CRC(f0c1d8b0) SHA1(aa6e53b56474fa97b17b60ef1123a28442482b80) )
		ROM_LOAD( "ortpu94b.bin", 0x08000, 0x08000, CRC(7460d8c0) SHA1(9e3560056da89108c58b320125deeed0e009d0a8) )
		ROM_LOAD( "ortpu95b.bin", 0x10000, 0x08000, CRC(081ee7a8) SHA1(2b884a8ed4173b64f7890edf9a6954c62b5ba012) )
	
		ROM_REGION( 0x20000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		/* 70 = empty */
		ROM_LOAD( "ortpu92b.bin",  0x04000, 0x4000, CRC(f9988e28) SHA1(250071f4a26782266303331ddbef5479cc241220) )
		ROM_LOAD( "ortpu69b.bin",  0x08000, 0x4000, CRC(fe5f8d8f) SHA1(5e520da33f30a594c8f37e8e214d0d257ba5c801) )
		/* 91 = empty */
		/* 68 = empty */
		ROM_LOAD( "ortpu90b.bin",  0x14000, 0x4000, CRC(bda2ecb1) SHA1(c7a70ed794cf1655aebdf4538ab25f74be38cda3) )
		ROM_LOAD( "ortpu67b.bin",  0x18000, 0x4000, CRC(38c9bf29) SHA1(aa681f0a3eb5d31f2b01116939162d296e113428) )
		/* 89 = empty */
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_pigout = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x040000, REGION_CPU1, 0 )
		ROM_LOAD( "poutu58t.bin",  0x00000, 0x10000, CRC(8fe4b683) SHA1(6f98a4e54a558a642b7193af85823b29ade46919) )
		ROM_LOAD( "poutu59t.bin",  0x10000, 0x10000, CRC(ab907762) SHA1(971c34ae42c17aa27880665966dc15a98387bebb) )
		ROM_LOAD( "poutu57t.bin",  0x20000, 0x10000, CRC(c22be0ff) SHA1(52b76918358046f40ea4b74e53a38d8984125dbb) )
	
		ROM_REGION( 0x080000, REGION_CPU2, 0 )
		ROM_LOAD( "poutu3.bin",   0x00000, 0x02000, CRC(af213cb7) SHA1(cf31ee09ee3685274f5ce1df954e7e87199e2e80) )
		ROM_LOAD( "poutu2t.bin",  0x10000, 0x10000, CRC(b23164c6) SHA1(11edbea7bf54a68cb85df36345f39654d726a7f2) )
		ROM_LOAD( "poutu3t.bin",  0x20000, 0x10000, CRC(d93f105f) SHA1(9fe469d674e84209eb55704fd2ad317d11e4caac) )
		ROM_LOAD( "poutu4t.bin",  0x30000, 0x10000, CRC(b7c47bfe) SHA1(42b1ce4401e3754f6fb1453ab4a661dc4237770d) )
		ROM_LOAD( "poutu5t.bin",  0x40000, 0x10000, CRC(d9b9dfbf) SHA1(a6f663638d9f6e14c1a6a99ca811d1d495664412) )
		ROM_LOAD( "poutu6t.bin",  0x50000, 0x10000, CRC(728c7c1a) SHA1(cc3211313a6b3998a0458d3865e3d2a0f9eb8a94) )
		ROM_LOAD( "poutu7t.bin",  0x60000, 0x10000, CRC(393bd990) SHA1(d66d3c5c6d97bb983549d5037bd69c481751b9bf) )
		ROM_LOAD( "poutu8t.bin",  0x70000, 0x10000, CRC(cb9ffaad) SHA1(f39fb33e5a30619cd3017574739ccace80afbe1f) )
	
		ROM_REGION( 0x100000, REGION_CPU3, 0 )
		ROM_LOAD16_BYTE( "poutu25t.bin", 0x040001, 0x10000, CRC(92cd2617) SHA1(88e318f4a41c67fd9e91f013b3c29b6275b69c31) )
		ROM_LOAD16_BYTE( "poutu13t.bin", 0x040000, 0x10000, CRC(9448c389) SHA1(7bb0bd49044ba4b302048d2922ed300f799a2efb) )
		ROM_LOAD16_BYTE( "poutu26t.bin", 0x060001, 0x10000, CRC(ab57de8f) SHA1(28a366e7441bc85dfb814f7a7797aa704a0277ba) )
		ROM_LOAD16_BYTE( "poutu14t.bin", 0x060000, 0x10000, CRC(30678e93) SHA1(6d2c8f5c9de3d016538dc1da99ec0017fefdf35a) )
		ROM_LOAD16_BYTE( "poutu27t.bin", 0x0e0001, 0x10000, CRC(37a8156e) SHA1(a0b44b1ba6701daaa26576c6c892fd97ec82d5e3) )
		ROM_LOAD16_BYTE( "poutu15t.bin", 0x0e0000, 0x10000, CRC(1c60d58b) SHA1(93f83a231d06cd958d3539a528e6ee6c2d9904ed) )
	
		ROM_REGION( 0x18000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "poutu93.bin", 0x000000, 0x08000, CRC(f102a04d) SHA1(3ecc0ab34a5d6e760679dc5fd7d32dd439f797d5) )
		ROM_LOAD( "poutu94.bin", 0x008000, 0x08000, CRC(ec63c015) SHA1(10010a17ffda468dbe2940fae6aae49c56e1ad78) )
		ROM_LOAD( "poutu95.bin", 0x010000, 0x08000, CRC(ba6e797e) SHA1(135f905b7663026a99fd9aca8e0247a72bf43cdb) )
	
		ROM_REGION( 0x40000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "poutu70.bin",  0x00000, 0x4000, CRC(7db4eaa1) SHA1(e1ec186a8359b3302071e87577092008065c39de) )
		ROM_LOAD( "poutu92.bin",  0x04000, 0x4000, CRC(20fa57bb) SHA1(7e94698a25c5459991f0e99a50e5e98f392cda41) )
		ROM_LOAD( "poutu69.bin",  0x08000, 0x4000, CRC(a16886f3) SHA1(48a0cbbea80cc38cd4d5594d3367282690724c59) )
		ROM_LOAD( "poutu91.bin",  0x0c000, 0x4000, CRC(482a3581) SHA1(bab1140a5c0a2ff4c3ef076155429e35cbfe2335) )
		ROM_LOAD( "poutu68.bin",  0x10000, 0x4000, CRC(7b62a3ed) SHA1(fc707626a3fa78d38f5b2cbe3b8786e8c4382563) )
		ROM_LOAD( "poutu90.bin",  0x14000, 0x4000, CRC(9615d710) SHA1(a9b2d2bf4d6edecdc212f5d96eec8095833bee22) )
		ROM_LOAD( "poutu67.bin",  0x18000, 0x4000, CRC(af85ce79) SHA1(76e421772dfdf4d27e36aa51993a987883e015b0) )
		ROM_LOAD( "poutu89.bin",  0x1c000, 0x4000, CRC(6c874a05) SHA1(a931ba5ac41facfaf32c5e940eb011e780ab234a) )
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_pigouta = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x040000, REGION_CPU1, 0 )
		ROM_LOAD( "03-29020.01", 0x00000, 0x10000, CRC(6c815982) SHA1(0720b22afd16e9bdc5d4a9e9a0071674ea46d038) )
		ROM_LOAD( "03-29021.01", 0x10000, 0x10000, CRC(9de7a763) SHA1(9a612730a9d80d84114c1afc4a1887277d1ad5bc) )
		ROM_LOAD( "poutu57t.bin", 0x20000, 0x10000, CRC(c22be0ff) SHA1(52b76918358046f40ea4b74e53a38d8984125dbb) )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 )
		ROM_LOAD( "poutu3.bin",   0x00000, 0x02000, CRC(af213cb7) SHA1(cf31ee09ee3685274f5ce1df954e7e87199e2e80) )
		ROM_LOAD( "poutu2t.bin",  0x10000, 0x10000, CRC(b23164c6) SHA1(11edbea7bf54a68cb85df36345f39654d726a7f2) )
		ROM_LOAD( "poutu3t.bin",  0x20000, 0x10000, CRC(d93f105f) SHA1(9fe469d674e84209eb55704fd2ad317d11e4caac) )
		ROM_LOAD( "poutu4t.bin",  0x30000, 0x10000, CRC(b7c47bfe) SHA1(42b1ce4401e3754f6fb1453ab4a661dc4237770d) )
		ROM_LOAD( "poutu5t.bin",  0x40000, 0x10000, CRC(d9b9dfbf) SHA1(a6f663638d9f6e14c1a6a99ca811d1d495664412) )
		ROM_LOAD( "poutu6t.bin",  0x50000, 0x10000, CRC(728c7c1a) SHA1(cc3211313a6b3998a0458d3865e3d2a0f9eb8a94) )
		ROM_LOAD( "poutu7t.bin",  0x60000, 0x10000, CRC(393bd990) SHA1(d66d3c5c6d97bb983549d5037bd69c481751b9bf) )
		ROM_LOAD( "poutu8t.bin",  0x70000, 0x10000, CRC(cb9ffaad) SHA1(f39fb33e5a30619cd3017574739ccace80afbe1f) )
	
		ROM_REGION( 0x100000, REGION_CPU3, 0 )
		ROM_LOAD16_BYTE( "poutu25t.bin", 0x040001, 0x10000, CRC(92cd2617) SHA1(88e318f4a41c67fd9e91f013b3c29b6275b69c31) )
		ROM_LOAD16_BYTE( "poutu13t.bin", 0x040000, 0x10000, CRC(9448c389) SHA1(7bb0bd49044ba4b302048d2922ed300f799a2efb) )
		ROM_LOAD16_BYTE( "poutu26t.bin", 0x060001, 0x10000, CRC(ab57de8f) SHA1(28a366e7441bc85dfb814f7a7797aa704a0277ba) )
		ROM_LOAD16_BYTE( "poutu14t.bin", 0x060000, 0x10000, CRC(30678e93) SHA1(6d2c8f5c9de3d016538dc1da99ec0017fefdf35a) )
		ROM_LOAD16_BYTE( "poutu27t.bin", 0x0e0001, 0x10000, CRC(37a8156e) SHA1(a0b44b1ba6701daaa26576c6c892fd97ec82d5e3) )
		ROM_LOAD16_BYTE( "poutu15t.bin", 0x0e0000, 0x10000, CRC(1c60d58b) SHA1(93f83a231d06cd958d3539a528e6ee6c2d9904ed) )
	
		ROM_REGION( 0x18000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "poutu93.bin", 0x000000, 0x08000, CRC(f102a04d) SHA1(3ecc0ab34a5d6e760679dc5fd7d32dd439f797d5) )
		ROM_LOAD( "poutu94.bin", 0x008000, 0x08000, CRC(ec63c015) SHA1(10010a17ffda468dbe2940fae6aae49c56e1ad78) )
		ROM_LOAD( "poutu95.bin", 0x010000, 0x08000, CRC(ba6e797e) SHA1(135f905b7663026a99fd9aca8e0247a72bf43cdb) )
	
		ROM_REGION( 0x40000, REGION_USER1, 0 )   /* Ordering: 70/92/69/91/68/90/67/89 */
		ROM_LOAD( "poutu70.bin",  0x00000, 0x4000, CRC(7db4eaa1) SHA1(e1ec186a8359b3302071e87577092008065c39de) )
		ROM_LOAD( "poutu92.bin",  0x04000, 0x4000, CRC(20fa57bb) SHA1(7e94698a25c5459991f0e99a50e5e98f392cda41) )
		ROM_LOAD( "poutu69.bin",  0x08000, 0x4000, CRC(a16886f3) SHA1(48a0cbbea80cc38cd4d5594d3367282690724c59) )
		ROM_LOAD( "poutu91.bin",  0x0c000, 0x4000, CRC(482a3581) SHA1(bab1140a5c0a2ff4c3ef076155429e35cbfe2335) )
		ROM_LOAD( "poutu68.bin",  0x10000, 0x4000, CRC(7b62a3ed) SHA1(fc707626a3fa78d38f5b2cbe3b8786e8c4382563) )
		ROM_LOAD( "poutu90.bin",  0x14000, 0x4000, CRC(9615d710) SHA1(a9b2d2bf4d6edecdc212f5d96eec8095833bee22) )
		ROM_LOAD( "poutu67.bin",  0x18000, 0x4000, CRC(af85ce79) SHA1(76e421772dfdf4d27e36aa51993a987883e015b0) )
		ROM_LOAD( "poutu89.bin",  0x1c000, 0x4000, CRC(6c874a05) SHA1(a931ba5ac41facfaf32c5e940eb011e780ab234a) )
	
	    ROM_REGION( LELAND_BATTERY_RAM_SIZE, REGION_USER2, 0 ) /* extra RAM regions */
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Driver initialization
	 *
	 *************************************/
	
	#ifdef MAME_DEBUG
	/*
	Copy this code into the init function and modify:
	{
		UINT8 *ram = memory_region(REGION_CPU1);
		FILE *output;
	
		output = fopen("indyheat.m", "w");
		dasm_chunk("Resident", 		&ram[0x00000], 0x0000, 0x2000, output);
		dasm_chunk("Bank 0x02000:", &ram[0x02000], 0x2000, 0x8000, output);
		dasm_chunk("Bank 0x10000:", &ram[0x10000], 0x2000, 0x8000, output);
		dasm_chunk("Bank 0x18000:", &ram[0x18000], 0x2000, 0x8000, output);
		dasm_chunk("Bank 0x20000:", &ram[0x20000], 0x2000, 0x8000, output);
		dasm_chunk("Bank 0x28000:", &ram[0x28000], 0x2000, 0x8000, output);
		dasm_chunk("Bank 0x30000:", &ram[0x30000], 0x2000, 0x8000, output);
		dasm_chunk("Bank 0x38000:", &ram[0x38000], 0x2000, 0x8000, output);
		dasm_chunk("Bank 0x40000:", &ram[0x40000], 0x2000, 0x8000, output);
		dasm_chunk("Bank 0x48000:", &ram[0x48000], 0x2000, 0x8000, output);
		dasm_chunk("Bank 0x50000:", &ram[0x50000], 0x2000, 0x8000, output);
		dasm_chunk("Bank 0x58000:", &ram[0x58000], 0x2000, 0x8000, output);
		dasm_chunk("Bank 0x60000:", &ram[0x60000], 0x2000, 0x8000, output);
		dasm_chunk("Bank 0x68000:", &ram[0x68000], 0x2000, 0x8000, output);
		dasm_chunk("Bank 0x70000:", &ram[0x70000], 0x2000, 0x8000, output);
		dasm_chunk("Bank 0x78000:", &ram[0x78000], 0x2000, 0x8000, output);
		fclose(output);
	}
	*/
	static void dasm_chunk(char *tag, UINT8 *base, UINT16 pc, UINT32 length, FILE *output)
	{
		
		UINT8 *old_rom = OP_ROM;
		UINT8 *old_ram = OP_RAM;
		char buffer[256];
		int count, offset, i;
	
		fprintf(output, "\n\n\n%s:\n", tag);
		OP_ROM = OP_RAM = &base[-pc];
		for (offset = 0; offset < length; offset += count)
		{
			count = DasmZ80(buffer, pc);
			for (i = 0; i < 4; i++)
				if (i < count)
					fprintf(output, "%c", (OP_ROM[pc + i] >= 32 && OP_ROM[pc + i] < 127) ? OP_ROM[pc + i] : ' ');
				else
					fprintf(output, " ");
			fprintf(output, " %04X: ", pc);
			for (i = 0; i < 4; i++)
				if (i < count)
					fprintf(output, "%02X ", OP_ROM[pc++]);
				else
					fprintf(output, "   ");
			fprintf(output, "%s\n", buffer);
		}
		OP_ROM = old_rom;
		OP_RAM = old_ram;
	}
	#endif
	
	
	static void init_master_ports(UINT8 mvram_base, UINT8 io_base)
	{
		/* set up the master CPU VRAM I/O */
		install_port_read_handler(0, mvram_base, mvram_base + 0x1f, leland_mvram_port_r);
		install_port_write_handler(0, mvram_base, mvram_base + 0x1f, leland_mvram_port_w);
	
		/* set up the master CPU I/O ports */
		install_port_read_handler(0, io_base, io_base + 0x1f, leland_master_input_r);
		install_port_write_handler(0, io_base, io_base + 0x0f, leland_master_output_w);
	}
	
	
	public static DriverInitHandlerPtr init_cerberus  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 cerberus_eeprom_data[] =
		{
			0x05,0x0001,
			0x06,0x0001,
			0x07,0x0001,
			0x08,0x0001,
			0x09,0x0004,
			0x0a,0x0004,
			0x0e,0x0001,
			0x0f,0x0003,
			0x10,0x0500,
			0x12,0x0005,
			0x13,0x0003,
			0x3f,0x001d,
			0xffff
		};
		leland_init_eeprom(0x00, cerberus_eeprom_data, 0, SERIAL_TYPE_NONE);
	
		/* master CPU bankswitching */
		leland_update_master_bank = cerberus_bankswitch;
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x40, 0x80);
	
		/* set up additional input ports */
		install_port_read_handler(0, 0x80, 0x80, cerberus_dial_1_r);
		install_port_read_handler(0, 0x90, 0x90, cerberus_dial_2_r);
	} };
	
	
	public static DriverInitHandlerPtr init_mayhem  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 mayhem_eeprom_data[] =
		{
			0x05,0x0001,
			0x06,0x0001,
			0x07,0x0001,
			0x08,0x0001,
			0x09,0x0004,
			0x0a,0x0004,
			0x0c,0xff00,
			0x13,0x28ff,
			0x14,0x0023,
			0x15,0x0005,
			0x1b,0x0060,
			0x1c,0x4a00,
			0x1d,0x4520,
			0x1e,0x4943,
			0x1f,0x454e,
			0x20,0x414d,
			0x21,0x5254,
			0x22,0x4e4f,
			0x23,0x4349,
			0x24,0x2053,
			0x25,0x2020,
			0x26,0x2020,
			0x27,0x2020,
			0x3f,0x0818,
			0xffff
		};
		leland_init_eeprom(0x00, mayhem_eeprom_data, 0x28, SERIAL_TYPE_ADD);
	
		/* master CPU bankswitching */
		leland_update_master_bank = mayhem_bankswitch;
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x00, 0xc0);
	} };
	
	
	public static DriverInitHandlerPtr init_powrplay  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 powrplay_eeprom_data[] =
		{
			0x21,0xfffe,
			0x22,0xfffe,
			0x23,0xfffe,
			0x24,0xfffe,
			0x25,0xfffb,
			0x26,0xfffb,
			0x27,0xfefe,
			0x28,0x0000,
			0x29,0xd700,
			0x2a,0xd7dc,
			0x2b,0xffdc,
			0x2c,0xfffb,
			0xffff
		};
		leland_init_eeprom(0xff, powrplay_eeprom_data, 0x2d, SERIAL_TYPE_ADD_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = mayhem_bankswitch;
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x40, 0x80);
	} };
	
	
	public static DriverInitHandlerPtr init_wseries  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 wseries_eeprom_data[] =
		{
			0x19,0xfefe,
			0x1a,0xfefe,
			0x1b,0xfbfb,
			0x1d,0x00ff,
			0xffff
		};
		leland_init_eeprom(0xff, wseries_eeprom_data, 0x12, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = mayhem_bankswitch;
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x40, 0x80);
	} };
	
	
	public static DriverInitHandlerPtr init_alleymas  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 alleymas_eeprom_data[] =
		{
			0x13,0xfefe,
			0x14,0xfefe,
			0x15,0xfbfb,
			0x17,0x00ff,
			0x18,0xff00,
			0x37,0x00ff,
			0xffff
		};
		leland_init_eeprom(0xff, alleymas_eeprom_data, 0x0c, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = mayhem_bankswitch;
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x00, 0xc0);
	
		/* kludge warning: the game uses location E0CA to determine if the joysticks are available */
		/* it gets cleared by the code, but there is no obvious way for the value to be set to a */
		/* non-zero value. If the value is zero, the joystick is never read. */
		alleymas_kludge_mem = install_mem_write_handler(0, 0xe0ca, 0xe0ca, alleymas_joystick_kludge);
	} };
	
	
	public static DriverInitHandlerPtr init_dangerz  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 dangerz_eeprom_data[] =
		{
			0x17,0xfefe,
			0x18,0xfefe,
			0x19,0xfbfb,
			0x1b,0x00ff,
			0x1c,0xfffa,
			0x38,0xb6bc,
			0x39,0xffb1,
			0x3a,0x8007,
			0xffff
		};
		leland_init_eeprom(0xff, dangerz_eeprom_data, 0x10, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = dangerz_bankswitch;
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x40, 0x80);
	
		/* set up additional input ports */
		install_port_read_handler(0, 0xf4, 0xf4, dangerz_input_upper_r);
		install_port_read_handler(0, 0xf8, 0xf8, dangerz_input_y_r);
		install_port_read_handler(0, 0xfc, 0xfc, dangerz_input_x_r);
	} };
	
	
	public static DriverInitHandlerPtr init_basebal2  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 basebal2_eeprom_data[] =
		{
			0x19,0xfefe,
			0x1a,0xfefe,
			0x1b,0xfbfb,
			0x1d,0x00ff,
			0xffff
		};
		leland_init_eeprom(0xff, basebal2_eeprom_data, 0x12, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = basebal2_bankswitch;
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x00, 0xc0);
	} };
	
	
	public static DriverInitHandlerPtr init_dblplay  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 dblplay_eeprom_data[] =
		{
			0x18,0xfefe,
			0x19,0xfefe,
			0x1a,0xfbfb,
			0x1c,0x00ff,
			0x3b,0xffe1,
			0xffff
		};
		leland_init_eeprom(0xff, dblplay_eeprom_data, 0x11, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = basebal2_bankswitch;
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x80, 0x40);
	} };
	
	
	public static DriverInitHandlerPtr init_strkzone  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 strkzone_eeprom_data[] =
		{
			0x16,0xfefe,
			0x17,0xfefe,
			0x18,0xfbfb,
			0x1a,0x00ff,
			0x1b,0xffe1,
			0xffff
		};
		leland_init_eeprom(0xff, strkzone_eeprom_data, 0x0f, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = basebal2_bankswitch;
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x00, 0x40);
	} };
	
	
	public static DriverInitHandlerPtr init_redlin2p  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 redlin2p_eeprom_data[] =
		{
			0x1f,0xfefe,
			0x20,0xfffb,
			0x21,0xfa00,
			0x22,0xfffe,
			0xffff
		};
		leland_init_eeprom(0xff, redlin2p_eeprom_data, 0x18, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = redline_bankswitch;
	
		leland_rotate_memory(0);
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x00, 0xc0);
	
		/* set up additional input ports */
		install_port_read_handler(0, 0xc0, 0xc0, redline_pedal_1_r);
		install_port_read_handler(0, 0xd0, 0xd0, redline_pedal_2_r);
		install_port_read_handler(0, 0xf8, 0xf8, redline_wheel_2_r);
		install_port_read_handler(0, 0xfb, 0xfb, redline_wheel_1_r);
	
		/* optimize the sound */
		leland_i86_optimize_address(0x828);
	} };
	
	
	public static DriverInitHandlerPtr init_quarterb  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 quarterb_eeprom_data[] =
		{
			0x34,0xfefe,
			0x35,0xfefe,
			0x36,0xfbfb,
			0x38,0x00ff,
			0x39,0x53ff,
			0x3a,0xffd9,
			0xffff
		};
		leland_init_eeprom(0xff, quarterb_eeprom_data, 0x24, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = viper_bankswitch;
	
		leland_rotate_memory(0);
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x40, 0x80);
	
		/* optimize the sound */
		leland_i86_optimize_address(0x9bc);
	} };
	
	
	public static DriverInitHandlerPtr init_viper  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 viper_eeprom_data[] =
		{
			0x13,0xfefe,
			0x14,0xfefe,
			0x15,0xfbfb,
			0x17,0x00ff,
			0x18,0xfcfa,
			0x1b,0xfffe,
			0xffff
		};
		leland_init_eeprom(0xff, viper_eeprom_data, 0x0c, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = viper_bankswitch;
	
		leland_rotate_memory(0);
		leland_rotate_memory(1);
		leland_rotate_memory(1);
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x00, 0xc0);
	
		/* set up additional input ports */
		install_port_read_handler(0, 0xa4, 0xa4, dangerz_input_upper_r);
		install_port_read_handler(0, 0xb8, 0xb8, dangerz_input_y_r);
		install_port_read_handler(0, 0xbc, 0xbc, dangerz_input_x_r);
	
		/* optimize the sound */
		leland_i86_optimize_address(0x788);
	} };
	
	
	public static DriverInitHandlerPtr init_teamqb  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 teamqb_eeprom_data[] =
		{
			0x36,0xfefe,
			0x37,0xfefe,
			0x38,0xfbfb,
			0x3a,0x5300,
			0x3b,0xffd9,
			0xffff
		};
		leland_init_eeprom(0xff, teamqb_eeprom_data, 0x1a, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = viper_bankswitch;
	
		leland_rotate_memory(0);
		leland_rotate_memory(1);
		leland_rotate_memory(1);
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x40, 0x80);
	
		/* set up additional input ports */
		install_port_read_handler(0, 0x7c, 0x7c, input_port_10_r);
		install_port_read_handler(0, 0x7f, 0x7f, input_port_11_r);
	
		/* optimize the sound */
		leland_i86_optimize_address(0x788);
	} };
	
	
	public static DriverInitHandlerPtr init_aafb  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 aafb_eeprom_data[] =
		{
			0x36,0xfefe,
			0x37,0xfefe,
			0x38,0xfbfb,
			0x3a,0x5300,
			0x3b,0xffd9,
			0xffff
		};
		leland_init_eeprom(0xff, aafb_eeprom_data, 0x1a, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = viper_bankswitch;
	
		leland_rotate_memory(0);
		leland_rotate_memory(1);
		leland_rotate_memory(1);
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x00, 0xc0);
	
		/* set up additional input ports */
		install_port_read_handler(0, 0x7c, 0x7c, input_port_10_r);
		install_port_read_handler(0, 0x7f, 0x7f, input_port_11_r);
	
		/* optimize the sound */
		leland_i86_optimize_address(0x788);
	} };
	
	
	public static DriverInitHandlerPtr init_aafbb  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 aafb_eeprom_data[] =
		{
			0x36,0xfefe,
			0x37,0xfefe,
			0x38,0xfbfb,
			0x3a,0x5300,
			0x3b,0xffd9,
			0xffff
		};
		leland_init_eeprom(0xff, aafb_eeprom_data, 0x1a, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = viper_bankswitch;
	
		leland_rotate_memory(0);
		leland_rotate_memory(1);
		leland_rotate_memory(1);
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x80, 0x40);
	
		/* set up additional input ports */
		install_port_read_handler(0, 0x7c, 0x7c, input_port_10_r);
		install_port_read_handler(0, 0x7f, 0x7f, input_port_11_r);
	
		/* optimize the sound */
		leland_i86_optimize_address(0x788);
	} };
	
	
	public static DriverInitHandlerPtr init_aafbd2p  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 aafb_eeprom_data[] =
		{
			0x36,0xfefe,
			0x37,0xfefe,
			0x38,0xfbfb,
			0x3a,0x5300,
			0x3b,0xffd9,
			0xffff
		};
		leland_init_eeprom(0xff, aafb_eeprom_data, 0x1a, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = viper_bankswitch;
	
		leland_rotate_memory(0);
		leland_rotate_memory(1);
		leland_rotate_memory(1);
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x00, 0x40);
	
		/* set up additional input ports */
		install_port_read_handler(0, 0x7c, 0x7c, input_port_10_r);
		install_port_read_handler(0, 0x7f, 0x7f, input_port_11_r);
	
		/* optimize the sound */
		leland_i86_optimize_address(0x788);
	} };
	
	
	public static DriverInitHandlerPtr init_offroad  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 offroad_eeprom_data[] =
		{
			0x09,0xfefe,
			0x0a,0xfffb,
			0x0d,0x00ff,
			0x0e,0xfffb,
			0x36,0xfeff,
			0x37,0xfefe,
			0x38,0xfffe,
			0x39,0x50ff,
			0x3a,0x976c,
			0x3b,0xffad,
			0xffff
		};
		leland_init_eeprom(0xff, offroad_eeprom_data, 0x00, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = offroad_bankswitch;
	
		leland_rotate_memory(0);
		leland_rotate_memory(1);
		leland_rotate_memory(1);
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x00, 0xc0);
		init_master_ports(0x40, 0x80);	/* yes, this is intentional */
	
		/* set up additional input ports */
		install_port_read_handler(0, 0xf8, 0xf8, offroad_wheel_3_r);
		install_port_read_handler(0, 0xf9, 0xf9, offroad_wheel_1_r);
		install_port_read_handler(0, 0xfb, 0xfb, offroad_wheel_2_r);
	
		/* optimize the sound */
		leland_i86_optimize_address(0x788);
	} };
	
	
	public static DriverInitHandlerPtr init_offroadt  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 offroadt_eeprom_data[] =
		{
			0x09,0xfefe,
			0x0a,0xfffb,
			0x0d,0x00ff,
			0x0e,0xfffb,
			0x36,0xfeff,
			0x37,0xfefe,
			0x38,0xfffe,
			0x39,0x50ff,
			0x3a,0x976c,
			0x3b,0xffad,
			0xffff
		};
		leland_init_eeprom(0xff, offroadt_eeprom_data, 0x00, SERIAL_TYPE_ENCRYPT_XOR);
	
		/* master CPU bankswitching */
		leland_update_master_bank = offroad_bankswitch;
	
		leland_rotate_memory(0);
		leland_rotate_memory(1);
		leland_rotate_memory(1);
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x80, 0x40);
	
		/* set up additional input ports */
		install_port_read_handler(0, 0xf8, 0xf8, offroad_wheel_3_r);
		install_port_read_handler(0, 0xf9, 0xf9, offroad_wheel_1_r);
		install_port_read_handler(0, 0xfb, 0xfb, offroad_wheel_2_r);
	
		/* optimize the sound */
		leland_i86_optimize_address(0x788);
	} };
	
	
	public static DriverInitHandlerPtr init_pigout  = new DriverInitHandlerPtr() { public void handler(){
		/* initialize the default EEPROM state */
		static const UINT16 pigout_eeprom_data[] =
		{
			0x09,0xfefe,
			0x0a,0xfefb,
			0x0b,0xfffe,
			0x0c,0xfefe,
			0x0d,0xfffb,
			0x39,0xfcff,
			0x3a,0xfb00,
			0x3b,0xfffc,
			0xffff
		};
		leland_init_eeprom(0xff, pigout_eeprom_data, 0x00, SERIAL_TYPE_ENCRYPT);
	
		/* master CPU bankswitching */
		leland_update_master_bank = offroad_bankswitch;
	
		leland_rotate_memory(0);
		leland_rotate_memory(1);
		leland_rotate_memory(1);
	
		/* set up the master CPU I/O ports */
		init_master_ports(0x00, 0x40);
	
		/* set up additional input ports */
		install_port_read_handler(0, 0x7f, 0x7f, input_port_4_r);
	
		/* optimize the sound */
		leland_i86_optimize_address(0x788);
	} };
	
	
	
	/*************************************
	 *
	 *	Game drivers
	 *
	 *************************************/
	
	/* small master banks, small slave banks */
	GAME( 1985, cerberus, 0,       leland,  cerberus, cerberus, ROT0,   "Cinematronics", "Cerberus" )
	GAME( 1985, mayhem,   0,       leland,  mayhem,   mayhem,   ROT0,   "Cinematronics", "Mayhem 2002" )
	GAME( 1985, powrplay, 0,       leland,  mayhem,   powrplay, ROT0,   "Cinematronics", "Power Play" )
	GAME( 1985, wseries,  0,       leland,  wseries,  wseries,  ROT0,   "Cinematronics", "World Series: The Season" )
	GAME( 1986, alleymas, 0,       leland,  alleymas, alleymas, ROT270, "Cinematronics", "Alley Master" )
	
	/* odd master banks, small slave banks */
	GAME( 1986, dangerz,  0,       leland,  dangerz,  dangerz,  ROT0,   "Cinematronics", "Danger Zone" )
	
	/* small master banks + extra top board, small slave banks */
	GAME( 1987, basebal2, 0,       leland,  basebal2, basebal2, ROT0,   "Cinematronics", "Baseball The Season II" )
	GAME( 1987, dblplay,  0,       leland,  basebal2, dblplay,  ROT0,   "Leland Corp. / Tradewest", "Super Baseball Double Play Home Run Derby" )
	GAME( 1988, strkzone, 0,       leland,  basebal2, strkzone, ROT0,   "Leland Corp.", "Strike Zone Baseball" )
	
	/* large master banks, small slave banks, I86 sound */
	GAME( 1987, redlin2p, 0,       redline, redline,  redlin2p, ROT270, "Cinematronics (Tradewest license)", "Redline Racer (2 players)" )
	GAME( 1987, quarterb, 0,       quarterb,quarterb, quarterb, ROT270, "Leland Corp.", "Quarterback" )
	GAME( 1987, quartrba, quarterb,quarterb,quarterb, quarterb, ROT270, "Leland Corp.", "Quarterback (set 2)" )
	
	/* large master banks, large slave banks, I86 sound */
	GAME( 1988, viper,    0,       lelandi, dangerz,  viper,    ROT0,   "Leland Corp.", "Viper" )
	GAME( 1988, teamqb,   0,       lelandi, teamqb,   teamqb,   ROT270, "Leland Corp.", "John Elway's Team Quarterback" )
	GAME( 1988, teamqb2,  teamqb,  lelandi, teamqb,   teamqb,   ROT270, "Leland Corp.", "John Elway's Team Quarterback (set 2)" )
	GAME( 1989, aafb,     0,       lelandi, teamqb,   aafb,     ROT270, "Leland Corp.", "All American Football (rev E)" )
	GAME( 1989, aafbd2p,  aafb,    lelandi, aafb2p,   aafbd2p,  ROT270, "Leland Corp.", "All American Football (rev D, 2 Players)" )
	GAME( 1989, aafbc,    aafb,    lelandi, teamqb,   aafbb,    ROT270, "Leland Corp.", "All American Football (rev C)" )
	GAME( 1989, aafbb,    aafb,    lelandi, teamqb,   aafbb,    ROT270, "Leland Corp.", "All American Football (rev B)" )
	
	/* huge master banks, large slave banks, I86 sound */
	GAME( 1989, offroad,  0,       lelandi, offroad,  offroad,  ROT0,   "Leland Corp.", "Ironman Stewart's Super Off-Road" )
	GAME( 1989, offroadt, 0,       lelandi, offroad,  offroadt, ROT0,   "Leland Corp.", "Ironman Stewart's Super Off-Road Track Pack" )
	GAME( 1990, pigout,   0,       lelandi, pigout,   pigout,   ROT0,   "Leland Corp.", "Pigout" )
	GAME( 1990, pigouta,  pigout,  lelandi, pigout,   pigout,   ROT0,   "Leland Corp.", "Pigout (alternate)" )
}
