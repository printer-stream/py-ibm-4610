package com.ibm.posj;

import com.ibm.posj.bus.HardTotalsCmdSubmitter;

public interface HardTotalsCmd extends HandleCmd {
   int HARDTOTALS_GET_INFORMATION_CMD_CODE = 201;
   int HARDTOTALS_READ_DATA_CMD_CODE = 202;
   int HARDTOTALS_WRITE_DATA_CMD_CODE = 203;
   String HARDTOTALS_GET_INFORMATION_CMD_NAME = "GetHardTotalsInfCmd";
   String HARDTOTALS_READ_DATA_CMD_NAME = "ReadHardTotalsDataCmd";
   String HARDTOTALS_WRITE_DATA_CMD_NAME = "WriteHardTotalsDataCmd";

   HardTotalsCmd.Factory getFactory();

   int getCompletionCode();

   void setCompletionCode(int var1);

   void submit() throws HandleException;

   public interface Factory extends SystemCmd.Factory {
      void setHardTotalsCmdSubmitter(HardTotalsCmdSubmitter var1);

      HardTotalsCmdSubmitter getHardTotalsCmdSubmitter();

      HardTotalsCmd.GetHardTotalsInfCmd createGetHardTotalsInfCmd();

      HardTotalsCmd.ReadHardTotalsDataCmd createReadHardTotalsDataCmd(int var1, int var2, byte[] var3);

      HardTotalsCmd.WriteHardTotalsDataCmd createWriteHardTotalsDataCmd(int var1, int var2, byte[] var3, int var4);
   }

   public interface GetHardTotalsInfCmd extends HardTotalsCmd {
      int getTotalSize();

      void setTotalSize(int var1);

      int getUserSize();

      void setUserSize(int var1);

      int getSystemSize();

      void setSystemSize(int var1);
   }

   public interface HardTotalsDataCmd extends HardTotalsCmd {
      int getOffset();

      void setOffset(int var1);

      int getLength();

      void setLength(int var1);

      byte[] getBuffer();

      void setBuffer(byte[] var1);

      int getBufferOffset();

      void setBufferOffset(int var1);
   }

   public interface ReadHardTotalsDataCmd extends HardTotalsCmd.HardTotalsDataCmd {
   }

   public interface WriteHardTotalsDataCmd extends HardTotalsCmd.HardTotalsDataCmd {
   }
}
