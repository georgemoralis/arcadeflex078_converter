/***************************************************************************

 Amidar hardware

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.03
 */ 
package arcadeflex.v078.drivers;

public class amidar
{
	
	
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new Memory_ReadAddress( 0x9000, 0x93ff, MRA_RAM ),
		new Memory_ReadAddress( 0x9800, 0x98ff, MRA_RAM ),
		new Memory_ReadAddress( 0xa800, 0xa800, watchdog_reset_r ),
		new Memory_ReadAddress( 0xb000, 0xb03f, amidar_ppi8255_0_r ),
		new Memory_ReadAddress( 0xb800, 0xb83f, amidar_ppi8255_1_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new Memory_WriteAddress( 0x9000, 0x93ff, galaxian_videoram_w, galaxian_videoram ),
		new Memory_WriteAddress( 0x9800, 0x983f, galaxian_attributesram_w, galaxian_attributesram ),
		new Memory_WriteAddress( 0x9840, 0x985f, MWA_RAM, galaxian_spriteram, galaxian_spriteram_size ),
		new Memory_WriteAddress( 0x9860, 0x98ff, MWA_RAM ),
		new Memory_WriteAddress( 0xa000, 0xa000, scramble_background_red_w ),
		new Memory_WriteAddress( 0xa008, 0xa008, galaxian_nmi_enable_w ),
		new Memory_WriteAddress( 0xa010, 0xa010, galaxian_flip_screen_x_w ),
		new Memory_WriteAddress( 0xa018, 0xa018, galaxian_flip_screen_y_w ),
		new Memory_WriteAddress( 0xa020, 0xa020, scramble_background_green_w ),
		new Memory_WriteAddress( 0xa028, 0xa028, scramble_background_blue_w ),
		new Memory_WriteAddress( 0xa030, 0xa030, galaxian_coin_counter_0_w ),
		new Memory_WriteAddress( 0xa038, 0xa038, galaxian_coin_counter_1_w ),
		new Memory_WriteAddress( 0xb000, 0xb03f, amidar_ppi8255_0_w ),
		new Memory_WriteAddress( 0xb800, 0xb83f, amidar_ppi8255_1_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	
	static InputPortPtr input_ports_amidar = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably space for button 2 */
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START(); 	/* IN1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "4" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_BITX (0,       0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably space for player 2 button 2 */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "30000 50000" );
		PORT_DIPSETTING(    0x04, "50000 50000" );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Cocktail") );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x04, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, "Disable All Coins" );
	INPUT_PORTS_END(); }}; 
	
	/* absolutely identical to amidar, the only difference is the BONUS dip switch */
	static InputPortPtr input_ports_amidaru = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably space for button 2 */
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START(); 	/* IN1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "4" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_BITX (0,       0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably space for player 2 button 2 */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "30000 70000" );
		PORT_DIPSETTING(    0x04, "50000 80000" );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Cocktail") );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x04, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, "Disable All Coins" );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_amidaro = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably space for button 2 */
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START(); 	/* IN1 */
		PORT_DIPNAME( 0x03, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "1" );
		PORT_DIPSETTING(    0x02, "2" );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably space for player 2 button 2 */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL );
		PORT_DIPNAME( 0x02, 0x00, "Level Progression" );
		PORT_DIPSETTING(    0x00, "Slow" );
		PORT_DIPSETTING(    0x02, "Fast" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "30000 70000" );
		PORT_DIPSETTING(    0x04, "50000 80000" );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Cocktail") );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x04, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, "Disable All Coins" );
	INPUT_PORTS_END(); }}; 
	
	/* similar to Amidar, dip switches are different and port 3, which in Amidar */
	/* selects coins per credit, is not used. */
	static InputPortPtr input_ports_turtles = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably space for button 2 */
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START(); 	/* IN1 */
		PORT_DIPNAME( 0x03, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_BITX (0,       0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "126", IP_KEY_NONE, IP_JOY_NONE );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably space for player 2 button 2 */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );
		PORT_DIPNAME( 0x06, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, "A 1/1 B 2/1 C 1/1" );
		PORT_DIPSETTING(    0x02, "A 1/2 B 1/1 C 1/2" );
		PORT_DIPSETTING(    0x04, "A 1/3 B 3/1 C 1/3" );
		PORT_DIPSETTING(    0x06, "A 1/4 B 4/1 C 1/4" );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Cocktail") );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	/* same as Turtles, but dip switches are different. */
	static InputPortPtr input_ports_turpin = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably space for button 2 */
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START(); 	/* IN1 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x02, "7" );
		PORT_BITX (0,       0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "126", IP_KEY_NONE, IP_JOY_NONE );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably space for player 2 button 2 */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL );
		PORT_DIPNAME( 0x06, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x06, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Cocktail") );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	public static MachineHandlerPtr machine_driver_amidar = new MachineHandlerPtr() {
        public void handler(InternalMachineDriver machine) {
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(galaxian_base)
		MDRV_CPU_MODIFY("main")
		MDRV_CPU_MEMORY(readmem,writemem)
	
		MDRV_CPU_ADD(Z80,14318000/8)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)	/* 1.78975 MHz */
		MDRV_CPU_MEMORY(scobra_sound_readmem,scobra_sound_writemem)
		MDRV_CPU_PORTS(scobra_sound_readport,scobra_sound_writeport)
	
		MDRV_MACHINE_INIT(scramble)
	
		/* video hardware */
		MDRV_PALETTE_LENGTH(32+64+2+8)
	
		MDRV_PALETTE_INIT(turtles)
		MDRV_VIDEO_START(turtles)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, scobra_ay8910_interface)
	MACHINE_DRIVER_END();
 }
};
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_amidar = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "amidar.2c",    0x0000, 0x1000, CRC(c294bf27) SHA1(399325bf1559e8cdbddf7cfbf0dc739f9ed72ef0) )
		ROM_LOAD( "amidar.2e",    0x1000, 0x1000, CRC(e6e96826) SHA1(e9c4f8c594640424b456505e676352a98b758c03) )
		ROM_LOAD( "amidar.2f",    0x2000, 0x1000, CRC(3656be6f) SHA1(9d652f66bedcf17a6453c0e0ead30bfd7ea0bd0a) )
		ROM_LOAD( "amidar.2h",    0x3000, 0x1000, CRC(1be170bd) SHA1(c047bc393b297c0d47668a5f6f4870e3fac937ef) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "amidar.5c",    0x0000, 0x1000, CRC(c4b66ae4) SHA1(9d09dbde4019f7be3abe0815b0e06d542c01c255) )
		ROM_LOAD( "amidar.5d",    0x1000, 0x1000, CRC(806785af) SHA1(c8c85e3a6a204feccd7859b4527bd649e96134b4) )
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "amidar.5f",    0x0000, 0x0800, CRC(5e51e84d) SHA1(dfe84db7e2b1a45a1d484fcf37291f536bc5324c) )
		ROM_LOAD( "amidar.5h",    0x0800, 0x0800, CRC(2f7f1c30) SHA1(83c330eca20dfcc6a4099001943b9ed7a7c3db5b) )
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 )
		ROM_LOAD( "amidar.clr",   0x0000, 0x0020, CRC(f940dcc3) SHA1(1015e56f37c244a850a8f4bf0e36668f047fd46d) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_amidaru = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "amidarus.2c",  0x0000, 0x1000, CRC(951e0792) SHA1(3a68b829c9ffb465bd6582c9ea566e0e947c6c19) )
		ROM_LOAD( "amidarus.2e",  0x1000, 0x1000, CRC(a1a3a136) SHA1(330ec857fdf4c1b28e2560a5f63a2432f87f9b2f) )
		ROM_LOAD( "amidarus.2f",  0x2000, 0x1000, CRC(a5121bf5) SHA1(fe15b91724758ede43dd332327919f164772c592) )
		ROM_LOAD( "amidarus.2h",  0x3000, 0x1000, CRC(051d1c7f) SHA1(3cfa0f728a5c27da0a3fe2579ad226129ccde232) )
		ROM_LOAD( "amidarus.2j",  0x4000, 0x1000, CRC(351f00d5) SHA1(6659357f40f888b21be00826246200fd3a8a88ce) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "amidarus.5c",  0x0000, 0x1000, CRC(8ca7b750) SHA1(4f4c2915503b85abe141d717fd254ee10c9da99e) )
		ROM_LOAD( "amidarus.5d",  0x1000, 0x1000, CRC(9b5bdc0a) SHA1(84d953618c8bf510d23b42232a856ac55f1baff5) )
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "amidarus.5f",  0x0000, 0x0800, CRC(2cfe5ede) SHA1(0d86a78008ac8653c17fff5be5ebdf1f0a9d31eb) )
		ROM_LOAD( "amidarus.5h",  0x0800, 0x0800, CRC(57c4fd0d) SHA1(8764deec9fbff4220d61df621b12fc36c3702601) )
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 )
		ROM_LOAD( "amidar.clr",   0x0000, 0x0020, CRC(f940dcc3) SHA1(1015e56f37c244a850a8f4bf0e36668f047fd46d) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_amidaro = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "107.2cd",      0x0000, 0x1000, CRC(c52536be) SHA1(3f64578214d2d9f0e4e7ee87e09b0aac33a73098) )
		ROM_LOAD( "108.2fg",      0x1000, 0x1000, CRC(38538b98) SHA1(12b2a0c09926d006781bee5d450bc0c391cc1fb5) )
		ROM_LOAD( "109.2fg",      0x2000, 0x1000, CRC(69907f0f) SHA1(f1d19a76ffc41ee8c5c574f10108cfdfe525b732) )
		ROM_LOAD( "110.2h",       0x3000, 0x1000, CRC(ba149a93) SHA1(9ef1d27f0780612be0ea2be94c3a2c781a4924c8) )
		ROM_LOAD( "111.2j",       0x4000, 0x1000, CRC(20d01c2e) SHA1(e09437ff440f04036d5ec74b355e97bbbbfefb95) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "amidarus.5c",  0x0000, 0x1000, CRC(8ca7b750) SHA1(4f4c2915503b85abe141d717fd254ee10c9da99e) )
		ROM_LOAD( "amidarus.5d",  0x1000, 0x1000, CRC(9b5bdc0a) SHA1(84d953618c8bf510d23b42232a856ac55f1baff5) )
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "amidarus.5f",  0x0000, 0x0800, CRC(2cfe5ede) SHA1(0d86a78008ac8653c17fff5be5ebdf1f0a9d31eb) )
		ROM_LOAD( "113.5h",       0x0800, 0x0800, CRC(bcdce168) SHA1(e593d03c460ef4607e3ba25019d9f01d4a717dd9) )  /* The letter 'S' is slightly different */
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 )
		ROM_LOAD( "amidar.clr",   0x0000, 0x0020, CRC(f940dcc3) SHA1(1015e56f37c244a850a8f4bf0e36668f047fd46d) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_amigo = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "2732.a1",      0x0000, 0x1000, CRC(930dc856) SHA1(7022f1f26830baccdc8b8f0b10fb1d1ccb080f22) )
		ROM_LOAD( "2732.a2",      0x1000, 0x1000, CRC(66282ff5) SHA1(986778278eb339768d190460680e7aa698812488) )
		ROM_LOAD( "2732.a3",      0x2000, 0x1000, CRC(e9d3dc76) SHA1(627c6068c65985175388aec43ac2a4248b004c97) )
		ROM_LOAD( "2732.a4",      0x3000, 0x1000, CRC(4a4086c9) SHA1(6f309b67dc68e06e6eb1d3ee2ae75afe253a4ce3) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "amidarus.5c",  0x0000, 0x1000, CRC(8ca7b750) SHA1(4f4c2915503b85abe141d717fd254ee10c9da99e) )
		ROM_LOAD( "amidarus.5d",  0x1000, 0x1000, CRC(9b5bdc0a) SHA1(84d953618c8bf510d23b42232a856ac55f1baff5) )
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "2716.a6",      0x0000, 0x0800, CRC(2082ad0a) SHA1(c6014d9575e92adf09b0961c2158a779ebe940c4) )
		ROM_LOAD( "2716.a5",      0x0800, 0x0800, CRC(3029f94f) SHA1(3b432b42e79f8b0a7d65e197f373a04e3c92ff20) )
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 )
		ROM_LOAD( "amidar.clr",   0x0000, 0x0020, CRC(f940dcc3) SHA1(1015e56f37c244a850a8f4bf0e36668f047fd46d) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_turtles = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "turt_vid.2c",  0x0000, 0x1000, CRC(ec5e61fb) SHA1(3ca89800fda7a7e61f54d71d5302908be2706def) )
		ROM_LOAD( "turt_vid.2e",  0x1000, 0x1000, CRC(fd10821e) SHA1(af74602bf2454eb8f3b9bb5c425e2476feeecd69) )
		ROM_LOAD( "turt_vid.2f",  0x2000, 0x1000, CRC(ddcfc5fa) SHA1(2af9383e5a289c2d7fbe6cf5e5b1519c352afbab) )
		ROM_LOAD( "turt_vid.2h",  0x3000, 0x1000, CRC(9e71696c) SHA1(3dcdf5dc601c875fc9d8b9a46e3ef588e7478e0d) )
		ROM_LOAD( "turt_vid.2j",  0x4000, 0x1000, CRC(fcd49fef) SHA1(bb1e91b2e6d4b5a861bf37907ef6b198328d8d83) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "turt_snd.5c",  0x0000, 0x1000, CRC(f0c30f9a) SHA1(5621f336e9be8acf986a34bbb8855ed5d45c28ef) )
		ROM_LOAD( "turt_snd.5d",  0x1000, 0x1000, CRC(af5fc43c) SHA1(8a49c55feba094b07380615cf0b6f0878c25a260) )
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "turt_vid.5h",  0x0000, 0x0800, CRC(e5999d52) SHA1(bc3f52cf6c6e19dfd2dacd1e8c9128f437e995fc) )
		ROM_LOAD( "turt_vid.5f",  0x0800, 0x0800, CRC(c3ffd655) SHA1(dee51d77be262a2944488e381541c10a2b6e5d83) )
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 )
		ROM_LOAD( "turtles.clr",  0x0000, 0x0020, CRC(f3ef02dd) SHA1(09fd795170d7d30f101d579f57553da5ff3800ab) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_turpin = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "m1",           0x0000, 0x1000, CRC(89177473) SHA1(0717b1e7308ffe527edfc578ec4353809e7d9eea) )
		ROM_LOAD( "m2",           0x1000, 0x1000, CRC(4c6ca5c6) SHA1(dd4ca7adaa523a8e775cdfaa99bb3cc25da32c08) )
		ROM_LOAD( "m3",           0x2000, 0x1000, CRC(62291652) SHA1(82965d3e9608afde4ff06cba1d7a4b11cd904c11) )
		ROM_LOAD( "turt_vid.2h",  0x3000, 0x1000, CRC(9e71696c) SHA1(3dcdf5dc601c875fc9d8b9a46e3ef588e7478e0d) )
		ROM_LOAD( "m5",           0x4000, 0x1000, CRC(7d2600f2) SHA1(1a9bdf63b50419c6e0d9c401c3dcf29d5b459fa6) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "turt_snd.5c",  0x0000, 0x1000, CRC(f0c30f9a) SHA1(5621f336e9be8acf986a34bbb8855ed5d45c28ef) )
		ROM_LOAD( "turt_snd.5d",  0x1000, 0x1000, CRC(af5fc43c) SHA1(8a49c55feba094b07380615cf0b6f0878c25a260) )
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "turt_vid.5h",  0x0000, 0x0800, CRC(e5999d52) SHA1(bc3f52cf6c6e19dfd2dacd1e8c9128f437e995fc) )
		ROM_LOAD( "turt_vid.5f",  0x0800, 0x0800, CRC(c3ffd655) SHA1(dee51d77be262a2944488e381541c10a2b6e5d83) )
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 )
		ROM_LOAD( "turtles.clr",  0x0000, 0x0020, CRC(f3ef02dd) SHA1(09fd795170d7d30f101d579f57553da5ff3800ab) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_600 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "600_vid.2c",   0x0000, 0x1000, CRC(8ee090ae) SHA1(3d491313da6cccd6dbc15774569be0555fe2f73a) )
		ROM_LOAD( "600_vid.2e",   0x1000, 0x1000, CRC(45bfaff2) SHA1(ba4f7aa499f4993ec2191b8832b5604fd41964bc) )
		ROM_LOAD( "600_vid.2f",   0x2000, 0x1000, CRC(9f4c8ed7) SHA1(2564dae82019097227351a7ddc9c5156ca00297a) )
		ROM_LOAD( "600_vid.2h",   0x3000, 0x1000, CRC(a92ef056) SHA1(c319d41a3345b84670fe9110f78332c1cfe1e163) )
		ROM_LOAD( "600_vid.2j",   0x4000, 0x1000, CRC(6dadd72d) SHA1(5602b5ebb2c287f72a5ce873b4e3dfd19b8412a0) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for the audio CPU */
		ROM_LOAD( "600_snd.5c",   0x0000, 0x1000, CRC(1773c68e) SHA1(cc4aa3a98e85bc6300f8c1ee1a0448071d7c6dfa) )
		ROM_LOAD( "600_snd.5d",   0x1000, 0x1000, CRC(a311b998) SHA1(39af321b8c3f211ed6d083a2aba4fbc8af11c9e8) )
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "600_vid.5h",   0x0000, 0x0800, CRC(006c3d56) SHA1(0c773e0e84d0bf45be5a5a7cfff960c1ca2f0320) )
		ROM_LOAD( "600_vid.5f",   0x0800, 0x0800, CRC(7dbc0426) SHA1(29eeb3cdb5a3bcf7115d8099e4d04cf76216b003) )
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 )
		ROM_LOAD( "turtles.clr",  0x0000, 0x0020, CRC(f3ef02dd) SHA1(09fd795170d7d30f101d579f57553da5ff3800ab) )
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_amidar	   = new GameDriver("1981"	,"amidar"	,"amidar.java"	,rom_amidar,null	,machine_driver_amidar	,input_ports_amidar	,init_amidar	,ROT90	,	"Konami", "Amidar" )
	public static GameDriver driver_amidaru	   = new GameDriver("1982"	,"amidaru"	,"amidar.java"	,rom_amidaru,driver_amidar	,machine_driver_amidar	,input_ports_amidaru	,init_amidar	,ROT90	,	"Konami (Stern license)", "Amidar (Stern)" )
	public static GameDriver driver_amidaro	   = new GameDriver("1982"	,"amidaro"	,"amidar.java"	,rom_amidaro,driver_amidar	,machine_driver_amidar	,input_ports_amidaro	,init_amidar	,ROT90	,	"Konami (Olympia license)", "Amidar (Olympia)" )
	public static GameDriver driver_amigo	   = new GameDriver("1982"	,"amigo"	,"amidar.java"	,rom_amigo,driver_amidar	,machine_driver_amidar	,input_ports_amidaru	,init_amidar	,ROT90	,	"bootleg", "Amigo" )
	public static GameDriver driver_turtles	   = new GameDriver("1981"	,"turtles"	,"amidar.java"	,rom_turtles,null	,machine_driver_amidar	,input_ports_turtles	,init_scramble_ppi	,ROT90	,	"[Konami] (Stern license)", "Turtles" )
	public static GameDriver driver_turpin	   = new GameDriver("1981"	,"turpin"	,"amidar.java"	,rom_turpin,driver_turtles	,machine_driver_amidar	,input_ports_turpin	,init_scramble_ppi	,ROT90	,	"[Konami] (Sega license)", "Turpin" )
	public static GameDriver driver_600	   = new GameDriver("1981"	,"600"	,"amidar.java"	,rom_600,driver_turtles	,machine_driver_amidar	,input_ports_turtles	,init_scramble_ppi	,ROT90	,	"Konami", "600" )
}
