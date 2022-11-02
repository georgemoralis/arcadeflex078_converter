/***************************************************************************

	Cinematronics vector hardware

	driver by Aaron Giles

	Special thanks to Neil Bradley, Zonn Moore, and Jeff Mitchell of the
	Retrocade Alliance

	Games supported:
		* Space Wars
		* Barrier
		* Star Hawk
		* Star Castle
		* Tailgunner
		* Rip Off
		* Speed Freak
		* Sundance
		* Warrior
		* Armor Attack
		* Solar Quest
		* Demon
		* War of the Worlds
		* Boxing Bugs

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class cinemat
{
	
	
	
	/*************************************
	 *
	 *	Sundance inputs
	 *
	 *************************************/
	
	static READ16_HANDLER( sundance_input_port_1_r )
	{
		UINT16 val = readinputport(1);
	
		switch (readinputport(4) & 0x1ff) /* player 1 keypad */
		{
		case 0x0001: val &= ~0x1201; break;
		case 0x0002: val &= ~0x1000; break;
		case 0x0004: val &= ~0x0001; break;
		case 0x0008: val &= ~0x4000; break;
		case 0x0010: val &= ~0x1001; break;
		case 0x0020: val &= ~0x0200; break;
		case 0x0040: val &= ~0x4001; break;
		case 0x0080: val &= ~0x1200; break;
		case 0x0100: val &= ~0x0201; break;
		}
	
		switch (readinputport(5) & 0x1ff) /* player 2 keypad */
		{
		case 0x0001: val &= ~0x2500; break;
		case 0x0002: val &= ~0x2000; break;
		case 0x0004: val &= ~0x0400; break;
		case 0x0008: val &= ~0x8000; break;
		case 0x0010: val &= ~0x2400; break;
		case 0x0020: val &= ~0x0100; break;
		case 0x0040: val &= ~0x8400; break;
		case 0x0080: val &= ~0x2100; break;
		case 0x0100: val &= ~0x0500; break;
		}
	
		return val;
	}
	
	
	
	/*************************************
	 *
	 *	Speed Freak inputs
	 *
	 *************************************/
	
	static UINT8 speedfrk_steer[] = {0xe, 0x6, 0x2, 0x0, 0x3, 0x7, 0xf};
	
	static READ16_HANDLER( speedfrk_input_port_1_r )
	{
	    static int last_wheel=0, delta_wheel, last_frame=0, gear=0xe0;
		int val, current_frame;
	
		/* check the fake gear input port and determine the bit settings for the gear */
		if ((input_port_5_r(0) & 0xf0) != 0xf0)
	        gear = input_port_5_r(0) & 0xf0;
	
	    val = (input_port_1_word_r(0, 0) & 0xff00) | gear;
	
		/* add the start key into the mix */
		if (input_port_1_word_r(0, 0) & 0x80)
	        val |= 0x80;
		else
	        val &= ~0x80;
	
		/* and for the cherry on top, we add the scrambled analog steering */
	    current_frame = cpu_getcurrentframe();
	    if (current_frame > last_frame)
	    {
	        /* the shift register is cleared once per 'frame' */
	        delta_wheel = input_port_4_r(0) - last_wheel;
	        last_wheel += delta_wheel;
	        if (delta_wheel > 3)
	            delta_wheel = 3;
	        else if (delta_wheel < -3)
	            delta_wheel = -3;
	    }
	    last_frame = current_frame;
	
	    val |= speedfrk_steer[delta_wheel + 3];
	
		return val;
	}
	
	
	
	/*************************************
	 *
	 *	Boxing Bugs inputs
	 *
	 *************************************/
	
	static READ16_HANDLER( boxingb_input_port_1_r )
	{
		if (cinemat_output_port_r(0,0) & 0x80)
			return ((input_port_4_r(0) & 0x0f) << 12) + input_port_1_word_r(0,0);
		else
			return ((input_port_4_r(0) & 0xf0) << 8)  + input_port_1_word_r(0,0);
	}
	
	
	
	/*************************************
	 *
	 *	Video overlays
	 *
	 *************************************/
	
	OVERLAY_START( starcas_overlay )
		OVERLAY_RECT( 0.0, 0.0, 1.0, 1.0,       MAKE_ARGB(0x24,0x00,0x3c,0xff) )
		OVERLAY_DISK_NOBLEND( 0.5, 0.5, 0.1225, MAKE_ARGB(0x24,0xff,0x20,0x20) )
		OVERLAY_DISK_NOBLEND( 0.5, 0.5, 0.0950, MAKE_ARGB(0x24,0xff,0x80,0x10) )
		OVERLAY_DISK_NOBLEND( 0.5, 0.5, 0.0725, MAKE_ARGB(0x24,0xff,0xff,0x20) )
	OVERLAY_END
	
	
	OVERLAY_START( tailg_overlay )
		OVERLAY_RECT( 0.0, 0.0, 1.0, 1.0, MAKE_ARGB(0x04,0x20,0xff,0xff) )
	OVERLAY_END
	
	
	OVERLAY_START( sundance_overlay )
		OVERLAY_RECT( 0.0, 0.0, 1.0, 1.0, MAKE_ARGB(0x04,0xff,0xff,0x20) )
	OVERLAY_END
	
	
	OVERLAY_START( solarq_overlay )
		OVERLAY_RECT( 0.0, 0.1, 1.0, 1.0, MAKE_ARGB(0x04,0x20,0x20,0xff) )
		OVERLAY_RECT( 0.0, 0.0, 1.0, 0.1, MAKE_ARGB(0x04,0xff,0x20,0x20) )
		OVERLAY_DISK_NOBLEND( 0.5, 0.5, 0.03, MAKE_ARGB(0x04,0xff,0xff,0x20) )
	OVERLAY_END
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	static MEMORY_READ16_START( readmem )
		{ 0x0000, 0x01ff, MRA16_RAM },
		{ 0x8000, 0xffff, MRA16_ROM },
	MEMORY_END
	
	
	static MEMORY_WRITE16_START( writemem )
		{ 0x0000, 0x01ff, MWA16_RAM },
		{ 0x8000, 0xffff, MWA16_ROM },
	MEMORY_END
	
	
	
	/*************************************
	 *
	 *	Main CPU port handlers
	 *
	 *************************************/
	
	static PORT_READ16_START( readport )
		{ CCPU_PORT_IOSWITCHES,   CCPU_PORT_IOSWITCHES+1,   input_port_0_word_r },
		{ CCPU_PORT_IOINPUTS,     CCPU_PORT_IOINPUTS+1,     input_port_1_word_r },
		{ CCPU_PORT_IOOUTPUTS,    CCPU_PORT_IOOUTPUTS+1,    cinemat_output_port_r },
		{ CCPU_PORT_IN_JOYSTICKX, CCPU_PORT_IN_JOYSTICKX+1, input_port_2_word_r },
		{ CCPU_PORT_IN_JOYSTICKY, CCPU_PORT_IN_JOYSTICKY+1, input_port_3_word_r },
	PORT_END
	
	
	static PORT_WRITE16_START( writeport )
		{ CCPU_PORT_IOOUTPUTS,    CCPU_PORT_IOOUTPUTS+1,    cinemat_output_port_w },
	PORT_END
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	/* switch definitions are all mangled; for ease of use, I created these handy macros */
	
	#define SW7 0x40
	#define SW6 0x02
	#define SW5 0x04
	#define SW4 0x08
	#define SW3 0x01
	#define SW2 0x20
	#define SW1 0x10
	
	#define SW7OFF SW7
	#define SW6OFF SW6
	#define SW5OFF SW5
	#define SW4OFF SW4
	#define SW3OFF SW3
	#define SW2OFF SW2
	#define SW1OFF SW1
	
	#define SW7ON  0
	#define SW6ON  0
	#define SW5ON  0
	#define SW4ON  0
	#define SW3ON  0
	#define SW2ON  0
	#define SW1ON  0
	
	
	static InputPortPtr input_ports_spacewar = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* switches */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_DIPNAME( SW2|SW1, SW2ON |SW1ON,  "Time" );
		PORT_DIPSETTING( 	   SW2OFF|SW1OFF, "0:45/coin" );
		PORT_DIPSETTING( 	   SW2ON |SW1ON,  "1:00/coin" );
		PORT_DIPSETTING( 	   SW2ON |SW1OFF, "1:30/coin" );
		PORT_DIPSETTING( 	   SW2OFF|SW1ON,  "2:00/coin" );
		PORT_DIPNAME( SW7,	   SW7OFF,		  DEF_STR( "Unknown") );
		PORT_DIPSETTING(	   SW7OFF,		  DEF_STR( "Off") );
		PORT_DIPSETTING(	   SW7ON,		  DEF_STR( "On") );
		PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
	
		PORT_START();  /* inputs */
		PORT_BIT ( 0x8000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT ( 0x4000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 );
		PORT_BIT ( 0x2000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 );
		PORT_BITX( 0x0800, IP_ACTIVE_LOW, 0, "Option 0", KEYCODE_0_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0400, IP_ACTIVE_LOW, 0, "Option 5", KEYCODE_5_PAD, IP_JOY_NONE );
		PORT_BIT ( 0x0200, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT ( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER1 );
		PORT_BITX( 0x0080, IP_ACTIVE_LOW, 0, "Option 7", KEYCODE_7_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0040, IP_ACTIVE_LOW, 0, "Option 2", KEYCODE_2_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0020, IP_ACTIVE_LOW, 0, "Option 6", KEYCODE_6_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0010, IP_ACTIVE_LOW, 0, "Option 1", KEYCODE_1_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0008, IP_ACTIVE_LOW, 0, "Option 9", KEYCODE_9_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0004, IP_ACTIVE_LOW, 0, "Option 4", KEYCODE_4_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0002, IP_ACTIVE_LOW, 0, "Option 8", KEYCODE_8_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0001, IP_ACTIVE_LOW, 0, "Option 3", KEYCODE_3_PAD, IP_JOY_NONE );
	
		PORT_START();  /* analog stick X - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* analog stick Y - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_barrier = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* switches */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_DIPNAME( SW1, SW1ON,  DEF_STR( "Lives") );
		PORT_DIPSETTING(   SW1ON,  "3" );
		PORT_DIPSETTING(   SW1OFF, "5" );
		PORT_DIPNAME( SW2, SW2OFF, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(   SW2ON,  DEF_STR( "Off") );
		PORT_DIPSETTING(   SW2OFF, DEF_STR( "On") );
		PORT_DIPNAME( SW3, SW3OFF, DEF_STR( "Unknown") );
		PORT_DIPSETTING(   SW3OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW3ON,  DEF_STR( "On") );
		PORT_DIPNAME( SW4, SW4OFF, DEF_STR( "Unknown") );
		PORT_DIPSETTING(   SW4OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW4ON,  DEF_STR( "On") );
		PORT_DIPNAME( SW5, SW5OFF, DEF_STR( "Unknown") );
		PORT_DIPSETTING(   SW5OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW5ON,  DEF_STR( "On") );
		PORT_DIPNAME( SW6, SW6OFF, DEF_STR( "Unknown") );
		PORT_DIPSETTING(   SW6OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW6ON,  DEF_STR( "On") );
		PORT_DIPNAME( SW7, SW7OFF, DEF_STR( "Unknown") );
		PORT_DIPSETTING(   SW7OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW7ON,  DEF_STR( "On") );
	
		PORT_START();  /* inputs */
		PORT_BIT ( 0x8000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER2 );
		PORT_BIT ( 0x4000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x2000, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER2 );
		PORT_BIT ( 0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x0800, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT ( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER2 );
		PORT_BIT ( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER2 );
		PORT_BIT ( 0x0080, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BITX( 0x0040, IP_ACTIVE_LOW, 0, "Skill C", KEYCODE_C, IP_JOY_NONE );
		PORT_BIT ( 0x0020, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0010, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT ( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER1 );
		PORT_BITX( 0x0004, IP_ACTIVE_LOW, 0, "Skill B", KEYCODE_B, IP_JOY_NONE );
		PORT_BIT ( 0x0002, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BITX( 0x0001, IP_ACTIVE_LOW, 0, "Skill A", KEYCODE_A, IP_JOY_NONE );
	
		PORT_START();  /* analog stick X - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* analog stick Y - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	/* TODO: 4way or 8way stick? */
	static InputPortPtr input_ports_starhawk = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* switches */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 2 );
		PORT_DIPNAME( SW7,	   SW7OFF,		  DEF_STR( "Unknown") );
		PORT_DIPSETTING(	   SW7OFF,		  DEF_STR( "Off") );
		PORT_DIPSETTING(	   SW7ON,		  DEF_STR( "On") );
		PORT_BIT ( SW6, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT ( SW5, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT ( SW4, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT ( SW3, IP_ACTIVE_LOW, IPT_START1 );
		PORT_DIPNAME( SW2|SW1, SW2OFF|SW1OFF, "Game Time" );
		PORT_DIPSETTING(	   SW2OFF|SW1OFF, "2:00/4:00" );
		PORT_DIPSETTING(	   SW2ON |SW1OFF, "1:30/3:00" );
		PORT_DIPSETTING(	   SW2OFF|SW1ON,  "1:00/2:00" );
		PORT_DIPSETTING(	   SW2ON |SW1ON,  "0:45/1:30" );
	
		PORT_START();  /* input */
		PORT_BIT ( 0x8000, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT ( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT ( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT ( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT ( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT ( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT ( 0x0200, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
		PORT_BIT ( 0x0100, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT ( 0x0080, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0040, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT ( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT ( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1 );
	
		PORT_START();  /* analog stick X - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* analog stick Y - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_starcas = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* switches */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_SERVICE( SW7,     SW7ON );
		PORT_DIPNAME( SW4|SW3, SW4OFF|SW3OFF, DEF_STR( "Coinage") );
		PORT_DIPSETTING(       SW4ON |SW3OFF, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(       SW4ON |SW3ON,  DEF_STR( "4C_3C") );
		PORT_DIPSETTING(       SW4OFF|SW3OFF, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(       SW4OFF|SW3ON,  DEF_STR( "2C_3C") );
		PORT_DIPNAME( SW2|SW1, SW2OFF|SW1OFF, DEF_STR( "Lives") );
		PORT_DIPSETTING(       SW2OFF|SW1OFF, "3" );
		PORT_DIPSETTING(       SW2ON |SW1OFF, "4" );
		PORT_DIPSETTING(       SW2OFF|SW1ON,  "5" );
		PORT_DIPSETTING(       SW2ON |SW1ON,  "6" );
		PORT_DIPNAME( SW5,     SW5OFF,        DEF_STR( "Unknown") );
		PORT_DIPSETTING(       SW5OFF,        DEF_STR( "Off") );
		PORT_DIPSETTING(       SW5ON,         DEF_STR( "On") );
		PORT_DIPNAME( SW6,     SW6OFF,        DEF_STR( "Unknown") );
		PORT_DIPSETTING(       SW6OFF,        DEF_STR( "Off") );
		PORT_DIPSETTING(       SW6ON,         DEF_STR( "On") );
	
		PORT_START();  /* inputs */
		PORT_BIT ( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT ( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x0400, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT ( 0x0200, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY );
		PORT_BIT ( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY );
		PORT_BIT ( 0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x0010, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x0008, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x0004, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT ( 0x0002, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x0001, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START();  /* analog stick X - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* analog stick Y - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_tailg = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* switches */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_DIPNAME( SW6|SW2|SW1, SW6OFF|SW2OFF|SW1OFF, "Shield Points" );
		PORT_DIPSETTING(		   SW6ON |SW2ON |SW1ON,  "15" );
		PORT_DIPSETTING(		   SW6ON |SW2OFF|SW1ON,  "20" );
		PORT_DIPSETTING(		   SW6ON |SW2ON |SW1OFF, "30" );
		PORT_DIPSETTING(		   SW6ON |SW2OFF|SW1OFF, "40" );
		PORT_DIPSETTING(		   SW6OFF|SW2ON |SW1ON,  "50" );
		PORT_DIPSETTING(		   SW6OFF|SW2OFF|SW1ON,  "60" );
		PORT_DIPSETTING(		   SW6OFF|SW2ON |SW1OFF, "70" );
		PORT_DIPSETTING(		   SW6OFF|SW2OFF|SW1OFF, "80" );
		PORT_DIPNAME( SW3,		   SW3OFF,				 DEF_STR( "Coinage") );
		PORT_DIPSETTING(		   SW3ON,				 DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		   SW3OFF,				 DEF_STR( "1C_1C") );
		PORT_DIPNAME( SW4,		   SW4OFF,				 DEF_STR( "Unknown") );
		PORT_DIPSETTING(		   SW4OFF,				 DEF_STR( "Off") );
		PORT_DIPSETTING(		   SW4ON, 				 DEF_STR( "On") );
		PORT_DIPNAME( SW5,		   SW5OFF,				 DEF_STR( "Unknown") );
		PORT_DIPSETTING(		   SW5OFF,				 DEF_STR( "Off") );
		PORT_DIPSETTING(		   SW5ON, 				 DEF_STR( "On") );
		PORT_DIPNAME( SW7,		   SW7OFF,				 DEF_STR( "Unknown") );
		PORT_DIPSETTING(		   SW7OFF,				 DEF_STR( "Off") );
		PORT_DIPSETTING(		   SW7ON, 				 DEF_STR( "On") );
	
		PORT_START();  /* inputs */
		PORT_BIT ( 0x8000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x4000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x2000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x1000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0800, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0400, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0200, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0100, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0080, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT ( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT ( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT ( 0x0010, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0008, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0004, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0002, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0001, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* analog stick X */
		PORT_ANALOG( 0xfff, 0x800, IPT_AD_STICK_X, 100, 50, 0x200, 0xe00 );
	
		PORT_START();  /* analog stick Y */
		PORT_ANALOG( 0xfff, 0x800, IPT_AD_STICK_Y, 100, 50, 0x200, 0xe00 );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_ripoff = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* switches */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_SERVICE( SW7,	   SW7OFF );
		PORT_DIPNAME( SW6,	   SW6ON,		  "Scores" );
		PORT_DIPSETTING(	   SW6ON,		  "Individual" );
		PORT_DIPSETTING(	   SW6OFF,		  "Combined" );
		PORT_DIPNAME( SW5,	   SW5OFF,		  DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	   SW5ON,		  DEF_STR( "Off") );
		PORT_DIPSETTING(	   SW5OFF,		  DEF_STR( "On") );
		PORT_DIPNAME( SW4|SW3, SW4ON |SW3ON,  DEF_STR( "Coinage") );
		PORT_DIPSETTING(	   SW4ON |SW3OFF, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	   SW4OFF|SW3OFF, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(	   SW4ON |SW3ON,  DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	   SW4OFF|SW3ON,  DEF_STR( "2C_3C") );
		PORT_DIPNAME( SW2|SW1, SW2OFF|SW1OFF, DEF_STR( "Lives") );
		PORT_DIPSETTING(	   SW2ON |SW1OFF, "4" );
		PORT_DIPSETTING(	   SW2OFF|SW1OFF, "8" );
		PORT_DIPSETTING(	   SW2ON |SW1ON,  "12" );
		PORT_DIPSETTING(	   SW2OFF|SW1ON,  "16" );
	
		PORT_START();  /* inputs */
		PORT_BIT ( 0x8000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT ( 0x4000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT ( 0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x0800, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0400, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0200, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0100, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0080, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0040, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT ( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT ( 0x0008, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT ( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 );
		PORT_BIT ( 0x0002, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT ( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 );
	
		PORT_START();  /* analog stick X - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* analog stick Y - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_speedfrk = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* switches */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_DIPNAME( SW7,     SW7OFF,        DEF_STR( "Unknown") );
		PORT_DIPSETTING(       SW7OFF,        DEF_STR( "Off") );
		PORT_DIPSETTING(       SW7ON,         DEF_STR( "On") );
		PORT_DIPNAME( SW6,     SW6OFF,        DEF_STR( "Unknown") );
		PORT_DIPSETTING(       SW6OFF,        DEF_STR( "Off") );
		PORT_DIPSETTING(       SW6ON,         DEF_STR( "On") );
		PORT_DIPNAME( SW5,     SW5OFF,        DEF_STR( "Unknown") );
		PORT_DIPSETTING(       SW5OFF,        DEF_STR( "Off") );
		PORT_DIPSETTING(       SW5ON,         DEF_STR( "On") );
		PORT_DIPNAME( SW4,     SW4OFF,        DEF_STR( "Unknown") );
		PORT_DIPSETTING(       SW4OFF,        DEF_STR( "Off") );
		PORT_DIPSETTING(       SW4ON,         DEF_STR( "On") );
		PORT_DIPNAME( SW3,     SW3OFF,        DEF_STR( "Unknown") );
		PORT_DIPSETTING(       SW3OFF,        DEF_STR( "Off") );
		PORT_DIPSETTING(       SW3ON,         DEF_STR( "On") );
		PORT_DIPNAME( SW2|SW1, SW2OFF|SW1ON,  "Extra Time" );
		PORT_DIPSETTING(       SW2ON |SW1ON,  "69" );
		PORT_DIPSETTING(       SW2ON |SW1OFF, "99" );
		PORT_DIPSETTING(       SW2OFF|SW1ON,  "129" );
		PORT_DIPSETTING(       SW2OFF|SW1OFF, "159" );
	
		PORT_START();  /* inputs */
		PORT_BIT (  0x8000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT (  0x4000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT (  0x2000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT (  0x1000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT (  0x0800, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT (  0x0400, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT (  0x0200, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT (  0x0100, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );/* gas */
		PORT_BIT (  0x0080, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT (  0x0070, IP_ACTIVE_LOW, IPT_UNUSED );/* gear shift, fake below */
		PORT_BIT (  0x000f, IP_ACTIVE_LOW, IPT_UNUSED );/* steering wheel, fake below */
	
		PORT_START();  /* analog stick X - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* analog stick Y - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* fake - steering wheel (in4) */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL, 100, 1, 0x00, 0xff );
	
		PORT_START();  /* fake - gear shift (in5) */
		PORT_BIT ( 0x0f, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BITX( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2, "1st gear", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2, "2nd gear", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2, "3rd gear", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2, "4th gear", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_sundance = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* switches */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_DIPNAME( SW5,	   SW5OFF,		 DEF_STR( "Unknown") );
		PORT_DIPSETTING(	   SW5OFF,		 DEF_STR( "Off") );
		PORT_DIPSETTING(	   SW5ON, 		 DEF_STR( "On") );
		PORT_DIPNAME( SW6,	   SW6OFF,		 DEF_STR( "Unknown") );
		PORT_DIPSETTING(	   SW6OFF,		 DEF_STR( "Off") );
		PORT_DIPSETTING(	   SW6ON,		 DEF_STR( "On") );
		PORT_DIPNAME( SW7,	   SW7OFF,		 DEF_STR( "Unknown") );
		PORT_DIPSETTING(	   SW7OFF,		 DEF_STR( "Off") );
		PORT_DIPSETTING(	   SW7ON,		 DEF_STR( "On") );
		PORT_DIPNAME( SW4,	   SW4OFF,		 DEF_STR( "Unknown") ); /* supposedly coinage, doesn't work */
		PORT_DIPSETTING(	   SW4OFF,		 DEF_STR( "Off") );
		PORT_DIPSETTING(	   SW4ON,		 DEF_STR( "On") );
		PORT_DIPNAME( SW3,	   SW3ON,		 "Language" );
		PORT_DIPSETTING(	   SW3OFF,		 "Japanese" );
		PORT_DIPSETTING(	   SW3ON,		 "English" );
		PORT_DIPNAME( SW2|SW1, SW2OFF|SW1ON, "Time" );
		PORT_DIPSETTING(	   SW2ON |SW1ON,  "0:45/coin" );
		PORT_DIPSETTING(	   SW2OFF|SW1ON,  "1:00/coin" );
		PORT_DIPSETTING(	   SW2ON |SW1OFF, "1:30/coin" );
		PORT_DIPSETTING(	   SW2OFF|SW1OFF, "2:00/coin" );
	
		PORT_START();  /* inputs */
		PORT_BIT ( 0x8000, IP_ACTIVE_LOW, IPT_SPECIAL );/* P2 Pad */
		PORT_BIT ( 0x4000, IP_ACTIVE_LOW, IPT_SPECIAL );/* P1 Pad */
		PORT_BIT ( 0x2000, IP_ACTIVE_LOW, IPT_SPECIAL );/* P2 Pad */
		PORT_BIT ( 0x1000, IP_ACTIVE_LOW, IPT_SPECIAL );/* P1 Pad */
		PORT_BITX( 0x0800, IP_ACTIVE_LOW, 0, "2 Suns", KEYCODE_COMMA, IP_JOY_NONE );
		PORT_BIT ( 0x0400, IP_ACTIVE_LOW, IPT_SPECIAL );/* P2 Pad */
		PORT_BIT ( 0x0200, IP_ACTIVE_LOW, IPT_SPECIAL );/* P1 Pad */
		PORT_BIT ( 0x0100, IP_ACTIVE_LOW, IPT_SPECIAL );/* P2 Pad */
		PORT_BITX( 0x0080, IP_ACTIVE_LOW, IPT_BUTTON10 | IPF_PLAYER2, "P2 Shoot", KEYCODE_LCONTROL, IP_JOY_DEFAULT );
		PORT_BITX( 0x0040, IP_ACTIVE_LOW, 0, "4 Suns", KEYCODE_SLASH, IP_JOY_NONE );
		PORT_BITX( 0x0020, IP_ACTIVE_LOW, 0, "Toggle Grid", KEYCODE_G, IP_JOY_NONE );
		PORT_BITX( 0x0010, IP_ACTIVE_LOW, 0, "3 Suns", KEYCODE_STOP, IP_JOY_NONE );
		PORT_BIT ( 0x0008, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT ( 0x0004, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BITX( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON10 | IPF_PLAYER1, "P1 Shoot", KEYCODE_0_PAD, IP_JOY_DEFAULT );
		PORT_BIT ( 0x0001, IP_ACTIVE_LOW, IPT_SPECIAL );/* P1 Pad */
	
		PORT_START();  /* analog stick X - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* analog stick Y - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 
		PORT_BITX( 0x0001, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1, "P1 Pad 1", KEYCODE_7_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0002, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1, "P1 Pad 2", KEYCODE_8_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0004, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1, "P1 Pad 3", KEYCODE_9_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0008, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER1, "P1 Pad 4", KEYCODE_4_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0010, IP_ACTIVE_HIGH, IPT_BUTTON5 | IPF_PLAYER1, "P1 Pad 5", KEYCODE_5_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0020, IP_ACTIVE_HIGH, IPT_BUTTON6 | IPF_PLAYER1, "P1 Pad 6", KEYCODE_6_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0040, IP_ACTIVE_HIGH, IPT_BUTTON7 | IPF_PLAYER1, "P1 Pad 7", KEYCODE_1_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0080, IP_ACTIVE_HIGH, IPT_BUTTON8 | IPF_PLAYER1, "P1 Pad 8", KEYCODE_2_PAD, IP_JOY_NONE );
		PORT_BITX( 0x0100, IP_ACTIVE_HIGH, IPT_BUTTON9 | IPF_PLAYER1, "P1 Pad 9", KEYCODE_3_PAD, IP_JOY_NONE );
	
		PORT_START(); 
		PORT_BITX( 0x0001, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2, "P2 Pad 1", KEYCODE_Q, IP_JOY_NONE );
		PORT_BITX( 0x0002, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2, "P2 Pad 2", KEYCODE_W, IP_JOY_NONE );
		PORT_BITX( 0x0004, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2, "P2 Pad 3", KEYCODE_E, IP_JOY_NONE );
		PORT_BITX( 0x0008, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER2, "P2 Pad 4", KEYCODE_A, IP_JOY_NONE );
		PORT_BITX( 0x0010, IP_ACTIVE_HIGH, IPT_BUTTON5 | IPF_PLAYER2, "P2 Pad 5", KEYCODE_S, IP_JOY_NONE );
		PORT_BITX( 0x0020, IP_ACTIVE_HIGH, IPT_BUTTON6 | IPF_PLAYER2, "P2 Pad 6", KEYCODE_D, IP_JOY_NONE );
		PORT_BITX( 0x0040, IP_ACTIVE_HIGH, IPT_BUTTON7 | IPF_PLAYER2, "P2 Pad 7", KEYCODE_Z, IP_JOY_NONE );
		PORT_BITX( 0x0080, IP_ACTIVE_HIGH, IPT_BUTTON8 | IPF_PLAYER2, "P2 Pad 8", KEYCODE_X, IP_JOY_NONE );
		PORT_BITX( 0x0100, IP_ACTIVE_HIGH, IPT_BUTTON9 | IPF_PLAYER2, "P2 Pad 9", KEYCODE_C, IP_JOY_NONE );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_warrior = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* switches */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_DIPNAME( SW7, SW7OFF, DEF_STR( "Unknown") );
		PORT_DIPSETTING(   SW7OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW7ON,  DEF_STR( "On") );
		PORT_DIPNAME( SW6, SW6OFF, DEF_STR( "Unknown") );
		PORT_DIPSETTING(   SW6OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW6ON,  DEF_STR( "On") );
		PORT_DIPNAME( SW5, SW5OFF, DEF_STR( "Unknown") );
		PORT_DIPSETTING(   SW5OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW5ON,  DEF_STR( "On") );
		PORT_DIPNAME( SW4, SW4OFF, DEF_STR( "Unknown") );
		PORT_DIPSETTING(   SW4OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW4ON,  DEF_STR( "On") );
		PORT_SERVICE( SW3, SW3ON );
		PORT_DIPNAME( SW2, SW2OFF, DEF_STR( "Unknown") );
		PORT_DIPSETTING(   SW2OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW2ON,  DEF_STR( "On") );
		PORT_DIPNAME( SW1, SW1ON,  DEF_STR( "Unknown") );
		PORT_DIPSETTING(   SW1OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW1ON,  DEF_STR( "On") );
	
		PORT_START();  /* inputs */
		PORT_BIT ( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x4000, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT ( 0x2000, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT ( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT ( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 );
		PORT_BIT ( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 );
		PORT_BIT ( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 );
		PORT_BIT ( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
		PORT_BIT ( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT ( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 );
		PORT_BIT ( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 );
		PORT_BIT ( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 );
		PORT_BIT ( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
	
		PORT_START();  /* analog stick X - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* analog stick Y - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_armora = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* switches */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_SERVICE( SW7,     SW7ON );
		PORT_DIPNAME( SW5,     SW5ON,         DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(       SW5OFF,        DEF_STR( "Off") );
		PORT_DIPSETTING(       SW5ON,         DEF_STR( "On") );
		PORT_DIPNAME( SW4|SW3, SW4OFF|SW3OFF, DEF_STR( "Coinage") );
		PORT_DIPSETTING(       SW4ON |SW3OFF, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(       SW4ON |SW3ON,  DEF_STR( "4C_3C") );
		PORT_DIPSETTING(       SW4OFF|SW3OFF, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(       SW4OFF|SW3ON,  DEF_STR( "2C_3C") );
		PORT_DIPNAME( SW2|SW1, SW2OFF|SW1OFF, DEF_STR( "Lives") );
		PORT_DIPSETTING(       SW2ON |SW1ON,  "2" );
		PORT_DIPSETTING(       SW2OFF|SW1ON,  "3" );
		PORT_DIPSETTING(       SW2ON |SW1OFF, "4" );
		PORT_DIPSETTING(       SW2OFF|SW1OFF, "5" );
	
		PORT_START();  /* inputs */
		PORT_BIT ( 0x8000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT ( 0x4000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT ( 0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x0800, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0400, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0200, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0100, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0080, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0040, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT ( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT ( 0x0008, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT ( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 );
		PORT_BIT ( 0x0002, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT ( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 );
	
		PORT_START();  /* analog stick X - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START();  /* analog stick Y - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_solarq = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* switches */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_SERVICE( SW7,	   SW7ON );
		PORT_DIPNAME( SW2,	   SW2OFF,		  DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	   SW2OFF,		  "25 captures" );
		PORT_DIPSETTING(	   SW2ON, 		  "40 captures" );
		PORT_DIPNAME( SW6,	   SW6OFF,		  DEF_STR( "Free_Play") );
		PORT_DIPSETTING(	   SW6OFF,		  DEF_STR( "Off") );
		PORT_DIPSETTING(	   SW6ON,		  DEF_STR( "On") );
		PORT_DIPNAME( SW1|SW3, SW1OFF|SW3OFF, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	   SW3ON |SW1OFF, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	   SW3ON |SW1ON,  DEF_STR( "4C_3C") );
		PORT_DIPSETTING(	   SW3OFF|SW1OFF, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	   SW3OFF|SW1ON,  DEF_STR( "2C_3C") );
		PORT_DIPNAME( SW5|SW4, SW5OFF|SW5OFF, DEF_STR( "Lives") );
		PORT_DIPSETTING(	   SW5OFF|SW4OFF, "2" );
		PORT_DIPSETTING(	   SW5ON |SW4OFF, "3" );
		PORT_DIPSETTING(	   SW5OFF|SW4ON,  "4" );
		PORT_DIPSETTING(	   SW5ON |SW4ON,  "5" );
	
		PORT_START();  /* inputs */
		PORT_BIT ( 0x8000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x4000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x2000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x1000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0800, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0400, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0200, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0100, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0080, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0040, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x0008, IP_ACTIVE_LOW, IPT_START1 );/* also hyperspace */
		PORT_BIT ( 0x0008, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT ( 0x0004, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT ( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT ( 0x0001, IP_ACTIVE_LOW, IPT_START2 );/* also nova */
		PORT_BIT ( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
	
		PORT_START();  /* analog stick X - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* analog stick Y - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_demon = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* switches */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_DIPNAME( SW7,     SW7OFF,        DEF_STR( "Free_Play") );
		PORT_DIPSETTING(       SW7OFF,        DEF_STR( "Off") );
		PORT_DIPSETTING(       SW7ON,         DEF_STR( "On") );
		PORT_DIPNAME( SW6,     SW6OFF,        DEF_STR( "Unknown") );
		PORT_DIPSETTING(       SW6OFF,        DEF_STR( "Off") );
		PORT_DIPSETTING(       SW6ON,         DEF_STR( "On") );
		PORT_DIPNAME( SW5,     SW5OFF,        DEF_STR( "Unknown") );
		PORT_DIPSETTING(       SW5OFF,        DEF_STR( "Off") );
		PORT_DIPSETTING(       SW5ON,         DEF_STR( "On") );
		PORT_DIPNAME( SW3|SW4, SW3ON |SW4ON,  DEF_STR( "Lives") );
		PORT_DIPSETTING(       SW3ON |SW4ON,  "3");
		PORT_DIPSETTING(       SW3OFF|SW4ON,  "4" );
		PORT_DIPSETTING(       SW3ON |SW4OFF, "5" );
		PORT_DIPSETTING(       SW3OFF|SW4OFF, "6" );
		PORT_DIPNAME( SW2|SW1, SW2OFF|SW1OFF, DEF_STR( "Coinage") );
		PORT_DIPSETTING(       SW2ON |SW1OFF, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(       SW2ON |SW1ON,  DEF_STR( "4C_3C") );
		PORT_DIPSETTING(       SW2OFF|SW1OFF, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(       SW2OFF|SW1ON,  DEF_STR( "2C_3C") );
	
		PORT_START();  /* inputs */
		PORT_BIT ( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );/* also mapped to Button 3, player 2 */
		PORT_BIT ( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT ( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT ( 0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 );
		PORT_BIT ( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 );
		PORT_BIT ( 0x0400, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT ( 0x0200, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT ( 0x0100, IP_ACTIVE_LOW, IPT_TILT );
		PORT_SERVICE( 0x0080, IP_ACTIVE_LOW );
		PORT_BIT ( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT ( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT ( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER1 );
		PORT_BIT ( 0x0002, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT ( 0x0001, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START();  /* analog stick X - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* analog stick Y - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_wotw = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* switches */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_SERVICE( SW7, SW7OFF );
		PORT_DIPNAME( SW6, SW6OFF, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(   SW6OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW6ON,  DEF_STR( "On") );
		PORT_DIPNAME( SW4, SW4OFF, DEF_STR( "Coinage") );
		PORT_DIPSETTING(   SW4OFF, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(   SW4ON,  DEF_STR( "2C_3C") );
		PORT_DIPNAME( SW2, SW2OFF, DEF_STR( "Lives") );
		PORT_DIPSETTING(   SW2OFF, "3" );
		PORT_DIPSETTING(   SW2ON,  "5" );
		PORT_DIPNAME( SW1, SW1OFF, DEF_STR( "Unknown") );
		PORT_DIPSETTING(   SW1OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW1ON,  DEF_STR( "On") );
		PORT_DIPNAME( SW3, SW3OFF, DEF_STR( "Unknown") );
		PORT_DIPSETTING(   SW3OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW3ON,  DEF_STR( "On") );
		PORT_DIPNAME( SW5, SW5OFF, DEF_STR( "Unknown") );
		PORT_DIPSETTING(   SW5OFF, DEF_STR( "Off") );
		PORT_DIPSETTING(   SW5ON,  DEF_STR( "On") );
	
		PORT_START();  /* inputs */
		PORT_BIT ( 0x8000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x4000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x2000, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT ( 0x0800, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0400, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT ( 0x0200, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY );
		PORT_BIT ( 0x0080, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY );
		PORT_BIT ( 0x0020, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0010, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0008, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0004, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT ( 0x0002, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x0001, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START();  /* analog stick X - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* analog stick Y - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_boxingb = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* switches */
		PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_SERVICE( SW7,	   SW7OFF );
		PORT_DIPNAME( SW6,	   SW6OFF,		  DEF_STR( "Free_Play") );
		PORT_DIPSETTING(	   SW6OFF,		  DEF_STR( "Off") );
		PORT_DIPSETTING(	   SW6ON,		  DEF_STR( "On") );
		PORT_DIPNAME( SW5,	   SW5ON,		  DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	   SW5OFF,		  DEF_STR( "Off") );
		PORT_DIPSETTING(	   SW5ON,		  DEF_STR( "On") );
		PORT_DIPNAME( SW4,	   SW4ON,		  DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	   SW4ON,		  "30,000" );
		PORT_DIPSETTING(	   SW4OFF,		  "50,000" );
		PORT_DIPNAME( SW3,	   SW3ON,		  DEF_STR( "Lives") );
		PORT_DIPSETTING(	   SW3OFF,		  "3" );
		PORT_DIPSETTING(	   SW3ON,		  "5" );
		PORT_DIPNAME( SW2|SW1, SW2OFF|SW1OFF, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	   SW2ON |SW1OFF, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	   SW2ON |SW1ON,  DEF_STR( "4C_3C") );
		PORT_DIPSETTING(	   SW2OFF|SW1OFF, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	   SW2OFF|SW1ON,  DEF_STR( "2C_3C") );
	
		PORT_START();  /* inputs */
		PORT_BIT ( 0xf000, IP_ACTIVE_HIGH, IPT_UNUSED );/* dial */
		PORT_BIT ( 0x0800, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x0400, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x0200, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x0100, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x0080, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x0040, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x0020, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT ( 0x0010, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT ( 0x0008, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT ( 0x0008, IP_ACTIVE_LOW,  IPT_START1 );
		PORT_BIT ( 0x0004, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT ( 0x0004, IP_ACTIVE_LOW,  IPT_START2 );
		PORT_BIT ( 0x0002, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT ( 0x0001, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER2 );
	
		PORT_START();  /* analog stick X - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* analog stick Y - unused */
		PORT_BIT ( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* fake (in4) */
		PORT_ANALOG( 0xff, 0x80, IPT_DIAL, 100, 5, 0x00, 0xff );
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Machine drivers
	 *
	 *************************************/
	
	/* Note: the CPU speed is somewhat arbitrary as the cycle timings in
	   the core are incomplete. */
	static MACHINE_DRIVER_START( cinemat )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(CCPU, 5000000)
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(readport,writeport)
	
		MDRV_FRAMES_PER_SECOND(38)
		MDRV_MACHINE_INIT(cinemat_sound)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_VECTOR | VIDEO_RGB_DIRECT)
		MDRV_SCREEN_SIZE(400, 300)
		MDRV_VISIBLE_AREA(0, 1024, 0, 768)
		MDRV_PALETTE_LENGTH(32768)
	
		MDRV_PALETTE_INIT(cinemat)
		MDRV_VIDEO_START(cinemat)
		MDRV_VIDEO_EOF(cinemat)
		MDRV_VIDEO_UPDATE(vector)
	
		/* sound hardware */
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( spacewar )
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(cinemat)
	
		/* video hardware */
		MDRV_VIDEO_UPDATE(spacewar)
	
		/* sound hardware */
		MDRV_SOUND_ADD(SAMPLES, spacewar_samples_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( starcas )
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(cinemat)
	
		/* sound hardware */
		MDRV_SOUND_ADD(SAMPLES, starcas_samples_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( ripoff )
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(cinemat)
	
		/* sound hardware */
		MDRV_SOUND_ADD(SAMPLES, ripoff_samples_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( warrior )
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(cinemat)
	
		/* video hardware */
		MDRV_VISIBLE_AREA(0, 1024, 0, 780)
	
		/* sound hardware */
		MDRV_SOUND_ADD(SAMPLES, warrior_samples_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( armora )
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(cinemat)
	
		/* video hardware */
		MDRV_VISIBLE_AREA(0, 1024, 0, 772)
	        /* sound hardware */
		MDRV_SOUND_ADD(SAMPLES, armora_samples_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( solarq )
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(cinemat)
	
		/* sound hardware */
		MDRV_SOUND_ADD(SAMPLES, solarq_samples_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( demon )
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(cinemat)
		MDRV_IMPORT_FROM(demon_sound)
	
		/* video hardware */
		MDRV_VISIBLE_AREA(0, 1024, 0, 800)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( sundance )
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(cinemat)
	
		/* sound hardware */
		MDRV_SOUND_ADD(SAMPLES, sundance_samples_interface)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( cincolor )
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(cinemat)
	
		/* video hardware */
		MDRV_PALETTE_INIT(cinemat_color)
	MACHINE_DRIVER_END
	
	
	
	
	/*************************************
	 *
	 *	ROM definitions
	 *
	 *************************************/
	
	static RomLoadPtr rom_spacewar = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 4k for code */
		ROM_LOAD16_BYTE( "spacewar.1l", 0x8000, 0x0800, CRC(edf0fd53) SHA1(a543d8b95bc77ec061c6b10161a6f3e07401e251) )
		ROM_LOAD16_BYTE( "spacewar.2r", 0x8001, 0x0800, CRC(4f21328b) SHA1(8889f1a9353d6bb1e1078829c1ba77557853739b) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_barrier = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 4k for code */
		ROM_LOAD16_BYTE( "barrier.t7", 0x8000, 0x0800, CRC(7c3d68c8) SHA1(1138029552b73e94522b3b48096befc057d603c7) )
		ROM_LOAD16_BYTE( "barrier.p7", 0x8001, 0x0800, CRC(aec142b5) SHA1(b268936b82e072f38f1f1dd54e0bc88bcdf19925) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_starhawk = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 4k for code */
		ROM_LOAD16_BYTE( "u7", 0x8000, 0x0800, CRC(376e6c5c) SHA1(7d9530ed2e75464578b541f61408ba64ee9d2a95) )
		ROM_LOAD16_BYTE( "r7", 0x8001, 0x0800, CRC(bb71144f) SHA1(79591cd3ef8df78ec26e158f7e82ca0dcd72260d) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_starcas = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 8k for code */
		ROM_LOAD16_BYTE( "starcas3.t7", 0x8000, 0x0800, CRC(b5838b5d) SHA1(6ac30be55514cba55180c85af69072b5056d1d4c) )
		ROM_LOAD16_BYTE( "starcas3.p7", 0x8001, 0x0800, CRC(f6bc2f4d) SHA1(ef6f01556b154cfb3e37b2a99d6ea6292e5ec844) )
		ROM_LOAD16_BYTE( "starcas3.u7", 0x9000, 0x0800, CRC(188cd97c) SHA1(c021e93a01e9c65013073de551a8c24fd1a68bde) )
		ROM_LOAD16_BYTE( "starcas3.r7", 0x9001, 0x0800, CRC(c367b69d) SHA1(98354d34ceb03e080b1846611d533be7bdff01cc) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_starcasp = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 8k for code */
		ROM_LOAD16_BYTE( "starcasp.t7", 0x8000, 0x0800, CRC(d2c551a2) SHA1(90b5e1c6988839b812028f1baaea16420c011c08) )
		ROM_LOAD16_BYTE( "starcasp.p7", 0x8001, 0x0800, CRC(baa4e422) SHA1(9035ac675fcbbb93ae3f658339fdfaef47796dab) )
		ROM_LOAD16_BYTE( "starcasp.u7", 0x9000, 0x0800, CRC(26941991) SHA1(4417f2f3e437c1f39ff389362467928f57045d74) )
		ROM_LOAD16_BYTE( "starcasp.r7", 0x9001, 0x0800, CRC(5dd151e5) SHA1(f3b0e2bd3121ac0649938eb2f676d171bcc7d4dd) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_starcas1 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 8k for code */
		ROM_LOAD16_BYTE( "starcast.t7", 0x8000, 0x0800, CRC(65d0a225) SHA1(e1fbee5ff42dd040ab2e90bbe2189fcb76d6167e) )
		ROM_LOAD16_BYTE( "starcast.p7", 0x8001, 0x0800, CRC(d8f58d9a) SHA1(abba459431dcacc75099b0d340b957be71b89cfd) )
		ROM_LOAD16_BYTE( "starcast.u7", 0x9000, 0x0800, CRC(d4f35b82) SHA1(cd4561ce8e1d0554ac1a8925bbf46d2c676a3b80) )
		ROM_LOAD16_BYTE( "starcast.r7", 0x9001, 0x0800, CRC(9fd3de54) SHA1(17195a490b190e68660829850ff9d702ca1939bb) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_starcase = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 8k for code */
		ROM_LOAD16_BYTE( "starcast.t7", 0x8000, 0x0800, CRC(65d0a225) SHA1(e1fbee5ff42dd040ab2e90bbe2189fcb76d6167e) )
		ROM_LOAD16_BYTE( "starcast.p7", 0x8001, 0x0800, CRC(d8f58d9a) SHA1(abba459431dcacc75099b0d340b957be71b89cfd) )
		ROM_LOAD16_BYTE( "starcast.u7", 0x9000, 0x0800, CRC(d4f35b82) SHA1(cd4561ce8e1d0554ac1a8925bbf46d2c676a3b80) )
		ROM_LOAD16_BYTE( "mottoeis.r7", 0x9001, 0x0800, CRC(a2c1ed52) SHA1(ed9743f44ee98c9e7c2a6819ec681af7c7a97fc9) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_stellcas = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 8k for code */
		ROM_LOAD16_BYTE( "starcast.t7", 0x8000, 0x0800, CRC(65d0a225) SHA1(e1fbee5ff42dd040ab2e90bbe2189fcb76d6167e) )
		ROM_LOAD16_BYTE( "starcast.p7", 0x8001, 0x0800, CRC(d8f58d9a) SHA1(abba459431dcacc75099b0d340b957be71b89cfd) )
		ROM_LOAD16_BYTE( "elttron.u7",  0x9000, 0x0800, CRC(d5b44050) SHA1(a5dd6050ab1a3b0275a229845bc5e9524e2da69c) )
		ROM_LOAD16_BYTE( "elttron.r7",  0x9001, 0x0800, CRC(6f1f261e) SHA1(a22a52af12a5cfbb9031fdd12c9c78db28f28ff1) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_tailg = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 8k for code */
		ROM_LOAD16_BYTE( "tgunner.t70", 0x8000, 0x0800, CRC(21ec9a04) SHA1(b442f34360d1d4769e7bca73a2d79ce97d335460) )
		ROM_LOAD16_BYTE( "tgunner.p70", 0x8001, 0x0800, CRC(8d7410b3) SHA1(59ead49bd229a873f15334d0999c872d3d6581d4) )
		ROM_LOAD16_BYTE( "tgunner.t71", 0x9000, 0x0800, CRC(2c954ab6) SHA1(9edf189a19b50a9abf458d4ef8ba25b53934385e) )
		ROM_LOAD16_BYTE( "tgunner.p71", 0x9001, 0x0800, CRC(8e2c8494) SHA1(65e461ec4938f9895e5ac31442193e06c8731dc1) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_ripoff = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 8k for code */
		ROM_LOAD16_BYTE( "ripoff.t7", 0x8000, 0x0800, CRC(40c2c5b8) SHA1(bc1f3b540475c9868443a72790a959b1f36b93c6) )
		ROM_LOAD16_BYTE( "ripoff.p7", 0x8001, 0x0800, CRC(a9208afb) SHA1(ea362494855be27a07014832b01e65c1645385d0) )
		ROM_LOAD16_BYTE( "ripoff.u7", 0x9000, 0x0800, CRC(29c13701) SHA1(5e7672deffac1fa8f289686a5527adf7e51eb0bb) )
		ROM_LOAD16_BYTE( "ripoff.r7", 0x9001, 0x0800, CRC(150bd4c8) SHA1(e1e2f0dfec4f53d8ff67b0e990514c304f496b3a) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_speedfrk = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 8k for code */
		ROM_LOAD16_BYTE( "speedfrk.t7", 0x8000, 0x0800, CRC(3552c03f) SHA1(c233dd064195b336556d7405b51065389b228c78) )
		ROM_LOAD16_BYTE( "speedfrk.p7", 0x8001, 0x0800, CRC(4b90cdec) SHA1(69e2312acdc22ef52236b1c4dfee9f51fcdcaa52) )
		ROM_LOAD16_BYTE( "speedfrk.u7", 0x9000, 0x0800, CRC(616c7cf9) SHA1(3c5bf59a09d85261f69e4b9d499cb7a93d79fb57) )
		ROM_LOAD16_BYTE( "speedfrk.r7", 0x9001, 0x0800, CRC(fbe90d63) SHA1(e42b17133464ae48c90263bba01a7d041e938a05) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_sundance = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 8k for code */
		ROM_LOAD16_BYTE( "sundance.t7", 0x8000, 0x0800, CRC(d5b9cb19) SHA1(72dca386b48a582186898c32123d61b4fd58632e) )
		ROM_LOAD16_BYTE( "sundance.p7", 0x8001, 0x0800, CRC(445c4f20) SHA1(972d0b0613f154ee3347206cae05ee8c36796f84) )
		ROM_LOAD16_BYTE( "sundance.u7", 0x9000, 0x0800, CRC(67887d48) SHA1(be225dbd3508fad2711286834880065a4fc0a2fc) )
		ROM_LOAD16_BYTE( "sundance.r7", 0x9001, 0x0800, CRC(10b77ebd) SHA1(3d43bd47c498d5ea74a7322f8d25dbc0c0187534) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_warrior = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 8k for code */
		ROM_LOAD16_BYTE( "warrior.t7", 0x8000, 0x0800, CRC(ac3646f9) SHA1(515c3acb638fad27fa57f6b438c8ec0b5b76f319) )
		ROM_LOAD16_BYTE( "warrior.p7", 0x8001, 0x0800, CRC(517d3021) SHA1(0483dcaf92c336a07d2c535823348ee886567e85) )
		ROM_LOAD16_BYTE( "warrior.u7", 0x9000, 0x0800, CRC(2e39340f) SHA1(4b3cfb3674dd2a668d4d65e28cb37d7ad20f118d) )
		ROM_LOAD16_BYTE( "warrior.r7", 0x9001, 0x0800, CRC(8e91b502) SHA1(27614c3a8613f49187039cfb05ee96303caf72ba) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_armora = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 16k for code */
		ROM_LOAD16_BYTE( "ar414le.t6", 0x8000, 0x1000, CRC(d7e71f84) SHA1(0b29278a6a698f07eae597bc0a8650e91eaabffa) )
		ROM_LOAD16_BYTE( "ar414lo.p6", 0x8001, 0x1000, CRC(df1c2370) SHA1(b74834d1a591a741892ec41269a831d3590ff766) )
		ROM_LOAD16_BYTE( "ar414ue.u6", 0xa000, 0x1000, CRC(b0276118) SHA1(88f33cb2f46a89819c85f810c7cff812e918391e) )
		ROM_LOAD16_BYTE( "ar414uo.r6", 0xa001, 0x1000, CRC(229d779f) SHA1(0cbdd83eb224146944049346f30d9c72d3ad5f52) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_armorap = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 16k for code */
		ROM_LOAD16_BYTE( "ar414le.t6", 0x8000, 0x1000, CRC(d7e71f84) SHA1(0b29278a6a698f07eae597bc0a8650e91eaabffa) )
		ROM_LOAD16_BYTE( "ar414lo.p6", 0x8001, 0x1000, CRC(df1c2370) SHA1(b74834d1a591a741892ec41269a831d3590ff766) )
		ROM_LOAD16_BYTE( "armorp.u7",  0xa000, 0x1000, CRC(4a86bd8a) SHA1(36647805c40688588dde81c7cbf4fe356b0974fc) )
		ROM_LOAD16_BYTE( "armorp.r7",  0xa001, 0x1000, CRC(d2dd4eae) SHA1(09afaeb0b8f88edb17e42bd2d754af0ae53e609a) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_armorar = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 16k for code */
		ROM_LOAD16_BYTE( "armorr.t7", 0x8000, 0x0800, CRC(256d1ed9) SHA1(8c101356c3fe93f2f49d5dc9d739f3b37cdb98b5) )
		ROM_LOAD16_BYTE( "armorr.p7", 0x8001, 0x0800, CRC(bf75c158) SHA1(4d52630ae0ea2ad16bb5f577ad6d21f52e2f0a3c) )
		ROM_LOAD16_BYTE( "armorr.u7", 0x9000, 0x0800, CRC(ba68331d) SHA1(871c3f5b6c2845f270e3a272fdb07aed8b527641) )
		ROM_LOAD16_BYTE( "armorr.r7", 0x9001, 0x0800, CRC(fa14c0b3) SHA1(37b233f0dac51eaf7d325628a6cced9367b6b6cb) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_solarq = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 16k for code */
		ROM_LOAD16_BYTE( "solar.6t", 0x8000, 0x1000, CRC(1f3c5333) SHA1(58d847b5f009a0363ae116768b22d0bcfb3d60a4) )
		ROM_LOAD16_BYTE( "solar.6p", 0x8001, 0x1000, CRC(d6c16bcc) SHA1(6953bdc698da060d37f6bc33a810ba44595b1257) )
		ROM_LOAD16_BYTE( "solar.6u", 0xa000, 0x1000, CRC(a5970e5c) SHA1(9ac07924ca86d003964022cffdd6a0436dde5624) )
		ROM_LOAD16_BYTE( "solar.6r", 0xa001, 0x1000, CRC(b763fff2) SHA1(af1fd978e46a4aee3048e6e36c409821d986f7ee) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_demon = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 16k for code */
		ROM_LOAD16_BYTE( "demon.7t",  0x8000, 0x1000, CRC(866596c1) SHA1(65202dcd5c6bf6c11fe76a89682a1505b1870cc9) )
		ROM_LOAD16_BYTE( "demon.7p",  0x8001, 0x1000, CRC(1109e2f1) SHA1(c779b6af1ca09e2e295fc9a0e221ddf283b683ed) )
		ROM_LOAD16_BYTE( "demon.7u",  0xa000, 0x1000, CRC(d447a3c3) SHA1(32f6fb01231aa4f3d93e32d639a89f0cf9624a71) )
		ROM_LOAD16_BYTE( "demon.7r",  0xa001, 0x1000, CRC(64b515f0) SHA1(2dd9a6d784ec1baf31e8c6797ddfdc1423c69470) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )	/* 64k for code */
		ROM_LOAD         ( "demon.snd", 0x0000, 0x1000, CRC(1e2cc262) SHA1(2aae537574ac69c92a3c6400b971e994de88d915) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_wotw = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 16k for code */
		ROM_LOAD16_BYTE( "wow_le.t7", 0x8000, 0x1000, CRC(b16440f9) SHA1(9656a26814736f8ff73575063b5ebbb2e8aa7dd0) )
		ROM_LOAD16_BYTE( "wow_lo.p7", 0x8001, 0x1000, CRC(bfdf4a5a) SHA1(db4eceb68e17020d0a597ba105ec3b91ce48b7c1) )
		ROM_LOAD16_BYTE( "wow_ue.u7", 0xa000, 0x1000, CRC(9b5cea48) SHA1(c2bc002e550a0d36e713d07f6aefa79c70b8e284) )
		ROM_LOAD16_BYTE( "wow_uo.r7", 0xa001, 0x1000, CRC(c9d3c866) SHA1(57a47bf06838fe562981321249fe5ae585316f22) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_boxingb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 32k for code */
		ROM_LOAD16_BYTE( "u1a", 0x8000, 0x1000, CRC(d3115b0f) SHA1(9448e7ac1cdb5c7e0739623151be230ab630c4ea) )
		ROM_LOAD16_BYTE( "u1b", 0x8001, 0x1000, CRC(3a44268d) SHA1(876ebe942ded787cfe357563a33d3e26a1483c5a) )
		ROM_LOAD16_BYTE( "u2a", 0xa000, 0x1000, CRC(c97a9cbb) SHA1(8bdeb9ee6b24c0a4554bbf4532a43481a0360019) )
		ROM_LOAD16_BYTE( "u2b", 0xa001, 0x1000, CRC(98d34ff5) SHA1(6767a02a99a01712383300f9acb96cdeffbc9c69) )
		ROM_LOAD16_BYTE( "u3a", 0xc000, 0x1000, CRC(5bb3269b) SHA1(a9dbc91b1455760f10bad0d2ccf540e040a00d4e) )
		ROM_LOAD16_BYTE( "u3b", 0xc001, 0x1000, CRC(85bf83ad) SHA1(9229042e39c53fae56dc93f8996bf3a3fcd35cb8) )
		ROM_LOAD16_BYTE( "u4a", 0xe000, 0x1000, CRC(25b51799) SHA1(46465fe62907ae66a0ce730581e4e9ba330d4369) )
		ROM_LOAD16_BYTE( "u4b", 0xe001, 0x1000, CRC(7f41de6a) SHA1(d01dffad3cb6e76c535a034ea0277dce5801c5f1) )
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Driver initialization
	 *
	 *************************************/
	
	static DRIVER_INIT( spacewar )
	{
		ccpu_Config(0, CCPU_MEMSIZE_4K, CCPU_MONITOR_BILEV);
		cinemat_sound_handler = spacewar_sound_w;
	}
	
	
	static DRIVER_INIT( barrier )
	{
		ccpu_Config(1, CCPU_MEMSIZE_4K, CCPU_MONITOR_BILEV);
		cinemat_sound_handler = 0;
	}
	
	
	static DRIVER_INIT( starhawk )
	{
		ccpu_Config(1, CCPU_MEMSIZE_4K, CCPU_MONITOR_BILEV);
		cinemat_sound_handler = 0;
	}
	
	
	static DRIVER_INIT( starcas )
	{
		ccpu_Config(1, CCPU_MEMSIZE_8K, CCPU_MONITOR_BILEV);
		cinemat_sound_handler = starcas_sound_w;
		artwork_set_overlay(starcas_overlay);
	}
	
	
	static DRIVER_INIT( tailg )
	{
		ccpu_Config(0, CCPU_MEMSIZE_8K, CCPU_MONITOR_BILEV);
		cinemat_sound_handler = 0;
		artwork_set_overlay(tailg_overlay);
	}
	
	
	static DRIVER_INIT( ripoff )
	{
		ccpu_Config(1, CCPU_MEMSIZE_8K, CCPU_MONITOR_BILEV);
		cinemat_sound_handler = ripoff_sound_w;
	}
	
	
	static DRIVER_INIT( speedfrk )
	{
		ccpu_Config(0, CCPU_MEMSIZE_8K, CCPU_MONITOR_BILEV);
		cinemat_sound_handler = 0;
	
		install_port_read16_handler(0, CCPU_PORT_IOINPUTS, CCPU_PORT_IOINPUTS+1, speedfrk_input_port_1_r);
	}
	
	
	static DRIVER_INIT( sundance )
	{
		ccpu_Config(1, CCPU_MEMSIZE_8K, CCPU_MONITOR_16LEV);
		cinemat_sound_handler = sundance_sound_w;
		artwork_set_overlay(sundance_overlay);
	
		install_port_read16_handler(0, CCPU_PORT_IOINPUTS, CCPU_PORT_IOINPUTS+1, sundance_input_port_1_r);
	}
	
	
	static DRIVER_INIT( warrior )
	{
		ccpu_Config(1, CCPU_MEMSIZE_8K, CCPU_MONITOR_BILEV);
		cinemat_sound_handler = warrior_sound_w;
	}
	
	
	static DRIVER_INIT( armora )
	{
		ccpu_Config(1, CCPU_MEMSIZE_16K, CCPU_MONITOR_BILEV);
		cinemat_sound_handler = armora_sound_w;
	}
	
	
	static DRIVER_INIT( armorar )
	{
		ccpu_Config(1, CCPU_MEMSIZE_8K, CCPU_MONITOR_BILEV);
		cinemat_sound_handler = armora_sound_w;
	}
	
	
	static DRIVER_INIT( solarq )
	{
		ccpu_Config(1, CCPU_MEMSIZE_16K, CCPU_MONITOR_BILEV);
		cinemat_sound_handler = solarq_sound_w;
		artwork_set_overlay(solarq_overlay);
	}
	
	
	static DRIVER_INIT( demon )
	{
		unsigned char *RAM = memory_region(REGION_CPU2);
	
		ccpu_Config(1, CCPU_MEMSIZE_16K, CCPU_MONITOR_BILEV);
		cinemat_sound_handler = demon_sound_w;
	
		RAM[0x0091]=0xcb;	/* bit 7,a */
		RAM[0x0092]=0x7f;
		RAM[0x0093]=0xc2;	/* jp nz,$0088 */
		RAM[0x0094]=0x88;
		RAM[0x0095]=0x00;
		RAM[0x0096]=0xc3;	/* jp $00fd */
		RAM[0x0097]=0xfd;
		RAM[0x0098]=0x00;
	}
	
	
	static DRIVER_INIT( wotw )
	{
		ccpu_Config(1, CCPU_MEMSIZE_16K, CCPU_MONITOR_WOWCOL);
		cinemat_sound_handler = 0;
	}
	
	
	static DRIVER_INIT( boxingb )
	{
		ccpu_Config(1, CCPU_MEMSIZE_32K, CCPU_MONITOR_WOWCOL);
		cinemat_sound_handler = 0;
	
		install_port_read16_handler(0, CCPU_PORT_IOINPUTS, CCPU_PORT_IOINPUTS+1, boxingb_input_port_1_r);
	}
	
	
	
	/*************************************
	 *
	 *	Game drivers
	 *
	 *************************************/
	
	GAME( 1978, spacewar, 0,       spacewar, spacewar, spacewar, ROT0,   "Cinematronics", "Space Wars" )
	GAMEX(1979, barrier,  0,       cinemat,  barrier,  barrier,  ROT270, "Vectorbeam", "Barrier", GAME_NO_SOUND )
	GAMEX(1981, starhawk, 0,       cinemat,  starhawk, starhawk, ROT0,   "Cinematronics", "Star Hawk", GAME_NO_SOUND )
	GAME( 1980, starcas,  0,       starcas,  starcas,  starcas,  ROT0,   "Cinematronics", "Star Castle (version 3)" )
	GAME( 1980, starcas1, starcas, starcas,  starcas,  starcas,  ROT0,   "Cinematronics", "Star Castle (older)" )
	GAME( 1980, starcasp, starcas, starcas,  starcas,  starcas,  ROT0,   "Cinematronics", "Star Castle (prototype)" )
	GAME( 1980, starcase, starcas, starcas,  starcas,  starcas,  ROT0,   "Cinematronics (Mottoeis license)", "Star Castle (Mottoeis)" )
	GAME( 1980, stellcas, starcas, starcas,  starcas,  starcas,  ROT0,   "bootleg", "Stellar Castle (Elettronolo)" )
	GAMEX(1979, tailg,    0,       cinemat,  tailg,    tailg,    ROT0,   "Cinematronics", "Tailgunner", GAME_NO_SOUND )
	GAME( 1979, ripoff,   0,       ripoff,   ripoff,   ripoff,   ROT0,   "Cinematronics", "Rip Off" )
	GAMEX(1979, speedfrk, 0,       cinemat,  speedfrk, speedfrk, ROT0,   "Vectorbeam", "Speed Freak", GAME_NO_SOUND )
	GAME(1979, sundance, 0,       sundance,  sundance, sundance, ROT270, "Cinematronics", "Sundance" )
	GAME( 1978, warrior,  0,       warrior,  warrior,  warrior,  ROT0,   "Vectorbeam", "Warrior" )
	GAME(1980, armora,   0,       armora,   armora,   armora,   ROT0,   "Cinematronics", "Armor Attack")
	GAME(1980, armorap,  armora,  armora,   armora,   armora,   ROT0,   "Cinematronics", "Armor Attack (prototype)")
	GAME(1980, armorar,  armora,  armora,   armora,   armorar,  ROT0,  "Cinematronics (Rock-ola license)", "Armor Attack (Rock-ola)" )
	GAME( 1981, solarq,   0,       solarq,   solarq,   solarq,   ORIENTATION_FLIP_X, "Cinematronics", "Solar Quest" )
	GAME( 1982, demon,    0,       demon,    demon,    demon,    ROT0,   "Rock-ola", "Demon" )
	GAMEX(1981, wotw,     0,       cincolor, wotw,     wotw,     ROT0,   "Cinematronics", "War of the Worlds", GAME_IMPERFECT_COLORS | GAME_NO_SOUND )
	GAMEX(1981, boxingb,  0,       cincolor, boxingb,  boxingb,  ROT0,   "Cinematronics", "Boxing Bugs", GAME_IMPERFECT_COLORS | GAME_NO_SOUND )
}
