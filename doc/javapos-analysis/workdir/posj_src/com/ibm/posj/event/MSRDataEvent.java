package com.ibm.posj.event;

public class MSRDataEvent extends DataEvent {
   private byte[] track1 = new byte[0];
   private byte[] track2 = new byte[0];
   private byte[] track3 = new byte[0];
   private byte[] JIS_IITrack = new byte[0];
   private boolean errorInSomeTrack = false;
   private boolean errorInTrack1 = false;
   private boolean errorInTrack2 = false;
   private boolean errorInTrack3 = false;
   private boolean errorInTrack4 = false;

   public MSRDataEvent(Object source, byte[] track1Data, byte[] track2Data, byte[] track3Data, byte[] JIS_IITrackData) {
      super(source);
      this.track1 = track1Data;
      this.track2 = track2Data;
      this.track3 = track3Data;
      this.JIS_IITrack = JIS_IITrackData;
   }

   public MSRDataEvent(Object source, byte[] Track1Data, byte[] Track2Data, byte[] Track3Data) {
      super(source);
      this.track1 = Track1Data;
      this.track2 = Track2Data;
      this.track3 = Track3Data;
   }

   public MSRDataEvent(Object source, byte[] track2Data, byte[] JIS_IITrackData) {
      super(source);
      this.track2 = track2Data;
      this.JIS_IITrack = JIS_IITrackData;
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

   public byte[] getJIS_IITrack() {
      return this.JIS_IITrack;
   }

   public boolean getErrorInSomeTrack() {
      return this.errorInSomeTrack;
   }

   public boolean getErrorInTrack1() {
      return this.errorInTrack1;
   }

   public boolean getErrorInTrack2() {
      return this.errorInTrack2;
   }

   public boolean getErrorInTrack3() {
      return this.errorInTrack3;
   }

   public boolean getErrorInTrack4() {
      return this.errorInTrack4;
   }

   public void setTrack1(byte[] track1) {
      this.track1 = track1;
   }

   public void setTrack2(byte[] track2) {
      this.track2 = track2;
   }

   public void setTrack3(byte[] track3) {
      this.track3 = track3;
   }

   public void setJIS_IITrack(byte[] JIS_IITrack) {
      this.JIS_IITrack = JIS_IITrack;
   }

   public void setErrorInSomeTrack(boolean errorInSomeTrack) {
      this.errorInSomeTrack = errorInSomeTrack;
   }

   public void setErrorInTrack1(boolean errorInTrack1) {
      this.errorInTrack1 = errorInTrack1;
   }

   public void setErrorInTrack2(boolean errorInTrack2) {
      this.errorInTrack2 = errorInTrack2;
   }

   public void setErrorInTrack3(boolean errorInTrack3) {
      this.errorInTrack3 = errorInTrack3;
   }

   public void setErrorInTrack4(boolean error) {
      this.errorInTrack4 = error;
   }
}
