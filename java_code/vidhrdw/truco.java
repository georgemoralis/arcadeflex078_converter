/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class truco
{
	
	public static PaletteInitHandlerPtr palette_init_truco  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int	r = ( i & 0x8 ) ? 0xff : 0x00;
			int g = ( i & 0x4 ) ? 0xff : 0x00;
			int b = ( i & 0x2 ) ? 0xff : 0x00;
	
			int dim = ( i & 0x1 );
	
			if ( dim ) {
				r >>= 1;
				g >>= 1;
				b >>= 1;
			}
	
			palette_set_color(i,r,g,b);
		}
	} };
	
	public static VideoUpdateHandlerPtr video_update_truco  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		data8_t		*vid = memory_region( REGION_CPU1 ) + 0x1800;
		int x, y;
	
		for( y = 0; y < 192; y++ ) {
			for( x = 0; x < 256; x++ ) {
				int		pixel;
	
				if ( x & 1 ) {
					pixel = vid[x>>1] & 0x0f;
				} else {
					pixel = ( vid[x>>1] >> 4 ) & 0x0f;
				}
	
				plot_pixel(bitmap,x,y,Machine.pens[pixel]);
			}
	
			vid += 0x80;
		}
	} };
}
