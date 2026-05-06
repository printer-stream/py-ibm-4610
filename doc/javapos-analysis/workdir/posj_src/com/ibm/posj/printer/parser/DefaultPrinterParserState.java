package com.ibm.posj.printer.parser;

import java.util.HashMap;

public abstract class DefaultPrinterParserState implements PrinterParserState {
   private HashMap tabMap = new HashMap(3);
   private int recLineChars = 0;
   private int jrnLineChars = 0;
   private int slpLineChars = 0;
   private int recLineOffset = 0;
   private int jrnLineOffset = 0;
   private int slpLineOffset = 0;
   private int recLineWidth = 0;
   private int jrnLineWidth = 0;
   private int slpLineWidth = 0;
   private byte recAlignment = 0;
   private byte jrnAlignment = 0;
   private byte slpAlignment = 0;
   private byte recWideMagnification = 0;
   private byte jrnWideMagnification = 0;
   private byte slpWideMagnification = 0;
   private byte recHighMagnification = 0;
   private byte jrnHighMagnification = 0;
   private byte slpHighMagnification = 0;
   private int recMaxPrintChars = 0;
   private int jrnMaxPrintChars = 0;
   private int slpMaxPrintChars = 0;
   private int recLinesToPaperCut = 0;
   private float recConversionFactor = 0.0F;
   private float jrnConversionFactor = 0.0F;
   private float slpConversionFactor = 0.0F;
   private int recRotation = 0;
   private int slpRotation = 0;
   private int characterSet = 0;
   private boolean rec2Color = false;
   private boolean jrn2Color = false;
   private boolean slp2Color = false;
   private boolean recPageMode = false;
   private boolean jrnPageMode = false;
   private boolean slpPageMode = false;
   private int nrmLineFeed = 0;
   private byte lastAlignment = -1;
   public static final byte PTR_REC_DEFAULT_ALIGNMENT = 0;
   public static final byte PTR_JRN_DEFAULT_ALIGNMENT = 0;
   public static final byte PTR_SLP_DEFAULT_ALIGNMENT = 0;
   public static final byte PTR_REC_DEFAULT_MAGNIFICATION = 1;
   public static final byte PTR_JRN_DEFAULT_MAGNIFICATION = 1;
   public static final byte PTR_SLP_DEFAULT_MAGNIFICATION = 1;

   public DefaultPrinterParserState() {
      this.initParserState();
   }

   public void initParserState() {
      this.resetAlignment((byte)2);
      this.resetAlignment((byte)8);
      this.resetAlignment((byte)4);
      this.resetFontWideMagnification((byte)2);
      this.resetFontWideMagnification((byte)8);
      this.resetFontWideMagnification((byte)4);
      this.resetFontHighMagnification((byte)2);
      this.resetFontHighMagnification((byte)8);
      this.resetFontHighMagnification((byte)4);
   }

   public abstract int getPrinterStateType();

   public void setMaxPrintChars(byte station, int maxPrintChars) {
      if (station == 2) {
         this.recMaxPrintChars = maxPrintChars;
      } else if (station == 8) {
         this.jrnMaxPrintChars = maxPrintChars;
      } else if (station == 4) {
         this.slpMaxPrintChars = maxPrintChars;
      }
   }

   public void setLineChars(byte station, int lineChars) {
      if (station == 2) {
         this.recLineChars = lineChars;
      } else if (station == 8) {
         this.jrnLineChars = lineChars;
      } else if (station == 4) {
         this.slpLineChars = lineChars;
      }

      this.setMaxPrintChars(station, lineChars);
   }

   public void setLineOffset(byte station, int lineOffset) {
      if (station == 2) {
         this.recLineOffset = lineOffset;
      } else if (station == 8) {
         this.jrnLineOffset = lineOffset;
      } else if (station == 4) {
         this.slpLineOffset = lineOffset;
      }
   }

   public void setLineWidth(byte station, int lineWidth) {
      if (station == 2) {
         this.recLineWidth = lineWidth;
      } else if (station == 8) {
         this.jrnLineWidth = lineWidth;
      } else if (station == 4) {
         this.slpLineWidth = lineWidth;
      }
   }

   public void setAlignment(byte station, byte alignment) {
      if (station == 2) {
         this.recAlignment = alignment;
      } else if (station == 8) {
         this.jrnAlignment = alignment;
      } else if (station == 4) {
         this.slpAlignment = alignment;
      }
   }

   public void setFontWideMagnification(byte station, byte magnification) {
      if (station == 2) {
         this.recWideMagnification = magnification;
      } else if (station == 8) {
         this.jrnWideMagnification = magnification;
      } else if (station == 4) {
         this.slpWideMagnification = magnification;
      }
   }

   public void setFontHighMagnification(byte station, byte magnification) {
      if (station == 2) {
         this.recHighMagnification = magnification;
      } else if (station == 8) {
         this.jrnHighMagnification = magnification;
      } else if (station == 4) {
         this.slpHighMagnification = magnification;
      }
   }

   public void setCharacterSet(int characterSet) {
      this.characterSet = characterSet;
   }

   public int getCharacterSet() {
      return this.characterSet;
   }

   public int getMaxPrintChars(byte station) {
      int maxPrintChars = 0;
      if (station == 2) {
         maxPrintChars = this.recMaxPrintChars;
      } else if (station == 8) {
         maxPrintChars = this.jrnMaxPrintChars;
      } else if (station == 4) {
         maxPrintChars = this.slpMaxPrintChars;
      }

      return maxPrintChars;
   }

   public int getLineChars(byte station) {
      int lineChars = 0;
      if (station == 2) {
         lineChars = this.recLineChars;
      } else if (station == 8) {
         lineChars = this.jrnLineChars;
      } else if (station == 4) {
         lineChars = this.slpLineChars;
      }

      return lineChars;
   }

   public int getLineOffset(byte station) {
      int lineChars = 0;
      if (station == 2) {
         lineChars = this.recLineOffset;
      } else if (station == 8) {
         lineChars = this.jrnLineOffset;
      } else if (station == 4) {
         lineChars = this.slpLineOffset;
      }

      return lineChars;
   }

   public int getCharSize(byte station) {
      return this.getFontWideMagnification(station) * this.getLineWidth(station) / this.getLineChars(station);
   }

   public int getLineWidth(byte station) {
      int lineWidth = 0;
      if (station == 2) {
         lineWidth = this.recLineWidth;
      } else if (station == 8) {
         lineWidth = this.jrnLineWidth;
      } else if (station == 4) {
         lineWidth = this.slpLineWidth;
      }

      return lineWidth;
   }

   public byte getAlignment(byte station) {
      byte alignment = 0;
      if (station == 2) {
         alignment = this.recAlignment;
      } else if (station == 8) {
         alignment = this.jrnAlignment;
      } else if (station == 4) {
         alignment = this.slpAlignment;
      }

      return alignment;
   }

   public byte getFontWideMagnification(byte station) {
      byte magnification = 0;
      if (station == 2) {
         magnification = this.recWideMagnification;
      } else if (station == 8) {
         magnification = this.jrnWideMagnification;
      } else if (station == 4) {
         magnification = this.slpWideMagnification;
      }

      return magnification;
   }

   public byte getFontHighMagnification(byte station) {
      byte magnification = 0;
      if (station == 2) {
         magnification = this.recHighMagnification;
      } else if (station == 8) {
         magnification = this.jrnHighMagnification;
      } else if (station == 4) {
         magnification = this.slpHighMagnification;
      }

      return magnification;
   }

   public void resetFontWideMagnification(byte station) {
      if (station == 2) {
         this.recWideMagnification = 1;
      } else if (station == 8) {
         this.jrnWideMagnification = 1;
      } else if (station == 4) {
         this.slpWideMagnification = 1;
      }
   }

   public void resetFontHighMagnification(byte station) {
      if (station == 2) {
         this.recHighMagnification = 1;
      } else if (station == 8) {
         this.jrnHighMagnification = 1;
      } else if (station == 4) {
         this.slpHighMagnification = 1;
      }
   }

   public void resetAlignment(byte station) {
      if (station == 2) {
         this.setAlignment((byte)2, (byte)0);
      } else if (station == 8) {
         this.setAlignment((byte)8, (byte)0);
      } else if (station == 4) {
         this.setAlignment((byte)4, (byte)0);
      }
   }

   public void setRecLinesToPaperCut(int linesToPaperCut) {
      this.recLinesToPaperCut = linesToPaperCut;
   }

   public int getRecLinesToPaperCut() {
      return this.recLinesToPaperCut;
   }

   public void setConversionFactor(byte station, float factor) {
      if (station == 2) {
         this.recConversionFactor = factor;
      } else if (station == 8) {
         this.jrnConversionFactor = factor;
      } else if (station == 4) {
         this.slpConversionFactor = factor;
      }
   }

   public float getConversionFactor(byte station) {
      float factor = 0.0F;
      if (station == 2) {
         factor = this.recConversionFactor;
      } else if (station == 8) {
         factor = this.jrnConversionFactor;
      } else if (station == 4) {
         factor = this.slpConversionFactor;
      }

      return factor;
   }

   public void setRotation(byte station, int rotation) {
      if (station == 2) {
         this.recRotation = rotation;
      } else if (station == 4) {
         this.slpRotation = rotation;
      }
   }

   public int getRotation(byte station) {
      int rotation = 0;
      if (station == 2) {
         rotation = this.recRotation;
      } else if (station == 4) {
         rotation = this.slpRotation;
      }

      return rotation;
   }

   public void setCap2Color(byte station, boolean capColor) {
      if (station == 2) {
         this.rec2Color = capColor;
      } else if (station == 8) {
         this.jrn2Color = capColor;
      } else if (station == 4) {
         this.slp2Color = capColor;
      }
   }

   public boolean getCap2Color(byte station) {
      boolean color = false;
      if (station == 2) {
         color = this.rec2Color;
      } else if (station == 8) {
         color = this.jrn2Color;
      } else if (station == 4) {
         color = this.slp2Color;
      }

      return color;
   }

   public void setCurrentTabs(byte station, int[] tabs) {
      if (null != tabs && tabs.length > 0) {
         Byte bst = new Byte(station);
         int[] bk = null;
         if (!this.tabMap.containsKey(bst)) {
            bk = new int[tabs.length];
         } else {
            bk = (int[])this.tabMap.get(bst);
            if (bk.length != tabs.length) {
               bk = new int[tabs.length];
            }
         }

         for (int i = 0; i < tabs.length; i++) {
            bk[i] = tabs[i];
         }

         this.tabMap.put(bst, bk);
      }
   }

   public boolean areNewTabs(byte station, int[] tabs) {
      if (null != tabs && tabs.length > 0) {
         Byte bst = new Byte(station);
         boolean ret = false;
         if (this.tabMap.containsKey(bst)) {
            int[] t = (int[])this.tabMap.get(bst);
            if (tabs.length == t.length) {
               for (int i = t.length - 1; i >= 0; i--) {
                  if (t[i] != tabs[i]) {
                     ret = true;
                     break;
                  }
               }
            } else {
               ret = true;
            }
         } else {
            ret = true;
         }

         return ret;
      } else {
         return false;
      }
   }

   public void clearTabs() {
      for (int[] v : this.tabMap.values()) {
         v[0] = -1;
      }
   }

   public boolean isPageModeOn(byte station) {
      boolean pageMode = false;
      if (station == 2) {
         pageMode = this.recPageMode;
      } else if (station == 8) {
         pageMode = this.jrnPageMode;
      } else if (station == 4) {
         pageMode = this.slpPageMode;
      }

      return pageMode;
   }

   public void setPageModeState(byte station, boolean state) {
      if (station == 2) {
         this.recPageMode = state;
      } else if (station == 8) {
         this.jrnPageMode = state;
      } else if (station == 4) {
         this.slpPageMode = state;
      }
   }

   public void setNormalWithLFState(int state, byte alignment) {
      this.nrmLineFeed = state;
      this.lastAlignment = alignment;
   }

   public int getNormalWithLFState() {
      return this.nrmLineFeed;
   }

   public byte getLastAlignment() {
      return this.lastAlignment;
   }
}
