/**************************************************************************
 *                      Intel 8039 Portable Emulator                      *
 *                                                                        *
 *                   Copyright (C) 1997 by Mirko Buffoni                  *
 *  Based on the original work (C) 1997 by Dan Boris, an 8048 emulator    *
 *     You are not allowed to distribute this software commercially       *
 *        Please, notify me, if you make any changes to this file         *
 **************************************************************************/

#ifndef _I8039_H
#define _I8039_H

#ifndef INLINE
#define INLINE static inline
#endif

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.cpu.i8039;

public class i8039H
{
	
	
	/**************************************************************************
		Internal Clock divisor
	
		External Clock is divided internally by 3 to produce the machine state
		generator. This is then divided by 5 for the instruction cycle times.
		(Each instruction cycle passes through 5 machine states).
	*/
	
	#define I8039_CLOCK_DIVIDER		(3*5)
	
	
	
	enum { I8039_PC=1, I8039_SP, I8039_PSW, I8039_A,  I8039_IRQ_STATE, I8039_TC,
		   I8039_P1,   I8039_P2, I8039_R0,  I8039_R1, I8039_R2,
		   I8039_R3,   I8039_R4, I8039_R5,  I8039_R6, I8039_R7
	};
	
	
	
	/*   This handling of special I/O ports should be better for actual MAME
	 *   architecture.  (i.e., define access to ports { I8039_p1, I8039_p1, dkong_out_w })
	 */
	
	#define  I8039_p0	0x100   /* Not used */
	#define  I8039_p1	0x101
	#define  I8039_p2	0x102
	#define  I8039_p4	0x104
	#define  I8039_p5	0x105
	#define  I8039_p6	0x106
	#define  I8039_p7	0x107
	#define  I8039_t0	0x110
	#define  I8039_t1	0x111
	#define  I8039_bus	0x120
	
	/**************************************************************************
	 * I8035 section
	 **************************************************************************/
	#if (HAS_I8035)
	#define I8035_PC				I8039_PC
	#define I8035_SP				I8039_SP
	#define I8035_PSW				I8039_PSW
	#define I8035_A 				I8039_A
	#define I8035_IRQ_STATE 		I8039_IRQ_STATE
	#define I8035_TC				I8039_TC
	#define I8035_P1				I8039_P1
	#define I8035_P2				I8039_P2
	#define I8035_R0				I8039_R0
	#define I8035_R1				I8039_R1
	#define I8035_R2				I8039_R2
	#define I8035_R3				I8039_R3
	#define I8035_R4				I8039_R4
	#define I8035_R5				I8039_R5
	#define I8035_R6				I8039_R6
	#define I8035_R7				I8039_R7
	
	#define I8035_CLOCK_DIVIDER		I8039_CLOCK_DIVIDER
	#define i8035_ICount			i8039_ICount
	
	#endif
	
	/**************************************************************************
	 * I8048 section
	 **************************************************************************/
	#if (HAS_I8048)
	#define I8048_PC				I8039_PC
	#define I8048_SP				I8039_SP
	#define I8048_PSW				I8039_PSW
	#define I8048_A 				I8039_A
	#define I8048_IRQ_STATE 		I8039_IRQ_STATE
	#define I8048_TC				I8039_TC
	#define I8048_P1				I8039_P1
	#define I8048_P2				I8039_P2
	#define I8048_R0				I8039_R0
	#define I8048_R1				I8039_R1
	#define I8048_R2				I8039_R2
	#define I8048_R3				I8039_R3
	#define I8048_R4				I8039_R4
	#define I8048_R5				I8039_R5
	#define I8048_R6				I8039_R6
	#define I8048_R7				I8039_R7
	
	#define I8048_CLOCK_DIVIDER		I8039_CLOCK_DIVIDER
	#define i8048_ICount			i8039_ICount
	
	const char *i8048_info(void *context, int regnum);
	#endif
	
	/**************************************************************************
	 * N7751 section
	 **************************************************************************/
	#if (HAS_N7751)
	#define N7751_PC				I8039_PC
	#define N7751_SP				I8039_SP
	#define N7751_PSW				I8039_PSW
	#define N7751_A 				I8039_A
	#define N7751_IRQ_STATE 		I8039_IRQ_STATE
	#define N7751_TC				I8039_TC
	#define N7751_P1				I8039_P1
	#define N7751_P2				I8039_P2
	#define N7751_R0				I8039_R0
	#define N7751_R1				I8039_R1
	#define N7751_R2				I8039_R2
	#define N7751_R3				I8039_R3
	#define N7751_R4				I8039_R4
	#define N7751_R5				I8039_R5
	#define N7751_R6				I8039_R6
	#define N7751_R7				I8039_R7
	
	#define N7751_CLOCK_DIVIDER		I8039_CLOCK_DIVIDER
	#define n7751_ICount			i8039_ICount
	
	#endif
	
	
	/*
	 *	 Input a UINT8 from given I/O port
	 */
	#define I8039_In(Port) ((UINT8)cpu_readport16(Port))
	
	
	/*
	 *	 Output a UINT8 to given I/O port
	 */
	#define I8039_Out(Port,Value) (cpu_writeport16(Port,Value))
	
	
	/*
	 *	 Read a UINT8 from given memory location
	 */
	#define I8039_RDMEM(A) ((unsigned)cpu_readmem16(A))
	
	
	/*
	 *	 Write a UINT8 to given memory location
	 */
	#define I8039_WRMEM(A,V) (cpu_writemem16(A,V))
	
	
	/*
	 *   I8039_RDOP() is identical to I8039_RDMEM() except it is used for reading
	 *   opcodes. In case of system with memory mapped I/O, this function can be
	 *   used to greatly speed up emulation
	 */
	#define I8039_RDOP(A) ((unsigned)cpu_readop(A))
	
	
	/*
	 *   I8039_RDOP_ARG() is identical to I8039_RDOP() except it is used for reading
	 *   opcode arguments. This difference can be used to support systems that
	 *   use different encoding mechanisms for opcodes and opcode arguments
	 */
	#define I8039_RDOP_ARG(A) ((unsigned)cpu_readop_arg(A))
	
	#ifdef  MAME_DEBUG
	int 	Dasm8039(char *dst, unsigned pc);
	#endif
	
	#endif  /* _I8039_H */
}
