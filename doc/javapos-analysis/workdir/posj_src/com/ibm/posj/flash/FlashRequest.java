package com.ibm.posj.flash;

import com.ibm.jutil.tracing.Tracing;
import com.ibm.posj.Handle;

public class FlashRequest {
   private Handle handle;
   private FlashFile flashFile;
   private short deviceECLevel = -1;
   private int printerID = -1;
   private int printerEC = -1;
   private int printerType = -1;

   public FlashRequest(Handle handle, FlashFile flashFile) {
      this.handle = handle;
      this.flashFile = flashFile;
   }

   public void setDeviceECLevel(short deviceECLevel) {
      Tracing.println("in FlashRequest->setDeviceECLevel():" + deviceECLevel);
      this.deviceECLevel = deviceECLevel;
   }

   public void setRs485PrinterInfo(int printerID, int printerEC, int printerType) {
      this.printerID = printerID;
      this.printerEC = printerEC;
      this.printerType = printerType;
   }

   public int getDeviceECLevel() {
      return this.deviceECLevel;
   }

   public void reset() throws FlashException {
   }

   public FlashFile getFlashFile() {
      return this.flashFile;
   }

   public Handle getHandle() {
      return this.handle;
   }

   public boolean validate4610FlashFileDevType(Handle handle) {
      if (handle.getHandleImp().getClass().getName().indexOf("4610") == -1) {
         return true;
      } else {
         int deviceType = IBM4610FlashUtil.getDeviceType(handle);
         int flashFileDeviceType = this.getFlashFile().getRs485DeviceType();
         return flashFileDeviceType == deviceType;
      }
   }
}
