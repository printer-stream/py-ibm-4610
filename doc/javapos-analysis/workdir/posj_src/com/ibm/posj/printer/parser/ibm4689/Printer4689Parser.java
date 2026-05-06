package com.ibm.posj.printer.parser.ibm4689;

import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.parser.AbstractPrinterParser;
import com.ibm.posj.printer.parser.PrinterEscCmdProcessor;
import com.ibm.posj.printer.parser.PrinterParserData;
import com.ibm.posj.printer.parser.PrinterParserState;

public class Printer4689Parser extends AbstractPrinterParser {
   private POSPrinterCmd.Factory factory = null;
   private PrinterHandleState handleState = null;
   private PrinterParserState parserState = new Printer4689ParserState();
   private PrinterEscCmdProcessor escProcessor = null;
   private PrinterParserData recPrinterParserData = null;
   private PrinterParserData jnPrinterParserData = null;
   public static final int INIT_STATE = 0;
   public static final int ELEM_RECEIVED_STATE = 1;
   public static final int LINE_COMPLETE_STATE = 2;
   public static final int TRUNCATE_VERIFICATION_STATE = 3;
   public static final int DATA_TRUNCATED_STATE = 4;
   public static final int ELEMENTS_REMAINIG_STATE = 5;
   public static final int END_OF_DATA_STATE = 6;
   public static final int CMD_CREATION_STATE = 7;
   public static final int NON_TXT_ATTR_CMD_STATE = 8;
   public static final int END_STATE = 9;

   public Printer4689Parser(POSPrinterCmd.Factory factory, PrinterHandleState handleState) {
      this.factory = factory;
      this.handleState = handleState;
      this.escProcessor = new Printer4689EscCmdProcessor(factory, handleState, this.parserState);
      this.recPrinterParserData = new Printer4689ParserData();
      this.recPrinterParserData.setEscCmdProcessor(this.escProcessor);
      this.recPrinterParserData.setStation((byte)2);
      this.jnPrinterParserData = new Printer4689ParserData();
      this.jnPrinterParserData.setEscCmdProcessor(this.escProcessor);
      this.jnPrinterParserData.setStation((byte)8);
   }

   public PrinterParserState getParserState() {
      return this.parserState;
   }

   public PrinterEscCmdProcessor getEscCmdProcessor() {
      return this.escProcessor;
   }

   public void reset() {
      this.recPrinterParserData.clearProperties();
      this.jnPrinterParserData.clearProperties();
   }

   protected PrinterParserData getParserData(byte station) {
      PrinterParserData parserData = null;
      if (station == 2) {
         parserData = this.recPrinterParserData;
      } else {
         if (station != 8) {
            throw new RuntimeException("Station " + station + " not supported");
         }

         parserData = this.jnPrinterParserData;
      }

      return parserData;
   }
}
