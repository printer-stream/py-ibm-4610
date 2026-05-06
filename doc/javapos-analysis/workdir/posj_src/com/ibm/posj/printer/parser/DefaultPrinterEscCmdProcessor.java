package com.ibm.posj.printer.parser;

import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.ByteEncoder;
import com.ibm.jutil.FileUtil;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.IBM4689PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterCmdVisitor;
import com.ibm.posj.printer.ImageStreamProducer;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.PrinterUtility;
import com.ibm.posj.printer.ibm4689.Image4689StreamProducer;
import com.ibm.posj.printer.sureone.SureoneImageStreamProducer;
import com.ibm.posj.util.DefaultPOSPrinterCmdV;
import java.awt.Frame;
import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import jpos.JposException;

public abstract class DefaultPrinterEscCmdProcessor implements PrinterEscCmdProcessor {
   protected PrinterHandleState handleState = null;
   protected POSPrinterCmd.Factory factory = null;
   protected PrinterParserState parserState = null;
   protected PrinterEscCmdVisitor checkCapCmdVisitor = null;
   protected PrinterEscCmdVisitor checkParCmdVisitor = null;
   protected PrinterEscCmdVisitor crtPOSPrtCmdVisitor = null;
   protected PrinterEscCmdVisitor updateParserStateVisitor = null;
   protected POSPrinterCmdVisitor clonPOSPrinterCmdVisitor = null;
   protected HashMap escCmdHashMap = null;
   private ByteEncoder byteEncoder = new ByteEncoder();

   public DefaultPrinterEscCmdProcessor(POSPrinterCmd.Factory factory, PrinterHandleState handleState, PrinterParserState parserState) throws IllegalArgumentException {
      if (handleState == null) {
         throw new IllegalArgumentException("Invalid handle state object passed to the constructor method");
      } else if (factory == null) {
         throw new IllegalArgumentException("Invalid POSPrinterCmd factory passed to the constructor method");
      } else if (parserState == null) {
         throw new IllegalArgumentException("Invalid PrinterParserState object passed to the constructor method");
      } else {
         this.factory = factory;
         this.handleState = handleState;
         this.parserState = parserState;
         this.getByteEncoder().setCharacterSet(998);
      }
   }

   public PrinterEscCmdVisitor getCheckCapCmdVisitor() {
      if (this.checkCapCmdVisitor == null) {
         this.checkCapCmdVisitor = new DefaultPrinterEscCmdProcessor.ParserCheckCmdCapsV(this.handleState, this.parserState);
      }

      return this.checkCapCmdVisitor;
   }

   public PrinterEscCmdVisitor getCheckParamCmdVisitor() {
      try {
         if (this.checkParCmdVisitor == null) {
            this.checkParCmdVisitor = new DefaultPrinterEscCmdProcessor.ParserCheckCmdParamV(this.handleState, this.parserState);
         }
      } catch (Exception var2) {
      }

      return this.checkParCmdVisitor;
   }

   public PrinterEscCmdVisitor getCrtPOSPrtCmdVisitor() {
      if (this.crtPOSPrtCmdVisitor == null) {
         this.crtPOSPrtCmdVisitor = new DefaultPrinterEscCmdProcessor.ParserCreatePOSPrinterCmdV(this.factory, this.parserState, this.handleState);
      }

      return this.crtPOSPrtCmdVisitor;
   }

   public PrinterEscCmdVisitor getUpdateParserStateVisitor() {
      if (this.updateParserStateVisitor == null) {
         this.updateParserStateVisitor = new DefaultPrinterEscCmdProcessor.UpdateParserStateV(this.parserState);
      }

      return this.updateParserStateVisitor;
   }

   public POSPrinterCmdVisitor getClonPOSPrinterCmdVisitor() {
      if (this.clonPOSPrinterCmdVisitor == null) {
         this.clonPOSPrinterCmdVisitor = new DefaultPrinterEscCmdProcessor.ClonPOSPrinterCmdV(this.factory);
      }

      return this.clonPOSPrinterCmdVisitor;
   }

   public POSPrinterCmd.Factory getFactory() {
      return this.factory;
   }

   public PrinterParserState getParserState() {
      return this.parserState;
   }

   public PrinterHandleState getHandleState() {
      return this.handleState;
   }

   public ByteEncoder getByteEncoder() {
      return this.byteEncoder;
   }

   public abstract HashMap getEscCmdHashMap();

   protected void doParPaperCut(PrinterEscCmd cmd) throws IllegalArgumentException {
      int paperCutParam = this.checkPaperCut(cmd);
   }

   protected void checkParScaleHorizontally(PrinterEscCmd cmd) throws IllegalArgumentException {
      byte station = cmd.getStation();
      if (cmd.hasParameter() && cmd.getParameter() >= 1) {
         if (cmd.getParameter() > this.handleState.getMaxScaleHorizontalNumber(station) + 1) {
            cmd.setClosestParameter(this.handleState.getMaxScaleHorizontalNumber(station) + 1);
            throw new IllegalArgumentException("Parameter not precisely supported");
         } else {
            cmd.setClosestParameter(cmd.getParameter());
         }
      } else {
         cmd.setClosestParameter(1);
         throw new IllegalArgumentException("Parameter not precisely supported");
      }
   }

   protected int checkPaperCut(PrinterEscCmd cmd) throws IllegalArgumentException {
      int paperCutParam = 0;
      boolean paramNotSupported = false;
      if (!cmd.hasParameter()) {
         paperCutParam = 100;
      } else if (cmd.getParameter() < 0) {
         paperCutParam = 0;
         paramNotSupported = true;
      } else if (cmd.getParameter() > 100) {
         paperCutParam = 100;
         paramNotSupported = true;
      } else {
         paperCutParam = this.getCmdParameter(cmd);
      }

      if (this.parserState.getRotation(cmd.getStation()) == 270 || this.parserState.getRotation(cmd.getStation()) == 90) {
         paperCutParam = 0;
      }

      cmd.setClosestParameter(paperCutParam);
      if (paramNotSupported) {
         throw new IllegalArgumentException("Parameter not precisely supported");
      } else {
         return paperCutParam;
      }
   }

   protected int getCmdParameter(PrinterEscCmd cmd) {
      return cmd.getParameter();
   }

   static class ClonPOSPrinterCmdV extends DefaultPOSPrinterCmdV implements POSPrinterCmdVisitor {
      POSPrinterCmd posPrinterCmd = null;
      POSPrinterCmd.Factory factory = null;

      ClonPOSPrinterCmdV(POSPrinterCmd.Factory factory) {
         this.factory = factory;
      }

      public Object getData() {
         return this.posPrinterCmd;
      }

      public void reset() {
         this.posPrinterCmd = null;
      }

      public void visitBoldCmd(POSPrinterCmd.BoldCmd boldCmd) {
         this.posPrinterCmd = this.factory.createBoldCmd(boldCmd.isActive());
      }

      public void visitFontColorCmd(POSPrinterCmd.FontColorCmd fontColorCmd) {
         this.posPrinterCmd = this.factory.createFontColorCmd(fontColorCmd.getFontColor());
      }

      public void visitReverseVideoCmd(POSPrinterCmd.ReverseVideoCmd revVideoCmd) {
         this.posPrinterCmd = this.factory.createReverseVideoCmd(true);
      }

      public void visitAlterWideHighCmd(POSPrinterCmd.AlterWideHighCmd alterWHCmd) {
         this.posPrinterCmd = this.factory.createAlterWideHighCmd(alterWHCmd.getOption());
      }

      public void visitScaleFontCmd(POSPrinterCmd.ScaleFontCmd scaleFontCmd) {
         this.posPrinterCmd = this.factory.createScaleFontCmd(scaleFontCmd.getStation(), scaleFontCmd.getWidth(), scaleFontCmd.getHeight());
      }

      public void visitNormalModeCmd(POSPrinterCmd.NormalModeCmd normalModeCmd) {
         this.posPrinterCmd = this.factory.createNormalModeCmd(true);
      }

      public void visitUnderlineCmd(POSPrinterCmd.UnderlineCmd underlineCmd) {
         this.posPrinterCmd = this.factory.createUnderlineCmd(underlineCmd.getThickness());
      }

      public void visitFontTypeCmd(POSPrinterCmd.FontTypeCmd cmd) {
         this.posPrinterCmd = this.factory.createFeedLinesCmd((short)0, cmd.getStation());
      }
   }

   public static class CmdKey {
      String escCommand = "";

      public CmdKey(String escCommand) {
         this.escCommand = escCommand;
      }

      public int hashCode() {
         int hashCode = 0;
         byte[] commandBytes = this.escCommand.getBytes();

         for (int i = 0; i < commandBytes.length; i++) {
            hashCode <<= 8;
            hashCode += commandBytes[i];
         }

         return hashCode;
      }
   }

   static class ParserCheckCmdCapsV extends DefaultPrinterEscCmdV {
      PrinterHandleState handleState = null;
      PrinterParserState pState = null;
      private static final String PRT_ERROR_CAP_NOT_SUPPORTED = "Command capability not supported";

      ParserCheckCmdCapsV(PrinterHandleState handleState, PrinterParserState pState) {
         this.handleState = handleState;
         this.pState = pState;
      }

      public POSPrinterCmd getPOSPrinterCmd() {
         return null;
      }

      public void reset() {
      }

      public void visitPaperCutEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapPaperCut(cmd);
      }

      public void visitFeedPaperCutEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapPaperCut(cmd);
      }

      public void visitFeedPaperCutStampEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapFeedPaperCutStamp(cmd);
      }

      public void visitFireStampEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         if (this.pState.getPrinterStateType() == 2) {
            this.checkCapFireStamp(cmd);
         } else {
            this.checkCapPrintBitmap(cmd);
         }
      }

      public void visitPrintBitmapEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapPrintBitmap(cmd);
      }

      public void visitPrintTopLogoEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapPrintTopLogo(cmd);
      }

      public void visitPrintBottomLogoEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapPrintBottomLogo(cmd);
      }

      public void visitFeedLinesEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      public void visitFeedUnitsEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      public void visitFeedReverseEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapFeedReverse(cmd);
      }

      public void visitFontTypefaceEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      public void visitBoldEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapBold(cmd);
      }

      public void visitUnderlineEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapUnderline(cmd);
      }

      public void visitItalicEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapItalic(cmd);
      }

      public void visitAlternateColorEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapAlternateColor(cmd);
      }

      public void visitReverseVideoEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapReverseVideo(cmd);
      }

      public void visitShadingEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapShading(cmd);
      }

      public void visitHighWideControlEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapHighWideControlEscCmd(cmd);
      }

      public void visitScaleHorizontallyEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapScaleHorizontally(cmd);
      }

      public void visitScaleVerticallyEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapScaleVertically(cmd);
      }

      public void visitRGBColorEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapRGBColorEscCmd(cmd);
      }

      public void visitSubScriptEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapSubScript(cmd);
      }

      public void visitSuperScriptEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapSubScript(cmd);
      }

      public void visitCenterEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      public void visitRightJustifyEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      public void visitNormalEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      public void visitPassThruEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      public void checkCapPaperCut(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapPapercut(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapFireStamp(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapStamp(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapFeedPaperCutStamp(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapPaperCut(cmd);
         this.checkCapFireStamp(cmd);
      }

      public void checkCapPrintBitmap(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapBitmap(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapPrintTopLogo(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      public void checkCapPrintBottomLogo(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      public void checkCapFeedReverse(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapFeedReverse(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapBold(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapBold(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapUnderline(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapUnderline(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapItalic(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapItalic(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapAlternateColor(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.pState.getCap2Color(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapReverseVideo(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapReverseVideo(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapShading(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapShading(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapDHigh(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapDhigh(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapDWide(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapDwide(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapHighWideControlEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkCapDHigh(cmd);
         this.checkCapDWide(cmd);
      }

      public void checkCapScaleHorizontally(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapScaleHorizontally(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapScaleVertically(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapScaleVertically(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapSubScript(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapSubScript(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapSuperScript(PrinterEscCmd cmd) throws IllegalAccessException {
         if (!this.handleState.getCapSuperScript(cmd.getStation())) {
            throw new IllegalAccessException("Command capability not supported");
         }
      }

      public void checkCapRGBColorEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         throw new IllegalAccessException("Command capability not supported");
      }
   }

   class ParserCheckCmdParamV extends DefaultPrinterEscCmdV {
      PrinterHandleState handleState = null;
      PrinterParserState parserState = null;
      private static final String PRT_ERROR_PAR_NOT_SUPPORTED = "Parameter not precisely supported";

      ParserCheckCmdParamV(PrinterHandleState handleState, PrinterParserState parserState) {
         this.handleState = handleState;
         this.parserState = parserState;
      }

      public POSPrinterCmd getPOSPrinterCmd() {
         return null;
      }

      public void reset() {
      }

      public void visitPaperCutEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException {
         DefaultPrinterEscCmdProcessor.this.doParPaperCut(cmd);
      }

      public void visitFeedPaperCutEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException {
         DefaultPrinterEscCmdProcessor.this.doParPaperCut(cmd);
      }

      public void visitFireStampEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkParStamp(cmd);
      }

      public void visitFeedPaperCutStampEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException, IllegalAccessException {
         DefaultPrinterEscCmdProcessor.this.doParPaperCut(cmd);
         this.checkParStamp(cmd);
      }

      public void visitPrintBitmapEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkParBitmap(cmd);
      }

      public void visitPrintTopLogoEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException {
      }

      public void visitPrintBottomLogoEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException {
      }

      public void visitFeedLinesEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException {
         this.checkParFeedLines(cmd);
      }

      public void visitFeedUnitsEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException {
         this.checkParFeedLinesUnits(cmd);
      }

      public void visitFeedReverseEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException {
         this.checkParFeedLines(cmd);
      }

      public void visitFontTypefaceEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         this.checkParFontTypeFace(cmd);
      }

      public void visitUnderlineEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException {
         this.checkParUnderline(cmd);
      }

      public void visitAlternateColorEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException {
         this.checkParAlternateColor(cmd);
      }

      public void visitShadingEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException {
      }

      public void visitHighWideControlEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException {
      }

      public void visitScaleHorizontallyEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException {
         DefaultPrinterEscCmdProcessor.this.checkParScaleHorizontally(cmd);
      }

      public void visitScaleVerticallyEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException {
         this.checkParScaleVertically(cmd);
      }

      public void visitRGBColorEscCmd(PrinterEscCmd cmd) throws IllegalArgumentException {
         this.checkParRGBColor(cmd);
      }

      public void checkParBitmap(PrinterEscCmd cmd) throws IllegalAccessException {
         byte station = cmd.getStation();
         if (cmd.getParameter() < 1) {
            cmd.setClosestParameter(1);
            throw new IllegalAccessException("Parameter not precisely supported");
         } else if (cmd.getParameter() > 20) {
            cmd.setClosestParameter(20);
            throw new IllegalAccessException("Parameter not precisely supported");
         } else {
            cmd.setClosestParameter(cmd.getParameter());
            if (this.handleState.getSetBitmapCmd((byte)cmd.getParameter(), cmd.getStation()) == null) {
               throw new IllegalArgumentException("Parameter not precisely supported");
            } else {
               byte bitmapStation = this.handleState.getSetBitmapCmd((byte)cmd.getParameter(), cmd.getStation()).getStation();
               if ((bitmapStation == 2 || bitmapStation == 0) && station == 4) {
                  throw new IllegalArgumentException("Parameter not precisely supported");
               } else if (bitmapStation == 4 && station == 2) {
                  throw new IllegalArgumentException("Parameter not precisely supported");
               }
            }
         }
      }

      public void checkParStamp(PrinterEscCmd cmd) throws IllegalAccessException {
         if (this.handleState.getSetBitmapCmd((byte)1, cmd.getStation()) == null) {
            cmd.setSecParameter(0);
         } else {
            cmd.setSecParameter(1);
         }
      }

      public void checkParFeedLinesUnits(PrinterEscCmd cmd) throws IllegalArgumentException {
         byte station = cmd.getStation();
         int tmpClosestValue = cmd.getParameter();
         if (!cmd.hasParameter()) {
            tmpClosestValue = 1;
         } else if (tmpClosestValue < 0) {
            int var5 = false;
            throw new IllegalArgumentException("Parameter not precisely supported");
         }

         int feedUnits = (int)((float)tmpClosestValue / this.parserState.getConversionFactor(station));
         if (feedUnits > this.handleState.getMaxLinesToFeed(station)) {
            cmd.setClosestParameter(this.handleState.getMaxLinesToFeed(station));
            throw new IllegalArgumentException("Parameter not precisely supported");
         } else {
            cmd.setClosestParameter(feedUnits);
         }
      }

      public void checkParFeedLines(PrinterEscCmd cmd) throws IllegalArgumentException {
         byte station = cmd.getStation();
         if (!cmd.hasParameter()) {
            cmd.setClosestParameter(1);
         } else {
            if (cmd.getParameter() < 0) {
               cmd.setClosestParameter(0);
               throw new IllegalArgumentException("Parameter not precisely supported");
            }

            if (cmd.getParameter() > this.handleState.getMaxLinesToFeed(station)) {
               cmd.setClosestParameter(this.handleState.getMaxLinesToFeed(station));
               throw new IllegalArgumentException("Parameter not precisely supported");
            }

            cmd.setClosestParameter(cmd.getParameter());
         }
      }

      public void checkParFontTypeFace(PrinterEscCmd cmd) throws IllegalAccessException {
         if (cmd.getParameter() != 0 || !cmd.hasParameter()) {
            throw new IllegalAccessException("Parameter not precisely supported");
         }
      }

      public void checkParUnderline(PrinterEscCmd cmd) throws IllegalArgumentException {
         byte station = cmd.getStation();
         if (!cmd.hasParameter()) {
            cmd.setClosestParameter(1);
         } else {
            if (cmd.getParameter() < 0) {
               cmd.setClosestParameter(0);
               throw new IllegalArgumentException("Parameter not precisely supported");
            }

            if (cmd.getParameter() > this.handleState.getMaxUnderlineNumber(station)) {
               cmd.setClosestParameter(this.handleState.getMaxUnderlineNumber(station));
               throw new IllegalArgumentException("Parameter not precisely supported");
            }

            cmd.setClosestParameter(cmd.getParameter());
         }
      }

      public void checkParAlternateColor(PrinterEscCmd cmd) throws IllegalArgumentException {
         byte station = cmd.getStation();
         boolean capColor = this.parserState.getCap2Color(station);
         if (!capColor) {
            if (cmd.getParameter() != 1) {
               throw new IllegalArgumentException("Parameter not precisely supported");
            }

            cmd.setClosestParameter(0);
         } else if (!cmd.hasParameter()) {
            cmd.setClosestParameter(1);
         } else {
            if (cmd.hasParameter() && cmd.getParameter() != 1 && cmd.getParameter() != 2) {
               throw new IllegalArgumentException("Parameter not precisely supported");
            }

            cmd.setClosestParameter(cmd.getParameter() - 1);
         }
      }

      public void checkParScaleHorizontally(PrinterEscCmd cmd) throws IllegalArgumentException {
         byte station = cmd.getStation();
         if (cmd.hasParameter() && cmd.getParameter() >= 1) {
            if (cmd.getParameter() > this.handleState.getMaxScaleHorizontalNumber(station) + 1) {
               cmd.setClosestParameter(this.handleState.getMaxScaleHorizontalNumber(station) + 1);
               throw new IllegalArgumentException("Parameter not precisely supported");
            } else {
               cmd.setClosestParameter(cmd.getParameter());
            }
         } else {
            cmd.setClosestParameter(1);
            throw new IllegalArgumentException("Parameter not precisely supported");
         }
      }

      public void checkParScaleVertically(PrinterEscCmd cmd) throws IllegalArgumentException {
         byte station = cmd.getStation();
         if (cmd.hasParameter() && cmd.getParameter() >= 1) {
            if (cmd.getParameter() > this.handleState.getMaxScaleVerticalNumber(station) + 1) {
               cmd.setClosestParameter(this.handleState.getMaxScaleVerticalNumber(station) + 1);
               throw new IllegalArgumentException("Parameter not precisely supported");
            } else {
               cmd.setClosestParameter(cmd.getParameter());
            }
         } else {
            cmd.setClosestParameter(1);
            throw new IllegalArgumentException("Parameter not precisely supported");
         }
      }

      public void checkParRGBColor(PrinterEscCmd cmd) throws IllegalArgumentException {
         byte station = cmd.getStation();
         if (!cmd.hasParameter()) {
            cmd.setClosestParameter(0);
         }

         if (cmd.getParameter() < 0) {
            cmd.setClosestParameter(0);
            throw new IllegalArgumentException("Parameter not precisely supported");
         } else if (cmd.getParameter() > this.handleState.getMaxRGBColorNumber(station)) {
            cmd.setClosestParameter(this.handleState.getMaxRGBColorNumber(station));
            throw new IllegalArgumentException("Parameter not precisely supported");
         } else {
            cmd.setClosestParameter(cmd.getParameter());
         }
      }
   }

   public static class ParserCreatePOSPrinterCmdV extends DefaultPrinterEscCmdV {
      POSPrinterCmd.Factory factory = null;
      PrinterParserState parserState = null;
      PrinterHandleState handleState = null;
      PrinterUtility printerUtility = new PrinterUtility();

      public ParserCreatePOSPrinterCmdV(POSPrinterCmd.Factory factory, PrinterParserState parserState, PrinterHandleState handleState) {
         this.factory = factory;
         this.parserState = parserState;
         this.handleState = handleState;
      }

      public POSPrinterCmd getPOSPrinterCmd() {
         return null;
      }

      public void reset() {
      }

      public void visitPaperCutEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         if (cmd.getClosestParameter() > 0) {
            cmd.setPOSPrinterCmd(this.factory.createCutPaperCmd((short)((byte)cmd.getClosestParameter())));
         } else {
            cmd.setPOSPrinterCmd(this.addMultiFeedLines(1, cmd.getStation()));
         }
      }

      public void visitFeedPaperCutEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         POSPrinterCmd masterCmd = null;
         masterCmd = this.addMultiFeedLines((short)this.parserState.getRecLinesToPaperCut(), cmd.getStation());
         if (cmd.getClosestParameter() > 0) {
            masterCmd.appendPOSPrinterCmd(this.factory.createCutPaperCmd((short)((byte)cmd.getClosestParameter())));
         } else {
            masterCmd.appendPOSPrinterCmd(this.addMultiFeedLines(0, cmd.getStation()));
         }

         cmd.setPOSPrinterCmd(masterCmd);
      }

      public void visitFeedPaperCutStampEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         POSPrinterCmd masterCmd = null;
         masterCmd = this.addMultiFeedLines((short)this.parserState.getRecLinesToPaperCut(), cmd.getStation());
         if (this.parserState.getPrinterStateType() != 2) {
            if (cmd.getClosestParameter() > 0) {
               masterCmd.appendPOSPrinterCmd(this.factory.createCutPaperCmd((short)((byte)cmd.getClosestParameter())));
            } else {
               masterCmd.appendPOSPrinterCmd(this.addMultiFeedLines(1, cmd.getStation()));
            }
         }

         if (this.parserState.getPrinterStateType() == 1) {
            masterCmd.appendPOSPrinterCmd(this.print4610Bitmap(cmd, (byte)1));
         } else if (this.parserState.getPrinterStateType() == 3) {
            masterCmd.appendPOSPrinterCmd(this.printSureoneBitmap(cmd, (byte)1));
         } else if (this.parserState.getPrinterStateType() == 2 && cmd.getClosestParameter() > 0) {
            masterCmd.appendPOSPrinterCmd(this.getFactory().createFeedReverseCmd((short)55));
            masterCmd.appendPOSPrinterCmd(((IBM4689PrinterCmd.Factory)this.factory).createPrintFullCutCmd());
         }

         cmd.setPOSPrinterCmd(masterCmd);
      }

      public void visitFireStampEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         if (this.parserState.getPrinterStateType() == 1) {
            cmd.setPOSPrinterCmd(this.print4610Bitmap(cmd, (byte)1));
         } else if (this.parserState.getPrinterStateType() == 3) {
            cmd.setPOSPrinterCmd(this.printSureoneBitmap(cmd, (byte)1));
         } else if (this.parserState.getPrinterStateType() == 2) {
            if (cmd.getStation() == 8) {
               cmd.setPOSPrinterCmd(this.addMultiFeedLines(0, cmd.getStation()));
            } else {
               cmd.setPOSPrinterCmd(((IBM4689PrinterCmd.Factory)this.factory).createPrintFullCutCmd());
            }
         }
      }

      public void visitPrintBitmapEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         if (this.parserState.getPrinterStateType() == 1) {
            cmd.setPOSPrinterCmd(this.print4610Bitmap(cmd, (byte)cmd.getClosestParameter()));
         } else if (this.parserState.getPrinterStateType() == 3) {
            cmd.setPOSPrinterCmd(this.printSureoneBitmap(cmd, (byte)cmd.getClosestParameter()));
         } else if (this.parserState.getPrinterStateType() == 2) {
            cmd.setPOSPrinterCmd(this.print4689Bitmap(cmd, (byte)cmd.getClosestParameter()));
         }
      }

      public void visitPrintTopLogoEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         if (this.handleState.getSetLogoCmd((byte)1) != null) {
            cmd.setPOSPrinterCmd(this.factory.createPrintSetLogoCmd(cmd.getStation(), (byte)1));
         } else {
            cmd.setPOSPrinterCmd(this.addMultiFeedLines(1, cmd.getStation()));
         }
      }

      public void visitPrintBottomLogoEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         if (this.handleState.getSetLogoCmd((byte)2) != null) {
            cmd.setPOSPrinterCmd(this.factory.createPrintSetLogoCmd(cmd.getStation(), (byte)2));
         } else {
            cmd.setPOSPrinterCmd(this.addMultiFeedLines(1, cmd.getStation()));
         }
      }

      public void visitFeedLinesEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         cmd.setPOSPrinterCmd(this.addMultiFeedLines((byte)cmd.getClosestParameter(), cmd.getStation()));
      }

      public void visitFeedUnitsEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         POSPrinterCmd masterCmd = null;
         if (this.parserState.getPrinterStateType() == 2) {
            masterCmd = this.factory.createSelectStationCmd(cmd.getStation());
            masterCmd.appendPOSPrinterCmd(this.factory.createFeedUnitsCmd((short)((byte)cmd.getClosestParameter())));
            cmd.setPOSPrinterCmd(masterCmd);
         } else if (this.parserState.getPrinterStateType() == 3) {
            byte parameter = (byte)Math.round((float)(cmd.getClosestParameter() / 2));
            cmd.setPOSPrinterCmd(this.factory.createFeedUnitsCmd((short)parameter));
         } else if (cmd.getStation() == 4) {
            byte parameter = (byte)((int)Math.round((double)cmd.getClosestParameter() / 2.2));
            cmd.setPOSPrinterCmd(this.factory.createFeedUnitsCmd((short)parameter));
         } else {
            cmd.setPOSPrinterCmd(this.factory.createFeedUnitsCmd((short)((byte)cmd.getClosestParameter())));
         }
      }

      public void visitFeedReverseEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         POSPrinterCmd masterCmd = null;
         cmd.setPOSPrinterCmd(this.factory.createFeedReverseCmd((short)((byte)(cmd.getClosestParameter() + 1))));
      }

      public void visitFontTypefaceEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         cmd.setPOSPrinterCmd(this.addMultiFeedLines(0, cmd.getStation()));
      }

      public void visitBoldEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         cmd.setPOSPrinterCmd(this.factory.createBoldCmd(true));
      }

      public void visitUnderlineEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         cmd.setPOSPrinterCmd(this.factory.createUnderlineCmd((short)((byte)cmd.getClosestParameter())));
      }

      public void visitItalicEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      public void visitAlternateColorEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         cmd.setPOSPrinterCmd(this.factory.createFontColorCmd((byte)cmd.getClosestParameter()));
      }

      public void visitReverseVideoEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         cmd.setPOSPrinterCmd(this.factory.createReverseVideoCmd(true));
      }

      public void visitShadingEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      public void visitHighWideControlEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         cmd.setPOSPrinterCmd(this.factory.createAlterWideHighCmd((short)((byte)cmd.getParameter())));
      }

      public void visitScaleHorizontallyEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         if (cmd.getStation() == 2) {
            cmd.setPOSPrinterCmd(
               this.factory
                  .createScaleFontCmd(
                     cmd.getStation(), (byte)(cmd.getClosestParameter() - 1), (byte)(this.parserState.getFontHighMagnification(cmd.getStation()) - 1)
                  )
            );
         } else {
            POSPrinterCmd masterCmd = null;
            if (cmd.getClosestParameter() == 1) {
               masterCmd = this.factory.createAlterWideHighCmd((short)1);
               if (this.parserState.getFontHighMagnification(cmd.getStation()) == 2) {
                  masterCmd.appendPOSPrinterCmd(this.factory.createAlterWideHighCmd((short)3));
               }
            } else {
               masterCmd = this.factory.createAlterWideHighCmd((short)2);
            }

            cmd.setPOSPrinterCmd(masterCmd);
         }
      }

      public void visitScaleVerticallyEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         if (cmd.getStation() == 2) {
            cmd.setPOSPrinterCmd(
               this.factory
                  .createScaleFontCmd(
                     cmd.getStation(), (byte)(this.parserState.getFontWideMagnification(cmd.getStation()) - 1), (byte)(cmd.getClosestParameter() - 1)
                  )
            );
         } else {
            POSPrinterCmd masterCmd = null;
            if (cmd.getClosestParameter() == 1) {
               masterCmd = this.factory.createAlterWideHighCmd((short)1);
               if (this.parserState.getFontWideMagnification(cmd.getStation()) == 2) {
                  masterCmd.appendPOSPrinterCmd(this.factory.createAlterWideHighCmd((short)2));
               }
            } else {
               masterCmd = this.factory.createAlterWideHighCmd((short)3);
            }

            cmd.setPOSPrinterCmd(masterCmd);
         }
      }

      public void visitRGBColorEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      public void visitSubScriptEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      public void visitSuperScriptEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      public void visitCenterEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         POSPrinterCmd pCmd = this.factory.createAlignPositionCmd(cmd.getStation(), (byte)1);
         cmd.setPOSPrinterCmd(pCmd);
      }

      public void visitRightJustifyEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         cmd.setPOSPrinterCmd(this.factory.createAlignPositionCmd(cmd.getStation(), (byte)2));
      }

      public void visitNormalEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
         cmd.setPOSPrinterCmd(this.factory.createNormalModeCmd(true));
      }

      public void visitPassThruEscCmd(PrinterEscCmd cmd) throws IllegalAccessException {
      }

      private POSPrinterCmd print4610Bitmap(PrinterEscCmd cmd, byte bitmapNumber) {
         POSPrinterCmd imageMaster = null;
         POSPrinterCmd.SetBitmapCmd setBitmapCmd = this.handleState.getSetBitmapCmd(bitmapNumber, cmd.getStation());
         if (cmd.getStation() == 4) {
            if (setBitmapCmd == null || setBitmapCmd.getStation() != cmd.getStation()) {
               return this.addMultiFeedLines(0, cmd.getStation());
            }

            int maxBitmapSize1 = this.handleState.getMaxPrintBitmapSize(cmd.getStation());
            int heightBlockInBytes1 = this.printerUtility.getBlockBytesHeight(setBitmapCmd.getWidth(), maxBitmapSize1);
            POSPrinterCmd pcc = null;
            int maxHeight = this.handleState.getBitmapMaxHeight(cmd.getStation());
            if (setBitmapCmd.getHeight() >= this.printerUtility.getBytesHeight(maxHeight)) {
               heightBlockInBytes1 = this.printerUtility.getBytesHeight(maxHeight);
            }

            int dataBlocks1 = setBitmapCmd.getHeight() / heightBlockInBytes1;
            if (setBitmapCmd.getHeight() % heightBlockInBytes1 != 0) {
               dataBlocks1++;
            }

            for (int i = 1; i <= dataBlocks1; i++) {
               int blockIndex = (i - 1) * heightBlockInBytes1 * setBitmapCmd.getWidth() * 8;
               int blockHeight = i * heightBlockInBytes1 > setBitmapCmd.getHeight()
                  ? setBitmapCmd.getHeight() - (i - 1) * heightBlockInBytes1
                  : heightBlockInBytes1;
               if (i == 1) {
                  pcc = this.factory
                     .createPrintBitmapCmd(
                        cmd.getStation(),
                        setBitmapCmd.getFileName(),
                        setBitmapCmd.getBitmapStream(),
                        blockIndex,
                        setBitmapCmd.getWidth(),
                        (byte)blockHeight,
                        (byte)0,
                        setBitmapCmd.getAlignment()
                     );
               } else {
                  pcc.appendPOSPrinterCmd(
                     this.factory
                        .createPrintBitmapCmd(
                           cmd.getStation(),
                           setBitmapCmd.getFileName(),
                           setBitmapCmd.getBitmapStream(),
                           blockIndex,
                           setBitmapCmd.getWidth(),
                           (byte)blockHeight,
                           (byte)0,
                           setBitmapCmd.getAlignment()
                        )
                  );
               }

               pcc.appendPOSPrinterCmd(this.factory.createFeedUnitsCmd((short)4));
               imageMaster = pcc;
            }

            imageMaster.appendPOSPrinterCmd(this.factory.createAlignPositionCmd(cmd.getStation(), this.parserState.getAlignment(cmd.getStation())));
            imageMaster.appendPOSPrinterCmd(((IBM4610PrinterCmd.Factory)this.factory).createECLevelRequestCmd(true));
         } else if (setBitmapCmd != null && (setBitmapCmd.getStation() == cmd.getStation() || setBitmapCmd.getStation() == 0)) {
            imageMaster = this.factory.createPrintNormalCmd(cmd.getStation(), "\n");
            ((POSPrinterCmd.PrintNormalCmd)imageMaster).completeLine();
            POSPrinterCmd psb = this.factory
               .createPrintSetBitmapCmd(cmd.getStation(), bitmapNumber, (byte)0, (byte)0, (byte)0, setBitmapCmd.getAlignment(), setBitmapCmd.getMargin());
            imageMaster.appendPOSPrinterCmd(psb);
            if (cmd.getStation() == 4) {
               imageMaster.appendPOSPrinterCmd(this.factory.createFeedUnitsCmd((short)6));
            }
         } else {
            imageMaster = this.addMultiFeedLines(1, cmd.getStation());
         }

         return imageMaster;
      }

      private POSPrinterCmd printSureoneBitmap(PrinterEscCmd cmd, byte bitmapNumber) {
         POSPrinterCmd imageMaster = null;
         POSPrinterCmd.SetBitmapCmd setBitmapCmd = this.handleState.getSetBitmapCmd((byte)(bitmapNumber - 1), cmd.getStation());
         if (setBitmapCmd != null) {
            imageMaster = this.factory.createPrintNormalCmd(cmd.getStation(), "\n");
            String fileName = setBitmapCmd.getFileName();
            int alignment = setBitmapCmd.getAlignment();
            int width = ((DefaultPOSPrinterCmd.SetBitmapCmd)setBitmapCmd).getIntegerWidth();
            File file = null;
            if (FileUtil.locateFile(fileName, true, true)) {
               try {
                  file = FileUtil.findFile(fileName, true);
               } catch (Exception var26) {
                  return this.addMultiFeedLines(1, cmd.getStation());
               }
            }

            if (file != null) {
               fileName = file.getAbsolutePath();
            }

            try {
               PrinterUtility pUtil = new PrinterUtility();
               ImageStreamProducer iProducer = new SureoneImageStreamProducer();
               Frame frame = new Frame();
               Image image = pUtil.loadImageFromDisk(fileName, true, frame);
               POSPrinterCmd ppc = null;
               if (width == -11 && image.getWidth(frame) > 560) {
                  throw new JposException(114, 206);
               }

               if (width > 560) {
                  throw new JposException(106, "Width value is too big");
               }

               if (width == -11) {
                  width = image.getWidth(frame);
               }

               width = Math.abs(width / 8);
               width *= 8;
               image = pUtil.scaleImage(image, frame, width, 560, 200, 4000);
               int iW = image.getWidth(frame);
               int iH = image.getHeight(frame);
               byte[] stream = new byte[iW * iH];
               stream = pUtil.getImageStream(image, frame, (byte)2, iProducer);
               int mod = 24 - iH % 24;
               int lines;
               if (mod == 24) {
                  lines = iH / 24;
               } else {
                  lines = (iH + mod) / 24;
               }

               byte[] streamplusW = new byte[stream.length + iW * 2];
               byte[][] stream2 = new byte[lines][streamplusW.length];
               if (mod > 0) {
                  for (int x = 0; x < stream.length; x++) {
                     streamplusW[x] = stream[x];
                  }
               }

               byte n1 = (byte)iW;
               byte n2 = (byte)(iW >>> 8);

               for (int x = 0; x < lines; x++) {
                  for (int y = 0; y < iW; y++) {
                     stream2[x][y * 3] = streamplusW[y + x * iW * 3];
                     stream2[x][y * 3 + 1] = streamplusW[y + iW + x * iW * 3];
                     stream2[x][y * 3 + 2] = streamplusW[y + iW * 2 + x * iW * 3];
                  }

                  byte[] finalstream = new byte[iW * 3];
                  System.arraycopy(stream2[x], 0, finalstream, 0, iW * 3);
                  if (x == 0) {
                     ppc = this.factory.createPrintBitmapCmd((byte)2, fileName, finalstream, (byte)iW, n1, n2, (byte)alignment);
                  } else {
                     ppc.appendPOSPrinterCmd(this.factory.createPrintBitmapCmd((byte)2, fileName, finalstream, (byte)iW, n1, n2, (byte)alignment));
                  }
               }

               imageMaster.appendPOSPrinterCmd(ppc);
            } catch (Exception var27) {
               if (Tracer.getInstance().isOn()) {
                  Tracer.getInstance().print(var27);
               }

               return this.addMultiFeedLines(1, cmd.getStation());
            }
         } else {
            imageMaster = this.addMultiFeedLines(1, cmd.getStation());
         }

         return imageMaster;
      }

      private POSPrinterCmd print4689Bitmap(PrinterEscCmd cmd, byte bitmapNumber) {
         POSPrinterCmd imageMaster = null;
         POSPrinterCmd.SetBitmapCmd setBitmapCmd = this.handleState.getSetBitmapCmd((byte)(bitmapNumber - 1), cmd.getStation());
         if (setBitmapCmd != null) {
            imageMaster = this.factory.createPrintNormalCmd(cmd.getStation(), "\n");
            String fileName = setBitmapCmd.getFileName();
            int width = ((DefaultPOSPrinterCmd.SetBitmapCmd)setBitmapCmd).getIntegerWidth();
            File file = null;
            if (FileUtil.locateFile(fileName, true, true)) {
               try {
                  file = FileUtil.findFile(fileName, true);
               } catch (Exception var23) {
                  return this.addMultiFeedLines(1, cmd.getStation());
               }
            }

            if (file != null) {
               fileName = file.getAbsolutePath();
            }

            try {
               PrinterUtility pUtil = new PrinterUtility();
               ImageStreamProducer iProducer = new Image4689StreamProducer();
               Frame frame = new Frame();
               Image image = pUtil.loadImageFromDisk(fileName, true, frame);
               byte[] bitmapstream = pUtil.getImageStream(image, frame, (byte)2, iProducer);
               POSPrinterCmd ppc = null;
               width = image.getWidth(frame);
               int height = image.getHeight(frame);
               int BytesWide = (width - 1) / 8 + 1;
               ByteBuffer data = new ByteBuffer();
               if (width == -11 && (image.getWidth(frame) - 1) / 8 + 1 > 53) {
                  throw new JposException(114, 206);
               }

               if ((width - 1) / 8 + 1 > 53) {
                  throw new JposException(106, "Width value is too big");
               }

               if (width == -11) {
                  width = image.getWidth(frame);
               }

               image = pUtil.scaleImage(image, frame, width, 424, 180, 10240);
               int TotalBytes = BytesWide * 30;
               byte HH = (byte)(TotalBytes >> 8 & 0xFF);
               byte LL = (byte)(TotalBytes & 0xFF);

               for (int row = 0; row < height; row += 30) {
                  data.reset();

                  for (int y = 0; y < 30; y++) {
                     for (int x = 0; x < BytesWide; x++) {
                        if (y + row >= height) {
                           data.append(0);
                        } else {
                           data.append(bitmapstream[(row + y) * BytesWide + x]);
                        }
                     }
                  }

                  ppc = this.factory.createPrintBitmapCmd((byte)2, fileName, data.getBytes(), HH, LL, (byte)0, (byte)0);
                  imageMaster.appendPOSPrinterCmd(ppc);
               }
            } catch (Exception var24) {
               return this.addMultiFeedLines(1, cmd.getStation());
            }
         } else {
            imageMaster = this.addMultiFeedLines(1, cmd.getStation());
         }

         return imageMaster;
      }

      public POSPrinterCmd.Factory getFactory() {
         return this.factory;
      }

      protected POSPrinterCmd addMultiFeedLines(int lineCnt, byte station) {
         if (lineCnt <= 0) {
            return this.factory.createFeedLinesCmd((short)0, station);
         } else {
            POSPrinterCmd masterCmd = null;
            masterCmd = this.factory.createFeedLinesCmd((short)1, station);

            for (int i = 1; lineCnt != i; i++) {
               masterCmd.appendPOSPrinterCmd(this.factory.createFeedLinesCmd((short)1, station));
            }

            return masterCmd;
         }
      }
   }

   static class UpdateParserStateV extends DefaultPrinterEscCmdV {
      PrinterParserState parserState = null;

      UpdateParserStateV(PrinterParserState parserState) {
         this.parserState = parserState;
      }

      public POSPrinterCmd getPOSPrinterCmd() {
         return null;
      }

      public void reset() {
      }

      public void visitCenterEscCmd(PrinterEscCmd cmd) {
         this.parserState.setAlignment(cmd.getStation(), (byte)1);
      }

      public void visitRightJustifyEscCmd(PrinterEscCmd cmd) {
         this.parserState.setAlignment(cmd.getStation(), (byte)2);
      }

      public void visitNormalEscCmd(PrinterEscCmd cmd) {
         this.parserState.setNormalWithLFState(1, this.parserState.getAlignment(cmd.getStation()));
         this.parserState.setAlignment(cmd.getStation(), (byte)0);
         this.parserState.setFontWideMagnification(cmd.getStation(), (byte)1);
         this.parserState.setFontHighMagnification(cmd.getStation(), (byte)1);
      }

      public void visitHighWideControlEscCmd(PrinterEscCmd cmd) {
         byte fontWideMagnification = this.parserState.getFontWideMagnification(cmd.getStation());
         byte fontHighMagnification = this.parserState.getFontHighMagnification(cmd.getStation());
         if (cmd.getParameter() == 1) {
            fontWideMagnification = 1;
            fontHighMagnification = 1;
         } else if (cmd.getParameter() == 2) {
            fontWideMagnification = 2;
         } else if (cmd.getParameter() == 3) {
            fontHighMagnification = 2;
         } else if (cmd.getParameter() == 4) {
            fontWideMagnification = 2;
            fontHighMagnification = 2;
         }

         this.parserState.setFontWideMagnification(cmd.getStation(), fontWideMagnification);
         this.parserState.setFontHighMagnification(cmd.getStation(), fontHighMagnification);
      }

      public void visitScaleHorizontallyEscCmd(PrinterEscCmd cmd) {
         this.parserState.setFontWideMagnification(cmd.getStation(), (byte)cmd.getClosestParameter());
      }

      public void visitScaleVerticallyEscCmd(PrinterEscCmd cmd) {
         this.parserState.setFontHighMagnification(cmd.getStation(), (byte)cmd.getClosestParameter());
      }
   }
}
