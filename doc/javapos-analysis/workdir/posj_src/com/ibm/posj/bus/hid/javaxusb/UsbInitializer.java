package com.ibm.posj.bus.hid.javaxusb;

import javax.usb.UsbDevice;

public interface UsbInitializer {
   boolean initialize();

   UsbDevice getUsbDevice();

   void setUsbDevice(UsbDevice var1);

   UsbHandlePopulator getUsbHandlePopulator();

   void setUsbHandlePopulator(UsbHandlePopulator var1);
}
