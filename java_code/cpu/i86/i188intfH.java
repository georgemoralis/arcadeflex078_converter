#ifndef __I188INTR_H_
#define __I188INTR_H_

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.cpu.i86;

public class i188intfH
{
	
	
	/* Public variables */
	#define i188_ICount i86_ICount
	
	/* Public functions */
	#define i188_init i86_init
	#define i188_reset i86_reset
	#define i188_exit i86_exit
	#define i188_execute i186_execute
	#define i188_get_context i86_get_context
	#define i188_set_context i86_set_context
	#define i188_get_reg i86_get_reg
	#define i188_set_reg i86_set_reg
	#define i188_set_irq_line i86_set_irq_line
	#define i188_set_irq_callback i86_set_irq_callback
	#define i188_dasm i186_dasm
	
	#endif
}
