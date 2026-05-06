package com.ibm.embedded;

public class HardTotalsPciDriverImp implements HardTotalsEmbeddedDriver {
   private int slotNumber = 1;
   private long adapterId = -1L;

   public HardTotalsPciDriverImp(long id) {
      if (id == 5L) {
         this.slotNumber = 2;
      }

      this.adapterId = id;
   }

   public native NvramInfo nativeReadNVRAMData(int var1, int var2, int var3);

   public native NvramInfo nativeWriteNVRAMData(int var1, int var2, int var3, byte[] var4, int var5);

   public native GetNvramInfo nativeNVRAMInfo(int var1);

   public NvramInfo readNVRAMData(int offset, int length) throws EmbeddedException {
      return this.nativeReadNVRAMData(this.slotNumber, offset, length);
   }

   public NvramInfo writeNVRAMData(int offset, int length, byte[] buffer, int bufferOffset) throws EmbeddedException {
      return this.nativeWriteNVRAMData(this.slotNumber, offset, length, buffer, bufferOffset);
   }

   public GetNvramInfo getNVRAMInfo() throws EmbeddedException {
      return this.nativeNVRAMInfo(this.slotNumber);
   }

   public VitalProductData getVPData() throws EmbeddedException {
      throw new EmbeddedException("VPD Not Supported");
   }

   public long getAdapterID() {
      return this.adapterId;
   }

   public void accept(EmbeddedDriverVisitor visitor) {
      visitor.visitHardTotals(this);
   }

   static {
      try {
         System.loadLibrary("aipposembedded");
      } catch (UnsatisfiedLinkError var1) {
      }
   }
}
