package com.ibm.posj.printer.parser;

import com.ibm.jutil.ByteBuffer;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import java.util.ArrayList;
import java.util.List;

public class PrinterParserData {
   private byte station = 2;
   private int dataIndex = 0;
   private ByteBuffer data = null;
   private PrinterParserElement text = new PrinterParserElement();
   private PrinterParserEscCmd escCmd = new PrinterParserEscCmd();
   private boolean process = true;
   private boolean newMaster = true;
   private boolean newLineList = true;
   private List lineList = new ArrayList(10);
   private boolean lastLineCompleted = false;
   private boolean addLineFeedAtTheEnd = true;
   private boolean removeNextElementFirstLF = false;
   protected boolean dataHasLogos = false;
   protected ParserLine line = null;
   public PrinterEscCmdProcessor escCmdProc = null;
   public POSPrinterCmd dataMasterCmd = null;
   public static final int PRT_DATA_NOT_SET = -1;
   public static final byte PRT_BYTE_ESCAPE = 27;
   public static final byte PRT_BYTE_VERTICALBAR = 124;
   public static final byte PTR_BYTE_LF = 10;
   public static final byte PTR_BYTE_CR = 13;
   public static final int CODE_PAGE_US = 437;
   public static final int CODE_PAGE_ML = 858;
   public static final byte YEN_BYTE_FOR_US = -99;
   public static final byte YEN_BYTE_FOR_ML = -66;

   public void setEscCmdProcessor(PrinterEscCmdProcessor escCmdProc) {
      this.init(escCmdProc);
   }

   public void setStation(byte station) throws IllegalArgumentException {
      if (station != 2 && station != 4 && station != 20 && station != 8 && station != 32) {
         throw new IllegalArgumentException("Invalid station value");
      } else {
         this.station = station;
         this.line.setStation(station);
         this.line.init();
      }
   }

   public void setData(ByteBuffer data) throws IllegalArgumentException {
      if (data == null) {
         throw new IllegalArgumentException("Invalid data buffer parameter");
      } else {
         this.data = data;
      }
   }

   public void setData(String data) throws IllegalArgumentException {
      if (data == null) {
         throw new IllegalArgumentException("Invalid data buffer parameter");
      } else {
         if (this.data == null) {
            this.data = new ByteBuffer();
         }

         this.data.reset();
         this.data.append(data.getBytes());
      }
   }

   public byte getStation() {
      return this.station;
   }

   public ByteBuffer getData() {
      return this.data;
   }

   public int getDataLen() {
      return this.data != null ? this.data.getByteCount() : -1;
   }

   public byte getByte(int bytePos) throws IllegalArgumentException {
      if (this.isEndOfData(bytePos)) {
         throw new IllegalArgumentException("Parameter value is out of the data boundaries");
      } else {
         return this.data.getBytesRef()[bytePos];
      }
   }

   public byte[] getBytesRef() {
      return this.data.getBytesRef();
   }

   public boolean parse() {
      boolean dataTruncated = true;
      int printedChars = 0;
      int totalCharsInLine = 0;
      POSPrinterCmd rememberedProperties = null;
      this.reset();
      PrinterParserElement parserElement = null;
      this.line.reset();

      while ((parserElement = this.getNextElement()) != null) {
         if (parserElement.getElementType() == 2) {
            int commandCode = ((PrinterParserEscCmd)parserElement).getEscCmd().getCode();
            if (commandCode == 6 || commandCode == 7) {
               this.dataHasLogos = true;
               return false;
            }
         }

         if (parserElement.getElementType() == 3) {
            this.appendCmd(parserElement.getPOSPrinterCmd());
         } else if (parserElement.getElementType() != 4) {
            dataTruncated = true;

            while (dataTruncated) {
               printedChars = this.line.appendElement(parserElement);
               totalCharsInLine += printedChars;
               this.lastLineCompleted = false;
               if (this.line.isLineCompleted()) {
                  POSPrinterCmd cmd = this.line.getLineMasterCmd();
                  int rotation = this.escCmdProc.getParserState().getRotation(this.station);
                  if (rotation == 270 || rotation == 90) {
                     ((DefaultPOSPrinterCmd)cmd).setMaxCharCnt(totalCharsInLine);
                     totalCharsInLine = 0;
                  }

                  this.appendCmd(cmd);
                  this.line.reset();
                  this.lastLineCompleted = true;
               }

               dataTruncated = parserElement.isDataTruncated();
               if (dataTruncated) {
                  parserElement.incIndex(printedChars);
               }
            }
         }

         this.removeNextElementFirstLF = parserElement.isActionLF();
      }

      if (!this.lastLineCompleted) {
         this.line.buildLineMasterCmd(this.addLineFeedAtTheEnd);
         this.appendCmd(this.line.getLineMasterCmd());
      }

      if (this.line.getProperties() != null && this.addLineFeedAtTheEnd) {
         this.appendCmd(this.escCmdProc.getFactory().createNormalModeCmd(true));
         this.line.resetProperties();
         if ((rememberedProperties = this.line.getProperties()) != null) {
            this.appendCmd(rememberedProperties);
         }
      }

      int rotation = this.escCmdProc.getParserState().getRotation(this.station);
      if (this.station == 2 && rotation != 180) {
         rotation = 0;
      }

      if (rotation != 0) {
         int iList = this.lineList.size() - 1;

         while (iList >= 0) {
            this.appendDataMasterCmd((POSPrinterCmd)this.lineList.get(iList--));
         }
      }

      return true;
   }

   public boolean validate(boolean valOnly) throws IllegalAccessException, IllegalArgumentException {
      this.reset();
      PrinterParserElement parserElement = null;

      while (!this.isEndOfData()) {
         parserElement = null;
         if (this.isEscapeSequence()) {
            this.escCmd.reset();
            this.escCmd.setCmdIndex(this.dataIndex);
            parserElement = this.escCmd;
            if (!this.escCmd.validate()) {
               throw new IllegalAccessException("The command is not valid");
            }

            this.escCmd.checkPrinterCap();
            this.escCmd.checkCmdParameter(valOnly);
         } else {
            this.text.reset();
            this.text.setTextIndex(this.dataIndex);
            this.text.parse();
            parserElement = this.text;
         }

         this.incrDataIndex(parserElement.getElementLength());
      }

      return true;
   }

   public PrinterParserElement getNextElement() {
      PrinterParserElement parserElement = null;
      boolean isValidElement = false;
      boolean isValidCmd = false;
      boolean isValidStation = false;
      boolean isValidParam = false;
      if (!this.isEndOfData()) {
         isValidElement = false;

         while (!isValidElement && !this.isEndOfData()) {
            if (this.isEscapeSequence()) {
               this.escCmd.reset();
               this.escCmd.setCmdIndex(this.dataIndex);
               parserElement = this.escCmd;
               isValidCmd = this.escCmd.parse();
               isValidParam = this.escCmd.checkParameterCap();
               isValidStation = this.escCmd.checkStationCap();
               if (!isValidCmd) {
                  this.text.reset();
                  if (this.incrDataIndex(2)) {
                     this.text.setTextIndex(this.dataIndex);
                     this.text.setRemoveFirstLF(this.removeNextElementFirstLF);
                     this.text.parse();
                     parserElement = this.text;
                  }
               } else if (!isValidStation) {
                  parserElement.reset();
               }
            } else {
               this.text.reset();
               this.text.setTextIndex(this.dataIndex);
               this.text.setRemoveFirstLF(this.removeNextElementFirstLF);
               this.text.parse();
               parserElement = this.text;
            }

            this.incrDataIndex(parserElement.getElementLength());
            if (!(isValidElement = parserElement.getLength() > 0)) {
               parserElement = null;
            }
         }
      }

      return parserElement;
   }

   public POSPrinterCmd getMasterCmd() {
      this.lineList.clear();
      POSPrinterCmd cmd = null;
      cmd = this.dataMasterCmd;
      this.dataMasterCmd = null;
      return cmd;
   }

   public void reset() {
      this.dataIndex = 0;
      this.escCmdProc.getParserState().resetFontWideMagnification(this.station);
      this.escCmdProc.getParserState().resetFontHighMagnification(this.station);
      this.escCmdProc.getParserState().resetAlignment(this.station);
      this.newMaster = true;
      this.newLineList = true;
      this.dataHasLogos = false;
   }

   public boolean hasLogos() {
      return this.dataHasLogos;
   }

   public void setProcessData(boolean process) {
      this.process = process;
   }

   public boolean isProcessingData() {
      return this.process;
   }

   public void clean() {
      this.reset();
   }

   public void clearProperties() {
      this.line.clearProperties();
   }

   public void setAddLineFeedAtTheEnd(boolean addLineFeedAtTheEnd) {
      this.addLineFeedAtTheEnd = addLineFeedAtTheEnd;
   }

   public boolean addLineFeedAtTheEnd() {
      return this.addLineFeedAtTheEnd;
   }

   protected void init(PrinterEscCmdProcessor escCmdProc) {
      if (escCmdProc == null) {
         throw new IllegalArgumentException("Invalid PrinterEscCmdProcessor parameter");
      } else {
         this.escCmdProc = escCmdProc;
         this.text.setEscCmdProcessor(escCmdProc);
         this.text.setData(this);
         this.escCmd.setEscCmdProcessor(escCmdProc);
         this.escCmd.setData(this);
         this.getParserLine().setEscCmdProcessor(escCmdProc);
      }
   }

   protected boolean isEndOfData(int dataIndex) {
      return dataIndex >= this.getDataLen();
   }

   protected boolean isEndOfData() {
      return this.dataIndex >= this.getDataLen();
   }

   protected boolean isEscapeSequence(int dataIndex) {
      return dataIndex + 1 < this.getDataLen() && this.data.getBytesRef()[dataIndex] == 27 && this.data.getBytesRef()[dataIndex + 1] == 124;
   }

   protected boolean isEscapeSequence() {
      return this.dataIndex + 1 < this.getDataLen() && this.data.getBytesRef()[this.dataIndex] == 27 && this.data.getBytesRef()[this.dataIndex + 1] == 124;
   }

   public boolean isYenSign() {
      return this.getCharacterSet() == 858 && this.data.getBytesRef()[this.dataIndex] == -66
         || this.getCharacterSet() == 437 && this.data.getBytesRef()[this.dataIndex] == -99;
   }

   public boolean isYenSign(int dataIndex) {
      return this.getCharacterSet() == 858 && this.data.getBytesRef()[dataIndex] == -66
         || this.getCharacterSet() == 437 && this.data.getBytesRef()[dataIndex] == -99;
   }

   public boolean isNewMaster() {
      return this.newMaster;
   }

   public void setNewMaster(boolean newMaster) {
      this.newMaster = newMaster;
   }

   protected boolean isLF(int dataIndex) {
      return dataIndex < this.getDataLen() && this.data.getBytesRef()[dataIndex] == 10;
   }

   protected boolean isLF() {
      return this.data.getBytesRef()[this.dataIndex] == 10;
   }

   protected boolean isCR(int dataIndex) {
      return dataIndex < this.getDataLen() && this.data.getBytesRef()[dataIndex] == 13;
   }

   protected boolean isCR() {
      return this.data.getBytesRef()[this.dataIndex] == 13;
   }

   protected boolean isLFCR(int dataIndex) {
      return dataIndex + 1 < this.getDataLen() && this.data.getBytesRef()[dataIndex] == 10 && this.data.getBytesRef()[dataIndex + 1] == 13;
   }

   protected boolean isLFCR() {
      return this.isLFCR(this.dataIndex);
   }

   protected boolean isCRLF(int dataIndex) {
      return dataIndex + 1 < this.getDataLen() && this.data.getBytesRef()[dataIndex] == 13 && this.data.getBytesRef()[dataIndex + 1] == 10;
   }

   protected boolean isCRLF() {
      return this.isCRLF(this.dataIndex);
   }

   protected boolean isESC(int dataIndex) {
      return dataIndex < this.getDataLen() && this.data.getBytesRef()[dataIndex] == 27 && !this.isEscapeSequence(dataIndex);
   }

   protected boolean incrDataIndex() {
      return this.incrDataIndex(1);
   }

   protected boolean incrDataIndex(int bytes) throws IllegalArgumentException {
      boolean canInclement = true;
      if (this.dataIndex + bytes >= this.getDataLen()) {
         canInclement = false;
      }

      this.dataIndex += bytes;
      return canInclement;
   }

   protected boolean decrDataIndex() {
      return this.decrDataIndex(1);
   }

   protected boolean decrDataIndex(int bytes) {
      boolean canDecrement = true;
      if (this.dataIndex - bytes < 0) {
         canDecrement = false;
      } else {
         this.dataIndex -= bytes;
      }

      return canDecrement;
   }

   protected void appendCmd(POSPrinterCmd prtCmd) {
      int rotation = this.escCmdProc.getParserState().getRotation(this.station);
      if (this.station == 2 && rotation != 180) {
         rotation = 0;
      }

      if (rotation != 0) {
         this.appendToLineList(prtCmd);
      } else {
         this.appendDataMasterCmd(prtCmd);
      }
   }

   protected void appendDataMasterCmd(POSPrinterCmd prtCmd) {
      if (prtCmd != null) {
         if (this.newMaster) {
            this.dataMasterCmd = this.escCmdProc.getFactory().createAlignPositionCmd(this.getStation(), (byte)0);
            this.dataMasterCmd.appendPOSPrinterCmd(this.escCmdProc.getFactory().createNormalModeCmd(true));
            this.newMaster = false;
         }

         if (!prtCmd.isSlave()) {
            this.dataMasterCmd.appendPOSPrinterCmd(prtCmd);
         }
      }
   }

   protected void appendToLineList(POSPrinterCmd prtCmd) {
      if (prtCmd != null) {
         if (this.newLineList) {
            this.lineList.clear();
            this.newLineList = false;
         }

         this.lineList.add(prtCmd);
      }
   }

   private int getCharacterSet() {
      return this.escCmdProc.getParserState().getCharacterSet();
   }

   private ParserLine getParserLine() {
      if (this.line == null) {
         this.line = new PrinterParserLine();
      }

      return this.line;
   }
}
