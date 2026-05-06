package com.ibm.posj.util;

public class CacheVectorEntry {
   public int station;
   public int alignment;
   public String message;
   public byte[] bitmap;
   public boolean downloaded;
   public int number;
   public int index;
   public int type;

   public CacheVectorEntry(int station, int alignment, String data, boolean downloaded, int number, int index, int type) {
      this.station = station;
      this.alignment = alignment;
      this.message = new String(data);
      this.downloaded = downloaded;
      this.number = number;
      this.index = index;
      this.type = type;
      this.bitmap = null;
   }

   public CacheVectorEntry(int station, int alignment, byte[] bitmap, boolean downloaded, int number, int index, int type) {
      this.station = station;
      this.alignment = alignment;
      this.message = null;
      this.downloaded = downloaded;
      this.number = number;
      this.index = index;
      this.type = type;
      this.bitmap = bitmap;
   }
}
