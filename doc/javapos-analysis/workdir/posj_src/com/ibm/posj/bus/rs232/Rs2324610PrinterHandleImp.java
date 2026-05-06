package com.ibm.posj.bus.rs232;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.ByteArrayCollector;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.SleepPolicy;
import com.ibm.jutil.Util;
import com.ibm.jutil.tasks.AbstractActiveObject;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.Handle;
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
import com.ibm.posj.bus.printer.PrinterOfflineHandler;
import com.ibm.posj.bus.printer.cmds.ibm4610.Print4610CmdFactory;
import com.ibm.posj.bus.printer.cmds.ibm4610.Rs232Cmd4610;
import com.ibm.posj.bus.rs232.javaxcomm.Rs232PortCommAdapter;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
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
import com.ibm.rs232.Rs232Port;
import com.ibm.rs232.event.Rs232DataEvent;
import com.ibm.rs232.event.Rs232OfflineEvent;
import com.ibm.rs232.event.Rs232OnlineEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Rs2324610PrinterHandleImp
   extends Rs232POSPrinterHandleImp
   implements IBM4610PrinterHandleImp,
   FlashHandleImpVisitable,
   PrinterOfflineHandler.OfflineCallback {
   private byte feature1 = 0;
   private byte feature2 = 0;
   private Rs2324610PrinterHandleImp.OnlineThreader olThread = new Rs2324610PrinterHandleImp.OnlineThreader();
   protected PrinterOfflineHandler offlineHandler = new PrinterOfflineHandler(this);
   private BooleanMonitor offLineMonitor = new BooleanMonitor(false);
   boolean writePending = false;
   private FlashHandleImp flashHandle = null;
   private IBM4610Utility util = null;
   private byte hangCnt = 0;
   private boolean initialized = false;
   private long offlineTimer = 0L;
   protected IBM4610Imp prImp = null;
   private PrinterWriter writer;
   private static IBM4610PrinterCmd.Factory factory = null;
   List extraDataList = new ArrayList(2);
   private static final int INIT_MAX_WAIT = 10000;
   private static int PRINTER_DATA_MAX = 1500;
   private static int MAX_HANG_CNT = 3;
   public static byte[] transfer = new byte[0];

   public Rs2324610PrinterHandleImp(HandleKey key, Rs232Port rs232Port, IBM4610PrinterHandleImp.PrinterInfo printerType) {
      super(key, rs232Port);
      this.util = new IBM4610Utility(printerType.getType(), this.getTracer());
      factory = new IBM4610PrinterCmd.Factory(new Print4610CmdFactory(PRINTER_DATA_MAX, new Rs232Cmd4610(), this.util.isLineFlag()));
      this.flashHandle = new Rs232FlashPrinterHandleImp(key, rs232Port);
      ((Rs232PortCommAdapter)rs232Port).setReaderStrategy(new Rs232PrinterReader(rs232Port));
      this.olThread.start();
   }

   public short getECLevel() {
      return 0;
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

   protected POSPrinterCmd getReinitCmds() {
      POSPrinterCmd cmd = factory.createSolicitStatusCmd(true);
      cmd.appendPOSPrinterCmd(factory.createStatusSentCmd(true, true, true, true, true, true, true));
      cmd.appendPOSPrinterCmd(factory.createErrorRecoveryCmd(true, false, false, false));
      boolean t = !PosSystemManager.getInstance().getProperties().isPropertyDefined("rs232.4610.EC");
      cmd.appendPOSPrinterCmd(factory.createEnableLineCntCmd(t));
      cmd.appendPOSPrinterCmd(factory.createResetLineCntCmd());
      return cmd;
   }

   protected byte[] getReinitRawCmds() {
      boolean t = PosSystemManager.getInstance().getProperties().isPropertyDefined("rs232.4610.EC");
      byte[] RAW_REINIT = new byte[]{16, 5, 65, 27, 41, 0, 27, 99, 52, 8, 27, 56, 0};
      if (t) {
         RAW_REINIT[RAW_REINIT.length - 1] = 1;
      }

      return RAW_REINIT;
   }

   public synchronized PrinterWriter getWriter() {
      this.util.setTracer(this.getTracer());
      if (this.writer == null) {
         PrinterPacket packet = new PrinterPacket(this, transfer, PRINTER_DATA_MAX);
         this.writer = this.util.createWriter(this, packet, (POSPrinterHandle)this.getHandle(), factory);
      }

      return this.writer;
   }

   public POSPrinterCmd.Factory getPrintCmdFactory() {
      return factory;
   }

   public IBMPrinterImp getPrinterImp() {
      if (null == this.prImp) {
         DefaultPOSPrinterHandle def = (DefaultPOSPrinterHandle)this.getHandle();
         this.prImp = (IBM4610Imp)this.util.createPrinterImp(this.getWriter(), def);
         this.addDevice(this.prImp.getHandleKey(), this.prImp);
      }

      return this.prImp;
   }

   public void processExtraData(PrintStatus ps) {
      this.firePrintDataEvent(this.createPrintDataEvent());
   }

   public List respondToFreeze() {
      this.hangCnt++;
      if (MAX_HANG_CNT <= this.hangCnt) {
         this.deviceOffline();
         return null;
      } else {
         Rs232Cmd4610 cmd = (Rs232Cmd4610)factory.getPrintCmdFactory().getCmdBytes();

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

   public PrintStatus createPrintStatus(byte[] data) {
      this.offLineMonitor.set(false);
      if (!this.getHandle().getState().isOnline()) {
         this.setOnline();
      }

      PrintStatus.PrintStatusFactory psf = P4610Status.getPrintStatusFactory(this);
      PrintStatus ps = psf.createPrintStatus(data, ((POSPrinterHandle)this.getHandle()).getPrinterLogHelper());
      if (data.length > 8) {
         Rs2324610PrinterHandleImp.ExtraData ed = new Rs2324610PrinterHandleImp.ExtraData();
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

   public PrintDataEvent createPrintDataEvent() {
      Rs2324610PrinterHandleImp.ExtraData ed = (Rs2324610PrinterHandleImp.ExtraData)this.extraDataList.remove(0);
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

   public FlashHandleImp getFlashHandleImp() throws FlashException {
      return this.flashHandle;
   }

   public void flash(FlashRequest flashRequest) throws FlashException {
      try {
         this.flashHandle.flash(flashRequest);
      } catch (FlashException var3) {
         throw var3;
      }
   }

   public void clearBuffers() throws HandleException {
      Rs232Cmd4610 cmd = (Rs232Cmd4610)factory.getPrintCmdFactory().getCmdBytes();

      try {
         try {
            this.writeToBus(cmd.CLEAR, 0, cmd.CLEAR.length);
         } catch (Exception var6) {
         }
      } finally {
         ;
      }
   }

   public void statusCheck() {
      Rs232Cmd4610 cmd = (Rs232Cmd4610)factory.getPrintCmdFactory().getCmdBytes();

      try {
         try {
            this.writeToBus(cmd.STATUS_REQUEST, 0, cmd.STATUS_REQUEST.length, false);
         } catch (Exception var6) {
         }
      } finally {
         ;
      }
   }

   public void setOffline() {
      if (this.isTracerOn()) {
         this.traceNormal("-->setOffline");
      }

      if (!this.printerReader.isReceiving()) {
         this.deviceOffline();
      }

      if (this.isTracerOn()) {
         this.traceNormal("<--setOffline");
      }
   }

   public void setOnline() {
      if (!this.getHandle().isOnline()) {
         this.onlineEventOccurred(null);
      }
   }

   public byte getFeatureByte1() {
      return this.feature1;
   }

   public byte getFeatureByte2() {
      return this.feature2;
   }

   public synchronized void writeToBus(byte[] data, int offset, int len) throws HandleException {
      this.writeToBus(data, offset, len, true);
   }

   public synchronized void writeToBus(byte[] data, int offset, int len, boolean extra) throws HandleException {
      this.writePending = true;

      try {
         if (extra) {
            this.offlineHandler.setEstimatedTimeout(STATUS_CHECK_TIMEOUT + len + EXTRA_TIMEOUT);
         } else {
            this.offlineHandler.setEstimatedTimeout(STATUS_CHECK_TIMEOUT);
         }

         this.offlineHandler.pulseSend();
         super.submit(data, offset, len);
      } catch (HandleException var9) {
         throw var9;
      } finally {
         this.writePending = false;
      }
   }

   public void rawSubmit(byte[] data) throws HandleException {
      this.writeToBus(data, 0, data.length);
   }

   protected void dataEventOccurred(Rs232DataEvent event) {
      byte[] data = event.getData();
      if (null != this.getHandle() && this.getHandle().isDirectIOMode()) {
         this.getHandle().getEventHelper().fireDirectIOEvent(new DirectIOEvent(this, data));
      }

      this.offlineHandler.receiveThread();
      if (this.isTracerOn()) {
         this.traceNormal("Status post-> " + Util.toFormatedHexString(data) + " " + data);
      }

      this.statusAccum.post(data, false);
   }

   protected void onlineEventOccurred(Rs232OnlineEvent rs232OEvent) {
      this.olThread.pulse();
   }

   protected void onlineHandler() {
      if (this.isTracerOn()) {
         this.traceNormal(">>>>>onlineEventOccurred---");
      }

      if (!this.getHandle().isOnline()) {
         SleepPolicy.sleep(2000L);
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

         try {
            this.writeToBus(this.getReinitRawCmds(), 0, this.getReinitRawCmds().length);
            this.offLineMonitor.waitForFalse(7000);
            this.getHandle().getState().setOnline(true);
         } catch (Exception var5) {
            if (this.isTracerOn()) {
               this.getTracer().print(var5);
            }

            this.getHandle().getState().setOnline(false);
         }

         try {
            if (this.getHandle().getState().isOnline()) {
               Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
               eventHelper.fireOnlineEvent(new OnlineEvent(this, System.currentTimeMillis()));
               this.getLogHelper().addLogEntry(2005, this + " offline.", "AllDevices", 2);
            }
         } catch (NullPointerException var4) {
         }
      }
   }

   protected void offlineEventOccurred(Rs232OfflineEvent rs232OEvent) {
      if (System.currentTimeMillis() - this.offlineTimer < 1250L) {
         this.offlineTimer = System.currentTimeMillis();
      } else {
         this.offlineTimer = System.currentTimeMillis();
         if (!this.writePending) {
            this.testOnline();
         }
      }
   }

   protected void deviceOffline() {
      this.offLineMonitor.set(true);
      Object dE = null;
      Iterator x = this.getSecondaryHandleImps();

      while (x.hasNext()) {
         PrinterSubDevices hi = (PrinterSubDevices)x.next();

         try {
            if (this.isTracerOn()) {
               this.traceNormal("set offline -> " + hi);
            }

            hi.offLine(dE);
         } catch (Exception var6) {
            if (this.isTracerOn()) {
               this.getTracer().print(var6);
            }
         }
      }

      try {
         this.getHandle().getState().setOnline(false);
         Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
         eventHelper.fireOfflineEvent(new OfflineEvent(this, System.currentTimeMillis()));
         this.getLogHelper().addLogEntry(2006, this + " offline.", "AllDevices", 2);
      } catch (NullPointerException var5) {
      }
   }

   protected void firePrintDataEvent(PrintDataEvent pde) {
      this.getPrinterImp().receivePrintDataEvent(pde);
      this.util.firePrintDataEvent(pde);
   }

   public void distributeStatus(PrintStatus ps) {
      this.util.subDeviceDistribute(this.getHandle(), ps);
   }

   public void addDevice(HandleKey key, PrinterSubDevices handleImp) {
      this.util.addDevice(key, handleImp);
   }

   public HandleImp getMainHandleImp() {
      return this;
   }

   public Iterator getSecondaryHandleImps() {
      return this.util.getSecondaryHandleImps();
   }

   private void testOnline() {
      Rs232Cmd4610 cmd = (Rs232Cmd4610)factory.getPrintCmdFactory().getCmdBytes();

      try {
         this.writeToBus(cmd.STATUS_REQUEST, 0, cmd.STATUS_REQUEST.length);
      } catch (Exception var3) {
         if (this.isTracerOn()) {
            this.getTracer().print(var3);
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

   protected class OnlineThreader extends AbstractActiveObject {
      BooleanMonitor poll = new BooleanMonitor(false);

      public void pulse() {
         this.poll.set(true);
      }

      protected void runActiveObject() {
         while (true) {
            this.poll.waitForTrue(-1);
            Rs2324610PrinterHandleImp.this.onlineHandler();
            this.poll.set(false);
         }
      }
   }
}
