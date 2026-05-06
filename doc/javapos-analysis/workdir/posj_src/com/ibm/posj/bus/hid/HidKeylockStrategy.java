package com.ibm.posj.bus.hid;

public interface HidKeylockStrategy {
   int LEGACY_STATUS_BYTE = 2;
   int LEGACY_REJECTED_BYTE = 1;
   int LEGACY_DEV_INFO_POSITION_BYTE = 1;

   int getKeylockStatusByteIndex();

   int getErrorStatusByteIndex();

   int getDevInfoPosition();

   int getDeviceId(byte[] var1);

   public interface BootModeKbdKeylockStrategy extends HidKeylockStrategy {
   }

   public interface Factory {
      HidKeylockStrategy createBootModeKbdKeylockStrategy();

      HidKeylockStrategy createNonBootModeKbdKeylockStrategy();

      HidKeylockStrategy createLegacyModeKbdKeylockStrategy();
   }

   public interface LegacyModeKbdKeylockStrategy extends HidKeylockStrategy {
   }

   public interface NonBootModeKbdKeylockStrategy extends HidKeylockStrategy {
   }
}
