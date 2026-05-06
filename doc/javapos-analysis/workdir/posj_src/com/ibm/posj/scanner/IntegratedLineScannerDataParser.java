package com.ibm.posj.scanner;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.event.ScannerDataEvent;

public class IntegratedLineScannerDataParser implements IntegratedScannerDataParser {
   private byte[] barcodeData = null;
   private int barcodeType = 0;
   private int statusBytesLength = 0;
   protected Tracer tracer = TracerFactory.getInstance().createTracer("Scanner", "IntegratedLineScannerDataParser");
   public static final byte CODEBAR_TYPE_BYTE = 3;
   public static final byte DATA_START_INDEX = 4;

   public ScannerDataEvent parseData(byte[] data) {
      this.setBarcodeType(data);
      this.setBarcodeData(data);
      return new ScannerDataEvent(this, this.barcodeData, this.barcodeType);
   }

   private void setBarcodeData(byte[] data) {
      if (this.barcodeType == 120) {
         this.barcodeData = new byte[data.length - 4 - 3];
         System.arraycopy(data, 7, this.barcodeData, 0, data.length - 7);
      } else {
         this.barcodeData = new byte[data.length - 4];
         System.arraycopy(data, 4, this.barcodeData, 0, data.length - 4);
      }
   }

   private void setBarcodeType(byte[] data) {
      switch (data[3]) {
         case -1:
            if (data.length > 13) {
               this.barcodeType = 118;
            } else {
               this.barcodeType = 103;
            }
            break;
         case 42:
            this.barcodeType = 108;
            break;
         case 65:
            if (data.length > 17) {
               this.barcodeType = 111;
            } else {
               this.barcodeType = 101;
            }
            break;
         case 66:
            this.barcodeType = 107;
            break;
         case 67:
            this.barcodeType = 109;
            break;
         case 68:
            if (data[4] == 93 && data[5] == 67 && data[6] == 49) {
               this.barcodeType = 120;
            } else {
               this.barcodeType = 110;
            }
            break;
         case 69:
            if (data.length > 13) {
               this.barcodeType = 112;
            } else {
               this.barcodeType = 102;
            }
            break;
         case 70:
            if (data.length > 18) {
               this.barcodeType = 119;
            } else {
               this.barcodeType = 104;
            }
            break;
         case 72:
            this.barcodeType = 105;
            break;
         case 73:
            this.barcodeType = 106;
            break;
         case 82:
            this.barcodeType = 131;
            break;
         case 83:
            this.barcodeType = 132;
            break;
         default:
            this.barcodeType = 501;
      }
   }
}
