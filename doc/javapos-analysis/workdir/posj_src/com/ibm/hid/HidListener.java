package com.ibm.hid;

import java.util.EventListener;

public interface HidListener extends EventListener {
   void reportEventOccurred(ReportEvent var1);

   void hidExceptionEventOccurred(HidExceptionEvent var1);

   void hidDeviceDisconnected(DisconnectEvent var1);
}
