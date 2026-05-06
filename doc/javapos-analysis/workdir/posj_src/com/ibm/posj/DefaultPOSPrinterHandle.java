package com.ibm.posj;

import com.ibm.jutil.logging.LogHelper;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.bus.POSPrinterHandleImp;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.PrinterTimeStamper;
import java.util.List;

public class DefaultPOSPrinterHandle extends AbstractHandle implements POSPrinterHandle {
   private static int count = 0;
   boolean activity = true;
   POSPrinterCmd.Factory factory = null;
   PrinterTimeStamper timer;

   public DefaultPOSPrinterHandle() {
      this.handleCount = count++;
   }

   public LogHelper getPrinterLogHelper() {
      return this.getLogHelper();
   }

   public void addHandleListener(Handle.Listener listener) {
      if (null != listener) {
         super.addHandleListener(listener);
         if (null != this.getHandleImp() && this.isOnline()) {
            PrinterWriter w = ((POSPrinterHandleImp)this.getHandleImp()).getWriter();
            if (null != w && null != w.getCurrentStatus()) {
               listener.statusEventOccurred(w.getCurrentStatus());
            }
         }
      }
   }

   public void setHandleImp(HandleImp handleImp) throws HandleException {
      super.setHandleImp(handleImp);
      this.timer = new PrinterTimeStamper(handleImp.getDevBus().getName());
   }

   public void clearOutput() {
      ((POSPrinterHandleImp)this.getHandleImp()).clearOutput();
   }

   public PrinterHandleState getPrinterHandleState() {
      return ((POSPrinterHandleImp)this.getHandleImp()).getPrinterHandleState();
   }

   public void submit(DefaultPOSPrinterCmd comm) throws HandleException {
      this.getHandleImp().submit(comm);
   }

   public POSPrinterHandleImp getPOSPrinterHandleImp() {
      return (POSPrinterHandleImp)this.getHandleImp();
   }

   public Handle getMainHandle() {
      return this;
   }

   public List getHandleList() {
      return null;
   }

   public void accept(HandleVisitor visitor) {
      visitor.visitPOSPrinter(this);
   }

   public HandleCmd.Factory getHandleCmdFactory() {
      return this.getPOSPrinterCmdFactory();
   }

   public SystemCmd.Factory getSystemCmdFactory() {
      return (SystemCmd.Factory)this.getPOSPrinterCmdFactory();
   }

   public POSPrinterCmd.Factory getPOSPrinterCmdFactory() {
      if (null == this.factory) {
         this.factory = ((POSPrinterHandleImp)this.getHandleImp()).getPrintCmdFactory();
      }

      return this.factory;
   }

   public boolean isDataPending() {
      return ((POSPrinterHandleImp)this.getHandleImp()).isDataPending();
   }

   public DevCat getDevCat() {
      return DevCats.POSPRINTER_DEVCAT;
   }

   public PrinterTimeStamper timeStamp() {
      return this.timer;
   }

   public boolean isActive() {
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("-->isActive");
      }

      boolean temp = this.activity;
      this.activity = false;
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("<--isActive");
      }

      return temp;
   }

   public void setActive() {
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("-->setActivity<--");
      }

      this.activity = true;
   }
}
