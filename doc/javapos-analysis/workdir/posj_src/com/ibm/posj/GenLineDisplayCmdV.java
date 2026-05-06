package com.ibm.posj;

public class GenLineDisplayCmdV extends DefaultLineDisplayCmdV {
   public byte[] data = new byte[20];
   public byte[] checkData = new byte[20];
   public HandleException handleException = null;
   public byte[] shortCmd = new byte[2];
   public byte[] characterSetCmd = new byte[2];
   public byte[] writeTopLineCmd = new byte[23];
   public byte[] writeBottomLineCmd = new byte[23];
   public byte[] writeRamCmd = new byte[10];
   public byte[] writeMarksCmd = new byte[5];
   public static final byte DISP_CHAR_SPACE = 32;
   public static final int DISP_LENGTH_DATA = 20;
   public byte[][] writeRamDisplayBKP = new byte[][]{
      {0, 0, 0, 0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0, 0, 0, 0}
   };
   protected boolean isClearLastCmdSubmitted = false;
   public static final byte CLEAR_TOP_LINE_CMD = 1;
   public static final byte CLEAR_BOTTOM_LINE_CMD = 2;
   public static final byte WRITE_TOP_LINE_CMD = -127;
   public static final byte WRITE_BOTTOM_LINE_CMD = -126;
   public static final byte WRITE_RAM_CMD = 64;
   public static final byte[] UNDERLINE_CURSOR_CMD = new byte[]{16, 0};
   public static final byte[] BOX_CURSOR_CMD = new byte[]{16, 1};
   public static final byte MOVE_CURSOR_CMD = 32;
   public static final byte WRITE_TRIANGLE_CMD = 33;
   public static final byte SET_CHARACTER_SET_CMD = 65;
   public static final byte[] TEST_REQUEST_CMD = new byte[]{0, 16};
   public static final byte[] STATUS_REQUEST_CMD = new byte[]{0, 32};
   public static final byte[] RESET_CMD = new byte[]{0, 64};
   public static final byte[] DEVICE_INFO_REQUEST = new byte[]{0, 0, 1};
   public static final int WRITE_LINE_DATA_LENGTH = 20;
   public static final int WRITE_RAM_DATA_LENGTH = 8;
   public static final int INDICATORS_LENGTH = 4;

   public void clearHandleException() {
      this.handleException = null;
   }

   public HandleException getHandleException() {
      return this.handleException;
   }

   public void visitClearDisplayCmd(LineDisplayCmd cmd) {
      try {
         LineDisplayCmd.ClearDisplayCmd cdC = (LineDisplayCmd.ClearDisplayCmd)cmd;
         this.shortCmd[0] = 1;
         this.shortCmd[1] = cdC.getCursorLocation();
         this.submit(this.shortCmd, "Could not Clear Top Line");
         this.shortCmd[0] = 2;
         this.shortCmd[1] = cdC.getCursorLocation();
         this.submit(this.shortCmd, "Could not Clear Botton Line");
         this.setIsClearLastCmd(true);
      } catch (HandleException var3) {
         this.handleException = new HandleException("could not clear Display", var3);
      }
   }

   public void visitWriteDisplayCmd(LineDisplayCmd cmd) {
      try {
         LineDisplayCmd.WriteDisplayCmd wdC = (LineDisplayCmd.WriteDisplayCmd)cmd;
         this.checkData = this.setDisplayRowData(wdC.getData());
         if (wdC.getRowPosition() == 0) {
            this.writeTopLineCmd[0] = -127;
            this.writeTopLineCmd[1] = wdC.getCursorLocation();
            System.arraycopy(this.checkData, 0, this.writeTopLineCmd, 2, 20);
            this.writeTopLineCmd[22] = this.checkSum(this.checkData, 20);
            this.submit(this.writeTopLineCmd, "Could not write top line");
         } else if (wdC.getRowPosition() == 1) {
            this.writeBottomLineCmd[0] = -126;
            this.writeBottomLineCmd[1] = wdC.getCursorLocation();
            System.arraycopy(this.checkData, 0, this.writeBottomLineCmd, 2, 20);
            this.writeBottomLineCmd[22] = this.checkSum(this.checkData, 20);
            this.submit(this.writeBottomLineCmd, "Could not write bottom line");
         }
      } catch (HandleException var3) {
         this.handleException = new HandleException("could not write to Display", var3);
      }
   }

   public void visitSetCharacterSetCmd(LineDisplayCmd cmd) {
      try {
         LineDisplayCmd.SetCharacterSetCmd scsC = (LineDisplayCmd.SetCharacterSetCmd)cmd;
         this.characterSetCmd[0] = 65;
         switch (scsC.getCharacterSet()) {
            case 0:
               this.characterSetCmd[1] = 0;
               break;
            case 1:
               this.characterSetCmd[1] = 10;
               break;
            case 2:
               this.characterSetCmd[1] = 3;
               break;
            case 3:
               this.characterSetCmd[1] = 4;
               break;
            case 4:
               this.characterSetCmd[1] = 5;
               break;
            case 5:
               this.characterSetCmd[1] = 2;
               break;
            case 6:
               this.characterSetCmd[1] = 6;
               break;
            case 7:
               this.characterSetCmd[1] = 7;
               break;
            case 8:
               this.characterSetCmd[1] = 8;
               break;
            case 9:
               this.characterSetCmd[1] = 9;
               break;
            case 10:
               this.characterSetCmd[1] = 11;
               break;
            case 11:
               this.characterSetCmd[1] = 1;
               break;
            default:
               throw new HandleException("invalid character set to set");
         }

         this.submit(this.characterSetCmd, "Could not set character set");
      } catch (HandleException var3) {
         this.handleException = new HandleException("could not set character set", var3);
      }
   }

   public void visitWriteTriangleMarksCmd(LineDisplayCmd cmd) {
      try {
         LineDisplayCmd.WriteTriangleMarksCmd wtmC = (LineDisplayCmd.WriteTriangleMarksCmd)cmd;
         this.writeMarksCmd[0] = 33;
         System.arraycopy(wtmC.getIndicators(), 0, this.writeMarksCmd, 1, 4);
         this.submit(this.writeMarksCmd, "Could not write triangle marks");
      } catch (HandleException var3) {
         this.handleException = new HandleException("could not set triangleMarks", var3);
      }
   }

   public void visitTestLDRequestCmd(LineDisplayCmd cmd) {
      try {
         this.submit(TEST_REQUEST_CMD, "Could not test LineDisplay");
      } catch (HandleException var3) {
         this.handleException = new HandleException("could not test Display", var3);
      }
   }

   public void visitBrightnessCmd(LineDisplayCmd cmd) {
      throw new RuntimeException("Invalid LineDisplayCmd submitted");
   }

   public void visitScreenModeCmd(LineDisplayCmd cmd) {
      throw new RuntimeException("Invalid LineDisplayCmd submitted");
   }

   public void visitDisplayModeCmd(LineDisplayCmd cmd) {
      throw new RuntimeException("Invalid LineDisplayCmd submitted");
   }

   public void visitCursorModeCmd(LineDisplayCmd cmd) {
      throw new RuntimeException("Invalid LineDisplayCmd submitted");
   }

   public void visitUserDefinableCharacterCmd(LineDisplayCmd cmd) {
      try {
         LineDisplayCmd.UserDefinableCharacterCmd udcC = (LineDisplayCmd.UserDefinableCharacterCmd)cmd;
         this.writeRamCmd[0] = 64;
         this.writeRamCmd[1] = (byte)udcC.getGlyphCode();

         for (int i = 0; i < udcC.getGlyph().length; i++) {
            this.writeRamCmd[2 + i] = udcC.getGlyph()[i];
         }

         for (int i = 0; i < udcC.getGlyph().length; i++) {
            this.writeRamDisplayBKP[(byte)udcC.getGlyphCode()][i] = this.writeRamCmd[i];
         }

         this.submit(this.writeRamCmd, "Could not set write ram cmd");
      } catch (HandleException var4) {
         this.handleException = new HandleException("could not set user definable character", var4);
      }
   }

   public void visitDisplayTextModeCmd(LineDisplayCmd cmd) {
      throw new RuntimeException("Invalid LineDisplayCmd submitted");
   }

   protected byte[] setDisplayRowData(byte[] b) throws IllegalArgumentException {
      if (b != null && b.length <= 20) {
         System.arraycopy(b, 0, this.data, 0, b.length);
         int index = b.length;

         while (index < 20) {
            this.data[index++] = 32;
         }

         return this.data;
      } else {
         throw new IllegalArgumentException("Invalid data string length for WriteDisplayCmd");
      }
   }

   protected byte checkSum(byte[] data, int length) {
      byte checkSum = data[0];

      for (int i = 1; i < length; i++) {
         checkSum ^= data[i];
      }

      return checkSum;
   }

   protected void submit(byte[] data, String errorMsg) throws HandleException {
   }

   public boolean getIsClearLastCmd() {
      return this.isClearLastCmdSubmitted;
   }

   public void setIsClearLastCmd(boolean lastCmd) {
      this.isClearLastCmdSubmitted = lastCmd;
   }
}
