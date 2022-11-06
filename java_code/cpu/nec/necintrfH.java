/* ASG 971222 -- rewrote this interface */
#ifndef __NEC_H_
#define __NEC_H_

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.cpu.nec;

public class necintrfH
{
	
	enum {
		NEC_IP=1, NEC_AW, NEC_CW, NEC_DW, NEC_BW, NEC_SP, NEC_BP, NEC_IX, NEC_IY,
		NEC_FLAGS, NEC_ES, NEC_CS, NEC_SS, NEC_DS,
		NEC_VECTOR, NEC_PENDING, NEC_NMI_STATE, NEC_IRQ_STATE};
	
	/* Public variables */
	
	/* Public functions */
	
	#define v20_ICount nec_ICount
	
	#define v30_ICount nec_ICount
	
	#define v33_ICount nec_ICount
	
	#ifdef MAME_DEBUG
	#endif
	
	#endif
}
