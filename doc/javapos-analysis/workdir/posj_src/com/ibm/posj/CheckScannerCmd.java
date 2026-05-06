package com.ibm.posj;

public interface CheckScannerCmd extends HandleCmd {
   void accept(CheckScannerCmdVisitor var1) throws HandleException;

   public interface BeginInsertionCmd extends CheckScannerCmd {
   }

   public interface BeginRemovalCmd extends CheckScannerCmd {
   }

   public interface ChangePrintSideCmd extends CheckScannerCmd {
   }

   public interface EndRemovalCmd extends CheckScannerCmd {
   }

   public interface EraseImagesCmd extends CheckScannerCmd {
   }

   public interface Factory extends SystemCmd.Factory {
      CheckScannerCmd createStartScanCmd(byte var1);

      CheckScannerCmd createBeginInsertionCmd();

      CheckScannerCmd createChangePrintSideCmd();

      CheckScannerCmd createBeginRemovalCmd();

      CheckScannerCmd createPrintScannedImgCmd(short var1, int var2, int var3, short var4, boolean var5);

      CheckScannerCmd createEndRemovalCmd();

      CheckScannerCmd createStoreScannedImgCmd(byte var1, int var2, int var3, int[] var4, int[] var5, String var6);

      CheckScannerCmd createStoreScannedImgCmd(byte var1, int var2, int var3, String var4);

      CheckScannerCmd createGetScannedImgCmd(short var1, int var2, short var3);

      CheckScannerCmd createScannerCalibCmd();

      CheckScannerCmd.GetNextImgLocCmd createGetNextImgLocCmd();

      CheckScannerCmd.GetFirstUnreadImgLocCmd createGetFirstUnreadImgLocCmd();

      CheckScannerCmd createSelCompressionFormatCmd(byte var1, byte var2);

      CheckScannerCmd createEraseImagesCmd();
   }

   public interface GetFirstUnreadImgLocCmd extends CheckScannerCmd {
      short getFirstUnreadImg();

      void setFirstUnreadImg(short var1);
   }

   public interface GetNextImgLocCmd extends CheckScannerCmd {
      short getNextImg();

      void setNextImg(short var1);
   }

   public interface GetScannedImgCmd extends CheckScannerCmd {
      short getLocation();

      int getOffset();

      short getNumBytes();
   }

   public interface PrintScannedImgCmd extends CheckScannerCmd {
      short getLocation();

      int getCorners();

      int getOffset();

      short getScale();

      boolean rotateImage();
   }

   public interface ScannerCalibCmd extends CheckScannerCmd {
   }

   public interface SelCompressionFormatCmd extends CheckScannerCmd {
      byte getFormat();

      byte getContrast();
   }

   public interface StartScanCmd extends CheckScannerCmd {
      boolean isScanAndMicrRead();

      byte getMode();
   }

   public interface StoreScannedImgCmd extends CheckScannerCmd {
      byte getStorageMethod();

      int getCorner();

      int getOffset();

      int[] getCornerSub();

      int[] getOffsetSub();

      String getTagData();
   }
}
