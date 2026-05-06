package com.ibm.posj.printer;

import com.ibm.posj.POSPrinterCmdVisitor;

public interface FilterCmdVisitor extends POSPrinterCmdVisitor {
   void reset();

   boolean isValidNormalorOtherCmd();
}
