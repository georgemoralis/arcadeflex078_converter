/*************************************************************************

	Atari G42 hardware

*************************************************************************/

/*----------- defined in vidhrdw/atarigx2.c -----------*/


VIDEO_START( atarigx2 );

WRITE16_HANDLER( atarigx2_mo_control_w );

void atarigx2_scanline_update(int scanline);
