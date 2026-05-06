package com.ibm.posj.printer.ibm4610;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.GeneralPOSPrinterCmd;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.P4610Status;
import com.ibm.posj.printer.event.PrintStatus;

public class LineCntCompleteVisitor extends CmdComplete4610StatusVisitor {
   int stlnc = 0;
   int reCount = -1;
   int completers = 0;
   boolean ecValid = true;

   public void visitContinuationCmd(IBM4610PrinterCmd.ContinuationCmd cc) {
      if (cc.isOutputCmd()) {
         this.completeLineCntCmd(cc);
      } else {
         super.visitContinuationCmd(cc);
      }
   }

   public void visitCutPaperCmd(POSPrinterCmd.CutPaperCmd cpc) {
      this.completeLineCntCmd(cpc);
   }

   public void visitPrintBarCodeCmd(POSPrinterCmd.PrintBarCodeCmd pbc) {
      this.completeLineCntCmd(pbc);
   }

   public void visitFeedUnitsCmd(POSPrinterCmd.FeedUnitsCmd fdc) {
      this.completeLineCntCmd(fdc);
   }

   public void visitFeedLinesCmd(POSPrinterCmd.FeedLinesCmd flc) {
      if (flc.getLines() != 0) {
         this.completeLineCntCmd(flc);
      } else {
         this.visit(flc);
      }
   }

   public void visitPrintSetLogoCmd(POSPrinterCmd.PrintSetLogoCmd slc) {
      this.reset();
      if (this.stats.getStatus(1282) && this.ecValid) {
         this.stlnc = ((P4610Status)this.getCurrentStatus()).getLineCnt();
         this.processor.processComplete(slc);
         this.succeeded();
      }
   }

   public void visitPrintBitmapCmd(POSPrinterCmd.PrintBitmapCmd pbc) {
      this.completeLineCntCmd(pbc);
   }

   public void visitPrintSetBitmapCmd(POSPrinterCmd.PrintSetBitmapCmd psb) {
      this.completeLineCntCmd(psb);
   }

   public void visitPrintNormalCmd(POSPrinterCmd.PrintNormalCmd pnc) {
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println(3, "--->>>>LineCntCompleteVisitor.visitPrintNormalCmd(" + pnc + ")");
         PrinterWriter.getTracer().println(3, "pnc.isLineCompleted()-->" + pnc.isLineCompleted());
         PrinterWriter.getTracer().println(3, "pnc.getOwnerCmd().isOutputCmd()-->" + pnc.getOwnerCmd().isOutputCmd());
      }

      if (pnc.isLineCompleted() && pnc.getOwnerCmd().isOutputCmd()) {
         this.completeLineCntCmd(pnc);
      } else {
         this.visit(pnc);
      }

      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println(3, "<<<<---LineCntCompleteVisitor.visitPrintNormalCmd(" + pnc + ")");
      }
   }

   public void visitStatusRequestCmd(POSPrinterCmd.StatusRequestCmd cmd) {
      ((GeneralPOSPrinterCmd.StatusRequestCmd)cmd).setStatus((PrintStatus)this.stats.clone());
      this.visit(cmd);
   }

   public void visit(POSPrinterCmd dpc) {
      this.reset();
      this.processor.processComplete(dpc);
      this.succeeded();
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
         this.stlnc = ((P4610Status)this.getCurrentStatus()).getLineCnt();
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

   public void visitFontDownloadCmd(POSPrinterCmd.FontDownloadCmd fdc) {
      if (this.stats.getStatus(9) || this.stats.getStatus(11)) {
         this.visit(fdc);
      }
   }

   public void visitECLevelRequestCmd(IBM4610PrinterCmd.ECLevelRequestCmd elrc) {
      this.completeECLevelCmd(elrc);
   }

   public void visitStoreScannedImageCmd(IBM4610PrinterCmd.StoreScannedImgCmd ssc) {
      if (this.getCurrentStatus().getStatus(896)) {
         this.visit(ssc);
      }
   }

   public void visitStartScanCmd(IBM4610PrinterCmd.StartScanCmd ssc) {
      this.visitDataRequesterCmd(ssc);
   }

   public void visitDataRequesterCmd(POSPrinterCmd.DataRequesterCmd req) {
      this.reset();
   }

   public void visitDevInfoCmd(POSPrinterCmd.DevInfoCmd dic) {
      this.visitDataRequesterCmd(dic);
   }

   public void visitStatisticCmd(POSPrinterCmd.StatisticCmd cmd) {
      this.visitDataRequesterCmd(cmd);
   }

   public void visitChangePrintSideCmd(IBM4610PrinterCmd.ChangePrintSideCmd cmd) {
      this.completeLineCntCmd(cmd);
   }

   private void completeECLevelCmd(POSPrinterCmd ecmd) {
      this.reset();
      if (this.stats.getStatus(1282) && this.ecValid) {
         this.ecValid = false;
         this.processor.processComplete(ecmd);
         this.completers--;
         if (this.completers > 0) {
            this.succeeded();
         }
      }
   }

   private void completeLineCntCmd(POSPrinterCmd pnc) {
      DefaultPOSPrinterCmd dpnc = (DefaultPOSPrinterCmd)pnc;
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println(2, "-->>completeLineCntCmd(" + pnc + ")");
      }

      if (!dpnc.isCompleted() && !dpnc.isPartiallyComplete()) {
         if (pnc.getOwnerCmd() instanceof IBM4610PrinterCmd.PageModeCmd) {
            this.processor.processComplete(pnc);
            this.succeeded();
            this.completers--;
         } else {
            this.reset();
            int clnc = ((P4610Status)this.getCurrentStatus()).getLineCnt();
            if (PrinterWriter.getTracer().isOn()) {
               PrinterWriter.getTracer().println(2, "stored " + this.stlnc + " clnc " + clnc);
            }

            if (this.stlnc != clnc) {
               this.processor.processComplete(pnc);
               if (this.stlnc < 255) {
                  this.stlnc++;
               } else {
                  this.stlnc = 0;
               }

               if (this.stlnc != clnc) {
                  this.succeeded();
               } else {
                  this.completers--;
                  if (this.completers > 0) {
                     this.succeeded();
                  }
               }
            }

            if (PrinterWriter.getTracer().isOn()) {
               PrinterWriter.getTracer().println(2, "<<--completeLineCntCmd(" + pnc + ")");
            }
         }
      }
   }

   public void setCurrentStatus(PrintStatus ps) {
      this.ecValid = true;
      this.completers = 0;
      if (((P4610Status)ps).getLineCnt() != this.stlnc) {
         this.completers++;
      }

      if (ps.getStatus(1282)) {
         this.completers++;
      }

      if (ps.getStatus(9) || ps.getStatus(11)) {
         this.completers++;
      }

      super.setCurrentStatus(ps);
   }
}
