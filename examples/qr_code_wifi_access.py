#!/usr/bin/env python3
"""qr_code_wifi_access.py — WiFi access QR code printing demo
                            for the IBM 4610 SureMark.

Demonstrates a way to print a WiFi access QR code using the qr_code().

Receipt station only.  Requires firmware 0F.xx or above 
(models 1NR, 2NR, 2CR, otherwise refer to the documentation).

Run with the printer connected over USB:

    python examples/qr_code_wifi_access.py
"""

import logging
from datetime import datetime

from py_ibm_4610 import IBM4610_1NR
from py_ibm_4610 import ALIGN_LEFT, ALIGN_CENTER, QR_EC_M

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
    p.text(datetime.now().strftime("%Y-%m-%d  %H:%M:%S") + "\n")

    # WiFi network config (standard scanned by Android / iOS)
    label(p, "WiFi config  (SSID + password)")
    p.qr_code("WIFI:T:WPA;S:MyNetwork;P:MyPassword;;", ec=QR_EC_M)
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
