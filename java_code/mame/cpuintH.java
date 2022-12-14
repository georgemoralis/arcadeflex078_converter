/***************************************************************************

	cpuint.h

	Core multi-CPU interrupt engine.

***************************************************************************/

#ifndef CPUINT_H
#define CPUINT_H

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.mame;

public class cpuintH
{
	
	#ifdef __cplusplus
	#endif
	
	
	/*************************************
	 *
	 *	Interrupt constants
	 *
	 *************************************/
	
	enum
	{
		/* generic "none" vector */
		INTERRUPT_NONE = 126
	};
	
	
	
	/*************************************
	 *
	 *	Startup/shutdown
	 *
	 *************************************/
	
	int cpuint_init(void);
	
	void cpuint_reset_cpu(int cpunum);
	
	
	
	
	/*************************************
	 *
	 *	Interrupt handling
	 *
	 *************************************/
	
	/* Install a driver callback for IRQ acknowledge */
	void cpu_set_irq_callback(int cpunum, int (*callback)(int irqline));
	
	/* Set the vector to be returned during a CPU's interrupt acknowledge cycle */
	void cpu_irq_line_vector_w(int cpunum, int irqline, int vector);
	
	/* set the IRQ line state for a specific irq line of a CPU */
	/* normally use state HOLD_LINE, irqline 0 for first IRQ type of a cpu */
	void cpu_set_irq_line(int cpunum, int irqline, int state);
	
	/* set the IRQ line state and a vector for the IRQ */
	void cpu_set_irq_line_and_vector(int cpunum, int irqline, int state, int vector);
	
	/* macro for handling NMI lines */
	#define cpu_set_nmi_line(cpunum, state) cpu_set_irq_line(cpunum, IRQ_LINE_NMI, state)
	
	
	
	/*************************************
	 *
	 *	Preferred interrupt callbacks
	 *
	 *************************************/
	
	
	
	
	
	
	
	
	
	
	
	
	/*************************************
	 *
	 *	Obsolete interrupt handling
	 *
	 *************************************/
	
	/* OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE */
	/* OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE */
	/* OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE */
	
	/* Obsolete functions: avoid using them in new drivers, as many of them will
	   go away in the future! */
	
	void cpu_interrupt_enable(int cpu,int enabled);
	
	/* OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE */
	/* OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE */
	/* OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE */
	
	
	
	#ifdef __cplusplus
	}
	#endif
	
	#endif	/* CPUEXEC_H */
}
