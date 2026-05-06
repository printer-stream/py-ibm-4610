package com.ibm.posj.bus.rs232;

import com.ibm.jutil.Util;
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
import com.ibm.posj.scanner.IntegratedLineScannerDataParser;
import com.ibm.posj.util.DefaultScannerCmdV;
import com.ibm.rs232.Rs232Port;
import java.util.Iterator;

public class Rs232IntegratedLineScannerHandleImp extends Rs232IntegratedScannerHandleImp {
   private ScannerCmdVisitor submitScannerCmdV = new Rs232IntegratedLineScannerHandleImp.SubmitScannerCmdV();
   private ScannerHandleCmdFilterV handleCmdFilterV = new ScannerHandleCmdFilterV();
   private Rs232IntegratedLineScannerInfoHelper helper = Rs232IntegratedLineScannerInfoHelper.getInstance();
   private Object dataLock = new Object();
   private boolean dataArrived = false;
   private static final int TIMEOUT = 5000;

   public Rs232IntegratedLineScannerHandleImp(HandleKey key, Rs232Port rs232Port, boolean enable, int time, int devicePollTime) {
      super(key, rs232Port, enable, time, devicePollTime);
   }

   public void init() throws HandleException {
      super.init();
      this.protocol = new Rs232IntegratedLineScannerProtocol(this, (Rs232PortCommAdapter)this.getRs232Port(), this.devicePollTime);
      this.dataParser = new IntegratedLineScannerDataParser();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (this.isTracerOn()) {
         this.traceMaximum("--> submit");
      }

      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            if (cmd.getCode() == 906) {
               return;
            }

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

            if (cmd.getCode() == 904) {
               if (!this.helper.equalThatPreviousConfig()) {
                  byte[] initTempPacket = new byte[128];
                  int pos = 0;
                  Iterator it = this.helper.getConfigCommandsList().iterator();

                  while (it.hasNext()) {
                     byte[] initCmd = (byte[])it.next();
                     if (pos == 0) {
                        System.arraycopy(initCmd, 0, initTempPacket, 0, initCmd.length);
                        pos = initCmd.length;
                     } else {
                        System.arraycopy(initCmd, 1, initTempPacket, pos, initCmd.length - 1);
                        pos += initCmd.length - 1;
                     }

                     if (pos > 88 || !it.hasNext()) {
                        this.dataArrived = false;
                        byte[] initPacket = new byte[pos];
                        System.arraycopy(initTempPacket, 0, initPacket, 0, pos);
                        pos = 0;
                        if (this.isTracerOn()) {
                           this.traceMaximum("Writing to protocol ( " + System.currentTimeMillis() + " )");
                        }

                        this.protocol.write(initPacket);
                        synchronized (this.dataLock) {
                           if (!this.dataArrived) {
                              try {
                                 this.dataLock.wait(5000L);
                              } catch (InterruptedException var25) {
                              }

                              if (!this.dataArrived && this.isTracerOn()) {
                                 this.traceMaximum("ERROR! Scanner did not respond in 5000 ms");
                              }
                           }
                        }
                     }
                  }
               }

               this.helper.resetConfigCommandsList();
            } else if (cmd.getCode() == 908) {
               this.dataArrived = false;
               this.protocol.write(((ScannerCmd.DirectIOScannerCmd)cmd).getDirectIOCmd());
               synchronized (this.dataLock) {
                  if (!this.dataArrived) {
                     try {
                        this.dataLock.wait(5000L);
                     } catch (InterruptedException var23) {
                     }

                     if (!this.dataArrived) {
                        this.traceMaximum("ERROR! Scanner did not respond in 5000 ms");
                     }
                  }
               }
            } else if (cmd.toBytes().length > 0) {
               this.dataArrived = false;
               this.protocol.write(cmd.toBytes());
               synchronized (this.dataLock) {
                  if (!this.dataArrived) {
                     try {
                        this.dataLock.wait(5000L);
                     } catch (InterruptedException var21) {
                     }

                     if (!this.dataArrived) {
                        this.traceMaximum("ERROR! Scanner did not respond in 5000 ms");
                     }
                  }
               }
            } else if (this.isTracerOn()) {
               this.traceNormal(cmd + " is empty.  Will not submit Empty cmds, just return!");
            }
         } catch (HandleException var27) {
            this.setHandleCmdResultInError(cmd, true);
            throw var27;
         } finally {
            cmd.setCompleted(true);
         }

         if (this.isTracerOn()) {
            this.traceMaximum("<-- submit");
         }
      }
   }

   public void fireDataEvent(byte[] data) {
      if (this.isTracerOn()) {
         this.traceMaximum("-->fireDataEvent");
         this.traceMaximum("Data Received: " + Util.toFormatedHexString(data));
      }

      if (this.helper.isResultOk(data)) {
         synchronized (this.dataLock) {
            this.dataArrived = true;
            this.dataLock.notify();
            if (this.isTracerOn()) {
               this.traceMaximum("Result OK received. Unlocking submit.");
            }
         }
      } else {
         this.fireDirectIOEvent(data);
         if (this.helper.isBarcodeData(data)) {
            if (this.isTracerOn()) {
               this.traceMaximum("Barcode Data Received. Firing Data Event");
            }

            super.fireDataEvent(data);
         } else if (this.isTracerOn()) {
            this.traceMaximum("Unexpected result received.");
         }
      }

      if (this.isTracerOn()) {
         this.traceMaximum("<--fireDataEvent");
      }
   }

   public void startProtocol() {
      this.protocol = new Rs232IntegratedLineScannerProtocol(this, (Rs232PortCommAdapter)this.getRs232Port(), this.devicePollTime);
   }

   protected class SubmitScannerCmdV extends DefaultScannerCmdV {
      public void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
         if (Rs232IntegratedLineScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedLineScannerHandleImp.this.traceMaximum("Rs232IntegratedLineScannerHandleImp:: visitDevInfoSystemCmd");
         }

         cmd.setDeviceId(4108);
      }

      public void visitTestSystemCmd(SystemCmd.TestRequestCmd cmd) {
         if (Rs232IntegratedLineScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedLineScannerHandleImp.this.traceMaximum("Rs232IntegratedLineScannerHandleImp:: visitTestSystemCmd");
         }

         byte[] cmdBytes = Rs232IntegratedLineScannerHandleImp.this.helper.getTestRequestCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs232IntegratedLineScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitStatusSystemCmd(SystemCmd.StatusRequestCmd cmd) {
         if (Rs232IntegratedLineScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedLineScannerHandleImp.this.traceMaximum("Rs232IntegratedLineScannerHandleImp:: visitStatusSystemCmd");
         }

         byte[] cmdBytes = Rs232IntegratedLineScannerHandleImp.this.helper.getStatusRequestCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs232IntegratedLineScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitResetSystemCmd(SystemCmd.ResetRequestCmd cmd) {
         if (Rs232IntegratedLineScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedLineScannerHandleImp.this.traceMaximum("Rs232IntegratedLineScannerHandleImp:: visitResetSystemCmd");
         }

         byte[] cmdBytes = Rs232IntegratedLineScannerHandleImp.this.helper.getResetRequestCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs232IntegratedLineScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitEnableScannerCmd(ScannerCmd.EnableScannerCmd cmd) {
         if (Rs232IntegratedLineScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedLineScannerHandleImp.this.traceMaximum("Rs232IntegratedLineScannerHandleImp:: visitEnableScannerCmd");
         }

         byte[] cmdBytes = Rs232IntegratedLineScannerHandleImp.this.helper.getEnableScannerCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs232IntegratedLineScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitDisableScannerCmd(ScannerCmd.DisableScannerCmd cmd) {
         if (Rs232IntegratedLineScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedLineScannerHandleImp.this.traceMaximum("Rs232IntegratedLineScannerHandleImp:: visitDisableScannerCmd");
         }

         byte[] cmdBytes = Rs232IntegratedLineScannerHandleImp.this.helper.getDisableScannerCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs232IntegratedLineScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitEnableBeeperScannerCmd(ScannerCmd.EnableBeeperScannerCmd cmd) {
         if (Rs232IntegratedLineScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedLineScannerHandleImp.this.traceMaximum("Rs232IntegratedLineScannerHandleImp:: visitEnableBeeperScannerCmd");
         }

         byte[] cmdBytes = Rs232IntegratedLineScannerHandleImp.this.helper.getEnableBeeperScannerCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs232IntegratedLineScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitDisableBeeperScannerCmd(ScannerCmd.DisableBeeperScannerCmd cmd) {
         if (Rs232IntegratedLineScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedLineScannerHandleImp.this.traceMaximum("Rs232IntegratedLineScannerHandleImp:: visitDisableBeeperScannerCmd");
         }

         byte[] cmdBytes = Rs232IntegratedLineScannerHandleImp.this.helper.getDisableBeeperScannerCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs232IntegratedLineScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitConfigScannerCmd(ScannerCmd.ConfigScannerCmd cmd) {
         if (Rs232IntegratedLineScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedLineScannerHandleImp.this.traceMaximum("Rs232IntegratedLineScannerHandleImp:: visitConfigScannerCmd");
         }

         byte[] cmdBytes = Rs232IntegratedLineScannerHandleImp.this.helper.getConfigScannerCmdBytes(cmd);
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs232IntegratedLineScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitReportScannerCmd(ScannerCmd.ReportScannerCmd cmd) {
         if (Rs232IntegratedLineScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedLineScannerHandleImp.this.traceMaximum("Rs232IntegratedLineScannerHandleImp:: visitReportScannerCmd");
         }

         byte[] cmdBytes = Rs232IntegratedLineScannerHandleImp.this.helper.getReportScannerCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs232IntegratedLineScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitConfigJAN13TwoLabelScannerCmd(ScannerCmd.ConfigJAN13TwoLabelScannerCmd cmd) {
         if (Rs232IntegratedLineScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedLineScannerHandleImp.this.traceMaximum("Rs232IntegratedLineScannerHandleImp:: visitConfigJaN13TwoLabelScannerCmd");
         }

         byte[] cmdBytes = Rs232IntegratedLineScannerHandleImp.this.helper.getConfigJan13TwoLabelScannerCmdBytes(cmd.getConfig());
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs232IntegratedLineScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitReportJAN13TwoLabelScannerCmd(ScannerCmd.ReportJAN13TwoLabelScannerCmd cmd) {
         if (Rs232IntegratedLineScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedLineScannerHandleImp.this.traceMaximum("Rs232IntegratedLineScannerHandleImp:: visitReportJaN13TwoLabelScannerCmd");
         }

         byte[] cmdBytes = Rs232IntegratedLineScannerHandleImp.this.helper.getReportJan13TwoLabelScannerCmdBytes();
         if (cmdBytes != null) {
            cmd.setCmdBytes(cmdBytes);
         } else {
            Rs232IntegratedLineScannerHandleImp.this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         }
      }

      public void visitDirectIOScannerCmd(ScannerCmd.DirectIOScannerCmd cmd) {
         if (Rs232IntegratedLineScannerHandleImp.this.isTracerOn()) {
            Rs232IntegratedLineScannerHandleImp.this.traceMaximum("Rs232IntegratedLineScannerHandleImp:: visitDirectIOScannerCmd");
         }

         cmd.setCmdBytes(Rs232IntegratedLineScannerHandleImp.this.helper.getDirectIOScannerCmdBytes());
      }
   }
}
