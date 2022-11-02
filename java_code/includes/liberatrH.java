/*************************************************************************

	Atari Liberator hardware

*************************************************************************/

/*----------- defined in vidhrdw/liberatr.c -----------*/


VIDEO_START( liberatr );

public static WriteHandlerPtr liberatr_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data) ;
