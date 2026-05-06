package com.ibm.posj.kbd;

public class SystemPOSKeyboardMap extends POSKeyboardMap {
   private static short[][] scancodeTable = new short[][]{
      {41, 126},
      {41, 96},
      {2, 49},
      {2, 33},
      {3, 50},
      {3, 64},
      {4, 51},
      {4, 35},
      {5, 52},
      {5, 36},
      {6, 53},
      {6, 37},
      {7, 54},
      {7, 94},
      {8, 55},
      {8, 38},
      {9, 56},
      {9, 42},
      {10, 57},
      {10, 40},
      {11, 48},
      {11, 41},
      {12, 45},
      {12, 95},
      {13, 43},
      {13, 61},
      {16, 81},
      {17, 87},
      {18, 69},
      {19, 82},
      {20, 84},
      {21, 89},
      {22, 85},
      {23, 73},
      {24, 79},
      {25, 80},
      {26, 123},
      {26, 91},
      {27, 125},
      {27, 93},
      {30, 65},
      {31, 83},
      {32, 68},
      {33, 70},
      {34, 71},
      {35, 72},
      {36, 74},
      {37, 75},
      {38, 76},
      {39, 58},
      {39, 59},
      {40, 34},
      {40, 39},
      {43, 124},
      {43, 92},
      {44, 90},
      {45, 88},
      {46, 67},
      {47, 86},
      {48, 66},
      {49, 78},
      {50, 77},
      {51, 60},
      {51, 44},
      {52, 62},
      {52, 46},
      {53, 63},
      {53, 47},
      {0, 0}
   };

   public SystemPOSKeyboardMap(String fileName) {
      super(fileName);
   }

   protected int convertToScancode(char inChar) {
      int s = inChar;
      if (Character.isLetter(inChar)) {
         inChar = Character.toUpperCase(inChar);
      }

      for (int i = 0; i < scancodeTable.length; i++) {
         if (scancodeTable[i][1] == inChar) {
            s = scancodeTable[i][0];
         }
      }

      return s;
   }
}
