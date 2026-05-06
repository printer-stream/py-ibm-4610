package com.ibm.posj.bus.hid.javaxusbold;

import javax.usb.UsbDevice;
import javax.usb.UsbInterface;

public abstract class AbstractUsbInitializer implements UsbInitializer {
   private UsbDevice usbDevice = null;
   private UsbInterface usbInterface = null;
   private UsbHandlePopulator usbHandlePopulator = null;

   public abstract boolean initialize();

   public UsbDevice getUsbDevice() {
      return this.usbDevice;
   }

   public void setUsbDevice(UsbDevice device) {
      this.usbDevice = device;
   }

   public UsbInterface getUsbInterface() {
      return this.usbInterface;
   }

   public void setUsbInterface(UsbInterface usbInterface) {
      this.usbInterface = usbInterface;
   }

   public UsbHandlePopulator getUsbHandlePopulator() {
      return this.usbHandlePopulator;
   }

   public void setUsbHandlePopulator(UsbHandlePopulator populator) {
      this.usbHandlePopulator = populator;
   }
}
