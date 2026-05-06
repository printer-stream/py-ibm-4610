package com.ibm.posj.printer.ibm4610;

import com.ibm.jutil.logging.LogHelper;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.HandleCmdVisitor;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.PrinterWriter;

public class IBM4610Rs232Imp extends IBM4610Imp {
   public IBM4610Rs232Imp(PrinterWriter mainWriter, DefaultPOSPrinterHandle handle, LogHelper logger) {
      super(mainWriter, handle, logger);
   }

   protected HandleCmdVisitor getChaseVisitor() {
      if (null == this.chaseVisitor) {
         this.chaseVisitor = new ChaseVisitors.LineCntChaseVisitor(this);
      }

      return this.chaseVisitor;
   }

   public boolean adjusted(DefaultPOSPrinterCmd dpc) {
      if (dpc instanceof POSPrinterCmd.FeedUnitsCmd) {
         this.writeSubmit();
      } else if (this.getPendingList().getDataSize() >= 2000) {
         this.writeSubmit();
      }

      return false;
   }
}
