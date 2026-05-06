package com.ibm.posj;

import com.ibm.posj.util.PosjUtil;

public class ScaleConfig {
   private byte[] config = new byte[3];
   public static final byte ENGLISH_MODE = 0;
   public static final byte METRIC_MODE = 1;
   public static final byte OP_MODE_US = 0;
   public static final byte OP_MODE_UK = 1;
   public static final byte VIB_SEN_NORMAL = 0;
   public static final byte VIB_SEN_LOW = 1;
   public static final byte VIB_SEN_VERY_LOW = 2;
   public static final byte VIB_SEN_ULTRA_LOW = 3;

   public ScaleConfig() {
   }

   public ScaleConfig(byte[] config) {
      this();
      this.setConfig(config);
   }

   public byte getOperationMode() {
      byte om = 0;
      if (PosjUtil.isBitSelected(this.config[0], 0)) {
         om = 1;
      } else {
         om = 0;
      }

      return om;
   }

   public boolean getDisplayRequired() {
      return PosjUtil.isBitSelected(this.config[0], 2);
   }

   public boolean getCenterOfZero() {
      return PosjUtil.isBitSelected(this.config[0], 3);
   }

   public byte getWeighMode() {
      byte wm;
      if (PosjUtil.isBitSelected(this.config[0], 4)) {
         wm = 1;
      } else {
         wm = 0;
      }

      return wm;
   }

   public boolean getEnforceZeroReturn() {
      return PosjUtil.isBitSelected(this.config[0], 5);
   }

   public byte getVibrationSensitivity() {
      byte vs = 0;
      if (PosjUtil.isBitSelected(this.config[0], 6)) {
         vs++;
      }

      if (PosjUtil.isBitSelected(this.config[0], 7)) {
         vs = (byte)(vs + 2);
      }

      return vs;
   }

   public boolean getFiveDigitWeight() {
      return PosjUtil.isBitSelected(this.config[1], 0);
   }

   public byte[] getConfig() {
      this.validateConfig();
      return this.config;
   }

   public void setOperationMode(byte operationMode) {
      switch (operationMode) {
         case 0:
            this.config[0] = (byte)PosjUtil.clearBit(this.config[0], 0);
            this.config[0] = (byte)PosjUtil.clearBit(this.config[0], 1);
            break;
         case 1:
            this.config[0] = (byte)PosjUtil.setBit(this.config[0], 0);
            this.config[0] = (byte)PosjUtil.clearBit(this.config[0], 1);
            break;
         default:
            throw new RuntimePosException("illegal operation mode argument");
      }
   }

   public void setDisplayRequired(boolean displayRequired) {
      if (displayRequired) {
         this.config[0] = (byte)PosjUtil.setBit(this.config[0], 2);
      } else {
         this.config[0] = (byte)PosjUtil.clearBit(this.config[0], 2);
      }
   }

   public void setCenterOfZero(boolean centerOfZero) {
      if (centerOfZero) {
         this.config[0] = (byte)PosjUtil.setBit(this.config[0], 3);
      } else {
         this.config[0] = (byte)PosjUtil.clearBit(this.config[0], 3);
      }
   }

   public void setWeighMode(byte weighMode) {
      switch (weighMode) {
         case 0:
            this.config[0] = (byte)PosjUtil.clearBit(this.config[0], 4);
            break;
         case 1:
            this.config[0] = (byte)PosjUtil.setBit(this.config[0], 4);
            break;
         default:
            throw new RuntimePosException("illegal weight mode argument");
      }
   }

   public void setEnforceZeroReturn(boolean enforceZeroReturn) {
      if (enforceZeroReturn) {
         this.config[0] = (byte)PosjUtil.setBit(this.config[0], 5);
      } else {
         this.config[0] = (byte)PosjUtil.clearBit(this.config[0], 5);
      }
   }

   public void setVibrationSensitivity(byte sensitivity) {
      switch (sensitivity) {
         case 0:
            this.config[0] = (byte)PosjUtil.clearBit(this.config[0], 6);
            this.config[0] = (byte)PosjUtil.clearBit(this.config[0], 7);
            break;
         case 1:
            this.config[0] = (byte)PosjUtil.setBit(this.config[0], 6);
            this.config[0] = (byte)PosjUtil.clearBit(this.config[0], 7);
            break;
         case 2:
            this.config[0] = (byte)PosjUtil.clearBit(this.config[0], 6);
            this.config[0] = (byte)PosjUtil.setBit(this.config[0], 7);
            break;
         case 3:
            this.config[0] = (byte)PosjUtil.setBit(this.config[0], 6);
            this.config[0] = (byte)PosjUtil.setBit(this.config[0], 7);
            break;
         default:
            throw new RuntimePosException("illegal vibration sensitivity argument");
      }
   }

   public void setFiveDigitWeight(boolean fivedigit) {
      if (fivedigit) {
         this.config[1] = (byte)PosjUtil.setBit(this.config[1], 0);
      } else {
         this.config[1] = (byte)PosjUtil.clearBit(this.config[1], 0);
      }
   }

   public void setConfig(byte[] config) {
      this.validateConfig(config);
      this.config = config;
   }

   private void validateConfig() {
      this.validateConfig(this.config);
   }

   private void validateConfig(byte[] config) {
      if (PosjUtil.isBitSelected(config[0], 1)) {
         throw new RuntimePosException("invalid configuration - operation mode should be 0 or 1");
      } else if (config[1] != 0 && config[1] != 1) {
         throw new RuntimePosException("invalid configuration - second data byte bits 7-1 are reserved");
      } else if (config[2] != 0) {
         throw new RuntimePosException("invalid configuration - third data byte is reserved");
      }
   }
}
