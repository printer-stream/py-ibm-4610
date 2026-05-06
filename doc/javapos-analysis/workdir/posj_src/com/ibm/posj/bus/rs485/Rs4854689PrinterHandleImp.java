package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jutil.ByteArrayCollector;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.SleepPolicy;
import com.ibm.jutil.Util;
import com.ibm.jutil.tasks.AbstractActiveObject;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.IBM4689PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.bus.FlashHandleImp;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.bus.POSPrinterHandleImp;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.bus.PrinterSubDevices;
import com.ibm.posj.bus.printer.IBM4689Utility;
import com.ibm.posj.bus.printer.cmds.ibm4689.Cmd4689;
import com.ibm.posj.bus.printer.cmds.ibm4689.Print4689CmdFactory;
import com.ibm.posj.flash.FlashException;
import com.ibm.posj.flash.FlashHandleImpVisitable;
import com.ibm.posj.flash.FlashPrinterHandleImpVisitor;
import com.ibm.posj.flash.FlashRequest;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.IBM4689Status;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.printer.ibm4689.IBM4689Imp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Rs4854689PrinterHandleImp extends Rs485POSPrinterHandleImp implements POSPrinterHandleImp, FlashHandleImpVisitable {
   private static final int MAX_BYTES_DATA_PER_CMD = 1400;
   private int hangCnt = 0;
   private static int MAX_HANG_CNT = 3;
   private byte feature1;
   private byte feature2;
   private Rs485FlashPrinterHandleImp rs485FlashPrinterHandleImp = null;
   private boolean initialized = false;
   private final int PRINTER_DATA_MAX = 1400;
   private static final int INIT_MAX_WAIT = 10000;
   protected IBM4689PrinterCmd.Factory factory = null;
   protected IBMPrinterImp prImp = null;
   protected IBM4689Utility util = null;
   private PrinterWriter writer;
   public static byte[] transfer = new byte[0];
   private Rs4854689PrinterHandleImp.CoverClosePoller poller = new Rs4854689PrinterHandleImp.CoverClosePoller(this.getHandle(), this.factory);
   List extraDataList = new ArrayList(2);

   public Rs4854689PrinterHandleImp(HandleKey key, SioDevice device) {
      super(key, device);
      this.util = new IBM4689Utility();
      this.factory = new IBM4689PrinterCmd.Factory(new Print4689CmdFactory(1400, new Cmd4689()));
      this.rs485FlashPrinterHandleImp = new Rs485Flash4689PrtHandleImp(key, device);
   }

   public void accept(FlashPrinterHandleImpVisitor visitor) {
      visitor.visit4689PosPrinter(this);
   }

   public void init() throws HandleException {
      if (!this.initialized) {
         super.init();
         this.util.init(this, this.getPrinterImp(), null, 10000);
         this.initialized = true;
         this.poller.start();
      }
   }

   public boolean isFlashable() {
      return true;
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
      return this.factory;
   }

   public IBMPrinterImp getPrinterImp() {
      if (null == this.prImp) {
         DefaultPOSPrinterHandle def = (DefaultPOSPrinterHandle)this.getHandle();
         this.prImp = (IBM4689Imp)this.util.createPrinterImp(this.getWriter(), def);
         this.util.addDevice(this.prImp.getHandleKey(), this.prImp);
      }

      return this.prImp;
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
         Cmd4689 cmd = (Cmd4689)this.factory.getPrintCmdFactory().getCmdBytes();

         try {
            this.writeToBus(cmd.STATUS_REQUEST, 0, cmd.STATUS_REQUEST.length);
         } catch (Exception var3) {
         }

         return null;
      }
   }

   public void clearBuffers() throws HandleException {
      Cmd4689 cmd = (Cmd4689)this.factory.getPrintCmdFactory().getCmdBytes();
   }

   public PrintDataEvent createPrintDataEvent() {
      Rs4854689PrinterHandleImp.ExtraData ed = (Rs4854689PrinterHandleImp.ExtraData)this.extraDataList.remove(0);
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
      PrintStatus.PrintStatusFactory psf = IBM4689Status.getPrintStatusFactory(this);
      PrintStatus ps = psf.createPrintStatus(data, ((POSPrinterHandle)this.getHandle()).getPrinterLogHelper());
      if (data.length > 8) {
         Rs4854689PrinterHandleImp.ExtraData ed = new Rs4854689PrinterHandleImp.ExtraData();
         ed.extraDataLen = data.length - 8;
         ed.extraDataType = this.util.getPrinterDataType(ps);
         if (ed.extraData.length != ed.extraDataLen) {
            ByteArrayCollector.getCollector().collect(ed.extraData);
            ed.extraData = ByteArrayCollector.getCollector().getArray(ed.extraDataLen);
         }

         System.arraycopy(data, 8, ed.extraData, 0, ed.extraDataLen);
         this.extraDataList.add(ed);
      }

      if (this.isTracerOn()) {
         this.traceNormal("createPrintStatusEvent " + Util.toFormatedHexString(data));
      }

      if (ps.isReady()) {
         this.poller.pulse(true);
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

   public void enable() {
      this.poller.pulse(true);
   }

   public void disable() {
      this.poller.pulse(false);
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
      return null;
   }

   public synchronized PrinterWriter getWriter() {
      this.util.setTracer(this.getTracer());
      if (this.writer == null) {
         PrinterPacket packet = new PrinterPacket(this, transfer, 1400);
         this.writer = this.util.createWriter(this, packet, (POSPrinterHandle)this.getHandle(), this.factory);
      }

      return this.writer;
   }

   protected void deviceOffline() {
      this.disable();
      Object dE = null;
      Iterator x = this.getSecondaryHandleImps();

      while (x.hasNext()) {
         PrinterSubDevices hi = (PrinterSubDevices)x.next();

         try {
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
                     this.getTracer().println(var5);
                  }
               }
            }

            POSPrinterCmd cmd = this.getReinitCmds();

            try {
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
         Cmd4689 cmd = (Cmd4689)this.factory.getPrintCmdFactory().getCmdBytes();
         byte[] initialize = new byte[cmd.INITIALIZE.length + 1];
         System.arraycopy(cmd.INITIALIZE, 0, initialize, 0, cmd.INITIALIZE.length);
         initialize[cmd.INITIALIZE.length] = 1;
         this.writeToBus(initialize, 0, initialize.length);
      } catch (Exception var3) {
         this.traceNormal("error :" + var3.toString());
         this.getTracer().print(var3);
      }

      if (this.isTracerOn()) {
         this.traceNormal("<--submitResetCmd");
      }
   }

   protected void writeToBus(byte[] data, int offset, int len) throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal(">>Sync Submit in Rs4854689Imp ");
         this.traceMaximum(Util.toFormatedHexString(data, offset, len));
         this.timestamp = System.currentTimeMillis();
         this.traceNormal(">>writeToBus.DataLength-->" + len);
      }

      this.poller.pulse(false);
      super.submitSync(data, offset, len);
      if (this.isTracerOn()) {
         this.traceNormal("<<Sync Submit in Rs4854689Imp " + (System.currentTimeMillis() - this.timestamp));
      }
   }

   public class CoverClosePoller extends AbstractActiveObject {
      private boolean send = false;
      private final long WAIT_STATUS_POLL = 3200L;
      private byte[] statusCmd = new byte[]{0, 32, 0, 0, 0, 0, 1};

      public CoverClosePoller(Handle handle, IBM4689PrinterCmd.Factory factory) {
      }

      public void pulse(boolean x) {
         this.send = x;
      }

      protected void runActiveObject() {
         while (true) {
            try {
               if (this.send) {
                  Rs4854689PrinterHandleImp.this.writeToBus(this.statusCmd, 0, this.statusCmd.length);
               }

               SleepPolicy.sleep(3200L);
            } catch (Exception var2) {
            }
         }
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
