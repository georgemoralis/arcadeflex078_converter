/* Variables needed by vidhrdw: */


/* Variables defined in vidhrdw: */


/* Functions defined in vidhrdw: */

WRITE16_HANDLER( unico_vram_0_w );
WRITE16_HANDLER( unico_vram_1_w );
WRITE16_HANDLER( unico_vram_2_w );
WRITE16_HANDLER( unico_palette_w );

WRITE32_HANDLER( unico_vram32_0_w );
WRITE32_HANDLER( unico_vram32_1_w );
WRITE32_HANDLER( unico_vram32_2_w );
WRITE32_HANDLER( unico_palette32_w );

VIDEO_START( unico );
VIDEO_UPDATE( unico );

VIDEO_START( zeropnt2 );
VIDEO_UPDATE( zeropnt2 );

