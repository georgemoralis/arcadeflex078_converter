/* Space Bugger - Video Hardware */

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class sbugger
{
	
	
	static struct tilemap *sbugger_tilemap;
	
	public static GetTileInfoHandlerPtr get_sbugger_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int tileno, color;
	
		tileno = sbugger_videoram[tile_index];
		color = sbugger_videoram_attr[tile_index];
	
		SET_TILE_INFO(0,tileno,color,0)
	} };
	
	public static WriteHandlerPtr sbugger_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		sbugger_videoram[offset] = data;
		tilemap_mark_tile_dirty(sbugger_tilemap,offset);
	} };
	
	public static WriteHandlerPtr sbugger_videoram_attr_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		sbugger_videoram_attr[offset] = data;
		tilemap_mark_tile_dirty(sbugger_tilemap,offset);
	} };
	
	public static VideoStartHandlerPtr video_start_sbugger  = new VideoStartHandlerPtr() { public int handler(){
	
		sbugger_tilemap = tilemap_create(get_sbugger_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE, 8, 16,64,16);
	
		if (!sbugger_tilemap)
			return 1;
	
		return 0;
	} };
	
	public static VideoUpdateHandlerPtr video_update_sbugger  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap,cliprect,sbugger_tilemap,0,0);
	} };
	
	/* not right but so we can see things ok */
	public static PaletteInitHandlerPtr palette_init_sbugger  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		/* just some random colours for now */
		int i;
	
		for (i = 0;i < 256;i++)
		{
			int r = rand()|0x80;
			int g = rand()|0x80;
			int b = rand()|0x80;
			if (i == 0) r = g = b = 0;
	
			palette_set_color(i*2+1,r,g,b);
			palette_set_color(i*2,0,0,0);
	
		}
	
	} };
}
