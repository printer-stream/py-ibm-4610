package com.ibm.posj.printer.parser.ibm4610;

import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.parser.DefaultPrinterEscCmd;
import com.ibm.posj.printer.parser.DefaultPrinterEscCmdProcessor;
import com.ibm.posj.printer.parser.PrinterEscCmd;
import com.ibm.posj.printer.parser.PrinterParserState;
import java.util.HashMap;

public class Legacy4610EscCmdProcessor extends DefaultPrinterEscCmdProcessor {
   private PrinterEscCmd.Factory escCmdFactory;
   private int paperCutParam = 0;

   public Legacy4610EscCmdProcessor(POSPrinterCmd.Factory factory, PrinterHandleState handleState, PrinterParserState parserState) throws IllegalArgumentException {
      super(factory, handleState, parserState);
      this.setEscCmdFactory(new DefaultPrinterEscCmd.Factory());
   }

   protected void setEscCmdFactory(PrinterEscCmd.Factory escCmdFactory) {
      this.escCmdFactory = escCmdFactory;
   }

   protected PrinterEscCmd.Factory getEscCmdFactory() {
      return this.escCmdFactory;
   }

   public HashMap getEscCmdHashMap() {
      if (this.escCmdHashMap == null) {
         this.escCmdHashMap = new HashMap();
         this.escCmdHashMap
            .put(new DefaultPrinterEscCmdProcessor.CmdKey("hC"), this.getEscCmdFactory().createScaleHorizontallyEscCmd(19, "Scale horizontally", (byte)1));
         this.escCmdHashMap
            .put(new DefaultPrinterEscCmdProcessor.CmdKey("vC"), this.getEscCmdFactory().createScaleVerticallyEscCmd(20, "Scale vertically", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("P"), this.getEscCmdFactory().createPaperCutEscCmd(1, "Paper cut", (byte)3));
         this.escCmdHashMap
            .put(new DefaultPrinterEscCmdProcessor.CmdKey("fP"), this.getEscCmdFactory().createFeedPaperCutEscCmd(2, "Feed and paper cut", (byte)3));
         this.escCmdHashMap
            .put(new DefaultPrinterEscCmdProcessor.CmdKey("sP"), this.getEscCmdFactory().createFeedPaperCutStampEscCmd(3, "Feed, paper cut and stamp", (byte)3));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("sL"), this.getEscCmdFactory().createFireStampEscCmd(4, "Fire stamp", (byte)3));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("B"), this.getEscCmdFactory().createPrintBitmapEscCmd(5, "Print bitmap", (byte)3));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("tL"), this.getEscCmdFactory().createPrintTopLogoEscCmd(6, "Print top logo", (byte)5));
         this.escCmdHashMap
            .put(new DefaultPrinterEscCmdProcessor.CmdKey("bL"), this.getEscCmdFactory().createPrintBottomLogoEscCmd(7, "Print bottom logo", (byte)5));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("lF"), this.getEscCmdFactory().createFeedLinesEscCmd(8, "Feed lines", (byte)3));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("uF"), this.getEscCmdFactory().createFeedUnitsEscCmd(9, "Feed units", (byte)3));
         this.escCmdHashMap
            .put(new DefaultPrinterEscCmdProcessor.CmdKey("fT"), this.getEscCmdFactory().createFontTypefaceEscCmd(11, "Font type face", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("bC"), this.getEscCmdFactory().createBoldEscCmd(12, "Bold", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("uC"), this.getEscCmdFactory().createUnderlineEscCmd(13, "Underline", (byte)1));
         this.escCmdHashMap
            .put(new DefaultPrinterEscCmdProcessor.CmdKey("rvC"), this.getEscCmdFactory().createReverseVideoEscCmd(16, "Reverse video", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("sC"), this.getEscCmdFactory().createShadingEscCmd(17, "Shading", (byte)1));
         this.escCmdHashMap
            .put(new DefaultPrinterEscCmdProcessor.CmdKey("C"), this.getEscCmdFactory().createHighWideControlEscCmd(18, "High/wide control", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("cA"), this.getEscCmdFactory().createCenterEscCmd(24, "Center justify", (byte)4));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("rA"), this.getEscCmdFactory().createRightJustifyEscCmd(25, "Right justify", (byte)4));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("N"), this.getEscCmdFactory().createNormalEscCmd(26, "Normal", (byte)1));
         this.escCmdHashMap
            .put(new DefaultPrinterEscCmdProcessor.CmdKey("rC"), this.getEscCmdFactory().createAlternateColorEscCmd(15, "Alternate color", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("fC"), this.getEscCmdFactory().createRGBColorEscCmd(21, "RGB color", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("tbC"), this.getEscCmdFactory().createSubScriptEscCmd(22, "Subscript", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("tpC"), this.getEscCmdFactory().createSuperScriptEscCmd(23, "Superscript", (byte)1));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("rF"), this.getEscCmdFactory().createFeedReverseEscCmd(10, "Fedd reverse", (byte)3));
         this.escCmdHashMap.put(new DefaultPrinterEscCmdProcessor.CmdKey("iC"), this.getEscCmdFactory().createItalicEscCmd(14, "Italic", (byte)1));
      }

      return this.escCmdHashMap;
   }

   protected int checkPaperCut(PrinterEscCmd cmd) throws IllegalArgumentException {
      super.checkPaperCut(cmd);
      int paperCutParam = this.getPaperCutParam();
      if (paperCutParam != 0 && paperCutParam != 100) {
         throw new IllegalArgumentException("The PaperCut parameter is invalid :" + paperCutParam);
      } else {
         return paperCutParam;
      }
   }

   protected int getCmdParameter(PrinterEscCmd cmd) {
      int param = cmd.getParameter();
      int closestParam = 0;
      if (0 < param && param <= 100) {
         closestParam = 100;
      }

      this.setPaperCutParam(param);
      return closestParam;
   }

   protected int getPaperCutParam() {
      return this.paperCutParam;
   }

   protected void setPaperCutParam(int i) {
      this.paperCutParam = i;
   }
}
