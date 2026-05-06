package com.ibm.posj.bus;

public interface IBM4610PrinterHandleImp extends IBMPrinterComposite {
   byte getFeatureByte1();

   byte getFeatureByte2();

   public static class PrinterInfo {
      private byte id;
      private byte type;

      public void setType(byte type) {
         this.type = type;
      }

      public byte getType() {
         return this.type;
      }

      public void setId(byte id) {
         this.id = id;
      }

      public byte getId() {
         return this.id;
      }
   }
}
