package com.ibm.posj.printer;

import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.event.PrintStatus;

public interface CmdCompleteStatusVisitor extends StatusVisitor {
   void setResponseProcessor(StatusVisitor.ResponseProcessor var1);

   void setCurrentStatus(PrintStatus var1);

   PrintStatus getCurrentStatus();

   void visit(POSPrinterCmd var1);

   boolean isSuccessful();

   void reset();

   void succeeded();

   void setCache(byte var1);

   byte getCache();
}
