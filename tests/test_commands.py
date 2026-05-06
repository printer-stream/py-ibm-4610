"""Tests for IBM4610 command methods — verifies ESC/POS byte sequences.

All tests run offline (no USB hardware required).  Command methods only
accumulate bytes in a buffer; ``build()`` drains that buffer so the
exact byte sequence can be asserted.

A ``_StubIBM4610`` subclass replaces the USB transport methods with
no-ops so tests that involve ``flush()`` (e.g. ``cut()``) also work
without a printer.
"""

import pytest

from py_ibm_4610 import IBM4610
from py_ibm_4610._constants import (
    STATION_RECEIPT, STATION_SLIP, STATION_LABEL,
    ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT,
    FONT_A, FONT_B, FONT_C,
    BC_CODE39, BC_CODE128A, BC_PDF417,
    STATISTIC_SUBCMDS,
)


class _StubIBM4610(IBM4610):
    """USB-less IBM4610 for offline unit tests."""

    def open(self) -> "_StubIBM4610":
        return self

    def close(self) -> None:
        pass

    def send_raw(self, data: bytes) -> int:
        return len(data)


@pytest.fixture
def p() -> _StubIBM4610:
    """Fresh stub printer for each test."""
    return _StubIBM4610()


# ---------------------------------------------------------------------------
# Text output
# ---------------------------------------------------------------------------

class TestText:
    def test_text_str_encodes_cp437(self, p):
        p.text("Hello")
        assert p.build() == b"Hello"

    def test_text_bytes_passthrough(self, p):
        p.text(b"\x1B\x40")
        assert p.build() == b"\x1B\x40"

    def test_text_bytearray_passthrough(self, p):
        p.text(bytearray(b"\x01\x02"))
        assert p.build() == b"\x01\x02"

    def test_text_custom_encoding(self, p):
        p.text("A", encoding="ascii")
        assert p.build() == b"A"

    def test_lf(self, p):
        p.lf()
        assert p.build() == b"\n"

    def test_crlf(self, p):
        p.crlf()
        assert p.build() == b"\r\n"

    def test_raw_bytes(self, p):
        p.raw_bytes(b"\xDE\xAD\xBE\xEF")
        assert p.build() == b"\xDE\xAD\xBE\xEF"


# ---------------------------------------------------------------------------
# Text formatting
# ---------------------------------------------------------------------------

class TestFormatting:
    def test_bold_on(self, p):
        p.bold(True)
        assert p.build() == bytes([0x1B, 0x47, 0x01])

    def test_bold_off(self, p):
        p.bold(False)
        assert p.build() == bytes([0x1B, 0x47, 0x00])

    def test_underline_on(self, p):
        p.underline(True)
        assert p.build() == bytes([0x1B, 0x2D, 0x01])

    def test_underline_off(self, p):
        p.underline(False)
        assert p.build() == bytes([0x1B, 0x2D, 0x00])

    def test_double_wide_on(self, p):
        p.double_wide(True)
        assert p.build() == bytes([0x1B, 0x57, 0x01])

    def test_double_high_on(self, p):
        p.double_high(True)
        assert p.build() == bytes([0x1B, 0x68, 0x01])

    def test_reverse_video_on(self, p):
        p.reverse_video(True)
        assert p.build() == bytes([0x1B, 0x48, 0x01])

    def test_normal_mode_resets_all_attributes(self, p):
        data = p.normal_mode().build()
        assert bytes([0x1B, 0x47, 0x00]) in data  # bold off
        assert bytes([0x1B, 0x57, 0x00]) in data  # double-wide off
        assert bytes([0x1B, 0x68, 0x00]) in data  # double-high off
        assert bytes([0x1B, 0x48, 0x00]) in data  # reverse-video off
        assert bytes([0x1B, 0x2D, 0x00]) in data  # underline off

    def test_scale_font(self, p):
        p.scale_font(width=2, height=3)
        assert p.build() == bytes([0x1D, 0x21, (2 << 4) | 3])

    def test_scale_font_defaults(self, p):
        p.scale_font()
        assert p.build() == bytes([0x1D, 0x21, 0x00])

    def test_select_font_a(self, p):
        p.select_font(FONT_A)
        assert p.build() == bytes([0x1B, 0x21, FONT_A])

    def test_alignment_center(self, p):
        p.alignment(ALIGN_CENTER)
        assert p.build() == bytes([0x1B, 0x61, ALIGN_CENTER])

    def test_alignment_right(self, p):
        p.alignment(ALIGN_RIGHT)
        assert p.build() == bytes([0x1B, 0x61, ALIGN_RIGHT])

    def test_font_color(self, p):
        p.font_color(1)
        assert p.build() == bytes([0x1B, 0x72, 0x01])

    def test_rotate_90_on(self, p):
        p.rotate_90(True)
        assert p.build() == bytes([0x1B, 0x56, 0x01])

    def test_rotate_180_off(self, p):
        p.rotate_180(False)
        assert p.build() == bytes([0x1B, 0x7B, 0x00])


# ---------------------------------------------------------------------------
# Station selection
# ---------------------------------------------------------------------------

class TestStationSelect:
    def test_select_receipt(self, p):
        p.select_station(STATION_RECEIPT)
        data = p.build()
        assert bytes([0x1B, 0x63, 0x30, 0x02]) in data

    def test_select_slip(self, p):
        p.select_station(STATION_SLIP)
        data = p.build()
        assert bytes([0x1B, 0x63, 0x30, 0x04]) in data

    def test_select_label(self, p):
        p.select_station(STATION_LABEL)
        assert p.build() == bytes([0x1B, 0x63, 0x30, 0x08])

    def test_select_unknown_station_raises(self, p):
        with pytest.raises(ValueError):
            p.select_station(0xFF)


# ---------------------------------------------------------------------------
# Paper feed
# ---------------------------------------------------------------------------

class TestFeed:
    def test_feed_one_line(self, p):
        p.feed(1)
        assert p.build() == b" \n"

    def test_feed_three_lines(self, p):
        p.feed(3)
        assert p.build() == b" \n \n \n"

    def test_feed_units(self, p):
        p.feed_units(50)
        assert p.build() == bytes([0x1B, 0x4A, 50])

    def test_feed_reverse(self, p):
        p.feed_reverse(3)
        assert p.build() == bytes([0x1B, 0x65, 3])


# ---------------------------------------------------------------------------
# Spacing / positioning
# ---------------------------------------------------------------------------

class TestSpacing:
    def test_line_spacing(self, p):
        p.line_spacing(30)
        assert p.build() == bytes([0x1B, 0x33, 30])

    def test_left_margin(self, p):
        p.left_margin(0x0100)
        assert p.build() == bytes([0x1B, 0x24, 0x01, 0x00])

    def test_set_tab_stops(self, p):
        p.set_tab_stops([0x0010, 0x0020])
        data = p.build()
        assert data[:2] == bytes([0x1B, 0x44])
        assert data[-2:] == b'\x00\x00'
        assert bytes([0x00, 0x10, 0x00, 0x20]) in data

    def test_dot_spacing_sbcs(self, p):
        p.dot_spacing(4)
        assert p.build() == bytes([0x1B, 0x20, 4])

    def test_dot_spacing_dbcs(self, p):
        p.dot_spacing(10, dbcs=True)
        assert p.build() == bytes([0x1B, 0x52, 10])

    def test_dot_spacing_clamps_sbcs_max(self, p):
        p.dot_spacing(99)
        assert p.build() == bytes([0x1B, 0x20, 8])

    def test_dot_spacing_clamps_sbcs_min(self, p):
        p.dot_spacing(-5)
        assert p.build() == bytes([0x1B, 0x20, 0])


# ---------------------------------------------------------------------------
# Cash drawer / beeper
# ---------------------------------------------------------------------------

class TestAccessories:
    def test_pulse_drawer_defaults(self, p):
        p.pulse_drawer()
        assert p.build() == bytes([0x1B, 0x70, 0x00, 50, 50])

    def test_pulse_drawer_pin1(self, p):
        p.pulse_drawer(pin=1, on_time=20, off_time=30)
        assert p.build() == bytes([0x1B, 0x70, 0x01, 20, 30])

    def test_beep_returns_self(self, p):
        result = p.beep()
        assert result is p

    def test_beep_produces_4_bytes(self, p):
        p.beep(duration_100ms=2)
        data = p.build()
        assert len(data) == 4
        assert data[0] == 0x1B
        assert data[1] == 0x07
        assert data[2] == 2  # duration


# ---------------------------------------------------------------------------
# Buffer control / status commands
# ---------------------------------------------------------------------------

class TestBufferControl:
    def test_hold_buffer(self, p):
        p.hold_buffer()
        assert p.build() == bytes([0x1B, 0x37])

    def test_release_buffer(self, p):
        p.release_buffer()
        assert p.build() == bytes([0x10, 0x05, 0x31])

    def test_cancel_buffer(self, p):
        p.cancel_buffer()
        assert p.build() == bytes([0x10, 0x05, 0x32])

    def test_status_request(self, p):
        p.status_request()
        assert p.build() == bytes([0x1B, 0x00, 0x20, 0x00])

    def test_reset_line_count(self, p):
        p.reset_line_count()
        assert p.build() == bytes([0x1B, 0x36])

    def test_enable_line_count_on(self, p):
        p.enable_line_count(True)
        assert p.build() == bytes([0x1B, 0x38, 0x00])  # 0 = enabled (inverted)

    def test_enable_line_count_off(self, p):
        p.enable_line_count(False)
        assert p.build() == bytes([0x1B, 0x38, 0x01])


# ---------------------------------------------------------------------------
# Cut (calls flush internally)
# ---------------------------------------------------------------------------

class TestCut:
    def test_cut_flushes_buffer(self, p):
        p.text("before cut")
        p.cut()
        # Buffer should be empty after cut
        assert p.build() == b""

    def test_cut_output_contains_cut_command(self, p):
        # Intercept what send_raw receives
        captured = []
        p.send_raw = lambda d: captured.append(d) or len(d)
        p.cut()
        combined = b"".join(captured)
        assert bytes([0x1B, 0x6D]) in combined  # ESC m — partial cut

    def test_cut_with_feed_includes_feed(self, p):
        captured = []
        p.send_raw = lambda d: captured.append(d) or len(d)
        p.cut(feed_lines=2)
        combined = b"".join(captured)
        assert b" \n \n" in combined  # 2 feed lines

    def test_cut_returns_self(self, p):
        assert p.cut() is p


# ---------------------------------------------------------------------------
# Chaining
# ---------------------------------------------------------------------------

class TestChaining:
    def test_method_chaining_returns_self(self, p):
        result = (
            p
            .bold(True)
            .text("Sale")
            .bold(False)
            .feed(2)
        )
        assert result is p

    def test_chained_buffer_content(self, p):
        p.bold(True).text("Hi").bold(False)
        data = p.build()
        assert bytes([0x1B, 0x47, 0x01]) in data  # bold on
        assert b"Hi" in data
        assert bytes([0x1B, 0x47, 0x00]) in data  # bold off


# ---------------------------------------------------------------------------
# Barcode
# ---------------------------------------------------------------------------

class TestBarcode:
    def test_barcode_code39_contains_gs_k(self, p):
        from py_ibm_4610._constants import BC_CODE39
        p.barcode(BC_CODE39, "12345")
        data = p.build()
        assert bytes([0x1D, 0x6B, BC_CODE39]) in data

    def test_barcode_pdf417_uses_gs_p(self, p):
        p.barcode(BC_PDF417, "test")
        data = p.build()
        assert bytes([0x1D, 0x50]) in data

    def test_barcode_code128a_contains_length_byte(self, p):
        payload = "ABC"
        p.barcode(BC_CODE128A, payload)
        data = p.build()
        # The length byte (3) should appear before the payload
        assert bytes([len(payload)]) + b"ABC" in data


# ---------------------------------------------------------------------------
# Flash erase
# ---------------------------------------------------------------------------

class TestFlashErase:
    def test_erase_logos(self, p):
        p.erase_logos()
        assert p.build() == bytes([0x1B, 0x23, 0x01])

    def test_erase_flash_invalid_raises(self, p):
        with pytest.raises(ValueError):
            p.erase_flash(99)

    def test_erase_flash_dl_graphics(self, p):
        from py_ibm_4610._constants import FLASH_DL_GRAPHICS
        p.erase_flash(FLASH_DL_GRAPHICS)
        assert p.build() == bytes([0x1B, 0x23, 0x01])


# ---------------------------------------------------------------------------
# Statistics
# ---------------------------------------------------------------------------

class TestStatistics:
    def test_statistic_valid_key(self, p):
        key = next(iter(STATISTIC_SUBCMDS))  # first valid key
        p.statistic(key)
        data = p.build()
        assert data[:2] == bytes([0x1B, 0x51])

    def test_statistic_invalid_raises(self, p):
        with pytest.raises(ValueError):
            p.statistic("NoSuchStat")


# ---------------------------------------------------------------------------
# parse_stat / parse_stat_with_remainder
# ---------------------------------------------------------------------------

# Empirically captured from real IBM 4610 hardware (PaperCutCount query).
# Byte layout:
#   [0:3]   transport header  → type=0x01, len_lsb=0x06, len_msb=0x00
#   [3:11]  8-byte printer status block
#   [11:15] count (LE uint32) = 0x00000028 = 40
#   [15:19] remainder (LE uint32) = 0
#   [19]    response status = 0x80
#   [20]    subcommand echo = 0x81 (PaperCutCount)
_PAPER_CUT_RAW = (
    b'\x01\x06\x00'           # transport header
    b'\x00\x00\x01\x0f\x00'  # status bytes (part 1)
    b'\x19\x20\x06'           # status bytes (part 2)
    b'\x28\x00\x00\x00'       # count = 40 (LE uint32)
    b'\x00\x00\x00\x00'       # remainder = 0 (LE uint32)
    b'\x80'                   # response status
    b'\x81'                   # subcommand echo (PaperCutCount)
)  # 21 bytes total


class TestParseStatHardwareCapture:
    def test_parse_stat_returns_40(self):
        from py_ibm_4610 import parse_stat
        assert parse_stat(_PAPER_CUT_RAW) == 40

    def test_parse_stat_with_remainder_count(self):
        from py_ibm_4610 import parse_stat_with_remainder
        count, _ = parse_stat_with_remainder(_PAPER_CUT_RAW)
        assert count == 40

    def test_parse_stat_with_remainder_zero(self):
        from py_ibm_4610 import parse_stat_with_remainder
        _, remainder = parse_stat_with_remainder(_PAPER_CUT_RAW)
        assert remainder == 0

    def test_parse_stat_nonzero_remainder(self):
        from py_ibm_4610 import parse_stat_with_remainder
        raw = bytearray(_PAPER_CUT_RAW)
        raw[15] = 7   # remainder = 7
        count, remainder = parse_stat_with_remainder(bytes(raw))
        assert count == 40
        assert remainder == 7

    def test_parse_stat_too_short_raises(self):
        from py_ibm_4610 import parse_stat
        with pytest.raises(ValueError, match="too short"):
            parse_stat(b'\x01\x06\x00' * 4)  # 12 bytes < 15

    def test_parse_stat_with_remainder_too_short_raises(self):
        from py_ibm_4610 import parse_stat_with_remainder
        with pytest.raises(ValueError, match="too short"):
            parse_stat_with_remainder(b'\x00' * 18)  # 18 bytes < 19

    def test_parse_stat_subcommand_echo_not_validated(self):
        # parse_stat is intentionally permissive — echo byte is informational
        from py_ibm_4610 import parse_stat
        raw = bytearray(_PAPER_CUT_RAW)
        raw[20] = 0xFF  # wrong echo byte
        assert parse_stat(bytes(raw)) == 40  # still returns count

    def test_parse_stat_large_count(self):
        from py_ibm_4610 import parse_stat
        raw = bytearray(_PAPER_CUT_RAW)
        raw[11:15] = (999_999).to_bytes(4, "little")
        assert parse_stat(bytes(raw)) == 999_999


# ---------------------------------------------------------------------------
# read_stat stride-scan (multi-response packet handling)
# ---------------------------------------------------------------------------

# A stale ReceiptLineFeedCount (echo=0x84) response with count=7,
# prepended before the real PaperCutCount (echo=0x81) response.
_STALE_RESPONSE = (
    b'\x01\x06\x00'
    b'\x00\x00\x01\x0f\x00'
    b'\x19\x20\x06'
    b'\x07\x00\x00\x00'      # count = 7 (stale ReceiptLineFeedCount)
    b'\x00\x00\x00\x00'
    b'\x80'
    b'\x84'                   # echo = ReceiptLineFeedCount
)  # 21 bytes


class TestReadStatStrideScan:
    """read_stat must find the matching frame inside a multi-response packet."""

    def _make_stub(self, packets):
        """Return a _StubIBM4610 whose read_response() yields each packet in turn."""
        stub = _StubIBM4610()
        queue = list(reversed(packets))

        def _fake_read_response(size=0, timeout=2000):
            return queue.pop() if queue else b""

        stub.read_response = _fake_read_response
        return stub

    def test_single_frame_returned_directly(self):
        """A 21-byte packet with the right echo is returned as-is."""
        stub = self._make_stub([_PAPER_CUT_RAW])
        result = stub.read_stat("PaperCutCount", timeout=5000)
        assert result == _PAPER_CUT_RAW

    def test_stale_first_frame_correct_second(self):
        """A 42-byte packet (stale + current) returns the matching second frame."""
        combined = _STALE_RESPONSE + _PAPER_CUT_RAW   # 42 bytes
        stub = self._make_stub([combined])
        result = stub.read_stat("PaperCutCount", timeout=5000)
        assert result == _PAPER_CUT_RAW

    def test_stale_only_packet_then_correct_packet(self):
        """Two separate reads: first packet has no match, second does."""
        stub = self._make_stub([_STALE_RESPONSE, _PAPER_CUT_RAW])
        result = stub.read_stat("PaperCutCount", timeout=5000)
        assert result == _PAPER_CUT_RAW

    def test_no_match_returns_empty(self):
        """All packets have wrong echo → read_stat returns b'' on timeout."""
        stub = self._make_stub([_STALE_RESPONSE])
        result = stub.read_stat("PaperCutCount", timeout=1)
        assert result == b""

    def test_stride_scan_extracts_count(self):
        """parse_stat on the extracted frame gives the correct count."""
        from py_ibm_4610 import parse_stat
        combined = _STALE_RESPONSE + _PAPER_CUT_RAW
        stub = self._make_stub([combined])
        result = stub.read_stat("PaperCutCount", timeout=5000)
        assert parse_stat(result) == 40


# ---------------------------------------------------------------------------
# print_line / print_receipt
# ---------------------------------------------------------------------------

class TestHighLevel:
    def test_print_line_flushes(self, p):
        captured = []
        p.send_raw = lambda d: captured.append(d) or len(d)
        p.print_line("Hello")
        combined = b"".join(captured)
        assert b"Hello\r\n" in combined

    def test_print_receipt_contains_lines(self, p):
        captured = []
        p.send_raw = lambda d: captured.append(d) or len(d)
        p.print_receipt(["Line 1", "Line 2"], cut=False)
        combined = b"".join(captured)
        assert b"Line 1\r\n" in combined
        assert b"Line 2\r\n" in combined

    def test_print_receipt_with_cut(self, p):
        captured = []
        p.send_raw = lambda d: captured.append(d) or len(d)
        p.print_receipt(["Receipt"], cut=True)
        combined = b"".join(captured)
        assert bytes([0x1B, 0x6D]) in combined  # ESC m — cut command
