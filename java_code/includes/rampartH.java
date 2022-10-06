/*************************************************************************

	Atari Rampart hardware

*************************************************************************/


/*----------- defined in vidhrdw/rampart.c -----------*/

WRITE16_HANDLER( rampart_bitmap_w );


int rampart_bitmap_init(int _xdim, int _ydim);
void rampart_bitmap_render(struct mame_bitmap *bitmap, const struct rectangle *cliprect);

