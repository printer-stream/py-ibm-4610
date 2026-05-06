package com.ibm.posj.kbd;

abstract class AbstractKbdMapping implements KbdMapping {
   public static final POSKeyboardData[] EMPTY_ARRAY = new POSKeyboardData[0];

   public boolean getExtendedKbdMapping() {
      return false;
   }

   public void setExtendedKbdMapping(boolean ext) {
   }
}
