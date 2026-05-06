package com.ibm.posj;

public interface FiscalPrinterInfoHelper {
   boolean hasFiscalData(byte[] var1);

   byte[] getFiscalData(byte[] var1);

   boolean isFinalStatus(byte[] var1);

   boolean isSuccessfulCommand(byte[] var1);

   int getCountry();

   boolean isPowerInterrupted();

   int getVersion();

   boolean getCoverOpen(byte[] var1);

   int getErrorCode(byte[] var1);

   boolean isValidPacket(byte[] var1);

   int getReturnCode(byte[] var1);

   boolean isAsynchronousStatus(byte[] var1);

   boolean isIPLEnd(byte[] var1);

   boolean getRecEmpty(byte[] var1);

   boolean getSlpEmpty(byte[] var1);
}
