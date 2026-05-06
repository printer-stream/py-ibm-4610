package com.ibm.posj;

public interface ScannerCmd extends HandleCmd {
   void accept(ScannerCmdVisitor var1);

   void setCmdBytes(byte[] var1);

   public interface ConfigJAN13TwoLabelScannerCmd extends ScannerCmd {
      byte[] getConfig();
   }

   public interface ConfigScannerCmd extends ScannerCmd {
      ScannerConfig getConfig();
   }

   public interface DirectIOScannerCmd extends ScannerCmd {
      byte[] getDirectIOCmd();

      int getCommandParam();

      int[] getDataParam();

      Object getObjectParam();
   }

   public interface DisableBeeperScannerCmd extends ScannerCmd {
   }

   public interface DisableScannerCmd extends ScannerCmd {
   }

   public interface EnableBeeperScannerCmd extends ScannerCmd {
   }

   public interface EnableScannerCmd extends ScannerCmd {
   }

   public interface Factory extends SystemCmd.Factory {
      ScannerCmd createEnableScannerCmd(boolean var1);

      ScannerCmd createBeeperScannerCmd(boolean var1);

      ScannerCmd createConfigScannerCmd(ScannerConfig var1);

      ScannerCmd createConfigJAN13TwoLabelScannerCmd(byte[] var1);

      ScannerCmd createReportScannerCmd();

      ScannerCmd createReportJAN13TwoLabelScannerCmd();

      ScannerCmd createDirectIOScannerCmd(byte[] var1);

      ScannerCmd createDirectIOScannerCmd(int var1, int[] var2, Object var3);
   }

   public interface ReportJAN13TwoLabelScannerCmd extends ScannerCmd {
   }

   public interface ReportScannerCmd extends ScannerCmd {
   }
}
