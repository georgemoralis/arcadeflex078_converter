/***************************************************************************

IronHorse

driver by Mirko Buffoni

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class ironhors
{
	
	
	static UINT8 *ironhors_interrupt_enable;
	
	
	
	
	public static InterruptHandlerPtr ironhors_interrupt = new InterruptHandlerPtr() {public void handler(){
		if (cpu_getiloops() == 0)
		{
			if (*ironhors_interrupt_enable & 4)
				cpu_set_irq_line(0, M6809_FIRQ_LINE, HOLD_LINE);
		}
		else if (cpu_getiloops() % 2)
		{
			if (*ironhors_interrupt_enable & 1)
				cpu_set_irq_line(0, IRQ_LINE_NMI, PULSE_LINE);
		}
	} };
	
	public static WriteHandlerPtr ironhors_sh_irqtrigger_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		cpu_set_irq_line_and_vector(1,0,HOLD_LINE,0xff);
	} };
	
	public static WriteHandlerPtr ironhors_filter_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		set_RC_filter(0,1000,2200,1000,data & 0x04 ? 220000 : 0); /* YM2203-SSG-A */
		set_RC_filter(1,1000,2200,1000,data & 0x02 ? 220000 : 0); /* YM2203-SSG-B */
		set_RC_filter(2,1000,2200,1000,data & 0x01 ? 220000 : 0); /* YM2203-SSG-C */
	} };
	
	
	public static Memory_ReadAddress ironhors_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0020, 0x003f, MRA_RAM ),
		new Memory_ReadAddress( 0x0900, 0x0900, input_port_5_r ),	/* Dipswitch settings 2 */
		new Memory_ReadAddress( 0x0a00, 0x0a00, input_port_3_r ),	/* Dipswitch settings 0 */
		new Memory_ReadAddress( 0x0b00, 0x0b00, input_port_4_r ),	/* Dipswitch settings 1 */
		new Memory_ReadAddress( 0x0b01, 0x0b01, input_port_2_r ),	/* player 2 controls */
		new Memory_ReadAddress( 0x0b02, 0x0b02, input_port_1_r ),	/* player 1 controls */
		new Memory_ReadAddress( 0x0b03, 0x0b03, input_port_0_r ),	/* coins + selftest */
		new Memory_ReadAddress( 0x2000, 0x2fff, MRA_RAM ),
		new Memory_ReadAddress( 0x3000, 0x3fff, MRA_RAM ),
		new Memory_ReadAddress( 0x4000, 0xffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress ironhors_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0003, 0x0003, ironhors_charbank_w ),
		new Memory_WriteAddress( 0x0004, 0x0004, MWA_RAM, ironhors_interrupt_enable ),
		new Memory_WriteAddress( 0x0020, 0x003f, MWA_RAM, ironhors_scroll ),
		new Memory_WriteAddress( 0x0800, 0x0800, soundlatch_w ),
		new Memory_WriteAddress( 0x0900, 0x0900, ironhors_sh_irqtrigger_w ),  /* cause interrupt on audio CPU */
		new Memory_WriteAddress( 0x0a00, 0x0a00, ironhors_palettebank_w ),	/* + coin counters */
		new Memory_WriteAddress( 0x0b00, 0x0b00, ironhors_flipscreen_w ),
		new Memory_WriteAddress( 0x2000, 0x23ff, ironhors_colorram_w, colorram ),
		new Memory_WriteAddress( 0x2400, 0x27ff, ironhors_videoram_w, videoram ),
		new Memory_WriteAddress( 0x2800, 0x2fff, MWA_RAM ),
		new Memory_WriteAddress( 0x3000, 0x30ff, MWA_RAM, spriteram_2 ),
		new Memory_WriteAddress( 0x3100, 0x37ff, MWA_RAM ),
		new Memory_WriteAddress( 0x3800, 0x38ff, MWA_RAM, spriteram, spriteram_size ),
		new Memory_WriteAddress( 0x3900, 0x3fff, MWA_RAM ),
		new Memory_WriteAddress( 0x4000, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress ironhors_sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new Memory_ReadAddress( 0x4000, 0x43ff, MRA_RAM ),
		new Memory_ReadAddress( 0x8000, 0x8000, soundlatch_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress ironhors_sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new Memory_WriteAddress( 0x4000, 0x43ff, MWA_RAM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort ironhors_sound_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x00, YM2203_status_port_0_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort ironhors_sound_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x00, YM2203_control_port_0_w ),
		new IO_WritePort( 0x01, 0x01, YM2203_write_port_0_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_ReadAddress farwest_sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new Memory_ReadAddress( 0x4000, 0x43ff, MRA_RAM ),
		new Memory_ReadAddress( 0x8000, 0x8000, soundlatch_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress farwest_sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new Memory_WriteAddress( 0x4000, 0x43ff, MWA_RAM ),
		new Memory_WriteAddress( 0x8000, 0x8000, YM2203_control_port_0_w ),
		new Memory_WriteAddress( 0x8001, 0x8001, YM2203_write_port_0_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	
	static InputPortPtr input_ports_ironhors = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( ironhors )
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		/* note that button 3 for player 1 and 2 are exchanged */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "30000 70000" );
		PORT_DIPSETTING(    0x10, "40000 80000" );
		PORT_DIPSETTING(    0x08, "40000" );
		PORT_DIPSETTING(    0x00, "50000" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Normal" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
	/* 	PORT_DIPSETTING(    0x00, "Invalid" );*/
	
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Upright Controls" );
		PORT_DIPSETTING(    0x02, "Single" );
		PORT_DIPSETTING(    0x00, "Dual" );
		PORT_DIPNAME( 0x04, 0x04, "Button Layout" );
		PORT_DIPSETTING(    0x04, "Power Atk Squat" );
		PORT_DIPSETTING(    0x00, "Squat Atk Power" );
		PORT_BIT( 0xf8, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_dairesya = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( dairesya )
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "30000 70000" );
		PORT_DIPSETTING(    0x10, "40000 80000" );
		PORT_DIPSETTING(    0x08, "40000" );
		PORT_DIPSETTING(    0x00, "50000" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Normal" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
	/* 	PORT_DIPSETTING(    0x00, "Invalid" );*/
	
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Upright Controls" );
		PORT_DIPSETTING(    0x02, "Single" );
		PORT_DIPSETTING(    0x00, "Dual" );
		PORT_DIPNAME( 0x04, 0x04, "Button Layout" );
		PORT_DIPSETTING(    0x04, "Power Atk Squat" );
		PORT_DIPSETTING(    0x00, "Squat Atk Power" );
		PORT_BIT( 0xf8, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout ironhors_charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8
	);
	
	static GfxLayout ironhors_spritelayout = new GfxLayout
	(
		16,16,
		512,
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
				32*8+0*4, 32*8+1*4, 32*8+2*4, 32*8+3*4, 32*8+4*4, 32*8+5*4, 32*8+6*4, 32*8+7*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
				16*32, 17*32, 18*32, 19*32, 20*32, 21*32, 22*32, 23*32 },
		32*32
	);
	
	static GfxDecodeInfo ironhors_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, ironhors_charlayout,         0, 16*8 ),
		new GfxDecodeInfo( REGION_GFX1, 0, ironhors_spritelayout, 16*8*16, 16*8 ),
		new GfxDecodeInfo( REGION_GFX1, 0, ironhors_charlayout,   16*8*16, 16*8 ),  /* to handle 8x8 sprites */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	static GfxLayout farwest_charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		2048,	/* 2048 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0, 2, 4, 6 },	/* the four bitplanes are packed in one byte */
		new int[] { 3*8+1, 3*8+0, 0*8+1, 0*8+0, 1*8+1, 1*8+0, 2*8+1, 2*8+0 },
		new int[] { 0*4*8, 1*4*8, 2*4*8, 3*4*8, 4*4*8, 5*4*8, 6*4*8, 7*4*8 },
		32*8	/* every char takes 32 consecutive bytes */
	);
	
	static GfxLayout farwest_spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		512,	/* 512 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 512*32*8, 2*512*32*8, 3*512*32*8 },	/* the four bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
				16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8	/* every sprite takes 32 consecutive bytes */
	);
	
	static GfxLayout farwest_spritelayout2 = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		2048,	/* 2048 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0, 2048*8*8, 2*2048*8*8, 3*2048*8*8 },	/* the four bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxDecodeInfo farwest_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, farwest_charlayout,         0, 16*8 ),
		new GfxDecodeInfo( REGION_GFX2, 0, farwest_spritelayout, 16*8*16, 16*8 ),
		new GfxDecodeInfo( REGION_GFX2, 0, farwest_spritelayout2,16*8*16, 16*8 ),  /* to handle 8x8 sprites */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	static struct YM2203interface ym2203_interface =
	{
		1,			/* 1 chip */
		18432000/6,		/* 3.072 MHz */
		{ YM2203_VOL(40,40) },
		{ 0 },
		{ 0 },
		{ ironhors_filter_w },
		{ 0 }
	};
	
	
	static MACHINE_DRIVER_START( ironhors )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M6809,18432000/6)        /* 3.072 MHz??? mod by Shingo Suzuki 1999/10/15 */
		MDRV_CPU_MEMORY(ironhors_readmem,ironhors_writemem)
		MDRV_CPU_VBLANK_INT(ironhors_interrupt,8)
	
		MDRV_CPU_ADD_TAG("sound",Z80,18432000/6)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)        /* 3.072 MHz */
		MDRV_CPU_MEMORY(ironhors_sound_readmem,ironhors_sound_writemem)
		MDRV_CPU_PORTS(ironhors_sound_readport,ironhors_sound_writeport)
	
		MDRV_FRAMES_PER_SECOND(30)
		MDRV_VBLANK_DURATION(DEFAULT_30HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(1*8, 31*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(ironhors_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(256)
		MDRV_COLORTABLE_LENGTH(16*8*16+16*8*16)
	
		MDRV_PALETTE_INIT(ironhors)
		MDRV_VIDEO_START(ironhors)
		MDRV_VIDEO_UPDATE(ironhors)
	
		/* sound hardware */
		MDRV_SOUND_ADD(YM2203, ym2203_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( farwest )
		MDRV_IMPORT_FROM(ironhors)
	
		MDRV_CPU_MODIFY("sound")
		MDRV_CPU_MEMORY(farwest_sound_readmem, farwest_sound_writemem)
	
		MDRV_GFXDECODE(farwest_gfxdecodeinfo)
	MACHINE_DRIVER_END
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_ironhors = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "13c_h03.bin",  0x4000, 0x8000, CRC(24539af1) SHA1(1eb96a2cb03007665587d6ec114894ab4cafdb23) )
		ROM_LOAD( "12c_h02.bin",  0xc000, 0x4000, CRC(fab07f86) SHA1(9f599d32d473d873113b89f2b24a54a435dbcbe5) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )     /* 64k for audio cpu */
		ROM_LOAD( "10c_h01.bin",  0x0000, 0x4000, CRC(2b17930f) SHA1(be7b21f050f6b74c75a33c9284455bbed5b03c63) )
	
		ROM_REGION( 0x20000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD16_BYTE( "08f_h06.bin",  0x00000, 0x8000, CRC(f21d8c93) SHA1(4245fff5360e10441e11d0d207d510e5c317bb0e) )
		ROM_LOAD16_BYTE( "07f_h05.bin",  0x00001, 0x8000, CRC(60107859) SHA1(ab59b6be155d36811a37dc873abbd97cd0a4120d) )
		ROM_LOAD16_BYTE( "09f_h07.bin",  0x10000, 0x8000, CRC(c761ec73) SHA1(78266c9ff3ea74a59fd3ce84afb4f8a1164c8bba) )
		ROM_LOAD16_BYTE( "06f_h04.bin",  0x10001, 0x8000, CRC(c1486f61) SHA1(4b96aebe5d35fd1d73bde8576689addbb1ff66ed) )
	
		ROM_REGION( 0x0500, REGION_PROMS, 0 )
		ROM_LOAD( "03f_h08.bin",  0x0000, 0x0100, CRC(9f6ddf83) SHA1(08a37182a974c5448156637f10fe60bfe5f225ad) ) /* palette red */
		ROM_LOAD( "04f_h09.bin",  0x0100, 0x0100, CRC(e6773825) SHA1(7523e7fa090d850fe79ff0069d3260c76645d65a) ) /* palette green */
		ROM_LOAD( "05f_h10.bin",  0x0200, 0x0100, CRC(30a57860) SHA1(3ec7535286c8bc65e203320f47e4ed6f1d3d61c9) ) /* palette blue */
		ROM_LOAD( "10f_h12.bin",  0x0300, 0x0100, CRC(5eb33e73) SHA1(f34916dc4617b0c48e0a7ac6ace97b35dfcf1c40) ) /* character lookup table */
		ROM_LOAD( "10f_h11.bin",  0x0400, 0x0100, CRC(a63e37d8) SHA1(1a0a76ecd14310125bdf41a8431d562ed498eb27) ) /* sprite lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_dairesya = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )	/* 64k for code */
		ROM_LOAD( "560-k03.13c",  0x4000, 0x8000, CRC(2ac6103b) SHA1(331e1be3f29df85d65081831c215743354d76778) )
		ROM_LOAD( "560-k02.12c",  0xc000, 0x4000, CRC(07bc13a9) SHA1(1d3a44ad41799f89bfa84cc05fbe0792e57305af) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )     /* 64k for audio cpu */
		ROM_LOAD( "560-j01.10c",  0x0000, 0x4000, CRC(a203b223) SHA1(fd19ae55bda467a09151539be6dce3791c28f18a) )
	
		ROM_REGION( 0x20000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD16_BYTE( "560-j06.8f",   0x00000, 0x8000, CRC(a6e8248d) SHA1(7df653bb3a2257c249c3cf2c3f4f324d687a6b39) )
		ROM_LOAD16_BYTE( "560-j05.7f",   0x00001, 0x8000, CRC(f75893d4) SHA1(dc71b912d9bf5104dc633f687c52043df37852f0) )
		ROM_LOAD16_BYTE( "560-k07.9f",   0x10000, 0x8000, CRC(c8a1b840) SHA1(753b6fcbb4b28bbb63a392cdef90568734eac9bd) )
		ROM_LOAD16_BYTE( "560-k04.6f",   0x10001, 0x8000, CRC(c883d856) SHA1(4c4f91b72dab841ec15ca62121ed0c0878dfff23) )
	
		ROM_REGION( 0x0500, REGION_PROMS, 0 )
		ROM_LOAD( "03f_h08.bin",  0x0000, 0x0100, CRC(9f6ddf83) SHA1(08a37182a974c5448156637f10fe60bfe5f225ad) ) /* palette red */
		ROM_LOAD( "04f_h09.bin",  0x0100, 0x0100, CRC(e6773825) SHA1(7523e7fa090d850fe79ff0069d3260c76645d65a) ) /* palette green */
		ROM_LOAD( "05f_h10.bin",  0x0200, 0x0100, CRC(30a57860) SHA1(3ec7535286c8bc65e203320f47e4ed6f1d3d61c9) ) /* palette blue */
		ROM_LOAD( "10f_h12.bin",  0x0300, 0x0100, CRC(5eb33e73) SHA1(f34916dc4617b0c48e0a7ac6ace97b35dfcf1c40) ) /* character lookup table */
		ROM_LOAD( "10f_h11.bin",  0x0400, 0x0100, CRC(a63e37d8) SHA1(1a0a76ecd14310125bdf41a8431d562ed498eb27) ) /* sprite lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_farwest = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x12000, REGION_CPU1, 0 )	/* 64k for code + 8k for extra ROM */
		ROM_LOAD( "ironhors.008", 0x04000, 0x4000, CRC(b1c8246c) SHA1(4ceb098bb0b4efcbe50bb4b23bd27a60dabf2b3e) )
		ROM_LOAD( "ironhors.009", 0x08000, 0x8000, CRC(ea34ecfc) SHA1(8c7f12e76d2b9eb592ebf1bfd3e16a6b130da8e5) )
		ROM_LOAD( "ironhors.007", 0x10000, 0x2000, CRC(471182b7) SHA1(48ff58cbbf971b257e8099ec331397cf73dc8325) )	/* don't know what this is for */
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 )     /* 64k for audio cpu */
		ROM_LOAD( "ironhors.010", 0x0000, 0x4000, CRC(a28231a6) SHA1(617e8fdf8129081c6a1bbbf140837a375a51da72) )
	
		ROM_REGION( 0x10000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "ironhors.005", 0x00000, 0x8000, CRC(f77e5b83) SHA1(6c72732dc96c1652713b2aba6f0a2410f9457818) )
		ROM_LOAD( "ironhors.006", 0x08000, 0x8000, CRC(7bbc0b51) SHA1(9b4890f2d20a8ddf5ba3f4325df070509252e06e) )
	
		ROM_REGION( 0x10000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "ironhors.001", 0x00000, 0x4000, CRC(a8fc21d3) SHA1(1e898aaccad1919bbacf8d7957f5a0761df20767) )
		ROM_LOAD( "ironhors.002", 0x04000, 0x4000, CRC(9c1e5593) SHA1(7d41d2224f0653e09d8728ccdec2df60f549e36e) )
		ROM_LOAD( "ironhors.003", 0x08000, 0x4000, CRC(3a0bf799) SHA1(b34d5c7edda06b8a579d6d390511781a43ffce83) )
		ROM_LOAD( "ironhors.004", 0x0c000, 0x4000, CRC(1fab18a3) SHA1(cc7ddf60b719e7c5a689f716ebee9bc04ade406a) )
	
		ROM_REGION( 0x0500, REGION_PROMS, 0 )
		ROM_LOAD( "ironcol.003",  0x0000, 0x0100, CRC(3e3fca11) SHA1(c92737659f063889a2b210cfe5c294b8a4864489) ) /* palette red */
		ROM_LOAD( "ironcol.001",  0x0100, 0x0100, CRC(dfb13014) SHA1(d9f9a5bed1300faf7c3864d5c5ae07087de25824) ) /* palette green */
		ROM_LOAD( "ironcol.002",  0x0200, 0x0100, CRC(77c88430) SHA1(e3041945b14955de109a505d9aa9f79046bed6a8) ) /* palette blue */
		ROM_LOAD( "10f_h12.bin",  0x0300, 0x0100, CRC(5eb33e73) SHA1(f34916dc4617b0c48e0a7ac6ace97b35dfcf1c40) ) /* character lookup table */
		ROM_LOAD( "ironcol.005",  0x0400, 0x0100, CRC(15077b9c) SHA1(c7fe24e3d481150452ff774f3908510db9e28367) ) /* sprite lookup table */
	ROM_END(); }}; 
	
	
	
	GAME( 1986, ironhors, 0,        ironhors, ironhors, 0, ROT0, "Konami", "Iron Horse" )
	GAME( 1986, dairesya, ironhors, ironhors, dairesya, 0, ROT0, "[Konami] (Kawakusu license)", "Dai Ressya Goutou (Japan)" )
	GAMEX(1986, farwest,  ironhors, farwest,  ironhors, 0, ROT0, "bootleg?", "Far West", GAME_NOT_WORKING )
}
