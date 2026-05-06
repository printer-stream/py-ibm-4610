package com.ibm.posj.util;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.IBMSureonePrinterCmd;
import com.ibm.posj.IBMSureonePrinterCmdVisitor;

public class IBMSureonePrinterCmdV extends DefaultPOSPrinterCmdV implements IBMSureonePrinterCmdVisitor {
   public void visitSelectIntCharSetCmd(IBMSureonePrinterCmd.SelectIntCharSetCmd x) {
      this.visit(x);
   }

   public void visitSetXonOffCmd(IBMSureonePrinterCmd.SetXonOffCmd x) {
      this.visit(x);
   }

   public void visitSelectIBMChar1Cmd(IBMSureonePrinterCmd.SelectIBMChar1Cmd x) {
      this.visit(x);
   }

   public void visitSelectIBMChar2Cmd(IBMSureonePrinterCmd.SelectIBMChar2Cmd c) {
      this.visit(c);
   }

   public void visitSelectNormalCharSpaceCmd(IBMSureonePrinterCmd.SelectNormalCharSpaceCmd c) {
      this.visit(c);
   }

   public void visitSelectMediumCharSpaceCmd(IBMSureonePrinterCmd.SelectMediumCharSpaceCmd smcsc) {
      this.visit(smcsc);
   }

   public void visitSelectWideCharSpaceCmd(IBMSureonePrinterCmd.SelectWideCharSpaceCmd swcsc) {
      this.visit(swcsc);
   }

   public void visitSelectXtraWideCharSpaceCmd(IBMSureonePrinterCmd.SelectXtraWideCharSpaceCmd x) {
      this.visit(x);
   }

   public void visitSelect2xCharWidthCmd(IBMSureonePrinterCmd.Select2xCharWidthCmd x) {
      this.visit(x);
   }

   public void visitSetWideModeCmd(IBMSureonePrinterCmd.SetWideModeCmd swmc) {
      this.visit(swmc);
   }

   public void visitSelect2xCharHeightCmd(IBMSureonePrinterCmd.Select2xCharHeightCmd x) {
      this.visit(x);
   }

   public void visitSetHighModeCmd(IBMSureonePrinterCmd.SetHighModeCmd shc) {
      this.visit(shc);
   }

   public void visitSelectBoldModeCmd(IBMSureonePrinterCmd.SelectBoldModeCmd x) {
      this.visit(x);
   }

   public void visitOverlineCmd(IBMSureonePrinterCmd.OverlineCmd x) {
      this.visit(x);
   }

   public void visitLineSpacingCmd(IBMSureonePrinterCmd.LineSpacingCmd x) {
      this.visit(x);
   }

   public void visitSetLineSpaceCmd(IBMSureonePrinterCmd.SetLineSpaceCmd x) {
      this.visit(x);
   }

   public void visitFeedLinesCmd(IBMSureonePrinterCmd.FeedLinesCmd x) {
      this.visit(x);
   }

   public void visitSetTightSpaceCmd(IBMSureonePrinterCmd.SetTightSpaceCmd x) {
      this.visit(x);
   }

   public void visitMicroFeedCmd(IBMSureonePrinterCmd.MicroFeedCmd x) {
      this.visit(x);
   }

   public void visitMicroBackFeedCmd(IBMSureonePrinterCmd.MicroBackFeedCmd x) {
      this.visit(x);
   }

   public void visit_8mmFeedCmd(IBMSureonePrinterCmd._8mmFeedCmd x) {
      this.visit(x);
   }

   public void visitSetCrowdedSpaceCmd(IBMSureonePrinterCmd.SetCrowdedSpaceCmd scsc) {
      this.visit(scsc);
   }

   public void visitFormFeedLengthCmd(IBMSureonePrinterCmd.FormFeedLengthCmd flc) {
      this.visit(flc);
   }

   public void visitSetPageLengthLinesCmd(IBMSureonePrinterCmd.SetPageLengthLinesCmd splc) {
      this.visit(splc);
   }

   public void visitSetPageLengthInchesCmd(IBMSureonePrinterCmd.SetPageLengthInchesCmd spic) {
      this.visit(spic);
   }

   public void visitVerticalTabCmd(IBMSureonePrinterCmd.VerticalTabCmd svtc) {
      this.visit(svtc);
   }

   public void visitVerticalTabPosCmd(IBMSureonePrinterCmd.VerticalTabPosCmd vtpc) {
      this.visit(vtpc);
   }

   public void visitSetBottomMarginCmd(IBMSureonePrinterCmd.SetBottomMarginCmd bmc) {
      this.visit(bmc);
   }

   public void visitSetLeftMarginCmd(IBMSureonePrinterCmd.SetLeftMarginCmd lmc) {
      this.visit(lmc);
   }

   public void visitSetRightMarginCmd(IBMSureonePrinterCmd.SetRightMarginCmd rmc) {
      this.visit(rmc);
   }

   public void visitHorizontalTabCmd(IBMSureonePrinterCmd.HorizontalTabCmd htc) {
      this.visit(htc);
   }

   public void visitHorizontalTabPosCmd(IBMSureonePrinterCmd.HorizontalTabPosCmd htpc) {
      this.visit(htpc);
   }

   public void visitNormalDensityCmd(IBMSureonePrinterCmd.NormalDensityCmd ndc) {
      this.visit(ndc);
   }

   public void visitHighDensityCmd(IBMSureonePrinterCmd.HighDensityCmd hdc) {
      this.visit(hdc);
   }

   public void visitFineDensityBitCmd(IBMSureonePrinterCmd.FineDensityBitCmd fdc) {
      this.visit(fdc);
   }

   public void visitFineDensityCmd(IBMSureonePrinterCmd.FineDensityCmd fdc) {
      this.visit(fdc);
   }

   public void visitEnableDownloadCharCmd(IBMSureonePrinterCmd.EnableDownloadCharCmd edcc) {
      this.visit(edcc);
   }

   public void visitDisableDownloadCharCmd(IBMSureonePrinterCmd.DisableDownloadCharCmd ddcc) {
      this.visit(ddcc);
   }

   public void visitDefineDownloadCharCmd(IBMSureonePrinterCmd.DefineDownloadCharCmd ddcc) {
      this.visit(ddcc);
   }

   public void visitDeleteDownloadCharCmd(IBMSureonePrinterCmd.DeleteDownloadCharCmd ddcc) {
      this.visit(ddcc);
   }

   public void visitImmediateDriveCmd(IBMSureonePrinterCmd.ImmediateDriveCmd idc) {
      this.visit(idc);
   }

   public void visitBeeperCmd(IBMSureonePrinterCmd.BeeperCmd bc) {
      this.visit(bc);
   }

   public void visitCancelPrintCmd(IBMSureonePrinterCmd.CancelPrintCmd cpc) {
      this.visit(cpc);
   }

   public void visitReinitPrinterCmd(IBMSureonePrinterCmd.ReinitPrinterCmd ipc) {
      this.visit(ipc);
   }

   public void visitInitPrinterCmd(IBMSureonePrinterCmd.InitPrinterCmd ipc) {
      this.visit(ipc);
   }

   public void visitVerticalColAlignCmd(IBMSureonePrinterCmd.VerticalColAlignCmd vcac) {
      this.visit(vcac);
   }

   public void visitPrintDensityCmd(IBMSureonePrinterCmd.PrintDensityCmd vpdc) {
      this.visit(vpdc);
   }

   public void visitCutPaperCmd(DefaultPOSPrinterCmd.CutPaperCmd cpc) {
      this.visit(cpc);
   }

   public void visitSelectHighligthCmd(IBMSureonePrinterCmd.SelectHighligthCmd shlc) {
      this.visit(shlc);
   }

   public void visitStatusRequestCmd(IBMSureonePrinterCmd.StatusRequestCmd ipc) {
      this.visit(ipc);
   }
}
