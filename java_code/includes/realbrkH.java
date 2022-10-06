#ifndef REALBRK_H
#define REALBRK_H

VIDEO_START(realbrk);
VIDEO_UPDATE(realbrk);

WRITE16_HANDLER( realbrk_vram_0_w );
WRITE16_HANDLER( realbrk_vram_1_w );
WRITE16_HANDLER( realbrk_vram_2_w );
WRITE16_HANDLER( realbrk_vregs_w );
WRITE16_HANDLER( realbrk_flipscreen_w );


#endif

