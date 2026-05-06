package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.IBMPOSPrinterToneIndicatorHandleImp;
import com.ibm.posj.printer.IBMPrinterToneIndicatorImp;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintStatus;

public class Rs485POSPrinterToneIndicatorHandleImp extends Rs485ToneIndicatorHandleImp implements IBMPOSPrinterToneIndicatorHandleImp {
   private IBMPrinterToneIndicatorImp tiLinker = null;

   public Rs485POSPrinterToneIndicatorHandleImp(HandleKey key, SioDevice device, IBMPrinterToneIndicatorImp linker) {
      super(key, device);
      this.tiLinker = linker;
   }

   public void init() throws HandleException {
      this.tiLinker.init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.tiLinker.submit(cmd);
   }

   public void busException(Object e) {
   }

   public void offLine(Object eObject) {
   }

   public void onLine(Object eObject) {
   }

   public void receivePrintDataEvent(PrintDataEvent pde) {
   }

   public void receivePrintStatus(PrintStatus ps) {
   }
}
