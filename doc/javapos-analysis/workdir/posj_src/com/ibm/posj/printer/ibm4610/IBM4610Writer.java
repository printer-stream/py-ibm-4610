package com.ibm.posj.printer.ibm4610;

import com.ibm.jutil.SleepPolicy;
import com.ibm.jutil.logging.LogHelper;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.PrinterBusWriter;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.printer.CmdCompleteStatusVisitor;
import com.ibm.posj.printer.CmdLoadedStatusVisitor;
import com.ibm.posj.printer.DefaultPrinterWriter;

public class IBM4610Writer extends DefaultPrinterWriter {
   private CmdCompleteStatusVisitor cvisitor = null;
   private CmdLoadedStatusVisitor lvisitor = null;
   private boolean setLoad = false;
   public static final int RESET_SLEEP = 1400;

   public IBM4610Writer(PrinterBusWriter parent, PrinterPacket packet, LogHelper logger, IBM4610PrinterCmd.Factory factory, boolean useLC) {
      super(parent, packet, logger);
      if (useLC) {
         this.cvisitor = new LineCntCompleteVisitor();
      } else {
         this.cvisitor = new ECCompleteVisitor();
      }

      this.lvisitor = new IBM4610Writer.CmdLoadVisitor4610();
      this.cvisitor.setResponseProcessor(this);
      this.lvisitor.setResponseProcessor(this);
   }

   public void busException(Object e) {
   }

   protected CmdCompleteStatusVisitor getCmdCompleteVisitor() {
      return this.cvisitor;
   }

   protected CmdLoadedStatusVisitor getCmdLoadVisitor() {
      return this.lvisitor;
   }

   public void processSend() {
      super.processSend();
      if (this.getCurrentStatus().getStatus(640)) {
         this.getCmdLoadVisitor().reset();
         this.setLoad = true;
         if (getTracer().isOn()) {
            getTracer().println(">>buffer full not doing sendNext<<");
         }
      }
   }

   public void processComplete(POSPrinterCmd dpc) {
      if (getTracer().isOn()) {
         getTracer().println("^processCOMPLETE??? ");
      }

      super.processComplete(dpc);
      if (!this.getCurrentStatus().getStatus(640) && this.setLoad) {
         this.getCmdLoadVisitor().succeeded();
         if (getTracer().isOn()) {
            getTracer().println(">>buffer no longer full should sendNext<<");
         }

         this.input.sendNextCmd();
         this.setLoad = false;
      }
   }

   protected class CmdLoadVisitor4610 extends CmdLoaded4610StatusVisitor {
      public void visit(POSPrinterCmd cp) {
         if (cp instanceof POSPrinterCmd.TestReqCmd) {
            this.decorateCmd((DefaultPOSPrinterCmd)cp);
            this.succeeded();
            this.processor.processSend();
         } else {
            super.visit(cp);
         }
      }

      public void visitResetCmd(POSPrinterCmd.ResetCmd rc) {
         SleepPolicy.sleep(1400L);
         this.succeeded();
         this.processor.processSend();
      }

      public void visitDevInfoCmd(POSPrinterCmd.DevInfoCmd dic) {
         if (this.stats.getStatus(1281)) {
            this.succeeded();
            this.processor.processSend();
            IBM4610Writer.this.readExtraData();
         }
      }

      public void visitStartScanCmd(IBM4610PrinterCmd.StartScanCmd ssc) {
         if (this.stats.isExtendedData()) {
            if (ssc.isScanAndMicrRead() && this.stats.getStatus(1284)) {
               ssc.clearScanRead();
            }

            if (!ssc.isScanAndMicrRead()) {
               this.succeeded();
               this.processor.processSend();
            }

            IBM4610Writer.this.readExtraData();
         }
      }

      public void visitDataRequesterCmd(POSPrinterCmd.DataRequesterCmd drc) {
         if (this.stats.isExtendedData()) {
            this.succeeded();
            this.processor.processSend();
            IBM4610Writer.this.readExtraData();
         }
      }

      public void decorateCmd(DefaultPOSPrinterCmd cmd) {
      }

      public void visitStatisticCmd(POSPrinterCmd.StatisticCmd cmd) {
         this.visitDataRequesterCmd(cmd);
      }
   }
}
