#ifndef Z80_H
#define Z80_H

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.cpu.z80;

public class z80H
{
	
	enum {
		Z80_PC=1, Z80_SP, Z80_AF, Z80_BC, Z80_DE, Z80_HL,
		Z80_IX, Z80_IY,	Z80_AF2, Z80_BC2, Z80_DE2, Z80_HL2,
		Z80_R, Z80_I, Z80_IM, Z80_IFF1, Z80_IFF2, Z80_HALT,
		Z80_NMI_STATE, Z80_IRQ_STATE, Z80_DC0, Z80_DC1, Z80_DC2, Z80_DC3
	};
	
	enum {
		Z80_TABLE_op,
		Z80_TABLE_cb,
		Z80_TABLE_ed,
		Z80_TABLE_xy,
		Z80_TABLE_xycb,
		Z80_TABLE_ex	/* cycles counts for taken jr/jp/call and interrupt latency (rst opcodes) */
	};
	
	
	
	#ifdef MAME_DEBUG
	#endif
	
	#endif
	
}
