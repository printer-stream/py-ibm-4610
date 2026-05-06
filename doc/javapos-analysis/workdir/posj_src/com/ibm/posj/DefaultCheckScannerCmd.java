package com.ibm.posj;

public abstract class DefaultCheckScannerCmd extends AbstractHandleCmd implements CheckScannerCmd {
   private String name = "";
   private int code = 0;
   protected byte[] byteArray = new byte[0];

   DefaultCheckScannerCmd(HandleCmd.Factory factory, String name, int code) {
      super(factory);
      this.name = name;
      this.code = code;
   }

   public String getName() {
      return this.name;
   }

   public int getCode() {
      return this.code;
   }

   public byte[] toBytes() {
      return this.byteArray;
   }

   public void accept(HandleCmdVisitor visitor) {
      visitor.visitCheckScannerCmd(this);
   }

   public abstract void accept(CheckScannerCmdVisitor var1) throws HandleException;

   static class BeginInsertionCmd extends DefaultCheckScannerCmd implements CheckScannerCmd.BeginInsertionCmd {
      BeginInsertionCmd(HandleCmd.Factory factory) {
         super(factory, "BEGIN_INSERTION_CMD", 1300);
      }

      public void accept(CheckScannerCmdVisitor v) throws HandleException {
         v.visitBeginInsertionCmd(this);
      }
   }

   static class BeginRemovalCmd extends DefaultCheckScannerCmd implements CheckScannerCmd.BeginRemovalCmd {
      BeginRemovalCmd(HandleCmd.Factory factory) {
         super(factory, "BEGIN_REMOVAL_CMD", 1302);
      }

      public void accept(CheckScannerCmdVisitor v) throws HandleException {
         v.visitBeginRemovalCmd(this);
      }
   }

   static class ChangePrintSideCmd extends DefaultCheckScannerCmd implements CheckScannerCmd.ChangePrintSideCmd {
      ChangePrintSideCmd(HandleCmd.Factory factory) {
         super(factory, "CHANGE_PRINT_SIDE_CMD", 1313);
      }

      public void accept(CheckScannerCmdVisitor v) throws HandleException {
         v.visitChangePrintSideCmd(this);
      }
   }

   static class EndRemovalCmd extends DefaultCheckScannerCmd implements CheckScannerCmd.EndRemovalCmd {
      EndRemovalCmd(HandleCmd.Factory factory) {
         super(factory, "END_REMOVAL_CMD", 1303);
      }

      public void accept(CheckScannerCmdVisitor v) throws HandleException {
         v.visitEndRemovalCmd(this);
      }
   }

   static class EraseImagesCmd extends DefaultCheckScannerCmd implements CheckScannerCmd.EraseImagesCmd {
      EraseImagesCmd(HandleCmd.Factory factory) {
         super(factory, "ERASE_IMAGES_CMD", 1311);
      }

      public void accept(CheckScannerCmdVisitor v) throws HandleException {
         v.visitEraseImagesCmd(this);
      }
   }

   static class Factory extends DefaultSystemCmd.Factory implements CheckScannerCmd.Factory {
      public CheckScannerCmd createStartScanCmd(byte mode) {
         return new DefaultCheckScannerCmd.StartScanCmd(this, mode);
      }

      public CheckScannerCmd createBeginInsertionCmd() {
         return new DefaultCheckScannerCmd.BeginInsertionCmd(this);
      }

      public CheckScannerCmd createBeginRemovalCmd() {
         return new DefaultCheckScannerCmd.BeginRemovalCmd(this);
      }

      public CheckScannerCmd createPrintScannedImgCmd(short loc, int top_left, int offset, short scale, boolean option) {
         return new DefaultCheckScannerCmd.PrintScannedImgCmd(this, loc, top_left, offset, scale, option);
      }

      public CheckScannerCmd createEndRemovalCmd() {
         return new DefaultCheckScannerCmd.EndRemovalCmd(this);
      }

      public CheckScannerCmd createStoreScannedImgCmd(byte method, int top_left, int offset, int[] top_left_sub, int[] offset_sub, String tag) {
         return new DefaultCheckScannerCmd.StoreScannedImgCmd(this, method, top_left, offset, top_left_sub, offset_sub, tag);
      }

      public CheckScannerCmd createStoreScannedImgCmd(byte method, int top_left, int offset, String tag) {
         return new DefaultCheckScannerCmd.StoreScannedImgCmd(this, method, top_left, offset, tag);
      }

      public CheckScannerCmd createGetScannedImgCmd(short loc, int from, short num) {
         return new DefaultCheckScannerCmd.GetScannedImgCmd(this, loc, from, num);
      }

      public CheckScannerCmd createScannerCalibCmd() {
         return new DefaultCheckScannerCmd.ScannerCalibCmd(this);
      }

      public CheckScannerCmd.GetNextImgLocCmd createGetNextImgLocCmd() {
         return new DefaultCheckScannerCmd.GetNextImgLocCmd(this);
      }

      public CheckScannerCmd.GetFirstUnreadImgLocCmd createGetFirstUnreadImgLocCmd() {
         return new DefaultCheckScannerCmd.GetFirstUnreadImgLocCmd(this);
      }

      public CheckScannerCmd createSelCompressionFormatCmd(byte format, byte contrast) {
         return new DefaultCheckScannerCmd.SelCompressionFormatCmd(this, format, contrast);
      }

      public CheckScannerCmd createEraseImagesCmd() {
         return new DefaultCheckScannerCmd.EraseImagesCmd(this);
      }

      public CheckScannerCmd createChangePrintSideCmd() {
         return new DefaultCheckScannerCmd.ChangePrintSideCmd(this);
      }
   }

   static class GetFirstUnreadImgLocCmd extends DefaultCheckScannerCmd implements CheckScannerCmd.GetFirstUnreadImgLocCmd {
      private short nextImg = 0;

      GetFirstUnreadImgLocCmd(HandleCmd.Factory factory) {
         super(factory, "GET_FIRST_UNREAD_IMG_LOC_CMD", 1309);
      }

      public void accept(CheckScannerCmdVisitor v) throws HandleException {
         v.visitGetFirstUnreadImgLocCmd(this);
      }

      public short getFirstUnreadImg() {
         return this.nextImg;
      }

      public void setFirstUnreadImg(short img) {
         this.nextImg = img;
      }
   }

   static class GetNextImgLocCmd extends DefaultCheckScannerCmd implements CheckScannerCmd.GetNextImgLocCmd {
      private short nextImg = 0;

      GetNextImgLocCmd(HandleCmd.Factory factory) {
         super(factory, "GET_NEXT_IMG_LOC_CMD", 1308);
      }

      public void accept(CheckScannerCmdVisitor v) throws HandleException {
         v.visitGetNextImgLocCmd(this);
      }

      public short getNextImg() {
         return this.nextImg;
      }

      public void setNextImg(short img) {
         this.nextImg = img;
      }
   }

   static class GetScannedImgCmd extends DefaultCheckScannerCmd implements CheckScannerCmd.GetScannedImgCmd {
      private short location;
      private int offset;
      private short numBytes;

      GetScannedImgCmd(HandleCmd.Factory factory, short loc, int from, short num) {
         super(factory, "GET_SCANNED_IMG_CMD", 1306);
         this.location = loc;
         this.offset = from;
         this.numBytes = num;
      }

      public void accept(CheckScannerCmdVisitor v) throws HandleException {
         v.visitGetScannedImgCmd(this);
      }

      public short getLocation() {
         return this.location;
      }

      public int getOffset() {
         return this.offset;
      }

      public short getNumBytes() {
         return this.numBytes;
      }
   }

   static class PrintScannedImgCmd extends DefaultCheckScannerCmd implements CheckScannerCmd.PrintScannedImgCmd {
      private short location;
      private int corners;
      private int offset;
      private short scale;
      private boolean rotate;

      PrintScannedImgCmd(HandleCmd.Factory factory, short loc, int top_left, int offset, short scale, boolean option) {
         super(factory, "PRINT_SCANNED_IMG_CMD", 1304);
         this.location = loc;
         this.corners = top_left;
         this.offset = offset;
         this.scale = scale;
         this.rotate = option;
      }

      public void accept(CheckScannerCmdVisitor v) throws HandleException {
         v.visitPrintScannedImgCmd(this);
      }

      public short getLocation() {
         return this.location;
      }

      public int getCorners() {
         return this.corners;
      }

      public int getOffset() {
         return this.offset;
      }

      public short getScale() {
         return this.scale;
      }

      public boolean rotateImage() {
         return this.rotate;
      }
   }

   static class ScannerCalibCmd extends DefaultCheckScannerCmd implements CheckScannerCmd.ScannerCalibCmd {
      ScannerCalibCmd(HandleCmd.Factory factory) {
         super(factory, "SCANNER_CALIB_CMD", 1307);
      }

      public void accept(CheckScannerCmdVisitor v) throws HandleException {
         v.visitScannerCalibCmd(this);
      }
   }

   static class SelCompressionFormatCmd extends DefaultCheckScannerCmd implements CheckScannerCmd.SelCompressionFormatCmd {
      private byte format;
      private byte contrast;

      SelCompressionFormatCmd(HandleCmd.Factory factory, byte frmt, byte ctrst) {
         super(factory, "SEL_COMPRESSION_FORMAT_CMD", 1310);
         this.format = frmt;
         this.contrast = ctrst;
      }

      public void accept(CheckScannerCmdVisitor v) throws HandleException {
         v.visitSelCompressionFormatCmd(this);
      }

      public byte getFormat() {
         return this.format;
      }

      public byte getContrast() {
         return this.contrast;
      }
   }

   static class StartScanCmd extends DefaultCheckScannerCmd implements CheckScannerCmd.StartScanCmd {
      private boolean isScanAndMicrRead;
      private byte mode;

      public StartScanCmd(HandleCmd.Factory factory, byte mode) {
         super(factory, "START_SCAN_CMD", 1301);
         switch (mode) {
            case 1:
            case 3:
            case 4:
               this.isScanAndMicrRead = true;
               break;
            case 2:
            default:
               this.isScanAndMicrRead = false;
         }

         this.mode = mode;
      }

      public void accept(CheckScannerCmdVisitor v) throws HandleException {
         v.visitStartScanCmd(this);
      }

      public boolean isScanAndMicrRead() {
         return this.isScanAndMicrRead;
      }

      public byte getMode() {
         return this.mode;
      }
   }

   static class StoreScannedImgCmd extends DefaultCheckScannerCmd implements CheckScannerCmd.StoreScannedImgCmd {
      private byte storageMethod = 0;
      private int corner = 0;
      private int offset = 0;
      private int[] cornerSub = new int[]{-1, -1, -1};
      private int[] offsetSub = new int[]{-1, -1, -1};
      private String tagData = null;

      StoreScannedImgCmd(HandleCmd.Factory factory, byte method, int top_left, int offset, int[] top_left_sub, int[] offset_sub, String tag) {
         super(factory, "STORE_SCAN_IMG_CMD", 1305);
         this.storageMethod = method;
         this.corner = top_left;
         this.offset = offset;
         this.cornerSub = top_left_sub;
         this.offsetSub = offset_sub;
         this.tagData = tag;
      }

      StoreScannedImgCmd(HandleCmd.Factory factory, byte method, int top_left, int offset, String tag) {
         super(factory, "STORE_SCAN_IMG_CMD", 1305);
         this.storageMethod = method;
         this.corner = top_left;
         this.offset = offset;
         this.tagData = tag;
      }

      public void accept(CheckScannerCmdVisitor v) throws HandleException {
         v.visitStoreScannedImgCmd(this);
      }

      public byte getStorageMethod() {
         return this.storageMethod;
      }

      public int getCorner() {
         return this.corner;
      }

      public int getOffset() {
         return this.offset;
      }

      public int[] getCornerSub() {
         return this.cornerSub;
      }

      public int[] getOffsetSub() {
         return this.offsetSub;
      }

      public String getTagData() {
         return this.tagData;
      }
   }
}
