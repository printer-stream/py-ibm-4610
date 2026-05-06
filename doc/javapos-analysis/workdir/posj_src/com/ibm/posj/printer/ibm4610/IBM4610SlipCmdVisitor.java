package com.ibm.posj.printer.ibm4610;

import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.SlipCmdVisitor;
import com.ibm.posj.util.IBM4610PrinterCmdV;

public class IBM4610SlipCmdVisitor extends IBM4610PrinterCmdV implements SlipCmdVisitor {
   private boolean slpManipulator = false;

   public void visitStartScanCmd(IBM4610PrinterCmd.StartScanCmd ssc) {
      this.slpManipulator = true;
   }

   public void visitReadSlipCmd(POSPrinterCmd.ReadSlipCmd rsc) {
      this.slpManipulator = true;
   }

   public void visitChangePrintSideCmd(IBM4610PrinterCmd.ChangePrintSideCmd cmd) {
      this.slpManipulator = true;
   }

   public void visitRotatePrintCmd(POSPrinterCmd.RotatePrintCmd rpc) {
      if ((4 & rpc.getStation()) > 0) {
         this.slpManipulator = true;
      }
   }

   public void visitSelectStationCmd(POSPrinterCmd.SelectStationCmd stc) {
      if ((4 & stc.getStation()) > 0 && stc.isOutputCmd()) {
         this.slpManipulator = true;
      }
   }

   public void visit(POSPrinterCmd x) {
      this.slpManipulator = false;
      if (null != x.getMasterCmd() && x.getMasterCmd() != x) {
         x.getMasterCmd().accept(this);
      }

      if (!this.slpManipulator && x != x.getOwnerCmd()) {
         x.getOwnerCmd().accept(this);
      }
   }

   public boolean isSlpManipulator() {
      return this.slpManipulator;
   }
}
