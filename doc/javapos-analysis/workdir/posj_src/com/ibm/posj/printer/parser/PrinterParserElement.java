package com.ibm.posj.printer.parser;

import com.ibm.posj.POSPrinterCmd;

public class PrinterParserElement {
   private int index = -1;
   private int length = -1;
   private int elementLength = -1;
   private int textInitPos = -1;
   private int textIndex = -1;
   private byte alignment = -1;
   private int sequence = -1;
   private int charsToPrint = -1;
   private int lineCount = 0;
   private byte appendByte = 0;
   private PrinterParserData data = null;
   private PrinterEscCmdProcessor escCmdProc = null;
   private byte elementType = 1;
   private boolean lineFeedAtTheEnd = false;
   private boolean carriageReturnAtTheEnd = false;
   private boolean removeFirstLF = false;
   private boolean dataTruncated = false;
   public static final byte PRT_PARSER_ELEMENT_OTHER = 0;
   public static final byte PRT_PARSER_ELEMENT_TEXT = 1;
   public static final byte PRT_PARSER_ELEMENT_COMMAND = 2;
   public static final byte PRT_PARSER_ELEMENT_LFCHAIN = 3;
   public static final byte PRT_PARSER_ELEMENT_CR = 4;
   public static final byte PRT_PARSER_ELEMENT_YEN_SIGN = 5;
   public static final int PRT_DATA_NOT_SET = -1;
   public static final int YEN_SIGN = 171;

   public PrinterParserElement(PrinterEscCmdProcessor escCmdProc) throws IllegalArgumentException {
      this.setEscCmdProcessor(escCmdProc);
   }

   public PrinterParserElement() {
   }

   public void setTextIndex(int textIndex) {
      this.textInitPos = textIndex;
      this.reset();
   }

   public void setEscCmdProcessor(PrinterEscCmdProcessor escCmdProc) throws IllegalArgumentException {
      if (escCmdProc == null) {
         throw new IllegalArgumentException("Invalid PrinterEscCmdProcessor parameter");
      } else {
         this.escCmdProc = escCmdProc;
      }
   }

   public void setData(PrinterParserData data) throws IllegalArgumentException {
      if (data == null) {
         throw new IllegalArgumentException("Invalid data buffer parameter");
      } else {
         this.data = data;
      }
   }

   void reset() {
      this.index = -1;
      this.length = -1;
      this.textIndex = this.textInitPos;
      this.appendByte = 0;
      this.lineFeedAtTheEnd = false;
      this.carriageReturnAtTheEnd = false;
      this.dataTruncated = false;
      this.elementType = 1;
   }

   public void setAppendByte(byte appendByte) {
      this.appendByte = appendByte;
   }

   public void setIndex(int index) throws IllegalArgumentException {
      if (index >= 0 && index < this.data.getDataLen()) {
         this.index = index;
      } else {
         throw new IllegalArgumentException("Invalid index value");
      }
   }

   public void setRemoveFirstLF(boolean removeLF) {
      this.removeFirstLF = removeLF;
   }

   public void setLength(int length) throws IllegalArgumentException {
      if (length >= 0 && this.index + length <= this.data.getDataLen()) {
         this.length = length;
      } else {
         throw new IllegalArgumentException("Invalid length value");
      }
   }

   public void setAlignment(byte alignment) throws IllegalArgumentException {
      if (alignment != 0 && alignment != 1 && alignment != 2) {
         throw new IllegalArgumentException("Invalid alignment argument");
      } else {
         this.alignment = alignment;
      }
   }

   public void setSequence(int sequence) throws IllegalArgumentException {
      if (sequence < 0) {
         throw new IllegalArgumentException("Invalid sequence argument");
      } else {
         this.sequence = sequence;
      }
   }

   public void setCharsToPrint(int charsToPrint) {
      if (charsToPrint < 0) {
         throw new IllegalArgumentException("Invalid argument value");
      } else {
         this.charsToPrint = charsToPrint;
         this.dataTruncated = charsToPrint < this.getLength();
      }
   }

   public int getCharsToPrint() {
      return this.charsToPrint;
   }

   public int getSequence() {
      return this.sequence;
   }

   public byte getAlignment() {
      return this.alignment;
   }

   public int getIndex() {
      return this.index;
   }

   public int getLength() {
      return this.length;
   }

   protected int getLengthInDots() {
      return this.getLength() * this.escCmdProc.getParserState().getCharSize(this.getStation());
   }

   public byte getStation() {
      return this.data.getStation();
   }

   public PrinterParserData getData() {
      return this.data;
   }

   public byte[] getBytesRef() {
      return this.data.getBytesRef();
   }

   public byte getElementType() {
      return this.elementType;
   }

   public int getElementLength() {
      return this.elementLength;
   }

   public POSPrinterCmd getPOSPrintNormalCmd() {
      POSPrinterCmd.PrintNormalCmd prtNormal = null;
      if (this.elementType == 5) {
         byte[] nullArray = null;
         prtNormal = (POSPrinterCmd.PrintNormalCmd)this.getFactory()
            .createPrintNormalCmd(this.getStation(), nullArray, this.getCharacterSet(), 171, this.appendByte);
      } else {
         prtNormal = (POSPrinterCmd.PrintNormalCmd)this.getFactory()
            .createPrintNormalCmd(this.getStation(), this.data.getData(), this.index, this.charsToPrint, this.appendByte);
      }

      prtNormal.incompleteLine();
      prtNormal.setVerticalScale((short)this.escCmdProc.getParserState().getFontHighMagnification(this.getStation()));
      return prtNormal;
   }

   public POSPrinterCmd getPOSPrinterCmd() {
      POSPrinterCmd printerCmd = null;
      short ONE = 1;
      if (this.elementType == 3) {
         printerCmd = this.getFactory().createFeedLinesCmd((short)1, this.getStation());

         for (int x = 0; x < this.getLength() - 1; x++) {
            printerCmd.appendPOSPrinterCmd(this.getFactory().createFeedLinesCmd((short)1, this.getStation()));
         }
      } else if (this.elementType == 4) {
      }

      return printerCmd;
   }

   public POSPrinterCmd.Factory getFactory() {
      return this.escCmdProc.getFactory();
   }

   public PrinterEscCmdProcessor getCmdProcessor() {
      return this.escCmdProc;
   }

   public boolean parse() {
      boolean validText = false;
      this.reset();
      if (this.getData().isLF(this.textIndex)) {
         this.elementType = 3;
      } else if (this.getData().isCR(this.textIndex)) {
         this.elementType = 4;
      } else if (this.getData().isYenSign(this.textIndex)) {
         this.elementType = 5;
      } else {
         this.elementType = 1;
      }

      return this.parseNormalText(this.elementType);
   }

   public void incIndex(int incChars) {
      this.setLength(this.getLength() - incChars);
      this.setIndex(this.getIndex() + incChars);
   }

   public boolean isLineFeedAtTheEnd() {
      return this.lineFeedAtTheEnd;
   }

   public void setLineFeedAtTheEnd() {
      this.lineFeedAtTheEnd = true;
   }

   public boolean isCarriageReturnAtTheEnd() {
      return this.carriageReturnAtTheEnd;
   }

   public void setCarriageReturnAtTheEnd() {
      this.carriageReturnAtTheEnd = true;
   }

   public boolean isDataTruncated() {
      return this.dataTruncated;
   }

   public boolean isDBCSLeadByte(int position) {
      return this.escCmdProc.getByteEncoder().isDBCSLeadByte(this.getData().getByte(this.index + position - 1));
   }

   public boolean isDBCSCodePage() {
      return this.escCmdProc.getByteEncoder().isDBCSCodePage();
   }

   public int getCharacterSet() {
      return this.escCmdProc.getParserState().getCharacterSet();
   }

   public boolean isDBCSChar(int position) {
      boolean isDBChar = false;
      if (this.isDBCSCodePage() && this.isDBCSLeadByte(position)) {
         int index = position - 1;
         boolean singleByteCharFound = false;

         while (index > 0 && !singleByteCharFound) {
            singleByteCharFound = !this.isDBCSLeadByte(index--);
         }

         boolean oddOffset = (position - (index + 1)) % 2 != 0;
         isDBChar = singleByteCharFound ? oddOffset : !oddOffset;
      }

      return isDBChar;
   }

   public boolean isAction() {
      return false;
   }

   public boolean isActionLF() {
      return false;
   }

   public boolean isProperty() {
      return false;
   }

   public boolean isCmdWithText() {
      return false;
   }

   public int hashCode() {
      int hashCode = 0;
      if (this.data != null) {
         for (int i = this.index; i < this.index + this.length; i++) {
            hashCode <<= 8;
            hashCode += this.data.getByte(i);
         }
      }

      return hashCode;
   }

   public boolean equals(Object str) {
      return str.hashCode() == this.hashCode();
   }

   public boolean isValidChar(int cmdIndex) {
      boolean isValid = true;
      byte charComp = this.getData().getBytesRef()[cmdIndex];
      switch (charComp) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 11:
         case 12:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 28:
         case 29:
         case 30:
         case 31:
            isValid = false;
         case 9:
         case 10:
         case 13:
         case 27:
         default:
            return isValid;
      }
   }

   private boolean isEndOfText(int cmdIndex) {
      return this.getData().isEndOfData(cmdIndex)
         || this.getData().isEscapeSequence(cmdIndex)
         || this.getData().isLF(cmdIndex)
         || this.getData().isESC(cmdIndex)
         || this.getData().isCR(cmdIndex)
         || this.getData().isYenSign(cmdIndex);
   }

   private boolean isEndOfLFChain(int cmdIndex) {
      return this.getData().isEndOfData(cmdIndex) || !this.getData().isLF(cmdIndex);
   }

   private boolean parseNormalText(byte elementType) {
      boolean validText = false;
      int initText = this.textInitPos;
      if (elementType == 1 || elementType == 5) {
         if (this.getData().isESC(this.textIndex)) {
            this.textIndex += 2;
            initText = this.textIndex;
         }

         if (elementType == 5) {
            this.textIndex++;
         } else {
            while (!this.isEndOfText(this.textIndex)) {
               if (this.isValidChar(this.textIndex)) {
                  this.textIndex++;
               } else {
                  byte[] charsForReplace = this.getCharsForReplace(this.textIndex);
                  this.getData().getData().setByte(charsForReplace[0], this.textIndex);
                  this.textIndex++;
                  this.getData().getData().insert(charsForReplace[1], this.textIndex);
                  this.textIndex++;
               }
            }
         }

         if (this.textIndex - initText > 0) {
            validText = true;
            this.setIndex(initText);
            this.setLength(this.textIndex - initText);
            if (this.getData().isLF(this.textIndex)) {
               this.lineFeedAtTheEnd = true;
               if (this.getData().isLFCR(this.textIndex)) {
                  this.textIndex++;
               }

               this.textIndex++;
            } else if (this.getData().isCR(this.textIndex)) {
               if (this.getData().isCRLF(this.textIndex)) {
                  this.lineFeedAtTheEnd = true;
                  this.textIndex++;
               } else {
                  this.carriageReturnAtTheEnd = true;
               }

               this.textIndex++;
            }

            this.elementLength = this.textIndex - this.textInitPos;
            this.charsToPrint = this.getLength();
            this.lineCount = elementType == 3 ? this.charsToPrint : 0;
         }
      } else if (elementType == 3) {
         int lfCount;
         for (lfCount = 0; !this.isEndOfLFChain(this.textIndex); this.textIndex++) {
            if (this.getData().isLFCR(this.textIndex)) {
               this.textIndex++;
            }

            lfCount++;
         }

         this.setIndex(initText);
         this.setLength(lfCount);
         this.elementLength = this.textIndex - this.textInitPos;
      } else {
         if (elementType != 4) {
            throw new IllegalArgumentException("Invalid argument value");
         }

         int len = 1;
         if (this.removeFirstLF) {
            this.removeFirstLF = false;
            if (this.getData().isCRLF(this.textIndex)) {
               this.textIndex++;
            }

            len = 0;
         }

         if (this.getData().isCRLF(this.textIndex)) {
            this.elementType = 3;
            this.textIndex++;
         }

         this.textIndex++;
         this.setIndex(initText);
         this.setLength(len);
         this.elementLength = this.textIndex - this.textInitPos;
      }

      return validText;
   }

   private byte[] getCharsForReplace(int position) {
      byte[] chars = null;
      switch (this.getData().getBytesRef()[position]) {
         case 0:
            chars = new byte[]{48, 48};
            break;
         case 1:
            chars = new byte[]{48, 49};
            break;
         case 2:
            chars = new byte[]{48, 50};
            break;
         case 3:
            chars = new byte[]{48, 51};
            break;
         case 4:
            chars = new byte[]{48, 52};
            break;
         case 5:
            chars = new byte[]{48, 53};
            break;
         case 6:
            chars = new byte[]{48, 54};
            break;
         case 7:
            chars = new byte[]{48, 55};
            break;
         case 8:
            chars = new byte[]{48, 56};
         case 9:
         case 10:
         case 13:
         case 27:
         default:
            break;
         case 11:
            chars = new byte[]{48, 66};
            break;
         case 12:
            chars = new byte[]{48, 67};
            break;
         case 14:
            chars = new byte[]{48, 69};
            break;
         case 15:
            chars = new byte[]{48, 70};
            break;
         case 16:
            chars = new byte[]{49, 48};
            break;
         case 17:
            chars = new byte[]{49, 49};
            break;
         case 18:
            chars = new byte[]{49, 50};
            break;
         case 19:
            chars = new byte[]{49, 51};
            break;
         case 20:
            chars = new byte[]{49, 52};
            break;
         case 21:
            chars = new byte[]{49, 53};
            break;
         case 22:
            chars = new byte[]{49, 54};
            break;
         case 23:
            chars = new byte[]{49, 55};
            break;
         case 24:
            chars = new byte[]{49, 56};
            break;
         case 25:
            chars = new byte[]{49, 57};
            break;
         case 26:
            chars = new byte[]{49, 65};
            break;
         case 28:
            chars = new byte[]{49, 67};
            break;
         case 29:
            chars = new byte[]{49, 68};
            break;
         case 30:
            chars = new byte[]{49, 69};
            break;
         case 31:
            chars = new byte[]{49, 70};
      }

      return chars;
   }
}
