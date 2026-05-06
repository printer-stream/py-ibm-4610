package com.ibm.posj.flash;

import com.ibm.hid.DisconnectEvent;
import com.ibm.hid.HidExceptionEvent;
import com.ibm.hid.ReportEvent;

public interface FlashExe {
   void execute() throws FlashException;

   void reset() throws FlashException;

   void setPrinterInfo(int var1, int var2, int var3);

   void hidFlashReportEventOccurred(ReportEvent var1);

   void hidFlashExceptionEventOccurred(HidExceptionEvent var1);

   void hidFlashDeviceDisconnected(DisconnectEvent var1);
}
