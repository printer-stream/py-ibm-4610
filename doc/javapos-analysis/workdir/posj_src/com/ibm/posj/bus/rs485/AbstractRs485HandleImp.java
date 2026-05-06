package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jsio.SioException;
import com.ibm.jsio.SioIrp;
import com.ibm.jsio.event.SioDeviceDataEvent;
import com.ibm.jsio.event.SioDeviceErrorEvent;
import com.ibm.jsio.event.SioDeviceListener;
import com.ibm.jsio.event.SioDeviceStatusEvent;
import com.ibm.jsio.os.SioIrpRecyclable;
import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.AbstractHandleImp;
import com.ibm.posj.bus.FlashHandleImp;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;

public abstract class AbstractRs485HandleImp extends AbstractHandleImp {
   private SioDevice sioDevice = null;
   private SioDeviceListener listener = new AbstractRs485HandleImp.AbstractListener();
   private Tracer tracer = null;
   public static final int EC_LEVEL_NOT_SUPPORTED = -1;
   private static String REMOVED_DEVICE_MSG = "SioDevice device removed";
   private static String ADDED_DEVICE_MSG = "SioDevice device added";

   public AbstractRs485HandleImp(HandleKey key, SioDevice device) {
      super(key);
      if (device == null) {
         throw new IllegalArgumentException();
      } else {
         this.sioDevice = device;
      }
   }

   public String getDeviceSerialNumber() {
      return null;
   }

   public DevBus getDevBus() {
      return DevBuses.RS485_DEVBUS;
   }

   public void setHandle(Handle handle) {
      super.setHandle(handle);
   }

   public short getECLevel() {
      return -1;
   }

   public void init() throws HandleException {
      try {
         if (!this.sioDevice.isOpen()) {
            this.sioDevice.open();
         }
      } catch (SioException var2) {
         this.setHandleOnline(false);
         throw new HandleException("Error at initialization : " + var2.toString(), var2);
      }

      this.sioDevice.addSioDeviceListener(this.listener);
   }

   public boolean isFlashable() {
      return false;
   }

   public FlashHandleImp getFlashHandleImp() {
      for (HandleImp currHandleImp : this.getHandleImpGroup()) {
         if (currHandleImp instanceof FlashHandleImp) {
            return (FlashHandleImp)currHandleImp;
         }
      }

      return null;
   }

   protected SioDevice getSioDevice() {
      return this.sioDevice;
   }

   protected void checkNullArg(byte[] cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      }
   }

   protected void submitSync(byte[] cmd) throws HandleException {
      this.submitSync(cmd, 0, cmd.length);
   }

   protected void submitSync(byte[] cmd, int off, int len) throws HandleException {
      this.checkNullArg(cmd);

      try {
         SioIrpRecyclable irp = (SioIrpRecyclable)this.getSioDevice().createSioIrp(cmd, off, len, true);
         this.sioDevice.syncSubmit(irp);
         irp.recycle();
      } catch (Exception var5) {
         throw new HandleException("Error while sending sync : " + Util.toFormatedHexString(cmd) + " to SioDevice", var5);
      }
   }

   protected void submitAsync(byte[] cmd) throws HandleException {
      this.submitAsync(cmd, 0, cmd.length);
   }

   protected void submitAsync(byte[] cmd, int off, int len) throws HandleException {
      this.checkNullArg(cmd);

      try {
         this.sioDevice.asyncSubmit(this.getSioDevice().createSioIrp(cmd, off, len, true));
      } catch (Exception var5) {
         throw new HandleException("Error while sending async : " + Util.toFormatedHexString(cmd) + " to SioDevice", var5);
      }
   }

   protected void dataEventOccurred(SioDeviceDataEvent event) {
   }

   protected void statusEventOccurred(SioDeviceStatusEvent event) {
      if (this.getHandle() != null) {
         if (this.tracer.isOn()) {
            this.tracer.println(2, "-->StatusEventOccurred : " + event.getStatus() + "<--");
         }

         if (event.getStatus() == 1) {
            this.handleOnline();
         } else if (event.getStatus() == 2) {
            this.handleOffline();
         } else if (event.getStatus() == 3) {
            this.sioIrpCompleted(event.getSioIrp());
         }
      }
   }

   protected void handleOnline() {
      if (this.tracer.isOn()) {
         this.tracer.println(2, "-->handleOnline");
         this.tracer.println(2, "reinitializing the Handle");
      }

      try {
         this.reinitialize();
         this.setHandleOnline(true);
         Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
         eventHelper.fireOnlineEvent(new OnlineEvent(this, System.currentTimeMillis()));
         this.getLogHelper().addLogEntry(2005, ADDED_DEVICE_MSG, "AllDevices", 2);
      } catch (HandleException var2) {
         this.tracer.println(2, "error :" + var2.toString());
         this.tracer.print(var2);
      }

      if (this.tracer.isOn()) {
         this.tracer.println(2, "<--handleOnline");
      }
   }

   protected void handleOffline() {
      this.getHandle().getState().setOnline(false);
      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
      eventHelper.fireOfflineEvent(new OfflineEvent(this, System.currentTimeMillis()));
      this.getLogHelper().addLogEntry(2006, REMOVED_DEVICE_MSG, "AllDevices", 2);
   }

   protected void reinitialize() throws HandleException {
   }

   protected void sioIrpCompleted(SioIrp irp) {
      if (irp != null && irp instanceof SioIrpRecyclable) {
         ((SioIrpRecyclable)irp).recycle();
      }
   }

   protected void errorEventOccurred(SioDeviceErrorEvent event) {
      if (this.getHandle() != null) {
         if (this.tracer.isOn()) {
            this.tracer.print(2, "-->errorEventOcurred()");
            this.tracer.print(2, "event.getErrorCode()" + event.getErrorCode() + " event.Exception :" + event.getSioException());
            this.tracer.print(2, "<--errorEventOcurred()");
         }

         if (event.getErrorCode() == 13) {
            this.communicationError();
         } else if (event.getErrorCode() == 11) {
            this.sioIrpDiscarded();
         } else if (event.getErrorCode() == 12) {
            this.handleOffline();
         } else if (event.getErrorCode() == 14) {
            this.handleOffline();
         }
      }
   }

   protected void sioIrpDiscarded() {
      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
      eventHelper.fireErrorEvent(new ErrorEvent(this, -100));
   }

   protected void communicationError() {
      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
      eventHelper.fireErrorEvent(new ErrorEvent(this, -100));
      this.submitResetCmd();
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

   protected boolean isHandleInit() {
      try {
         if (this.getHandle() != null && this.getHandle().isInit()) {
            return true;
         }
      } catch (HandleException var2) {
      }

      return false;
   }

   private void submitResetCmd() {
      if (this.tracer.isOn()) {
         this.tracer.println(2, "-->submitResetCmd");
         this.tracer.println(2, "Reseting the device due an error");
      }

      try {
         this.submit(this.getHandle().getSystemCmdFactory().createResetCmd());
      } catch (Exception var2) {
         this.tracer.println(2, "error :" + var2.toString());
         this.tracer.print(var2);
      }

      if (this.tracer.isOn()) {
         this.tracer.println(2, "<--submitResetCmd");
      }
   }

   private class AbstractListener implements SioDeviceListener {
      private AbstractListener() {
      }

      public void dataEventOccurred(SioDeviceDataEvent event) {
         AbstractRs485HandleImp.this.dataEventOccurred(event);
      }

      public void errorEventOccurred(SioDeviceErrorEvent event) {
         AbstractRs485HandleImp.this.errorEventOccurred(event);
      }

      public void statusEventOccurred(SioDeviceStatusEvent event) {
         AbstractRs485HandleImp.this.statusEventOccurred(event);
      }
   }
}
