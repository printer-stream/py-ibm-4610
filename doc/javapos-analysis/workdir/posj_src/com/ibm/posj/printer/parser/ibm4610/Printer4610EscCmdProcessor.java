package com.ibm.posj.printer.parser.ibm4610;

import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.parser.PrinterParserState;
import java.util.HashMap;

public class Printer4610EscCmdProcessor extends Legacy4610EscCmdProcessor {
   public Printer4610EscCmdProcessor(POSPrinterCmd.Factory factory, PrinterHandleState handleState, PrinterParserState parserState) throws IllegalArgumentException {
      super(factory, handleState, parserState);
   }

   public HashMap getEscCmdHashMap() {
      super.getEscCmdHashMap();
      return this.escCmdHashMap;
   }
}
