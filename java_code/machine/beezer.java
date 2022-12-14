/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.machine;

public class beezer
{
	
	
	static int pbus;
	
	static void b_via_0_irq (int level);
	
	static void b_via_1_irq (int level);
	
	static struct via6522_interface b_via_0_interface =
	{
		/*inputs : A/B         */ 0, b_via_0_pb_r,
		/*inputs : CA/B1,CA/B2 */ 0, via_1_ca2_r, b_via_0_ca2_r, via_1_ca1_r,
		/*outputs: A/B,CA/B2   */ b_via_0_pa_w, b_via_0_pb_w, b_via_0_ca2_w, via_1_ca1_w,
		/*irq                  */ b_via_0_irq
	};
	
	static struct via6522_interface b_via_1_interface =
	{
		/*inputs : A/B         */ b_via_1_pa_r, b_via_1_pb_r,
		/*inputs : CA/B1,CA/B2 */ via_0_cb2_r, 0, via_0_cb1_r, 0,
		/*outputs: A/B,CA/B2   */ b_via_1_pa_w, b_via_1_pb_w, via_0_cb1_w, 0,
		/*irq                  */ b_via_1_irq
	};
	
	public static ReadHandlerPtr b_via_0_ca2_r  = new ReadHandlerPtr() { public int handler(int offset){
		return 0;
	} };
	
	public static WriteHandlerPtr b_via_0_ca2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	} };
	
	static void b_via_0_irq (int level)
	{
		cpu_set_irq_line(0, M6809_IRQ_LINE, level);
	}
	
	public static ReadHandlerPtr b_via_0_pb_r  = new ReadHandlerPtr() { public int handler(int offset){
		return pbus;
	} };
	
	public static WriteHandlerPtr b_via_0_pa_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if ((data & 0x08) == 0)
			cpu_set_reset_line(1, ASSERT_LINE);
		else
			cpu_set_reset_line(1, CLEAR_LINE);
	
		if ((data & 0x04) == 0)
		{
			switch (data & 0x03)
			{
			case 0:
				pbus = input_port_0_r(0);
				break;
			case 1:
				pbus = input_port_1_r(0) | (input_port_2_r(0) << 4);
				break;
			case 2:
				pbus = input_port_3_r(0);
				break;
			case 3:
				pbus = 0xff;
				break;
			}
		}
	} };
	
	public static WriteHandlerPtr b_via_0_pb_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		pbus = data;
	} };
	
	static void b_via_1_irq (int level)
	{
		cpu_set_irq_line(1, M6809_IRQ_LINE, level);
	}
	
	public static ReadHandlerPtr b_via_1_pa_r  = new ReadHandlerPtr() { public int handler(int offset){
		return pbus;
	} };
	
	public static ReadHandlerPtr b_via_1_pb_r  = new ReadHandlerPtr() { public int handler(int offset){
		return 0xff;
	} };
	
	public static WriteHandlerPtr b_via_1_pa_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		pbus = data;
	} };
	
	public static WriteHandlerPtr b_via_1_pb_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	} };
	
	public static DriverInitHandlerPtr init_beezer  = new DriverInitHandlerPtr() { public void handler(){
		via_config(0, &b_via_0_interface);
		via_config(1, &b_via_1_interface);
		via_reset();
		pbus = 0;
	} };
	
	public static WriteHandlerPtr beezer_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if ((data & 0x07) == 0)
		{
			install_mem_write_handler(0, 0xc600, 0xc7ff, watchdog_reset_w);
			install_mem_write_handler(0, 0xc800, 0xc9ff, beezer_map_w);
			install_mem_read_handler(0, 0xca00, 0xcbff, beezer_line_r);
			install_mem_read_handler(0, 0xce00, 0xcfff, via_0_r);
			install_mem_write_handler(0, 0xce00, 0xcfff, via_0_w);
		}
		else
		{
			const UINT8 *rom = memory_region(REGION_CPU1) + 0x10000;
			install_mem_read_handler(0, 0xc000, 0xcfff, MRA_BANK1);
			install_mem_write_handler(0, 0xc000, 0xcfff, MWA_BANK1);
			cpu_setbank(1, rom + (data & 0x07) * 0x2000 + ((data & 0x08) ? 0x1000: 0));
		}
	} };
	
	
}
