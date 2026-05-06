package com.ibm.posj;

import com.ibm.jutil.logging.LogHelper;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.util.PrinterTimeStamper;

public interface POSPrinterHandle extends Handle {
   POSPrinterCmd.Factory getPOSPrinterCmdFactory();

   PrinterHandleState getPrinterHandleState();

   PrinterTimeStamper timeStamp();

   boolean isDataPending();

   void clearOutput();

   boolean isActive();

   LogHelper getPrinterLogHelper();
}
