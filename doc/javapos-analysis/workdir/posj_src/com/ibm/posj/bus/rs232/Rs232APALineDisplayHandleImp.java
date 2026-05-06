package com.ibm.posj.bus.rs232;

import com.ibm.jutil.tasks.Mapping;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.LineDisplayCmd;
import com.ibm.posj.SystemCmd;
import com.ibm.rs232.Rs232Port;

public class Rs232APALineDisplayHandleImp extends Rs232LineDisplayHandleImp {
   public byte[] updatePosition = new byte[0];
   protected byte numberOfRows;
   private Mapping mapping = new Mapping();
   private boolean isCursorOn;
   private byte[] userCharacterDefinitionCmd;
   public static final byte[] BRIGHTNESS_CMD = new byte[]{27, 92, 63, 76, 68, 0};
   public static final int BRIGHTNESS_LEVEL0_CMD = 48;
   public static final int BRIGHTNESS_LEVEL1_CMD = 49;
   public static final int BRIGHTNESS_LEVEL2_CMD = 50;
   public static final int BRIGHTNESS_LEVEL3_CMD = 51;
   public static final int BRIGHTNESS_LEVEL4_CMD = 52;
   public static final int BRIGHTNESS_LEVEL5_CMD = 53;
   public static final byte[] CHARACTER_SET_CMD = new byte[]{27, 83, 0};
   public static final int SET_ENGLISH = 48;
   public static final int SET_JAPANESE = 49;
   public static final int SET_KOREAN = 50;
   public static final int SET_SIMPLIFIED_CHINESE = 51;
   public static final int SET_TRADITIONAL_CHINESE = 52;
   public static final int SET_ASCII = 48;
   public static final byte[] CURSOR_MODE_CMD = new byte[]{27, 92, 63, 76, 67, 0};
   public static final int CURSOR_MODE_NO_LIGHTING = 48;
   public static final int CURSOR_MODE_BLINKING = 49;
   public static final int CURSOR_MODE_LIGHTING = 50;
   public static final byte[] CLEAR_DISPLAY_CMD = new byte[]{27, 91, 50, 74};
   public static final byte[] DISPLAY_HOME_POSITION_CMD = new byte[]{27, 91, 72, 39};
   public static final byte[] DISPLAY_POSITION_LEFT10_CMD = new byte[]{27, 91, 0, 59, 0, 72};
   public static final byte[] DISPLAY_POSITION_RIGHT10_CMD = new byte[]{27, 91, 0, 59, 49, 0, 72};
   public static final byte[] DISPLAY_MODE_CMD = new byte[]{27, 91, 0, 109};
   public static final int DISPLAY_MODE_NORMAL = 48;
   public static final int DISPLAY_MODE_BLINKING = 53;
   public static final int DISPLAY_MODE_REVERSE = 55;
   public static final byte[] SCREEN_MODE_CMD = new byte[]{27, 92, 63, 76, 83, 0};
   public static final int SCREEN_MODE_5_BY_7_DOTS_4ROWS = 54;
   public static final int SCREEN_MODE_5_BY_7_DOTS_5ROWS = 55;
   public static final int SCREEN_MODE_16_BY_16_DOTS_2ROWS = 56;
   public static final byte[] USER_DEFINABLE_FONT_SET = new byte[]{27, 92, 63, 76, 87, 0, 59, 0, 59, 0, 59};
   public static final int FONT_SIZE_5_BY_7_DOTS = 49;
   public static final int FONT_SIZE_8_BY_16_DOTS = 50;
   public static final int FONT_SIZE_16_BY_16_DOTS = 51;
   public static final int FONT_NUMBER_5_BY_7_DOTS = 49;
   public static final int FONT_NUMBER_8_BY_16_DOTS = 50;
   public static final int FONT_NUMBER_16_BY_16_DOTS = 54;
   public static final int USER_CHARACTER_DEFINITION_DATA_LENGTH = 32;
   public static final int COLUMNS = 20;
   public static final int DISP_CS_JAPANESE = 1;
   public static final int DISP_CS_KOREAN = 2;
   public static final int DISP_CS_SIMPLIFIED_CHINESE = 3;
   public static final int DISP_CS_TRADITIONAL_CHINESE = 4;
   public static final int DISP_CS_ASCII = 5;

   public Rs232APALineDisplayHandleImp(HandleKey key, Rs232Port rs232Port, byte cursorState) {
      super(key, rs232Port);
      this.userCharacterDefinitionCmd = new byte[USER_DEFINABLE_FONT_SET.length + 32];
      this.setCursorStateValue(cursorState);
   }

   public void init() throws HandleException {
      super.init();
      this.initiateCursor();
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
      cmd.setDeviceId(3020);
      cmd.setDeviceType(3003);
   }

   protected void visitWriteDisplayCmd(LineDisplayCmd cmd) {
      if (this.isCursorOn()) {
         this.submitCursorState((byte)0);
      }

      LineDisplayCmd.WriteDisplayCmd wdC = (LineDisplayCmd.WriteDisplayCmd)cmd;
      DISPLAY_POSITION_LEFT10_CMD[2] = (byte)(wdC.getRowPosition() + 49);
      DISPLAY_POSITION_LEFT10_CMD[4] = 49;
      this.submit(DISPLAY_POSITION_LEFT10_CMD, "update display position home failed");
      this.submit(wdC.getData(), "data not found");
      this.submit(this.updateCursorPosition(wdC.getRowPosition(), wdC.getCursorLocation()), "UpdateCursorPositionCmd failed");
      if (this.isCursorOn()) {
         this.submitCursorState((byte)1);
      }
   }

   protected void visitClearDisplayCmd(LineDisplayCmd cmd) {
      if (this.isCursorOn()) {
         this.submitCursorState((byte)0);
      }

      LineDisplayCmd.ClearDisplayCmd cD = (LineDisplayCmd.ClearDisplayCmd)cmd;
      this.submit(CLEAR_DISPLAY_CMD, "Could not set clear cmd");
      this.submit(DISPLAY_HOME_POSITION_CMD, "update display position home failed");
      if (this.isCursorOn()) {
         this.submitCursorState((byte)1);
      }
   }

   protected void visitWriteTriangleMarksCmd(LineDisplayCmd cmd) {
      throw new RuntimeException("Invalid LineDisplayCmd submitted");
   }

   protected void visitBrightnessCmd(LineDisplayCmd cmd) {
      LineDisplayCmd.BrightnessCmd bC = (LineDisplayCmd.BrightnessCmd)cmd;
      int brightness_mapped = this.mapping.mapValue(bC.getBrightness(), this.getBrightnessValues());
      BRIGHTNESS_CMD[5] = (byte)brightness_mapped;
      this.submit(BRIGHTNESS_CMD, "Could not set Brightness cmd");
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
         CHARACTER_SET_CMD[2] = (byte)correspondingCharSet;
         this.submit(CHARACTER_SET_CMD, "Could not set characterSetCmd");
      } else {
         this.handleException = new HandleException("Unsupported character set value to set");
      }
   }

   protected void visitScreenModeCmd(LineDisplayCmd cmd) {
      LineDisplayCmd.ScreenModeCmd smC = (LineDisplayCmd.ScreenModeCmd)cmd;
      boolean screenModefound = false;
      int screenModeCounter = 0;

      int correspondingscreenMode;
      for (correspondingscreenMode = -1; screenModeCounter < this.getScreenModeValues()[0].length && !screenModefound; screenModeCounter++) {
         if (smC.getScreenMode() == this.getScreenModeValues()[0][screenModeCounter]) {
            screenModefound = true;
            correspondingscreenMode = this.getScreenModeValues()[1][screenModeCounter];
         }
      }

      if (correspondingscreenMode != -1) {
         SCREEN_MODE_CMD[5] = (byte)correspondingscreenMode;
         this.submit(SCREEN_MODE_CMD, "Could not set screenModeCmd");
         this.numberOfRows = (byte)correspondingscreenMode;
      } else {
         this.handleException = new HandleException("Unsupported screen mode value to set");
      }
   }

   protected void visitDisplayModeCmd(LineDisplayCmd cmd) {
      LineDisplayCmd.DisplayModeCmd dmC = (LineDisplayCmd.DisplayModeCmd)cmd;
      boolean displayModefound = false;
      int displayModeCounter = 0;

      int correspondingdisplayMode;
      for (correspondingdisplayMode = -1; displayModeCounter < this.getDisplayModeValues()[0].length && !displayModefound; displayModeCounter++) {
         if (dmC.getDisplayMode() == this.getDisplayModeValues()[0][displayModeCounter]) {
            displayModefound = true;
            correspondingdisplayMode = this.getDisplayModeValues()[1][displayModeCounter];
         }
      }

      if (correspondingdisplayMode != -1) {
         DISPLAY_MODE_CMD[2] = (byte)correspondingdisplayMode;
         this.submit(DISPLAY_MODE_CMD, "Could not set displayModeCmd");
      } else {
         this.handleException = new HandleException("Unsupported display mode value to set");
      }
   }

   protected void visitCursorModeCmd(LineDisplayCmd cmd) {
      LineDisplayCmd.CursorModeCmd cmC = (LineDisplayCmd.CursorModeCmd)cmd;
      boolean cursorModefound = false;
      int cursorModeCounter = 0;

      int correspondingCursorMode;
      for (correspondingCursorMode = -1; cursorModeCounter < this.getCursorModeValues()[0].length && !cursorModefound; cursorModeCounter++) {
         if (cmC.getCursorMode() == this.getCursorModeValues()[0][cursorModeCounter]) {
            cursorModefound = true;
            correspondingCursorMode = this.getCursorModeValues()[1][cursorModeCounter];
         }
      }

      if (correspondingCursorMode != -1) {
         CURSOR_MODE_CMD[5] = (byte)correspondingCursorMode;
         this.submit(CURSOR_MODE_CMD, "Could not set cursorModeCmd");
      } else {
         this.handleException = new HandleException("Unsupported cursor mode value to set");
      }
   }

   protected void visitUserDefinableCharacterCmd(LineDisplayCmd cmd) {
      LineDisplayCmd.UserDefinableCharacterCmd udcC = (LineDisplayCmd.UserDefinableCharacterCmd)cmd;
      System.arraycopy(USER_DEFINABLE_FONT_SET, 0, this.userCharacterDefinitionCmd, 0, USER_DEFINABLE_FONT_SET.length);
      this.userCharacterDefinitionCmd[9] = (byte)udcC.getGlyphCode();
      if (udcC.getGlyph().length == 7) {
         this.userCharacterDefinitionCmd[5] = 49;
         this.userCharacterDefinitionCmd[7] = 49;
         int glyph = 0;

         for (int i = 0; i < udcC.getGlyph().length; i++) {
            int var5 = udcC.getGlyph()[i];
            var5 <<= 3;
            this.userCharacterDefinitionCmd[11 + i] = (byte)var5;
         }

         for (int i = 18; i < this.userCharacterDefinitionCmd.length; i++) {
            this.userCharacterDefinitionCmd[i] = 0;
         }
      } else if (udcC.getGlyph().length == 16) {
         this.userCharacterDefinitionCmd[5] = 50;
         this.userCharacterDefinitionCmd[7] = 50;

         for (int i = 0; i < udcC.getGlyph().length; i++) {
            this.userCharacterDefinitionCmd[11 + i] = udcC.getGlyph()[i];
         }

         for (int i = 27; i < this.userCharacterDefinitionCmd.length; i++) {
            this.userCharacterDefinitionCmd[i] = 0;
         }
      } else if (udcC.getGlyph().length == 32) {
         this.userCharacterDefinitionCmd[5] = 51;
         this.userCharacterDefinitionCmd[7] = 54;

         for (int i = 0; i < udcC.getGlyph().length; i++) {
            this.userCharacterDefinitionCmd[11 + i] = udcC.getGlyph()[i];
         }
      }

      this.submit(this.userCharacterDefinitionCmd, "Could not set UserDefinableCharacterCmd");
   }

   protected void visitDisplayTextModeCmd(LineDisplayCmd cmd) {
      throw new RuntimeException("unsupported visitDisplayTextModeCmd");
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
            CURSOR_MODE_CMD[5] = 48;
            this.submit(CURSOR_MODE_CMD, "Could not set cursorOFFcmd");
            break;
         case 1:
            CURSOR_MODE_CMD[5] = 50;
            this.submit(CURSOR_MODE_CMD, "Could not set cursorONcmd");
            break;
         default:
            this.handleException = new HandleException("Unsupported cursor mode value to set");
      }
   }

   private byte[] updateCursorPosition(byte row, byte cursorLocation) {
      cursorLocation = (byte)(cursorLocation - row * 20);
      if (cursorLocation >= 0 && cursorLocation <= 8) {
         DISPLAY_POSITION_LEFT10_CMD[2] = (byte)(row + 49);
         DISPLAY_POSITION_LEFT10_CMD[4] = (byte)(cursorLocation + 49);
         this.updatePosition = DISPLAY_POSITION_LEFT10_CMD;
      }

      if (cursorLocation >= 9 && cursorLocation <= 18) {
         DISPLAY_POSITION_RIGHT10_CMD[2] = (byte)(row + 49);
         DISPLAY_POSITION_RIGHT10_CMD[5] = (byte)(cursorLocation + 39);
         this.updatePosition = DISPLAY_POSITION_RIGHT10_CMD;
      }

      if (cursorLocation == 19) {
         DISPLAY_POSITION_RIGHT10_CMD[2] = (byte)(row + 49);
         DISPLAY_POSITION_RIGHT10_CMD[5] = (byte)(cursorLocation + 45);
         this.updatePosition = DISPLAY_POSITION_RIGHT10_CMD;
      }

      if ((this.numberOfRows == 54 && row == 3 || this.numberOfRows == 55 && row == 4 || this.numberOfRows == 56 && row == 1) && cursorLocation == 20) {
         DISPLAY_POSITION_RIGHT10_CMD[2] = (byte)(row + 49);
         DISPLAY_POSITION_RIGHT10_CMD[5] = (byte)(cursorLocation + 45);
         this.updatePosition = DISPLAY_POSITION_RIGHT10_CMD;
      } else if (cursorLocation == 20) {
         DISPLAY_POSITION_LEFT10_CMD[2] = (byte)(row + 49 + 1);
         DISPLAY_POSITION_LEFT10_CMD[4] = 49;
         this.updatePosition = DISPLAY_POSITION_LEFT10_CMD;
      }

      return this.updatePosition;
   }

   private boolean isCursorOn() {
      return this.isCursorOn;
   }

   private int[][] getBrightnessValues() {
      return new int[][]{{0, 32, 45, 59, 79, 100}, {48, 49, 50, 51, 52, 53}};
   }

   private int[][] getCharacterSetValues() {
      return new int[][]{{0, 1, 2, 3, 4, 5}, {48, 49, 50, 51, 52, 48}};
   }

   private int[][] getDisplayModeValues() {
      return new int[][]{{0, 1, 2}, {48, 53, 55}};
   }

   private int[][] getScreenModeValues() {
      return new int[][]{{0, 1, 2}, {54, 55, 56}};
   }

   private int[][] getCursorModeValues() {
      return new int[][]{{0, 6, 4}, {48, 49, 50}};
   }
}
