package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.PrintStatus;
import java.util.Iterator;
import java.util.List;

public class Rs485POSPrinterMod4HandleImp extends Rs485POSPrinterHandleImp {
   public Rs485POSPrinterMod4HandleImp(HandleKey key, SioDevice device) {
      super(key, device);
   }

   public void clearOutput() {
   }

   public POSPrinterCmd.Factory getPrintCmdFactory() {
      return null;
   }

   public PrinterHandleState getPrinterHandleState() {
      return null;
   }

   public int getPrinterID_microcodeLevel() {
      return 0;
   }

   public Iterator getSecondaryHandleImps() {
      return null;
   }

   public PrinterWriter getWriter() {
      return null;
   }

   public void setPrinterID_microcodeLevel(int printerID_microcodeLevel) {
   }

   protected void deviceOffline() {
   }

   protected IBMPrinterImp getPrinterImp() {
      return null;
   }

   public void processExtraData(PrintStatus status) {
   }

   public List respondToFreeze() {
      return null;
   }

   public void clearBuffers() throws HandleException {
   }

   public PrintStatus createPrintStatus(byte[] o) {
      return null;
   }

   public void distributeStatus(PrintStatus ps) {
   }
}
