package com.ibm.posj.printer;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.PosSystem;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.bus.PrinterSubDevices;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.printer.event.PrintErrorEvent;
import com.ibm.posj.printer.event.PrintStatus;

public abstract class PrinterWriter implements PrinterSubDevices {
   private boolean ready = true;
   private static Tracer printerTracer = TracerFactory.getInstance().createTracer("posjPrinter");
   public static final String ERROR_TIMEOUT = "posj.printer.error.timeout";
   public static int NONRESPONDING_TIMEOUT = 2000;

   protected PrinterWriter() {
      PosSystem.Properties properties = PosSystemManager.getInstance().getProperties();
      if (properties.isPropertyDefined("posj.printer.error.timeout")) {
         NONRESPONDING_TIMEOUT = Integer.valueOf(properties.getPropertyString("posj.printer.error.timeout"));
      }

      getTracer().println("Nonresponding timeout = " + NONRESPONDING_TIMEOUT);
   }

   public static void print(String a) {
      if (getTracer().isOn()) {
         getTracer().println(a);
      }
   }

   public static Tracer getTracer() {
      return printerTracer;
   }

   public boolean isOnline() {
      return this.ready;
   }

   public abstract PrintStatus getCurrentStatus();

   public abstract ByteBuffer getFormattedBuffer();

   public abstract void setMainImp(PrinterWriter.PrinterWriterUser var1);

   public abstract int getMaxCmdLen();

   public void registerDeviceQueue(RetryInputQueue diq) {
      diq.setInputQueue(this.getInputQ());
      this.getInputQ().addDeviceQueue(diq);
   }

   public void deregisterDeviceQueue(RetryInputQueue diq) {
      this.getInputQ().removeDeviceQueue(diq);
   }

   public abstract POSPrinterCmd getNextProcessCmd();

   protected abstract boolean getErrorPending();

   protected abstract InputQueue getInputQ();

   protected abstract boolean responsePending();

   protected void setOffline(boolean off) {
      this.ready = !off;
   }

   public interface PrinterWriterUser {
      void handleError(PrintCmd var1, PrintErrorEvent var2);

      void handleCmdComplete(PrintCmd var1);

      boolean handleImmediateError(PrintCmd var1, PrintErrorEvent var2);
   }

   public interface WriterInputQueue {
      void flagQueue();

      boolean isDataPending();

      BooleanMonitor getBlockKey();
   }
}
