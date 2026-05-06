package com.ibm.posj.util;

import com.ibm.jutil.Util;
import com.ibm.jutil.logging.LogHelper;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.PosSystemManager;

public class MSRDataHelper {
   protected byte[] msrData = null;
   private int msrId = 3300;
   private int state = 0;
   private boolean errorInSomeTrack = false;
   private boolean errorInTrack1_JIS_II = false;
   private boolean errorInTrack2 = false;
   private boolean errorInTrack3 = false;
   private byte track = 1;
   private byte track1JISSentinel = 0;
   private byte track2Sentinel = 0;
   private byte track3Sentinel = 0;
   private byte endSentinel = 0;
   protected byte[] track1 = new byte[0];
   protected byte[] track2 = new byte[0];
   protected byte[] track3 = new byte[0];
   protected byte[] trackJIS_II = new byte[0];
   protected LogHelper logHelper = PosSystemManager.getInstance().getLogHelper();
   private Tracer tracer = TracerFactory.getInstance().createTracer("MSR", "MSR");
   private static final byte TRACK_ERROR = 69;
   private static final byte TRACK_END_SENTINEL = 63;
   private static final int START_STATE = 0;
   private static final int TRK1_J_STATE = 1;
   protected static final int TRK2_STATE = 2;
   private static final int TRK3_STATE = 3;
   private static final int WRONG_DATA_STATE = 4;
   private static final int TRK_FINISHED_STATE = 5;
   private static final int END_STATE = 6;
   private static final int TRK_ERROR_STATE = 7;
   private static final int AUX_STATE = 8;
   private static final String WRONG_DATA_MSG = "Wrong data received from card";

   private MSRDataHelper() {
   }

   public MSRDataHelper(int id, byte trk1Sent, byte trk2Sent, byte trk3Sent, byte endSent) {
      if (id != 3303) {
         this.msrId = id;
      }

      this.setSentinels(trk1Sent, trk2Sent, trk3Sent, endSent);
   }

   public byte[] getData() {
      return this.msrData != null ? this.msrData : new byte[0];
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

   public byte[] getTrackJIS_II() {
      return this.trackJIS_II;
   }

   public boolean cardHasErrorsInTracks() {
      return this.errorInSomeTrack;
   }

   public boolean hasErrorInTrack1_JIS_II() {
      return this.errorInTrack1_JIS_II;
   }

   public boolean hasErrorInTrack2() {
      return this.errorInTrack2;
   }

   public boolean hasErrorInTrack3() {
      return this.errorInTrack3;
   }

   public void parseData(byte[] data) {
      if (this.tracer.isOn()) {
         this.tracer.println(2, "--> parseData" + Util.toFormatedHexString(data));
      }

      if (data != null) {
         this.msrData = data;
      }

      if (this.msrData.length == 0) {
         this.logHelper.addLogEntry(1013, "No Data to parse", "AllDevices", 4);
      } else {
         this.initializeTracksAndStatus();
         int lastState = 0;
         byte currentByte = 0;
         int i = 0;

         do {
            currentByte = this.msrData[i];
            switch (this.state) {
               case 0:
                  if (currentByte == this.track1JISSentinel) {
                     this.state = 1;
                  } else if (currentByte == this.track3Sentinel) {
                     this.state = 3;
                  } else if (currentByte == this.track2Sentinel) {
                     this.state = 2;
                  } else if (currentByte == 69) {
                     this.state = 7;
                  } else {
                     this.state = 4;
                  }
                  break;
               case 1:
                  if (this.tracer.isOn()) {
                     this.tracer.println(2, "TRK1_J_STATE");
                  }

                  if (currentByte != this.track2Sentinel && currentByte != this.track3Sentinel && currentByte != 69) {
                     if (currentByte == this.endSentinel) {
                        lastState = 5;
                        this.state = 8;
                        break;
                     }

                     if (currentByte == this.track1JISSentinel) {
                        if (this.tracer.isOn()) {
                           this.tracer.println(2, "Track 1/JIS_II Start sentinel repeated continuously");
                        }

                        this.state = 1;
                        break;
                     }

                     int trackLen = 0;
                     int jxx = i;

                     while (jxx < this.msrData.length && this.msrData[jxx] != this.endSentinel && this.msrData[jxx] != this.track1JISSentinel) {
                        jxx++;
                        trackLen++;
                        if (this.tracer.isOn()) {
                           this.tracer
                              .println(
                                 2, "msrData.length = " + this.msrData.length + " j = " + jxx + " trackLen = " + trackLen + " currChar = " + this.msrData[jxx]
                              );
                        }
                     }

                     if (this.msrData[jxx] == this.track1JISSentinel) {
                        if (this.tracer.isOn()) {
                           this.tracer.println(2, "Track 1 Start sentinel repeated NON continuously");
                        }

                        i = jxx;
                        this.state = 1;
                        this.track1 = new byte[0];
                        this.trackJIS_II = new byte[0];
                        break;
                     }

                     if (this.msrId == 3301) {
                        this.trackJIS_II = new byte[trackLen];
                        System.arraycopy(this.msrData, i, this.trackJIS_II, 0, trackLen);
                     } else {
                        this.track1 = new byte[trackLen];
                        System.arraycopy(this.msrData, i, this.track1, 0, trackLen);

                        for (int index = 0; index < this.track1.length; index++) {
                           this.track1[index] = (byte)(this.track1[index] - 32);
                        }
                     }

                     i = jxx - 1;
                     break;
                  }

                  this.state = 4;
                  break;
               case 2:
                  if (this.tracer.isOn()) {
                     this.tracer.println(2, "TRK2_STATE");
                  }

                  if (lastState != 3
                     && (currentByte == this.track1JISSentinel || currentByte == this.track3Sentinel || currentByte == 69)
                     && this.track2Sentinel != this.track3Sentinel) {
                     this.state = 4;
                  } else if (currentByte == this.endSentinel) {
                     this.state = 5;
                  } else if (currentByte == this.track2Sentinel) {
                     if (this.tracer.isOn()) {
                        this.tracer.println(2, "Track 2 Start sentinel repeated continuously");
                     }

                     this.state = 2;
                  } else {
                     int trackLen = 0;
                     int jxx = i;

                     while (this.msrData[jxx] != this.endSentinel && this.msrData[jxx] != this.track2Sentinel && jxx < this.msrData.length) {
                        jxx++;
                        trackLen++;
                        if (this.tracer.isOn()) {
                           this.tracer
                              .println(
                                 2, "msrData.length = " + this.msrData.length + " j = " + jxx + " trackLen = " + trackLen + " currChar = " + this.msrData[jxx]
                              );
                        }
                     }

                     if (this.msrData[jxx] != this.track2Sentinel) {
                        this.track2 = new byte[trackLen];
                        System.arraycopy(this.msrData, i, this.track2, 0, trackLen);
                        i = jxx - 1;
                     } else {
                        if (this.tracer.isOn()) {
                           this.tracer.println(2, "Track 2 Start sentinel repeated NON continuously");
                        }

                        i = jxx;
                        this.state = 2;
                        this.track2 = new byte[0];
                     }
                  }
                  break;
               case 3:
                  if (this.tracer.isOn()) {
                     this.tracer.println(2, "TRK3_STATE");
                  }

                  if (this.track2Sentinel == this.track3Sentinel) {
                     int totalSentinels = 0;

                     for (int j = 0; j < this.msrData.length; j++) {
                        if (this.msrData[j] == 59) {
                           totalSentinels++;
                        }
                     }

                     if (totalSentinels == 1) {
                        this.state = 2;
                        i--;
                        break;
                     }
                  }

                  if (currentByte != this.track1JISSentinel && currentByte != 69) {
                     if (currentByte == this.endSentinel) {
                        lastState = this.state;
                        this.state = 8;
                        break;
                     }

                     if (currentByte != this.track2Sentinel && currentByte != this.track3Sentinel) {
                        int trackLen = 0;

                        int jx;
                        for (jx = i; this.msrData[jx] != this.endSentinel && this.msrData[jx] != this.track3Sentinel && jx < this.msrData.length; trackLen++) {
                           jx++;
                        }

                        if (this.msrData[jx] != this.track3Sentinel) {
                           this.track3 = new byte[trackLen];
                           System.arraycopy(this.msrData, i, this.track3, 0, trackLen);
                           i = jx - 1;
                        } else {
                           if (this.tracer.isOn()) {
                              this.tracer.println(2, "Track 3 Start sentinel repeated NON continuously");
                           }

                           i = jx;
                           this.state = 3;
                           this.track3 = new byte[0];
                        }
                        break;
                     }

                     this.state = 3;
                     break;
                  }

                  this.state = 4;
                  break;
               case 4:
                  if (this.tracer.isOn()) {
                     this.tracer.println(2, "WRONG_DATA_STATE");
                  }

                  this.logHelper.addLogEntry(1013, "Wrong data received from card", DevCats.MSR_DEVCAT.toString(), 4);
                  this.setError(this.track);
                  i = this.msrData.length + 1;
                  this.state = 0;
                  break;
               case 5:
                  if (this.tracer.isOn()) {
                     this.tracer.println(2, "TRK_FINISHED_STATE" + this.track);
                  }

                  if (currentByte == this.track2Sentinel) {
                     if (this.track < 2) {
                        this.state = 2;
                     } else {
                        this.state = 4;
                     }
                  } else if (currentByte == this.track3Sentinel) {
                     if (this.track < 3) {
                        this.state = 3;
                     } else {
                        this.state = 4;
                     }
                  } else if (currentByte == 13) {
                     i++;
                  } else if (currentByte == 69) {
                     this.state = 7;
                  } else {
                     this.state = 4;
                  }

                  this.track++;
                  break;
               case 6:
                  if (this.tracer.isOn()) {
                     this.tracer.println(2, "END_STATE");
                  }

                  i = this.msrData.length + 1;
                  this.state = 0;
                  break;
               case 7:
                  if (this.tracer.isOn()) {
                     this.tracer.println(2, "TRK_ERROR_STATE");
                  }

                  this.setError(this.track);
                  if (currentByte == this.track2Sentinel) {
                     this.state = 2;
                  } else if (currentByte == this.track3Sentinel) {
                     this.state = 3;
                  } else if (currentByte == 69) {
                     if (this.track > 3) {
                        this.state = 4;
                     }
                  } else {
                     this.state = 4;
                  }

                  this.track++;
                  break;
               case 8:
                  if (this.tracer.isOn()) {
                     this.tracer.println(2, "AUX_STATE");
                  }

                  if (currentByte == 13) {
                     this.state = 2;
                  } else {
                     i--;
                     this.state = lastState;
                  }
               default:
                  this.logHelper.addLogEntry(1013, "Wrong data received from card", DevCats.MSR_DEVCAT.toString(), 5);
            }

            i++;
            if (i == this.msrData.length && this.msrData[i - 1] == 69) {
               i--;
            }
         } while (i < this.msrData.length);

         if (this.tracer.isOn()) {
            this.tracer.println(2, "<-- parseData");
         }
      }
   }

   protected void setError(byte trk) {
      this.errorInSomeTrack = true;
      switch (trk) {
         case 1:
            this.errorInTrack1_JIS_II = true;
            break;
         case 2:
            this.errorInTrack2 = true;
            break;
         case 3:
            this.errorInTrack3 = true;
      }
   }

   protected void setSentinels(byte t1JISSentinel, byte t2Sentinel, byte t3Sentinel, byte eSentinel) {
      this.track1JISSentinel = t1JISSentinel;
      this.track2Sentinel = t2Sentinel;
      this.track3Sentinel = t3Sentinel;
      this.endSentinel = eSentinel;
   }

   protected void initializeTracksAndStatus() {
      this.track = 1;
      this.state = 0;
      this.errorInSomeTrack = false;
      this.errorInTrack1_JIS_II = false;
      this.errorInTrack2 = false;
      this.errorInTrack3 = false;
      this.track1 = new byte[0];
      this.track2 = new byte[0];
      this.track3 = new byte[0];
      this.trackJIS_II = new byte[0];
   }
}
