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

public class asteroid
{
	
	
	public static InterruptHandlerPtr asteroid_interrupt = new InterruptHandlerPtr() {public void handler(){
		/* Turn off interrupts if self-test is enabled */
		if (!(readinputport(0) & 0x80))
			cpu_set_irq_line(0, IRQ_LINE_NMI, PULSE_LINE);
	} };
	
	public static InterruptHandlerPtr asterock_interrupt = new InterruptHandlerPtr() {public void handler(){
		/* Turn off interrupts if self-test is enabled */
		if ((readinputport(0) & 0x80))
			cpu_set_irq_line(0, IRQ_LINE_NMI, PULSE_LINE);
	} };
	
	public static InterruptHandlerPtr llander_interrupt = new InterruptHandlerPtr() {public void handler(){
		/* Turn off interrupts if self-test is enabled */
		if (readinputport(0) & 0x02)
			cpu_set_irq_line(0, IRQ_LINE_NMI, PULSE_LINE);
	} };
	
	
	public static ReadHandlerPtr asteroid_IN0_r  = new ReadHandlerPtr() { public int handler(int offset){
	
		int res;
		int bitmask;
	
		res=readinputport(0);
	
		bitmask = (1 << offset);
	
		if (activecpu_gettotalcycles() & 0x100)
			res |= 0x02;
		if (!avgdvg_done())
			res |= 0x04;
	
		if (res & bitmask)
			res = 0x80;
		else
			res = ~0x80;
	
		return res;
	} };
	
	
	public static ReadHandlerPtr asteroib_IN0_r  = new ReadHandlerPtr() { public int handler(int offset){
		int res;
	
		res=readinputport(0);
	
	//	if (activecpu_gettotalcycles() & 0x100)
	//		res |= 0x02;
		if (!avgdvg_done())
			res |= 0x80;
	
		return res;
	} };
	
	public static ReadHandlerPtr asterock_IN0_r  = new ReadHandlerPtr() { public int handler(int offset){
		int res;
		int bitmask;
	
		res=readinputport(0);
	
		bitmask = (1 << offset);
	
		if (activecpu_gettotalcycles() & 0x100)
			res |= 0x04;
		if (!avgdvg_done())
			res |= 0x01;
	
		if (res & bitmask)
			res = ~0x80;
		else
			res = 0x80;
	
		return res;
	} };
	
	/*
	 * These 7 memory locations are used to read the player's controls.
	 * Typically, only the high bit is used. This is handled by one input port.
	 */
	
	public static ReadHandlerPtr asteroid_IN1_r  = new ReadHandlerPtr() { public int handler(int offset){
		int res;
		int bitmask;
	
		res=readinputport(1);
		bitmask = (1 << offset);
	
		if (res & bitmask)
			res = 0x80;
		else
		 	res = ~0x80;
		return (res);
	} };
	
	
	public static ReadHandlerPtr asteroid_DSW1_r  = new ReadHandlerPtr() { public int handler(int offset){
		int res;
		int res1;
	
		res1 = readinputport(2);
	
		res = 0xfc | ((res1 >> (2 * (3 - (offset & 0x3)))) & 0x3);
		return res;
	} };
	
	
	public static WriteHandlerPtr asteroid_bank_switch_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		static int asteroid_bank = 0;
		int asteroid_newbank;
		unsigned char *RAM = memory_region(REGION_CPU1);
	
	
		asteroid_newbank = (data >> 2) & 1;
		if (asteroid_bank != asteroid_newbank) {
			/* Perform bankswitching on page 2 and page 3 */
			int temp;
			int i;
	
			asteroid_bank = asteroid_newbank;
			for (i = 0; i < 0x100; i++) {
				temp = RAM[0x200 + i];
				RAM[0x200 + i] = RAM[0x300 + i];
				RAM[0x300 + i] = temp;
			}
		}
		set_led_status (0, ~data & 0x02);
		set_led_status (1, ~data & 0x01);
	} };
	
	
	public static WriteHandlerPtr astdelux_bank_switch_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		static int astdelux_bank = 0;
		int astdelux_newbank;
		unsigned char *RAM = memory_region(REGION_CPU1);
	
	
		astdelux_newbank = (data >> 7) & 1;
		if (astdelux_bank != astdelux_newbank) {
			/* Perform bankswitching on page 2 and page 3 */
			int temp;
			int i;
	
			astdelux_bank = astdelux_newbank;
			for (i = 0; i < 0x100; i++) {
				temp = RAM[0x200 + i];
				RAM[0x200 + i] = RAM[0x300 + i];
				RAM[0x300 + i] = temp;
			}
		}
	} };
	
	
	public static WriteHandlerPtr astdelux_led_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		set_led_status(offset,(data&0x80)?0:1);
	} };
	
	
	public static MachineInitHandlerPtr machine_init_asteroid  = new MachineInitHandlerPtr() { public void handler(){
		asteroid_bank_switch_w (0,0);
	} };
	
	
	/*
	 * This is Lunar Lander's Inputport 0.
	 */
	public static ReadHandlerPtr llander_IN0_r  = new ReadHandlerPtr() { public int handler(int offset){
		int res;
	
		res = readinputport(0);
	
		if (avgdvg_done())
			res |= 0x01;
		if (activecpu_gettotalcycles() & 0x100)
			res |= 0x40;
	
		return res;
	} };
}
