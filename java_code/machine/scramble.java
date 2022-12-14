/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.machine;

public class scramble
{
	
	
	void cclimber_decode(const unsigned char xortable[8][16]);
	
	
	
	static int irq_line;
	
	static void galaxian_7474_9M_2_callback(void)
	{
		/* Q bar clocks the other flip-flop,
		   Q is VBLANK (not visible to the CPU) */
		TTL7474_clock_w(1, TTL7474_output_comp_r(0));
		TTL7474_update(1);
	}
	
	static void galaxian_7474_9M_1_callback(void)
	{
		/* Q goes to the NMI line */
		cpu_set_irq_line(0, irq_line, TTL7474_output_r(1) ? CLEAR_LINE : ASSERT_LINE);
	}
	
	static const struct TTL7474_interface galaxian_7474_9M_2_intf =
	{
		galaxian_7474_9M_2_callback
	};
	
	static const struct TTL7474_interface galaxian_7474_9M_1_intf =
	{
		galaxian_7474_9M_1_callback
	};
	
	
	public static WriteHandlerPtr galaxian_nmi_enable_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		TTL7474_preset_w(1, data);
		TTL7474_update(1);
	} };
	
	
	static void interrupt_timer(int param)
	{
		/* 128V, 64V and 32V go to D */
		TTL7474_d_w(0, (param & 0xe0) != 0xe0);
	
		/* 16V clocks the flip-flop */
		TTL7474_clock_w(0, param & 0x10);
	
		param = (param + 0x10) & 0xff;
	
		timer_set(cpu_getscanlinetime(param), param, interrupt_timer);
	
		TTL7474_update(0);
	}
	
	
	static void machine_init_common( int line )
	{
		irq_line = line;
	
		/* initalize main CPU interrupt generator flip-flops */
		TTL7474_config(0, &galaxian_7474_9M_2_intf);
		TTL7474_preset_w(0, 1);
		TTL7474_clear_w (0, 1);
	
		TTL7474_config(1, &galaxian_7474_9M_1_intf);
		TTL7474_clear_w (1, 1);
		TTL7474_d_w     (1, 0);
		TTL7474_preset_w(1, 0);
	
		/* start a timer to generate interrupts */
		timer_set(cpu_getscanlinetime(0), 0, interrupt_timer);
	}
	
	public static MachineInitHandlerPtr machine_init_galaxian  = new MachineInitHandlerPtr() { public void handler(){
		machine_init_common(IRQ_LINE_NMI);
	} };
	
	public static MachineInitHandlerPtr machine_init_devilfsg  = new MachineInitHandlerPtr() { public void handler(){
		machine_init_common(0);
	} };
	
	public static MachineInitHandlerPtr machine_init_scramble  = new MachineInitHandlerPtr() { public void handler(){
		machine_init_galaxian();
	
		if (cpu_gettotalcpu() > 1)
		{
			scramble_sh_init();
		}
	} };
	
	public static MachineInitHandlerPtr machine_init_sfx  = new MachineInitHandlerPtr() { public void handler(){
		machine_init_scramble();
	
		sfx_sh_init();
	} };
	
	public static MachineInitHandlerPtr machine_init_explorer  = new MachineInitHandlerPtr() { public void handler(){
		UINT8 *RAM = memory_region(REGION_CPU1);
		RAM[0x47ff] = 0; /* If not set, it doesn't reset after the 1st time */
	
		machine_init_scramble();
	} };
	
	public static WriteHandlerPtr galaxian_coin_lockout_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		coin_lockout_global_w(~data & 1);
	} };
	
	
	public static WriteHandlerPtr galaxian_coin_counter_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		coin_counter_w(offset, data & 0x01);
	} };
	
	public static WriteHandlerPtr galaxian_coin_counter_1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		coin_counter_w(1, data & 0x01);
	} };
	
	public static WriteHandlerPtr galaxian_coin_counter_2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		coin_counter_w(2, data & 0x01);
	} };
	
	
	public static WriteHandlerPtr galaxian_leds_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		set_led_status(offset,data & 1);
	} };
	
	
	public static ReadHandlerPtr scrambls_input_port_2_r  = new ReadHandlerPtr() { public int handler(int offset){
		data8_t res;
	
	
		res = readinputport(2);
	
	/*logerror("%04x: read IN2\n",activecpu_get_pc());*/
	
		/* avoid protection */
		if (activecpu_get_pc() == 0x00e4) res &= 0x7f;
	
		return res;
	} };
	
	public static ReadHandlerPtr ckongs_input_port_1_r  = new ReadHandlerPtr() { public int handler(int offset){
		return (readinputport(1) & 0xfc) | ((readinputport(2) & 0x06) >> 1);
	} };
	
	public static ReadHandlerPtr ckongs_input_port_2_r  = new ReadHandlerPtr() { public int handler(int offset){
		return (readinputport(2) & 0xf9) | ((readinputport(1) & 0x03) << 1);
	} };
	
	
	static data8_t moonwar_port_select;
	
	public static WriteHandlerPtr moonwar_port_select_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		moonwar_port_select = data & 0x10;
	} };
	
	public static ReadHandlerPtr moonwar_input_port_0_r  = new ReadHandlerPtr() { public int handler(int offset){
		data8_t sign;
		data8_t delta;
	
		delta = (moonwar_port_select ? readinputport(3) : readinputport(4));
	
		sign = (delta & 0x80) >> 3;
		delta &= 0x0f;
	
		return ((readinputport(0) & 0xe0) | delta | sign );
	} };
	
	
	/* the coinage DIPs are spread accross two input ports */
	public static ReadHandlerPtr stratgyx_input_port_2_r  = new ReadHandlerPtr() { public int handler(int offset){
		return (readinputport(2) & ~0x06) | ((readinputport(4) << 1) & 0x06);
	} };
	
	public static ReadHandlerPtr stratgyx_input_port_3_r  = new ReadHandlerPtr() { public int handler(int offset){
		return (readinputport(3) & ~0x03) | ((readinputport(4) >> 2) & 0x03);
	} };
	
	
	public static ReadHandlerPtr darkplnt_input_port_1_r  = new ReadHandlerPtr() { public int handler(int offset){
		static data8_t remap[] = {0x03, 0x02, 0x00, 0x01, 0x21, 0x20, 0x22, 0x23,
								  0x33, 0x32, 0x30, 0x31, 0x11, 0x10, 0x12, 0x13,
								  0x17, 0x16, 0x14, 0x15, 0x35, 0x34, 0x36, 0x37,
								  0x3f, 0x3e, 0x3c, 0x3d, 0x1d, 0x1c, 0x1e, 0x1f,
								  0x1b, 0x1a, 0x18, 0x19, 0x39, 0x38, 0x3a, 0x3b,
								  0x2b, 0x2a, 0x28, 0x29, 0x09, 0x08, 0x0a, 0x0b,
								  0x0f, 0x0e, 0x0c, 0x0d, 0x2d, 0x2c, 0x2e, 0x2f,
								  0x27, 0x26, 0x24, 0x25, 0x05, 0x04, 0x06, 0x07 };
		data8_t val;
	
		val = readinputport(1);
	
		return ((val & 0x03) | (remap[val >> 2] << 2));
	} };
	
	
	
	public static WriteHandlerPtr scramble_protection_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* nothing to do yet */
	} };
	
	public static ReadHandlerPtr scramble_protection_r  = new ReadHandlerPtr() { public int handler(int offset){
		switch (activecpu_get_pc())
		{
		case 0x00a8: return 0xf0;
		case 0x00be: return 0xb0;
		case 0x0c1d: return 0xf0;
		case 0x0c6a: return 0xb0;
		case 0x0ceb: return 0x40;
		case 0x0d37: return 0x60;
		case 0x1ca2: return 0x00;  /* I don't think it's checked */
		case 0x1d7e: return 0xb0;
		default:
			logerror("%04x: read protection\n",activecpu_get_pc());
			return 0;
		}
	} };
	
	public static ReadHandlerPtr scrambls_protection_r  = new ReadHandlerPtr() { public int handler(int offset){
		logerror("%04x: read protection\n",activecpu_get_pc());
	
		return 0x6f;
	} };
	
	
	public static ReadHandlerPtr scramblb_protection_1_r  = new ReadHandlerPtr() { public int handler(int offset){
		switch (activecpu_get_pc())
		{
		case 0x01da: return 0x80;
		case 0x01e4: return 0x00;
		default:
			logerror("%04x: read protection 1\n",activecpu_get_pc());
			return 0;
		}
	} };
	
	public static ReadHandlerPtr scramblb_protection_2_r  = new ReadHandlerPtr() { public int handler(int offset){
		switch (activecpu_get_pc())
		{
		case 0x01ca: return 0x90;
		default:
			logerror("%04x: read protection 2\n",activecpu_get_pc());
			return 0;
		}
	} };
	
	
	public static ReadHandlerPtr jumpbug_protection_r  = new ReadHandlerPtr() { public int handler(int offset){
		switch (offset)
		{
		case 0x0114:  return 0x4f;
		case 0x0118:  return 0xd3;
		case 0x0214:  return 0xcf;
		case 0x0235:  return 0x02;
		case 0x0311:  return 0x00;  /* not checked */
		default:
			logerror("Unknown protection read. Offset: %04X  PC=%04X\n",0xb000+offset,activecpu_get_pc());
		}
	
		return 0;
	} };
	
	
	public static WriteHandlerPtr theend_coin_counter_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		coin_counter_w(0, data & 0x80);
	} };
	
	
	public static ReadHandlerPtr mariner_protection_1_r  = new ReadHandlerPtr() { public int handler(int offset){
		return 7;
	} };
	
	public static ReadHandlerPtr mariner_protection_2_r  = new ReadHandlerPtr() { public int handler(int offset){
		return 3;
	} };
	
	
	public static ReadHandlerPtr triplep_pip_r  = new ReadHandlerPtr() { public int handler(int offset){
		logerror("PC %04x: triplep read port 2\n",activecpu_get_pc());
		if (activecpu_get_pc() == 0x015a) return 0xff;
		else if (activecpu_get_pc() == 0x0886) return 0x05;
		else return 0;
	} };
	
	public static ReadHandlerPtr triplep_pap_r  = new ReadHandlerPtr() { public int handler(int offset){
		logerror("PC %04x: triplep read port 3\n",activecpu_get_pc());
		if (activecpu_get_pc() == 0x015d) return 0x04;
		else return 0;
	} };
	
	
	public static ReadHandlerPtr checkmaj_protection_r  = new ReadHandlerPtr() { public int handler(int offset){
		switch (activecpu_get_pc())
		{
		case 0x0f15:  return 0xf5;
		case 0x0f8f:  return 0x7c;
		case 0x10b3:  return 0x7c;
		case 0x10e0:  return 0x00;
		case 0x10f1:  return 0xaa;
		case 0x1402:  return 0xaa;
		default:
			logerror("Unknown protection read. PC=%04X\n",activecpu_get_pc());
		}
	
		return 0;
	} };
	
	
	/* Zig Zag can swap ROMs 2 and 3 as a form of copy protection */
	public static WriteHandlerPtr zigzag_sillyprotection_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		data8_t *RAM = memory_region(REGION_CPU1);
	
	
		if (data)
		{
			/* swap ROM 2 and 3! */
			cpu_setbank(1,&RAM[0x3000]);
			cpu_setbank(2,&RAM[0x2000]);
		}
		else
		{
			cpu_setbank(1,&RAM[0x2000]);
			cpu_setbank(2,&RAM[0x3000]);
		}
	} };
	
	
	public static ReadHandlerPtr dingo_3000_r  = new ReadHandlerPtr() { public int handler(int offset){
		return 0xaa;
	} };
	
	public static ReadHandlerPtr dingo_3035_r  = new ReadHandlerPtr() { public int handler(int offset){
		return 0x8c;
	} };
	
	
	static int kingball_speech_dip;
	
	/* Hack? If $b003 is high, we'll check our "fake" speech dipswitch (marked as SLAM) */
	public static ReadHandlerPtr kingball_IN0_r  = new ReadHandlerPtr() { public int handler(int offset){
		if (kingball_speech_dip)
			return (readinputport(0) & ~0x40) | ((readinputport(3) & 0x01) << 6);
		else
			return readinputport(0);
	} };
	
	public static ReadHandlerPtr kingball_IN1_r  = new ReadHandlerPtr() { public int handler(int offset){
		/* bit 5 is the NOISE line from the sound circuit.  The code just verifies
		   that it's working, doesn't actually use return value, so we can just use
		   rand() */
	
		return (readinputport(1) & ~0x20) | (rand() & 0x20);
	} };
	
	public static WriteHandlerPtr kingball_speech_dip_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		kingball_speech_dip = data;
	} };
	
	static int kingball_sound;
	
	public static WriteHandlerPtr kingball_sound1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		kingball_sound = (kingball_sound & ~0x01) | data;
	} };
	
	public static WriteHandlerPtr kingball_sound2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		kingball_sound = (kingball_sound & ~0x02) | (data << 1);
		soundlatch_w.handler (0, kingball_sound | 0xf0);
	} };
	
	
	
	public static ReadHandlerPtr azurian_IN1_r  = new ReadHandlerPtr() { public int handler(int offset){
		return (readinputport(1) & ~0x40) | ((readinputport(3) & 0x01) << 6);
	} };
	
	public static ReadHandlerPtr azurian_IN2_r  = new ReadHandlerPtr() { public int handler(int offset){
		return (readinputport(2) & ~0x04) | ((readinputport(3) & 0x02) << 1);
	} };
	
	
	static int _4in1_bank;
	
	public static WriteHandlerPtr _4in1_bank_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* games are banked at 0x0000 - 0x3fff */
		offs_t bankaddress;
		data8_t *RAM=memory_region(REGION_CPU1);
	
		_4in1_bank = data & 0x03;
	
		bankaddress = (_4in1_bank * 0x4000) + 0x10000;
		cpu_setbank(1, &RAM[bankaddress]);
	
		galaxian_gfxbank_w(0, _4in1_bank);
	} };
	
	public static ReadHandlerPtr _4in1_input_port_1_r  = new ReadHandlerPtr() { public int handler(int offset){
		return (readinputport(1) & ~0xc0) | (readinputport(3+_4in1_bank) & 0xc0);
	} };
	
	public static ReadHandlerPtr _4in1_input_port_2_r  = new ReadHandlerPtr() { public int handler(int offset){
		return (readinputport(2) & 0x04) | (readinputport(3+_4in1_bank) & ~0xc4);
	} };
	
	
	static int gmgalax_selected_game;
	
	static void gmgalax_select_game(int game)
	{
		/* games are banked at 0x0000 - 0x3fff */
		offs_t bankaddress;
		data8_t *RAM=memory_region(REGION_CPU1);
	
		gmgalax_selected_game = game;
	
		bankaddress = (gmgalax_selected_game * 0x4000) + 0x10000;
		cpu_setbank(1, &RAM[bankaddress]);
	
		galaxian_gfxbank_w(0, gmgalax_selected_game);
	}
	
	public static ReadHandlerPtr gmgalax_input_port_0_r  = new ReadHandlerPtr() { public int handler(int offset){
		return readinputport(gmgalax_selected_game ? 3 : 0);
	} };
	
	public static ReadHandlerPtr gmgalax_input_port_1_r  = new ReadHandlerPtr() { public int handler(int offset){
		return readinputport(gmgalax_selected_game ? 4 : 1);
	} };
	
	public static ReadHandlerPtr gmgalax_input_port_2_r  = new ReadHandlerPtr() { public int handler(int offset){
		return readinputport(gmgalax_selected_game ? 5 : 2);
	} };
	
	
	static void cavelon_banksw(void)
	{
		/* any read/write access in the 0x8000-0xffff region causes a bank switch.
		   Only the lower 0x2000 is switched but we switch the whole region
		   to keep the CPU core happy at the boundaries */
	
		static int cavelon_bank;
	
		UINT8 *ROM = memory_region(REGION_CPU1);
	
		if (cavelon_bank)
		{
			cavelon_bank = 0;
			cpu_setbank(1, &ROM[0x0000]);
		}
		else
		{
			cavelon_bank = 1;
			cpu_setbank(1, &ROM[0x10000]);
		}
	}
	
	public static ReadHandlerPtr cavelon_banksw_r  = new ReadHandlerPtr() { public int handler(int offset){
		cavelon_banksw();
	
		if      ((offset >= 0x0100) && (offset <= 0x0103))
			return ppi8255_0_r(offset - 0x0100);
		else if ((offset >= 0x0200) && (offset <= 0x0203))
			return ppi8255_1_r(offset - 0x0200);
	
		return 0xff;
	} };
	
	public static WriteHandlerPtr cavelon_banksw_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		cavelon_banksw();
	
		if      ((offset >= 0x0100) && (offset <= 0x0103))
			ppi8255_0_w(offset - 0x0100, data);
		else if ((offset >= 0x0200) && (offset <= 0x0203))
			ppi8255_1_w(offset - 0x0200, data);
	} };
	
	
	public static ReadHandlerPtr hunchbks_mirror_r  = new ReadHandlerPtr() { public int handler(int offset){
		return cpu_readmem16(0x1000+offset);
	} };
	
	public static WriteHandlerPtr hunchbks_mirror_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		cpu_writemem16(0x1000+offset,data);
	} };
	
	
	public static ReadHandlerPtr frogger_ppi8255_0_r  = new ReadHandlerPtr() { public int handler(int offset){
		return ppi8255_0_r(offset >> 1);
	} };
	
	public static ReadHandlerPtr frogger_ppi8255_1_r  = new ReadHandlerPtr() { public int handler(int offset){
		return ppi8255_1_r(offset >> 1);
	} };
	
	public static WriteHandlerPtr frogger_ppi8255_0_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		ppi8255_0_w(offset >> 1, data);
	} };
	
	public static WriteHandlerPtr frogger_ppi8255_1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		ppi8255_1_w(offset >> 1, data);
	} };
	
	
	public static ReadHandlerPtr scobra_type2_ppi8255_0_r  = new ReadHandlerPtr() { public int handler(int offset){
		return ppi8255_0_r(offset >> 2);
	} };
	
	public static ReadHandlerPtr scobra_type2_ppi8255_1_r  = new ReadHandlerPtr() { public int handler(int offset){
		return ppi8255_1_r(offset >> 2);
	} };
	
	public static WriteHandlerPtr scobra_type2_ppi8255_0_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		ppi8255_0_w(offset >> 2, data);
	} };
	
	public static WriteHandlerPtr scobra_type2_ppi8255_1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		ppi8255_1_w(offset >> 2, data);
	} };
	
	
	public static ReadHandlerPtr hustler_ppi8255_0_r  = new ReadHandlerPtr() { public int handler(int offset){
		return ppi8255_0_r(offset >> 3);
	} };
	
	public static ReadHandlerPtr hustler_ppi8255_1_r  = new ReadHandlerPtr() { public int handler(int offset){
		return ppi8255_1_r(offset >> 3);
	} };
	
	public static WriteHandlerPtr hustler_ppi8255_0_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		ppi8255_0_w(offset >> 3, data);
	} };
	
	public static WriteHandlerPtr hustler_ppi8255_1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		ppi8255_1_w(offset >> 3, data);
	} };
	
	
	public static ReadHandlerPtr amidar_ppi8255_0_r  = new ReadHandlerPtr() { public int handler(int offset){
		return ppi8255_0_r(offset >> 4);
	} };
	
	public static ReadHandlerPtr amidar_ppi8255_1_r  = new ReadHandlerPtr() { public int handler(int offset){
		return ppi8255_1_r(offset >> 4);
	} };
	
	public static WriteHandlerPtr amidar_ppi8255_0_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		ppi8255_0_w(offset >> 4, data);
	} };
	
	public static WriteHandlerPtr amidar_ppi8255_1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		ppi8255_1_w(offset >> 4, data);
	} };
	
	
	public static ReadHandlerPtr mars_ppi8255_0_r  = new ReadHandlerPtr() { public int handler(int offset){
		return ppi8255_0_r(((offset >> 2) & 0x02) | ((offset >> 1) & 0x01));
	} };
	
	public static ReadHandlerPtr mars_ppi8255_1_r  = new ReadHandlerPtr() { public int handler(int offset){
		return ppi8255_1_r(((offset >> 2) & 0x02) | ((offset >> 1) & 0x01));
	} };
	
	public static WriteHandlerPtr mars_ppi8255_0_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		ppi8255_0_w(((offset >> 2) & 0x02) | ((offset >> 1) & 0x01), data);
	} };
	
	public static WriteHandlerPtr mars_ppi8255_1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		ppi8255_1_w(((offset >> 2) & 0x02) | ((offset >> 1) & 0x01), data);
	} };
	
	
	static ppi8255_interface ppi8255_intf =
	{
		2, 								/* 2 chips */
		{input_port_0_r, 0},			/* Port A read */
		{input_port_1_r, 0},			/* Port B read */
		{input_port_2_r, 0},			/* Port C read */
		{0, soundlatch_w},				/* Port A write */
		{0, scramble_sh_irqtrigger_w},	/* Port B write */
		{0, 0}, 						/* Port C write */
	};
	
	/* extra chip for sample latch */
	static ppi8255_interface sfx_ppi8255_intf =
	{
		3, 									/* 3 chips */
		{input_port_0_r, 0, soundlatch2_r},	/* Port A read */
		{input_port_1_r, 0, 0},				/* Port B read */
		{input_port_2_r, 0, 0},				/* Port C read */
		{0, soundlatch_w, 0},				/* Port A write */
		{0, scramble_sh_irqtrigger_w, 0},	/* Port B write */
		{0, 0, 0}, 							/* Port C write */
	};
	
	
	public static DriverInitHandlerPtr init_pisces  = new DriverInitHandlerPtr() { public void handler(){
		/* the coin lockout was replaced */
		install_mem_write_handler(0, 0x6002, 0x6002, galaxian_gfxbank_w);
	} };
	
	public static DriverInitHandlerPtr init_checkmaj  = new DriverInitHandlerPtr() { public void handler(){
		/* for the title screen */
		install_mem_read_handler(0, 0x3800, 0x3800, checkmaj_protection_r);
	} };
	
	public static DriverInitHandlerPtr init_dingo  = new DriverInitHandlerPtr() { public void handler(){
		install_mem_read_handler(0, 0x3000, 0x3000, dingo_3000_r);
		install_mem_read_handler(0, 0x3035, 0x3035, dingo_3035_r);
	} };
	
	public static DriverInitHandlerPtr init_kingball  = new DriverInitHandlerPtr() { public void handler(){
		install_mem_read_handler(0, 0xa000, 0xa000, kingball_IN0_r);
		install_mem_read_handler(0, 0xa800, 0xa800, kingball_IN1_r);
	} };
	
	
	static data8_t decode_mooncrst(data8_t data,offs_t addr)
	{
		data8_t res;
	
		res = data;
		if (BIT(data,1)) res ^= 0x40;
		if (BIT(data,5)) res ^= 0x04;
		if ((addr & 1) == 0)
			res = (res & 0xbb) | (BIT(res,6) << 2) | (BIT(res,2) << 6);
		return res;
	}
	
	public static DriverInitHandlerPtr init_mooncrsu  = new DriverInitHandlerPtr() { public void handler(){
		install_mem_write_handler(0, 0xa000, 0xa002, galaxian_gfxbank_w);
	} };
	
	public static DriverInitHandlerPtr init_mooncrst  = new DriverInitHandlerPtr() { public void handler(){
		offs_t i;
		data8_t *rom = memory_region(REGION_CPU1);
	
	
		for (i = 0;i < memory_region_length(REGION_CPU1);i++)
			rom[i] = decode_mooncrst(rom[i],i);
	
		init_mooncrsu();
	} };
	
	public static DriverInitHandlerPtr init_mooncrgx  = new DriverInitHandlerPtr() { public void handler(){
		install_mem_write_handler(0, 0x6000, 0x6002, galaxian_gfxbank_w);
	} };
	
	public static DriverInitHandlerPtr init_moonqsr  = new DriverInitHandlerPtr() { public void handler(){
		offs_t i;
		data8_t *rom = memory_region(REGION_CPU1);
		offs_t diff = memory_region_length(REGION_CPU1) / 2;
	
	
		memory_set_opcode_base(0,rom+diff);
	
		for (i = 0;i < diff;i++)
			rom[i + diff] = decode_mooncrst(rom[i],i);
	} };
	
	public static DriverInitHandlerPtr init_checkman  = new DriverInitHandlerPtr() { public void handler(){
	/*
	                     Encryption Table
	                     ----------------
	+---+---+---+------+------+------+------+------+------+------+------+
	|A2 |A1 |A0 |D7    |D6    |D5    |D4    |D3    |D2    |D1    |D0    |
	+---+---+---+------+------+------+------+------+------+------+------+
	| 0 | 0 | 0 |D7    |D6    |D5    |D4    |D3    |D2    |D1    |D0^^D6|
	| 0 | 0 | 1 |D7    |D6    |D5    |D4    |D3    |D2    |D1^^D5|D0    |
	| 0 | 1 | 0 |D7    |D6    |D5    |D4    |D3    |D2^^D4|D1^^D6|D0    |
	| 0 | 1 | 1 |D7    |D6    |D5    |D4^^D2|D3    |D2    |D1    |D0^^D5|
	| 1 | 0 | 0 |D7    |D6^^D4|D5^^D1|D4    |D3    |D2    |D1    |D0    |
	| 1 | 0 | 1 |D7    |D6^^D0|D5^^D2|D4    |D3    |D2    |D1    |D0    |
	| 1 | 1 | 0 |D7    |D6    |D5    |D4    |D3    |D2^^D0|D1    |D0    |
	| 1 | 1 | 1 |D7    |D6    |D5    |D4^^D1|D3    |D2    |D1    |D0    |
	+---+---+---+------+------+------+------+------+------+------+------+
	
	For example if A2=1, A1=1 and A0=0 then D2 to the CPU would be an XOR of
	D2 and D0 from the ROM's. Note that D7 and D3 are not encrypted.
	
	Encryption PAL 16L8 on cardridge
	         +--- ---+
	    OE --|   U   |-- VCC
	 ROMD0 --|       |-- D0
	 ROMD1 --|       |-- D1
	 ROMD2 --|VER 5.2|-- D2
	    A0 --|       |-- NOT USED
	    A1 --|       |-- A2
	 ROMD4 --|       |-- D4
	 ROMD5 --|       |-- D5
	 ROMD6 --|       |-- D6
	   GND --|       |-- M1 (NOT USED)
	         +-------+
	Pin layout is such that links can replace the PAL if encryption is not used.
	
	*/
		static const UINT8 xortable[8][4] =
		{
			{ 6,0,6,0 },
			{ 5,1,5,1 },
			{ 4,2,6,1 },
			{ 2,4,5,0 },
			{ 4,6,1,5 },
			{ 0,6,2,5 },
			{ 0,2,0,2 },
			{ 1,4,1,4 }
		};
	
		offs_t i;
		data8_t *rom = memory_region(REGION_CPU1);
	
	
		for (i = 0; i < memory_region_length(REGION_CPU1); i++)
		{
			UINT8 data_xor;
			int line = i & 0x07;
	
			data_xor = (BIT(rom[i],xortable[line][0]) << xortable[line][1]) |
					   (BIT(rom[i],xortable[line][2]) << xortable[line][3]);
	
			rom[i] ^= data_xor;
		}
	} };
	
	public static DriverInitHandlerPtr init_gteikob2  = new DriverInitHandlerPtr() { public void handler(){
		init_pisces();
	
		install_mem_write_handler(0, 0x7006, 0x7006, gteikob2_flip_screen_x_w);
		install_mem_write_handler(0, 0x7007, 0x7007, gteikob2_flip_screen_y_w);
	} };
	
	public static DriverInitHandlerPtr init_azurian  = new DriverInitHandlerPtr() { public void handler(){
		init_pisces();
	
		install_mem_read_handler(0, 0x6800, 0x6800, azurian_IN1_r);
		install_mem_read_handler(0, 0x7000, 0x7000, azurian_IN2_r);
	} };
	
	public static DriverInitHandlerPtr init_4in1  = new DriverInitHandlerPtr() { public void handler(){
		offs_t i;
		data8_t *RAM = memory_region(REGION_CPU1);
	
		/* Decrypt Program Roms */
		for (i = 0; i < memory_region_length(REGION_CPU1); i++)
			RAM[i] = RAM[i] ^ (i & 0xff);
	
		_4in1_bank_w(0, 0); /* set the initial CPU bank */
	} };
	
	public static DriverInitHandlerPtr init_mshuttle  = new DriverInitHandlerPtr() { public void handler(){
		static const UINT8 convtable[8][16] =
		{
			/* -1 marks spots which are unused and therefore unknown */
			{ 0x40,0x41,0x44,0x15,0x05,0x51,0x54,0x55,0x50,0x00,0x01,0x04,  -1,0x10,0x11,0x14 },
			{ 0x45,0x51,0x55,0x44,0x40,0x11,0x05,0x41,0x10,0x14,0x54,0x50,0x15,0x04,0x00,0x01 },
			{ 0x11,0x14,0x10,0x00,0x44,0x05,  -1,0x04,0x45,0x15,0x55,0x50,  -1,0x01,0x54,0x51 },
			{ 0x14,0x01,0x11,0x10,0x50,0x15,0x00,0x40,0x04,0x51,0x45,0x05,0x55,0x54,  -1,0x44 },
			{ 0x04,0x10,  -1,0x40,0x15,0x41,0x50,0x50,0x11,  -1,0x14,0x00,0x51,0x45,0x55,0x01 },
			{ 0x44,0x45,0x00,0x51,  -1,  -1,0x15,0x11,0x01,0x10,0x04,0x55,0x05,0x40,0x50,0x41 },
			{ 0x51,0x00,0x01,0x05,0x04,0x55,0x54,0x50,0x41,  -1,0x11,0x15,0x14,0x10,0x44,0x40 },
			{ 0x05,0x04,0x51,0x01,  -1,  -1,0x55,  -1,0x00,0x50,0x15,0x14,0x44,0x41,0x40,0x54 },
		};
	
		cclimber_decode(convtable);
	} };
	
	public static DriverInitHandlerPtr init_scramble_ppi  = new DriverInitHandlerPtr() { public void handler(){
		ppi8255_init(&ppi8255_intf);
	} };
	
	public static DriverInitHandlerPtr init_scobra  = new DriverInitHandlerPtr() { public void handler(){
		init_scramble_ppi();
	
		install_mem_write_handler(0, 0xa803, 0xa803, scramble_background_enable_w);
	} };
	
	public static DriverInitHandlerPtr init_atlantis  = new DriverInitHandlerPtr() { public void handler(){
		init_scramble_ppi();
	
		install_mem_write_handler(0, 0x6803, 0x6803, scramble_background_enable_w);
	} };
	
	public static DriverInitHandlerPtr init_scramble  = new DriverInitHandlerPtr() { public void handler(){
		init_atlantis();
	
		ppi8255_set_portCread (1, scramble_protection_r);
		ppi8255_set_portCwrite(1, scramble_protection_w);
	} };
	
	public static DriverInitHandlerPtr init_scrambls  = new DriverInitHandlerPtr() { public void handler(){
		init_atlantis();
	
		ppi8255_set_portCread(0, scrambls_input_port_2_r);
		ppi8255_set_portCread(1, scrambls_protection_r);
		ppi8255_set_portCwrite(1, scramble_protection_w);
	} };
	
	public static DriverInitHandlerPtr init_theend  = new DriverInitHandlerPtr() { public void handler(){
		init_scramble_ppi();
	
		ppi8255_set_portCwrite(0, theend_coin_counter_w);
	} };
	
	public static DriverInitHandlerPtr init_stratgyx  = new DriverInitHandlerPtr() { public void handler(){
		init_scramble_ppi();
	
		install_mem_write_handler(0, 0xb000, 0xb000, scramble_background_green_w);
		install_mem_write_handler(0, 0xb002, 0xb002, scramble_background_blue_w);
		install_mem_write_handler(0, 0xb00a, 0xb00a, scramble_background_red_w);
	
		ppi8255_set_portCread(0, stratgyx_input_port_2_r);
		ppi8255_set_portCread(1, stratgyx_input_port_3_r);
	} };
	
	public static DriverInitHandlerPtr init_tazmani2  = new DriverInitHandlerPtr() { public void handler(){
		init_scramble_ppi();
	
		install_mem_write_handler(0, 0xb002, 0xb002, scramble_background_enable_w);
	} };
	
	public static DriverInitHandlerPtr init_amidar  = new DriverInitHandlerPtr() { public void handler(){
		init_scramble_ppi();
	
		/* Amidar has a the DIP switches connected to port C of the 2nd 8255 */
		ppi8255_set_portCread(1, input_port_3_r);
	} };
	
	public static DriverInitHandlerPtr init_ckongs  = new DriverInitHandlerPtr() { public void handler(){
		init_scramble_ppi();
	
		ppi8255_set_portBread(0, ckongs_input_port_1_r);
		ppi8255_set_portCread(0, ckongs_input_port_2_r);
	} };
	
	public static DriverInitHandlerPtr init_mariner  = new DriverInitHandlerPtr() { public void handler(){
		init_scramble_ppi();
	
		/* extra ROM */
		install_mem_read_handler (0, 0x5800, 0x67ff, MRA_ROM);
		install_mem_write_handler(0, 0x5800, 0x67ff, MWA_ROM);
	
		install_mem_read_handler(0, 0x9008, 0x9008, mariner_protection_2_r);
		install_mem_read_handler(0, 0xb401, 0xb401, mariner_protection_1_r);
	
		/* ??? (it's NOT a background enable) */
		/*install_mem_write_handler(0, 0x6803, 0x6803, MWA_NOP);*/
	} };
	
	public static DriverInitHandlerPtr init_frogger  = new DriverInitHandlerPtr() { public void handler(){
		offs_t A;
		UINT8 *ROM;
	
	
		init_scramble_ppi();
	
	
		/* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
		ROM = memory_region(REGION_CPU2);
		for (A = 0;A < 0x0800;A++)
			ROM[A] = BITSWAP8(ROM[A],7,6,5,4,3,2,0,1);
	
		/* likewise, the 2nd gfx ROM has data lines D0 and D1 swapped. Decode it. */
		ROM = memory_region(REGION_GFX1);
		for (A = 0x0800;A < 0x1000;A++)
			ROM[A] = BITSWAP8(ROM[A],7,6,5,4,3,2,0,1);
	} };
	
	public static DriverInitHandlerPtr init_froggers  = new DriverInitHandlerPtr() { public void handler(){
		offs_t A;
		UINT8 *ROM;
	
	
		init_scramble_ppi();
	
		/* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
		ROM = memory_region(REGION_CPU2);
		for (A = 0;A < 0x0800;A++)
			ROM[A] = BITSWAP8(ROM[A],7,6,5,4,3,2,0,1);
	} };
	
	public static DriverInitHandlerPtr init_devilfsh  = new DriverInitHandlerPtr() { public void handler(){
		offs_t i;
		UINT8 *RAM;
	
	
		init_scramble_ppi();
	
	
		/* Address lines are scrambled on the main CPU */
	
		/* A0 -> A2 */
		/* A1 -> A0 */
		/* A2 -> A3 */
		/* A3 -> A1 */
	
		RAM = memory_region(REGION_CPU1);
		for (i = 0; i < 0x10000; i += 16)
		{
			offs_t j;
			UINT8 swapbuffer[16];
	
			for (j = 0; j < 16; j++)
			{
				offs_t new = BITSWAP8(j,7,6,5,4,2,0,3,1);
	
				swapbuffer[j] = RAM[i + new];
			}
	
			memcpy(&RAM[i], swapbuffer, 16);
		}
	} };
	
	public static DriverInitHandlerPtr init_mars  = new DriverInitHandlerPtr() { public void handler(){
		init_devilfsh();
	
		/* extra port */
		ppi8255_set_portCread(1, input_port_3_r);
	} };
	
	public static DriverInitHandlerPtr init_hotshock  = new DriverInitHandlerPtr() { public void handler(){
		/* protection??? The game jumps into never-neverland here. I think
		   it just expects a RET there */
		memory_region(REGION_CPU1)[0x2ef9] = 0xc9;
	} };
	
	public static DriverInitHandlerPtr init_cavelon  = new DriverInitHandlerPtr() { public void handler(){
		init_scramble_ppi();
	
		/* banked ROM */
		install_mem_read_handler(0, 0x0000, 0x3fff, MRA_BANK1);
	
		/* A15 switches memory banks */
		install_mem_read_handler (0, 0x8000, 0xffff, cavelon_banksw_r);
		install_mem_write_handler(0, 0x8000, 0xffff, cavelon_banksw_w);
	
		install_mem_write_handler(0, 0x2000, 0x2000, MWA_NOP);	/* ??? */
		install_mem_write_handler(0, 0x3800, 0x3801, MWA_NOP);  /* looks suspicously like
																   an AY8910, but not sure */
	} };
	
	public static DriverInitHandlerPtr init_moonwar  = new DriverInitHandlerPtr() { public void handler(){
		init_scramble_ppi();
	
		/* special handler for the spinner */
		ppi8255_set_portAread (0, moonwar_input_port_0_r);
		ppi8255_set_portCwrite(0, moonwar_port_select_w);
	} };
	
	public static DriverInitHandlerPtr init_darkplnt  = new DriverInitHandlerPtr() { public void handler(){
		init_scramble_ppi();
	
		/* special handler for the spinner */
		ppi8255_set_portBread(0, darkplnt_input_port_1_r);
	
		install_mem_write_handler(0, 0xb00a, 0xb00a, darkplnt_bullet_color_w);
	} };
	
	public static DriverInitHandlerPtr init_mimonkey  = new DriverInitHandlerPtr() { public void handler(){
		static const UINT8 xortable[16][16] = 
		{
			{ 0x03,0x03,0x05,0x07,0x85,0x00,0x85,0x85,0x80,0x80,0x06,0x03,0x03,0x00,0x00,0x81 },
			{ 0x83,0x87,0x03,0x87,0x06,0x00,0x06,0x04,0x02,0x00,0x84,0x84,0x04,0x00,0x01,0x83 },
			{ 0x82,0x82,0x84,0x02,0x04,0x00,0x00,0x03,0x82,0x00,0x06,0x80,0x03,0x00,0x81,0x07 },
			{ 0x06,0x06,0x82,0x81,0x85,0x00,0x04,0x07,0x81,0x05,0x04,0x00,0x03,0x00,0x82,0x84 },
			{ 0x07,0x07,0x80,0x07,0x07,0x00,0x85,0x86,0x00,0x07,0x06,0x04,0x85,0x00,0x86,0x85 },
			{ 0x81,0x83,0x02,0x02,0x87,0x00,0x86,0x03,0x04,0x06,0x80,0x05,0x87,0x00,0x81,0x81 },
			{ 0x01,0x01,0x00,0x07,0x07,0x00,0x01,0x01,0x07,0x07,0x06,0x00,0x06,0x00,0x07,0x07 },
			{ 0x80,0x87,0x81,0x87,0x83,0x00,0x84,0x01,0x01,0x86,0x86,0x80,0x86,0x00,0x86,0x86 },
			{ 0x03,0x03,0x05,0x07,0x85,0x00,0x85,0x85,0x80,0x80,0x06,0x03,0x03,0x00,0x00,0x81 },
			{ 0x83,0x87,0x03,0x87,0x06,0x00,0x06,0x04,0x02,0x00,0x84,0x84,0x04,0x00,0x01,0x83 },
			{ 0x82,0x82,0x84,0x02,0x04,0x00,0x00,0x03,0x82,0x00,0x06,0x80,0x03,0x00,0x81,0x07 },
			{ 0x06,0x06,0x82,0x81,0x85,0x00,0x04,0x07,0x81,0x05,0x04,0x00,0x03,0x00,0x82,0x84 },
			{ 0x07,0x07,0x80,0x07,0x07,0x00,0x85,0x86,0x00,0x07,0x06,0x04,0x85,0x00,0x86,0x85 },
			{ 0x81,0x83,0x02,0x02,0x87,0x00,0x86,0x03,0x04,0x06,0x80,0x05,0x87,0x00,0x81,0x81 },
			{ 0x01,0x01,0x00,0x07,0x07,0x00,0x01,0x01,0x07,0x07,0x06,0x00,0x06,0x00,0x07,0x07 },
			{ 0x80,0x87,0x81,0x87,0x83,0x00,0x84,0x01,0x01,0x86,0x86,0x80,0x86,0x00,0x86,0x86 }
		};
	
		unsigned char *ROM = memory_region(REGION_CPU1);
		int A, ctr = 0, line, col;
	
		for( A = 0; A < 0x4000; A++ )
		{
			line = (ctr & 0x07) | ((ctr & 0x200) >> 6);
			col = ((ROM[A] & 0x80) >> 4) | (ROM[A] & 0x07);
			ROM[A] = ROM[A] ^ xortable[line][col];
			ctr++;
		}
	
		init_scramble_ppi();
	
		install_mem_write_handler(0, 0xa804, 0xa804, scramble_background_enable_w);
	} };
	
	public static DriverInitHandlerPtr init_mimonsco  = new DriverInitHandlerPtr() { public void handler(){
		init_scramble_ppi();
	
		install_mem_write_handler(0, 0xa804, 0xa804, scramble_background_enable_w);
	} };
	
	public static DriverInitHandlerPtr init_mimonscr  = new DriverInitHandlerPtr() { public void handler(){
		init_scramble_ppi();
	
		install_mem_write_handler(0, 0x6804, 0x6804, scramble_background_enable_w);
	} };
	
	
	static int bit(int i,int n)
	{
		return ((i >> n) & 1);
	}
	
	
	public static DriverInitHandlerPtr init_anteater  = new DriverInitHandlerPtr() { public void handler(){
		offs_t i;
		UINT8 *RAM;
		UINT8 *scratch;
	
	
		init_scobra();
	
		/*
		*   Code To Decode Lost Tomb by Mirko Buffoni
		*   Optimizations done by Fabio Buffoni
		*/
	
		RAM = memory_region(REGION_GFX1);
	
		scratch = malloc(memory_region_length(REGION_GFX1));
	
		if (scratch)
		{
			memcpy(scratch, RAM, memory_region_length(REGION_GFX1));
	
			for (i = 0; i < memory_region_length(REGION_GFX1); i++)
			{
				int j;
	
	
				j = i & 0x9bf;
				j |= ( bit(i,4) ^ bit(i,9) ^ ( bit(i,2) & bit(i,10) ) ) << 6;
				j |= ( bit(i,2) ^ bit(i,10) ) << 9;
				j |= ( bit(i,0) ^ bit(i,6) ^ 1 ) << 10;
	
				RAM[i] = scratch[j];
			}
	
			free(scratch);
		}
	} };
	
	public static DriverInitHandlerPtr init_rescue  = new DriverInitHandlerPtr() { public void handler(){
		offs_t i;
		UINT8 *RAM;
		UINT8 *scratch;
	
	
		init_scobra();
	
		/*
		*   Code To Decode Lost Tomb by Mirko Buffoni
		*   Optimizations done by Fabio Buffoni
		*/
	
		RAM = memory_region(REGION_GFX1);
	
		scratch = malloc(memory_region_length(REGION_GFX1));
	
		if (scratch)
		{
			memcpy(scratch, RAM, memory_region_length(REGION_GFX1));
	
			for (i = 0; i < memory_region_length(REGION_GFX1); i++)
			{
				int j;
	
	
				j = i & 0xa7f;
				j |= ( bit(i,3) ^ bit(i,10) ) << 7;
				j |= ( bit(i,1) ^ bit(i,7) ) << 8;
				j |= ( bit(i,0) ^ bit(i,8) ) << 10;
	
				RAM[i] = scratch[j];
			}
	
			free(scratch);
		}
	} };
	
	public static DriverInitHandlerPtr init_minefld  = new DriverInitHandlerPtr() { public void handler(){
		offs_t i;
		UINT8 *RAM;
		UINT8 *scratch;
	
	
		init_scobra();
	
		/*
		*   Code To Decode Minefield by Mike Balfour and Nicola Salmoria
		*/
	
		RAM = memory_region(REGION_GFX1);
	
		scratch = malloc(memory_region_length(REGION_GFX1));
	
		if (scratch)
		{
			memcpy(scratch, RAM, memory_region_length(REGION_GFX1));
	
			for (i = 0; i < memory_region_length(REGION_GFX1); i++)
			{
				int j;
	
	
				j  = i & 0xd5f;
				j |= ( bit(i,3) ^ bit(i,7) ) << 5;
				j |= ( bit(i,2) ^ bit(i,9) ^ ( bit(i,0) & bit(i,5) ) ^
					 ( bit(i,3) & bit(i,7) & ( bit(i,0) ^ bit(i,5) ))) << 7;
				j |= ( bit(i,0) ^ bit(i,5) ^ ( bit(i,3) & bit(i,7) ) ) << 9;
	
				RAM[i] = scratch[j];
			}
	
			free(scratch);
		}
	} };
	
	public static DriverInitHandlerPtr init_losttomb  = new DriverInitHandlerPtr() { public void handler(){
		offs_t i;
		UINT8 *RAM;
		UINT8 *scratch;
	
	
		init_scramble();
	
		/*
		*   Code To Decode Lost Tomb by Mirko Buffoni
		*   Optimizations done by Fabio Buffoni
		*/
	
		RAM = memory_region(REGION_GFX1);
	
		scratch = malloc(memory_region_length(REGION_GFX1));
	
		if (scratch)
		{
			memcpy(scratch, RAM, memory_region_length(REGION_GFX1));
	
			for (i = 0; i < memory_region_length(REGION_GFX1); i++)
			{
				int j;
	
	
				j = i & 0xa7f;
				j |= ( (bit(i,1) & bit(i,8)) | ((1 ^ bit(i,1)) & (bit(i,10)))) << 7;
				j |= ( bit(i,7) ^ (bit(i,1) & ( bit(i,7) ^ bit(i,10) ))) << 8;
				j |= ( (bit(i,1) & bit(i,7)) | ((1 ^ bit(i,1)) & (bit(i,8)))) << 10;
	
				RAM[i] = scratch[j];
			}
	
			free(scratch);
		}
	} };
	
	public static DriverInitHandlerPtr init_superbon  = new DriverInitHandlerPtr() { public void handler(){
		offs_t i;
		UINT8 *RAM;
	
	
		init_scramble();
	
		/* Deryption worked out by hand by Chris Hardy. */
	
		RAM = memory_region(REGION_CPU1);
	
		for (i = 0;i < 0x1000;i++)
		{
			/* Code is encrypted depending on bit 7 and 9 of the address */
			switch (i & 0x0280)
			{
			case 0x0000:
				RAM[i] ^= 0x92;
				break;
			case 0x0080:
				RAM[i] ^= 0x82;
				break;
			case 0x0200:
				RAM[i] ^= 0x12;
				break;
			case 0x0280:
				RAM[i] ^= 0x10;
				break;
			}
		}
	} };
	
	
	public static DriverInitHandlerPtr init_hustler  = new DriverInitHandlerPtr() { public void handler(){
		offs_t A;
	
	
		init_scramble_ppi();
	
	
		for (A = 0;A < 0x4000;A++)
		{
			UINT8 xormask;
			int bits[8];
			int i;
			UINT8 *rom = memory_region(REGION_CPU1);
	
	
			for (i = 0;i < 8;i++)
				bits[i] = (A >> i) & 1;
	
			xormask = 0xff;
			if (bits[0] ^ bits[1]) xormask ^= 0x01;
			if (bits[3] ^ bits[6]) xormask ^= 0x02;
			if (bits[4] ^ bits[5]) xormask ^= 0x04;
			if (bits[0] ^ bits[2]) xormask ^= 0x08;
			if (bits[2] ^ bits[3]) xormask ^= 0x10;
			if (bits[1] ^ bits[5]) xormask ^= 0x20;
			if (bits[0] ^ bits[7]) xormask ^= 0x40;
			if (bits[4] ^ bits[6]) xormask ^= 0x80;
	
			rom[A] ^= xormask;
		}
	
		/* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
		{
			UINT8 *rom = memory_region(REGION_CPU2);
	
	
			for (A = 0;A < 0x0800;A++)
				rom[A] = BITSWAP8(rom[A],7,6,5,4,3,2,0,1);
		}
	} };
	
	public static DriverInitHandlerPtr init_billiard  = new DriverInitHandlerPtr() { public void handler(){
		offs_t A;
	
	
		init_scramble_ppi();
	
	
		for (A = 0;A < 0x4000;A++)
		{
			UINT8 xormask;
			int bits[8];
			int i;
			UINT8 *rom = memory_region(REGION_CPU1);
	
	
			for (i = 0;i < 8;i++)
				bits[i] = (A >> i) & 1;
	
			xormask = 0x55;
			if (bits[2] ^ ( bits[3] &  bits[6])) xormask ^= 0x01;
			if (bits[4] ^ ( bits[5] &  bits[7])) xormask ^= 0x02;
			if (bits[0] ^ ( bits[7] & !bits[3])) xormask ^= 0x04;
			if (bits[3] ^ (!bits[0] &  bits[2])) xormask ^= 0x08;
			if (bits[5] ^ (!bits[4] &  bits[1])) xormask ^= 0x10;
			if (bits[6] ^ (!bits[2] & !bits[5])) xormask ^= 0x20;
			if (bits[1] ^ (!bits[6] & !bits[4])) xormask ^= 0x40;
			if (bits[7] ^ (!bits[1] &  bits[0])) xormask ^= 0x80;
	
			rom[A] ^= xormask;
	
			rom[A] = BITSWAP8(rom[A],6,1,2,5,4,3,0,7);
		}
	
		/* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
		{
			UINT8 *rom = memory_region(REGION_CPU2);
	
	
			for (A = 0;A < 0x0800;A++)
				rom[A] = BITSWAP8(rom[A],7,6,5,4,3,2,0,1);
		}
	} };
	
	public static DriverInitHandlerPtr init_ladybugg  = new DriverInitHandlerPtr() { public void handler(){
	/* Doesn't actually use the bank, but it mustn't have a coin lock! */
	install_mem_write_handler(0, 0x6002, 0x6002, galaxian_gfxbank_w);
	} };
	
	/************************************************************
	 mr kougar protected main cpu - by HIGHWAYMAN
	 mr kougar contains a steel module at location S7,
	 this module contains a Z80c cpu with the following changes:
	 IOREQ pin cut, RD & WR pins swapped and the following
	 address lines swapped - a0-a2,a1-a0,a2-a3,a3-a1.
	*************************************************************/
	
	public static DriverInitHandlerPtr init_mrkougar  = new DriverInitHandlerPtr() { public void handler(){
		init_devilfsh();
	
		/* no sound enabled bit */
		ppi8255_set_portBwrite(1, mrkougar_sh_irqtrigger_w);
	} };
	
	public static DriverInitHandlerPtr init_mrkougb  = new DriverInitHandlerPtr() { public void handler(){
		init_scramble_ppi();
	
		/* no sound enabled bit */
		ppi8255_set_portBwrite(1, mrkougar_sh_irqtrigger_w);
	} };
	
	public static DriverInitHandlerPtr init_sfx  = new DriverInitHandlerPtr() { public void handler(){
		ppi8255_init(&sfx_ppi8255_intf);
	} };
	
	public static DriverInitHandlerPtr init_gmgalax  = new DriverInitHandlerPtr() { public void handler(){
		gmgalax_select_game(input_port_6_r(0) & 0x01);
	} };
	
	
	public static InterruptHandlerPtr hunchbks_vh_interrupt = new InterruptHandlerPtr() {public void handler(){
		cpu_set_irq_line_and_vector(0,0,PULSE_LINE,0x03);
	} };
	
	public static InterruptHandlerPtr gmgalax_vh_interrupt = new InterruptHandlerPtr() {public void handler(){
		// reset the cpu if the selected game changed
		int new_game = input_port_6_r(0) & 0x01;
	
		if (gmgalax_selected_game != new_game)
		{
			gmgalax_select_game(new_game);
	
			/* Ghost Muncher never clears this */
			galaxian_stars_enable_w(0, 0);
	
			cpu_set_reset_line(0, ASSERT_LINE);
		}
	} };
}
