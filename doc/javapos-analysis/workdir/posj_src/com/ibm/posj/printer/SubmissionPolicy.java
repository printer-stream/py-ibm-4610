package com.ibm.posj.printer;

import com.ibm.posj.bus.printer.cmds.PrintCmdList;

public interface SubmissionPolicy {
   void setMaxData(int var1);

   void setMinimumData(int var1);

   PrintCmdList askForData();
}
