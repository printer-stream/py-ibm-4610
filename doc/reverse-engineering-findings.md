# IBM 4610 SureMark — Reverse Engineering Findings

> Findings from decompiling `posj.jar` (IBM/Toshiba official JavaPOS driver)
> and cross-checking against live USB HID captures on hardware.
> Date: May 2026.

---

## 1. USB transport

| Property | Value |
|----------|-------|
| Vendor ID | `0x04B3` (IBM) |
| Product ID | `0x4535` |
| USB interface | **1** (not 0) |
| OUT endpoint | Control transfer — `bmRequestType=0x21, bRequest=0x09, wValue=0x0201` |
| IN endpoint | Interrupt IN on interface 1 |
| HID report ID | `0x35` (53 decimal) — returned by `getReportID()` in the Java driver |
| Report size | **1022 bytes** (fixed, zero-padded) |

### HID report ID

`getReportID()` in `Hid4610PrinterHandleImp` returns `53` (= `0x35`).  This
is used as the report ID argument to `getReport()` on the IN path
(`getHidDevice().getReport((byte)3, this.getReportID())`).

The **OUT path** uses `setReport((byte)2, (byte)0, ...)`, which maps to a
standard HID SET_REPORT control transfer:

```
bmRequestType = 0x21   (HID, host→device, interface)
bRequest      = 0x09   (SET_REPORT)
wValue        = 0x0200 | reportID   → 0x0200 in practice (report ID in payload)
wIndex        = 1      (interface 1)
```

Note: the Python driver uses `wValue=0x0201` (output report type `0x02`,
report ID field `0x01`) which is the correct HID class encoding.

Source: `HidPOSPrinterHandleImp.setReport()` / `getReport()` and
`Hid4610PrinterHandleImp.getReportID()` (returns `bipush 53`).

Every command sent to the printer must be wrapped in a 1022-byte HID report:

```
Byte [0]:     0x02 if payload length field < 255, else 0x01
Byte [1]:     (N + 4) & 0xFF          ← low byte of length field, N = data length
Byte [2]:     (N + 4) >> 8            ← high byte
Byte [3]:     0x01                    ← fixed (posj transfer[] initialiser)
Bytes [4..6]: 0x00 0x00 0x00          ← fixed padding
Bytes [7..7+N-1]: actual printer data
Remaining: zero-padded to 1022 bytes
```

Source: `PrinterPacket.java` (`format()` / `transport()`), confirmed via
`Hid4610PrinterHandleImp.transport()` bytecode (line 9–28 of `Hid4610.txt`).

The constant `1015` appearing throughout the Java source is the maximum usable
payload size: `1022 − 7 = 1015`.

---

## 2. Command protocol — ESC/POS dialect

The 4610 uses a strict IBM subset of ESC/POS. All command bytes come from
`Cmd4610.java` / `Cmd4610Const.java`. Key sequences:

### Station selection (required before printing)

Must be sent before any print data; the firmware routes output based on the
active station.

| Station | Command bytes |
|---------|--------------|
| Receipt (thermal) | `ESC c 0 \x02` → `[0x1B, 0x63, 0x30, 0x02]` |
| Slip (impact) | `ESC c 0 \x04` → `[0x1B, 0x63, 0x30, 0x04]` |
| Label | `ESC c 0 \x14` → `[0x1B, 0x63, 0x30, 0x14]` |
| None | `ESC c 0 \x00` → `[0x1B, 0x63, 0x30, 0x00]` |

Source: `Print4610CmdFactory.java` / `IBM4610PrinterCmdConst.java`.

### Cut (receipt only)

| Action | Command bytes |
|--------|--------------|
| Full cut | `[0x1B, 0x69]` |
| Partial cut | `[0x1B, 0x6D]` |

### Text / formatting

| Command | Bytes |
|---------|-------|
| Bold on | `[0x1B, 0x45, 0x01]` |
| Bold off | `[0x1B, 0x45, 0x00]` |
| Underline on | `[0x1B, 0x2D, 0x01]` |
| Underline off | `[0x1B, 0x2D, 0x00]` |
| Font A (normal) | `[0x1B, 0x4D, 0x00]` |
| Font B (condensed) | `[0x1B, 0x4D, 0x01]` |
| Font C | `[0x1B, 0x4D, 0x02]` |
| Align left | `[0x1B, 0x61, 0x00]` |
| Align center | `[0x1B, 0x61, 0x01]` |
| Align right | `[0x1B, 0x61, 0x02]` |
| Line feed | `[0x0A]` |
| Carriage return + feed | `[0x0D, 0x0A]` |

### Statistics query

```
ESC Q <subcommand>   →   [0x1B, 0x51, <subcmd_byte>]
```

Source: `Cmd4610.java` field `COMMON_STATISTIC_CMD = new byte[]{27, 81}`.

---

## 3. Statistics response packet format

Discovered empirically by querying `PaperCutCount` (`0x81`) from a live
IBM 4610-TF6 and confirmed against `createPrintDataEvent()` bytecode in
`Hid4610.txt`.

### Raw capture

```
Query:    [0x1B, 0x51, 0x81]   (PaperCutCount)
Response: b'\x01\x06\x00\x00\x00\x01\x0f\x00\x19\x20\x06
            \x28\x00\x00\x00\x00\x00\x00\x00\x80\x81'
          (21 bytes total)
```

### Byte map

| Bytes | Length | Content |
|-------|--------|---------|
| `[0]` | 1 | Transport type byte (`0x01` = long, `0x02` = short) |
| `[1]` | 1 | Length field LSB |
| `[2]` | 1 | Length field MSB |
| `[3:11]` | 8 | Printer status block (see §4) |
| **`[11:15]`** | **4** | **Count value — little-endian uint32** |
| `[15:19]` | 4 | Remainder value — little-endian uint32 (0 when not used) |
| `[19]` | 1 | Response status (`0x80` = data valid / OK) |
| `[20]` | 1 | Subcommand echo (mirrors the request byte, e.g. `0x81`) |

**Total: 21 bytes for all numeric/count statistics.**

`PaperCutCount` example: `raw[11:15] = b'\x28\x00\x00\x00'` → `0x28 = 40`.

### Parsing in Python

```python
def parse_stat(raw: bytes) -> int:
    if len(raw) < 15:
        raise ValueError(f"Stat response too short: {len(raw)} bytes")
    return int.from_bytes(raw[11:15], "little")

def parse_stat_with_remainder(raw: bytes) -> tuple[int, int]:
    if len(raw) < 19:
        raise ValueError(f"Stat response too short: {len(raw)} bytes")
    return (
        int.from_bytes(raw[11:15], "little"),
        int.from_bytes(raw[15:19], "little"),
    )
```

Both functions are exported from `py_ibm_4610`.

`IBM4610.read_stat_value(stat_type)` wraps `read_stat()` + `parse_stat()`
into a single call with a 5-second default timeout (see §3.4).

### 3.3 — Identifying the stat response in the IN stream

The 4610 sends **continuous unsolicited ~8-byte status frames** on the
interrupt IN endpoint (mirroring the Java `StatusDaemon` thread).  After
mechanical operations (cuts, paper feeds) the printer may also send
additional status frames while it finishes the work.  The response to a
stat query therefore arrives among a stream of shorter frames.

The reliable way to identify the stat response is the **subcommand echo
byte at offset +20 within each 21-byte frame**.  The printer mirrors back
the subcommand byte from the request (e.g. `0x81` for `PaperCutCount`).
Checking at a fixed offset (rather than `resp[-1]`) is necessary because
some Linux USB stacks zero-pad interrupt IN packets to `wMaxPacketSize`.

Additionally, the printer may **concatenate multiple queued stat responses
into a single USB IN packet** — for example when a stale query from a
previous session was still pending in the printer's command queue alongside
the current query.  The driver therefore scans in 21-byte strides and
returns the first frame whose echo byte matches:

```python
_STAT_FRAME = 21
expected_echo = STATISTIC_SUBCMDS[stat_type][0]
while True:
    resp = read_response(timeout=remaining_ms)
    if not resp:
        return b""  # timeout
    for start in range(0, len(resp) - _STAT_FRAME + 1, _STAT_FRAME):
        if resp[start + 20] == expected_echo:
            return resp[start:start + _STAT_FRAME]
    # discard packet — no matching frame found
```

### 3.4 — Timeout after mechanical operations

The printer processes commands sequentially from its internal queue.  If
four cut commands are pending, the stat query is not processed until all
four cuts complete.  Each cut takes roughly 300–500 ms of physical time,
so four cuts can consume ≈ 1.5–2 s before the stat response arrives.

The default timeout in `read_stat` / `read_stat_value` is therefore
**5000 ms**.  For workflows involving heavy mechanical activity before a
stat query, pass a longer timeout explicitly:

```python
p.read_stat_value("PaperCutCount", timeout=10_000)
```

---

## 4. Printer status block (bytes [3:11])

After stripping the 3-byte transport header the 4610 returns an 8-byte
status block parsed by `P4610Status.updateStatus()`. The relevant bits:

| Byte (0-based within block) | Bit | Meaning |
|-----------------------------|-----|---------|
| 0 | 0 | `STATUS_CMDLOADED` |
| 0 | 1 | `STATUS_CR_RIGHTHOME` |
| 0 | 2 | `STATUS_CR_LEFTHOME` |
| 0 | 5 | `STATUS_NORMAL` |
| 1 | 0 | `STATUS_RIBBON_COVER` |
| 1 | 2 | `STATUS_CR_ERROR` |
| 1 | 5 | `STATUS_CMDREJECT` |
| 2 | 0 | `STATUS_DI_READY` |
| 2 | 1 | `STATUS_DI_FRONT` |
| 2 | 2 | `STATUS_DI_TOP` |
| 3 | 0 | `STATUS_BUFFER_HELD` |
| 3 | 1 | `STATUS_OPEN_THROAT` |
| 3 | 4 | `STATUS_BUFFER_EMPTY` |
| 3 | 5 | `STATUS_BUFFER_FULL` |
| 3 | 6 | `STATUS_MEMORY_FULL` |
| 4 | 0 | `STATUS_HOME_ERROR` |
| 4 | 1 | `STATUS_DI_ERROR` |
| 4 | 5 | `STATUS_EPROM_MCT_ERR` |
| 5 | 0 | `STATUS_FLASH_FULL` |
| 5 | 2 | `STATUS_FIRMWARE_ERROR` |
| 6 | varies | extended data flags (`STATUS_PRINTERID_DATA`, `STATUS_ECLEVEL_DATA`, etc.) |

Source: `P4610Status.java` (`parseByte1` through `parseByte8`).

---

## 5. Statistics subcommand table

All 35 known subcommand bytes, from `Cmd4610.java` and
`Gen4610CmdFactory.java`:

| Stat name | Subcommand byte |
|-----------|----------------|
| `ManufactureDate` | `0x70` |
| `PaperCutCount` | `0x81` |
| `PaperCutCountRemainder` | `0x93` |
| `FailedPaperCutCount` | `0x86` |
| `ReceiptCoverOpenCount` | `0x85` |
| `ReceiptLineFeedCount` | `0x84` |
| `ReceiptLineFeedCountRemainder` | `0x92` |
| `ReceiptCharacterPrintedCount` | `0x83` |
| `ReceiptCharacterPrintedCountRemainder` | `0x82` |
| `SlipCharacterPrintedCount` | `0x87` |
| `SlipCharacterPrintedCountRemainder` | `0x94` |
| `SlipCoverOpenCount` | `0x8B` |
| `SlipLineFeedCount` | `0x88` |
| `SlipLineFeedCountRemainder` | `0x95` |
| `FormInsertionCount` | `0x8C` |
| `FormInsertionCountRemainder` | `0x96` |
| `HomeErrorCount` | `0x8A` |
| `PrintSideChangeCount` | `0x90` |
| `PrintSideChangeCountRemainder` | `0x9A` |
| `FailedPrintSideChangeCount` | `0x91` |
| `FailedPrintSideChangeCountRemainder` | `0x98` |
| `BarcodePrintedCount` | `0xD8` |
| `BarcodePrintedCountRemainder` | `0x9D` |
| `MaximumTempReachedCount` | `0xD9` |
| `NVRAMWriteCount` | `0xD2` |
| `FailedReadCount` | `0x8F` |
| `FailedReadCountRemainder` | `0x99` |
| `TotalReadCount` | `0x8D` |
| `TotalReadCountRemainder` | `0x9B` |
| `IBM_CheckScannedCount` | `0xD3` |
| `IBM_CheckScannedCountRemainder` | `0x9E` |
| `IBM_CheckScannerBrightnessQuality` | `0x6D` |
| `IBM_CheckScannerContrastQuality` | `0x6D` |
| `IBM_CheckScannerFocusQuality` | `0x6E` |
| `IBM_ChecksFailedQualityCount` | `0xD4` |

Java encodes these as signed bytes; the table above shows the unsigned
Python equivalent (`negative_java_byte + 256`).

---

## 6. Key Java source files and what they contain

| File | Key findings |
|------|-------------|
| `Cmd4610.java` | Every raw byte sequence — commands, station bytes, cut, statistics subcommands |
| `PrinterPacket.java` | 1022-byte HID report framing, `transport[]` array, length field encoding |
| `Hid4610PrinterHandleImp.java` | USB read/write loop, `createPrintDataEvent()`, `createPrintStatus()`, `doHidRead()` |
| `HidPOSPrinterHandleImp.java` | Base class: `transport()`, `printStatusDataEvent` field, `getPrintStatusDataEvent()` |
| `P4610Status.java` | Status byte parser: `parseByte1`–`parseByte8`, `isExtendedData()` |
| `Print4610CmdFactory.java` | Station selection, cut, feed, image print |
| `Gen4610CmdFactory.java` | Statistics mapping, error recovery, NVRAM |
| `Font4610CmdFactory.java` | Bold, italic, font size, underline |
| `IBM4610Utility.java` | Factory / initialiser — no stat parsing |
| `IBM4610PrinterCmdConst.java` | Named constants for all command bytes |

---

## 7. Library implementation summary

The findings above were used to build `py-ibm-4610`, a pure-Python USB HID
driver. Final state after this reverse-engineering effort:

| Component | File | Status |
|-----------|------|--------|
| HID packet framing | `py_ibm_4610/_transport.py` | Complete |
| Protocol constants | `py_ibm_4610/_constants.py` | Complete (35 stat subcmds, all ESC sequences) |
| Base printer / buffer | `py_ibm_4610/printer/base.py` | Complete |
| Full driver | `py_ibm_4610/printer/ibm4610.py` | Complete |
| Receipt-only model | `py_ibm_4610/printer/ibm4610_1nr.py` | Complete (auto-selects receipt station on `open()`) |
| Stat response parser | `py_ibm_4610/printer/ibm4610.py` | `parse_stat()`, `parse_stat_with_remainder()`, `read_stat_value()`; response identified by subcommand echo byte; 5 s default timeout |
| Tests | `tests/` | 133 tests, all offline (no printer required) |
| Build | `pyproject.toml` | `setuptools.build_meta`, wheel builds cleanly |

### Quick-start

```python
from py_ibm_4610 import IBM4610_1NR

with IBM4610_1NR() as p:
    p.bold(True).text("Hello, World!\n").bold(False)
    p.feed(4).cut()

# Read a hardware counter
with IBM4610_1NR() as p:
    cuts = p.read_stat_value("PaperCutCount")   # e.g. 40
```
