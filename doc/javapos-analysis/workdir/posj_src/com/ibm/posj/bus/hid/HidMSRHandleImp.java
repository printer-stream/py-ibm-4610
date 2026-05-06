package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.HidException;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.Util;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.MSRCmd;
import com.ibm.posj.MSRDevInfoConst;
import com.ibm.posj.MSRHandle;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.MSRHandleImp;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.MSRDataEvent;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.MSRTrackParser;

public class HidMSRHandleImp extends AbstractHidHandleImp implements HidHandleImp, MSRHandleImp {
   private int msrType = 2001;
   private int productId = 3303;
   private boolean isJUCCDBCS = false;
   private MSRTrackParser trackParser = null;
   private byte[] usbConfigCmd = new byte[]{1, 0};
   private Object lockObj = new Object();
   public static final byte MSR_TYPE_BYTE_INDEX = 5;
   public static final byte DEV_INFO_CMD_LENGTH = 5;
   public static final int MSR_STATUS_LENGTH = 4;
   public static final int MSR_LENGTH_BYTE1_POSITION = 2;
   public static final int MSR_LENGTH_BYTE2_POSITION = 3;
   public static final int LENGTH_BYTES = 1;
   public static final String MSR_USB_FEATURE_REPORT_CMD_NAME = "Get feature report";
   public static final String MSR_STRING_ERROR_INVALID_COMMAND = "Invalid MSRCmd object submitted!";
   public static final String MSR_STRING_ERROR_SUBMIT_WHILE_FLASHING = "Attempting to submit to MSR while flashing";

   public HidMSRHandleImp(HandleKey key, HidDevice device, short id) {
      super(key, device);
      this.setProductId(id);
   }

   public void init() throws HandleException {
      super.init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (this.isLocked.isTrue()) {
         throw new HandleException("Attempting to submit to MSR while flashing");
      } else if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            try {
               if (cmd.getCode() == 103) {
                  this.submitDevInfoRequestCmd(cmd);
               } else if (cmd.getCode() == 104) {
                  this.submitCmd(cmd.getName(), cmd.toBytes());
               } else {
                  this.submitSetReportRequestCmd("MSRCmd", (byte)2, this.getCmdBytes(cmd));
               }
            } catch (IllegalArgumentException var7) {
               throw new HandleException("Invalid MSRCmd object submitted!");
            }
         } catch (HandleException var8) {
            this.setHandleCmdResultInError(cmd, true);
            throw var8;
         } finally {
            cmd.setCompleted(true);
         }
      }
   }

   public void submitDevInfoRequestCmd(HandleCmd cmd) throws HandleException {
      SystemCmd.DeviceInfoRequestCmd devInfoCmd = (SystemCmd.DeviceInfoRequestCmd)cmd;
      if (this.isTracerOn()) {
         this.traceNormal("--> submitDevInfoRequestCmd : " + devInfoCmd.getName());
      }

      if (this.msrType == 2001) {
         synchronized (this.lockObj) {
            this.submitSetReportRequestCmd(devInfoCmd.getName(), (byte)2, this.getCmdBytes(cmd));

            try {
               this.lockObj.wait(100L);
            } catch (InterruptedException var9) {
            }
         }

         if (this.msrType == 2001) {
            synchronized (this.lockObj) {
               this.submitSetReportRequestCmd(devInfoCmd.getName(), (byte)2, this.getCmdBytes(cmd));

               try {
                  this.lockObj.wait(100L);
               } catch (InterruptedException var7) {
               }
            }
         }
      }

      devInfoCmd.setDeviceId(this.productId);
      devInfoCmd.setDeviceType(this.msrType);
      devInfoCmd.setFirmwareLevel(this.getBCDLevel());
      devInfoCmd.setSerialNumber(this.getSerialNumber());
      if (this.isTracerOn()) {
         this.traceNormal("<-- submitDevInfoRequestCmd - MSR Keyboard is : " + this.productId + " msrType is : " + this.msrType);
      }
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidMSRHandleImp(this);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitMSR(this);
   }

   public DevCat getDevCat() {
      return DevCats.MSR_DEVCAT;
   }

   public boolean isComposite() {
      return true;
   }

   public void reinitialize() throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("--> reinitialize");
      }

      this.msrType = 2001;
      this.getMSRHandle().reset();
      if (this.isTracerOn()) {
         this.traceNormal("<-- reinitialize");
      }
   }

   protected void reportEventOccurred(ReportEvent rE) {
      byte[] data = rE.getData();
      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
      if (this.isTracerOn()) {
         this.traceNormal("--> reportEventOcurred : Status bytes -> " + Util.toFormatedHexString(data));
      }

      if (this.getHandle().isDirectIOMode()) {
         eventHelper.fireDirectIOEvent(new DirectIOEvent(this, data));
         if (this.isTracerOn()) {
            this.traceNormal("DirectIOEvent Ocurred");
         }
      }

      try {
         MSRTrackParser.checkForCmdRejectError(data);
         this.parseDataAndFireEvent(data);
      } catch (HandleException var7) {
         eventHelper.fireErrorEvent(new ErrorEvent(this, -101));
      } catch (ArrayIndexOutOfBoundsException var8) {
         if (this.isTracerOn()) {
            this.traceMaximum("Command read error!!");
         }
      }

      synchronized (this.lockObj) {
         this.lockObj.notifyAll();
      }

      if (this.isTracerOn()) {
         this.traceNormal("<-- reportEventOcurred");
      }
   }

   protected MSRHandle getMSRHandle() {
      return (MSRHandle)this.getHandle();
   }

   protected void submitSetReportRequestCmd(String name, byte reportType, byte[] cmd) throws HandleException {
      try {
         this.getHidDevice().setReport(reportType, (byte)0, (short)cmd.length, cmd);
      } catch (HidException var5) {
         throw new HandleException("Error while sending " + name + " to HidDevice", var5);
      }
   }

   protected void setMSRType(byte dataByte) {
      switch (dataByte) {
         case 1:
            this.msrType = 3300;
            break;
         case 3:
            this.isJUCCDBCS = true;
         case 2:
            this.msrType = 3301;
            break;
         case 4:
         default:
            this.msrType = 2001;
            break;
         case 5:
            this.msrType = 3302;
      }
   }

   protected MSRDataEvent createMSRDataEvent(int msrStatus, byte[] trk1, byte[] trk2, byte[] trk3, byte[] trkJISII) {
      if (this.isTracerOn()) {
         this.traceNormal("--> createMSRDataEvent");
      }

      MSRDataEvent msrDataEvent = new MSRDataEvent(this, trk1, trk2, trk3, trkJISII);
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
         this.traceNormal("<-- createMSRDataEvent");
      }

      return msrDataEvent;
   }

   protected void parseDataAndFireEvent(byte[] stBytes) throws HandleException {
      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
      boolean isDeviceInfoResponse = MSRTrackParser.isDeviceInfoResponseData(stBytes);
      boolean isDataAvailable = MSRTrackParser.isDataAvailable(stBytes);
      byte[] data = null;
      int dataLen = 0;
      if (isDeviceInfoResponse || isDataAvailable) {
         data = this.submitGetReportRequestCmd("Get feature report", (byte)3);
         dataLen = this.getLengthFromStatusBytes(data);
         if (this.isTracerOn()) {
            this.traceNormal("Data received -> " + Util.toFormatedHexString(data));
         }

         if (isDeviceInfoResponse && dataLen == 5) {
            this.setMSRType(data[5]);
         }

         if (isDataAvailable) {
            this.trackParser = new MSRTrackParser(4, this.msrType, 1);
            this.trackParser.setTracksMaxLengths(this.isJUCCDBCS);
            if (dataLen > this.trackParser.getMaxLength(this.isJUCCDBCS)) {
               if (this.isTracerOn()) {
                  this.traceNormal("length " + dataLen + " MaxLen " + this.trackParser.getMaxLength(this.isJUCCDBCS));
               }

               eventHelper.fireErrorEvent(new ErrorEvent(this, -602));
            } else {
               try {
                  int msrStatus = (data[0] << 8) + data[1];
                  MSRDataEvent dataEvent = null;
                  this.trackParser.separateInTracks(data);
                  dataEvent = this.createMSRDataEvent(
                     msrStatus, this.trackParser.getTrack1(), this.trackParser.getTrack2(), this.trackParser.getTrack3(), this.trackParser.getTrackJISII()
                  );
                  eventHelper.fireDataEvent(dataEvent);
               } catch (HandleException var9) {
                  eventHelper.fireErrorEvent(new ErrorEvent(this, -103, var9.toString()));
               }
            }
         } else if (dataLen < 3 && dataLen != 0) {
            eventHelper.fireErrorEvent(new ErrorEvent(this, -601));
         }

         if (this.isTracerOn()) {
            this.traceNormal("<-- parseDataAndFireEvent");
         }
      }
   }

   protected int getLengthFromStatusBytes(byte[] data) {
      int len = 0;
      len = data[2] + data[3];
      if (this.isTracerOn()) {
         this.traceNormal("Data read is " + len + " bytes");
      }

      return len;
   }

   protected byte[] submitGetReportRequestCmd(String name, byte reportType) throws HandleException {
      byte[] report;
      try {
         report = this.getHidDevice().getReport(reportType, (byte)0);
      } catch (HidException var5) {
         throw new HandleException("Error while sending " + name + " to HidDevice", var5);
      }

      if (report != null) {
         return report;
      } else {
         throw new HandleException("Error while receiving data from HidDevice");
      }
   }

   private byte[] getCmdBytes(HandleCmd cmd) throws IllegalArgumentException {
      int cmdCode = cmd.getCode();
      byte[] usbCmdBytes = null;
      if (cmdCode == 600) {
         usbCmdBytes = this.usbConfigCmd;
         usbCmdBytes[1] = this.getISOTracksBits((MSRCmd.ConfigMSRCmd)cmd);
      } else if (cmdCode == 102) {
         usbCmdBytes = MSRDevInfoConst.MSR_RESET_REQ_CMD;
      } else if (cmdCode == 101) {
         usbCmdBytes = MSRDevInfoConst.MSR_STATUS_REQ_CMD;
      } else if (cmdCode == 100) {
         usbCmdBytes = MSRDevInfoConst.MSR_TEST_REQ_CMD;
      } else {
         if (cmdCode != 103) {
            throw new IllegalArgumentException();
         }

         usbCmdBytes = MSRDevInfoConst.MSR_DEV_INFO_REQ_CMD;
      }

      if (this.isTracerOn()) {
         this.traceNormal("HidMSRHandleImp:: Command sent : " + cmd.getName() + ": " + Util.toFormatedHexString(usbCmdBytes));
      }

      return usbCmdBytes;
   }

   private byte getISOTracksBits(MSRCmd.ConfigMSRCmd cCmd) {
      byte enableTrackBits = 0;
      if (cCmd.isEnabledJIS_IItrack()) {
         enableTrackBits = (byte)(enableTrackBits + 8);
      }

      if (cCmd.isEnabledISOtrack3() && this.msrType != 3301 && this.msrType != 3302) {
         enableTrackBits = (byte)(enableTrackBits + 4);
      }

      if (cCmd.isEnabledISOtrack2()) {
         enableTrackBits = (byte)(enableTrackBits + 2);
      }

      if (cCmd.isEnabledISOtrack1() && this.msrType != 3301 && this.msrType != 3302) {
         enableTrackBits++;
      }

      return enableTrackBits;
   }

   private void setProductId(short id) {
      switch (id) {
         case 17922:
         case 17926:
            this.productId = 3307;
            break;
         case 17923:
            this.productId = 3308;
            break;
         case 17924:
            this.productId = 3309;
            break;
         case 17925:
            this.productId = 3312;
            break;
         case 17937:
            this.productId = 3313;
            break;
         case 17942:
            this.productId = 3333;
            break;
         case 18033:
         case 18034:
            this.productId = 3310;
            break;
         case 18035:
         case 18036:
            this.productId = 3311;
            break;
         case 18178:
         case 18182:
            this.productId = 3321;
            break;
         case 18179:
            this.productId = 3322;
            break;
         case 18180:
            this.productId = 3323;
            break;
         case 18181:
            this.productId = 3326;
            break;
         case 18193:
            this.productId = 3327;
            break;
         case 18198:
            this.productId = 3334;
            break;
         case 18289:
         case 18290:
            this.productId = 3324;
            break;
         case 18291:
         case 18292:
            this.productId = 3325;
            break;
         case 18434:
         case 18438:
         case 18439:
            this.productId = 3314;
            break;
         case 18435:
            this.productId = 3315;
            break;
         case 18436:
            this.productId = 3316;
            break;
         case 18437:
            this.productId = 3319;
            break;
         case 18449:
            this.productId = 3320;
            break;
         case 18454:
            this.productId = 3335;
            break;
         case 18545:
         case 18546:
            this.productId = 3317;
            break;
         case 18547:
         case 18548:
            this.productId = 3318;
            break;
         default:
            this.productId = 3303;
      }
   }
}
