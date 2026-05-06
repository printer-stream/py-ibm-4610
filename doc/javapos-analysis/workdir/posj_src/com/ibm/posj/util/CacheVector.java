package com.ibm.posj.util;

import java.util.Vector;

public class CacheVector extends Vector {
   public static int BITMAP = 1;
   public static int MESSAGE = 2;

   public void add(CacheVectorEntry newEntry) {
      if (this.exists(newEntry.type, newEntry.station, newEntry.number)) {
         this.remove(newEntry.type, newEntry.station, newEntry.number);
      }

      super.addElement(newEntry);
   }

   public void remove(int type, int station, int number) {
      CacheVectorEntry entry = this.get(type, station, number);
      if (entry != null) {
         super.removeElement(entry);
      }
   }

   public boolean exists(int type, int station, int number) {
      return this.get(type, station, number) != null;
   }

   public CacheVectorEntry get(int type, int station, int number) {
      int index = this.indexOf(type, station, number);
      return index >= 0 ? (CacheVectorEntry)this.elementAt(index) : (CacheVectorEntry)null;
   }

   public boolean isEmpty(int type) {
      boolean returnValue = true;

      for (int index = 0; index < this.elementCount && returnValue; index++) {
         if (((CacheVectorEntry)this.elementAt(index)).type == type) {
            returnValue = false;
         }
      }

      return returnValue;
   }

   public void setDownloaded(int type, int station, int number, boolean downloaded) {
      int index = this.indexOf(type, station, number);
      if (index >= 0) {
         ((CacheVectorEntry)this.elementAt(index)).downloaded = downloaded;
      }
   }

   public int indexOf(int type, int station, int number) {
      for (int index = 0; index < this.elementCount; index++) {
         if (((CacheVectorEntry)this.elementAt(index)).number == number
            && ((CacheVectorEntry)this.elementAt(index)).station == station
            && ((CacheVectorEntry)this.elementAt(index)).type == type) {
            return index;
         }
      }

      return -1;
   }

   public void clearAllDownloaded(int type) {
      for (int index = 0; index < this.elementCount; index++) {
         if (((CacheVectorEntry)this.elementAt(index)).type == type) {
            ((CacheVectorEntry)this.elementAt(index)).downloaded = false;
         }
      }
   }

   public void removeAllElements(int type) {
      int index = 0;
      boolean foundOne = true;

      while (foundOne) {
         foundOne = false;

         for (int var4 = 0; var4 < this.elementCount; var4++) {
            if (((CacheVectorEntry)this.elementAt(var4)).type == type) {
               this.removeElementAt(var4);
               foundOne = true;
               break;
            }
         }
      }
   }
}
