package com.ibm.posj.printer;

public interface ImageStreamProducer {
   short getNextByteForStation(byte var1);

   void setImageValues(long var1, long var3, long var5, long var7, int[] var9);

   boolean streamDone();

   void freeISP();
}
