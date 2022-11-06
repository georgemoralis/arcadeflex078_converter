/***************************************************************************

	Video emulation for Astro Invader, Space Intruder et al

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class astinvad
{
	
	static int spaceint_color;
	static int astinvad_adjust;
	static int astinvad_flash;
	
	
	void astinvad_set_flash(int data)
	{
		astinvad_flash = data;
	}
	
	
	public static WriteHandlerPtr spaceint_color_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		spaceint_color = data & 15;
	} };
	
	
	static void plot_byte(int x, int y, int data, int col)
	{
		int i;
	
		for (i = 0; i < 8; i++)
		{
			if (flip_screen())
			{
				plot_pixel(tmpbitmap, 255 - (x + i), 255 - y, (data & 1) ? col : 0);
			}
			else
			{
				plot_pixel(tmpbitmap, x + i, y, (data & 1) ? col : 0);
			}
	
			data >>= 1;
		}
	}
	
	
	static void spaceint_refresh(int offset)
	{
		int n = ((offset >> 5) & 0xf0) | colorram.read(offset);
	
		//
		//	This is almost certainly wrong.
		//
	
		int col = memory_region(REGION_PROMS)[n];
	
		plot_byte(8 * (offset / 256), 255 - offset % 256, videoram.read(offset), col & 7);
	}
	
	
	static void astinvad_refresh(int offset)
	{
		int n = ((offset >> 3) & ~0x1f) | (offset & 0x1f);
	
		int col;
	
		if (!flip_screen())
		{
			col = memory_region(REGION_PROMS)[(~n + astinvad_adjust) & 0x3ff];
		}
		else
		{
			col = memory_region(REGION_PROMS)[n] >> 4;
		}
	
		plot_byte(8 * (offset % 32), offset / 32, videoram.read(offset), col & 7);
	}
	
	
	public static WriteHandlerPtr spaceint_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		videoram.write(offset,data);
		colorram.write(offset,spaceint_color);
	
		spaceint_refresh(offset);
	} };
	
	
	public static WriteHandlerPtr astinvad_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		videoram.write(offset,data);
	
		astinvad_refresh(offset);
	} };
	
	
	public static VideoStartHandlerPtr video_start_astinvad  = new VideoStartHandlerPtr() { public int handler(){
		astinvad_adjust = 0x80;
	
		return video_start_generic_bitmapped();
	} };
	
	
	public static VideoStartHandlerPtr video_start_spcking2  = new VideoStartHandlerPtr() { public int handler(){
		astinvad_adjust = 0;
	
		return video_start_generic_bitmapped();
	} };
	
	
	public static VideoStartHandlerPtr video_start_spaceint  = new VideoStartHandlerPtr() { public int handler(){
		colorram = auto_malloc(0x2000);
	
		if (colorram == NULL)
		{
			return 1;
		}
	
		memset(colorram, 0, 0x2000);
	
		return video_start_generic_bitmapped();
	} };
	
	
	public static VideoUpdateHandlerPtr video_update_spaceint  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		if (get_vh_global_attribute_changed())
		{
			int offset;
	
			for (offset = 0; offset < videoram_size[0]; offset++)
			{
				spaceint_refresh(offset);
			}
		}
	
		copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, cliprect, TRANSPARENCY_NONE, 0);
	} };
	
	
	public static VideoUpdateHandlerPtr video_update_astinvad  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		if (astinvad_flash)
		{
			fillbitmap(bitmap, 1, cliprect);
		}
		else
		{
			if (get_vh_global_attribute_changed())
			{
				int offset;
	
				for (offset = 0; offset < videoram_size[0]; offset++)
				{
					astinvad_refresh(offset);
				}
			}
	
			copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, cliprect, TRANSPARENCY_NONE, 0);
		}
	} };
}
