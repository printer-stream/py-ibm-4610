package com.ibm.posj.bus.rs232;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.AbstractHandleImp;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.rs232.DefaultRs232PortListener;
import com.ibm.rs232.Rs232Exception;
import com.ibm.rs232.Rs232Port;
import com.ibm.rs232.event.Rs232ControlLineEvent;
import com.ibm.rs232.event.Rs232DataEvent;
import com.ibm.rs232.event.Rs232ErrorEvent;
import com.ibm.rs232.event.Rs232OfflineEvent;
import com.ibm.rs232.event.Rs232OnlineEvent;

public abstract class AbstractRs232HandleImp extends AbstractHandleImp {
   private Rs232Port rs232Port = null;
   private Rs232Port.Listener listener = new AbstractRs232HandleImp.Rs232PortListener();
   private Tracer tracer = null;
   public static final int EC_LEVEL_NOT_SUPPORTED = -1;
   private static String REMOVED_DEVICE_MSG = "RS232 device removed";
   private static String ADDED_DEVICE_MSG = "RS232 device added";

   public AbstractRs232HandleImp(HandleKey key, Rs232Port rs232Port) {
      super(key);
      this.rs232Port = rs232Port;
   }

   public String getDeviceSerialNumber() {
      return null;
   }

   public synchronized void init() throws HandleException {
      try {
         this.getRs232Port().open(this.getHandle().getName());
      } catch (Rs232Exception var2) {
         this.setHandleOnline(false);
         throw new HandleException("Error at initialization : " + var2.toString(), var2);
      }

      this.getRs232Port().addRs232PortListener(this.listener);
   }

   public DevBus getDevBus() {
      return DevBuses.RS232_DEVBUS;
   }

   public void setHandle(Handle handle) {
      super.setHandle(handle);
   }

   protected void submit(byte[] data) throws HandleException {
      try {
         this.getRs232Port().submit(data);
      } catch (Rs232Exception var3) {
         throw new HandleException("Error when submiting data to the Rs232Port", var3);
      }
   }

   protected void submit(byte[] data, int offset, int length) throws HandleException {
      try {
         this.getRs232Port().submit(data, offset, length);
      } catch (Rs232Exception var5) {
         throw new HandleException("Error when submiting data to the Rs232Port", var5);
      }
   }

   protected void dataEventOccurred(Rs232DataEvent event) {
   }

   protected void errorEventOccurred(Rs232ErrorEvent event) {
   }

   protected void onlineEventOccurred(Rs232OnlineEvent rs232OEvent) {
      if (this.tracer.isOn()) {
         this.tracer.println(2, "-->onlineEventOccurred");
         this.tracer.println(2, "reinitializing the Handle");
      }

      try {
         this.reinitialize();
         this.setHandleOnline(true);
         Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
         eventHelper.fireOnlineEvent(new OnlineEvent(this, System.currentTimeMillis()));
         this.getLogHelper().addLogEntry(2005, ADDED_DEVICE_MSG, "AllDevices", 2);
      } catch (HandleException var3) {
         this.tracer.println(2, "error :" + var3.toString());
         this.tracer.print(var3);
      }

      if (this.tracer.isOn()) {
         this.tracer.println(2, "<--onlineEventOccurred");
      }
   }

   protected void controlLineEventOccurred(Rs232ControlLineEvent event) {
   }

   protected void reinitialize() throws HandleException {
   }

   protected void offlineEventOccurred(Rs232OfflineEvent rs232OEvent) {
      this.getHandle().getState().setOnline(false);
      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
      eventHelper.fireOfflineEvent(new OfflineEvent(this, System.currentTimeMillis()));
      this.getLogHelper().addLogEntry(2006, REMOVED_DEVICE_MSG, "AllDevices", 2);
   }

   protected Rs232Port getRs232Port() {
      return this.rs232Port;
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

   protected class Rs232PortListener extends DefaultRs232PortListener {
      public void dataEventOccurred(Rs232DataEvent rs232DataEvent) {
         AbstractRs232HandleImp.this.dataEventOccurred(rs232DataEvent);
      }

      public void errorEventOccurred(Rs232ErrorEvent rs232ErrorEvent) {
         AbstractRs232HandleImp.this.errorEventOccurred(rs232ErrorEvent);
      }

      public void onlineEventOccurred(Rs232OnlineEvent rs232OEvent) {
         AbstractRs232HandleImp.this.onlineEventOccurred(rs232OEvent);
      }

      public void offlineEventOccurred(Rs232OfflineEvent rs232OEvent) {
         AbstractRs232HandleImp.this.offlineEventOccurred(rs232OEvent);
      }

      public void controlLineEventOccurred(Rs232ControlLineEvent rs232ControlLineEvent) {
         AbstractRs232HandleImp.this.controlLineEventOccurred(rs232ControlLineEvent);
      }
   }
}
