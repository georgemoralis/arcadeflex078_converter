/*******************************************************************************

	Irem M107 games:

	Fire Barrel 							(c) 1993 Irem Corporation
	Dream Soccer '94						(c) 1994 Data East Corporation
	World PK Soccer							(c) 1995 Jaleco


	Graphics glitches in both games.

	Emulation by Bryan McPhail, mish@tendril.co.uk

*******************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class m107
{
	
	static unsigned char *m107_ram;
	static int m107_irq_vectorbase,m107_vblank,raster_enable;
	
	#define m107_IRQ_0 ((m107_irq_vectorbase+0)/4) /* VBL interrupt*/
	#define m107_IRQ_1 ((m107_irq_vectorbase+4)/4) /* ??? */
	#define m107_IRQ_2 ((m107_irq_vectorbase+8)/4) /* Raster interrupt */
	#define m107_IRQ_3 ((m107_irq_vectorbase+12)/4) /* ??? */
	
	void m107_vh_raster_partial_refresh(struct mame_bitmap *bitmap,int start_line,int end_line);
	void m107_screenrefresh(struct mame_bitmap *bitmap,const struct rectangle *clip);
	
	/*****************************************************************************/
	
	public static WriteHandlerPtr bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		unsigned char *RAM = memory_region(REGION_CPU1);
	
		if (offset==1) return; /* Unused top byte */
		cpu_setbank(1,&RAM[0x100000 + ((data&0x7)*0x10000)]);
	} };
	
	public static ReadHandlerPtr m107_port_4_r  = new ReadHandlerPtr() { public int handler(int offset){
		if (m107_vblank) return readinputport(4) | 0;
		return readinputport(4) | 0x80;
	} };
	
	public static WriteHandlerPtr m107_coincounter_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (offset==0) {
			coin_counter_w(0,data & 0x01);
			coin_counter_w(1,data & 0x02);
		}
	} };
	
	
	
	enum { VECTOR_INIT, YM2151_ASSERT, YM2151_CLEAR, V30_ASSERT, V30_CLEAR };
	
	static void setvector_callback(int param)
	{
		static int irqvector;
	
		switch(param)
		{
			case VECTOR_INIT:	irqvector = 0;		break;
			case YM2151_ASSERT:	irqvector |= 0x2;	break;
			case YM2151_CLEAR:	irqvector &= ~0x2;	break;
			case V30_ASSERT:	irqvector |= 0x1;	break;
			case V30_CLEAR:		irqvector &= ~0x1;	break;
		}
	
		if (irqvector & 0x2)		/* YM2151 has precedence */
			cpu_irq_line_vector_w(1,0,0x18);
		else if (irqvector & 0x1)	/* V30 */
			cpu_irq_line_vector_w(1,0,0x19);
	
		if (irqvector == 0)	/* no IRQs pending */
			cpu_set_irq_line(1,0,CLEAR_LINE);
		else	/* IRQ pending */
			cpu_set_irq_line(1,0,ASSERT_LINE);
	}
	
	public static WriteHandlerPtr m92_soundlatch_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (offset==0)
		{
			timer_set(TIME_NOW,V30_ASSERT,setvector_callback);
			soundlatch_w.handler(0,data);
	//		logerror("soundlatch_w.handler %02x\n",data);
		}
	} };
	
	static int sound_status;
	
	public static ReadHandlerPtr m92_sound_status_r  = new ReadHandlerPtr() { public int handler(int offset){
		return 0xff;
	} };
	
	public static ReadHandlerPtr m92_soundlatch_r  = new ReadHandlerPtr() { public int handler(int offset){
		if (offset == 0)
		{
			int res = soundlatch_r(offset);
	//		logerror("soundlatch_r %02x\n",res);
			return res;
		}
		else return 0xff;
	} };
	
	public static WriteHandlerPtr m92_sound_irq_ack_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (offset == 0)
		{
			timer_set(TIME_NOW,V30_CLEAR,setvector_callback);
		}
	} };
	
	public static WriteHandlerPtr m92_sound_status_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (offset == 0)
		{
	//		usrintf_showmessage("sound answer %02x",data);
			sound_status = data;
		}
	} };
	
	/*****************************************************************************/
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x00000, 0x9ffff, MRA_ROM ),
		new Memory_ReadAddress( 0xa0000, 0xbffff, MRA_BANK1 ),
		new Memory_ReadAddress( 0xd0000, 0xdffff, m107_vram_r ),
		new Memory_ReadAddress( 0xe0000, 0xeffff, MRA_RAM ),
		new Memory_ReadAddress( 0xf8000, 0xf8fff, MRA_RAM ),
		new Memory_ReadAddress( 0xf9000, 0xf9fff, paletteram_r ),
		new Memory_ReadAddress( 0xffff0, 0xfffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x00000, 0xbffff, MWA_ROM ),
		new Memory_WriteAddress( 0xd0000, 0xdffff, m107_vram_w, m107_vram_data ),
		new Memory_WriteAddress( 0xe0000, 0xeffff, MWA_RAM, m107_ram ), /* System ram */
		new Memory_WriteAddress( 0xf8000, 0xf8fff, MWA_RAM, spriteram ),
		new Memory_WriteAddress( 0xf9000, 0xf9fff, paletteram_xBBBBBGGGGGRRRRR_w, paletteram ),
		new Memory_WriteAddress( 0xffff0, 0xfffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x00, input_port_0_r ), /* Player 1 */
		new IO_ReadPort( 0x01, 0x01, input_port_1_r ), /* Player 2 */
		new IO_ReadPort( 0x02, 0x02, m107_port_4_r ),	/* Coins */
		new IO_ReadPort( 0x03, 0x03, input_port_7_r ), /* Dip 3 */
		new IO_ReadPort( 0x04, 0x04, input_port_6_r ), /* Dip 2 */
		new IO_ReadPort( 0x05, 0x05, input_port_5_r ), /* Dip 1 */
		new IO_ReadPort( 0x06, 0x06, input_port_2_r ), /* Player 3 */
		new IO_ReadPort( 0x07, 0x07, input_port_3_r ), /* Player 4 */
		new IO_ReadPort( 0x08, 0x09, m92_sound_status_r ),	/* answer from sound CPU */
		new IO_ReadPort( 0xc0, 0xc2, MRA_NOP ), /* Only wpksoc: ticket related? */
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x01, m92_soundlatch_w ),
		new IO_WritePort( 0x02, 0x03, m107_coincounter_w ),
		new IO_WritePort( 0x04, 0x05, MWA_NOP ), /* ??? 0008 */
		new IO_WritePort( 0x06, 0x07, bankswitch_w ),
		new IO_WritePort( 0x80, 0x9f, m107_control_w ),
		new IO_WritePort( 0xa0, 0xaf, MWA_NOP ), /* Written with 0's in interrupt */
		new IO_WritePort( 0xb0, 0xb1, m107_spritebuffer_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	/******************************************************************************/
	
	public static Memory_ReadAddress sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x00000, 0x1ffff, MRA_ROM ),
		new Memory_ReadAddress( 0xa0000, 0xa3fff, MRA_RAM ),
		new Memory_ReadAddress( 0xa8042, 0xa8043, YM2151_status_port_0_r ),
		new Memory_ReadAddress( 0xa8044, 0xa8045, m92_soundlatch_r ),
		new Memory_ReadAddress( 0xffff0, 0xfffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x00000, 0x1ffff, MWA_ROM ),
		new Memory_WriteAddress( 0x9ff00, 0x9ffff, MWA_NOP ), /* Irq controller? */
		new Memory_WriteAddress( 0xa0000, 0xa3fff, MWA_RAM ),
		new Memory_WriteAddress( 0xa8000, 0xa803f, IremGA20_w ),
		new Memory_WriteAddress( 0xa8040, 0xa8041, YM2151_register_port_0_w ),
		new Memory_WriteAddress( 0xa8042, 0xa8043, YM2151_data_port_0_w ),
		new Memory_WriteAddress( 0xa8044, 0xa8045, m92_sound_irq_ack_w ),
		new Memory_WriteAddress( 0xa8046, 0xa8047, m92_sound_status_w ),
		new Memory_WriteAddress( 0xffff0, 0xfffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	/******************************************************************************/
	
	static InputPortPtr input_ports_firebarr = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( firebarr )
		IREM_JOYSTICK_1_2(1)
		IREM_JOYSTICK_1_2(2)
		PORT_UNUSED
		PORT_UNUSED
		IREM_COINS
		IREM_SYSTEM_DIPSWITCH
	
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "4" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "Continuous Play" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_dsoccr94 = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( dsoccr94 )
		IREM_JOYSTICK_1_2(1)
		IREM_JOYSTICK_1_2(2)
		IREM_JOYSTICK_3_4(3)
		IREM_JOYSTICK_3_4(4)
		IREM_COINS
		IREM_SYSTEM_DIPSWITCH_4PLAYERS
	
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x03, "Time" );
		PORT_DIPSETTING(    0x00, "1:30" );
		PORT_DIPSETTING(    0x03, "2:00" );
		PORT_DIPSETTING(    0x02, "2:30" );
		PORT_DIPSETTING(    0x01, "3:00" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, "Starting Button" );
		PORT_DIPSETTING(    0x00, "Button 1" );
		PORT_DIPSETTING(    0x20, "Start Button" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_wpksoc = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( wpksoc )
		IREM_JOYSTICK_1_2(1)
		IREM_JOYSTICK_1_2(2)
		PORT_START();  /* not used */
		PORT_START();  /* not used*/
		IREM_COINS
		IREM_SYSTEM_DIPSWITCH_4PLAYERS
	
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	/***************************************************************************/
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		4,
		new int[] { 8, 0, 24, 16 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(3,4), RGN_FRAC(2,4), RGN_FRAC(1,4), RGN_FRAC(0,4) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
			16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8
	);
	
	static GfxLayout spritelayout2 = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(3,4), RGN_FRAC(2,4), RGN_FRAC(1,4), RGN_FRAC(0,4) },
		new int[] { 8, 9, 10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
		32*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   0, 128 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 0, 128 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo firebarr_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   0, 128 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout2,0, 128 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/***************************************************************************/
	
	static void sound_irq(int state)
	{
		if (state)
			timer_set(TIME_NOW,YM2151_ASSERT,setvector_callback);
		else
			timer_set(TIME_NOW,YM2151_CLEAR,setvector_callback);
	}
	
	static struct YM2151interface ym2151_interface =
	{
		1,
		14318180/4,
		{ YM3012_VOL(40,MIXER_PAN_LEFT,40,MIXER_PAN_RIGHT) },
		{ sound_irq }
	};
	
	static struct IremGA20_interface iremGA20_interface =
	{
		14318180/4,
		REGION_SOUND1,
		{ MIXER(100,MIXER_PAN_LEFT), MIXER(100,MIXER_PAN_RIGHT) },
	};
	
	/***************************************************************************/
	
	public static InterruptHandlerPtr m107_interrupt = new InterruptHandlerPtr() {public void handler(){
		m107_vblank=0;
		m107_vh_raster_partial_refresh(Machine->scrbitmap,0,248);
		cpu_set_irq_line_and_vector(0, 0, HOLD_LINE, m107_IRQ_0); /* VBL */
	} };
	
	public static InterruptHandlerPtr m107_raster_interrupt = new InterruptHandlerPtr() {public void handler(){
		static int last_line=0;
		int line = 256 - cpu_getiloops();
	
		if (keyboard_pressed_memory(KEYCODE_F1)) {
			raster_enable ^= 1;
			if (raster_enable)
				usrintf_showmessage("Raster IRQ enabled");
			else
				usrintf_showmessage("Raster IRQ disabled");
		}
	
		/* Raster interrupt */
		if (raster_enable && line==m107_raster_irq_position) {
			if (osd_skip_this_frame()==0)
				m107_vh_raster_partial_refresh(Machine->scrbitmap,last_line,line);
			last_line=line+1;
	
			cpu_set_irq_line_and_vector(0, 0, HOLD_LINE, m107_IRQ_2);
		}
	
		/* Kludge to get Fire Barrel running */
		else if (line==118)
		{
			cpu_set_irq_line_and_vector(0, 0, HOLD_LINE, m107_IRQ_3);
		}
	
		/* Redraw screen, then set vblank and trigger the VBL interrupt */
		else if (line==248) {
			if (osd_skip_this_frame()==0)
				m107_vh_raster_partial_refresh(Machine->scrbitmap,last_line,248);
			last_line=0;
			m107_vblank=1;
			cpu_set_irq_line_and_vector(0, 0, HOLD_LINE, m107_IRQ_0);
		}
	
		/* End of vblank */
		else if (line==255)
			m107_vblank=0;
	} };
	
	static MACHINE_DRIVER_START( firebarr )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(V33, 28000000/2)	/* NEC V33, 28MHz clock */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(readport,writeport)
		MDRV_CPU_VBLANK_INT(m107_raster_interrupt,256) /* 8 prelines, 240 visible lines, 8 for vblank? */
	
		MDRV_CPU_ADD(V30, 14318000/2)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)	/* 14.318 MHz */
		MDRV_CPU_MEMORY(sound_readmem,sound_writemem)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 512)
		MDRV_VISIBLE_AREA(80, 511-112, 128+8, 511-128-8) /* 320 x 240 */
		MDRV_GFXDECODE(firebarr_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(2048)
	
		MDRV_VIDEO_START(m107)
		MDRV_VIDEO_UPDATE(m107)
	
		/* sound hardware */
		MDRV_SOUND_ATTRIBUTES(SOUND_SUPPORTS_STEREO)
		MDRV_SOUND_ADD(YM2151, ym2151_interface)
		MDRV_SOUND_ADD(IREMGA20, iremGA20_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( dsoccr94 )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(V33, 20000000/2)	/* NEC V33, Could be 28MHz clock? */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(readport,writeport)
		MDRV_CPU_VBLANK_INT(m107_interrupt,1)
	
		MDRV_CPU_ADD(V30, 14318000/2)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)	/* 14.318 MHz */
		MDRV_CPU_MEMORY(sound_readmem,sound_writemem)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 512)
		MDRV_VISIBLE_AREA(80, 511-112, 128+8, 511-128-8) /* 320 x 240 */
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(2048)
	
		MDRV_VIDEO_START(m107)
		MDRV_VIDEO_UPDATE(m107)
	
		/* sound hardware */
		MDRV_SOUND_ATTRIBUTES(SOUND_SUPPORTS_STEREO)
		MDRV_SOUND_ADD(YM2151, ym2151_interface)
		MDRV_SOUND_ADD(IREMGA20, iremGA20_interface)
	MACHINE_DRIVER_END
	
	/***************************************************************************/
	
	static RomLoadPtr rom_firebarr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 )
		ROM_LOAD16_BYTE( "f4-h0",  0x000001, 0x40000, CRC(2aa5676e) SHA1(7f51c462c58b63fa4f34ec9dd2ee69c932ebf718) )
		ROM_LOAD16_BYTE( "f4-l0",  0x000000, 0x40000, CRC(42f75d59) SHA1(eba3a02874d608ecb8c93160c8f0b4c8bb8061d2) )
		ROM_LOAD16_BYTE( "f4-h1",  0x080001, 0x20000, CRC(bb7f6968) SHA1(366747672aac939454d9915cda5277b0438f063b) )
		ROM_LOAD16_BYTE( "f4-l1",  0x080000, 0x20000, CRC(9d57edd6) SHA1(16122829b61aa3aee88aeb6634831e8cf95eaee0) )
	
		ROM_REGION( 0x100000 * 2, REGION_CPU2, 0 )
		ROM_LOAD16_BYTE( "f4-sh0", 0x000001, 0x10000, CRC(30a8e232) SHA1(d4695aed35a1aa796b2872e58a6014e8b28bc154) )
		ROM_LOAD16_BYTE( "f4-sl0", 0x000000, 0x10000, CRC(204b5f1f) SHA1(f0386500773cd7cca93f0e8e740db29182320c70) )
	
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE )	/* chars */
		ROM_LOAD16_BYTE( "f4-c00", 0x000000, 0x80000, CRC(50cab384) SHA1(66e88a1dfa943e0d49c2e186ac2f6cbf5cfe0864) )
		ROM_LOAD16_BYTE( "f4-c10", 0x000001, 0x80000, CRC(330c6df2) SHA1(f199d959385398adb6b86ec8ec5de8b40899597c) )
		ROM_LOAD16_BYTE( "f4-c01", 0x100000, 0x80000, CRC(12a698c8) SHA1(74d21768bac70e8cb7e1a6737f758f33869b6af9) )
		ROM_LOAD16_BYTE( "f4-c11", 0x100001, 0x80000, CRC(3f9add18) SHA1(840339a1f33d68c555e42618dd436788639b1edf) )
	
		ROM_REGION( 0x400000, REGION_GFX2, ROMREGION_DISPOSE )	/* sprites */
		ROM_LOAD16_BYTE( "f4-000", 0x000000, 0x80000, CRC(920deee9) SHA1(6341eeccdad97fde5337f32f317ddc94f6b8d07a) )
		ROM_LOAD16_BYTE( "f4-001", 0x000001, 0x80000, CRC(e5725eaf) SHA1(c884d69742484a7c07eb0c7882a33d90b240529e) )
		ROM_LOAD16_BYTE( "f4-010", 0x100000, 0x80000, CRC(3505d185) SHA1(1330c18eaadb3e23d6205f3912015cb9ca5f3590) )
		ROM_LOAD16_BYTE( "f4-011", 0x100001, 0x80000, CRC(1912682f) SHA1(d0234877aabf94df7f6a6091e38247954725e1f3) )
		ROM_LOAD16_BYTE( "f4-020", 0x200000, 0x80000, CRC(ec130b8e) SHA1(6a4562f3e39d02f97f3b917e4a51f48b6f43a4c8) )
		ROM_LOAD16_BYTE( "f4-021", 0x200001, 0x80000, CRC(8dd384dc) SHA1(dee79d0d48762b98c20c88ba6617de5e939f596d) )
		ROM_LOAD16_BYTE( "f4-030", 0x300000, 0x80000, CRC(7e7b30cd) SHA1(eca9d2a5d9f9deebb565456018126bc37a1de1d8) )
		ROM_LOAD16_BYTE( "f4-031", 0x300001, 0x80000, CRC(83ac56c5) SHA1(47e1063c71d5570fecf8591c2cb7c74fd45199f5) )
	
		ROM_REGION( 0x40000, REGION_USER1, 0 )	/* sprite tables */
		ROM_LOAD16_BYTE( "f4-drh", 0x000001, 0x20000, CRC(12001372) SHA1(a5346d8a741cd1a93aa289562bb56d2fc40c1bbb) )
		ROM_LOAD16_BYTE( "f4-drl", 0x000000, 0x20000, CRC(08cb7533) SHA1(9e0d8f8498bddfa1c6135abbab4465e9eeb033fe) )
	
		ROM_REGION( 0x80000, REGION_SOUND1, ROMREGION_SOUNDONLY )	/* ADPCM samples */
		ROM_LOAD( "f4-da0",          0x000000, 0x80000, CRC(7a493e2e) SHA1(f6a8bacbe25760c86bdd8e8bb6d052ff15718eef) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_dsoccr94 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x180000, REGION_CPU1, 0 ) /* v30 main cpu */
		ROM_LOAD16_BYTE( "ds_h0-c.rom",  0x000001, 0x040000, CRC(d01d3fd7) SHA1(925dff999252bf3b920bc0f427744e1464620fe8) )
		ROM_LOAD16_BYTE( "ds_l0-c.rom",  0x000000, 0x040000, CRC(8af0afe2) SHA1(423c77d392a79cdaed66ad8c13039450d34d3f6d) )
		ROM_LOAD16_BYTE( "ds_h1-c.rom",  0x100001, 0x040000, CRC(6109041b) SHA1(063898a88f8a6a9f1510aa55e53a39f037b02903) )
		ROM_LOAD16_BYTE( "ds_l1-c.rom",  0x100000, 0x040000, CRC(97a01f6b) SHA1(e188e28f880f5f3f4d7b49eca639d643989b1468) )
	
		ROM_REGION( 0x100000 * 2, REGION_CPU2, 0 )
		ROM_LOAD16_BYTE( "ds_sh0.rom",   0x000001, 0x010000, CRC(23fe6ffc) SHA1(896377961cafc19e44d9d889f9fbfdbaedd556da) )
		ROM_LOAD16_BYTE( "ds_sl0.rom",   0x000000, 0x010000, CRC(768132e5) SHA1(1bb64516eb58d3b246f08e1c07f091e78085689f) )
	
		ROM_REGION( 0x400000, REGION_GFX1, ROMREGION_DISPOSE )	/* chars */
		ROM_LOAD16_BYTE( "ds_c00.rom",   0x000000, 0x100000, CRC(2d31d418) SHA1(6cd0e362bc2e3f2b20d96ee97a04bff46ee3016a) )
		ROM_LOAD16_BYTE( "ds_c10.rom",   0x000001, 0x100000, CRC(57f7bcd3) SHA1(a38e7cdfdea72d882fba414cae391ba09443e73c) )
		ROM_LOAD16_BYTE( "ds_c01.rom",   0x200000, 0x100000, CRC(9d31a464) SHA1(1e38ac296f64d77fabfc0d5f7921a9b7a8424875) )
		ROM_LOAD16_BYTE( "ds_c11.rom",   0x200001, 0x100000, CRC(a372e79f) SHA1(6b0889cfc2970028832566e25257927ddc461ea6) )
	
		ROM_REGION( 0x400000, REGION_GFX2, ROMREGION_DISPOSE )	/* sprites */
		ROM_LOAD( "ds_000.rom",   0x000000, 0x100000, CRC(366b3e29) SHA1(cb016dcbdc6e8ea56c28c00135263666b07df991) )
		ROM_LOAD( "ds_010.rom",   0x100000, 0x100000, CRC(28a4cc40) SHA1(7f4e1ef995eaadf1945ee22ab3270cb8a21c601d) )
		ROM_LOAD( "ds_020.rom",   0x200000, 0x100000, CRC(5a310f7f) SHA1(21969e4247c8328d27118d00604096deaf6700af) )
		ROM_LOAD( "ds_030.rom",   0x300000, 0x100000, CRC(328b1f45) SHA1(4cbbd4d9be4fc151d426175bdbd35d8481bf2966) )
	
		ROM_REGION( 0x100000, REGION_SOUND1, ROMREGION_SOUNDONLY )	 /* ADPCM samples */
		ROM_LOAD( "ds_da0.rom" ,  0x000000, 0x100000, CRC(67fc52fd) SHA1(5771e948115af8fe4a6d3f448c03a2a9b42b6f20) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_wpksoc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x180000, REGION_CPU1, 0 ) /* v30 main cpu */
		ROM_LOAD16_BYTE( "pkeurd.h0",    0x000001, 0x040000, CRC(b4917788) SHA1(673294c518eaf28354fa6a3058f9325c6d9ddde6) )
		ROM_LOAD16_BYTE( "pkeurd.l0",    0x000000, 0x040000, CRC(03816bae) SHA1(832e2ec722b41d41626fec583fc11e9ff62cdaa0) )
	
		ROM_REGION( 0x100000 * 2, REGION_CPU2, 0 )
		ROM_LOAD16_BYTE( "pkos.sh0",     0x000001, 0x010000, CRC(1145998c) SHA1(cdb2a428e0f35302b81696dab02d3dd2c433f6e5) )
		ROM_LOAD16_BYTE( "pkos.sl0",     0x000000, 0x010000, CRC(542ee1c7) SHA1(b934adeecbba17cf96b06a2b1dc1ceaebdf9ad10) )
	
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE )	/* chars */
		ROM_LOAD16_BYTE( "pkos.c00", 0x000000, 0x80000, CRC(42ae3d73) SHA1(e4777066155c9882695ebff0412bd879b8d6f716) )
		ROM_LOAD16_BYTE( "pkos.c10", 0x000001, 0x80000, CRC(86acf45c) SHA1(3b3d2abcf8000161a37d5e2619df529533aea47d) )
		ROM_LOAD16_BYTE( "pkos.c01", 0x100000, 0x80000, CRC(b0d33f87) SHA1(f2c0e3a10615c6861a3f6fd82a3f066e8e264233) )
		ROM_LOAD16_BYTE( "pkos.c11", 0x100001, 0x80000, CRC(19de7d63) SHA1(6d0633e412b47accaecc887a5c39f542eda49e81) )
	
		ROM_REGION( 0x400000, REGION_GFX2, ROMREGION_DISPOSE )	/* sprites */
		ROM_LOAD16_BYTE( "pk.o00", 0x000000, 0x80000, CRC(165ce027) SHA1(3510b323c683ade4dd7307b539072bb342b6796d) )
		ROM_LOAD16_BYTE( "pk.o01", 0x000001, 0x80000, CRC(e2745147) SHA1(99026525449c2ca84e054a7d633c400e0e836461) )
		ROM_LOAD16_BYTE( "pk.o10", 0x100000, 0x80000, CRC(6c171b73) SHA1(a99c9f012f21373daea08d28554cc36170f4e1fa) )
		ROM_LOAD16_BYTE( "pk.o11", 0x100001, 0x80000, CRC(471c0bf4) SHA1(1cace5ffd5db91850662de929cb9086dc154d662) )
		ROM_LOAD16_BYTE( "pk.o20", 0x200000, 0x80000, CRC(c886dad1) SHA1(9b58a2f108547c3f55399932a7e56031c5658737) )
		ROM_LOAD16_BYTE( "pk.o21", 0x200001, 0x80000, CRC(91e877ff) SHA1(3df095632728ab16ab229d592ab12d3df44b2629) )
		ROM_LOAD16_BYTE( "pk.o30", 0x300000, 0x80000, CRC(3390621c) SHA1(4138c690666f78b1c5cf83d815ed6b37239a94b4) )
		ROM_LOAD16_BYTE( "pk.o31", 0x300001, 0x80000, CRC(4d322804) SHA1(b5e2b40e3ce83b6f97b2b57edaa79df6968d0997) )
	
		ROM_REGION( 0x100000, REGION_SOUND1, ROMREGION_SOUNDONLY )	 /* ADPCM samples */
		ROM_LOAD( "pk.da0",       0x000000, 0x80000, CRC(26a34cf4) SHA1(a8a7cd91cdc6d644ee02ca16e7fdc8debf8f3a5f) )
	ROM_END(); }}; 
	
	/***************************************************************************/
	
	public static DriverInitHandlerPtr init_firebarr  = new DriverInitHandlerPtr() { public void handler(){
		unsigned char *RAM = memory_region(REGION_CPU1);
	
		memcpy(RAM+0xffff0,RAM+0x7fff0,0x10); /* Start vector */
		cpu_setbank(1,&RAM[0xa0000]); /* Initial bank */
	
		RAM = memory_region(REGION_CPU2);
		memcpy(RAM+0xffff0,RAM+0x1fff0,0x10); /* Sound cpu Start vector */
	
		irem_cpu_decrypt(1,rtypeleo_decryption_table);
	
		m107_irq_vectorbase=0x20;
		m107_spritesystem = 1;
	
		raster_enable=1;
	} };
	
	public static DriverInitHandlerPtr init_dsoccr94  = new DriverInitHandlerPtr() { public void handler(){
		unsigned char *RAM = memory_region(REGION_CPU1);
	
		memcpy(RAM+0xffff0,RAM+0x7fff0,0x10); /* Start vector */
		cpu_setbank(1,&RAM[0xa0000]); /* Initial bank */
	
		RAM = memory_region(REGION_CPU2);
		memcpy(RAM+0xffff0,RAM+0x1fff0,0x10); /* Sound cpu Start vector */
	
		irem_cpu_decrypt(1,dsoccr94_decryption_table);
	
		m107_irq_vectorbase=0x80;
		m107_spritesystem = 0;
	
		/* This game doesn't use raster IRQ's */
		raster_enable=0;
	} };
	
	public static DriverInitHandlerPtr init_wpksoc  = new DriverInitHandlerPtr() { public void handler(){
		unsigned char *RAM = memory_region(REGION_CPU1);
	
		memcpy(RAM+0xffff0,RAM+0x7fff0,0x10); /* Start vector */
		cpu_setbank(1,&RAM[0xa0000]); /* Initial bank */
	
		RAM = memory_region(REGION_CPU2);
		memcpy(RAM+0xffff0,RAM+0x1fff0,0x10); /* Sound cpu Start vector */
	
		irem_cpu_decrypt(1,leagueman_decryption_table);
	
		m107_irq_vectorbase=0x80;
		m107_spritesystem = 0;
	
		raster_enable=0;
	} };
	
	/***************************************************************************/
	
	GAMEX(1993, firebarr, 0, firebarr, firebarr, firebarr, ROT270, "Irem", "Fire Barrel (Japan)", GAME_NO_SOUND | GAME_IMPERFECT_GRAPHICS )
	GAME( 1994, dsoccr94, 0, dsoccr94, dsoccr94, dsoccr94, ROT0,   "Irem (Data East Corporation license)", "Dream Soccer '94" )
	GAMEX(1995, wpksoc,   0, firebarr, wpksoc,	 wpksoc,   ROT0,   "Jaleco", "World PK Soccer", GAME_NOT_WORKING | GAME_IMPERFECT_GRAPHICS )
}
