/***************************************************************************

	Pit&Run  
	
	Based on TaitsoSJ driver
	68705 has access to Z80 memory space

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.machine;

public class pitnrun
{
	
	
	static unsigned char fromz80,toz80;
	static int zaccept,zready;
	
	public static MachineInitHandlerPtr machine_init_pitnrun  = new MachineInitHandlerPtr() { public void handler(){
		zaccept = 1;
		zready = 0;
		cpu_set_irq_line(2,0,CLEAR_LINE);
	} };
	
	void pitnrun_mcu_real_data_r(int param)
	{
		zaccept = 1;
	}
	
	public static ReadHandlerPtr pitnrun_mcu_data_r  = new ReadHandlerPtr() { public int handler(int offset){
		timer_set(TIME_NOW,0,pitnrun_mcu_real_data_r);
		return toz80;
	} };
	
	void pitnrun_mcu_real_data_w(int data)
	{
		zready = 1;
		cpu_set_irq_line(2,0,ASSERT_LINE);
		fromz80 = data;
	}
	
	public static WriteHandlerPtr pitnrun_mcu_data_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		timer_set(TIME_NOW,data,pitnrun_mcu_real_data_w);
	} };
	
	public static ReadHandlerPtr pitnrun_mcu_status_r  = new ReadHandlerPtr() { public int handler(int offset){
		/* mcu synchronization */
		cpu_yielduntil_time (TIME_IN_USEC(5));
		/* bit 0 = the 68705 has read data from the Z80 */
		/* bit 1 = the 68705 has written data for the Z80 */
		return ~((zready << 1) | (zaccept << 0));
	} };
	
	static unsigned char portA_in,portA_out;
	
	public static ReadHandlerPtr pitnrun_68705_portA_r  = new ReadHandlerPtr() { public int handler(int offset){
		return portA_in;
	} };
	
	public static WriteHandlerPtr pitnrun_68705_portA_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		portA_out = data;
	} };
	
	
	
	/*
	 *  Port B connections:
	 *
	 *  all bits are logical 1 when read (+5V pullup)
	 *
	 *  0   W  !68INTRQ
	 *  1   W  !68LRD (enables latch which holds command from the Z80)
	 *  2   W  !68LWR (loads the latch which holds data for the Z80, and sets a
	 *                 status bit so the Z80 knows there's data waiting)
	 *  3   W  to Z80 !BUSRQ (aka !WAIT) pin
	 *  4   W  !68WRITE (triggers write to main Z80 memory area )
	 *  5   W  !68READ (triggers read from main Z80 memory area )
	 *  6   W  !LAL (loads the latch which holds the low 8 bits of the address of
	 *               the main Z80 memory location to access)
	 *  7   W  !UAL (loads the latch which holds the high 8 bits of the address of
	 *               the main Z80 memory location to access)
	 */
	
	public static ReadHandlerPtr pitnrun_68705_portB_r  = new ReadHandlerPtr() { public int handler(int offset){
		return 0xff;
	} };
	
	static int address;
	
	void pitnrun_mcu_data_real_r(int param)
	{
		zready = 0;
	}
	
	void pitnrun_mcu_status_real_w(int data)
	{
		toz80 = data;
		zaccept = 0;
	}
	
	public static WriteHandlerPtr pitnrun_68705_portB_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (~data & 0x02)
		{
			/* 68705 is going to read data from the Z80 */
			timer_set(TIME_NOW,0,pitnrun_mcu_data_real_r);
			cpu_set_irq_line(2,0,CLEAR_LINE);
			portA_in = fromz80;
		}
		if (~data & 0x04)
		{
			/* 68705 is writing data for the Z80 */
			timer_set(TIME_NOW,portA_out,pitnrun_mcu_status_real_w);
		}
		if (~data & 0x10)
		{
	    memory_set_context(0);
			cpu_writemem16(address, portA_out);
	    memory_set_context(2);
		}
		if (~data & 0x20)
		{
	        memory_set_context(0);
					portA_in = cpu_readmem16(address);
	        memory_set_context(2);
		}
		if (~data & 0x40)
		{
			address = (address & 0xff00) | portA_out;
		}
		if (~data & 0x80)
		{
			address = (address & 0x00ff) | (portA_out << 8);
		}
	} };
	
	/*
	 *  Port C connections:
	 *
	 *  0   R  ZREADY (1 when the Z80 has written a command in the latch)
	 *  1   R  ZACCEPT (1 when the Z80 has read data from the latch)
	 *  2   R  from Z80 !BUSAK pin
	 *  3   R  68INTAK (goes 0 when the interrupt request done with 68INTRQ
	 *                  passes through)
	 */
	
	public static ReadHandlerPtr pitnrun_68705_portC_r  = new ReadHandlerPtr() { public int handler(int offset){
		return (zready << 0) | (zaccept << 1);
	} };
	
	
	
}
