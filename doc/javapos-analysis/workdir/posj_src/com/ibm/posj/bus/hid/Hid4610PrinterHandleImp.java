package com.ibm.posj.bus.hid;

import com.ibm.hid.DisconnectEvent;
import com.ibm.hid.HidDevice;
import com.ibm.hid.HidExceptionEvent;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.SleepPolicy;
import com.ibm.jutil.Util;
import com.ibm.jutil.tasks.AbstractActiveObject;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.bus.IBM4610PrinterHandleImp;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.bus.PrinterSubDevices;
import com.ibm.posj.bus.printer.IBM4610Utility;
import com.ibm.posj.bus.printer.cmds.ibm4610.Cmd4610;
import com.ibm.posj.bus.printer.cmds.ibm4610.Print4610CmdFactory;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.flash.FlashHandleImpVisitable;
import com.ibm.posj.flash.FlashPrinterHandleImpVisitor;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.IBMPrinterState;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.P4610Status;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.printer.ibm4610.IBM4610Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Hid4610PrinterHandleImp extends HidPOSPrinterHandleImp implements HidHandleImp, IBM4610PrinterHandleImp, FlashHandleImpVisitable {
   private Hid4610PrinterHandleImp.StatusDaemon sdaemon = new Hid4610PrinterHandleImp.StatusDaemon();
   private byte feature1 = 0;
   private byte feature2 = 0;
   private IBM4610Utility util = null;
   private boolean initialized = false;
   private IBM4610Writer writer;
   private IBMPrinterImp printerImp;
   private static IBM4610PrinterCmd.Factory factory = null;
   protected static final Integer CashDrawerA = new Integer(0);
   protected static final Integer CashDrawerB = new Integer(1);
   private static final boolean RAW_BYTE_SUBMIT = true;
   private static final int USB_STATUS_SIZE = 5;
   private static final byte ADAPTER_HEADER_SIZE = 7;
   private static final byte PRINTER_DATA_HEADER_SIZE = 3;
   private static final byte PRINTER_STATUS_BYTES_SIZE = 8;
   private static final int INIT_MAX_WAIT = 10000;
   private static final int PRINTER_DATA_MAX = 1015;
   private static final int PRINTER_DATA_ALT = 255;
   private static final int SHORT_CMD = 2;
   private static final int LONG_CMD = 1;
   private static final int REPORT_ID = 53;
   private static int MAX_HANG_CNT = 3;
   private boolean formerMod4 = false;
   private byte hangCnt = 0;
   private List currPDE = new ArrayList(2);
   PrinterPacket packet;
   public static byte[] transfer = new byte[]{1, 0, 0, 1, 0, 0, 0};

   public Hid4610PrinterHandleImp(HandleKey key, HidDevice hidDevice, byte printerType) {
      super(key, hidDevice);
      this.util = new IBM4610Utility(printerType, this.getTracer());
      factory = new IBM4610PrinterCmd.Factory(new Print4610CmdFactory(1015, new Cmd4610(), this.util.isLineFlag()));
      this.statusAccum.stop();
      this.statusAccum = null;
      this.sdaemon.start();
   }

   public boolean isStatusPending() {
      return !this.statusAccum.isEmpty();
   }

   public void accept(FlashPrinterHandleImpVisitor visitor) {
      visitor.visit4610PosPrinter(this);
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd instanceof SystemCmd.DirectWriteCmd) {
         ByteBuffer bb = this.packet.format();
         bb.append(cmd.toBytes());
         this.transport(bb, bb.getByteCount());
         bb.recycle();
      } else {
         super.submit(cmd);
      }
   }

   public List respondToFreeze() {
      this.hangCnt++;
      if (MAX_HANG_CNT <= this.hangCnt) {
         this.hidDeviceDisconnected(null);
         return null;
      } else {
         Cmd4610 cmd = (Cmd4610)factory.getPrintCmdFactory().getCmdBytes();

         try {
            try {
               ByteBuffer buff = this.packet.format();
               buff.append(cmd.STATUS_REQUEST);
               this.transport(buff, buff.getByteCount());
            } catch (Exception var6) {
            }

            return null;
         } finally {
            ;
         }
      }
   }

   public void setFormerMod4(boolean formerMod4) {
      this.formerMod4 = formerMod4;
   }

   public void processExtraData(PrintStatus status) {
      if (this.isTracerOn()) {
         this.traceNormal("GetExtra Data");
      }

      if (!this.currPDE.isEmpty()) {
         this.firePrintDataEvent((PrintDataEvent)this.currPDE.remove(0));
      } else if (this.isTracerOn()) {
         this.traceNormal("This should never happen!!! No data to remove in processExtraData");
      }
   }

   public HandleImp getMainHandleImp() {
      return this;
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHid4610PrinterHandleImp(this);
   }

   public boolean isComposite() {
      return true;
   }

   public boolean isCompositeParent() {
      return true;
   }

   public PrintStatus createPrintStatus(byte[] data) {
      int copy = data.length - 5;
      if (this.isTracerOn()) {
         this.traceMaximum("createPrintStatus bytes to copy-->" + copy);
      }

      System.arraycopy(data, 5, data, 0, copy);
      PrintStatus.PrintStatusFactory psf = P4610Status.getPrintStatusFactory(this);
      return psf.createPrintStatus(data, ((POSPrinterHandle)this.getHandle()).getPrinterLogHelper());
   }

   public PrintStatus createPrintStatus(byte[] data, boolean noLog) {
      int copy = data.length - 3;
      System.arraycopy(data, 3, data, 0, copy);
      PrintStatus.PrintStatusFactory psf = P4610Status.getPrintStatusFactory(this);
      return psf.createPrintStatus(data, null);
   }

   public synchronized PrinterWriter getWriter() {
      this.util.setTracer(this.getTracer());
      if (this.writer == null) {
         this.packet = new PrinterPacket(this, transfer, 1015);
         this.writer = (IBM4610Writer)this.util.createWriter(this, this.packet, (POSPrinterHandle)this.getHandle(), factory);
      }

      return this.writer;
   }

   public Iterator getSecondaryHandleImps() {
      return this.util.getSecondaryHandleImps();
   }

   public void transport(ByteBuffer bytes, int len) throws HandleException {
      if (len != 0) {
         byte[] transfer = bytes.getBytesRef();
         len -= 3;
         transfer[1] = (byte)len;
         transfer[2] = (byte)(len >> 8);
         transfer[0] = 2;
         if (len < 255) {
            super.transport(bytes, len + 3, false);
         } else {
            transfer[0] = 1;
            super.transport(bytes, len + 3, false);
         }
      }
   }

   protected PrintStatus doHidRead() throws HandleException {
      ByteBuffer bytes = ByteBuffer.getByteBufferFactory().createByteBuffer();
      if (this.isTracerOn()) {
         this.traceNormal("----------------Transport raw--------------------");
      }

      this.setAsyncMode(false);
      super.transport(bytes, 0, true);
      this.setAsyncMode(true);

      try {
         if (this.getHandle().isDirectIOMode()) {
            byte[] copy = this.getPrintStatusDataEvent().getBytes();
            copy[0] = -1;
            this.getHandle().getEventHelper().fireDirectIOEvent(new DirectIOEvent(this, copy));
         }
      } catch (Exception var3) {
         if (this.isTracerOn()) {
            this.getTracer().print(var3);
         }
      }

      if (!this.getHandle().isDirectIOMode()) {
         ByteBuffer hidReadData = this.getPrintStatusDataEvent();
         this.currPDE.add(this.createPrintDataEvent(hidReadData));
         return this.createPrintStatus(hidReadData.getBytes(), true);
      } else {
         return null;
      }
   }

   public synchronized void init() throws HandleException {
      if (!this.initialized) {
         this.initialized = true;
         super.init();
         this.util.init(this, this.imp, this.getReinitCmds(), 10000);
         ((IBMPrinterState)this.getPrinterHandleState()).setFormerMod4(this.formerMod4);
      }
   }

   public POSPrinterCmd.Factory getPrintCmdFactory() {
      return factory;
   }

   public PrintDataEvent createPrintDataEvent(ByteBuffer data) {
      if (this.isTracerOn()) {
         this.traceNormal("createPrintDataEvent - " + Util.toFormatedHexString(data.getBytesRef(), 0, data.getBytesRef().length));
      }

      ByteBuffer dataHead = ByteBuffer.getByteBufferFactory().createByteBuffer(3);
      dataHead.copy(data.getBytesRef(), 0, 0, 3);
      byte[] xcopy = dataHead.getBytesRef();
      int bytestoFollow = Util.toInt(xcopy[2], xcopy[1]);
      dataHead.recycle();
      ByteBuffer pStatus = ByteBuffer.getByteBufferFactory().createByteBuffer(8);
      pStatus.copy(data.getBytesRef(), 0, 0, 8);
      int cnt = 3;
      PrintStatus ps = this.createPrintStatus(pStatus.getBytesRef(), true);
      bytestoFollow -= 8;
      ByteBuffer xdata = ByteBuffer.getByteBufferFactory().createByteBuffer(bytestoFollow);
      cnt += 8;
      xdata.copy(data.getBytesRef(), cnt, 0, bytestoFollow);
      cnt = this.util.getPrinterDataType(ps);
      if (1 == cnt) {
         this.feature1 = xdata.getBytesRef()[2];
         this.feature2 = xdata.getBytesRef()[3];
      }

      if (this.isTracerOn()) {
         this.traceNormal(Util.toFormatedHexString(xdata.getBytesRef(), 0, xdata.getByteCount()));
      }

      ps.recycle();
      pStatus.recycle();
      return new PrintDataEvent(this, cnt, xdata);
   }

   public void distributeStatus(PrintStatus ps) {
      PrintStatus extra = null;
      if (ps.getData()[0] != 0 || ps.getData()[1] != 0 || ps.getData()[2] != 0 || ps.getData()[3] != 0) {
         if (this.isTracerOn()) {
            this.traceMaximum("distributeStatus ps.isExtendedData()-->" + ps.isExtendedData() + " " + ps);
         }

         if (ps.isExtendedData()) {
            try {
               extra = this.doHidRead();
               ps.recycle();
               ps = extra;
            } catch (Exception var4) {
               if (this.isTracerOn()) {
                  this.getTracer().print(var4);
               }
            }
         }

         this.util.subDeviceDistribute(this.getHandle(), ps);
      }
   }

   public void addDevice(HandleKey key, PrinterSubDevices handleImp) {
      this.util.addDevice(key, handleImp);
   }

   public void clearBuffers() throws HandleException {
      Cmd4610 cmd = (Cmd4610)factory.getPrintCmdFactory().getCmdBytes();
      ByteBuffer x = this.packet.format();
      x.append(cmd.CLEAR, 0, cmd.CLEAR.length);
      this.transport(x, x.getByteCount());
   }

   public byte getFeatureByte1() {
      return this.feature1;
   }

   public byte getFeatureByte2() {
      return this.feature2;
   }

   public void rawSubmit(byte[] data) throws HandleException {
      ByteBuffer x = this.packet.format();
      x.append(data, 0, data.length);
      this.transport(x, x.getByteCount());
   }

   protected byte getReportID() {
      return 53;
   }

   protected void submitResetCmd() throws HandleException {
   }

   protected boolean parseUSBStatus(byte[] eventBytes) {
      return eventBytes[1] == 112 ? false : eventBytes[1] != 114;
   }

   protected POSPrinterCmd getReinitCmds() {
      boolean t = !PosSystemManager.getInstance().getProperties().isPropertyDefined("usb.4610.EC");
      POSPrinterCmd mainCmd = factory.createEnableLineCntCmd(t);
      mainCmd.appendPOSPrinterCmd(factory.createResetLineCntCmd());
      mainCmd.appendPOSPrinterCmd(factory.createErrorRecoveryCmd(true, false, false, false));
      return mainCmd;
   }

   protected ByteBuffer getReinitRawCmds() {
      boolean t = PosSystemManager.getInstance().getProperties().isPropertyDefined("usb.4610.EC");
      byte[] RAW_REINIT = new byte[]{27, 41, 0, 27, 99, 52, 8, 27, 56, 0};
      if (t) {
         RAW_REINIT[RAW_REINIT.length - 1] = 1;
      }

      ByteBuffer b = ByteBuffer.getByteBufferFactory().createByteBuffer(RAW_REINIT.length + transfer.length);
      b.append(transfer);
      b.append(RAW_REINIT);
      return b;
   }

   protected void reinitialize() throws HandleException {
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

      if (this.initialized) {
         if (this.isTracerOn()) {
            this.traceNormal("Online handling");
         }

         this.setHandleOnline(true);
         ByteBuffer re = this.getReinitRawCmds();

         try {
            ByteBuffer reset = ByteBuffer.getByteBufferFactory().createByteBuffer();
            reset.append(transfer);
            reset.append((new Cmd4610()).REINIT);
            this.transport(reset, reset.getByteCount());
            SleepPolicy.sleep(2000L);
            reset.recycle();
            this.transport(re, re.getByteCount());
            SleepPolicy.sleep(1300L);
            re.recycle();
         } catch (Exception var4) {
            throw new HandleException("Could not reinit printer", var4);
         }
      }
   }

   protected void hidExceptionEventOccurred(HidExceptionEvent heE) {
      Iterator x = this.getSecondaryHandleImps();

      while (x.hasNext()) {
         PrinterSubDevices hi = (PrinterSubDevices)x.next();
         if (null != hi.getHandleKey()) {
            try {
               hi.busException(heE);
            } catch (Exception var5) {
               if (this.isTracerOn()) {
                  this.getTracer().print(var5);
               }
            }
         }
      }

      super.hidExceptionEventOccurred(heE);
   }

   protected void hidDeviceDisconnected(DisconnectEvent dE) {
      Iterator x = this.getSecondaryHandleImps();

      while (x.hasNext()) {
         PrinterSubDevices hi = (PrinterSubDevices)x.next();

         try {
            if (this.isTracerOn()) {
               this.traceNormal(">--set offline " + hi);
            }

            hi.offLine(dE);
            if (this.isTracerOn()) {
               this.traceNormal("<--set offline " + hi);
            }
         } catch (Exception var5) {
            if (this.isTracerOn()) {
               this.getTracer().print(var5);
            }
         }
      }

      super.hidDeviceDisconnected(dE);
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

   protected void reportEventOccurred(ReportEvent rE) {
      try {
         if (null == rE || null == rE.getData()) {
            return;
         }

         if (this.isTracerOn()) {
            this.traceNormal("Printer reportEvent " + Thread.currentThread().getName());
            this.traceMaximum("Status post-> " + Util.toFormatedHexString(rE.getData()) + " " + rE.getData());
         }

         if (null != this.getHandle() && this.getHandle().isDirectIOMode()) {
            ByteBuffer bb = ByteBuffer.getByteBufferFactory().createByteBuffer(rE.getData().length);
            this.getHandle().getEventHelper().fireDirectIOEvent(new DirectIOEvent(this, bb.append(rE.getData()).getBytes()));
         }

         if (this.parseUSBStatus(rE.getData())) {
            this.sdaemon.add(rE.getData());
         }

         if (this.isTracerOn()) {
            this.traceNormal("Printer reportEvent exit");
         }
      } catch (Exception var3) {
         this.getTracer().print(var3);
      }
   }

   public class StatusDaemon extends AbstractActiveObject {
      private List list = Collections.synchronizedList(new ArrayList(4));
      BooleanMonitor d = new BooleanMonitor(false);
      boolean flag = false;

      public boolean isEmpty() {
         return this.list.isEmpty();
      }

      public synchronized void add(byte[] a) {
         this.flag = this.isEmpty();
         this.list.add(a);
         if (this.flag) {
            this.d.set(true);
         }
      }

      protected void runActiveObject() {
         while (true) {
            if (this.isEmpty()) {
               try {
                  this.d.set(false);
                  this.d.waitForTrue(3250);
               } catch (Exception var4) {
               }
            }

            try {
               if (!this.isEmpty()) {
                  byte[] x = (byte[])this.list.remove(0);
                  PrintStatus p = Hid4610PrinterHandleImp.this.createPrintStatus(x);
                  Hid4610PrinterHandleImp.this.getTracer().println("Status Post <- " + p);
                  Hid4610PrinterHandleImp.this.distributeStatus(p);
               }
            } catch (Exception var3) {
               Hid4610PrinterHandleImp.this.getTracer().print(var3);
            }
         }
      }
   }
}
