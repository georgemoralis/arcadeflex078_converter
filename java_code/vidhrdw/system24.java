/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class system24
{
	
	public static VideoStartHandlerPtr video_start_system24  = new VideoStartHandlerPtr() { public int handler(){
		if(sys24_tile_vh_start(0xfff))
			return 1;
	
		if(sys24_sprite_vh_start())
			return 1;
	
		if(sys24_mixer_vh_start())
			return 1;
	
		return 0;
	} };
	
	static int layer_cmp(const void *pl1, const void *pl2)
	{
		static const int default_pri[12] = { 0, 1, 2, 3, 4, 5, 6, 7, -4, -3, -2, -1 };
		int l1 = *(int *)pl1;
		int l2 = *(int *)pl2;
		int p1 = sys24_mixer_get_reg(l1) & 7;
		int p2 = sys24_mixer_get_reg(l2) & 7;
		if(p1 != p2)
			return p1 - p2;
		return default_pri[l2] - default_pri[l1];
	}
	
	public static VideoUpdateHandlerPtr video_update_system24  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int i, level;
		int order[12], spri[4];
	
		if(sys24_mixer_get_reg(13) & 1) {
			fillbitmap(bitmap, get_black_pen(), 0);
			return;
		}
	
		sys24_tile_update();
	
		fillbitmap(priority_bitmap, 0, 0);
		fillbitmap(bitmap, Machine.pens[0], Machine.visible_area);
	
		for(i=0; i<12; i++)
			order[i] = i;
		qsort(order, 12, sizeof(int), layer_cmp);
	
		level = 0;
		for(i=0; i<12; i++)
			if(order[i] < 8)
				sys24_tile_draw(bitmap, cliprect, order[i], level, 0);
			else {
				spri[order[i]-8] = level;
				level++;
			}
	
		sys24_sprite_draw(bitmap, cliprect, spri);
	} };
}
