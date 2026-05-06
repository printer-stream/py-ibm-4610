package com.ibm.posj.printer.parser.sureone;

import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.parser.DefaultPrinterEscCmd;
import com.ibm.posj.printer.parser.DefaultPrinterEscCmdProcessor;
import com.ibm.posj.printer.parser.PrinterEscCmd;
import com.ibm.posj.printer.parser.PrinterParserState;
import java.util.HashMap;

public class PrinterSureoneEscCmdProcessor extends DefaultPrinterEscCmdProcessor {
   public PrinterSureoneEscCmdProcessor(POSPrinterCmd.Factory factory, PrinterHandleState handleState, PrinterParserState parserState) throws IllegalArgumentException {
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
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("fT"), escCmdFactory.createFontTypefaceEscCmd(11, "Font type face", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("uF"), escCmdFactory.createFeedUnitsEscCmd(9, "Feed units", (byte)3));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("iC"), escCmdFactory.createItalicEscCmd(14, "Italic", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("rC"), escCmdFactory.createAlternateColorEscCmd(15, "Alternate color", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("rvC"), escCmdFactory.createReverseVideoEscCmd(16, "Reverse video", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("sC"), escCmdFactory.createShadingEscCmd(17, "Shading", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("rF"), escCmdFactory.createFeedReverseEscCmd(10, "Fedd reverse", (byte)3));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("bC"), escCmdFactory.createBoldEscCmd(12, "Bold", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("uC"), escCmdFactory.createUnderlineEscCmd(13, "Underline", (byte)1));
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

   protected int checkPaperCut(PrinterEscCmd cmd) throws IllegalArgumentException {
      int paperCutParam = super.checkPaperCut(cmd);
      if (cmd.isValidateOnly() && paperCutParam != 0 && paperCutParam != 100) {
         throw new IllegalArgumentException("The PaperCut parameter is invalid :" + paperCutParam);
      } else {
         return paperCutParam;
      }
   }
}
