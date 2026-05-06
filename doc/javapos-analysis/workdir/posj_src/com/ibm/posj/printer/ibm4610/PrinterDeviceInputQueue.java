package com.ibm.posj.printer.ibm4610;

import com.ibm.posj.bus.printer.cmds.PrintCmdList;
import com.ibm.posj.printer.DefaultDeviceInputQueue;
import com.ibm.posj.printer.PrinterWriter;

public class PrinterDeviceInputQueue extends DefaultDeviceInputQueue {
   public PrinterDeviceInputQueue(PrinterWriter.PrinterWriterUser user, String deviceName) {
      super(user, deviceName);
   }

   public void addCmdList(PrintCmdList p) {
      if (null == p) {
         this.getMasterQ().flagQueue();
      } else {
         if (this.traceOn()) {
            this.trace("add ", p.toString());
         }

         if (p.hasCommands()) {
            this.getPendingQueue().add(p);
            if (p.isOutputList()) {
               this.incrementOutCnt();
            }
         }
      }
   }
}
