package com.ibm.posj.printer.parser.ibm4610;

import com.ibm.posj.printer.parser.DefaultPrinterParserState;

public class Printer4610ParserState extends DefaultPrinterParserState {
   public int getPrinterStateType() {
      return 1;
   }

   public void initParserState() {
      super.initParserState();
      this.setCap2Color((byte)2, true);
      this.setCap2Color((byte)4, false);
   }
}
