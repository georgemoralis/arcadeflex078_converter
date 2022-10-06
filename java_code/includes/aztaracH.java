/*************************************************************************

	Centuri Aztarac hardware

*************************************************************************/

/*----------- defined in sndhrdw/aztarac.c -----------*/

READ16_HANDLER( aztarac_sound_r );
WRITE16_HANDLER( aztarac_sound_w );


INTERRUPT_GEN( aztarac_snd_timed_irq );


/*----------- defined in vidhrdw/aztarac.c -----------*/


WRITE16_HANDLER( aztarac_ubr_w );


