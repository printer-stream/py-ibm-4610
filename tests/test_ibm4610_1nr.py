"""Tests for IBM4610_1NR — receipt-only model restrictions (no hardware required)."""

import pytest

from py_ibm_4610 import IBM4610_1NR
from py_ibm_4610._constants import STATION_RECEIPT, STATION_SLIP, STATION_LABEL


class _StubIBM4610_1NR(IBM4610_1NR):
    """USB-less 1NR for offline tests."""

    def open(self) -> "_StubIBM4610_1NR":
        # Skip USB; call select_station directly like the real open() does
        self.select_station(STATION_RECEIPT)
        return self

    def close(self) -> None:
        pass

    def send_raw(self, data: bytes) -> int:
        return len(data)


@pytest.fixture
def p() -> _StubIBM4610_1NR:
    return _StubIBM4610_1NR()


class TestStationRestriction:
    def test_receipt_station_is_allowed(self, p):
        # Should not raise
        p.select_station(STATION_RECEIPT)

    def test_slip_station_raises(self, p):
        with pytest.raises(NotImplementedError):
            p.select_station(STATION_SLIP)

    def test_label_station_raises(self, p):
        with pytest.raises(NotImplementedError):
            p.select_station(STATION_LABEL)

    def test_unknown_station_raises(self, p):
        with pytest.raises(NotImplementedError):
            p.select_station(0xFF)


class TestSlipMethodsDisabled:
    """All slip / document-insertion methods must raise NotImplementedError."""

    def test_eject_slip(self, p):
        with pytest.raises(NotImplementedError):
            p.eject_slip()

    def test_open_jaws(self, p):
        with pytest.raises(NotImplementedError):
            p.open_jaws()

    def test_begin_insertion(self, p):
        with pytest.raises(NotImplementedError):
            p.begin_insertion()

    def test_end_insertion(self, p):
        with pytest.raises(NotImplementedError):
            p.end_insertion()

    def test_register_document(self, p):
        with pytest.raises(NotImplementedError):
            p.register_document()

    def test_end_register_document(self, p):
        with pytest.raises(NotImplementedError):
            p.end_register_document()

    def test_flip_check(self, p):
        with pytest.raises(NotImplementedError):
            p.flip_check()

    def test_change_print_side(self, p):
        with pytest.raises(NotImplementedError):
            p.change_print_side()

    def test_set_chase_mode(self, p):
        with pytest.raises(NotImplementedError):
            p.set_chase_mode()

    def test_unidirectional(self, p):
        with pytest.raises(NotImplementedError):
            p.unidirectional(True)


class TestReceiptCommandsWork:
    """Inherited receipt commands must still function on 1NR."""

    def test_bold_produces_bytes(self, p):
        p.bold(True)
        assert p.build() == bytes([0x1B, 0x47, 0x01])

    def test_text_produces_bytes(self, p):
        p.text("1NR")
        assert p.build() == b"1NR"

    def test_feed_produces_bytes(self, p):
        p.feed(2)
        assert p.build() == b" \n \n"

    def test_cut_flushes(self, p):
        p.cut()
        assert p.build() == b""

    def test_chaining_works(self, p):
        result = p.bold(True).text("OK").bold(False)
        assert result is p
