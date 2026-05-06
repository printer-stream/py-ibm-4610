package com.ibm.posj.bus.printer;

import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.IBM4689PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.bus.PrinterBusWriter;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.bus.PrinterSubDevices;
import com.ibm.posj.bus.hid.HidHandleImp;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.IBMPrinterState;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.printer.ibm4689.IBM4689Imp;
import com.ibm.posj.printer.ibm4689.IBM4689Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IBM4689Utility implements PrinterUtil {
   private Tracer tracer;
   private List subDevices = new ArrayList(5);
   private boolean updateCollection = false;

   public IBMPrinterImp createPrinterImp(PrinterWriter writer, DefaultPOSPrinterHandle def) {
      return new IBM4689Imp(writer, def, def.getPrinterLogHelper());
   }

   public PrinterWriter createWriter(PrinterBusWriter pbw, PrinterPacket pack, POSPrinterHandle handle, POSPrinterCmd.Factory factory) {
      PrinterWriter writer = new IBM4689Writer(pbw, pack, handle.getPrinterLogHelper(), (IBM4689PrinterCmd.Factory)factory);
      this.addDevice(writer.getHandleKey(), writer);
      return writer;
   }

   public void firePrintDataEvent(PrintDataEvent pde) {
      if (this.traceOn()) {
         PrinterWriter.getTracer().print("fire pde");
      }

      Iterator it = this.getSecondaryHandleImps();

      while (it.hasNext()) {
         try {
            PrinterSubDevices sub = (PrinterSubDevices)it.next();
            sub.receivePrintDataEvent(pde);
         } catch (Exception var4) {
            if (this.traceOn()) {
               PrinterWriter.getTracer().print(var4);
            }
         }
      }
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
      int type = 0;
      if (ps.getStatus(640)) {
         type++;
      }

      if (ps.getStatus(514)) {
         type += 2;
      }

      if (ps.getStatus(258)) {
         type += 4;
      }

      return type;
   }

   public void init(HandleImp dev, IBMPrinterImp imp, POSPrinterCmd reinitCmds, int wait) throws HandleException {
      if (this.traceOn()) {
         this.trace(">>INIT 4689 sending EcLevelRequest Cmd");
      }

      IBM4689PrinterCmd.Factory factory = (IBM4689PrinterCmd.Factory)dev.getHandle().getHandleCmdFactory();
      imp.setPOSPrintCmdFactory(factory);
      ByteBuffer dataReq = ByteBuffer.getByteBufferFactory().createByteBuffer();
      POSPrinterCmd c = factory.createECLevelRequestCmd(false);
      dev.submit(c);

      try {
         c.waitUntilCompleted((long)wait);
         dataReq = ((IBM4689PrinterCmd.ECLevelRequestCmd)c).getRequestData();
         if (dataReq != null && dataReq.getByteCount() >= 2) {
            int EcLevel = Util.toInt(dataReq.byteAt(0), dataReq.byteAt(1));
            ((IBMPrinterState)imp.getPrinterHandleState()).setECLevel(EcLevel);
         }
      } catch (Exception var11) {
         ((DefaultPOSPrinterHandle)dev.getHandle()).setOnline(false);
         imp.clearOutput();
         throw new HandleException("Couldn't initialize");
      }

      if (dev instanceof HidHandleImp) {
         imp.getPrinterState().setSerialNumber(((HidHandleImp)dev).getSerialNumber());
      }

      if (this.traceOn()) {
         this.trace("INIT 4689 sending DeviceInfoCmd Cmd");
      }

      POSPrinterCmd d = factory.createDevInfoCmd(false);
      dev.submit(d);

      try {
         d.waitUntilCompleted();
      } catch (Exception var10) {
         ((DefaultPOSPrinterHandle)dev.getHandle()).setOnline(false);
         imp.clearOutput();
         throw new HandleException("Couldn't initialize");
      }

      c.recycle();
      d.recycle();
      if (this.traceOn()) {
         this.trace("<<INIT 4689");
      }
   }

   public void addDevice(HandleKey key, PrinterSubDevices handleImp) {
      this.subDevices.add(handleImp);
      this.updateCollection = true;
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
