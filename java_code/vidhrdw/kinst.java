/***************************************************************************

	Killer Instinct hardware

	driver by Aaron Giles and Bryan McPhail

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class kinst
{
	
	
	static UINT16 *vram_buffer;
	
	
	
	/*************************************
	 *
	 *	Video RAM buffering
	 *
	 *************************************/
	
	void kinst_buffer_vram(data32_t *base)
	{
		UINT16 *dest = vram_buffer;
		int y;
	
		/*  it is not at all understood how this really works; it breaks in
			the disk test, but this seems to be a close approximation to the
			real thing otherwise */
	
		/* loop over rows */
		for (y = 0; y < 240; y++)
		{
			data32_t *src = &base[640/4 * y];
			int i;
	
			/* loop over columns */
			for (i = 0; i < 320; i += 2)
			{
				*dest++ = *src & 0x7fff;
				*dest++ = (*src++ >> 16) & 0x7fff;
			}
		}
	}
	
	
	
	/*************************************
	 *
	 *	Palette setup
	 *
	 *************************************/
	
	public static PaletteInitHandlerPtr palette_init_kinst  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
		/* standard 5-5-5 RGB palette */
		for (i = 0; i < 32768; i++)
		{
			int r = i & 31;
			int g = (i >> 5) & 31;
			int b = (i >> 10) & 31;
			palette_set_color(i, (r << 3) | (r >> 2), (g << 3) | (g >> 2), (b << 3) | (b >> 2));
		}
	} };
	
	
	
	/*************************************
	 *
	 *	Video system start
	 *
	 *************************************/
	
	public static VideoStartHandlerPtr video_start_kinst  = new VideoStartHandlerPtr() { public int handler(){
		vram_buffer = auto_malloc(320 * 240 * sizeof(UINT16));
		if (!vram_buffer)
			return 1;
		return 0;
	} };
	
	
	
	/*************************************
	 *
	 *	Main refresh
	 *
	 *************************************/
	
	public static VideoUpdateHandlerPtr video_update_kinst  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int y;
	
		/* loop over rows and copy to the destination */
		for (y = cliprect.min_y; y <= cliprect.max_y; y++)
			memcpy(bitmap.line[y], &vram_buffer[y * 320], 320 * sizeof(UINT16));
	} };
}
