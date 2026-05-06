package com.ibm.posj;

public interface SystemCmd extends HandleCmd {
   int TEST_REQUEST_CMD_CODE = 100;
   int STATUS_REQUEST_CMD_CODE = 101;
   int RESET_CMD_CODE = 102;
   int DEVICE_INFO_REQUEST_CMD_CODE = 103;
   int DIRECT_WRITE_CMD_CODE = 104;
   int STATISTIC_CMD_CODE = 105;
   String TEST_REQUEST_CMD_NAME = "TEST_REQUEST_CMD";
   String STATUS_REQUEST_CMD_NAME = "STATUS_REQUEST_CMD";
   String RESET_CMD_NAME = "RESET_CMD";
   String DEVICE_INFO_REQUEST_CMD_NAME = "DEVICE_INFO_REQUEST_CMD";
   String DIRECT_WRITE_CMD_NAME = "DIRECT_WRITE_CMD";
   String STATISTIC_CMD_NAME = "STATISTIC_CMD";

   void accept(SystemCmdVisitor var1);

   void setCmdBytes(byte[] var1);

   public interface DeviceInfoRequestCmd extends SystemCmd {
      void setDeviceType(int var1);

      int getDeviceType();

      void setDeviceId(int var1);

      int getDeviceId();

      byte getLanguage();

      byte getCommandSet();

      int getFirmwareLevel();

      String getSerialNumber();

      void setFirmwareLevel(int var1);

      void setSerialNumber(String var1);
   }

   public interface DirectWriteCmd extends SystemCmd {
   }

   public interface Factory extends HandleCmd.Factory {
      SystemCmd.TestRequestCmd createTestRequestCmd();

      SystemCmd.StatusRequestCmd createStatusRequestCmd();

      SystemCmd.DeviceInfoRequestCmd createDeviceInfoRequestCmd();

      SystemCmd.ResetRequestCmd createResetCmd();

      SystemCmd.DirectWriteCmd createDirectWriteCmd();

      SystemCmd.StatisticCmd createStatisticCmd(String var1);
   }

   public interface ResetRequestCmd extends SystemCmd {
   }

   public interface StatisticCmd extends SystemCmd {
      String getStatisticType();

      void setStatisticResult(Object var1);

      Object getStatisticResult();
   }

   public interface StatusRequestCmd extends SystemCmd {
   }

   public interface TestRequestCmd extends SystemCmd {
   }
}
