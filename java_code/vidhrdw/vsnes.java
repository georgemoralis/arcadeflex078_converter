/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class vsnes
{
	
	/* from machine */
	
	
	public static PaletteInitHandlerPtr palette_init_vsnes  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		ppu2c03b_init_palette( 0 );
	} };
	
	public static PaletteInitHandlerPtr palette_init_vsdual  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		ppu2c03b_init_palette( 0 );
		ppu2c03b_init_palette( 64 );
	} };
	
	static void ppu_irq( int num, int *ppu_regs )
	{
		cpu_set_nmi_line( num, PULSE_LINE );
	}
	
	/* our ppu interface											*/
	static struct ppu2c03b_interface ppu_interface =
	{
		1,						/* num */
		{ REGION_GFX1 },		/* vrom gfx region */
		{ 0 },					/* gfxlayout num */
		{ 0 },					/* color base */
		{ PPU_MIRROR_NONE },	/* mirroring */
		{ ppu_irq }				/* irq */
	};
	
	/* our ppu interface for dual games								*/
	static struct ppu2c03b_interface ppu_dual_interface =
	{
		2,										/* num */
		{ REGION_GFX1, REGION_GFX2 },			/* vrom gfx region */
		{ 0, 1 },								/* gfxlayout num */
		{ 0, 64 },								/* color base */
		{ PPU_MIRROR_NONE, PPU_MIRROR_NONE },	/* mirroring */
		{ ppu_irq, ppu_irq }					/* irq */
	};
	
	public static VideoStartHandlerPtr video_start_vsnes  = new VideoStartHandlerPtr() { public int handler(){
		return ppu2c03b_init( &ppu_interface );
	} };
	
	public static VideoStartHandlerPtr video_start_vsdual  = new VideoStartHandlerPtr() { public int handler(){
		return ppu2c03b_init( &ppu_dual_interface );
	} };
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	public static VideoUpdateHandlerPtr video_update_vsnes  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		/* render the ppu */
		ppu2c03b_render( 0, bitmap, 0, 0, 0, 0 );
	
			/* if this is a gun game, draw a simple crosshair */
			if ( vsnes_gun_controller )
			{
				int x_center = readinputport( 4 );
				int y_center = readinputport( 5 );
	
				draw_crosshair(bitmap,x_center,y_center,Machine.visible_area);
	
			}
	
		} };
	
	
	public static VideoUpdateHandlerPtr video_update_vsdual  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		/* render the ppu's */
		ppu2c03b_render( 0, bitmap, 0, 0, 0, 0 );
		ppu2c03b_render( 1, bitmap, 0, 0, 32*8, 0 );
	} };
}
