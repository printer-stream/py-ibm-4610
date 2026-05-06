package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.ReportEvent;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.IBMPOSPrinterToneIndicatorHandleImp;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
import com.ibm.posj.printer.IBMPrinterToneIndicatorImp;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public class HidPOSPrinterToneIndicatorHandleImp extends AbstractHidHandleImp implements IBMPOSPrinterToneIndicatorHandleImp, HidHandleImp {
   private IBMPrinterToneIndicatorImp printerTI = null;
   private static String REMOVED_DEVICE_MSG = " POSPrinter subdevice removed";
   private static String ADDED_DEVICE_MSG = " POSPrinter subdevice added";

   public HidPOSPrinterToneIndicatorHandleImp(HandleKey key, HidDevice hidDevice, IBMPrinterToneIndicatorImp printerTI) {
      super(key, hidDevice);
      this.printerTI = printerTI;
   }

   public void offLine(Object eObject) {
      if (this.handle.getHandleImp().getHandle() != null) {
         if (this.isTracerOn()) {
            this.traceNormal("-->offLine() : Handle : " + this.getHandle().getName() + ", OFFLINE = " + this.getHandle().isOnline() + "<--");
         }

         if (this.handle.getHandleImp().getHandle().getState().isOnline()) {
            this.handle.getHandleImp().getHandle().getState().setOnline(false);
            Handle.EventHelper eventHelper = this.handle.getHandleImp().getHandle().getEventHelper();
            eventHelper.fireOfflineEvent(new OfflineEvent(this, System.currentTimeMillis()));
            this.getLogHelper().addLogEntry(2006, this.handle.getHandleImp().getHandle().getDevCat() + " " + REMOVED_DEVICE_MSG, "AllDevices", 2);
         }
      }
   }

   public void onLine(Object eObject) {
      if (this.handle.getHandleImp().getHandle() != null) {
         if (this.isTracerOn()) {
            this.traceNormal("-->onLine() : Handle : " + this.getHandle().getName() + ", ONLINE = " + this.getHandle().isOnline() + "<--");
         }

         if (!this.handle.getHandleImp().getHandle().getState().isOnline()) {
            this.handle.getHandleImp().getHandle().getState().setOnline(true);
            OnlineEvent onlineEvent = new OnlineEvent(this.handle.getHandleImp().getHandle(), System.currentTimeMillis());
            this.handle.getHandleImp().getHandle().getEventHelper().fireOnlineEvent(onlineEvent);
            this.getLogHelper().addLogEntry(2005, this.handle.getHandleImp().getHandle().getDevCat() + " " + ADDED_DEVICE_MSG, "AllDevices", 2);
         }
      }
   }

   public void init() throws HandleException {
      this.getPOSPrinterToneIndicator().init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.getPOSPrinterToneIndicator().submit(cmd);
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidPOSPrinterToneIndicatorHandleImp(this);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitToneIndicator(this);
   }

   public short getECLevel() {
      return -1;
   }

   public boolean isFlashable() {
      return false;
   }

   public void receivePrintStatus(PrintStatus ps) {
   }

   public void receivePrintDataEvent(PrintDataEvent pde) {
   }

   public boolean isComposite() {
      return true;
   }

   public DevCat getDevCat() {
      return DevCats.TONEINDICATOR_DEVCAT;
   }

   protected void reportEventOccurred(ReportEvent rE) {
   }

   public void busException(Object e) {
   }

   protected IBMPrinterToneIndicatorImp getPOSPrinterToneIndicator() {
      return this.printerTI;
   }
}
