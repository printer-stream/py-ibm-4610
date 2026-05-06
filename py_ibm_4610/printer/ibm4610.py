"""printer/ibm4610.py — IBM 4610 SureMark printer driver (USB HID transport).

Implements the full command set reverse-engineered from the official
JavaPOS driver (posj.jar):

  - Cmd4610.java              — raw ESC/POS byte sequences
  - Gen4610CmdFactory.java    — general command construction
  - Font4610CmdFactory.java   — font / text attribute commands
  - Grap4610CmdFactory.java   — graphics / barcode commands
  - Print4610CmdFactory.java  — station routing

Transport: USB HID (VID=0x04b3, PID=0x4535), Interface 1.
Each HID output report is exactly 1022 bytes (7-byte header + payload).

Quick start::

    from py_ibm_4610 import IBM4610, STATION_RECEIPT

    with IBM4610() as p:
        p.select_station(STATION_RECEIPT)
        p.bold(True).text("Hello, World!\\n").bold(False)
        p.feed(4).cut()
"""

from __future__ import annotations

import logging

import usb.core
import usb.util

from .base import BasePrinter
from .._constants import (
    VENDOR, PRODUCT, IFACE, REPORT_SIZE,
    STATION_RECEIPT, STATION_SLIP, STATION_LABEL,
    ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT,
    FONT_A, FONT_B, FONT_C,
    BC_PDF417, BC_CODE128A,
    FLASH_DL_GRAPHICS, FLASH_PRE_MESSAGES,
    FLASH_USR_DEF_IMPACT_CHARSETS, FLASH_USR_DEF_THERMAL_CHARSETS,
    FLASH_USR_FLA_STORAGE, FLASH_ALL_DBCS, FLASH_CHECK_IMAGES,
    STATISTIC_SUBCMDS,
)
from .._transport import make_packet, MAX_PAYLOAD

_log = logging.getLogger(__name__)


class IBM4610(BasePrinter):
    """Full-capability driver for the IBM / Toshiba 4610 SureMark printer.

    Commands are accumulated in an internal buffer via :meth:`write` and
    dispatched to the printer via :meth:`flush`.  All chainable command
    methods return *self* so calls may be chained::

        p.bold(True).text("Sale!").bold(False).feed(2)

    Use :meth:`send_raw` to bypass buffering and transmit immediately.

    Example::

        with IBM4610() as p:
            p.select_station(STATION_RECEIPT)
            p.text("Hello, World!\\n")
            p.feed(4)
            p.cut()
    """

    #: USB vendor ID.
    VENDOR_ID: int = VENDOR
    #: USB product ID.
    PRODUCT_ID: int = PRODUCT
    #: HID interface number.
    IFACE: int = IFACE
    #: Bytes per HID SET_REPORT transfer.
    REPORT_SIZE: int = REPORT_SIZE

    _MAX_PAYLOAD: int = MAX_PAYLOAD  # 1015 bytes per packet

    def __init__(
        self,
        vendor:      int = VENDOR,
        product:     int = PRODUCT,
        iface:       int = IFACE,
        report_size: int = REPORT_SIZE,
    ) -> None:
        super().__init__()
        self._vendor      = vendor
        self._product     = product
        self._iface       = iface
        self._report_size = report_size
        self._dev         = None
        self._ep_in       = None  # interrupt IN endpoint (populated in open())

    # ------------------------------------------------------------------
    # Transport — open / close
    # ------------------------------------------------------------------

    def open(self) -> "IBM4610":
        """Open the USB connection to the printer.

        Detaches the kernel HID driver if active, claims the interface,
        and discovers the interrupt IN endpoint needed for status reads.

        Raises:
            RuntimeError: if the printer cannot be found on the USB bus.
        """
        _log.info(
            "Opening USB connection (VID=%s, PID=%s, iface=%d)",
            hex(self._vendor), hex(self._product), self._iface,
        )
        self._dev = usb.core.find(idVendor=self._vendor, idProduct=self._product)
        if self._dev is None:
            raise RuntimeError(
                f"IBM 4610 printer not found "
                f"(VID={self._vendor:#06x}, PID={self._product:#06x})."
            )
        _log.debug("USB device found: %s", self._dev)
        if self._dev.is_kernel_driver_active(self._iface):
            _log.debug("Detaching kernel driver from interface %d", self._iface)
            self._dev.detach_kernel_driver(self._iface)
        usb.util.claim_interface(self._dev, self._iface)
        _log.debug("Interface %d claimed", self._iface)

        self._ep_in = None
        for cfg in self._dev:
            for intf in cfg:
                if intf.bInterfaceNumber == self._iface:
                    for ep in intf:
                        if (usb.util.endpoint_direction(ep.bEndpointAddress)
                                == usb.util.ENDPOINT_IN):
                            self._ep_in = ep
                            _log.debug(
                                "Interrupt IN endpoint: addr=0x%02x, maxPacketSize=%d",
                                ep.bEndpointAddress, ep.wMaxPacketSize,
                            )
                            break
        if self._ep_in is None:
            _log.warning("No interrupt IN endpoint found on interface %d", self._iface)
        return self

    def close(self) -> None:
        """Release the USB interface and re-attach the kernel driver."""
        if self._dev is not None:
            _log.info("Closing USB connection (iface=%d)", self._iface)
            try:
                usb.util.release_interface(self._dev, self._iface)
                _log.debug("Interface %d released", self._iface)
                self._dev.attach_kernel_driver(self._iface)
                _log.debug("Kernel driver re-attached to interface %d", self._iface)
            except Exception as exc:
                _log.debug("close: cleanup error (ignored): %s", exc)
            self._dev = None
            self._ep_in = None

    # ------------------------------------------------------------------
    # Transport — low-level send / receive
    # ------------------------------------------------------------------

    def send_raw(self, data: bytes) -> int:
        """Frame *data* as a HID output report and transmit it immediately.

        Returns the number of bytes transferred (always *report_size*).

        Raises:
            RuntimeError: if the printer has not been opened.
        """
        if self._dev is None:
            raise RuntimeError("Printer not open — call open() or use a 'with' block.")
        _log.debug("send_raw: %d payload bytes", len(data))
        pkt = make_packet(data, self._report_size)
        transferred = self._dev.ctrl_transfer(
            bmRequestType=0x21,           # HID, host→device, interface
            bRequest=0x09,                # SET_REPORT
            wValue=0x0201,                # Output report, Report ID 1
            wIndex=self._iface,
            data_or_wLength=pkt,
            timeout=5000,
        )
        _log.debug("send_raw: %d wire bytes transferred", transferred)
        return transferred

    def drain_in(self, timeout: int = 100) -> int:
        """Discard all pending interrupt IN packets; return the count discarded.

        The printer sends unsolicited status frames on the interrupt IN
        endpoint continuously (mirroring the Java ``StatusDaemon`` thread).
        Call this before issuing a query command so that
        :meth:`read_response` receives the actual reply rather than a
        stale status packet.

        Args:
            timeout: per-read USB timeout in milliseconds (default 100).
        """
        if self._dev is None or self._ep_in is None:
            _log.debug("drain_in: skipped (device not open)")
            return 0
        count = 0
        size = self._ep_in.wMaxPacketSize
        while True:
            try:
                self._dev.read(self._ep_in.bEndpointAddress, size, timeout=timeout)
                count += 1
            except Exception:
                break
        _log.debug("drain_in: discarded %d stale IN packet(s)", count)
        return count

    def read_response(self, size: int = 0, timeout: int = 2000) -> bytes:
        """Read one interrupt IN report from the printer.

        Used to collect the printer's reply to query commands such as
        :meth:`statistic`.  Returns the raw report bytes, or an empty
        ``bytes`` object on timeout.

        Args:
            size:    bytes to read.  Defaults to ``ep.wMaxPacketSize``.
                     **Must** equal the endpoint's packet size to avoid
                     ``EOVERFLOW`` from libusb.
            timeout: USB read timeout in milliseconds (default 2000).
        """
        if self._dev is None:
            raise RuntimeError("Printer not open.")
        if self._ep_in is None:
            raise RuntimeError("No interrupt IN endpoint found on interface.")
        if size <= 0:
            size = self._ep_in.wMaxPacketSize
        _log.debug("read_response: reading up to %d bytes (timeout=%dms)", size, timeout)
        try:
            data = bytes(self._dev.read(
                self._ep_in.bEndpointAddress, size, timeout=timeout,
            ))
            _log.debug("read_response: received %d bytes", len(data))
            return data
        except Exception as exc:
            _log.debug("read_response: timeout or error (%s)", exc)
            return b""

    def read_stat(self, stat_type: str, timeout: int = 2000) -> bytes:
        """Send a statistics query, flush immediately, and read the response.

        Drains all pending unsolicited IN packets first, sends the query,
        then returns the raw report bytes (empty on timeout).

        Args:
            stat_type: key from :data:`~py_ibm_4610.STATISTIC_SUBCMDS`.
            timeout:   USB read timeout in milliseconds (default 2000).
        """
        _log.debug("read_stat: querying %r", stat_type)
        self.drain_in()
        self.statistic(stat_type)
        self.flush()
        resp = self.read_response(timeout=timeout)
        _log.debug("read_stat: %r → %d bytes", stat_type, len(resp))
        return resp

    # ------------------------------------------------------------------
    # Station / printer control  (Gen4610CmdFactory + Print4610CmdFactory)
    # ------------------------------------------------------------------

    def reset(self) -> "IBM4610":
        """Full hardware reset — ``RESET = [0x00, 0x40, 0x00]``."""
        _log.debug("reset: issuing full hardware reset")
        return self.write(bytes([0x00, 0x40, 0x00]))

    def reinit(self) -> "IBM4610":
        """Software re-initialise — ``ESC @`` (0x1B 0x40)."""
        _log.debug("reinit: software re-initialise")
        return self.write(bytes([0x1B, 0x40]))

    def select_station(self, station: int) -> "IBM4610":
        """Select the active print station.

        The 4610 has three physically separate print mechanisms, each
        requiring its own station-select sequence before sending data:

        * ``STATION_RECEIPT`` (0x02) — Thermal roll receipt.
          Sends ``CR_COMM + CR_SETTINGS`` (``ESC c 0 2``, ``ESC c 1 2``).

        * ``STATION_SLIP`` (0x04) — Impact document / cheque / slip (DIP).
          Sends ``DIP_COMM + DIP_SETTINGS`` (``ESC c 0 4``, ``ESC c 1 4``).

        * ``STATION_LABEL`` (0x14) — Landscape slip / label (DIL).
          Sends ``DIL_COMM`` (``ESC c 0 8``).

        Mirrors ``Print4610CmdFactory.setCmdsStation()``.

        Raises:
            ValueError: for an unrecognised station code.
        """
        if station == STATION_RECEIPT:
            _log.debug("select_station: RECEIPT")
            return self.write(bytes([
                0x1B, 0x63, 0x30, 0x02,  # CR_COMM
                0x1B, 0x63, 0x31, 0x02,  # CR_SETTINGS
            ]))
        if station == STATION_SLIP:
            _log.debug("select_station: SLIP")
            return self.write(bytes([
                0x1B, 0x63, 0x30, 0x04,  # DIP_COMM
                0x1B, 0x63, 0x31, 0x04,  # DIP_SETTINGS
            ]))
        if station == STATION_LABEL:
            _log.debug("select_station: LABEL")
            return self.write(bytes([0x1B, 0x63, 0x30, 0x08]))  # DIL_COMM
        raise ValueError(f"Unknown station: {station:#x}")

    def status_request(self) -> "IBM4610":
        """Request printer status — ``STATUS_REQUEST = ESC NUL SP NUL``."""
        return self.write(bytes([0x1B, 0x00, 0x20, 0x00]))

    def device_info(self, buffered: bool = False) -> "IBM4610":
        """Request device / firmware info.

        Args:
            buffered: ``False`` → ``DEVICE_INFO [0x00, 0x00, 0x01]``.
                      ``True``  → ``BUFFERED_DEV_INFO [0x1B, 0x00, 0x00, 0x01]``.
        """
        if buffered:
            return self.write(bytes([0x1B, 0x00, 0x00, 0x01]))
        return self.write(bytes([0x00, 0x00, 0x01]))

    def ec_level_request(self, buffered: bool = True) -> "IBM4610":
        """Request EC (error-correction) level.

        Args:
            buffered: ``True``  → ``BUFFERED_EC  [0x1B, 0x00, 0x80, 0x00]``.
                      ``False`` → ``IMMEDIATE_EC [0x00, 0x80, 0x00]``.
        """
        if buffered:
            return self.write(bytes([0x1B, 0x00, 0x80, 0x00]))
        return self.write(bytes([0x00, 0x80, 0x00]))

    def hold_buffer(self) -> "IBM4610":
        """Hold the print buffer — ``ESC 7``."""
        return self.write(bytes([0x1B, 0x37]))

    def release_buffer(self) -> "IBM4610":
        """Release the print buffer — ``DLE ENQ 1``."""
        return self.write(bytes([0x10, 0x05, 0x31]))

    def cancel_buffer(self) -> "IBM4610":
        """Cancel (clear) the print buffer — ``DLE ENQ 2``."""
        return self.write(bytes([0x10, 0x05, 0x32]))

    def test_request(self) -> "IBM4610":
        """Send the internal self-test request packet."""
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

        Each flag, when ``True``, enables the corresponding notification.
        Flags map to bits in the parameter byte (inverted: 0 = enabled).
        ``STATUS_SENT = ESC ) <flags>``

        Mirrors ``Gen4610CmdFactory.createStatusSentCmd()``.
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

    def text(self, s, encoding: str = "cp437") -> "IBM4610":
        """Encode *s* and append to the send buffer.

        Args:
            s:        text to print (``str`` or ``bytes``).  ``bytes`` are
                      written as-is; ``str`` is encoded with *encoding*.
            encoding: codec for encoding (default ``cp437`` — IBM PC Latin-1).
        """
        if isinstance(s, (bytes, bytearray)):
            return self.write(s)
        return self.write(s.encode(encoding))

    def raw_bytes(self, data: bytes) -> "IBM4610":
        """Append *data* directly to the send buffer (no encoding)."""
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
        """Enable/disable bold (emphasize) — ``ESC G n``."""
        return self.write(bytes([0x1B, 0x47, 0x01 if on else 0x00]))

    def underline(self, on: bool) -> "IBM4610":
        """Enable/disable underline — ``ESC - n`` (0 = off, 1 = single dot)."""
        return self.write(bytes([0x1B, 0x2D, 0x01 if on else 0x00]))

    def double_wide(self, on: bool) -> "IBM4610":
        """Enable/disable double-wide mode — ``ESC W n``."""
        return self.write(bytes([0x1B, 0x57, 0x01 if on else 0x00]))

    def double_high(self, on: bool) -> "IBM4610":
        """Enable/disable double-high mode — ``ESC h n``."""
        return self.write(bytes([0x1B, 0x68, 0x01 if on else 0x00]))

    def double_wide_high(self, on: bool) -> "IBM4610":
        """Enable/disable both double-wide and double-high simultaneously."""
        self.double_wide(on)
        return self.double_high(on)

    def reverse_video(self, on: bool) -> "IBM4610":
        """Enable/disable reverse video (inverted characters) — ``ESC H n``."""
        return self.write(bytes([0x1B, 0x48, 0x01 if on else 0x00]))

    def normal_mode(self) -> "IBM4610":
        """Reset all text attributes to normal.

        Resets superscript, bold, double-wide, double-high, reverse video,
        and underline in a single sequence (``Cmd4610.NORMAL_MODE``).
        """
        return self.write(bytes([
            0x1B, 0x5F, 0x00,  # superscript off
            0x1B, 0x47, 0x00,  # bold off
            0x1B, 0x57, 0x00,  # double-wide off
            0x1B, 0x68, 0x00,  # double-high off
            0x1B, 0x48, 0x00,  # reverse-video off
            0x1B, 0x2D, 0x00,  # underline off
        ]))

    def scale_font(self, width: int = 0, height: int = 0) -> "IBM4610":
        """Scale the font — ``GS ! n``.

        Args:
            width:  horizontal scale 0–7 (0 = normal, 1 = 2×, … 7 = 8×).
            height: vertical scale 0–7.

        Packed as ``(width << 4) | height`` in the parameter byte.
        Mirrors ``Font4610CmdFactory.createScaleFontCmd()``.
        """
        n = ((width & 0x0F) << 4) | (height & 0x0F)
        return self.write(bytes([0x1D, 0x21, n]))

    def select_font(self, font: int) -> "IBM4610":
        """Select font face — ``ESC ! n``.

        Args:
            font: ``FONT_A`` (0), ``FONT_B`` (1), or ``FONT_C`` (2).
        """
        return self.write(bytes([0x1B, 0x21, font & 0xFF]))

    def alignment(self, align: int) -> "IBM4610":
        """Set text alignment — ``ESC a n``.

        Args:
            align: ``ALIGN_LEFT`` (0), ``ALIGN_CENTER`` (1), ``ALIGN_RIGHT`` (2).
        """
        return self.write(bytes([0x1B, 0x61, align & 0xFF]))

    def font_color(self, color: int) -> "IBM4610":
        """Select font color — ``ESC r n``.

        Args:
            color: 0 = black, 1 = red (dual-color thermal ribbon only).
        """
        return self.write(bytes([0x1B, 0x72, color & 0xFF]))

    def color_select_mode(self, mode: int) -> "IBM4610":
        """Color select mode — ``GS ; n``."""
        return self.write(bytes([0x1D, 0x3B, mode & 0xFF]))

    def rotate_90(self, on: bool) -> "IBM4610":
        """90-degree character rotation — ``ESC V n``."""
        return self.write(bytes([0x1B, 0x56, 0x01 if on else 0x00]))

    def rotate_180(self, on: bool) -> "IBM4610":
        """180-degree character rotation — ``ESC { n``."""
        return self.write(bytes([0x1B, 0x7B, 0x01 if on else 0x00]))

    def upside_down(self) -> "IBM4610":
        """Set upside-down print mode off — ``ESC { 0``."""
        return self.write(bytes([0x1B, 0x7B, 0x00]))

    def fix_font(self) -> "IBM4610":
        """Restore proportional spacing — ``ESC :``."""
        return self.write(bytes([0x1B, 0x3A]))

    def unidirectional(self, on: bool) -> "IBM4610":
        """Unidirectional print (improves slip quality) — ``ESC U n``."""
        return self.write(bytes([0x1B, 0x55, 0x01 if on else 0x00]))

    def print_quality(
        self,
        high:    bool,
        station: int = STATION_RECEIPT,
    ) -> "IBM4610":
        """Set print quality.

        * Receipt: ``ESC / n`` (1 = high quality).
        * Slip: ``ESC U n`` (1 = unidirectional, improves quality).
        """
        if station == STATION_RECEIPT:
            return self.write(bytes([0x1B, 0x2F, 0x01 if high else 0x00]))
        return self.write(bytes([0x1B, 0x55, 0x01 if high else 0x00]))

    def reprint_char(self) -> "IBM4610":
        """Reprint last character — ``ESC +``."""
        return self.write(bytes([0x1B, 0x2B]))

    # ------------------------------------------------------------------
    # Character sets  (Font4610CmdFactory + Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def select_code_page(
        self,
        page:               int,
        reset_to_resident:  bool = False,
    ) -> "IBM4610":
        """Select code page — ``ESC t n``.

        Args:
            page:               code page number.
            reset_to_resident:  if ``True``, first issues ``ESC % 0``
                                (resident character set) before selecting
                                the code page, as ``Font4610CmdFactory`` does.
        """
        if reset_to_resident:
            self.write(bytes([0x1B, 0x25, 0x00]))
        return self.write(bytes([0x1B, 0x74, page & 0xFF]))

    def resident_char_set(self) -> "IBM4610":
        """Switch to resident character set — ``ESC % 0``."""
        return self.write(bytes([0x1B, 0x25, 0x00]))

    def user_char_set(self) -> "IBM4610":
        """Switch to user-defined character set — ``ESC % 1``."""
        return self.write(bytes([0x1B, 0x25, 0x01]))

    def select_user_charset_page(self, page: int) -> "IBM4610":
        """Select user-defined character set page — ``ESC ! n``."""
        return self.write(bytes([0x1B, 0x21, page & 0xFF]))

    def dot_spacing(self, n: int, dbcs: bool = False) -> "IBM4610":
        """Set inter-character dot spacing.

        * SBCS → ``ESC SP n`` (0–8 dots).
        * DBCS → ``ESC R  n`` (0–32 dots).
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
        """Download a user-defined SBCS font to printer RAM — ``ESC & cp start end data``."""
        return self.write(bytes([0x1B, 0x26, code_page & 0xFF,
                                 start_char & 0xFF, end_char & 0xFF]) + char_data)

    def download_dbcs_font(
        self,
        num_chars:  int,
        char_data:  bytes,
        station:    int = STATION_RECEIPT,
    ) -> "IBM4610":
        """Download a user-defined DBCS font — ``ESC ( <st> <cnt> <data>``.

        Args:
            num_chars: number of characters in *char_data*.
            char_data: raw character bitmap data.
            station:   ``STATION_RECEIPT`` (0x00) or ``STATION_SLIP`` (0x01).
        """
        st = 0x00 if station == STATION_RECEIPT else 0x01
        return self.write(bytes([0x1B, 0x28, st, num_chars & 0xFF]) + char_data)

    # ------------------------------------------------------------------
    # Spacing / positioning  (Font4610CmdFactory + Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def line_spacing(self, dots: int) -> "IBM4610":
        """Set line spacing in dots — ``ESC 3 n``."""
        return self.write(bytes([0x1B, 0x33, dots & 0xFF]))

    def formfeed_length(self, n: int) -> "IBM4610":
        """Set form-feed / page length — ``ESC C n``."""
        return self.write(bytes([0x1B, 0x43, n & 0xFF]))

    def set_tab_stops(self, stops: list) -> "IBM4610":
        """Set horizontal tab stops — ``ESC D <hi> <lo> … 0x00 0x00``.

        Args:
            stops: list of 16-bit column values (big-endian).
                   Terminated automatically by two zero bytes.
        """
        data = bytearray([0x1B, 0x44])
        for s in stops:
            data.append((s >> 8) & 0xFF)
            data.append(s & 0xFF)
        data += b'\x00\x00'
        return self.write(bytes(data))

    def left_margin(self, value: int) -> "IBM4610":
        """Set absolute left-margin position — ``ESC $ hi lo``."""
        return self.write(bytes([0x1B, 0x24,
                                 (value >> 8) & 0xFF, value & 0xFF]))

    def relative_position(self, offset: int) -> "IBM4610":
        """Set relative horizontal position — ``ESC \\ hi lo``.

        Args:
            offset: signed 16-bit displacement.
        """
        v = offset & 0xFFFF
        return self.write(bytes([0x1B, 0x5C,
                                 (v >> 8) & 0xFF, v & 0xFF]))

    def horizontal_position(self, pos: int) -> "IBM4610":
        """Set absolute horizontal position (page mode) — ``ESC $ hi lo``."""
        return self.write(bytes([0x1B, 0x24,
                                 (pos >> 8) & 0xFF, pos & 0xFF]))

    def vertical_position(self, pos: int) -> "IBM4610":
        """Set absolute vertical position (page mode) — ``GS $ hi lo``."""
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
        """Feed *n* motion units — ``ESC J n``."""
        return self.write(bytes([0x1B, 0x4A, n & 0xFF]))

    def feed_reverse(self, lines: int) -> "IBM4610":
        """Reverse-feed *lines* lines — ``ESC e n``."""
        return self.write(bytes([0x1B, 0x65, lines & 0xFF]))

    # ------------------------------------------------------------------
    # Paper cut / eject  (Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def cut(self, feed_lines: int = 0) -> "IBM4610":
        """Partial paper cut.

        Optionally feeds *feed_lines* lines first.
        Always selects the receipt station before cutting.
        Flushes the buffer immediately.
        ``CUT_PAPER = [ESC c 0 2, ESC m]``
        """
        _log.debug("cut: feed_lines=%d", feed_lines)
        if feed_lines > 0:
            self.feed(feed_lines)
        self.write(bytes([
            0x1B, 0x63, 0x30, 0x02,  # CR_COMM — receipt station
            0x1B, 0x6D,              # ESC m — partial cut
        ]))
        self.flush()
        return self

    def eject_slip(self) -> "IBM4610":
        """Eject the document / slip — ``DI_EJECT = [ESC c 0 4, ESC m]``."""
        return self.write(bytes([
            0x1B, 0x63, 0x30, 0x04,  # DIP_COMM — slip station
            0x1B, 0x6D,              # ESC m — eject
        ]))

    def return_home(self, position: int = 0) -> "IBM4610":
        """Move the print head to home / insertion position — ``ESC < n``.

        Common position values:

        * 0 → ``RETURN_HOME``
        * 1 → ``OPEN_JAWS``
        * 4 → ``SLIP_RETURN_HOME``
        * 5 → ``BEGIN_INSERTION`` / ``GRAB_SLIP``
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
        """Open a cash drawer — ``ESC p pin t_on t_off``.

        Args:
            pin:      0 = drawer 1, 1 = drawer 2.
            on_time:  pulse on-time  in 2 ms units (50 → 100 ms).
            off_time: pulse off-time in 2 ms units (50 → 100 ms).
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
        """Sound the internal beeper — ``ESC BEL <duration> <freq_vol>``.

        Mirrors ``Gen4610CmdFactory.createBeeperCmd()``.

        Args:
            duration_100ms: 1–254, each unit ≈ 100 ms.
            frequency:      tone in Hz (mapped to the nearest semitone above
                            middle-C 261.7 Hz).
            volume:         > 50 = high volume, ≤ 50 = low volume.
        """
        dur     = max(1, min(254, duration_100ms))
        vol_bit = 0x00 if volume > 50 else 0x80
        semitones = 0
        f = float(frequency)
        while f > 261.7 and semitones < 127:
            f /= 1.0595
            semitones += 1
        octave        = (semitones // 12) << 4
        note          = semitones % 12
        freq_vol_byte = vol_bit | octave | note
        return self.write(bytes([0x1B, 0x07, dur, freq_vol_byte & 0xFF]))

    # ------------------------------------------------------------------
    # Slip / document insertion  (Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def open_jaws(self) -> "IBM4610":
        """Open document insertion jaws — ``ESC < 1``."""
        return self.write(bytes([0x1B, 0x3C, 0x01]))

    def begin_insertion(self) -> "IBM4610":
        """Begin document insertion — ``ESC < 5`` (grab the slip)."""
        return self.write(bytes([0x1B, 0x3C, 0x05]))

    def end_insertion(self) -> "IBM4610":
        """End document insertion — ``SOH DC3``."""
        return self.write(bytes([0x01, 0x13]))

    def register_document(self) -> "IBM4610":
        """Register document for MICR reading.

        ``REGISTER_DOC = [ESC c 0 4, ESC f 2 1, ESC c 0 4]``
        """
        return self.write(bytes([
            0x1B, 0x63, 0x30, 0x04,  # select slip
            0x1B, 0x66, 0x02, 0x01,  # ESC f 2 1 — register
            0x1B, 0x63, 0x30, 0x04,  # select slip again
        ]))

    def end_register_document(self) -> "IBM4610":
        """End document registration.

        ``END_REGISTER_DOC = [ESC c 0 4, ESC f 2 0]``
        """
        return self.write(bytes([
            0x1B, 0x63, 0x30, 0x04,  # select slip
            0x1B, 0x66, 0x02, 0x00,  # ESC f 2 0 — end register
        ]))

    def flip_check(self) -> "IBM4610":
        """Flip the document for back-side printing — ``ESC 5``."""
        return self.write(bytes([0x1B, 0x35]))

    def change_print_side(self) -> "IBM4610":
        """Alias for :meth:`flip_check` (``ChangePrintSideCmd``)."""
        return self.flip_check()

    def set_chase_mode(self) -> "IBM4610":
        """Set chase mode for the slip station — ``ESC S 0x0A``."""
        return self.write(bytes([0x1B, 0x53, 0x0A]))

    # ------------------------------------------------------------------
    # MICR / check scanner  (Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def micr_read(self) -> "IBM4610":
        """Initiate MICR read — ``ESC I``."""
        return self.write(bytes([0x1B, 0x49]))

    def start_scan(self) -> "IBM4610":
        """Start image scan — ``ESC >``."""
        return self.write(bytes([0x1B, 0x3E]))

    def print_scanned_image(self) -> "IBM4610":
        """Print the last scanned check image — ``ESC 0``."""
        return self.write(bytes([0x1B, 0x30]))

    def retrieve_image(self) -> "IBM4610":
        """Retrieve scan-image data from printer — ``ESC 9``."""
        return self.write(bytes([0x1B, 0x39]))

    def store_scanned_image(self) -> "IBM4610":
        """Store scanned image to flash — ``ESC A``."""
        return self.write(bytes([0x1B, 0x41]))

    def get_next_image_location(self) -> "IBM4610":
        """Get next stored image location — ``GS N 1 0``."""
        return self.write(bytes([0x1D, 0x4E, 0x01, 0x00]))

    def get_first_unread_image_location(self) -> "IBM4610":
        """Get first unread image location — ``GS N 1 1``."""
        return self.write(bytes([0x1D, 0x4E, 0x01, 0x01]))

    def scanner_calibrate(self) -> "IBM4610":
        """Calibrate the check scanner — ``GS c 0``."""
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
        hri:      int = 2,   # HRI_BELOW
        encoding: str = "ascii",
    ) -> "IBM4610":
        """Print a barcode.

        Mirrors ``Grap4610CmdFactory.createPrintBarCodeCmd()``.

        Args:
            symbol:   ``BC_*`` constant (e.g. ``BC_CODE39``, ``BC_EAN13``).
            data:     barcode payload string.
            height:   bar height in dots (``GS h``).
            width:    bar module width 2–6 (``GS w``).
            align:    ``ALIGN_LEFT`` / ``ALIGN_CENTER`` / ``ALIGN_RIGHT``.
            hri:      ``HRI_NONE`` / ``HRI_ABOVE`` / ``HRI_BELOW`` / ``HRI_BOTH``.
            encoding: codec for *data* (default ``ascii``).

        ``BC_PDF417`` is handled via ``GS P`` instead of the standard ``GS k``.
        """
        self.alignment(align)
        data_bytes = data.encode(encoding)

        if symbol == BC_PDF417:
            self.write(bytes([0x1D, 0x50]))   # GS P
            self.write(data_bytes + b'\x00')
            return self

        self.write(bytes([0x1D, 0x48, hri & 0xFF]))       # GS H — HRI position
        self.write(bytes([0x1D, 0x68, height & 0xFF]))    # GS h — bar height
        self.write(bytes([0x1D, 0x77, width & 0xFF]))     # GS w — bar width
        self.write(bytes([0x1D, 0x6B, symbol & 0xFF]))    # GS k — barcode type

        if symbol == BC_CODE128A:
            self.write(bytes([len(data_bytes)]))
        self.write(data_bytes)
        if symbol != BC_CODE128A:
            self.write(b'\x00')

        self.alignment(ALIGN_LEFT)
        return self

    # ------------------------------------------------------------------
    # Graphics / bitmaps  (Grap4610CmdFactory)
    # ------------------------------------------------------------------

    def print_bitmap(
        self,
        density:      int,
        width_bytes:  int,
        height_bytes: int,
        data:         bytes,
        align:        int = ALIGN_LEFT,
    ) -> "IBM4610":
        """Print an inline raster bitmap — ``ESC * density width height data``.

        Uses the IBM 4610 ``PRINT_LOGOS`` command (``{0x1B, 0x2A}``),
        mirroring ``Grap4610CmdFactory.createPrintBitmapCmd()``.

        Args:
            density:      ``DENSITY_NORMAL`` (0) or ``DENSITY_DOUBLE`` (1).
            width_bytes:  horizontal extent in 8-dot units (pixel columns / 8).
            height_bytes: vertical extent in 8-dot units (pixel rows / 8).
            data:         column-major bitmap bytes.
                          Length must equal ``width_bytes * height_bytes * 8``.
            align:        horizontal alignment.
        """
        self.alignment(align)
        self.write(bytes([0x1B, 0x2A, density & 0xFF,
                          width_bytes & 0xFF, height_bytes & 0xFF]))
        return self.write(data)

    def set_bitmap(
        self,
        bitmap_no: int,
        width:     int,
        height:    int,
        data:      bytes,
    ) -> "IBM4610":
        """Define a stored (RAM) bitmap — ``GS * no w h data``.

        Use :meth:`print_set_bitmap` to print it later.
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
        """Print a previously stored bitmap — ``GS / density bitmap_no``.

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
        """Define stored-logo position — ``GS : location``."""
        return self.write(bytes([0x1D, 0x3A, location & 0xFF]))

    def print_set_logo(self, location: int) -> "IBM4610":
        """Print a stored logo — ``GS ^ location``, then request EC level."""
        self.write(bytes([0x1D, 0x5E, location & 0xFF]))
        return self.ec_level_request(buffered=True)

    def download_logo(
        self,
        density:      int,
        width_bytes:  int,
        height_bytes: int,
        data:         bytes,
    ) -> "IBM4610":
        """Download a logo to flash — ``ESC * 0 w h data``."""
        return self.write(bytes([0x1B, 0x2A, 0x00,
                                 width_bytes & 0xFF, height_bytes & 0xFF]) + data)

    def erase_logos(self) -> "IBM4610":
        """Erase all downloaded logos from flash — ``ESC # 1``."""
        return self.write(bytes([0x1B, 0x23, 0x01]))

    # ------------------------------------------------------------------
    # Page mode  (Grap4610CmdFactory)
    # ------------------------------------------------------------------

    def page_mode_enable(self) -> "IBM4610":
        """Enter page mode — ``ESC L``."""
        return self.write(bytes([0x1B, 0x4C]))

    def page_mode_disable(self) -> "IBM4610":
        """Exit page mode back to standard (line) mode — ``ESC O``."""
        return self.write(bytes([0x1B, 0x4F]))

    def page_mode_define(
        self,
        x:  int,
        y:  int,
        dx: int,
        dy: int,
    ) -> "IBM4610":
        """Define page-mode print area — ``ESC X x y dx dy``.

        All coordinates are in dots.
        Mirrors ``Grap4610CmdFactory.createPMPageDefineCmd()``.
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
        """Set page-mode print direction — ``ESC T n``.

        * 0 — top→bottom, left→right
        * 1 — bottom→top, left→right
        * 2 — bottom→top, right→left
        * 3 — top→bottom, right→left
        """
        return self.write(bytes([0x1B, 0x54, position & 0xFF]))

    def page_mode_print(
        self,
        clear_after: bool = False,
        re_enable:   bool = False,
    ) -> "IBM4610":
        """Print the page-mode buffer — ``ESC FF``.

        Args:
            clear_after: also send ``CAN`` (0x18) to clear the buffer.
            re_enable:   wrap with ``ESC L … ESC O`` so page mode stays active.
        """
        if re_enable:
            self.page_mode_enable()
        self.write(bytes([0x1B, 0x0C]))
        if clear_after:
            self.write(bytes([0x18]))
        if re_enable:
            self.page_mode_disable()
        return self

    def page_mode_clear(self) -> "IBM4610":
        """Clear page-mode buffer — ``CAN`` (0x18)."""
        return self.write(bytes([0x18]))

    def page_mode_normal(self) -> "IBM4610":
        """Return to line mode from page mode — ``FF`` (0x0C)."""
        return self.write(bytes([0x0C]))

    # ------------------------------------------------------------------
    # Flash management  (Gen4610CmdFactory.createEraseFlashSectorCmd)
    # ------------------------------------------------------------------

    def erase_flash(self, sector: int) -> "IBM4610":
        """Erase a flash sector — ``ESC # n``.

        Args:
            sector: ``FLASH_*`` constant (1–6, 8).

        Raises:
            ValueError: for an unrecognised sector number.
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
        """Erase pre-stored messages from flash — ``ESC # 2``."""
        return self.write(bytes([0x1B, 0x23, 0x02]))

    # ------------------------------------------------------------------
    # Error recovery / line count  (Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def error_recovery(self, flags: int) -> "IBM4610":
        """Configure error-recovery behaviour — ``ESC c 4 flags``.

        Build *flags* from ``ER_*`` constants::

            p.error_recovery(ER_RELEASE_AFTER_CORRECTION | ER_WAIT_FOR_DOCUMENT)

        Mirrors ``Gen4610CmdFactory.createErrorRecoveryCmd()``.
        """
        return self.write(bytes([0x1B, 0x63, 0x34, flags & 0xFF]))

    def enable_line_count(self, enable: bool) -> "IBM4610":
        """Enable/disable the line counter — ``ESC 8 n``.

        Note: the Java driver uses an inverted sense (0 = enabled, 1 = disabled).
        """
        return self.write(bytes([0x1B, 0x38, 0x00 if enable else 0x01]))

    def reset_line_count(self) -> "IBM4610":
        """Reset the line counter — ``ESC 6``."""
        return self.write(bytes([0x1B, 0x36]))

    def enable_feed_button(self, on: bool, button: int = 0) -> "IBM4610":
        """Enable/disable the feed button — ``ESC c 5 n``.

        Args:
            on:     ``True`` = enable, ``False`` = disable.
            button: button identifier (0 = primary feed button).
        """
        return self.write(bytes([0x1B, 0x63, 0x35,
                                 0x01 if on else 0x00]))

    # ------------------------------------------------------------------
    # MCT (Magnetic Card Transport)  (Gen4610CmdFactory)
    # ------------------------------------------------------------------

    def mct_read(self, mct_value: int) -> "IBM4610":
        """Read MCT (magnetic card track) — ``ESC S value``."""
        return self.write(bytes([0x1B, 0x53, mct_value & 0xFF]))

    def mct_write(self, matrix: int, high: int, low: int) -> "IBM4610":
        """Write MCT value — ``ESC M matrix hi lo``."""
        return self.write(bytes([0x1B, 0x4D,
                                 matrix & 0xFF, high & 0xFF, low & 0xFF]))

    # ------------------------------------------------------------------
    # Statistics  (Gen4610CmdFactory.createStatisticCmd)
    # ------------------------------------------------------------------

    def statistic(self, stat_type: str) -> "IBM4610":
        """Request a printer statistic — ``ESC Q <sub-cmd>``.

        Args:
            stat_type: key from :data:`~py_ibm_4610.STATISTIC_SUBCMDS`,
                       e.g. ``"PaperCutCount"``, ``"ManufactureDate"``.

        Raises:
            ValueError: if *stat_type* is not in ``STATISTIC_SUBCMDS``.
        """
        sub = STATISTIC_SUBCMDS.get(stat_type)
        if sub is None:
            raise ValueError(
                f"Unknown statistic type {stat_type!r}. "
                f"Valid keys: {sorted(STATISTIC_SUBCMDS)}"
            )
        return self.write(bytes([0x1B, 0x51]) + sub)

    # ------------------------------------------------------------------
    # High-level convenience helpers
    # ------------------------------------------------------------------

    def print_line(self, text: str, encoding: str = "cp437") -> int:
        """Encode and print *text* + CRLF, flush immediately.

        Returns the number of bytes transferred.
        """
        _log.debug("print_line: %r", text[:80])
        self.write(text.encode(encoding) + b'\r\n')
        return self.flush()

    def print_receipt(
        self,
        lines:   list,
        feed:    int  = 5,
        cut:     bool = True,
        station: int  = STATION_RECEIPT,
    ) -> int:
        """High-level receipt helper — print *lines*, feed, and optionally cut.

        Args:
            lines:   list of ``str`` or ``bytes`` — each printed as one line.
            feed:    blank lines to feed before cutting (default 5).
            cut:     issue a partial paper cut at the end (default ``True``).
            station: station to select (default ``STATION_RECEIPT``).

        Returns total bytes transferred.
        """
        _log.info("print_receipt: %d line(s), feed=%d, cut=%s", len(lines), feed, cut)
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
