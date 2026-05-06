package com.ibm.posj.event;

public interface PosSystemListener {
   void posDeviceAttached(PosSystemEvent var1);

   void posDeviceDetached(PosSystemEvent var1);
}
