"""Tests for _transport.py — HID report framing (no hardware required)."""

import pytest

from py_ibm_4610._transport import make_packet, MAX_PAYLOAD
from py_ibm_4610._constants import REPORT_SIZE


class TestMakePacket:
    """make_packet() wraps ESC/POS data in the IBM 4610 HID frame."""

    def test_packet_is_always_report_size(self):
        pkt = make_packet(b"Hello")
        assert len(pkt) == REPORT_SIZE

    def test_custom_report_size(self):
        pkt = make_packet(b"Hi", report_size=64)
        assert len(pkt) == 64

    def test_empty_payload_length(self):
        pkt = make_packet(b"")
        assert len(pkt) == REPORT_SIZE

    def test_zero_padding(self):
        # bytes after header+data must be zero
        data = b"\xAA\xBB"
        pkt = make_packet(data)
        assert pkt[9:] == bytes(REPORT_SIZE - 9)

    def test_header_fields_short_payload(self):
        # len_field = 7 + N - 3 = 4 + N; for N=1 → len_field=5 (<255) → cmd=0x02
        data = b"\xFF"
        pkt = make_packet(data)
        N = 1
        len_field = 7 + N - 3  # = 5
        assert len_field < 255
        assert pkt[0] == 0x02          # cmd byte
        assert pkt[1] == len_field & 0xFF
        assert pkt[2] == (len_field >> 8) & 0xFF
        assert pkt[3] == 0x01          # fixed
        assert pkt[4] == 0x00
        assert pkt[5] == 0x00
        assert pkt[6] == 0x00

    def test_cmd_byte_is_0x01_for_large_payload(self):
        # len_field >= 255 → cmd = 0x01
        # need N such that 7 + N - 3 >= 255 → N >= 251
        data = bytes(251)
        pkt = make_packet(data)
        len_field = 7 + 251 - 3  # = 255
        assert len_field >= 255
        assert pkt[0] == 0x01

    def test_cmd_byte_is_0x02_for_small_payload(self):
        data = bytes(250)
        pkt = make_packet(data)
        len_field = 7 + 250 - 3  # = 254
        assert len_field < 255
        assert pkt[0] == 0x02

    def test_data_is_present_after_header(self):
        data = b"\x1B\x47\x01"  # bold on
        pkt = make_packet(data)
        assert pkt[7:10] == data

    def test_max_payload_constant(self):
        assert MAX_PAYLOAD == REPORT_SIZE - 7
        assert MAX_PAYLOAD == 1015

    def test_full_payload_no_truncation(self):
        data = bytes(range(256)) * 3 + bytes(range(MAX_PAYLOAD % 256))
        data = data[:MAX_PAYLOAD]
        assert len(data) == MAX_PAYLOAD
        pkt = make_packet(data)
        assert len(pkt) == REPORT_SIZE
        assert pkt[7:7 + MAX_PAYLOAD] == data
