/*************************************************************************

	Atari ThunderJaws hardware

*************************************************************************/

/*----------- defined in vidhrdw/thunderj.c -----------*/


VIDEO_START( thunderj );
VIDEO_UPDATE( thunderj );

void thunderj_mark_high_palette(struct mame_bitmap *bitmap, UINT16 *pf, UINT16 *mo, int x, int y);
void thunderj_scanline_update(int scanline);
