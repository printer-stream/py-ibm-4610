package com.ibm.posj.bus.hid.javaxusbold;

public class ProtocolConverterInitializer extends DeferredUsbInitializer implements UsbInitializer {
   public static final byte[] RESET = new byte[]{-32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

   protected void deferredInitialize() {
      if (this.getTracer().isOn()) {
         this.getTracer().println("-->deferredInitialize() : Renumerating converter...<--");
      }

      byte interfaceNumber = 0;
      byte[] cmd = new byte[RESET.length];
      System.arraycopy(RESET, 0, cmd, 0, cmd.length);
      this.submitSync(interfaceNumber, cmd);
   }
}
