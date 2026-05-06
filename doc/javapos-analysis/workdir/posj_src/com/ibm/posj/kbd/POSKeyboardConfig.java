package com.ibm.posj.kbd;

public class POSKeyboardConfig {
   private byte click = 0;
   private boolean typematic = false;
   private boolean kbdScanning = false;
   private byte typematicDelay = 0;
   private byte typematicRate = 0;
   private byte fatFingerTimeout = 0;

   public POSKeyboardConfig() {
   }

   public POSKeyboardConfig(byte click, boolean typematic, boolean kbdScanning, byte typematicDelay, byte typematicRate, byte fatFingerTimeout) {
      this.click = click;
      this.typematic = typematic;
      this.kbdScanning = kbdScanning;
      this.typematicDelay = typematicDelay;
      this.typematicRate = typematicRate;
      this.fatFingerTimeout = fatFingerTimeout;
   }

   public void setClick(byte click) {
      this.click = click;
   }

   public byte getClick() {
      return this.click;
   }

   public void setTypematic(boolean b) {
      this.typematic = b;
   }

   public boolean getTypematic() {
      return this.typematic;
   }

   public void setKbdScanning(boolean b) {
      this.kbdScanning = b;
   }

   public boolean getKbdScanning() {
      return this.kbdScanning;
   }

   public void setTypematicDelay(byte b) {
      this.typematicDelay = b;
   }

   public byte getTypematicDelay() {
      return this.typematicDelay;
   }

   public void setTypematicRate(byte b) {
      this.typematicRate = b;
   }

   public byte getTypematicRate() {
      return this.typematicRate;
   }

   public void setFatFingerTimeout(byte b) {
      this.fatFingerTimeout = b;
   }

   public byte getFatFingerTimeout() {
      return this.fatFingerTimeout;
   }

   public String toString() {
      return "Click = "
         + this.getClick()
         + " Typematic = "
         + this.getTypematic()
         + " KbdScanning = "
         + this.getKbdScanning()
         + " TypematicDelay = "
         + this.getTypematicDelay()
         + " TypematicRate = "
         + this.getTypematicRate()
         + " FatFingerTimeOut = "
         + this.getFatFingerTimeout();
   }
}
