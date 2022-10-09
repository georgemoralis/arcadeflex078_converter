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
DRIVER_INIT( pisces );
DRIVER_INIT( checkmaj );
DRIVER_INIT( dingo );
DRIVER_INIT( kingball );
DRIVER_INIT( mooncrsu );
DRIVER_INIT( mooncrst );
DRIVER_INIT( mooncrgx );
DRIVER_INIT( moonqsr );
DRIVER_INIT( checkman );
DRIVER_INIT( gteikob2 );
DRIVER_INIT( azurian );
DRIVER_INIT( 4in1 );
DRIVER_INIT( mshuttle );
DRIVER_INIT( scramble_ppi );
DRIVER_INIT( amidar );
DRIVER_INIT( frogger );
DRIVER_INIT( froggers );
DRIVER_INIT( scobra );
DRIVER_INIT( stratgyx );
DRIVER_INIT( moonwar );
DRIVER_INIT( darkplnt );
DRIVER_INIT( tazmani2 );
DRIVER_INIT( anteater );
DRIVER_INIT( rescue );
DRIVER_INIT( minefld );
DRIVER_INIT( losttomb );
DRIVER_INIT( superbon );
DRIVER_INIT( hustler );
DRIVER_INIT( billiard );
DRIVER_INIT( mimonkey );
DRIVER_INIT( mimonsco );
DRIVER_INIT( atlantis );
DRIVER_INIT( scramble );
DRIVER_INIT( scrambls );
DRIVER_INIT( theend );
DRIVER_INIT( ckongs );
DRIVER_INIT( mariner );
DRIVER_INIT( mars );
DRIVER_INIT( devilfsh );
DRIVER_INIT( hotshock );
DRIVER_INIT( cavelon );
DRIVER_INIT( mrkougar );
DRIVER_INIT( mrkougb );
DRIVER_INIT( mimonscr );
DRIVER_INIT( sfx );
DRIVER_INIT( gmgalax );
DRIVER_INIT( ladybugg );

MACHINE_INIT( scramble );
MACHINE_INIT( sfx );
MACHINE_INIT( explorer );
MACHINE_INIT( galaxian );
MACHINE_INIT( devilfsg );

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




