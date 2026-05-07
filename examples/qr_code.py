#!/usr/bin/env python3
"""qr_code.py — QR code printing demo for the IBM 4610 SureMark.

Demonstrates every QR code option exposed by qr_code():
  - Error correction levels  (L / M / Q / H)
  - Encoding modes           (Byte / Alpha-Numeric / Numeric / ECI / Mixing)
  - Alignment                (left / center / right)
  - Long payload             (approaches the hardware limit)

Receipt station only.  Requires firmware 0F.xx or above (models 1NR, 2NR, 2CR).

Run with the printer connected over USB:

    python examples/qr_code.py
"""

import logging
from datetime import datetime

from py_ibm_4610 import IBM4610_1NR
from py_ibm_4610 import version
from py_ibm_4610 import (
    ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT,
    QR_MODE_BYTE, QR_MODE_ECI,
    QR_EC_L, QR_EC_M, QR_EC_Q, QR_EC_H,
)

logging.basicConfig(level=logging.WARNING)

WIDTH = 42


def sep(char: str = "-", n: int = WIDTH) -> str:
    return char * n


def header(p, title: str) -> None:
    """Print a centred section header between two separator lines."""
    p.text(sep() + "\n")
    p.alignment(ALIGN_CENTER)
    p.bold(True).text(title + "\n").bold(False)
    p.alignment(ALIGN_LEFT)
    p.text(sep() + "\n")


def label(p, text: str) -> None:
    """Print a small descriptive label above a QR code."""
    p.text(text + "\n")


with IBM4610_1NR() as p:
    # ------------------------------------------------------------------
    # Title
    # ------------------------------------------------------------------
    p.alignment(ALIGN_CENTER)
    p.bold(True).double_wide(True).text("QR CODE\n").double_wide(False).bold(False)
    p.text("IBM 4610 SureMark demo\n")
    p.text(f"{datetime.now().strftime('%Y-%m-%d  %H:%M:%S')} v{version}\n")
    p.alignment(ALIGN_LEFT)

    # ==================================================================
    # 1. Error correction levels
    #    Same payload, four levels: L (7%) → M (15%) → Q (25%) → H (30%)
    #    Higher level = larger symbol, better damage tolerance.
    # ==================================================================
    header(p, "1. ERROR CORRECTION LEVELS")
    p.text("Same URL, four EC levels.\n")
    p.text("Higher level = larger symbol.\n\n")

    label(p, "L  ~7% recovery (smallest)")
    p.qr_code("https://github.com/printer-stream/py-ibm-4610", ec=QR_EC_L)
    p.lf()

    label(p, "M  ~15% recovery (default)")
    p.qr_code("https://github.com/printer-stream/py-ibm-4610", ec=QR_EC_M)
    p.lf()

    label(p, "Q  ~25% recovery")
    p.qr_code("https://github.com/printer-stream/py-ibm-4610", ec=QR_EC_Q)
    p.lf()

    label(p, "H  ~30% recovery (largest)")
    p.qr_code("https://github.com/printer-stream/py-ibm-4610", ec=QR_EC_H)
    p.lf()

    # ==================================================================
    # 2. Encoding modes
    #    Different modes encode different character sets more efficiently.
    # ==================================================================
    header(p, "2. ENCODING MODES")

    # -- Byte mode (default) ------------------------------------------
    # Any 8-bit data, including lower-case and ASCII symbols.
    # QR_MODE_NUMERIC, QR_MODE_ALPHANUM, QR_MODE_KANJI, and QR_MODE_MIXING
    # are defined but NOT supported by the 1NR hardware — using them emits a
    # UserWarning and produces no valid QR symbol on the printer.
    label(p, "Byte  (full ASCII, default mode)")
    p.qr_code("Hello, World! py-ibm-4610", mode=QR_MODE_BYTE, ec=QR_EC_M)
    p.lf()

    # -- ECI mode (UTF-8, ECI value = 26) ------------------------------
    # ECI tells the decoder which character set to use.
    # Value 26 = UTF-8; allows Unicode/emoji content.
    label(p, "ECI  UTF-8 (ECI=26) - Unicode text")
    p.qr_code(
        "py-ibm-4610 🍺",
        mode=QR_MODE_ECI,
        ec=QR_EC_M,
        eci=26,          # 26 = UTF-8
        encoding="utf-8",
    )
    p.lf()

    # ==================================================================
    # 3. Alignment
    # ==================================================================
    header(p, "3. ALIGNMENT")

    label(p, "Left-aligned")
    p.qr_code("ALIGN LEFT", align=ALIGN_LEFT)
    p.lf()

    label(p, "Centered (default)")
    p.qr_code("ALIGN CENTER", align=ALIGN_CENTER)
    p.lf()

    label(p, "Right-aligned")
    p.qr_code("ALIGN RIGHT", align=ALIGN_RIGHT)
    p.lf()

    # ==================================================================
    # 4. Real-world payloads
    # ==================================================================
    header(p, "4. REAL-WORLD PAYLOADS")

    # URL
    label(p, "URL")
    p.qr_code("https://printerrr.com", ec=QR_EC_M)
    p.lf()

    # WiFi network config (standard scanned by Android / iOS)
    label(p, "WiFi config  (SSID + password)")
    p.qr_code("WIFI:T:WPA;S:MyNetwork;P:MyPassword;;", ec=QR_EC_M)
    p.lf()

    # vCard contact
    label(p, "vCard contact")
    vcard = (
        "BEGIN:VCARD\n"
        "VERSION:3.0\n"
        "FN:Printer Stream\n"
        "EMAIL:demo@printer.stream\n"
        "URL:https://printer.stream\n"
        "END:VCARD"
    )
    p.qr_code(vcard, ec=QR_EC_M)
    p.lf()

    # ------------------------------------------------------------------
    # Footer
    # ------------------------------------------------------------------
    p.text(sep("=") + "\n")
    p.alignment(ALIGN_CENTER)
    p.text("End of QR demo\n")
    p.text("github.com/printer-stream/py-ibm-4610\n")
    p.alignment(ALIGN_LEFT)

    p.feed(6)
    p.cut()
