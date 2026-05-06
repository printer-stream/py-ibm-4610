package com.ibm.posj;

public interface IBM4689PrinterCmdVisitor extends POSPrinterCmdVisitor {
   void visitLoadUDCBufferCmd(IBM4689PrinterCmd.LoadUDCBufferCmd var1);

   void visitInitPrinterCmd(IBM4689PrinterCmd.InitPrinterCmd var1);

   void visitFeedCmd(IBM4689PrinterCmd.FeedCmd var1);

   void visitPartialCutCmd(IBM4689PrinterCmd.PartialCutCmd var1);

   void visitPrintFullCutCmd(IBM4689PrinterCmd.PrintFullCutCmd var1);

   void visitFeedUnitsBackCmd(IBM4689PrinterCmd.FeedUnitsBackCmd var1);

   void visitDownloadFontCmd(IBM4689PrinterCmd.DownloadFontCmd var1);

   void visitDownloadDBCSFontCmd(IBM4689PrinterCmd.DownloadDBCSFontCmd var1);

   void visitECLevelRequestCmd(IBM4689PrinterCmd.ECLevelRequestCmd var1);

   void visitSelectHSizeCmd(IBM4689PrinterCmd.SelectHSizeCmd var1);

   void visitSelectVSizeCmd(IBM4689PrinterCmd.SelectVSizeCmd var1);

   void visitSelectHRIPositionCmd(IBM4689PrinterCmd.SelectHRIPositionCmd var1);

   void visitSendCheckSumCmd(IBM4689PrinterCmd.SendCheckSumCmd var1);

   void visitContinuation4689Cmd(IBM4689PrinterCmd.Continuation4689Cmd var1);
}
