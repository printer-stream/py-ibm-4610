package com.ibm.posj.bus.hid.javaxusb;

import javax.usb.UsbDevice;

public abstract class AbstractUsbInitializer implements UsbInitializer {
   private UsbDevice usbDevice = null;
   private UsbHandlePopulator usbHandlePopulator = null;

   public abstract boolean initialize();

   public UsbDevice getUsbDevice() {
      return this.usbDevice;
   }

   public void setUsbDevice(UsbDevice device) {
      this.usbDevice = device;
   }

   public UsbHandlePopulator getUsbHandlePopulator() {
      return this.usbHandlePopulator;
   }

   public void setUsbHandlePopulator(UsbHandlePopulator populator) {
      this.usbHandlePopulator = populator;
   }
}
