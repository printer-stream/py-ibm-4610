package com.ibm.posj;

public interface CheckScannerHandle extends Handle {
   CheckScannerCmd.Factory getCheckScannerCmdFactory();

   CheckScannerHandle.State getCheckScannerState();

   CheckScannerHandle.ImageMemory getImageMemory();

   CheckScannerHandle.ImageHeader createImageHeader(short var1, byte[] var2);

   public interface ImageHeader {
      void update(short var1, byte[] var2);

      byte getCompression();

      short getImageWidth();

      short getImageHeight();

      short getImageLocation();

      int getUserImageLocation();

      int getImageHeaderSize();

      int getSize();

      String getTagData();

      boolean getQualityDetectionBit();
   }

   public interface ImageMemory {
      void addImage(CheckScannerHandle.ImageHeader var1);

      boolean existImageHeader(short var1);

      CheckScannerHandle.ImageHeader getImage(short var1);

      boolean existImageHeaderByUserLoc(int var1);

      CheckScannerHandle.ImageHeader getImageByUserLocation(int var1);

      void calculateSize();

      void clearMemory();

      int getImagesNumber();

      long getSize();

      long getTotalSize();

      long getRemainingSize();

      int getRemainingImagesEstimate();
   }

   public interface State extends Handle.State {
      boolean isDocInserted();

      void setDocInserted(boolean var1);

      boolean isDocRegistered();

      void setDocRegistered(boolean var1);

      void setCoverOpen(boolean var1);

      boolean isCoverOpen();
   }
}
