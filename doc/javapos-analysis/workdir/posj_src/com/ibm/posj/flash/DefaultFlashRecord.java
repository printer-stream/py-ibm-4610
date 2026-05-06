package com.ibm.posj.flash;

public class DefaultFlashRecord implements FlashRecord {
   private byte[] data = new byte[0];

   public DefaultFlashRecord(byte[] byteArray) {
      this.data = byteArray;
   }

   public int getLenght() {
      return this.data.length;
   }

   public void setLenght(int length) {
   }

   public void setRecordData(byte[] data) {
      this.data = data;
   }

   public byte[] getRecordData() {
      return this.data;
   }
}
