/*************************************************************************

	Atari G42 hardware

*************************************************************************/

/*----------- defined in machine/asic65.c -----------*/

void asic65_reset(int state);

READ16_HANDLER( asic65_io_r );
READ16_HANDLER( asic65_r );
WRITE16_HANDLER( asic65_data_w );


/*----------- defined in vidhrdw/atarig42.c -----------*/



WRITE16_HANDLER( atarig42_mo_control_w );

void atarig42_scanline_update(int scanline);

