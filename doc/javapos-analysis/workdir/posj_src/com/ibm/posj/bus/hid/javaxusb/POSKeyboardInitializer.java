package com.ibm.posj.bus.hid.javaxusb;

public class POSKeyboardInitializer extends ReenumerateInitializer implements UsbInitializer {
   protected byte getUsbInterfaceNumber() {
      return 1;
   }

   protected byte[] getReenumerateCommand() {
      return new byte[]{31};
   }

   protected short getProductId() {
      short base = (short)(255 & this.getUsbDevice().getUsbDeviceDescriptor().idProduct());
      return (short)(18432 | base);
   }
}
