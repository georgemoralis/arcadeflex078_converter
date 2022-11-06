/***************************************************************************

	Kyugo hardware games

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.machine;

public class kyugo
{
	
	
	data8_t *kyugo_sharedram;
	
	
	/*************************************
	 *
	 *	Machine initialization
	 *
	 *************************************/
	
	public static MachineInitHandlerPtr machine_init_kyugo  = new MachineInitHandlerPtr() { public void handler(){
		// must start with interrupts and sub CPU disabled
		cpu_interrupt_enable(0, 0);
		kyugo_sub_cpu_control_w(0, 0);
	} };
	
	
	public static WriteHandlerPtr kyugo_sub_cpu_control_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		cpu_set_halt_line(1, data ? CLEAR_LINE : ASSERT_LINE);
	} };
	
	
	/*************************************
	 *
	 *	Shared RAM handlers
	 *
	 *************************************/
	
	public static WriteHandlerPtr kyugo_sharedram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		kyugo_sharedram[offset] = data;
	} };
	
	
	public static ReadHandlerPtr kyugo_sharedram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return kyugo_sharedram[offset];
	} };
}
