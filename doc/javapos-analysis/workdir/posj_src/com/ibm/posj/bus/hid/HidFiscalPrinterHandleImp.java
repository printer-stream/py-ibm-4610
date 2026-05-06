package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.HidException;
import com.ibm.hid.ReportEvent;
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

public class HidFiscalPrinterHandleImp extends AbstractHidHandleImp implements HidHandleImp, FiscalPrinterHandleImp {
   private boolean protocolStarted = false;
   private boolean IPLEndInProgress = true;
   private static final byte[] RESET_CMD = new byte[]{1, 4, 0, -6, 2};
   private boolean devInfoReceived = false;
   private byte devID;
   private byte countryCode;

   public HidFiscalPrinterHandleImp(HandleKey key, HidDevice hidDevice) {
      super(key, hidDevice);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitFiscalPrinter(this);
   }

   public DevCat getDevCat() {
      return DevCats.FISCALPRINTER_DEVCAT;
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidFiscalPrinterImp(this);
   }

   public FiscalPrinterInfoHelper getInfoHelper() {
      return HidFiscalPrinterInfoHelper.getInstance();
   }

   public void startProtocol() {
      if (!this.protocolStarted) {
         this.protocolStarted = true;
         this.IPLEndInProgress = true;

         try {
            this.submitCmd("Reset Printer(FA02)", RESET_CMD);
         } catch (HandleException var2) {
            if (this.isTracerOn()) {
               this.traceNormal("startProtocol-->Exception while submiting reset command<--startProtocol");
            }
         }
      }
   }

   public void stopProtocol() {
      this.protocolStarted = false;
   }

   protected void reportEventOccurred(ReportEvent rE) {
      byte[] buffer = null;
      if (this.isTracerOn()) {
         this.traceNormal("-->dataEventOccurred");
         this.traceNormal("data length: " + rE.getData().length);
      }

      if (this.getHandle() == null) {
         if (this.isTracerOn()) {
            this.traceNormal("null handle <--dataEventOccurred");
         }
      } else {
         int dataLen = rE.getData()[2] & 255;
         dataLen <<= 8;
         dataLen |= rE.getData()[1] & 255;
         if (dataLen >= 64 && dataLen <= 255) {
            try {
               if (this.isTracerOn()) {
                  this.traceNormal("Retrieving Feature 2");
               }

               byte[] feature = this.getHidDevice().getReport((byte)3, (byte)2);
               if (this.isTracerOn() && feature != null) {
                  this.traceNormal("Feature 2=" + feature.length);
               }

               if (feature != null) {
                  buffer = new byte[feature.length - 3];
                  System.arraycopy(feature, 3, buffer, 0, feature.length - 3);
               }
            } catch (HidException var6) {
               if (this.isTracerOn()) {
                  this.traceNormal("unable to retrieve feature report <--dataEventOccurred");
               }
            }
         } else if (dataLen >= 256 && dataLen <= 2111) {
            try {
               if (this.isTracerOn()) {
                  this.traceNormal("Retrieving Feature 1");
               }

               byte[] featurex = this.getHidDevice().getReport((byte)3, (byte)1);
               if (this.isTracerOn() && featurex != null) {
                  this.traceNormal("Feature 1=" + featurex.length);
               }

               if (featurex != null) {
                  buffer = new byte[featurex.length - 3];
                  System.arraycopy(featurex, 3, buffer, 0, featurex.length - 3);
               }
            } catch (HidException var5) {
               if (this.isTracerOn()) {
                  this.traceNormal("unable to retrieve feature report <--dataEventOccurred");
               }
            }
         } else {
            buffer = new byte[dataLen - 2];
            System.arraycopy(rE.getData(), 3, buffer, 0, dataLen - 2);
         }

         if (this.getHandle().isDirectIOMode()) {
            this.getHandle().getEventHelper().fireDirectIOEvent(new DirectIOEvent(this, buffer));
         }

         if ((rE.getData()[12] & -128) == -128) {
            this.devInfoReceived = true;
            this.devID = rE.getData()[19];
            this.countryCode = rE.getData()[13];
         }

         if (this.IPLEndInProgress & (rE.getData()[11] & 4) == 0) {
            this.getHandle().getEventHelper().fireStatusEvent(new StatusEvent(this, 2));
            this.IPLEndInProgress = false;
            if (this.isTracerOn()) {
               this.traceNormal("<--dataEventOccurred");
            }
         } else {
            this.IPLEndInProgress = (rE.getData()[11] & 4) != 0;
            if (this.isTracerOn() && this.IPLEndInProgress) {
               this.traceNormal("IPL in progress");
            }

            HidFiscalPrinterInfoHelper.getInstance().setFiscalInformation(buffer);
            this.getHandle().getEventHelper().fireDataEvent(new DataEvent(this, buffer));
            if (this.isTracerOn()) {
               this.traceNormal("<--dataEventOccurred");
            }
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

                  ln = cmd.toBytes().length + 2;
                  cmddata = new byte[ln + 1];
                  cmddata[0] = 1;
                  cmddata[1] = (byte)(ln % 256);
                  cmddata[2] = (byte)(ln / 256);
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 3, cmd.toBytes().length);
                  this.submitCmd(cmd.getName(), cmddata);
                  break;
               case 101:
                  if (this.isTracerOn()) {
                     this.traceNormal("STATUS_REQUEST_CMD_CODE");
                  }

                  ln = cmd.toBytes().length + 2;
                  cmddata = new byte[ln + 1];
                  cmddata[0] = 1;
                  cmddata[1] = (byte)(ln % 256);
                  cmddata[2] = (byte)(ln / 256);
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 3, cmd.toBytes().length);
                  this.submitCmd(cmd.getName(), cmddata);
                  break;
               case 102:
                  if (this.isTracerOn()) {
                     this.traceNormal("RESET_CMD_CODE");
                  }

                  ln = cmd.toBytes().length + 2;
                  cmddata = new byte[ln + 1];
                  cmddata[0] = 1;
                  cmddata[1] = (byte)(ln % 256);
                  cmddata[2] = (byte)(ln / 256);
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 3, cmd.toBytes().length);
                  this.submitCmd(cmd.getName(), cmddata);
                  break;
               case 103:
                  if (this.isTracerOn()) {
                     this.traceNormal("DEVICE_INFO_REQUEST_CMD_CODE");
                  }

                  ln = cmd.toBytes().length + 2;
                  cmddata = new byte[ln + 1];
                  cmddata[0] = 1;
                  cmddata[1] = (byte)(ln % 256);
                  cmddata[2] = (byte)(ln / 256);
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 3, cmd.toBytes().length);
                  int i = 0;

                  while (this.IPLEndInProgress && i < 30) {
                     i++;

                     try {
                        Thread.sleep(100L);
                     } catch (InterruptedException var12) {
                     }
                  }

                  this.submitCmd(cmd.getName(), cmddata);
                  i = 0;

                  while (!this.devInfoReceived && i < 30) {
                     i++;

                     try {
                        Thread.sleep(100L);
                     } catch (InterruptedException var11) {
                     }
                  }

                  ((SystemCmd.DeviceInfoRequestCmd)cmd).setFirmwareLevel(HidFiscalPrinterInfoHelper.getInstance().getVersion());
                  if (this.devID == 0) {
                     ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceType(2702);
                     ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceId(2724);
                  } else if (this.devID == 1) {
                     ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceType(2703);
                     ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceId(2723);
                  }
                  break;
               case 104:
                  if (this.isTracerOn()) {
                     this.traceNormal("DIRECT_WRITE_CMD_CODE");
                  }

                  ln = cmd.toBytes().length + 2;
                  cmddata = new byte[ln + 1];
                  cmddata[0] = 1;
                  cmddata[1] = (byte)(ln % 256);
                  cmddata[2] = (byte)(ln / 256);
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 3, cmd.toBytes().length);
                  this.submitCmd(cmd.getName(), cmddata);
                  break;
               case 1201:
                  if (this.isTracerOn()) {
                     this.traceNormal("_WRITE_CODE");
                  }

                  ln = cmd.toBytes().length + 4;
                  cmddata = new byte[ln + 1];
                  cmddata[0] = 1;
                  cmddata[1] = (byte)(ln % 256);
                  cmddata[2] = (byte)(ln / 256);
                  cmddata[3] = 27;
                  cmddata[4] = 102;
                  System.arraycopy(cmd.toBytes(), 0, cmddata, 5, cmd.toBytes().length);
                  this.submitCmd(cmd.getName(), cmddata);
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
}
