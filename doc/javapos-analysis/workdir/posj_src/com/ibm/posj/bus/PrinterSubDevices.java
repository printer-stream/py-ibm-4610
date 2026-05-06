package com.ibm.posj.bus;

import com.ibm.posj.HandleKey;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintStatus;

public interface PrinterSubDevices {
   HandleKey getHandleKey();

   void receivePrintStatus(PrintStatus var1);

   void receivePrintDataEvent(PrintDataEvent var1);

   void offLine(Object var1);

   void onLine(Object var1);

   void busException(Object var1);
}
