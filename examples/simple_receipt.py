#!/usr/bin/env python3
"""simple_receipt.py — Recreate a real-world Swedish pub receipt.

Demonstrates:
  - centered header with bold store name
  - two-column item rows (name + right-aligned price)
  - bold + double-wide total line
  - VAT table with four fixed-width columns
  - card payment block
  - centered footer

Run with the printer connected over USB:

    python examples/simple_receipt.py
"""

import logging

from py_ibm_4610 import IBM4610_1NR
from py_ibm_4610 import version
from py_ibm_4610 import ALIGN_LEFT, ALIGN_CENTER, BC_CODE128, QR_EC_M

logging.basicConfig(level=logging.WARNING)

# Printable width in characters (FONT_A, normal density on 80 mm paper)
WIDTH = 42

# Swedish characters need cp850 (covers Å Ä Ö å ä ö)
ENC = "cp850"


def sep(char: str = ".", n: int = WIDTH) -> str:
    """Return a full-width separator string."""
    return char * n


def row(left: str, right: str, width: int = WIDTH) -> str:
    """Left-align *left* and right-align *right* within *width* columns."""
    pad = width - len(left) - len(right)
    return left + " " * max(pad, 1) + right


def row_dw(left: str, right: str) -> str:
    """Two-column row for double-wide mode (effective width = WIDTH // 2)."""
    return row(left, right, WIDTH // 2)


with IBM4610_1NR() as p:
    # ------------------------------------------------------------------
    # Header
    # ------------------------------------------------------------------
    p.alignment(ALIGN_CENTER)
    p.bold(True).text("The Jolly Bailiff\n", ENC).bold(False)
    p.text("Frying Pan Alley 12\n", ENC)
    p.text("DY9 9TN Bell End\nWorcestershire, UK\n", ENC)
    p.text("Org Nr: 30922888293\n", ENC)
    p.alignment(ALIGN_LEFT)
    p.text(sep() + "\n")


    # ------------------------------------------------------------------
    # Table number
    # ------------------------------------------------------------------
    p.alignment(ALIGN_CENTER)
    p.bold(True).text("Bord Nr: 909\n").bold(False)
    p.alignment(ALIGN_LEFT)
    p.text(sep() + "\n")

    # ------------------------------------------------------------------
    # Transaction metadata
    # ------------------------------------------------------------------
    p.text("Datum: 2023-05-21 21:10:13\n")
    p.text("Kvittonr: 01-00310324\n")
    p.text("Kassör: Kassör 1 (1011)\n", ENC)
    p.text(sep() + "\n")

    # ------------------------------------------------------------------
    # Items
    # ------------------------------------------------------------------
    p.text(row("En God Öl", "624,00") + "\n", ENC)
    p.text("  12 st * 52,00 kr/st\n")
    p.text(sep("=") + "\n")

    # ------------------------------------------------------------------
    # Item count
    # ------------------------------------------------------------------
    p.text(row("Antal art.", "12") + "\n")
    p.lf()

    # ------------------------------------------------------------------
    # Total — bold, double-wide
    # ------------------------------------------------------------------
    p.bold(True).double_wide(True)
    p.text(row_dw("ATT BETALA", "624,00") + "\n")
    p.double_wide(False).bold(False)
    p.lf()

    # ------------------------------------------------------------------
    # Payment method
    # ------------------------------------------------------------------
    p.text(row("KORT", "684,00") + "\n")
    p.text(row("(EXTRA", "60,00)") + "\n")
    p.lf()
    p.lf()

    # ------------------------------------------------------------------
    # VAT table — four columns: Moms | Belopp | Netto | Brutto
    # ------------------------------------------------------------------
    p.text(sep() + "\n")
    p.text(f"{'Moms':<6}{'Belopp':<12}{'Netto':<9}Brutto\n")
    p.text(f"{'25%':<6}{'20,80':<12}{'83,20':<9}624,00\n")
    p.text(sep() + "\n")

    # ------------------------------------------------------------------
    # Card receipt details
    # ------------------------------------------------------------------
    p.alignment(ALIGN_CENTER)
    p.text("RIHTT0D0000044371\n")
    p.alignment(ALIGN_LEFT)
    p.text(row("TOTAL:", "664,00 kr") + "\n")
    p.text(row("PAN: **** **** **** 9329", "Debit Visa") + "\n")
    p.text(row("AID: A0000000010302", "Betalning") + "\n", ENC)
    p.text(row("2023-05-21 21:09:56", "Kontaktlös") + "\n", ENC)
    p.text("Transaktion: 0000084959839\n")
    p.text("Auktorisation: 520124\n")
    p.text("Butik: 62230593\n")
    p.text("TermId: 00000001\n")
    p.text("APPROVED\n")
    
    # ------------------------------------------------------------------
    # QR Code with store URL
    # ------------------------------------------------------------------
    p.text(sep() + "\n")
    p.qr_code("https://github.com/printer-stream/py-ibm-4610", ec=QR_EC_M)
    p.flush()  # ensure QR code is printed before the rest of the receipt

    # ------------------------------------------------------------------
    # Barcode with transaction ID
    # ------------------------------------------------------------------
    p.text(sep() + "\n")
    p.barcode(data="0000084959839", symbol=BC_CODE128, width=4)

    # ------------------------------------------------------------------
    # Footer
    # ------------------------------------------------------------------
    p.alignment(ALIGN_CENTER)
    p.text("The Jolly Bailiff\n", ENC)
    p.text(f"Tack för besöket v{version}\n", ENC)
    p.text("Välkommen åter!\n", ENC)
    p.bold(True).text("Spara kvitto\n", ENC).bold(False)
    p.alignment(ALIGN_LEFT)

    # Feed above the cutter and cut
    p.feed(6)
    p.cut()
