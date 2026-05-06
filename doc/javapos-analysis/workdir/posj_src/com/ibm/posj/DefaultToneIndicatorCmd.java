package com.ibm.posj;

abstract class DefaultToneIndicatorCmd extends AbstractHandleCmd implements ToneIndicatorCmd {
   private String name = "";
   private int code = 0;
   protected byte[] byteArray = new byte[0];

   DefaultToneIndicatorCmd(HandleCmd.Factory factory, String name, int code) {
      super(factory);
      this.name = name;
      this.code = code;
   }

   public void setCmdBytes(byte[] cmd) {
      this.byteArray = cmd;
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
      visitor.visitToneIndicatorCmd(this);
   }

   static class Factory extends DefaultSystemCmd.Factory implements ToneIndicatorCmd.Factory {
      public ToneIndicatorCmd createToneCmd(boolean on, boolean timed, int duration, int frequency, int volume) {
         return new DefaultToneIndicatorCmd.ToneCmd(this, on, timed, duration, frequency, volume);
      }
   }

   static class ToneCmd extends DefaultToneIndicatorCmd implements ToneIndicatorCmd.ToneCmd {
      private boolean toneOn = false;
      private boolean toneTimed = false;
      private int toneDuration = 0;
      private int toneFrequency = 0;
      private int toneVolume = 0;

      ToneCmd(HandleCmd.Factory factory, boolean on, boolean timed, int duration, int frequency, int volume) {
         super(factory, "SOUND_CMD", 1000);
         this.toneOn = on;
         this.toneTimed = timed;
         this.toneDuration = duration;
         this.toneFrequency = frequency;
         this.toneVolume = volume;
      }

      public boolean getOn() {
         return this.toneOn;
      }

      public boolean getTimed() {
         return this.toneTimed;
      }

      public int getDuration() {
         return this.toneDuration;
      }

      public int getFrequency() {
         return this.toneFrequency;
      }

      public int getVolume() {
         return this.toneVolume;
      }
   }
}
