"""printer/ibm4610_1nr.py — IBM 4610 model 1NR (receipt-only) driver.

The 1NR is a single-station thermal receipt printer.  It shares the
USB HID interface with the full 4610 but has no slip/document station,
no MICR reader, and no check scanner.

Any attempt to use slip, document-insertion, MICR, or check-scanner
commands raises ``NotImplementedError`` at the Python level, preventing
silent mis-use of hardware that does not exist on this model.
"""

from __future__ import annotations

from .ibm4610 import IBM4610
from .._constants import STATION_RECEIPT, VENDOR, PRODUCT, IFACE, REPORT_SIZE


class IBM4610_1NR(IBM4610):
    """IBM 4610 model 1NR — single-station thermal receipt printer.

    The 1NR has one print mechanism: the thermal receipt roll.
    Slip, document-insertion, MICR, and check-scanner features are
    **not available** on this model.

    All receipt-station commands from :class:`IBM4610` are fully supported.

    Example::

        with IBM4610_1NR() as p:
            p.select_station(STATION_RECEIPT)
            p.bold(True).text("Hello from 1NR!\\n").bold(False)
            p.feed(4).cut()
    """

    def __init__(
        self,
        vendor:      int = VENDOR,
        product:     int = PRODUCT,
        iface:       int = IFACE,
        report_size: int = REPORT_SIZE,
    ) -> None:
        super().__init__(
            vendor=vendor,
            product=product,
            iface=iface,
            report_size=report_size,
        )

    # ------------------------------------------------------------------
    # Station restriction
    # ------------------------------------------------------------------

    def select_station(self, station: int) -> "IBM4610_1NR":
        """Select the print station.

        The 1NR only has a receipt station (``STATION_RECEIPT``).

        Raises:
            NotImplementedError: if *station* is not ``STATION_RECEIPT``.
        """
        if station != STATION_RECEIPT:
            raise NotImplementedError(
                f"IBM 4610 1NR does not have station {station:#x}. "
                "Only STATION_RECEIPT (0x02) is available on this model."
            )
        return super().select_station(station)

    # ------------------------------------------------------------------
    # Slip / document insertion  — not available on 1NR
    # ------------------------------------------------------------------

    def eject_slip(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no slip station.")

    def open_jaws(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no document insertion jaws.")

    def begin_insertion(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no document insertion jaws.")

    def end_insertion(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no document insertion jaws.")

    def register_document(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no document station.")

    def end_register_document(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no document station.")

    def flip_check(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no flip / check mechanism.")

    def change_print_side(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no flip / check mechanism.")

    def set_chase_mode(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no slip station.")

    def unidirectional(self, on: bool) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no impact slip station.")

    # ------------------------------------------------------------------
    # MICR / check scanner  — not available on 1NR
    # ------------------------------------------------------------------

    def micr_read(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no MICR reader.")

    def start_scan(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no check scanner.")

    def print_scanned_image(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no check scanner.")

    def retrieve_image(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no check scanner.")

    def store_scanned_image(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no check scanner.")

    def get_next_image_location(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no check scanner.")

    def get_first_unread_image_location(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no check scanner.")

    def scanner_calibrate(self) -> "IBM4610_1NR":
        raise NotImplementedError("IBM 4610 1NR has no check scanner.")
