package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jsio.event.SioDeviceDataEvent;
import com.ibm.jsio.event.SioDeviceErrorEvent;
import com.ibm.jsio.event.SioDeviceStatusEvent;
import com.ibm.posj.DefaultScaleCmdV;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.RuntimePosException;
import com.ibm.posj.ScaleCmd;
import com.ibm.posj.ScaleConfig;
import com.ibm.posj.ScaleHandleConst;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.ScaleHandleImp;
import com.ibm.posj.event.DataEvent;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.scale.DefaultScaleHandleState;
import com.ibm.posj.scale.ScaleDataParser;
import com.ibm.posj.scale.ScaleHandleState;
import com.ibm.posj.util.DefaultHandleCmdV;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public class Rs485ScaleHandleImp extends AbstractRs485HandleImp implements ScaleHandleImp, ScaleHandleConst {
   private Handle.EventHelper eventHelper = null;
   protected DefaultScaleHandleState state = new DefaultScaleHandleState();
   private HandleCmd handleCmd = null;
   private ScaleConfig scaleConfig = null;
   private ScaleDataParser scaleDataParser;
   private Rs485ScaleHandleImp.HandleCmdFilterV handleCmdFilterV = new Rs485ScaleHandleImp.HandleCmdFilterV();
   private Rs485ScaleHandleImp.SubmitScaleCmdV submitScaleCmdV = new Rs485ScaleHandleImp.SubmitScaleCmdV();
   private int weight = 0;
   private boolean isSystemCmdPending = false;
   private boolean isConfigCmdPending = false;
   private boolean isZeroCmdPending = false;
   private boolean isWeightCmdPending = false;
   private int cmdPending = 0;
   private boolean errorConfiguring = false;
   private boolean errorZeroing = false;
   private Object submitLock = new Object();
   private int scaleType = 0;
   private static final int DEFAULT_COMMAND_TIMEOUT = 500;
   public static final byte[] SCALE_TEST_REQ_CMD = new byte[]{0, 16};
   public static final byte[] SCALE_STATUS_REQ_CMD = new byte[]{0, 32};
   public static final byte[] SCALE_RESET_CMD = new byte[]{0, 64};
   public static final byte[] SCALE_CLEAR_DISPLAY_CMD = new byte[]{6};
   public static final byte[] SCALE_CONFIG_CMD = new byte[]{32, 0, 0, 0};
   public static final byte[] SCALE_ENABLE_EXTENDED_STATUS_CMD = new byte[]{4};
   public static final byte[] SCALE_DISABLE_EXTENDED_STATUS_CMD = new byte[]{5};
   public static final byte[] SCALE_REPORT_CONFIG_CMD = new byte[]{33};
   public static final byte[] SCALE_ENGLISH_WEIGHT_REQ_CMD = new byte[]{1};
   public static final byte[] SCALE_METRIC_WEIGHT_REQ_CMD = new byte[]{2};
   public static final byte[] ZERO_SCALE_CMD = new byte[]{3};

   public Rs485ScaleHandleImp(HandleKey key, SioDevice sioDevice, int scaleType) {
      super(key, sioDevice);
      this.scaleType = scaleType;
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitScale(this);
   }

   public DevCat getDevCat() {
      return DevCats.SCALE_DEVCAT;
   }

   public ScaleHandleState getHandleState() {
      if (this.state == null) {
         this.state = new DefaultScaleHandleState();
         this.initState();
      }

      return this.state;
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else if (!this.getHandle().isOnline()) {
         throw new HandleException("Attempted to submit a command when handle is offline");
      } else {
         try {
            this.handleCmdFilterV.reset();
            cmd.accept(this.handleCmdFilterV);
            if (this.handleCmdFilterV.isSystemCmd()) {
               switch (cmd.getCode()) {
                  case 100:
                  default:
                     break;
                  case 101:
                     this.cmdPending = 101;
                     this.isSystemCmdPending = true;
                     this.submitSync(SCALE_STATUS_REQ_CMD);

                     try {
                        synchronized (this.submitLock) {
                           this.submitLock.wait(500L);
                           break;
                        }
                     } catch (InterruptedException var15) {
                        if (this.isTracerOn()) {
                           this.traceNormal("InterruptedException received during submit Status Req Cmd - " + var15);
                        }

                        throw new HandleException("InterruptedException received during submit Status Req Cmd", var15);
                     }
                  case 102:
                     this.cmdPending = 102;
                     this.isSystemCmdPending = true;
                     this.submitSync(SCALE_RESET_CMD);

                     try {
                        synchronized (this.submitLock) {
                           this.submitLock.wait(500L);
                           break;
                        }
                     } catch (InterruptedException var14) {
                        if (this.isTracerOn()) {
                           this.traceNormal("InterruptedException received during submit Status Req Cmd - " + var14);
                        }

                        throw new HandleException("InterruptedException received during submit Status Req Cmd", var14);
                     }
                  case 103:
                     if (this.isTracerOn()) {
                        this.traceNormal("DeviceInfoRequestCmd called");
                     }

                     SystemCmd.DeviceInfoRequestCmd devInfoCmd = (SystemCmd.DeviceInfoRequestCmd)cmd;
                     devInfoCmd.setDeviceId(4002);
               }
            } else {
               if (!this.handleCmdFilterV.isScaleCmd()) {
                  throw new HandleException("Invalid ScaleCmd submitted!");
               }

               this.submitScaleCmdV.clearHandleException();
               ((ScaleCmd)cmd).accept(this.submitScaleCmdV);
               HandleException he = this.submitScaleCmdV.getHandleException();
               if (he != null) {
                  throw he;
               }
            }
         } catch (HandleException var16) {
            this.setHandleCmdResultInError(cmd, true);
            throw var16;
         } finally {
            cmd.setCompleted(true);
         }
      }
   }

   public void init() throws HandleException {
      super.init();
      this.initialize();
   }

   protected void errorEventOccurred(SioDeviceErrorEvent event) {
      synchronized (this.submitLock) {
         try {
            this.submitLock.notifyAll();
         } catch (IllegalMonitorStateException var5) {
            if (this.isTracerOn()) {
               this.traceNormal("IllegalMonitorStateException received during errorEventOccurred " + var5);
            }
         }
      }

      super.errorEventOccurred(event);
   }

   protected void statusEventOccurred(SioDeviceStatusEvent event) {
      synchronized (this.submitLock) {
         try {
            this.submitLock.notifyAll();
         } catch (IllegalMonitorStateException var5) {
            if (this.isTracerOn()) {
               this.traceNormal("IllegalMonitorStateException received during statusEventOccurred " + var5);
            }
         }
      }

      super.statusEventOccurred(event);
   }

   protected void dataEventOccurred(SioDeviceDataEvent event) {
      synchronized (this.submitLock) {
         try {
            this.submitLock.notifyAll();
         } catch (IllegalMonitorStateException var6) {
            if (this.isTracerOn()) {
               this.traceNormal("IllegalMonitorStateException received during dataEventOccurred " + var6);
            }
         }
      }

      if (this.isTracerOn()) {
         this.traceMaximum(
            "isConfigCmdPending - "
               + this.isConfigCmdPending
               + "isSystemCmdPending - "
               + this.isSystemCmdPending
               + "isWeightCmdPending - "
               + this.isWeightCmdPending
               + "isZeroCmdPending - "
               + this.isZeroCmdPending
         );
      }

      try {
         this.scaleDataParser.parse(event.getData());
         if (this.scaleDataParser.cmdRejected() || this.scaleDataParser.unacceptableCommand()) {
            this.traceNormal("Command was rejected..  sending errorEvent - report - " + this.scaleDataParser.toString());
            this.eventHelper.fireErrorEvent(new ErrorEvent(this, -101));
         }

         if (this.scaleDataParser.isConfigData()) {
            if (this.isTracerOn()) {
               this.traceNormal(" scale config data received - report - " + this.scaleDataParser.toString());
            }

            this.eventHelper.fireDataEvent(new DataEvent(this, this.scaleDataParser.getData()));
         }

         if (this.scaleDataParser.hardwareError()) {
            if (this.isTracerOn()) {
               this.traceNormal("scale hardware error  - report -" + this.scaleDataParser.toString());
            }

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
            if (this.scaleDataParser.zeroingRequired()) {
               this.errorZeroing = true;
            }
         }
      } catch (HandleException var5) {
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
         if (this.weight > 0) {
            this.eventHelper.fireStatusEvent(new StatusEvent(this, this.weight));
         }
      }
   }

   protected void initialize() throws HandleException {
      this.traceNormal("initializing HandleImp");
      this.weight = 0;
      this.eventHelper = this.getHandle().getEventHelper();
      this.scaleDataParser = new ScaleDataParser();
      this.initState();
   }

   protected void initState() {
      this.state.setCapDisplay(true);
      this.state.setCapDisplayText(false);
      this.state.setCapPriceCalculating(false);
      this.state.setCapTareWeight(false);
      if (this.scaleType == 1) {
         this.state.setCapZeroScale(false);
      } else {
         this.state.setCapZeroScale(false);
      }
   }

   protected void reinitialize() throws HandleException {
      super.reinitialize();
      this.initialize();
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
         if (Rs485ScaleHandleImp.this.scaleType == 1) {
            this.handleException = new HandleException("Extended status is not supported by this scale model");
         } else {
            try {
               Rs485ScaleHandleImp.this.submitSync(Rs485ScaleHandleImp.SCALE_CLEAR_DISPLAY_CMD);
               synchronized (Rs485ScaleHandleImp.this.submitLock) {
                  Rs485ScaleHandleImp.this.submitLock.wait(500L);
               }
            } catch (HandleException var5) {
               this.handleException = var5;
            } catch (InterruptedException var6) {
               if (Rs485ScaleHandleImp.this.isTracerOn()) {
                  Rs485ScaleHandleImp.this.traceNormal("InterruptedException received during visitClearDisplayCmd" + var6);
               }

               this.handleException = new HandleException("InterruptedException received during visitClearDisplayCmd", var6);
            }
         }
      }

      public void visitConfigScaleCmd(ScaleCmd.ConfigScaleCmd cmd) {
         if (Rs485ScaleHandleImp.this.scaleType == 1) {
            this.handleException = new HandleException("Configuration is not supported by this scale model");
         } else {
            Rs485ScaleHandleImp.this.scaleConfig = cmd.getConfig();
            Rs485ScaleHandleImp.this.isConfigCmdPending = true;
            Rs485ScaleHandleImp.this.errorConfiguring = false;

            try {
               byte[] cmdBytes = Rs485ScaleHandleImp.SCALE_CONFIG_CMD;
               byte[] configBytes = cmd.getConfig().getConfig();
               if (cmdBytes.length < 4) {
                  throw new RuntimePosException("Invalid length of the command");
               }

               cmdBytes[1] = configBytes[0];
               Rs485ScaleHandleImp.this.submitSync(cmdBytes);
               synchronized (Rs485ScaleHandleImp.this.submitLock) {
                  Rs485ScaleHandleImp.this.submitLock.wait(500L);
               }
            } catch (HandleException var7) {
               this.handleException = var7;
            } catch (InterruptedException var8) {
               if (Rs485ScaleHandleImp.this.isTracerOn()) {
                  Rs485ScaleHandleImp.this.traceNormal("InterruptedException received during visitConfigScaleCmd" + var8);
               }

               this.handleException = new HandleException("InterruptedException received during visitConfigScaleCmd", var8);
            }

            if (Rs485ScaleHandleImp.this.errorConfiguring) {
               Rs485ScaleHandleImp.this.traceNormal("Scale Config failure");
            }

            Rs485ScaleHandleImp.this.setWeightUnits(cmd.getConfig());
         }
      }

      public void visitEnableExtendedStatusCmd(ScaleCmd.EnableExtendedStatusCmd cmd) {
         if (Rs485ScaleHandleImp.this.scaleType == 1) {
            this.handleException = new HandleException("Extended status is not supported by this scale model");
         } else {
            try {
               if (cmd.getEnable()) {
                  Rs485ScaleHandleImp.this.submitSync(Rs485ScaleHandleImp.SCALE_ENABLE_EXTENDED_STATUS_CMD);
               } else {
                  Rs485ScaleHandleImp.this.submitSync(Rs485ScaleHandleImp.SCALE_DISABLE_EXTENDED_STATUS_CMD);
               }

               synchronized (Rs485ScaleHandleImp.this.submitLock) {
                  Rs485ScaleHandleImp.this.submitLock.wait(500L);
               }
            } catch (HandleException var5) {
               this.handleException = var5;
            } catch (InterruptedException var6) {
               if (Rs485ScaleHandleImp.this.isTracerOn()) {
                  Rs485ScaleHandleImp.this.traceNormal("InterruptedException received during visitEnableExtendedStatusCmd - " + var6);
               }

               this.handleException = new HandleException("InterruptedException received during visitEnableExtendedStatusCmd", var6);
            }
         }
      }

      public void visitReportConfigCmd(ScaleCmd.ReportConfigCmd cmd) {
         if (Rs485ScaleHandleImp.this.scaleType == 1) {
            this.handleException = new HandleException("Report config is not supported by this scale model");
         } else {
            try {
               Rs485ScaleHandleImp.this.submitSync(Rs485ScaleHandleImp.SCALE_REPORT_CONFIG_CMD);
               synchronized (Rs485ScaleHandleImp.this.submitLock) {
                  Rs485ScaleHandleImp.this.submitLock.wait(500L);
               }
            } catch (HandleException var5) {
               this.handleException = var5;
            } catch (InterruptedException var6) {
               if (Rs485ScaleHandleImp.this.isTracerOn()) {
                  Rs485ScaleHandleImp.this.traceNormal("InterruptedException received during visitReportConfigCmd" + var6);
               }

               this.handleException = new HandleException("InterruptedException received during visitReportConfigCmd", var6);
            }
         }
      }

      public void visitWeightReqCmd(ScaleCmd.WeightReqCmd cmd) {
         Rs485ScaleHandleImp.this.isWeightCmdPending = true;

         try {
            if (cmd.getMode() == 0) {
               Rs485ScaleHandleImp.this.submitSync(Rs485ScaleHandleImp.SCALE_ENGLISH_WEIGHT_REQ_CMD);
            } else {
               Rs485ScaleHandleImp.this.submitSync(Rs485ScaleHandleImp.SCALE_METRIC_WEIGHT_REQ_CMD);
            }

            synchronized (Rs485ScaleHandleImp.this.submitLock) {
               Rs485ScaleHandleImp.this.submitLock.wait(500L);
            }
         } catch (HandleException var5) {
            this.handleException = var5;
         } catch (InterruptedException var6) {
            if (Rs485ScaleHandleImp.this.isTracerOn()) {
               Rs485ScaleHandleImp.this.traceNormal("InterruptedException received during visitWeightReqCmd - " + var6);
            }

            this.handleException = new HandleException("InterruptedException received during visitWeightReqCmd", var6);
         }
      }

      public void visitZeroScaleCmd(ScaleCmd.ZeroScaleCmd cmd) {
         if (Rs485ScaleHandleImp.this.scaleType == 1) {
            this.handleException = new HandleException("Extended status is not supported by this scale model");
         } else {
            Rs485ScaleHandleImp.this.isZeroCmdPending = true;

            try {
               Rs485ScaleHandleImp.this.submitSync(Rs485ScaleHandleImp.ZERO_SCALE_CMD);
               synchronized (Rs485ScaleHandleImp.this.submitLock) {
                  Rs485ScaleHandleImp.this.submitLock.wait(500L);
               }

               if (Rs485ScaleHandleImp.this.errorZeroing) {
                  this.handleException = new HandleException("Error while zeroing the scale");
               }
            } catch (HandleException var5) {
               this.handleException = var5;
            } catch (InterruptedException var6) {
               if (Rs485ScaleHandleImp.this.isTracerOn()) {
                  Rs485ScaleHandleImp.this.traceNormal("InterruptedException received during visitZeroScaleCmd" + var6);
               }

               this.handleException = new HandleException("InterruptedException received during visitZeroScaleCmd", var6);
            }
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
