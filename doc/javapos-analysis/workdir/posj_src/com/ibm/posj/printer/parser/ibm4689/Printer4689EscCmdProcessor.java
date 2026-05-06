package com.ibm.posj.printer.parser.ibm4689;

import com.ibm.posj.IBM4689PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.parser.DefaultPrinterEscCmd;
import com.ibm.posj.printer.parser.DefaultPrinterEscCmdProcessor;
import com.ibm.posj.printer.parser.PrinterEscCmd;
import com.ibm.posj.printer.parser.PrinterEscCmdVisitor;
import com.ibm.posj.printer.parser.PrinterParserState;
import java.util.HashMap;

public class Printer4689EscCmdProcessor extends DefaultPrinterEscCmdProcessor {
   public Printer4689EscCmdProcessor(POSPrinterCmd.Factory factory, PrinterHandleState handleState, PrinterParserState parserState) throws IllegalArgumentException {
      super(factory, handleState, parserState);
   }

   public HashMap getEscCmdHashMap() {
      if (this.escCmdHashMap == null) {
         PrinterEscCmd.Factory escCmdFactory = new DefaultPrinterEscCmd.Factory();
         this.escCmdHashMap = new HashMap();
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("P"), escCmdFactory.createPaperCutEscCmd(1, "Paper cut", (byte)3));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("fP"), escCmdFactory.createFeedPaperCutEscCmd(2, "Feed and paper cut", (byte)3));
         this.escCmdHashMap
            .put(new DefaultPrinterEscCmdProcessor.CmdKey("sP"), escCmdFactory.createFeedPaperCutStampEscCmd(3, "Feed, paper cut and stamp", (byte)3));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("sL"), escCmdFactory.createFireStampEscCmd(4, "Fire stamp", (byte)3));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("B"), escCmdFactory.createPrintBitmapEscCmd(5, "Print bitmap", (byte)3));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("tL"), escCmdFactory.createPrintTopLogoEscCmd(6, "Print top logo", (byte)5));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("bL"), escCmdFactory.createPrintBottomLogoEscCmd(7, "Print bottom logo", (byte)5));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("lF"), escCmdFactory.createFeedLinesEscCmd(8, "Feed lines", (byte)3));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("uF"), escCmdFactory.createFeedUnitsEscCmd(9, "Feed units", (byte)3));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("rF"), escCmdFactory.createFeedReverseEscCmd(10, "Fedd reverse", (byte)3));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("fT"), escCmdFactory.createFontTypefaceEscCmd(11, "Font type face", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("bC"), escCmdFactory.createBoldEscCmd(12, "Bold", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("uC"), escCmdFactory.createUnderlineEscCmd(13, "Underline", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("iC"), escCmdFactory.createItalicEscCmd(14, "Italic", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("rC"), escCmdFactory.createAlternateColorEscCmd(15, "Alternate color", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("rvC"), escCmdFactory.createReverseVideoEscCmd(16, "Reverse video", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("sC"), escCmdFactory.createShadingEscCmd(17, "Shading", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("C"), escCmdFactory.createHighWideControlEscCmd(18, "High/wide control", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("hC"), escCmdFactory.createScaleHorizontallyEscCmd(19, "Scale horizontally", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("vC"), escCmdFactory.createScaleVerticallyEscCmd(20, "Scale vertically", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("fC"), escCmdFactory.createRGBColorEscCmd(21, "RGB color", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("tbC"), escCmdFactory.createSubScriptEscCmd(22, "Subscript", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("tpC"), escCmdFactory.createSuperScriptEscCmd(23, "Superscript", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("cA"), escCmdFactory.createCenterEscCmd(24, "Center justify", (byte)4));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("rA"), escCmdFactory.createRightJustifyEscCmd(25, "Right justify", (byte)4));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("N"), escCmdFactory.createNormalEscCmd(26, "Normal", (byte)1));
      }

      return this.escCmdHashMap;
   }

   public PrinterEscCmdVisitor getCrtPOSPrtCmdVisitor() {
      if (this.crtPOSPrtCmdVisitor == null) {
         this.crtPOSPrtCmdVisitor = new Printer4689EscCmdProcessor.Parser4689CreatePOSPrinterCmdV(this.factory, this.parserState, this.handleState);
      }

      return this.crtPOSPrtCmdVisitor;
   }

   protected int checkPaperCut(PrinterEscCmd cmd) throws IllegalArgumentException {
      int paperCutParam = super.checkPaperCut(cmd);
      if (cmd.getParameter() == 0) {
         paperCutParam = 0;
      }

      if (cmd.isValidateOnly()) {
         if (cmd.getStation() == 8 || cmd.getStation() == 4) {
            throw new IllegalArgumentException("Parameter not precisely supported");
         }

         if (cmd.getParameter() > 0 && cmd.getParameter() < 100) {
            throw new IllegalArgumentException("The PaperCut parameter is invalid :" + paperCutParam);
         }
      }

      return paperCutParam;
   }

   protected void checkParScaleHorizontally(PrinterEscCmd cmd) throws IllegalArgumentException {
      if (cmd.getParameter() > 2) {
         cmd.setParameter(2);
      }

      super.checkParScaleHorizontally(cmd);
   }

   static class Parser4689CreatePOSPrinterCmdV extends DefaultPrinterEscCmdProcessor.ParserCreatePOSPrinterCmdV {
      static short MAX_FEED_LINES_BACK = 250;

      Parser4689CreatePOSPrinterCmdV(POSPrinterCmd.Factory factory, PrinterParserState parserState, PrinterHandleState handleState) {
         super(factory, parserState, handleState);
      }

      public void visitPaperCutEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         if (cmd.getStation() == 2) {
            if (cmd.getClosestParameter() > 0) {
               super.visitPaperCutEscCmd(cmd);
            } else {
               cmd.setPOSPrinterCmd(this.addMultiFeedLines(1, cmd.getStation()));
            }
         }
      }

      public void visitFeedPaperCutEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         if (cmd.getStation() == 2) {
            super.visitFeedPaperCutEscCmd(cmd);
         } else {
            cmd.setPOSPrinterCmd(this.addMultiFeedLines(1, cmd.getStation()));
         }
      }

      public void visitFeedPaperCutStampEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         if (cmd.getStation() == 2) {
            super.visitFeedPaperCutStampEscCmd(cmd);
         } else {
            cmd.setPOSPrinterCmd(this.addMultiFeedLines(1, cmd.getStation()));
         }
      }

      public void visitFeedReverseEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         float factor = 30.88F;
         POSPrinterCmd masterCmd = null;
         float units = (float)cmd.getClosestParameter() * factor;
         units += factor;

         for (masterCmd = this.getFactory().createSelectStationCmd(cmd.getStation()); units >= (float)MAX_FEED_LINES_BACK; units -= (float)MAX_FEED_LINES_BACK) {
            masterCmd.appendPOSPrinterCmd(this.getFactory().createFeedReverseCmd(MAX_FEED_LINES_BACK));
         }

         if (units > 0.0F) {
            masterCmd.appendPOSPrinterCmd(this.getFactory().createFeedReverseCmd((short)((int)units)));
         }

         masterCmd.appendPOSPrinterCmd(((IBM4689PrinterCmd.Factory)this.getFactory()).createInitPrinterCmd());
         cmd.setPOSPrinterCmd(masterCmd);
      }
   }
}
