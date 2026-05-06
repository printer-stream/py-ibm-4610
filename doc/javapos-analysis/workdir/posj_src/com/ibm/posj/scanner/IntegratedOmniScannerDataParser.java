package com.ibm.posj.scanner;

import com.ibm.jutil.Util;
import com.ibm.posj.event.ScannerDataEvent;

public class IntegratedOmniScannerDataParser implements IntegratedScannerDataParser {
   private byte[] barcodeData = null;
   private int barcodeType = 0;
   private static final int BARCODE_TYPE_BYTE_POS = 0;
   private static final int FIRST_DATA_BYTE_POS = 1;
   private static final int S_APPEND_LENGTH_OF_FIRST_PACKET_BYTE_POS = 4;
   private static final int S_APPEND_BARCODE_TYPE_BYTE_POS = 1;
   private static final int S_APPEND_MAIN_BARCODE_DATA_START_POS = 6;

   public ScannerDataEvent parseData(byte[] data) {
      if (data[0] == -103) {
         data = this.parseStructuredAppend(data);
      }

      this.setBarcodeType(data);
      this.setBarcodeData(data);
      return new ScannerDataEvent(this, this.barcodeData, this.barcodeType);
   }

   private void setBarcodeType(byte[] data) {
      switch (data[0]) {
         case -120:
         case 72:
            this.barcodeType = 111;
            break;
         case -119:
         case 73:
            this.barcodeType = 112;
            break;
         case -118:
         case 74:
            this.barcodeType = 118;
            break;
         case -117:
         case 75:
            this.barcodeType = 119;
            break;
         case 1:
            this.barcodeType = 108;
            break;
         case 2:
            this.barcodeType = 107;
            break;
         case 3:
            this.barcodeType = 110;
            break;
         case 4:
            this.barcodeType = 105;
            break;
         case 6:
            this.barcodeType = 106;
            break;
         case 7:
            this.barcodeType = 109;
            break;
         case 8:
            this.barcodeType = 101;
            break;
         case 9:
            this.barcodeType = 102;
            break;
         case 10:
            this.barcodeType = 103;
            break;
         case 11:
            this.barcodeType = 104;
            break;
         case 15:
            this.barcodeType = 120;
            break;
         case 17:
            this.barcodeType = 201;
            break;
         case 48:
            this.barcodeType = 131;
            break;
         case 50:
            this.barcodeType = 132;
            break;
         default:
            this.barcodeType = 501;
      }
   }

   private void setBarcodeData(byte[] data) {
      byte[] aux = new byte[data.length - 1];
      System.arraycopy(data, 1, aux, 0, aux.length);
      this.barcodeData = aux;
   }

   private byte[] parseStructuredAppend(byte[] data) {
      int mainBarCodeDataLength = Util.toInt(data[4], data[5]);
      byte[] newData = new byte[mainBarCodeDataLength + 1];
      newData[0] = data[1];
      System.arraycopy(data, 6, newData, 1, mainBarCodeDataLength);
      return newData;
   }
}
