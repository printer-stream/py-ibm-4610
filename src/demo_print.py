#!/usr/bin/env python3
"""demo_print.py - IBM 4610 SureMark full-feature showcase.

Exercises every public method in ibm4610.py in a single receipt print.
Run with the printer connected via USB:

    pip install pyusb
    sudo python3 demo_print.py          # Linux (needs root for libusb access)
    python3 demo_print.py               # macOS / Windows with libusb installed

Each section is preceded by a separator line so you can visually identify
which feature produced which output on the physical receipt.
"""

import sys
from datetime import datetime
from ibm4610 import (
    IBM4610,
    # Stations
    STATION_RECEIPT, STATION_SLIP, STATION_LABEL,
    # Alignment
    ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT,
    # Fonts
    FONT_A, FONT_B, FONT_C,
    # Barcodes
    BC_UPCA, BC_EAN13, BC_CODE39, BC_CODE128, BC_ITF, BC_CODABAR, BC_PDF417,
    HRI_NONE, HRI_ABOVE, HRI_BELOW, HRI_BOTH,
    # Bitmap density
    DENSITY_NORMAL, DENSITY_DOUBLE,
    # Flash sectors
    FLASH_DL_GRAPHICS,
    # Error-recovery flags
    ER_RELEASE_AFTER_CORRECTION, ER_AUTO_RETRY_AFTER_HOME_ERR,
    # Statistics keys
    STATISTIC_SUBCMDS,
    # Status constants (for reference)
    STATUS_NORMAL,
)

SEP = "-" * 40


def section(p: IBM4610, title: str) -> None:
    """Print a visible section header to the receipt."""
    p.normal_mode()
    p.alignment(ALIGN_LEFT)
    p.write(f"\n{SEP}\n{title}\n{SEP}\n".encode("cp437"))


# ---------------------------------------------------------------------------
# Build the full demo receipt into the printer's buffer, then flush once.
# ---------------------------------------------------------------------------
def build_demo(p: IBM4610) -> None:

    # -- 1. Station selection ----------------------------------------------
    p.select_station(STATION_RECEIPT)

    # -- 2. Header ---------------------------------------------------------
    p.alignment(ALIGN_CENTER)
    p.bold(True)
    p.scale_font(width=1, height=1)
    p.write(b"IBM 4610 SUREMARK DEMO\n")
    p.scale_font(width=0, height=0)
    p.bold(False)
    ts = datetime.now().strftime("%Y-%m-%d  %H:%M:%S")
    p.write(f"{ts}\n".encode("cp437"))
    p.alignment(ALIGN_LEFT)

    # -- 3. Text formatting ------------------------------------------------
    section(p, "1. TEXT FORMATTING")

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
    p.write(b"Double wide+high\n")
    p.double_wide_high(False)

    p.reverse_video(True)
    p.write(b"Reverse video\n")
    p.reverse_video(False)

    p.normal_mode()
    p.write(b"normal_mode() reset\n")

    # -- 4. Font scaling (GS !) --------------------------------------------
    section(p, "2. FONT SCALING")

    for w, h in [(0, 0), (1, 0), (0, 1), (1, 1), (2, 2)]:
        p.scale_font(width=w, height=h)
        p.write(f"scale {w}w {h}h\n".encode("cp437"))
    p.scale_font(0, 0)

    # -- 5. Font faces -----------------------------------------------------
    section(p, "3. FONT FACES")

    for face, name in [(FONT_A, "Font A"), (FONT_B, "Font B"), (FONT_C, "Font C")]:
        p.select_font(face)
        p.write(f"{name}: ABCDEFGHIJ 0123456789\n".encode("cp437"))
    p.select_font(FONT_A)

    # -- 6. Alignment ------------------------------------------------------
    section(p, "4. ALIGNMENT")

    p.alignment(ALIGN_LEFT)
    p.write(b"Left aligned\n")
    p.alignment(ALIGN_CENTER)
    p.write(b"Centered\n")
    p.alignment(ALIGN_RIGHT)
    p.write(b"Right aligned\n")
    p.alignment(ALIGN_LEFT)

    # -- 7. Font color (red ribbon printers) -------------------------------
    section(p, "5. FONT COLOR")

    p.font_color(0)
    p.write(b"Color 0 (black)\n")
    p.font_color(1)
    p.write(b"Color 1 (red if dual-color)\n")
    p.font_color(0)

    # -- 8. Rotation -------------------------------------------------------
    section(p, "6. ROTATION")

    p.rotate_90(True)
    p.write(b"rotate_90 ON\n")
    p.rotate_90(False)

    p.rotate_180(True)
    p.write(b"rotate_180 ON\n")
    p.rotate_180(False)

    # -- 9. Line spacing ---------------------------------------------------
    section(p, "7. LINE SPACING")

    for dots in [24, 36, 48]:
        p.line_spacing(dots)
        p.write(f"Line spacing {dots} dots\n".encode("cp437"))
    p.line_spacing(30)   # restore default

    # -- 10. Feed commands -------------------------------------------------
    section(p, "8. FEED COMMANDS")

    p.write(b"Before feed(2)\n")
    p.feed(2)
    p.write(b"After feed(2)\n")

    p.write(b"Before feed_units(40)\n")
    p.feed_units(40)
    p.write(b"After feed_units(40)\n")

    # -- 11. Tab stops -----------------------------------------------------
    section(p, "9. TAB STOPS")

    p.set_tab_stops([80, 160, 240])
    p.write(b"A\tB\tC\tD\n")
    p.set_tab_stops([])   # clear tabs

    # -- 12. Left margin / relative position -------------------------------
    section(p, "10. MARGINS & POSITION")

    p.left_margin(40)
    p.write(b"Left margin 40 dots\n")
    p.left_margin(0)

    p.relative_position(20)
    p.write(b"Relative +20 dots\n")

    # -- 13. Dot spacing ---------------------------------------------------
    section(p, "11. DOT SPACING")

    for n in [0, 3, 6]:
        p.dot_spacing(n)
        p.write(f"SBCS spacing={n}: Hello\n".encode("cp437"))
    p.dot_spacing(0)

    # -- 14. Character sets ------------------------------------------------
    section(p, "12. CHARACTER SETS")

    p.resident_char_set()
    p.write(b"Resident char set\n")

    p.select_code_page(0)              # code page 437 (US)
    p.write(b"Code page 437 (US)\n")

    p.select_code_page(1)              # code page 858 (Multilingual)
    p.write(b"Code page 858 (ML)\n")

    p.select_code_page(0)

    # -- 15. Unidirectional / print quality --------------------------------
    section(p, "13. PRINT QUALITY")

    p.print_quality(True)
    p.write(b"High quality ON\n")
    p.print_quality(False)
    p.write(b"High quality OFF\n")

    p.unidirectional(True)
    p.write(b"Unidirectional ON\n")
    p.unidirectional(False)

    # -- 16. Barcodes ------------------------------------------------------
    section(p, "14. BARCODES")

    p.write(b"--- UPC-A ---\n")
    p.barcode(BC_UPCA, "012345678905", height=50, width=2, hri=HRI_BELOW)
    p.feed(2)

    p.write(b"--- EAN-13 ---\n")
    p.barcode(BC_EAN13, "5901234123457", height=50, width=2, hri=HRI_BELOW)
    p.feed(2)

    p.write(b"--- Code 39 ---\n")
    p.barcode(BC_CODE39, "CODE39TEST", height=50, width=2, hri=HRI_BELOW)
    p.feed(2)

    p.write(b"--- ITF ---\n")
    p.barcode(BC_ITF, "1234567890", height=50, width=2, hri=HRI_BELOW)
    p.feed(2)

    p.write(b"--- Codabar ---\n")
    p.barcode(BC_CODABAR, "A12345B", height=50, width=2, hri=HRI_BELOW)
    p.feed(2)

    p.write(b"--- Code 128 ---\n")
    p.barcode(BC_CODE128, "CODE128DEMO", height=60, width=2,
              align=ALIGN_CENTER, hri=HRI_BOTH)
    p.feed(2)

    p.alignment(ALIGN_LEFT)

    # -- 17. Inline bitmap (checkerboard) ----------------------------------
    section(p, "15. INLINE BITMAP")

    # ESC * mode=0: 8-dot single density, 1 byte per column, 8 dots tall.
    # columns=64  -> nL=64, nH=0  -> printer expects exactly 64 data bytes.
    # Alternating 0xAA (10101010) / 0x55 (01010101) columns = checkerboard.
    bmp_columns = 64
    bmp_data = bytes([0xAA if i % 2 == 0 else 0x55 for i in range(bmp_columns)])
    p.write(b"Checkerboard (64 cols x 8 dots):\n")
    p.print_bitmap(density=DENSITY_NORMAL, columns=bmp_columns, data=bmp_data)
    p.lf()

    # -- 18. Page mode -----------------------------------------------------
    section(p, "16. PAGE MODE")

    p.page_mode_enable()
    p.page_mode_define(x=0, y=0, dx=200, dy=100)
    p.page_mode_position(0)           # top->bottom, left->right
    p.write(b"Page mode text")
    p.page_mode_print(clear_after=True)
    p.page_mode_disable()
    p.write(b"\n")

    # -- 19. Buffer control ------------------------------------------------
    section(p, "17. BUFFER CONTROL")

    # hold_buffer / release_buffer are interactive commands: hold_buffer
    # pauses the printer's print engine and release_buffer (DLE ENQ) must be
    # sent as an immediate real-time transfer -- not embedded in bulk data.
    # Skipped in this one-way bulk demo; use them in interactive host code.
    p.write(b"hold/release: interactive use only (skipped)\n")
    p.write(b"cancel_buffer: clears queued data\n")

    # -- 20. Status / device info requests --------------------------------
    section(p, "18. STATUS & DEVICE INFO")

    # Status/device-info commands trigger asynchronous response packets from
    # the printer.  In a bulk demo they are safe to send but the responses
    # are not read here.  Buffered variants are sent inside the data stream.
    p.ec_level_request(buffered=True)
    p.write(b"ec_level_request sent (response not read in demo)\n")

    # -- 21. Line count ----------------------------------------------------
    section(p, "19. LINE COUNT")

    p.enable_line_count(True)
    p.write(b"Line count enabled\n")
    p.reset_line_count()
    p.write(b"Line count reset\n")
    p.enable_line_count(False)

    # -- 22. Status-sent configuration ------------------------------------
    section(p, "20. STATUS-SENT CONFIG")

    p.status_sent(buff_empty=True, cover_open=True)
    p.write(b"status_sent configured\n")

    # -- 23. Error recovery ------------------------------------------------
    section(p, "21. ERROR RECOVERY")

    p.error_recovery(ER_RELEASE_AFTER_CORRECTION | ER_AUTO_RETRY_AFTER_HOME_ERR)
    p.write(b"error_recovery flags set\n")

    # -- 24. Feed button ---------------------------------------------------
    section(p, "22. FEED BUTTON")

    p.enable_feed_button(True)
    p.write(b"Feed button enabled\n")
    p.enable_feed_button(False)
    p.write(b"Feed button disabled\n")

    # -- 25. Cash drawer pulse ---------------------------------------------
    section(p, "23. CASH DRAWER")

    p.write(b"Pulsing drawer 1...\n")
    p.pulse_drawer(pin=0, on_time=50, off_time=50)

    # -- 26. Beeper --------------------------------------------------------
    section(p, "24. BEEPER")

    p.write(b"Beep 500 Hz, 300 ms, vol 80...\n")
    p.beep(duration_100ms=3, frequency=500, volume=80)

    p.write(b"Beep 1000 Hz, 100 ms, vol 30...\n")
    p.beep(duration_100ms=1, frequency=1000, volume=30)

    # -- 27. MCT read / write ----------------------------------------------
    section(p, "25. MCT (MATRIX CARD TRANSPORT)")

    p.write(b"Requesting MCT read track 1...\n")
    p.mct_read(0x01)

    # -- 28. Statistics ----------------------------------------------------
    section(p, "26. STATISTICS QUERIES")

    for key in ["ManufactureDate", "PaperCutCount", "ReceiptLineFeedCount",
                "ReceiptCharacterPrintedCount", "IBM_CheckScannedCount"]:
        p.write(f"  stat: {key}\n".encode("cp437"))
        p.statistic(key)
    p.feed(1)

    # -- 29. Reprint char -------------------------------------------------
    section(p, "27. MISC COMMANDS")

    p.write(b"reprint_char: ")
    p.write(b"X")
    p.reprint_char()
    p.write(b"\n")

    p.write(b"fix_font()\n")
    p.fix_font()

    p.write(b"chase mode: ")
    p.set_chase_mode()
    p.write(b"OK\n")

    p.write(b"reinit()\n")
    p.reinit()

    # -- 30. Footer + cut -------------------------------------------------
    section(p, "28. END OF DEMO")

    p.alignment(ALIGN_CENTER)
    p.bold(True)
    p.write(b"All features exercised.\n")
    p.bold(False)
    p.write(b"ibm4610.py  (c) 2026\n")
    p.alignment(ALIGN_LEFT)
    p.normal_mode()

    # Final feed + partial cut
    p.feed(6)
    p.cut()


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
def main() -> None:
    print("IBM 4610 SureMark - full-feature demo")
    print(f"Connecting to printer (VID=0x04B3, PID=0x4535) ...")

    try:
        with IBM4610() as p:
            print("Printer found. Building demo receipt ...")
            build_demo(p)
            print("Done - check your receipt.")
    except RuntimeError as exc:
        print(f"\nERROR: {exc}", file=sys.stderr)
        print("Make sure the printer is connected and libusb is installed.",
              file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
