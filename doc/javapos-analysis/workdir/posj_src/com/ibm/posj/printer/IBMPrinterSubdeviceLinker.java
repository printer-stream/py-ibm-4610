package com.ibm.posj.printer;

import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.bus.HandleImp;

public interface IBMPrinterSubdeviceLinker {
   void init() throws HandleException;

   void submit(HandleCmd var1) throws HandleException;

   void setHandleImp(HandleImp var1);

   HandleImp getHandleImp();
}
