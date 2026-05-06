package com.ibm.posj.printer.ibm4610;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.CmdLoadedStatusVisitor;
import com.ibm.posj.printer.StatusVisitor;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.IBM4610PrinterCmdV;

public abstract class CmdLoaded4610StatusVisitor extends IBM4610PrinterCmdV implements CmdLoadedStatusVisitor {
   private boolean success = false;
   protected PrintStatus stats = null;
   protected StatusVisitor.ResponseProcessor processor = null;

   public abstract void decorateCmd(DefaultPOSPrinterCmd var1);

   public void setResponseProcessor(StatusVisitor.ResponseProcessor response) {
      this.processor = response;
   }

   public void setCurrentStatus(PrintStatus ps) {
      this.stats = ps;
   }

   public PrintStatus getCurrentStatus() {
      return this.stats;
   }

   public void visit(POSPrinterCmd dpc) {
      PrintStatus ps = this.stats;
      this.reset();
      if (ps.isReady() || null != ps.getSWError()) {
         this.decorateCmd((DefaultPOSPrinterCmd)dpc);
         this.succeeded();
         this.processor.processSend();
      }
   }

   public boolean isSuccessful() {
      return this.success;
   }

   public void reset() {
      this.success = false;
   }

   public void succeeded() {
      this.success = true;
   }
}
