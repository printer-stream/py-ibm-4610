package com.ibm.posj.printer.parser.sureone;

import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.parser.AbstractPrinterParser;
import com.ibm.posj.printer.parser.PrinterEscCmdProcessor;
import com.ibm.posj.printer.parser.PrinterParserData;
import com.ibm.posj.printer.parser.PrinterParserState;

public class PrinterSureoneParser extends AbstractPrinterParser {
   private POSPrinterCmd.Factory factory = null;
   private PrinterHandleState handleState = null;
   private PrinterParserState parserState = new PrinterSureoneParserState();
   private PrinterEscCmdProcessor escProcessor = null;
   private PrinterParserData recPrinterParserData = null;
   private PrinterParserData slpPrinterParserData = null;

   public PrinterSureoneParser(POSPrinterCmd.Factory factory, PrinterHandleState handleState) {
      this.factory = factory;
      this.handleState = handleState;
      this.escProcessor = new PrinterSureoneEscCmdProcessor(factory, handleState, this.parserState);
      this.recPrinterParserData = new PrinterParserData();
      this.recPrinterParserData.setEscCmdProcessor(this.escProcessor);
      this.recPrinterParserData.setStation((byte)2);
      this.slpPrinterParserData = new PrinterParserData();
      this.slpPrinterParserData.setEscCmdProcessor(this.escProcessor);
      this.slpPrinterParserData.setStation((byte)4);
   }

   public PrinterParserState getParserState() {
      return this.parserState;
   }

   protected PrinterParserData getParserData(byte station) {
      PrinterParserData parserData = null;
      if (station == 2) {
         parserData = this.recPrinterParserData;
      } else {
         if (station != 4) {
            throw new RuntimeException("Station " + station + "not supported");
         }

         parserData = this.slpPrinterParserData;
      }

      return parserData;
   }

   public PrinterEscCmdProcessor getEscCmdProcessor() {
      return this.escProcessor;
   }

   public void reset() {
      this.recPrinterParserData.clearProperties();
      this.slpPrinterParserData.clearProperties();
   }
}
