package com.ibm.posj.printer.parser;

import com.ibm.posj.POSPrinterCmd;

public abstract class DefaultPrinterEscCmd implements PrinterEscCmd {
   private int cmdCode = 0;
   private String cmdName = "";
   private int cmdParam = 0;
   private int secCmdParam = 0;
   private byte cmdStation = 0;
   private int cmdClosestParam = 0;
   private POSPrinterCmd posPrinterCmd = null;
   private byte cmdType = 0;
   private boolean hasParameter = false;
   private boolean validateOnly = false;

   public DefaultPrinterEscCmd(int cmdCode, String cmdName, byte cmdType) {
      this.setCode(cmdCode);
      this.setName(cmdName);
      this.setCommandType(cmdType);
   }

   public int getParameter() {
      return this.cmdParam;
   }

   public int getSecParameter() {
      return this.secCmdParam;
   }

   public int getCode() {
      return this.cmdCode;
   }

   public String getName() {
      return this.cmdName;
   }

   public void setCommandType(byte cmdType) {
      this.cmdType = cmdType;
   }

   public void setParameter(int cmdParameter) {
      this.cmdParam = cmdParameter;
   }

   public void setSecParameter(int secCmdParameter) {
      this.secCmdParam = secCmdParameter;
   }

   public byte getStation() {
      return this.cmdStation;
   }

   public void setStation(byte cmdStat) {
      this.cmdStation = cmdStat;
   }

   public void setClosestParameter(int closestParam) {
      this.cmdClosestParam = closestParam;
   }

   public int getClosestParameter() {
      return this.cmdClosestParam;
   }

   public POSPrinterCmd getPOSPrinterCmd() {
      return this.posPrinterCmd;
   }

   public void setPOSPrinterCmd(POSPrinterCmd cmd) {
      this.posPrinterCmd = cmd;
   }

   public byte getType() {
      return this.cmdType;
   }

   public void validParameter(boolean valid) {
      this.hasParameter = valid;
   }

   public boolean hasParameter() {
      return this.hasParameter;
   }

   public void reset() {
      int cmdParam = 0;
      byte cmdStation = 0;
      int cmdClosestParam = 0;
      POSPrinterCmd posPrinterCmd = null;
      this.hasParameter = false;
   }

   public void setValidateOnly(boolean b) {
      this.validateOnly = b;
   }

   public boolean isValidateOnly() {
      return this.validateOnly;
   }

   public abstract void accept(PrinterEscCmdVisitor var1) throws IllegalAccessException, IllegalArgumentException;

   private void setCode(int cmdCodeParam) {
      this.cmdCode = cmdCodeParam;
   }

   private void setName(String cmdNameParam) {
      this.cmdName = cmdNameParam;
   }

   static class AlternateColorEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.AlternateColorEscCmd {
      AlternateColorEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitAlternateColorEscCmd(this);
      }
   }

   static class BoldEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.BoldEscCmd {
      BoldEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitBoldEscCmd(this);
      }
   }

   static class CenterEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.CenterEscCmd {
      CenterEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitCenterEscCmd(this);
      }
   }

   public static class Factory implements PrinterEscCmd.Factory {
      public PrinterEscCmd.PaperCutEscCmd createPaperCutEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.PaperCutEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.FeedPaperCutEscCmd createFeedPaperCutEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.FeedPaperCutEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.FeedPaperCutStampEscCmd createFeedPaperCutStampEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.FeedPaperCutStampEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.FireStampEscCmd createFireStampEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.FireStampEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.PrintBitmapEscCmd createPrintBitmapEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.PrintBitmapEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.PrintTopLogoEscCmd createPrintTopLogoEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.PrintTopLogoEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.PrintBottomLogoEscCmd createPrintBottomLogoEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.PrintBottomLogoEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.FeedLinesEscCmd createFeedLinesEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.FeedLinesEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.FeedUnitsEscCmd createFeedUnitsEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.FeedUnitsEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.FeedReverseEscCmd createFeedReverseEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.FeedReverseEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.FontTypefaceEscCmd createFontTypefaceEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.FontTypefaceEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.BoldEscCmd createBoldEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.BoldEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.UnderlineEscCmd createUnderlineEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.UnderlineEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.ItalicEscCmd createItalicEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.ItalicEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.AlternateColorEscCmd createAlternateColorEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.AlternateColorEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.ReverseVideoEscCmd createReverseVideoEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.ReverseVideoEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.ShadingEscCmd createShadingEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.ShadingEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.HighWideControlEscCmd createHighWideControlEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.HighWideControlEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.ScaleHorizontallyEscCmd createScaleHorizontallyEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.ScaleHorizontallyEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.ScaleVerticallyEscCmd createScaleVerticallyEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.ScaleVerticallyEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.RGBColorEscCmd createRGBColorEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.RGBColorEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.SubScriptEscCmd createSubScriptEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.SubScriptEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.SuperScriptEscCmd createSuperScriptEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.SuperScriptEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.CenterEscCmd createCenterEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.CenterEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.RightJustifyEscCmd createRightJustifyEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.RightJustifyEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.NormalEscCmd createNormalEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.NormalEscCmd(cmdCode, cmdName, type);
      }

      public PrinterEscCmd.PassThruEscCmd createPassThruEscCmd(int cmdCode, String cmdName, byte type) {
         return new DefaultPrinterEscCmd.PassThruEscCmd(cmdCode, cmdName, type);
      }
   }

   static class FeedLinesEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.FeedLinesEscCmd {
      FeedLinesEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitFeedLinesEscCmd(this);
      }
   }

   static class FeedPaperCutEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.FeedPaperCutEscCmd {
      FeedPaperCutEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitFeedPaperCutEscCmd(this);
      }
   }

   static class FeedPaperCutStampEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.FeedPaperCutStampEscCmd {
      FeedPaperCutStampEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitFeedPaperCutStampEscCmd(this);
      }
   }

   static class FeedReverseEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.FeedReverseEscCmd {
      FeedReverseEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitFeedReverseEscCmd(this);
      }
   }

   static class FeedUnitsEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.FeedUnitsEscCmd {
      FeedUnitsEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitFeedUnitsEscCmd(this);
      }
   }

   static class FireStampEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.FireStampEscCmd {
      FireStampEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitFireStampEscCmd(this);
      }
   }

   static class FontTypefaceEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.FontTypefaceEscCmd {
      FontTypefaceEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitFontTypefaceEscCmd(this);
      }
   }

   static class HighWideControlEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.HighWideControlEscCmd {
      HighWideControlEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitHighWideControlEscCmd(this);
      }
   }

   static class ItalicEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.ItalicEscCmd {
      ItalicEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitItalicEscCmd(this);
      }
   }

   static class NormalEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.NormalEscCmd {
      NormalEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitNormalEscCmd(this);
      }
   }

   static class PaperCutEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.PaperCutEscCmd {
      PaperCutEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitPaperCutEscCmd(this);
      }
   }

   static class PassThruEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.PassThruEscCmd {
      PassThruEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitPassThruEscCmd(this);
      }
   }

   static class PrintBitmapEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.PrintBitmapEscCmd {
      PrintBitmapEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitPrintBitmapEscCmd(this);
      }
   }

   static class PrintBottomLogoEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.PrintBottomLogoEscCmd {
      PrintBottomLogoEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitPrintBottomLogoEscCmd(this);
      }
   }

   static class PrintTopLogoEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.PrintTopLogoEscCmd {
      PrintTopLogoEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitPrintTopLogoEscCmd(this);
      }
   }

   static class RGBColorEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.RGBColorEscCmd {
      RGBColorEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitRGBColorEscCmd(this);
      }
   }

   static class ReverseVideoEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.ReverseVideoEscCmd {
      ReverseVideoEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitReverseVideoEscCmd(this);
      }
   }

   static class RightJustifyEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.RightJustifyEscCmd {
      RightJustifyEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitRightJustifyEscCmd(this);
      }
   }

   static class ScaleHorizontallyEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.ScaleHorizontallyEscCmd {
      ScaleHorizontallyEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitScaleHorizontallyEscCmd(this);
      }
   }

   static class ScaleVerticallyEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.ScaleVerticallyEscCmd {
      ScaleVerticallyEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitScaleVerticallyEscCmd(this);
      }
   }

   static class ShadingEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.ShadingEscCmd {
      ShadingEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitShadingEscCmd(this);
      }
   }

   static class SubScriptEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.SubScriptEscCmd {
      SubScriptEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitSubScriptEscCmd(this);
      }
   }

   static class SuperScriptEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.SuperScriptEscCmd {
      SuperScriptEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitSuperScriptEscCmd(this);
      }
   }

   static class UnderlineEscCmd extends DefaultPrinterEscCmd implements PrinterEscCmd.UnderlineEscCmd {
      UnderlineEscCmd(int cmdCode, String cmdName, byte type) {
         super(cmdCode, cmdName, type);
      }

      public void accept(PrinterEscCmdVisitor visitor) throws IllegalAccessException {
         visitor.visitUnderlineEscCmd(this);
      }
   }
}
