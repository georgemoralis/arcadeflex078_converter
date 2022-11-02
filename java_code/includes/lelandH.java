/*************************************************************************

	Cinemat/Leland driver

*************************************************************************/

#define LELAND_BATTERY_RAM_SIZE 0x4000
#define ATAXX_EXTRA_TRAM_SIZE 0x800


/*----------- defined in machine/leland.c -----------*/

#define SERIAL_TYPE_NONE		0
#define SERIAL_TYPE_ADD			1
#define SERIAL_TYPE_ADD_XOR		2
#define SERIAL_TYPE_ENCRYPT		3
#define SERIAL_TYPE_ENCRYPT_XOR	4











void cerberus_bankswitch(void);
void mayhem_bankswitch(void);
void dangerz_bankswitch(void);
void basebal2_bankswitch(void);
void redline_bankswitch(void);
void viper_bankswitch(void);
void offroad_bankswitch(void);
void ataxx_bankswitch(void);

void leland_init_eeprom(UINT8 default_val, const UINT16 *data, UINT8 serial_offset, UINT8 serial_type);
void ataxx_init_eeprom(UINT8 default_val, const UINT16 *data, UINT8 serial_offset);









void leland_rotate_memory(int cpunum);


/*----------- defined in sndhrdw/leland.c -----------*/

int leland_sh_start(const struct MachineSound *msound);
void leland_sh_stop(void);
void leland_dac_update(int dacnum, UINT8 sample);

int leland_i186_sh_start(const struct MachineSound *msound);
int redline_i186_sh_start(const struct MachineSound *msound);

void leland_i186_sound_init(void);


void leland_i86_optimize_address(offs_t offset);






/*----------- defined in vidhrdw/leland.c -----------*/







