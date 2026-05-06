package com.ibm.posj.printer;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.logging.LogHelper;
import com.ibm.jutil.tasks.AbstractTask;
import com.ibm.jutil.tasks.FifoScheduler;
import com.ibm.jutil.tasks.SharedThreader;
import com.ibm.jutil.tasks.TaskScheduler;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.HandleKeyVisitor;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.PrinterBusWriter;
import com.ibm.posj.bus.PrinterPacket;
import com.ibm.posj.bus.printer.cmds.DefaultCmdList;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintErrorEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import java.util.ArrayList;
import java.util.List;

public abstract class DefaultPrinterWriter extends PrinterWriter implements StatusVisitor.ResponseProcessor, InputReceiver {
   private static HandleKey wKey = new DefaultPrinterWriter.WriterKey();
   private LogHelper logger = null;
   private TaskScheduler errorScheduler = new FifoScheduler();
   private DefaultPrinterWriter.ErrorTask errorTask = new DefaultPrinterWriter.ErrorTask();
   private List errList = new ArrayList(10);
   protected SubmissionContainer sentQueue;
   protected InputQueue input = null;
   protected OutputProcessor outProcessor = null;
   protected PrinterPacket packet;
   private SharedThreader errorThread = new SharedThreader();
   protected PrintStatus lastStatus;
   protected PrinterBusWriter parent = null;
   private BooleanMonitor errorBlock = new BooleanMonitor(false);
   private BooleanMonitor errorPending = new BooleanMonitor(false);
   private BooleanMonitor errorPause = new BooleanMonitor(false);
   private BooleanMonitor idleMonitor = new BooleanMonitor(true);
   private PrintCmd errorCmd = null;
   private boolean onLine = false;
   protected PrinterWriter.PrinterWriterUser mainImp = null;
   protected PrintCmdList currentCmd = null;

   public DefaultPrinterWriter(PrinterBusWriter parent, PrinterPacket packet, LogHelper logger) {
      if (getTracer().isOn()) {
         Tracer var10000 = getTracer();
         getTracer();
         var10000.print(1, "PrinterWriter Version 1.35.8.8");
      }

      this.packet = packet;
      this.parent = parent;
      this.logger = logger;
      this.sentQueue = new SubmissionContainer();
      this.errorPending.set(false);
      this.input = new InputQueue(this);
      this.outProcessor = new OutputProcessor(new DefaultPrinterWriter.DefaultOuputWriterInterface());
      this.onLine(null);
      this.input.start();
      this.input.sendNextCmd();
   }

   public boolean isOnline() {
      return this.onLine;
   }

   public boolean isOutputPending() {
      PrintCmdList p = this.sentQueue.getNextCmdListToComplete();
      boolean flag = false;
      if (this.currentCmd != null) {
         flag = this.currentCmd.isOutputList();
      }

      return flag || p != null;
   }

   public POSPrinterCmd getNextProcessCmd() {
      PrintCmdList l = this.sentQueue.getNextCmdListToComplete();
      if (null == l) {
         l = this.currentCmd;
      }

      if (null == l) {
         return null;
      } else {
         PrintCmd pc = l.getErrorResponsePending();
         POSPrinterCmd pcmd = null;
         if (null != pc) {
            pcmd = pc.getHandleCmd();
         }

         return pcmd;
      }
   }

   public ByteBuffer getFormattedBuffer() {
      return this.packet.format();
   }

   public PrintStatus getCurrentStatus() {
      return this.lastStatus;
   }

   public BooleanMonitor getIdleKey() {
      return this.idleMonitor;
   }

   public void setMainImp(PrinterWriter.PrinterWriterUser imp) {
      this.mainImp = imp;
   }

   public int getMaxCmdLen() {
      return this.packet.getMaximum();
   }

   public void processSend() {
      this.processSent();
   }

   public void processComplete(POSPrinterCmd dpc) {
      this.commandComplete(dpc);
   }

   public boolean isStatusPending() {
      return this.parent.isStatusPending();
   }

   public synchronized void writePrintCmds() {
      try {
         this.errorBlock.waitForFalse(5000);
      } catch (Exception var4) {
         return;
      }

      if (getTracer().isOn()) {
         getTracer().println("writePrintCmds " + this.input);
      }

      if (this.input.isEmpty()) {
         this.input.sendNextCmd();
      } else if (null == this.currentCmd) {
         PrintCmdList cCmd = this.input.getNextList();
         if (cCmd == null) {
            this.input.sendNextCmd();
         } else if (cCmd.hasCommands() && 0 < cCmd.getDataSize() && !cCmd.isFullyComplete()) {
            if (getTracer().isOn()) {
               print("------------------------------wpc " + cCmd);
            }

            if ((null == this.lastStatus || !this.lastStatus.isHardwareError()) && this.isOnline() || null != (cCmd = this.writeErrorCmd(cCmd))) {
               this.flush(cCmd);
               if (getTracer().isOn()) {
                  print("writePrintCmds>>");
               }
            }
         } else {
            if (getTracer().isOn()) {
               print("Tossing a bad cmd??? " + cCmd);
            }

            try {
               while (cCmd.commandComplete() != null) {
                  try {
                     cCmd.commandComplete().getHandleCmd().setCompleted(true);
                  } catch (Exception var3) {
                     getTracer().print(var3);
                  }
               }

               cCmd.recycle();
               PrintCmdList var6 = null;
            } catch (Exception var5) {
               if (getTracer().isOn()) {
                  getTracer().print(var5);
               }
            }

            this.input.sendNextCmd();
         }
      }
   }

   public HandleKey getHandleKey() {
      return wKey;
   }

   public void receivePrintStatus(PrintStatus ps) {
      try {
         if (getTracer().isOn()) {
            getTracer().println("writer->>recPrintStatus " + ps + " " + this.currentCmd);
         }

         if (ps.getData()[0] == 0 && ps.getData()[1] == 0 && ps.getData()[2] == 0) {
            if (getTracer().isOn()) {
               print("THIS IS TRASH???");
            }

            return;
         }

         if (this.lastStatus != null) {
            this.lastStatus.recycle();
         }

         this.lastStatus = ps;
         this.errorPause.set(false);
         this.outProcessor.processStatus(ps, this.currentCmd);
         this.postStatusProcess(ps);
         this.sentQueue.format();
         if (getTracer().isOn()) {
            getTracer().println("writer<<-recPrintStatus");
         }
      } catch (Exception var3) {
         if (getTracer().isOn()) {
            getTracer().print(var3);
         }
      }
   }

   public void postStatusProcess(PrintStatus ps) {
      boolean cFlag = 0 >= this.sentQueue.size() && null == this.currentCmd;
      getTracer().println("Poststatus " + cFlag + " imonitor " + this.idleMonitor.isTrue());
      if ((null != this.currentCmd || !ps.getStatus(9)) && !cFlag) {
         if (this.idleMonitor.isTrue()) {
            this.idleMonitor.set(false);
         }
      } else {
         if (getTracer().isOn()) {
            getTracer().println("Set idlemonitor");
         }

         if (this.idleMonitor.isFalse()) {
            this.idleMonitor.set(true);
         }
      }

      if (cFlag) {
         if (getTracer().isOn()) {
            getTracer().println(">>Clear all error list<< - " + this.errorPending.isTrue());
         }

         if (0 < this.errList.size()) {
            this.errList.clear();
         }

         if (this.errorPending.isTrue()) {
            this.errorPending.set(false);
            this.input.sendNextCmd();
         }
      }

      try {
         if (getTracer().isOn()) {
            getTracer().println(ps.checkCmdComplete() + "\n Q cnt sent" + this.sentQueue + "\n\npending" + this.input);
         }
      } catch (Exception var4) {
      }
   }

   public void receivePrintDataEvent(PrintDataEvent pevent) {
      if (getTracer().isOn()) {
         getTracer().println("receivePrintData");
      }

      PrintCmdList p = this.sentQueue.getNextCmdListToComplete();

      for (int i = 1; p != null; p = this.sentQueue.getCmdListToComplete(i++)) {
         if (null == p) {
            this.input.sendNextCmd();
            if (getTracer().isOn()) {
               getTracer().println(">>DataEventOccured");
            }

            return;
         }

         PrintCmd cmd = p.getResponsePending();
         DefaultPOSPrinterCmd req = cmd == null ? null : (DefaultPOSPrinterCmd)cmd.getHandleCmd();
         if (req instanceof POSPrinterCmd.DataRequesterCmd) {
            IBM4610PrinterCmd.StartScanCmd ssc = null;
            if (req instanceof IBM4610PrinterCmd.StartScanCmd) {
               ssc = (IBM4610PrinterCmd.StartScanCmd)req;
            }

            if (ssc == null || ssc.getRequestData() == null) {
               req.setRequestedData(pevent.getDataBuffer());
            }

            if (ssc == null || !ssc.isScanAndMicrRead()) {
               this.commandComplete(req);
            }
            break;
         }
      }
   }

   public void offLine(Object eObject) {
      this.onLine = false;
      PrintStatus pee = (PrintStatus)this.getCurrentStatus().clone();
      pee.setOfflineStatus();
      this.sentQueue.format();
      this.processSent();
      this.errList.add(pee);
      this.errorScheduler.post(this.errorTask);
      this.input.sendNextCmd();
   }

   public void onLine(Object eObject) {
      this.onLine = true;
   }

   protected void checkForHang() {
      this.parent.respondToFreeze();
   }

   protected boolean responsePending() {
      return this.sentQueue.size() > 0 || this.currentCmd != null;
   }

   protected InputQueue getInputQ() {
      return this.input;
   }

   protected PrintErrorEvent setPostErrorEvent(byte station, byte priority) {
      PrintErrorEvent event = null;
      if (!this.isOnline()) {
         PrintStatus.ErrorStruct es = new PrintStatus.ErrorStruct();
         es.type = 2;
         es.code = 256;
         event = PrintErrorEvent.createErrorEvent(this, station, es);
      } else if (-1 == station) {
         event = this.getCurrentStatus().getCachedError((byte)-1);
      } else {
         if (0 < (station & 8)) {
            event = this.getCurrentStatus().getCachedError((byte)8);
         }

         if (0 < (station & 2) && (priority | 8) > 0 && null == event) {
            event = this.getCurrentStatus().getCachedError((byte)2);
         }

         if ((0 < (station & 4) || 0 < (station | 20)) && null == event && ((priority | 8) > 0 || (priority | 2) > 0)) {
            event = this.getCurrentStatus().getCachedError((byte)4);
         }
      }

      return event;
   }

   protected void rejectImmediateCmd(PrintCmdList pcl, PrintErrorEvent error) {
      try {
         PrintCmd prtCmd = pcl.getPending();
         if (prtCmd.getParent().handleImmediateError(prtCmd, error)) {
            this.input.quickRetry(pcl);
         }
      } catch (Exception var4) {
         if (getTracer().isOn()) {
            getTracer().getTracerOutput().print("Error Extracting immediate cmd ");
            getTracer().getTracerOutput().print(var4);
         }
      }
   }

   protected PrintCmdList createPrintCmdList() {
      return DefaultCmdList.getFactory().createPrintCmdList(this.getMaxCmdLen());
   }

   protected abstract CmdLoadedStatusVisitor getCmdLoadVisitor();

   protected abstract CmdCompleteStatusVisitor getCmdCompleteVisitor();

   protected PrintCmdList findErrorList() {
      PrintCmdList badCl = this.sentQueue.getNextCmdListToComplete();
      int i = 1;

      while (null != badCl && null == badCl.getErrorResponsePending()) {
         badCl = this.sentQueue.getCmdListToComplete(i++);
         if (null == badCl) {
            break;
         }
      }

      return badCl;
   }

   protected void delegateError(PrintStatus ps) {
      PrintErrorEvent pe0 = null;

      try {
         if (getTracer().isOn()) {
            print("delegateError<< sq" + ps);
         }

         PrintCmdList badCl = null;
         if (getTracer().isOn()) {
            print("delegateError>>>SWError-->" + ps.getSWError());
         }

         if (null != ps.getSWError()) {
            badCl = this.currentCmd == null ? this.sentQueue.getLastSent() : this.currentCmd;
            pe0 = ps.getSWError();
         } else if (ps.isHardwareError()) {
            badCl = this.findErrorList();
            if (badCl != null) {
               PrintCmd errorCmd = badCl.getErrorResponsePending();
               if (getTracer().isOn()) {
                  print("ERResponsePending " + badCl.getErrorResponsePending());
               }

               pe0 = ps.getCachedError(errorCmd.getHandleCmd().getImpliedStations());
               if (pe0 == null) {
                  ps.recycle();
                  this.errorPending.set(false);
                  return;
               }
            }
         }

         if (null == badCl) {
            if (null == this.currentCmd || ps.getSWError() == null) {
               ps.recycle();
               this.errorPending.set(false);
               this.input.sendNextCmd();
               return;
            }

            this.processSent();
            badCl = this.currentCmd;
            this.currentCmd = null;
         }

         ps.recycle();
         if (this.isOnline()) {
            this.parent.clearBuffers();
            ((IBMPrinterImp)this.mainImp).cancelRotateMode();
         }

         this.fireErrorToHandler(pe0, badCl);
      } catch (Exception var5) {
         if (getTracer().isOn()) {
            print("delegateError Should not be here 2");
         }

         this.errorBlock.set(true);
         this.mainImp.handleError(null, pe0);
         this.errorBlock.set(false);
      }

      this.input.sendNextCmd();
   }

   protected void fireErrorToHandler(PrintErrorEvent pe, PrintCmdList badCl) {
      if (getTracer().isOn()) {
         print(">>fireErrorToHandler");
      }

      Object parent = badCl.getParent();
      this.errorCmd = pe.getErrorType() == 2 ? badCl.getErrorResponsePending() : badCl.getResponsePending();
      if (this.errorCmd == null && pe.getPrinterErrorCode() == 256) {
         this.errorCmd = badCl.getPending();
      }

      pe.setOffender(((DefaultPOSPrinterCmd)this.errorCmd.getHandleCmd()).getOwnerCmd());
      if (null != this.currentCmd) {
         this.processSent();
      }

      if (0 == pe.getErrorStation()) {
         pe.setErrorStation(this.errorCmd.getStation());
      }

      if (parent instanceof RetryInputQueue) {
         this.errorBlock.set(true);
         RetryInputQueue pws = (RetryInputQueue)parent;
         pws.handleError(this.errorCmd, pe);
         this.errList.clear();
         this.resendCmds();
         this.errorThread.setActionObject(pws);
         this.errorBlock.set(false);
         this.errorPending.set(false);
      } else {
         if (getTracer().isOn()) {
            print("delegateError Should not be here 1");
         }

         this.errorBlock.set(true);
         this.mainImp.handleError(this.errorCmd, pe);
         this.resendCmds();
         this.errorBlock.set(false);
      }

      if (getTracer().isOn()) {
         print("<<fireErrorToHandler");
      }
   }

   protected boolean getErrorPending() {
      return this.errorPending.isTrue();
   }

   protected void commandComplete(POSPrinterCmd pospc) {
      if (getTracer().isOn()) {
         print(">>cmdComplete " + pospc);
      }

      if (pospc != null) {
         PrintCmd pcmd = ((DefaultPOSPrinterCmd)pospc).getPrintCmd();
         if (null == pcmd) {
            pospc.setCompleted(true);
         } else {
            pcmd.setSucceeded(true);
            PrinterWriter.PrinterWriterUser pws = pcmd.getParent();
            if (pws == null) {
               POSPrinterCmd err = pcmd.getHandleCmd();
               if (err == null) {
                  return;
               }

               err.setCompleted(true);
               err.recycle();
            } else {
               if (pcmd.getHandleCmd().getMasterCmd() == pcmd.getHandleCmd()) {
                  this.sentQueue.format();
               }

               pws.handleCmdComplete(pcmd);
            }
         }
      }
   }

   protected void processSent() {
      if (null != this.currentCmd) {
         if (getTracer().isOn()) {
            print(Thread.currentThread().getName() + ">~~~~~~~~Move~~~~~~~~<" + this.currentCmd);
         }

         if (getTracer().isOn()) {
            this.parent.timeStamp().cmdLoaded(this.currentCmd);
         }

         this.sentQueue.addToContainer(this.currentCmd);
         this.currentCmd = null;
         if (getTracer().isOn()) {
            print(Thread.currentThread().getName() + "<~~~~~~~~Move~~~~~~~~>" + this.currentCmd);
         }
      }
   }

   protected void readExtraData() {
      if (this.getCurrentStatus().isExtendedData()) {
         this.parent.processExtraData(this.getCurrentStatus());
      }
   }

   protected void flush(PrintCmdList pcl) {
      if (null != pcl) {
         try {
            pcl.setWriteToBuffer(this.getFormattedBuffer());
            ByteBuffer b = pcl.getWriteBuffer();
            if (!this.isOnline()) {
               return;
            }

            if (getTracer().isOn()) {
               this.parent.timeStamp().flush(pcl, b);
            }

            this.currentCmd = pcl;
            this.packet.send(b);
            if (pcl.isAutoLoad()) {
               if (getTracer().isOn()) {
                  print(">>>DOING AUTOLOADING<<<");
               }

               this.processSent();
               this.input.sendNextCmd();
            }
         } catch (Exception var3) {
            this.logger.addLogEntry(1010, var3.toString(), "POSPrinter", 5);
            if (getTracer().isOn()) {
               getTracer().print(var3);
            }
         }
      }
   }

   private PrintCmdList writeErrorCmd(PrintCmdList tCmd) {
      if (!this.isOnline()) {
         if (getTracer().isOn()) {
            print("Offline");
         }

         this.rejectList(tCmd);
         return null;
      } else {
         if (tCmd.compareStations(this.lastStatus.getErrorStation())) {
            if (getTracer().isOn()) {
               print("Restructuring");
            }

            PrintCmdList ncurrent = this.createPrintCmdList();
            tCmd.restructure(ncurrent, this.lastStatus.getErrorStation());
            this.sentQueue.format();
            if (!ncurrent.hasCommands()) {
               if (getTracer().isOn()) {
                  print("Restructuring none can filter");
               }

               this.rejectList(tCmd);
               return null;
            }

            if (!tCmd.hasCommands()) {
               tCmd.recycle();
            } else {
               tCmd.reset();
               this.input.quickRetry(tCmd);
            }

            tCmd = ncurrent;
         }

         return tCmd;
      }
   }

   private void rejectList(PrintCmdList tCmd) {
      tCmd.setError(true);
      DefaultPOSPrinterCmd c = (DefaultPOSPrinterCmd)tCmd.getPending().getHandleCmd();
      PrintErrorEvent event = null;
      if (c.getOwnerCmd().getStation() == 0) {
         event = this.setPostErrorEvent((byte)2, (byte)2);
      } else {
         event = this.setPostErrorEvent(c.getOwnerCmd().getStation(), c.getStation());
      }

      PrintCmdList var7 = null;
      tCmd.reset();
      this.input.quickRetry(tCmd);
      this.fireErrorToHandler(event, tCmd);

      try {
         if (this.isOnline()) {
            this.parent.clearBuffers();
            ((IBMPrinterImp)this.mainImp).cancelRotateMode();
         }
      } catch (HandleException var6) {
         if (getTracer().isOn()) {
            getTracer().print(var6);
         }
      }

      this.input.sendNextCmd();
   }

   private void resendCmds() {
      if (getTracer().isOn()) {
         print("resending cmds");
      }

      this.input.resendCmds();

      try {
         this.sentQueue.resendCmds();
      } catch (Exception var2) {
         if (getTracer().isOn()) {
            getTracer().print(var2);
         }
      }
   }

   private class DefaultOuputWriterInterface implements OutputProcessor.WriterInterface {
      private DefaultOuputWriterInterface() {
      }

      public void forceLoad() {
         DefaultPrinterWriter.this.processSent();
      }

      public CmdCompleteStatusVisitor getCmdCompleteVisitor() {
         return DefaultPrinterWriter.this.getCmdCompleteVisitor();
      }

      public CmdLoadedStatusVisitor getCmdLoadVisitor() {
         return DefaultPrinterWriter.this.getCmdLoadVisitor();
      }

      public boolean getErrorPending() {
         return DefaultPrinterWriter.this.getErrorPending();
      }

      public SubmissionContainer getSentQueue() {
         return DefaultPrinterWriter.this.sentQueue;
      }

      public boolean isStatusPending() {
         return DefaultPrinterWriter.this.isStatusPending();
      }

      public void setError(PrintStatus e) {
         if (null != e) {
            e = (PrintStatus)e.clone();
            if (null != e.getSWError()) {
               PrinterWriter.getTracer().println("-----directreport-----");
               DefaultPrinterWriter.this.delegateError(e);
            }

            if (!DefaultPrinterWriter.this.errList.isEmpty()) {
               PrintStatus p1 = (PrintStatus)DefaultPrinterWriter.this.errList.get(DefaultPrinterWriter.this.errList.size() - 1);
               if (!p1.errorCompare(e)) {
                  PrinterWriter.getTracer().println("appending error 1 " + e);
                  DefaultPrinterWriter.this.errorPending.set(true);
                  DefaultPrinterWriter.this.errList.add(e);
                  DefaultPrinterWriter.this.errorScheduler.post(DefaultPrinterWriter.this.errorTask);
               }
            } else {
               PrinterWriter.getTracer().println("appending error 2 " + e);
               DefaultPrinterWriter.this.errorPending.set(true);
               DefaultPrinterWriter.this.errList.add(e);
               DefaultPrinterWriter.this.errorScheduler.post(DefaultPrinterWriter.this.errorTask);
            }
         }
      }

      public void sendNext() {
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("sendnext");
         }

         DefaultPrinterWriter.this.input.sendNextCmd();
      }
   }

   private class ErrorTask extends AbstractTask {
      private ErrorTask() {
      }

      public void executeTask() {
         if (DefaultPrinterWriter.this.errList.isEmpty()) {
            if (PrinterWriter.getTracer().isOn()) {
               PrinterWriter.getTracer().println("All errors apparently have been cleared!!!!!");
            }
         } else {
            try {
               while (true) {
                  if (PrinterWriter.getTracer().isOn()) {
                     PrinterWriter.print("Error task posted<---------------------");
                  }

                  try {
                     DefaultPrinterWriter.this.errorPause.set(true);
                     DefaultPrinterWriter.this.errorPause.waitForFalse(PrinterWriter.NONRESPONDING_TIMEOUT);
                  } catch (Exception var2) {
                  }

                  if (DefaultPrinterWriter.this.errorPause.isTrue()) {
                     if (PrinterWriter.getTracer().isOn()) {
                        PrinterWriter.print("Error task posted--------------------->");
                     }

                     if (0 < DefaultPrinterWriter.this.sentQueue.size()) {
                        while (DefaultPrinterWriter.this.errList.size() > 0) {
                           DefaultPrinterWriter.this.delegateError((PrintStatus)DefaultPrinterWriter.this.errList.remove(0));
                        }
                     } else {
                        if (PrinterWriter.getTracer().isOn()) {
                           PrinterWriter.print("Empty resetting error ");
                        }

                        DefaultPrinterWriter.this.errorPending.set(false);
                        if (!DefaultPrinterWriter.this.errList.isEmpty()) {
                           DefaultPrinterWriter.this.errList.remove(0);
                        }

                        DefaultPrinterWriter.this.input.sendNextCmd();
                     }
                     break;
                  }
               }
            } catch (Exception var3) {
               if (PrinterWriter.getTracer().isOn()) {
                  PrinterWriter.getTracer().print(var3);
               }
            }
         }
      }
   }

   static class WriterKey implements HandleKey {
      private String string = "WriterKey";

      public void accept(HandleKeyVisitor visitor) {
         visitor.visitUnknownHandleKey(this);
      }

      public String toString() {
         return this.string;
      }

      public int hashCode() {
         return this.string.hashCode();
      }

      public boolean equals(Object obj) {
         return obj != null && obj instanceof DefaultPrinterWriter.WriterKey ? obj.toString().equals(this.string) : false;
      }

      public DevCat getDevCat() {
         return DevCats.UNKNOWN_DEVCAT;
      }

      public DevBus getDevBus() {
         return DevBuses.UNKNOWN_DEVBUS;
      }
   }
}
