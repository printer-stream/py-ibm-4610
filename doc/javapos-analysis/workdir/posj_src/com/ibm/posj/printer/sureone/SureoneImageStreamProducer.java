package com.ibm.posj.printer.sureone;

import com.ibm.posj.printer.DefaultImageStreamProducer;

public class SureoneImageStreamProducer extends DefaultImageStreamProducer {
   public short getNextByteForStation(byte station) {
      return this.getNextByteForImpact();
   }

   public short getNextByteForImpact() {
      int bPixel = 0;
      int bByte = 0;

      for (int i = 8; i > 0; i--) {
         if (this.row >= this.height) {
            bPixel = 0;
         } else if (this.col >= this.width) {
            bPixel = 0;
         } else {
            bPixel = this.pixels[(int)(this.row * this.width + this.col)];
            bPixel = bPixel == -1 ? 1 : 0;
         }

         bByte |= bPixel << i;
         this.row++;
      }

      this.col++;
      if (this.col == this.width && this.row >= this.height) {
         this.streamDone = true;
      }

      if (this.col >= this.width && this.col % 8L == 0L) {
         this.col = 0L;
      } else {
         this.row -= 8L;
      }

      return (short)bByte;
   }
}
