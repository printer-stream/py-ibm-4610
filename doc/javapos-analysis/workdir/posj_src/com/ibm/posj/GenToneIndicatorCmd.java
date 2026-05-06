package com.ibm.posj;

import com.ibm.jutil.tasks.Mapping;

public class GenToneIndicatorCmd {
   public static final byte[] TONEINDICATOR_CMD = new byte[]{19, 0, 0};
   public static final byte[] POSKEYBOARD_TEST_REQUEST_CMD = new byte[]{0, 16};
   public static final byte[] POSKEYBOARD_STATUS_REQUEST_CMD = new byte[]{0, 32};
   public static final byte[] POSKEYBOARD_RESET_REQUEST_CMD = new byte[]{0, 64};
   public static final byte[] TONEINDICATOR_DEV_INFO_REQUEST_CMD = new byte[]{0, 0, 1};
   public Mapping mapping = new Mapping();
   public boolean isToneActive = false;
   public int toneDevId = -1;
   public int toneFeatures = -1;
   public byte statusByteToneState = -1;
   public int[][] createCommonFrequencies;
   public int[] durations;
   public int[][] volumes;
   public Object lockobj = new Object();
   public static final byte TONE_ON = 1;
   public static final byte TONE_OFF = 0;
   public static final int TI_VOLUME_LOW = 1;
   public static final int TI_VOLUME_HIGH = 100;
   public static final int TI_MIN_DURATION = 0;
   public static final int TI_MAX_DURATION = 256;
   public static final int TONE_ENABLE_CMD = 32;
   public static final int TONE_DISABLE_CMD = 64;
   public static final int TONE_TIMED_CMD = 0;
   public static final int VOLUME_LOW_CMD = 4;
   public static final int VOLUME_HIGH_CMD = 0;
   public static final int LOW_FREQUENCY_CMD = 0;
   public static final int MEDIUM_FREQUENCY_CMD = 8;
   public static final int HIGH_FREQUENCY_CMD = 16;
   public static final int NO_FREQUENCY_CMD = 24;

   public GenToneIndicatorCmd() {
      this.durations = new int[256];
      int duration = 0;

      while (duration < this.durations.length) {
         this.durations[duration] = duration++;
      }

      this.volumes = new int[][]{{1, 100}, {4, 0}};
   }

   public int[][] getVolumeValues() {
      return this.volumes;
   }

   public int[] getDurationValues() {
      return this.durations;
   }

   public int[][] getFrequencyValues() {
      return this.createCommonFrequencies;
   }

   public void formatToneIndicatorCmd(ToneIndicatorCmd toneIndicatorCmd) throws RuntimePosException {
      ToneIndicatorCmd.ToneCmd sound = (ToneIndicatorCmd.ToneCmd)toneIndicatorCmd;
      byte ppByte = 0;
      byte ttByte = 0;
      if (sound.getOn() && sound.getTimed()) {
         ppByte = (byte)(ppByte + this.mapping.mapValue(sound.getFrequency(), this.getFrequencyValues()));
         ppByte = (byte)(ppByte + this.mapping.mapValue(sound.getVolume(), this.getVolumeValues()));
         ttByte = (byte)this.mapping.mapValue(sound.getDuration() / 100, this.getDurationValues());
      } else if (!sound.getOn() || sound.getTimed()) {
         if (sound.getOn()) {
            throw new RuntimePosException("attempt to submit an invalid ToneIndicator cmd instruction");
         }

         ppByte = (byte)(ppByte + 64);
      }

      TONEINDICATOR_CMD[1] = ppByte;
      TONEINDICATOR_CMD[2] = ttByte;
   }

   public void lockUntilSoundCompleted(int msecs) {
      synchronized (this.lockobj) {
         try {
            this.lockobj.wait((long)(msecs + 1000));
         } catch (InterruptedException var5) {
         }
      }
   }
}
