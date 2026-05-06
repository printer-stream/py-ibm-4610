package com.ibm.posj.bus.embedded;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.AbstractHandleImp;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;

public abstract class AbstractEmbeddedHandleImp extends AbstractHandleImp {
   private Tracer tracer = null;

   public AbstractEmbeddedHandleImp(HandleKey key) {
      super(key);
   }

   public String getDeviceSerialNumber() {
      return null;
   }

   public DevBus getDevBus() {
      return DevBuses.EMBEDDED_DEVBUS;
   }

   public void setHandle(Handle handle) {
      super.setHandle(handle);
   }

   public short getECLevel() {
      return -1;
   }

   protected Tracer getTracer() {
      if (this.tracer == null) {
         String className = this.getClass().getName();
         className = className.substring(className.lastIndexOf(46) + 1);
         if (this.getHandle() != null) {
            this.tracer = TracerFactory.getInstance().createTracer(this.getHandle().getName(), className);
         } else {
            this.tracer = TracerFactory.getInstance().createTracer(this.getDevCat().toString(), className);
         }
      }

      return this.tracer;
   }

   protected boolean isTracerOn() {
      return this.getTracer().isOn();
   }

   protected void traceMinimum(String msg) {
      this.getTracer().println(1, msg);
   }

   protected void traceNormal(String msg) {
      this.getTracer().println(2, msg);
   }

   protected void traceMaximum(String msg) {
      this.getTracer().println(3, msg);
   }
}
