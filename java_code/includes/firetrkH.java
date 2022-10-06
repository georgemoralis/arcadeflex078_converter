/*************************************************************************

Atari Fire Truck + Super Bug + Monte Carlo driver

*************************************************************************/

#define GAME_IS_FIRETRUCK   (firetrk_game == 1)
#define GAME_IS_SUPERBUG    (firetrk_game == 2)
#define GAME_IS_MONTECARLO  (firetrk_game == 3)

/*----------- defined in drivers/firetrk.c -----------*/

extern int firetrk_game;

/*----------- defined in vidhrdw/firetrk.c -----------*/

extern VIDEO_UPDATE( firetrk );
extern VIDEO_START( firetrk );
extern VIDEO_EOF( firetrk );

extern extern extern extern extern extern extern 
extern void firetrk_set_flash(int flag);
extern void firetrk_set_blink(int flag);

extern UINT8* firetrk_alpha_num_ram;
extern UINT8* firetrk_playfield_ram;

extern int firetrk_crash[2];
extern int firetrk_skid[2];
