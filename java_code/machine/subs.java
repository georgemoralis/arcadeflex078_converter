/***************************************************************************

	Atari Subs hardware

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.04
 */ 
package arcadeflex.v078.machine;

public class subs
{
	
	static int subs_steering_buf1;
	static int subs_steering_buf2;
	static int subs_steering_val1;
	static int subs_steering_val2;
	
	/***************************************************************************
	subs_init_machine
	***************************************************************************/
	public static MachineInitHandlerPtr machine_init_subs  = new MachineInitHandlerPtr() { public void handler(){
		subs_steering_buf1 = 0;
		subs_steering_buf2 = 0;
		subs_steering_val1 = 0x00;
		subs_steering_val2 = 0x00;
	} };
	
	/***************************************************************************
	subs_interrupt
	***************************************************************************/
	public static InterruptHandlerPtr subs_interrupt = new InterruptHandlerPtr() {public void handler(){
		/* only do NMI interrupt if not in TEST mode */
		if ((input_port_2_r(0) & 0x40)==0x40)
			cpu_set_irq_line(0,IRQ_LINE_NMI,PULSE_LINE);
	} };
	
	/***************************************************************************
	Steering
	
	When D7 is high, the steering wheel has moved.
	If D6 is high, it moved left.  If D6 is low, it moved right.
	Be sure to keep returning a direction until steer_reset is called.
	***************************************************************************/
	static int subs_steering_1(void)
	{
		static int last_val=0;
		int this_val;
		int delta;
	
		this_val=input_port_3_r(0);
	
		delta=this_val-last_val;
		last_val=this_val;
		if (delta>128) delta-=256;
		else if (delta<-128) delta+=256;
		/* Divide by four to make our steering less sensitive */
		subs_steering_buf1+=(delta/4);
	
		if (subs_steering_buf1>0)
		{
		      subs_steering_buf1--;
		      subs_steering_val1=0xC0;
		}
		else if (subs_steering_buf1<0)
		{
		      subs_steering_buf1++;
		      subs_steering_val1=0x80;
		}
	
		return subs_steering_val1;
	}
	
	static int subs_steering_2(void)
	{
		static int last_val=0;
		int this_val;
		int delta;
	
		this_val=input_port_4_r(0);
	
		delta=this_val-last_val;
		last_val=this_val;
		if (delta>128) delta-=256;
		else if (delta<-128) delta+=256;
		/* Divide by four to make our steering less sensitive */
		subs_steering_buf2+=(delta/4);
	
		if (subs_steering_buf2>0)
		{
			subs_steering_buf2--;
			subs_steering_val2=0xC0;
		}
		else if (subs_steering_buf2<0)
		{
			subs_steering_buf2++;
			subs_steering_val2=0x80;
		}
	
		return subs_steering_val2;
	}
	
	/***************************************************************************
	subs_steer_reset
	***************************************************************************/
	public static WriteHandlerPtr subs_steer_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	    subs_steering_val1 = 0x00;
	    subs_steering_val2 = 0x00;
	} };
	
	/***************************************************************************
	subs_control_r
	***************************************************************************/
	public static ReadHandlerPtr subs_control_r  = new ReadHandlerPtr() { public int handler(int offset){
		int inport = input_port_1_r.handler(offset);
	
		switch (offset & 0x07)
		{
			case 0x00:		return ((inport & 0x01) << 7);	/* diag step */
			case 0x01:		return ((inport & 0x02) << 6);	/* diag hold */
			case 0x02:		return ((inport & 0x04) << 5);	/* slam */
			case 0x03:		return ((inport & 0x08) << 4);	/* spare */
			case 0x04:		return ((subs_steering_1() & 0x40) << 1);	/* steer dir 1 */
			case 0x05:		return ((subs_steering_1() & 0x80) << 0);	/* steer flag 1 */
			case 0x06:		return ((subs_steering_2() & 0x40) << 1);	/* steer dir 2 */
			case 0x07:		return ((subs_steering_2() & 0x80) << 0);	/* steer flag 2 */
		}
	
		return 0;
	} };
	
	/***************************************************************************
	subs_coin_r
	***************************************************************************/
	public static ReadHandlerPtr subs_coin_r  = new ReadHandlerPtr() { public int handler(int offset){
		int inport = input_port_2_r.handler(offset);
	
		switch (offset & 0x07)
		{
			case 0x00:		return ((inport & 0x01) << 7);	/* coin 1 */
			case 0x01:		return ((inport & 0x02) << 6);	/* start 1 */
			case 0x02:		return ((inport & 0x04) << 5);	/* coin 2 */
			case 0x03:		return ((inport & 0x08) << 4);	/* start 2 */
			case 0x04:		return ((inport & 0x10) << 3);	/* VBLANK */
			case 0x05:		return ((inport & 0x20) << 2);	/* fire 1 */
			case 0x06:		return ((inport & 0x40) << 1);	/* test */
			case 0x07:		return ((inport & 0x80) << 0);	/* fire 2 */
		}
	
		return 0;
	} };
	
	/***************************************************************************
	subs_options_r
	***************************************************************************/
	public static ReadHandlerPtr subs_options_r  = new ReadHandlerPtr() { public int handler(int offset){
		int opts = input_port_0_r.handler(offset);
	
		switch (offset & 0x03)
		{
			case 0x00:		return ((opts & 0xC0) >> 6);		/* language */
			case 0x01:		return ((opts & 0x30) >> 4);		/* credits */
			case 0x02:		return ((opts & 0x0C) >> 2);		/* game time */
			case 0x03:		return ((opts & 0x03) >> 0);		/* extended time */
		}
	
		return 0;
	} };
	
	/***************************************************************************
	subs_lamp1_w
	***************************************************************************/
	public static WriteHandlerPtr subs_lamp1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		set_led_status(0,~offset & 1);
	} };
	
	/***************************************************************************
	subs_lamp2_w
	***************************************************************************/
	public static WriteHandlerPtr subs_lamp2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		set_led_status(1,~offset & 1);
	} };
	
	/***************************************************************************
	sub sound functions
	***************************************************************************/
	
	public static WriteHandlerPtr subs_sonar2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		discrete_sound_w(1, offset & 0x01);
	} };
	
	public static WriteHandlerPtr subs_sonar1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		discrete_sound_w(0, offset & 0x01);
	} };
	
	public static WriteHandlerPtr subs_crash_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		discrete_sound_w(4, ~offset & 0x01);
	} };
	
	public static WriteHandlerPtr subs_explode_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		discrete_sound_w(5, ~offset & 0x01);
	} };
	
	public static WriteHandlerPtr subs_noise_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* Pulse noise reset */
		discrete_sound_w(6, 0);
	} };
	
}
