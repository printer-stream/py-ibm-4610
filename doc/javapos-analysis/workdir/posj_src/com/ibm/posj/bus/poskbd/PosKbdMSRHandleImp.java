package com.ibm.posj.bus.poskbd;

import com.ibm.jutil.Util;
import com.ibm.posj.AbstractHandleCmd;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.MSRCmd;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.MSRHandleImp;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.MSRDataEvent;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.MSRDataHelper;
import com.ibm.posj.util.MSRTrackParser;
import com.ibm.poskbd.Function;
import com.ibm.poskbd.KeyboardFunction;
import com.ibm.poskbd.MsrFunction;
import com.ibm.poskbd.MsrTracks;
import com.ibm.poskbd.event.MsrEvent;
import com.ibm.poskbd.event.MsrListener;
import com.ibm.poskbd.util.MsrTracksFactory;

class PosKbdMSRHandleImp extends AbstractPosKbdHandleImp implements MSRHandleImp {
   private MSRTrackParser trackParser = null;
   private MSRDataHelper dataHelperParser = null;
   private int msrType = 3303;
   private int productId = 3303;
   private MsrFunction msrFunction = null;
   private KeyboardFunction keyboardFunction = null;
   PosKbdMSRHandleImp.MSRListener msrListener = new PosKbdMSRHandleImp.MSRListener();
   public static final int FIRST_BYTE_DATA = 2;
   public static final int LENGTH_BYTES = 2;
   public static final int MSR_THREE_TRACKS_MAX_LENGTH = 291;
   public static final int MSR_TWO_HEAD_MAX_LENGTH = 228;
   private static final byte TRACK1_SENTINEL = 37;
   private static final byte TRACK2_SENTINEL = 59;
   private static final byte TRACK3_SENTINEL = 59;
   private static final byte END_SENTINEL = 63;

   public PosKbdMSRHandleImp(HandleKey key, Function function) {
      super(key);
      this.msrFunction = (MsrFunction)function;
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitMSR(this);
   }

   public DevCat getDevCat() {
      return DevCats.MSR_DEVCAT;
   }

   public void init() throws HandleException {
      this.msrFunction.addMsrListener(this.msrListener);
   }

   public void setKeyboardFunction(KeyboardFunction keyboard) {
      this.keyboardFunction = keyboard;
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd.getCode() == 600) {
         this.submitConfigMSRCmd((MSRCmd.ConfigMSRCmd)cmd);
      } else {
         if (!(cmd instanceof SystemCmd)) {
            throw new HandleException("Invalid MSRCmd object submitted!");
         }

         this.submitSystemCmd((SystemCmd)cmd);
      }
   }

   protected void submitConfigMSRCmd(MSRCmd.ConfigMSRCmd msrCmd) throws HandleException {
      MsrTracksFactory tracksFactory = new MsrTracksFactory();
      MsrTracks tracks = tracksFactory.createMsrTracks(
         msrCmd.isEnabledISOtrack1(), msrCmd.isEnabledISOtrack2(), msrCmd.isEnabledISOtrack3(), msrCmd.isEnabledJIS_IItrack()
      );
      this.msrFunction.enableTracks(tracks);
   }

   protected void submitSystemCmd(SystemCmd systemCmd) throws HandleException {
      try {
         if (systemCmd.getCode() != 101 && systemCmd.getCode() != 100) {
            if (systemCmd.getCode() != 103) {
               throw new HandleException("Invalid SystemCmd.getCode() value!");
            }

            SystemCmd.DeviceInfoRequestCmd cmd = (SystemCmd.DeviceInfoRequestCmd)systemCmd;
            this.setMSRType((byte)this.keyboardFunction.getDeviceInfo().getMsrType());
            cmd.setDeviceId(this.getProductId(this.keyboardFunction.getDeviceInfo().getKeyboardSubtype()));
            cmd.setDeviceType(this.msrType);
         }
      } catch (HandleException var3) {
         ((AbstractHandleCmd.DefaultResult)systemCmd.getResult()).setInError(true);
         throw new HandleException("Error while sending SystemCmd to MSR", var3);
      }
   }

   protected int getProductId(short kbdId) {
      switch (kbdId) {
         case 1:
            this.productId = 3329;
            break;
         case 2:
            this.productId = 3332;
            break;
         case 3:
            this.productId = 3330;
            break;
         case 4:
         default:
            this.productId = 3303;
            break;
         case 5:
            this.productId = 3331;
      }

      return this.productId;
   }

   protected void handleMSREvent(MsrEvent event) {
      byte[] data = event.getMsrData();
      if (this.isTracerOn()) {
         this.traceNormal("--> handleMSREvent Data comming : " + Util.toFormatedHexString(data));
      }

      if (this.isSureOneDataFormat(data)) {
         this.parseSentinelData(data);
      } else {
         this.parseDataAndFireEvent(data);
      }

      if (this.isTracerOn()) {
         this.traceNormal("<-- handleMSREvent");
      }
   }

   protected void parseDataAndFireEvent(byte[] data) {
      if (this.isTracerOn()) {
         this.traceNormal("--> parseDataAndFireEvent = " + Util.toFormatedHexString(data));
      }

      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();

      try {
         MSRTrackParser.checkForCmdRejectError(data);
      } catch (HandleException var6) {
         eventHelper.fireErrorEvent(new ErrorEvent(this, -101));
      } catch (ArrayIndexOutOfBoundsException var7) {
         if (this.isTracerOn()) {
            this.traceNormal("Byte Array is empty!");
         }
      }

      if (MSRTrackParser.isDataAvailable(data)) {
         this.trackParser = new MSRTrackParser(2, this.msrType, 2);
         this.trackParser.setTracksMaxLengths(false);
         if (data.length > this.trackParser.getMaxLength(false)) {
            eventHelper.fireErrorEvent(new ErrorEvent(this, -602));
         } else {
            try {
               int msrStatus = (data[0] << 8) + data[1];
               MSRDataEvent dataEvent = null;
               this.trackParser.separateInTracks(data);
               if (this.msrType == 3300) {
                  dataEvent = this.createMSRDataEvent(msrStatus, this.trackParser.getTrack1(), this.trackParser.getTrack2(), this.trackParser.getTrack3());
               } else if (this.msrType == 3301) {
                  dataEvent = this.createMSRDataEvent(msrStatus, this.trackParser.getTrack2(), this.trackParser.getTrackJISII());
               }

               eventHelper.fireDataEvent(dataEvent);
            } catch (HandleException var5) {
               eventHelper.fireErrorEvent(new ErrorEvent(this, -103, var5.toString()));
            }
         }
      } else if (data.length < 1) {
         eventHelper.fireErrorEvent(new ErrorEvent(this, -601));
      }

      if (this.isTracerOn()) {
         this.traceNormal("<-- parseDataAndFireEvent = " + Util.toFormatedHexString(data));
      }
   }

   protected void parseSentinelData(byte[] data) {
      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
      this.dataHelperParser = new MSRDataHelper(3300, (byte)37, (byte)59, (byte)59, (byte)63);
      this.dataHelperParser.parseData(data);
      eventHelper.fireDataEvent(this.createMSRDataEvent());
   }

   protected void setMSRType(int dataByte) {
      switch (dataByte) {
         case 1:
            this.msrType = 3300;
            break;
         case 2:
            this.msrType = 3301;
            break;
         default:
            this.msrType = 2001;
      }
   }

   protected MSRDataEvent createMSRDataEvent() {
      MSRDataEvent msrDataEvent = new MSRDataEvent(
         this, this.dataHelperParser.getTrack1(), this.dataHelperParser.getTrack2(), this.dataHelperParser.getTrack3(), this.dataHelperParser.getTrackJIS_II()
      );
      if (this.dataHelperParser.cardHasErrorsInTracks()) {
         msrDataEvent.setErrorInSomeTrack(true);
         if (this.dataHelperParser.hasErrorInTrack1_JIS_II()) {
            if (this.msrType == 3300) {
               msrDataEvent.setErrorInTrack1(true);
            } else {
               msrDataEvent.setErrorInTrack4(true);
            }
         }

         if (this.dataHelperParser.hasErrorInTrack2()) {
            msrDataEvent.setErrorInTrack2(true);
         }

         if (this.dataHelperParser.hasErrorInTrack3()) {
            msrDataEvent.setErrorInTrack3(true);
         }
      }

      if (this.isTracerOn()) {
         this.traceNormal("<-- createMSRDataEvent()");
      }

      return msrDataEvent;
   }

   protected MSRDataEvent createMSRDataEvent(int msrStatus, byte[] trk1, byte[] trk2, byte[] trk3) {
      if (this.isTracerOn()) {
         this.traceNormal("--> createMSRDataEvent(ISO)");
      }

      MSRDataEvent msrDataEvent = new MSRDataEvent(this, trk1, trk2, trk3);
      if ((msrStatus & 21) != 0) {
         msrDataEvent.setErrorInSomeTrack(true);
         if ((1 & msrStatus) != 0) {
            if (this.msrType == 3300) {
               msrDataEvent.setErrorInTrack1(true);
            } else {
               msrDataEvent.setErrorInTrack4(true);
            }
         }

         if ((4 & msrStatus) != 0) {
            msrDataEvent.setErrorInTrack2(true);
         }

         if ((16 & msrStatus) != 0) {
            msrDataEvent.setErrorInTrack3(true);
         }
      }

      if (this.isTracerOn()) {
         this.traceNormal("<-- createMSRDataEvent(ISO)");
      }

      return msrDataEvent;
   }

   protected MSRDataEvent createMSRDataEvent(int msrStatus, byte[] trk2, byte[] trkJISII) {
      if (this.isTracerOn()) {
         this.traceNormal("--> creatMSRDataEvent(JUCC)");
      }

      MSRDataEvent msrDataEvent = new MSRDataEvent(this, trk2, trkJISII);
      if ((msrStatus & 21) != 0) {
         msrDataEvent.setErrorInSomeTrack(true);
         if ((1 & msrStatus) != 0) {
            if (this.msrType == 3300) {
               msrDataEvent.setErrorInTrack1(true);
            } else {
               msrDataEvent.setErrorInTrack4(true);
            }
         }

         if ((4 & msrStatus) != 0) {
            msrDataEvent.setErrorInTrack2(true);
         }
      }

      if (this.isTracerOn()) {
         this.traceNormal("<-- creatMSRDataEvent(JUCC)");
      }

      return msrDataEvent;
   }

   private boolean isSureOneDataFormat(byte[] data) {
      boolean isSureOneData = false;
      int inLen = data.length;
      if (this.isTracerOn()) {
         this.traceNormal("--> isSureOneDataFormat");
      }

      if (inLen <= 0) {
         if (this.isTracerOn()) {
            this.traceNormal("No sentinel data to parse");
         }

         return false;
      } else {
         if (data[0] == 37 || data[0] == 59) {
            if (this.isTracerOn()) {
               this.traceNormal("Valid start Sentinels");
            }

            isSureOneData = true;
         }

         if ((data[inLen - 2] == 63 || data[inLen - 1] == 13) && isSureOneData) {
            if (this.isTracerOn()) {
               this.traceNormal("Valid end Sentinels");
            }
         } else {
            isSureOneData = false;
         }

         if (this.isTracerOn()) {
            this.traceNormal("<-- isSureOneDataFormat? " + isSureOneData);
         }

         return isSureOneData;
      }
   }

   protected class MSRListener implements MsrListener {
      public void msrDataReceived(MsrEvent event) {
         PosKbdMSRHandleImp.this.handleMSREvent(event);
      }
   }
}
