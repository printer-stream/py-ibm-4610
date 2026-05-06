package com.ibm.posj.bus.hid.javaxusbold;

import javax.usb.UsbDevice;
import javax.usb.UsbInterface;

public interface UsbInitializer {
   boolean initialize();

   UsbDevice getUsbDevice();

   void setUsbDevice(UsbDevice var1);

   UsbInterface getUsbInterface();

   void setUsbInterface(UsbInterface var1);

   UsbHandlePopulator getUsbHandlePopulator();

   void setUsbHandlePopulator(UsbHandlePopulator var1);
}
