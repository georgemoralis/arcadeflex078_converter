/*************************************************************************

	3dfx Voodoo Graphics SST-1 emulator

	driver by Aaron Giles

**************************************************************************/



void voodoo_set_init_enable(data32_t newval);

void voodoo_reset(void);

WRITE32_HANDLER( voodoo_regs_w );
WRITE32_HANDLER( voodoo2_regs_w );
READ32_HANDLER( voodoo_regs_r );

WRITE32_HANDLER( voodoo_framebuf_w );
READ32_HANDLER( voodoo_framebuf_r );

WRITE32_HANDLER( voodoo_textureram_w );
