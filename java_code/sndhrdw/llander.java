/***************************************************************************

	Lunar Lander Specific Sound Code

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.sndhrdw;

public class llander
{
	
	/************************************************************************/
	/* Lunar Lander Sound System Analog emulation by K.Wilkins Nov 2000     */
	/* Questions/Suggestions to mame@dysfunction.demon.co.uk                */
	/************************************************************************/
	#define LLANDER_TONE3K_EN	NODE_01
	#define LLANDER_TONE6K_EN	NODE_02
	#define LLANDER_THRUST_DATA	NODE_03
	#define LLANDER_EXPLOD_EN	NODE_04
	#define LLANDER_NOISE_RESET	NODE_05
	
	#define LLANDER_NOISE			NODE_10
	#define LLANDER_TONE_3K_SND		NODE_11
	#define LLANDER_TONE_6K_SND		NODE_12
	#define LLANDER_THRUST_EXPLOD_SND	NODE_13
	
	const struct discrete_lfsr_desc llander_lfsr={
		16,			/* Bit Length */
		0,			/* Reset Value */
		6,			/* Use Bit 6 as XOR input 0 */
		14,			/* Use Bit 14 as XOR input 1 */
		DISC_LFSR_XNOR,		/* Feedback stage1 is inverted XOR */
		DISC_LFSR_IN0,		/* Feedback stage2 is just stage 1 output external feed not used */
		DISC_LFSR_REPLACE,	/* Feedback stage3 replaces the shifted register contents */
		0x000001,		/* Everything is shifted into the first bit only */
		0,			/* Output not inverted */
		14			/* Output bit */
	};
	
	DISCRETE_SOUND_START(llander_sound_interface)
		/************************************************/
		/* llander Effects Relataive Gain Table         */
		/*                                              */
		/* Effect       V-ampIn   Gain ratio  Relative  */
		/* Tone3k        4        10/390          9.2   */
		/* Tone6k        4        10/390          9.2   */
		/* Explode       3.8      10/6.8*2     1000.0   */
		/* Thrust        3.8      10/6.8*2      600.0   */
		/*  NOTE: Thrust gain has to be tweaked, due to */
		/*        the filter stage.                     */
		/************************************************/
	
		/*                        NODE            ADDR  MASK    GAIN      OFFSET  INIT */
		DISCRETE_INPUTX     (LLANDER_THRUST_DATA,  0,  0x0007,  600.0/7,   0,      0)
		DISCRETE_INPUT      (LLANDER_TONE3K_EN,    1,  0x0007,                     0)
		DISCRETE_INPUT      (LLANDER_TONE6K_EN,    2,  0x0007,                     0)
		DISCRETE_INPUT      (LLANDER_EXPLOD_EN,    3,  0x0007,                     0)
		DISCRETE_INPUT_PULSE(LLANDER_NOISE_RESET,  4,  0x0007,                     1)
	
		DISCRETE_LFSR_NOISE(NODE_20, 1, LLANDER_NOISE_RESET, 12000, 1, 0, 0, &llander_lfsr)	// 12KHz Noise source for thrust
		DISCRETE_RCFILTER(LLANDER_NOISE, 1, NODE_20, 2247, 1e-6)
	
		DISCRETE_SQUAREWFIX(LLANDER_TONE_3K_SND, LLANDER_TONE3K_EN, 3000, 9.2, 50, 0, 0)	// 3KHz
	
		DISCRETE_SQUAREWFIX(LLANDER_TONE_6K_SND, LLANDER_TONE6K_EN, 6000, 9.2, 50, 0, 0)	// 6KHz
	
		DISCRETE_MULTIPLY(NODE_30, 1, LLANDER_NOISE, LLANDER_THRUST_DATA)	// Mix in 12KHz Noise source for thrust
		/* TBD - replace this line with a Sallen-Key Bandpass macro */
		DISCRETE_FILTER2(NODE_31, 1, NODE_30, 89.5, (1.0 / 7.6), DISC_FILTER_BANDPASS)
		DISCRETE_MULTIPLY(NODE_32, LLANDER_EXPLOD_EN, NODE_30, 1000.0/600.0)	// Explode adds original noise source onto filtered source
		DISCRETE_ADDER2(NODE_33, 1, NODE_31, NODE_32)
		/* TBD - replace this line with a Active Lowpass macro */
		DISCRETE_FILTER1(LLANDER_THRUST_EXPLOD_SND, 1, NODE_33, 560, DISC_FILTER_LOWPASS)
	
		DISCRETE_ADDER3(NODE_90, 1, LLANDER_TONE_3K_SND, LLANDER_TONE_6K_SND, LLANDER_THRUST_EXPLOD_SND)	// Mix all four sound sources
		DISCRETE_GAIN(NODE_91, NODE_90, 65534.0/(9.2+9.2+600+1000))
	
		DISCRETE_OUTPUT(NODE_91, 100)															// Take the output from the mixer
	DISCRETE_SOUND_END
	
	public static WriteHandlerPtr llander_snd_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* Resets the LFSR that is used for the white noise generator       */
		discrete_sound_w(4, 0);				/* Reset */
	} };
	
	public static WriteHandlerPtr llander_sounds_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		discrete_sound_w(0,data&0x07);		/* Thrust volume */
		discrete_sound_w(1,data&0x10);		/* Tone 3KHz enable */
		discrete_sound_w(2,data&0x20);		/* Tone 6KHz enable */
		discrete_sound_w(3,data&0x08);		/* Explosion */
	} };
	
}
