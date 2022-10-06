/*************************************************************************

	Atari Skull & Crossbones hardware

*************************************************************************/

/*----------- defined in vidhrdw/skullxbo.c -----------*/

WRITE16_HANDLER( skullxbo_playfieldlatch_w );
WRITE16_HANDLER( skullxbo_xscroll_w );
WRITE16_HANDLER( skullxbo_yscroll_w );
WRITE16_HANDLER( skullxbo_mobmsb_w );


void skullxbo_scanline_update(int param);
