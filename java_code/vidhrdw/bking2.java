/***************************************************************************

  bking2.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class bking2
{
	
	
	
	static int pc3259_output[4];
	static int pc3259_mask;
	
	static UINT8 xld1;
	static UINT8 xld2;
	static UINT8 xld3;
	static UINT8 yld1;
	static UINT8 yld2;
	static UINT8 yld3;
	
	static int ball1_pic;
	static int ball2_pic;
	static int crow_pic;
	static int crow_flip;
	static int palette_bank;
	static int controller;
	static int hit;
	static int flip_screen();
	
	static struct mame_bitmap* helper0;
	static struct mame_bitmap* helper1;
	
	static struct tilemap* tilemap;
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  The palette PROM is connected to the RGB output this way:
	
	  bit 7 -- 390 ohm resistor  -- BLUE
	        -- 220 ohm resistor  -- BLUE
	        -- 820 ohm resistor  -- GREEN
	        -- 390 ohm resistor  -- GREEN
	        -- 220 ohm resistor  -- GREEN
	        -- 820 ohm resistor  -- RED
	        -- 390 ohm resistor  -- RED
	  bit 0 -- 220 ohm resistor  -- RED
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_bking2  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,r,g,b;
	
	
			/* red component */
			bit0 = (color_prom.read()>> 0) & 0x01;
			bit1 = (color_prom.read()>> 1) & 0x01;
			bit2 = (color_prom.read()>> 2) & 0x01;
			r = 0x92 * bit0 + 0x46 * bit1 + 0x27 * bit2;
			/* green component */
			bit0 = (color_prom.read()>> 3) & 0x01;
			bit1 = (color_prom.read()>> 4) & 0x01;
			bit2 = (color_prom.read()>> 5) & 0x01;
			g = 0x92 * bit0 + 0x46 * bit1 + 0x27 * bit2;
			/* blue component */
			bit0 = (color_prom.read()>> 6) & 0x01;
			bit1 = (color_prom.read()>> 7) & 0x01;
			bit2 = 0;
			b = 0x92 * bit0 + 0x46 * bit1 + 0x27 * bit2;
	
			palette_set_color(i,r,g,b);
			color_prom++;
		}
	
		/* color PROM A7-A8 is the palette select */
	
		/* character colors. Image bits go to A0-A2 of the color PROM */
		for (i = 0; i < TOTAL_COLORS(0); i++)
		{
			COLOR(0,i) = ((i << 4) & 0x180) | (i & 0x07);
		}
	
		/* crow colors. Image bits go to A5-A6. */
		for (i = 0; i < TOTAL_COLORS(1); i++)
		{
			COLOR(1,i) = ((i << 5) & 0x180) | ((i & 0x03) << 5);
		}
	
		/* ball colors. Ball 1 image bit goes to A3. Ball 2 to A4. */
		for (i = 0; i < TOTAL_COLORS(2); i++)
		{
			COLOR(2,i) = ((i << 6) & 0x180) | ((i & 0x01) << 3);
			COLOR(3,i) = ((i << 6) & 0x180) | ((i & 0x01) << 4);
		}
	} };
	
	
	public static WriteHandlerPtr bking2_xld1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		xld1 = -data;
	} };
	
	public static WriteHandlerPtr bking2_yld1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		yld1 = -data;
	} };
	
	public static WriteHandlerPtr bking2_xld2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		xld2 = -data;
	} };
	
	public static WriteHandlerPtr bking2_yld2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		yld2 = -data;
	} };
	
	public static WriteHandlerPtr bking2_xld3_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		xld3 = -data;
	} };
	
	public static WriteHandlerPtr bking2_yld3_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		yld3 = -data;
	} };
	
	
	public static WriteHandlerPtr bking2_cont1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* D0 = COIN LOCK */
		/* D1 = BALL 5 (Controller selection) */
		/* D2 = VINV (flip screen) */
		/* D3 = Not Connected */
		/* D4-D7 = CROW0-CROW3 (selects crow picture) */
	
		coin_lockout_global_w(~data & 0x01);
	
		flip_screen() = data & 0x04;
	
		tilemap_set_flip(ALL_TILEMAPS, flip_screen() ? TILEMAP_FLIPX | TILEMAP_FLIPY : 0);
	
		controller = data & 0x02;
	
		crow_pic = (data >> 4) & 0x0f;
	} };
	
	public static WriteHandlerPtr bking2_cont2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* D0-D2 = BALL10 - BALL12 (Selects player 1 ball picture) */
		/* D3-D5 = BALL20 - BALL22 (Selects player 2 ball picture) */
		/* D6 = HIT1 */
		/* D7 = HIT2 */
	
		ball1_pic = (data >> 0) & 0x07;
		ball2_pic = (data >> 3) & 0x07;
	
		hit = data >> 6;
	} };
	
	public static WriteHandlerPtr bking2_cont3_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* D0 = CROW INV (inverts Crow picture and coordinates) */
		/* D1-D2 = COLOR 0 - COLOR 1 (switches 4 color palettes, global across all graphics) */
		/* D3 = SOUND STOP */
	
		crow_flip = ~data & 0x01;
	
		if (palette_bank != ((data >> 1) & 0x03))
		{
			tilemap_mark_all_tiles_dirty(tilemap);
		}
	
		palette_bank = (data >> 1) & 0x03;
	
		mixer_sound_enable_global_w(~data & 0x08);
	} };
	
	
	public static WriteHandlerPtr bking2_msk_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		pc3259_mask++;
	} };
	
	
	public static WriteHandlerPtr bking2_hitclr_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		pc3259_mask = 0;
	
		pc3259_output[0] = 0;
		pc3259_output[1] = 0;
		pc3259_output[2] = 0;
		pc3259_output[3] = 0;
	} };
	
	
	public static WriteHandlerPtr bking2_playfield_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (bking2_playfield_ram[offset] != data)
		{
			tilemap_mark_tile_dirty(tilemap, offset / 2);
		}
	
		bking2_playfield_ram[offset] = data;
	} };
	
	
	public static ReadHandlerPtr bking2_input_port_5_r  = new ReadHandlerPtr() { public int handler(int offset){
		return readinputport(controller ? 7 : 5);
	} };
	
	public static ReadHandlerPtr bking2_input_port_6_r  = new ReadHandlerPtr() { public int handler(int offset){
		return readinputport(controller ? 8 : 6);
	} };
	
	public static ReadHandlerPtr bking2_pos_r  = new ReadHandlerPtr() { public int handler(int offset){
		return pc3259_output[offset / 8] << 4;
	} };
	
	
	static UINT32 get_memory_offset(UINT32 col, UINT32 row, UINT32 num_cols, UINT32 num_rows)
	{
		return num_cols * row + col;
	}
	
	
	public static GetTileInfoHandlerPtr get_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		UINT8 code0 = bking2_playfield_ram[2 * tile_index + 0];
		UINT8 code1 = bking2_playfield_ram[2 * tile_index + 1];
	
		int flags = 0;
	
		if (code1 & 4) flags |= TILE_FLIPX;
		if (code1 & 8) flags |= TILE_FLIPY;
	
		SET_TILE_INFO(0, code0 + 256 * code1, palette_bank, flags)
	} };
	
	
	public static VideoStartHandlerPtr video_start_bking2  = new VideoStartHandlerPtr() { public int handler(){
		if ((tilemap = tilemap_create(get_tile_info, get_memory_offset, 0, 8, 8, 32, 32)) == NULL)
		{
			return 1;
		}
		if ((helper0 = auto_bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == NULL)
		{
			return 1;
		}
		if ((helper1 = auto_bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == NULL)
		{
			return 1;
		}
	
		return 0;
	} };
	
	
	public static VideoUpdateHandlerPtr video_update_bking2  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap, cliprect, tilemap, 0, 0);
	
		/* draw the balls */
	
		drawgfx(bitmap, Machine.gfx[2],
			ball1_pic,
			palette_bank,
			0, 0,
			xld1, yld1,
			cliprect, TRANSPARENCY_PEN, 0);
	
		drawgfx(bitmap, Machine.gfx[3],
			ball2_pic,
			palette_bank,
			0, 0,
			xld2, yld2,
			cliprect, TRANSPARENCY_PEN, 0);
	
		/* draw the crow */
	
		drawgfx(bitmap, Machine.gfx[1],
			crow_pic,
			palette_bank,
			crow_flip, crow_flip,
			crow_flip ? xld3 - 16 : 256 - xld3, crow_flip ? yld3 - 16 : 256 - yld3,
			cliprect, TRANSPARENCY_PEN, 0);
	} };
	
	
	public static VideoEofHandlerPtr video_eof_bking2  = new VideoEofHandlerPtr() { public void handler(){
		static struct rectangle rect = { 0, 7, 0, 15 };
	
		int xld = 0;
		int yld = 0;
	
		UINT32 latch = 0;
	
		if (pc3259_mask == 6)	/* player 1 */
		{
			xld = xld1;
			yld = yld1;
	
			drawgfx(helper1, Machine->gfx[2],
				ball1_pic,
				0,
				0, 0,
				0, 0,
				&rect, TRANSPARENCY_NONE, 0);
	
			latch = 0x0C00;
		}
	
		if (pc3259_mask == 3)	/* player 2 */
		{
			xld = xld2;
			yld = yld2;
	
			drawgfx(helper1, Machine->gfx[3],
				ball2_pic,
				0,
				0, 0,
				0, 0,
				&rect, TRANSPARENCY_NONE, 0);
	
			latch = 0x0400;
		}
	
		tilemap_set_scrollx(tilemap, 0, flip_screen() ? -xld : xld);
		tilemap_set_scrolly(tilemap, 0, flip_screen() ? -yld : yld);
	
		tilemap_draw(helper0, &rect, tilemap, 0, 0);
	
		tilemap_set_scrollx(tilemap, 0, 0);
		tilemap_set_scrolly(tilemap, 0, 0);
	
		if (latch != 0)
		{
			const UINT8* MASK = memory_region(REGION_USER1) + 8 * hit;
	
			int x;
			int y;
	
			for (y = rect.min_y; y <= rect.max_y; y++)
			{
				const UINT16* p0 = helper0->line[y];
				const UINT16* p1 = helper1->line[y];
	
				for (x = rect.min_x; x <= rect.max_x; x++)
				{
					if (MASK[p0[x] & 7] && p1[x])
					{
						int col = (xld + x) / 8 + 1;
						int row = (yld + y) / 8 + 0;
	
						latch |= (flip_screen() ? 31 - col : col) << 0;
						latch |= (flip_screen() ? 31 - row : row) << 5;
	
						pc3259_output[0] = (latch >> 0x0) & 0xf;
						pc3259_output[1] = (latch >> 0x4) & 0xf;
						pc3259_output[2] = (latch >> 0x8) & 0xf;
						pc3259_output[3] = (latch >> 0xc) & 0xf;
	
						return;
					}
				}
			}
		}
	} };
}
