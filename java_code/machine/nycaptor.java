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

public class nycaptor
{
	
	static unsigned char from_main,from_mcu;
	static int mcu_sent = 0,main_sent = 0;
	
	
	static unsigned char portA_in,portA_out,ddrA;
	
	public static ReadHandlerPtr nycaptor_68705_portA_r  = new ReadHandlerPtr() { public int handler(int offset){
	
		return (portA_out & ddrA) | (portA_in & ~ddrA);
	} };
	
	public static WriteHandlerPtr nycaptor_68705_portA_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	
		portA_out = data;
	} };
	
	public static WriteHandlerPtr nycaptor_68705_ddrA_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		ddrA = data;
	} };
	
	/*
	 *  Port B connections:
	 *
	 *  all bits are logical 1 when read (+5V pullup)
	 *
	 *  1   W  when 1->0, enables latch which brings the command from main CPU (read from port A)
	 *  2   W  when 0->1, copies port A to the latch for the main CPU
	 */
	
	static unsigned char portB_in,portB_out,ddrB;
	
	public static ReadHandlerPtr nycaptor_68705_portB_r  = new ReadHandlerPtr() { public int handler(int offset){
		return (portB_out & ddrB) | (portB_in & ~ddrB);
	} };
	
	public static WriteHandlerPtr nycaptor_68705_portB_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	
	
		if ((ddrB & 0x02) && (~data & 0x02) && (portB_out & 0x02))
		{
			portA_in = from_main;
			if (main_sent) cpu_set_irq_line(3,0,CLEAR_LINE);
			main_sent = 0;
	
		}
		if ((ddrB & 0x04) && (data & 0x04) && (~portB_out & 0x04))
		{
	
			from_mcu = portA_out;
			mcu_sent = 1;
		}
	
		portB_out = data;
	} };
	
	public static WriteHandlerPtr nycaptor_68705_ddrB_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		ddrB = data;
	} };
	
	
	static unsigned char portC_in,portC_out,ddrC;
	
	public static ReadHandlerPtr nycaptor_68705_portC_r  = new ReadHandlerPtr() { public int handler(int offset){
		portC_in = 0;
		if (main_sent) portC_in |= 0x01;
		if (!mcu_sent) portC_in |= 0x02;
	
		return (portC_out & ddrC) | (portC_in & ~ddrC);
	} };
	
	public static WriteHandlerPtr nycaptor_68705_portC_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	
		portC_out = data;
	} };
	
	public static WriteHandlerPtr nycaptor_68705_ddrC_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		ddrC = data;
	} };
	
	public static WriteHandlerPtr nycaptor_mcu_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	
		from_main = data;
		main_sent = 1;
		cpu_set_irq_line(3,0,ASSERT_LINE);
	} };
	
	public static ReadHandlerPtr nycaptor_mcu_r  = new ReadHandlerPtr() { public int handler(int offset){
	
		mcu_sent = 0;
		return from_mcu;
	} };
	
	public static ReadHandlerPtr nycaptor_mcu_status_r1  = new ReadHandlerPtr() { public int handler(int offset){
		/* bit 1 = when 1, mcu has sent data to the main cpu */
	
		return mcu_sent?2:0;
	} };
	
	public static ReadHandlerPtr nycaptor_mcu_status_r2  = new ReadHandlerPtr() { public int handler(int offset){
		/* bit 0 = when 1, mcu is ready to receive data from main cpu */
	  return main_sent?0:1;
	
	} };
}
