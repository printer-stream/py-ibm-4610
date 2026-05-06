package com.ibm.posj.printer;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.util.DefaultHandleCmdV;

class IBMPrinterImp$1 extends DefaultHandleCmdV {
   IBMPrinterImp$1(IBMPrinterImp this$0) {
      this.this$0 = this$0;
   }

   public void visitPOSPrinterCmd(POSPrinterCmd cmd1) {
      if (this.this$0.getPrinterHandleState().getPrinterID() > 0 && cmd1.getCode() == 103) {
         ((SystemCmd.DeviceInfoRequestCmd)cmd1).setDeviceId(this.this$0.getPrinterState().getPrinterID());
         ((SystemCmd.DeviceInfoRequestCmd)cmd1).setDeviceType(this.this$0.getPrinterState().getPrinterType());
         ((SystemCmd.DeviceInfoRequestCmd)cmd1).setSerialNumber(this.this$0.getPrinterState().getSerialNumber());
         ((SystemCmd.DeviceInfoRequestCmd)cmd1).setFirmwareLevel(this.this$0.getPrinterState().getPrinterEC());
         cmd1.setCompleted(true);
      } else {
         DefaultPOSPrinterCmd posPrinterCmd = (DefaultPOSPrinterCmd)cmd1;
         if (this.this$0.traceOn()) {
            this.this$0.trace(Thread.currentThread().getName() + " >>scheduler post" + cmd1);
         }

         posPrinterCmd.setEventHandler(IBMPrinterImp.access$000(this.this$0).getEventHelper());

         try {
            posPrinterCmd.acceptMultiVisitor(this.this$0.filter);
         } catch (Exception var4) {
         }

         if (posPrinterCmd.isImmediate()) {
            IBMPrinterImp.access$100(this.this$0).postImmediate(posPrinterCmd, !posPrinterCmd.isSettingsCmd());
         } else {
            if (this.this$0.traceOn()) {
               this.this$0.getHandle().timeStamp().onHandleQueue(cmd1);
            }

            IBMPrinterImp.access$100(this.this$0).post(posPrinterCmd, !posPrinterCmd.isSettingsCmd());
         }

         IBMPrinterImp.access$208(this.this$0);
         if (this.this$0.traceOn()) {
            this.this$0.trace("done posting ");
         }
      }
   }

   public void visitSystemCmd(SystemCmd cmd) {
      IBMPrinterImp.access$100(this.this$0).postImmediate(cmd, true);
   }
}
