package com.ibm.posj.util;

import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.IBM4610PrinterCmdVisitor;
import com.ibm.posj.POSPrinterCmd;

public class IBM4610PrinterCmdV extends DefaultPOSPrinterCmdV implements IBM4610PrinterCmdVisitor {
   public void visitECLevelRequestCmd(IBM4610PrinterCmd.ECLevelRequestCmd elrc) {
      this.visit(elrc);
   }

   public void visitReleasePrintBufferCmd(IBM4610PrinterCmd.ReleasePrintBufferCmd rpcd) {
      this.visit(rpcd);
   }

   public void visitPageModeCmd(IBM4610PrinterCmd.PageModeCmd ppm) {
      this.visit(ppm);
   }

   public void visitStartScanCmd(IBM4610PrinterCmd.StartScanCmd ssc) {
      this.visit(ssc);
   }

   public void visitReadSlipCmd(POSPrinterCmd.ReadSlipCmd rsc) {
      this.visitDataRequesterCmd(rsc);
   }

   public void visitStoreScannedImageCmd(IBM4610PrinterCmd.StoreScannedImgCmd ssc) {
      this.visit(ssc);
   }

   public void visitContinuationCmd(IBM4610PrinterCmd.ContinuationCmd cc) {
      this.visit(cc);
   }

   public void visitChangePrintSideCmd(IBM4610PrinterCmd.ChangePrintSideCmd cmd) {
      this.visit(cmd);
   }

   public void visitPageModeNormalCmd(IBM4610PrinterCmd.PMPageModeNormalCmd cmd) {
      this.visit(cmd);
   }

   public void visitPrintPageModePageCmd(IBM4610PrinterCmd.PrintPageModePageCmd cmd) {
      this.visit(cmd);
   }
}
