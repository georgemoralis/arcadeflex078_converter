/*************************************************************************

	Driver for Atari polygon racer games

**************************************************************************/

/*----------- defined in machine/harddriv.c -----------*/










/* Driver/Multisync board */

WRITE16_HANDLER( hd68k_irq_ack_w );
void hdgsp_irq_gen(int state);
void hdmsp_irq_gen(int state);

READ16_HANDLER( hd68k_gsp_io_r );
WRITE16_HANDLER( hd68k_gsp_io_w );

READ16_HANDLER( hd68k_msp_io_r );
WRITE16_HANDLER( hd68k_msp_io_w );

READ16_HANDLER( hd68k_port0_r );
READ16_HANDLER( hd68k_adc8_r );
READ16_HANDLER( hd68k_adc12_r );
READ16_HANDLER( hdc68k_port1_r );
READ16_HANDLER( hda68k_port1_r );
READ16_HANDLER( hdc68k_wheel_r );
READ16_HANDLER( hd68k_sound_reset_r );

WRITE16_HANDLER( hd68k_adc_control_w );
WRITE16_HANDLER( hd68k_wr0_write );
WRITE16_HANDLER( hd68k_wr1_write );
WRITE16_HANDLER( hd68k_wr2_write );
WRITE16_HANDLER( hd68k_nwr_w );
WRITE16_HANDLER( hdc68k_wheel_edge_reset_w );

READ16_HANDLER( hd68k_zram_r );
WRITE16_HANDLER( hd68k_zram_w );

READ16_HANDLER( hd68k_duart_r );
WRITE16_HANDLER( hd68k_duart_w );

WRITE16_HANDLER( hdgsp_io_w );

WRITE16_HANDLER( hdgsp_protection_w );

WRITE16_HANDLER( stmsp_sync0_w );
WRITE16_HANDLER( stmsp_sync1_w );
WRITE16_HANDLER( stmsp_sync2_w );

/* ADSP board */
READ16_HANDLER( hd68k_adsp_program_r );
WRITE16_HANDLER( hd68k_adsp_program_w );

READ16_HANDLER( hd68k_adsp_data_r );
WRITE16_HANDLER( hd68k_adsp_data_w );

READ16_HANDLER( hd68k_adsp_buffer_r );
WRITE16_HANDLER( hd68k_adsp_buffer_w );

WRITE16_HANDLER( hd68k_adsp_control_w );
WRITE16_HANDLER( hd68k_adsp_irq_clear_w );
READ16_HANDLER( hd68k_adsp_irq_state_r );

READ16_HANDLER( hdadsp_special_r );
WRITE16_HANDLER( hdadsp_special_w );

/* DS III board */
WRITE16_HANDLER( hd68k_ds3_control_w );
READ16_HANDLER( hd68k_ds3_girq_state_r );
READ16_HANDLER( hd68k_ds3_sirq_state_r );
READ16_HANDLER( hd68k_ds3_gdata_r );
WRITE16_HANDLER( hd68k_ds3_gdata_w );
READ16_HANDLER( hd68k_ds3_sdata_r );
WRITE16_HANDLER( hd68k_ds3_sdata_w );

READ16_HANDLER( hdds3_special_r );
WRITE16_HANDLER( hdds3_special_w );
READ16_HANDLER( hdds3_control_r );
WRITE16_HANDLER( hdds3_control_w );

READ16_HANDLER( hd68k_ds3_program_r );
WRITE16_HANDLER( hd68k_ds3_program_w );

/* DSK board */
void hddsk_update_pif(UINT32 pins);
WRITE16_HANDLER( hd68k_dsk_control_w );
READ16_HANDLER( hd68k_dsk_ram_r );
WRITE16_HANDLER( hd68k_dsk_ram_w );
READ16_HANDLER( hd68k_dsk_zram_r );
WRITE16_HANDLER( hd68k_dsk_zram_w );
READ16_HANDLER( hd68k_dsk_small_rom_r );
READ16_HANDLER( hd68k_dsk_rom_r );
WRITE16_HANDLER( hd68k_dsk_dsp32_w );
READ16_HANDLER( hd68k_dsk_dsp32_r );
WRITE32_HANDLER( rddsp32_sync0_w );
WRITE32_HANDLER( rddsp32_sync1_w );

/* DSPCOM board */
WRITE16_HANDLER( hddspcom_control_w );

WRITE16_HANDLER( rd68k_slapstic_w );
READ16_HANDLER( rd68k_slapstic_r );

/* Game-specific protection */
WRITE16_HANDLER( st68k_sloop_w );
READ16_HANDLER( st68k_sloop_r );
READ16_HANDLER( st68k_sloop_alt_r );
WRITE16_HANDLER( st68k_protosloop_w );
READ16_HANDLER( st68k_protosloop_r );

/* GSP optimizations */
READ16_HANDLER( hdgsp_speedup_r );
WRITE16_HANDLER( hdgsp_speedup1_w );
WRITE16_HANDLER( hdgsp_speedup2_w );
READ16_HANDLER( rdgsp_speedup1_r );
WRITE16_HANDLER( rdgsp_speedup1_w );

/* MSP optimizations */
READ16_HANDLER( hdmsp_speedup_r );
WRITE16_HANDLER( hdmsp_speedup_w );
READ16_HANDLER( stmsp_speedup_r );

/* ADSP optimizations */
READ16_HANDLER( hdadsp_speedup_r );
READ16_HANDLER( hdds3_speedup_r );


/*----------- defined in sndhrdw/harddriv.c -----------*/

void hdsnd_init(void);


READ16_HANDLER( hd68k_snd_data_r );
READ16_HANDLER( hd68k_snd_status_r );
WRITE16_HANDLER( hd68k_snd_data_w );
WRITE16_HANDLER( hd68k_snd_reset_w );

READ16_HANDLER( hdsnd68k_data_r );
WRITE16_HANDLER( hdsnd68k_data_w );

READ16_HANDLER( hdsnd68k_switches_r );
READ16_HANDLER( hdsnd68k_320port_r );
READ16_HANDLER( hdsnd68k_status_r );

WRITE16_HANDLER( hdsnd68k_latches_w );
WRITE16_HANDLER( hdsnd68k_speech_w );
WRITE16_HANDLER( hdsnd68k_irqclr_w );

READ16_HANDLER( hdsnd68k_320ram_r );
WRITE16_HANDLER( hdsnd68k_320ram_w );
READ16_HANDLER( hdsnd68k_320ports_r );
WRITE16_HANDLER( hdsnd68k_320ports_w );
READ16_HANDLER( hdsnd68k_320com_r );
WRITE16_HANDLER( hdsnd68k_320com_w );

READ16_HANDLER( hdsnddsp_get_bio );

WRITE16_HANDLER( hdsnddsp_dac_w );
WRITE16_HANDLER( hdsnddsp_comport_w );
WRITE16_HANDLER( hdsnddsp_mute_w );
WRITE16_HANDLER( hdsnddsp_gen68kirq_w );
WRITE16_HANDLER( hdsnddsp_soundaddr_w );

READ16_HANDLER( hdsnddsp_rom_r );
READ16_HANDLER( hdsnddsp_comram_r );
READ16_HANDLER( hdsnddsp_compare_r );


/*----------- defined in vidhrdw/harddriv.c -----------*/


void hdgsp_write_to_shiftreg(UINT32 address, UINT16 *shiftreg);
void hdgsp_read_from_shiftreg(UINT32 address, UINT16 *shiftreg);
void hdgsp_display_update(UINT32 offs, int rowbytes, int scanline);

READ16_HANDLER( hdgsp_control_lo_r );
WRITE16_HANDLER( hdgsp_control_lo_w );
READ16_HANDLER( hdgsp_control_hi_r );
WRITE16_HANDLER( hdgsp_control_hi_w );

READ16_HANDLER( hdgsp_vram_2bpp_r );
WRITE16_HANDLER( hdgsp_vram_1bpp_w );
WRITE16_HANDLER( hdgsp_vram_2bpp_w );

READ16_HANDLER( hdgsp_paletteram_lo_r );
WRITE16_HANDLER( hdgsp_paletteram_lo_w );
READ16_HANDLER( hdgsp_paletteram_hi_r );
WRITE16_HANDLER( hdgsp_paletteram_hi_w );

