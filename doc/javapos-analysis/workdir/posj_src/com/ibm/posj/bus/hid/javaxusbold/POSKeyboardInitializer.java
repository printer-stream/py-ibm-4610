package com.ibm.posj.bus.hid.javaxusbold;

public class POSKeyboardInitializer extends DeferredUsbInitializer implements UsbInitializer {
   public static final byte[] SET_SIO_LEGACY_MODE_CMD = new byte[]{31};

   protected void deferredInitialize() {
      if (this.getTracer().isOn()) {
         this.getTracer().println("-->deferredInitialize() :Renumerating Kbd as legacy mode...<--");
      }

      byte interfaceNumber = 1;
      byte[] cmd = new byte[SET_SIO_LEGACY_MODE_CMD.length];
      System.arraycopy(SET_SIO_LEGACY_MODE_CMD, 0, cmd, 0, cmd.length);
      this.submitSync(interfaceNumber, cmd);
   }
}
