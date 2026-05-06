package com.ibm.posj.printer;

import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;

public interface IBMPrinterToneIndicatorImp extends PrinterWriter.PrinterWriterUser {
   void submit(HandleCmd var1) throws HandleException;

   void init() throws HandleException;
}
