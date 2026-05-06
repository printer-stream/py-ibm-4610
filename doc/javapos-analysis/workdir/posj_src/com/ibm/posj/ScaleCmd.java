package com.ibm.posj;

public interface ScaleCmd extends HandleCmd {
   void accept(ScaleCmdVisitor var1);

   void setCmdBytes(byte[] var1);

   public interface ClearDisplayCmd extends ScaleCmd {
   }

   public interface ConfigScaleCmd extends ScaleCmd {
      ScaleConfig getConfig();
   }

   public interface EnableExtendedStatusCmd extends ScaleCmd {
      boolean getEnable();
   }

   public interface Factory extends SystemCmd.Factory {
      ScaleCmd createClearDisplayCmd();

      ScaleCmd createConfigScaleCmd(ScaleConfig var1);

      ScaleCmd createEnableExtendedStatusCmd(boolean var1);

      ScaleCmd createReportConfigCmd();

      ScaleCmd createWeightReqCmd(byte var1);

      ScaleCmd createZeroScaleCmd();
   }

   public interface ReportConfigCmd extends ScaleCmd {
   }

   public interface WeightReqCmd extends ScaleCmd {
      int getMode();
   }

   public interface ZeroScaleCmd extends ScaleCmd {
   }
}
