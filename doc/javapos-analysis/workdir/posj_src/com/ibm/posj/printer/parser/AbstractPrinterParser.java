package com.ibm.posj.printer.parser;

import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.ByteEncoder;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.HandleException;
import com.ibm.posj.POSPrinterCmd;

public abstract class AbstractPrinterParser implements PrinterParser {
   private POSPrinterCmd masterPrinterCmd = null;
   protected ByteBuffer byteBuffer = null;
   private static Tracer printerTracer = TracerFactory.getInstance().createTracer("PrinterParser");
   private static final String TOP_LOGO_CMD = "\u001b|tL";
   private static final String BOTTOM_LOGO_CMD = "\u001b|bL";

   public boolean validateData(byte station, String data) throws IllegalAccessException, IllegalArgumentException {
      return this.validateData(station, data, false);
   }

   public boolean validateData(byte station, String data, boolean valOnly) throws IllegalAccessException, IllegalArgumentException {
      boolean success = true;
      this.byteBuffer = ByteBuffer.getByteBufferFactory().createByteBuffer();
      this.byteBuffer.replace(this.getByteEncoder().getBytes(data));
      PrinterParserData parserData = this.getParserData(station);
      parserData.reset();
      parserData.setData(this.byteBuffer);
      return parserData.validate(valOnly);
   }

   public boolean parseData(byte station, String data) throws HandleException {
      return this.parseData(station, data, true);
   }

   public boolean parseData(byte station, String data, boolean addLineFeedAtTheEnd) throws HandleException {
      boolean success = true;
      this.byteBuffer = ByteBuffer.getByteBufferFactory().createByteBuffer();
      this.setCharacterSet(this.getParserState().getCharacterSet());
      this.byteBuffer.replace(this.getByteEncoder().getBytes(data));
      PrinterParserData parserData = this.getParserData(station);
      parserData.reset();
      parserData.setData(this.byteBuffer);
      parserData.setAddLineFeedAtTheEnd(addLineFeedAtTheEnd);
      success = parserData.parse();
      if (!success && parserData.hasLogos()) {
         String dataLogos = this.expandLogos(data);
         this.byteBuffer.replace(this.getByteEncoder().getBytes(dataLogos));
         parserData.reset();
         parserData.setData(this.byteBuffer);
         success = parserData.parse();
      }

      this.masterPrinterCmd = parserData.getMasterCmd();
      return success;
   }

   public POSPrinterCmd getMasterPrinterCmd() {
      return this.masterPrinterCmd;
   }

   public abstract PrinterParserState getParserState();

   public abstract PrinterEscCmdProcessor getEscCmdProcessor();

   public abstract void reset();

   protected abstract PrinterParserData getParserData(byte var1);

   protected ByteEncoder getByteEncoder() {
      return this.getEscCmdProcessor().getByteEncoder();
   }

   protected void setCharacterSet(int characterSet) {
      if (characterSet != this.getByteEncoder().getCharacterSet()) {
         this.getByteEncoder().setCharacterSet(characterSet);
      }
   }

   protected String expandLogos(String data) {
      String topLogo = this.getLogoText((byte)1);
      String botLogo = this.getLogoText((byte)2);
      if (topLogo == null) {
         topLogo = "";
      } else {
         topLogo = replaceAll(topLogo, "\u001b|tL", "");
         topLogo = replaceAll(topLogo, "\u001b|bL", "");
      }

      String expandedText = replaceAll(data, "\u001b|tL", topLogo);
      if (botLogo == null) {
         botLogo = "";
      } else {
         botLogo = replaceAll(botLogo, "\u001b|tL", "");
         botLogo = replaceAll(botLogo, "\u001b|bL", "");
      }

      return replaceAll(expandedText, "\u001b|bL", botLogo);
   }

   protected String getLogoText(byte position) {
      String logoText = null;
      POSPrinterCmd.SetLogoCmd setLogoCmd = this.getEscCmdProcessor().getHandleState().getSetLogoCmd(position);
      if (setLogoCmd != null) {
         logoText = setLogoCmd.getData();
      }

      return logoText;
   }

   public static String replaceAll(String text, String textToReplace, String replacedBy) {
      int index = 0;
      int startingPos = 0;
      boolean textReplaced = false;
      String newString = "";

      while ((index = text.indexOf(textToReplace, startingPos)) != -1) {
         newString = newString + text.substring(startingPos, index);
         newString = newString + replacedBy;
         startingPos = index + textToReplace.length();
         if (!textReplaced) {
            textReplaced = true;
         }
      }

      if (!textReplaced) {
         newString = text;
      } else {
         newString = newString + text.substring(startingPos, text.length());
      }

      return newString;
   }

   public static Tracer getTracer() {
      return printerTracer;
   }
}
