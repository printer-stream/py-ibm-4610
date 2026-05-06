package com.ibm.posj.bus.rs232;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Util;
import com.ibm.jutil.tasks.AbstractActiveObject;
import com.ibm.jutil.tasks.SharedThreader;
import com.ibm.jutil.tasks.SharedThreader.ActionObject;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.IBMSureonePrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.bus.POSPrinterHandleImp;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.bus.printer.IBMSureoneUtility;
import com.ibm.posj.bus.printer.SureoneOfflineHandler;
import com.ibm.posj.bus.printer.cmds.sureone.CmdSureone;
import com.ibm.posj.bus.printer.cmds.sureone.PrintSureoneCmdFactory;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.IBMSureoneStatus;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.rs232.Rs232Port;
import com.ibm.rs232.event.Rs232DataEvent;
import java.util.Iterator;
import java.util.List;

public class Rs232SureonePrinterHandleImp extends Rs232POSPrinterHandleImp implements POSPrinterHandleImp, SureoneOfflineHandler.OfflineCallback {
   private final int PRINTER_DATA_MAX = 1607;
   CmdSureone cmdArrays = new CmdSureone();
   private final IBMSureonePrinterCmd.Factory factory = new IBMSureonePrinterCmd.Factory(new PrintSureoneCmdFactory(1607, this.cmdArrays));
   protected IBMPrinterImp prImp = null;
   protected IBMSureoneUtility util = null;
   private PrinterWriter writer;
   private Rs232SureonePrinterHandleImp.StatusPoller poller = new Rs232SureonePrinterHandleImp.StatusPoller(this.cmdArrays);
   private final byte ST_COVER_OPEN = 4;
   private Rs232SureonePrinterHandleImp.ThreadedTransport ttransport = new Rs232SureonePrinterHandleImp.ThreadedTransport();

   public Rs232SureonePrinterHandleImp(HandleKey key, Rs232Port rs232Port) {
      super(key, rs232Port);
      this.util = new IBMSureoneUtility();
      this.distributor.setMinimumLength(1);
      STATUS_CHECK_TIMEOUT = 1200;
      this.poller.start();
   }

   public void init() throws HandleException {
      super.init();
      this.util.init(this, this.getPrinterImp(), null, 4500);
   }

   public boolean isFlashable() {
      return false;
   }

   protected void deviceOffline() {
      byte[] data = new byte[]{4};
      if (this.isTracerOn()) {
         this.traceNormal("------DEO--------" + Util.toFormatedHexString(data) + " " + data);
      }

      this.statusAccum.post(data, false);
   }

   public void transport(ByteBuffer data, int len) throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("-->transport");
      }

      this.poller.queueOutput(data, len);
      this.poller.pulse();
   }

   public void realTransport(ByteBuffer data, int len) throws HandleException {
      try {
         super.transport(data, len);
      } catch (Exception var4) {
         this.getTracer().print(var4);
      }
   }

   protected IBMPrinterImp getPrinterImp() {
      if (null == this.prImp) {
         DefaultPOSPrinterHandle def = (DefaultPOSPrinterHandle)this.getHandle();
         this.prImp = this.util.createPrinterImp(this.getWriter(), def);
         this.util.addDevice(this.prImp.getHandleKey(), this.prImp);
      }

      return this.prImp;
   }

   public PrinterWriter getWriter() {
      this.util.setTracer(this.getTracer());
      if (this.writer == null) {
         byte[] transfer = new byte[0];
         PrinterPacket packet = new PrinterPacket(this, transfer, 1607);
         this.writer = this.util.createWriter(this, packet, (POSPrinterHandle)this.getHandle(), this.factory);
      }

      return this.writer;
   }

   public Iterator getSecondaryHandleImps() {
      return this.util.getSecondaryHandleImps();
   }

   public void processExtraData(PrintStatus status) {
   }

   public List respondToFreeze() {
      return null;
   }

   public void clearBuffers() throws HandleException {
      try {
         try {
            if (this.isTracerOn()) {
               this.traceNormal("-->clearBuffers()");
            }
         } catch (Exception var5) {
         }
      } finally {
         ;
      }
   }

   public PrintStatus createPrintStatus(byte[] data) {
      if (!this.getHandle().getState().isOnline()) {
         this.setOnline();
      }

      if (this.isTracerOn()) {
         this.traceNormal("-->createPrintStatus");
      }

      PrintStatus.PrintStatusFactory psf = IBMSureoneStatus.getPrintStatusFactory(this);
      PrintStatus ps = psf.createPrintStatus(data, ((POSPrinterHandle)this.getHandle()).getPrinterLogHelper());
      if (this.isTracerOn()) {
         this.traceNormal("<--createPrintStatusEvent " + Util.toFormatedHexString(data));
      }

      if (ps.checkCmdComplete() || ps.checkErrors()) {
         this.poller.pause();
      }

      return ps;
   }

   public void distributeStatus(PrintStatus ps) {
      this.util.subDeviceDistribute(this.getHandle(), ps);
   }

   public POSPrinterCmd.Factory getPrintCmdFactory() {
      return this.factory;
   }

   public short getECLevel() {
      return 0;
   }

   public void statusCheck() {
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
   }

   public synchronized void writeToBus(byte[] data, int offset, int len) throws HandleException {
      super.submit(data, offset, len);
   }

   protected void dataEventOccurred(Rs232DataEvent event) {
      this.poller.online();
      byte[] data = event.getData();
      if (this.isTracerOn()) {
         this.traceNormal("------DEO--------" + Util.toFormatedHexString(data) + " " + data);
      }

      this.statusAccum.post(data, false);
   }

   public class StatusPoller extends AbstractActiveObject {
      private BooleanMonitor offlineMonitor = new BooleanMonitor(false);
      private boolean isPolling = false;
      private BooleanMonitor poll = new BooleanMonitor(true);
      private CmdSureone csmd;
      private final int WAIT_CMD_COMPLETE = 3750;
      private final int WAIT_STATUS_POLL = 1000;
      private ByteBuffer xdata = null;
      private int xlen = 0;

      public StatusPoller(CmdSureone x) {
         this.csmd = x;
      }

      public void online() {
         this.offlineMonitor.set(false);
      }

      public void pulse() {
         this.poll.set(true);
      }

      public void pause() {
         this.poll.set(false);
      }

      public void queueOutput(ByteBuffer data, int len) {
         this.xdata = data;
         this.xlen = len;
         if (this.xdata.getBytesRef()[0] != 5) {
            this.xdata.append(this.csmd.STATUS_REQUEST);
            this.xlen = this.xlen + this.csmd.STATUS_REQUEST.length;
         }
      }

      public void clearQueue() {
         this.xdata = null;
         this.xlen = 0;
      }

      public boolean isPolling() {
         return this.isPolling;
      }

      protected void runActiveObject() {
         while (true) {
            try {
               if (this.offlineMonitor.isFalse()) {
                  this.offlineMonitor.set(true);
               }

               if (this.xdata != null && !this.offlineMonitor.isNeutral()) {
                  this.isPolling = false;
                  this.pause();
                  SharedThreader.getInstance().setActionObject(Rs232SureonePrinterHandleImp.this.ttransport.setData(this.xdata, this.xlen));
                  this.clearQueue();
                  if (this.testOffline(3750)) {
                     continue;
                  }
               } else {
                  this.isPolling = true;
                  if (this.offlineMonitor.isNeutral()) {
                     Rs232SureonePrinterHandleImp.this.writeToBus(this.csmd.STATUS_REQUEST, 0, this.csmd.STATUS_REQUEST.length);
                  } else {
                     SharedThreader.getInstance()
                        .setActionObject(Rs232SureonePrinterHandleImp.this.ttransport.setData(this.csmd.STATUS_REQUEST, this.csmd.STATUS_REQUEST.length));
                  }

                  if (this.testOffline(1000)) {
                     continue;
                  }
               }

               try {
                  this.poll.waitForTrue(5000);
               } catch (Exception var2) {
               }
            } catch (Exception var3) {
            }
         }
      }

      private boolean testOffline(int timeout) {
         try {
            this.offlineMonitor.waitForFalse(timeout);
            return false;
         } catch (Exception var3) {
            Rs232SureonePrinterHandleImp.this.traceNormal("<-offline bad");
            this.offlineMonitor.set(true);
            this.offlineMonitor.setNeutral();
            if (Rs232SureonePrinterHandleImp.this.isTracerOn()) {
               Rs232SureonePrinterHandleImp.this.getTracer().print(var3);
            }

            Rs232SureonePrinterHandleImp.this.setOffline();
            return true;
         }
      }
   }

   protected class ThreadedTransport implements ActionObject {
      int len;
      ByteBuffer data;
      boolean recycle = false;

      public Rs232SureonePrinterHandleImp.ThreadedTransport setData(ByteBuffer b, int len) {
         this.len = len;
         this.data = b;
         this.recycle = false;
         return this;
      }

      public Rs232SureonePrinterHandleImp.ThreadedTransport setData(byte[] b, int len) {
         this.len = len;
         this.data = ByteBuffer.getByteBufferFactory().createByteBuffer(len);
         this.data.append(b);
         this.recycle = true;
         return this;
      }

      public void execute() {
         try {
            Rs232SureonePrinterHandleImp.this.realTransport(this.data, this.len);
         } catch (HandleException var2) {
            if (Rs232SureonePrinterHandleImp.this.isTracerOn()) {
               Rs232SureonePrinterHandleImp.this.getTracer().print(var2);
            }
         }

         this.len = 0;
         if (this.recycle) {
            this.data.recycle();
         }
      }
   }
}
