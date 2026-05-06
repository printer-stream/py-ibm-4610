package com.ibm.posj.printer.sureone;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.IBMSureonePrinterCmd;
import com.ibm.posj.IBMSureonePrinterCmdVisitor;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.AbstractCmdCompleteVisitor;
import com.ibm.posj.printer.IBMPrinterState;

public class CmdCompleteSOVisitor extends AbstractCmdCompleteVisitor implements IBMSureonePrinterCmdVisitor {
   private IBMPrinterState state;

   public CmdCompleteSOVisitor(IBMPrinterState state) {
      this.state = state;
   }

   public void visitDevInfoCmd(POSPrinterCmd.DevInfoCmd dic) {
      dic.setDeviceId(this.state.getPrinterID());
      dic.setDeviceType(this.state.getPrinterType());
      dic.setSerialNumber(this.state.getSerialNumber());
      dic.setFirmwareLevel(this.state.getPrinterEC());
   }

   public void visitSetLogoCmd(DefaultPOSPrinterCmd.SetLogoCmd setLogoCmd) {
      this.state.setSetLogoCmd(setLogoCmd);
      this.setStateChanged(true);
   }

   public void visitPrintSetLogoCmd(DefaultPOSPrinterCmd.PrintSetLogoCmd printSetLogoCmd) {
   }

   public void visitSelectIntCharSetCmd(IBMSureonePrinterCmd.SelectIntCharSetCmd sicsc) {
   }

   public void visitSetXonOffCmd(IBMSureonePrinterCmd.SetXonOffCmd sxofc) {
   }

   public void visitSelectIBMChar1Cmd(IBMSureonePrinterCmd.SelectIBMChar1Cmd sibm1c) {
   }

   public void visitSelectIBMChar2Cmd(IBMSureonePrinterCmd.SelectIBMChar2Cmd sibm2c) {
   }

   public void visitSelectNormalCharSpaceCmd(IBMSureonePrinterCmd.SelectNormalCharSpaceCmd sncsc) {
   }

   public void visitSelectMediumCharSpaceCmd(IBMSureonePrinterCmd.SelectMediumCharSpaceCmd smcsc) {
   }

   public void visitSelectWideCharSpaceCmd(IBMSureonePrinterCmd.SelectWideCharSpaceCmd swcsc) {
   }

   public void visitSelectXtraWideCharSpaceCmd(IBMSureonePrinterCmd.SelectXtraWideCharSpaceCmd sxwcsc) {
   }

   public void visitSelect2xCharWidthCmd(IBMSureonePrinterCmd.Select2xCharWidthCmd s2xwcsc) {
   }

   public void visitSetWideModeCmd(IBMSureonePrinterCmd.SetWideModeCmd swmc) {
   }

   public void visitSelect2xCharHeightCmd(IBMSureonePrinterCmd.Select2xCharHeightCmd s2hc) {
   }

   public void visitSetHighModeCmd(IBMSureonePrinterCmd.SetHighModeCmd shc) {
   }

   public void visitSelectBoldModeCmd(IBMSureonePrinterCmd.SelectBoldModeCmd sbc) {
   }

   public void visitOverlineCmd(IBMSureonePrinterCmd.OverlineCmd olc) {
   }

   public void visitSelectHighligthCmd(IBMSureonePrinterCmd.SelectHighligthCmd shlc) {
   }

   public void visitLineSpacingCmd(IBMSureonePrinterCmd.LineSpacingCmd lsc) {
   }

   public void visitSetLineSpaceCmd(IBMSureonePrinterCmd.SetLineSpaceCmd slsc) {
   }

   public void visitFeedLinesCmd(IBMSureonePrinterCmd.FeedLinesCmd flc) {
   }

   public void visitSetTightSpaceCmd(IBMSureonePrinterCmd.SetTightSpaceCmd stsc) {
   }

   public void visitMicroFeedCmd(IBMSureonePrinterCmd.MicroFeedCmd mfc) {
   }

   public void visitMicroBackFeedCmd(IBMSureonePrinterCmd.MicroBackFeedCmd mbfc) {
   }

   public void visit_8mmFeedCmd(IBMSureonePrinterCmd._8mmFeedCmd fc) {
   }

   public void visitSetCrowdedSpaceCmd(IBMSureonePrinterCmd.SetCrowdedSpaceCmd scsc) {
   }

   public void visitFormFeedLengthCmd(IBMSureonePrinterCmd.FormFeedLengthCmd flc) {
   }

   public void visitSetPageLengthLinesCmd(IBMSureonePrinterCmd.SetPageLengthLinesCmd splc) {
   }

   public void visitSetPageLengthInchesCmd(IBMSureonePrinterCmd.SetPageLengthInchesCmd spic) {
   }

   public void visitVerticalTabCmd(IBMSureonePrinterCmd.VerticalTabCmd svtc) {
   }

   public void visitVerticalTabPosCmd(IBMSureonePrinterCmd.VerticalTabPosCmd vtpc) {
   }

   public void visitSetBottomMarginCmd(IBMSureonePrinterCmd.SetBottomMarginCmd bmc) {
   }

   public void visitSetLeftMarginCmd(IBMSureonePrinterCmd.SetLeftMarginCmd lmc) {
   }

   public void visitSetRightMarginCmd(IBMSureonePrinterCmd.SetRightMarginCmd rmc) {
   }

   public void visitHorizontalTabCmd(IBMSureonePrinterCmd.HorizontalTabCmd htc) {
   }

   public void visitHorizontalTabPosCmd(IBMSureonePrinterCmd.HorizontalTabPosCmd htpc) {
   }

   public void visitNormalDensityCmd(IBMSureonePrinterCmd.NormalDensityCmd ndc) {
   }

   public void visitHighDensityCmd(IBMSureonePrinterCmd.HighDensityCmd hdc) {
   }

   public void visitFineDensityBitCmd(IBMSureonePrinterCmd.FineDensityBitCmd fdc) {
   }

   public void visitFineDensityCmd(IBMSureonePrinterCmd.FineDensityCmd fdc) {
   }

   public void visitEnableDownloadCharCmd(IBMSureonePrinterCmd.EnableDownloadCharCmd edcc) {
   }

   public void visitDisableDownloadCharCmd(IBMSureonePrinterCmd.DisableDownloadCharCmd ddcc) {
   }

   public void visitDefineDownloadCharCmd(IBMSureonePrinterCmd.DefineDownloadCharCmd ddcc) {
   }

   public void visitDeleteDownloadCharCmd(IBMSureonePrinterCmd.DeleteDownloadCharCmd ddcc) {
   }

   public void visitImmediateDriveCmd(IBMSureonePrinterCmd.ImmediateDriveCmd idc) {
   }

   public void visitBeeperCmd(IBMSureonePrinterCmd.BeeperCmd bc) {
   }

   public void visitCancelPrintCmd(IBMSureonePrinterCmd.CancelPrintCmd cpc) {
   }

   public void visitReinitPrinterCmd(IBMSureonePrinterCmd.ReinitPrinterCmd ipc) {
   }

   public void visitStatusRequestCmd(IBMSureonePrinterCmd.StatusRequestCmd ipc) {
   }

   public void visitInitPrinterCmd(IBMSureonePrinterCmd.InitPrinterCmd ipc) {
   }

   public void visitVerticalColAlignCmd(IBMSureonePrinterCmd.VerticalColAlignCmd vcac) {
   }

   public void visitPrintDensityCmd(IBMSureonePrinterCmd.PrintDensityCmd vpdc) {
   }
}
