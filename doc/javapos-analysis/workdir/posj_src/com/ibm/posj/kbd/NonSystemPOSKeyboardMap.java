package com.ibm.posj.kbd;

public class NonSystemPOSKeyboardMap extends POSKeyboardMap {
   private static short[][] scancodeTable = new short[][]{
      {81, 126},
      {81, 96},
      {17, 49},
      {17, 33},
      {18, 50},
      {18, 64},
      {19, 51},
      {19, 35},
      {20, 52},
      {20, 36},
      {84, 53},
      {84, 37},
      {85, 54},
      {85, 94},
      {21, 55},
      {21, 38},
      {24, 56},
      {24, 42},
      {22, 57},
      {22, 40},
      {23, 48},
      {23, 41},
      {87, 45},
      {87, 95},
      {88, 43},
      {88, 61},
      {97, 81},
      {98, 87},
      {99, 69},
      {100, 82},
      {116, 84},
      {117, 89},
      {101, 85},
      {104, 73},
      {102, 79},
      {103, 80},
      {119, 123},
      {119, 91},
      {120, 125},
      {120, 93},
      {129, 65},
      {130, 83},
      {131, 68},
      {132, 70},
      {36, 71},
      {37, 72},
      {133, 74},
      {136, 75},
      {134, 76},
      {135, 58},
      {135, 59},
      {39, 34},
      {39, 39},
      {71, 124},
      {71, 92},
      {65, 90},
      {66, 88},
      {67, 67},
      {68, 86},
      {52, 66},
      {53, 78},
      {69, 77},
      {72, 60},
      {72, 44},
      {70, 62},
      {70, 46},
      {55, 63},
      {55, 47},
      {0, 0}
   };

   public NonSystemPOSKeyboardMap(String fileName) {
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
