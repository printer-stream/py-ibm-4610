package com.ibm.posj.bus;

import com.ibm.posj.HandleException;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.PrinterWriter;
import java.util.Iterator;

public interface POSPrinterHandleImp extends HandleImp {
   PrinterHandleState getPrinterHandleState();

   POSPrinterCmd.Factory getPrintCmdFactory();

   PrinterWriter getWriter();

   void clearOutput();

   boolean isDataPending();

   Iterator getSecondaryHandleImps();

   int getPrinterID_microcodeLevel();

   void setPrinterID_microcodeLevel(int var1);

   void enable();

   void disable();

   void rawSubmit(byte[] var1) throws HandleException;
}
