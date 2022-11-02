/*************************************************************************

	SNK NeoGeo hardware

*************************************************************************/

/*----------- defined in drivers/neogeo.c -----------*/


void neogeo_set_cpu1_second_bank(UINT32 bankaddress);
void neogeo_init_cpu2_setbank(void);
void neogeo_register_main_savestate(void);

/*----------- defined in machine/neogeo.c -----------*/





WRITE16_HANDLER( neogeo_sram16_lock_w );
WRITE16_HANDLER( neogeo_sram16_unlock_w );
READ16_HANDLER( neogeo_sram16_r );
WRITE16_HANDLER( neogeo_sram16_w );


READ16_HANDLER( neogeo_memcard16_r );
WRITE16_HANDLER( neogeo_memcard16_w );
int neogeo_memcard_load(int);
void neogeo_memcard_save(void);
void neogeo_memcard_eject(void);
int neogeo_memcard_create(int);


/*----------- defined in machine/neocrypt.c -----------*/


void kof99_neogeo_gfx_decrypt(int extra_xor);
void kof2000_neogeo_gfx_decrypt(int extra_xor);


/*----------- defined in vidhrdw/neogeo.c -----------*/


WRITE16_HANDLER( neogeo_setpalbank0_16_w );
WRITE16_HANDLER( neogeo_setpalbank1_16_w );
READ16_HANDLER( neogeo_paletteram16_r );
WRITE16_HANDLER( neogeo_paletteram16_w );

WRITE16_HANDLER( neogeo_vidram16_offset_w );
READ16_HANDLER( neogeo_vidram16_data_r );
WRITE16_HANDLER( neogeo_vidram16_data_w );
WRITE16_HANDLER( neogeo_vidram16_modulo_w );
READ16_HANDLER( neogeo_vidram16_modulo_r );
WRITE16_HANDLER( neo_board_fix_16_w );
WRITE16_HANDLER( neo_game_fix_16_w );
WRITE16_HANDLER (neogeo_select_bios_vectors);
WRITE16_HANDLER (neogeo_select_game_vectors);

void neogeo_vh_raster_partial_refresh(struct mame_bitmap *bitmap,int current_line);
