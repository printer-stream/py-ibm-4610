package com.ibm.posj.bus.rs232;

import com.ibm.jutil.Util;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.MSRHandleImp;
import com.ibm.posj.bus.rs232.javaxcomm.Rs232PortCommAdapter;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.event.MSRDataEvent;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.MSRDataHelper;
import com.ibm.rs232.Rs232Port;
import com.ibm.rs232.event.Rs232DataEvent;
import com.ibm.rs232.event.Rs232ErrorEvent;

public class Rs232MSRHandleImp extends AbstractRs232HandleImp implements MSRHandleImp {
   protected int deviceType = 3303;
   private int msrState = 0;
   private byte[] msrDataObtained = null;
   private MSRDataHelper rs232MSRHelper = null;
   private Handle.EventHelper eventHelper = null;
   private int timeout;
   private boolean watcherEnabled;
   private Rs232MSRReader msrReader = new Rs232MSRReader();
   private Rs232PortCommAdapter rs232Adapter = null;
   public static final String HW_ERROR_MSG = "Rs232 MSR hardware error";
   public static final String CMD_NOT_SUPPORTED_MSG = " Command not supported for Rs232 MSR";
   public static final int BEGIN_JOINIG_DATA_STATE = 0;
   public static final int CONTINUE_JOINIG_DATA_STATE = 1;
   public static final int EC_LEVEL_NOT_SUPPORTED = -1;
   private static final byte TRACK1_SENTINEL = 37;
   private static final byte TRACK2_SENTINEL = 59;
   private static final byte TRACK3_SENTINEL = 43;
   private static final byte END_SENTINEL = 63;

   public Rs232MSRHandleImp(HandleKey key, Rs232Port rs232Port, int devType, boolean watchEnabled, int time) {
      super(key, rs232Port);
      this.deviceType = devType;
      this.timeout = time;
      this.watcherEnabled = watchEnabled;
   }

   protected void dataEventOccurred(Rs232DataEvent event) {
      byte[] data = event.getData();
      if (this.isTracerOn()) {
         this.traceNormal(": dataEventOccurred.[" + data.length + "] event = " + Util.toFormatedHexString(data));
      }

      if (this.getHandle().isDirectIOMode()) {
         this.fireDirectIOEvent(data);
      }

      this.processEvent(data);
      if (this.isTracerOn()) {
         this.traceNormal(": MSR deviceId to use = ");
      }
   }

   protected void errorEventOccurred(Rs232ErrorEvent event) {
      if (this.isTracerOn()) {
         this.traceNormal(": errorEvent occurred");
      }

      this.getLogHelper().addLogEntry(1000, "Rs232 MSR hardware error", "AllDevices", 4);
   }

   protected void processEvent(byte[] data) {
      int inLen = data.length;
      if (this.isTracerOn()) {
         this.traceNormal(": joining events");
      }

      if (this.msrState == 0) {
         if (data[0] != 37 && data[0] != 59 && data[0] != 43 && data[0] != 69) {
            if (this.isTracerOn()) {
               this.traceNormal(": data with-out start sentinel!!!");
            }

            this.msrState = 0;
            this.msrDataObtained = new byte[0];
         } else {
            if (this.isTracerOn()) {
               this.traceNormal(": storing first data");
            }

            this.msrDataObtained = new byte[inLen];
            System.arraycopy(data, 0, this.msrDataObtained, 0, inLen);
         }

         if (this.msrDataObtained[inLen - 1] != 63 && this.msrDataObtained[inLen - 1] != 69) {
            if (this.isTracerOn()) {
               this.traceNormal(": Wait for more data");
            }

            this.msrState = 1;
         } else {
            if (this.isTracerOn()) {
               this.traceNormal(": events joined");
            }

            this.parseDataAndFireEvent();
            this.msrState = 0;
         }
      } else if (this.msrState == 1) {
         byte[] tmp = new byte[inLen + this.msrDataObtained.length];
         System.arraycopy(this.msrDataObtained, 0, tmp, 0, this.msrDataObtained.length);
         System.arraycopy(data, 0, tmp, this.msrDataObtained.length, inLen);
         this.msrDataObtained = tmp;
         if (this.msrDataObtained[this.msrDataObtained.length - 1] == 63 || this.msrDataObtained[this.msrDataObtained.length - 1] == 69) {
            if (this.isTracerOn()) {
               this.traceNormal(": events joined");
            }

            this.parseDataAndFireEvent();
            this.msrState = 0;
         }
      }

      if (this.isTracerOn()) {
         this.traceNormal(": exiting processEvent");
      }
   }

   protected void fireDirectIOEvent(byte[] data) {
      this.eventHelper = this.getHandle().getEventHelper();
      this.eventHelper.fireDirectIOEvent(new DirectIOEvent(this, data));
      if (this.isTracerOn()) {
         this.traceNormal("DirectIOEvent fired");
      }
   }

   protected void parseDataAndFireEvent() {
      this.rs232MSRHelper.parseData(this.msrDataObtained);
      this.eventHelper.fireDataEvent(this.createMSRDataEvent());
      if (this.isTracerOn()) {
         this.traceNormal("DataEvent fired");
      }
   }

   public MSRDataEvent createMSRDataEvent() {
      MSRDataEvent msrDataEvent = new MSRDataEvent(
         this, this.rs232MSRHelper.getTrack1(), this.rs232MSRHelper.getTrack2(), this.rs232MSRHelper.getTrack3(), this.rs232MSRHelper.getTrackJIS_II()
      );
      if (this.rs232MSRHelper.cardHasErrorsInTracks()) {
         msrDataEvent.setErrorInSomeTrack(true);
         if (this.rs232MSRHelper.hasErrorInTrack1_JIS_II()) {
            if (this.getDeviceType() == 3300) {
               msrDataEvent.setErrorInTrack1(true);
            } else {
               msrDataEvent.setErrorInTrack4(true);
            }
         }

         if (this.rs232MSRHelper.hasErrorInTrack2()) {
            msrDataEvent.setErrorInTrack2(true);
         }

         if (this.rs232MSRHelper.hasErrorInTrack3()) {
            msrDataEvent.setErrorInTrack3(true);
         }
      }

      return msrDataEvent;
   }

   public void init() throws HandleException {
      super.init();
      this.eventHelper = this.getHandle().getEventHelper();
      this.rs232MSRHelper = new MSRDataHelper(this.deviceType, (byte)37, (byte)59, (byte)43, (byte)63);
      this.rs232Adapter = (Rs232PortCommAdapter)this.getRs232Port();
      this.rs232Adapter.setReaderStrategy(this.msrReader);
      if (this.watcherEnabled) {
         this.rs232Adapter.getOnlineWatcher().setEnable(this.watcherEnabled, new byte[]{0, 32}, (long)this.timeout);
      }

      this.traceNormal("watcherEnabled : " + this.watcherEnabled + " timeout : " + this.timeout);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitMSR(this);
   }

   public DevCat getDevCat() {
      return DevCats.MSR_DEVCAT;
   }

   public short getECLevel() {
      return -1;
   }

   public boolean isFlashable() {
      return false;
   }

   public int getDeviceType() {
      return this.deviceType;
   }

   public void setDeviceType(byte devType) {
      this.deviceType = devType;
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd.getCode() == 600) {
         this.getLogHelper().addLogEntry(1014, cmd.getName() + " Command not supported for Rs232 MSR", "MSR", 1);
      } else if (cmd.getCode() == 103) {
         SystemCmd.DeviceInfoRequestCmd infoCmd = (SystemCmd.DeviceInfoRequestCmd)cmd;
         if (this.getDeviceType() != 3300 && this.getDeviceType() != 3301) {
            infoCmd.setDeviceType(2001);
         } else {
            infoCmd.setDeviceType(this.deviceType);
         }

         infoCmd.setDeviceId(3328);
      } else if (cmd.getCode() != 101 && cmd.getCode() != 100) {
         if (cmd.getCode() != 104) {
            throw new HandleException("Invalid MSRCmd object submitted!");
         }

         super.submit(cmd.toBytes());
      } else {
         this.getLogHelper().addLogEntry(1014, cmd.getName() + " Command not supported for Rs232 MSR", "MSR", 1);
      }

      cmd.setCompleted(true);
   }
}
