package com.ibm.posj.printer.ibm4689;

import com.ibm.jutil.SleepPolicy;
import com.ibm.jutil.logging.LogHelper;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.GeneralPOSPrinterCmd;
import com.ibm.posj.IBM4689PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.PrinterBusWriter;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.printer.CmdCompleteStatusVisitor;
import com.ibm.posj.printer.CmdLoadedStatusVisitor;
import com.ibm.posj.printer.DefaultPrinterWriter;
import com.ibm.posj.printer.event.IBM4689Status;
import com.ibm.posj.printer.event.PrintStatus;

public class IBM4689Writer extends DefaultPrinterWriter {
   public static final int RESET_SLEEP = 1400;
   private CmdCompleteStatusVisitor cvisitor = null;
   private CmdLoadedStatusVisitor lvisitor = null;
   private IBM4689PrinterCmd.Factory factory;

   public IBM4689Writer(PrinterBusWriter parent, PrinterPacket packet, LogHelper logger, IBM4689PrinterCmd.Factory factory) {
      super(parent, packet, logger);
      this.cvisitor = new IBM4689Writer.CmdCompleteVisitor4689();
      this.lvisitor = new IBM4689Writer.CmdLoadVisitor4689();
      this.cvisitor.setResponseProcessor(this);
      this.lvisitor.setResponseProcessor(this);
      this.factory = factory;
   }

   protected CmdLoadedStatusVisitor getCmdLoadVisitor() {
      return this.lvisitor;
   }

   protected CmdCompleteStatusVisitor getCmdCompleteVisitor() {
      return this.cvisitor;
   }

   public void busException(Object e) {
   }

   private class CmdCompleteVisitor4689 extends CmdComplete4689StatusVisitor {
      boolean idValid = false;
      boolean endRespValid = false;

      private CmdCompleteVisitor4689() {
      }

      public void visitStatusRequestCmd(POSPrinterCmd.StatusRequestCmd cmd) {
         ((GeneralPOSPrinterCmd.StatusRequestCmd)cmd).setStatus((PrintStatus)this.stats.clone());
         this.visit(cmd);
      }

      public void visit(POSPrinterCmd dpc) {
         if (dpc instanceof POSPrinterCmd.TestReqCmd) {
            if (this.getCurrentStatus().getStatus(9)) {
               this.reset();
               this.processor.processComplete(dpc);
               this.succeeded();
            }
         } else {
            this.reset();
            this.processor.processComplete(dpc);
            this.succeeded();
         }
      }

      public void visitTestReqCmd(POSPrinterCmd.TestReqCmd cmd) {
         this.reset();
         if (this.stats.getStatus(11) || this.stats.getStatus(9)) {
            this.processor.processComplete(cmd);
            this.succeeded();
         }
      }

      public void visitResetCmd(POSPrinterCmd.ResetCmd rc) {
         this.reset();
         this.processor.processComplete(rc);
         this.succeeded();
      }

      public void visitECLevelRequestCmd(IBM4689PrinterCmd.ECLevelRequestCmd elrc) {
         elrc.setRequestedData(((IBM4689Status)this.stats).getECBytes());
         this.completeECLevelCmd(elrc);
      }

      public void visitDataRequesterCmd(POSPrinterCmd.DataRequesterCmd req) {
         this.reset();
         this.processor.processComplete(req);
         this.succeeded();
      }

      public void visitStatisticCmd(POSPrinterCmd.StatisticCmd cmd) {
         this.visitDataRequesterCmd(cmd);
      }

      public void visitDevInfoCmd(POSPrinterCmd.DevInfoCmd dic) {
         this.visitDataRequesterCmd(dic);
      }

      private void completeECLevelCmd(POSPrinterCmd ecmd) {
         this.reset();
         if (this.stats.getStatus(514)) {
            this.processor.processComplete(ecmd);
         }
      }

      public void visitIDCmd(POSPrinterCmd ic) {
         IBM4689Status st = (IBM4689Status)this.getCurrentStatus();
         if (st.getStatusID() == ic.getID() || ic.getID() == 0) {
            this.processor.processComplete(ic);
            this.succeeded();
         }
      }

      public void setCurrentStatus(PrintStatus ps) {
         if (ps.getStatus(384)) {
            this.endRespValid = true;
         }

         this.idValid = true;
         super.setCurrentStatus(ps);
      }
   }

   protected class CmdLoadVisitor4689 extends CmdLoaded4689StatusVisitor {
      public void decorateCmd(DefaultPOSPrinterCmd cmd) {
      }

      public void visit(POSPrinterCmd dpc) {
         if (dpc instanceof POSPrinterCmd.TestReqCmd) {
            this.decorateCmd((DefaultPOSPrinterCmd)dpc);
            this.succeeded();
            this.processor.processSend();
         } else {
            super.visit(dpc);
         }
      }

      public void visitResetCmd(POSPrinterCmd.ResetCmd rc) {
         SleepPolicy.sleep(1400L);
         this.succeeded();
         this.processor.processSend();
      }

      public void visitDevInfoCmd(POSPrinterCmd.DevInfoCmd dic) {
         if (this.stats.getStatus(640)) {
            this.succeeded();
            this.processor.processSend();
            IBM4689Writer.this.readExtraData();
         }
      }

      public void visitDataRequesterCmd(POSPrinterCmd.DataRequesterCmd drc) {
         this.succeeded();
         this.processor.processSend();
      }

      public void visitStatisticCmd(POSPrinterCmd.StatisticCmd cmd) {
         this.visitDataRequesterCmd(cmd);
         IBM4689Writer.this.readExtraData();
      }
   }
}
