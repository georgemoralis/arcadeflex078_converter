/***************************************************************************

	Atari Sky Diver hardware

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class skydiver
{
	
	
	data8_t *skydiver_videoram;
	
	static struct tilemap *bg_tilemap;
	static int width = 0;
	
	
	public static MachineInitHandlerPtr machine_init_skydiver  = new MachineInitHandlerPtr() { public void handler(){
		/* reset all latches */
		skydiver_start_lamp_1_w(0, 0);
		skydiver_start_lamp_2_w(0, 0);
		skydiver_lamp_s_w(0, 0);
		skydiver_lamp_k_w(0, 0);
		skydiver_lamp_y_w(0, 0);
		skydiver_lamp_d_w(0, 0);
		skydiver_lamp_i_w(0, 0);
		skydiver_lamp_v_w(0, 0);
		skydiver_lamp_e_w(0, 0);
		skydiver_lamp_r_w(0, 0);
		skydiver_width_w(0, 0);
		skydiver_coin_lockout_w(0, 0);
	} };
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoHandlerPtr get_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		data8_t code = skydiver_videoram[tile_index];
		SET_TILE_INFO(0, code & 0x3f, code >> 6, 0)
	} };
	
	
	
	/*************************************
	 *
	 *	Video system start
	 *
	 *************************************/
	
	public static VideoStartHandlerPtr video_start_skydiver  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32,32);
	
		return !bg_tilemap;
	} };
	
	
	/*************************************
	 *
	 *	Memory handlers
	 *
	 *************************************/
	
	public static WriteHandlerPtr skydiver_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (skydiver_videoram[offset] != data)
		{
			skydiver_videoram[offset] = data;
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	
	public static ReadHandlerPtr skydiver_wram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return skydiver_videoram[offset | 0x380];
	} };
	
	public static WriteHandlerPtr skydiver_wram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		skydiver_videoram[offset | 0x0380] = data;
	} };
	
	
	public static WriteHandlerPtr skydiver_width_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		width = offset;
	} };
	
	
	public static WriteHandlerPtr skydiver_coin_lockout_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		coin_lockout_global_w(!offset);
	} };
	
	
	public static WriteHandlerPtr skydiver_start_lamp_1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		set_led_status(0, offset);
	} };
	
	public static WriteHandlerPtr skydiver_start_lamp_2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		set_led_status(1, offset);
	} };
	
	
	public static WriteHandlerPtr skydiver_lamp_s_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		artwork_show("lamps", offset);
	} };
	
	public static WriteHandlerPtr skydiver_lamp_k_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		artwork_show("lampk", offset);
	} };
	
	public static WriteHandlerPtr skydiver_lamp_y_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		artwork_show("lampy", offset);
	} };
	
	public static WriteHandlerPtr skydiver_lamp_d_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		artwork_show("lampd", offset);
	} };
	
	public static WriteHandlerPtr skydiver_lamp_i_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		artwork_show("lampi", offset);
	} };
	
	public static WriteHandlerPtr skydiver_lamp_v_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		artwork_show("lampv", offset);
	} };
	
	public static WriteHandlerPtr skydiver_lamp_e_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		artwork_show("lampe", offset);
	} };
	
	public static WriteHandlerPtr skydiver_lamp_r_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		artwork_show("lampr", offset);
	} };
	
	
	/*************************************
	 *
	 *	Video update
	 *
	 *************************************/
	
	static void draw_sprites(struct mame_bitmap *bitmap, const struct rectangle *cliprect)
	{
		int pic;
	
	
		/* draw each one of our four motion objects, the two PLANE sprites
		   can be drawn double width */
		for (pic = 3; pic >= 0; pic--)
		{
			int sx,sy;
			int charcode;
			int xflip, yflip;
			int color;
			int wide;
	
			sx = 29*8 - skydiver_videoram[pic + 0x0390];
			sy = 30*8 - skydiver_videoram[pic*2 + 0x0398];
			charcode = skydiver_videoram[pic*2 + 0x0399];
			xflip = charcode & 0x10;
			yflip = charcode & 0x08;
			wide = (~pic & 0x02) && width;
			charcode = (charcode & 0x07) | ((charcode & 0x60) >> 2);
			color = pic & 0x01;
	
			if (wide)
			{
				sx -= 8;
			}
	
			drawgfxzoom(bitmap,Machine->gfx[1],
				charcode, color,
				xflip,yflip,sx,sy,
				cliprect,TRANSPARENCY_PEN,0,
				wide ? 0x20000 : 0x10000, 0x10000);
		}
	}
	
	
	public static VideoUpdateHandlerPtr video_update_skydiver  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap,cliprect,bg_tilemap,0,0);
	
		draw_sprites(bitmap, cliprect);
	} };
	
}
