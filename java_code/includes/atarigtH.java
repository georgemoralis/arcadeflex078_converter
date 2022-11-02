/*************************************************************************

	Atari GT hardware

*************************************************************************/

/*----------- defined in vidhrdw/atarigt.c -----------*/



/*----------- defined in vidhrdw/atarigt.c -----------*/


void atarigt_colorram_w(offs_t address, data16_t data, data16_t mem_mask);
data16_t atarigt_colorram_r(offs_t address);


void atarigt_scanline_update(int scanline);
