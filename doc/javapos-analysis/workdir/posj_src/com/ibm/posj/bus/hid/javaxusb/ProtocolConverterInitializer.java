package com.ibm.posj.bus.hid.javaxusb;

public class ProtocolConverterInitializer extends ReenumerateInitializer implements UsbInitializer {
   protected byte getUsbInterfaceNumber() {
      return 0;
   }

   protected byte[] getReenumerateCommand() {
      return new byte[]{-32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   }

   protected boolean shouldWaitForDevice() {
      return false;
   }
}
