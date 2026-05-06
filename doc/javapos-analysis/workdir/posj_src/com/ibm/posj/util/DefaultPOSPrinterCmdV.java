package com.ibm.posj.util;

import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterCmdVisitor;

public class DefaultPOSPrinterCmdV implements POSPrinterCmdVisitor {
   public Object getData() {
      return null;
   }

   public void reset() {
   }

   public void visit(POSPrinterCmd x) {
   }

   public void visitIDCmd(POSPrinterCmd.IDCmd idCmd) {
      this.visit(idCmd);
   }

   public void visitPrintNormalCmd(POSPrinterCmd.PrintNormalCmd pnc) {
      this.visit(pnc);
   }

   public void visitSelectStationCmd(POSPrinterCmd.SelectStationCmd ssc) {
      this.visit(ssc);
   }

   public void visitDotSpacingCmd(POSPrinterCmd.DotSpacingCmd dsc) {
      this.visit(dsc);
   }

   public void visitFontTypeCmd(POSPrinterCmd.FontTypeCmd ftc) {
      this.visit(ftc);
   }

   public void visitSetBitmapCmd(POSPrinterCmd.SetBitmapCmd setBmpCmd) {
      this.visit(setBmpCmd);
   }

   public void visitSetLogoCmd(POSPrinterCmd.SetLogoCmd setLogoCmd) {
      this.visit(setLogoCmd);
   }

   public void visitResetCmd(POSPrinterCmd.ResetCmd rc) {
      this.visit(rc);
   }

   public void visitDevInfoCmd(POSPrinterCmd.DevInfoCmd dic) {
      this.visit(dic);
   }

   public void visitEraseFlashSectorCmd(POSPrinterCmd.EraseFlashSectorCmd efsc) {
      this.visit(efsc);
   }

   public void visitDataRequesterCmd(POSPrinterCmd.DataRequesterCmd req) {
      this.visit(req);
   }

   public void visitAlignPositionCmd(POSPrinterCmd.AlignPositionCmd algPosCmd) {
      this.visit(algPosCmd);
   }

   public void visitBoldCmd(POSPrinterCmd.BoldCmd boldCmd) {
      this.visit(boldCmd);
   }

   public void visitReverseVideoCmd(POSPrinterCmd.ReverseVideoCmd revVideoCmd) {
      this.visit(revVideoCmd);
   }

   public void visitAlterWideHighCmd(POSPrinterCmd.AlterWideHighCmd alterWHCmd) {
      this.visit(alterWHCmd);
   }

   public void visitScaleFontCmd(POSPrinterCmd.ScaleFontCmd scaleFontCmd) {
      this.visit(scaleFontCmd);
   }

   public void visitNormalModeCmd(POSPrinterCmd.NormalModeCmd normalModeCmd) {
      this.visit(normalModeCmd);
   }

   public void visitUnderlineCmd(POSPrinterCmd.UnderlineCmd underlineCmd) {
      this.visit(underlineCmd);
   }

   public void visitFontColorCmd(POSPrinterCmd.FontColorCmd fontColorCmd) {
      this.visit(fontColorCmd);
   }

   public void visitRotatePrintCmd(POSPrinterCmd.RotatePrintCmd rpc) {
      this.visit(rpc);
   }

   public void visitStatusRequestCmd(POSPrinterCmd.StatusRequestCmd cmd) {
      this.visit(cmd);
   }

   public void visitTestReqCmd(POSPrinterCmd.TestReqCmd cmd) {
      this.visit(cmd);
   }

   public void visitSetLeftMarginCmd(POSPrinterCmd.SetLeftMarginCmd setLeftMarginCmd) {
      this.visit(setLeftMarginCmd);
   }

   public void visitPrintSetLogoCmd(POSPrinterCmd.PrintSetLogoCmd psl) {
      this.visit(psl);
   }

   public void visitCutPaperCmd(POSPrinterCmd.CutPaperCmd cpc) {
      this.visit(cpc);
   }

   public void visitPrintBarCodeCmd(POSPrinterCmd.PrintBarCodeCmd pbc) {
      this.visit(pbc);
   }

   public void visitPrintBitmapCmd(POSPrinterCmd.PrintBitmapCmd pbc) {
      this.visit(pbc);
   }

   public void visitPrintSetBitmapCmd(POSPrinterCmd.PrintSetBitmapCmd psb) {
      this.visit(psb);
   }

   public void visitFontDownloadCmd(POSPrinterCmd.FontDownloadCmd fdc) {
      this.visit(fdc);
   }

   public void visitFeedUnitsCmd(POSPrinterCmd.FeedUnitsCmd fdc) {
      this.visit(fdc);
   }

   public void visitFeedLinesCmd(POSPrinterCmd.FeedLinesCmd flc) {
      this.visit(flc);
   }

   public void visitReadSlipCmd(POSPrinterCmd.ReadSlipCmd rsc) {
      this.visit(rsc);
   }

   public void visitStatisticCmd(POSPrinterCmd.StatisticCmd cmd) {
      this.visit(cmd);
   }
}
