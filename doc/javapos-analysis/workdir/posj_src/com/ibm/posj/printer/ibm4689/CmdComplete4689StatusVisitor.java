package com.ibm.posj.printer.ibm4689;

import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.CmdCompleteStatusVisitor;
import com.ibm.posj.printer.StatusVisitor;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.IBM4689PrinterCmdV;

public class CmdComplete4689StatusVisitor extends IBM4689PrinterCmdV implements CmdCompleteStatusVisitor {
   private boolean success = false;
   protected PrintStatus stats = null;
   protected StatusVisitor.ResponseProcessor processor = null;
   private byte cache = 0;

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
      this.processor.processComplete(dpc);
      this.succeeded();
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

   public void setCache(byte c) {
      this.cache = c;
   }

   public byte getCache() {
      return this.cache;
   }
}
