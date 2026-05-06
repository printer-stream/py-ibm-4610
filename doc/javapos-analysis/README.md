# IBM 4610 SureMark — JavaPOS Driver Decompilation Guide

## Prerequisites

```bash
# Rocky Linux 9 (or any RHEL-based distro)
sudo dnf install -y java-17-openjdk wget
```

## Step 1 — Get the JAR

```bash
mkdir ~/ibm-4610 && cd ~/ibm-4610

wget "https://github.com/ByINTI/AuxiliarTotem/raw/master/Programas%20Totem/PinPad/Auttar/kit-distribuicao-CTFClient-03.02.01-D03/program%20files/Auttar/CTFClient/ibmjpos/Lib/posj.jar"
```

## Step 2 — Explore the JAR contents

```bash
# List all classes related to printer, HID, 4610
jar tf posj.jar | grep -i "hid\|print\|4610\|suremark\|transport\|station" | sort
```

Key packages to know:
| Package | Purpose |
|---|---|
| `com/ibm/posj/bus/hid/` | HID transport implementations |
| `com/ibm/posj/bus/printer/cmds/ibm4610/` | Raw byte command sequences |
| `com/ibm/posj/bus/hid/javaxusb/` | USB initializers |
| `com/ibm/posj/IBM4610PrinterCmd*.class` | High-level command definitions |

## Step 3 — Extract and disassemble specific classes

```bash
# Extract the classes you need (example: packet framing + HID driver)
jar xf posj.jar \
  com/ibm/posj/bus/PrinterPacket.class \
  'com/ibm/posj/bus/PrinterPacket$Transport.class' \
  'com/ibm/posj/bus/PrinterPacket$PacketBufferCreator.class' \
  'com/ibm/posj/bus/PrinterPacket$PacketByteBufferFactory.class' \
  com/ibm/posj/bus/hid/Hid4610PrinterHandleImp.class \
  'com/ibm/posj/bus/hid/Hid4610PrinterHandleImp$StatusDaemon.class' \
  com/ibm/posj/bus/hid/javaxusb/IBM4610PrinterInitializer.class \
  com/ibm/posj/bus/PrinterBusWriter.class \
  com/ibm/posj/bus/printer/cmds/ibm4610/Cmd4610.class \
  com/ibm/posj/IBM4610PrinterCmd.class \
  com/ibm/posj/IBM4610PrinterCmdConst.class

# Disassemble — -c gives bytecode, -p includes private members
javap -c -p com/ibm/posj/bus/PrinterPacket.class > PrinterPacket.txt
javap -c -p 'com/ibm/posj/bus/PrinterPacket$Transport.class' >> PrinterPacket.txt
javap -c -p com/ibm/posj/bus/hid/Hid4610PrinterHandleImp.class > Hid4610.txt
javap -c -p com/ibm/posj/bus/printer/cmds/ibm4610/Cmd4610.class > Cmd4610.txt
javap -c -p com/ibm/posj/IBM4610PrinterCmdConst.class > Cmd4610Const.txt
```

## Step 4 — Full decompilation to Java source (optional)

```bash
# Download Vineflower decompiler (actively maintained Fernflower fork)
wget https://github.com/Vineflower/vineflower/releases/download/1.10.1/vineflower-1.10.1.jar

mkdir posj_src
java -jar vineflower-1.10.1.jar posj.jar posj_src/

# Then grep the source for what you need
grep -rn "bold\|italic\|font\|setHeader\|transfer\|REPORT" posj_src/ --include="*.java" | less
```

## Step 5 — USB monitoring (for cross-checking)

```bash
# Load usbmon kernel module
sudo modprobe usbmon

# Find which USB bus the printer is on
lsusb | grep -i "04b3:4535"
# Example output: Bus 002 Device 019 — so monitor bus 2

# Start capture in background
sudo cat /sys/kernel/debug/usb/usbmon/2u &

# Run your Python script
python3 print_hid_framed_correct.py

# Kill monitor
sudo kill %1
```

## What we discovered (summary)

### HID packet structure

The 4610 uses **interface 1**, report ID `0x01`, packet size **1022 bytes**.

```
Byte 0:    0x02 (short cmd) or 0x01 (long cmd, if len_field >= 255)
Byte 1:    (data_len + 4) & 0xFF        ← low byte of length
Byte 2:    (data_len + 4) >> 8          ← high byte of length
Byte 3:    0x01                          ← fixed (from transfer[])
Bytes 4-6: 0x00 0x00 0x00               ← fixed padding
Bytes 7+:  actual printer data
Remaining: zero-padded to 1022 bytes
```

### Working Python send function

```python name=ibm4610_send.py
import usb.core
import usb.util

VENDOR  = 0x04b3
PRODUCT = 0x4535
IFACE   = 1
REPORT_SIZE = 1022

def make_packet(data: bytes) -> bytes:
    N         = len(data)
    len_field = (7 + N) - 3
    cmd       = 0x01 if len_field >= 255 else 0x02
    header    = bytes([cmd, len_field & 0xFF, (len_field >> 8) & 0xFF,
                       0x01, 0x00, 0x00, 0x00])
    return (header + data).ljust(REPORT_SIZE, b'\x00')

def send(data: bytes):
    dev = usb.core.find(idVendor=VENDOR, idProduct=PRODUCT)
    if dev is None:
        raise RuntimeError("Printer not found")
    if dev.is_kernel_driver_active(IFACE):
        dev.detach_kernel_driver(IFACE)
    usb.util.claim_interface(dev, IFACE)
    try:
        dev.ctrl_transfer(
            bmRequestType=0x21, bRequest=0x09,
            wValue=0x0201, wIndex=IFACE,
            data_or_wLength=make_packet(data), timeout=5000
        )
    finally:
        usb.util.release_interface(dev, IFACE)
        dev.attach_kernel_driver(IFACE)
```

## Next steps / classes still to decompile

| Class | Why |
|---|---|
| `Cmd4610.class` | All raw ESC byte sequences (bold, font size, italic, feed, cut…) |
| `IBM4610PrinterCmdConst.class` | Named constants for all commands |
| `Gen4610CmdFactory.class` | General printer commands |
| `Font4610CmdFactory.class` | Font selection commands |
| `Print4610CmdFactory.class` | Print/feed/cut commands |
| `IBM4610Writer.class` | How commands are assembled into packets |
