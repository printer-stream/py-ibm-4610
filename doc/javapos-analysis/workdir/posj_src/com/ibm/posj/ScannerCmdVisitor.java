package com.ibm.posj;

public interface ScannerCmdVisitor {
   void visitEnableScannerCmd(ScannerCmd.EnableScannerCmd var1);

   void visitDisableScannerCmd(ScannerCmd.DisableScannerCmd var1);

   void visitEnableBeeperScannerCmd(ScannerCmd.EnableBeeperScannerCmd var1);

   void visitDisableBeeperScannerCmd(ScannerCmd.DisableBeeperScannerCmd var1);

   void visitConfigScannerCmd(ScannerCmd.ConfigScannerCmd var1);

   void visitReportScannerCmd(ScannerCmd.ReportScannerCmd var1);

   void visitConfigJAN13TwoLabelScannerCmd(ScannerCmd.ConfigJAN13TwoLabelScannerCmd var1);

   void visitReportJAN13TwoLabelScannerCmd(ScannerCmd.ReportJAN13TwoLabelScannerCmd var1);

   void visitDirectIOScannerCmd(ScannerCmd.DirectIOScannerCmd var1);
}
