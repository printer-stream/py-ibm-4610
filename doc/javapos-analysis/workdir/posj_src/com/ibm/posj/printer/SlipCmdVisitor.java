package com.ibm.posj.printer;

import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterCmdVisitor;

public interface SlipCmdVisitor extends POSPrinterCmdVisitor {
   void visitReadSlipCmd(POSPrinterCmd.ReadSlipCmd var1);

   void visitRotatePrintCmd(POSPrinterCmd.RotatePrintCmd var1);

   void visit(POSPrinterCmd var1);

   boolean isSlpManipulator();
}
