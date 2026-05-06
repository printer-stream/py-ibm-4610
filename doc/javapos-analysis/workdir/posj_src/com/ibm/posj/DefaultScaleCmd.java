package com.ibm.posj;

public abstract class DefaultScaleCmd extends AbstractHandleCmd implements ScaleCmd, ScaleHandleConst {
   private String name = "";
   private int code = 0;
   protected HandleCmd.Result result = null;
   protected byte[] byteArray = new byte[0];

   DefaultScaleCmd(HandleCmd.Factory factory, String name, int code) {
      super(factory);
      this.name = name;
      this.code = code;
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

   public void accept(HandleCmdVisitor visitor) {
      visitor.visitScaleCmd(this);
   }

   public abstract void accept(ScaleCmdVisitor var1);

   public HandleCmd.Result getResult() {
      if (this.result == null) {
         this.result = new AbstractHandleCmd.DefaultResult();
      }

      return this.result;
   }

   static class ClearDisplayCmd extends DefaultScaleCmd implements ScaleCmd.ClearDisplayCmd {
      ClearDisplayCmd(HandleCmd.Factory factory) {
         super(factory, "CLEAR_DISPLAY_SCALE_CMD", 1100);
      }

      public void accept(ScaleCmdVisitor visitor) {
         visitor.visitClearDisplayCmd(this);
      }
   }

   static class ConfigScaleCmd extends DefaultScaleCmd implements ScaleCmd.ConfigScaleCmd {
      private ScaleConfig config = null;

      ConfigScaleCmd(HandleCmd.Factory factory, ScaleConfig config) {
         super(factory, "CONFIG_SCALE_CMD", 1101);
         this.config = config;
      }

      public void accept(ScaleCmdVisitor visitor) {
         visitor.visitConfigScaleCmd(this);
      }

      public ScaleConfig getConfig() {
         return this.config;
      }
   }

   static class EnableExtendedStatusCmd extends DefaultScaleCmd implements ScaleCmd.EnableExtendedStatusCmd {
      private boolean enable = true;

      EnableExtendedStatusCmd(HandleCmd.Factory factory, boolean enable) {
         super(factory, "ENABLE_EXTENDED_STATUS_CMD", 1102);
         this.enable = enable;
      }

      public void accept(ScaleCmdVisitor visitor) {
         visitor.visitEnableExtendedStatusCmd(this);
      }

      public boolean getEnable() {
         return this.enable;
      }
   }

   static class Factory extends DefaultSystemCmd.Factory implements ScaleCmd.Factory {
      public ScaleCmd createClearDisplayCmd() {
         return new DefaultScaleCmd.ClearDisplayCmd(this);
      }

      public ScaleCmd createConfigScaleCmd(ScaleConfig config) {
         return new DefaultScaleCmd.ConfigScaleCmd(this, config);
      }

      public ScaleCmd createEnableExtendedStatusCmd(boolean enable) {
         return new DefaultScaleCmd.EnableExtendedStatusCmd(this, enable);
      }

      public ScaleCmd createReportConfigCmd() {
         return new DefaultScaleCmd.ReportConfigCmd(this);
      }

      public ScaleCmd createWeightReqCmd(byte mode) {
         return new DefaultScaleCmd.WeightReqCmd(this, mode);
      }

      public ScaleCmd createZeroScaleCmd() {
         return new DefaultScaleCmd.ZeroScaleCmd(this);
      }
   }

   static class ReportConfigCmd extends DefaultScaleCmd implements ScaleCmd.ReportConfigCmd {
      ReportConfigCmd(HandleCmd.Factory factory) {
         super(factory, "REPORT_CONFIG_SCALE_CMD", 1103);
      }

      public void accept(ScaleCmdVisitor visitor) {
         visitor.visitReportConfigCmd(this);
      }
   }

   static class WeightReqCmd extends DefaultScaleCmd implements ScaleCmd.WeightReqCmd {
      private int mode = 0;

      WeightReqCmd(HandleCmd.Factory factory, byte mode) {
         super(factory, "WEIGHT_REQ_SCALE_CMD", 1104);
         this.mode = mode;
      }

      public void accept(ScaleCmdVisitor visitor) {
         visitor.visitWeightReqCmd(this);
      }

      public int getMode() {
         return this.mode;
      }
   }

   static class ZeroScaleCmd extends DefaultScaleCmd implements ScaleCmd.ZeroScaleCmd {
      ZeroScaleCmd(HandleCmd.Factory factory) {
         super(factory, "ZERO_SCALE_CMD", 1105);
      }

      public void accept(ScaleCmdVisitor visitor) {
         visitor.visitZeroScaleCmd(this);
      }
   }
}
