package com.ibm.posj.bus.hid;

import com.ibm.posj.kbd.POSKeyboardIndicator;
import java.util.BitSet;

public interface HidPOSKeyboardStrategy {
   int getStatusLength();

   int getDevInfoPosition();

   int getDevID(byte[] var1);

   byte createIndicatorConfig(POSKeyboardIndicator var1);

   void createIndicatorConfig(POSKeyboardIndicator var1, BitSet var2);

   public interface BootModeDBCSPOSKeyboardStrategy extends HidPOSKeyboardStrategy {
   }

   public interface BootModeSBCSPOSKeyboardStrategy extends HidPOSKeyboardStrategy {
   }

   public interface Factory {
      HidPOSKeyboardStrategy createNonBootModePOSKeyboardStrategy(boolean var1);

      HidPOSKeyboardStrategy createBootModePOSKeyboardStrategy(boolean var1);

      HidPOSKeyboardStrategy createLegacyModePOSKeyboardStrategy(boolean var1);
   }

   public interface LegacyModeDBCSPOSKeyboardStrategy extends HidPOSKeyboardStrategy {
   }

   public interface LegacyModeSBCSPOSKeyboardStrategy extends HidPOSKeyboardStrategy {
   }

   public interface NonBootModeDBCSPOSKeyboardStrategy extends HidPOSKeyboardStrategy {
   }

   public interface NonBootModeSBCSPOSKeyboardStrategy extends HidPOSKeyboardStrategy {
   }
}
