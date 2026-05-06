package com.ibm.posj.util;

import com.ibm.posj.ScannerCmd;
import com.ibm.posj.ScannerCmdVisitor;

public class DefaultScannerCmdV extends DefaultSystemCmdV implements ScannerCmdVisitor {
   public void visitEnableScannerCmd(ScannerCmd.EnableScannerCmd cmd) {
   }

   public void visitDisableScannerCmd(ScannerCmd.DisableScannerCmd cmd) {
   }

   public void visitEnableBeeperScannerCmd(ScannerCmd.EnableBeeperScannerCmd cmd) {
   }

   public void visitDisableBeeperScannerCmd(ScannerCmd.DisableBeeperScannerCmd cmd) {
   }

   public void visitConfigScannerCmd(ScannerCmd.ConfigScannerCmd cmd) {
   }

   public void visitReportScannerCmd(ScannerCmd.ReportScannerCmd cmd) {
   }

   public void visitConfigJAN13TwoLabelScannerCmd(ScannerCmd.ConfigJAN13TwoLabelScannerCmd cmd) {
   }

   public void visitReportJAN13TwoLabelScannerCmd(ScannerCmd.ReportJAN13TwoLabelScannerCmd cmd) {
   }

   public void visitDirectIOScannerCmd(ScannerCmd.DirectIOScannerCmd cmd) {
   }
}
