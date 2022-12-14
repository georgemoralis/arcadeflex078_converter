
/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class xyonix
{
	
	
	static struct tilemap *xyonix_tilemap;
	
	
	public static PaletteInitHandlerPtr palette_init_xyonix  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,r,g,b;
	
			/* red component */
			bit0 = (color_prom.read(i)>> 0) & 0x01;
			bit1 = (color_prom.read(i)>> 1) & 0x01;
			bit2 = (color_prom.read(i)>> 2) & 0x01;
			r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (color_prom.read(i)>> 5) & 0x01;
			bit1 = (color_prom.read(i)>> 6) & 0x01;
			bit2 = (color_prom.read(i)>> 7) & 0x01;
			g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = (color_prom.read(i)>> 3) & 0x01;
			bit1 = (color_prom.read(i)>> 4) & 0x01;
			b = 0x4f * bit0 + 0xa8 * bit1;
	
			palette_set_color(i,r,g,b);
		}
	} };
	
	
	public static GetTileInfoHandlerPtr get_xyonix_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int tileno;
		int attr = xyonix_vidram[tile_index+0x1000+1];
	
		tileno = (xyonix_vidram[tile_index+1] << 0) | ((attr & 0x0f) << 8);
	
		SET_TILE_INFO(0,tileno,attr >> 4,0)
	} };
	
	public static WriteHandlerPtr xyonix_vidram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		xyonix_vidram[offset] = data;
		tilemap_mark_tile_dirty(xyonix_tilemap,(offset-1)&0x0fff);
	} };
	
	public static VideoStartHandlerPtr video_start_xyonix  = new VideoStartHandlerPtr() { public int handler(){
		xyonix_tilemap = tilemap_create(get_xyonix_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE, 4, 8,80,32);
	
		return 0;
	} };
	
	public static VideoUpdateHandlerPtr video_update_xyonix  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap,cliprect,xyonix_tilemap,0,0);
	} };
}
