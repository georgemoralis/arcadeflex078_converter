/***************************************************************************

	Atari Clay Shoot hardware

	driver by Zsolt Vasvari

****************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class clayshoo
{
	
	
	/*************************************
	 *
	 *	Palette generation
	 *
	 *************************************/
	
	public static PaletteInitHandlerPtr palette_init_clayshoo  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		palette_set_color(0,0x00,0x00,0x00); /* black */
		palette_set_color(1,0xff,0xff,0xff);  /* white */
	} };
	
	
	/*************************************
	 *
	 *	Memory handlers
	 *
	 *************************************/
	
	public static WriteHandlerPtr clayshoo_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		UINT8 x,y;
		int i;
	
	
		x = ((offset & 0x1f) << 3);
		y = 191 - (offset >> 5);
	
		for (i = 0; i < 8; i++)
		{
			plot_pixel(tmpbitmap, x, y, (data & 0x80) ? Machine->pens[1] : Machine->pens[0]);
	
			x++;
			data <<= 1;
		}
	} };
}
