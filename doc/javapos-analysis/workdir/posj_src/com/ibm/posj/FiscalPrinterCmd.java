package com.ibm.posj;

public interface FiscalPrinterCmd extends HandleCmd {
   public interface Factory extends SystemCmd.Factory {
      FiscalPrinterCmd.FiscalWriteCmd createFiscalWriteCmd(byte[] var1);
   }

   public interface FiscalWriteCmd extends FiscalPrinterCmd {
   }
}
