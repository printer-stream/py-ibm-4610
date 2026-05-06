package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.HidException;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.Util;
import com.ibm.posj.DefaultScaleCmdV;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.RuntimePosException;
import com.ibm.posj.ScaleCmd;
import com.ibm.posj.ScaleCmdVisitor;
import com.ibm.posj.ScaleConfig;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.ScaleHandleImp;
import com.ibm.posj.event.DataEvent;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.scale.OEMScaleHandleState;
import com.ibm.posj.scale.ScaleDataParser;
import com.ibm.posj.scale.ScaleHandleState;
import com.ibm.posj.util.DefaultHandleCmdV;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public class HidScaleHandleImp extends AbstractHidHandleImp implements HidHandleImp, ScaleHandleImp {
   private Object configLock = new Object();
   private Object zeroLock = new Object();
   private Object systemCmdLock = new Object();
   private boolean isWeightCmdPending = false;
   private boolean isSystemCmdPending = false;
   private boolean isConfigCmdPending = false;
   private boolean isZeroCmdPending = false;
   private Handle.EventHelper eventHelper = null;
   private ScaleDataParser scaleDataParser = null;
   private ScaleCmdVisitor submitScaleCmdV = new HidScaleHandleImp.SubmitScaleCmdV();
   private HidScaleHandleImp.HandleCmdFilterV handleCmdFilterV = new HidScaleHandleImp.HandleCmdFilterV();
   private int weight = 0;
   protected boolean errorConfiguring = false;
   protected boolean errorZeroing = false;
   ScaleHandleState state = null;
   private HandleCmd handleCmd = null;
   public static final int MAX_CMD_RETRY = 3;
   public static final int CMD_TIMEOUT = 500;
   public static final byte[] SCALE_TEST_REQ_CMD = new byte[]{0, 16};
   public static final byte[] SCALE_STATUS_REQ_CMD = new byte[]{0, 32};
   public static final byte[] SCALE_RESET_CMD = new byte[]{0, 64};
   public static final byte[] SCALE_CLEAR_DISPLAY_CMD = new byte[]{6, 0};
   public static final byte[] SCALE_CONFIG_CMD = new byte[]{32, 0, 0, 0, 0};
   public static final byte[] SCALE_ENABLE_EXTENDED_STATUS_CMD = new byte[]{4, 0};
   public static final byte[] SCALE_DISABLE_EXTENDED_STATUS_CMD = new byte[]{5, 0};
   public static final byte[] SCALE_REPORT_CONFIG_CMD = new byte[]{33, 0};
   public static final byte[] SCALE_ENGLISH_WEIGHT_REQ_CMD = new byte[]{1, 0};
   public static final byte[] SCALE_METRIC_WEIGHT_REQ_CMD = new byte[]{2, 0};
   public static final byte[] ZERO_SCALE_CMD = new byte[]{3, 0};

   public HidScaleHandleImp(HandleKey key, HidDevice device) {
      super(key, device);
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidScaleHandleImp(this);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitScale(this);
   }

   public DevCat getDevCat() {
      return DevCats.SCALE_DEVCAT;
   }

   public void init() throws HandleException {
      super.init();
      this.initialize();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            this.handleCmdFilterV.reset();
            cmd.accept(this.handleCmdFilterV);
            if (this.handleCmdFilterV.isSystemCmd()) {
               switch (cmd.getCode()) {
                  case 100:
                     this.submitSetReportRequestCmd("TestReqCmd", (byte)2, SCALE_TEST_REQ_CMD);
                     this.waitForSystemCmdComplete();
                     break;
                  case 101:
                     this.submitSetReportRequestCmd("StatusReqCmd", (byte)2, SCALE_STATUS_REQ_CMD);
                     this.waitForSystemCmdComplete();
                     break;
                  case 102:
                     this.submitSetReportRequestCmd("ResetCmd", (byte)2, SCALE_RESET_CMD);
                     this.waitForSystemCmdComplete();
                     break;
                  case 103:
                     if (this.isTracerOn()) {
                        this.traceNormal("DeviceInfoRequestCmd called");
                     }

                     SystemCmd.DeviceInfoRequestCmd devInfoCmd = (SystemCmd.DeviceInfoRequestCmd)cmd;
                     devInfoCmd.setDeviceId(4001);
                     devInfoCmd.setFirmwareLevel(this.getBCDLevel());
                     devInfoCmd.setSerialNumber(this.getSerialNumber());
               }
            } else {
               if (!this.handleCmdFilterV.isScaleCmd()) {
                  throw new HandleException("Invalid ScaleCmd submitted!");
               }

               ((HidScaleHandleImp.SubmitScaleCmdV)this.submitScaleCmdV).clearHandleException();
               ((ScaleCmd)cmd).accept(this.submitScaleCmdV);
               HandleException he = ((HidScaleHandleImp.SubmitScaleCmdV)this.submitScaleCmdV).getHandleException();
               if (he != null) {
                  throw he;
               }
            }
         } catch (HandleException var6) {
            this.setHandleCmdResultInError(cmd, true);
            throw var6;
         } finally {
            cmd.setCompleted(true);
         }
      }
   }

   public ScaleHandleState getHandleState() {
      if (this.state == null) {
         this.state = new OEMScaleHandleState();
      }

      return this.state;
   }

   protected void initialize() throws HandleException {
      this.traceNormal("initializing HandleImp");
      this.weight = 0;
      this.eventHelper = this.getHandle().getEventHelper();
      this.scaleDataParser = new ScaleDataParser();
      this.isSystemCmdPending = true;
      this.submitInitCmd();
      this.waitForSystemCmdComplete();
   }

   protected void reinitialize() throws HandleException {
      this.initialize();
   }

   protected void reportEventOccurred(ReportEvent rE) {
      if (this.isTracerOn()) {
         this.traceNormal("reportEventOccurred:: " + Util.toFormatedHexString(rE.getData()));
      }

      this.weight = 0;

      try {
         this.scaleDataParser.parse(rE.getData());
         if (this.scaleDataParser.cmdRejected() || this.scaleDataParser.unacceptableCommand()) {
            this.traceNormal("Command was rejected..  sending errorEvent - report - " + this.scaleDataParser.toString());
            this.eventHelper.fireErrorEvent(new ErrorEvent(this, -101));
         }

         if (this.scaleDataParser.isConfigData()) {
            this.traceNormal(" scale config data received - report - " + this.scaleDataParser.toString());
            this.eventHelper.fireDataEvent(new DataEvent(this, this.scaleDataParser.getData()));
         }

         if (this.scaleDataParser.hardwareError()) {
            this.traceNormal("scale hardware error  - report -" + this.scaleDataParser.toString());
            this.eventHelper.fireErrorEvent(new ErrorEvent(this, -1406));
         }

         if (this.isWeightCmdPending) {
            this.isWeightCmdPending = false;
            this.checkReport();
         }

         if (this.isSystemCmdPending) {
            this.isSystemCmdPending = false;
            if (this.isTracerOn()) {
               this.traceNormal("System cmd response received!");
            }
         }

         if (this.isConfigCmdPending && !this.isSystemCmdPending) {
            if (!this.scaleDataParser.configSuccessful()) {
               this.errorConfiguring = true;
            }

            this.isConfigCmdPending = false;
         }

         if (this.isZeroCmdPending) {
            this.isZeroCmdPending = false;
            if (!this.scaleDataParser.centerOfZero()) {
               this.errorZeroing = true;
            }
         }
      } catch (HandleException var3) {
         this.traceNormal("HandleException occurred while parsing Scale data");
         this.eventHelper.fireErrorEvent(new ErrorEvent(this, -103));
      }
   }

   protected void checkReport() throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("scale check report  - " + this.scaleDataParser.toString());
      }

      if (this.scaleDataParser.notReadyforWeighing()) {
         this.eventHelper.fireErrorEvent(new ErrorEvent(this, -1401));
      } else if (this.scaleDataParser.dataValueError()) {
         this.eventHelper.fireErrorEvent(new ErrorEvent(this, -1403));
      } else if (this.scaleDataParser.readError()) {
         this.eventHelper.fireErrorEvent(new ErrorEvent(this, -1404));
      } else if (this.scaleDataParser.displayRequiredAbsent()) {
         this.eventHelper.fireErrorEvent(new ErrorEvent(this, -1405));
      } else if (this.scaleDataParser.underZero()) {
         this.eventHelper.fireErrorEvent(new ErrorEvent(this, -1407));
      } else if (this.scaleDataParser.overCapacity()) {
         this.eventHelper.fireErrorEvent(new ErrorEvent(this, -1408));
      } else if (this.scaleDataParser.centerOfZero()) {
         this.eventHelper.fireErrorEvent(new ErrorEvent(this, -1409));
      } else if (this.scaleDataParser.zeroingRequired()) {
         this.eventHelper.fireErrorEvent(new ErrorEvent(this, -1410));
      } else if (this.scaleDataParser.warmupInProgress()) {
         this.eventHelper.fireErrorEvent(new ErrorEvent(this, -1411));
      } else if (this.scaleDataParser.duplicateWeight()) {
         this.eventHelper.fireErrorEvent(new ErrorEvent(this, -1412));
      } else if (!this.scaleDataParser.containsWeight()) {
         this.eventHelper.fireErrorEvent(new ErrorEvent(this, -1402));
      } else {
         this.weight = this.scaleDataParser.getWeight();
         this.eventHelper.fireStatusEvent(new StatusEvent(this, this.weight));
      }
   }

   protected void submitSetReportRequestCmd(String name, byte reportType, byte[] cmd) throws HandleException {
      try {
         this.getHidDevice().setReport(reportType, (byte)0, (short)cmd.length, cmd);
      } catch (HidException var5) {
         throw new HandleException("Error while sending " + name + " to HidDevice", var5);
      }
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

   protected void waitForConfigCmdComplete() throws HandleException {
      if (this.isConfigCmdPending) {
         synchronized (this.configLock) {
            if (this.isTracerOn()) {
               this.traceNormal("waiting for config Cmd to complete");
            }

            try {
               this.configLock.wait(500L);
            } catch (InterruptedException var4) {
            }
         }
      }

      if (this.isConfigCmdPending && this.isTracerOn()) {
         this.traceNormal("Timeout occurred while waiting for config cmd to complete");
      }
   }

   protected void waitForZeroCmdComplete() throws HandleException {
      if (this.isZeroCmdPending) {
         this.traceNormal("waiting for zero Cmd to complete");

         try {
            Thread.sleep(500L);
         } catch (InterruptedException var2) {
         }
      }

      if (this.isZeroCmdPending) {
         this.traceNormal("Timeout occurred while waiting for zero  cmd to complete");
         throw new HandleException("Timeout occurred while waiting for zero cmd to complete");
      }
   }

   protected void submitInitCmd() throws HandleException {
      try {
         byte[] statusRequestCmdBytes = SCALE_STATUS_REQ_CMD;
         this.traceNormal("submitInitCmd: statusRequestCmdBytes=" + Util.toFormatedHexString(statusRequestCmdBytes));
         if (statusRequestCmdBytes.length == 0) {
            this.traceNormal("Invalid cmd length");
            throw new HandleException("Invalid statusRequestCmdBytes sent to Scale");
         } else {
            this.getHidDevice().setReport((byte)2, (byte)0, (short)((byte)statusRequestCmdBytes.length), statusRequestCmdBytes);
         }
      } catch (HidException var3) {
         String msg = "Error while sending SystemCmd to HID Scale";
         this.traceNormal(msg);
         throw new HandleException(msg, var3);
      }
   }

   protected void waitForSystemCmdComplete() throws HandleException {
      int retry = 0;

      while (this.isSystemCmdPending && retry < 3) {
         synchronized (this.systemCmdLock) {
            try {
               this.systemCmdLock.wait(500L);
            } catch (InterruptedException var5) {
            }
         }

         if (!this.isSystemCmdPending) {
            return;
         }

         this.submitInitCmd();
         this.traceNormal("SystemCmd not completed, retry = " + ++retry);
      }

      if (retry >= 3 && this.isSystemCmdPending) {
         throw new HandleException("Scale: timeout while waiting for system command to complete");
      }
   }

   private void setWeightUnits(ScaleConfig config) {
      byte devunits = config.getWeighMode();
      if (devunits == 1) {
         ScaleHandleState var10000 = this.getHandleState();
         this.getHandleState();
         var10000.setWeightUnit((byte)1);
      } else {
         ScaleHandleState var3 = this.getHandleState();
         this.getHandleState();
         var3.setWeightUnit((byte)0);
      }
   }

   protected class HandleCmdFilterV extends DefaultHandleCmdV {
      private boolean isScaleCmd = false;
      private boolean isSystemCmd = false;
      private boolean isInvalidCmd = false;

      public boolean isSystemCmd() {
         return this.isSystemCmd;
      }

      public boolean isScaleCmd() {
         return this.isScaleCmd;
      }

      public boolean isInvalidCmd() {
         return this.isInvalidCmd;
      }

      public void reset() {
         this.isInvalidCmd = false;
         this.isSystemCmd = false;
         this.isScaleCmd = false;
      }

      protected void visitHandleCmd(HandleCmd cmd) {
         this.isInvalidCmd = true;
      }

      public void visitSystemCmd(SystemCmd cmd) {
         this.isSystemCmd = true;
      }

      public void visitScaleCmd(ScaleCmd cmd) {
         this.isScaleCmd = true;
      }
   }

   protected class SubmitScaleCmdV extends DefaultScaleCmdV {
      protected HandleException handleException = null;

      public void visitClearDisplayCmd(ScaleCmd.ClearDisplayCmd cmd) {
         try {
            HidScaleHandleImp.this.submitSetReportRequestCmd("ClearDisplayCmd", (byte)2, HidScaleHandleImp.SCALE_CLEAR_DISPLAY_CMD);
         } catch (HandleException var3) {
            this.handleException = var3;
         }
      }

      public void visitConfigScaleCmd(ScaleCmd.ConfigScaleCmd cmd) {
         HidScaleHandleImp.this.isConfigCmdPending = true;
         HidScaleHandleImp.this.errorConfiguring = false;

         try {
            byte[] cmdBytes = HidScaleHandleImp.SCALE_CONFIG_CMD;
            byte[] configBytes = cmd.getConfig().getConfig();
            if (cmdBytes.length < 5) {
               throw new RuntimePosException("Invalid length of the command");
            }

            for (int i = 2; i < cmdBytes.length; i++) {
               cmdBytes[i] = configBytes[i - 2];
            }

            HidScaleHandleImp.this.submitSetReportRequestCmd("ConfigScaleCmd", (byte)2, cmdBytes);
            HidScaleHandleImp.this.waitForConfigCmdComplete();
         } catch (HandleException var5) {
            this.handleException = var5;
         }

         if (HidScaleHandleImp.this.errorConfiguring) {
            HidScaleHandleImp.this.traceNormal("Scale Config failure");
            this.handleException = new HandleException("Scale Config failure");
         }

         HidScaleHandleImp.this.setWeightUnits(cmd.getConfig());
      }

      public void visitEnableExtendedStatusCmd(ScaleCmd.EnableExtendedStatusCmd cmd) {
         try {
            if (cmd.getEnable()) {
               HidScaleHandleImp.this.submitSetReportRequestCmd("EnableExtendedStatusCmd", (byte)2, HidScaleHandleImp.SCALE_ENABLE_EXTENDED_STATUS_CMD);
            } else {
               HidScaleHandleImp.this.submitSetReportRequestCmd("EnableExtendedStatusCmd", (byte)2, HidScaleHandleImp.SCALE_DISABLE_EXTENDED_STATUS_CMD);
            }
         } catch (HandleException var3) {
            this.handleException = var3;
         }
      }

      public void visitReportConfigCmd(ScaleCmd.ReportConfigCmd cmd) {
         try {
            HidScaleHandleImp.this.submitSetReportRequestCmd("ReportConfigCmd", (byte)2, HidScaleHandleImp.SCALE_REPORT_CONFIG_CMD);
         } catch (HandleException var3) {
            this.handleException = var3;
         }
      }

      public void visitWeightReqCmd(ScaleCmd.WeightReqCmd cmd) {
         HidScaleHandleImp.this.isWeightCmdPending = true;

         try {
            if (cmd.getMode() == 0) {
               HidScaleHandleImp.this.submitSetReportRequestCmd("WeightReqCmd", (byte)2, HidScaleHandleImp.SCALE_ENGLISH_WEIGHT_REQ_CMD);
            } else {
               HidScaleHandleImp.this.submitSetReportRequestCmd("WeightReqCmd", (byte)2, HidScaleHandleImp.SCALE_METRIC_WEIGHT_REQ_CMD);
            }
         } catch (HandleException var3) {
            this.handleException = var3;
         }
      }

      public void visitZeroScaleCmd(ScaleCmd.ZeroScaleCmd cmd) {
         HidScaleHandleImp.this.isZeroCmdPending = true;

         try {
            HidScaleHandleImp.this.submitSetReportRequestCmd("ZeroScaleCmd", (byte)2, HidScaleHandleImp.ZERO_SCALE_CMD);
            HidScaleHandleImp.this.waitForZeroCmdComplete();
         } catch (HandleException var3) {
            this.handleException = var3;
         }

         if (HidScaleHandleImp.this.errorZeroing) {
            this.handleException = new HandleException("Error while zeroing the scale");
         }
      }

      public void clearHandleException() {
         this.handleException = null;
      }

      public HandleException getHandleException() {
         return this.handleException;
      }
   }
}
