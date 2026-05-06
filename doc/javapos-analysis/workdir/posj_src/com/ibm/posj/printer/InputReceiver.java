package com.ibm.posj.printer;

import com.ibm.jutil.BooleanMonitor;

public interface InputReceiver {
   void writePrintCmds();

   BooleanMonitor getIdleKey();

   boolean isOutputPending();
}
