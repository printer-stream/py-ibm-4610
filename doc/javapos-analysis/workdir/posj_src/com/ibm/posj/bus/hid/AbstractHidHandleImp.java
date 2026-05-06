package com.ibm.posj.bus.hid;

import com.ibm.hid.DisconnectEvent;
import com.ibm.hid.HidDevice;
import com.ibm.hid.HidException;
import com.ibm.hid.HidExceptionEvent;
import com.ibm.hid.HidListener;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.jutil.tracing.Tracing;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.AbstractHandleImp;
import com.ibm.posj.bus.FlashHandleImp;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
import com.ibm.posj.flash.FlashException;
import com.ibm.posj.flash.FlashRequest;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;

public abstract class AbstractHidHandleImp extends AbstractHandleImp {
   private HidDevice hidDevice = null;
   private HidListener hidListener = new AbstractHidHandleImp.AbstractHidListener();
   private String serialNumber = "";
   private short vendorID = 0;
   private short productID = 0;
   private short bcdLevel = 0;
   private Tracer tracer = null;
   private static String REATTACHMENT_ERROR_MSG = "An error accurred when reattaching hid device";
   private static String REMOVED_DEVICE_MSG = "hid device removed";
   private static String ADDED_DEVICE_MSG = "hid device added";

   public AbstractHidHandleImp(HandleKey key, HidDevice hidDevice) {
      super(key);
      this.hidDevice = hidDevice;
      this.hidDevice.addHidListener(this.hidListener);
   }

   public DevBus getDevBus() {
      return DevBuses.USB_DEVBUS;
   }

   public String getDeviceSerialNumber() {
      return null;
   }

   public boolean isFlashable() {
      for (HandleImp currHandleImp : this.getHandleImpGroup()) {
         if (currHandleImp instanceof HidFlashHandleImp) {
            if (!this.isComposite()) {
               return true;
            }

            if (this.isCompositeParent()) {
               return true;
            }
         }
      }

      return false;
   }

   public void flash(FlashRequest flashRequest) throws FlashException {
      if (this.isCompositeParent()) {
         this.lockSubDevices();
      }

      Tracing.println("In AbstractHidHandleImp -> flash() - flashfilename:" + flashRequest.getFlashFile().getFilename());
      Tracing.println("In AbstractHidHandleImp -> flash() - deviceECLevel(): " + this.getBCDLevel());
      flashRequest.setDeviceECLevel(this.getBCDLevel());
      Tracing.println("In AbstractHidHandleImp -> preparing to flash " + this.getDevCat().toString());
      this.getFlashHandleImp().setFlashRequest(flashRequest);
      this.getFlashHandleImp().flash();
      if (this.isCompositeParent()) {
         this.unlockSubDevices();
      }
   }

   public void init() throws HandleException {
   }

   public void setHandle(Handle handle) {
      super.setHandle(handle);
   }

   public HidDevice getHidDevice() {
      return this.hidDevice;
   }

   public void setSerialNumber(String sn) {
      this.serialNumber = null == sn ? "" : sn;
   }

   public void setVendorID(short id) {
      this.vendorID = id;
   }

   public void setProductID(short id) {
      this.productID = id;
   }

   public String getSerialNumber() {
      return this.serialNumber;
   }

   public short getVendorID() {
      return this.vendorID;
   }

   public short getProductID() {
      return this.productID;
   }

   public short getBCDLevel() {
      return this.bcdLevel;
   }

   public void setBCDLevel(short bcd) {
      this.bcdLevel = bcd;
   }

   public short getECLevel() {
      return 0;
   }

   public void setECLevel(short ec) {
   }

   public final void reattachHidDevice(HidDevice newHidDevice) throws HandleException {
      try {
         this.hidDevice = newHidDevice;
         newHidDevice.addHidListener(this.hidListener);
         this.reinitialize();
         this.setHandleOnline(true);
         OnlineEvent onlineEvent = new OnlineEvent(this.getHandle(), System.currentTimeMillis());
         this.getHandle().getEventHelper().fireOnlineEvent(onlineEvent);
         this.getLogHelper().addLogEntry(2005, ADDED_DEVICE_MSG, "AllDevices", 2);
      } catch (HandleException var3) {
         this.getLogHelper().addLogEntry(2005, REATTACHMENT_ERROR_MSG, "AllDevices", 5);
         throw var3;
      }
   }

   protected void reinitialize() throws HandleException {
   }

   protected abstract void reportEventOccurred(ReportEvent var1);

   protected void hidExceptionEventOccurred(HidExceptionEvent heE) {
      if (this.getHandle() != null) {
         Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
         eventHelper.fireErrorEvent(new ErrorEvent(this, -100));
      }
   }

   protected void hidDeviceDisconnected(DisconnectEvent dE) {
      try {
         this.getHandle().getState().setOnline(false);
         Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
         eventHelper.fireOfflineEvent(new OfflineEvent(this, System.currentTimeMillis()));
         this.getLogHelper().addLogEntry(2006, REMOVED_DEVICE_MSG, "AllDevices", 2);
         if (this.tracer.isOn()) {
            this.tracer.println("-->hidDeviceDisconnected() : Handle : " + this.getHandle().getName() + ", isOnline = " + this.getHandle().isOnline() + "<--");
         }
      } catch (NullPointerException var3) {
      }
   }

   protected void submitCmd(String name, byte[] cmd) throws HandleException {
      try {
         this.getHidDevice().setReport((byte)2, (byte)0, (short)cmd.length, cmd);
      } catch (HidException var4) {
         throw new HandleException("Error while sending " + name + " to HidDevice", var4);
      }
   }

   public FlashHandleImp getFlashHandleImp() {
      for (HandleImp currHandleImp : this.getHandleImpGroup()) {
         if (currHandleImp instanceof FlashHandleImp) {
            return (FlashHandleImp)currHandleImp;
         }
      }

      return null;
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

   private class AbstractHidListener implements HidListener {
      private AbstractHidListener() {
      }

      public void reportEventOccurred(ReportEvent rE) {
         AbstractHidHandleImp.this.reportEventOccurred(rE);
      }

      public void hidExceptionEventOccurred(HidExceptionEvent heE) {
         AbstractHidHandleImp.this.hidExceptionEventOccurred(heE);
      }

      public void hidDeviceDisconnected(DisconnectEvent dE) {
         AbstractHidHandleImp.this.hidDevice.removeHidListener(this);
         AbstractHidHandleImp.this.hidDeviceDisconnected(dE);
      }
   }
}
