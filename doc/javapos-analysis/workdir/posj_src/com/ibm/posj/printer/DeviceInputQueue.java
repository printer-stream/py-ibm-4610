package com.ibm.posj.printer;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;

public interface DeviceInputQueue {
   void addCmdList(PrintCmdList var1);

   void addImmediateCmdList(PrintCmdList var1);

   void submit(PrintCmd var1);

   void processErrorResponse(PrintCmd var1, byte var2);

   void setSubmissionPolicy(SubmissionPolicy var1);

   BooleanMonitor getBlockKey();

   PrintCmdList getNextList();

   boolean isDataPending();
}
