"""printer/base.py — Abstract base class for IBM 4610 printer drivers.

Provides buffer management and a uniform interface for all concrete
transport implementations (USB HID, serial, network, etc.).
"""

from __future__ import annotations

import logging
from abc import ABC, abstractmethod

_log = logging.getLogger(__name__)


class BasePrinter(ABC):
    """Transport-agnostic base for IBM 4610 printers.

    Subclasses must implement :meth:`open`, :meth:`close`, and
    :meth:`send_raw`.  All command methods accumulate ESC/POS bytes via
    :meth:`write`; call :meth:`flush` to dispatch the buffer.

    All chainable command methods return *self*.
    """

    #: Maximum ESC/POS bytes per single ``send_raw`` call.
    #: Subclasses should override this to match their MTU.
    _MAX_PAYLOAD: int = 1015

    def __init__(self) -> None:
        self._buf: bytearray = bytearray()

    def __repr__(self) -> str:
        return f"<{self.__class__.__name__} buf={len(self._buf)}B>"

    # ------------------------------------------------------------------
    # Context manager
    # ------------------------------------------------------------------

    def __enter__(self) -> "BasePrinter":
        return self.open()

    def __exit__(self, *_) -> None:
        self.close()

    # ------------------------------------------------------------------
    # Abstract transport interface
    # ------------------------------------------------------------------

    @abstractmethod
    def open(self) -> "BasePrinter":
        """Open the connection to the printer and return *self*."""

    @abstractmethod
    def close(self) -> None:
        """Release the connection to the printer."""

    @abstractmethod
    def send_raw(self, data: bytes) -> int:
        """Transmit *data* to the printer immediately.

        Returns the number of bytes transferred.  *data* may be empty
        (some transports use an empty send as a keep-alive or flush signal).
        """

    # ------------------------------------------------------------------
    # Buffer management
    # ------------------------------------------------------------------

    def write(self, data: bytes) -> "BasePrinter":
        """Append raw *data* to the internal send buffer.

        Nothing is transmitted until :meth:`flush` is called.
        Returns *self* for method chaining.
        """
        self._buf.extend(data)
        _log.debug("write: %d bytes (buffer total %d bytes)", len(data), len(self._buf))
        return self

    def flush(self) -> int:
        """Chunk and send all buffered bytes, then clear the buffer.

        Data is split into :attr:`_MAX_PAYLOAD`-byte pieces and each
        piece is passed to :meth:`send_raw`.  If the buffer is empty a
        single empty :meth:`send_raw` call is still made (required by
        some transports to signal end-of-data).

        Returns total bytes transferred.
        """
        data = bytes(self._buf)
        self._buf.clear()
        chunks = max(1, -(-len(data) // self._MAX_PAYLOAD)) if data else 1
        _log.debug("flush: %d bytes → %d chunk(s)", len(data), chunks)
        total = 0
        for offset in range(0, max(len(data), 1), self._MAX_PAYLOAD):
            total += self.send_raw(data[offset: offset + self._MAX_PAYLOAD])
        return total

    def build(self) -> bytes:
        """Return all buffered bytes and clear the buffer *without* sending.

        Useful for unit-testing command sequences or deferring dispatch.
        """
        data = bytes(self._buf)
        self._buf.clear()
        _log.debug("build: returning %d buffered bytes", len(data))
        return data
