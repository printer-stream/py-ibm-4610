package com.ibm.posj.util;

import java.awt.image.RGBImageFilter;

public class PrintBitmapBWImageFilter extends RGBImageFilter {
   public PrintBitmapBWImageFilter() {
      this.canFilterIndexColorModel = true;
   }

   public int filterRGB(int x, int y, int rgb) {
      int red = (rgb & 0xFF0000) >> 16;
      int green = (rgb & 0xFF00) >> 8;
      int blue = rgb & 0xFF;
      int bw = red + green + blue;
      return bw > 382 ? -16777216 : -1;
   }
}
