/*************************************************************************

	BattleToads

	Common definitions

*************************************************************************/

/*----------- defined in vidhrdw/btoads.c -----------*/



WRITE16_HANDLER( btoads_misc_control_w );
WRITE16_HANDLER( btoads_display_control_w );

WRITE16_HANDLER( btoads_scroll0_w );
WRITE16_HANDLER( btoads_scroll1_w );

WRITE16_HANDLER( btoads_paletteram_w );
READ16_HANDLER( btoads_paletteram_r );

WRITE16_HANDLER( btoads_vram_bg0_w );
WRITE16_HANDLER( btoads_vram_bg1_w );
READ16_HANDLER( btoads_vram_bg0_r );
READ16_HANDLER( btoads_vram_bg1_r );

WRITE16_HANDLER( btoads_vram_fg_display_w );
WRITE16_HANDLER( btoads_vram_fg_draw_w );
READ16_HANDLER( btoads_vram_fg_display_r );
READ16_HANDLER( btoads_vram_fg_draw_r );

void btoads_to_shiftreg(UINT32 address, UINT16 *shiftreg);
void btoads_from_shiftreg(UINT32 address, UINT16 *shiftreg);
