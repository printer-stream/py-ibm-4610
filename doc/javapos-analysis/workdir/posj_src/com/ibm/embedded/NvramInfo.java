package com.ibm.embedded;

public class NvramInfo {
   private int completionCode;
   private int slotNumber;
   private int offset;
   private int length;
   private byte[] buffer;

   public NvramInfo(int completionCode, int slotNumber, int offset, int length, byte[] buffer) {
      this.completionCode = completionCode;
      this.slotNumber = slotNumber;
      this.offset = offset;
      this.length = length;
      this.buffer = buffer;
   }

   public int getCompletionCode() {
      return this.completionCode;
   }

   public int getSlotNumber() {
      return this.slotNumber;
   }

   public int getOffset() {
      return this.offset;
   }

   public int getLength() {
      return this.length;
   }

   public byte[] getBuffer() {
      return this.buffer;
   }
}
