package com.ibm.posj.bus.rs232;

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
import com.ibm.rs232.Rs232Port;

public class Rs232POSPrinterToneIndicatorHandleImp extends Rs232ToneIndicatorHandleImp implements IBMPOSPrinterToneIndicatorHandleImp {
   private IBMPrinterToneIndicatorImp printerTI = null;
   private static String REMOVED_DEVICE_MSG = " Tone POSPrinter subdevice removed";
   private static String ADDED_DEVICE_MSG = " Tone POSPrinter subdevice added";

   public Rs232POSPrinterToneIndicatorHandleImp(HandleKey key, Rs232Port rs232Port, IBMPrinterToneIndicatorImp printerTI) {
      super(key, rs232Port);
      this.printerTI = printerTI;
   }

   public void offLine(Object eObject) {
      if (null != this.getHandle()) {
         if (this.getHandle().getState().isOnline()) {
            this.getHandle().getState().setOnline(false);
            Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
            eventHelper.fireOfflineEvent(new OfflineEvent(this, System.currentTimeMillis()));
            this.getLogHelper().addLogEntry(2006, REMOVED_DEVICE_MSG, "AllDevices", 2);
         }
      }
   }

   public void onLine(Object eObject) {
      if (null != this.getHandle()) {
         if (!this.getHandle().getState().isOnline()) {
            this.getHandle().getState().setOnline(true);
            OnlineEvent onlineEvent = new OnlineEvent(this.getHandle(), System.currentTimeMillis());
            this.getHandle().getEventHelper().fireOnlineEvent(onlineEvent);
            this.getLogHelper().addLogEntry(2005, ADDED_DEVICE_MSG, "AllDevices", 2);
         }
      }
   }

   public void init() throws HandleException {
      this.getPOSPrinterToneIndicator().init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.getPOSPrinterToneIndicator().submit(cmd);
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

   public void busException(Object e) {
   }

   protected IBMPrinterToneIndicatorImp getPOSPrinterToneIndicator() {
      return this.printerTI;
   }
}
