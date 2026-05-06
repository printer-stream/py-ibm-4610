package com.ibm.posj.bus.rs232;

import com.ibm.jutil.tasks.Mapping;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.LineDisplayCmd;
import com.ibm.posj.SystemCmd;
import com.ibm.rs232.Rs232Port;

public class Rs232VFDLineDisplayHandleImp extends Rs232LineDisplayHandleImp {
   private Mapping mapping = new Mapping();
   private boolean isCursorOn;
   private byte[] userCharacterDefinitionCmd = new byte[10];
   public static final int WRITE_LINE_DATA_LENGTH = 20;
   public static final byte[] BRIGHTNESS_CMD = new byte[]{4, 0};
   public static final int BRIGHTNESS_LEVEL1_CMD = 32;
   public static final int BRIGHTNESS_LEVEL2_CMD = 64;
   public static final int BRIGHTNESS_LEVEL3_CMD = 96;
   public static final int BRIGHTNESS_LEVEL4_CMD = 255;
   public static final byte[] CHARACTER_SET_CMD = new byte[]{2, 0};
   public static final int SET_ENGLISH = 0;
   public static final int SET_KATAKANA = 1;
   public static final int MULTILINGUAL_INTERNATIONAL = 2;
   public static final int SET_CENTRAL_EUROPE = 3;
   public static final int SET_CYRILLIC = 4;
   public static final int SET_TURKEY = 5;
   public static final int SET_ISRAEL = 6;
   public static final int SET_CANADIAN_FRENCH = 7;
   public static final int SET_ARABIC = 8;
   public static final int SET_NORDIC = 9;
   public static final int SET_CYRILLIC_RUSSIA = 10;
   public static final int SET_GREECE = 11;
   public static final int SET_ASCII = 0;
   public static final byte[] CLEAR_DISPLAY_CMD = new byte[]{
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32,
      32
   };
   public static final byte[] CURSOR_OFF_CMD = new byte[]{20};
   public static final byte[] CURSOR_ON_CMD = new byte[]{19};
   public static final byte[] DISPLAY_POSITION_CMD = new byte[]{16, 0};
   public static final byte[] RESET = new byte[]{31};
   public static final byte[] IBM_EMULATION_MODE = new byte[]{0, 1};
   public static final byte[] NORM_SCROLL_CTRL_MODE = new byte[]{17};
   public static final byte USER_CHARACTER_DEFINITION_CMD = 3;
   public static final int USER_CHARACTER_DEFINITION_DATA_LENGTH = 8;
   public static final int DISP_CS_KATAKANA = 1;
   public static final int DISP_CS_M_INTERNATIONAL = 2;
   public static final int DISP_CS_CENTRAL_EUROPE = 3;
   public static final int DISP_CS_CYRILLIC = 4;
   public static final int DISP_CS_TURKEY = 5;
   public static final int DISP_CS_ISRAEL = 0;
   public static final int DISP_CS_CANADIAN_FRENCH = 1;
   public static final int DISP_CS_ARABIC = 2;
   public static final int DISP_CS_NORDIC = 3;
   public static final int DISP_CS_CYRILLIC_RUSSIA = 4;
   public static final int DISP_CS_GRECE = 5;
   public static final int DISP_CS_ASCII = 0;

   public Rs232VFDLineDisplayHandleImp(HandleKey key, Rs232Port rs232Port, byte cursorState) {
      super(key, rs232Port);
      this.setCursorStateValue(cursorState);
   }

   public void init() throws HandleException {
      super.init();
      this.submit(RESET);
      this.submit(IBM_EMULATION_MODE);
      this.submit(NORM_SCROLL_CTRL_MODE);
      this.initiateCursor();
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
      cmd.setDeviceId(3019);
      cmd.setDeviceType(3002);
   }

   protected void visitWriteDisplayCmd(LineDisplayCmd cmd) {
      if (this.isCursorOn()) {
         this.submitCursorState((byte)0);
      }

      LineDisplayCmd.WriteDisplayCmd wdC = (LineDisplayCmd.WriteDisplayCmd)cmd;
      if (wdC.getRowPosition() == 0) {
         DISPLAY_POSITION_CMD[1] = 0;
         this.submit(DISPLAY_POSITION_CMD, "can not submit display position cmd");
         this.submit(wdC.getData(), "data not found");
         this.submit(this.updateCursorPosition(wdC.getRowPosition(), wdC.getCursorLocation()), "UpdateCursorPositionCmd failed");
      } else if (wdC.getRowPosition() == 1) {
         DISPLAY_POSITION_CMD[1] = 20;
         this.submit(DISPLAY_POSITION_CMD, "can not submit display position cmd");
         this.submit(wdC.getData(), "data not found");
         this.submit(this.updateCursorPosition(wdC.getRowPosition(), wdC.getCursorLocation()), "UpdateCursorPositionCmd failed");
      }

      if (this.isCursorOn()) {
         this.submitCursorState((byte)1);
      }
   }

   protected void visitBrightnessCmd(LineDisplayCmd cmd) {
      LineDisplayCmd.BrightnessCmd bC = (LineDisplayCmd.BrightnessCmd)cmd;
      int brightness_mapped = this.mapping.mapValue(bC.getBrightness(), this.getBrightnessValues());
      BRIGHTNESS_CMD[1] = (byte)brightness_mapped;
      this.submit(BRIGHTNESS_CMD, "Could not set Brightness cmd");
   }

   protected void visitClearDisplayCmd(LineDisplayCmd cmd) {
      if (this.isCursorOn()) {
         this.submitCursorState((byte)0);
      }

      DISPLAY_POSITION_CMD[1] = 0;
      this.submit(DISPLAY_POSITION_CMD, "can not submit display position cmd");
      this.submit(CLEAR_DISPLAY_CMD, "Could not submit clear cmd");
      this.submit(DISPLAY_POSITION_CMD, "Could not submit clear cmd");
      if (this.isCursorOn()) {
         this.submitCursorState((byte)1);
      }
   }

   protected void visitWriteTriangleMarksCmd(LineDisplayCmd cmd) {
      throw new RuntimeException("Unsupported LineDisplay Cmd");
   }

   protected void visitSetCharacterSetCmd(LineDisplayCmd cmd) {
      LineDisplayCmd.SetCharacterSetCmd scsC = (LineDisplayCmd.SetCharacterSetCmd)cmd;
      boolean charSetfound = false;
      int charSetCounter = 0;

      int correspondingCharSet;
      for (correspondingCharSet = -1; charSetCounter < this.getCharacterSetValues()[0].length && !charSetfound; charSetCounter++) {
         if (scsC.getCharacterSet() == this.getCharacterSetValues()[0][charSetCounter]) {
            charSetfound = true;
            correspondingCharSet = this.getCharacterSetValues()[1][charSetCounter];
         }
      }

      if (correspondingCharSet != -1) {
         CHARACTER_SET_CMD[1] = (byte)correspondingCharSet;
         this.submit(CHARACTER_SET_CMD, "Could not set characterSetCmd");
      } else {
         this.handleException = new HandleException("Unsupported set character set value to set");
      }
   }

   protected void visitScreenModeCmd(LineDisplayCmd cmd) {
      throw new RuntimeException("Invalid LineDisplayCmd submitted");
   }

   protected void visitDisplayModeCmd(LineDisplayCmd cmd) {
      throw new RuntimeException("Invalid LineDisplayCmd submitted");
   }

   protected void visitCursorModeCmd(LineDisplayCmd cmd) {
      LineDisplayCmd.CursorModeCmd cmC = (LineDisplayCmd.CursorModeCmd)cmd;
      switch (cmC.getCursorMode()) {
         case 0:
            this.submit(CURSOR_OFF_CMD, "Could not set cursorOFFcmd");
            this.setIsCursorOn(false);
            break;
         case 4:
            this.submit(CURSOR_ON_CMD, "Could not set cursorONcmd");
            this.setIsCursorOn(true);
            break;
         default:
            this.handleException = new HandleException("Unsupported cursor mode value to set");
      }
   }

   protected void visitUserDefinableCharacterCmd(LineDisplayCmd cmd) {
      LineDisplayCmd.UserDefinableCharacterCmd udcC = (LineDisplayCmd.UserDefinableCharacterCmd)cmd;
      int glyph = 0;
      this.userCharacterDefinitionCmd[0] = 3;
      this.userCharacterDefinitionCmd[1] = (byte)udcC.getGlyphCode();
      this.userCharacterDefinitionCmd[9] = 0;

      for (int i = 0; i < udcC.getGlyph().length; i++) {
         int var5 = udcC.getGlyph()[i];
         var5 <<= 3;
         this.userCharacterDefinitionCmd[2 + i] = (byte)var5;
      }

      this.submit(this.userCharacterDefinitionCmd, "Could not set UserDefinableCharacterCmd");
   }

   protected void visitDisplayTextModeCmd(LineDisplayCmd cmd) {
      throw new RuntimeException("Invalid LineDisplayCmd submitted");
   }

   protected void initiateCursor() {
      this.submitCursorState(this.getCursorStateValue());
      if (this.getCursorStateValue() == 0) {
         this.isCursorOn = false;
      } else {
         this.isCursorOn = true;
      }
   }

   protected void submitCursorState(byte cursor) {
      switch (cursor) {
         case 0:
            this.submit(CURSOR_OFF_CMD, "Could not set cursorOFFcmd");
            break;
         case 1:
            this.submit(CURSOR_ON_CMD, "Could not set cursorONcmd");
            break;
         default:
            this.handleException = new HandleException("Unsupported cursor mode value to set");
      }
   }

   protected boolean isCursorOn() {
      return this.isCursorOn;
   }

   protected void setIsCursorOn(boolean cursor) {
      this.isCursorOn = cursor;
   }

   private int[][] getCharacterSetValues() {
      return new int[][]{{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, {0, 10, 3, 4, 5, 2, 6, 7, 8, 9, 11, 1, 0}};
   }

   private int[][] getBrightnessValues() {
      return new int[][]{{20, 40, 60, 100}, {32, 64, 96, 255}};
   }

   private byte[] updateCursorPosition(byte row, byte cursorLocation) {
      if (cursorLocation != 39 && cursorLocation != 40) {
         DISPLAY_POSITION_CMD[1] = cursorLocation;
      } else {
         DISPLAY_POSITION_CMD[1] = 39;
      }

      return DISPLAY_POSITION_CMD;
   }
}
