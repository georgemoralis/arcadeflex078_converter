/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class hexion
{
	
	
	static data8_t *vram[2],*unkram;
	static int bankctrl,rambank,pmcbank,gfxrom_select;
	static struct tilemap *tilemap[2];
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	INLINE void get_tile_info(int tile_index,data8_t *ram)
	{
		tile_index *= 4;
		SET_TILE_INFO(
				0,
				ram[tile_index] + ((ram[tile_index+1] & 0x3f) << 8),
				ram[tile_index+2] & 0x0f,
				0)
	}
	
	public static GetTileInfoHandlerPtr get_tile_info0 = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		get_tile_info(tile_index,vram[0]);
	} };
	
	public static GetTileInfoHandlerPtr get_tile_info1 = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		get_tile_info(tile_index,vram[1]);
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VideoStartHandlerPtr video_start_hexion  = new VideoStartHandlerPtr() { public int handler(){
		tilemap[0] = tilemap_create(get_tile_info0,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,64,32);
		tilemap[1] = tilemap_create(get_tile_info1,tilemap_scan_rows,TILEMAP_OPAQUE,     8,8,64,32);
	
		if (!tilemap[0] || !tilemap[1])
			return 1;
	
		tilemap_set_transparent_pen(tilemap[0],0);
		tilemap_set_scrollx(tilemap[1],0,-4);
		tilemap_set_scrolly(tilemap[1],0,4);
	
		vram[0] = memory_region(REGION_CPU1) + 0x30000;
		vram[1] = vram[0] + 0x2000;
		unkram = vram[1] + 0x2000;
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr hexion_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		unsigned char *rom = memory_region(REGION_CPU1) + 0x10000;
	
		/* bits 0-3 select ROM bank */
		cpu_setbank(1,rom + 0x2000 * (data & 0x0f));
	
		/* does bit 6 trigger the 052591? */
		if (data & 0x40)
		{
			int bank = unkram[0]&1;
			memset(vram[bank],unkram[1],0x2000);
			tilemap_mark_all_tiles_dirty(tilemap[bank]);
		}
		/* bit 7 = PMC-BK */
		pmcbank = (data & 0x80) >> 7;
	
		/* other bits unknown */
	if (data & 0x30)
		usrintf_showmessage("bankswitch %02x",data&0xf0);
	
	//logerror("%04x: bankswitch_w %02x\n",activecpu_get_pc(),data);
	} };
	
	public static ReadHandlerPtr hexion_bankedram_r  = new ReadHandlerPtr() { public int handler(int offset){
		if (gfxrom_select && offset < 0x1000)
		{
			return memory_region(REGION_GFX1)[((gfxrom_select & 0x7f) << 12) + offset];
		}
		else if (bankctrl == 0)
		{
			return vram[rambank][offset];
		}
		else if (bankctrl == 2 && offset < 0x800)
		{
			return unkram[offset];
		}
		else
		{
	//logerror("%04x: bankedram_r offset %04x, bankctrl = %02x\n",activecpu_get_pc(),offset,bankctrl);
			return 0;
		}
	} };
	
	public static WriteHandlerPtr hexion_bankedram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (bankctrl == 3 && offset == 0 && (data & 0xfe) == 0)
		{
	//logerror("%04x: bankedram_w offset %04x, data %02x, bankctrl = %02x\n",activecpu_get_pc(),offset,data,bankctrl);
			rambank = data & 1;
		}
		else if (bankctrl == 0)
		{
			if (pmcbank)
			{
	//logerror("%04x: bankedram_w offset %04x, data %02x, bankctrl = %02x\n",activecpu_get_pc(),offset,data,bankctrl);
				if (vram[rambank][offset] != data)
				{
					vram[rambank][offset] = data;
					tilemap_mark_tile_dirty(tilemap[rambank],offset/4);
				}
			}
			else
				logerror("%04x pmc internal ram %04x = %02x\n",activecpu_get_pc(),offset,data);
		}
		else if (bankctrl == 2 && offset < 0x800)
		{
			if (pmcbank)
			{
	//logerror("%04x: unkram_w offset %04x, data %02x, bankctrl = %02x\n",activecpu_get_pc(),offset,data,bankctrl);
				unkram[offset] = data;
			}
			else
				logerror("%04x pmc internal ram %04x = %02x\n",activecpu_get_pc(),offset,data);
		}
		else
	logerror("%04x: bankedram_w offset %04x, data %02x, bankctrl = %02x\n",activecpu_get_pc(),offset,data,bankctrl);
	} };
	
	public static WriteHandlerPtr hexion_bankctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	//logerror("%04x: bankctrl_w %02x\n",activecpu_get_pc(),data);
		bankctrl = data;
	} };
	
	public static WriteHandlerPtr hexion_gfxrom_select_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	//logerror("%04x: gfxrom_select_w %02x\n",activecpu_get_pc(),data);
		gfxrom_select = data;
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	public static VideoUpdateHandlerPtr video_update_hexion  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap,cliprect,tilemap[1],0,0);
		tilemap_draw(bitmap,cliprect,tilemap[0],0,0);
	} };
}
