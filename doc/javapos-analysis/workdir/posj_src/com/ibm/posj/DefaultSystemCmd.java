package com.ibm.posj;

class DefaultSystemCmd extends AbstractHandleCmd implements SystemCmd {
   private String name = "";
   private int code = 0;
   protected byte[] byteArray = new byte[0];

   DefaultSystemCmd(HandleCmd.Factory factory, String name, int code) {
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
      visitor.visitSystemCmd(this);
   }

   public void accept(SystemCmdVisitor visitor) {
   }

   static class DeviceInfoRequestCmd extends DefaultSystemCmd implements SystemCmd.DeviceInfoRequestCmd {
      private int deviceType = 0;
      private int deviceId = 0;
      private byte language = 0;
      private byte commandSet = 0;
      private byte[] byteArray = new byte[0];
      private int firmwareLevel = -1;
      private String serialNumber = "";

      DeviceInfoRequestCmd(HandleCmd.Factory factory) {
         super(factory, "DEVICE_INFO_REQUEST_CMD", 103);
      }

      public void accept(SystemCmdVisitor visitor) {
         visitor.visitDevInfoSystemCmd(this);
      }

      public void setDeviceType(int type) {
         this.deviceType = type;
      }

      public int getDeviceType() {
         return this.deviceType;
      }

      public int getDeviceId() {
         return this.deviceId;
      }

      public void setDeviceId(int id) {
         this.deviceId = id;
      }

      public byte getLanguage() {
         return this.language;
      }

      public byte getCommandSet() {
         return this.commandSet;
      }

      public int getFirmwareLevel() {
         return this.firmwareLevel;
      }

      public String getSerialNumber() {
         return this.serialNumber;
      }

      public void setFirmwareLevel(int fl) {
         this.firmwareLevel = fl;
      }

      public void setSerialNumber(String sn) {
         this.serialNumber = sn;
      }
   }

   static class DirectWriteCmd extends DefaultSystemCmd implements SystemCmd.DirectWriteCmd {
      DirectWriteCmd(HandleCmd.Factory factory) {
         super(factory, "DIRECT_WRITE_CMD", 104);
      }

      public void accept(SystemCmdVisitor visitor) {
         visitor.visitDirectWriteCmd(this);
      }
   }

   static class Factory extends AbstractHandleCmd.Factory implements SystemCmd.Factory {
      public SystemCmd.TestRequestCmd createTestRequestCmd() {
         return new DefaultSystemCmd.TestRequestCmd(this);
      }

      public SystemCmd.StatusRequestCmd createStatusRequestCmd() {
         return new DefaultSystemCmd.StatusRequestCmd(this);
      }

      public SystemCmd.DeviceInfoRequestCmd createDeviceInfoRequestCmd() {
         return new DefaultSystemCmd.DeviceInfoRequestCmd(this);
      }

      public SystemCmd.ResetRequestCmd createResetCmd() {
         return new DefaultSystemCmd.ResetRequestCmd(this);
      }

      public SystemCmd.DirectWriteCmd createDirectWriteCmd() {
         return new DefaultSystemCmd.DirectWriteCmd(this);
      }

      public SystemCmd.StatisticCmd createStatisticCmd(String statisticType) {
         return new DefaultSystemCmd.StatisticCmd(this, statisticType);
      }
   }

   static class ResetRequestCmd extends DefaultSystemCmd implements SystemCmd.ResetRequestCmd {
      ResetRequestCmd(HandleCmd.Factory factory) {
         super(factory, "RESET_CMD", 102);
      }

      public void accept(SystemCmdVisitor visitor) {
         visitor.visitResetSystemCmd(this);
      }
   }

   static class StatisticCmd extends DefaultSystemCmd implements SystemCmd.StatisticCmd {
      private String statisticType = null;
      private Object statisticResult = null;

      StatisticCmd(HandleCmd.Factory factory, String statisticType) {
         super(factory, "STATISTIC_CMD", 105);
         this.statisticType = statisticType;
      }

      public String getStatisticType() {
         return this.statisticType;
      }

      public void accept(SystemCmdVisitor visitor) {
         visitor.visitStatisticCmd(this);
      }

      public void setStatisticResult(Object statisticResult) {
         this.statisticResult = statisticResult;
      }

      public Object getStatisticResult() {
         return this.statisticResult;
      }
   }

   static class StatusRequestCmd extends DefaultSystemCmd implements SystemCmd.StatusRequestCmd {
      StatusRequestCmd(HandleCmd.Factory factory) {
         super(factory, "STATUS_REQUEST_CMD", 101);
      }

      public void accept(SystemCmdVisitor visitor) {
         visitor.visitStatusSystemCmd(this);
      }
   }

   static class TestRequestCmd extends DefaultSystemCmd implements SystemCmd.TestRequestCmd {
      TestRequestCmd(HandleCmd.Factory factory) {
         super(factory, "TEST_REQUEST_CMD", 100);
      }

      public void accept(SystemCmdVisitor visitor) {
         visitor.visitTestSystemCmd(this);
      }
   }
}
