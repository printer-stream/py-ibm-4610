package com.ibm.posj.bus.printer;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.IBMSureonePrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.bus.PrinterBusWriter;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.bus.PrinterSubDevices;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.printer.sureone.IBMSureoneImp;
import com.ibm.posj.printer.sureone.IBMSureoneWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IBMSureoneUtility implements PrinterUtil {
   private Tracer tracer = null;
   private List subDevices = new ArrayList(5);

   public IBMPrinterImp createPrinterImp(PrinterWriter writer, DefaultPOSPrinterHandle def) {
      return new IBMSureoneImp(writer, def, def.getPrinterLogHelper());
   }

   public PrinterWriter createWriter(PrinterBusWriter pbw, PrinterPacket pack, POSPrinterHandle handle, POSPrinterCmd.Factory factory) {
      PrinterWriter writer = new IBMSureoneWriter(pbw, pack, handle.getPrinterLogHelper(), factory);
      this.addDevice(writer.getHandleKey(), writer);
      return writer;
   }

   public void firePrintDataEvent(PrintDataEvent pde) {
   }

   public void subDeviceDistribute(Handle handle, PrintStatus ps) {
      if (this.traceOn()) {
         this.trace("distributeStatus");
      }

      Iterator it = this.getSecondaryHandleImps();

      try {
         while (it.hasNext()) {
            PrinterSubDevices obj = (PrinterSubDevices)it.next();
            if (this.traceOn()) {
               this.trace("-->sendStatus - " + obj, 3);
            }

            obj.receivePrintStatus((PrintStatus)ps.clone());
            if (this.traceOn()) {
               this.trace("<--sendStatus");
            }
         }

         ps.recycle();
      } catch (Exception var5) {
         if (this.traceOn()) {
            PrinterWriter.getTracer().print(var5);
         }
      }

      if (this.traceOn()) {
         this.trace("distributeStatus>>");
      }
   }

   public int getPrinterDataType(PrintStatus ps) {
      return 0;
   }

   public void init(HandleImp dev, IBMPrinterImp imp, POSPrinterCmd reinitCmds, int wait) throws HandleException {
      if (this.traceOn()) {
         this.trace(">>INIT");
      }

      IBMSureonePrinterCmd.Factory factory = (IBMSureonePrinterCmd.Factory)dev.getHandle().getHandleCmdFactory();
      imp.setPOSPrintCmdFactory(factory);
      POSPrinterCmd d = factory.createSetXonOffCmd();
      dev.submit(d);

      try {
         d.waitUntilCompleted();
      } catch (Exception var8) {
         ((DefaultPOSPrinterHandle)dev.getHandle()).setOnline(false);
         imp.clearOutput();
         throw new HandleException("Couldn't initialize");
      }

      d.recycle();
      imp.setPrinterStateId(1, 21);
      if (this.traceOn()) {
         this.trace("<<INIT");
      }
   }

   public void addDevice(HandleKey key, PrinterSubDevices handleImp) {
      this.subDevices.add(handleImp);
   }

   public Iterator getSecondaryHandleImps() {
      return this.subDevices.iterator();
   }

   public void setTracer(Tracer trace) {
      this.tracer = trace;
   }

   protected Tracer getTracer() {
      return this.tracer;
   }

   protected boolean traceOn() {
      return this.tracer.isOn();
   }

   protected void trace(String msg, int level) {
      this.getTracer().println(level, " " + msg);
   }

   protected void trace(String msg) {
      this.getTracer().println(" " + msg);
   }

   protected void trace(Exception e) {
      this.getTracer().print(e);
   }
}
