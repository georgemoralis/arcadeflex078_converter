#ifndef SNDINTRF_H
#define SNDINTRF_H


struct MachineSound
{
	int sound_type;
	void *sound_interface;
	const char *tag;
};


/*
 * ported to v0.78
 * using automatic conversion tool v0.02
 */ 

public class sndintrfH
{
	
	#if (HAS_SAMPLES)
	#endif
	#if (HAS_DAC)
	#endif
	#if (HAS_DISCRETE)
	#endif
	#if (HAS_AY8910)
	#endif
	#if (HAS_YM2203)
	#endif
	#if (HAS_YM2608)
	#endif
	#if (HAS_YM2612 || HAS_YM3438)
	#endif
	#if (HAS_YM2151 || HAS_YM2151_ALT)
	#endif
	#if (HAS_YM2608)
	#endif
	#if (HAS_YM2610 || HAS_YM2610B)
	#endif
	#if (HAS_YM3812 || HAS_YM3526 || HAS_Y8950)
	#endif
	#if (HAS_YM2413)
	#endif
	#if (HAS_YMZ280B)
	#endif
	#if (HAS_SN76477)
	#endif
	#if (HAS_SN76496)
	#endif
	#if (HAS_POKEY)
	#endif
	#if (HAS_NES)
	 #ifndef MESS
	 #else
	 #endif
	#endif
	#if (HAS_ASTROCADE)
	#endif
	#if (HAS_NAMCO)
	#endif
	#if (HAS_NAMCONA)
	#endif
	#if (HAS_TMS36XX)
	#endif
	#if (HAS_TMS5110)
	#endif
	#if (HAS_TMS5220)
	#endif
	#if (HAS_VLM5030)
	#endif
	#if (HAS_ADPCM || HAS_OKIM6295)
	#endif
	#if (HAS_MSM5205)
	#endif
	#if (HAS_MSM5232)
	#endif
	#if (HAS_UPD7759)
	#endif
	#if (HAS_HC55516)
	#endif
	#if (HAS_K005289)
	#endif
	#if (HAS_K007232)
	#endif
	#if (HAS_K051649)
	#endif
	#if (HAS_K053260)
	#endif
	#if (HAS_K054539)
	#endif
	#if (HAS_SEGAPCM)
	#endif
	#if (HAS_RF5C68)
	#endif
	#if (HAS_CEM3394)
	#endif
	#if (HAS_C140)
	#endif
	#if (HAS_QSOUND)
	#endif
	#if (HAS_SAA1099)
	#endif
	#if (HAS_IREMGA20)
	#endif
	#if (HAS_ES5505 || HAS_ES5506)
	#endif
	#if (HAS_BSMT2000)
	#endif
	#if (HAS_YMF262)
	#endif
	#if (HAS_YMF278B)
	#endif
	#if (HAS_GAELCO_CG1V || HAS_GAELCO_GAE1)
	#endif
	#if (HAS_X1_010)
	#endif
	#if (HAS_MULTIPCM)
	#endif
	#if (HAS_C6280)
	#endif
	#if (HAS_TIA)
	#endif
	#if (HAS_SP0250)
	#endif
	#if (HAS_SCSP)
	#endif
	#if (HAS_PSXSPU)
	#endif
	#if (HAS_YMF271)
	#endif
	
	#ifdef MESS
	#if (HAS_BEEP)
	#endif
	#if (HAS_SPEAKER)
	#endif
	#if (HAS_WAVE)
	#endif
	#endif
	
	
	#define SOUND_YM2151_ALT SOUND_YM2151
	
	/* the following list is automatically generated by makelist.pl - don't edit manually! */
	enum
	{
		SOUND_DUMMY,
	#if (HAS_CUSTOM)
		SOUND_CUSTOM,
	#endif
	#if (HAS_SAMPLES)
		SOUND_SAMPLES,
	#endif
	#if (HAS_DAC)
		SOUND_DAC,
	#endif
	#if (HAS_DISCRETE)
		SOUND_DISCRETE,
	#endif
	#if (HAS_AY8910)
		SOUND_AY8910,
	#endif
	#if (HAS_YM2203)
		SOUND_YM2203,
	#endif
	#if (HAS_YM2151)
		SOUND_YM2151,
	#endif
	#if (HAS_YM2151_ALT)
		SOUND_YM2151_ALT,
	#endif
	#if (HAS_YM2608)
		SOUND_YM2608,
	#endif
	#if (HAS_YM2610)
		SOUND_YM2610,
	#endif
	#if (HAS_YM2610B)
		SOUND_YM2610B,
	#endif
	#if (HAS_YM2612)
		SOUND_YM2612,
	#endif
	#if (HAS_YM3438)
		SOUND_YM3438,
	#endif
	#if (HAS_YM2413)
		SOUND_YM2413,
	#endif
	#if (HAS_YM3812)
		SOUND_YM3812,
	#endif
	#if (HAS_YM3526)
		SOUND_YM3526,
	#endif
	#if (HAS_YMZ280B)
		SOUND_YMZ280B,
	#endif
	#if (HAS_Y8950)
		SOUND_Y8950,
	#endif
	#if (HAS_SN76477)
		SOUND_SN76477,
	#endif
	#if (HAS_SN76496)
		SOUND_SN76496,
	#endif
	#if (HAS_POKEY)
		SOUND_POKEY,
	#endif
	#if (HAS_NES)
		SOUND_NES,
	#endif
	#if (HAS_ASTROCADE)
		SOUND_ASTROCADE,
	#endif
	#if (HAS_NAMCO)
		SOUND_NAMCO,
	#endif
	#if (HAS_NAMCONA)
		SOUND_NAMCONA,
	#endif
	#if (HAS_TMS36XX)
		SOUND_TMS36XX,
	#endif
	#if (HAS_TMS5110)
		SOUND_TMS5110,
	#endif
	#if (HAS_TMS5220)
		SOUND_TMS5220,
	#endif
	#if (HAS_VLM5030)
		SOUND_VLM5030,
	#endif
	#if (HAS_ADPCM)
		SOUND_ADPCM,
	#endif
	#if (HAS_OKIM6295)
		SOUND_OKIM6295,
	#endif
	#if (HAS_MSM5205)
		SOUND_MSM5205,
	#endif
	#if (HAS_MSM5232)
		SOUND_MSM5232,
	#endif
	#if (HAS_UPD7759)
		SOUND_UPD7759,
	#endif
	#if (HAS_HC55516)
		SOUND_HC55516,
	#endif
	#if (HAS_K005289)
		SOUND_K005289,
	#endif
	#if (HAS_K007232)
		SOUND_K007232,
	#endif
	#if (HAS_K051649)
		SOUND_K051649,
	#endif
	#if (HAS_K053260)
		SOUND_K053260,
	#endif
	#if (HAS_K054539)
		SOUND_K054539,
	#endif
	#if (HAS_SEGAPCM)
		SOUND_SEGAPCM,
	#endif
	#if (HAS_RF5C68)
		SOUND_RF5C68,
	#endif
	#if (HAS_CEM3394)
		SOUND_CEM3394,
	#endif
	#if (HAS_C140)
		SOUND_C140,
	#endif
	#if (HAS_QSOUND)
		SOUND_QSOUND,
	#endif
	#if (HAS_SAA1099)
		SOUND_SAA1099,
	#endif
	#if (HAS_IREMGA20)
		SOUND_IREMGA20,
	#endif
	#if (HAS_ES5505)
		SOUND_ES5505,
	#endif
	#if (HAS_ES5506)
		SOUND_ES5506,
	#endif
	#if (HAS_BSMT2000)
		SOUND_BSMT2000,
	#endif
	#if (HAS_YMF262)
		SOUND_YMF262,
	#endif
	#if (HAS_YMF278B)
		SOUND_YMF278B,
	#endif
	#if (HAS_GAELCO_CG1V)
		SOUND_GAELCO_CG1V,
	#endif
	#if (HAS_GAELCO_GAE1)
		SOUND_GAELCO_GAE1,
	#endif
	#if (HAS_X1_010)
		SOUND_X1_010,
	#endif
	#if (HAS_MULTIPCM)
		SOUND_MULTIPCM,
	#endif
	#if (HAS_C6280)
	 SOUND_C6280,
	#endif
	#if (HAS_TIA)
		SOUND_TIA,
	#endif
	#if (HAS_SP0250)
		SOUND_SP0250,
	#endif
	#if (HAS_SCSP)
		SOUND_SCSP,
	#endif
	#if (HAS_PSXSPU)
		SOUND_PSXSPU,
	#endif
	#if (HAS_YMF271)
		SOUND_YMF271,
	#endif
	
	#ifdef MESS
	#if (HAS_BEEP)
		SOUND_BEEP,
	#endif
	#if (HAS_SPEAKER)
		SOUND_SPEAKER,
	#endif
	#if (HAS_WAVE)
		SOUND_WAVE,
	#endif
	#endif
		SOUND_COUNT
	};
	
	
	/* structure for SOUND_CUSTOM sound drivers */
	struct CustomSound_interface
	{
		int (*sh_start)(const struct MachineSound *msound);
		void (*sh_stop)(void);
		void (*sh_update)(void);
	};
	
	
	int sound_start(void);
	void sound_stop(void);
	void sound_update(void);
	void sound_reset(void);
	
	/* returns name of the sound system */
	const char *soundtype_name(int soundtype);
	const char *sound_name(const struct MachineSound *msound);
	/* returns number of chips, or 0 if the sound type doesn't support multiple instances */
	int sound_num(const struct MachineSound *msound);
	/* returns clock rate, or 0 if the sound type doesn't support a clock frequency */
	int sound_clock(const struct MachineSound *msound);
	
	int sound_scalebufferpos(int value);
	
	
	READ16_HANDLER( soundlatch_word_r );
	READ16_HANDLER( soundlatch2_word_r );
	READ16_HANDLER( soundlatch3_word_r );
	READ16_HANDLER( soundlatch4_word_r );
	
	WRITE16_HANDLER( soundlatch_word_w );
	WRITE16_HANDLER( soundlatch2_word_w );
	WRITE16_HANDLER( soundlatch3_word_w );
	WRITE16_HANDLER( soundlatch4_word_w );
	
	
	
	/* If you're going to use soundlatchX_clear_w, and the cleared value is
	   something other than 0x00, use this function from machine_init. Note
	   that this one call effects all 4 latches */
	void soundlatch_setclearedvalue(int value);
	
	
	#endif
}
