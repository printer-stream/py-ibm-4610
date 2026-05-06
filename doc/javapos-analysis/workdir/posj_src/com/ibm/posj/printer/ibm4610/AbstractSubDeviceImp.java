package com.ibm.posj.printer.ibm4610;

import com.ibm.jutil.logging.LogHelper;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.bus.IBMPrinterComposite;
import com.ibm.posj.bus.PrinterSubDevices;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
import com.ibm.posj.printer.DefaultDeviceInputQueue;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintErrorEvent;

public abstract class AbstractSubDeviceImp implements PrinterSubDevices, PrinterWriter.PrinterWriterUser {
   private DefaultDeviceInputQueue devQ = null;
   private IBMPrinterComposite printerParent = null;
   private HandleKey handleKey = null;
   private HandleImp handleImp = null;
   private boolean isInit = false;
   private LogHelper logHelper = null;
   private Tracer tracer = null;
   private static String REMOVED_DEVICE_MSG = " POSPrinter subdevice removed";
   private static String ADDED_DEVICE_MSG = " POSPrinter subdevice added";

   public AbstractSubDeviceImp(HandleKey key, IBMPrinterComposite printerParent) {
      this.handleKey = key;
      this.printerParent = printerParent;
   }

   public void init() throws HandleException {
      try {
         this.getPrinterParent().init();
      } catch (HandleException var2) {
         this.getHandleImp().getHandle().getState().setOnline(false);
         throw var2;
      }

      this.isInit = true;
   }

   public boolean isInit() {
      return this.isInit;
   }

   public void setHandleImp(HandleImp hi) {
      this.handleImp = hi;
   }

   public HandleImp getHandleImp() {
      return this.handleImp;
   }

   public DefaultDeviceInputQueue getDevQueue() {
      if (null == this.devQ) {
         this.devQ = new DefaultDeviceInputQueue(this, this.getHandleKey().getDevCat().toString());
         this.getPrinterWriter().registerDeviceQueue(this.getDevQueue());
      }

      return this.devQ;
   }

   public void busException(Object e) {
   }

   public HandleKey getHandleKey() {
      return this.handleKey;
   }

   public void offLine(Object eObject) {
      if (null != this.handleImp && null != this.handleImp.getHandle()) {
         if (this.isTracerOn()) {
            this.traceNormal(
               "-->offLine() : Handle : " + this.handleImp.getHandle().getName() + ", OFFLINE = " + this.handleImp.getHandle().getState().isOnline() + "<--"
            );
         }

         if (this.handleImp.getHandle().getState().isOnline()) {
            this.handleImp.getHandle().getState().setOnline(false);
            Handle.EventHelper eventHelper = this.handleImp.getHandle().getEventHelper();
            eventHelper.fireOfflineEvent(new OfflineEvent(this, System.currentTimeMillis()));
            this.getLogHelper().addLogEntry(2006, this.handleImp.getDevCat() + " " + REMOVED_DEVICE_MSG, "AllDevices", 2);
         }
      }
   }

   public void onLine(Object eObject) {
      if (null != this.handleImp && null != this.handleImp.getHandle()) {
         if (this.isTracerOn()) {
            this.traceNormal(
               "-->onLine() : Handle : " + this.handleImp.getHandle().getName() + ", ONLINE = " + this.handleImp.getHandle().getState().isOnline() + "<--"
            );
         }

         if (!this.handleImp.getHandle().getState().isOnline()) {
            this.handleImp.getHandle().getState().setOnline(true);
            OnlineEvent onlineEvent = new OnlineEvent(this.handleImp.getHandle(), System.currentTimeMillis());
            this.handleImp.getHandle().getEventHelper().fireOnlineEvent(onlineEvent);
            this.getLogHelper().addLogEntry(2005, this.handleImp.getDevCat() + " " + ADDED_DEVICE_MSG, "AllDevices", 2);
         }
      }
   }

   public void receivePrintDataEvent(PrintDataEvent pde) {
   }

   public boolean handleImmediateError(PrintCmd err, PrintErrorEvent event) {
      return false;
   }

   protected IBMPrinterComposite getPrinterParent() {
      return this.printerParent;
   }

   protected PrinterWriter getPrinterWriter() {
      return this.getPrinterParent().getWriter();
   }

   protected Handle.EventHelper getEventHelper() {
      return this.handleImp.getHandle().getEventHelper();
   }

   protected IBM4610PrinterCmd.Factory get4610PrinterCmdFactory() {
      return (IBM4610PrinterCmd.Factory)this.getPrinterParent().getPrintCmdFactory();
   }

   protected void submitPreCond(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      }
   }

   protected void submitStatusCmd() {
      this.receivePrintStatus(this.getPrinterWriter().getCurrentStatus());
   }

   protected LogHelper getLogHelper() {
      if (this.logHelper == null) {
         this.logHelper = PosSystemManager.getInstance().getLogHelper();
      }

      return this.logHelper;
   }

   protected Tracer getTracer() {
      if (this.tracer == null) {
         String className = this.getClass().getName();
         className = className.substring(className.lastIndexOf(46) + 1);
         if (this.getHandleImp().getHandle() != null) {
            this.tracer = TracerFactory.getInstance().createTracer(this.getHandleImp().getHandle().getName(), className);
         } else {
            this.tracer = TracerFactory.getInstance().createTracer(this.getHandleImp().getDevCat().toString(), className);
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
         if (this.getHandleImp().getHandle() != null && this.getHandleImp().getHandle().isInit()) {
            return true;
         }
      } catch (HandleException var2) {
      }

      return false;
   }
}
