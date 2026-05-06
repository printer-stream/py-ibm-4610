package com.ibm.posj.bus;

import com.ibm.posj.RuntimePosException;

public interface KbdCompositeHandleImp extends CompositeHandleImp, CompositeSubmitter {
   POSKeyboardHandleImp getPOSKeyboardHandleImp();

   KeylockHandleImp getKeylockHandleImp() throws RuntimePosException;

   LineDisplayHandleImp getLineDisplayHandleImp() throws RuntimePosException;

   MSRHandleImp getMSRHandleImp() throws RuntimePosException;

   ToneIndicatorHandleImp getToneIndicatorHandleImp() throws RuntimePosException;

   boolean hasKeylock();

   boolean hasLineDisplay();

   boolean hasMSR();

   boolean hasToneIndicator();
}
