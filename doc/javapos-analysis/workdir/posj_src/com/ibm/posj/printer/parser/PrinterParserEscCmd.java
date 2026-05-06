package com.ibm.posj.printer.parser;

import com.ibm.posj.POSPrinterCmd;

public class PrinterParserEscCmd extends PrinterParserElement {
   private int cmdInitPos = -1;
   private int cmdIndex = -1;
   private int cmdParameter = -1;
   private int cmdSeqLen = -1;
   private byte parameterRadix = 10;
   private boolean validCommandFormat = false;
   private boolean hasParameter = false;
   private PrinterEscCmd escCommand = null;
   private byte elementType = 2;
   public static final int PRT_DATA_NOT_SET = -1;
   public static final byte MINUS_SIGN = 45;

   public PrinterParserEscCmd(PrinterEscCmdProcessor escCmdProc) throws IllegalArgumentException {
      super(escCmdProc);
   }

   public PrinterParserEscCmd() {
   }

   public void setCmdIndex(int cmdIndex) {
      this.cmdInitPos = cmdIndex;
      this.initState();
   }

   public boolean validate() {
      return this.parse(false);
   }

   public boolean parse() {
      return this.parse(true);
   }

   public boolean parse(boolean isProcessing) {
      this.initState();
      boolean validCommand = false;
      short paramSign = 0;
      if (!this.getData().isEndOfData(this.cmdIndex)) {
         if (this.getData().isEscapeSequence(this.cmdIndex)) {
            this.cmdIndex += 2;
         }

         int tempParam = 0;
         if ((paramSign = this.getSign()) == -1) {
            this.cmdIndex++;
         }

         int paramInitPos;
         for (paramInitPos = this.cmdIndex; !this.isEndOfParameter(this.cmdIndex); this.cmdIndex++) {
            tempParam = tempParam * this.parameterRadix + Character.digit((char)this.getData().getBytesRef()[this.cmdIndex], this.parameterRadix);
         }

         if (this.cmdIndex - paramInitPos > 0) {
            this.cmdParameter = paramSign * tempParam;
            this.hasParameter = true;
         }

         if (!this.getData().isEndOfData(this.cmdIndex)) {
            this.reset();
            this.setIndex(this.cmdIndex);

            while (!this.isEndOfCommand(this.cmdIndex)) {
               this.cmdIndex++;
            }

            if (this.isUpperCase()) {
               this.cmdIndex++;
               this.setLength(this.cmdIndex - this.getIndex());
               this.validCommandFormat = true;
            }
         }

         if (this.getData().isLF(this.cmdIndex)) {
            this.setLineFeedAtTheEnd();
            if (this.getData().isLFCR(this.cmdIndex)) {
               this.cmdIndex++;
            }

            this.cmdIndex++;
         } else if (this.getData().isCR(this.cmdIndex)) {
            if (this.getData().isCRLF(this.cmdIndex)) {
               this.setLineFeedAtTheEnd();
               this.cmdIndex++;
            } else {
               this.setCarriageReturnAtTheEnd();
            }

            this.cmdIndex++;
         }

         this.cmdSeqLen = this.cmdIndex - this.cmdInitPos;
         if (!(validCommand = this.isValidCommand())) {
            this.initState();
         }
      }

      return validCommand;
   }

   public boolean checkStationCap() {
      boolean valid = this.isValidCommand();
      if (valid) {
         try {
            this.checkPrinterCap();
         } catch (IllegalAccessException var3) {
            valid = false;
         }
      }

      return valid;
   }

   public boolean checkParameterCap() {
      boolean valid = this.isValidCommand();
      if (valid) {
         try {
            this.checkCmdParameter(false);
         } catch (IllegalAccessException var3) {
            valid = false;
         } catch (IllegalArgumentException var4) {
         }
      }

      return valid;
   }

   public boolean isValidSyntax() {
      return this.validCommandFormat;
   }

   public boolean isValidCommand() {
      if (this.escCommand == null && this.isValidSyntax()) {
         this.escCommand = (PrinterEscCmd)this.getCmdProcessor().getEscCmdHashMap().get(this);
         if (this.escCommand != null) {
            if (!this.isValidMode(this.getData().getStation())) {
               return false;
            }

            this.escCommand.validParameter(this.hasParameter());
            this.escCommand.setParameter(this.cmdParameter);
            this.escCommand.setStation(this.getData().getStation());
         }
      }

      return this.escCommand != null;
   }

   private boolean isValidMode(byte station) {
      String cmdName = "";
      if (null != this.escCommand) {
         cmdName = this.escCommand.getName();
      }

      return this.getCmdProcessor().getParserState().isPageModeOn(station)
         ? !cmdName.equals("Feed and paper cut")
            && !cmdName.equals("Feed, paper cut and stamp")
            && !cmdName.equals("Paper cut")
            && !cmdName.equals("Paper cut")
         : true;
   }

   public void checkPrinterCap() throws IllegalAccessException, IllegalArgumentException {
      if (!this.isValidCommand()) {
         throw new IllegalAccessException("The command is not valid");
      } else {
         this.escCommand.accept(this.getCmdProcessor().getCheckCapCmdVisitor());
      }
   }

   public void checkCmdParameter(boolean b) throws IllegalAccessException, IllegalArgumentException {
      if (!this.isValidCommand()) {
         throw new IllegalAccessException("The command is not valid");
      } else {
         this.escCommand.setValidateOnly(b);
         this.escCommand.accept(this.getCmdProcessor().getCheckParamCmdVisitor());
      }
   }

   public POSPrinterCmd getPOSPrinterCmd() throws IllegalArgumentException {
      if (!this.isValidCommand()) {
         throw new IllegalArgumentException("The command is not valid");
      } else {
         try {
            this.escCommand.accept(this.getCmdProcessor().getCrtPOSPrtCmdVisitor());
         } catch (IllegalAccessException var2) {
         }

         return this.escCommand.getPOSPrinterCmd();
      }
   }

   public int getParameter() {
      return this.cmdParameter;
   }

   public PrinterEscCmd getEscCmd() {
      this.isValidCommand();
      return this.escCommand;
   }

   public byte getElementType() {
      return this.elementType;
   }

   public int getElementLength() {
      return this.cmdSeqLen;
   }

   public boolean isAction() {
      return this.getElementType() == 2 && (this.getEscCmd().getType() == 2 || this.getEscCmd().getType() == 3 || this.getEscCmd().getType() == 5);
   }

   public boolean isProperty() {
      return this.getElementType() == 2 && (this.getEscCmd().getType() == 0 || this.getEscCmd().getType() == 1);
   }

   public boolean isActionLF() {
      return this.getElementType() == 2 && (this.getEscCmd().getType() == 3 || this.getEscCmd().getType() == 5);
   }

   public boolean isCmdWithText() {
      return this.getElementType() == 2 && this.getEscCmd().getType() == 5;
   }

   protected boolean hasParameter() {
      return this.hasParameter;
   }

   private void initState() {
      this.reset();
      this.cmdParameter = -1;
      this.cmdIndex = this.cmdInitPos;
      this.escCommand = null;
      this.validCommandFormat = false;
      this.hasParameter = false;
      this.elementType = 2;
   }

   private void makeNormalText() {
      int tmpCmdIndex = this.cmdIndex;
      this.initState();
      this.elementType = 1;
      if (this.cmdInitPos + 2 < this.getData().getDataLen()) {
         this.setIndex(this.cmdInitPos + 2);
         this.setLength(tmpCmdIndex - this.getIndex());
      }
   }

   private boolean isDigit() {
      return Character.isDigit((char)this.getData().getBytesRef()[this.cmdIndex]);
   }

   private short getSign() {
      return (short)(this.getData().getBytesRef()[this.cmdIndex] == 45 ? -1 : 1);
   }

   private boolean isUpperCase() {
      return Character.isUpperCase((char)this.getData().getBytesRef()[this.cmdIndex]);
   }

   private boolean isEndOfCommand(int cmdIndex) {
      return this.getData().isEndOfData(cmdIndex) || this.getData().isEscapeSequence(cmdIndex) || this.isUpperCase();
   }

   private boolean isEndOfParameter(int cmdIndex) {
      return this.getData().isEndOfData(cmdIndex) || !this.isDigit();
   }
}
