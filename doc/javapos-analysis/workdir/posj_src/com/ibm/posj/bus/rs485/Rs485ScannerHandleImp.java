package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jsio.event.SioDeviceDataEvent;
import com.ibm.jsio.event.SioDeviceErrorEvent;
import com.ibm.jsio.event.SioDeviceStatusEvent;
import com.ibm.jutil.Util;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.PosSystem;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.ScannerCmd;
import com.ibm.posj.ScannerCmdVisitor;
import com.ibm.posj.ScannerConfig;
import com.ibm.posj.ScannerHandle;
import com.ibm.posj.ScannerHandleCmdFilterV;
import com.ibm.posj.ScannerHandleConst;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.SystemCmdVisitor;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.ScannerHandleImp;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.scanner.DefaultScannerCmdStrategy;
import com.ibm.posj.scanner.Scanner1520CmdStrategy;
import com.ibm.posj.scanner.Scanner4500CmdStrategy;
import com.ibm.posj.scanner.Scanner4501CmdStrategy;
import com.ibm.posj.scanner.Scanner4687CmdStrategy;
import com.ibm.posj.scanner.Scanner4696CmdStrategy;
import com.ibm.posj.scanner.Scanner4697CmdStrategy;
import com.ibm.posj.scanner.Scanner4698CmdStrategy;
import com.ibm.posj.scanner.ScannerDataParser;
import com.ibm.posj.util.DefaultScannerCmdV;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public class Rs485ScannerHandleImp extends AbstractRs485HandleImp implements ScannerHandleImp, ScannerHandleConst {
   protected DefaultScannerCmdStrategy scannerCmdStrategy = null;
   protected ScannerDataParser scannerDataParser = null;
   private ScannerCmdVisitor submitScannerCmdV = new Rs485ScannerHandleImp.SubmitScannerCmdV();
   private Rs485ScannerHandleImp.ScannerDeviceTypeFilter scannerDeviceTypeFilter = null;
   private byte[] data = null;
   private Object waitObj = new Object();
   private boolean reportCfgPending = false;
   private long cmdTimeout = 3000L;
   private ScannerHandleCmdFilterV handleCmdFilterV = new ScannerHandleCmdFilterV();
   private int scannerType = -1;
   public static final long DEFAULT_COMMAND_TIMEOUT = 500L;

   public Rs485ScannerHandleImp(HandleKey key, SioDevice sioDevice, int scannerType) {
      super(key, sioDevice);
      this.scannerType = scannerType;

      try {
         this.scannerDeviceTypeFilter = new Rs485ScannerHandleImp.ScannerDeviceTypeFilter(scannerType);
         this.scannerCmdStrategy = this.scannerDeviceTypeFilter.getCmdStrategy(scannerType);
      } catch (HandleException var5) {
         this.getTracer().print(var5);
         this.traceNormal("Error creating Scanner HandleImp: " + var5.toString());
      }
   }

   public void init() throws HandleException {
      super.init();
      if (this.scannerCmdStrategy == null) {
         throw new HandleException("Could not Initialize Handle... ScannerType not defined");
      } else {
         this.scannerDataParser = new ScannerDataParser(this.scannerCmdStrategy.getStatusBytesLength());
         this.initCmdTimeOut();
      }
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitScanner(this);
   }

   public DevCat getDevCat() {
      return DevCats.SCANNER_DEVCAT;
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
                  throw new HandleException("Invalid Scanner object submitted!");
               }

               ((SystemCmd)cmd).accept((SystemCmdVisitor)this.submitScannerCmdV);
            }

            if (cmd.getCode() != 103 && cmd.toBytes().length > 0) {
               this.submitAsync(cmd.toBytes());
               synchronized (this.waitObj) {
                  this.traceMaximum("before wait");
                  this.waitObj.wait(this.cmdTimeout);
                  this.traceMaximum("after wait");
               }
            }
         } catch (InterruptedException var10) {
            this.traceNormal("interruptedException caught during submit ");
         } catch (HandleException var11) {
            this.setHandleCmdResultInError(cmd, true);
            throw var11;
         } finally {
            cmd.setCompleted(true);
         }

         if (this.reportCfgPending) {
            this.reportCfgPending = false;
            ScannerCmd.Factory factory = ((ScannerHandle)this.getHandle()).getScannerCmdFactory();
            ScannerCmd reportCmd = factory.createReportScannerCmd();
            this.submit(reportCmd);
         }
      }
   }

   protected void errorEventOccurred(SioDeviceErrorEvent event) {
      synchronized (this.waitObj) {
         this.traceMaximum("before error notify all");
         this.waitObj.notifyAll();
         this.traceMaximum("after  error notify all");
      }

      super.errorEventOccurred(event);
   }

   protected void dataEventOccurred(SioDeviceDataEvent event) {
      if (this.isTracerOn()) {
         this.traceNormal("DataEvent received :" + Util.toFormatedHexString(event.getData()));
      }

      this.data = event.getData();
      if (this.data == null) {
         this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -103));
      }

      if (this.scannerCmdStrategy.isCmdRejected(this.data)) {
         if (this.isTracerOn()) {
            this.traceNormal("CMD REJECTED");
         }

         this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
      }

      if (this.scannerCmdStrategy.isHwError(this.data)) {
         this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -100));
      }

      if (this.scannerCmdStrategy.isConfigInResponse(this.data)) {
         int statusLen = this.scannerCmdStrategy.getStatusBytesLength();
         byte[] data = event.getData();
         byte[] configData = new byte[data.length - statusLen];
         if (configData.length == 0) {
            this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -103));
         }

         System.arraycopy(data, statusLen, configData, 0, data.length - statusLen);
         ScannerConfig config = this.scannerCmdStrategy.getScannerConfigObject(configData);
         String newConfig = config.toString();
         if (this.isTracerOn()) {
            this.traceNormal(newConfig);
         }

         this.getLogHelper().addLogEntry(1003, "CONFIGURATION COERCED \n" + newConfig, "Scanner", 3);
      }

      if (this.scannerCmdStrategy.configSucceed(this.data)) {
         if (this.isTracerOn()) {
            this.traceNormal("Scanner Configuration was successful");
         }

         if (this.scannerCmdStrategy.isConfigCoerced(this.data)) {
            if (this.isTracerOn()) {
               this.traceNormal("...but the configuration was coerced,  will send a report config command");
            }

            this.reportCfgPending = true;
         }
      }

      if (this.scannerCmdStrategy.isDataInResponse(this.data)) {
         this.getHandle().getEventHelper().fireDataEvent(this.scannerDataParser.parseData(event.getData()));
      }

      if (event.getData().length <= this.scannerCmdStrategy.getStatusBytesLength() || this.scannerCmdStrategy.isConfigInResponse(this.data)) {
         synchronized (this.waitObj) {
            if (this.isTracerOn()) {
               this.traceMaximum("before data notify all");
            }

            this.waitObj.notifyAll();
            if (this.isTracerOn()) {
               this.traceMaximum("after data notify all");
            }
         }
      }
   }

   protected void statusEventOccurred(SioDeviceStatusEvent event) {
      synchronized (this.waitObj) {
         if (event.getStatus() != 3) {
            this.traceMaximum("before status notify all");
            this.waitObj.notifyAll();
            this.traceMaximum("after status notify all");
         }
      }

      super.statusEventOccurred(event);
      if (this.isTracerOn()) {
         this.traceNormal("StatusEvent received :" + event.getStatus());
      }
   }

   protected void initCmdTimeOut() {
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

   private long getCmdTimeout() {
      return this.cmdTimeout;
   }

   private void setCmdTimeout(long t) {
      this.cmdTimeout = t;
   }

   protected class ScannerDeviceTypeFilter {
      private int scannerId = -1;
      private DefaultScannerCmdStrategy cmdStrategy = null;

      public ScannerDeviceTypeFilter(int deviceType) throws HandleException {
         switch (deviceType) {
            case 0:
               this.cmdStrategy = new Scanner1520CmdStrategy();
               this.scannerId = 4102;
               break;
            case 1:
               this.cmdStrategy = new Scanner4500CmdStrategy();
               this.scannerId = 4104;
            case 2:
               this.cmdStrategy = new Scanner4501CmdStrategy();
               this.scannerId = 4104;
               break;
            case 3:
               this.cmdStrategy = new Scanner4501CmdStrategy();
               this.scannerId = 4104;
               break;
            case 4:
               this.cmdStrategy = new Scanner4687CmdStrategy();
               this.scannerId = 4100;
               break;
            case 5:
               this.cmdStrategy = new Scanner4696CmdStrategy();
               this.scannerId = 4105;
               break;
            case 6:
               this.cmdStrategy = new Scanner4697CmdStrategy();
               this.scannerId = 4106;
               break;
            case 7:
               this.cmdStrategy = new Scanner4698CmdStrategy();
               this.scannerId = 4107;
               break;
            default:
               throw new HandleException("Invalid scanner Type = " + deviceType);
         }
      }

      public DefaultScannerCmdStrategy getCmdStrategy(int deviceType) {
         return this.cmdStrategy;
      }

      public int getScannerId(int deviceType) {
         return this.scannerId;
      }
   }

   protected class SubmitScannerCmdV extends DefaultScannerCmdV {
      public void visitTestSystemCmd(SystemCmd.TestRequestCmd cmd) {
         if (Rs485ScannerHandleImp.this.isTracerOn()) {
            Rs485ScannerHandleImp.this.traceNormal("Rs485ScannerHandleImp:: visitTestSystemCmd");
         }

         byte[] cmdBytes = Rs485ScannerHandleImp.this.scannerCmdStrategy.getTestRequestCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs485ScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitStatusSystemCmd(SystemCmd.StatusRequestCmd cmd) {
         if (Rs485ScannerHandleImp.this.isTracerOn()) {
            Rs485ScannerHandleImp.this.traceNormal("Rs485ScannerHandleImp:: visitStatusSystemCmd");
         }

         byte[] cmdBytes = Rs485ScannerHandleImp.this.scannerCmdStrategy.getStatusRequestCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs485ScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitResetSystemCmd(SystemCmd.ResetRequestCmd cmd) {
         if (Rs485ScannerHandleImp.this.isTracerOn()) {
            Rs485ScannerHandleImp.this.traceNormal("Rs485ScannerHandleImp:: visitResetSystemCmd");
         }

         byte[] cmdBytes = Rs485ScannerHandleImp.this.scannerCmdStrategy.getResetRequestCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs485ScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
         cmd.setDeviceId(Rs485ScannerHandleImp.this.scannerDeviceTypeFilter.getScannerId(Rs485ScannerHandleImp.this.scannerType));
      }

      public void visitEnableScannerCmd(ScannerCmd.EnableScannerCmd cmd) {
         if (Rs485ScannerHandleImp.this.isTracerOn()) {
            Rs485ScannerHandleImp.this.traceNormal("Rs485ScannerHandleImp:: visitEnableScannerCmd");
         }

         byte[] cmdBytes = Rs485ScannerHandleImp.this.scannerCmdStrategy.getEnableScannerCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs485ScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitDisableScannerCmd(ScannerCmd.DisableScannerCmd cmd) {
         if (Rs485ScannerHandleImp.this.isTracerOn()) {
            Rs485ScannerHandleImp.this.traceNormal("Rs485ScannerHandleImp:: visitDisableScannerCmd");
         }

         byte[] cmdBytes = Rs485ScannerHandleImp.this.scannerCmdStrategy.getDisableScannerCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs485ScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitEnableBeeperScannerCmd(ScannerCmd.EnableBeeperScannerCmd cmd) {
         if (Rs485ScannerHandleImp.this.isTracerOn()) {
            Rs485ScannerHandleImp.this.traceNormal("Rs485ScannerHandleImp:: visitEnableBeeperScannerCmd");
         }

         byte[] cmdBytes = Rs485ScannerHandleImp.this.scannerCmdStrategy.getEnableBeeperScannerCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs485ScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitDisableBeeperScannerCmd(ScannerCmd.DisableBeeperScannerCmd cmd) {
         if (Rs485ScannerHandleImp.this.isTracerOn()) {
            Rs485ScannerHandleImp.this.traceNormal("Rs485ScannerHandleImp:: visitDisableBeeperScannerCmd");
         }

         byte[] cmdBytes = Rs485ScannerHandleImp.this.scannerCmdStrategy.getDisableBeeperScannerCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs485ScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitConfigScannerCmd(ScannerCmd.ConfigScannerCmd cmd) {
         if (Rs485ScannerHandleImp.this.isTracerOn()) {
            Rs485ScannerHandleImp.this.traceNormal("Rs485ScannerHandleImp:: visitConfigScannerCmd");
         }

         byte[] cmdBytes = Rs485ScannerHandleImp.this.scannerCmdStrategy.getConfigScannerCmdBytes(cmd);
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs485ScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitReportScannerCmd(ScannerCmd.ReportScannerCmd cmd) {
         if (Rs485ScannerHandleImp.this.isTracerOn()) {
            Rs485ScannerHandleImp.this.traceNormal("Rs485ScannerHandleImp:: visitReportScannerCmd");
         }

         byte[] cmdBytes = Rs485ScannerHandleImp.this.scannerCmdStrategy.getReportScannerCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs485ScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitConfigJAN13TwoLabelScannerCmd(ScannerCmd.ConfigJAN13TwoLabelScannerCmd cmd) {
         if (Rs485ScannerHandleImp.this.isTracerOn()) {
            Rs485ScannerHandleImp.this.traceNormal("Rs485ScannerHandleImp:: visitConfigJaN13TwoLabelScannerCmd");
         }

         byte[] cmdBytes = Rs485ScannerHandleImp.this.scannerCmdStrategy.getConfigJan13TwoLabelScannerCmdBytes(cmd.getConfig());
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs485ScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitReportJAN13TwoLabelScannerCmd(ScannerCmd.ReportJAN13TwoLabelScannerCmd cmd) {
         if (Rs485ScannerHandleImp.this.isTracerOn()) {
            Rs485ScannerHandleImp.this.traceNormal("Rs485ScannerHandleImp:: visitReportJaN13TwoLabelScannerCmd");
         }

         byte[] cmdBytes = Rs485ScannerHandleImp.this.scannerCmdStrategy.getReportJan13TwoLabelScannerCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs485ScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitDirectIOScannerCmd(ScannerCmd.DirectIOScannerCmd cmd) {
         if (Rs485ScannerHandleImp.this.isTracerOn()) {
            Rs485ScannerHandleImp.this.traceNormal("Rs485ScannerHandleImp:: visitDirectIOScannerCmd");
         }

         byte[] cmdBytes = Rs485ScannerHandleImp.this.scannerCmdStrategy.getDirectIOScannerCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(Rs485ScannerHandleImp.this.scannerCmdStrategy.getDirectIOScannerCmdBytes());
         } else {
            Rs485ScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }
   }
}
