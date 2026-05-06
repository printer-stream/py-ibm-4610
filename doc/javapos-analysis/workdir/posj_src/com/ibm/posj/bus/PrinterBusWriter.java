package com.ibm.posj.bus;

import com.ibm.posj.HandleException;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.PrinterTimeStamper;
import java.util.List;

public interface PrinterBusWriter {
   boolean isStatusPending();

   void processExtraData(PrintStatus var1);

   List respondToFreeze();

   PrinterTimeStamper timeStamp();

   void clearBuffers() throws HandleException;
}
