package com.ibm.posj.bus.hid;

public interface HidToneIndicatorStrategy {
   int LEGACY_DEV_INFO_POSITION_BYTE = 1;

   int getDeviceId(byte[] var1);

   int getDevInfoPosition();

   int[][] getFrequencyValues();

   int getByteToneState();

   public interface BootModeKbdToneStrategy extends HidToneIndicatorStrategy {
   }

   public interface Factory {
      HidToneIndicatorStrategy createNonBootModeKbdToneStrategy();

      HidToneIndicatorStrategy createBootModeKbdToneStrategy();

      HidToneIndicatorStrategy createLegacyModeKbdToneStrategy();

      HidToneIndicatorStrategy createLegacyModeSurepointToneStrategy();

      HidToneIndicatorStrategy createLegacyMode485K03ToneStrategy();
   }

   public interface LegacyMode4685K03ToneStrategy extends HidToneIndicatorStrategy {
   }

   public interface LegacyModeKbdToneStrategy extends HidToneIndicatorStrategy {
   }

   public interface LegacyModeSurepointToneStrategy extends HidToneIndicatorStrategy {
   }

   public interface NonBootModeKbdToneStrategy extends HidToneIndicatorStrategy {
   }
}
