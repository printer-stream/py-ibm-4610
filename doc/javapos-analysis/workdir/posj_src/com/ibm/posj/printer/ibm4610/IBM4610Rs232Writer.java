package com.ibm.posj.printer.ibm4610;

import com.ibm.jutil.logging.LogHelper;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.PrinterBusWriter;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.printer.CmdLoadedStatusVisitor;

public class IBM4610Rs232Writer extends IBM4610Writer {
   private CmdLoadedStatusVisitor lvisitor = null;
   public String WRITER_MINIMUM = "posj.IBM4610Writer.minimum";
   public static final int RESET_SLEEP = 1400;

   public IBM4610Rs232Writer(PrinterBusWriter parent, PrinterPacket packet, LogHelper logger, IBM4610PrinterCmd.Factory factory, boolean useLC) {
      super(parent, packet, logger, factory, useLC);
      this.lvisitor = new IBM4610Rs232Writer.CmdLoadVisitor4610();
      this.lvisitor.setResponseProcessor(this);
   }

   protected CmdLoadedStatusVisitor getCmdLoadVisitor() {
      return this.lvisitor;
   }

   private class CmdLoadVisitor4610 extends IBM4610Writer.CmdLoadVisitor4610 {
      private CmdLoadVisitor4610() {
      }

      public void visit(POSPrinterCmd dpc) {
         this.reset();
         this.decorateCmd((DefaultPOSPrinterCmd)dpc);
         this.succeeded();
         this.processor.processSend();
      }

      public void decorateCmd(DefaultPOSPrinterCmd cmd) {
      }
   }
}
