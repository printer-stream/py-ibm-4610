package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jsio.event.SioDeviceDataEvent;
import com.ibm.jutil.ByteArrayCollector;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Util;
import com.ibm.jutil.tasks.SubmitTaskScheduler;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.POSPrinterHandleImp;
import com.ibm.posj.bus.PrinterBusWriter;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.bus.printer.StatusDistributor;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.PrinterTimeStamper;

public abstract class Rs485POSPrinterHandleImp
   extends AbstractRs485HandleImp
   implements POSPrinterHandleImp,
   PrinterPacket.Transport,
   PrinterBusWriter,
   StatusDistributor.Distributee {
   protected boolean isReset = false;
   long timestamp = 0L;
   protected SubmitTaskScheduler statusAccum = new SubmitTaskScheduler();
   public int printerID_microcodeLevel = -1;

   public Rs485POSPrinterHandleImp(HandleKey key, SioDevice device) {
      super(key, device);
      this.statusAccum.setSubmitter(new StatusDistributor(this, null));
   }

   public String getDeviceSerialNumber() {
      return this.getPrinterImp().getSerialNumber();
   }

   public String getSerialNumber() {
      return this.getPrinterImp().getSerialNumber();
   }

   public PrinterTimeStamper timeStamp() {
      return ((POSPrinterHandle)this.getHandle()).timeStamp();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd instanceof SystemCmd.DirectWriteCmd) {
         ByteBuffer bb = ByteBuffer.getByteBufferFactory().createByteBuffer(cmd.toBytes().length);
         bb.append(cmd.toBytes());
         this.transport(bb, bb.getByteCount());
         bb.recycle();
      } else {
         this.getPrinterImp().submit(cmd);
      }
   }

   public PrinterHandleState getPrinterHandleState() {
      return this.getPrinterImp().getPrinterHandleState();
   }

   public void init() throws HandleException {
      super.init();
      this.getWriter().setMainImp(this.getPrinterImp());
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitPOSPrinter(this);
   }

   public DevCat getDevCat() {
      return DevCats.POSPRINTER_DEVCAT;
   }

   public void clearOutput() {
      this.getPrinterImp().clearOutput();
   }

   public boolean isStatusPending() {
      return !this.statusAccum.isEmpty();
   }

   public int getPrinterID_microcodeLevel() {
      return this.printerID_microcodeLevel;
   }

   public void setPrinterID_microcodeLevel(int printer_ID_microcodeLevel) {
      this.printerID_microcodeLevel = printer_ID_microcodeLevel;
   }

   public boolean isDataPending() {
      return this.getPrinterImp().isDataPending();
   }

   public boolean isFlashable() {
      return true;
   }

   public void transport(ByteBuffer data, int len) throws HandleException {
      this.isReset = false;
      if (0 < len) {
         if (this.getTracer().isOn()) {
            this.timeStamp().sendToLowerLayer(data);
         }

         this.writeToBus(data.getBytesRef(), 0, len);
      }
   }

   public void reset(byte[] data, int offset, int len) throws HandleException {
      this.isReset = true;
      this.writeToBus(data, 0, len);
   }

   public void enable() {
   }

   public void disable() {
   }

   public void rawSubmit(byte[] data) throws HandleException {
   }

   protected abstract void deviceOffline();

   protected void writeToBus(byte[] data, int offset, int len) throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal(Util.toFormatedHexString(data, offset, len));
         this.traceNormal(">>Sync Submit");
         this.timestamp = System.currentTimeMillis();
      }

      super.submitSync(data, offset, len);
      if (this.isTracerOn()) {
         this.traceNormal("<<Sync Submit " + (System.currentTimeMillis() - this.timestamp));
      }
   }

   protected abstract IBMPrinterImp getPrinterImp();

   protected void dataEventOccurred(SioDeviceDataEvent event) {
      byte[] data = event.getData();
      if (this.isTracerOn()) {
         this.traceNormal("Status post-> " + Util.toFormatedHexString(data) + " " + data);
      }

      if (null != this.getHandle() && this.getHandle().isDirectIOMode()) {
         byte[] ldata = ByteArrayCollector.getCollector().getArray(data.length);
         System.arraycopy(data, 0, ldata, 0, data.length);
         this.getHandle().getEventHelper().fireDirectIOEvent(new DirectIOEvent(this, ldata));
      }

      this.statusAccum.post(data, false);
   }

   protected void handleOffline() {
      if (!this.isReset) {
         if (this.getHandle().getState().isOnline()) {
            super.handleOffline();
            this.deviceOffline();
         }
      }
   }

   protected void reinitialize() throws HandleException {
   }
}
