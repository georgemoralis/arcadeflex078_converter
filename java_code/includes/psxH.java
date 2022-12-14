/***************************************************************************

  includes/psx.h

***************************************************************************/

#if !defined( PSX_H )

/* vidhrdw */
READ32_HANDLER( psx_gpu_r );
WRITE32_HANDLER( psx_gpu_w );

/* machine */
typedef void ( *psx_dma_read_handler )( UINT32, INT32 );
typedef void ( *psx_dma_write_handler )( UINT32, INT32 );
WRITE32_HANDLER( psx_irq_w );
READ32_HANDLER( psx_irq_r );
WRITE32_HANDLER( psx_dma_w );
READ32_HANDLER( psx_dma_r );
WRITE32_HANDLER( psx_counter_w );
READ32_HANDLER( psx_counter_r );
WRITE32_HANDLER( psx_sio_w );
READ32_HANDLER( psx_sio_r );
typedef void ( *psx_sio_write_handler )( data8_t );
WRITE32_HANDLER( psx_mdec_w );
READ32_HANDLER( psx_mdec_r );

#define PSX_H ( 1 )
#endif
