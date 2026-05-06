package com.ibm.posj.flash;

import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevCat;

public interface FlashFile {
   void load() throws FlashException;

   void loadHeader() throws FlashException;

   int getVersion();

   int getUsbPid();

   int getRs485BootLevel();

   int getRs485DeviceType();

   DevBus getDevBus();

   DevCat getDevCat();

   boolean equals(FlashFormat var1);

   FlashFormat getFlashFormat();

   boolean isFlashFileVersionNewer(int var1);

   boolean isProductIDMatched(int var1);

   boolean isUSBHardwareLevelMatched(int var1);

   boolean isDevBusMatched(DevBus var1);

   boolean isDevCatMatched(DevCat var1);

   byte[] getCheckSum();

   String getFilename();
}
