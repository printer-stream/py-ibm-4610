package com.ibm.posj.bus.hid;

import com.ibm.posj.util.ToneIndicatorUtil;

public abstract class DefaultHidToneIndicatorStrategy implements HidToneIndicatorStrategy {
   public int[][] createCommonFrequencyValues() {
      return new int[][]{{875, 1300, 2000}, {0, 8, 16}};
   }

   abstract static class AbstractAdministrativeHidToneStrategy extends DefaultHidToneIndicatorStrategy {
      int ADMIN_STATUS_BYTE = 1;
      int ADMIN_DEV_INFO_POSITION_BYTE = 0;

      public int getDevInfoPosition() {
         return this.ADMIN_DEV_INFO_POSITION_BYTE;
      }

      public int[][] getFrequencyValues() {
         return this.createCommonFrequencyValues();
      }

      public int getByteToneState() {
         return this.ADMIN_STATUS_BYTE;
      }

      public abstract int getDeviceId(byte[] var1);
   }

   static class BootModeKbdToneStrategy
      extends DefaultHidToneIndicatorStrategy.AbstractAdministrativeHidToneStrategy
      implements HidToneIndicatorStrategy.BootModeKbdToneStrategy {
      public int getDeviceId(byte[] response) {
         return ToneIndicatorUtil.getUsbBootToneIndicatorID(response);
      }
   }

   public static class Factory implements HidToneIndicatorStrategy.Factory {
      public HidToneIndicatorStrategy createNonBootModeKbdToneStrategy() {
         return new DefaultHidToneIndicatorStrategy.NonBootModeKbdToneStrategy();
      }

      public HidToneIndicatorStrategy createBootModeKbdToneStrategy() {
         return new DefaultHidToneIndicatorStrategy.BootModeKbdToneStrategy();
      }

      public HidToneIndicatorStrategy createLegacyModeKbdToneStrategy() {
         return new DefaultHidToneIndicatorStrategy.LegacyModeKbdToneStrategy();
      }

      public HidToneIndicatorStrategy createLegacyModeSurepointToneStrategy() {
         return new DefaultHidToneIndicatorStrategy.LegacyModeSurepointToneStrategy();
      }

      public HidToneIndicatorStrategy createLegacyMode485K03ToneStrategy() {
         return new DefaultHidToneIndicatorStrategy.LegacyMode4685K03ToneStrategy();
      }
   }

   static class LegacyMode4685K03ToneStrategy extends DefaultHidToneIndicatorStrategy implements HidToneIndicatorStrategy.LegacyMode4685K03ToneStrategy {
      public int getDevInfoPosition() {
         return 1;
      }

      public int getDeviceId(byte[] response) {
         return ToneIndicatorUtil.getUsbLegacyToneIndicatorID(response);
      }

      public int[][] getFrequencyValues() {
         return new int[][]{{943, 1562, 1603}, {0, 8, 16}};
      }

      public int getByteToneState() {
         return 2;
      }
   }

   static class LegacyModeKbdToneStrategy extends DefaultHidToneIndicatorStrategy implements HidToneIndicatorStrategy.LegacyModeKbdToneStrategy {
      public int getDevInfoPosition() {
         return 1;
      }

      public int getDeviceId(byte[] response) {
         return ToneIndicatorUtil.getUsbLegacyToneIndicatorID(response);
      }

      public int[][] getFrequencyValues() {
         return this.createCommonFrequencyValues();
      }

      public int getByteToneState() {
         return 2;
      }
   }

   static class LegacyModeSurepointToneStrategy extends DefaultHidToneIndicatorStrategy implements HidToneIndicatorStrategy.LegacyModeSurepointToneStrategy {
      public int getDevInfoPosition() {
         return 1;
      }

      public int getDeviceId(byte[] response) {
         return ToneIndicatorUtil.getUsbLegacyToneIndicatorID(response);
      }

      public int[][] getFrequencyValues() {
         return new int[][]{{600, 1010, 1603}, {0, 8, 16}};
      }

      public int getByteToneState() {
         return 2;
      }
   }

   static class NonBootModeKbdToneStrategy
      extends DefaultHidToneIndicatorStrategy.AbstractAdministrativeHidToneStrategy
      implements HidToneIndicatorStrategy.BootModeKbdToneStrategy {
      public int getDeviceId(byte[] response) {
         return ToneIndicatorUtil.getUsbNonBootToneIndicatorID(response);
      }
   }
}
