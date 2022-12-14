#ifndef S2650_H
#define S2650_H

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.cpu.s2650;

public class s2650H
{
	
	enum {
		S2650_PC=1, S2650_PS, S2650_R0, S2650_R1, S2650_R2, S2650_R3,
		S2650_R1A, S2650_R2A, S2650_R3A,
		S2650_HALT, S2650_IRQ_STATE, S2650_SI, S2650_FO
	};
	
	/* fake control port   M/~IO=0 D/~C=0 E/~NE=0 */
	#define S2650_CTRL_PORT 0x100
	
	/* fake data port      M/~IO=0 D/~C=1 E/~NE=0 */
	#define S2650_DATA_PORT 0x101
	
	/* extended i/o ports  M/~IO=0 D/~C=x E/~NE=1 */
	#define S2650_EXT_PORT	0xff
	
	/* Fake Sense Line */
	#define S2650_SENSE_PORT 0x102
	
	
	
	
	#ifdef  MAME_DEBUG
	#endif
	
	#endif
}
