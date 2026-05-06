package com.ibm.posj;

public interface IBM4610PrinterCmdVisitor extends POSPrinterCmdVisitor {
   void visitECLevelRequestCmd(IBM4610PrinterCmd.ECLevelRequestCmd var1);

   void visitReleasePrintBufferCmd(IBM4610PrinterCmd.ReleasePrintBufferCmd var1);

   void visitPageModeCmd(IBM4610PrinterCmd.PageModeCmd var1);

   void visitStartScanCmd(IBM4610PrinterCmd.StartScanCmd var1);

   void visitStoreScannedImageCmd(IBM4610PrinterCmd.StoreScannedImgCmd var1);

   void visitContinuationCmd(IBM4610PrinterCmd.ContinuationCmd var1);

   void visitChangePrintSideCmd(IBM4610PrinterCmd.ChangePrintSideCmd var1);

   void visitPageModeNormalCmd(IBM4610PrinterCmd.PMPageModeNormalCmd var1);

   void visitPrintPageModePageCmd(IBM4610PrinterCmd.PrintPageModePageCmd var1);
}
