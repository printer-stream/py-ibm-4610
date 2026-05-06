package com.ibm.posj.bus.rs485;

import com.ibm.posj.FiscalPrinterInfoHelper;

public class Rs485FiscalPrinterInfoHelper implements FiscalPrinterInfoHelper {
   public static final int FISCAL_RETURN_CODE_POS = 8;
   public static final int STATUS_LENGTH = 9;
   public static final int FISCAL_FINAL_STATUS_POS = 6;
   public static final int FISCAL_ASYNC_STATUS_POS = 6;
   public static final int COUNTRY_POS = 7;
   public static final int VERSION_POS = 7;
   public static final int EC_LEVEL_POS = 13;
   public static final int COVER_OPEN_POS = 1;
   public static final int STS_0 = 0;
   public static final int STS_1 = 1;
   public static final int STS_2 = 2;
   public static final int STS_3 = 3;
   public static final int STS_4 = 4;
   public static final int STS_5 = 5;
   public static final int STS_6 = 6;
   public static final int STS_7 = 7;
   public static final int STS_8 = 8;
   public static final int MP2_PosSTATUS_COVER_OPEN = 1;
   public static final int MP2_PosSTATUS_TRANSPORT_ERROR = 2;
   public static final int MP2_PosSTATUS_SJ_PAPER_ERROR = 4;
   public static final int MP2_PosSTATUS_DOCUMENT_AT_FRONT = 8;
   public static final int MP2_PosSTATUS_DOCUMENT_AT_TOP = 16;
   public static final int MP2_PosSTATUS_DOCUMENT_READY = 32;
   public static final int MP2_PosSTATUS_HEAD_PARKED = 64;
   public static final int MP2_PosSTATUS_INSERTED_FORWARD = 128;
   public static final int MP2_PosSTATUS_ERROR_PENDING = 256;
   public static final int MP2_PosSTATUS_DI_FRONT_LOAD_ERROR = 512;
   public static final int MP2_PosSTATUS_DI_TOP_LOAD_ERROR = 1024;
   public static final int RC_OK = 67;
   public static final int RC_ROL = 112;
   public static final int RC_ERROR = 87;
   public static final int RC_IPL_IN_PROGRESS = 164;
   public static final int RC_COMMAND_REJECT = 192;
   public static final int RC_FRONT_DOC_ERROR = 193;
   public static final int RC_HOME_ERROR = 194;
   public static final int RC_CR_CVR_OR_CR_PAPER_OUT = 200;
   public static final int RC_FRONT_DOC_NOT_PRESENT = 201;
   public static final int RC_DOC_NOT_READY = 202;
   public static final int RC_COVER_OPEN = 203;
   public static final int RC_KEY_PRESSED = 205;
   public static final int RC_JOURNAL_PAPER_ERROR = 206;
   public static final int RC_SUMMARY_COVER_OPEN = 209;
   public static final int RC_EEPROM_LOAD_ERROR = 211;
   public static final int RC_TOP_DOC_NOT_PRESENT = 213;
   public static final int RC_TOP_DOC_ERROR = 214;
   public static final int RC_FRONT_DOC_PRESENT = 225;
   public static final int RC_DOC_READY = 226;
   public static final int RC_TOP_DOC_PRESENT = 237;
   public static final byte STS1_PRINTER_CR_PRINT_ERROR = 16;
   public static final byte STS1_PRINTER_JOURNAL_COVER_OPEN = 16;
   public static final byte STS0_PRINTER_HOME_ERROR = 2;
   public static final byte STS1_PRINTER_JOURNAL_PAPER_FAULT = 16;
   public static final byte STS1_PRINTER_DOC_NOT_PRESENT_FL = 64;
   public static final byte STS2_PRINTER_DOC_NOT_PRESENT_TL = 4;
   public static final byte STS1_PRINTER_DOC_NOT_READY = 32;
   public static final byte STS0_PRINTER_LEFT_HOME = 2;
   public static final byte STS0_PRINTER_DOCUMENT_ERROR = 64;
   public static final byte STS6_PRINTER_DEV_INFO_PRESENT = 1;
   public static final byte STS6_PRINTER_IPL_END = 4;
   public static final byte STS6_EC_LEVEL_RESPONSE = 8;
   static Rs485FiscalPrinterInfoHelper instance = null;
   int country = 0;
   int version = 0;
   int ecLevel = 0;
   boolean powerInterrupted = false;

   private Rs485FiscalPrinterInfoHelper() {
   }

   public static Rs485FiscalPrinterInfoHelper getInstance() {
      if (instance == null) {
         instance = new Rs485FiscalPrinterInfoHelper();
      }

      return instance;
   }

   public boolean hasFiscalData(byte[] data) {
      return data.length > 9;
   }

   public byte[] getFiscalData(byte[] data) {
      byte[] fiscalData = new byte[data.length - 9];
      System.arraycopy(data, 9, fiscalData, 0, data.length - 9);
      return fiscalData;
   }

   public boolean isFinalStatus(byte[] data) {
      return (data[6] & 64) == 0 && !this.isAsynchronousStatus(data) && (data[6] & 2) == 0;
   }

   public boolean isAsynchronousStatus(byte[] data) {
      return (data[6] & 32) != 0;
   }

   public boolean isSuccessfulCommand(byte[] data) {
      return this.getReturnCode(data) == 67 || (data[6] & 8) == 8;
   }

   public int getCountry() {
      return this.country;
   }

   public boolean isPowerInterrupted() {
      return this.powerInterrupted;
   }

   public int getVersion() {
      return this.version;
   }

   public int getECLevel() {
      return this.ecLevel;
   }

   public boolean getCoverOpen(byte[] data) {
      return (data[1] & 16) != 0;
   }

   public boolean isValidPacket(byte[] data) {
      return data == null ? false : data.length >= 9;
   }

   public int getReturnCode(byte[] data) {
      return data[8];
   }

   public int getErrorCode(byte[] data) {
      int rtnsts = 0;
      if ((data[1] & 16) != 0 || (data[1] & 16) != 0) {
         rtnsts |= 1;
      }

      if ((data[0] & 2) != 0) {
         rtnsts |= 2;
      }

      if ((data[1] & 64) != 0) {
         rtnsts |= 8;
      }

      if ((data[2] & 4) != 0) {
         rtnsts |= 16;
      }

      if ((data[1] & 32) != 0) {
         rtnsts |= 32;
         rtnsts |= 128;
      }

      if ((data[0] & 2) != 0) {
         rtnsts |= 64;
      }

      if ((data[0] & 64) != 0) {
         rtnsts |= 512;
      }

      return rtnsts;
   }

   public void setFiscalInformation(byte[] data) {
      if (this.isValidPacket(data)) {
         this.country = data[7] & 31;
         this.powerInterrupted = (data[6] & 16) != 0;
         this.version = data[7] & 96;
         if (this.isDeviceInfo(data)) {
            this.ecLevel = data[13];
         }
      }
   }

   public boolean isIPLEnd(byte[] data) {
      return (data[6] & 4) == 0;
   }

   public boolean isDeviceInfo(byte[] data) {
      return (data[6] & 1) == 1;
   }

   public boolean getRecEmpty(byte[] data) {
      return (data[1] & 16) != 0;
   }

   public boolean getSlpEmpty(byte[] data) {
      return (data[1] & 16) != 0;
   }
}
