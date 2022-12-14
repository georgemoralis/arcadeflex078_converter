/***************************************************************************

Bottom of the Ninth (c) 1989 Konami

Similar to S.P.Y.

driver by Nicola Salmoria

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class bottom9
{
	
	
	
	
	
	
	public static InterruptHandlerPtr bottom9_interrupt = new InterruptHandlerPtr() {public void handler(){
		if (K052109_is_IRQ_enabled())
			cpu_set_irq_line(0, 0, HOLD_LINE);
	} };
	
	
	static int zoomreadroms,K052109_selected;
	
	public static ReadHandlerPtr bottom9_bankedram1_r  = new ReadHandlerPtr() { public int handler(int offset){
		if (K052109_selected)
			return K052109_051960_r(offset);
		else
		{
			if (zoomreadroms)
				return K051316_rom_0_r(offset);
			else
				return K051316_0_r(offset);
		}
	} };
	
	public static WriteHandlerPtr bottom9_bankedram1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (K052109_selected) K052109_051960_w(offset,data);
		else K051316_0_w(offset,data);
	} };
	
	public static ReadHandlerPtr bottom9_bankedram2_r  = new ReadHandlerPtr() { public int handler(int offset){
		if (K052109_selected) return K052109_051960_r(offset + 0x2000);
		else return paletteram_r(offset);
	} };
	
	public static WriteHandlerPtr bottom9_bankedram2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (K052109_selected) K052109_051960_w(offset + 0x2000,data);
		else paletteram_xBBBBBGGGGGRRRRR_swap_w(offset,data);
	} };
	
	public static WriteHandlerPtr bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		unsigned char *RAM = memory_region(REGION_CPU1);
		int offs;
	
		/* bit 0 = RAM bank */
	if ((data & 1) == 0) usrintf_showmessage("bankswitch RAM bank 0");
	
		/* bit 1-4 = ROM bank */
		if (data & 0x10) offs = 0x20000 + (data & 0x06) * 0x1000;
		else offs = 0x10000 + (data & 0x0e) * 0x1000;
		cpu_setbank(1,&RAM[offs]);
	} };
	
	public static WriteHandlerPtr bottom9_1f90_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* bits 0/1 = coin counters */
		coin_counter_w(0,data & 0x01);
		coin_counter_w(1,data & 0x02);
	
		/* bit 2 = enable char ROM reading through the video RAM */
		K052109_set_RMRD_line((data & 0x04) ? ASSERT_LINE : CLEAR_LINE);
	
		/* bit 3 = disable video */
		bottom9_video_enable = ~data & 0x08;
	
		/* bit 4 = enable 051316 ROM reading */
		zoomreadroms = data & 0x10;
	
		/* bit 5 = RAM bank */
		K052109_selected = data & 0x20;
	} };
	
	public static WriteHandlerPtr bottom9_sh_irqtrigger_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		cpu_set_irq_line_and_vector(1,0,HOLD_LINE,0xff);
	} };
	
	static int nmienable;
	
	public static InterruptHandlerPtr bottom9_sound_interrupt = new InterruptHandlerPtr() {public void handler(){
		if (nmienable)
			cpu_set_irq_line(1, IRQ_LINE_NMI, PULSE_LINE);
	} };
	
	public static WriteHandlerPtr nmi_enable_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		nmienable = data;
	} };
	
	public static WriteHandlerPtr sound_bank_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		int bank_A,bank_B;
	
		bank_A = ((data >> 0) & 0x03);
		bank_B = ((data >> 2) & 0x03);
		K007232_set_bank( 0, bank_A, bank_B );
		bank_A = ((data >> 4) & 0x03);
		bank_B = ((data >> 6) & 0x03);
		K007232_set_bank( 1, bank_A, bank_B );
	} };
	
	
	
	public static Memory_ReadAddress bottom9_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x07ff, bottom9_bankedram1_r ),
		new Memory_ReadAddress( 0x1fd0, 0x1fd0, input_port_4_r ),
		new Memory_ReadAddress( 0x1fd1, 0x1fd1, input_port_0_r ),
		new Memory_ReadAddress( 0x1fd2, 0x1fd2, input_port_1_r ),
		new Memory_ReadAddress( 0x1fd3, 0x1fd3, input_port_2_r ),
		new Memory_ReadAddress( 0x1fe0, 0x1fe0, input_port_3_r ),
		new Memory_ReadAddress( 0x2000, 0x27ff, bottom9_bankedram2_r ),
		new Memory_ReadAddress( 0x0000, 0x3fff, K052109_051960_r ),
		new Memory_ReadAddress( 0x4000, 0x5fff, MRA_RAM ),
		new Memory_ReadAddress( 0x6000, 0x7fff, MRA_BANK1 ),
		new Memory_ReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress bottom9_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x07ff, bottom9_bankedram1_w ),
		new Memory_WriteAddress( 0x1f80, 0x1f80, bankswitch_w ),
		new Memory_WriteAddress( 0x1f90, 0x1f90, bottom9_1f90_w ),
		new Memory_WriteAddress( 0x1fa0, 0x1fa0, watchdog_reset_w ),
		new Memory_WriteAddress( 0x1fb0, 0x1fb0, soundlatch_w ),
		new Memory_WriteAddress( 0x1fc0, 0x1fc0, bottom9_sh_irqtrigger_w ),
		new Memory_WriteAddress( 0x1ff0, 0x1fff, K051316_ctrl_0_w ),
		new Memory_WriteAddress( 0x2000, 0x27ff, bottom9_bankedram2_w, paletteram ),
		new Memory_WriteAddress( 0x0000, 0x3fff, K052109_051960_w ),
		new Memory_WriteAddress( 0x4000, 0x5fff, MWA_RAM ),
		new Memory_WriteAddress( 0x6000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress bottom9_sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new Memory_ReadAddress( 0xa000, 0xa00d, K007232_read_port_0_r ),
		new Memory_ReadAddress( 0xb000, 0xb00d, K007232_read_port_1_r ),
		new Memory_ReadAddress( 0xd000, 0xd000, soundlatch_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress bottom9_sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new Memory_WriteAddress( 0x9000, 0x9000, sound_bank_w ),
		new Memory_WriteAddress( 0xa000, 0xa00d, K007232_write_port_0_w ),
		new Memory_WriteAddress( 0xb000, 0xb00d, K007232_write_port_1_w ),
		new Memory_WriteAddress( 0xf000, 0xf000, nmi_enable_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	
	static InputPortPtr input_ports_bottom9 = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( bottom9 )
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_5C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_5C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
	
		PORT_START(); 
		PORT_DIPNAME( 0x07, 0x04, "Play Time" );
		PORT_DIPSETTING(    0x07, "1'00" );
		PORT_DIPSETTING(    0x06, "1'10" );
		PORT_DIPSETTING(    0x05, "1'20" );
		PORT_DIPSETTING(    0x04, "1'30" );
		PORT_DIPSETTING(    0x03, "1'40" );
		PORT_DIPSETTING(    0x02, "1'50" );
		PORT_DIPSETTING(    0x01, "2'00" );
		PORT_DIPSETTING(    0x00, "2'10" );
		PORT_DIPNAME( 0x18, 0x08, "Bonus Time" );
		PORT_DIPSETTING(    0x18, "00" );
		PORT_DIPSETTING(    0x10, "20" );
		PORT_DIPSETTING(    0x08, "30" );
		PORT_DIPSETTING(    0x00, "40" );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x60, "Easy" );
		PORT_DIPSETTING(	0x40, "Normal" );
		PORT_DIPSETTING(	0x20, "Difficult" );
		PORT_DIPSETTING(	0x00, "Very Difficult" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x40, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x80, 0x80, "Fielder Control" );
		PORT_DIPSETTING(    0x80, "Normal" );
		PORT_DIPSETTING(    0x00, "Auto" );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_mstadium = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( mstadium )
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_5C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_5C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
	
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x01, "Play Inning" );
		PORT_DIPSETTING(    0x03, "1" );
		PORT_DIPSETTING(    0x02, "2" );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_DIPNAME( 0x0c, 0x08, "Play Inning Time" );
		PORT_DIPSETTING(    0x0c, "6 Min" );
		PORT_DIPSETTING(    0x08, "8 Min" );
		PORT_DIPSETTING(    0x04, "10 Min" );
		PORT_DIPSETTING(    0x00, "12 Min" );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x60, "Easy" );
		PORT_DIPSETTING(	0x40, "Normal" );
		PORT_DIPSETTING(	0x20, "Difficult" );
		PORT_DIPSETTING(	0x00, "Very Difficult" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x40, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x80, 0x80, "Fielder Control" );
		PORT_DIPSETTING(    0x80, "Normal" );
		PORT_DIPSETTING(    0x00, "Auto" );
	INPUT_PORTS_END(); }}; 
	
	
	
	static void volume_callback0(int v)
	{
		K007232_set_volume(0,0,(v >> 4) * 0x11,0);
		K007232_set_volume(0,1,0,(v & 0x0f) * 0x11);
	}
	
	static void volume_callback1(int v)
	{
		K007232_set_volume(1,0,(v >> 4) * 0x11,0);
		K007232_set_volume(1,1,0,(v & 0x0f) * 0x11);
	}
	
	static struct K007232_interface k007232_interface =
	{
		2,			/* number of chips */
		3579545,	/* clock */
		{ REGION_SOUND1, REGION_SOUND2 },	/* memory regions */
		{ K007232_VOL(40,MIXER_PAN_CENTER,40,MIXER_PAN_CENTER),
				K007232_VOL(40,MIXER_PAN_CENTER,40,MIXER_PAN_CENTER) },	/* volume */
		{ volume_callback0, volume_callback1 }	/* external port callback */
	};
	
	
	
	static MACHINE_DRIVER_START( bottom9 )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M6809, 2000000) /* ? */
		MDRV_CPU_MEMORY(bottom9_readmem,bottom9_writemem)
		MDRV_CPU_VBLANK_INT(bottom9_interrupt,1)
	
		MDRV_CPU_ADD(Z80, 3579545)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)
		MDRV_CPU_MEMORY(bottom9_sound_readmem,bottom9_sound_writemem)
		MDRV_CPU_VBLANK_INT(bottom9_sound_interrupt,8)	/* irq is triggered by the main CPU */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_HAS_SHADOWS)
		MDRV_SCREEN_SIZE(64*8, 32*8)
		MDRV_VISIBLE_AREA(14*8, (64-14)*8-1, 2*8, 30*8-1 )
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(bottom9)
		MDRV_VIDEO_UPDATE(bottom9)
	
		/* sound hardware */
		MDRV_SOUND_ADD(K007232, k007232_interface)
	MACHINE_DRIVER_END
	
	
	/***************************************************************************
	
	  Game ROMs
	
	***************************************************************************/
	
	static RomLoadPtr rom_bottom9 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1, 0 ) /* code + banked roms */
		ROM_LOAD( "891n03.k17",   0x10000, 0x10000, CRC(8b083ff3) SHA1(045fef944b192e4bb147fa0f28680c0602af7377) )
	    ROM_LOAD( "891-t02.k15",  0x20000, 0x08000, CRC(2c10ced2) SHA1(ecd43825a67b495cade94a454c96a19143d87760) )
	    ROM_CONTINUE(             0x08000, 0x08000 )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 ) /* Z80 code */
		ROM_LOAD( "891j01.g8",    0x0000, 0x8000, CRC(31b0a0a8) SHA1(8e047f81c19f25de97fa22e70dcfe9e06bfae699) )
	
		ROM_REGION( 0x080000, REGION_GFX1, 0 ) /* graphics ( dont dispose as the program can read them, 0 ) */
		ROM_LOAD16_BYTE( "891e10c", 0x00000, 0x10000, CRC(209b0431) SHA1(07f05f63267d5ed5c99b5f786bb66a87045db9e1) )	/* characters */
		ROM_LOAD16_BYTE( "891e10a", 0x00001, 0x10000, CRC(8020a9e8) SHA1(3792794a1b875506089da63cae955668cc61f54b) )
		ROM_LOAD16_BYTE( "891e10d", 0x20000, 0x10000, CRC(16d5fd7a) SHA1(895a53e41173a70c48337d812466857676908a23) )
		ROM_LOAD16_BYTE( "891e10b", 0x20001, 0x10000, CRC(30121cc0) SHA1(79174d00b79855c00c9c872b8f32946be1bf1d8a) )
		ROM_LOAD16_BYTE( "891e09c", 0x40000, 0x10000, CRC(9dcaefbf) SHA1(8b61b1627737b959158aa6c7ea5db63f6aec7436) )
		ROM_LOAD16_BYTE( "891e09a", 0x40001, 0x10000, CRC(56b0ead9) SHA1(ef4b00ed0de93f61f4c8661ec0e6049c51a25cf6) )
		ROM_LOAD16_BYTE( "891e09d", 0x60000, 0x10000, CRC(4e1335e6) SHA1(b892ab40a41978a89658ea2e7aabe9b073430b5d) )
		ROM_LOAD16_BYTE( "891e09b", 0x60001, 0x10000, CRC(b6f914fb) SHA1(e95f3e899c2ead15ef8a529dbc67e8f4a0f88bdd) )
	
		ROM_REGION( 0x100000, REGION_GFX2, 0 ) /* graphics ( dont dispose as the program can read them, 0 ) */
		ROM_LOAD16_BYTE( "891e06e", 0x00000, 0x10000, CRC(0b04db1c) SHA1(0beae7bb8da49379915c0253ce03091eb71a58b5) )	/* sprites */
		ROM_LOAD16_BYTE( "891e06a", 0x00001, 0x10000, CRC(5ee37327) SHA1(f63ddaf63af06ea5421b0361315940582ef57922) )
		ROM_LOAD16_BYTE( "891e06f", 0x20000, 0x10000, CRC(f9ada524) SHA1(2df1fe91f43b95bb4e4a24a0931ab6f540496f65) )
		ROM_LOAD16_BYTE( "891e06b", 0x20001, 0x10000, CRC(2295dfaa) SHA1(96070e1bd07b33b6701e45ee1e200f24532e8630) )
		ROM_LOAD16_BYTE( "891e06g", 0x40000, 0x10000, CRC(04abf78f) SHA1(9a21cc71993c3074a8a61c654b998466503b31ef) )
		ROM_LOAD16_BYTE( "891e06c", 0x40001, 0x10000, CRC(dbdb0d55) SHA1(8269b9be8f36116eb6d10efbb6b7050846a9290c) )
		ROM_LOAD16_BYTE( "891e06h", 0x60000, 0x10000, CRC(5d5ded8c) SHA1(2581aa387c1ba1f2b7c59bae2c59fbf127aa4e86) )
		ROM_LOAD16_BYTE( "891e06d", 0x60001, 0x10000, CRC(f9ecbd71) SHA1(45e28a8b40159fd0cdcc8ad253ffc7eba6cf3535) )
		ROM_LOAD16_BYTE( "891e05e", 0x80000, 0x10000, CRC(b356e729) SHA1(2cda591415b0f139fdb1f80c349d432bb0579d8e) )
		ROM_LOAD16_BYTE( "891e05a", 0x80001, 0x10000, CRC(bfd5487e) SHA1(24e0de9f12f6df6bde6268d090fe9e1ea827c0dc) )
		ROM_LOAD16_BYTE( "891e05f", 0xa0000, 0x10000, CRC(ecdd11c5) SHA1(8eac76b3b0f2ab4d59491e10070a62fd9f1eba81) )
		ROM_LOAD16_BYTE( "891e05b", 0xa0001, 0x10000, CRC(aba18d24) SHA1(ba8e1fab9537199ece2af26bb3f5c8d85d5213d4) )
		ROM_LOAD16_BYTE( "891e05g", 0xc0000, 0x10000, CRC(c315f9ae) SHA1(8e2c8ca1c6dcfe5b7302ea89275b231ffb2e0e84) )
		ROM_LOAD16_BYTE( "891e05c", 0xc0001, 0x10000, CRC(21fcbc6f) SHA1(efc65973ea7702a1b5c26a966f452804ad97dbd4) )
		ROM_LOAD16_BYTE( "891e05h", 0xe0000, 0x10000, CRC(b0aba53b) SHA1(e76b345ae354533959ed06217b91ce3c93b22a23) )
		ROM_LOAD16_BYTE( "891e05d", 0xe0001, 0x10000, CRC(f6d3f886) SHA1(b8bdcc9470aa93849b8c8a1f03971281cacc6d44) )
	
		ROM_REGION( 0x020000, REGION_GFX3, 0 ) /* graphics ( dont dispose as the program can read them, 0 ) */
		ROM_LOAD( "891e07a",      0x00000, 0x10000, CRC(b8d8b939) SHA1(ee91fb46d70db2d17f5909c4ea7ee1cf2d317d10) )	/* zoom/rotate */
		ROM_LOAD( "891e07b",      0x10000, 0x10000, CRC(83b2f92d) SHA1(c4972018e1f8109656784fae3e023a5522622c4b) )
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 )
		ROM_LOAD( "891b11.f23",   0x0000, 0x0100, CRC(ecb854aa) SHA1(3bd321ca3076d4e0042e0af656d51909fa6a5b3b) )	/* priority encoder (not used) */
	
		ROM_REGION( 0x40000, REGION_SOUND1, 0 ) /* samples for 007232 #0 */
		ROM_LOAD( "891e08a",      0x00000, 0x10000, CRC(cef667bf) SHA1(e773fc0ced45e01e13cdee18c404d609356d2d0e) )
		ROM_LOAD( "891e08b",      0x10000, 0x10000, CRC(f7c14a7a) SHA1(05261a065de33e158e8d72d74eb657035abb5d03) )
		ROM_LOAD( "891e08c",      0x20000, 0x10000, CRC(756b7f3c) SHA1(6f36f0b4e08db27a8b6e180d12be6427677ad62d) )
		ROM_LOAD( "891e08d",      0x30000, 0x10000, CRC(cd0d7305) SHA1(82403ce1f38014ebf94008a66c98697a572303f9) )
	
		ROM_REGION( 0x40000, REGION_SOUND2, 0 ) /* samples for 007232 #1 */
		ROM_LOAD( "891e04a",      0x00000, 0x10000, CRC(daebbc74) SHA1(f61daebf80e5e4640c4cea4ea5767e64a49d928d) )
		ROM_LOAD( "891e04b",      0x10000, 0x10000, CRC(5ffb9ad1) SHA1(e8f00c63dc3091aa344e82dc29f41aedd5a764b4) )
		ROM_LOAD( "891e04c",      0x20000, 0x10000, CRC(2dbbf16b) SHA1(84b2005a1fe61a6a0cf1aa6e0fdf7ff8b1f8f82a) )
		ROM_LOAD( "891e04d",      0x30000, 0x10000, CRC(8b0cd2cc) SHA1(e14109c69fa24d309aed4ff3589cc6619e29f97f) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_bottom9n = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1, 0 ) /* code + banked roms */
		ROM_LOAD( "891n03.k17",   0x10000, 0x10000, CRC(8b083ff3) SHA1(045fef944b192e4bb147fa0f28680c0602af7377) )
	    ROM_LOAD( "891n02.k15",   0x20000, 0x08000, CRC(d44d9ed4) SHA1(2a12bcfba81ab7e074569e2ad2da6a237a1c0ce5) )
	    ROM_CONTINUE(             0x08000, 0x08000 )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 ) /* Z80 code */
		ROM_LOAD( "891j01.g8",    0x0000, 0x8000, CRC(31b0a0a8) SHA1(8e047f81c19f25de97fa22e70dcfe9e06bfae699) )
	
		ROM_REGION( 0x080000, REGION_GFX1, 0 ) /* graphics ( dont dispose as the program can read them, 0 ) */
		ROM_LOAD16_BYTE( "891e10c", 0x00000, 0x10000, CRC(209b0431) SHA1(07f05f63267d5ed5c99b5f786bb66a87045db9e1) )	/* characters */
		ROM_LOAD16_BYTE( "891e10a", 0x00001, 0x10000, CRC(8020a9e8) SHA1(3792794a1b875506089da63cae955668cc61f54b) )
		ROM_LOAD16_BYTE( "891e10d", 0x20000, 0x10000, CRC(16d5fd7a) SHA1(895a53e41173a70c48337d812466857676908a23) )
		ROM_LOAD16_BYTE( "891e10b", 0x20001, 0x10000, CRC(30121cc0) SHA1(79174d00b79855c00c9c872b8f32946be1bf1d8a) )
		ROM_LOAD16_BYTE( "891e09c", 0x40000, 0x10000, CRC(9dcaefbf) SHA1(8b61b1627737b959158aa6c7ea5db63f6aec7436) )
		ROM_LOAD16_BYTE( "891e09a", 0x40001, 0x10000, CRC(56b0ead9) SHA1(ef4b00ed0de93f61f4c8661ec0e6049c51a25cf6) )
		ROM_LOAD16_BYTE( "891e09d", 0x60000, 0x10000, CRC(4e1335e6) SHA1(b892ab40a41978a89658ea2e7aabe9b073430b5d) )
		ROM_LOAD16_BYTE( "891e09b", 0x60001, 0x10000, CRC(b6f914fb) SHA1(e95f3e899c2ead15ef8a529dbc67e8f4a0f88bdd) )
	
		ROM_REGION( 0x100000, REGION_GFX2, 0 ) /* graphics ( dont dispose as the program can read them, 0 ) */
		ROM_LOAD16_BYTE( "891e06e", 0x00000, 0x10000, CRC(0b04db1c) SHA1(0beae7bb8da49379915c0253ce03091eb71a58b5) )	/* sprites */
		ROM_LOAD16_BYTE( "891e06a", 0x00001, 0x10000, CRC(5ee37327) SHA1(f63ddaf63af06ea5421b0361315940582ef57922) )
		ROM_LOAD16_BYTE( "891e06f", 0x20000, 0x10000, CRC(f9ada524) SHA1(2df1fe91f43b95bb4e4a24a0931ab6f540496f65) )
		ROM_LOAD16_BYTE( "891e06b", 0x20001, 0x10000, CRC(2295dfaa) SHA1(96070e1bd07b33b6701e45ee1e200f24532e8630) )
		ROM_LOAD16_BYTE( "891e06g", 0x40000, 0x10000, CRC(04abf78f) SHA1(9a21cc71993c3074a8a61c654b998466503b31ef) )
		ROM_LOAD16_BYTE( "891e06c", 0x40001, 0x10000, CRC(dbdb0d55) SHA1(8269b9be8f36116eb6d10efbb6b7050846a9290c) )
		ROM_LOAD16_BYTE( "891e06h", 0x60000, 0x10000, CRC(5d5ded8c) SHA1(2581aa387c1ba1f2b7c59bae2c59fbf127aa4e86) )
		ROM_LOAD16_BYTE( "891e06d", 0x60001, 0x10000, CRC(f9ecbd71) SHA1(45e28a8b40159fd0cdcc8ad253ffc7eba6cf3535) )
		ROM_LOAD16_BYTE( "891e05e", 0x80000, 0x10000, CRC(b356e729) SHA1(2cda591415b0f139fdb1f80c349d432bb0579d8e) )
		ROM_LOAD16_BYTE( "891e05a", 0x80001, 0x10000, CRC(bfd5487e) SHA1(24e0de9f12f6df6bde6268d090fe9e1ea827c0dc) )
		ROM_LOAD16_BYTE( "891e05f", 0xa0000, 0x10000, CRC(ecdd11c5) SHA1(8eac76b3b0f2ab4d59491e10070a62fd9f1eba81) )
		ROM_LOAD16_BYTE( "891e05b", 0xa0001, 0x10000, CRC(aba18d24) SHA1(ba8e1fab9537199ece2af26bb3f5c8d85d5213d4) )
		ROM_LOAD16_BYTE( "891e05g", 0xc0000, 0x10000, CRC(c315f9ae) SHA1(8e2c8ca1c6dcfe5b7302ea89275b231ffb2e0e84) )
		ROM_LOAD16_BYTE( "891e05c", 0xc0001, 0x10000, CRC(21fcbc6f) SHA1(efc65973ea7702a1b5c26a966f452804ad97dbd4) )
		ROM_LOAD16_BYTE( "891e05h", 0xe0000, 0x10000, CRC(b0aba53b) SHA1(e76b345ae354533959ed06217b91ce3c93b22a23) )
		ROM_LOAD16_BYTE( "891e05d", 0xe0001, 0x10000, CRC(f6d3f886) SHA1(b8bdcc9470aa93849b8c8a1f03971281cacc6d44) )
	
		ROM_REGION( 0x020000, REGION_GFX3, 0 ) /* graphics ( dont dispose as the program can read them, 0 ) */
		ROM_LOAD( "891e07a",      0x00000, 0x10000, CRC(b8d8b939) SHA1(ee91fb46d70db2d17f5909c4ea7ee1cf2d317d10) )	/* zoom/rotate */
		ROM_LOAD( "891e07b",      0x10000, 0x10000, CRC(83b2f92d) SHA1(c4972018e1f8109656784fae3e023a5522622c4b) )
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 )
		ROM_LOAD( "891b11.f23",   0x0000, 0x0100, CRC(ecb854aa) SHA1(3bd321ca3076d4e0042e0af656d51909fa6a5b3b) )	/* priority encoder (not used) */
	
		ROM_REGION( 0x40000, REGION_SOUND1, 0 ) /* samples for 007232 #0 */
		ROM_LOAD( "891e08a",      0x00000, 0x10000, CRC(cef667bf) SHA1(e773fc0ced45e01e13cdee18c404d609356d2d0e) )
		ROM_LOAD( "891e08b",      0x10000, 0x10000, CRC(f7c14a7a) SHA1(05261a065de33e158e8d72d74eb657035abb5d03) )
		ROM_LOAD( "891e08c",      0x20000, 0x10000, CRC(756b7f3c) SHA1(6f36f0b4e08db27a8b6e180d12be6427677ad62d) )
		ROM_LOAD( "891e08d",      0x30000, 0x10000, CRC(cd0d7305) SHA1(82403ce1f38014ebf94008a66c98697a572303f9) )
	
		ROM_REGION( 0x40000, REGION_SOUND2, 0 ) /* samples for 007232 #1 */
		ROM_LOAD( "891e04a",      0x00000, 0x10000, CRC(daebbc74) SHA1(f61daebf80e5e4640c4cea4ea5767e64a49d928d) )
		ROM_LOAD( "891e04b",      0x10000, 0x10000, CRC(5ffb9ad1) SHA1(e8f00c63dc3091aa344e82dc29f41aedd5a764b4) )
		ROM_LOAD( "891e04c",      0x20000, 0x10000, CRC(2dbbf16b) SHA1(84b2005a1fe61a6a0cf1aa6e0fdf7ff8b1f8f82a) )
		ROM_LOAD( "891e04d",      0x30000, 0x10000, CRC(8b0cd2cc) SHA1(e14109c69fa24d309aed4ff3589cc6619e29f97f) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mstadium = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1, 0 ) /* code + banked roms */
		ROM_LOAD( "891-403.k17",   0x10000, 0x10000, CRC(1c00c4e8) SHA1(8a3400a8df44f21616422e5af3bca84d0f390f63) )
	    ROM_LOAD( "891-402.k15",   0x20000, 0x08000, CRC(b850bbce) SHA1(a64300d1b1068e59eb59c427946c9bff164e2da8) )
	    ROM_CONTINUE(             0x08000, 0x08000 )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 ) /* Z80 code */
		ROM_LOAD( "891w01.g8",    0x0000, 0x8000, CRC(edec565a) SHA1(69cba0d00c6ef76c4ce2b553e3fd15de8abbbf31) )
	
		ROM_REGION( 0x080000, REGION_GFX1, 0 ) /* graphics ( dont dispose as the program can read them, 0 ) */
		ROM_LOAD16_BYTE( "891e10c", 0x00000, 0x10000, CRC(209b0431) SHA1(07f05f63267d5ed5c99b5f786bb66a87045db9e1) )	/* characters */
		ROM_LOAD16_BYTE( "891e10a", 0x00001, 0x10000, CRC(8020a9e8) SHA1(3792794a1b875506089da63cae955668cc61f54b) )
		ROM_LOAD16_BYTE( "891e10d", 0x20000, 0x10000, CRC(16d5fd7a) SHA1(895a53e41173a70c48337d812466857676908a23) )
		ROM_LOAD16_BYTE( "891e10b", 0x20001, 0x10000, CRC(30121cc0) SHA1(79174d00b79855c00c9c872b8f32946be1bf1d8a) )
		ROM_LOAD16_BYTE( "891e09c", 0x40000, 0x10000, CRC(9dcaefbf) SHA1(8b61b1627737b959158aa6c7ea5db63f6aec7436) )
		ROM_LOAD16_BYTE( "891e09a", 0x40001, 0x10000, CRC(56b0ead9) SHA1(ef4b00ed0de93f61f4c8661ec0e6049c51a25cf6) )
		ROM_LOAD16_BYTE( "891e09d", 0x60000, 0x10000, CRC(4e1335e6) SHA1(b892ab40a41978a89658ea2e7aabe9b073430b5d) )
		ROM_LOAD16_BYTE( "891e09b", 0x60001, 0x10000, CRC(b6f914fb) SHA1(e95f3e899c2ead15ef8a529dbc67e8f4a0f88bdd) )
	
		ROM_REGION( 0x100000, REGION_GFX2, 0 ) /* graphics ( dont dispose as the program can read them, 0 ) */
		ROM_LOAD16_BYTE( "891e06e", 0x00000, 0x10000, CRC(0b04db1c) SHA1(0beae7bb8da49379915c0253ce03091eb71a58b5) )	/* sprites */
		ROM_LOAD16_BYTE( "891e06a", 0x00001, 0x10000, CRC(5ee37327) SHA1(f63ddaf63af06ea5421b0361315940582ef57922) )
		ROM_LOAD16_BYTE( "891e06f", 0x20000, 0x10000, CRC(f9ada524) SHA1(2df1fe91f43b95bb4e4a24a0931ab6f540496f65) )
		ROM_LOAD16_BYTE( "891e06b", 0x20001, 0x10000, CRC(2295dfaa) SHA1(96070e1bd07b33b6701e45ee1e200f24532e8630) )
		ROM_LOAD16_BYTE( "891e06g", 0x40000, 0x10000, CRC(04abf78f) SHA1(9a21cc71993c3074a8a61c654b998466503b31ef) )
		ROM_LOAD16_BYTE( "891e06c", 0x40001, 0x10000, CRC(dbdb0d55) SHA1(8269b9be8f36116eb6d10efbb6b7050846a9290c) )
		ROM_LOAD16_BYTE( "891e06h", 0x60000, 0x10000, CRC(5d5ded8c) SHA1(2581aa387c1ba1f2b7c59bae2c59fbf127aa4e86) )
		ROM_LOAD16_BYTE( "891e06d", 0x60001, 0x10000, CRC(f9ecbd71) SHA1(45e28a8b40159fd0cdcc8ad253ffc7eba6cf3535) )
		ROM_LOAD16_BYTE( "891e05e", 0x80000, 0x10000, CRC(b356e729) SHA1(2cda591415b0f139fdb1f80c349d432bb0579d8e) )
		ROM_LOAD16_BYTE( "891e05a", 0x80001, 0x10000, CRC(bfd5487e) SHA1(24e0de9f12f6df6bde6268d090fe9e1ea827c0dc) )
		ROM_LOAD16_BYTE( "891e05f", 0xa0000, 0x10000, CRC(ecdd11c5) SHA1(8eac76b3b0f2ab4d59491e10070a62fd9f1eba81) )
		ROM_LOAD16_BYTE( "891e05b", 0xa0001, 0x10000, CRC(aba18d24) SHA1(ba8e1fab9537199ece2af26bb3f5c8d85d5213d4) )
		ROM_LOAD16_BYTE( "891e05g", 0xc0000, 0x10000, CRC(c315f9ae) SHA1(8e2c8ca1c6dcfe5b7302ea89275b231ffb2e0e84) )
		ROM_LOAD16_BYTE( "891e05c", 0xc0001, 0x10000, CRC(21fcbc6f) SHA1(efc65973ea7702a1b5c26a966f452804ad97dbd4) )
		ROM_LOAD16_BYTE( "891e05h", 0xe0000, 0x10000, CRC(b0aba53b) SHA1(e76b345ae354533959ed06217b91ce3c93b22a23) )
		ROM_LOAD16_BYTE( "891e05d", 0xe0001, 0x10000, CRC(f6d3f886) SHA1(b8bdcc9470aa93849b8c8a1f03971281cacc6d44) )
	
		ROM_REGION( 0x020000, REGION_GFX3, 0 ) /* graphics ( dont dispose as the program can read them, 0 ) */
		ROM_LOAD( "891e07a",      0x00000, 0x10000, CRC(b8d8b939) SHA1(ee91fb46d70db2d17f5909c4ea7ee1cf2d317d10) )	/* zoom/rotate */
		ROM_LOAD( "891e07b",      0x10000, 0x10000, CRC(83b2f92d) SHA1(c4972018e1f8109656784fae3e023a5522622c4b) )
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 )
		ROM_LOAD( "891b11.f23",   0x0000, 0x0100, CRC(ecb854aa) SHA1(3bd321ca3076d4e0042e0af656d51909fa6a5b3b) )	/* priority encoder (not used) */
	
		ROM_REGION( 0x40000, REGION_SOUND1, 0 ) /* samples for 007232 #0 */
		ROM_LOAD( "891e08a",      0x00000, 0x10000, CRC(cef667bf) SHA1(e773fc0ced45e01e13cdee18c404d609356d2d0e) )
		ROM_LOAD( "891e08b",      0x10000, 0x10000, CRC(f7c14a7a) SHA1(05261a065de33e158e8d72d74eb657035abb5d03) )
		ROM_LOAD( "891e08c",      0x20000, 0x10000, CRC(756b7f3c) SHA1(6f36f0b4e08db27a8b6e180d12be6427677ad62d) )
		ROM_LOAD( "891e08d",      0x30000, 0x10000, CRC(cd0d7305) SHA1(82403ce1f38014ebf94008a66c98697a572303f9) )
	
		ROM_REGION( 0x40000, REGION_SOUND2, 0 ) /* samples for 007232 #1 */
		ROM_LOAD( "891e04a",      0x00000, 0x10000, CRC(daebbc74) SHA1(f61daebf80e5e4640c4cea4ea5767e64a49d928d) )
		ROM_LOAD( "891e04b",      0x10000, 0x10000, CRC(5ffb9ad1) SHA1(e8f00c63dc3091aa344e82dc29f41aedd5a764b4) )
		ROM_LOAD( "891e04c",      0x20000, 0x10000, CRC(2dbbf16b) SHA1(84b2005a1fe61a6a0cf1aa6e0fdf7ff8b1f8f82a) )
		ROM_LOAD( "891e04d",      0x30000, 0x10000, CRC(8b0cd2cc) SHA1(e14109c69fa24d309aed4ff3589cc6619e29f97f) )
	ROM_END(); }}; 
	
	
	
	public static DriverInitHandlerPtr init_bottom9  = new DriverInitHandlerPtr() { public void handler(){
		konami_rom_deinterleave_2(REGION_GFX1);
		konami_rom_deinterleave_2(REGION_GFX2);
	} };
	
	
	
	GAME( 1989, bottom9,  0,       bottom9, bottom9,  bottom9, ROT0, "Konami", "Bottom of the Ninth (version T)" )
	GAME( 1989, bottom9n, bottom9, bottom9, bottom9,  bottom9, ROT0, "Konami", "Bottom of the Ninth (version N)" )
	GAME( 1989, mstadium, bottom9, bottom9, mstadium, bottom9, ROT0, "Konami", "Main Stadium (Japan)" )
}
