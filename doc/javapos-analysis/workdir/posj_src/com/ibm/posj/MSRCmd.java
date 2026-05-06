package com.ibm.posj;

public interface MSRCmd extends HandleCmd {
   String CONFIG_MSR_CMD_NAME = "Config MSR";
   int CONFIG_MSR_CMD_CODE = 600;
   String WRITE_MSR_CMD_NAME = "Write MSR";
   int WRITE_MSR_CMD_CODE = 601;

   public interface ConfigMSRCmd extends MSRCmd {
      boolean isEnabledISOtrack1();

      boolean isEnabledISOtrack2();

      boolean isEnabledISOtrack3();

      boolean isEnabledJIS_IItrack();

      int getTimeout();
   }

   public interface Factory extends HandleCmd.Factory {
      MSRCmd createConfigMSRCmd(boolean var1, boolean var2, boolean var3, boolean var4, int var5);

      MSRCmd createWriteMSRCmd(byte[] var1);
   }

   public interface WriteMSRCmd extends MSRCmd {
      byte[] getData();

      void setData(byte[] var1);
   }
}
