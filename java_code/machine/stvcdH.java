void cdb_build_ftree(void);
void cdb_build_toc(void);
void iso_reset(void);
void CD_com_update(UINT32 count);
UINT32 cdb_find_track(UINT32 fad);
UINT32 cdb_find_file(UINT32 fad);
void cdb_inject_file_info(UINT32 fid, UINT8 * dst);


#define CDB_SEND_REPORT()								\
		if((CD_status == CDB_STAT_BUSY) ||					\
		   (CD_status == CDB_STAT_STDBY) ||					\
		   (CD_status == CDB_STAT_OPEN) ||					\
		   (CD_status == CDB_STAT_NODISC) ||					\
		   (CD_status == CDB_STAT_REJECT)){					\
			CR1 = (CD_status << 8) | 0xff;				\
			CR2 = 0xffff;							\
			CR3 = 0xffff;							\
			CR4 = 0xffff;							\
		}else{									\
			CR1 = (CD_status << 8) | CD_flag | CD_repeat;	\
			CR2 = (CD_cur_ctrl << 8) | CD_cur_track;		\
			CR3 = (CD_cur_idx  << 8) | (CD_cur_fad >> 16);	\
			CR4 = CD_cur_fad;						\
		}



#define CDB_FLAG_CDROM		0x80		// on CDROM area (else CDDA, <SEEK> or <SCAN>)




#define CDB_SECT_NUM	200		// 200 sectors can be contained in the cd buffer
#define CDB_SEL_NUM	24		// 24 selectors (buffer partitions / filters) available
#define CDB_FID_NUM	1000		// number of total file ids (directory info) to keep track in the fid array

typedef struct filt_t {

	UINT8		true;		// true connection
	UINT8		false;		// false connection
	UINT8		mode;		// filter mode
	UINT32		fad;		// range start fad
	UINT32		range;		// range length in fads
	UINT8		chan;		// channel
	UINT8		fid;		// file id
	UINT8		sub_val;	// subheader value
	UINT8		sub_mask;	// subheader mask
	UINT8		cod_val;	// code value
	UINT8		cod_mask;	// code mask

} filt_t;


typedef struct track_t {

	UINT32		ctrl;		// track control
	UINT32		idx;		// track index
	UINT32		fad;		// track fad
	UINT32		min;		// track time, minute
	UINT32		sec;		// track time, second
	UINT32		fra;		// track time, frame
	UINT32		num;		// track number (just here for speed)

} track_t;


typedef struct sect_t {

	UINT8		dummy1[2048];	// for MSCDEX
	UINT8		data[2352];	// sector data
	UINT8		dummy2[2048];	// for MSCDEX
	UINT32		size;		// used space in bytes
	UINT32		fad;		// sector fad
	UINT8		fid;		// file id
	UINT8		chan;		// channel
	UINT8		sub;		// submode info
	UINT8		cod;		// code info

} sect_t;

typedef struct part_t {

	UINT32		size;				// partition size in bytes (also points to the last used sector)
	sect_t	* sect[CDB_SECT_NUM];	// sector info pointers

} part_t;


typedef struct toc_t {

	track_t	track[100];	// tracks (0 ~ 99)
	track_t	first;		// first track
	track_t	last;		// last track
	track_t	leadout;	// leadout (last session)

} toc_t;

typedef struct file_t {

	UINT32		fad;		// file data fad
	UINT32		size;		// file data size in bytes
	UINT8		attr;		// file attributes
	UINT8		unit;		// interleave unit
	UINT8		gap;		// interleave gap
	UINT8		name_len;	// file name length
	char		name[33];	// file name

} file_t;












////////////////////////////////////////////////////////////////



////////////////////////////////////////////////////////////////

//************
#define CDB_STAT_BUSY		0x00		// status change in progress
#define CDB_STAT_PAUSE		0x01		// temporarily stopped
#define CDB_STAT_STDBY		0x02		// stopped
#define CDB_STAT_PLAY		0x03		// play in progress
#define CDB_STAT_SEEK		0x04		// seeking
#define CDB_STAT_SCAN		0x05		// scanning
#define CDB_STAT_OPEN		0x06		// tray open
#define CDB_STAT_NODISC		0x07		// no disc
#define CDB_STAT_RETRY		0x08		// read retry in progress
#define CDB_STAT_ERROR		0x09		// read data error
#define CDB_STAT_FATAL		0x0a		// fatal error
#define CDB_STAT_PERI		0x20		// it is periodical response
#define CDB_STAT_TRNS		0x40		// transfer request
#define CDB_STAT_WAIT		0x80		// waiting
#define CDB_STAT_REJECT		0xff		// command rejected
//**********

#define CD_FLAG_CDROM		0x80		// on CDROM area (else CDDA, <SEEK> or <SCAN>)

#define CDB_FILTMODE_FID	0x01		// file number matters
#define CDB_FILTMODE_CHAN	0x02		// channel number matters
#define CDB_FILTMODE_SUB	0x04		// submode matters
#define CDB_FILTMODE_COD	0x08		// code info matters
#define CDB_FILTMODE_RANGE	0x40		// range matters

//************
#define HIRQ_CMOK			0x0001	// ready for command
#define HIRQ_DRDY			0x0002	// data transfer setup complete
#define HIRQ_CSCT			0x0004	// 1 sector stored
#define HIRQ_BFUL			0x0008	// buffer full
#define HIRQ_PEND			0x0010	// play ended
#define HIRQ_DCHG			0x0020	// disk changed
#define HIRQ_ESEL			0x0040	// soft reset/selector finished
#define HIRQ_EHST			0x0080	// host i/o finished
#define HIRQ_ECPY			0x0100	// sector copy/move finished
#define HIRQ_EFLS			0x0200	// block file system finished
#define HIRQ_SCDQ			0x0400	// subcode q decoded for current sector
#define HIRQ_MPED			0x0800	// mpeg enabled ?
#define HIRQ_MPCM			0x1000	// mpeg command ?
#define HIRQ_MPST			0x2000	// mpeg status ?
//***********












