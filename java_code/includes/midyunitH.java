/*************************************************************************

	Williams/Midway Y/Z-unit system

**************************************************************************/

/* these are accurate for MK Rev 5 according to measurements done by Bryan on a real board */
/* due to the way the TMS34010 core operates, however, we need to use 0 for our VBLANK */
/* duration (263ms is the measured value) */
#define MKLA5_VBLANK_DURATION		0
#define MKLA5_FPS					53.204950


/*----------- defined in machine/midyunit.c -----------*/




WRITE16_HANDLER( midyunit_cmos_w );
READ16_HANDLER( midyunit_cmos_r );

WRITE16_HANDLER( midyunit_cmos_enable_w );
READ16_HANDLER( midyunit_protection_r );

READ16_HANDLER( midyunit_input_r );

READ16_HANDLER( midyunit_generic_speedup_1_16bit );
READ16_HANDLER( midyunit_generic_speedup_1_mixedbits );
READ16_HANDLER( midyunit_generic_speedup_1_32bit );
READ16_HANDLER( midyunit_generic_speedup_3 );










WRITE16_HANDLER( midyunit_sound_w );


/*----------- defined in vidhrdw/midyunit.c -----------*/



READ16_HANDLER( midyunit_gfxrom_r );

WRITE16_HANDLER( midyunit_vram_w );
READ16_HANDLER( midyunit_vram_r );

void midyunit_to_shiftreg(UINT32 address, UINT16 *shiftreg);
void midyunit_from_shiftreg(UINT32 address, UINT16 *shiftreg);

WRITE16_HANDLER( midyunit_control_w );
WRITE16_HANDLER( midyunit_paletteram_w );

READ16_HANDLER( midyunit_dma_r );
WRITE16_HANDLER( midyunit_dma_w );

void midyunit_display_addr_changed(UINT32 offs, int rowbytes, int scanline);
void midyunit_display_interrupt(int scanline);

WRITE16_HANDLER( midyunit_io_register_w );

