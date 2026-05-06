package com.ibm.posj.bus.hid.javaxusb;

public class IBM4689PrinterInitializer extends ReenumerateInitializer implements UsbInitializer {
   protected byte getUsbInterfaceNumber() {
      return 1;
   }

   protected byte[] getReenumerateCommand() {
      return new byte[]{2, 0, 64, 0, 0, 0, 0, 1};
   }
}
