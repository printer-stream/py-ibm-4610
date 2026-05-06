package com.ibm.embedded;

public interface HardTotalsEmbeddedDriver extends EmbeddedDriver {
   NvramInfo readNVRAMData(int var1, int var2) throws EmbeddedException;

   NvramInfo writeNVRAMData(int var1, int var2, byte[] var3, int var4) throws EmbeddedException;

   GetNvramInfo getNVRAMInfo() throws EmbeddedException;

   VitalProductData getVPData() throws EmbeddedException;

   long getAdapterID();
}
