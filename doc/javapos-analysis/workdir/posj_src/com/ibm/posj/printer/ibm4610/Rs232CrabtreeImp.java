package com.ibm.posj.printer.ibm4610;

import com.ibm.jutil.logging.LogHelper;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.printer.IBMPrinterState;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.PrintDataEvent;

public class Rs232CrabtreeImp extends IBM4610Rs232Imp {
   public Rs232CrabtreeImp(PrinterWriter mainWriter, DefaultPOSPrinterHandle handle, LogHelper logger) {
      super(mainWriter, handle, logger);
   }

   protected PrinterHandleState createPrinterHandleState() {
      return new Rs232CrabtreeImp.CrabtreeState(this.logger, this.getHandle());
   }

   public void receivePrintDataEvent(PrintDataEvent pde) {
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("receivePrintDataEvent printimp " + pde.getType());
      }

      IBMPrinterState ibmState = (IBMPrinterState)this.getPrinterHandleState();
      if ((1 & pde.getType()) > 0) {
         super.receivePrintDataEvent(pde);
         ibmState.setFlipperPresent(true);
         ibmState.setMICRPresent(true);
         ibmState.setECLevel(pde.byteAt((byte)5) & 255);
         ibmState.setMaxPrintBitmapSize((byte)2, 8000);
         ibmState.setMaxPrintBitmapSize((byte)4, 8000);
      }
   }

   class CrabtreeState extends IBM4610Imp.IBM4610PrinterState {
      CrabtreeState(LogHelper logger, POSPrinterHandle h) {
         super(logger, h);
      }

      public boolean stationPresent(byte station) {
         switch (station) {
            case 2:
            case 4:
               return true;
            default:
               return false;
         }
      }
   }
}
