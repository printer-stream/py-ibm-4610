package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jsio.event.SioDeviceDataEvent;
import com.ibm.posj.FiscalPrinterInfoHelper;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.FiscalPrinterHandleImp;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.event.DataEvent;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public class Rs485FiscalPrinterHandleImp extends AbstractRs485HandleImp implements FiscalPrinterHandleImp {
   private boolean protocolStarted = false;
   private boolean devInfoReceived = false;
   private byte devID = 0;

   public Rs485FiscalPrinterHandleImp(HandleKey key, SioDevice sioDevice) {
      super(key, sioDevice);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitFiscalPrinter(this);
   }

   public DevCat getDevCat() {
      return DevCats.FISCALPRINTER_DEVCAT;
   }

   public short getECLevel() {
      return (short)Rs485FiscalPrinterInfoHelper.getInstance().getECLevel();
   }

   public FiscalPrinterInfoHelper getInfoHelper() {
      return Rs485FiscalPrinterInfoHelper.getInstance();
   }

   public void startProtocol() {
      if (!this.protocolStarted) {
         this.protocolStarted = true;
         Rs485FiscalPrinterHandleImp.IPLEndSimm submitter = new Rs485FiscalPrinterHandleImp.IPLEndSimm(this);
         submitter.start();
      }
   }

   public void stopProtocol() {
      this.protocolStarted = false;
   }

   protected void dataEventOccurred(SioDeviceDataEvent event) {
      if (this.isTracerOn()) {
         this.traceNormal("-->dataEventOccurred");
         this.traceNormal("data length: " + event.getData().length);
      }

      if (this.getHandle() == null) {
         if (this.isTracerOn()) {
            this.traceNormal("null handle <--dataEventOccurred");
         }
      } else {
         if (this.getHandle().isDirectIOMode()) {
            this.getHandle().getEventHelper().fireDirectIOEvent(new DirectIOEvent(this, event.getData()));
         }

         if (Rs485FiscalPrinterInfoHelper.getInstance().isDeviceInfo(event.getData())) {
            this.devInfoReceived = true;
         }

         Rs485FiscalPrinterInfoHelper.getInstance().setFiscalInformation(event.getData());
         this.getHandle().getEventHelper().fireDataEvent(new DataEvent(this, event.getData()));
         if (this.isTracerOn()) {
            this.traceNormal("<--dataEventOccurred");
         }
      }
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
                  this.submitSync(cmddata, 0, ln);
                  break;
               case 101:
                  if (this.isTracerOn()) {
                     this.traceNormal("STATUS_REQUEST_CMD_CODE");
                  }

                  ln = cmd.toBytes().length;
                  cmddata = new byte[ln];
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 0, ln);
                  this.submitSync(cmddata, 0, ln);
                  break;
               case 102:
                  if (this.isTracerOn()) {
                     this.traceNormal("RESET_CMD_CODE");
                  }

                  ln = cmd.toBytes().length;
                  cmddata = new byte[ln];
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 0, ln);
                  this.submitSync(cmddata, 0, ln);
                  break;
               case 103:
                  if (this.isTracerOn()) {
                     this.traceNormal("DEVICE_INFO_REQUEST_CMD_CODE");
                  }

                  ln = cmd.toBytes().length;
                  cmddata = new byte[ln];
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 0, ln);
                  this.submitSync(cmddata, 0, ln);
                  int i = 0;

                  while (!this.devInfoReceived && i < 30) {
                     i++;

                     try {
                        Thread.sleep(100L);
                     } catch (InterruptedException var10) {
                     }
                  }

                  ((SystemCmd.DeviceInfoRequestCmd)cmd).setFirmwareLevel(this.getECLevel());
                  if (this.devID == 0) {
                     ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceType(2702);
                     ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceId(2720);
                  } else if (this.devID == 1) {
                     ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceType(2703);
                     ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceId(2719);
                  }
                  break;
               case 104:
                  if (this.isTracerOn()) {
                     this.traceNormal("_DIRECT_WRITE_CMD_CODE");
                  }

                  ln = cmd.toBytes().length;
                  cmddata = new byte[ln];
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 0, ln);
                  this.submitSync(cmddata, 0, ln);
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
                  this.submitSync(cmddata, 0, ln);
                  break;
               default:
                  if (this.isTracerOn()) {
                     this.traceNormal("unknown command");
                  }

                  throw new HandleException("unknown command");
            }
         } catch (HandleException var11) {
            if (this.isTracerOn()) {
               this.traceNormal("handle exception");
            }

            this.setHandleCmdResultInError(cmd, true);
            throw var11;
         } finally {
            cmd.setCompleted(true);
         }

         cmd.setCompleted(true);
         if (this.isTracerOn()) {
            this.traceNormal("<--submit");
         }
      }
   }

   private class IPLEndSimm extends Thread {
      private Rs485FiscalPrinterHandleImp handleImp = null;

      public IPLEndSimm(Rs485FiscalPrinterHandleImp imp) {
         this.handleImp = imp;
      }

      public void run() {
         try {
            sleep(2000L);
         } catch (InterruptedException var3) {
         }

         byte[] data = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 67};
         this.handleImp.getHandle().getEventHelper().fireStatusEvent(new StatusEvent(this.handleImp, 2));
         SioDeviceDataEvent ev = new SioDeviceDataEvent(this.handleImp.getSioDevice(), this.handleImp.getSioDevice().createSioIrp(data, false), data);
         this.handleImp.dataEventOccurred(ev);
      }
   }
}
