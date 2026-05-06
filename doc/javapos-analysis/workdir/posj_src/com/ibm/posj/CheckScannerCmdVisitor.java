package com.ibm.posj;

public interface CheckScannerCmdVisitor {
   void visitBeginInsertionCmd(CheckScannerCmd.BeginInsertionCmd var1) throws HandleException;

   void visitBeginRemovalCmd(CheckScannerCmd.BeginRemovalCmd var1) throws HandleException;

   void visitStartScanCmd(CheckScannerCmd.StartScanCmd var1) throws HandleException;

   void visitEndRemovalCmd(CheckScannerCmd.EndRemovalCmd var1) throws HandleException;

   void visitPrintScannedImgCmd(CheckScannerCmd.PrintScannedImgCmd var1) throws HandleException;

   void visitGetNextImgLocCmd(CheckScannerCmd.GetNextImgLocCmd var1) throws HandleException;

   void visitSelCompressionFormatCmd(CheckScannerCmd.SelCompressionFormatCmd var1) throws HandleException;

   void visitEraseImagesCmd(CheckScannerCmd.EraseImagesCmd var1) throws HandleException;

   void visitStoreScannedImgCmd(CheckScannerCmd.StoreScannedImgCmd var1) throws HandleException;

   void visitGetScannedImgCmd(CheckScannerCmd.GetScannedImgCmd var1) throws HandleException;

   void visitGetFirstUnreadImgLocCmd(CheckScannerCmd.GetFirstUnreadImgLocCmd var1) throws HandleException;

   void visitScannerCalibCmd(CheckScannerCmd.ScannerCalibCmd var1) throws HandleException;

   void visitChangePrintSideCmd(CheckScannerCmd.ChangePrintSideCmd var1) throws HandleException;
}
