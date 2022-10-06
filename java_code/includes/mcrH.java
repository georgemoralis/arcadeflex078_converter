/*************************************************************************

	Driver for Midway MCR games

**************************************************************************/

/* constants */
#define MAIN_OSC_MCR_I		19968000


/*----------- defined in machine/mcr.c -----------*/




MACHINE_INIT( mcr );
MACHINE_INIT( mcr68 );
MACHINE_INIT( zwackery );

INTERRUPT_GEN( mcr_interrupt );
INTERRUPT_GEN( mcr68_interrupt );


WRITE16_HANDLER( mcr68_6840_upper_w );
WRITE16_HANDLER( mcr68_6840_lower_w );
READ16_HANDLER( mcr68_6840_upper_r );
READ16_HANDLER( mcr68_6840_lower_r );


/*----------- defined in vidhrdw/mcr12.c -----------*/


VIDEO_START( mcr1 );
VIDEO_START( mcr2 );
VIDEO_START( twotigra );
VIDEO_START( journey );


VIDEO_UPDATE( mcr1 );
VIDEO_UPDATE( mcr2 );
VIDEO_UPDATE( journey );


/*----------- defined in vidhrdw/mcr3.c -----------*/




void mcr3_update_sprites(struct mame_bitmap *bitmap, const struct rectangle *cliprect, int color_mask, int code_xor, int dx, int dy);

VIDEO_START( mcr3 );
VIDEO_START( mcrmono );
VIDEO_START( spyhunt );
VIDEO_START( dotron );

PALETTE_INIT( spyhunt );

VIDEO_UPDATE( mcr3 );
VIDEO_UPDATE( spyhunt );
VIDEO_UPDATE( dotron );


/*----------- defined in vidhrdw/mcr68.c -----------*/


WRITE16_HANDLER( mcr68_paletteram_w );
WRITE16_HANDLER( mcr68_videoram_w );

VIDEO_START( mcr68 );
VIDEO_UPDATE( mcr68 );

WRITE16_HANDLER( zwackery_paletteram_w );
WRITE16_HANDLER( zwackery_videoram_w );
WRITE16_HANDLER( zwackery_spriteram_w );

PALETTE_INIT( zwackery );
VIDEO_START( zwackery );
VIDEO_UPDATE( zwackery );
