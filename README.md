# IBM/Toshiba 4610 SureMark

Python library for **IBM / Toshiba 4610 SureMark** POS thermal receipt printers,
communicating over USB.

## Hardware

| Model    | Station(s)                          | Tested  | Notes                   |
|----------|-------------------------------------|---------|-------------------------|
| 4610-1NR | Receipt (thermal roll)              | + | Common receipt-only variant   |
| 4610-TI3 | Receipt + Slip/DI + Check scanner   | - | Full-featured model           |
| Other    | Varies                              | - | Use `IBM4610` base class      |

USB identifiers: `VID=0x04B3`, `PID=0x4535`, Interface 1.

## Installation

```bash
pip install py-ibm-4610
```

`pyusb` may require **libusb** to be installed on the system. This needs to be verified.

Also udev features must listed and verified.

## Quick start

```python
import logging
from py_ibm_4610 import IBM4610, IBM4610_1NR, STATION_RECEIPT
from py_ibm_4610 import FONT_A, FONT_B, FONT_C

logging.basicConfig(
    level=logging.DEBUG,
)

# Context manager opens and closes the USB connection automatically
with IBM4610_1NR() as p:
    p.select_font(FONT_C)
    p.bold(True).text("Hello, World!\n").bold(False)
    p.feed(10)
    p.cut()
```

## Reference page

There is a `ReferencePage()` class slop-coded recently. It needs to be refactored.

Available sections:
`TEXT_FORMATTING`, `FONT_SCALING`, `FONT_FACES`, `ALIGNMENT`, `FONT_COLOR`,
`ROTATION`, `LINE_SPACING`, `FEED`, `TAB_STOPS`, `MARGINS`, `DOT_SPACING`,
`CHAR_SETS`, `PRINT_QUALITY`, `BARCODES`, `BITMAP`, `PAGE_MODE`,
`BUFFER_CONTROL`, `STATUS`, `LINE_COUNT`, `STATUS_SENT`, `ERROR_RECOVERY`,
`FEED_BUTTON`, `CASH_DRAWER`, `BEEPER`, `MCT`, `STATISTICS`.

## API overview

All command methods return `self` for chaining. Data is buffered until
`flush()` is called (or `cut()`, which flushes automatically).

```python
p.select_station(STATION_RECEIPT)
p.bold(True).text("Header\n").bold(False)
p.alignment(ALIGN_CENTER).text("Centered\n").alignment(ALIGN_LEFT)
p.barcode(BC_EAN13, "5901234123457", height=60)
p.feed(5)
p.cut()

# Read printer statistics over USB
resp = p.read_stat("PaperCutCount")
```

### Buffered workflow

```python
p.select_station(STATION_RECEIPT)
p.text("Line 1\n")
p.text("Line 2\n")
total_bytes = p.flush()             # sends everything at once
```

### High-level helper

```python
p.print_receipt(
    lines=["Item A   $1.00", "Item B   $2.50", "Total    $3.50"],
    feed=5,
    cut=True,
)
```

## Building from source

```bash
make build        # produces dist/py_ibm_4610-<version>-py3-none-any.whl
make install      # installs the wheel for the current user
make clean        # removes the built wheel
```

## Notes

Reverse-engineered from the IBM JavaPOS driver (`posj.jar`) sourced out of a random place most probably.

## License

GNU General Public License v3.0
