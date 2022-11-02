/*************************************************************************

	Driver for Midway MCR games

**************************************************************************/

/* constants */
#define MAIN_OSC_MCR_I		19968000


/*----------- defined in machine/mcr.c -----------*/







WRITE16_HANDLER( mcr68_6840_upper_w );
WRITE16_HANDLER( mcr68_6840_lower_w );
READ16_HANDLER( mcr68_6840_upper_r );
READ16_HANDLER( mcr68_6840_lower_r );


/*----------- defined in vidhrdw/mcr12.c -----------*/






/*----------- defined in vidhrdw/mcr3.c -----------*/




void mcr3_update_sprites(struct mame_bitmap *bitmap, const struct rectangle *cliprect, int color_mask, int code_xor, int dx, int dy);





/*----------- defined in vidhrdw/mcr68.c -----------*/


WRITE16_HANDLER( mcr68_paletteram_w );
WRITE16_HANDLER( mcr68_videoram_w );


WRITE16_HANDLER( zwackery_paletteram_w );
WRITE16_HANDLER( zwackery_videoram_w );
WRITE16_HANDLER( zwackery_spriteram_w );

