package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jsio.event.SioDeviceDataEvent;
import com.ibm.jutil.Timer;
import com.ibm.jutil.Timerable;
import com.ibm.jutil.Util;
import com.ibm.posj.AbstractHandleCmd;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.MSRCmd;
import com.ibm.posj.MSRDevInfoConst;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.MSRHandleImp;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.MSRDataEvent;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.MSRTrackParser;
import com.ibm.posj.util.PosjUtil;
import com.ibm.posj.util.RetryHelper;

public class Rs485MSRHandleImp extends AbstractRs485HandleImp implements MSRHandleImp {
   private MSRTrackParser trackParser = null;
   private int msrType = 2001;
   private int msrId = 3303;
   private RetryHelper retryHelper = null;
   private int timeout = 0;
   private AbstractHandleCmd.DefaultResult defaultResult = new AbstractHandleCmd.DefaultResult();
   private boolean errorInPreviousEvent = false;
   private MSRDataEvent previousDataEvent;
   private boolean isPreviousEventError = false;
   private Rs485MSRHandleImp.MsrTimerable kTimerable = new Rs485MSRHandleImp.MsrTimerable();
   private Timer kTimer = new Timer(this.kTimerable);
   private boolean isJUCCDBCS = false;
   public static final int LENGTH_BYTES = 1;
   public static final int MSR_THREE_TRACKS_MAX_LENGTH = 288;
   public static final int MSR_TWO_HEAD_MAX_LENGTH = 138;
   public static final int MSR_WRITE_TIME_OUT_BIT = 4;
   public static final int MSR_WRITE_BIT = 5;
   public static final byte MSR_TYPE_BYTE_INDEX = 3;
   public static final byte MSR_WRITE_LENGTH = 1;
   public static final byte WRITE_MSR_INDEX = 2;
   public final int WAIT_FOR_ERROR_EVENT = 300;

   public Rs485MSRHandleImp(HandleKey key, SioDevice sioDevice) {
      super(key, sioDevice);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitMSR(this);
   }

   public DevCat getDevCat() {
      return DevCats.MSR_DEVCAT;
   }

   public void init() throws HandleException {
      super.init();
   }

   public void parseDataAndFireEvent(byte[] data) {
      if (this.isTracerOn()) {
         this.traceNormal("--> parseDataAndFireEvent");
      }

      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
      if (this.getHandle().isDirectIOMode()) {
         eventHelper.fireDirectIOEvent(new DirectIOEvent(this, data));
         if (this.isTracerOn()) {
            this.traceNormal("DirectIOEvent Ocurred");
         }
      }

      try {
         MSRTrackParser.checkForCmdRejectError(data);
      } catch (HandleException var7) {
         if (this.isTracerOn()) {
            this.traceNormal("Command reject error");
         }

         eventHelper.fireErrorEvent(new ErrorEvent(this, -101));
      } catch (ArrayIndexOutOfBoundsException var8) {
         if (this.isTracerOn()) {
            this.traceNormal("Command read error!!");
         }
      }

      if (MSRTrackParser.isDeviceInfoResponseData(data)) {
         this.setMSRType(data[3]);
      }

      RetryHelper rh = this.getRetryHelper();
      if (rh.isCmdPending()) {
         if (rh.getLastCmd().getCode() == 601) {
            byte output = data[1];
            if (PosjUtil.isBitSelected(output, 4)) {
               this.defaultResult.setInError(true);
               this.defaultResult.setErrorCode(-604);
               rh.getLastCmd().setResult(this.defaultResult);
               rh.getLastCmd().setCompleted(true);
            } else if (PosjUtil.isBitSelected(output, 5)) {
               this.defaultResult.setInError(false);
               rh.setCmdPending(false);
               rh.setLastResponse(data);
               rh.notifyAllCmdPending();
            } else if (this.isTracerOn()) {
               this.traceNormal("Skippin response ! ");
            }
         } else {
            this.defaultResult.setInError(false);
            rh.setCmdPending(false);
            rh.setLastResponse(data);
            rh.notifyAllCmdPending();
         }
      }

      if (MSRTrackParser.isDataAvailable(data)) {
         this.trackParser = new MSRTrackParser(2, this.msrType, 1);
         this.trackParser.setTracksMaxLengths(this.isJUCCDBCS);
         if (this.isTracerOn()) {
            this.traceNormal("Data found on event");
         }

         if (data.length > this.trackParser.getMaxLength(this.isJUCCDBCS)) {
            if (this.isTracerOn()) {
               this.traceNormal("Too much data received");
            }

            eventHelper.fireErrorEvent(new ErrorEvent(this, -602));
         } else {
            try {
               int msrStatus = (data[0] << 8) + data[1];
               MSRDataEvent dataEvent = null;
               this.trackParser.separateInTracks(data);
               if (this.msrType == 3300) {
                  dataEvent = this.createMSRDataEvent(msrStatus, this.trackParser.getTrack1(), this.trackParser.getTrack2(), this.trackParser.getTrack3());
               } else if (this.msrType == 3301 || this.msrType == 3302) {
                  dataEvent = this.createMSRDataEvent(msrStatus, this.trackParser.getTrack2(), this.trackParser.getTrackJISII());
               }

               this.previousDataEvent = dataEvent;
               if (this.validateFireErrorEvent(dataEvent.getErrorInSomeTrack())) {
                  this.fireDataEvent(dataEvent);
               }
            } catch (HandleException var6) {
               if (this.isTracerOn()) {
                  this.traceNormal("Handle process data error");
               }

               eventHelper.fireErrorEvent(new ErrorEvent(this, -103, var6.toString()));
            }
         }
      } else if (data.length < 1) {
         if (this.isTracerOn()) {
            this.traceNormal("Status length not valid");
         }

         eventHelper.fireErrorEvent(new ErrorEvent(this, -601));
      }

      if (this.isTracerOn()) {
         this.traceNormal("<-- parseDataAndFireEvent");
      }
   }

   protected boolean validateFireErrorEvent(boolean isError) {
      boolean validEvent = true;
      if (this.msrType == 3301) {
         this.setErrorInPreviousEventFlag(false);
         if (!this.isPreviousEventError && isError) {
            this.setErrorInPreviousEventFlag(true);
            this.kTimer.setTime(300);
            this.kTimer.start();
            validEvent = false;
         }

         this.isPreviousEventError = isError;
      }

      return validEvent;
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("--> submit" + cmd.getName());
      }

      if (cmd.getCode() == 600) {
         this.submitConfigMSRCmd((MSRCmd.ConfigMSRCmd)cmd);
      } else if (cmd instanceof SystemCmd) {
         this.submitSystemCmd((SystemCmd)cmd);
      } else if (cmd.getCode() == 601) {
         this.submitWriteMSRCmd((MSRCmd.WriteMSRCmd)cmd);
      } else {
         if (cmd.getCode() != 104) {
            throw new HandleException("Invalid MSRCmd object submitted!");
         }

         this.submitCmd(cmd, cmd.toBytes(), 1, this.timeout);
      }

      if (this.isTracerOn()) {
         this.traceNormal("<-- submit");
      }
   }

   protected void submitWriteMSRCmd(MSRCmd.WriteMSRCmd msrCmd) throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("--> submitWriteMSRCmd");
      }

      if (this.msrType != 3302) {
         throw new HandleException("Invalid MSR attempted to use a write!");
      } else {
         byte[] data = msrCmd.getData();
         int lengthToWrite = MSRDevInfoConst.WRITE_MSR_DATA_CMD.length + 1 + data.length;
         byte[] writeData = new byte[lengthToWrite];
         System.arraycopy(MSRDevInfoConst.WRITE_MSR_DATA_CMD, 0, writeData, 0, MSRDevInfoConst.WRITE_MSR_DATA_CMD.length);
         writeData[2] = (byte)data.length;
         System.arraycopy(data, 0, writeData, 3, data.length);
         if (this.isTracerOn()) {
            this.traceNormal("Write command : " + Util.toFormatedHexString(writeData));
         }

         this.submitCmd(msrCmd, writeData, 1, this.timeout);
         if (this.isTracerOn()) {
            this.traceNormal("<-- submitWriteMSRCmd");
         }
      }
   }

   protected void submitCmd(HandleCmd handleCmd, byte[] cmd, int times, int waitFor) throws HandleException {
      RetryHelper rh = this.getRetryHelper();
      rh.setLastCmd(handleCmd);
      rh.setCmdPending(true);
      int retry = 0;

      while (rh.isCmdPending() && retry++ < times) {
         this.submitSync(cmd);

         try {
            rh.waitCmdPending((long)waitFor);
         } catch (InterruptedException var8) {
         }

         if (!rh.isCmdPending()) {
            return;
         }
      }

      if (retry >= times && rh.isCmdPending()) {
         throw new HandleException("timeout while waiting for " + handleCmd.getName() + " command to complete");
      }
   }

   protected void submitConfigMSRCmd(MSRCmd.ConfigMSRCmd msrCmd) throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("--> submitConfigMSRCmd");
      }

      byte enabledTracks = 0;
      if (msrCmd.isEnabledISOtrack1() && this.msrType != 3301 && this.msrType != 3302) {
         enabledTracks++;
      }

      if (msrCmd.isEnabledISOtrack2()) {
         enabledTracks = (byte)(enabledTracks + 2);
      }

      if (msrCmd.isEnabledISOtrack3() && this.msrType != 3301 && this.msrType != 3302) {
         enabledTracks = (byte)(enabledTracks + 4);
      }

      if (msrCmd.isEnabledJIS_IItrack()) {
         enabledTracks = (byte)(enabledTracks + 8);
      }

      byte[] configMSRCmd = null;
      if (enabledTracks == 0) {
         this.timeout = msrCmd.getTimeout();
         configMSRCmd = new byte[3];
         System.arraycopy(MSRDevInfoConst.ENABLE_WRITE_MSR_DATA_CMD, 0, configMSRCmd, 0, MSRDevInfoConst.ENABLE_WRITE_MSR_DATA_CMD.length);
         configMSRCmd[2] = (byte)(this.timeout / 100);
      } else {
         configMSRCmd = new byte[]{1, enabledTracks};
      }

      this.submitCmd(msrCmd, configMSRCmd, 1, 500);
      if (this.isTracerOn()) {
         this.traceNormal("<-- submitConfigMSRCmd");
      }
   }

   protected void submitSystemCmd(SystemCmd systemCmd) throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("--> submitSystemCmd");
      }

      try {
         if (systemCmd.getCode() != 100) {
            if (systemCmd.getCode() == 101) {
               this.submitSync(MSRDevInfoConst.MSR_STATUS_REQ_CMD);
            } else if (systemCmd.getCode() == 103) {
               this.submitDevInfoCmd((SystemCmd.DeviceInfoRequestCmd)systemCmd);
            } else {
               if (systemCmd.getCode() != 104) {
                  throw new HandleException("Invalid SystemCmd.getCode() value!");
               }

               this.submitCmd(systemCmd, systemCmd.toBytes(), 1, this.timeout);
            }
         }
      } catch (HandleException var3) {
         ((AbstractHandleCmd.DefaultResult)systemCmd.getResult()).setInError(true);
         throw new HandleException("Error while sending SystemCmd to MSR", var3);
      }

      if (this.isTracerOn()) {
         this.traceNormal("<-- submitSystemCmd");
      }
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd devInfoCmd) throws HandleException {
      this.submitCmd(devInfoCmd, MSRDevInfoConst.MSR_DEV_INFO_REQ_CMD, 1, 500);
      devInfoCmd.setDeviceType(this.msrType);
      devInfoCmd.setDeviceId(this.msrId);
      if (this.isTracerOn()) {
         this.traceNormal("devInfoCmd.getDeviceId() --> " + devInfoCmd.getDeviceId());
         this.traceNormal("devInfoCmd.getDeviceType() --> " + devInfoCmd.getDeviceType());
      }
   }

   protected MSRDataEvent createMSRDataEvent(int msrStatus, byte[] trk2, byte[] trkJISII) {
      if (this.isTracerOn()) {
         this.traceNormal("--> createMSRDataEvent(JUCC)");
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
         this.traceNormal("<-- createMSRDataEvent(JUCC)");
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

   protected void dataEventOccurred(SioDeviceDataEvent event) {
      byte[] data = event.getData();
      if (this.isTracerOn()) {
         this.traceNormal("--> dataEventOcurred : " + Util.toFormatedHexString(data));
      }

      this.parseDataAndFireEvent(data);
      if (this.isTracerOn()) {
         this.traceNormal("<-- dataEventOcurred : ");
      }
   }

   protected void reinitialize() throws HandleException {
      super.reinitialize();
   }

   protected void setMSRType(byte dataByte) {
      switch (dataByte) {
         case 1:
            this.msrType = 3300;
            this.msrId = 3304;
            break;
         case 2:
            this.msrId = 3304;
            this.msrType = 3301;
            break;
         case 3:
            this.isJUCCDBCS = true;
            this.msrId = 3305;
            this.msrType = 3301;
            break;
         case 4:
         default:
            this.msrId = 3303;
            this.msrType = 2001;
            break;
         case 5:
            this.msrId = 3306;
            this.msrType = 3302;
      }
   }

   protected RetryHelper getRetryHelper() {
      if (this.retryHelper == null) {
         this.retryHelper = new RetryHelper();
      }

      return this.retryHelper;
   }

   protected void fireDataEvent(MSRDataEvent dataEvent) {
      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
      if (this.isTracerOn()) {
         this.traceNormal("--> ******* fireDataEvent:");
      }

      eventHelper.fireDataEvent(dataEvent);
   }

   public boolean getErrorInPreviousEventFlag() {
      return this.errorInPreviousEvent;
   }

   public void setErrorInPreviousEventFlag(boolean error) {
      this.errorInPreviousEvent = error;
   }

   protected class MsrTimerable implements Timerable {
      public void timerExpired() {
         if (Rs485MSRHandleImp.this.getErrorInPreviousEventFlag()) {
            if (Rs485MSRHandleImp.this.isTracerOn()) {
               Rs485MSRHandleImp.this.traceMaximum("-->timerExpired()");
            }

            Rs485MSRHandleImp.this.fireDataEvent(Rs485MSRHandleImp.this.previousDataEvent);
         }
      }
   }
}
