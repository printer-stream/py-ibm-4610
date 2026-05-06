package com.ibm.posj;

public interface ToneIndicatorCmd extends HandleCmd {
   int SOUND_CMD_CODE = 1000;
   String SOUND_CMD_NAME = "SOUND_CMD";
   boolean TONE_ON = true;
   boolean TONE_OFF = false;
   boolean TONE_TIMED_ON = true;
   boolean TONE_TIMED_OFF = false;

   void setCmdBytes(byte[] var1);

   public interface Factory extends SystemCmd.Factory {
      ToneIndicatorCmd createToneCmd(boolean var1, boolean var2, int var3, int var4, int var5);
   }

   public interface ToneCmd extends ToneIndicatorCmd {
      boolean getOn();

      boolean getTimed();

      int getDuration();

      int getFrequency();

      int getVolume();
   }
}
