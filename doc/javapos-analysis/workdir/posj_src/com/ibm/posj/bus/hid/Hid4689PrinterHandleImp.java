package com.ibm.posj.bus.hid;

import com.ibm.hid.DisconnectEvent;
import com.ibm.hid.HidDevice;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.ByteArrayCollector;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.SleepPolicy;
import com.ibm.jutil.Util;
import com.ibm.jutil.tasks.AbstractActiveObject;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.IBM4689PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.bus.POSPrinterHandleImp;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.bus.PrinterSubDevices;
import com.ibm.posj.bus.printer.IBM4689Utility;
import com.ibm.posj.bus.printer.cmds.ibm4689.Cmd4689;
import com.ibm.posj.bus.printer.cmds.ibm4689.Print4689CmdFactory;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.flash.FlashHandleImpVisitable;
import com.ibm.posj.flash.FlashPrinterHandleImpVisitor;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.IBM4689Status;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.printer.ibm4689.IBM4689Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Hid4689PrinterHandleImp extends HidPOSPrinterHandleImp implements HidHandleImp, POSPrinterHandleImp, FlashHandleImpVisitable {
   private byte feature1 = 0;
   private byte feature2 = 0;
   private static final int REPORT_ID = 50;
   protected IBM4689PrinterCmd.Factory factory;
   protected IBMPrinterImp prImp = null;
   protected IBM4689Utility util = null;
   private PrinterWriter writer;
   private boolean initialized = false;
   private IBMPrinterImp printerImp;
   private Object monitorStatusLock = new Object();
   private byte[] lastStatus = null;
   private byte[] initialize = new byte[]{0, 1, 0, 16, 0, 0, 0, 1};
   private ByteBuffer initializeCmd = new ByteBuffer(this.initialize);
   private static final boolean RAW_BYTE_SUBMIT = true;
   private static final int USB_STATUS_SIZE = 1;
   private static final byte PRINTER_STATUS_BYTES_SIZE = 8;
   private static final int RESET_SLEEP = 1600;
   private static final int INIT_MAX_WAIT = 10000;
   private static final int PRINTER_DATA_MAX = 1500;
   private static final byte INTERFACE_NO = 1;
   private static final int SHORT_CMD = 2;
   private static final int LONG_CMD = 1;
   private static int MAX_HANG_CNT = 3;
   private byte hangCnt = 0;
   private List currPDE = new ArrayList(2);
   PrinterPacket packet;
   public static byte[] transfer = new byte[]{1};
   private Hid4689PrinterHandleImp.CoverClosePoller poller = new Hid4689PrinterHandleImp.CoverClosePoller();
   List extraDataList = new ArrayList(2);

   public Hid4689PrinterHandleImp(HandleKey key, HidDevice hidDevice) {
      super(key, hidDevice);
      this.util = new IBM4689Utility();
      this.factory = new IBM4689PrinterCmd.Factory(new Print4689CmdFactory(1500, new Cmd4689()));
   }

   public void accept(FlashPrinterHandleImpVisitor visitor) {
      visitor.visit4689PosPrinter(this);
   }

   public List respondToFreeze() {
      this.hangCnt++;
      if (MAX_HANG_CNT <= this.hangCnt) {
         this.hidDeviceDisconnected(null);
         return null;
      } else {
         Cmd4689 cmd = (Cmd4689)this.factory.getPrintCmdFactory().getCmdBytes();

         try {
            ByteBuffer buff = this.packet.format();
            buff.append(cmd.STATUS_REQUEST);
            this.transport(buff, buff.getByteCount());
         } catch (Exception var3) {
         }

         return null;
      }
   }

   public HandleImp getMainHandleImp() {
      return this;
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHid4689PrinterHandleImp(this);
   }

   public boolean isComposite() {
      return false;
   }

   public boolean isCompositeParent() {
      return true;
   }

   public PrintStatus createPrintStatus(byte[] data) {
      int copy = data.length - 1;
      if (data[0] == 1) {
         System.arraycopy(data, 1, data, 0, copy);
      } else {
         System.arraycopy(data, 2, data, 0, copy);
      }

      PrintStatus.PrintStatusFactory psf = IBM4689Status.getPrintStatusFactory(this);
      PrintStatus ps = psf.createPrintStatus(data, ((POSPrinterHandle)this.getHandle()).getPrinterLogHelper());
      if (data.length > 8) {
         Hid4689PrinterHandleImp.ExtraData ed = new Hid4689PrinterHandleImp.ExtraData();
         ed.extraDataLen = data.length - 8 - 2;
         ed.extraDataType = this.util.getPrinterDataType(ps);
         if (ed.extraData.length != ed.extraDataLen) {
            ByteArrayCollector.getCollector().collect(ed.extraData);
            ed.extraData = ByteArrayCollector.getCollector().getArray(ed.extraDataLen);
         }

         System.arraycopy(data, 8, ed.extraData, 0, ed.extraDataLen);
         this.extraDataList.clear();
         this.extraDataList.add(ed);
      }

      if (this.isTracerOn()) {
         this.traceNormal("createPrintStatusEvent " + Util.toFormatedHexString(data));
      }

      if (!ps.checkErrors()) {
         synchronized (this.monitorStatusLock) {
            this.monitorStatusLock.notifyAll();
         }
      }

      return ps;
   }

   public synchronized PrinterWriter getWriter() {
      this.util.setTracer(this.getTracer());
      if (this.writer == null) {
         this.packet = new PrinterPacket(this, transfer, 1500);
         this.writer = (IBM4689Writer)this.util.createWriter(this, this.packet, (POSPrinterHandle)this.getHandle(), this.factory);
      }

      return this.writer;
   }

   public Iterator getSecondaryHandleImps() {
      return this.util.getSecondaryHandleImps();
   }

   public void transport(ByteBuffer bytes, int len) throws HandleException {
      if (len != 0) {
         byte[] transfer = bytes.getBytesRef();
         transfer[0] = 1;
         if (this.isTracerOn()) {
            this.traceNormal(Util.toFormatedHexString(bytes.getBytes(), 0, len));
            this.traceNormal(">>writeToBus.DataLength-->" + len);
         }

         super.transport(bytes, len, false);
      }
   }

   public synchronized void init() throws HandleException {
      if (!this.initialized) {
         this.initialized = true;
         super.init();
         this.util.init(this, this.imp, this.getReinitCmds(), 10000);
         this.poller.start();
      }
   }

   public POSPrinterCmd.Factory getPrintCmdFactory() {
      return this.factory;
   }

   public PrintDataEvent createPrintDataEvent(ByteBuffer data) {
      Hid4689PrinterHandleImp.ExtraData ed = (Hid4689PrinterHandleImp.ExtraData)this.extraDataList.remove(0);
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

   public void distributeStatus(PrintStatus ps) {
      if (ps.getData()[0] != 0 || ps.getData()[1] != 0 || ps.getData()[2] != 0 || ps.getData()[3] != 0) {
         this.util.subDeviceDistribute(this.getHandle(), ps);
      }
   }

   public void addDevice(HandleKey key, PrinterSubDevices handleImp) {
      this.util.addDevice(key, handleImp);
   }

   public void clearBuffers() throws HandleException {
   }

   public byte getFeatureByte1() {
      return this.feature1;
   }

   public byte getFeatureByte2() {
      return this.feature2;
   }

   public void processExtraData(PrintStatus status) {
      ByteBuffer data = new ByteBuffer(status.getData());
      if (this.isTracerOn()) {
         this.traceNormal(">>processExtraData-->" + Util.toFormatedHexString(data.getBytes()));
      }

      this.firePrintDataEvent(this.createPrintDataEvent(data));
   }

   public void enable() {
      this.poller.pulse(true);
   }

   public void disable() {
      this.poller.pulse(false);
   }

   protected void reportEventOccurred(ReportEvent rE) {
      if (null != rE && null != rE.getData() && null != this.statusAccum) {
         if (this.isTracerOn()) {
            this.traceNormal("Printer reportEvent");
         }

         if (this.isTracerOn()) {
            this.traceMaximum("Status post-> " + Util.toFormatedHexString(rE.getData()) + " " + rE.getData());
         }

         if (this.getHandle().isDirectIOMode()) {
            ByteBuffer bb = ByteBuffer.getByteBufferFactory().createByteBuffer(rE.getData().length);
            this.getHandle().getEventHelper().fireDirectIOEvent(new DirectIOEvent(this, bb.append(rE.getData()).getBytes()));
         }

         if (this.parseUSBStatus(rE.getData())) {
            this.lastStatus = rE.getData();
         } else {
            this.statusAccum.post(this.lastStatus, false);
         }

         if (this.isTracerOn()) {
            this.traceNormal("Printer reportEvent exit");
         }
      }
   }

   protected void hidDeviceDisconnected(DisconnectEvent dE) {
      this.disable();
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

      super.hidDeviceDisconnected(dE);
      if (this.isHandleInit()) {
         ;
      }
   }

   protected byte getReportID() {
      return 50;
   }

   protected void submitResetCmd() throws HandleException {
   }

   protected boolean parseUSBStatus(byte[] eventBytes) {
      if (this.isTracerOn()) {
         this.traceNormal(">>parseUSBStatus-->" + Util.toFormatedHexString(eventBytes));
      }

      return eventBytes.length >= 2 ? eventBytes[0] != -1 && eventBytes[1] != -1 && eventBytes[0] != 0 && eventBytes[1] != 0 : false;
   }

   protected POSPrinterCmd getReinitCmds() {
      return (POSPrinterCmd)this.factory.createResetCmd();
   }

   protected void reinitialize() throws HandleException {
      if (this.initialized) {
         try {
            this.transport(this.initializeCmd, 8);
            synchronized (this.monitorStatusLock) {
               this.monitorStatusLock.wait(8000L);
            }
         } catch (Exception var5) {
            throw new HandleException("Could not reinit printer", var5);
         }
      }

      Iterator x = this.getSecondaryHandleImps();

      while (x.hasNext()) {
         PrinterSubDevices hi = (PrinterSubDevices)x.next();

         try {
            hi.onLine(null);
         } catch (Exception var6) {
            if (this.isTracerOn()) {
               this.getTracer().print(var6);
            }
         }
      }

      this.enable();
   }

   protected IBMPrinterImp getPrinterImp() {
      if (null == this.printerImp) {
         DefaultPOSPrinterHandle def = (DefaultPOSPrinterHandle)this.getHandle();
         this.printerImp = this.util.createPrinterImp(this.getWriter(), def);
         this.util.addDevice(this.printerImp.getHandleKey(), this.printerImp);
      }

      return this.printerImp;
   }

   protected void firePrintDataEvent(PrintDataEvent pde) {
      this.getPrinterImp().receivePrintDataEvent(pde);
      this.util.firePrintDataEvent(pde);
   }

   public class CoverClosePoller extends AbstractActiveObject {
      byte[] cmdBytes = new byte[]{0, 0, 32, 0, 0, 0, 0, 1};
      ByteBuffer cmd = new ByteBuffer(this.cmdBytes);
      private boolean send = false;
      private final long WAIT_STATUS_POLL = 3200L;

      public void pulse(boolean x) {
         this.send = x;
      }

      protected void runActiveObject() {
         while (true) {
            try {
               if (this.send) {
                  Hid4689PrinterHandleImp.this.transport(this.cmd, 8);
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
