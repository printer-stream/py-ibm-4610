package com.ibm.posj.bus;

import com.ibm.posj.FiscalPrinterInfoHelper;

public interface FiscalPrinterHandleImp extends HandleImp {
   FiscalPrinterInfoHelper getInfoHelper();

   void startProtocol();

   void stopProtocol();
}
