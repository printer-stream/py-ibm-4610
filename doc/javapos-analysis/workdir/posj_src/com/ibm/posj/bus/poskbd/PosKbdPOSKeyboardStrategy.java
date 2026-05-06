package com.ibm.posj.bus.poskbd;

import com.ibm.posj.kbd.POSKeyboardIndicator;
import java.util.BitSet;

public interface PosKbdPOSKeyboardStrategy {
   byte COMMON_OFFLINE_POSITION = 2;
   byte COMMON_SCROLL_LOCK_POSITION = 7;
   byte COMMON_NUM_LOCK_POSITION = 5;
   byte COMMON_CAPS_LOCK_POSITION = 6;
   byte SBCS_WAIT_POSITION = 1;
   byte SBCS_MSG_PEND_POSITION = 3;
   byte SBCS_BLANK_POSITION = 4;
   byte DBCS_WAIT_POSITION = 3;
   byte DBCS_SYS_MSG_POSITION = 1;
   byte DBCS_READY_POSITION = 4;

   BitSet createIndicatorConfig(POSKeyboardIndicator var1);

   public interface DBCSPOSKeyboardStrategy extends PosKbdPOSKeyboardStrategy {
   }

   public interface Factory {
      PosKbdPOSKeyboardStrategy createSBCSPOSKeyboardStrategy();

      PosKbdPOSKeyboardStrategy createDBCSPOSKeyboardStrategy();
   }

   public interface SBCSPOSKeyboardStrategy extends PosKbdPOSKeyboardStrategy {
   }
}
