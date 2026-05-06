package com.ibm.posj.printer.ibm4610;

import com.ibm.jutil.AsyncBitSet;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.IBM4610PrinterCmdVisitor;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.FilterCmdVisitor;
import com.ibm.posj.util.DefaultPOSPrinterCmdV;

public class IBM4610CmdFilterV extends DefaultPOSPrinterCmdV implements FilterCmdVisitor, IBM4610PrinterCmdVisitor {
   private boolean valid = true;
   private AsyncBitSet settings = new AsyncBitSet(9);
   private static final int WIDE_HIGH = 6;
   private static final int BOLD = 5;
   private static final int UNDERLINE = 4;
   private static final int REVERSE_VIDEO = 3;
   private static final int SCALING = 2;
   private static final int ALIGNMENT = 1;
   private static final int COLOR = 0;

   public void reset() {
      this.valid = true;
      this.settings.setAll();
   }

   public boolean isValidNormalorOtherCmd() {
      return this.valid;
   }

   public void visit(POSPrinterCmd c) {
      this.valid = true;
   }

   public void visitBoldCmd(POSPrinterCmd.BoldCmd boldCmd) {
      this.settings.clear(5);
      if (boldCmd.isActive()) {
         this.settings.set(5);
      }

      this.valid = true;
   }

   public void visitUnderlineCmd(POSPrinterCmd.UnderlineCmd underlineCmd) {
      this.settings.clear(4);
      if (underlineCmd.getThickness() >= 1) {
         this.settings.set(4);
      }

      this.valid = true;
   }

   public void visitReverseVideoCmd(POSPrinterCmd.ReverseVideoCmd revc) {
      this.settings.clear(3);
      if (revc.isActive()) {
         this.settings.set(3);
      }

      this.valid = true;
   }

   public void visitAlterWideHighCmd(POSPrinterCmd.AlterWideHighCmd alterWHCmd) {
      this.settings.clear(6);
      if (1 != alterWHCmd.getOption()) {
         this.settings.set(6);
      }

      this.valid = true;
   }

   public void visitScaleFontCmd(POSPrinterCmd.ScaleFontCmd scaleFontCmd) {
      this.settings.clear(2);
      int t = scaleFontCmd.getHeight() + scaleFontCmd.getWidth();
      if (t > 0) {
         this.settings.set(2);
      }

      this.valid = true;
   }

   public void visitAlignPositionCmd(POSPrinterCmd.AlignPositionCmd align) {
      this.settings.clear(1);
      if (align.getOption() != 0) {
         this.settings.set(1);
      }

      this.valid = true;
   }

   public void visitFontColorCmd(POSPrinterCmd.FontColorCmd fontc) {
      this.settings.clear(0);
      if (fontc.getFontColor() != 0) {
         this.settings.set(0);
      }

      this.valid = true;
   }

   public void visitNormalModeCmd(POSPrinterCmd.NormalModeCmd nmc) {
      if (this.valid && this.settings.get(5)) {
         nmc.setBoldOn(true);
      }

      if (this.valid && this.settings.get(4)) {
         nmc.setUnderlineOn(true);
      }

      if (this.valid && this.settings.get(3)) {
         nmc.setReverseVideoOn(true);
      }

      if (this.valid && this.settings.get(6)) {
         nmc.setDoubleWideHighOn(true);
      }

      if (this.valid && this.settings.get(2)) {
         nmc.setScaleOn(true);
      }

      if (this.valid && this.settings.get(1)) {
         nmc.setAlignmentOn(true);
      }

      if (this.valid && this.settings.get(0)) {
         nmc.setColorOn(true);
      }

      this.valid = false;
      this.settings.clearAll();
   }

   public void visitECLevelRequestCmd(IBM4610PrinterCmd.ECLevelRequestCmd elrc) {
   }

   public void visitReleasePrintBufferCmd(IBM4610PrinterCmd.ReleasePrintBufferCmd rpcd) {
   }

   public void visitPageModeCmd(IBM4610PrinterCmd.PageModeCmd pmc) {
   }

   public void visitStartScanCmd(IBM4610PrinterCmd.StartScanCmd ssc) {
   }

   public void visitStoreScannedImageCmd(IBM4610PrinterCmd.StoreScannedImgCmd ssc) {
   }

   public void visitContinuationCmd(IBM4610PrinterCmd.ContinuationCmd cc) {
   }

   public void visitChangePrintSideCmd(IBM4610PrinterCmd.ChangePrintSideCmd cmd) {
   }

   public void visitPageModeNormalCmd(IBM4610PrinterCmd.PMPageModeNormalCmd cmd) {
   }

   public void visitPrintPageModePageCmd(IBM4610PrinterCmd.PrintPageModePageCmd cmd) {
   }
}
