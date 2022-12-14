/***************************************************************************

	Atari Sprint 2 video emulation

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class sprint2
{
	
	UINT8* sprint2_video_ram;
	
	static struct tilemap* tilemap;
	static struct mame_bitmap* helper;
	
	static int collision[2];
	
	
	public static GetTileInfoHandlerPtr get_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		UINT8 code = sprint2_video_ram[tile_index];
	
		SET_TILE_INFO(0, code & 0x3f, code >> 7, 0)
	} };
	
	
	public static VideoStartHandlerPtr video_start_sprint2  = new VideoStartHandlerPtr() { public int handler(){
		helper = auto_bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height);
	
		if (helper == NULL)
		{
			return 1;
		}
	
		tilemap = tilemap_create(get_tile_info, tilemap_scan_rows, TILEMAP_OPAQUE, 16, 8, 32, 32);
	
		if (tilemap == NULL)
		{
			return 1;
		}
	
		return 0;
	} };
	
	
	public static ReadHandlerPtr sprint2_collision1_r  = new ReadHandlerPtr() { public int handler(int offset){
		return collision[0];
	} };
	public static ReadHandlerPtr sprint2_collision2_r  = new ReadHandlerPtr() { public int handler(int offset){
		return collision[1];
	} };
	
	
	public static WriteHandlerPtr sprint2_collision_reset1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		collision[0] = 0;
	} };
	public static WriteHandlerPtr sprint2_collision_reset2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		collision[1] = 0;
	} };
	
	
	public static WriteHandlerPtr sprint2_video_ram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (data != sprint2_video_ram[offset])
		{
			tilemap_mark_tile_dirty(tilemap, offset);
		}
	
		sprint2_video_ram[offset] = data;
	} };
	
	
	static UINT8 collision_check(struct rectangle* rect)
	{
		UINT8 data = 0;
	
		int x;
		int y;
	
		for (y = rect->min_y; y <= rect->max_y; y++)
		{
			for (x = rect->min_x; x <= rect->max_x; x++)
			{
				pen_t a = read_pixel(helper, x, y);
	
				if (a == 0)
				{
					data |= 0x40;
				}
				if (a == 3)
				{
					data |= 0x80;
				}
			}
		}
	
		return data;
	}
	
	
	static int get_sprite_code(int n)
	{
		return sprint2_video_ram[0x398 + 2 * n + 1] >> 3;
	}
	static int get_sprite_x(int n)
	{
		return 2 * (248 - sprint2_video_ram[0x390 + 1 * n]);
	}
	static int get_sprite_y(int n)
	{
		return 1 * (248 - sprint2_video_ram[0x398 + 2 * n]);
	}
	
	
	public static VideoUpdateHandlerPtr video_update_sprint2  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int i;
	
		tilemap_draw(bitmap, cliprect, tilemap, 0, 0);
		
		/* draw the sprites */
	
		for (i = 0; i < 4; i++)
		{
			drawgfx(bitmap, Machine.gfx[1],
				get_sprite_code(i),
				i,
				0, 0,
				get_sprite_x(i),
				get_sprite_y(i),
				cliprect, TRANSPARENCY_PEN, 0);
		}
	} };
	
	
	public static VideoEofHandlerPtr video_eof_sprint2  = new VideoEofHandlerPtr() { public void handler(){
		int i;
		int j;
	
		/*
		 * Collisions are detected for both player cars:
		 *
		 * D7 => collision w/ white playfield
		 * D6 => collision w/ black playfield or another car
		 *
		 */
	
		for (i = 0; i < 2; i++)
		{
			struct rectangle rect;
	
			rect.min_x = get_sprite_x(i);
			rect.min_y = get_sprite_y(i);
			rect.max_x = get_sprite_x(i) + Machine->gfx[1]->width - 1;
			rect.max_y = get_sprite_y(i) + Machine->gfx[1]->height - 1;
	
			if (rect.min_x < Machine->visible_area.min_x)
				rect.min_x = Machine->visible_area.min_x;
			if (rect.min_y < Machine->visible_area.min_y)
				rect.min_y = Machine->visible_area.min_y;
			if (rect.max_x > Machine->visible_area.max_x)
				rect.max_x = Machine->visible_area.max_x;
			if (rect.max_y > Machine->visible_area.max_y)
				rect.max_y = Machine->visible_area.max_y;
	
			/* check for sprite-tilemap collisions */
	
			tilemap_draw(helper, &rect, tilemap, 0, 0);
	
			drawgfx(helper, Machine->gfx[1],
				get_sprite_code(i),
				0,
				0, 0,
				get_sprite_x(i),
				get_sprite_y(i),
				&rect, TRANSPARENCY_PEN, 1);
	
			collision[i] |= collision_check(&rect);
	
			/* check for sprite-sprite collisions */
	
			for (j = 0; j < 4; j++)
			{
				if (j != i)
				{
					drawgfx(helper, Machine->gfx[1],
						get_sprite_code(j),
						1,
						0, 0,
						get_sprite_x(j),
						get_sprite_y(j),
						&rect, TRANSPARENCY_PEN, 0);
				}
			}
	
			drawgfx(helper, Machine->gfx[1],
				get_sprite_code(i),
				0,
				0, 0,
				get_sprite_x(i),
				get_sprite_y(i),
				&rect, TRANSPARENCY_PEN, 1);
	
			collision[i] |= collision_check(&rect);
		}
	} };
}
