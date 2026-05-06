package com.ibm.posj.printer.ibm4689;

import com.ibm.posj.printer.DefaultImageStreamProducer;

public class Image4689StreamProducer extends DefaultImageStreamProducer {
   public short getNextByteForStation(byte station) {
      return this.getNextByteForThermal();
   }

   public short getNextByteForThermal() {
      int bByte = 0;
      if (this.row >= this.height) {
         bByte = 0;
         this.col += 8L;
         if (this.col >= this.width) {
            if (this.row % 8L == 7L) {
               this.streamDone = true;
            } else {
               this.col = 0L;
               this.row++;
            }
         }
      } else {
         for (int i = 0; i < 8; i++) {
            int bPixel = this.pixels[(int)(this.row * this.width + this.col)];
            byte var4;
            if (bPixel == -1) {
               var4 = 1;
            } else {
               var4 = 0;
            }

            bByte = bByte << 1 | var4;
            this.col++;
            if (this.col == this.width) {
               bByte <<= 7 - i;
               i = 8;
               if (this.row == this.height - 1L && this.row % 8L == 7L) {
                  this.streamDone = true;
               } else {
                  this.col = 0L;
                  this.row++;
               }
            }
         }
      }

      return (short)bByte;
   }
}
