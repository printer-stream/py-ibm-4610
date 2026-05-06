package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jsio.event.SioDeviceDataEvent;
import com.ibm.jutil.ByteArrayCollector;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Util;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.bus.FlashHandleImp;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.bus.IBM4610PrinterHandleImp;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.bus.PrinterSubDevices;
import com.ibm.posj.bus.printer.IBM4610Utility;
import com.ibm.posj.bus.printer.cmds.ibm4610.Cmd4610;
import com.ibm.posj.bus.printer.cmds.ibm4610.Print4610CmdFactory;
import com.ibm.posj.flash.FlashException;
import com.ibm.posj.flash.FlashHandleImpVisitable;
import com.ibm.posj.flash.FlashPrinterHandleImpVisitor;
import com.ibm.posj.flash.FlashRequest;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.P4610Status;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.printer.ibm4610.IBM4610Imp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Rs4854610PrinterHandleImp extends Rs485POSPrinterHandleImp implements IBM4610PrinterHandleImp, FlashHandleImpVisitable {
   private int hangCnt = 0;
   private byte feature1;
   private byte feature2;
   private IBM4610Utility util = null;
   private boolean initialized = false;
   private Rs485FlashPrinterHandleImp rs485FlashPrinterHandleImp = null;
   List extraDataList = new ArrayList(2);
   protected IBM4610Imp prImp = null;
   private PrinterWriter writer;
   private static IBM4610PrinterCmd.Factory factory = null;
   private static int MAX_HANG_CNT = 3;
   public static byte[] transfer = new byte[0];
   private int PRINTER_DATA_MAX = 1018;
   private int TI12_DATA_MAX = 248;
   private static final int INIT_MAX_WAIT = 10000;

   public Rs4854610PrinterHandleImp(HandleKey key, SioDevice device, IBM4610PrinterHandleImp.PrinterInfo printerType) {
      super(key, device);
      this.util = new IBM4610Utility(printerType.getType(), this.getTracer());
      if (3819 == printerType.getId()) {
         this.PRINTER_DATA_MAX = this.TI12_DATA_MAX;
      }

      factory = new IBM4610PrinterCmd.Factory(new Print4610CmdFactory(this.PRINTER_DATA_MAX, new Cmd4610(), this.util.isLineFlag()));
      this.rs485FlashPrinterHandleImp = new Rs485FlashPrinterHandleImp(key, device);
   }

   public void accept(FlashPrinterHandleImpVisitor visitor) {
      visitor.visit4610PosPrinter(this);
   }

   public void init() throws HandleException {
      if (!this.initialized) {
         super.init();
         this.util.init(this, this.getPrinterImp(), this.getReinitCmds(), 10000);
         this.initialized = true;
      }
   }

   public byte getFeatureByte1() {
      return this.feature1;
   }

   public byte getFeatureByte2() {
      return this.feature2;
   }

   public void addDevice(HandleKey key, PrinterSubDevices printerSubDevice) {
      this.util.addDevice(key, printerSubDevice);
   }

   public POSPrinterCmd.Factory getPrintCmdFactory() {
      return factory;
   }

   public IBMPrinterImp getPrinterImp() {
      if (null == this.prImp) {
         DefaultPOSPrinterHandle def = (DefaultPOSPrinterHandle)this.getHandle();
         this.prImp = (IBM4610Imp)this.util.createPrinterImp(this.getWriter(), def);
         this.util.addDevice(this.prImp.getHandleKey(), this.prImp);
      }

      return this.prImp;
   }

   protected void dataEventOccurred(SioDeviceDataEvent event) {
      super.dataEventOccurred(event);
   }

   protected void firePrintDataEvent(PrintDataEvent pde) {
      this.getPrinterImp().receivePrintDataEvent(pde);
      this.util.firePrintDataEvent(pde);
   }

   public void distributeStatus(PrintStatus ps) {
      this.util.subDeviceDistribute(this.getHandle(), ps);
   }

   public void processExtraData(PrintStatus ps) {
      this.firePrintDataEvent(this.createPrintDataEvent());
   }

   public Iterator getSecondaryHandleImps() {
      return this.util.getSecondaryHandleImps();
   }

   public HandleImp getMainHandleImp() {
      return this;
   }

   public List respondToFreeze() {
      this.hangCnt++;
      if (MAX_HANG_CNT <= this.hangCnt) {
         return null;
      } else {
         Cmd4610 cmd = (Cmd4610)factory.getPrintCmdFactory().getCmdBytes();

         try {
            try {
               this.writeToBus(cmd.STATUS_REQUEST, 0, cmd.STATUS_REQUEST.length);
            } catch (Exception var6) {
            }

            return null;
         } finally {
            ;
         }
      }
   }

   public void clearBuffers() throws HandleException {
      Cmd4610 cmd = (Cmd4610)factory.getPrintCmdFactory().getCmdBytes();

      try {
         try {
            this.writeToBus(cmd.CLEAR, 0, cmd.CLEAR.length);
         } catch (Exception var6) {
         }
      } finally {
         ;
      }
   }

   public PrintDataEvent createPrintDataEvent() {
      Rs4854610PrinterHandleImp.ExtraData ed = (Rs4854610PrinterHandleImp.ExtraData)this.extraDataList.remove(0);
      if (null != ed.extraData && -1 != ed.extraDataType) {
         int len = ed.extraDataLen;
         if (this.isTracerOn()) {
            this.traceNormal("createPrintDataEvent " + Util.toFormatedHexString(ed.extraData));
         }

         ByteBuffer xdata = ByteBuffer.getByteBufferFactory().createByteBuffer(len);
         xdata.append(ed.extraData);
         xdata.setByteCount(len, true);
         if (1 == ed.extraDataType) {
            this.feature1 = xdata.getBytesRef()[2];
            this.feature2 = xdata.getBytesRef()[3];
         }

         return new PrintDataEvent(this, ed.extraDataType, xdata);
      } else {
         return null;
      }
   }

   public PrintStatus createPrintStatus(byte[] data) {
      this.hangCnt = 0;
      PrintStatus.PrintStatusFactory psf = P4610Status.getPrintStatusFactory(this);
      PrintStatus ps = psf.createPrintStatus(data, ((POSPrinterHandle)this.getHandle()).getPrinterLogHelper());
      if (data.length > 8) {
         Rs4854610PrinterHandleImp.ExtraData ed = new Rs4854610PrinterHandleImp.ExtraData();
         ed.extraDataLen = data.length - 8;
         ed.extraDataType = this.util.getPrinterDataType(ps);
         if (ed.extraData.length != ed.extraDataLen) {
            ByteArrayCollector.getCollector().collect(ed.extraData);
            ed.extraData = ByteArrayCollector.getCollector().getArray(ed.extraDataLen);
         }

         System.arraycopy(data, 8, ed.extraData, 0, ed.extraDataLen);
         if (null == this.getHandle() || !this.getHandle().isDirectIOMode()) {
            this.extraDataList.add(ed);
         }
      }

      if (this.isTracerOn()) {
         this.traceNormal("createPrintStatusEvent " + Util.toFormatedHexString(data));
      }

      return ps;
   }

   public FlashHandleImp getFlashHandleImp() {
      return this.rs485FlashPrinterHandleImp;
   }

   public void flash(FlashRequest flashRequest) throws FlashException {
      try {
         this.rs485FlashPrinterHandleImp.flash(flashRequest);
      } catch (FlashException var3) {
         throw var3;
      }
   }

   public void rawSubmit(byte[] data) throws HandleException {
      this.writeToBus(data, 0, data.length);
   }

   protected void sioIrpDiscarded() {
      byte[] error = new byte[]{1, 1, 1, 1, 1, 1, 1, 1};
      this.statusAccum.post(error, false);
   }

   protected void communicationError() {
      byte[] error = new byte[]{1, 1, 1, 1, 1, 1, 1, 1};
      this.statusAccum.post(error, false);
      this.submitResetCmd();
   }

   protected POSPrinterCmd getReinitCmds() {
      POSPrinterCmd cmd = factory.createStatusSentCmd(true, true, true, true, true, true, true);
      cmd.appendPOSPrinterCmd(factory.createErrorRecoveryCmd(true, false, false, false));
      boolean t = !PosSystemManager.getInstance().getProperties().isPropertyDefined("rs485.4610.EC");
      cmd.appendPOSPrinterCmd(factory.createEnableLineCntCmd(t));
      return cmd;
   }

   public synchronized PrinterWriter getWriter() {
      this.util.setTracer(this.getTracer());
      if (this.writer == null) {
         PrinterPacket packet = new PrinterPacket(this, transfer, this.PRINTER_DATA_MAX);
         this.writer = this.util.createWriter(this, packet, (POSPrinterHandle)this.getHandle(), factory);
      }

      return this.writer;
   }

   protected void deviceOffline() {
      Object dE = null;
      Iterator x = this.getSecondaryHandleImps();

      while (x.hasNext()) {
         PrinterSubDevices hi = (PrinterSubDevices)x.next();

         try {
            if (this.isTracerOn()) {
               this.traceNormal("set offline -> " + hi);
            }

            hi.offLine(dE);
         } catch (Exception var5) {
            if (this.isTracerOn()) {
               this.getTracer().print(var5);
            }
         }
      }
   }

   protected void reinitialize() throws HandleException {
      if (!this.isReset) {
         if (!this.getHandle().isOnline()) {
            Iterator x = this.getSecondaryHandleImps();

            while (x.hasNext()) {
               PrinterSubDevices hi = (PrinterSubDevices)x.next();

               try {
                  hi.onLine(null);
               } catch (Exception var5) {
                  if (this.isTracerOn()) {
                     this.getTracer().print(var5);
                  }
               }
            }

            POSPrinterCmd cmd = this.getReinitCmds();

            try {
               this.getPrinterImp().submit((HandleCmd)cmd);
               cmd.waitUntilCompleted(1850L);
               this.getHandle().getState().setOnline(true);
            } catch (Exception var4) {
               if (this.isTracerOn()) {
                  this.getTracer().print(var4);
               }

               this.getHandle().getState().setOnline(false);
            }
         }
      }
   }

   private void submitResetCmd() {
      if (this.isTracerOn()) {
         this.traceNormal("-->submitResetCmd");
         this.traceNormal("Reseting the device due an error");
      }

      try {
         Cmd4610 cmd = (Cmd4610)factory.getPrintCmdFactory().getCmdBytes();
         this.writeToBus(cmd.RESET, 0, cmd.RESET.length);
      } catch (Exception var2) {
         this.traceNormal("error :" + var2.toString());
         this.getTracer().print(var2);
      }

      if (this.isTracerOn()) {
         this.traceNormal("<--submitResetCmd");
      }
   }

   private class ExtraData {
      byte[] extraData = new byte[100];
      int extraDataLen = -1;
      int extraDataType = -1;

      private ExtraData() {
      }
   }
}
