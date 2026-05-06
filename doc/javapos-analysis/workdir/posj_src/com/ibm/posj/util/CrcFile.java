package com.ibm.posj.util;

import com.ibm.jutil.Util;

public class CrcFile {
   private int number;
   private int crc;
   private int station;
   private int width;
   private int alignment;
   private int tabAlignment;
   private String fileName;
   private int fileSize;
   static short[][] LookupTable = new short[][]{
      {0, 0},
      {17, 137},
      {35, 18},
      {50, 155},
      {70, 36},
      {87, 173},
      {101, 54},
      {116, 191},
      {140, 72},
      {157, 193},
      {175, 90},
      {190, 211},
      {202, 108},
      {219, 229},
      {233, 126},
      {248, 247},
      {16, 129},
      {1, 8},
      {51, 147},
      {34, 26},
      {86, 165},
      {71, 44},
      {117, 183},
      {100, 62},
      {156, 201},
      {141, 64},
      {191, 219},
      {174, 82},
      {218, 237},
      {203, 100},
      {249, 255},
      {232, 118},
      {33, 2},
      {48, 139},
      {2, 16},
      {19, 153},
      {103, 38},
      {118, 175},
      {68, 52},
      {85, 189},
      {173, 74},
      {188, 195},
      {142, 88},
      {159, 209},
      {235, 110},
      {250, 231},
      {200, 124},
      {217, 245},
      {49, 131},
      {32, 10},
      {18, 145},
      {3, 24},
      {119, 167},
      {102, 46},
      {84, 181},
      {69, 60},
      {189, 203},
      {172, 66},
      {158, 217},
      {143, 80},
      {251, 239},
      {234, 102},
      {216, 253},
      {201, 116},
      {66, 4},
      {83, 141},
      {97, 22},
      {112, 159},
      {4, 32},
      {21, 169},
      {39, 50},
      {54, 187},
      {206, 76},
      {223, 197},
      {237, 94},
      {252, 215},
      {136, 104},
      {153, 225},
      {171, 122},
      {186, 243},
      {82, 133},
      {67, 12},
      {113, 151},
      {96, 30},
      {20, 161},
      {5, 40},
      {55, 179},
      {38, 58},
      {222, 205},
      {207, 68},
      {253, 223},
      {236, 86},
      {152, 233},
      {137, 96},
      {187, 251},
      {170, 114},
      {99, 6},
      {114, 143},
      {64, 20},
      {81, 157},
      {37, 34},
      {52, 171},
      {6, 48},
      {23, 185},
      {239, 78},
      {254, 199},
      {204, 92},
      {221, 213},
      {169, 106},
      {184, 227},
      {138, 120},
      {155, 241},
      {115, 135},
      {98, 14},
      {80, 149},
      {65, 28},
      {53, 163},
      {36, 42},
      {22, 177},
      {7, 56},
      {255, 207},
      {238, 70},
      {220, 221},
      {205, 84},
      {185, 235},
      {168, 98},
      {154, 249},
      {139, 112},
      {132, 8},
      {149, 129},
      {167, 26},
      {182, 147},
      {194, 44},
      {211, 165},
      {225, 62},
      {240, 183},
      {8, 64},
      {25, 201},
      {43, 82},
      {58, 219},
      {78, 100},
      {95, 237},
      {109, 118},
      {124, 255},
      {148, 137},
      {133, 0},
      {183, 155},
      {166, 18},
      {210, 173},
      {195, 36},
      {241, 191},
      {224, 54},
      {24, 193},
      {9, 72},
      {59, 211},
      {42, 90},
      {94, 229},
      {79, 108},
      {125, 247},
      {108, 126},
      {165, 10},
      {180, 131},
      {134, 24},
      {151, 145},
      {227, 46},
      {242, 167},
      {192, 60},
      {209, 181},
      {41, 66},
      {56, 203},
      {10, 80},
      {27, 217},
      {111, 102},
      {126, 239},
      {76, 116},
      {93, 253},
      {181, 139},
      {164, 2},
      {150, 153},
      {135, 16},
      {243, 175},
      {226, 38},
      {208, 189},
      {193, 52},
      {57, 195},
      {40, 74},
      {26, 209},
      {11, 88},
      {127, 231},
      {110, 110},
      {92, 245},
      {77, 124},
      {198, 12},
      {215, 133},
      {229, 30},
      {244, 151},
      {128, 40},
      {145, 161},
      {163, 58},
      {178, 179},
      {74, 68},
      {91, 205},
      {105, 86},
      {120, 223},
      {12, 96},
      {29, 233},
      {47, 114},
      {62, 251},
      {214, 141},
      {199, 4},
      {245, 159},
      {228, 22},
      {144, 169},
      {129, 32},
      {179, 187},
      {162, 50},
      {90, 197},
      {75, 76},
      {121, 215},
      {104, 94},
      {28, 225},
      {13, 104},
      {63, 243},
      {46, 122},
      {231, 14},
      {246, 135},
      {196, 28},
      {213, 149},
      {161, 42},
      {176, 163},
      {130, 56},
      {147, 177},
      {107, 70},
      {122, 207},
      {72, 84},
      {89, 221},
      {45, 98},
      {60, 235},
      {14, 112},
      {31, 249},
      {247, 143},
      {230, 6},
      {212, 157},
      {197, 20},
      {177, 171},
      {160, 34},
      {146, 185},
      {131, 48},
      {123, 199},
      {106, 78},
      {88, 213},
      {73, 92},
      {61, 227},
      {44, 106},
      {30, 241},
      {15, 120}
   };

   public CrcFile(int n, int stat, int w, int align, int tabAlign, byte[] dataStream, String fn) {
      this.number = n;
      this.crc = calculateCRC(dataStream);
      this.station = stat;
      this.width = w;
      this.alignment = align;
      this.tabAlignment = tabAlign;
      this.fileName = fn.trim();
      this.fileSize = dataStream.length;
   }

   public CrcFile() {
   }

   public boolean equals(Object obj) {
      if (obj instanceof CrcFile && obj != null) {
         CrcFile crcFile = (CrcFile)obj;
         return this.number == crcFile.number
            && this.crc == crcFile.crc
            && this.station == crcFile.station
            && this.width == crcFile.width
            && this.alignment == crcFile.alignment
            && this.tabAlignment == crcFile.tabAlignment
            && this.fileSize == crcFile.fileSize;
      } else {
         return false;
      }
   }

   public String toString() {
      return "Number="
         + this.number
         + ","
         + "Crc="
         + this.crc
         + ","
         + "Station="
         + this.station
         + ","
         + "Width="
         + this.width
         + ","
         + "Alignment="
         + this.alignment
         + ","
         + "TabAlignment="
         + this.tabAlignment
         + ","
         + "FileName="
         + this.fileName
         + ","
         + "FileSize="
         + this.fileSize;
   }

   public int hashCode() {
      return this.toString().hashCode();
   }

   public int getAlignment() {
      return this.alignment;
   }

   public void setAlignment(int alignment) {
      this.alignment = alignment;
   }

   public int getTabAlignment() {
      return this.tabAlignment;
   }

   public void setTabAlignment(int tabAlignment) {
      this.tabAlignment = tabAlignment;
   }

   public int getCrc() {
      return this.crc;
   }

   public void setCrc(int crc) {
      this.crc = crc;
   }

   public String getFileName() {
      return this.fileName;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName.trim();
   }

   public int getNumber() {
      return this.number;
   }

   public void setNumber(int number) {
      this.number = number;
   }

   public int getStation() {
      return this.station;
   }

   public void setStation(int station) {
      this.station = station;
   }

   public int getWidth() {
      return this.width;
   }

   public void setWidth(int width) {
      this.width = width;
   }

   public int getFileSize() {
      return this.fileSize;
   }

   public void setFileSize(int fileSize) {
      this.fileSize = fileSize;
   }

   static int calculateCRC(byte[] dataArray) {
      if (dataArray == null) {
         return 0;
      } else {
         short actualX = -1;
         short dataX = 0;

         for (int i = 0; i < dataArray.length || i + 1 < dataArray.length; i++) {
            dataX = Util.toShort((byte)(dataArray[i] & 239), (byte)(dataArray[i++] & 239));
            dataX = (short)(dataX ^ actualX);
            dataX = (short)(255 & dataX);
            actualX = (short)(actualX >> 8);
            actualX = (short)(255 & actualX);
            int index = (short)(255 & dataX);
            dataX = Util.toShort((byte)LookupTable[index][1], (byte)LookupTable[index][0]);
            actualX = (short)(actualX ^ dataX);
         }

         return (char)(~(actualX << 8 | actualX));
      }
   }
}
