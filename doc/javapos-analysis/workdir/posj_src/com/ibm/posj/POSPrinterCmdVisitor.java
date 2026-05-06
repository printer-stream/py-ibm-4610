package com.ibm.posj;

public interface POSPrinterCmdVisitor {
   Object getData();

   void reset();

   void visit(POSPrinterCmd var1);

   void visitIDCmd(POSPrinterCmd.IDCmd var1);

   void visitPrintNormalCmd(POSPrinterCmd.PrintNormalCmd var1);

   void visitRotatePrintCmd(POSPrinterCmd.RotatePrintCmd var1);

   void visitSelectStationCmd(POSPrinterCmd.SelectStationCmd var1);

   void visitFontTypeCmd(POSPrinterCmd.FontTypeCmd var1);

   void visitSetBitmapCmd(POSPrinterCmd.SetBitmapCmd var1);

   void visitSetLogoCmd(POSPrinterCmd.SetLogoCmd var1);

   void visitDotSpacingCmd(POSPrinterCmd.DotSpacingCmd var1);

   void visitResetCmd(POSPrinterCmd.ResetCmd var1);

   void visitDevInfoCmd(POSPrinterCmd.DevInfoCmd var1);

   void visitEraseFlashSectorCmd(POSPrinterCmd.EraseFlashSectorCmd var1);

   void visitDataRequesterCmd(POSPrinterCmd.DataRequesterCmd var1);

   void visitAlignPositionCmd(POSPrinterCmd.AlignPositionCmd var1);

   void visitBoldCmd(POSPrinterCmd.BoldCmd var1);

   void visitReverseVideoCmd(POSPrinterCmd.ReverseVideoCmd var1);

   void visitAlterWideHighCmd(POSPrinterCmd.AlterWideHighCmd var1);

   void visitScaleFontCmd(POSPrinterCmd.ScaleFontCmd var1);

   void visitNormalModeCmd(POSPrinterCmd.NormalModeCmd var1);

   void visitUnderlineCmd(POSPrinterCmd.UnderlineCmd var1);

   void visitFontColorCmd(POSPrinterCmd.FontColorCmd var1);

   void visitStatusRequestCmd(POSPrinterCmd.StatusRequestCmd var1);

   void visitTestReqCmd(POSPrinterCmd.TestReqCmd var1);

   void visitSetLeftMarginCmd(POSPrinterCmd.SetLeftMarginCmd var1);

   void visitPrintSetLogoCmd(POSPrinterCmd.PrintSetLogoCmd var1);

   void visitPrintBitmapCmd(POSPrinterCmd.PrintBitmapCmd var1);

   void visitPrintBarCodeCmd(POSPrinterCmd.PrintBarCodeCmd var1);

   void visitPrintSetBitmapCmd(POSPrinterCmd.PrintSetBitmapCmd var1);

   void visitCutPaperCmd(POSPrinterCmd.CutPaperCmd var1);

   void visitFontDownloadCmd(POSPrinterCmd.FontDownloadCmd var1);

   void visitFeedUnitsCmd(POSPrinterCmd.FeedUnitsCmd var1);

   void visitFeedLinesCmd(POSPrinterCmd.FeedLinesCmd var1);

   void visitReadSlipCmd(POSPrinterCmd.ReadSlipCmd var1);

   void visitStatisticCmd(POSPrinterCmd.StatisticCmd var1);
}
