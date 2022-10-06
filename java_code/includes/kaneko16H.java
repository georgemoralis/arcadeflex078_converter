/***************************************************************************

							-= Kaneko 16 Bit Games =-

***************************************************************************/

/* Tile Layers: */


WRITE16_HANDLER( kaneko16_vram_0_w );
WRITE16_HANDLER( kaneko16_vram_1_w );
WRITE16_HANDLER( kaneko16_vram_2_w );
WRITE16_HANDLER( kaneko16_vram_3_w );

WRITE16_HANDLER( kaneko16_layers_0_regs_w );
WRITE16_HANDLER( kaneko16_layers_1_regs_w );


/* Sprites: */


READ16_HANDLER ( kaneko16_sprites_regs_r );
WRITE16_HANDLER( kaneko16_sprites_regs_w );

void kaneko16_draw_sprites(struct mame_bitmap *bitmap, const struct rectangle *cliprect, int pri);

/* Pixel Layer: */


READ16_HANDLER ( kaneko16_bg15_select_r );
WRITE16_HANDLER( kaneko16_bg15_select_w );

READ16_HANDLER ( kaneko16_bg15_reg_r );
WRITE16_HANDLER( kaneko16_bg15_reg_w );

PALETTE_INIT( berlwall );


/* Priorities: */

typedef struct
{
	int tile[4];
	int sprite[4];
}	kaneko16_priority_t;



/* Machine */

VIDEO_START( kaneko16_sprites );
VIDEO_START( kaneko16_1xVIEW2 );
VIDEO_START( kaneko16_2xVIEW2 );
VIDEO_START( berlwall );
VIDEO_START( sandscrp_1xVIEW2 );


VIDEO_UPDATE( kaneko16 );

MACHINE_INIT( kaneko16 );


/* in drivers/galpani2.c */

void galpani2_mcu_run(void);

/* in vidhrdw/galpani2.c */



PALETTE_INIT( galpani2 );
VIDEO_START( galpani2 );
VIDEO_UPDATE( galpani2 );

WRITE16_HANDLER( galpani2_palette_0_w );
WRITE16_HANDLER( galpani2_palette_1_w );

READ16_HANDLER ( galpani2_bg8_regs_0_r );
READ16_HANDLER ( galpani2_bg8_regs_1_r );
WRITE16_HANDLER( galpani2_bg8_regs_0_w );
WRITE16_HANDLER( galpani2_bg8_regs_1_w );
WRITE16_HANDLER( galpani2_bg8_0_w );
WRITE16_HANDLER( galpani2_bg8_1_w );

WRITE16_HANDLER( galpani2_bg15_w );
