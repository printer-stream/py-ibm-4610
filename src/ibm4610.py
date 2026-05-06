"""ibm4610.py — IBM / Toshiba 4610 SureMark printer driver for Python.

Extracted from the official JavaPOS driver (posj.jar) decompilation:
  - Cmd4610.java            — raw ESC/POS byte sequences
  - Gen4610CmdFactory.java  — general command construction
  - Font4610CmdFactory.java — font / text attribute commands
  - Grap4610CmdFactory.java — graphics / barcode commands
  - Print4610CmdFactory.java — station routing
  - IBM4610PrinterConst.java / IBM4610PrinterCmdConst.java — constants

Transport: USB HID  (VID=0x04b3, PID=0x4535)
  Interface 1, Report ID 0x01, Report size 1022 bytes.
  Packet framing matches PrinterPacket$Transport.transport().

Dependencies:
  pip install pyusb

Quick start::

    with IBM4610() as p:
        p.select_station(STATION_RECEIPT)
        p.bold(True)
        p.text("Hello World\\n")
        p.bold(False)
        p.feed(4)
        p.cut()

All high-level methods return *self* so calls can be chained.
Raw bytes are accumulated in an internal buffer and only sent
when :meth:`flush` is called (or by the convenience helpers that call it).
"""

from __future__ import annotations

import usb.core
import usb.util

# ---------------------------------------------------------------------------
# USB identifiers
# ---------------------------------------------------------------------------
VENDOR       = 0x04B3
PRODUCT      = 0x4535
IFACE        = 1          # HID interface carrying report ID 0x01
REPORT_SIZE  = 1022       # bytes per SET_REPORT transfer

# ---------------------------------------------------------------------------
# Station constants  (POSPrinterCmdConst + Print4610CmdFactory)
# ---------------------------------------------------------------------------
STATION_NONE     = 0x00
STATION_RECEIPT  = 0x02   # CR  — thermal receipt roll
STATION_SLIP     = 0x04   # DIP — impact document / check / slip
STATION_LABEL    = 0x14   # DIL — landscape slip / label (station 20)

# ---------------------------------------------------------------------------
# Text alignment  (POSPrinterCmdConst)
# ---------------------------------------------------------------------------
ALIGN_LEFT   = 0
ALIGN_CENTER = 1
ALIGN_RIGHT  = 2

# ---------------------------------------------------------------------------
# Font face  (IBM4610PrinterCmdConst — receipt station)
# ---------------------------------------------------------------------------
FONT_A = 0   # 10×20 dots
FONT_B = 1   # 12×24 dots
FONT_C = 2   # 8×16  dots  (thermal only)

# Slip station font constants
SLIP_FONT_A = 0
SLIP_FONT_B = 1
SLIP_PAGE2  = 2

# ---------------------------------------------------------------------------
# Rotation  (POSPrinterCmdConst)
# ---------------------------------------------------------------------------
ROTATE_NONE  = 0
ROTATE_LEFT  = 90    # PTR_RP_LEFT90
ROTATE_RIGHT = 270   # PTR_RP_RIGHT90
ROTATE_180   = 180

# ---------------------------------------------------------------------------
# Barcode symbology  (POSPrinterCmdConst → GS k type byte)
# ---------------------------------------------------------------------------
BC_UPCA     = 0
BC_UPCE     = 1
BC_EAN13    = 2
BC_EAN8     = 3
BC_CODE39   = 4
BC_ITF      = 5
BC_CODABAR  = 6
BC_CODE128  = 7
BC_CODE93   = 8
BC_CODE128A = 9   # Code128 automatic (length-prefixed in JavaPOS)
BC_PDF417   = 201 # special handling — GS P

# Barcode HRI text position
HRI_NONE  = 0
HRI_ABOVE = 1
HRI_BELOW = 2
HRI_BOTH  = 3

# ---------------------------------------------------------------------------
# Flash sector  (POSPrinterCmdConst / IBM4610PrinterCmdConst)
# ---------------------------------------------------------------------------
FLASH_DL_GRAPHICS              = 1   # downloaded graphics
FLASH_PRE_MESSAGES             = 2   # pre-stored messages
FLASH_USR_DEF_IMPACT_CHARSETS  = 3   # user-defined impact char sets
FLASH_USR_DEF_THERMAL_CHARSETS = 4   # user-defined thermal char sets
FLASH_USR_FLA_STORAGE          = 5   # user flash-area storage
FLASH_ALL_DBCS                 = 6   # all double-byte char sets
FLASH_CHECK_IMAGES             = 8   # IBM4610PrinterCmdConst.FLASH_SECTOR_EIGHT

# ---------------------------------------------------------------------------
# Error-recovery flags  (IBM4610PrinterCmdConst.ER_*)
# Used as a bitmask argument to error_recovery()
# ---------------------------------------------------------------------------
ER_RELEASE_AFTER_CORRECTION  = 0x01
ER_AUTO_RETRY_AFTER_HOME_ERR = 0x02
ER_WAIT_FOR_DOCUMENT         = 0x04
ER_RELEASE_AFTER_FLIP_ERROR  = 0x08

# ---------------------------------------------------------------------------
# Bitmap density  (POSPrinterCmdConst)
# ---------------------------------------------------------------------------
DENSITY_NORMAL            = 0
DENSITY_DOUBLE            = 1
DENSITY_DOUBLE_WIDTH_HEIGHT = 2

# ---------------------------------------------------------------------------
# Status response word constants  (IBM4610PrinterConst)
# The printer returns a 2-byte big-endian word; compare against these.
# ---------------------------------------------------------------------------
STATUS_CMDLOADED       = 0x0101
STATUS_CR_RIGHTHOME    = 0x0102
STATUS_CR_LEFTHOME     = 0x0104
STATUS_NORMAL          = 0x0108
STATUS_RIBBON_COVER    = 0x0120
STATUS_CR_ERROR        = 0x0140
STATUS_CMDREJECT       = 0x0180
STATUS_DI_READY        = 0x0201
STATUS_DI_FRONT        = 0x0202
STATUS_DI_TOP          = 0x0204
STATUS_BUFFER_HELD     = 0x0210
STATUS_OPEN_THROAT     = 0x0220
STATUS_BUFFER_EMPTY    = 0x0240
STATUS_BUFFER_FULL     = 0x0280
STATUS_MEMORY_FULL     = 0x0301
STATUS_HOME_ERROR      = 0x0302
STATUS_DI_ERROR        = 0x0304
STATUS_EPROM_MCT_ERR   = 0x0308
STATUS_FLASH_FULL      = 0x0320
STATUS_FIRMWARE_ERROR  = 0x0340
STATUS_PRINTERID_DATA  = 0x0501
STATUS_ECLEVEL_DATA    = 0x0502
STATUS_MICR_DATA       = 0x0504
STATUS_MCT_DATA        = 0x0508
STATUS_IMAGE_SCAN_DONE = 0x0540
STATUS_IMAGE_DATA      = 0x0580

# ---------------------------------------------------------------------------
# Statistics sub-command lookup table
# (Gen4610CmdFactory.getStatisticsMapping — Cmd4610 field byte values)
# Java signed byte → Python unsigned: negative_value + 256
# ---------------------------------------------------------------------------
STATISTIC_SUBCMDS: dict[str, bytes] = {
    "ManufactureDate":                       bytes([0x70]),  # 112
    "FormInsertionCount":                    bytes([0x8C]),  # -116
    "FormInsertionCountRemainder":           bytes([0x96]),  # -106
    "HomeErrorCount":                        bytes([0x8A]),  # -118
    "PaperCutCount":                         bytes([0x81]),  # -127
    "PaperCutCountRemainder":                bytes([0x93]),  # -109
    "FailedPaperCutCount":                   bytes([0x86]),  # -122
    "ReceiptCoverOpenCount":                 bytes([0x85]),  # -123
    "SlipCharacterPrintedCount":             bytes([0x87]),  # -121
    "SlipCharacterPrintedCountRemainder":    bytes([0x94]),  # -108
    "SlipCoverOpenCount":                    bytes([0x8B]),  # -117
    "ReceiptCharacterPrintedCountRemainder": bytes([0x82]),  # -126
    "ReceiptCharacterPrintedCount":          bytes([0x83]),  # -125
    "PrintSideChangeCount":                  bytes([0x90]),  # -112
    "PrintSideChangeCountRemainder":         bytes([0x9A]),  # -102
    "FailedPrintSideChangeCount":            bytes([0x91]),  # -111
    "FailedPrintSideChangeCountRemainder":   bytes([0x98]),  # -104
    "ReceiptLineFeedCount":                  bytes([0x84]),  # -124
    "ReceiptLineFeedCountRemainder":         bytes([0x92]),  # -110
    "SlipLineFeedCount":                     bytes([0x88]),  # -120
    "SlipLineFeedCountRemainder":            bytes([0x95]),  # -107
    "BarcodePrintedCount":                   bytes([0xD8]),  # -40
    "BarcodePrintedCountRemainder":          bytes([0x9D]),  # -99
    "MaximumTempReachedCount":               bytes([0xD9]),  # -39
    "NVRAMWriteCount":                       bytes([0xD2]),  # -46
    "FailedReadCount":                       bytes([0x8F]),  # -113
    "FailedReadCountRemainder":              bytes([0x99]),  # -103
    "TotalReadCount":                        bytes([0x8D]),  # -115
    "TotalReadCountRemainder":               bytes([0x9B]),  # -101
    "IBM_CheckScannedCount":                 bytes([0xD3]),  # -45
    "IBM_CheckScannedCountRemainder":        bytes([0x9E]),  # -98
    "IBM_CheckScannerBrightnessQuality":     bytes([0x6D]),  # 109
    "IBM_CheckScannerContrastQuality":       bytes([0x6D]),  # 109
    "IBM_CheckScannerFocusQuality":          bytes([0x6E]),  # 110
    "IBM_ChecksFailedQualityCount":          bytes([0xD4]),  # -44
}


# ---------------------------------------------------------------------------
# HID packet framing
# ---------------------------------------------------------------------------

def make_packet(data: bytes) -> bytes:
    """Wrap *data* in the IBM 4610 HID report frame (1022 bytes).

    Reverse-engineered from ``PrinterPacket$Transport.transport()``:

    .. code-block:: text

        total_len = 7 + len(data)
        len_field = total_len - 3
        cmd       = 0x01 if len_field >= 255 else 0x02
        header    = [cmd,
                     len_field & 0xFF, (len_field >> 8) & 0xFF,
                     0x01, 0x00, 0x00, 0x00]

    The resulting header+data is zero-padded to REPORT_SIZE bytes.
    """
    N         = len(data)
    len_field = (7 + N) - 3
    cmd       = 0x01 if len_field >= 255 else 0x02
    header    = bytes([
        cmd,
        len_field & 0xFF,
        (len_field >> 8) & 0xFF,
        0x01,
        0x00,
        0x00,
        0x00,
    ])
    return (header + data).ljust(REPORT_SIZE, b'\x00')


# ---------------------------------------------------------------------------
# Main driver class
# ---------------------------------------------------------------------------

class IBM4610:
    """Full-capability driver for the IBM / Toshiba 4610 SureMark printer.

    Commands are accumulated in an internal buffer with :meth:`write` and
    dispatched to the printer with :meth:`flush`.  All chainable methods
    return *self*.  Use :meth:`send_raw` to bypass buffering.

    Example::

        with IBM4610() as p:
            p.select_station(STATION_RECEIPT)
            p.bold(True)
            p.text("Hello World\\n")
            p.bold(False)
            p.feed(4)
            p.cut()
    """

    def __init__(
        self,
        vendor:      int = VENDOR,
        product:     int = PRODUCT,
        iface:       int = IFACE,
        report_size: int = REPORT_SIZE,
    ) -> None:
        self._vendor      = vendor
        self._product     = product
        self._iface       = iface
        self._report_size = report_size
        self._dev         = None
        self._buf         = bytearray()

    # ------------------------------------------------------------------
    # Context-manager / open / close
    # ------------------------------------------------------------------

    def open(self) -> "IBM4610":
        """Open USB connection to the printer."""
        self._dev = usb.core.find(idVendor=self._vendor, idProduct=self._product)
        if self._dev is None:
            raise RuntimeError(
                f"IBM 4610 printer not found "
                f"(VID={self._vendor:#06x}, PID={self._product:#06x})"
            )
        if self._dev.is_kernel_driver_active(self._iface):
            self._dev.detach_kernel_driver(self._iface)
        usb.util.claim_interface(self._dev, self._iface)
        return self

    def close(self) -> None:
        """Release the USB interface and re-attach the kernel driver."""
        if self._dev is not None:
            try:
                usb.util.release_interface(self._dev, self._iface)
                self._dev.attach_kernel_driver(self._iface)
            except Exception:
                pass
            self._dev = None

    def __enter__(self) -> "IBM4610":
        return self.open()

    def __exit__(self, *_) -> None:
        self.close()

    # ------------------------------------------------------------------
    # Low-level transport
    # ------------------------------------------------------------------

    def send_raw(self, data: bytes) -> int:
        """Frame *data* as an HID report and send it immediately.

        Returns the number of bytes actually transferred.
        """
        if self._dev is None:
            raise RuntimeError("Printer not open — call open() first.")
        pkt = make_packet(data)
        return self._dev.ctrl_transfer(
            bmRequestType=0x21,    # HID, host→device, interface
            bRequest=0x09,         # SET_REPORT
            wValue=0x0201,         # Output report, Report ID 1
            wIndex=self._iface,
            data_or_wLength=pkt,
            timeout=5000,
        )

    # ------------------------------------------------------------------
    # Buffered build / flush helpers
    # ------------------------------------------------------------------

    def write(self, data: bytes) -> "IBM4610":
        """Append raw *data* to the internal send buffer (not sent yet)."""
        self._buf.extend(data)
        return self

    def flush(self) -> int:
        """Send everything accumulated with :meth:`write`, then clear the buffer.

        Data is chunked into MAX_PAYLOAD-byte pieces because each HID
        SET_REPORT carries exactly REPORT_SIZE (1022) bytes — 7 header bytes
        leaves 1015 bytes of payload per packet.
        """
        MAX_PAYLOAD = REPORT_SIZE - 7  # 1015 bytes of ESC/POS per packet
        data = bytes(self._buf)
        self._buf.clear()
        total = 0
        for offset in range(0, max(len(data), 1), MAX_PAYLOAD):
            chunk = data[offset : offset + MAX_PAYLOAD]
            total += self.send_raw(chunk)
        return total

    def build(self) -> bytes:
        """Return the buffered bytes without sending, then clear the buffer."""
        data = bytes(self._buf)
        self._buf.clear()
        return data

    # ------------------------------------------------------------------
    # Station / printer control  (Gen4610CmdFactory + Print4610CmdFactory)
    # ------------------------------------------------------------------

    def reset(self) -> "IBM4610":
        """Full hardware reset — Cmd4610.RESET = [0x00, 0x40, 0x00]."""
        return self.write(bytes([0x00, 0x40, 0x00]))

    def reinit(self) -> "IBM4610":
        """Software re-initialise receipt station — ESC @ (0x1B 0x40)."""
        return self.write(bytes([0x1B, 0x40]))

    def select_station(self, station: int) -> "IBM4610":
        """Select the active print station and configure print mode.

        The 4610 has three physically separate print mechanisms, each
        requiring its own station-select sequence before sending data:

        * ``STATION_RECEIPT`` (0x02) -- Thermal roll receipt printer.
          The main paper roll on the front of the unit.  Used for
          standard customer receipts.  Sends CR_COMM + CR_SETTINGS
          (ESC c 0 2, ESC c 1 2).

        * ``STATION_SLIP`` (0x04) -- Impact document / slip station (DIP).
          Accepts inserted paper documents such as cheques, vouchers or
          multi-part forms.  Requires the paper to be inserted into the
          document slot first (see ``open_jaws`` / ``begin_insertion``).
          Sends DIP_COMM + DIP_SETTINGS (ESC c 0 4, ESC c 1 4).

        * ``STATION_LABEL`` (0x14) -- Landscape slip / label station (DIL).
          A second document-insert path oriented in landscape mode, used
          for pre-cut labels or wide documents.  Sends DIL_COMM
          (ESC c 0 8).

        Mirrors ``Print4610CmdFactory.setCmdsStation()``.
        """
        if station == STATION_RECEIPT:
            return self.write(bytes([
                0x1B, 0x63, 0x30, 0x02,   # CR_COMM
                0x1B, 0x63, 0x31, 0x02,   # CR_SETTINGS
            ]))
        if station == STATION_SLIP:
            return self.write(bytes([
                0x1B, 0x63, 0x30, 0x04,   # DIP_COMM
                0x1B, 0x63, 0x31, 0x04,   # DIP_SETTINGS
            ]))
        if station == STATION_LABEL:
            return self.write(bytes([0x1B, 0x63, 0x30, 0x08]))  # DIL_COMM
        raise ValueError(f"Unknown station: {station:#x}")

    def status_request(self) -> "IBM4610":
        """Request printer status — STATUS_REQUEST = ESC NUL SP NUL."""
        return self.write(bytes([0x1B, 0x00, 0x20, 0x00]))

    def device_info(self, buffered: bool = False) -> "IBM4610":
        """Request device / firmware info.

        * ``buffered=False`` → DEVICE_INFO      [0x00, 0x00, 0x01]
        * ``buffered=True``  → BUFFERED_DEV_INFO [0x1B, 0x00, 0x00, 0x01]
        """
        if buffered:
            return self.write(bytes([0x1B, 0x00, 0x00, 0x01]))
        return self.write(bytes([0x00, 0x00, 0x01]))

    def ec_level_request(self, buffered: bool = True) -> "IBM4610":
        """Request EC (error-correction) level.

        * ``buffered=True``  → BUFFERED_EC  [0x1B, 0x00, 0x80, 0x00]
        * ``buffered=False`` → IMMEDIATE_EC [0x00, 0x80, 0x00]
        """
        if buffered:
            return self.write(bytes([0x1B, 0x00, 0x80, 0x00]))
        return self.write(bytes([0x00, 0x80, 0x00]))

    def hold_buffer(self) -> "IBM4610":
        """Hold the print buffer — HOLD_BUFFER = ESC 7."""
        return self.write(bytes([0x1B, 0x37]))

    def release_buffer(self) -> "IBM4610":
        """Release (resume) the print buffer — RELEASE_BUFFER = DLE ENQ 1."""
        return self.write(bytes([0x10, 0x05, 0x31]))

    def cancel_buffer(self) -> "IBM4610":
        """Cancel (clear) the print buffer — CANCEL_BUFFER = DLE ENQ 2."""
        return self.write(bytes([0x10, 0x05, 0x32]))

    def test_request(self) -> "IBM4610":
        """Send the self-test request packet — TEST_REQ."""
        return self.write(bytes([0x00, 0x00, 0x00, 0x02, 0x6F, 0x6B, 0x0A]))

    def status_sent(
        self,
        buff_empty:   bool = True,
        front_di:     bool = True,
        top_di:       bool = True,
        line_cnt:     bool = True,
        cd_change:    bool = True,
        key_change:   bool = True,
        cover_open:   bool = True,
    ) -> "IBM4610":
        """Configure which events trigger a STATUS_SENT notification.

        Mirrors ``Gen4610CmdFactory.createStatusSentCmd()``.
        Each flag, when *True*, enables the corresponding notification.
        Flags map to bits in the parameter byte (inverted: 0 = enabled).

        STATUS_SENT = ESC ) <flags>
        """
        sbyte = 0
        if not buff_empty:  sbyte |= 0x01
        if not front_di:    sbyte |= 0x04
        if not top_di:      sbyte |= 0x08
        if not line_cnt:    sbyte |= 0x10
        if not cd_change:   sbyte |= 0x20
        if not key_change:  sbyte |= 0x40
        if not cover_open:  sbyte |= 0x80
        return self.write(bytes([0x1B, 0x29, sbyte & 0xFF]))

    # ------------------------------------------------------------------
    # Text output
    # ------------------------------------------------------------------

    def text(self, s: str, encoding: str = "cp437") -> "IBM4610":
        """Encode *s* with *encoding* and append to the send buffer."""
        return self.write(s.encode(encoding))

    def raw_bytes(self, data: bytes) -> "IBM4610":
        """Append raw bytes directly to the send buffer."""
        return self.write(data)

    def lf(self) -> "IBM4610":
        """Line feed (0x0A)."""
        return self.write(b'\n')

    def crlf(self) -> "IBM4610":
        """Carriage return + line feed (0x0D 0x0A)."""
        return self.write(b'\r\n')

    # ------------------------------------------------------------------
    # Text formatting  (Font4610CmdFactory)
    # ------------------------------------------------------------------

    def bold(self, on: bool) -> "IBM4610":
        """Enable/disable bold (emphasize) — ESC G n."""
        return self.write(bytes([0x1B, 0x47, 0x01 if on else 0x00]))

    def underline(self, on: bool) -> "IBM4610":
        """Enable/disable underline — ESC - n  (0 = off, 1 = single dot)."""
        return self.write(bytes([0x1B, 0x2D, 0x01 if on else 0x00]))

    def double_wide(self, on: bool) -> "IBM4610":
        """Enable/disable double-wide mode — ESC W n."""
        return self.write(bytes([0x1B, 0x57, 0x01 if on else 0x00]))

    def double_high(self, on: bool) -> "IBM4610":
        """Enable/disable double-high mode — ESC h n."""
        return self.write(bytes([0x1B, 0x68, 0x01 if on else 0x00]))

    def double_wide_high(self, on: bool) -> "IBM4610":
        """Enable/disable both double-wide and double-high simultaneously."""
        self.double_wide(on)
        return self.double_high(on)

    def reverse_video(self, on: bool) -> "IBM4610":
        """Enable/disable reverse video (inverted chars) — ESC H n."""
        return self.write(bytes([0x1B, 0x48, 0x01 if on else 0x00]))

    def normal_mode(self) -> "IBM4610":
        """Reset all text attributes to normal.

        Sequence matches ``Cmd4610.NORMAL_MODE``::

            ESC _ 0   superscript off
            ESC G 0   bold off
            ESC W 0   double-wide off
            ESC h 0   double-high off
            ESC H 0   reverse-video off
            ESC - 0   underline off
        """
        return self.write(bytes([
            0x1B, 0x5F, 0x00,
            0x1B, 0x47, 0x00,
            0x1B, 0x57, 0x00,
            0x1B, 0x68, 0x00,
            0x1B, 0x48, 0x00,
            0x1B, 0x2D, 0x00,
        ]))

    def scale_font(self, width: int = 0, height: int = 0) -> "IBM4610":
        """Scale font — GS ! n  (new-model command, replaces double-wide/high).

        *width* and *height* are 0–7 (0 = normal, 1 = 2×, …, 7 = 8×).
        Packed as ``(width << 4) | height`` in the parameter byte.
        Mirrors ``Font4610CmdFactory.createScaleFontCmd()``.
        """
        n = ((width & 0x0F) << 4) | (height & 0x0F)
        return self.write(bytes([0x1D, 0x21, n]))

    def select_font(self, font: int) -> "IBM4610":
        """Select font face — ESC ! n.

        *font*: ``FONT_A`` (0), ``FONT_B`` (1), ``FONT_C`` (2)
        """
        return self.write(bytes([0x1B, 0x21, font & 0xFF]))

    def alignment(self, align: int) -> "IBM4610":
        """Set text alignment — ESC a n.

        *align*: ``ALIGN_LEFT`` (0), ``ALIGN_CENTER`` (1), ``ALIGN_RIGHT`` (2)
        """
        return self.write(bytes([0x1B, 0x61, align & 0xFF]))

    def font_color(self, color: int) -> "IBM4610":
        """Select font color — ESC r n.

        0 = black, 1 = red (dual-color thermal ribbon printers only).
        """
        return self.write(bytes([0x1B, 0x72, color & 0xFF]))

    def color_select_mode(self, mode: int) -> "IBM4610":
        """Color select mode — GS ; n (FONT_COLOR_MODE)."""
        return self.write(bytes([0x1D, 0x3B, mode & 0xFF]))

    def rotate_90(self, on: bool) -> "IBM4610":
        """90-degree character rotation — ESC V n."""
        return self.write(bytes([0x1B, 0x56, 0x01 if on else 0x00]))

    def rotate_180(self, on: bool) -> "IBM4610":
        """180-degree character rotation — ESC { n."""
        return self.write(bytes([0x1B, 0x7B, 0x01 if on else 0x00]))

    def upside_down(self) -> "IBM4610":
        """Set upside-down print mode off — ESC { 0 (UPSIDE_DOWN)."""
        return self.write(bytes([0x1B, 0x7B, 0x00]))

    def fix_font(self) -> "IBM4610":
        """Restore proportional spacing — ESC : (FIX_FONT)."""
        return self.write(bytes([0x1B, 0x3A]))

    def unidirectional(self, on: bool) -> "IBM4610":
        """Unidirectional print (improves slip quality) — ESC U n."""
        return self.write(bytes([0x1B, 0x55, 0x01 if on else 0x00]))

    def print_quality(self, high: bool, station: int = STATION_RECEIPT) -> "IBM4610":
        """Set print quality.

        Receipt station → ESC / n (PRINT_QUALITY, high-quality = 1).
        Slip station    → ESC U n (UNI_DIRECTIONAL, unidirectional = 1).
        """
        if station == STATION_RECEIPT:
            return self.write(bytes([0x1B, 0x2F, 0x01 if high else 0x00]))
        return self.write(bytes([0x1B, 0x55, 0x01 if high else 0x00]))

    def reprint_char(self) -> "IBM4610":
        """Reprint last character — ESC + (REPRINT_CHAR)."""
        return self.write(bytes([0x1B, 0x2B]))

    # ------------------------------------------------------------------
    # Character sets  (Font4610CmdFactory + Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def select_code_page(self, page: int, reset_to_resident: bool = False) -> "IBM4610":
        """Select code page — ESC t n.

        If *reset_to_resident* is True, first issues ESC % 0 (resident char set)
        before selecting the code page, as ``Font4610CmdFactory`` does.
        """
        if reset_to_resident:
            self.write(bytes([0x1B, 0x25, 0x00]))   # RESIDENT_CHAR_SET
        return self.write(bytes([0x1B, 0x74, page & 0xFF]))

    def resident_char_set(self) -> "IBM4610":
        """Switch to resident character set — ESC % 0."""
        return self.write(bytes([0x1B, 0x25, 0x00]))

    def user_char_set(self) -> "IBM4610":
        """Switch to user-defined character set — ESC % 1."""
        return self.write(bytes([0x1B, 0x25, 0x01]))

    def select_user_charset_page(self, page: int) -> "IBM4610":
        """Select user-defined character set page — ESC ! n (SELECT_USER_CHARSET)."""
        return self.write(bytes([0x1B, 0x21, page & 0xFF]))

    def dot_spacing(self, n: int, dbcs: bool = False) -> "IBM4610":
        """Set inter-character dot spacing.

        SBCS → ESC SP n   (SET_DOT_SPACING,      0–8 dots)
        DBCS → ESC R  n   (SET_DBCS_DOT_SPACING, 0–32 dots)
        """
        n = max(0, min(n, 32 if dbcs else 8))
        if dbcs:
            return self.write(bytes([0x1B, 0x52, n]))
        return self.write(bytes([0x1B, 0x20, n]))

    def download_font(
        self,
        code_page:  int,
        start_char: int,
        end_char:   int,
        char_data:  bytes,
    ) -> "IBM4610":
        """Download user-defined SBCS font to printer RAM — ESC & cp start end data."""
        return self.write(bytes([0x1B, 0x26, code_page & 0xFF,
                                 start_char & 0xFF, end_char & 0xFF]) + char_data)

    def download_dbcs_font(
        self,
        num_chars:  int,
        char_data:  bytes,
        station:    int = STATION_RECEIPT,
    ) -> "IBM4610":
        """Download user-defined DBCS font — ESC ( <st> <cnt> <data>.

        station byte: 0 = receipt, 1 = slip (as per Font4610CmdFactory).
        """
        st = 0x00 if station == STATION_RECEIPT else 0x01
        return self.write(bytes([0x1B, 0x28, st, num_chars & 0xFF]) + char_data)

    # ------------------------------------------------------------------
    # Spacing / positioning  (Font4610CmdFactory + Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def line_spacing(self, dots: int) -> "IBM4610":
        """Set line spacing in dots — ESC 3 n (LINE_SPACING)."""
        return self.write(bytes([0x1B, 0x33, dots & 0xFF]))

    def formfeed_length(self, n: int) -> "IBM4610":
        """Set form-feed / page length — ESC C n (FORMFEED_LENGTH)."""
        return self.write(bytes([0x1B, 0x43, n & 0xFF]))

    def set_tab_stops(self, stops: list) -> "IBM4610":
        """Set horizontal tab stops — ESC D <hi> <lo> … 0x00 0x00.

        Each entry in *stops* is a 16-bit column value (big-endian).
        Terminated by two zero bytes as required by Cmd4610.TAB_SET.
        """
        data = bytearray([0x1B, 0x44])
        for s in stops:
            data.append((s >> 8) & 0xFF)
            data.append(s & 0xFF)
        data += b'\x00\x00'
        return self.write(bytes(data))

    def left_margin(self, value: int) -> "IBM4610":
        """Set absolute left-margin position — ESC $ hi lo (SET_LEFT_MARGIN_POS)."""
        return self.write(bytes([0x1B, 0x24,
                                 (value >> 8) & 0xFF, value & 0xFF]))

    def relative_position(self, offset: int) -> "IBM4610":
        """Set relative horizontal position — ESC \\ hi lo (SET_RELATIVE_POS).

        *offset* is a signed 16-bit value.
        """
        v = offset & 0xFFFF
        return self.write(bytes([0x1B, 0x5C,
                                 (v >> 8) & 0xFF, v & 0xFF]))

    def horizontal_position(self, pos: int) -> "IBM4610":
        """Set absolute horizontal position (page mode) — ESC $ hi lo."""
        return self.write(bytes([0x1B, 0x24,
                                 (pos >> 8) & 0xFF, pos & 0xFF]))

    def vertical_position(self, pos: int) -> "IBM4610":
        """Set absolute vertical position (page mode) — GS $ hi lo."""
        return self.write(bytes([0x1D, 0x24,
                                 (pos >> 8) & 0xFF, pos & 0xFF]))

    # ------------------------------------------------------------------
    # Paper feed / motion  (Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def feed(self, lines: int = 1) -> "IBM4610":
        """Feed *lines* lines.

        Matches ``Gen4610CmdFactory.insertFeedLineChar()``:
        each line is a space (0x20) followed by LF (0x0A).
        """
        data = bytearray()
        for _ in range(max(1, lines)):
            data += b' \n'
        return self.write(bytes(data))

    def feed_units(self, n: int) -> "IBM4610":
        """Feed *n* motion units — ESC J n (FEED_UNITS)."""
        return self.write(bytes([0x1B, 0x4A, n & 0xFF]))

    def feed_reverse(self, lines: int) -> "IBM4610":
        """Reverse-feed *lines* lines — ESC e n (FEED_REVERSE)."""
        return self.write(bytes([0x1B, 0x65, lines & 0xFF]))

    # ------------------------------------------------------------------
    # Paper cut / eject  (Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def cut(self, feed_lines: int = 0) -> "IBM4610":
        """Partial paper cut.

        Optionally feeds *feed_lines* before cutting.
        Selects receipt station, issues ESC m, then flushes the buffer.
        Matches ``Cmd4610.CUT_PAPER = [ESC c 0 2, ESC m]``.
        """
        if feed_lines > 0:
            self.feed(feed_lines)
        self.write(bytes([
            0x1B, 0x63, 0x30, 0x02,  # CR_COMM -- select receipt
            0x1B, 0x6D,              # ESC m -- partial cut
        ]))
        self.flush()
        return self

    def eject_slip(self) -> "IBM4610":
        """Eject document / slip.

        ``DI_EJECT = [ESC c 0 4, ESC m]``
        """
        return self.write(bytes([
            0x1B, 0x63, 0x30, 0x04,  # DIP_COMM — select slip
            0x1B, 0x6D,              # ESC m — eject
        ]))

    def return_home(self, position: int = 0) -> "IBM4610":
        """Move print head to home position — ESC < [position].

        * ``position=0`` → RETURN_HOME = ESC < (no param appended by Java, but
          the method appends position=0 as the position byte per
          ``Gen4610CmdFactory.createHeadMovementCmd()``)
        * ``position=4`` → SLIP_RETURN_HOME = ESC < 4
        * ``position=1`` → OPEN_JAWS = ESC < 1
        * ``position=5`` → BEGIN_INSERTION = ESC < 5
        """
        return self.write(bytes([0x1B, 0x3C, position & 0xFF]))

    # ------------------------------------------------------------------
    # Cash drawer / beeper  (Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def pulse_drawer(
        self,
        pin:      int = 0,
        on_time:  int = 50,
        off_time: int = 50,
    ) -> "IBM4610":
        """Open cash drawer — ESC p pin t_on t_off (PULSE_DRAWER).

        *pin*: 0 = drawer 1, 1 = drawer 2.
        *on_time* / *off_time*: pulse width in 2 ms units (50 → 100 ms).
        """
        return self.write(bytes([
            0x1B, 0x70,
            pin & 0xFF,
            on_time & 0xFF,
            off_time & 0xFF,
        ]))

    def beep(
        self,
        duration_100ms: int = 1,
        frequency:      int = 523,
        volume:         int = 50,
    ) -> "IBM4610":
        """Sound the internal beeper — ESC BEL <duration> <freq_vol>.

        Mirrors ``Gen4610CmdFactory.createBeeperCmd()``:

        * duration byte 1–254 (each unit ≈ 100 ms)
        * freq_vol byte = ``volume_bit | (octave << 4) | note``
          * volume_bit: 0x80 if volume ≤ 50 (low), 0x00 if volume > 50 (high)
          * semitone offset above middle-C (261.7 Hz) split into octave/note
        """
        dur      = max(1, min(254, duration_100ms))
        vol_bit  = 0x00 if volume > 50 else 0x80
        semitones = 0
        f = float(frequency)
        while f > 261.7 and semitones < 127:
            f /= 1.0595
            semitones += 1
        octave       = (semitones // 12) << 4
        note         = semitones % 12
        freq_vol_byte = vol_bit | octave | note
        return self.write(bytes([0x1B, 0x07, dur, freq_vol_byte & 0xFF]))

    # ------------------------------------------------------------------
    # Slip / document insertion  (Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def open_jaws(self) -> "IBM4610":
        """Open document insertion jaws — ESC < 1 (OPEN_JAWS)."""
        return self.write(bytes([0x1B, 0x3C, 0x01]))

    def begin_insertion(self) -> "IBM4610":
        """Begin document insertion (grab slip) — ESC < 5 (BEGIN_INSERTION / GRAB_SLIP)."""
        return self.write(bytes([0x1B, 0x3C, 0x05]))

    def end_insertion(self) -> "IBM4610":
        """End document insertion — SOH DC3 (END_INSERTION)."""
        return self.write(bytes([0x01, 0x13]))

    def register_document(self) -> "IBM4610":
        """Register document in jaws for MICR.

        Matches ``Cmd4610.REGISTER_DOC = [ESC c 0 4, ESC f 2 1, ESC c 0 4]``.
        """
        return self.write(bytes([
            0x1B, 0x63, 0x30, 0x04,   # select slip
            0x1B, 0x66, 0x02, 0x01,   # ESC f 2 1 — register
            0x1B, 0x63, 0x30, 0x04,   # select slip again
        ]))

    def end_register_document(self) -> "IBM4610":
        """End document registration.

        Matches ``Cmd4610.END_REGISTER_DOC = [ESC c 0 4, ESC f 2 0]``.
        """
        return self.write(bytes([
            0x1B, 0x63, 0x30, 0x04,   # select slip
            0x1B, 0x66, 0x02, 0x00,   # ESC f 2 0 — end register
        ]))

    def flip_check(self) -> "IBM4610":
        """Flip document for back-side printing — ESC 5 (FLIP_CHECK)."""
        return self.write(bytes([0x1B, 0x35]))

    def change_print_side(self) -> "IBM4610":
        """Alias for :meth:`flip_check` (ChangePrintSideCmd)."""
        return self.flip_check()

    def set_chase_mode(self) -> "IBM4610":
        """Set chase mode for slip station — ESC S 0x0A (SET_CHASE_MODE)."""
        return self.write(bytes([0x1B, 0x53, 0x0A]))

    # ------------------------------------------------------------------
    # MICR / check scanner  (Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def micr_read(self) -> "IBM4610":
        """Initiate MICR read — ESC I (MICR_READ)."""
        return self.write(bytes([0x1B, 0x49]))

    def start_scan(self) -> "IBM4610":
        """Start image scan — ESC > (START_SCAN)."""
        return self.write(bytes([0x1B, 0x3E]))

    def print_scanned_image(self) -> "IBM4610":
        """Print the last scanned check image — ESC 0 (PRINT_SCANNED_IMG)."""
        return self.write(bytes([0x1B, 0x30]))

    def retrieve_image(self) -> "IBM4610":
        """Retrieve scan-image data from printer — ESC 9 (RETRIEVE_IMAGE)."""
        return self.write(bytes([0x1B, 0x39]))

    def store_scanned_image(self) -> "IBM4610":
        """Store scanned image to flash — ESC A (STORE_SCANNED_IMG)."""
        return self.write(bytes([0x1B, 0x41]))

    def get_next_image_location(self) -> "IBM4610":
        """Get next stored image location — GS N 1 0 (GET_NEXT_IMG_LOC)."""
        return self.write(bytes([0x1D, 0x4E, 0x01, 0x00]))

    def get_first_unread_image_location(self) -> "IBM4610":
        """Get first unread image location — GS N 1 1 (GET_FIRST_UNREAD_IMG_LOC)."""
        return self.write(bytes([0x1D, 0x4E, 0x01, 0x01]))

    def scanner_calibrate(self) -> "IBM4610":
        """Calibrate the check scanner — GS c 0 (SCANNER_CALIB)."""
        return self.write(bytes([0x1D, 0x63, 0x30]))

    # ------------------------------------------------------------------
    # Barcode  (Grap4610CmdFactory)
    # ------------------------------------------------------------------

    def barcode(
        self,
        symbol:   int,
        data:     str,
        height:   int = 50,
        width:    int = 2,
        align:    int = ALIGN_LEFT,
        hri:      int = HRI_BELOW,
        encoding: str = "ascii",
    ) -> "IBM4610":
        """Print a barcode.

        Mirrors ``Grap4610CmdFactory.createPrintBarCodeCmd()``.

        Args:
            symbol:   ``BC_*`` constant (BC_CODE39, BC_EAN13, BC_CODE128, …)
            data:     barcode payload string
            height:   bar height in dots (GS h)
            width:    bar module width   (GS w, 2–6)
            align:    ALIGN_LEFT / ALIGN_CENTER / ALIGN_RIGHT
            hri:      HRI_NONE / HRI_ABOVE / HRI_BELOW / HRI_BOTH
            encoding: text encoding for *data* (default ``"ascii"``)

        PDF417 (``BC_PDF417 = 201``) is handled specially via GS P.
        """
        self.alignment(align)
        data_bytes = data.encode(encoding)

        if symbol == BC_PDF417:
            # Grap4610CmdFactory.handlePDF417 just appends GS P
            self.write(bytes([0x1D, 0x50]))   # PRINT_PDF417
            self.write(data_bytes + b'\x00')
            return self

        # Standard GS k barcode
        self.write(bytes([0x1D, 0x48, hri & 0xFF]))        # GS H — HRI position
        self.write(bytes([0x1D, 0x68, height & 0xFF]))     # GS h — bar height
        self.write(bytes([0x1D, 0x77, width & 0xFF]))      # GS w — bar width
        self.write(bytes([0x1D, 0x6B, symbol & 0xFF]))     # GS k — barcode type

        if symbol == BC_CODE128A:
            # Length-prefixed variant used for Code128abc
            self.write(bytes([len(data_bytes)]))
        self.write(data_bytes)
        if symbol != BC_CODE128A:
            self.write(b'\x00')   # NUL terminator for most types

        self.alignment(ALIGN_LEFT)                          # restore default
        return self

    # ------------------------------------------------------------------
    # Graphics / bitmaps  (Grap4610CmdFactory)
    # ------------------------------------------------------------------

    def print_bitmap(
        self,
        density:  int,
        columns:  int,
        data:     bytes,
        align:    int = ALIGN_LEFT,
    ) -> "IBM4610":
        """Print an inline raster bitmap — ESC * mode nL nH d1...dk.

        Mirrors ``Grap4610CmdFactory.createPrintBitmapCmd()``.

        *density* / *mode*:
          - ``DENSITY_NORMAL`` (0)  — 8-dot single-density.  1 byte per column,
            8 dots tall.  Data length must equal *columns*.
          - ``DENSITY_DOUBLE`` (1)  — 8-dot double-density.  1 byte per column,
            8 dots tall.  Data length must equal *columns*.
          - 32 / 33               — 24-dot single / double density.  3 bytes per
            column, 24 dots tall.  Data length must equal 3 * *columns*.

        *columns*:  number of dot-columns to print (the 16-bit nL+nH value).
        *data*:     raw bitmap bytes — see *density* above for expected length.

        ESC/POS command layout::

            ESC * mode nL nH d1 ... dk
            nL = columns & 0xFF
            nH = (columns >> 8) & 0xFF
        """
        nL = columns & 0xFF
        nH = (columns >> 8) & 0xFF
        self.alignment(align)
        self.write(bytes([0x1B, 0x2A, density & 0xFF, nL, nH]))
        return self.write(data)

    def set_bitmap(
        self,
        bitmap_no: int,
        width:     int,
        height:    int,
        data:      bytes,
    ) -> "IBM4610":
        """Define a stored (RAM) bitmap — GS * no w h data (SET_BITMAP).

        Use :meth:`print_set_bitmap` to print it.
        """
        return self.write(bytes([0x1D, 0x2A, bitmap_no & 0xFF,
                                 width & 0xFF, height & 0xFF]) + data)

    def print_set_bitmap(
        self,
        density:   int,
        bitmap_no: int,
        align:     int = ALIGN_LEFT,
        margin:    int = 0,
    ) -> "IBM4610":
        """Print a stored bitmap — GS / density bitmap_no (PRINT_SET_BITMAP).

        Mirrors ``Grap4610CmdFactory.createPrintSetBitmapCmd()``.
        If *margin* > 0 it takes precedence over *align*.
        """
        if margin > 0:
            self.left_margin(margin)
        elif align:
            self.alignment(align)
        self.write(bytes([0x1D, 0x2F, density & 0xFF, bitmap_no & 0xFF]))
        if margin > 0:
            self.left_margin(0)
        elif align:
            self.alignment(ALIGN_LEFT)
        return self

    def set_logo(self, location: int) -> "IBM4610":
        """Define stored-logo position — GS : location (SET_LOGO)."""
        return self.write(bytes([0x1D, 0x3A, location & 0xFF]))

    def print_set_logo(self, location: int) -> "IBM4610":
        """Print stored logo — GS ^ location (PRINT_SET_LOGO), then request EC."""
        self.write(bytes([0x1D, 0x5E, location & 0xFF]))
        return self.ec_level_request(buffered=True)

    def download_logo(
        self,
        density:      int,
        width_bytes:  int,
        height_bytes: int,
        data:         bytes,
    ) -> "IBM4610":
        """Download a logo to flash — ESC * 0 w h data (DOWNLOAD_LOGOS)."""
        return self.write(bytes([0x1B, 0x2A, 0x00,
                                 width_bytes & 0xFF, height_bytes & 0xFF]) + data)

    def erase_logos(self) -> "IBM4610":
        """Erase all downloaded logos from flash — ESC # 1 (ERASE_LOGOS)."""
        return self.write(bytes([0x1B, 0x23, 0x01]))

    # ------------------------------------------------------------------
    # Page mode  (Grap4610CmdFactory)
    # ------------------------------------------------------------------

    def page_mode_enable(self) -> "IBM4610":
        """Enter page mode — ESC L (ENABLE_PAGEMODE)."""
        return self.write(bytes([0x1B, 0x4C]))

    def page_mode_disable(self) -> "IBM4610":
        """Exit page mode back to standard (line) mode — ESC O (DISABLE_PAGEMODE)."""
        return self.write(bytes([0x1B, 0x4F]))

    def page_mode_define(
        self,
        x:  int,
        y:  int,
        dx: int,
        dy: int,
    ) -> "IBM4610":
        """Define page-mode print area — ESC X x_hi x_lo y_hi y_lo dx_hi dx_lo dy_hi dy_lo.

        Mirrors ``Grap4610CmdFactory.createPMPageDefineCmd()``.
        All coordinates are in dots.
        """
        def split(v: int):
            return (v >> 8) & 0xFF, v & 0xFF

        xh,  xl  = split(x)
        yh,  yl  = split(y)
        dxh, dxl = split(dx)
        dyh, dyl = split(dy)
        return self.write(bytes([0x1B, 0x58,
                                 xh, xl, yh, yl, dxh, dxl, dyh, dyl]))

    def page_mode_position(self, position: int) -> "IBM4610":
        """Set page-mode print direction — ESC T n (PAGEMODE_POSITION).

        * 0 = top→bottom, left→right
        * 1 = bottom→top, left→right
        * 2 = bottom→top, right→left
        * 3 = top→bottom, right→left
        """
        return self.write(bytes([0x1B, 0x54, position & 0xFF]))

    def page_mode_print(
        self,
        clear_after: bool = False,
        re_enable:   bool = False,
    ) -> "IBM4610":
        """Print the page-mode buffer — ESC FF (PAGEMODE_PRINT).

        Mirrors ``Grap4610CmdFactory.createPrintPageModePageCmd()``.

        *clear_after*: also send CAN (0x18) to clear the buffer.
        *re_enable*:   wrap with ESC L … ESC O so page mode stays active.
        """
        if re_enable:
            self.page_mode_enable()
        self.write(bytes([0x1B, 0x0C]))     # ESC FF — print page buffer
        if clear_after:
            self.write(bytes([0x18]))       # CAN — clear page buffer
        if re_enable:
            self.page_mode_disable()
        return self

    def page_mode_clear(self) -> "IBM4610":
        """Clear page-mode buffer — CAN 0x18 (PAGEMODE_CLEAR)."""
        return self.write(bytes([0x18]))

    def page_mode_normal(self) -> "IBM4610":
        """Return to line mode from page mode — FF 0x0C (PAGEMODE_NORMODE)."""
        return self.write(bytes([0x0C]))

    # ------------------------------------------------------------------
    # Flash management  (Gen4610CmdFactory.createEraseFlashSectorCmd)
    # ------------------------------------------------------------------

    def erase_flash(self, sector: int) -> "IBM4610":
        """Erase a flash sector — ESC # n.

        *sector*: ``FLASH_*`` constant (1–6, 8).
        """
        _sector_cmds: dict[int, bytes] = {
            FLASH_DL_GRAPHICS:              bytes([0x1B, 0x23, 0x01]),
            FLASH_PRE_MESSAGES:             bytes([0x1B, 0x23, 0x02]),
            FLASH_USR_DEF_IMPACT_CHARSETS:  bytes([0x1B, 0x23, 0x03]),
            FLASH_USR_DEF_THERMAL_CHARSETS: bytes([0x1B, 0x23, 0x04]),
            FLASH_USR_FLA_STORAGE:          bytes([0x1B, 0x23, 0x05]),
            FLASH_ALL_DBCS:                 bytes([0x1B, 0x23, 0x06]),
            FLASH_CHECK_IMAGES:             bytes([0x1B, 0x23, 0x08]),
        }
        cmd = _sector_cmds.get(sector)
        if cmd is None:
            raise ValueError(f"Unknown flash sector: {sector}")
        return self.write(cmd)

    def erase_messages(self) -> "IBM4610":
        """Erase pre-stored messages from flash — ESC # 2 (ERASE_MESSAGES)."""
        return self.write(bytes([0x1B, 0x23, 0x02]))

    # ------------------------------------------------------------------
    # Error recovery / line count  (Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def error_recovery(self, flags: int) -> "IBM4610":
        """Configure error-recovery behaviour — ESC c 4 flags.

        Mirrors ``Gen4610CmdFactory.createErrorRecoveryCmd()``.
        Build *flags* from ``ER_*`` constants::

            p.error_recovery(ER_RELEASE_AFTER_CORRECTION | ER_WAIT_FOR_DOCUMENT)

        The Java code maps the booleans to the following bits:
          bit 2 (0x04) — NOT print-buffer-release on correction
          bit 3 (0x08) — NOT auto-home error retry
          bit 4 (0x10) — hold for document present
          bit 5 (0x20) — hold for flip error

        Pass the pre-assembled byte directly.
        """
        return self.write(bytes([0x1B, 0x63, 0x34, flags & 0xFF]))

    def enable_line_count(self, enable: bool) -> "IBM4610":
        """Enable/disable the line counter — ESC 8 n.

        Java note: enabled → 0x00, disabled → 0x01  (inverted sense).
        """
        return self.write(bytes([0x1B, 0x38, 0x00 if enable else 0x01]))

    def reset_line_count(self) -> "IBM4610":
        """Reset the line counter — ESC 6 (RESET_LINECNT)."""
        return self.write(bytes([0x1B, 0x36]))

    def enable_feed_button(self, on: bool, button: int = 0) -> "IBM4610":
        """Enable/disable the feed button — ESC c 5 n (BUTTON_ENABLE).

        *button*: button identifier (0 = primary feed button).
        """
        return self.write(bytes([0x1B, 0x63, 0x35,
                                 0x01 if on else 0x00]))

    # ------------------------------------------------------------------
    # MCT (Magnetic Card Transport)  (Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def mct_read(self, mct_value: int) -> "IBM4610":
        """Read MCT (magnetic card track) — ESC S value (MCT_READ)."""
        return self.write(bytes([0x1B, 0x53, mct_value & 0xFF]))

    def mct_write(self, matrix: int, high: int, low: int) -> "IBM4610":
        """Write MCT value — ESC M matrix hi lo (MCT_WRITE)."""
        return self.write(bytes([0x1B, 0x4D,
                                 matrix & 0xFF, high & 0xFF, low & 0xFF]))

    # ------------------------------------------------------------------
    # Statistics  (Gen4610CmdFactory.createStatisticCmd)
    # ------------------------------------------------------------------

    def statistic(self, stat_type: str) -> "IBM4610":
        """Request a printer statistic — ESC Q <sub-cmd>.

        *stat_type* must be one of the keys in ``STATISTIC_SUBCMDS``, e.g.::

            p.statistic("ManufactureDate")
            p.statistic("PaperCutCount")
            p.statistic("ReceiptLineFeedCount")

        See the ``STATISTIC_SUBCMDS`` dict at module level for all keys.
        """
        sub = STATISTIC_SUBCMDS.get(stat_type)
        if sub is None:
            raise ValueError(
                f"Unknown statistic type {stat_type!r}. "
                f"Valid keys: {list(STATISTIC_SUBCMDS)}"
            )
        return self.write(bytes([0x1B, 0x51]) + sub)

    # ------------------------------------------------------------------
    # High-level convenience helpers
    # ------------------------------------------------------------------

    def print_line(self, text: str, encoding: str = "cp437") -> int:
        """Write *text* + CRLF and flush immediately."""
        self.write(text.encode(encoding) + b'\r\n')
        return self.flush()

    def print_receipt(
        self,
        lines:   list,
        feed:    int  = 5,
        cut:     bool = True,
        station: int  = STATION_RECEIPT,
    ) -> int:
        """High-level receipt helper.

        Args:
            lines:   list of str or bytes — each is printed as one line.
            feed:    number of blank lines to feed before cutting.
            cut:     whether to issue a partial paper cut at the end.
            station: station to select (default STATION_RECEIPT).

        Returns the number of bytes transferred.
        """
        self.select_station(station)
        for line in lines:
            if isinstance(line, bytes):
                self.write(line)
            else:
                self.write(line.encode("cp437"))
            self.write(b'\r\n')
        if feed > 0:
            self.feed(feed)
        if cut:
            self.cut()
        return self.flush()
