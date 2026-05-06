"""ref_page.py — ReferencePage: a printable reference / demo page.

Exercises the major feature groups of the IBM 4610 SureMark printer,
providing an easy way to verify hardware functionality and driver
correctness out of the box.

Each feature group is a named *section* that can be independently
enabled or disabled before printing::

    from py_ibm_4610 import IBM4610, ReferencePage

    with IBM4610() as p:
        ref = ReferencePage(p)
        ref.disable(ReferencePage.MCT, ReferencePage.STATISTICS)
        ref.print()

    # Print only barcodes and bitmap sections:
    with IBM4610() as p:
        ReferencePage(p).only(ReferencePage.BARCODES, ReferencePage.BITMAP).print()
"""

from __future__ import annotations

import logging
from datetime import datetime

from .printer.ibm4610 import IBM4610
from ._constants import (
    STATION_RECEIPT,
    ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT,
    FONT_A, FONT_B, FONT_C,
    BC_UPCA, BC_EAN13, BC_CODE39, BC_ITF, BC_CODABAR, BC_CODE128,
    HRI_BELOW, HRI_BOTH,
    DENSITY_NORMAL,
    ER_RELEASE_AFTER_CORRECTION, ER_AUTO_RETRY_AFTER_HOME_ERR,
)

_log = logging.getLogger(__name__)

_SEP = "-" * 40


class ReferencePage:
    """Printable reference / demo page for IBM 4610 SureMark printers.

    Instantiate with a connected :class:`~py_ibm_4610.IBM4610` (or subclass),
    optionally enable/disable sections, then call :meth:`print`.

    All sections are **enabled by default**.  Use :meth:`disable` to turn off
    sections you are not interested in, or :meth:`only` to print a subset.

    Section name constants
    ----------------------
    .. code-block:: text

        TEXT_FORMATTING   — bold, underline, double-wide, double-high, reverse video
        FONT_SCALING      — GS ! scale commands (width × height)
        FONT_FACES        — Font A, Font B, Font C
        ALIGNMENT         — left, center, right text alignment
        FONT_COLOR        — color 0 (black) and color 1 (red/dual-color)
        ROTATION          — 90° and 180° character rotation
        LINE_SPACING      — variable dot-line spacing
        FEED              — feed() and feed_units()
        TAB_STOPS         — custom horizontal tab stops
        MARGINS           — left_margin, relative_position
        DOT_SPACING       — inter-character dot spacing
        CHAR_SETS         — resident and code-page character sets
        PRINT_QUALITY     — high/normal quality, unidirectional (slip only)
        BARCODES          — UPC-A, EAN-13, Code39, ITF, Codabar, Code128
        BITMAP            — inline raster bitmap (solid band + checkerboard)
        PAGE_MODE         — ESC L page-mode print area
        BUFFER_CONTROL    — informational note on hold/release/cancel
        STATUS            — buffered EC level request
        LINE_COUNT        — enable/reset line counter
        STATUS_SENT       — status-notification configuration
        ERROR_RECOVERY    — error-recovery flag configuration
        FEED_BUTTON       — enable/disable feed button
        CASH_DRAWER       — cash drawer pulse
        BEEPER            — beeper tones at different frequencies / volumes
        MCT               — magnetic card transport read
        STATISTICS        — selected printer statistics (USB request/response)
    """

    # ------------------------------------------------------------------
    # Section name constants
    # ------------------------------------------------------------------

    TEXT_FORMATTING: str = "text_formatting"
    FONT_SCALING:    str = "font_scaling"
    FONT_FACES:      str = "font_faces"
    ALIGNMENT:       str = "alignment"
    FONT_COLOR:      str = "font_color"
    ROTATION:        str = "rotation"
    LINE_SPACING:    str = "line_spacing"
    FEED:            str = "feed"
    TAB_STOPS:       str = "tab_stops"
    MARGINS:         str = "margins"
    DOT_SPACING:     str = "dot_spacing"
    CHAR_SETS:       str = "char_sets"
    PRINT_QUALITY:   str = "print_quality"
    BARCODES:        str = "barcodes"
    BITMAP:          str = "bitmap"
    PAGE_MODE:       str = "page_mode"
    BUFFER_CONTROL:  str = "buffer_control"
    STATUS:          str = "status"
    LINE_COUNT:      str = "line_count"
    STATUS_SENT:     str = "status_sent"
    ERROR_RECOVERY:  str = "error_recovery"
    FEED_BUTTON:     str = "feed_button"
    CASH_DRAWER:     str = "cash_drawer"
    BEEPER:          str = "beeper"
    MCT:             str = "mct"
    STATISTICS:      str = "statistics"

    #: Ordered list of all section names (determines print order).
    ALL_SECTIONS: tuple = (
        TEXT_FORMATTING,
        FONT_SCALING,
        FONT_FACES,
        ALIGNMENT,
        FONT_COLOR,
        ROTATION,
        LINE_SPACING,
        FEED,
        TAB_STOPS,
        MARGINS,
        DOT_SPACING,
        CHAR_SETS,
        PRINT_QUALITY,
        BARCODES,
        BITMAP,
        PAGE_MODE,
        BUFFER_CONTROL,
        STATUS,
        LINE_COUNT,
        STATUS_SENT,
        ERROR_RECOVERY,
        FEED_BUTTON,
        CASH_DRAWER,
        BEEPER,
        MCT,
        STATISTICS,
    )

    # ------------------------------------------------------------------
    # Construction
    # ------------------------------------------------------------------

    def __init__(self, printer: IBM4610) -> None:
        """Create a ReferencePage attached to *printer*.

        All sections are enabled by default.

        Args:
            printer: an open (or to-be-opened) :class:`IBM4610` instance.
        """
        self._printer:  IBM4610    = printer
        self._enabled:  set[str]   = set(self.ALL_SECTIONS)

    # ------------------------------------------------------------------
    # Section selection API
    # ------------------------------------------------------------------

    def enable(self, *sections: str) -> "ReferencePage":
        """Enable one or more sections by name.

        Args:
            sections: one or more ``ReferencePage.*`` section name constants.

        Returns *self* for chaining.
        """
        self._enabled.update(sections)
        return self

    def disable(self, *sections: str) -> "ReferencePage":
        """Disable one or more sections by name.

        Args:
            sections: one or more ``ReferencePage.*`` section name constants.

        Returns *self* for chaining.
        """
        self._enabled -= set(sections)
        return self

    def only(self, *sections: str) -> "ReferencePage":
        """Enable *only* the given sections, disabling all others.

        Args:
            sections: one or more ``ReferencePage.*`` section name constants.

        Returns *self* for chaining.
        """
        self._enabled = set(sections)
        return self

    def enable_all(self) -> "ReferencePage":
        """Re-enable every section.  Returns *self* for chaining."""
        self._enabled = set(self.ALL_SECTIONS)
        return self

    def is_enabled(self, section: str) -> bool:
        """Return ``True`` if *section* is currently enabled."""
        return section in self._enabled

    # ------------------------------------------------------------------
    # Public print entry point
    # ------------------------------------------------------------------

    def print(self, cut: bool = True) -> None:
        """Print the reference page on the receipt station.

        Builds the enabled sections into the printer buffer, flushes,
        and optionally cuts.

        Args:
            cut: issue a partial paper cut at the end (default ``True``).
        """
        enabled = [s for s in self.ALL_SECTIONS if s in self._enabled]
        _log.info(
            "ReferencePage: printing %d/%d section(s): %s",
            len(enabled), len(self.ALL_SECTIONS),
            ", ".join(enabled) or "(none)",
        )
        p = self._printer
        p.select_station(STATION_RECEIPT)
        self._print_header()

        for section_name in self.ALL_SECTIONS:
            if section_name in self._enabled:
                _log.debug("ReferencePage: section %s", section_name)
                getattr(self, f"_section_{section_name}")()

        self._print_footer(cut=cut)
        _log.info("ReferencePage: done (cut=%s)", cut)

    # ------------------------------------------------------------------
    # Internal helpers
    # ------------------------------------------------------------------

    def _sep(self, title: str) -> None:
        """Print a visible section separator line."""
        p = self._printer
        p.normal_mode()
        p.alignment(ALIGN_LEFT)
        p.write(f"\n{_SEP}\n{title}\n{_SEP}\n".encode("cp437"))

    # ------------------------------------------------------------------
    # Header / footer
    # ------------------------------------------------------------------

    def _print_header(self) -> None:
        p = self._printer
        p.alignment(ALIGN_CENTER)
        p.bold(True)
        p.scale_font(width=1, height=1)
        p.write(b"IBM 4610 SUREMARK\n")
        p.write(b"REFERENCE PAGE\n")
        p.scale_font(width=0, height=0)
        p.bold(False)
        ts = datetime.now().strftime("%Y-%m-%d  %H:%M:%S")
        p.write(f"{ts}\n".encode("cp437"))
        p.alignment(ALIGN_LEFT)
        p.flush()

    def _print_footer(self, cut: bool) -> None:
        p = self._printer
        p.write(f"\n{_SEP}\n".encode("cp437"))
        p.alignment(ALIGN_CENTER)
        p.bold(True)
        p.write(b"END OF REFERENCE PAGE\n")
        p.bold(False)
        p.alignment(ALIGN_LEFT)
        p.normal_mode()
        p.feed(6)
        if cut:
            p.cut()
        else:
            p.flush()

    # ------------------------------------------------------------------
    # Section methods
    # (named _section_<SECTION_NAME_CONSTANT> — must match exactly)
    # ------------------------------------------------------------------

    def _section_text_formatting(self) -> None:
        p = self._printer
        self._sep("1. TEXT FORMATTING")

        p.write(b"Normal text\n")

        p.bold(True)
        p.write(b"Bold text\n")
        p.bold(False)

        p.underline(True)
        p.write(b"Underline text\n")
        p.underline(False)

        p.double_wide(True)
        p.write(b"Double wide\n")
        p.double_wide(False)

        p.double_high(True)
        p.write(b"Double high\n")
        p.double_high(False)

        p.double_wide_high(True)
        p.write(b"Double wide + high\n")
        p.double_wide_high(False)

        p.reverse_video(True)
        p.write(b"Reverse video\n")
        p.reverse_video(False)

        p.normal_mode()
        p.write(b"normal_mode() reset\n")
        p.flush()

    def _section_font_scaling(self) -> None:
        p = self._printer
        self._sep("2. FONT SCALING (GS !)")

        for w, h in [(0, 0), (1, 0), (0, 1), (1, 1), (2, 2)]:
            p.scale_font(width=w, height=h)
            p.write(f"scale {w}w {h}h\n".encode("cp437"))
        p.scale_font(0, 0)
        p.flush()

    def _section_font_faces(self) -> None:
        p = self._printer
        self._sep("3. FONT FACES")

        for face, name in [
            (FONT_A, "Font A (10x20)"),
            (FONT_B, "Font B (12x24)"),
            (FONT_C, "Font C ( 8x16)"),
        ]:
            p.select_font(face)
            p.write(f"{name}: ABCDEFGHIJ 0123456789\n".encode("cp437"))
        p.select_font(FONT_A)
        p.flush()

    def _section_alignment(self) -> None:
        p = self._printer
        self._sep("4. ALIGNMENT")

        p.alignment(ALIGN_LEFT)
        p.write(b"Left aligned\n")
        p.alignment(ALIGN_CENTER)
        p.write(b"Centered\n")
        p.alignment(ALIGN_RIGHT)
        p.write(b"Right aligned\n")
        p.alignment(ALIGN_LEFT)
        p.flush()

    def _section_font_color(self) -> None:
        p = self._printer
        self._sep("5. FONT COLOR")

        p.font_color(0)
        p.write(b"Color 0 (black)\n")
        p.font_color(1)
        p.write(b"Color 1 (red on dual-color models)\n")
        p.font_color(0)
        p.flush()

    def _section_rotation(self) -> None:
        p = self._printer
        self._sep("6. ROTATION")

        p.rotate_90(True)
        p.write(b"rotate_90 ON\n")
        p.rotate_90(False)

        p.rotate_180(True)
        p.write(b"rotate_180 ON\n")
        p.rotate_180(False)
        p.flush()

    def _section_line_spacing(self) -> None:
        p = self._printer
        self._sep("7. LINE SPACING")

        for dots in [24, 36, 48]:
            p.line_spacing(dots)
            p.write(f"Line spacing {dots} dots\n".encode("cp437"))
        p.line_spacing(30)
        p.flush()

    def _section_feed(self) -> None:
        p = self._printer
        self._sep("8. FEED COMMANDS")

        p.write(b"Before feed(2)\n")
        p.feed(2)
        p.write(b"After feed(2)\n")

        p.write(b"Before feed_units(40)\n")
        p.feed_units(40)
        p.write(b"After feed_units(40)\n")
        p.flush()

    def _section_tab_stops(self) -> None:
        p = self._printer
        self._sep("9. TAB STOPS")

        p.set_tab_stops([80, 160, 240])
        p.write(b"A\tB\tC\tD\n")
        p.set_tab_stops([])
        p.flush()

    def _section_margins(self) -> None:
        p = self._printer
        self._sep("10. MARGINS & POSITION")

        p.left_margin(40)
        p.write(b"Left margin 40 dots\n")
        p.left_margin(0)

        p.relative_position(20)
        p.write(b"Relative +20 dots\n")
        p.flush()

    def _section_dot_spacing(self) -> None:
        p = self._printer
        self._sep("11. DOT SPACING")

        for n in [0, 3, 6]:
            p.dot_spacing(n)
            p.write(f"SBCS spacing={n}: Hello World\n".encode("cp437"))
        p.dot_spacing(0)
        p.flush()

    def _section_char_sets(self) -> None:
        p = self._printer
        self._sep("12. CHARACTER SETS")

        p.resident_char_set()
        p.write(b"Resident char set\n")

        p.select_code_page(0)
        p.write(b"Code page 437 (US)\n")

        p.select_code_page(1)
        p.write(b"Code page 858 (Multilingual)\n")

        p.select_code_page(0)
        p.flush()

    def _section_print_quality(self) -> None:
        p = self._printer
        self._sep("13. PRINT QUALITY")

        p.print_quality(True)
        p.write(b"High quality ON\n")
        p.print_quality(False)
        p.write(b"High quality OFF (standard)\n")
        p.flush()

    def _section_barcodes(self) -> None:
        p = self._printer
        self._sep("14. BARCODES")

        barcodes = [
            (BC_UPCA,    "UPC-A",    "012345678905"),
            (BC_EAN13,   "EAN-13",   "5901234123457"),
            (BC_CODE39,  "Code 39",  "CODE39TEST"),
            (BC_ITF,     "ITF",      "1234567890"),
            (BC_CODABAR, "Codabar",  "A12345B"),
            (BC_CODE128, "Code 128", "CODE128DEMO"),
        ]
        for symbol, label, data in barcodes:
            p.write(f"--- {label} ---\n".encode("cp437"))
            hri = HRI_BOTH if symbol == BC_CODE128 else HRI_BELOW
            align = ALIGN_CENTER if symbol == BC_CODE128 else ALIGN_LEFT
            p.barcode(symbol, data, height=50, width=2,
                      align=align, hri=hri)
            p.feed(2)
            p.flush()

    def _section_bitmap(self) -> None:
        p = self._printer
        self._sep("15. INLINE BITMAP")

        # Solid black band: 64 columns wide (8 bytes × 8 dots), 32 dots tall
        bw, bh = 8, 4
        p.write(b"Solid black band (64x32 dots):\n")
        p.print_bitmap(DENSITY_NORMAL, bw, bh, bytes([0xFF] * bw * bh * 8))
        p.lf()

        # Checkerboard: 8×8 dot cells, 64 columns wide, 64 dots tall
        bw, bh = 8, 8
        bmp_data = bytes(
            0xFF if ((c // 8) + b) % 2 == 0 else 0x00
            for c in range(bw * 8)
            for b in range(bh)
        )
        p.write(b"Checkerboard (8x8 dot cells, 64x64 dots):\n")
        p.print_bitmap(DENSITY_NORMAL, bw, bh, bmp_data)
        p.lf()
        p.flush()

    def _section_page_mode(self) -> None:
        p = self._printer
        self._sep("16. PAGE MODE")

        p.page_mode_enable()
        p.page_mode_define(x=0, y=0, dx=200, dy=100)
        p.page_mode_position(0)
        p.write(b"Page mode text")
        p.page_mode_print(clear_after=True)
        p.page_mode_disable()
        p.write(b"\n")
        p.flush()

    def _section_buffer_control(self) -> None:
        p = self._printer
        self._sep("17. BUFFER CONTROL")

        p.write(b"hold/release: interactive use only\n")
        p.write(b"cancel_buffer: clears queued data\n")
        p.flush()

    def _section_status(self) -> None:
        p = self._printer
        self._sep("18. STATUS & DEVICE INFO")

        p.ec_level_request(buffered=True)
        p.write(b"ec_level_request sent\n")
        p.write(b"(response on interrupt IN endpoint)\n")
        p.flush()

    def _section_line_count(self) -> None:
        p = self._printer
        self._sep("19. LINE COUNT")

        p.enable_line_count(True)
        p.write(b"Line count enabled\n")
        p.reset_line_count()
        p.write(b"Line count reset\n")
        p.enable_line_count(False)
        p.flush()

    def _section_status_sent(self) -> None:
        p = self._printer
        self._sep("20. STATUS-SENT CONFIG")

        p.status_sent(buff_empty=True, cover_open=True)
        p.write(b"status_sent configured\n")
        p.write(b"  buff_empty=True, cover_open=True\n")
        p.flush()

    def _section_error_recovery(self) -> None:
        p = self._printer
        self._sep("21. ERROR RECOVERY")

        p.error_recovery(
            ER_RELEASE_AFTER_CORRECTION | ER_AUTO_RETRY_AFTER_HOME_ERR
        )
        p.write(b"error_recovery flags set:\n")
        p.write(b"  RELEASE_AFTER_CORRECTION\n")
        p.write(b"  AUTO_RETRY_AFTER_HOME_ERR\n")
        p.flush()

    def _section_feed_button(self) -> None:
        p = self._printer
        self._sep("22. FEED BUTTON")

        p.enable_feed_button(True)
        p.write(b"Feed button enabled\n")
        p.enable_feed_button(False)
        p.write(b"Feed button disabled\n")
        p.flush()

    def _section_cash_drawer(self) -> None:
        p = self._printer
        self._sep("23. CASH DRAWER")

        p.write(b"Pulsing drawer 1 (100ms on, 100ms off)...\n")
        p.pulse_drawer(pin=0, on_time=50, off_time=50)
        p.flush()

    def _section_beeper(self) -> None:
        p = self._printer
        self._sep("24. BEEPER")

        tones = [
            (3, 500,  80, "500 Hz, 300ms, vol high"),
            (1, 1000, 30, "1000 Hz, 100ms, vol low"),
            (2, 880,  80, "880 Hz (A5), 200ms, vol high"),
        ]
        for dur, freq, vol, label in tones:
            p.write(f"  {label}\n".encode("cp437"))
            p.beep(duration_100ms=dur, frequency=freq, volume=vol)
            p.flush()

    def _section_mct(self) -> None:
        p = self._printer
        self._sep("25. MCT (MAGNETIC CARD TRANSPORT)")

        p.write(b"Requesting MCT read track 1...\n")
        p.mct_read(0x01)
        p.write(b"(response on interrupt IN endpoint)\n")
        p.flush()

    def _section_statistics(self) -> None:
        p = self._printer
        self._sep("26. STATISTICS QUERIES")

        keys = [
            "ManufactureDate",
            "PaperCutCount",
            "ReceiptLineFeedCount",
            "ReceiptCharacterPrintedCount",
        ]
        for key in keys:
            p.write(f"  {key}:\n".encode("cp437"))
            p.flush()
            try:
                resp = p.read_stat(key, timeout=2000)
                if resp:
                    trimmed = resp.rstrip(b'\x00')
                    display = trimmed[:32]
                    suffix = b"..." if len(trimmed) > 32 else b""
                    p.write(b"  " + display.hex().encode("ascii") + suffix + b"\n")
                else:
                    p.write(b"  (no response)\n")
            except Exception as exc:
                short = str(exc)[:36]
                p.write(f"  err: {short}\n".encode("cp437"))
            p.flush()
