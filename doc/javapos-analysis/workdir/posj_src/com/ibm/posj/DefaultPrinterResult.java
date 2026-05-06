package com.ibm.posj;

import com.ibm.posj.printer.event.PrintErrorEvent;

public class DefaultPrinterResult extends AbstractHandleCmd.DefaultResult implements POSPrinterCmd.PrinterResult {
   PrintErrorEvent event = null;

   public PrintErrorEvent getErrorEvent() {
      return this.event;
   }

   public void setPrintErrorEvent(PrintErrorEvent event) {
      this.event = event;
   }
}
