package com.ibm.posj.util;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.HandleException;
import com.ibm.posj.MSRDevInfoConst;

public class MSRTrackParser {
   private MSRDevInfoConst msrDevInfo = new MSRDevInfoConst();
   private Tracer tracer = TracerFactory.getInstance().createTracer("MSR", "MSR");
   private byte[] track1 = null;
   private byte[] track2 = null;
   private byte[] track3 = null;
   private byte[] trackJISII = null;
   private int msrType = 3303;
   private int trackLengthFieldSize = 0;
   private int track1MaxLen = 0;
   private int track2MaxLen = 0;
   private int track3MaxLen = 0;
   private int trackJISIIMaxLen = 0;
   private int startOfTrackInfo = 0;
   private int minDataLen = 0;
   public static final int TRACK_LEN_1 = 1;
   public static final int TRACK_LEN_2 = 2;
   public static final int NUMBER_OF_ISO_TRACKS = 3;
   public static final int NUMBER_OF_JUCC_TRACKS = 2;
   public static final int VALID_DATA_MASK = 42;
   public static final int ERROR_DATA_MASK = 21;
   public static final int FIRST_STBYTE_POSITION = 0;
   public static final int SECOND_STBYTE_POSITION = 1;

   public MSRTrackParser(int startByte, int type, int lenField) {
      this.startOfTrackInfo = startByte;
      this.msrType = type;
      this.trackLengthFieldSize = lenField;
      this.minDataLen = startByte;
      this.track1 = new byte[0];
      this.track2 = new byte[0];
      this.track3 = new byte[0];
      this.trackJISII = new byte[0];
      if (this.getDevInfoConst().isISOMSRType(this.msrType)) {
         this.tracer.println("ISO MSR set on constructor");
         this.minDataLen = this.minDataLen + this.trackLengthFieldSize * 3;
      } else if (!this.getDevInfoConst().isJUCCMSRType(this.msrType) && !this.getDevInfoConst().isWriteCapableMSRType(this.msrType)) {
         this.tracer.println("No type defined");
      } else {
         this.tracer.println("JUCC MSR set on constructor");
         this.minDataLen = this.minDataLen + this.trackLengthFieldSize * 2;
      }
   }

   public void setTracksMaxLengths(boolean isDBCS) {
      if (this.tracer.isOn()) {
         this.tracer.println(2, "--> setTracksMaxLengths(ISO)");
      }

      if (this.msrType == 3300) {
         this.track1MaxLen = 98;
         this.track2MaxLen = 46;
         this.track3MaxLen = 139;
      } else if (this.msrType == 3301 && !isDBCS) {
         this.trackJISIIMaxLen = 88;
         this.track2MaxLen = 46;
      } else if (this.msrType == 3302 || isDBCS) {
         this.trackJISIIMaxLen = 69;
         this.track2MaxLen = 37;
      }

      if (this.tracer.isOn()) {
         this.tracer.println(2, "<-- setTracksMaxLengths(ISO)");
      }
   }

   public int getMaxLength(boolean isDBCS) {
      int maxLength = 0;
      if (this.msrType == 3300) {
         maxLength = 290;
      } else if (this.msrType == 3301 && !isDBCS) {
         maxLength = 140;
      } else if (this.msrType == 3302 || isDBCS) {
         maxLength = 110;
      }

      return maxLength;
   }

   public void separateInTracks(byte[] data) throws HandleException {
      if (this.tracer.isOn()) {
         this.tracer.println(2, "--> separateInTracks, msrType : " + this.msrType);
      }

      if (this.getDevInfoConst().isISOMSRType(this.msrType)) {
         if (this.tracer.isOn()) {
            this.tracer.println(2, "ISO Format");
         }

         this.parseISOData(data);
      } else if (this.getDevInfoConst().isJUCCMSRType(this.msrType) || this.getDevInfoConst().isWriteCapableMSRType(this.msrType)) {
         if (this.tracer.isOn()) {
            this.tracer.println(2, "JUCC Format");
         }

         this.parseJUCCData(data);
      }

      if (this.tracer.isOn()) {
         this.tracer.println(2, "<-- separateInTracks");
      }
   }

   public static boolean isDataAvailable(byte[] data) {
      boolean isDataPresent = false;
      byte stbyte2 = data[1];
      if ((stbyte2 & 42) != 0 || (stbyte2 & 21) != 0) {
         isDataPresent = true;
      }

      return isDataPresent;
   }

   public static boolean isDeviceInfoResponseData(byte[] data) {
      boolean isResponse = false;
      if (PosjUtil.isBitSelected(data[0], 0)) {
         isResponse = true;
      }

      return isResponse;
   }

   public static void checkForCmdRejectError(byte[] stBytes) throws HandleException {
      if (PosjUtil.isBitSelected(stBytes[0], 7)) {
         throw new HandleException("Command Rejected by MSR");
      }
   }

   public byte[] getTrack1() {
      return this.track1;
   }

   public byte[] getTrack2() {
      return this.track2;
   }

   public byte[] getTrack3() {
      return this.track3;
   }

   public byte[] getTrackJISII() {
      return this.trackJISII;
   }

   private void parseISOData(byte[] data) throws HandleException {
      String substitute_secure_datastr = "";
      if (this.tracer.isOn()) {
         this.tracer.println(2, "--> parseISOData");
      }

      int track1LenBytePosition = this.startOfTrackInfo;
      int track1Len = this.getTrackLength(data, track1LenBytePosition, this.track1MaxLen);
      int track2LenBytePosition = track1LenBytePosition + track1Len + this.trackLengthFieldSize;
      int track2Len = this.getTrackLength(data, track2LenBytePosition, this.track2MaxLen);
      int track3LenBytePosition = track2LenBytePosition + track2Len + this.trackLengthFieldSize;
      int track3Len = this.getTrackLength(data, track3LenBytePosition, this.track3MaxLen);
      this.track1 = new byte[track1Len];
      this.track2 = new byte[track2Len];
      this.track3 = new byte[track3Len];

      try {
         System.arraycopy(data, track1LenBytePosition + this.trackLengthFieldSize, this.track1, 0, track1Len);
         System.arraycopy(data, track2LenBytePosition + this.trackLengthFieldSize, this.track2, 0, track2Len);
         System.arraycopy(data, track3LenBytePosition + this.trackLengthFieldSize, this.track3, 0, track3Len);
      } catch (ArrayIndexOutOfBoundsException var10) {
         throw new HandleException("HandleException occurred while copying MSR data", var10);
      }
   }

   private void parseJUCCData(byte[] data) throws HandleException {
      String substitute_secure_datastr = "";
      if (this.tracer.isOn()) {
         this.tracer.println(2, "--> parseJUCCData");
      }

      int trackJISIILenBytePosition = this.startOfTrackInfo;
      int trackJISIILen = this.getTrackLength(data, trackJISIILenBytePosition, this.trackJISIIMaxLen);
      int track2LenBytePosition = trackJISIILenBytePosition + trackJISIILen + this.trackLengthFieldSize;
      int track2Len = this.getTrackLength(data, track2LenBytePosition, this.track2MaxLen);
      this.track2 = new byte[track2Len];
      this.trackJISII = new byte[trackJISIILen];

      try {
         System.arraycopy(data, track2LenBytePosition + this.trackLengthFieldSize, this.track2, 0, track2Len);
         System.arraycopy(data, trackJISIILenBytePosition + this.trackLengthFieldSize, this.trackJISII, 0, trackJISIILen);
      } catch (ArrayIndexOutOfBoundsException var8) {
         throw new HandleException("HandleException occurred while copying MSR data", var8);
      }
   }

   private int getTrackLength(byte[] data, int position, int maxLen) throws HandleException {
      int trackLen = 0;
      if (this.trackLengthFieldSize == 1) {
         trackLen = data[position];
      } else if (this.trackLengthFieldSize == 2) {
         byte firstByte = (byte)PosjUtil.clearBit(data[position], 7);
         byte secondByte = (byte)PosjUtil.clearBit(data[position + 1], 7);
         trackLen = firstByte + secondByte;
      }

      if (trackLen > maxLen) {
         throw new HandleException("Track length exceeds the limit permitted");
      } else {
         return trackLen;
      }
   }

   private MSRDevInfoConst getDevInfoConst() {
      return this.msrDevInfo;
   }
}
