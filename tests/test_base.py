"""Tests for printer/base.py — BasePrinter buffer management (no hardware required)."""

import pytest

from py_ibm_4610.printer.base import BasePrinter


class _StubPrinter(BasePrinter):
    """Minimal concrete implementation for testing BasePrinter in isolation."""

    def __init__(self, max_payload: int = BasePrinter._MAX_PAYLOAD):
        super().__init__()
        self._MAX_PAYLOAD = max_payload
        self.sent: list[bytes] = []

    def open(self) -> "_StubPrinter":
        return self

    def close(self) -> None:
        pass

    def send_raw(self, data: bytes) -> int:
        self.sent.append(data)
        return len(data)


class TestWrite:
    def test_write_returns_self(self):
        p = _StubPrinter()
        assert p.write(b"abc") is p

    def test_write_accumulates(self):
        p = _StubPrinter()
        p.write(b"Hello").write(b", ").write(b"World")
        assert bytes(p._buf) == b"Hello, World"

    def test_write_empty_bytes(self):
        p = _StubPrinter()
        p.write(b"")
        assert bytes(p._buf) == b""


class TestBuild:
    def test_build_returns_buffered_bytes(self):
        p = _StubPrinter()
        p.write(b"\x1B\x47\x01")
        assert p.build() == b"\x1B\x47\x01"

    def test_build_clears_buffer(self):
        p = _StubPrinter()
        p.write(b"data")
        p.build()
        assert bytes(p._buf) == b""

    def test_build_empty_returns_empty(self):
        p = _StubPrinter()
        assert p.build() == b""


class TestFlush:
    def test_flush_sends_data(self):
        p = _StubPrinter()
        p.write(b"test")
        p.flush()
        assert b"test" in p.sent

    def test_flush_clears_buffer(self):
        p = _StubPrinter()
        p.write(b"data")
        p.flush()
        assert bytes(p._buf) == b""

    def test_flush_empty_still_calls_send_raw(self):
        # Empty flush must still call send_raw once (end-of-data signal)
        p = _StubPrinter()
        p.flush()
        assert len(p.sent) == 1
        assert p.sent[0] == b""

    def test_flush_returns_bytes_transferred(self):
        p = _StubPrinter()
        p.write(b"hello")
        total = p.flush()
        assert total == 5

    def test_flush_splits_into_chunks(self):
        # Use a tiny max_payload so multiple chunks are needed
        p = _StubPrinter(max_payload=4)
        p.write(b"ABCDEFGH")  # 8 bytes → 2 chunks of 4
        p.flush()
        assert len(p.sent) == 2
        assert p.sent[0] == b"ABCD"
        assert p.sent[1] == b"EFGH"

    def test_flush_single_chunk_for_small_data(self):
        p = _StubPrinter()
        p.write(b"small")
        p.flush()
        assert len(p.sent) == 1


class TestContextManager:
    def test_context_manager_calls_open_close(self):
        class _TrackingPrinter(_StubPrinter):
            def __init__(self):
                super().__init__()
                self.opened = False
                self.closed = False

            def open(self):
                self.opened = True
                return self

            def close(self):
                self.closed = True

        p = _TrackingPrinter()
        with p:
            assert p.opened
        assert p.closed

    def test_context_manager_returns_self(self):
        p = _StubPrinter()
        with p as q:
            assert q is p
