package com.ibm.posj.printer.parser;

import com.ibm.posj.POSPrinterCmd;

public class PrinterParserLine implements ParserLine {
   private int[] tab3 = new int[]{0, 0, 0};
   private int[] tab2 = new int[]{0, 0};
   private int[] tab1 = new int[]{0};
   private PrinterParserLine.LeftText leftText = new PrinterParserLine.LeftText();
   private PrinterParserLine.CenterText centerText = new PrinterParserLine.CenterText();
   private PrinterParserLine.RightText rightText = new PrinterParserLine.RightText();
   private PrinterEscCmdProcessor escCmdProc = null;
   private static PrinterPropertiesManager propManager = new PrinterPropertiesManager();
   private byte station = 2;
   protected boolean restoreLF = false;
   protected boolean newProperties = false;
   public boolean newLineElement = true;
   public int charsInLine = 0;
   public POSPrinterCmd lineMasterCmd = null;
   public boolean lineCompleted = false;
   public boolean needsLF = true;
   public boolean needsCR = false;
   public boolean addLineFeedAtTheEnd = true;
   private static final byte[] INITARRAY = new byte[0];
   private static final byte TAB = 9;
   private static final byte LF = 10;

   PrinterParserLine() {
   }

   public PrinterParserLine(PrinterEscCmdProcessor escCmdProc) {
      this.setEscCmdProcessor(escCmdProc);
   }

   public void init() {
      this.reset();
      this.leftText.init();
      this.centerText.init();
      this.rightText.init();
   }

   public void reset() {
      this.lineMasterCmd = null;
      this.charsInLine = 0;
      this.lineCompleted = false;
      this.newLineElement = true;
      this.needsLF = true;
      this.needsCR = false;
      this.addLineFeedAtTheEnd = true;
      this.getLeftText().init();
      this.getCenterText().init();
      this.getRightText().init();
   }

   public void setStation(byte station) throws IllegalArgumentException {
      if (station != 2 && station != 4 && station != 8 && station != 32) {
         throw new IllegalArgumentException("Invalid station value");
      } else {
         this.station = station;
      }
   }

   public int appendElement(PrinterParserElement parserElement) {
      int appendedChars = 0;
      if (parserElement.getElementType() == 2) {
         this.updateState((PrinterParserEscCmd)parserElement);
         if (parserElement.isProperty()) {
            propManager.append(((PrinterParserEscCmd)parserElement).getPOSPrinterCmd(), ((PrinterParserEscCmd)parserElement).getEscCmd().getType());
            this.getAlignedText().setNewProperty(true);
         }
      }

      if (parserElement.isAction()) {
         if (parserElement.isCmdWithText()) {
            if (propManager.arePropertiesPresent()) {
               this.getAlignedText().appendToTextMasterCmd(propManager.getPOSMasterCmd(false));
            }

            this.getState().setNormalWithLFState(0, (byte)-1);
         }

         if (this.getState().getNormalWithLFState() != 1) {
            this.getAlignedText().appendCmd((PrinterParserEscCmd)parserElement);
            this.getState().setNormalWithLFState(0, (byte)-1);
         } else {
            this.getAlignedText(this.getState().getLastAlignment()).appendCmd((PrinterParserEscCmd)parserElement);
            this.getState().setNormalWithLFState(0, (byte)-1);
         }
      } else if (parserElement.getElementType() == 1 || parserElement.getElementType() == 5) {
         this.getState().setNormalWithLFState(0, (byte)-1);
         appendedChars = this.getAlignedText().appendText(parserElement);
         this.charsInLine += appendedChars;
      }

      this.restoreLF = parserElement.isLineFeedAtTheEnd() || parserElement.isCarriageReturnAtTheEnd();
      if (appendedChars < parserElement.getLength() && (parserElement.getElementType() == 5 || parserElement.getElementType() == 1)
         || this.restoreLF
         || parserElement.isActionLF()) {
         if (parserElement.isActionLF()) {
            this.lFNotNeeded();
         }

         if (parserElement.isCarriageReturnAtTheEnd() && this.escCmdProc.getHandleState().getCapFeedReverse(this.station)) {
            this.cRNeeded();
         }

         this.lineCompleted = true;
         this.buildLineMasterCmd();
      }

      return appendedChars;
   }

   public boolean isLineCompleted() {
      return this.lineCompleted;
   }

   public void setEscCmdProcessor(PrinterEscCmdProcessor escCmdProc) throws IllegalArgumentException {
      if (escCmdProc == null) {
         throw new IllegalArgumentException("Invalid PrinterEscCmdProcessor parameter");
      } else {
         this.escCmdProc = escCmdProc;
         propManager.setEscCmdProcessor(escCmdProc);
      }
   }

   public PrinterEscCmdProcessor getEscCmdProcessor() throws IllegalAccessException {
      if (this.escCmdProc == null) {
         throw new IllegalAccessException("Illegal acces to PrinterEscCmdProcessor");
      } else {
         return this.escCmdProc;
      }
   }

   public PrinterParserLine.AlignedText getAlignedText(byte alignment) {
      PrinterParserLine.AlignedText alignedText = null;
      if (alignment == 1) {
         alignedText = this.getCenterText();
      } else if (alignment == 0) {
         alignedText = this.getLeftText();
      } else if (alignment == 2) {
         alignedText = this.getRightText();
      }

      return alignedText;
   }

   public byte getStation() {
      return this.station;
   }

   public PrinterParserLine.AlignedText getLeftText() {
      return this.leftText;
   }

   public PrinterParserLine.AlignedText getCenterText() {
      return this.centerText;
   }

   public PrinterParserLine.AlignedText getRightText() {
      return this.rightText;
   }

   public void buildLineMasterCmd(boolean addLineFeedAtTheEnd) {
      this.addLineFeedAtTheEnd = addLineFeedAtTheEnd;
      this.createAlignmentTabs();
      this.completeTextMasterCommands();
      this.joinTextMasterCommands();
   }

   public void buildLineMasterCmd() {
      this.buildLineMasterCmd(true);
   }

   public boolean isLFNeeded() {
      return this.needsLF;
   }

   public void lFNotNeeded() {
      this.needsLF = false;
   }

   public boolean isCRNeeded() {
      return this.needsCR;
   }

   public void cRNeeded() {
      this.needsCR = true;
   }

   public POSPrinterCmd getLineMasterCmd() {
      return this.lineMasterCmd;
   }

   public POSPrinterCmd getProperties() {
      return propManager.getPOSMasterCmd(false);
   }

   public void resetProperties() {
      propManager.reset();
   }

   public void clearProperties() {
      propManager.clear();
   }

   public PrinterParserLine.AlignedText getAlignedText() {
      return this.getAlignedText(this.getAlignment());
   }

   protected PrinterParserState getState() {
      return this.escCmdProc.getParserState();
   }

   protected int getCharsInLine() {
      return this.charsInLine;
   }

   protected POSPrinterCmd.Factory getFactory() {
      return this.escCmdProc.getFactory();
   }

   protected boolean areNewPropertiesInLine() {
      return this.newProperties;
   }

   public void updateState(PrinterParserEscCmd commandElement) {
      PrinterEscCmd escCmd = commandElement.getEscCmd();

      try {
         escCmd.accept(this.escCmdProc.getUpdateParserStateVisitor());
      } catch (IllegalAccessException var4) {
      }
   }

   private byte getAlignment() {
      return this.getState().getAlignment(this.getStation());
   }

   private void createAlignmentTabs() {
      int[] tabs = null;
      boolean center = false;
      int offset = this.station == 4 ? this.getState().getLineOffset(this.station) : 0;
      if (this.leftText.isTextPresent() && offset > 0) {
         if (this.centerText.isTextPresent()) {
            if (this.rightText.isTextPresent()) {
               this.tab3[0] = offset;
               this.tab3[1] = this.centerText.getInitialPosition() + offset;
               this.tab3[2] = this.rightText.getInitialPosition() + offset;
               tabs = this.tab3;
            } else {
               this.tab2[0] = offset;
               this.tab2[1] = this.centerText.getInitialPosition() + offset;
               tabs = this.tab2;
            }
         } else if (this.rightText.isTextPresent()) {
            this.tab2[0] = offset;
            this.tab2[1] = this.rightText.getInitialPosition() + offset;
            tabs = this.tab2;
         } else {
            this.tab1[0] = offset;
            tabs = this.tab1;
         }
      } else if (this.centerText.isTextPresent()) {
         int centerStartingPos = this.centerText.getInitialPosition() == 0 ? 1 : this.centerText.getInitialPosition();
         center = true;
         if (this.rightText.isTextPresent()) {
            this.tab2[0] = centerStartingPos + offset;
            this.tab2[1] = this.rightText.getInitialPosition() + offset;
            tabs = this.tab2;
         } else {
            this.tab1[0] = centerStartingPos + offset;
            tabs = this.tab1;
         }
      } else if (this.rightText.isTextPresent()) {
         int rightStartingPos = this.rightText.getInitialPosition() == 0 ? 1 : this.rightText.getInitialPosition();
         this.tab1[0] = rightStartingPos + offset;
         tabs = this.tab1;
      }

      this.newLineElement = true;
      if (this.getState().getRotation(this.getStation()) == 180) {
         this.getState().clearTabs();
      }

      if (tabs != null && this.getState().areNewTabs(this.getStation(), tabs)) {
         this.getState().setCurrentTabs(this.getStation(), tabs);
         this.appendLineMasterCmd(this.getFactory().createSetTabStopsCmd(this.getStation(), tabs, center));
      }
   }

   private void completeTextMasterCommands() {
      this.leftText.completeTextMasterCommand();
      this.centerText.completeTextMasterCommand();
      this.rightText.completeTextMasterCommand();
   }

   private void joinTextMasterCommands() {
      boolean left = this.leftText.isNewElement();
      boolean center = this.centerText.isNewElement();
      boolean right = this.rightText.isNewElement();
      if (!left) {
         this.appendLineMasterCmd(this.leftText.getTextMasterCmd());
      }

      if (!center) {
         this.appendLineMasterCmd(this.centerText.getTextMasterCmd());
      }

      if (!right) {
         this.appendLineMasterCmd(this.rightText.getTextMasterCmd());
      }

      if (left && center && right) {
         this.appendLineMasterCmd(this.getFactory().createPrintNormalCmd(this.getStation(), "\n"));
      }
   }

   private void appendLineMasterCmd(POSPrinterCmd prtCmd) {
      if (prtCmd != null) {
         if (this.newLineElement) {
            this.lineMasterCmd = prtCmd;
            this.newLineElement = false;
         } else {
            this.lineMasterCmd.appendPOSPrinterCmd(prtCmd);
         }
      }
   }

   public abstract class AlignedText {
      private int leftLimit = 0;
      private int rightLimit = 0;
      private int initPosition = 0;
      protected POSPrinterCmd textMasterCmd = null;
      private boolean newElement = true;
      protected POSPrinterCmd lastCommand = null;
      protected int textSize = 0;
      private boolean hasProperties = false;

      public void init() {
         this.reset();
         this.setLeftLimit(0);
         this.setRightLimit(PrinterParserLine.this.getState().getLineWidth(PrinterParserLine.this.getStation()));
      }

      public void reset() {
         this.textSize = 0;
         this.setNewElement(true);
         this.setNewProperty(false);
      }

      public void setLeftLimit(int leftLimit) throws IllegalArgumentException {
         if (leftLimit < 0) {
            throw new IllegalArgumentException("Invalid left limit value");
         } else {
            this.leftLimit = leftLimit;
         }
      }

      public void setRightLimit(int rightLimit) throws IllegalArgumentException {
         if (rightLimit < 0) {
            throw new IllegalArgumentException("Invalid right limit value");
         } else {
            this.rightLimit = rightLimit;
         }
      }

      public boolean isTextPresent() {
         return this.textSize > 0;
      }

      public void increaseTextSize(int size) {
         this.textSize += size;
      }

      protected int appendText(PrinterParserElement element) {
         if (element == null) {
            throw new IllegalArgumentException("Invalid text element");
         } else {
            int maxCharsInText = this.getMaxCharsInText();
            if (maxCharsInText >= 0) {
               if (maxCharsInText > element.getLength()) {
                  maxCharsInText = element.getLength();
               }

               if (PrinterParserLine.this.getCharsInLine() + maxCharsInText
                  > PrinterParserLine.this.getState().getMaxPrintChars(PrinterParserLine.this.getStation())) {
                  maxCharsInText = PrinterParserLine.this.getState().getMaxPrintChars(PrinterParserLine.this.getStation())
                     - PrinterParserLine.this.getCharsInLine();
               }

               if (maxCharsInText < element.getLength() && element.isDBCSChar(maxCharsInText)) {
                  maxCharsInText--;
               }

               element.setCharsToPrint(maxCharsInText);
               if (maxCharsInText > 0) {
                  this.textSize = this.textSize + maxCharsInText * PrinterParserLine.this.getState().getCharSize(PrinterParserLine.this.getStation());
                  this.updateLimits(this.textSize);
                  if (PrinterParserLine.propManager.arePropertiesPresent()) {
                     this.appendToTextMasterCmd(PrinterParserLine.propManager.getPOSMasterCmd(false));
                  }

                  this.appendToTextMasterCmd(element.getPOSPrintNormalCmd());
               }
            }

            return maxCharsInText;
         }
      }

      protected void appendCmd(PrinterParserEscCmd escCmd) {
         if (escCmd != null) {
            if (this.isNewElement()) {
               this.setTextMasterCmd(escCmd.getPOSPrinterCmd());
               this.setNewElement(false);
            } else {
               this.appendToMasterCmd(escCmd.getPOSPrinterCmd());
            }
         }
      }

      protected void appendToTextMasterCmd(POSPrinterCmd prtCmd) {
         if (prtCmd != null) {
            boolean rotation = PrinterParserLine.this.getState().getRotation(PrinterParserLine.this.station) != 0
               && PrinterParserLine.this.getState().getRotation(PrinterParserLine.this.station) != 180
               && PrinterParserLine.this.station == 2;
            if (this.isNewElement()) {
               POSPrinterCmd clearProCmd = this.createClearPropertiesBeforeTab();
               if (clearProCmd != null) {
                  this.setTextMasterCmd(clearProCmd);
                  if (!rotation) {
                     this.appendToMasterCmd(this.createAppendByteCmd((byte)9));
                  }

                  this.appendToMasterCmd(prtCmd);
               } else if (!rotation) {
                  this.setTextMasterCmd(this.createAppendByteCmd((byte)9));
                  this.appendToMasterCmd(prtCmd);
               } else {
                  this.setTextMasterCmd(prtCmd);
               }

               this.setNewElement(false);
            } else {
               this.appendToMasterCmd(prtCmd);
            }
         }
      }

      protected void appendToMasterCmd(POSPrinterCmd prtCmd) {
         this.getTextMasterCmd().appendPOSPrinterCmd(prtCmd);
         this.lastCommand = prtCmd;
      }

      protected void setInitialPosition(int initPosition) {
         if (initPosition < 0) {
            throw new IllegalArgumentException("Invalid initial position value");
         } else {
            this.initPosition = initPosition;
         }
      }

      public int getLeftLimit() {
         return this.leftLimit;
      }

      public int getRightLimit() {
         return this.rightLimit;
      }

      protected int getInitialPosition() {
         return this.initPosition;
      }

      public int getTextSize() {
         return this.textSize;
      }

      protected int getFreeSpace() {
         int freeSpace = this.rightLimit - this.leftLimit - this.textSize;
         if (freeSpace < 0) {
            freeSpace = 0;
         }

         return freeSpace;
      }

      protected int getMaxCharsInText() {
         return this.getFreeSpace() / PrinterParserLine.this.getState().getCharSize(PrinterParserLine.this.getStation());
      }

      protected boolean isPOSPrinterCmdPresent() {
         return !this.isNewElement() || this.hasProperties();
      }

      protected POSPrinterCmd getTextMasterCmd() {
         return this.textMasterCmd;
      }

      protected void setTextMasterCmd(POSPrinterCmd textMasterCmd) {
         this.textMasterCmd = textMasterCmd;
         this.lastCommand = textMasterCmd;
      }

      protected POSPrinterCmd createAppendByteCmd(byte appendByte) {
         byte[] nullArray = null;
         int lcount = appendByte == 10 ? 1 : 0;
         return PrinterParserLine.this.getFactory().createPrintNormalCmd(PrinterParserLine.this.getStation(), nullArray, 0, 0, appendByte);
      }

      protected POSPrinterCmd createClearPropertiesBeforeTab() {
         POSPrinterCmd normalModeCmd = null;
         if (PrinterParserLine.propManager.arePropertiesPresent()) {
            normalModeCmd = PrinterParserLine.this.getFactory().createNormalModeCmd(true);
         }

         return normalModeCmd;
      }

      protected boolean isNewElement() {
         return this.newElement;
      }

      protected void setNewElement(boolean newElement) {
         this.newElement = newElement;
      }

      protected void setNewProperty(boolean newProperty) {
         this.hasProperties = newProperty;
      }

      protected boolean hasProperties() {
         return this.hasProperties;
      }

      protected void completeTextObject() {
         if (PrinterParserLine.this.isLFNeeded() && PrinterParserLine.this.addLineFeedAtTheEnd) {
            if (PrinterParserLine.this.restoreLF && this.lastCommand instanceof POSPrinterCmd.PrintNormalCmd) {
               ((POSPrinterCmd.PrintNormalCmd)this.lastCommand).setAppendByte((byte)10);
               ((POSPrinterCmd.PrintNormalCmd)this.lastCommand).completeLine();
            } else {
               this.appendToTextMasterCmd(this.createAppendByteCmd((byte)10));
            }
         }

         boolean rotation = PrinterParserLine.this.getState().getRotation(PrinterParserLine.this.station) != 0 && PrinterParserLine.this.station == 2;
         if (PrinterParserLine.propManager.arePropertiesPresent()) {
            if (rotation) {
               this.appendToTextMasterCmd(PrinterParserLine.propManager.getPOSMasterCmd(false));
            } else {
               this.appendToTextMasterCmd(PrinterParserLine.propManager.getPOSMasterCmd(true));
            }
         }
      }

      protected abstract void updateLimits(int var1);

      protected abstract void completeTextMasterCommand();
   }

   class CenterText extends PrinterParserLine.AlignedText {
      protected void completeTextMasterCommand() {
         if (this.isPOSPrinterCmdPresent() && !PrinterParserLine.this.rightText.isPOSPrinterCmdPresent()) {
            this.completeTextObject();
         }
      }

      protected void updateLimits(int textSize) {
         int lineWidth = PrinterParserLine.this.getState().getLineWidth(PrinterParserLine.this.getStation());
         int centerLeftLimit = (lineWidth - textSize) / 2;
         int centerRightLimit = (lineWidth + textSize) / 2;
         this.setInitialPosition(centerLeftLimit);
         PrinterParserLine.this.leftText.setRightLimit(centerLeftLimit);
         PrinterParserLine.this.rightText.setLeftLimit(centerRightLimit);
      }
   }

   class LeftText extends PrinterParserLine.AlignedText {
      protected void appendToTextMasterCmd(POSPrinterCmd prtCmd) {
         if (prtCmd != null) {
            if (this.isNewElement()) {
               if (PrinterParserLine.this.station == 4 && PrinterParserLine.this.getState().getLineOffset(PrinterParserLine.this.station) > 0) {
                  POSPrinterCmd clearProCmd = this.createClearPropertiesBeforeTab();
                  if (clearProCmd != null) {
                     this.setTextMasterCmd(clearProCmd);
                     this.appendToMasterCmd(this.createAppendByteCmd((byte)9));
                  } else {
                     this.setTextMasterCmd(this.createAppendByteCmd((byte)9));
                  }

                  this.appendToMasterCmd(prtCmd);
               } else {
                  this.setTextMasterCmd(prtCmd);
               }

               this.setNewElement(false);
            } else {
               this.appendToMasterCmd(prtCmd);
            }
         }
      }

      protected void completeTextMasterCommand() {
         if (this.isPOSPrinterCmdPresent()
            && !PrinterParserLine.this.centerText.isPOSPrinterCmdPresent()
            && !PrinterParserLine.this.rightText.isPOSPrinterCmdPresent()) {
            this.completeTextObject();
         }
      }

      protected void updateLimits(int textSize) {
         int lineWidth = PrinterParserLine.this.getState().getLineWidth(PrinterParserLine.this.getStation());
         PrinterParserLine.this.centerText.setLeftLimit(textSize);
         PrinterParserLine.this.centerText.setRightLimit(lineWidth - textSize);
         if (!PrinterParserLine.this.centerText.isTextPresent()) {
            PrinterParserLine.this.rightText.setLeftLimit(textSize);
         }
      }
   }

   class RightText extends PrinterParserLine.AlignedText {
      protected void completeTextMasterCommand() {
         if (this.isPOSPrinterCmdPresent()) {
            this.completeTextObject();
         }
      }

      protected void updateLimits(int textSize) {
         int lineWidth = PrinterParserLine.this.getState().getLineWidth(PrinterParserLine.this.getStation());
         int rightRightLimit = lineWidth - textSize;
         this.setInitialPosition(rightRightLimit);
         PrinterParserLine.this.centerText.setRightLimit(rightRightLimit);
         PrinterParserLine.this.centerText.setLeftLimit(textSize);
         if (!PrinterParserLine.this.centerText.isTextPresent()) {
            PrinterParserLine.this.leftText.setRightLimit(rightRightLimit);
         }
      }
   }
}
