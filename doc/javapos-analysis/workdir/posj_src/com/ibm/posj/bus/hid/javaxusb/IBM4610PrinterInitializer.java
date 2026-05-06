package com.ibm.posj.bus.hid.javaxusb;

public class IBM4610PrinterInitializer extends ReenumerateInitializer implements UsbInitializer {
   protected byte getUsbInterfaceNumber() {
      return 0;
   }

   protected byte[] getReenumerateCommand() {
      return new byte[]{2, 0, 0, 64};
   }
}
