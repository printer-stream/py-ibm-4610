"""py_ibm_4610 — IBM / Toshiba 4610 SureMark printer library.

Quick start::

    from py_ibm_4610 import IBM4610, STATION_RECEIPT

    with IBM4610() as p:
        p.select_station(STATION_RECEIPT)
        p.bold(True).text("Hello, World!\\n").bold(False)
        p.feed(4).cut()

Reference page demo::

    from py_ibm_4610 import IBM4610, ReferencePage

    with IBM4610() as p:
        ReferencePage(p).print()
"""

from .version import __version__

# Printer classes
from .printer.ibm4610     import IBM4610
from .printer.ibm4610_1nr import IBM4610_1NR

# Reference / demo page
from .ref_page import ReferencePage

# Constants — USB
from ._constants import (
    VENDOR,
    PRODUCT_1NR,
    IFACE,
    REPORT_SIZE,
)

# Constants — stations
from ._constants import (
    STATION_NONE,
    STATION_RECEIPT,
    STATION_SLIP,
    STATION_LABEL,
)

# Constants — text
from ._constants import (
    ALIGN_LEFT,
    ALIGN_CENTER,
    ALIGN_RIGHT,
    FONT_A,
    FONT_B,
    FONT_C,
    SLIP_FONT_A,
    SLIP_FONT_B,
    SLIP_PAGE2,
    ROTATE_NONE,
    ROTATE_LEFT,
    ROTATE_RIGHT,
    ROTATE_180,
)

from ._constants import (
    ALIGN_CENTER as ALIGN_CENTRE,
)

# Constants — barcodes
from ._constants import (
    BC_UPCA,
    BC_UPCE,
    BC_EAN13,
    BC_EAN8,
    BC_CODE39,
    BC_ITF,
    BC_CODABAR,
    BC_CODE128,
    BC_CODE93,
    BC_CODE128A,
    BC_PDF417,
    HRI_NONE,
    HRI_ABOVE,
    HRI_BELOW,
    HRI_BOTH,
    QR_MODE_BYTE,
    QR_MODE_ALPHANUM,
    QR_MODE_NUMERIC,
    QR_MODE_KANJI,
    QR_MODE_ECI,
    QR_MODE_MIXING,
    QR_EC_L,
    QR_EC_M,
    QR_EC_Q,
    QR_EC_H,
)

# Constants — graphics
from ._constants import (
    DENSITY_NORMAL,
    DENSITY_DOUBLE,
    DENSITY_DOUBLE_WIDTH_HEIGHT,
)

# Constants — flash sectors
from ._constants import (
    FLASH_DL_GRAPHICS,
    FLASH_PRE_MESSAGES,
    FLASH_USR_DEF_IMPACT_CHARSETS,
    FLASH_USR_DEF_THERMAL_CHARSETS,
    FLASH_USR_FLA_STORAGE,
    FLASH_ALL_DBCS,
    FLASH_CHECK_IMAGES,
)

# Constants — error recovery
from ._constants import (
    ER_RELEASE_AFTER_CORRECTION,
    ER_AUTO_RETRY_AFTER_HOME_ERR,
    ER_WAIT_FOR_DOCUMENT,
    ER_RELEASE_AFTER_FLIP_ERROR,
)

# Constants — status words
from ._constants import (
    STATUS_CMDLOADED,
    STATUS_CR_RIGHTHOME,
    STATUS_CR_LEFTHOME,
    STATUS_NORMAL,
    STATUS_RIBBON_COVER,
    STATUS_CR_ERROR,
    STATUS_CMDREJECT,
    STATUS_DI_READY,
    STATUS_DI_FRONT,
    STATUS_DI_TOP,
    STATUS_BUFFER_HELD,
    STATUS_OPEN_THROAT,
    STATUS_BUFFER_EMPTY,
    STATUS_BUFFER_FULL,
    STATUS_MEMORY_FULL,
    STATUS_HOME_ERROR,
    STATUS_DI_ERROR,
    STATUS_EPROM_MCT_ERR,
    STATUS_FLASH_FULL,
    STATUS_FIRMWARE_ERROR,
    STATUS_PRINTERID_DATA,
    STATUS_ECLEVEL_DATA,
    STATUS_MICR_DATA,
    STATUS_MCT_DATA,
    STATUS_IMAGE_SCAN_DONE,
    STATUS_IMAGE_DATA,
)

# Statistics sub-command table
from ._constants import STATISTIC_SUBCMDS

# Low-level transport helper (for advanced / custom use)
from ._transport import make_packet, MAX_PAYLOAD

__all__ = [
    # version
    "__version__",
    # classes
    "IBM4610",
    "IBM4610_1NR",
    "ReferencePage",
    # USB
    "VENDOR", "PRODUCT_1NR", "IFACE", "REPORT_SIZE",
    # stations
    "STATION_NONE", "STATION_RECEIPT", "STATION_SLIP", "STATION_LABEL",
    # text
    "ALIGN_LEFT", "ALIGN_CENTER", "ALIGN_RIGHT",
    "ALIGN_CENTRE",
    "FONT_A", "FONT_B", "FONT_C",
    "SLIP_FONT_A", "SLIP_FONT_B", "SLIP_PAGE2",
    "ROTATE_NONE", "ROTATE_LEFT", "ROTATE_RIGHT", "ROTATE_180",
    # barcodes
    "BC_UPCA", "BC_UPCE", "BC_EAN13", "BC_EAN8", "BC_CODE39",
    "BC_ITF", "BC_CODABAR", "BC_CODE128", "BC_CODE93",
    "BC_CODE128A", "BC_PDF417",
    "HRI_NONE", "HRI_ABOVE", "HRI_BELOW", "HRI_BOTH",
    # graphics
    "DENSITY_NORMAL", "DENSITY_DOUBLE", "DENSITY_DOUBLE_WIDTH_HEIGHT",
    # flash
    "FLASH_DL_GRAPHICS", "FLASH_PRE_MESSAGES",
    "FLASH_USR_DEF_IMPACT_CHARSETS", "FLASH_USR_DEF_THERMAL_CHARSETS",
    "FLASH_USR_FLA_STORAGE", "FLASH_ALL_DBCS", "FLASH_CHECK_IMAGES",
    # error recovery
    "ER_RELEASE_AFTER_CORRECTION", "ER_AUTO_RETRY_AFTER_HOME_ERR",
    "ER_WAIT_FOR_DOCUMENT", "ER_RELEASE_AFTER_FLIP_ERROR",
    # status words
    "STATUS_CMDLOADED", "STATUS_CR_RIGHTHOME", "STATUS_CR_LEFTHOME",
    "STATUS_NORMAL", "STATUS_RIBBON_COVER", "STATUS_CR_ERROR",
    "STATUS_CMDREJECT", "STATUS_DI_READY", "STATUS_DI_FRONT",
    "STATUS_DI_TOP", "STATUS_BUFFER_HELD", "STATUS_OPEN_THROAT",
    "STATUS_BUFFER_EMPTY", "STATUS_BUFFER_FULL", "STATUS_MEMORY_FULL",
    "STATUS_HOME_ERROR", "STATUS_DI_ERROR", "STATUS_EPROM_MCT_ERR",
    "STATUS_FLASH_FULL", "STATUS_FIRMWARE_ERROR", "STATUS_PRINTERID_DATA",
    "STATUS_ECLEVEL_DATA", "STATUS_MICR_DATA", "STATUS_MCT_DATA",
    "STATUS_IMAGE_SCAN_DONE", "STATUS_IMAGE_DATA",
    # statistics
    "STATISTIC_SUBCMDS",
    # transport
    "make_packet", "MAX_PAYLOAD",
]
