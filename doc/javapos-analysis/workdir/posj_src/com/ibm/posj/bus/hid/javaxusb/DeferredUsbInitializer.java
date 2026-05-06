package com.ibm.posj.bus.hid.javaxusb;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;

public abstract class DeferredUsbInitializer extends AbstractUsbInitializer implements UsbInitializer {
   private Runnable runnable = new DeferredUsbInitializer$1(this);
   private Thread thread = new Thread(this.runnable);
   private String packageName = this.getClass().getPackage().getName();
   private String className = this.getClass().getName().substring(this.packageName.length() + 1);
   protected Tracer tracer = TracerFactory.getInstance().createTracer("HID", this.className);

   public boolean initialize() {
      this.thread.setDaemon(true);
      this.thread.start();
      return false;
   }

   protected abstract void deferredInitialize();
}
