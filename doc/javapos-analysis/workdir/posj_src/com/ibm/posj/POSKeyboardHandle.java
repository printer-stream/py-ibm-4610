package com.ibm.posj;

import com.ibm.posj.kbd.KbdMapping;
import com.ibm.posj.kbd.POSIndicator;
import com.ibm.posj.kbd.POSKeyboardMap;
import com.ibm.posj.kbd.PS2Indicator;

public interface POSKeyboardHandle extends Handle {
   POSKeyboardCmd.Factory getPOSKeyboardCmdFactory();

   void setPOSIndicatorState(POSIndicator var1);

   POSIndicator getPOSIndicatorState();

   PS2Indicator getPS2IndicatorState();

   KbdMapping createKbdMapping() throws HandleException;

   POSKeyboardMap createUserMapping(String var1) throws HandleException;

   void reset();
}
