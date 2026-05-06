package com.ibm.posj.printer;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.event.PrintStatus;

public interface CmdLoadedStatusVisitor extends StatusVisitor {
   void decorateCmd(DefaultPOSPrinterCmd var1);

   void setResponseProcessor(StatusVisitor.ResponseProcessor var1);

   void setCurrentStatus(PrintStatus var1);

   PrintStatus getCurrentStatus();

   void visit(POSPrinterCmd var1);

   boolean isSuccessful();

   void reset();

   void succeeded();
}
