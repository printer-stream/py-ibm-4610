package com.ibm.posj;

public interface IBMSureonePrinterCmdVisitor extends POSPrinterCmdVisitor {
   void visitSelectIntCharSetCmd(IBMSureonePrinterCmd.SelectIntCharSetCmd var1);

   void visitSetXonOffCmd(IBMSureonePrinterCmd.SetXonOffCmd var1);

   void visitSelectIBMChar1Cmd(IBMSureonePrinterCmd.SelectIBMChar1Cmd var1);

   void visitSelectIBMChar2Cmd(IBMSureonePrinterCmd.SelectIBMChar2Cmd var1);

   void visitSelectNormalCharSpaceCmd(IBMSureonePrinterCmd.SelectNormalCharSpaceCmd var1);

   void visitSelectMediumCharSpaceCmd(IBMSureonePrinterCmd.SelectMediumCharSpaceCmd var1);

   void visitSelectWideCharSpaceCmd(IBMSureonePrinterCmd.SelectWideCharSpaceCmd var1);

   void visitSelectXtraWideCharSpaceCmd(IBMSureonePrinterCmd.SelectXtraWideCharSpaceCmd var1);

   void visitSelect2xCharWidthCmd(IBMSureonePrinterCmd.Select2xCharWidthCmd var1);

   void visitSetWideModeCmd(IBMSureonePrinterCmd.SetWideModeCmd var1);

   void visitSelect2xCharHeightCmd(IBMSureonePrinterCmd.Select2xCharHeightCmd var1);

   void visitSetHighModeCmd(IBMSureonePrinterCmd.SetHighModeCmd var1);

   void visitSelectBoldModeCmd(IBMSureonePrinterCmd.SelectBoldModeCmd var1);

   void visitOverlineCmd(IBMSureonePrinterCmd.OverlineCmd var1);

   void visitSelectHighligthCmd(IBMSureonePrinterCmd.SelectHighligthCmd var1);

   void visitLineSpacingCmd(IBMSureonePrinterCmd.LineSpacingCmd var1);

   void visitSetLineSpaceCmd(IBMSureonePrinterCmd.SetLineSpaceCmd var1);

   void visitFeedLinesCmd(IBMSureonePrinterCmd.FeedLinesCmd var1);

   void visitSetTightSpaceCmd(IBMSureonePrinterCmd.SetTightSpaceCmd var1);

   void visitMicroFeedCmd(IBMSureonePrinterCmd.MicroFeedCmd var1);

   void visitMicroBackFeedCmd(IBMSureonePrinterCmd.MicroBackFeedCmd var1);

   void visit_8mmFeedCmd(IBMSureonePrinterCmd._8mmFeedCmd var1);

   void visitSetCrowdedSpaceCmd(IBMSureonePrinterCmd.SetCrowdedSpaceCmd var1);

   void visitFormFeedLengthCmd(IBMSureonePrinterCmd.FormFeedLengthCmd var1);

   void visitSetPageLengthLinesCmd(IBMSureonePrinterCmd.SetPageLengthLinesCmd var1);

   void visitSetPageLengthInchesCmd(IBMSureonePrinterCmd.SetPageLengthInchesCmd var1);

   void visitVerticalTabCmd(IBMSureonePrinterCmd.VerticalTabCmd var1);

   void visitVerticalTabPosCmd(IBMSureonePrinterCmd.VerticalTabPosCmd var1);

   void visitSetBottomMarginCmd(IBMSureonePrinterCmd.SetBottomMarginCmd var1);

   void visitSetLeftMarginCmd(IBMSureonePrinterCmd.SetLeftMarginCmd var1);

   void visitSetRightMarginCmd(IBMSureonePrinterCmd.SetRightMarginCmd var1);

   void visitHorizontalTabCmd(IBMSureonePrinterCmd.HorizontalTabCmd var1);

   void visitHorizontalTabPosCmd(IBMSureonePrinterCmd.HorizontalTabPosCmd var1);

   void visitNormalDensityCmd(IBMSureonePrinterCmd.NormalDensityCmd var1);

   void visitHighDensityCmd(IBMSureonePrinterCmd.HighDensityCmd var1);

   void visitFineDensityBitCmd(IBMSureonePrinterCmd.FineDensityBitCmd var1);

   void visitFineDensityCmd(IBMSureonePrinterCmd.FineDensityCmd var1);

   void visitEnableDownloadCharCmd(IBMSureonePrinterCmd.EnableDownloadCharCmd var1);

   void visitDisableDownloadCharCmd(IBMSureonePrinterCmd.DisableDownloadCharCmd var1);

   void visitDefineDownloadCharCmd(IBMSureonePrinterCmd.DefineDownloadCharCmd var1);

   void visitDeleteDownloadCharCmd(IBMSureonePrinterCmd.DeleteDownloadCharCmd var1);

   void visitImmediateDriveCmd(IBMSureonePrinterCmd.ImmediateDriveCmd var1);

   void visitBeeperCmd(IBMSureonePrinterCmd.BeeperCmd var1);

   void visitCancelPrintCmd(IBMSureonePrinterCmd.CancelPrintCmd var1);

   void visitReinitPrinterCmd(IBMSureonePrinterCmd.ReinitPrinterCmd var1);

   void visitStatusRequestCmd(IBMSureonePrinterCmd.StatusRequestCmd var1);

   void visitInitPrinterCmd(IBMSureonePrinterCmd.InitPrinterCmd var1);

   void visitVerticalColAlignCmd(IBMSureonePrinterCmd.VerticalColAlignCmd var1);

   void visitPrintDensityCmd(IBMSureonePrinterCmd.PrintDensityCmd var1);
}
