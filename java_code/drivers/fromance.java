/******************************************************************************

	Game Driver for Video System Mahjong series.

	Idol-Mahjong Final Romance (Japan)
	(c)1991 Video System Co.,Ltd.

	Nekketsu Mahjong Sengen! AFTER 5 (Japan)
	(c)1991 Video System Co.,Ltd.

	Mahjong Daiyogen (Japan)
	(c)1990 Video System Co.,Ltd.

	Mahjong Fun Club - Idol Saizensen (Japan)
	(c)1989 Video System Co.,Ltd.

	Mahjong Natsu Monogatari (Mahjong Summer Story) (Japan)
	(c)1989 Video System Co.,Ltd.

	Natsuiro Mahjong (Mahjong Summer Story) (Japan)
	(c)1989 Video System Co.,Ltd.

	Idol-Mahjong Housoukyoku (Japan)
	(c)1988 System Service Co.,Ltd.

	Rettou Juudan Nekkyoku Janshi - Higashi Nippon Hen (Japan)
	(c)1988 Video System Co.,Ltd.

	Driver by Takahiro Nogi <nogi@kt.rim.or.jp> 2001/02/04 -
	and Nicola Salmoria, Aaron Giles

******************************************************************************/
/******************************************************************************
Memo:

- 2player's input is not supported.

- Communication between MAIN CPU and SUB CPU can be wrong.

******************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class fromance
{
	
	
	/* Local variables */
	static UINT8 fromance_directionflag;
	static UINT8 fromance_commanddata;
	static UINT8 fromance_portselect;
	
	static UINT8 fromance_adpcm_reset;
	static UINT8 fromance_adpcm_data;
	static UINT8 fromance_vclk_left;
	
	
	
	/*************************************
	 *
	 *	Machine init
	 *
	 *************************************/
	
	public static MachineInitHandlerPtr machine_init_fromance  = new MachineInitHandlerPtr() { public void handler(){
		fromance_directionflag = 0;
		fromance_commanddata = 0;
		fromance_portselect = 0;
	
		fromance_adpcm_reset = 0;
		fromance_adpcm_data = 0;
		fromance_vclk_left = 0;
	} };
	
	
	
	/*************************************
	 *
	 *	Master/slave communication
	 *
	 *************************************/
	
	public static ReadHandlerPtr fromance_commanddata_r  = new ReadHandlerPtr() { public int handler(int offset){
		return fromance_commanddata;
	} };
	
	
	static void deferred_commanddata_w(int data)
	{
		fromance_commanddata = data;
		fromance_directionflag = 1;
	}
	
	
	public static WriteHandlerPtr fromance_commanddata_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* do this on a timer to let the slave CPU synchronize */
		timer_set(TIME_NOW, data, deferred_commanddata_w);
	} };
	
	
	public static ReadHandlerPtr fromance_busycheck_main_r  = new ReadHandlerPtr() { public int handler(int offset){
		/* set a timer to force synchronization after the read */
		timer_set(TIME_NOW, 0, NULL);
	
		if (!fromance_directionflag) return 0x00;		// standby
		else return 0xff;								// busy
	} };
	
	
	public static ReadHandlerPtr fromance_busycheck_sub_r  = new ReadHandlerPtr() { public int handler(int offset){
		if (fromance_directionflag) return 0xff;		// standby
		else return 0x00;								// busy
	} };
	
	
	public static WriteHandlerPtr fromance_busycheck_sub_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		fromance_directionflag = 0;
	} };
	
	
	
	/*************************************
	 *
	 *	Slave CPU ROM banking
	 *
	 *************************************/
	
	public static WriteHandlerPtr fromance_rombank_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		unsigned char *ROM = memory_region(REGION_CPU2);
	
		cpu_setbank(1, &ROM[0x010000 + (0x4000 * data)]);
	} };
	
	
	
	/*************************************
	 *
	 *	ADPCM interface
	 *
	 *************************************/
	
	public static WriteHandlerPtr fromance_adpcm_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		fromance_adpcm_reset = (data & 0x01);
		fromance_vclk_left = 0;
	
		MSM5205_reset_w(0, !(data & 0x01));
	} };
	
	
	public static WriteHandlerPtr fromance_adpcm_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		fromance_adpcm_data = data;
		fromance_vclk_left = 2;
	} };
	
	
	static void fromance_adpcm_int(int irq)
	{
		/* skip if we're reset */
		if (!fromance_adpcm_reset)
			return;
	
		/* clock the data through */
		if (fromance_vclk_left)
		{
			MSM5205_data_w(0, (fromance_adpcm_data >> 4));
			fromance_adpcm_data <<= 4;
			fromance_vclk_left--;
		}
	
		/* generate an NMI if we're out of data */
		if (!fromance_vclk_left)
			cpu_set_nmi_line(1, PULSE_LINE);
	}
	
	
	
	/*************************************
	 *
	 *	Input handlers
	 *
	 *************************************/
	
	public static WriteHandlerPtr fromance_portselect_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		fromance_portselect = data;
	} };
	
	
	public static ReadHandlerPtr fromance_keymatrix_r  = new ReadHandlerPtr() { public int handler(int offset){
		int ret = 0xff;
	
		if (fromance_portselect & 0x01)
			ret &= readinputport(4);
		if (fromance_portselect & 0x02)
			ret &= readinputport(5);
		if (fromance_portselect & 0x04)
			ret &= readinputport(6);
		if (fromance_portselect & 0x08)
			ret &= readinputport(7);
		if (fromance_portselect & 0x10)
			ret &= readinputport(8);
	
		return ret;
	} };
	
	
	
	/*************************************
	 *
	 *	Coin counters
	 *
	 *************************************/
	
	public static WriteHandlerPtr fromance_coinctr_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		//
	} };
	
	
	
	/*************************************
	 *
	 *	Master CPU memory handlers
	 *
	 *************************************/
	
	public static Memory_ReadAddress nekkyoku_readmem_main[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new Memory_ReadAddress( 0xf000, 0xf000, input_port_0_r ),
		new Memory_ReadAddress( 0xf001, 0xf001, fromance_keymatrix_r ),
		new Memory_ReadAddress( 0xf002, 0xf002, input_port_1_r ),
		new Memory_ReadAddress( 0xf003, 0xf003, fromance_busycheck_main_r ),
		new Memory_ReadAddress( 0xf004, 0xf004, input_port_3_r ),
		new Memory_ReadAddress( 0xf005, 0xf005, input_port_2_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress nekkyoku_writemem_main[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xdfff, MWA_RAM ),
		new Memory_WriteAddress( 0xf000, 0xf000, fromance_portselect_w ),
		new Memory_WriteAddress( 0xf001, 0xf001, MWA_NOP ),
		new Memory_WriteAddress( 0xf002, 0xf002, fromance_coinctr_w ),
		new Memory_WriteAddress( 0xf003, 0xf003, fromance_commanddata_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_ReadAddress fromance_readmem_main[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new Memory_ReadAddress( 0x9e89, 0x9e89, MRA_NOP ),			// unknown (idolmj)
		new Memory_ReadAddress( 0xe000, 0xe000, input_port_0_r ),
		new Memory_ReadAddress( 0xe001, 0xe001, fromance_keymatrix_r ),
		new Memory_ReadAddress( 0xe002, 0xe002, input_port_1_r ),
		new Memory_ReadAddress( 0xe003, 0xe003, fromance_busycheck_main_r ),
		new Memory_ReadAddress( 0xe004, 0xe004, input_port_3_r ),
		new Memory_ReadAddress( 0xe005, 0xe005, input_port_2_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress fromance_writemem_main[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xdfff, MWA_RAM ),
		new Memory_WriteAddress( 0xe000, 0xe000, fromance_portselect_w ),
		new Memory_WriteAddress( 0xe002, 0xe002, fromance_coinctr_w ),
		new Memory_WriteAddress( 0xe003, 0xe003, fromance_commanddata_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	
	/*************************************
	 *
	 *	Slave CPU memory handlers
	 *
	 *************************************/
	
	public static Memory_ReadAddress nekkyoku_readmem_sub[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0xbfff, MRA_BANK1 ),
		new Memory_ReadAddress( 0xc000, 0xefff, fromance_videoram_r ),
		new Memory_ReadAddress( 0xf000, 0xf7ff, MRA_RAM ),
		new Memory_ReadAddress( 0xf800, 0xffff, fromance_paletteram_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress nekkyoku_writemem_sub[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0xbfff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xefff, fromance_videoram_w ),
		new Memory_WriteAddress( 0xf000, 0xf7ff, MWA_RAM ),
		new Memory_WriteAddress( 0xf800, 0xffff, fromance_paletteram_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_ReadAddress fromance_readmem_sub[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0xbfff, MRA_BANK1 ),
		new Memory_ReadAddress( 0xc000, 0xc7ff, MRA_RAM ),
		new Memory_ReadAddress( 0xc800, 0xcfff, fromance_paletteram_r ),
		new Memory_ReadAddress( 0xd000, 0xffff, fromance_videoram_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress fromance_writemem_sub[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0xbfff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xc7ff, MWA_RAM ),
		new Memory_WriteAddress( 0xc800, 0xcfff, fromance_paletteram_w ),
		new Memory_WriteAddress( 0xd000, 0xffff, fromance_videoram_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	
	/*************************************
	 *
	 *	Slave CPU port handlers
	 *
	 *************************************/
	
	public static IO_ReadPort nekkyoku_readport_sub[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x12, 0x12, IORP_NOP ),				// unknown
		new IO_ReadPort( 0xe1, 0xe1, fromance_busycheck_sub_r ),
		new IO_ReadPort( 0xe6, 0xe6, fromance_commanddata_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort nekkyoku_writeport_sub[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x10, 0x10, fromance_crtc_data_w ),
		new IO_WritePort( 0x11, 0x11, fromance_crtc_register_w ),
		new IO_WritePort( 0xe0, 0xe0, fromance_rombank_w ),
		new IO_WritePort( 0xe1, 0xe1, fromance_gfxreg_w ),
		new IO_WritePort( 0xe2, 0xe5, fromance_scroll_w ),
		new IO_WritePort( 0xe6, 0xe6, fromance_busycheck_sub_w ),
		new IO_WritePort( 0xe7, 0xe7, fromance_adpcm_reset_w ),
		new IO_WritePort( 0xe8, 0xe8, fromance_adpcm_w ),
		new IO_WritePort( 0xe9, 0xe9, AY8910_write_port_0_w ),
		new IO_WritePort( 0xea, 0xea, AY8910_control_port_0_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort fromance_readport_sub[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x12, 0x12, IORP_NOP ),				// unknown
		new IO_ReadPort( 0x21, 0x21, fromance_busycheck_sub_r ),
		new IO_ReadPort( 0x26, 0x26, fromance_commanddata_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort idolmj_writeport_sub[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x10, 0x10, fromance_crtc_data_w ),
		new IO_WritePort( 0x11, 0x11, fromance_crtc_register_w ),
		new IO_WritePort( 0x20, 0x20, fromance_rombank_w ),
		new IO_WritePort( 0x21, 0x21, fromance_gfxreg_w ),
		new IO_WritePort( 0x22, 0x25, fromance_scroll_w ),
		new IO_WritePort( 0x26, 0x26, fromance_busycheck_sub_w ),
		new IO_WritePort( 0x27, 0x27, fromance_adpcm_reset_w ),
		new IO_WritePort( 0x28, 0x28, fromance_adpcm_w ),
		new IO_WritePort( 0x29, 0x29, AY8910_write_port_0_w ),
		new IO_WritePort( 0x2a, 0x2a, AY8910_control_port_0_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort fromance_writeport_sub[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x10, 0x10, fromance_crtc_data_w ),
		new IO_WritePort( 0x11, 0x11, fromance_crtc_register_w ),
		new IO_WritePort( 0x20, 0x20, fromance_rombank_w ),
		new IO_WritePort( 0x21, 0x21, fromance_gfxreg_w ),
		new IO_WritePort( 0x22, 0x25, fromance_scroll_w ),
		new IO_WritePort( 0x26, 0x26, fromance_busycheck_sub_w ),
		new IO_WritePort( 0x27, 0x27, fromance_adpcm_reset_w ),
		new IO_WritePort( 0x28, 0x28, fromance_adpcm_w ),
		new IO_WritePort( 0x2a, 0x2a, YM2413_register_port_0_w ),
		new IO_WritePort( 0x2b, 0x2b, YM2413_data_port_0_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	#define FROMANCE_KEYMATRIX1 \
		PORT_START();  \
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 A", KEYCODE_A, IP_JOY_NONE );\
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 E", KEYCODE_E, IP_JOY_NONE );\
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 I", KEYCODE_I, IP_JOY_NONE );\
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 M", KEYCODE_M, IP_JOY_NONE );\
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Kan", KEYCODE_LCONTROL, IP_JOY_NONE );\
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START1 );\
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );\
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );\
	
	#define FROMANCE_KEYMATRIX2 \
		PORT_START();  \
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 B", KEYCODE_B, IP_JOY_NONE );\
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 F", KEYCODE_F, IP_JOY_NONE );\
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 J", KEYCODE_J, IP_JOY_NONE );\
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 N", KEYCODE_N, IP_JOY_NONE );\
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Reach", KEYCODE_LSHIFT, IP_JOY_NONE );\
		PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 Bet", KEYCODE_2, IP_JOY_NONE );\
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );\
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );\
	
	#define FROMANCE_KEYMATRIX3 \
		PORT_START();  \
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 C", KEYCODE_C, IP_JOY_NONE );\
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 G", KEYCODE_G, IP_JOY_NONE );\
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 K", KEYCODE_K, IP_JOY_NONE );\
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Chi", KEYCODE_SPACE, IP_JOY_NONE );\
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Ron", KEYCODE_Z, IP_JOY_NONE );\
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );\
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );\
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );\
	
	#define FROMANCE_KEYMATRIX4 \
		PORT_START();  \
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 D", KEYCODE_D, IP_JOY_NONE );\
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 H", KEYCODE_H, IP_JOY_NONE );\
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 L", KEYCODE_L, IP_JOY_NONE );\
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Pon", KEYCODE_LALT, IP_JOY_NONE );\
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );\
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );\
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );\
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
	#define FROMANCE_KEYMATRIX5 \
		PORT_START();  \
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 Last Chance", KEYCODE_RALT, IP_JOY_NONE );\
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 Take Score", KEYCODE_RCONTROL, IP_JOY_NONE );\
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 Double Up", KEYCODE_RSHIFT, IP_JOY_NONE );\
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Flip", KEYCODE_X, IP_JOY_NONE );\
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Big", KEYCODE_ENTER, IP_JOY_NONE );\
		PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 Small", KEYCODE_BACKSPACE, IP_JOY_NONE );\
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );\
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
	
	static InputPortPtr input_ports_nekkyoku = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( nekkyoku )
		PORT_START(); 	/* (0) TEST SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (1) COIN SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );		// COIN1
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (2) DIPSW-1 */
		PORT_DIPNAME( 0x01, 0x00, "DIPSW 1-1" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIPSW 1-2" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIPSW 1-3" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIPSW 1-4" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "DIPSW 1-5" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "DIPSW 1-6" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "DIPSW 1-7" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "DIPSW 1-8" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START(); 	/* (3) DIPSW-2 */
		PORT_DIPNAME( 0x01, 0x00, "DIPSW 2-1" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIPSW 2-2" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIPSW 2-3" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIPSW 2-4" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "DIPSW 2-5" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "DIPSW 2-6" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		FROMANCE_KEYMATRIX1	/* (4) PORT 1-0 */
		FROMANCE_KEYMATRIX2	/* (5) PORT 1-1 */
		FROMANCE_KEYMATRIX3	/* (6) PORT 1-2 */
		FROMANCE_KEYMATRIX4	/* (7) PORT 1-3 */
		FROMANCE_KEYMATRIX5	/* (8) PORT 1-4 */
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_idolmj = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( idolmj )
		PORT_START(); 	/* (0) TEST SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE3 );	// MEMORY RESET
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BITX( 0x08, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )	// TEST
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (1) COIN SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );		// COIN1
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (2) DIPSW-1 */
		PORT_DIPNAME( 0x01, 0x00, "DIPSW 1-1" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIPSW 1-2" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIPSW 1-3" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIPSW 1-4" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "Voices" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "DIPSW 1-6" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "DIPSW 1-7" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "DIPSW 1-8" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START(); 	/* (3) DIPSW-2 */
		PORT_DIPNAME( 0x01, 0x00, "DIPSW 2-1" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIPSW 2-2" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIPSW 2-3" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIPSW 2-4" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "DIPSW 2-5" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "DIPSW 2-6" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "DIPSW 2-7" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		FROMANCE_KEYMATRIX1	/* (4) PORT 1-0 */
		FROMANCE_KEYMATRIX2	/* (5) PORT 1-1 */
		FROMANCE_KEYMATRIX3	/* (6) PORT 1-2 */
		FROMANCE_KEYMATRIX4	/* (7) PORT 1-3 */
		FROMANCE_KEYMATRIX5	/* (8) PORT 1-4 */
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_fromance = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( fromance )
		PORT_START(); 	/* (0) TEST SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE3 );	// MEMORY RESET
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BITX( 0x08, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )	// TEST
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (1) COIN SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );		// COIN1
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (2) DIPSW-1 */
		PORT_DIPNAME( 0x01, 0x00, "DIPSW 1-1" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIPSW 1-2" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIPSW 1-3" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIPSW 1-4" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "DIPSW 1-5" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "DIPSW 1-7" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
	
		PORT_START(); 	/* (3) DIPSW-2 */
		PORT_DIPNAME( 0x01, 0x00, "DIPSW 2-1" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIPSW 2-2" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIPSW 2-3" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIPSW 2-4" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "DIPSW 2-5" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "DIPSW 2-6" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "DIPSW 2-7" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		FROMANCE_KEYMATRIX1	/* (4) PORT 1-0 */
		FROMANCE_KEYMATRIX2	/* (5) PORT 1-1 */
		FROMANCE_KEYMATRIX3	/* (6) PORT 1-2 */
		FROMANCE_KEYMATRIX4	/* (7) PORT 1-3 */
		FROMANCE_KEYMATRIX5	/* (8) PORT 1-4 */
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_nmsengen = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( nmsengen )
		PORT_START(); 	/* (0) TEST SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE3 );	// MEMORY RESET
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BITX( 0x08, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )	// TEST
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (1) COIN SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );		// COIN1
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (2) DIPSW-1 */
		PORT_DIPNAME( 0x01, 0x00, "DIPSW 1-1" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIPSW 1-2" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIPSW 1-3" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIPSW 1-4" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "DIPSW 1-5" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "DIPSW 1-7" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
	
		PORT_START(); 	/* (3) DIPSW-2 */
		PORT_DIPNAME( 0x01, 0x00, "DIPSW 2-1" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIPSW 2-2" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIPSW 2-3" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIPSW 2-4" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "DIPSW 2-5" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "DIPSW 2-6" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "DIPSW 2-7" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		FROMANCE_KEYMATRIX1	/* (4) PORT 1-0 */
		FROMANCE_KEYMATRIX2	/* (5) PORT 1-1 */
		FROMANCE_KEYMATRIX3	/* (6) PORT 1-2 */
		FROMANCE_KEYMATRIX4	/* (7) PORT 1-3 */
		FROMANCE_KEYMATRIX5	/* (8) PORT 1-4 */
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_daiyogen = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( daiyogen )
		PORT_START(); 	/* (0) TEST SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE3 );	// MEMORY RESET
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BITX( 0x08, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )	// TEST
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (1) COIN SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );		// COIN1
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (2) DIPSW-1 */
		PORT_DIPNAME( 0x01, 0x00, "DIPSW 1-1" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIPSW 1-2" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIPSW 1-3" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIPSW 1-4" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "DIPSW 1-5" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "DIPSW 1-7" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "DIPSW 1-8" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START(); 	/* (3) DIPSW-2 */
		PORT_DIPNAME( 0x01, 0x00, "DIPSW 2-1" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIPSW 2-2" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIPSW 2-3" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIPSW 2-4" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "DIPSW 2-5" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "DIPSW 2-6" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "DIPSW 2-7" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		FROMANCE_KEYMATRIX1	/* (4) PORT 1-0 */
		FROMANCE_KEYMATRIX2	/* (5) PORT 1-1 */
		FROMANCE_KEYMATRIX3	/* (6) PORT 1-2 */
		FROMANCE_KEYMATRIX4	/* (7) PORT 1-3 */
		FROMANCE_KEYMATRIX5	/* (8) PORT 1-4 */
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_mfunclub = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( mfunclub )
		PORT_START(); 	/* (0) TEST SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE3 );	// MEMORY RESET
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BITX( 0x08, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )	// TEST
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (1) COIN SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );		// COIN1
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (2) DIPSW-1 */
		PORT_DIPNAME( 0x01, 0x00, "DIPSW 1-1" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIPSW 1-2" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIPSW 1-3" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIPSW 1-4" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "Voices" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "DIPSW 1-6" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "DIPSW 1-7" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "DIPSW 1-8" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START(); 	/* (3) DIPSW-2 */
		PORT_DIPNAME( 0x01, 0x00, "DIPSW 2-1" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIPSW 2-2" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIPSW 2-3" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIPSW 2-4" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "DIPSW 2-5" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "DIPSW 2-6" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "DIPSW 2-7" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		FROMANCE_KEYMATRIX1	/* (4) PORT 1-0 */
		FROMANCE_KEYMATRIX2	/* (5) PORT 1-1 */
		FROMANCE_KEYMATRIX3	/* (6) PORT 1-2 */
		FROMANCE_KEYMATRIX4	/* (7) PORT 1-3 */
		FROMANCE_KEYMATRIX5	/* (8) PORT 1-4 */
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_mjnatsu = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( mjnatsu )
		PORT_START(); 	/* (0) TEST SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE3 );	// MEMORY RESET
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BITX( 0x08, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )	// TEST
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (1) COIN SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );		// COIN1
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (2) DIPSW-1 */
		PORT_DIPNAME( 0x01, 0x00, "DIPSW 1-1" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIPSW 1-2" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIPSW 1-3" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIPSW 1-4" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "Voices" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "DIPSW 1-6" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "DIPSW 1-7" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "DIPSW 1-8" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START(); 	/* (3) DIPSW-2 */
		PORT_DIPNAME( 0x01, 0x00, "DIPSW 2-1" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIPSW 2-2" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIPSW 2-3" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIPSW 2-4" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "DIPSW 2-5" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "DIPSW 2-6" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "DIPSW 2-7" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		FROMANCE_KEYMATRIX1	/* (4) PORT 1-0 */
		FROMANCE_KEYMATRIX2	/* (5) PORT 1-1 */
		FROMANCE_KEYMATRIX3	/* (6) PORT 1-2 */
		FROMANCE_KEYMATRIX4	/* (7) PORT 1-3 */
		FROMANCE_KEYMATRIX5	/* (8) PORT 1-4 */
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Graphics definitions
	 *
	 *************************************/
	
	static GfxLayout bglayout = new GfxLayout
	(
		8,4,
		RGN_FRAC(1,1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 4, 0, 12, 8, 20, 16, 28, 24 },
		new int[] { 0*32, 1*32, 2*32, 3*32 },
		16*8
	);
	
	
	static GfxDecodeInfo fromance_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, bglayout,   0, 128 ),
		new GfxDecodeInfo( REGION_GFX2, 0, bglayout,   0, 128 ),
		new GfxDecodeInfo( -1 )
	};
	
	
	
	/*************************************
	 *
	 *	Sound definitions
	 *
	 *************************************/
	
	static struct YM2413interface ym2413_interface=
	{
		1,						/* 1 chip */
		3579545,				/* 3.579545 MHz ? */
		{ YM2413_VOL(100,MIXER_PAN_CENTER,100,MIXER_PAN_CENTER) }
	};
	
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		1,						/* 1 chip */
		12000000/6,				/* 1.5 MHz ? */
		new int[] { 15 },					/* volume */
		new ReadHandlerPtr[] { 0 },					/* read port #0 */
		new ReadHandlerPtr[] { 0 },					/* read port #1 */
		new WriteHandlerPtr[] { 0 },					/* write port #0 */
		new WriteHandlerPtr[] { 0 }					/* write port #1 */
	);
	
	
	static struct MSM5205interface msm5205_interface =
	{
		1,						/* 1 chip */
		384000,					/* 384 KHz */
		{ fromance_adpcm_int },	/* IRQ handler */
		{ MSM5205_S48_4B },		/* 8 KHz */
		{ 80 }					/* volume */
	};
	
	static struct MSM5205interface fromance_msm5205_interface =
	{
		1,						/* 1 chip */
		384000,					/* 384 KHz */
		{ fromance_adpcm_int },	/* IRQ handler */
		{ MSM5205_S48_4B },		/* 8 KHz */
		{ 10 }					/* volume */
	};
	
	
	/*************************************
	 *
	 *	Machine drivers
	 *
	 *************************************/
	
	static MACHINE_DRIVER_START( nekkyoku )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80,12000000/2)		/* 6.00 Mhz ? */
		MDRV_CPU_MEMORY(nekkyoku_readmem_main,nekkyoku_writemem_main)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_CPU_ADD(Z80,12000000/2)		/* 6.00 Mhz ? */
		MDRV_CPU_MEMORY(nekkyoku_readmem_sub,nekkyoku_writemem_sub)
		MDRV_CPU_PORTS(nekkyoku_readport_sub,nekkyoku_writeport_sub)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		MDRV_MACHINE_INIT(fromance)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 256)
		MDRV_VISIBLE_AREA(0, 352-1, 0, 240-1)
		MDRV_GFXDECODE(fromance_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(nekkyoku)
		MDRV_VIDEO_UPDATE(fromance)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
		MDRV_SOUND_ADD(MSM5205, msm5205_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( idolmj )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80,12000000/2)		/* 6.00 Mhz ? */
		MDRV_CPU_MEMORY(fromance_readmem_main,fromance_writemem_main)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_CPU_ADD(Z80,12000000/2)		/* 6.00 Mhz ? */
		MDRV_CPU_MEMORY(fromance_readmem_sub,fromance_writemem_sub)
		MDRV_CPU_PORTS(fromance_readport_sub,idolmj_writeport_sub)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		MDRV_MACHINE_INIT(fromance)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 256)
		MDRV_VISIBLE_AREA(0, 352-1, 0, 240-1)
		MDRV_GFXDECODE(fromance_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(2048)
	
		MDRV_VIDEO_START(fromance)
		MDRV_VIDEO_UPDATE(fromance)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
		MDRV_SOUND_ADD(MSM5205, msm5205_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( fromance )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80,12000000/2)		/* 6.00 Mhz ? */
		MDRV_CPU_MEMORY(fromance_readmem_main,fromance_writemem_main)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_CPU_ADD(Z80,12000000/2)		/* 6.00 Mhz ? */
		MDRV_CPU_MEMORY(fromance_readmem_sub,fromance_writemem_sub)
		MDRV_CPU_PORTS(fromance_readport_sub,fromance_writeport_sub)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		MDRV_MACHINE_INIT(fromance)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 256)
		MDRV_VISIBLE_AREA(0, 352-1, 0, 240-1)
		MDRV_GFXDECODE(fromance_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(2048)
	
		MDRV_VIDEO_START(fromance)
		MDRV_VIDEO_UPDATE(fromance)
	
		/* sound hardware */
		MDRV_SOUND_ADD(YM2413, ym2413_interface)
		MDRV_SOUND_ADD(MSM5205, fromance_msm5205_interface)
	MACHINE_DRIVER_END
	
	
	
	/*************************************
	 *
	 *	ROM definitions
	 *
	 *************************************/
	
	static RomLoadPtr rom_nekkyoku = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x010000, REGION_CPU1, 0 )
		ROM_LOAD( "1-ic1a.bin",  0x000000, 0x008000, CRC(bb52d959) SHA1(1dfeb108879978dbcc1398e64b26c36505bee6d0) )
		ROM_LOAD( "2-ic2a.bin",  0x008000, 0x008000, CRC(61848d8b) SHA1(72048c53e4364544ca8a79e213db9d02b7b4778f) )
	
		ROM_REGION( 0x210000, REGION_CPU2, 0 )
		ROM_LOAD( "3-ic3a.bin",  0x000000, 0x008000, CRC(a13da011) SHA1(601180f65ba42b7b1b6b058c0eccf88af1b430ca) )
		ROM_LOAD( "ic4a.bin",    0x010000, 0x080000, CRC(1cc4d31b) SHA1(6ea8ec3d6b3bbffbbab3460e9c5dae74195eafc6) )
		ROM_LOAD( "ic5a.bin",    0x090000, 0x080000, CRC(8b0945a1) SHA1(c52f77d817c687afa0cd93e7725cdf2021158a11) )
		ROM_LOAD( "ic6a.bin",    0x110000, 0x080000, CRC(d5615e1d) SHA1(ffee1a240045636db5d03345faadb9991b51f2c9) )
		ROM_LOAD( "4-ic7a.bin",  0x190000, 0x008000, CRC(e259cfbb) SHA1(d92a71e5f840b338aa2080a6b5872e23c7b6146f) )
	
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "ic11a.bin",   0x000000, 0x080000, CRC(2bc2b1d0) SHA1(1c516b058a640ca07b065f4c55959b63a11ca015) )
		ROM_LOAD( "ic12a.bin",   0x080000, 0x040000, CRC(cac93dc0) SHA1(e47f547ac83ac005e787a2c9f802101d251d4a66) )
		ROM_LOAD( "6-ic13a.bin", 0x0c0000, 0x008000, CRC(84830e34) SHA1(e3af89938b1e122909b13faf0022b6b60afb1f3f) )
		ROM_FILL(                0x0c8000, 0x038000, 0xff )
		ROM_FILL(                0x100000, 0x100000, 0xff )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "ic8a.bin",    0x000000, 0x080000, CRC(599790d8) SHA1(4e4ade1a89d6cb93b0808867883d70c4c7ed78dd) )
		ROM_LOAD( "ic9a.bin",    0x080000, 0x040000, CRC(78c1906f) SHA1(54459e0120ec58a962d3f4a1287e68d2fbb28be9) )
		ROM_LOAD( "5-ic10a.bin", 0x0c0000, 0x008000, CRC(2e78515f) SHA1(397985c082ffc0df07cd44d54e4fef909c30a4f1) )
		ROM_FILL(                0x0c8000, 0x038000, 0xff )
		ROM_FILL(                0x100000, 0x100000, 0xff )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_idolmj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x010000, REGION_CPU1, 0 )
		ROM_LOAD( "3-13g.bin", 0x000000, 0x008000, CRC(910e9e7a) SHA1(5d577549ca25def14fbc6db682afda105244b7c1) )
	
		ROM_REGION( 0x410000, REGION_CPU2, 0 )
		ROM_LOAD( "5-13e.bin", 0x000000, 0x008000, CRC(cda33264) SHA1(88ac345fccce82fafc346675726b1b806ecab1bc) )
		ROM_LOAD( "18e.bin",   0x010000, 0x080000, CRC(7ee5aaf3) SHA1(f1aaa512a475bfb56250ef4350d4b17e6165c5f7) )
		ROM_LOAD( "17e.bin",   0x090000, 0x080000, CRC(38055f94) SHA1(7724e099efda2a255ccc7989f2584fc543e77061) )
		ROM_LOAD( "4-14e.bin", 0x190000, 0x010000, CRC(84d80b43) SHA1(7d6310d313b953ed460a6891316845b64bffdd31) )
	
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "6e.bin",    0x000000, 0x080000, CRC(51dadedd) SHA1(ce0188cce457759130d70b71b2877a7eb98824e4) )
		ROM_LOAD( "2-8e.bin",  0x080000, 0x008000, CRC(a1a62c4c) SHA1(c946ad5dab918e0a7951c7eca88f1b32bf96e895) )
		ROM_FILL(              0x088000, 0x008000, 0xff )
		ROM_FILL(              0x090000, 0x170000, 0xff )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "3e.bin",    0x000000, 0x080000, CRC(eff9b562) SHA1(895d70be5efd746e15393bb138e6c6f7d39b2e54) )
		ROM_LOAD( "1-1e.bin",  0x080000, 0x008000, CRC(abf03c62) SHA1(0ad88ffe3f06f493978f292154894415ed38f797) )
		ROM_FILL(              0x088000, 0x008000, 0xff )
		ROM_FILL(              0x090000, 0x170000, 0xff )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_mjnatsu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x010000, REGION_CPU1, 0 )
		ROM_LOAD( "3-ic70.bin", 0x000000, 0x008000, CRC(543eb9e1) SHA1(cfe1d33bdf6541e2207465a941f342be21b69f7d) )
	
		ROM_REGION( 0x410000, REGION_CPU2, 0 )
		ROM_LOAD( "4-ic47.bin", 0x000000, 0x008000, CRC(27a61dc7) SHA1(e12e36eefb7cbce8e1383b7c1b57793cde2cd6cd) )
		ROM_LOAD( "ic87.bin",   0x010000, 0x080000, CRC(caec9310) SHA1(a92be0116d7e682763e387230a51daedbe749c26) )
		ROM_LOAD( "ic78.bin",   0x090000, 0x080000, CRC(2b291006) SHA1(5360158ee9e50bd956f149ce0e58920d4d294b42) )
		ROM_LOAD( "ic72.bin",   0x110000, 0x020000, CRC(42464fba) SHA1(b16da303f3e16f47322d00f7a27d2004be63f611) )
		ROM_LOAD( "5-ic48.bin", 0x210000, 0x010000, CRC(d3c06cd9) SHA1(53c10670557d72a87f2a289f22ea2749d8d35976) )
	
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "ic58.bin",   0x000000, 0x080000, CRC(257a8075) SHA1(c741a4f10cbec8e6d641d4dc2b36749277bc9548) )
		ROM_LOAD( "ic63.bin",   0x080000, 0x020000, CRC(b54c7d3a) SHA1(6be26b7d2cdf884fc140c8ed44865784d107583a) )
		ROM_LOAD( "1-ic74.bin", 0x0a0000, 0x008000, CRC(fbafa46b) SHA1(3867180f8a3cb3becf78023d3bbc2bb66580a13d) )
		ROM_FILL(               0x0a8000, 0x008000, 0xff )
		ROM_FILL(               0x0b0000, 0x150000, 0xff )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "ic59.bin",   0x000000, 0x080000, CRC(03983ac7) SHA1(1e14ec3c614b227a6362f2a3a7582ac7a4f58ee3) )
		ROM_LOAD( "ic64.bin",   0x080000, 0x040000, CRC(9bd8e855) SHA1(a445dd7634958f69e58aed2581513e7a583ff66b) )
		ROM_FILL(               0x0c0000, 0x140000, 0xff )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_natsuiro = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x010000, REGION_CPU1, 0 )
		ROM_LOAD( "3-ic70.bin", 0x000000, 0x008000, CRC(543eb9e1) SHA1(cfe1d33bdf6541e2207465a941f342be21b69f7d) )
	
		ROM_REGION( 0x410000, REGION_CPU2, 0 )
		ROM_LOAD( "m4.bin",     0x000000, 0x008000, CRC(8d37cc3f) SHA1(90fee63b5864002bec95c61003532e743d351244) )
		ROM_LOAD( "ic87.bin",   0x010000, 0x080000, CRC(caec9310) SHA1(a92be0116d7e682763e387230a51daedbe749c26) )
		ROM_LOAD( "ic78.bin",   0x090000, 0x080000, CRC(2b291006) SHA1(5360158ee9e50bd956f149ce0e58920d4d294b42) )
		ROM_LOAD( "ic72.bin",   0x110000, 0x020000, CRC(42464fba) SHA1(b16da303f3e16f47322d00f7a27d2004be63f611) )
		ROM_LOAD( "m5.bin",     0x210000, 0x010000, CRC(cd1ea5f7) SHA1(26965663485c7311c7a421dba0921fdfb8392df9) )
	
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "ic58.bin",   0x000000, 0x080000, CRC(257a8075) SHA1(c741a4f10cbec8e6d641d4dc2b36749277bc9548) )
		ROM_LOAD( "ic63.bin",   0x080000, 0x020000, CRC(b54c7d3a) SHA1(6be26b7d2cdf884fc140c8ed44865784d107583a) )
		ROM_LOAD( "m1.bin",     0x0a0000, 0x008000, CRC(fbafa46b) SHA1(3867180f8a3cb3becf78023d3bbc2bb66580a13d) )
		ROM_FILL(               0x0a8000, 0x008000, 0xff )
		ROM_FILL(               0x0b0000, 0x150000, 0xff )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "ic59.bin",   0x000000, 0x080000, CRC(03983ac7) SHA1(1e14ec3c614b227a6362f2a3a7582ac7a4f58ee3) )
		ROM_LOAD( "ic64.bin",   0x080000, 0x040000, CRC(9bd8e855) SHA1(a445dd7634958f69e58aed2581513e7a583ff66b) )
		ROM_LOAD( "m2.bin",     0x0c0000, 0x008000, CRC(61129677) SHA1(a1789b4e23b46c61c822eb02fdfa65b7d0a34ce8) )
		ROM_FILL(               0x0c8000, 0x138000, 0xff )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_mfunclub = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x010000, REGION_CPU1, 0 )
		ROM_LOAD( "3.70",        0x000000, 0x008000, CRC(e6f76ca3) SHA1(2f4292e50770c3325c1573781cb21940d73e8fb1) )
	
		ROM_REGION( 0x410000, REGION_CPU2, 0 )
		ROM_LOAD( "4.47",        0x000000, 0x008000, CRC(d71ee0e3) SHA1(87a5bc53bc2f027fc644427f34141826de9f48a5) )
		ROM_LOAD( "586.87",      0x010000, 0x080000, CRC(e197af4a) SHA1(e68530bf8dbf9cb0913b36879f1ec0756f202b4e) )
		ROM_LOAD( "587.78",      0x090000, 0x080000, CRC(08ff39c3) SHA1(d1f6011c9d08820eef3bb80abcf2fdb9acd9d967) )
		ROM_LOAD( "5.57",        0x290000, 0x010000, CRC(bf988bde) SHA1(6e965f26f7c72b4ede954bd9941db282dfe0e6a9) )
	
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "584.58",      0x000000, 0x080000, CRC(d65af431) SHA1(0006846631619af41bfd4a14563c16966ea1573b) )
		ROM_LOAD( "lh634a14.63", 0x080000, 0x080000, CRC(cdda9b9e) SHA1(ffb2ca1233d5e969abc9da2faa0252ba8e779b42) )
		ROM_FILL(                0x100000, 0x080000, 0xff )
		ROM_LOAD( "1.74",        0x180000, 0x008000, CRC(5b0b2efc) SHA1(4524d9e8b6c059e0cca31d9f0d677cb6bd09561d) )
		ROM_FILL(                0x188000, 0x078000, 0xff )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "585.59",      0x000000, 0x080000, CRC(58ce0937) SHA1(729e1b906454145b3d069717f2e058ab41750db1) )
		ROM_FILL(                0x080000, 0x100000, 0xff )
		ROM_LOAD( "2.75",        0x180000, 0x010000, CRC(4dd4f786) SHA1(06f398cffc54ac9e7c42eddc1d46e00c0a64d9c7) )
		ROM_FILL(                0x190000, 0x070000, 0xff )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_daiyogen = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x010000, REGION_CPU1, 0 )
		ROM_LOAD( "n1-ic70.bin", 0x000000, 0x008000, CRC(29af632b) SHA1(9a55cc7a82dc2735be6310de27521ff0f5c352bd) )
	
		ROM_REGION( 0x130000, REGION_CPU2, 0 )
		ROM_LOAD( "n2-ic47.bin", 0x000000, 0x008000, CRC(8896604c) SHA1(305a42c6c770645d6dcb5297325dbb3c7e337443) )
		ROM_LOAD( "ic87.bin",    0x010000, 0x080000, CRC(4f86ffe2) SHA1(159952b6be9800b117b70d992ef86d72fdac1802) )
		ROM_LOAD( "ic78.bin",    0x090000, 0x080000, CRC(ae52bccd) SHA1(591bf4736807b004a2576c220066985d3218e0a1) )
		ROM_LOAD( "7-ic72.bin",  0x110000, 0x020000, CRC(30279296) SHA1(6943fd16c1b87f5000c89c380091dc922866e5ac) )
	
		ROM_REGION( 0x0c0000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "ic58.bin",    0x000000, 0x080000, CRC(8cf3d5f5) SHA1(26470a9b02fd5017cbb64ceee00abb51c58b97d3) )
		ROM_LOAD( "ic63.bin",    0x080000, 0x040000, CRC(64611070) SHA1(203aa055c318a9bc6cd6f56a6fbd2ff12e4e4d5a) )
	
		ROM_REGION( 0x100000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "ic59.bin",    0x000000, 0x080000, CRC(715f2f8c) SHA1(a1206bc21556c2644688f6332b9a57c7188ea7fa) )
		ROM_LOAD( "ic64.bin",    0x080000, 0x080000, CRC(e5a41864) SHA1(73b25fe87e347feabe7256ced80a696c377d690c) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_nmsengen = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x010000, REGION_CPU1, 0 )
		ROM_LOAD( "3-ic70.bin",   0x000000, 0x008000, CRC(4e6edbbb) SHA1(890f93569a6dec6bf7c917a3db4f268c6ec64564) )
	
		ROM_REGION( 0x410000, REGION_CPU2, 0 )
		ROM_LOAD( "4-ic47.bin",   0x000000, 0x008000, CRC(d31c596e) SHA1(6063805b93c39054ad4804d6d476b8370c4249ca) )
		ROM_LOAD( "vsj-ic87.bin", 0x010000, 0x100000, CRC(d3e8bd73) SHA1(a8c8f3c589e561cb79ac586209369ae192507212) )
		ROM_LOAD( "j5-ic72.bin",  0x210000, 0x020000, CRC(db937253) SHA1(35fe382393e05911e7d801fff49b5427c6337ba3) )
	
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "vsk-ic63.bin", 0x000000, 0x100000, CRC(f95f9c67) SHA1(1826b52ad404ca44d79fa869aa5a974299861f40) )
		ROM_LOAD( "ic58.bin",     0x100000, 0x040000, CRC(c66dcf18) SHA1(a64f2fb478a8b3d08d0157059c9d4ab30d43c9bf) )
		ROM_FILL(                 0x140000, 0x080000, 0xff )
		ROM_LOAD( "1-ic68.bin",   0x1c0000, 0x020000, CRC(a944a8d6) SHA1(1dfaa34c6c406c7216aa989268a0d8e680f419e2) )
		ROM_FILL(                 0x1e0000, 0x020000, 0xff )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "vsh-ic64.bin", 0x000000, 0x100000, CRC(f546ffaf) SHA1(bb4babc2dffc36fb321d64cd4a28c3089f3ee366) )
		ROM_LOAD( "vsg-ic59.bin", 0x100000, 0x080000, CRC(25bae018) SHA1(ba00718d473d1fbaec8d18cc481ed146657ef14f) )
		ROM_LOAD( "ic69.bin",     0x180000, 0x040000, CRC(dc867ccd) SHA1(69745b9f6aecb92fea8e70b0cec4895dea444c04) )
		ROM_LOAD( "2-ic75.bin",   0x1c0000, 0x020000, CRC(e2fad82e) SHA1(3ab41e8e7674f8d12b0a0f518c5a54af8afa1231) )
		ROM_FILL(                 0x1e0000, 0x020000, 0xff )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_fromance = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x010000, REGION_CPU1, 0 )
		ROM_LOAD( "2-ic70.bin", 0x000000, 0x008000, CRC(a0866e26) SHA1(019a8dfaa54dd397f642622d7ed847b7147a61f7) )
	
		ROM_REGION( 0x410000, REGION_CPU2, 0 )
		ROM_LOAD( "1-ic47.bin", 0x000000, 0x008000, CRC(ac859917) SHA1(86808c06fba4a3320298d38815e8e5d32ad29182) )
		ROM_LOAD( "ic87.bin",   0x010000, 0x100000, CRC(bb0d224e) SHA1(020a5b09f58f1c18cae89c8da019562ebed16710) )
		ROM_LOAD( "ic78.bin",   0x110000, 0x040000, CRC(ba2dba83) SHA1(ca744b4ddd2200d29a5d4efe6a3378d7cfe28a36) )
		ROM_LOAD( "5-ic72.bin", 0x210000, 0x020000, CRC(377cd57c) SHA1(7e81ca5b898f5ffe24068be1e7cd0a57a3ca68b8) )
	
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE )
		ROM_LOAD( "ic63.bin",   0x000000, 0x100000, CRC(faa9cdf3) SHA1(194dfaf04dc960fa1d575ad494a333cc7e7d221a) )
		ROM_FILL(               0x100000, 0x0c0000, 0xff )
		ROM_LOAD( "4-ic68.bin", 0x1c0000, 0x020000, CRC(9b35cea3) SHA1(e99df84c57e6d473681b7622ad6a28fce63f1128) )
		ROM_FILL(               0x1e0000, 0x020000, 0xff )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE )
		ROM_LOAD( "ic64.bin",   0x000000, 0x100000, CRC(23b9a484) SHA1(4299b689eb1da3d7e69c604eb28651ebfd4dad09) )
		ROM_FILL(               0x100000, 0x080000, 0xff )
		ROM_LOAD( "ic69.bin",   0x180000, 0x040000, CRC(d06a0fc0) SHA1(ea645b77f671ad71049e4db547b2c4a22e12d6c3) )
		ROM_LOAD( "3-ic75.bin", 0x1c0000, 0x020000, CRC(bb314e78) SHA1(58ce4f9c94e45b05543289643b848fbdcb9ab473) )
		ROM_FILL(               0x1e0000, 0x020000, 0xff )
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Game drivers
	 *
	 *************************************/
	
	GAMEX(1988, nekkyoku,        0, nekkyoku, nekkyoku, 0, ROT0, "Video System Co.", "Rettou Juudan Nekkyoku Janshi - Higashi Nippon Hen (Japan)", GAME_IMPERFECT_GRAPHICS )
	GAME( 1988, idolmj,          0, idolmj,   idolmj,   0, ROT0, "System Service", "Idol-Mahjong Housoukyoku (Japan)" )
	GAME( 1989, mjnatsu,         0, fromance, mjnatsu,  0, ROT0, "Video System Co.", "Mahjong Natsu Monogatari (Japan)" )
	GAME( 1989, natsuiro,  mjnatsu, fromance, mjnatsu,  0, ROT0, "Video System Co.", "Natsuiro Mahjong (Japan)" )
	GAME( 1989, mfunclub,        0, fromance, mfunclub, 0, ROT0, "Video System Co.", "Mahjong Fun Club - Idol Saizensen (Japan)" )
	GAME( 1990, daiyogen,        0, fromance, daiyogen, 0, ROT0, "Video System Co.", "Mahjong Daiyogen (Japan)" )
	GAME( 1991, nmsengen,        0, fromance, nmsengen, 0, ROT0, "Video System Co.", "Nekketsu Mahjong Sengen! AFTER 5 (Japan)" )
	GAME( 1991, fromance,        0, fromance, fromance, 0, ROT0, "Video System Co.", "Idol-Mahjong Final Romance (Japan)" )
}
