package com.ibm.posj.bus.hid.javaxusbold;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.PosException;
import javax.usb.UsbControlIrp;
import javax.usb.UsbInterface;

public abstract class DeferredUsbInitializer extends AbstractUsbInitializer implements UsbInitializer {
   private Runnable runnable = new DeferredUsbInitializer$1(this);
   private Thread thread = new Thread(this.runnable);
   private Tracer tracer = null;

   public boolean initialize() {
      this.thread.setDaemon(true);
      this.thread.start();
      return false;
   }

   protected abstract void deferredInitialize();

   protected void submitSync(byte interfaceNumber, byte[] cmd) {
      try {
         UsbControlIrp irp = this.getUsbDevice().createUsbControlIrp((byte)33, (byte)9, (short)512, (short)interfaceNumber);
         irp.setData(cmd);
         this.getInterface(interfaceNumber).claim(DefaultUsbInterfacePolicy.getInstance());
         this.getUsbDevice().syncSubmit(irp);
      } catch (Exception var4) {
         if (this.getTracer().isOn()) {
            this.getTracer().println("-->Exception occurred " + var4.toString() + "<--");
            this.getTracer().print(var4);
         }

         this.getUsbHandlePopulator().setLastException(new PosException("Could not initialize device : " + this.getUsbDevice(), var4));
      }
   }

   private UsbInterface getInterface(byte interfaceNumber) {
      return this.getUsbDevice().getActiveUsbConfiguration().getUsbInterface(interfaceNumber);
   }

   protected Tracer getTracer() {
      if (this.tracer == null) {
         String className = this.getClass().getName();
         className = className.substring(className.lastIndexOf(46) + 1);
         this.tracer = TracerFactory.getInstance().createTracer("HID", className);
      }

      return this.tracer;
   }
}
