package com.ibm.posj.bus.hid.javaxusbold;

public class IBM4689PrinterInitializer extends DeferredUsbInitializer implements UsbInitializer {
   public static final byte[] RESET = new byte[]{2, 0, 64, 0, 0, 0, 0, 1};

   protected void deferredInitialize() {
      if (this.getTracer().isOn()) {
         this.getTracer().println("-->deferredInitialize() : Renumerating 4689 printer...<--");
      }

      byte interfaceNumber = 1;
      byte[] cmd = new byte[RESET.length];
      System.arraycopy(RESET, 0, cmd, 0, cmd.length);
      this.submitSync(interfaceNumber, cmd);
   }
}
