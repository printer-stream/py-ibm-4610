package com.ibm.posj.bus.poskbd;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.HandleKeyVisitor;
import com.ibm.posj.bus.AbstractHandleImp;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.flash.FlashRequest;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public abstract class AbstractPosKbdHandleImp extends AbstractHandleImp {
   private Tracer tracer = null;

   public AbstractPosKbdHandleImp(HandleKey key) {
      super(key);
   }

   public DevBus getDevBus() {
      return DevBuses.POSKBD_DEVBUS;
   }

   public String getDeviceSerialNumber() {
      return null;
   }

   public void init() throws HandleException {
   }

   public abstract void accept(HandleImpVisitor var1);

   public void setHandle(Handle handle) {
      super.setHandle(handle);
   }

   public void flash(FlashRequest flashRequest) {
   }

   public boolean isFlashable() {
      return false;
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

   public static class DefaultHandleKey implements HandleKey {
      public void accept(HandleKeyVisitor visitor) {
         visitor.visitUnknownHandleKey(this);
      }

      public String toString() {
         return Integer.toString(this.hashCode());
      }

      public DevBus getDevBus() {
         return DevBuses.POSKBD_DEVBUS;
      }

      public DevCat getDevCat() {
         return DevCats.POSKEYBOARD_DEVCAT;
      }
   }
}
