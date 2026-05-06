"""_transport.py — IBM 4610 HID report framing.

Reverse-engineered from ``PrinterPacket$Transport.transport()`` in
the official posj.jar JavaPOS driver.
"""

from __future__ import annotations

from ._constants import REPORT_SIZE


def make_packet(data: bytes, report_size: int = REPORT_SIZE) -> bytes:
    """Wrap *data* in the IBM 4610 HID output report frame.

    The frame is always exactly *report_size* bytes (default 1022), zero-padded.

    Frame layout::

        total_len = 7 + len(data)
        len_field = total_len - 3
        cmd       = 0x01 if len_field >= 255 else 0x02
        header    = [cmd,
                     len_field & 0xFF, (len_field >> 8) & 0xFF,
                     0x01, 0x00, 0x00, 0x00]
        packet    = header + data, zero-padded to report_size bytes

    Args:
        data:        ESC/POS payload to wrap (up to report_size - 7 bytes).
        report_size: total report size in bytes (default 1022).

    Returns:
        Exactly *report_size* bytes ready for ``ctrl_transfer`` SET_REPORT.
    """
    N = len(data)
    len_field = (7 + N) - 3
    cmd = 0x01 if len_field >= 255 else 0x02
    header = bytes([
        cmd,
        len_field & 0xFF,
        (len_field >> 8) & 0xFF,
        0x01,
        0x00,
        0x00,
        0x00,
    ])
    return (header + data).ljust(report_size, b'\x00')


#: Maximum ESC/POS payload bytes per HID packet (report_size - 7 header bytes).
MAX_PAYLOAD: int = REPORT_SIZE - 7  # 1015
