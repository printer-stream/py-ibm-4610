package com.ibm.posj.bus.rs232;

import com.ibm.jutil.ByteBuffer;
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
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.PrinterTimeStamper;
import com.ibm.rs232.Rs232Port;
import com.ibm.rs232.event.Rs232DataEvent;

public abstract class Rs232POSPrinterHandleImp
   extends AbstractRs232HandleImp
   implements POSPrinterHandleImp,
   PrinterPacket.Transport,
   PrinterBusWriter,
   StatusDistributor.Distributee {
   protected static int EXTRA_TIMEOUT = 10000;
   protected static int STATUS_CHECK_TIMEOUT = 4500;
   protected StatusDistributor distributor = null;
   protected SubmitTaskScheduler statusAccum = new SubmitTaskScheduler();
   protected Rs232PrinterReader printerReader = null;
   public int printerID_microcodeLevel = -1;

   public Rs232POSPrinterHandleImp(HandleKey key, Rs232Port rs232Port) {
      super(key, rs232Port);
      this.distributor = new StatusDistributor(this, null);
      this.statusAccum.setSubmitter(this.distributor);
      this.printerReader = new Rs232PrinterReader(this.getRs232Port());
   }

   public String getDeviceSerialNumber() {
      return this.getPrinterImp().getSerialNumber();
   }

   public String getSerialNumber() {
      return this.getPrinterImp().getSerialNumber();
   }

   public PrinterHandleState getPrinterHandleState() {
      return this.getPrinterImp().getPrinterHandleState();
   }

   public PrinterTimeStamper timeStamp() {
      return ((POSPrinterHandle)this.getHandle()).timeStamp();
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

   public boolean isFlashable() {
      return true;
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else if (cmd instanceof SystemCmd.DirectWriteCmd) {
         ByteBuffer bb = ByteBuffer.getByteBufferFactory().createByteBuffer(cmd.toBytes().length);
         bb.append(cmd.toBytes());
         this.transport(bb, bb.getByteCount());
         bb.recycle();
      } else {
         try {
            this.getPrinterImp().submit(cmd);
         } catch (Exception var3) {
            if (this.isTracerOn()) {
               this.getTracer().print(var3);
            }

            throw new HandleException("GPF???");
         }
      }
   }

   public void transport(ByteBuffer data, int len) throws HandleException {
      if (0 < len) {
         if (this.getTracer().isOn()) {
            this.timeStamp().sendToLowerLayer(data);
         }

         this.writeToBus(data.getBytesRef(), 0, len);
      }
   }

   public synchronized void writeToBus(byte[] data, int offset, int len) throws HandleException {
   }

   public void clearOutput() {
      this.getPrinterImp().clearOutput();
   }

   public boolean isStatusPending() {
      return !this.statusAccum.isEmpty();
   }

   public abstract PrintStatus createPrintStatus(byte[] var1);

   public abstract void distributeStatus(PrintStatus var1);

   public int getPrinterID_microcodeLevel() {
      return this.printerID_microcodeLevel;
   }

   public void setPrinterID_microcodeLevel(int printer_ID_microcodeLevel) {
      this.printerID_microcodeLevel = printer_ID_microcodeLevel;
   }

   public boolean isDataPending() {
      return this.getPrinterImp().isDataPending();
   }

   public void enable() {
   }

   public void disable() {
   }

   public void rawSubmit(byte[] data) throws HandleException {
   }

   protected abstract IBMPrinterImp getPrinterImp();

   protected void firePrintDataEvent(PrintDataEvent pde) {
      this.getPrinterImp().receivePrintDataEvent(pde);
   }

   protected void dataEventOccurred(Rs232DataEvent event) {
   }
}
