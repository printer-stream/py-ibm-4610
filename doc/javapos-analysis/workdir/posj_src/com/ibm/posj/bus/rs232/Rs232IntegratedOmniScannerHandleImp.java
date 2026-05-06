package com.ibm.posj.bus.rs232;

import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.ScannerCmd;
import com.ibm.posj.ScannerCmdVisitor;
import com.ibm.posj.ScannerHandleCmdFilterV;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.SystemCmdVisitor;
import com.ibm.posj.bus.rs232.javaxcomm.Rs232PortCommAdapter;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.scanner.IntegratedOmniScannerDataParser;
import com.ibm.posj.util.DefaultScannerCmdV;
import com.ibm.rs232.Rs232Port;
import java.util.Iterator;
import java.util.List;
import jpos.JposException;

public class Rs232IntegratedOmniScannerHandleImp extends Rs232IntegratedScannerHandleImp {
   private Rs232IntegratedOmniScannerInfoHelper helper = Rs232IntegratedOmniScannerInfoHelper.getInstance();
   private ScannerHandleCmdFilterV handleCmdFilterV = new ScannerHandleCmdFilterV();
   private ScannerCmdVisitor scannerCmdV = new Rs232IntegratedOmniScannerHandleImp.SubmitScannerCmdV();

   public Rs232IntegratedOmniScannerHandleImp(HandleKey key, Rs232Port rs232Port, boolean enable, int time, int devicePollTime) {
      super(key, rs232Port, enable, time, devicePollTime);
   }

   public void startProtocol() {
      if (this.isTracerOn()) {
         this.traceMaximum("-->Starting protocol... ");
      }

      this.protocol = new Rs232IntegratedOmniScannerProtocol(this, (Rs232PortCommAdapter)this.getRs232Port(), this.devicePollTime);
      if (this.isTracerOn()) {
         this.traceMaximum("<--done.");
      }
   }

   public synchronized void init() throws HandleException {
      if (this.isTracerOn()) {
         this.traceMaximum("--> Init()");
      }

      super.init();
      this.protocol = new Rs232IntegratedOmniScannerProtocol(this, (Rs232PortCommAdapter)this.getRs232Port(), this.devicePollTime);
      this.dataParser = new IntegratedOmniScannerDataParser();
      if (this.isTracerOn()) {
         this.traceMaximum("<-- Init()");
      }
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (this.isTracerOn()) {
         this.traceMaximum("--> submit(HandleCmd)");
      }

      try {
         if (cmd == null) {
            throw new HandleException("Attempted to submit a null command to handle");
         }

         if (cmd.getCode() == 906) {
            return;
         }

         this.handleCmdFilterV.reset();
         cmd.accept(this.handleCmdFilterV);
         if (this.handleCmdFilterV.isScannerCmd()) {
            ((ScannerCmd)cmd).accept(this.scannerCmdV);
         } else {
            if (!this.handleCmdFilterV.isSystemCmd()) {
               throw new HandleException("Invalid Scanner object submitted!");
            }

            ((SystemCmd)cmd).accept((SystemCmdVisitor)this.scannerCmdV);
         }

         if (cmd.getCode() == 904) {
            List listOfPackets = this.helper.getConfigurationPackets(((ScannerCmd.ConfigScannerCmd)cmd).getConfig());
            Iterator configurationPackets = listOfPackets.iterator();

            while (configurationPackets.hasNext()) {
               this.protocol.write((byte[])configurationPackets.next());
            }
         } else if (cmd.toBytes() == null) {
            this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         } else if (cmd.toBytes().length == 0) {
            if (this.isTracerOn()) {
               this.traceNormal(cmd + " is empty.  Will not submit Empty cmds, just return!");
            }
         } else {
            this.protocol.write(cmd.toBytes());
         }
      } catch (HandleException var8) {
         this.setHandleCmdResultInError(cmd, true);
         throw var8;
      } finally {
         cmd.setCompleted(true);
         if (this.isTracerOn()) {
            this.traceMaximum("<-- submit(HandleCmd)");
         }
      }
   }

   protected class SubmitScannerCmdV extends DefaultScannerCmdV {
      public void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
         if (Rs232IntegratedOmniScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedOmniScannerHandleImp.this.traceMaximum("Rs232IntegratedOmniScannerHandleImp :: visitDevInfoSystemCmd");
         }

         cmd.setDeviceId(4110);
      }

      public void visitDirectWriteCmd(SystemCmd.DirectWriteCmd cmd) {
         try {
            cmd.setCmdBytes(Rs232IntegratedOmniScannerPacketManager.getInstance().createDirectPacket(cmd.toBytes()));
         } catch (JposException var3) {
            cmd.setCmdBytes(null);
         }
      }

      public void visitResetSystemCmd(SystemCmd.ResetRequestCmd cmd) {
         cmd.setCmdBytes(null);
      }

      public void visitStatusSystemCmd(SystemCmd.StatusRequestCmd cmd) {
         cmd.setCmdBytes(null);
      }

      public void visitTestSystemCmd(SystemCmd.TestRequestCmd cmd) {
         cmd.setCmdBytes(null);
      }

      public void visitConfigJAN13TwoLabelScannerCmd(ScannerCmd.ConfigJAN13TwoLabelScannerCmd cmd) {
         cmd.setCmdBytes(null);
      }

      public void visitReportJAN13TwoLabelScannerCmd(ScannerCmd.ReportJAN13TwoLabelScannerCmd cmd) {
         cmd.setCmdBytes(null);
      }

      public void visitConfigScannerCmd(ScannerCmd.ConfigScannerCmd cmd) {
         cmd.setCmdBytes(null);
      }

      public void visitDirectIOScannerCmd(ScannerCmd.DirectIOScannerCmd cmd) {
         cmd.setCmdBytes(Rs232IntegratedOmniScannerHandleImp.this.helper.getDirectIOCmdBytes(cmd.getCommandParam(), cmd.getDataParam()));
      }

      public void visitDisableBeeperScannerCmd(ScannerCmd.DisableBeeperScannerCmd cmd) {
         if (Rs232IntegratedOmniScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedOmniScannerHandleImp.this.traceMaximum("-->visitDisableBeeperScannerCmd()");
         }

         cmd.setCmdBytes(Rs232IntegratedOmniScannerHandleImp.this.helper.getDisableBeeperScannerCmdBytes());
         if (Rs232IntegratedOmniScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedOmniScannerHandleImp.this.traceMaximum("<--visitDisableBeeperScannerCmd()");
         }
      }

      public void visitEnableBeeperScannerCmd(ScannerCmd.EnableBeeperScannerCmd cmd) {
         if (Rs232IntegratedOmniScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedOmniScannerHandleImp.this.traceMaximum("-->visitEnableBeeperScannerCmd()");
         }

         cmd.setCmdBytes(Rs232IntegratedOmniScannerHandleImp.this.helper.getEnableBeeperScannerCmdBytes());
         if (Rs232IntegratedOmniScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedOmniScannerHandleImp.this.traceMaximum("<--visitEnableBeeperScannerCmd()");
         }
      }

      public void visitDisableScannerCmd(ScannerCmd.DisableScannerCmd cmd) {
         if (Rs232IntegratedOmniScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedOmniScannerHandleImp.this.traceMaximum("-->visitDisableScannerCmd()");
         }

         cmd.setCmdBytes(Rs232IntegratedOmniScannerHandleImp.this.helper.getStopSessionScannerCmdBytes());
         if (Rs232IntegratedOmniScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedOmniScannerHandleImp.this.traceMaximum("<--visitDisableScannerCmd()");
         }
      }

      public void visitEnableScannerCmd(ScannerCmd.EnableScannerCmd cmd) {
         if (Rs232IntegratedOmniScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedOmniScannerHandleImp.this.traceMaximum("-->visitEnableScannerCmd()");
         }

         cmd.setCmdBytes(Rs232IntegratedOmniScannerHandleImp.this.helper.getStartSessionScannerCmdBytes());
         if (Rs232IntegratedOmniScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedOmniScannerHandleImp.this.traceMaximum("<--visitEnableScannerCmd()");
         }
      }

      public void visitReportScannerCmd(ScannerCmd.ReportScannerCmd cmd) {
         cmd.setCmdBytes(null);
      }
   }
}
