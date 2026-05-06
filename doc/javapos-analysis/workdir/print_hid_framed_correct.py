import usb.core
import usb.util
import struct

dev = usb.core.find(idVendor=0x04b3, idProduct=0x4535)
if dev is None:
    raise RuntimeError("Printer not found")

IFACE = 1         # interface 1: report ID=0x01, size=1022 bytes
REPORT_SIZE = 1022

if dev.is_kernel_driver_active(IFACE):
    dev.detach_kernel_driver(IFACE)
usb.util.claim_interface(dev, IFACE)

def make_packet(data: bytes) -> bytes:
    """Build IBM 4610 HID packet exactly as JavaPOS does it."""
    N = len(data)
    total_len = 7 + N        # header(7) + data
    len_field = total_len - 3  # as per transport(): iinc 2, -3

    cmd = 0x01 if len_field >= 255 else 0x02

    header = bytes([
        cmd,
        len_field & 0xFF,
        (len_field >> 8) & 0xFF,
        0x01,   # transfer[3] — unchanged
        0x00,   # transfer[4]
        0x00,   # transfer[5]
        0x00,   # transfer[6]
    ])
    payload = header + data
    return payload.ljust(REPORT_SIZE, b'\x00')

try:
    # Compatibility Legacy mode: ESC B = begin doc, ESC E = end doc
    data = (
        b'\x1bB'           # ESC B — begin document / select receipt station
        + b'Hello World\r\n'
        + b'Line 2\r\n'
        + b'\r\n' * 4      # feed
        + b'\x1bE'         # ESC E — end document (triggers print)
    )

    payload = make_packet(data)
    print(f"Sending {len(payload)}-byte HID report")
    print(f"Header: {payload[:7].hex()}")
    print(f"Data:   {payload[7:7+len(data)].hex()}")

    n = dev.ctrl_transfer(
        bmRequestType=0x21, bRequest=0x09,
        wValue=0x0201,   # Output report, ID=1
        wIndex=IFACE,
        data_or_wLength=payload,
        timeout=5000
    )
    print(f"Sent {n} bytes")

finally:
    usb.util.release_interface(dev, IFACE)
    dev.attach_kernel_driver(IFACE)

