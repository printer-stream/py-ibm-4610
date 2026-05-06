package com.ibm.posj.printer.ibm4610;

import com.ibm.posj.GeneralPOSPrinterCmd;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.event.PrintStatus;

public class ECCompleteVisitor extends CmdComplete4610StatusVisitor {
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

   public void visitReleasePrintBufferCmd(IBM4610PrinterCmd.ReleasePrintBufferCmd rpcd) {
      this.reset();
      if (this.stats.getStatus(11) || this.stats.getStatus(9)) {
         this.processor.processComplete(rpcd);
         this.succeeded();
      }
   }

   public void visitResetCmd(POSPrinterCmd.ResetCmd rc) {
      this.reset();
      this.processor.processComplete(rc);
      this.succeeded();
   }

   public void visitEraseFlashSectorCmd(POSPrinterCmd.EraseFlashSectorCmd efsc) {
      this.reset();
      if (this.stats.getStatus(896)) {
         this.processor.processComplete(efsc);
         this.succeeded();
      }
   }

   public void visitECLevelRequestCmd(IBM4610PrinterCmd.ECLevelRequestCmd elrc) {
      this.completeECLevelCmd(elrc);
   }

   public void visitDataRequesterCmd(POSPrinterCmd.DataRequesterCmd req) {
      this.reset();
   }

   public void visitStartScanCmd(IBM4610PrinterCmd.StartScanCmd ssc) {
      this.visitDataRequesterCmd(ssc);
   }

   public void visitStoreScannedImageCmd(IBM4610PrinterCmd.StoreScannedImgCmd ssc) {
      if (this.getCurrentStatus().getStatus(896)) {
         this.visit(ssc);
      }
   }

   public void visitDevInfoCmd(POSPrinterCmd.DevInfoCmd dic) {
      this.visitDataRequesterCmd(dic);
   }

   public void visitStatisticCmd(POSPrinterCmd.StatisticCmd cmd) {
      this.visitDataRequesterCmd(cmd);
   }

   public void visitChangePrintSideCmd(IBM4610PrinterCmd.ChangePrintSideCmd cmd) {
      this.reset();
      if (this.stats.getStatus(9)) {
         this.processor.processComplete(cmd);
         this.succeeded();
      }
   }

   private void completeECLevelCmd(POSPrinterCmd ecmd) {
      this.reset();
      if (this.stats.getStatus(1282)) {
         this.processor.processComplete(ecmd);
      }
   }
}
