package com.ibm.posj.printer;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.DefaultPOSPrinterCmdV;

public abstract class DefCmdLoadedStatusVisitor extends DefaultPOSPrinterCmdV implements CmdLoadedStatusVisitor {
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
      if (ps.getStatus(11) || !ps.isHardwareError() && ps.checkErrors()) {
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
