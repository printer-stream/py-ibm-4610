package com.ibm.posj.bus.printer;

import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.bus.PrinterBusWriter;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.bus.PrinterSubDevices;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintStatus;
import java.util.Iterator;

public interface PrinterUtil {
   IBMPrinterImp createPrinterImp(PrinterWriter var1, DefaultPOSPrinterHandle var2);

   PrinterWriter createWriter(PrinterBusWriter var1, PrinterPacket var2, POSPrinterHandle var3, POSPrinterCmd.Factory var4);

   void firePrintDataEvent(PrintDataEvent var1);

   void subDeviceDistribute(Handle var1, PrintStatus var2);

   int getPrinterDataType(PrintStatus var1);

   void init(HandleImp var1, IBMPrinterImp var2, POSPrinterCmd var3, int var4) throws HandleException;

   void addDevice(HandleKey var1, PrinterSubDevices var2);

   Iterator getSecondaryHandleImps();
}
