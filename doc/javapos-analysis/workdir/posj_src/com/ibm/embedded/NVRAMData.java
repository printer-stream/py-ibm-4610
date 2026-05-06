package com.ibm.embedded;

public class NVRAMData {
   private long completionCode;
   private long slot;
   private long builtIn;
   private long numAreas;
   private long totalSize;
   private long offset;
   private long size;
   private long inUseFlag;
   private long length;
   private StringBuffer buffer;
   private long[] vpd;
   public static final int VITAL_PRODUCT_DATA_LENGTH = 24;

   public long getCompletionCode() {
      return this.completionCode;
   }

   public long getSlot() {
      return this.slot;
   }

   public long getBuiltIn() {
      return this.builtIn;
   }

   public long getNumAreas() {
      return this.numAreas;
   }

   public long getTotalSize() {
      return this.totalSize;
   }

   public long getOffset() {
      return this.offset;
   }

   public long getSize() {
      return this.size;
   }

   public long getInUseFlag() {
      return this.inUseFlag;
   }

   public long getLength() {
      return this.length;
   }

   public StringBuffer getBuffer() {
      return this.buffer;
   }

   public long[] getVpd() {
      return this.vpd;
   }
}
