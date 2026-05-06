package com.ibm.posj.event;

public class CheckScannerStatusEvent extends StatusEvent {
   private boolean booleanValue = false;
   private int docHeight = 0;
   private int docWidth = 0;
   private boolean FLASHerased = false;
   private byte[] statusBytes = null;
   private int numBytes = 0;
   public static final int CHK_SUE_SCANCOMPLETE = 11;

   public CheckScannerStatusEvent(Object o, int id, boolean value) {
      super(o, id);
      this.booleanValue = value;
   }

   public CheckScannerStatusEvent(Object o, int id, int w, int h) {
      super(o, id);
      this.docHeight = h;
      this.docWidth = w;
   }

   public boolean getValue() {
      return this.booleanValue;
   }

   public int getDocumentHeight() {
      return this.docHeight;
   }

   public int getDocumentWidth() {
      return this.docWidth;
   }

   public boolean wasFLASHerased() {
      return this.FLASHerased;
   }

   public byte[] getStatusBytes() {
      return this.statusBytes;
   }

   public int getNumBytes() {
      return this.numBytes;
   }

   protected void setValues(int cmdCode, byte[] info) {
      int temp = 0;
      switch (cmdCode) {
         case 1301:
            this.FLASHerased = false;
            this.numBytes = 0;
         case 1306:
         default:
            break;
         case 1311:
            this.docWidth = 0;
            this.docHeight = 0;
            this.FLASHerased = true;
            this.numBytes = 0;
            break;
         case 1312:
            this.numBytes = 0;

            for (int i = 0; i < 4; i++) {
               int var6 = 0;
               int temporal = info[8 + i];
               temporal <<= 8 * i;
               this.numBytes |= var6;
            }

            System.out.println("CheckScannerStatusEvent-->numBytes = " + this.numBytes);
      }
   }
}
