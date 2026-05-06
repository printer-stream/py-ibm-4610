package com.ibm.posj.printer;

import com.ibm.jutil.tasks.SharedThreader.ActionObject;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;
import com.ibm.posj.printer.event.PrintErrorEvent;

public interface RetryInputQueue extends DeviceInputQueue, ActionObject {
   void retryOutput(PrintCmdList var1);

   void handleError(PrintCmd var1, PrintErrorEvent var2);

   void setInputQueue(PrinterWriter.WriterInputQueue var1);
}
