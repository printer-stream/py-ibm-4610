package com.ibm.posj.printer.sureone;

import com.ibm.jutil.logging.LogHelper;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.PrinterBusWriter;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.printer.CmdCompleteStatusVisitor;
import com.ibm.posj.printer.CmdLoadedStatusVisitor;
import com.ibm.posj.printer.DefaultPrinterWriter;
import com.ibm.posj.printer.StatusVisitor;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.IBMSureonePrinterCmdV;

public class IBMSureoneWriter extends DefaultPrinterWriter {
   private CmdCompleteStatusVisitor cvisitor = null;
   private CmdLoadedStatusVisitor lvisitor = null;

   public IBMSureoneWriter(PrinterBusWriter parent, PrinterPacket packet, LogHelper logger, POSPrinterCmd.Factory factory) {
      super(parent, packet, logger);
      this.cvisitor = new IBMSureoneWriter.CmdCompleteVisitorSO();
      this.lvisitor = new IBMSureoneWriter.CmdLoadVisitorSO();
      this.cvisitor.setResponseProcessor(this);
      this.lvisitor.setResponseProcessor(this);
   }

   protected CmdLoadedStatusVisitor getCmdLoadVisitor() {
      return this.lvisitor;
   }

   protected CmdCompleteStatusVisitor getCmdCompleteVisitor() {
      return this.cvisitor;
   }

   public void busException(Object e) {
   }

   private class CmdCompleteVisitorSO extends IBMSureonePrinterCmdV implements CmdCompleteStatusVisitor {
      private boolean success = false;
      protected PrintStatus stats = null;
      protected StatusVisitor.ResponseProcessor processor = null;
      private byte cache = 0;

      private CmdCompleteVisitorSO() {
      }

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
         this.reset();
         if (this.stats.checkCmdComplete()) {
            this.processor.processComplete(dpc);
            this.succeeded();
         }
      }

      public void visitPrintNormalCmd(POSPrinterCmd.PrintNormalCmd pnc) {
         if (pnc.isLineCompleted() && pnc.getOwnerCmd().isOutputCmd()) {
            this.visit(pnc);
         } else {
            this.reset();
            this.processor.processComplete(pnc);
            this.succeeded();
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

      public void setCache(byte c) {
         this.cache = c;
      }

      public byte getCache() {
         return this.cache;
      }
   }

   protected class CmdLoadVisitorSO extends IBMSureonePrinterCmdV implements CmdLoadedStatusVisitor {
      private boolean success = false;
      protected PrintStatus stats = null;
      protected StatusVisitor.ResponseProcessor processor = null;

      public void decorateCmd(DefaultPOSPrinterCmd cmd) {
      }

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
         this.decorateCmd((DefaultPOSPrinterCmd)dpc);
         this.succeeded();
         this.processor.processSend();
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
}
