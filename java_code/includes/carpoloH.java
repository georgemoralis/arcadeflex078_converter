/***************************************************************************

	Exidy Car Polo hardware

	driver by Zsolt Vasvari

****************************************************************************/

/* defined in machine/carpolo.c */






void carpolo_generate_car_car_interrupt(int car1, int car2);
void carpolo_generate_ball_screen_interrupt(data8_t cause);
void carpolo_generate_car_goal_interrupt(int car, int right_goal);
void carpolo_generate_car_ball_interrupt(int car, int car_x, int car_y);
void carpolo_generate_car_border_interrupt(int car, int horizontal_border);


/* defined in vidhrdw/carpolo.c */


