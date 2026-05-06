package com.ibm.posj.bus.hid;

import com.ibm.posj.util.KeylockUtil;

public abstract class DefaultHidKeylockStrategy implements HidKeylockStrategy {
   public int getKeylockStatusByteIndex() {
      return -1;
   }

   public int getErrorStatusByteIndex() {
      return -1;
   }

   abstract static class AbstractAdministrativeHidKeylockStrategy extends DefaultHidKeylockStrategy {
      int ADMIN_STATUS_BYTE = 1;
      int ADMIN_REJECTED_BYTE = 0;
      int ADMIN_DEV_INFO_POSITION_BYTE = 0;

      public int getKeylockStatusByteIndex() {
         return this.ADMIN_STATUS_BYTE;
      }

      public int getErrorStatusByteIndex() {
         return this.ADMIN_REJECTED_BYTE;
      }

      public int getDevInfoPosition() {
         return this.ADMIN_DEV_INFO_POSITION_BYTE;
      }

      public abstract int getDeviceId(byte[] var1);
   }

   static class BootModeKbdKeylockStrategy
      extends DefaultHidKeylockStrategy.AbstractAdministrativeHidKeylockStrategy
      implements HidKeylockStrategy.BootModeKbdKeylockStrategy {
      public int getDeviceId(byte[] response) {
         return KeylockUtil.getUsbBootKeylockID(response);
      }
   }

   public static class Factory implements HidKeylockStrategy.Factory {
      public HidKeylockStrategy createNonBootModeKbdKeylockStrategy() {
         return new DefaultHidKeylockStrategy.NonBootModeKbdKeylockStrategy();
      }

      public HidKeylockStrategy createBootModeKbdKeylockStrategy() {
         return new DefaultHidKeylockStrategy.BootModeKbdKeylockStrategy();
      }

      public HidKeylockStrategy createLegacyModeKbdKeylockStrategy() {
         return new DefaultHidKeylockStrategy.LegacyModeKbdKeylockStrategy();
      }
   }

   static class LegacyModeKbdKeylockStrategy extends DefaultHidKeylockStrategy implements HidKeylockStrategy.LegacyModeKbdKeylockStrategy {
      public int getKeylockStatusByteIndex() {
         return 2;
      }

      public int getErrorStatusByteIndex() {
         return 1;
      }

      public int getDevInfoPosition() {
         return 1;
      }

      public int getDeviceId(byte[] response) {
         return KeylockUtil.getUsbLegacyKeylockID(response);
      }
   }

   static class NonBootModeKbdKeylockStrategy
      extends DefaultHidKeylockStrategy.AbstractAdministrativeHidKeylockStrategy
      implements HidKeylockStrategy.BootModeKbdKeylockStrategy {
      public int getDeviceId(byte[] response) {
         return KeylockUtil.getUsbNonBootKeylockID(response);
      }
   }
}
