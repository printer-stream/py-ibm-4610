package com.ibm.posj.bus.printer;

import com.ibm.jutil.logging.LogHelper;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.bus.PrinterBusWriter;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.bus.PrinterSubDevices;
import com.ibm.posj.event.DataEvent;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
import com.ibm.posj.event.OutputCompleteEvent;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.IBMPrinterState;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintErrorEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.printer.ibm4610.ChaseVisitors;
import com.ibm.posj.printer.ibm4610.CrabtreeImp;
import com.ibm.posj.printer.ibm4610.IBM4610Imp;
import com.ibm.posj.printer.ibm4610.IBM4610Rs232Imp;
import com.ibm.posj.printer.ibm4610.IBM4610Rs232Writer;
import com.ibm.posj.printer.ibm4610.IBM4610Writer;
import com.ibm.posj.printer.ibm4610.Rs232CrabtreeImp;
import com.ibm.posj.util.DevBuses;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IBM4610Utility implements PrinterUtil {
   private boolean lineFlag = true;
   private Tracer tracer = null;
   private byte printerType = 0;
   private List subDevices = new ArrayList(5);
   private IBM4610Utility.InitListener initListener = new IBM4610Utility.InitListener();
   public static final String USE_EC_LEVEL232 = "rs232.4610.EC";
   public static final String USE_EC_LEVEL485 = "rs485.4610.EC";
   public static final String USE_EC_LEVELUSB = "usb.4610.EC";

   public IBM4610Utility(byte printerType, Tracer tracer) {
      this.printerType = printerType;
      this.tracer = tracer;
   }

   public IBMPrinterImp createPrinterImp(PrinterWriter writer, DefaultPOSPrinterHandle def) {
      IBM4610Imp imp = null;
      if (def.getDevBus() == DevBuses.RS232_DEVBUS) {
         if (49 == this.printerType) {
            if (this.traceOn()) {
               this.getTracer().println("creating Rs232CrabtreeImp");
            }

            imp = new Rs232CrabtreeImp(writer, def, def.getPrinterLogHelper());
         } else {
            if (this.traceOn()) {
               this.getTracer().println("creating IBM4610Rs232Imp");
            }

            imp = new IBM4610Rs232Imp(writer, def, def.getPrinterLogHelper());
         }
      } else if (49 == this.printerType) {
         if (this.traceOn()) {
            this.getTracer().println("creating CrabtreeImp");
         }

         imp = new CrabtreeImp(writer, def, def.getPrinterLogHelper());
      } else {
         if (this.traceOn()) {
            this.getTracer().println("creating IBM4610Imp");
         }

         imp = new IBM4610Imp(writer, def, def.getPrinterLogHelper());
      }

      if (this.isLineFlag()) {
         if (this.traceOn()) {
            PrinterWriter.getTracer().println("setting IBM4610Imp with linecnt");
         }

         imp.setChaseVisitor(new ChaseVisitors.LineCntChaseVisitor(imp));
      } else {
         if (this.traceOn()) {
            PrinterWriter.getTracer().println("setting IBM4610Imp with EC");
         }

         imp.setChaseVisitor(new ChaseVisitors.EcLevelChaseVisitor(imp));
      }

      return imp;
   }

   public PrinterWriter createWriter(PrinterBusWriter pbw, PrinterPacket pack, POSPrinterHandle handle, POSPrinterCmd.Factory factory) {
      LogHelper logger = handle.getPrinterLogHelper();
      IBM4610Writer writer = null;
      Object var7;
      if (handle.getDevBus() == DevBuses.RS232_DEVBUS) {
         this.setLineFlag(!PosSystemManager.getInstance().getProperties().isPropertyDefined("rs232.4610.EC"));
         if (this.traceOn()) {
            this.getTracer().println("creating IBM4610Rs232Writer");
         }

         var7 = new IBM4610Rs232Writer(pbw, pack, logger, (IBM4610PrinterCmd.Factory)factory, this.isLineFlag());
      } else {
         if (handle.getDevBus() == DevBuses.RS485_DEVBUS) {
            this.setLineFlag(!PosSystemManager.getInstance().getProperties().isPropertyDefined("rs485.4610.EC"));
         } else {
            this.setLineFlag(!PosSystemManager.getInstance().getProperties().isPropertyDefined("usb.4610.EC"));
         }

         if (this.traceOn()) {
            this.getTracer().println("creating IBM4610Writer lineCnt = " + this.isLineFlag());
         }

         var7 = new IBM4610Writer(pbw, pack, logger, (IBM4610PrinterCmd.Factory)factory, this.isLineFlag());
      }

      this.addDevice(null, (PrinterSubDevices)var7);
      return (PrinterWriter)var7;
   }

   public void firePrintDataEvent(PrintDataEvent pde) {
      if (this.traceOn()) {
         this.getTracer().print("fire pde");
      }

      Iterator it = this.getSecondaryHandleImps();

      while (it.hasNext()) {
         try {
            PrinterSubDevices sub = (PrinterSubDevices)it.next();
            if (this.traceOn()) {
               this.getTracer().print(">--pde " + sub);
            }

            sub.receivePrintDataEvent(pde);
            if (this.traceOn()) {
               this.getTracer().print("<--pde");
            }
         } catch (Exception var4) {
            if (this.traceOn()) {
               this.getTracer().print(var4);
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
            this.getTracer().print(var5);
         }
      }

      if (this.traceOn()) {
         this.trace("distributeStatus>>");
      }
   }

   public int getPrinterDataType(PrintStatus ps) {
      int type = 0;
      if (ps.getStatus(1281)) {
         type++;
      }

      if (ps.getStatus(10)) {
         type += 16;
      }

      if (ps.getStatus(1282)) {
         type += 2;
      }

      if (ps.getStatus(1288)) {
         type += 4;
      }

      if (ps.getStatus(1296)) {
         type += 8;
      }

      if (ps.getStatus(1344)) {
         type += 32;
      }

      if (ps.getStatus(1408)) {
         type += 64;
      }

      return type;
   }

   public void init(HandleImp dev, IBMPrinterImp imp, POSPrinterCmd reinitCmds, int wait) throws HandleException {
      if (this.traceOn()) {
         this.trace(">>INIT");
      }

      IBM4610PrinterCmd.Factory factory = (IBM4610PrinterCmd.Factory)dev.getHandle().getHandleCmdFactory();
      imp.setPOSPrintCmdFactory(factory);
      dev.getHandle().addHandleListener(this.initListener);
      ((IBMPrinterState)imp.getPrinterHandleState()).setSerialNumber(imp.getSerialNumber());
      dev.getHandle().removeHandleListener(this.initListener);
      POSPrinterCmd c = factory.createReleasePrintBufferCmd(true);
      c.appendPOSPrinterCmd(reinitCmds);
      c.appendPOSPrinterCmd(factory.createEndRegisterSlipCmd());
      dev.submit(factory.createDevInfoCmd(false));
      dev.submit(c);

      try {
         c.waitUntilCompleted((long)wait);
      } catch (Exception var8) {
         ((DefaultPOSPrinterHandle)dev.getHandle()).setOnline(false);
         imp.clearOutput();
         throw new HandleException("Couldn't initialize");
      }

      c.recycle();
      if (this.traceOn()) {
         this.trace("<<INIT");
      }
   }

   public void addDevice(HandleKey key, PrinterSubDevices handleImp) {
      if (key == null) {
         this.subDevices.add(0, handleImp);
      } else {
         this.subDevices.add(handleImp);
      }
   }

   public Iterator getSecondaryHandleImps() {
      return this.subDevices.iterator();
   }

   private void setLineFlag(boolean lineFlag) {
      this.lineFlag = lineFlag;
   }

   public boolean isLineFlag() {
      return this.lineFlag;
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

   public class InitListener implements Handle.Listener {
      public void outputCompleteEventOccurred(OutputCompleteEvent event) {
      }

      public void dataEventOccurred(DataEvent event) {
      }

      public void directIOEventOccurred(DirectIOEvent event) {
      }

      public void statusEventOccurred(StatusEvent event) {
      }

      public void errorEventOccurred(ErrorEvent event) {
         ((PrintErrorEvent)event).setResponse((byte)3);
      }

      public void onlineEventOccurred(OnlineEvent event) {
      }

      public void offlineEventOccurred(OfflineEvent event) {
      }
   }
}
