package com.ibm.posj.bus.rs232;

import com.ibm.posj.FiscalPrinterInfoHelper;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.FiscalPrinterHandleImp;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.rs232.javaxcomm.Rs232PortCommAdapter;
import com.ibm.posj.event.DataEvent;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.rs232.Rs232Port;
import com.ibm.rs232.event.Rs232OfflineEvent;

public class Rs232FiscalPrinterHandleImp extends AbstractRs232HandleImp implements FiscalPrinterHandleImp, Rs232FiscalPrinterProtocolListener {
   private Rs232FiscalPrinterProtocol protocol = null;
   private boolean devInfoReceived = false;
   private boolean iplEndReceived = false;
   private byte devID;
   private byte countryCode;

   public Rs232FiscalPrinterHandleImp(HandleKey key, Rs232Port rs232Port) {
      super(key, rs232Port);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitFiscalPrinter(this);
   }

   public DevCat getDevCat() {
      return DevCats.FISCALPRINTER_DEVCAT;
   }

   public short getECLevel() {
      return (short)Rs232FiscalPrinterInfoHelper.getInstance().getVersion();
   }

   public boolean isFlashable() {
      return false;
   }

   public void init() throws HandleException {
      super.init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      int ln = 0;
      byte[] cmddata = null;
      if (this.isTracerOn()) {
         this.traceNormal("-->submit");
      }

      if (cmd == null) {
         if (this.isTracerOn()) {
            this.traceNormal("null command exception");
         }

         throw new HandleException("Attempted to submit a null command to handle");
      } else if (this.protocol == null) {
         if (this.isTracerOn()) {
            this.traceNormal("null protocol exception?");
         }

         throw new HandleException("Attempted to submit when protocol is null");
      } else {
         try {
            switch (cmd.getCode()) {
               case 100:
                  if (this.isTracerOn()) {
                     this.traceNormal("TEST_REQUEST_CMD_CODE");
                  }

                  ln = cmd.toBytes().length;
                  cmddata = new byte[ln];
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 0, ln);
                  this.protocol.write(cmddata, ln);
                  break;
               case 101:
                  if (this.isTracerOn()) {
                     this.traceNormal("STATUS_REQUEST_CMD_CODE");
                  }

                  ln = cmd.toBytes().length;
                  cmddata = new byte[ln];
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 0, ln);
                  this.protocol.write(cmddata, ln);
                  break;
               case 102:
                  if (this.isTracerOn()) {
                     this.traceNormal("RESET_CMD_CODE");
                  }

                  this.protocol.resetPrinter();
                  break;
               case 103:
                  if (this.isTracerOn()) {
                     this.traceNormal("DEVICE_INFO_REQUEST_CMD_CODE");
                  }

                  ln = cmd.toBytes().length;
                  cmddata = new byte[ln];
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 0, ln);
                  int i = 0;

                  while (!this.iplEndReceived && i < 30) {
                     i++;

                     try {
                        Thread.sleep(100L);
                     } catch (InterruptedException var12) {
                     }
                  }

                  this.protocol.write(cmddata, ln);
                  i = 0;

                  while (!this.devInfoReceived && i < 30) {
                     i++;

                     try {
                        Thread.sleep(100L);
                     } catch (InterruptedException var11) {
                     }
                  }

                  ((SystemCmd.DeviceInfoRequestCmd)cmd).setFirmwareLevel(Rs232FiscalPrinterInfoHelper.getInstance().getVersion());
                  if (this.devID == 0) {
                     ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceType(2702);
                     if (this.countryCode == 6) {
                        ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceId(2711);
                     } else {
                        ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceId(2712);
                     }
                  } else if (this.devID == 1) {
                     ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceType(2703);
                     ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceId(2713);
                  }
                  break;
               case 104:
                  if (this.isTracerOn()) {
                     this.traceNormal("_DIRECT_WRITE_CMD_CODE");
                  }

                  ln = cmd.toBytes().length;
                  cmddata = new byte[ln];
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 0, ln);
                  this.protocol.write(cmddata, ln);
                  break;
               case 1201:
                  if (this.isTracerOn()) {
                     this.traceNormal("_WRITE_CODE");
                  }

                  ln = cmd.toBytes().length + 2;
                  cmddata = new byte[ln];
                  cmddata[0] = 27;
                  cmddata[1] = 102;
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 2, ln - 2);
                  this.protocol.write(cmddata, ln);
                  break;
               default:
                  if (this.isTracerOn()) {
                     this.traceNormal("unknown command");
                  }

                  throw new HandleException("unknown command");
            }
         } catch (HandleException var13) {
            if (this.isTracerOn()) {
               this.traceNormal("handle exception");
            }

            this.setHandleCmdResultInError(cmd, true);
            throw var13;
         } finally {
            cmd.setCompleted(true);
         }

         cmd.setCompleted(true);
         if (this.isTracerOn()) {
            this.traceNormal("<--submit");
         }
      }
   }

   public void writeToPort(byte[] data, int length) {
      if (this.isTracerOn()) {
         this.traceNormal("-->writeToPort");
      }

      try {
         super.submit(data, 0, length);
      } catch (HandleException var4) {
         if (this.isTracerOn()) {
            this.traceNormal("writeToPort Error: " + var4.toString());
         }
      }

      if (this.isTracerOn()) {
         this.traceNormal("<--writeToPort");
      }
   }

   public void fireErrorEvent(int type) {
      if (this.isTracerOn()) {
         this.traceNormal("-->fireErrorEvent");
      }

      this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, type));
      if (this.isTracerOn()) {
         this.traceNormal("<--fireErrorEvent");
      }
   }

   public void fireDataEvent(byte[] data) {
      if (this.isTracerOn()) {
         this.traceNormal("-->fireDataEvent");
         this.traceNormal("data length: " + data.length);
      }

      if ((data[9] & -128) == -128) {
         this.devInfoReceived = true;
         this.devID = data[16];
         this.countryCode = data[10];
      }

      this.getHandle().getEventHelper().fireDataEvent(new DataEvent(this, data));
      if (this.isTracerOn()) {
         this.traceNormal("<--fireDataEvent");
      }
   }

   public void fireDirectIOEvent(byte[] data) {
      if (this.isTracerOn()) {
         this.traceNormal("-->fireDirectIOEvent");
         this.traceNormal("data length: " + data.length);
      }

      this.getHandle().getEventHelper().fireDirectIOEvent(new DirectIOEvent(this, data));
      if (this.isTracerOn()) {
         this.traceNormal("<--fireDirectIOEvent");
      }
   }

   public void fireStatusEvent(int sts) {
      if (this.isTracerOn()) {
         this.traceNormal("-->fireStatusEvent");
      }

      if (sts == 2) {
         this.iplEndReceived = true;
      }

      this.getHandle().getEventHelper().fireStatusEvent(new StatusEvent(this, sts));
      if (this.isTracerOn()) {
         this.traceNormal("<--fireStatusEvent");
      }
   }

   public FiscalPrinterInfoHelper getInfoHelper() {
      return Rs232FiscalPrinterInfoHelper.getInstance();
   }

   public void startProtocol() {
      if (this.protocol == null) {
         this.protocol = new Rs232FiscalPrinterProtocol();
         ((Rs232PortCommAdapter)this.getRs232Port()).setReaderStrategy(this.protocol);
         ((Rs232PortCommAdapter)this.getRs232Port()).setDTR(false);
         this.protocol.setHandleImp(this);
      }
   }

   public void stopProtocol() {
      if (this.protocol != null) {
         this.protocol.setHandleImp(null);
         this.protocol = null;
      }
   }

   public boolean isDirectIOMode() {
      return this.getHandle().isDirectIOMode();
   }

   protected void offlineEventOccurred(Rs232OfflineEvent rs232OEvent) {
   }
}
