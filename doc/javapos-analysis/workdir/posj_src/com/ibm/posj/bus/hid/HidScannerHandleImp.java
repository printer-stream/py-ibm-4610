package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.Util;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.PosSystem;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.RuntimePosException;
import com.ibm.posj.ScannerCmd;
import com.ibm.posj.ScannerCmdVisitor;
import com.ibm.posj.ScannerConfig;
import com.ibm.posj.ScannerHandleCmdFilterV;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.SystemCmdVisitor;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.ScannerHandleImp;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.ScannerDataEvent;
import com.ibm.posj.scanner.ScannerDataParser;
import com.ibm.posj.util.DefaultScannerCmdV;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.PosjUtil;
import com.ibm.posj.util.RetryHelper;

public class HidScannerHandleImp extends AbstractHidHandleImp implements HidHandleImp, ScannerHandleImp {
   private long cmdTimeout = 3000L;
   private byte[] barcodeData = new byte[0];
   private int barcodeType = 0;
   private int extraData = 0;
   private byte[] data = null;
   private int dataBytesRemaining = 0;
   private int currentPosition = 0;
   private RetryHelper retryHelper = null;
   private HidScannerHandleImp.ReceiverScannerCmdV receiverScannerCmdV = new HidScannerHandleImp.ReceiverScannerCmdV();
   private ScannerCmdVisitor submitScannerCmdV = new HidScannerHandleImp.SubmitScannerCmdV();
   private ScannerHandleCmdFilterV handleCmdFilterV = new ScannerHandleCmdFilterV();
   protected OEMScannerConfig oemConfig = new OEMScannerConfig();
   protected ScannerDataParser scannerDataParser = null;
   public static final byte[] SCANNER_TEST_REQUEST_CMD = new byte[]{0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   public static final byte[] SCANNER_STATUS_REQUEST_CMD = new byte[]{0, 32, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   public static final byte[] SCANNER_RESET_REQUEST_CMD = new byte[]{0, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   public static final byte[] ENABLE_SCANNER_CMD = new byte[]{17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   public static final byte[] DISABLE_SCANNER_CMD = new byte[]{18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   public static final byte[] ENABLE_BEEPER_SCANNER_CMD = new byte[]{20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   public static final byte[] DISABLE_BEEPER_SCANNER_CMD = new byte[]{24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   public static final byte[] CONFIG_SCANNER_CMD = new byte[]{32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   public static final byte[] CONFIG_JAN13_TWO_LABEL_SCANNER_CMD = new byte[]{35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   public static final byte[] REPORT_SCANNER_CMD = new byte[]{33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   public static final byte[] REPORT_JAN13_TWO_LABEL_SCANNER_CMD = new byte[]{52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   public static final byte[] DIRECTIO_SCANNER_CMD = new byte[]{48, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   public static final int TYPE_EANJAN13_CODE = 22;
   public static final int TYPE_EANJAN8_CODE = 12;
   public static final int TYPE_UPCA_CODE = 13;
   public static final int TYPE_UPCE_CODE = 10;
   public static final int TYPE_UPC_D1_CODE = 17;
   public static final int TYPE_UPC_D2_CODE = 18;
   public static final int TYPE_UPC_D3_CODE = 20;
   public static final int TYPE_UPC_D4_CODE = 23;
   public static final int TYPE_UPC_D5_CODE = 29;
   public static final int TYPE_ITF_CODE = 13;
   public static final int TYPE_CODABAR_CODE = 14;
   public static final int TYPE_CODE39_CODE = 10;
   public static final int TYPE_CODE93_CODE = 25;
   public static final int TYPE_CODE128_CODE = 24;
   public static final int TYPE_UPC_A_PLUS_2_CODE = 22;
   public static final int TYPE_UPC_A_PLUS_5_CODE = 17;
   public static final int TYPE_UPC_E_PLUS_2_CODE = 18;
   public static final int TYPE_UPC_E_PLUS_5_CODE = 20;
   public static final int TYPE_EAN8_PLUS_2_CODE = 23;
   public static final int TYPE_EAN8_PLUS_5_CODE = 29;
   public static final int TYPE_EAN13_PLUS_2_CODE = 19;
   public static final int TYPE_EAN13_PLUS_5_CODE = 21;
   public static final int TYPE_EAN128_CODE = 37;
   public static final int TYPE_UPCA_CODE128_S = 32;
   public static final int TYPE_UPCE_CODE128_S = 33;
   public static final int TYPE_EANJAN8_CODE128_S = 34;
   public static final int TYPE_EANJAN13_CODE128_S = 35;
   public static final int TYPE_UNKNOWN_CODE = -1;
   public static final int RESPONSE_LENGTH = 4;
   public static final byte ONE_BYTE_TYPE_LENGTH = 1;
   public static final byte TWO_BYTE_TYPE_LENGTH = 2;
   public static final byte THREE_BYTE_TYPE_LENGTH = 3;
   public static final byte HIGH_PART_ZERO_AND_MASK = -16;
   public static final byte THREE_BYTES_TYPE_LAST_BYTE = 11;
   public static final byte[] LABEL_DATA_OR_MASK = new byte[]{0, 48, 4, 0};
   public static final byte[] LABEL_DATA_RESPONSE_MASK = new byte[]{0, 48, 7, 0};
   public static final int HW_ERROR_BIT_POSITION = 5;
   public static final int CMD_REJECT_BIT_POSITION = 7;
   public static final int DIRECT_IO_CMD_NOT_ALLOWED_BIT_POSITION = 4;
   public static final int DIRECT_IO_CMD_UNDEFINED_BIT_POSITION = 5;
   public static final int SCANNER_ENABLED_BIT_POSITION = 1;
   public static final int GOOD_READ_BEEP_BIT_POSITION = 4;
   public static final int SUCCESSFUL_CONFIG_BIT_POSITION = 0;
   public static final int SUCCESSFUL_EANJAN13_CONFIG_BIT_POSITION = 2;
   public static final long DEFAULT_COMMAND_TIMEOUT = 500L;
   public static final int TOTAL_RETRY = 3;

   public HidScannerHandleImp(HandleKey key, HidDevice device) {
      super(key, device);
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidScannerHandleImp(this);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitScanner(this);
   }

   public DevCat getDevCat() {
      return DevCats.SCANNER_DEVCAT;
   }

   public void init() throws HandleException {
      super.init();
      this.initCmdTimeOut();
      this.initialize();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            this.handleCmdFilterV.reset();
            cmd.accept(this.handleCmdFilterV);
            if (this.handleCmdFilterV.isScannerCmd()) {
               ((ScannerCmd)cmd).accept(this.submitScannerCmdV);
            } else {
               if (!this.handleCmdFilterV.isSystemCmd()) {
                  throw new HandleException("Invalid ScannerCmd object submitted!");
               }

               ((SystemCmd)cmd).accept((SystemCmdVisitor)this.submitScannerCmdV);
            }

            if (cmd.getCode() == 103) {
               if (this.isTracerOn()) {
                  this.traceNormal(" DEVICE_INFO_REQUEST_CMD  deviceID =  " + ((SystemCmd.DeviceInfoRequestCmd)cmd).getDeviceId());
               }

               ((SystemCmd.DeviceInfoRequestCmd)cmd).setFirmwareLevel(this.getBCDLevel());
               ((SystemCmd.DeviceInfoRequestCmd)cmd).setSerialNumber(this.getSerialNumber());
            } else if (cmd.getCode() == 100) {
               this.submitCmd(cmd.getName(), cmd.toBytes());
            } else {
               this.submitCmd(cmd, 3);
            }
         } catch (HandleException var6) {
            this.setHandleCmdResultInError(cmd, true);
            throw var6;
         } finally {
            cmd.setCompleted(true);
         }
      }
   }

   protected void initialize() {
      try {
         this.submit(this.getHandle().getSystemCmdFactory().createStatusRequestCmd());
      } catch (HandleException var2) {
         if (this.isTracerOn()) {
            this.traceNormal("Failed to submit status request cmd ");
            this.getTracer().print(var2);
         }
      }

      this.barcodeData = new byte[0];
      this.barcodeType = 0;
      this.extraData = 0;
      this.data = null;
      this.dataBytesRemaining = 0;
      this.currentPosition = 0;
      this.scannerDataParser = new ScannerDataParser(4);
   }

   protected void reinitialize() throws HandleException {
      this.initialize();
   }

   protected RetryHelper getRetryHelper() {
      if (this.retryHelper == null) {
         this.retryHelper = new RetryHelper();
      }

      return this.retryHelper;
   }

   protected void submitCmd(HandleCmd cmd, int times) throws HandleException {
      RetryHelper rh = this.getRetryHelper();
      rh.setLastCmd(cmd);
      rh.setCmdPending(true);
      this.submitCmd(cmd.getName(), cmd.toBytes());

      int retry;
      for (retry = 0; rh.isCmdPending() && retry < times; retry++) {
         try {
            rh.waitCmdPending(this.getCmdTimeout());
         } catch (InterruptedException var6) {
         }

         if (!rh.isCmdPending()) {
            return;
         }

         this.submitCmd(cmd.getName(), cmd.toBytes());
      }

      if (retry >= times && rh.isCmdPending()) {
         throw new HandleException(this.getCmdTimeout() + " timeout while waiting for " + cmd.getName() + " command to complete");
      }
   }

   protected void reportEventOccurred(ReportEvent rE) {
      if (this.isTracerOn()) {
         this.traceNormal("Event->" + Util.toFormatedHexString(rE.getData()));
      }

      byte[] processData = this.processDataEvent(rE.getData());
      if (processData.length >= 4) {
         if (PosjUtil.isBitSelected(processData[1], 5)) {
            this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -103));
         }

         if (PosjUtil.isBitSelected(processData[2], 7)) {
            this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }

         if (this.isLabelDataResponse(processData)) {
            try {
               this.getHandle().getEventHelper().fireDataEvent(this.parseData(processData));
            } catch (IllegalArgumentException var5) {
               Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
               eventHelper.fireErrorEvent(new ErrorEvent(this, -103, "Error processing data : " + var5.getMessage()));
            }
         } else {
            RetryHelper rh = this.getRetryHelper();
            if (rh.isCmdPending()) {
               rh.setCmdPending(false);
               rh.setLastResponse(processData);
               HandleCmd cmd = rh.getLastCmd();
               if (cmd instanceof ScannerCmd) {
                  ((ScannerCmd)cmd).accept(this.receiverScannerCmdV);
               } else {
                  ((SystemCmd)cmd).accept(this.receiverScannerCmdV);
               }

               rh.notifyAllCmdPending();
            }
         }
      }
   }

   protected byte[] processDataEvent(byte[] tempData) {
      int lengthResponse = tempData[0];
      if (this.extraData-- > 0) {
         if (this.dataBytesRemaining <= tempData.length) {
            System.arraycopy(tempData, 0, this.data, this.currentPosition, this.dataBytesRemaining);
            return this.data;
         } else {
            System.arraycopy(tempData, 0, this.data, this.currentPosition, tempData.length);
            this.currentPosition += tempData.length;
            this.dataBytesRemaining -= tempData.length;
            return new byte[0];
         }
      } else {
         if (lengthResponse != 0 && lengthResponse <= 64) {
            this.data = new byte[lengthResponse];
            if (lengthResponse <= tempData.length) {
               System.arraycopy(tempData, 0, this.data, 0, this.data.length);
               return this.data;
            }

            this.extraData = lengthResponse / tempData.length;
            if (lengthResponse % tempData.length == 0) {
               this.extraData--;
            }

            this.dataBytesRemaining = lengthResponse - tempData.length;
            this.currentPosition = tempData.length;
            System.arraycopy(tempData, 0, this.data, 0, tempData.length);
         }

         return new byte[0];
      }
   }

   protected ScannerDataEvent parseData(byte[] data) {
      return this.scannerDataParser.parseData(data);
   }

   protected ScannerConfig getScannerConfigObject(byte[] configBytes) {
      return this.oemConfig.getScannerConfigObject(configBytes);
   }

   void initCmdTimeOut() {
      PosSystem.Properties prop = PosSystemManager.getInstance().getProperties();
      if (!prop.isLoaded()) {
         prop.loadProperties();
      }

      if (prop.isPropertyDefined("com.ibm.posj.bus.ScannerHandleImp.COMMAND_TIMEOUT")) {
         String value = prop.getPropertyString("com.ibm.posj.bus.ScannerHandleImp.COMMAND_TIMEOUT");

         try {
            this.setCmdTimeout(Long.decode(value));
         } catch (Exception var4) {
            this.traceNormal("Error reading : com.ibm.posj.bus.ScannerHandleImp.COMMAND_TIMEOUT");
            this.getTracer().print(var4);
            this.setCmdTimeout(500L);
         }
      } else {
         this.setCmdTimeout(500L);
      }

      if (this.isTracerOn()) {
         this.traceNormal("->initCmdTimeOut() Use value : " + this.getCmdTimeout() + "ms. <-");
      }
   }

   long getCmdTimeout() {
      return this.cmdTimeout;
   }

   void setCmdTimeout(long t) {
      this.cmdTimeout = t;
   }

   private boolean isLabelDataResponse(byte[] data) {
      return data[0] != 4
         && (data[1] | LABEL_DATA_OR_MASK[1]) == LABEL_DATA_RESPONSE_MASK[1]
         && (data[2] | LABEL_DATA_OR_MASK[2]) == LABEL_DATA_RESPONSE_MASK[2]
         && data[3] == LABEL_DATA_RESPONSE_MASK[3];
   }

   protected class ReceiverScannerCmdV extends DefaultScannerCmdV {
      public void visitEnableScannerCmd(ScannerCmd.EnableScannerCmd cmd) {
         if (!PosjUtil.isBitSelected(HidScannerHandleImp.this.getRetryHelper().getLastResponse()[2], 1)) {
            HidScannerHandleImp.this.getRetryHelper().setCmdPending(true);
         }
      }

      public void visitDisableScannerCmd(ScannerCmd.DisableScannerCmd cmd) {
         if (PosjUtil.isBitSelected(HidScannerHandleImp.this.getRetryHelper().getLastResponse()[2], 1)) {
            HidScannerHandleImp.this.getRetryHelper().setCmdPending(true);
         }
      }

      public void visitEnableBeeperScannerCmd(ScannerCmd.EnableBeeperScannerCmd cmd) {
         if (!PosjUtil.isBitSelected(HidScannerHandleImp.this.getRetryHelper().getLastResponse()[1], 4)) {
            HidScannerHandleImp.this.getRetryHelper().setCmdPending(true);
         }
      }

      public void visitDisableBeeperScannerCmd(ScannerCmd.DisableBeeperScannerCmd cmd) {
         if (PosjUtil.isBitSelected(HidScannerHandleImp.this.getRetryHelper().getLastResponse()[1], 4)) {
            HidScannerHandleImp.this.getRetryHelper().setCmdPending(true);
         }
      }

      public void visitConfigScannerCmd(ScannerCmd.ConfigScannerCmd cmd) {
         if (!PosjUtil.isBitSelected(HidScannerHandleImp.this.getRetryHelper().getLastResponse()[3], 0)) {
            HidScannerHandleImp.this.getHandle()
               .getEventHelper()
               .fireErrorEvent(new ErrorEvent(this, -103, "Invalid parameters specifications in a Configure Scanner command"));
         }
      }

      public void visitReportScannerCmd(ScannerCmd.ReportScannerCmd cmd) {
         int len = HidScannerHandleImp.this.getRetryHelper().getLastResponse().length - 4;
         byte[] responseBytes = new byte[len];
         System.arraycopy(HidScannerHandleImp.this.getRetryHelper().getLastResponse(), 4, responseBytes, 0, len);
         ScannerConfig scannerConfig = HidScannerHandleImp.this.getScannerConfigObject(responseBytes);
         if (HidScannerHandleImp.this.isTracerOn()) {
            HidScannerHandleImp.this.traceNormal(
               " REPORT_SCANNER_CONFIGURATION : " + Util.toFormatedHexString(HidScannerHandleImp.this.getRetryHelper().getLastResponse())
            );
            HidScannerHandleImp.this.traceNormal("<ScannerConfiguration>" + scannerConfig.toString() + "</ScannerConfiguration>");
         }
      }

      public void visitConfigJAN13TwoLabelScannerCmd(ScannerCmd.ConfigJAN13TwoLabelScannerCmd cmd) {
         if (!PosjUtil.isBitSelected(HidScannerHandleImp.this.getRetryHelper().getLastResponse()[3], 2)) {
            HidScannerHandleImp.this.getHandle()
               .getEventHelper()
               .fireErrorEvent(new ErrorEvent(this, -103, "Invalid parameters specifications EANJAN13TwoLabel Configure Scanner command"));
         }
      }

      public void visitReportJAN13TwoLabelScannerCmd(ScannerCmd.ReportJAN13TwoLabelScannerCmd cmd) {
         int len = HidScannerHandleImp.this.getRetryHelper().getLastResponse().length - 4;
         byte[] responseBytes = new byte[len];
         System.arraycopy(HidScannerHandleImp.this.getRetryHelper().getLastResponse(), 4, responseBytes, 0, len);
         HidScannerHandleImp.this.getHandle().getEventHelper().fireDataEvent(new ScannerDataEvent(this, responseBytes, 0));
      }

      public void visitDirectIOScannerCmd(ScannerCmd.DirectIOScannerCmd cmd) {
         if (PosjUtil.isBitSelected(HidScannerHandleImp.this.getRetryHelper().getLastResponse()[3], 4)
            || PosjUtil.isBitSelected(HidScannerHandleImp.this.getRetryHelper().getLastResponse()[3], 5)) {
            HidScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }
   }

   protected class SubmitScannerCmdV extends DefaultScannerCmdV {
      public void visitTestSystemCmd(SystemCmd.TestRequestCmd cmd) {
         cmd.setCmdBytes(HidScannerHandleImp.SCANNER_TEST_REQUEST_CMD);
      }

      public void visitStatusSystemCmd(SystemCmd.StatusRequestCmd cmd) {
         cmd.setCmdBytes(HidScannerHandleImp.SCANNER_STATUS_REQUEST_CMD);
      }

      public void visitResetSystemCmd(SystemCmd.ResetRequestCmd cmd) {
         cmd.setCmdBytes(HidScannerHandleImp.SCANNER_RESET_REQUEST_CMD);
      }

      public void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
         cmd.setDeviceId(4101);
      }

      public void visitEnableScannerCmd(ScannerCmd.EnableScannerCmd cmd) {
         cmd.setCmdBytes(HidScannerHandleImp.ENABLE_SCANNER_CMD);
      }

      public void visitDisableScannerCmd(ScannerCmd.DisableScannerCmd cmd) {
         cmd.setCmdBytes(HidScannerHandleImp.DISABLE_SCANNER_CMD);
      }

      public void visitEnableBeeperScannerCmd(ScannerCmd.EnableBeeperScannerCmd cmd) {
         cmd.setCmdBytes(HidScannerHandleImp.ENABLE_BEEPER_SCANNER_CMD);
      }

      public void visitDisableBeeperScannerCmd(ScannerCmd.DisableBeeperScannerCmd cmd) {
         cmd.setCmdBytes(HidScannerHandleImp.DISABLE_BEEPER_SCANNER_CMD);
      }

      public void visitConfigScannerCmd(ScannerCmd.ConfigScannerCmd cmd) {
         byte[] byteCmd = HidScannerHandleImp.CONFIG_SCANNER_CMD;
         ScannerConfig scannerConfig = cmd.getConfig();
         byte[] byteConfig = HidScannerHandleImp.this.oemConfig.getScannerConfigurationBytes(scannerConfig);
         if (byteCmd.length < 9) {
            throw new RuntimePosException("Invalid length of the command");
         } else {
            for (int i = 2; i < byteCmd.length; i++) {
               byteCmd[i] = byteConfig[i - 2];
            }

            cmd.setCmdBytes(byteCmd);
         }
      }

      public void visitReportScannerCmd(ScannerCmd.ReportScannerCmd cmd) {
         cmd.setCmdBytes(HidScannerHandleImp.REPORT_SCANNER_CMD);
      }

      public void visitConfigJAN13TwoLabelScannerCmd(ScannerCmd.ConfigJAN13TwoLabelScannerCmd cmd) {
         byte[] byteCmd = HidScannerHandleImp.CONFIG_JAN13_TWO_LABEL_SCANNER_CMD;
         byte[] byteConfig = cmd.getConfig();
         if (byteCmd.length < 8) {
            throw new RuntimePosException("Invalid length of the command");
         } else {
            for (int i = 2; i < byteCmd.length - 1; i++) {
               byteCmd[i] = byteConfig[i - 2];
            }

            cmd.setCmdBytes(byteCmd);
         }
      }

      public void visitReportJAN13TwoLabelScannerCmd(ScannerCmd.ReportJAN13TwoLabelScannerCmd cmd) {
         cmd.setCmdBytes(HidScannerHandleImp.REPORT_JAN13_TWO_LABEL_SCANNER_CMD);
      }

      public void visitDirectIOScannerCmd(ScannerCmd.DirectIOScannerCmd cmd) {
         byte[] byteCmd = HidScannerHandleImp.DIRECTIO_SCANNER_CMD;
         byte[] byteConfig = cmd.getDirectIOCmd();
         if (byteCmd.length < 10) {
            throw new RuntimePosException("Invalid length of the command");
         } else {
            for (int i = 1; i < byteCmd.length; i++) {
               byteCmd[i] = byteConfig[i - 1];
            }

            cmd.setCmdBytes(byteCmd);
         }
      }
   }
}
