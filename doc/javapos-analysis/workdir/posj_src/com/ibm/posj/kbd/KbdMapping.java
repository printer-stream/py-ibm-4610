package com.ibm.posj.kbd;

public interface KbdMapping {
   POSKeyboardData[] translateKey(byte[] var1);

   void setExtendedKbdMapping(boolean var1);

   boolean getExtendedKbdMapping();
}
