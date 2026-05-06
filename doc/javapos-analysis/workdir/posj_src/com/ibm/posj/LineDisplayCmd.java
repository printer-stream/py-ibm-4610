package com.ibm.posj;

public interface LineDisplayCmd extends HandleCmd {
   void accept(LineDisplayCmdVisitor var1);

   public interface BrightnessCmd extends LineDisplayCmd {
      void setBrightness(int var1);

      int getBrightness();
   }

   public interface ClearDisplayCmd extends LineDisplayCmd {
      byte getCursorLocation();

      void setCursorLocation(byte var1);
   }

   public interface CursorModeCmd extends LineDisplayCmd {
      void setCursorMode(byte var1);

      byte getCursorMode();
   }

   public interface DisplayModeCmd extends LineDisplayCmd {
      void setDisplayMode(byte var1);

      byte getDisplayMode();
   }

   public interface DisplayTextModeCmd extends LineDisplayCmd {
      void setDisplayTextMode(byte var1);

      byte getDisplayTextMode();
   }

   public interface Factory extends SystemCmd.Factory {
      LineDisplayCmd.ClearDisplayCmd createClearDisplayCmd(byte var1);

      LineDisplayCmd.WriteDisplayCmd createWriteDisplayCmd(byte[] var1, byte var2, byte var3);

      LineDisplayCmd.SetCharacterSetCmd createSetCharacterSetCmd(int var1);

      LineDisplayCmd.WriteTriangleMarksCmd createWriteTriangleMarksCmd(byte[] var1);

      LineDisplayCmd.TestLDRequestCmd createTestLDRequestCmd();

      LineDisplayCmd.BrightnessCmd createBrightnessCmd(int var1);

      LineDisplayCmd.ScreenModeCmd createScreenModeCmd(byte var1);

      LineDisplayCmd.DisplayModeCmd createDisplayModeCmd(byte var1);

      LineDisplayCmd.CursorModeCmd createCursorModeCmd(byte var1);

      LineDisplayCmd.UserDefinableCharacterCmd createUserDefinableCharacterCmd(int var1, byte[] var2);

      LineDisplayCmd.DisplayTextModeCmd createDisplayTextModeCmd(byte var1);
   }

   public interface ScreenModeCmd extends LineDisplayCmd {
      void setScreenMode(byte var1);

      byte getScreenMode();
   }

   public interface SetCharacterSetCmd extends LineDisplayCmd {
      void setCharacterSet(int var1);

      int getCharacterSet();
   }

   public interface TestLDRequestCmd extends LineDisplayCmd {
   }

   public interface UserDefinableCharacterCmd extends LineDisplayCmd {
      void setGlyphCode(int var1);

      int getGlyphCode();

      void setGlyph(byte[] var1);

      byte[] getGlyph();
   }

   public interface WriteDisplayCmd extends LineDisplayCmd {
      byte[] getData();

      void setData(byte[] var1);

      byte getRowPosition();

      void setRowPosition(byte var1);

      byte getCursorLocation();

      void setCursorLocation(byte var1);
   }

   public interface WriteTriangleMarksCmd extends LineDisplayCmd {
      void setIndicators(byte[] var1);

      byte[] getIndicators();
   }
}
