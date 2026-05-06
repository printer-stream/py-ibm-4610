package com.ibm.posj.scanner;

import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.ScannerHandleConst;
import com.ibm.posj.event.ScannerDataEvent;

public class ScannerDataParser implements ScannerHandleConst {
   private byte[] barcodeData = new byte[0];
   private int barcodeType = 0;
   private int statusBytesLength = 0;
   protected Tracer tracer = TracerFactory.getInstance().createTracer("Scanner", "ScannerDataParser");
   public static final byte ONE_BYTE_TYPE_LENGTH = 1;
   public static final byte TWO_BYTE_TYPE_LENGTH = 2;
   public static final byte THREE_BYTE_TYPE_LENGTH = 3;
   public static final String COMMAND_TIMEOUT_PROP_NAME = "com.ibm.posj.bus.ScannerHandleImp.COMMAND_TIMEOUT";

   public ScannerDataParser(int statusBytesLength) {
      this.statusBytesLength = statusBytesLength;
   }

   public ScannerDataEvent parseData(byte[] data) {
      if (this.isOneByteTypeLength(data)) {
         return new ScannerDataEvent(this, this.barcodeData, this.barcodeType);
      } else if (this.isTwoByteTypeLength(data)) {
         return new ScannerDataEvent(this, this.barcodeData, this.barcodeType);
      } else if (this.isThreeByteTypeLength(data)) {
         return new ScannerDataEvent(this, this.barcodeData, this.barcodeType);
      } else {
         throw new IllegalArgumentException("Unable to identify bar code type: " + Util.toFormatedHexString(data));
      }
   }

   public boolean isOneByteTypeLength(byte[] data) {
      switch (data[data.length - 1]) {
         case 10:
            this.barcodeType = 102;
            break;
         case 12:
            this.barcodeType = 103;
            break;
         case 13:
            this.barcodeType = 101;
            break;
         case 22:
            this.barcodeType = 104;
            break;
         default:
            return false;
      }

      this.barcodeData = this.assignData(data, (byte)1);
      this.tracer.println("barcode is ONE Byte Type length");
      return true;
   }

   public boolean isTwoByteTypeLength(byte[] data) {
      if (data[data.length - 2] != 0) {
         return false;
      } else {
         switch (data[data.length - 1]) {
            case 17:
               this.barcodeType = 113;
               break;
            case 18:
               this.barcodeType = 114;
               break;
            case 19:
            case 21:
            case 22:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            default:
               return false;
            case 20:
               this.barcodeType = 115;
               break;
            case 23:
               this.barcodeType = 116;
               break;
            case 29:
               this.barcodeType = 117;
         }

         this.barcodeData = this.assignData(data, (byte)2);
         this.tracer.println("barcode is TWO Bytes Type length");
         return true;
      }
   }

   public boolean isThreeByteTypeLength(byte[] data) {
      if (data[data.length - 1] != 11 && data[data.length - 3] != 0) {
         return false;
      } else {
         switch (data[data.length - 2]) {
            case -1:
            case 32:
            case 33:
            case 34:
            case 35:
               this.barcodeType = 0;
               break;
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 11:
            case 12:
            case 15:
            case 16:
            case 26:
            case 27:
            case 28:
            case 30:
            case 31:
            case 36:
            default:
               return false;
            case 10:
               this.barcodeType = 108;
               break;
            case 13:
               this.barcodeType = 106;
               break;
            case 14:
               this.barcodeType = 107;
               break;
            case 17:
            case 22:
               this.barcodeType = 111;
               break;
            case 18:
            case 20:
               this.barcodeType = 112;
               break;
            case 19:
            case 21:
               this.barcodeType = 119;
               break;
            case 23:
            case 29:
               this.barcodeType = 118;
               break;
            case 24:
               this.barcodeType = 110;
               break;
            case 25:
               this.barcodeType = 109;
               break;
            case 37:
               this.barcodeType = 120;
         }

         this.barcodeData = this.assignData(data, (byte)3);
         this.tracer.println("barcode is THREE Bytes Type length");
         return true;
      }
   }

   public byte[] assignData(byte[] data, byte typeLength) {
      int barcodeDataLength = 0;
      barcodeDataLength = data.length - typeLength - this.statusBytesLength;
      byte[] barcodeData = new byte[barcodeDataLength];
      System.arraycopy(data, this.statusBytesLength, barcodeData, 0, barcodeDataLength);
      if (barcodeData.length > 0 && this.isDecimalFormat(barcodeData)) {
         this.convertToAsciiFormat(barcodeData);
      }

      return barcodeData;
   }

   public void convertToAsciiFormat(byte[] byteArray) {
      for (int i = 0; i < byteArray.length; i++) {
         byteArray[i] = (byte)(byteArray[i] | 48);
      }
   }

   public boolean isDecimalFormat(byte[] byteArray) {
      return byteArray[0] <= 9;
   }
}
