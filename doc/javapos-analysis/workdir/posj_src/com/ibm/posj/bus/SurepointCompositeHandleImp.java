package com.ibm.posj.bus;

import com.ibm.posj.RuntimePosException;

public interface SurepointCompositeHandleImp extends CompositeHandleImp, CompositeSubmitter {
   KeylockHandleImp getKeylockHandleImp() throws RuntimePosException;

   MSRHandleImp getMSRHandleImp() throws RuntimePosException;

   POSKeyboardHandleImp getPOSKeyboardHandleImp();

   ToneIndicatorHandleImp getToneIndicatorHandleImp() throws RuntimePosException;

   boolean hasMSR();

   boolean hasPOSKeyboard();

   boolean hasToneIndicator();
}
