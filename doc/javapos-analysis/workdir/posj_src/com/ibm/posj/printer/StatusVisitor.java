package com.ibm.posj.printer;

import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterCmdVisitor;
import com.ibm.posj.printer.event.PrintStatus;

public interface StatusVisitor extends POSPrinterCmdVisitor {
   void setResponseProcessor(StatusVisitor.ResponseProcessor var1);

   PrintStatus getCurrentStatus();

   void setCurrentStatus(PrintStatus var1);

   boolean isSuccessful();

   void succeeded();

   void reset();

   public interface ResponseProcessor {
      void processSend();

      void processComplete(POSPrinterCmd var1);
   }
}
