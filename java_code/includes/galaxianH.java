/***************************************************************************

  Galaxian hardware family

  This include file is used by the following drivers:
    - galaxian.c
    - scramble.c
    - scobra.c
    - frogger.c
    - amidar.c

***************************************************************************/

/* defined in drivers/galaxian.c */
MACHINE_DRIVER_EXTERN(galaxian_base);


/* defined in drivers/scobra.c */


/* defined in drivers/frogger.c */


/* defined in vidhrdw/galaxian.c */












/* defined in machine/scramble.c */


READ_HANDLER(scobra_type2_ppi8255_0_r);
READ_HANDLER(scobra_type2_ppi8255_1_r);
WRITE_HANDLER(scobra_type2_ppi8255_0_w);
WRITE_HANDLER(scobra_type2_ppi8255_1_w);

READ_HANDLER(hustler_ppi8255_0_r);
READ_HANDLER(hustler_ppi8255_1_r);
WRITE_HANDLER(hustler_ppi8255_0_w);
WRITE_HANDLER(hustler_ppi8255_1_w);

READ_HANDLER(amidar_ppi8255_0_r);
READ_HANDLER(amidar_ppi8255_1_r);
WRITE_HANDLER(amidar_ppi8255_0_w);
WRITE_HANDLER(amidar_ppi8255_1_w);

READ_HANDLER(frogger_ppi8255_0_r);
READ_HANDLER(frogger_ppi8255_1_r);
WRITE_HANDLER(frogger_ppi8255_0_w);
WRITE_HANDLER(frogger_ppi8255_1_w);

READ_HANDLER(mars_ppi8255_0_r);
READ_HANDLER(mars_ppi8255_1_r);
WRITE_HANDLER(mars_ppi8255_0_w);
WRITE_HANDLER(mars_ppi8255_1_w);


#define galaxian_coin_counter_0_w galaxian_coin_counter_w










/* defined in sndhrdw/galaxian.c */


/* defined in sndhrdw/scramble.c */
void scramble_sh_init(void);
void sfx_sh_init(void);




