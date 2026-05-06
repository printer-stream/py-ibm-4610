"""_constants.py — All IBM 4610 SureMark constants.

Sourced from the official JavaPOS driver decompilation:
  IBM4610PrinterConst.java, IBM4610PrinterCmdConst.java,
  POSPrinterCmdConst.java, Cmd4610.java.
"""

# ---------------------------------------------------------------------------
# USB identifiers
# ---------------------------------------------------------------------------

VENDOR: int = 0x04B3
"""IBM/Toshiba USB vendor ID."""

PRODUCT_1NR: int = 0x4535
"""IBM 4610 SureMark USB product ID."""

IFACE: int = 1
"""HID interface number carrying report ID 0x01."""

REPORT_SIZE: int = 1022
"""Bytes per SET_REPORT (HID output) transfer."""

# ---------------------------------------------------------------------------
# Station constants  (POSPrinterCmdConst + Print4610CmdFactory)
# ---------------------------------------------------------------------------

STATION_NONE: int = 0x00
"""No station selected."""

STATION_RECEIPT: int = 0x02
"""CR — thermal receipt roll (front paper)."""

STATION_SLIP: int = 0x04
"""DIP — impact document / cheque / multi-part slip."""

STATION_LABEL: int = 0x14
"""DIL — landscape slip / label (station 20)."""

# ---------------------------------------------------------------------------
# Text alignment  (POSPrinterCmdConst)
# ---------------------------------------------------------------------------

ALIGN_LEFT: int = 0
ALIGN_CENTER: int = 1
ALIGN_RIGHT: int = 2

# ---------------------------------------------------------------------------
# Font face  (IBM4610PrinterCmdConst — receipt station)
# ---------------------------------------------------------------------------

FONT_A: int = 0
"""10×20 dot matrix font (receipt station)."""

FONT_B: int = 1
"""12×24 dot matrix font (receipt station)."""

FONT_C: int = 2
"""8×16 dot matrix font — thermal receipt only."""

# Slip / impact station font constants
SLIP_FONT_A: int = 0
SLIP_FONT_B: int = 1
SLIP_PAGE2: int = 2

# ---------------------------------------------------------------------------
# Rotation  (POSPrinterCmdConst)
# ---------------------------------------------------------------------------

ROTATE_NONE: int = 0
ROTATE_LEFT: int = 90    # PTR_RP_LEFT90
ROTATE_RIGHT: int = 270  # PTR_RP_RIGHT90
ROTATE_180: int = 180

# ---------------------------------------------------------------------------
# Barcode symbology  (POSPrinterCmdConst — GS k type byte)
# ---------------------------------------------------------------------------

BC_UPCA: int = 0
BC_UPCE: int = 1
BC_EAN13: int = 2
BC_EAN8: int = 3
BC_CODE39: int = 4
BC_ITF: int = 5
BC_CODABAR: int = 6
BC_CODE128: int = 7
BC_CODE93: int = 8
BC_CODE128A: int = 9   # Code128 automatic — length-prefixed in JavaPOS
BC_PDF417: int = 201   # PDF-417 — uses GS P instead of GS k

# Barcode HRI (human-readable interpretation) text position
HRI_NONE: int = 0
HRI_ABOVE: int = 1
HRI_BELOW: int = 2
HRI_BOTH: int = 3

# ---------------------------------------------------------------------------
# Flash sectors  (POSPrinterCmdConst / IBM4610PrinterCmdConst)
# ---------------------------------------------------------------------------

FLASH_DL_GRAPHICS: int = 1
"""Downloaded graphics sector."""

FLASH_PRE_MESSAGES: int = 2
"""Pre-stored messages sector."""

FLASH_USR_DEF_IMPACT_CHARSETS: int = 3
"""User-defined impact character sets sector."""

FLASH_USR_DEF_THERMAL_CHARSETS: int = 4
"""User-defined thermal character sets sector."""

FLASH_USR_FLA_STORAGE: int = 5
"""User flash-area storage sector."""

FLASH_ALL_DBCS: int = 6
"""All double-byte character sets sector."""

FLASH_CHECK_IMAGES: int = 8
"""Check images sector (IBM4610PrinterCmdConst.FLASH_SECTOR_EIGHT)."""

# ---------------------------------------------------------------------------
# Error-recovery flags  (IBM4610PrinterCmdConst.ER_*)
# Used as a bitmask argument to IBM4610.error_recovery()
# ---------------------------------------------------------------------------

ER_RELEASE_AFTER_CORRECTION: int = 0x01
"""Release print buffer after error correction."""

ER_AUTO_RETRY_AFTER_HOME_ERR: int = 0x02
"""Automatically retry after a home error."""

ER_WAIT_FOR_DOCUMENT: int = 0x04
"""Hold and wait for document present."""

ER_RELEASE_AFTER_FLIP_ERROR: int = 0x08
"""Release after a flip/print-side-change error."""

# ---------------------------------------------------------------------------
# Bitmap density  (POSPrinterCmdConst)
# ---------------------------------------------------------------------------

DENSITY_NORMAL: int = 0
"""Single-density 8-dot raster."""

DENSITY_DOUBLE: int = 1
"""Double-density 8-dot raster."""

DENSITY_DOUBLE_WIDTH_HEIGHT: int = 2
"""Double-density, double width and height."""

# ---------------------------------------------------------------------------
# Status response word constants  (IBM4610PrinterConst)
# The printer returns a 2-byte big-endian word on the interrupt IN endpoint.
# ---------------------------------------------------------------------------

STATUS_CMDLOADED: int = 0x0101
STATUS_CR_RIGHTHOME: int = 0x0102
STATUS_CR_LEFTHOME: int = 0x0104
STATUS_NORMAL: int = 0x0108
STATUS_RIBBON_COVER: int = 0x0120
STATUS_CR_ERROR: int = 0x0140
STATUS_CMDREJECT: int = 0x0180
STATUS_DI_READY: int = 0x0201
STATUS_DI_FRONT: int = 0x0202
STATUS_DI_TOP: int = 0x0204
STATUS_BUFFER_HELD: int = 0x0210
STATUS_OPEN_THROAT: int = 0x0220
STATUS_BUFFER_EMPTY: int = 0x0240
STATUS_BUFFER_FULL: int = 0x0280
STATUS_MEMORY_FULL: int = 0x0301
STATUS_HOME_ERROR: int = 0x0302
STATUS_DI_ERROR: int = 0x0304
STATUS_EPROM_MCT_ERR: int = 0x0308
STATUS_FLASH_FULL: int = 0x0320
STATUS_FIRMWARE_ERROR: int = 0x0340
STATUS_PRINTERID_DATA: int = 0x0501
STATUS_ECLEVEL_DATA: int = 0x0502
STATUS_MICR_DATA: int = 0x0504
STATUS_MCT_DATA: int = 0x0508
STATUS_IMAGE_SCAN_DONE: int = 0x0540
STATUS_IMAGE_DATA: int = 0x0580

# ---------------------------------------------------------------------------
# Statistics sub-command lookup table
# (Gen4610CmdFactory.getStatisticsMapping — Cmd4610 field byte values)
# Java signed byte → Python unsigned: negative_value + 256
# ---------------------------------------------------------------------------

STATISTIC_SUBCMDS: dict[str, bytes] = {
    "ManufactureDate":                       bytes([0x70]),
    "FormInsertionCount":                    bytes([0x8C]),
    "FormInsertionCountRemainder":           bytes([0x96]),
    "HomeErrorCount":                        bytes([0x8A]),
    "PaperCutCount":                         bytes([0x81]),
    "PaperCutCountRemainder":                bytes([0x93]),
    "FailedPaperCutCount":                   bytes([0x86]),
    "ReceiptCoverOpenCount":                 bytes([0x85]),
    "SlipCharacterPrintedCount":             bytes([0x87]),
    "SlipCharacterPrintedCountRemainder":    bytes([0x94]),
    "SlipCoverOpenCount":                    bytes([0x8B]),
    "ReceiptCharacterPrintedCountRemainder": bytes([0x82]),
    "ReceiptCharacterPrintedCount":          bytes([0x83]),
    "PrintSideChangeCount":                  bytes([0x90]),
    "PrintSideChangeCountRemainder":         bytes([0x9A]),
    "FailedPrintSideChangeCount":            bytes([0x91]),
    "FailedPrintSideChangeCountRemainder":   bytes([0x98]),
    "ReceiptLineFeedCount":                  bytes([0x84]),
    "ReceiptLineFeedCountRemainder":         bytes([0x92]),
    "SlipLineFeedCount":                     bytes([0x88]),
    "SlipLineFeedCountRemainder":            bytes([0x95]),
    "BarcodePrintedCount":                   bytes([0xD8]),
    "BarcodePrintedCountRemainder":          bytes([0x9D]),
    "MaximumTempReachedCount":               bytes([0xD9]),
    "NVRAMWriteCount":                       bytes([0xD2]),
    "FailedReadCount":                       bytes([0x8F]),
    "FailedReadCountRemainder":              bytes([0x99]),
    "TotalReadCount":                        bytes([0x8D]),
    "TotalReadCountRemainder":               bytes([0x9B]),
    "IBM_CheckScannedCount":                 bytes([0xD3]),
    "IBM_CheckScannedCountRemainder":        bytes([0x9E]),
    "IBM_CheckScannerBrightnessQuality":     bytes([0x6D]),
    "IBM_CheckScannerContrastQuality":       bytes([0x6D]),
    "IBM_CheckScannerFocusQuality":          bytes([0x6E]),
    "IBM_ChecksFailedQualityCount":          bytes([0xD4]),
}
