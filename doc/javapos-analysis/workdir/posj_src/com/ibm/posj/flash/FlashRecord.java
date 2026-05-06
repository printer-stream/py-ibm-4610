package com.ibm.posj.flash;

public interface FlashRecord {
   int getLenght();

   void setLenght(int var1);

   void setRecordData(byte[] var1);

   byte[] getRecordData();
}
