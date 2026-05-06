package com.ibm.posj.printer.parser.ibm4689;

import com.ibm.posj.printer.parser.DefaultPrinterParserState;

public class Printer4689ParserState extends DefaultPrinterParserState {
   public int getPrinterStateType() {
      return 2;
   }

   public int getCharSize(byte station) {
      return this.getFontWideMagnification(station) * this.getLineWidth(station) / this.getLineChars(station);
   }
}
