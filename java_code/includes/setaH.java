/***************************************************************************

							-= Seta Hardware =-

***************************************************************************/

/* Variables and functions defined in drivers/seta.c */

void seta_coin_lockout_w(int data);


/* Variables and functions defined in vidhrdw/seta.c */




WRITE16_HANDLER( twineagl_tilebank_w );

WRITE16_HANDLER( seta_vram_0_w );
WRITE16_HANDLER( seta_vram_2_w );
WRITE16_HANDLER( seta_vregs_w );


VIDEO_START( seta_no_layers);
VIDEO_START( twineagl_1_layer);
VIDEO_START( seta_1_layer);
VIDEO_START( seta_2_layers);
VIDEO_START( oisipuzl_2_layers );



/* Variables and functions defined in vidhrdw/seta2.c */


WRITE16_HANDLER( seta2_vregs_w );

VIDEO_START( seta2 );
VIDEO_START( seta2_offset );
VIDEO_EOF( seta2 );


/* Variables and functions defined in sndhrdw/seta.c */
#define	__uPD71054_TIMER	1


/* Variables and functions defined in vidhrdw/ssv.c */





READ16_HANDLER( ssv_vblank_r );
WRITE16_HANDLER( ssv_scroll_w );
WRITE16_HANDLER( paletteram16_xrgb_swap_word_w );
void ssv_enable_video(int enable);

VIDEO_START( ssv );
