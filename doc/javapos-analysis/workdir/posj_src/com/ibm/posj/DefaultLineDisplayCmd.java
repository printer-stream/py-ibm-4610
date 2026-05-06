package com.ibm.posj;

abstract class DefaultLineDisplayCmd extends AbstractHandleCmd implements LineDisplayCmd, LineDisplayCmdConst {
   private String name = "";
   private int code = 0;
   protected byte[] byteArray = new byte[0];

   DefaultLineDisplayCmd(HandleCmd.Factory factory, String name, int code) {
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
      visitor.visitLineDisplayCmd(this);
   }

   public abstract void accept(LineDisplayCmdVisitor var1);

   static class BrightnessCmd extends DefaultLineDisplayCmd implements LineDisplayCmd.BrightnessCmd {
      private int brightness = 0;

      BrightnessCmd(HandleCmd.Factory factory, int brightness) {
         super(factory, "BRIGHTNESS_CMD", 406);
         this.setBrightness(brightness);
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("<BrightnessCmd");
         sb.append(" brightness=\"" + this.getBrightness() + "\"");
         sb.append("/>");
         return sb.toString();
      }

      public void setBrightness(int b) {
         this.brightness = b;
      }

      public int getBrightness() {
         return this.brightness;
      }

      public void accept(LineDisplayCmdVisitor visitor) {
         visitor.visitBrightnessCmd(this);
      }
   }

   static class ClearDisplayCmd extends DefaultLineDisplayCmd implements LineDisplayCmd.ClearDisplayCmd {
      private byte cursorLocation = 0;

      ClearDisplayCmd(HandleCmd.Factory factory, byte b) {
         super(factory, "CLEAR_DISPLAY", 400);
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("<ClearDisplayCmd");
         sb.append(" cursorLocation=\"" + this.getCursorLocation() + "\"");
         sb.append("/>");
         return sb.toString();
      }

      public void accept(LineDisplayCmdVisitor visitor) {
         visitor.visitClearDisplayCmd(this);
      }

      public byte getCursorLocation() {
         return this.cursorLocation;
      }

      public void setCursorLocation(byte b) {
         this.cursorLocation = b;
      }
   }

   static class CursorModeCmd extends DefaultLineDisplayCmd implements LineDisplayCmd.CursorModeCmd {
      private byte cursorMode = -1;

      CursorModeCmd(HandleCmd.Factory factory, byte cursorMode) {
         super(factory, "CURSOR_MODE_CMD", 409);
         this.setCursorMode(cursorMode);
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("<CursorModeCmd");
         sb.append(" CursorMode=\"" + this.getCursorMode() + "\"");
         sb.append("/>");
         return sb.toString();
      }

      public void setCursorMode(byte b) {
         this.cursorMode = b;
      }

      public byte getCursorMode() {
         return this.cursorMode;
      }

      public void accept(LineDisplayCmdVisitor visitor) {
         visitor.visitCursorModeCmd(this);
      }
   }

   static class DisplayModeCmd extends DefaultLineDisplayCmd implements LineDisplayCmd.DisplayModeCmd {
      private byte displayMode = -1;

      DisplayModeCmd(HandleCmd.Factory factory, byte displayMode) {
         super(factory, "DISPLAY_MODE_CMD", 408);
         this.setDisplayMode(displayMode);
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("<DisplayModeCmd");
         sb.append(" DisplayMode=\"" + this.getDisplayMode() + "\"");
         sb.append("/>");
         return sb.toString();
      }

      public void setDisplayMode(byte b) {
         this.displayMode = b;
      }

      public byte getDisplayMode() {
         return this.displayMode;
      }

      public void accept(LineDisplayCmdVisitor visitor) {
         visitor.visitDisplayModeCmd(this);
      }
   }

   static class DisplayTextModeCmd extends DefaultLineDisplayCmd implements LineDisplayCmd.DisplayTextModeCmd {
      private byte displayTextMode;

      DisplayTextModeCmd(HandleCmd.Factory factory, byte displayTextMode) {
         super(factory, "DISPLAY_TEXT_MODE_CMD", 411);
         this.setDisplayTextMode(displayTextMode);
      }

      public void setDisplayTextMode(byte b) {
         this.displayTextMode = b;
      }

      public byte getDisplayTextMode() {
         return this.displayTextMode;
      }

      public void accept(LineDisplayCmdVisitor visitor) {
         visitor.visitDisplayTextModeCmd(this);
      }
   }

   static class Factory extends DefaultSystemCmd.Factory implements LineDisplayCmd.Factory {
      public LineDisplayCmd.ClearDisplayCmd createClearDisplayCmd(byte b) {
         return new DefaultLineDisplayCmd.ClearDisplayCmd(this, b);
      }

      public LineDisplayCmd.WriteDisplayCmd createWriteDisplayCmd(byte[] data, byte rowPosition, byte cursorLocation) {
         return new DefaultLineDisplayCmd.WriteDisplayCmd(this, data, rowPosition, cursorLocation);
      }

      public LineDisplayCmd.SetCharacterSetCmd createSetCharacterSetCmd(int characterSet) {
         return new DefaultLineDisplayCmd.SetCharacterSetCmd(this, characterSet);
      }

      public LineDisplayCmd.WriteTriangleMarksCmd createWriteTriangleMarksCmd(byte[] indicators) {
         return new DefaultLineDisplayCmd.WriteTriangleMarksCmd(this, indicators);
      }

      public LineDisplayCmd.TestLDRequestCmd createTestLDRequestCmd() {
         return new DefaultLineDisplayCmd.TestLDRequestCmd(this);
      }

      public LineDisplayCmd.BrightnessCmd createBrightnessCmd(int brightness) {
         return new DefaultLineDisplayCmd.BrightnessCmd(this, brightness);
      }

      public LineDisplayCmd.ScreenModeCmd createScreenModeCmd(byte screenMode) {
         return new DefaultLineDisplayCmd.ScreenModeCmd(this, screenMode);
      }

      public LineDisplayCmd.DisplayModeCmd createDisplayModeCmd(byte displayMode) {
         return new DefaultLineDisplayCmd.DisplayModeCmd(this, displayMode);
      }

      public LineDisplayCmd.CursorModeCmd createCursorModeCmd(byte cursorMode) {
         return new DefaultLineDisplayCmd.CursorModeCmd(this, cursorMode);
      }

      public LineDisplayCmd.UserDefinableCharacterCmd createUserDefinableCharacterCmd(int glyphCode, byte[] glyph) {
         return new DefaultLineDisplayCmd.UserDefinableCharacterCmd(this, glyphCode, glyph);
      }

      public LineDisplayCmd.DisplayTextModeCmd createDisplayTextModeCmd(byte displayTextMode) {
         return new DefaultLineDisplayCmd.DisplayTextModeCmd(this, displayTextMode);
      }
   }

   static class ScreenModeCmd extends DefaultLineDisplayCmd implements LineDisplayCmd.ScreenModeCmd {
      private byte screenMode = 0;

      ScreenModeCmd(HandleCmd.Factory factory, byte screenMode) {
         super(factory, "SCREEN_MODE_CMD", 407);
         this.setScreenMode(screenMode);
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("<ScreenModeCmd");
         sb.append(" screenMode=\"" + this.getScreenMode() + "\"");
         sb.append("/>");
         return sb.toString();
      }

      public void setScreenMode(byte b) {
         this.screenMode = b;
      }

      public byte getScreenMode() {
         return this.screenMode;
      }

      public void accept(LineDisplayCmdVisitor visitor) {
         visitor.visitScreenModeCmd(this);
      }
   }

   static class SetCharacterSetCmd extends DefaultLineDisplayCmd implements LineDisplayCmd.SetCharacterSetCmd {
      private int characterSet = 0;

      SetCharacterSetCmd(HandleCmd.Factory factory, int characterSet) {
         super(factory, "SET_CHARACTER_SET", 402);
         this.setCharacterSet(characterSet);
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("<SetCharacterSetCmd");
         sb.append(" CharacterSet=\"" + this.getCharacterSet() + "\"");
         sb.append("/>");
         return sb.toString();
      }

      public void setCharacterSet(int b) {
         this.characterSet = b;
      }

      public int getCharacterSet() {
         return this.characterSet;
      }

      public void accept(LineDisplayCmdVisitor visitor) {
         visitor.visitSetCharacterSetCmd(this);
      }
   }

   static class TestLDRequestCmd extends DefaultLineDisplayCmd implements LineDisplayCmd.TestLDRequestCmd {
      TestLDRequestCmd(HandleCmd.Factory factory) {
         super(factory, "TEST_REQUEST_CMD", 404);
      }

      public void accept(LineDisplayCmdVisitor visitor) {
         visitor.visitTestLDRequestCmd(this);
      }
   }

   static class UserDefinableCharacterCmd extends DefaultLineDisplayCmd implements LineDisplayCmd.UserDefinableCharacterCmd {
      private int glyphCode = -1;
      private byte[] glyph;

      UserDefinableCharacterCmd(HandleCmd.Factory factory, int glyphCode, byte[] glyph) {
         super(factory, "USER_DEFINABLE_CHARACTER_CMD", 410);
         this.setGlyphCode(glyphCode);
         this.setGlyph(glyph);
      }

      public void setGlyphCode(int b) {
         this.glyphCode = b;
      }

      public int getGlyphCode() {
         return this.glyphCode;
      }

      public void setGlyph(byte[] b) {
         this.glyph = b;
      }

      public byte[] getGlyph() {
         return this.glyph;
      }

      public void accept(LineDisplayCmdVisitor visitor) {
         visitor.visitUserDefinableCharacterCmd(this);
      }
   }

   static class WriteDisplayCmd extends DefaultLineDisplayCmd implements LineDisplayCmd.WriteDisplayCmd {
      private byte cursorLocation = 0;
      private byte[] data = new byte[20];
      private byte rowPosition = -1;

      WriteDisplayCmd(HandleCmd.Factory factory, byte[] data, byte rowPosition, byte cursorLocation) {
         super(factory, "WRITE_DISPLAY", 401);
         this.setData(data);
         this.setRowPosition(rowPosition);
         this.setCursorLocation(cursorLocation);
      }

      public void accept(LineDisplayCmdVisitor visitor) {
         visitor.visitWriteDisplayCmd(this);
      }

      public byte getCursorLocation() {
         return this.cursorLocation;
      }

      public void setCursorLocation(byte b) {
         this.cursorLocation = b;
      }

      public byte getRowPosition() {
         return this.rowPosition;
      }

      public void setRowPosition(byte b) {
         this.rowPosition = b;
      }

      public byte[] getData() {
         return this.data;
      }

      public void setData(byte[] b) throws IllegalArgumentException {
         this.data = b;
      }
   }

   static class WriteTriangleMarksCmd extends DefaultLineDisplayCmd implements LineDisplayCmd.WriteTriangleMarksCmd {
      private byte[] indicators = new byte[4];

      WriteTriangleMarksCmd(HandleCmd.Factory factory, byte[] indicators) {
         super(factory, "WRITE_TRIANGLE_MARKS", 403);
         this.setIndicators(indicators);
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("<WriteTriangleMarksCmd");
         sb.append(" Indicators=\"" + this.getIndicators() + "\"");
         sb.append("/>");
         return sb.toString();
      }

      public void setIndicators(byte[] b) {
         if (b.length != this.indicators.length) {
            throw new IllegalArgumentException("Invalid indicators array length for WriteTriangleMarksCmd");
         } else {
            System.arraycopy(b, 0, this.indicators, 0, this.indicators.length);
         }
      }

      public byte[] getIndicators() {
         return this.indicators;
      }

      public void accept(LineDisplayCmdVisitor visitor) {
         visitor.visitWriteTriangleMarksCmd(this);
      }
   }
}
