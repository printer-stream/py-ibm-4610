package com.ibm.posj.ram;

import com.ibm.posj.HardTotalsHandle;

public interface RamDevice {
   void setHardTotalsHandle(HardTotalsHandle var1);

   int getUserSize();

   int getDataSize();

   int getFreeDataSize();

   int createFile(String var1, int var2, boolean var3) throws IllegalArgumentException, IllegalAccessException, UnsupportedOperationException;

   void deleteFile(int var1) throws IllegalArgumentException;

   int getNumberOfFiles();

   int getNumberOfClaimedFiles();

   int getHandleByName(String var1);

   int getHandleByIndex(int var1);

   RamFile getFile(int var1) throws IllegalArgumentException;

   void claimRamDevice(String var1);

   String getClaimOwner();

   boolean isClaimedRamDevice();

   void releaseRamDevice();

   boolean claimFile(int var1, long var2);

   boolean isClaimed(int var1);

   void release(int var1);

   void write(int var1, byte[] var2, int var3, int var4) throws IllegalArgumentException;

   void setAll(int var1, byte var2) throws IllegalArgumentException;

   void read(int var1, byte[] var2, int var3, int var4) throws IllegalArgumentException;

   void rename(int var1, String var2) throws IllegalArgumentException, UnsupportedOperationException;

   void checkStatus() throws UnsupportedOperationException;

   boolean restoreFileStructure() throws IllegalArgumentException, IllegalAccessException, UnsupportedOperationException;
}
