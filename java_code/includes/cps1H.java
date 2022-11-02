#ifndef _CPS1_H_
#define _CPS1_H_



READ16_HANDLER( qsound_sharedram1_r );
WRITE16_HANDLER( qsound_sharedram1_w );

READ16_HANDLER( cps1_eeprom_port_r );
WRITE16_HANDLER( cps1_eeprom_port_w );

READ16_HANDLER( cps1_output_r );
WRITE16_HANDLER( cps1_output_w );

WRITE16_HANDLER( cps1_gfxram_w );

WRITE16_HANDLER( cps2_objram_bank_w );
READ16_HANDLER( cps2_objram1_r );
READ16_HANDLER( cps2_objram2_r );
WRITE16_HANDLER( cps2_objram1_w );
WRITE16_HANDLER( cps2_objram2_w );





#endif
