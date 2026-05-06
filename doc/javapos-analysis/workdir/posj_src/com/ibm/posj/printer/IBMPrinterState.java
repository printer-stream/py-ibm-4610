package com.ibm.posj.printer;

import com.ibm.jutil.AsyncBitSet;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.logging.LogHelper;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.parser.PrinterParser;
import java.util.LinkedList;
import java.util.List;

public abstract class IBMPrinterState implements PrinterHandleState {
   private boolean transaction = false;
   private boolean busy = false;
   private boolean wasMod4 = false;
   protected List listens = new LinkedList();
   protected LogHelper logger = null;
   protected AsyncBitSet slpbools = new AsyncBitSet(8);
   protected AsyncBitSet recbools = new AsyncBitSet(8);
   protected AsyncBitSet jrnbools = new AsyncBitSet(8);
   protected static final int CAP_UNDERLINE = 0;
   protected static final int CAP_BOLD = 1;
   protected static final int CAP_DOUBLEHIGH = 2;
   protected static final int CAP_DOUBLEWIDE = 3;
   protected static final int CAP_REVERSEVIDEO = 4;
   protected static final int CAP_REVERSEFEED = 5;
   protected static final int CAP_SCALEVERT = 6;
   protected static final int CAP_SCALEHORIZ = 7;
   protected static final int END = 8;
   protected byte[][] fontTable = (byte[][])null;
   protected byte recCharHeight = 0;
   protected byte jrnCharHeight = 0;
   protected byte slpCharHeight = 0;
   protected int[] alignmentList = null;
   protected int[] textPositionList = null;
   protected int recBarCodeMaxHeight = 0;
   protected int recBarCodeMaxWidth = 0;
   protected int slpBarCodeMaxHeight = 0;
   protected int slpBarCodeMaxWidth = 0;
   protected int recBarCodeMinHorSize = 0;
   protected int recBarCodeMaxHorSize = 0;
   protected int slpBarCodeMinHorSize = 0;
   protected int slpBarCodeMaxHorSize = 0;
   protected byte currStation = 2;
   protected int maxCmdSize = 0;
   protected boolean dbMode = false;
   protected boolean dbcs = false;
   protected boolean micrPresent = false;
   protected boolean flipperPresent = false;
   private int recStationWidth = 0;
   private int slpStationWidth = 0;
   private int slpLandStationWidth = 2000;
   protected int jrnStationWidth = 0;
   protected int printerType = 0;
   protected int printerID = 0;
   protected int ecLevel = 0;
   protected String serialNumber = "";
   protected int recMaxPrintBitmapSize = 0;
   protected int slpMaxPrintBitmapSize = 0;
   protected int recBitmapMaxHeight = 0;
   protected int slpBitmapMaxHeight = 0;
   protected int recFeedStepsPerInch = 0;
   protected int slpFeedStepsPerInch = 0;
   protected int jrnFeedStepsPerInch = 0;
   protected int slpLandscapeSPI = 0;
   protected byte recAlignment = 0;
   protected byte slpAlignment = 0;
   protected byte jrnAlignment = 0;
   protected ByteBuffer mctArea = null;
   protected ByteBuffer udf1Thermal = null;
   protected ByteBuffer udf2Thermal = null;
   protected ByteBuffer udf3Thermal = null;
   protected ByteBuffer udf4Thermal = null;
   protected ByteBuffer udf1Impact = null;
   protected ByteBuffer udf2Impact = null;
   protected boolean mctValue = false;
   protected boolean capConcurrentJrnRec = false;
   protected boolean capConcurrentJrnSlp = false;
   protected boolean capConcurrentRecSlp = false;
   protected boolean capCoverSensor = false;
   protected boolean capRecPartialLine = false;
   protected boolean capSlpPartialLine = false;
   protected boolean capJrnPartialLine = false;
   protected boolean capRec2Color = false;
   protected boolean capSlp2Color = false;
   protected boolean capJrn2Color = false;
   protected boolean capRecDwideDhigh = false;
   protected boolean capSlpDwideDhigh = false;
   protected boolean capJrnDwideDhigh = false;
   protected boolean capRecEmptySensor = false;
   protected boolean capSlpEmptySensor = false;
   protected boolean capJrnEmptySensor = false;
   protected boolean capRecNearEndSensor = false;
   protected boolean capSlpNearEndSensor = false;
   protected boolean capJrnNearEndSensor = false;
   protected boolean capRecBarCode = false;
   protected boolean capSlpBarCode = false;
   protected boolean capJrnBarCode = false;
   protected boolean capRecBitmap = false;
   protected boolean capSlpBitmap = false;
   protected boolean capJrnBitmap = false;
   protected boolean capRecLeft90 = false;
   protected boolean capSlpLeft90 = false;
   protected boolean capJrnLeft90 = false;
   protected boolean capRecRight90 = false;
   protected boolean capSlpRight90 = false;
   protected boolean capJrnRight90 = false;
   protected boolean capRecRotate180 = false;
   protected boolean capSlpRotate180 = false;
   protected boolean capJrnRotate180 = false;
   protected boolean capRecPapercut = false;
   protected boolean capSlpPapercut = false;
   protected boolean capJrnPapercut = false;
   protected boolean capRecStamp = false;
   protected boolean capSlpStamp = false;
   protected boolean capJrnStamp = false;
   protected boolean capRecScaleHorizontally = false;
   protected boolean capSlpScaleHorizontally = false;
   protected boolean capJrnScaleHorizontally = false;
   protected boolean capRecScaleVertically = false;
   protected boolean capSlpScaleVertically = false;
   protected boolean capJrnScaleVertically = false;
   protected boolean capFullslip = false;
   protected boolean capTransaction = false;
   protected byte recMaxBitmapNumber = 0;
   protected byte slpMaxBitmapNumber = 0;
   protected byte jrnMaxBitmapNumber = 0;
   protected byte recMaxLogoNumber = 0;
   protected byte slpMaxLogoNumber = 0;
   protected byte jrnMaxLogoNumber = 0;
   protected byte recMaxColorNumber = 0;
   protected byte slpMaxColorNumber = 0;
   protected byte jrnMaxColorNumber = 0;
   protected int recMaxUnderlineNumber = 0;
   protected int slpMaxUnderlineNumber = 0;
   protected int jrnMaxUnderlineNumber = 0;
   protected byte recMaxScaleHorizontalNumber = 0;
   protected byte slpMaxScaleHorizontalNumber = 0;
   protected byte jrnMaxScaleHorizontalNumber = 0;
   protected byte recMaxScaleVerticalNumber = 0;
   protected byte slpMaxScaleVerticalNumber = 0;
   protected byte jrnMaxScaleVerticalNumber = 0;
   protected int recMaxLinesToFeed = 0;
   protected int slpMaxLinesToFeed = 0;
   protected int jrnMaxLinesToFeed = 0;
   protected int recSidewaysLineWidth = 0;
   protected int slpSidewaysLineWidth = 0;
   protected int jrnSidewaysLineWidth = 0;
   protected int recSidewaysMaxChars = 0;
   protected int slpSidewaysMaxChars = 0;
   protected int jrnSidewaysMaxChars = 0;
   protected int recSidewaysMaxLines = 0;
   protected int slpSidewaysMaxLines = 0;
   protected int jrnSidewaysMaxLines = 0;
   protected byte slpPrintSide = 1;
   public static final byte SIDE1 = 1;
   public static final byte SIDE2 = 2;
   private final int PRT_BMP_SIZE_RATION = 8;
   private final int PRT_BMP_SIZE_MIN = 1;
   POSPrinterCmd.SetLogoCmd[] logoLoaded = null;
   POSPrinterCmd.SetBitmapCmd[] bitmapLoaded = null;

   public IBMPrinterState(LogHelper logger) {
      this.logger = logger;
   }

   public abstract PrinterParser getParser();

   public byte getCurrentStation() {
      return this.currStation;
   }

   public boolean isFormerMod4() {
      return this.wasMod4;
   }

   public void setFormerMod4(boolean b) {
      this.wasMod4 = b;
   }

   public void setCurrentStation(byte station) {
      this.currStation = station;
   }

   public abstract void setCharWidth(byte var1, byte var2);

   public void setCharHeight(byte station, byte dots) {
      switch (station) {
         case 2:
            this.recCharHeight = dots;
            break;
         case 4:
         case 20:
            this.slpCharHeight = dots;
            break;
         case 8:
            this.jrnCharHeight = dots;
      }
   }

   public synchronized byte getSlpPrintSide() {
      return this.slpPrintSide;
   }

   public synchronized void setSlpPrintSide(byte selectedSide) {
      this.slpPrintSide = selectedSide;
   }

   public byte getCharHeight(byte station) {
      switch (station) {
         case 2:
            return this.recCharHeight;
         case 4:
         case 20:
            return this.slpCharHeight;
         case 8:
            return this.jrnCharHeight;
         default:
            return 0;
      }
   }

   public abstract void setSpacingWidth(byte var1, int var2);

   public void setSetBitmapCmd(POSPrinterCmd.SetBitmapCmd setBmpCmd) {
      if (this.bitmapLoaded == null) {
         this.bitmapLoaded = new POSPrinterCmd.SetBitmapCmd[this.recMaxBitmapNumber];
         int i = 0;

         while (i < this.recMaxBitmapNumber) {
            this.bitmapLoaded[i++] = null;
         }
      }

      this.bitmapLoaded[setBmpCmd.getBitmapNo() - 1] = setBmpCmd;
   }

   public void cleanBitmaps(byte station) {
      if (this.bitmapLoaded != null) {
         int i = 0;

         while (i < this.recMaxBitmapNumber) {
            this.bitmapLoaded[i++] = null;
         }
      }
   }

   public void deleteBitmap(byte location, byte station) {
      if (this.bitmapLoaded != null) {
         this.bitmapLoaded[location - 1] = null;
      }
   }

   public POSPrinterCmd.SetBitmapCmd getSetBitmapCmd(byte location, byte station) {
      POSPrinterCmd.SetBitmapCmd setBitmapCmd = null;
      if (this.bitmapLoaded != null && location < this.recMaxBitmapNumber) {
         setBitmapCmd = this.bitmapLoaded[location];
      }

      return setBitmapCmd;
   }

   public void setSetLogoCmd(POSPrinterCmd.SetLogoCmd setLogoCmd) {
      if (setLogoCmd.getLocation() != 100) {
         if (this.logoLoaded == null) {
            this.logoLoaded = new POSPrinterCmd.SetLogoCmd[this.recMaxLogoNumber];
            int i = 0;

            while (i < this.recMaxLogoNumber) {
               this.logoLoaded[i++] = null;
            }
         }

         this.logoLoaded[setLogoCmd.getLocation() - 1] = setLogoCmd;
      }
   }

   public void cleanLogos() {
      if (this.logoLoaded != null) {
         int i = 0;

         while (i < this.recMaxLogoNumber) {
            this.logoLoaded[i++] = null;
         }
      }
   }

   public void deleteLogo(byte location) {
      if (this.logoLoaded != null) {
         this.logoLoaded[location - 1] = null;
      }
   }

   public POSPrinterCmd.SetLogoCmd getSetLogoCmd(byte location) {
      POSPrinterCmd.SetLogoCmd setLogoCmd = null;
      if (this.logoLoaded != null && location <= this.recMaxLogoNumber) {
         setLogoCmd = this.logoLoaded[location - 1];
      }

      return setLogoCmd;
   }

   public POSPrinterCmd.SetBitmapCmd getTopBitmapCmd(byte station) {
      int i = 0;

      while (i < this.recMaxBitmapNumber && this.bitmapLoaded[i] == null) {
         i++;
      }

      return i < this.recMaxBitmapNumber ? this.bitmapLoaded[i] : null;
   }

   public abstract int getMicronsPerStep(byte var1);

   public boolean isMICRPresent() {
      return this.micrPresent;
   }

   public void setMICRPresent(boolean present) {
      this.micrPresent = present;
      if (!present) {
         this.logger.addLogEntry(3006, "POSPrinter");
      }
   }

   public boolean isFlipperPresent() {
      return this.flipperPresent;
   }

   public void setFlipperPresent(boolean present) {
      this.flipperPresent = present;
   }

   public boolean isPrinterBusy() {
      return this.busy;
   }

   public void setPrinterBusy(boolean isBusy) {
      this.busy = isBusy;
   }

   public int getAlignment(int alignment) throws IllegalArgumentException {
      return this.mapParameter(this.alignmentList, alignment);
   }

   public int getTextPosition(int textPosition) throws IllegalArgumentException {
      return this.mapParameter(this.textPositionList, textPosition);
   }

   public int getBarCodeHeight(int height, int station) {
      return height;
   }

   public int getBarCodeWidth(int width, byte station) {
      int lineWith = this.getStationWidth(station);
      int minHorSize = this.getBarCodeMinHorSize(station);
      int maxHorSize = this.getBarCodeMaxHorSize(station);
      int magnificationScale = maxHorSize - minHorSize + 1;
      int roundFactor = lineWith / (magnificationScale * 2);
      int widthMagnification = (width + roundFactor) * magnificationScale / lineWith + minHorSize - 1;
      if (widthMagnification < minHorSize) {
         widthMagnification = minHorSize;
      } else if (widthMagnification > maxHorSize) {
         widthMagnification = maxHorSize;
      }

      return widthMagnification;
   }

   public int getStationWidth(byte station) {
      switch (station) {
         case 2:
            return this.recStationWidth;
         case 4:
            return this.slpStationWidth;
         case 8:
            return this.jrnStationWidth;
         case 20:
            return this.slpLandStationWidth;
         default:
            return 0;
      }
   }

   public int getBarCodeMax(int station) {
      return station == 2 ? this.recBarCodeMinHorSize : this.slpBarCodeMinHorSize;
   }

   public int getBarCodeMaxHeight(int station) {
      return station == 2 ? this.recBarCodeMaxHeight : this.slpBarCodeMaxHeight;
   }

   public int getBarCodeMaxWidth(int station) {
      return station == 2 ? this.recBarCodeMaxWidth : this.slpBarCodeMaxWidth;
   }

   public int getMaxPrintBitmapSize(int station) {
      return station == 2 ? this.recMaxPrintBitmapSize : this.slpMaxPrintBitmapSize;
   }

   public void setMaxPrintBitmapSize(byte station, int max) {
      switch (station) {
         case 2:
         default:
            this.recMaxPrintBitmapSize = max;
            break;
         case 4:
         case 20:
            this.slpMaxPrintBitmapSize = max;
      }
   }

   public int getBitmapMaxHeight(int station) {
      return station == 2 ? this.recBitmapMaxHeight : this.slpBitmapMaxHeight;
   }

   public int getBitmapHeight(int height, int station) {
      return (height - 1) / 8 + 1;
   }

   public int getBitmapWidth(int width, byte station) {
      int maxWidth = this.getStationWidth(station) / 8;
      int prtWidth = (width - 1) / 8 + 1;
      if (prtWidth > maxWidth) {
         prtWidth = maxWidth;
      } else if (prtWidth < 1) {
         prtWidth = 1;
      }

      return prtWidth;
   }

   public abstract ImageStreamProducer getImageStreamProducer();

   public int getPrinterType() {
      return this.printerType;
   }

   public int getPrinterID() {
      return this.printerID;
   }

   public String getSerialNumber() {
      return this.serialNumber;
   }

   public int getPrinterEC() {
      return this.ecLevel;
   }

   public int maxCmdSize() {
      return this.maxCmdSize;
   }

   public void setMaxCmdSize(int size) {
      this.maxCmdSize = size;
   }

   public boolean isDoubleByteMode() {
      return this.dbMode;
   }

   public boolean isDBCSPrinter() {
      return this.dbcs;
   }

   public byte getMaxFontNumber(byte station) {
      byte maxFontNumber = 0;

      for (int i = 0; i < this.fontTable.length; i++) {
         if (this.fontTable[i][0] == station) {
            maxFontNumber++;
         }
      }

      return maxFontNumber;
   }

   public byte getFontSize(byte station, byte typeFace) throws IllegalArgumentException {
      try {
         return this.fontTable[station][typeFace];
      } catch (ArrayIndexOutOfBoundsException var4) {
         throw new IllegalArgumentException();
      }
   }

   public void setDoubleByteMode(boolean mode) {
      this.dbMode = mode;
   }

   public void setDBCSPrinter(boolean dbcs) {
      this.dbcs = dbcs;
   }

   public void setECLevel(int ecLevel) {
      this.ecLevel = ecLevel;
   }

   public void setPrinterType(int type) {
      this.printerType = type;
   }

   public void setPrinterID(int id) {
      this.printerID = id;
   }

   public void setSerialNumber(String serial) {
      this.serialNumber = serial;
   }

   public void setStationWidth(byte station, int width) {
      switch (station) {
         case 2:
            this.recStationWidth = width;
            break;
         case 4:
            this.slpStationWidth = width;
            break;
         case 8:
            this.jrnStationWidth = width;
            break;
         case 20:
            this.slpLandStationWidth = width;
      }
   }

   protected int mapParameter(int[] parameterTable, int parameterValue) throws IllegalArgumentException {
      int sIndex = 0;

      while (sIndex < parameterTable.length && parameterTable[sIndex] != parameterValue) {
         sIndex++;
      }

      if (sIndex == parameterTable.length) {
         throw new IllegalArgumentException("Invalid argument passed to a POSj state command");
      } else {
         return sIndex;
      }
   }

   protected int getBarCodeMinHorSize(int station) {
      return station == 2 ? this.recBarCodeMinHorSize : this.slpBarCodeMinHorSize;
   }

   protected int getBarCodeMaxHorSize(int station) {
      return station == 2 ? this.recBarCodeMaxHorSize : this.slpBarCodeMaxHorSize;
   }

   public int getRecSidewaysMaxChars(int station) {
      if (station == 2) {
         return this.recSidewaysMaxChars;
      } else if (station == 4) {
         return this.slpSidewaysMaxChars;
      } else {
         return station == 8 ? this.jrnSidewaysMaxChars : 0;
      }
   }

   public int getRecSidewaysMaxLines(int station) {
      if (station == 2) {
         return this.recSidewaysMaxLines;
      } else if (station == 4) {
         return this.slpSidewaysMaxLines;
      } else {
         return station == 8 ? this.jrnSidewaysMaxLines : 0;
      }
   }

   public void setAlignment(byte station, byte alignment) {
      if (station == 2) {
         this.recAlignment = alignment;
      } else if (station == 4) {
         this.slpAlignment = alignment;
      } else {
         this.jrnAlignment = alignment;
      }
   }

   public byte getAlignment(byte station) {
      if (station == 2) {
         return this.recAlignment;
      } else {
         return station == 4 ? this.slpAlignment : this.jrnAlignment;
      }
   }

   public boolean getCapConcurrentJrnRec() {
      return this.capConcurrentJrnRec;
   }

   public boolean getCapConcurrentJrnSlp() {
      return this.capConcurrentJrnSlp;
   }

   public boolean getCapConcurrentRecSlp() {
      return this.capConcurrentRecSlp;
   }

   public boolean getCapCoverSensor() {
      return this.capCoverSensor;
   }

   public boolean getCapTransaction() {
      return this.capTransaction;
   }

   public boolean getCapPartialLinePrint(byte station) {
      if (station == 2) {
         return this.capRecPartialLine;
      } else if (station == 4) {
         return this.capSlpPartialLine;
      } else {
         return station == 8 ? this.capJrnPartialLine : false;
      }
   }

   public boolean getCap2Color(byte station) {
      if (station == 2) {
         return this.capRec2Color;
      } else if (station == 4) {
         return this.capSlp2Color;
      } else {
         return station == 8 ? this.capJrn2Color : false;
      }
   }

   public boolean getCapBold(byte station) {
      if (station == 2) {
         return this.recbools.get(1);
      } else if (station == 4) {
         return this.slpbools.get(1);
      } else {
         return station == 8 ? this.jrnbools.get(1) : false;
      }
   }

   public boolean getCapDhigh(byte station) {
      if (station == 2) {
         return this.recbools.get(2);
      } else if (station == 4) {
         return this.slpbools.get(2);
      } else {
         return station == 8 ? this.jrnbools.get(2) : false;
      }
   }

   public boolean getCapDwide(byte station) {
      if (station == 2) {
         return this.recbools.get(3);
      } else if (station == 4) {
         return this.slpbools.get(3);
      } else {
         return station == 8 ? this.jrnbools.get(3) : false;
      }
   }

   public boolean getCapDwideDhigh(byte station) {
      if (station == 2) {
         return this.capRecDwideDhigh;
      } else if (station == 4) {
         return this.capSlpDwideDhigh;
      } else {
         return station == 8 ? this.capJrnDwideDhigh : false;
      }
   }

   public boolean getCapEmptySensor(byte station) {
      if (station == 2) {
         return this.capRecEmptySensor;
      } else if (station == 4) {
         return this.capSlpEmptySensor;
      } else {
         return station == 8 ? this.capJrnEmptySensor : false;
      }
   }

   public boolean getCapItalic(byte station) {
      return false;
   }

   public boolean getCapNearEndSensor(byte station) {
      if (station == 2) {
         return this.capRecNearEndSensor;
      } else if (station == 4) {
         return this.capSlpNearEndSensor;
      } else {
         return station == 8 ? this.capJrnNearEndSensor : false;
      }
   }

   public boolean getCapUnderline(byte station) {
      if (station == 2) {
         return this.recbools.get(0);
      } else if (station == 4) {
         return this.slpbools.get(0);
      } else {
         return station == 8 ? this.jrnbools.get(0) : false;
      }
   }

   public boolean getCapBarCode(byte station) {
      if (station == 2) {
         return this.capRecBarCode;
      } else if (station == 4) {
         return this.capSlpBarCode;
      } else {
         return station == 8 ? this.capJrnBarCode : false;
      }
   }

   public boolean getCapBitmap(byte station) {
      if (station == 2) {
         return this.capRecBitmap;
      } else if (station == 4) {
         return this.capSlpBitmap;
      } else {
         return station == 8 ? this.capJrnBitmap : false;
      }
   }

   public boolean getCapLeft90(byte station) {
      if (station == 2) {
         return this.capRecLeft90;
      } else if (station == 4) {
         return this.capSlpBitmap;
      } else {
         return station == 8 ? this.capJrnBitmap : false;
      }
   }

   public boolean getCapRight90(byte station) {
      if (station == 2) {
         return this.capRecRight90;
      } else if (station == 4) {
         return this.capSlpRight90;
      } else {
         return station == 8 ? this.capJrnRight90 : false;
      }
   }

   public boolean getCapRotate180(byte station) {
      if (station == 2) {
         return this.capRecRotate180;
      } else if (station == 4) {
         return this.capSlpRotate180;
      } else {
         return station == 8 ? this.capJrnRotate180 : false;
      }
   }

   public boolean getCapPapercut(byte station) {
      if (station == 2) {
         return this.capRecPapercut;
      } else if (station == 4) {
         return this.capSlpPapercut;
      } else {
         return station == 8 ? this.capJrnPapercut : false;
      }
   }

   public boolean getCapStamp(byte station) {
      if (station == 2) {
         return this.capRecStamp;
      } else if (station == 4) {
         return this.capSlpStamp;
      } else {
         return station == 8 ? this.capJrnStamp : false;
      }
   }

   public boolean getCapFullslip(byte station) {
      return this.capFullslip;
   }

   public boolean getCapFeedReverse(byte station) {
      if (station == 2) {
         return this.recbools.get(5);
      } else if (station == 4) {
         return this.slpbools.get(5);
      } else {
         return station == 8 ? this.jrnbools.get(5) : false;
      }
   }

   public boolean getCapReverseVideo(byte station) {
      if (station == 2) {
         return this.recbools.get(4);
      } else if (station == 4) {
         return this.slpbools.get(4);
      } else {
         return station == 8 ? this.jrnbools.get(4) : false;
      }
   }

   public boolean getCapShading(byte station) {
      return false;
   }

   public boolean getCapScaleHorizontally(byte station) {
      if (station == 2) {
         return this.recbools.get(7);
      } else if (station == 4) {
         return this.slpbools.get(7);
      } else {
         return station == 8 ? this.jrnbools.get(7) : false;
      }
   }

   public boolean getCapScaleVertically(byte station) {
      if (station == 2) {
         return this.recbools.get(6);
      } else if (station == 4) {
         return this.slpbools.get(6);
      } else {
         return station == 8 ? this.jrnbools.get(6) : false;
      }
   }

   public boolean getCapSubScript(byte station) {
      return false;
   }

   public boolean getCapSuperScript(byte station) {
      return false;
   }

   public byte getMaxBitmapNumber(byte station) {
      if (station == 2) {
         return this.recMaxBitmapNumber;
      } else if (station == 4) {
         return this.slpMaxBitmapNumber;
      } else {
         return station == 8 ? this.jrnMaxBitmapNumber : 0;
      }
   }

   public byte getMaxLogoNumber(byte station) {
      if (station == 2) {
         return this.recMaxLogoNumber;
      } else if (station == 4) {
         return this.slpMaxLogoNumber;
      } else {
         return station == 8 ? this.jrnMaxLogoNumber : 0;
      }
   }

   public int getMaxUnderlineNumber(byte station) {
      if (station == 2) {
         return this.recMaxUnderlineNumber;
      } else if (station == 4) {
         return this.slpMaxUnderlineNumber;
      } else {
         return station == 8 ? this.jrnMaxUnderlineNumber : 0;
      }
   }

   public byte getMaxColorNumber(byte station) {
      if (station == 2) {
         return this.recMaxColorNumber;
      } else if (station == 4) {
         return this.slpMaxColorNumber;
      } else {
         return station == 8 ? this.jrnMaxColorNumber : 0;
      }
   }

   public byte getMaxScaleHorizontalNumber(byte station) {
      if (station == 2) {
         return this.recMaxScaleHorizontalNumber;
      } else if (station == 4) {
         return this.slpMaxScaleHorizontalNumber;
      } else {
         return station == 8 ? this.jrnMaxScaleHorizontalNumber : 0;
      }
   }

   public byte getMaxScaleVerticalNumber(byte station) {
      if (station == 2) {
         return this.recMaxScaleVerticalNumber;
      } else if (station == 4) {
         return this.slpMaxScaleVerticalNumber;
      } else {
         return station == 8 ? this.jrnMaxScaleVerticalNumber : 0;
      }
   }

   public byte getMaxRGBColorNumber(byte station) {
      return 0;
   }

   public int getFeedStepsPerInch(byte station) {
      if (station == 2) {
         return this.recFeedStepsPerInch;
      } else if (station == 4) {
         return this.slpFeedStepsPerInch;
      } else if (station == 20) {
         return this.slpLandscapeSPI;
      } else {
         return station == 8 ? this.jrnFeedStepsPerInch : 0;
      }
   }

   public int getMaxLinesToFeed(byte station) {
      if (station == 2) {
         return this.recMaxLinesToFeed;
      } else if (station == 4) {
         return this.slpMaxLinesToFeed;
      } else {
         return station == 8 ? this.jrnMaxLinesToFeed : 0;
      }
   }

   public int getSidewaysLineWidth(byte station) {
      if (station == 2) {
         return this.recSidewaysLineWidth;
      } else if (station == 4) {
         return this.slpSidewaysLineWidth;
      } else {
         return station == 8 ? this.jrnSidewaysLineWidth : 0;
      }
   }

   public boolean isMctValueRead() {
      return this.mctValue;
   }

   public void setMctValueRead(boolean value) {
      this.mctValue = value;
   }

   public void setMCT56Area(ByteBuffer mct56Area) {
      this.mctArea = mct56Area;
   }

   public void setMatrixUDF1Thermal(ByteBuffer udf1) {
      this.udf1Thermal = udf1;
   }

   public void setMatrixUDF2Thermal(ByteBuffer udf2) {
      this.udf2Thermal = udf2;
   }

   public void setMatrixUDF3Thermal(ByteBuffer udf3) {
      this.udf3Thermal = udf3;
   }

   public void setMatrixUDF4Thermal(ByteBuffer udf4) {
      this.udf4Thermal = udf4;
   }

   public void setMatrixUDF1Impact(ByteBuffer udf1) {
      this.udf1Impact = udf1;
   }

   public void setMatrixUDF2Impact(ByteBuffer udf2) {
      this.udf2Impact = udf2;
   }

   public ByteBuffer getMCT56Area() {
      return this.mctArea;
   }

   public ByteBuffer getMatrixUDF1Thermal() {
      return this.udf1Thermal;
   }

   public ByteBuffer getMatrixUDF2Thermal() {
      return this.udf2Thermal;
   }

   public ByteBuffer getMatrixUDF3Thermal() {
      return this.udf3Thermal;
   }

   public ByteBuffer getMatrixUDF4Thermal() {
      return this.udf4Thermal;
   }

   public ByteBuffer getMatrixUDF1Impact() {
      return this.udf1Impact;
   }

   public ByteBuffer getMatrixUDF2Impact() {
      return this.udf2Impact;
   }

   public void checkMatrixValues() {
      if (this.udf1Thermal != null
         && this.udf2Thermal != null
         && this.udf3Thermal != null
         && this.udf4Thermal != null
         && this.udf1Impact != null
         && this.udf2Impact != null) {
         this.mctValue = true;
      }
   }

   public void setTransactionMode(boolean value) {
      this.transaction = value;
   }

   public boolean isTransactionModeOn() {
      return this.transaction;
   }

   protected void setBitSet(int idx, boolean onOff, AsyncBitSet bools) {
      if (onOff) {
         bools.set(idx);
      } else {
         bools.clear(idx);
      }
   }
}
