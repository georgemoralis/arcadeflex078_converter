/***************************************************************************

Atari Fire Truck + Super Bug + Monte Carlo video emulation

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class firetrk
{
	
	UINT8* firetrk_alpha_num_ram;
	UINT8* firetrk_playfield_ram;
	
	int firetrk_skid[2];
	int firetrk_crash[2];
	
	static struct mame_bitmap *helper1;
	static struct mame_bitmap *helper2;
	
	static int blink;
	static int flash;
	static int drone_hpos;
	static int drone_vpos;
	
	static const struct rectangle playfield_window = { 0x02A, 0x115, 0x000, 0x0FF };
	
	struct sprite_data
	{
		int layout;
		int number;
		int x;
		int y;
		int flipx;
		int flipy;
		int color;
	};
	
	static struct sprite_data car[2];
	
	static struct tilemap* tilemap1; /* for screen display */
	static struct tilemap* tilemap2; /* for collision detection */
	
	
	
	INLINE int arrow_code(int c)
	{
		if (GAME_IS_FIRETRUCK != 0)
		{
			return (c & 0x3F) >= 0x4 && (c & 0x3F) <= 0xB;
		}
		if (GAME_IS_SUPERBUG != 0)
		{
			return (c & 0x3F) >= 0x8 && (c & 0x3F) <= 0xF;
		}
	
		return 0;
	}
	
	
	void firetrk_set_flash(int flag)
	{
		tilemap_mark_all_tiles_dirty(tilemap1);
	
		if (GAME_IS_FIRETRUCK || GAME_IS_SUPERBUG)
		{
			if (flag != 0)
			{
				car[0].color = 1;
				car[1].color = 1;
			}
			else
			{
				car[0].color = 0;
				car[1].color = 0;
			}
		}
	
		flash = flag;
	}
	
	
	void firetrk_set_blink(int flag)
	{
		int offset;
	
		for (offset = 0; offset < 0x100; offset++)
		{
			if (arrow_code(firetrk_playfield_ram[offset]))
			{
				tilemap_mark_tile_dirty(tilemap1, offset);
			}
		}
	
		blink = flag;
	}
	
	
	static UINT32 get_memory_offset(UINT32 col, UINT32 row, UINT32 num_cols, UINT32 num_rows)
	{
		return num_cols * row + col;
	}
	
	
	static void get_tile_info1(int tile_index)
	{
		UINT8 code = firetrk_playfield_ram[tile_index];
	
		int color = code >> 6;
	
		if (blink && arrow_code(code))
		{
			color = 0;
		}
		if (flash != 0)
		{
			color |= 4;
		}
	
		SET_TILE_INFO(1, code & 0x3f, color, 0)
	}
	
	
	static void get_tile_info2(int tile_index)
	{
		UINT8 code = firetrk_playfield_ram[tile_index];
	
		int color = 0;
	
		/* palette 1 for crash and palette 2 for skid */
	
		if (GAME_IS_FIRETRUCK != 0)
		{
			if ((code & 0x30) != 0x00 || (code & 0x0c) == 0x00)
			{
				color = 1;   /* palette 0, 1 */
			}
			if ((code & 0x3c) == 0x0c)
			{
				color = 2;   /* palette 0, 2 */
			}
		}
	
		if (GAME_IS_SUPERBUG != 0)
		{
			if ((code & 0x30) != 0x00)
			{
				color = 1;   /* palette 0, 1 */
			}
			if ((code & 0x38) == 0x00)
			{
				color = 2;   /* palette 0, 2 */
			}
		}
	
		if (GAME_IS_MONTECARLO != 0)
		{
			if ((code & 0xc0) == 0x40 || (code & 0xc0) == 0x80)
			{
				color = 2;   /* palette 2, 1 */
			}
			if ((code & 0xc0) == 0xc0)
			{
				color = 1;   /* palette 2, 0 */
			}
			if ((code & 0xc0) == 0x00)
			{
				color = 3;   /* palette 2, 2 */
			}
			if ((code & 0x30) == 0x30)
			{
				color = 0;   /* palette 0, 0 */
			}
		}
	
		SET_TILE_INFO(2, code & 0x3f, color, 0)
	}
	
	
	public static WriteHandlerPtr firetrk_vert_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		tilemap_set_scrolly(tilemap1, 0, data);
		tilemap_set_scrolly(tilemap2, 0, data);
	} };
	
	
	public static WriteHandlerPtr firetrk_horz_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		tilemap_set_scrollx(tilemap1, 0, data - 37);
		tilemap_set_scrollx(tilemap2, 0, data - 37);
	} };
	
	
	public static WriteHandlerPtr firetrk_drone_hpos_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		drone_hpos = data;
	} };
	
	
	public static WriteHandlerPtr firetrk_drone_vpos_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		drone_vpos = data;
	} };
	
	
	public static WriteHandlerPtr firetrk_car_rot_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (GAME_IS_FIRETRUCK != 0)
		{
			car[0].number = data & 0x03;
	
			if ((data & 0x10) != 0) /* swap xy */
			{
				car[0].layout = 4;
			}
			else
			{
				car[0].layout = 3;
			}
	
			car[0].flipx = data & 0x04;
			car[0].flipy = data & 0x08;
		}
	
		if (GAME_IS_SUPERBUG != 0)
		{
			car[0].number = (data & 0x03) ^ 3;
	
			if ((data & 0x10) != 0) /* swap xy */
			{
				car[0].layout = 4;
			}
			else
			{
				car[0].layout = 3;
			}
	
			car[0].flipx = data & 0x04;
			car[0].flipy = data & 0x08;
		}
	
		if (GAME_IS_MONTECARLO != 0)
		{
			car[0].number = data & 0x07;
	
			if ((data & 0x80) != 0)
			{
				car[1].color |= 2;
			}
			else
			{
				car[1].color &= ~2;
			}
	
			car[0].flipx = data & 0x10;
			car[0].flipy = data & 0x08;
		}
	} };
	
	
	public static WriteHandlerPtr firetrk_drone_rot_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		car[1].number = data & 0x07;
	
		if (GAME_IS_FIRETRUCK != 0)
		{
			car[1].flipx = data & 0x08;
			car[1].flipy = data & 0x10;
		}
	
		if (GAME_IS_MONTECARLO != 0)
		{
			car[1].flipx = data & 0x10;
			car[1].flipy = data & 0x08;
	
			if ((data & 0x80) != 0)
			{
				car[1].color |= 1;
			}
			else
			{
				car[1].color &= ~1;
			}
		}
	} };
	
	
	public static WriteHandlerPtr firetrk_playfield_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (firetrk_playfield_ram[offset] != data)
		{
			tilemap_mark_tile_dirty(tilemap1, offset);
			tilemap_mark_tile_dirty(tilemap2, offset);
		}
	
		firetrk_playfield_ram[offset] = data;
	} };
	
	
	public static VideoUpdateHandlerPtr video_update_firetrk  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		helper1 = auto_bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height);
		helper2 = auto_bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height);
	
		if (helper1 == NULL || helper2 == NULL)
		{
			return 1;
		}
	
		tilemap1 = tilemap_create(get_tile_info1, get_memory_offset, TILEMAP_OPAQUE, 16, 16, 16, 16);
		tilemap2 = tilemap_create(get_tile_info2, get_memory_offset, TILEMAP_OPAQUE, 16, 16, 16, 16);
	
		if (tilemap1 == NULL || tilemap2 == NULL)
		{
			return 1;
		}
	
		memset(&car[0], 0, sizeof (struct sprite_data));
		memset(&car[1], 0, sizeof (struct sprite_data));
	
		if (GAME_IS_FIRETRUCK != 0)
		{
			car[0].layout = 3;
			car[1].layout = 5;
		}
		if (GAME_IS_SUPERBUG != 0)
		{
			car[0].layout = 3;
			car[1].layout = 0;
		}
		if (GAME_IS_MONTECARLO != 0)
		{
			car[0].layout = 3;
			car[1].layout = 4;
		}
	
		return 0;
	} };
	
	
	static void calc_car_positions(void)
	{
		car[0].x = 144;
		car[0].y = 104;
	
		if (GAME_IS_FIRETRUCK != 0)
		{
			car[1].x = car[1].flipx ? drone_hpos - 63 : 192 - drone_hpos;
			car[1].y = car[1].flipy ? drone_vpos - 63 : 192 - drone_vpos;
	
			car[1].x += 36;
		}
	
		if (GAME_IS_MONTECARLO != 0)
		{
			car[1].x = car[1].flipx ? drone_hpos - 31 : 224 - drone_hpos;
			car[1].y = car[1].flipy ? drone_vpos - 31 : 224 - drone_vpos;
	
			car[1].x += 34;
		}
	}
	
	
	static void draw_text(struct mame_bitmap* bitmap, const struct rectangle* cliprect)
	{
		const UINT8* p = firetrk_alpha_num_ram;
	
		int i;
	
		for (i = 0; i < 2; i++)
		{
			int x = 0;
			int y = 0;
	
			if (GAME_IS_SUPERBUG || GAME_IS_FIRETRUCK)
			{
				x = (i == 0) ? 296 : 8;
			}
			if (GAME_IS_MONTECARLO != 0)
			{
				x = (i == 0) ? 24 : 16;
			}
	
			for (y = 0; y < 256; y += Machine.gfx[0].width)
			{
				drawgfx(bitmap, Machine.gfx[0], *p++, 0, 0, 0,
					x, y, cliprect, TRANSPARENCY_NONE, 0);
			}
		}
	}
	
	
	public static VideoUpdateHandlerPtr video_update_firetrk  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		int i;
	
		tilemap_draw(bitmap, &playfield_window, tilemap1, 0, 0);
	
		calc_car_positions();
	
		for (i = 1; i >= 0; i--)
		{
			if (GAME_IS_SUPERBUG && i == 1)
			{
				continue;
			}
	
			drawgfx(bitmap,
				Machine.gfx[car[i].layout],
				car[i].number,
				car[i].color,
				car[i].flipx,
				car[i].flipy,
				car[i].x,
				car[i].y,
				&playfield_window,
				TRANSPARENCY_PEN, 0);
		}
	
		draw_text(bitmap, cliprect);
	} };
	
	
	public static VideoUpdateHandlerPtr video_update_firetrk  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		int i;
	
		tilemap_draw(helper1, &playfield_window, tilemap2, 0, 0);
	
		calc_car_positions();
	
		for (i = 1; i >= 0; i--)
		{
			int width = Machine.gfx[car[i].layout].width;
			int height = Machine.gfx[car[i].layout].height;
	
			int x;
			int y;
	
			if (GAME_IS_SUPERBUG && i == 1)
			{
				continue;
			}
	
			drawgfx(helper2,
				Machine.gfx[car[i].layout],
				car[i].number,
				0,
				car[i].flipx,
				car[i].flipy,
				car[i].x,
				car[i].y,
				&playfield_window,
				TRANSPARENCY_NONE, 0);
	
			for (y = car[i].y; y < car[i].y + height; y++)
			{
				for (x = car[i].x; x < car[i].x + width; x++)
				{
					pen_t a;
					pen_t b;
	
					if (x < playfield_window.min_x)
						continue;
					if (x > playfield_window.max_x)
						continue;
					if (y < playfield_window.min_y)
						continue;
					if (y > playfield_window.max_y)
						continue;
	
					a = read_pixel(helper1, x, y);
					b = read_pixel(helper2, x, y);
	
					if (b != 0 && a == 1)
					{
						firetrk_crash[i] = 1;
					}
					if (b != 0 && a == 2)
					{
						firetrk_skid[i] = 1;
					}
				}
			}
		}
	
		if (blink != 0)
		{
			firetrk_set_blink(0);
		}
	} };
}
