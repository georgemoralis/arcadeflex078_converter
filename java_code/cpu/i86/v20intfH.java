/* ASG 971222 -- rewrote this interface */
#ifndef __V20INTRF_H_
#define __V20INTRF_H_

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.cpu.i86;

public class v20intfH
{
	
	
	/* Public variables */
	#define v20_ICount i86_ICount
	
	/* Public functions */
	#define v20_init v30_init
	#define v20_reset v30_reset
	#define v20_exit i86_exit
	#define v20_execute v30_execute
	#define v20_get_context i86_get_context
	#define v20_set_context i86_set_context
	#define v20_get_reg i86_get_reg
	#define v20_set_reg i86_set_reg
	#define v20_set_irq_line i86_set_irq_line
	#define v20_set_irq_callback i86_set_irq_callback
	#define v20_dasm v30_dasm
	
	#endif
}
