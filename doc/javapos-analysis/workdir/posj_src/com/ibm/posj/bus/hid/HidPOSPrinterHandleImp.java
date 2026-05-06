package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.HidException;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Util;
import com.ibm.jutil.tasks.SubmitTaskScheduler;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.POSPrinterHandleImp;
import com.ibm.posj.bus.PrinterBusWriter;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.bus.printer.StatusDistributor;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.PrinterTimeStamper;
import java.util.List;

public abstract class HidPOSPrinterHandleImp
   extends AbstractHidHandleImp
   implements HidHandleImp,
   POSPrinterHandleImp,
   PrinterPacket.Transport,
   PrinterBusWriter,
   StatusDistributor.Distributee {
   protected SubmitTaskScheduler statusAccum = new SubmitTaskScheduler();
   private boolean async = true;
   protected IBMPrinterImp imp;
   private ByteBuffer printStatusDataEvent = null;
   public static int ENDPOINT_ADDRESS = 130;
   public static final int BUS_MAX = 1022;
   public static final byte USB_BUS_HEADER_SIZE = 8;
   public int printerID_microcodeLevel = -1;

   public HidPOSPrinterHandleImp(HandleKey key, HidDevice hidDevice) {
      super(key, hidDevice);
      this.statusAccum.setSubmitter(new StatusDistributor(this, null));
   }

   public String getDeviceSerialNumber() {
      return this.getPrinterImp().getSerialNumber();
   }

   public List respondToFreeze() {
      return null;
   }

   public int getPrinterID_microcodeLevel() {
      return this.printerID_microcodeLevel;
   }

   public void setPrinterID_microcodeLevel(int printer_ID_microcodeLevel) {
      this.printerID_microcodeLevel = printer_ID_microcodeLevel;
   }

   public abstract void processExtraData(PrintStatus var1);

   public abstract PrintStatus createPrintStatus(byte[] var1);

   public abstract PrintDataEvent createPrintDataEvent(ByteBuffer var1);

   public abstract POSPrinterCmd.Factory getPrintCmdFactory();

   protected abstract IBMPrinterImp getPrinterImp();

   public PrinterTimeStamper timeStamp() {
      return ((POSPrinterHandle)this.getHandle()).timeStamp();
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidPOSPrinterHandleImp(this);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitPOSPrinter(this);
   }

   public DevCat getDevCat() {
      return DevCats.POSPRINTER_DEVCAT;
   }

   public PrinterHandleState getPrinterHandleState() {
      return this.imp.getPrinterHandleState();
   }

   public boolean isDataPending() {
      return this.getPrinterImp().isDataPending();
   }

   public void init() throws HandleException {
      super.init();
      this.imp = this.getPrinterImp();
      this.getWriter().setMainImp(this.imp);
      if (this.isTracerOn()) {
         this.traceNormal("lower init done");
      }
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            this.imp.submit(cmd);
         } catch (Exception var3) {
            if (this.isTracerOn()) {
               this.getTracer().print(var3);
            }

            throw new HandleException("GPF???");
         }
      }
   }

   public void transport(ByteBuffer tlast, int len, boolean isRaw) throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("In HidPOSPrinterHandleImp tansport len " + len);
      }

      try {
         if (isRaw) {
            if (this.isTracerOn()) {
               this.traceNormal("Doing a get Report");
               this.getTracer().print("|-->Raw HidSubmit " + Thread.currentThread().getName() + " time " + System.currentTimeMillis());
            }

            byte[] data = this.getReport(len, "Could not submit directly to POS Printer");
            if (this.isTracerOn()) {
               this.getTracer().print("<--|Raw HidSubmit " + Thread.currentThread().getName() + " time " + System.currentTimeMillis());
               this.traceNormal("Data back = " + Util.toFormatedHexString(data));
            }

            this.printStatusDataEvent = ByteBuffer.getByteBufferFactory().createByteBuffer(data.length);
            this.printStatusDataEvent.append(data);
         } else {
            if (this.isTracerOn()) {
               this.traceNormal(" to HW -> " + Util.toFormatedHexString(tlast.getBytesRef(), 0, len));
            }

            this.setReport(tlast.getBytesRef(), len, "Could not submit to POS Printer");
         }
      } catch (Exception var5) {
         if (this.isTracerOn()) {
            this.getTracer().print(var5);
         }

         throw new HandleException("Error while sending the command to POS Printer", var5);
      }
   }

   public boolean isComposite() {
      return true;
   }

   public boolean isCompositeParent() {
      return true;
   }

   public void clearOutput() {
      this.imp.clearOutput();
   }

   public boolean isStatusPending() {
      return !this.statusAccum.isEmpty();
   }

   public abstract void distributeStatus(PrintStatus var1);

   public void enable() {
   }

   public void disable() {
   }

   public void rawSubmit(byte[] data) throws HandleException {
   }

   protected abstract byte getReportID();

   protected void reportEventOccurred(ReportEvent rE) {
      try {
         if (null == rE || null == rE.getData() || null == this.statusAccum) {
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
            this.statusAccum.post(rE.getData(), false);
         }

         if (this.isTracerOn()) {
            this.traceNormal("Printer reportEvent exit");
         }
      } catch (Exception var3) {
         this.getTracer().print(var3);
      }
   }

   protected ByteBuffer getPrintStatusDataEvent() {
      return this.printStatusDataEvent;
   }

   protected abstract boolean parseUSBStatus(byte[] var1);

   protected void setAsyncMode(boolean async) {
      this.async = async;
   }

   protected boolean asyncMode() {
      return this.async;
   }

   protected void firePrintDataEvent(PrintDataEvent pde) {
      this.imp.receivePrintDataEvent(pde);
   }

   public void setReport(byte[] data, int len, String errorMsg) throws HandleException {
      if (this.isTracerOn()) {
         this.getTracer().print("|--> HidSubmit " + Thread.currentThread().getName() + " time " + System.currentTimeMillis());
      }

      try {
         this.getHidDevice().setReport((byte)2, (byte)0, (short)len, data);
         if (this.isTracerOn()) {
            this.traceNormal("<--| HidSubmit " + Thread.currentThread().getName() + " time " + System.currentTimeMillis());
         }
      } catch (HidException var5) {
         throw new HandleException(errorMsg, var5);
      }
   }

   public byte[] getReport(int len, String errorMsg) throws HandleException {
      try {
         return this.getHidDevice().getReport((byte)3, this.getReportID());
      } catch (HidException var4) {
         throw new HandleException("Error receiving Printer Info", var4);
      }
   }
}
