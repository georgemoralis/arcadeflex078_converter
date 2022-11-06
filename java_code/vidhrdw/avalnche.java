/***************************************************************************

	Atari Avalanche hardware

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class avalnche
{
	
	
	public static WriteHandlerPtr avalnche_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		videoram.write(offset,data);
	
		if (offset >= 0x200)
		{
			int x,y,i;
	
			x = 8 * (offset % 32);
			y = offset / 32;
	
			for (i = 0;i < 8;i++)
				plot_pixel(tmpbitmap,x+7-i,y,Machine->pens[(data >> i) & 1]);
		}
	} };
	
	
	public static VideoUpdateHandlerPtr video_update_avalnche  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		if (get_vh_global_attribute_changed())
		{
			int offs;
	
			for (offs = 0;offs < videoram_size[0]; offs++)
				avalnche_videoram_w(offs,videoram.read(offs));
		}
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,Machine.visible_area,TRANSPARENCY_NONE,0);
	} };
}
