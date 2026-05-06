package com.ibm.posj.printer;

import com.ibm.jutil.IntArrayCollector;

public class DefaultImageStreamProducer implements ImageStreamProducer {
   protected long row = 0L;
   protected long col = 0L;
   protected long width = 0L;
   protected long height = 0L;
   protected boolean streamDone = false;
   protected int[] pixels = null;
   protected static final int VAL_BYTE_LEN = 8;
   protected static final int VAL_PIXEL_OFF = 0;
   protected static final int VAL_PIXEL_ON = 1;
   protected static final int VAL_PIXEL_MASK = -1;
   protected static final int VAL_PIXEL_INIT = 0;
   protected static final int ISP_DEFAULT_ROW = 0;
   protected static final int ISP_DEFAULT_COL = 0;
   protected static final int ISP_DEFAULT_WIDTH = 0;
   protected static final int ISP_DEFAULT_HEIGHT = 0;
   protected static final boolean ISP_DEFAULT_DONE = false;

   public void setImageValues(long row, long col, long width, long height, int[] pixels) {
      this.row = row;
      this.col = col;
      this.width = width;
      this.height = height;
      this.pixels = pixels;
      this.streamDone = false;
   }

   public boolean streamDone() {
      return this.streamDone;
   }

   public short getNextByteForStation(byte station) {
      return station == 2 ? this.getNextByteForThermal() : this.getNextByteForImpact();
   }

   protected short getNextByteForThermal() {
      int bPixel = 0;
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
            bPixel = this.pixels[(int)(this.row * this.width + this.col)];
            bPixel = bPixel == -1 ? 1 : 0;
            bByte = bByte << 1 | bPixel;
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

   public short getNextByteForImpact() {
      int bPixel = 0;
      int bByte = 0;

      for (int i = 0; i < 8; i++) {
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

   public void freeISP() {
      IntArrayCollector.getCollector().collect(this.pixels);
   }
}
