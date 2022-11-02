/*************************************************************************

	Art & Magic hardware

**************************************************************************/

/*----------- defined in vidhrdw/artmagic.c -----------*/




void artmagic_to_shiftreg(offs_t address, data16_t *data);
void artmagic_from_shiftreg(offs_t address, data16_t *data);

READ16_HANDLER( artmagic_blitter_r );
WRITE16_HANDLER( artmagic_blitter_w );

