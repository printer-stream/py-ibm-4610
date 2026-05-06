package com.ibm.posj;

abstract class DefaultScannerCmd extends AbstractHandleCmd implements ScannerCmd, ScannerHandleConst {
   private String name = "";
   private int code = 0;
   protected byte[] byteArray = new byte[0];

   DefaultScannerCmd(HandleCmd.Factory factory, String name, int code) {
      super(factory);
      this.name = name;
      this.code = code;
   }

   public void accept(HandleCmdVisitor visitor) {
      visitor.visitScannerCmd(this);
   }

   public String getName() {
      return this.name;
   }

   public int getCode() {
      return this.code;
   }

   public byte[] toBytes() {
      return this.byteArray;
   }

   public void setCmdBytes(byte[] cmd) {
      this.byteArray = cmd;
   }

   static class ConfigJAN13TwoLabelScannerCmd extends DefaultScannerCmd implements ScannerCmd.ConfigJAN13TwoLabelScannerCmd {
      private byte[] scannerConfig = new byte[8];

      ConfigJAN13TwoLabelScannerCmd(HandleCmd.Factory factory, byte[] config) {
         super(factory, "CONFIG_JAN13_TWO_LABEL_SCANNER_CMD", 905);
         this.scannerConfig = config;
      }

      public void accept(ScannerCmdVisitor visitor) {
         visitor.visitConfigJAN13TwoLabelScannerCmd(this);
      }

      public byte[] getConfig() {
         return this.scannerConfig;
      }
   }

   static class ConfigScannerCmd extends DefaultScannerCmd implements ScannerCmd.ConfigScannerCmd {
      private ScannerConfig scannerConfig = null;

      ConfigScannerCmd(HandleCmd.Factory factory, ScannerConfig config) {
         super(factory, "CONFIG_SCANNER_CMD_NAME", 904);
         this.scannerConfig = config;
      }

      public void accept(ScannerCmdVisitor visitor) {
         visitor.visitConfigScannerCmd(this);
      }

      public ScannerConfig getConfig() {
         return this.scannerConfig;
      }
   }

   static class DirectIOScannerCmd extends DefaultScannerCmd implements ScannerCmd.DirectIOScannerCmd {
      private byte[] directIOCmd = new byte[0];
      private int commandParam = -1;
      private int[] dataParam = new int[0];
      private Object objectParam = null;

      DirectIOScannerCmd(HandleCmd.Factory factory, byte[] cmd) {
         super(factory, "DIRECT_IO_SCANNER_CMD", 908);
         this.directIOCmd = cmd;
      }

      DirectIOScannerCmd(HandleCmd.Factory factory, int command, int[] data, Object object) {
         super(factory, "DIRECT_IO_SCANNER_CMD", 908);
         this.commandParam = command;
         this.dataParam = data;
         this.objectParam = object;
      }

      public void accept(ScannerCmdVisitor visitor) {
         visitor.visitDirectIOScannerCmd(this);
      }

      public byte[] getDirectIOCmd() {
         return this.directIOCmd;
      }

      public int getCommandParam() {
         return this.commandParam;
      }

      public int[] getDataParam() {
         return this.dataParam;
      }

      public Object getObjectParam() {
         return this.objectParam;
      }
   }

   static class DisableBeeperScannerCmd extends DefaultScannerCmd implements ScannerCmd.DisableBeeperScannerCmd {
      DisableBeeperScannerCmd(HandleCmd.Factory factory) {
         super(factory, "DISABLE_BEEPER_SCANNER_CMD", 903);
      }

      public void accept(ScannerCmdVisitor visitor) {
         visitor.visitDisableBeeperScannerCmd(this);
      }
   }

   static class DisableScannerCmd extends DefaultScannerCmd implements ScannerCmd.DisableScannerCmd {
      DisableScannerCmd(HandleCmd.Factory factory) {
         super(factory, "DISABLE_SCANNER_CMD", 901);
      }

      public void accept(ScannerCmdVisitor visitor) {
         visitor.visitDisableScannerCmd(this);
      }
   }

   static class EnableBeeperScannerCmd extends DefaultScannerCmd implements ScannerCmd.EnableBeeperScannerCmd {
      EnableBeeperScannerCmd(HandleCmd.Factory factory) {
         super(factory, "ENABLE_BEEPER_SCANNER_CMD", 902);
      }

      public void accept(ScannerCmdVisitor visitor) {
         visitor.visitEnableBeeperScannerCmd(this);
      }
   }

   static class EnableScannerCmd extends DefaultScannerCmd implements ScannerCmd.EnableScannerCmd {
      EnableScannerCmd(HandleCmd.Factory factory) {
         super(factory, "ENABLE_SCANNER_CMD", 900);
      }

      public void accept(ScannerCmdVisitor visitor) {
         visitor.visitEnableScannerCmd(this);
      }
   }

   static class Factory extends DefaultSystemCmd.Factory implements ScannerCmd.Factory {
      public ScannerCmd createEnableScannerCmd(boolean enable) {
         return (ScannerCmd)(enable ? new DefaultScannerCmd.EnableScannerCmd(this) : new DefaultScannerCmd.DisableScannerCmd(this));
      }

      public ScannerCmd createBeeperScannerCmd(boolean enable) {
         return (ScannerCmd)(enable ? new DefaultScannerCmd.EnableBeeperScannerCmd(this) : new DefaultScannerCmd.DisableBeeperScannerCmd(this));
      }

      public ScannerCmd createConfigScannerCmd(ScannerConfig config) {
         return new DefaultScannerCmd.ConfigScannerCmd(this, config);
      }

      public ScannerCmd createConfigJAN13TwoLabelScannerCmd(byte[] config) {
         return new DefaultScannerCmd.ConfigJAN13TwoLabelScannerCmd(this, config);
      }

      public ScannerCmd createReportScannerCmd() {
         return new DefaultScannerCmd.ReportScannerCmd(this);
      }

      public ScannerCmd createReportJAN13TwoLabelScannerCmd() {
         return new DefaultScannerCmd.ReportJAN13TwoLabelScannerCmd(this);
      }

      public ScannerCmd createDirectIOScannerCmd(byte[] cmd) {
         return new DefaultScannerCmd.DirectIOScannerCmd(this, cmd);
      }

      public ScannerCmd createDirectIOScannerCmd(int command, int[] data, Object object) {
         return new DefaultScannerCmd.DirectIOScannerCmd(this, command, data, object);
      }
   }

   static class ReportJAN13TwoLabelScannerCmd extends DefaultScannerCmd implements ScannerCmd.ReportJAN13TwoLabelScannerCmd {
      ReportJAN13TwoLabelScannerCmd(HandleCmd.Factory factory) {
         super(factory, "REPORT_JAN13_TWO_LABEL_SCANNER_CMD", 907);
      }

      public void accept(ScannerCmdVisitor visitor) {
         visitor.visitReportJAN13TwoLabelScannerCmd(this);
      }
   }

   static class ReportScannerCmd extends DefaultScannerCmd implements ScannerCmd.ReportScannerCmd {
      ReportScannerCmd(HandleCmd.Factory factory) {
         super(factory, "REPORT_SCANNER_CMD", 906);
      }

      public void accept(ScannerCmdVisitor visitor) {
         visitor.visitReportScannerCmd(this);
      }
   }
}
