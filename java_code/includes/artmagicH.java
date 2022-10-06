/*************************************************************************

	Art & Magic hardware

**************************************************************************/

/*----------- defined in vidhrdw/artmagic.c -----------*/



VIDEO_START( artmagic );

void artmagic_to_shiftreg(offs_t address, data16_t *data);
void artmagic_from_shiftreg(offs_t address, data16_t *data);

READ16_HANDLER( artmagic_blitter_r );
WRITE16_HANDLER( artmagic_blitter_w );

VIDEO_UPDATE( artmagic );
