package com.ibm.posj.bus;

import com.ibm.posj.HandleKey;

public interface IBMPrinterComposite extends POSPrinterHandleImp, CompositeHandleImp {
   void addDevice(HandleKey var1, PrinterSubDevices var2);
}
