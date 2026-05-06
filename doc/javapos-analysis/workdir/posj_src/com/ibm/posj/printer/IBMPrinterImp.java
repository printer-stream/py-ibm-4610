package com.ibm.posj.printer;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.logging.LogHelper;
import com.ibm.jutil.tasks.AbstractActiveObject;
import com.ibm.jutil.tasks.SubmitTaskScheduler.Submitter;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.DefaultPrinterResult;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleCmdVisitor;
import com.ibm.posj.HandleKey;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.bus.PrinterSubDevices;
import com.ibm.posj.bus.printer.cmds.DefaultCmdList;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;
import com.ibm.posj.printer.event.PrintErrorEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.printer.ibm4610.IBM4610CmdFilterV;
import com.ibm.posj.printer.ibm4610.PrinterDeviceInputQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class IBMPrinterImp implements PrinterWriter.PrinterWriterUser, PrinterSubDevices, Submitter {
   private static Tracer tracer = TracerFactory.getInstance().createTracer("PrtImp");
   private StringBuffer traceBuffer = null;
   private DefaultPOSPrinterHandle handle;
   private DefaultPOSPrinterCmd.Factory factory;
   private IBMPrinterImp.PostDaemon scheduler = new IBMPrinterImp.PostDaemon();
   private PrintCmdList pendingList = null;
   private BooleanMonitor ready = new BooleanMonitor(true);
   private boolean hasBeenAsked = false;
   private short postedCnt = 0;
   private boolean m_immediateMode = false;
   private HandleCmdVisitor incomingCmdV = null;
   private boolean processing = false;
   FilterCmdVisitor filter = new IBM4610CmdFilterV();
   protected boolean clearOutput = false;
   protected int minimumData = 0;
   protected int maximumData = 0;
   protected boolean slCount = true;
   protected DeviceInputQueue inputQ;
   protected PrinterWriter writer;
   protected PrinterHandleState phandleState = null;
   protected LogHelper logger = null;
   public static int INPUT_HOG_STOP = 40;
   public static int INPUT_HOG_TEST = 5;

   public IBMPrinterImp(PrinterWriter mainWriter, DefaultPOSPrinterHandle handle, LogHelper logger) {
      this.writer = mainWriter;
      this.inputQ = new PrinterDeviceInputQueue(this, "Printer");
      this.writer.registerDeviceQueue((RetryInputQueue)this.inputQ);
      this.handle = handle;
      this.logger = logger;
      this.scheduler.start();
      this.inputQ.setSubmissionPolicy(new IBMPrinterImp.ImpSubmissionPolicy());
      this.phandleState = this.createPrinterHandleState();
      this.getPrinterState().setMaxCmdSize(this.getMaxCmdLen());
      this.incomingCmdV = new IBMPrinterImp$1(this);
   }

   public boolean adjusted(DefaultPOSPrinterCmd dpc) {
      return false;
   }

   public abstract String getSerialNumber();

   public void log(int ID, String extra) {
      if (null != this.logger) {
         ;
      }
   }

   public boolean isDataPending() {
      boolean flag = this.getInputQ().isDataPending();
      return flag | !this.scheduler.isEmpty();
   }

   public HandleKey getHandleKey() {
      return null;
   }

   public void setPOSPrintCmdFactory(POSPrinterCmd.Factory factory) {
      this.factory = (DefaultPOSPrinterCmd.Factory)factory;
   }

   public abstract AbstractCmdCompleteVisitor getCmdCompleteVisitor();

   public PrinterHandleState getPrinterHandleState() {
      return this.phandleState;
   }

   public abstract IBMPrinterState getPrinterState();

   public void setChaseVisitor(HandleCmdVisitor h) {
   }

   public void clearOutput() {
      this.clearOutput = true;
      this.scheduler.clearOutput();
   }

   public void receivePrintStatus(PrintStatus ps) {
      this.getHandle().setActive();
      this.getHandle().getEventHelper().fireStatusEvent(ps);
   }

   public void offLine(Object eObject) {
   }

   public void onLine(Object eObject) {
   }

   public void submit(HandleCmd cmd1) {
      if (!this.writer.isOnline()) {
         this.getHandle().setOnline(false);
      }

      cmd1.accept(this.incomingCmdV);
   }

   public void submit(Object o) {
      if (this.ready.isFalse()) {
         this.ready.waitForTrue(15000);
      }

      this.processing = true;
      this.postedCnt--;

      try {
         this.clearOutput = false;
         if (this.traceOn()) {
            this.getHandle().timeStamp().offHandleQueue(o);
         }

         DefaultPOSPrinterCmd dpc = (DefaultPOSPrinterCmd)o;
         if (this.traceOn()) {
            this.trace(Thread.currentThread().getName() + (dpc.isImmediate() ? " Immediate " : "") + " <<scheduler dequeue " + o);
            this.trace(dpc.print());
         }

         if (this.m_immediateMode == dpc.isImmediate()) {
            this.writeSubmit();
         }

         this.submitDefaultCmd(dpc);
         if (this.m_immediateMode) {
            this.writeSubmit();
            this.inputQ.addImmediateCmdList(null);
            this.m_immediateMode = false;
         } else if (this.scheduler.isEmpty()) {
            this.writeSubmit();
            this.inputQ.addCmdList(null);
         } else if (this.hasBeenAsked || this.minimumData <= this.getPendingList().getDataSize()) {
            this.writeSubmit();
            this.inputQ.addCmdList(null);
         }

         if (this.traceOn()) {
            this.trace("return to scheduler " + o);
            this.trace("scheduler " + this.scheduler);
         }
      } catch (Exception var6) {
         if (this.traceOn()) {
            PrinterWriter.getTracer().print(var6);
         }
      } finally {
         this.processing = false;
      }
   }

   public int getMaxCmdLen() {
      return this.writer.getMaxCmdLen();
   }

   public PrintCmdList createPrintCmdList() {
      PrintCmdList l = DefaultCmdList.getFactory().createPrintCmdList(this.writer.getMaxCmdLen());
      this.trace("new list for use " + l);
      return l;
   }

   public boolean handleImmediateError(PrintCmd err, PrintErrorEvent event) {
      DefaultPOSPrinterCmd dpc = (DefaultPOSPrinterCmd)err.getHandleCmd();
      this.handleHandleCmdComplete(dpc.getOwnerCmd(), false);
      return false;
   }

   public void handleCmdComplete(PrintCmd ccmd) {
      if (this.traceOn()) {
         this.trace("Hcc " + ccmd + " Hc " + ccmd.getHandleCmd());
      }

      if (ccmd.getHandleCmd() != null) {
         this.handleHandleCmdComplete(ccmd.getHandleCmd(), ccmd.isSucceeded());
      }
   }

   public void handleError(PrintCmd err, PrintErrorEvent pe) {
      if (this.traceOn()) {
         this.trace("handleError " + err);
      }

      if (err == null) {
         this.inputQ.processErrorResponse(null, (byte)0);
      } else {
         this.filter.reset();
         err.setSucceeded(false);
         err.setRetry(true);
         if (pe.getOffender().isAsyncCmd()) {
            this.sendErrorEvent(pe);
         } else {
            DefaultPrinterResult res = new DefaultPrinterResult();
            res.setInError(true);
            res.setPrintErrorEvent(pe);
            pe.getOffender().setResult(res);
            ((DefaultPOSPrinterCmd)pe.getOffender()).forceComplete();
            this.inputQ.processErrorResponse(null, (byte)0);
         }
      }
   }

   public void cancelRotateMode() {
   }

   protected boolean isClearOutput() {
      return this.clearOutput;
   }

   protected DeviceInputQueue getInputQ() {
      return this.inputQ;
   }

   protected PrintCmdList getPendingList() {
      if (null == this.pendingList) {
         this.pendingList = this.createPrintCmdList();
      }

      return this.pendingList;
   }

   protected void submitDefaultCmd(DefaultPOSPrinterCmd dpc) {
      if (dpc.hasListCmds()) {
         if (this.traceOn()) {
            this.trace(Thread.currentThread().getName() + ">~submitDefaultCmd~" + dpc + "<");
         }

         this.submitList(dpc);
         if (this.traceOn()) {
            this.trace("<~submitDefaultCmd~" + dpc + ">");
         }
      } else {
         boolean adjusted1 = this.adjusted(dpc);
         if (adjusted1) {
            if (this.traceOn()) {
               this.trace("submitDefaultCmd adjust>> " + adjusted1);
            }

            this.submitList(dpc);
         } else {
            PrintCmd printCmd = dpc.getPrintCmd();
            this.finalSubmit(printCmd);
         }
      }
   }

   protected void handleHandleCmdComplete(POSPrinterCmd pcmd, boolean successful) {
      if (null == pcmd.getResult() || !(pcmd.getResult() instanceof DefaultPrinterResult)) {
         DefaultPrinterResult res = new DefaultPrinterResult();
         res.setInError(!successful);
         pcmd.setResult(res);
      }

      if (this.traceOn()) {
         this.trace("Notifying ->" + successful + " $ " + pcmd);
      }

      DefaultPOSPrinterCmd dpcmd = (DefaultPOSPrinterCmd)pcmd.getOwnerCmd();
      if (this.traceOn()) {
         this.getHandle().timeStamp().cmdComplete(pcmd);
      }

      boolean devInfo = false;
      boolean setBmpCmd = false;
      if (dpcmd.getCode() == 103) {
         devInfo = true;
         dpcmd.acceptCompletionVisitor(this.getCmdCompleteVisitor());
      } else if (dpcmd instanceof DefaultPOSPrinterCmd.SetBitmapCmd) {
         setBmpCmd = true;
         dpcmd.acceptCompletionVisitor(this.getCmdCompleteVisitor());
      }

      pcmd.setCompleted(true);
      if (dpcmd.isFullyComplete() && !devInfo && !setBmpCmd) {
         dpcmd.acceptCompletionVisitor(this.getCmdCompleteVisitor());
      }

      if (!successful) {
         dpcmd.forceComplete();
      }
   }

   protected void setPendingList(PrintCmdList l) {
      this.pendingList = l;
   }

   protected abstract PrinterHandleState createPrinterHandleState();

   protected abstract String getMinimumPropertyText();

   protected DefaultPOSPrinterHandle getHandle() {
      return this.handle;
   }

   protected void preAddToCmdList(PrintCmd cmd) {
   }

   protected void preSubmit(PrintCmdList l) {
   }

   protected void finalSubmit(PrintCmd pc) {
      if (null != pc.getHandleCmd() && pc.getHandleCmd().isCompleted()) {
         this.handleCmdComplete(pc);
      } else {
         if (this.traceOn()) {
            this.trace("-->finalSubmit " + pc);
         }

         this.addToPrintCmdList(pc);
         if (this.traceOn()) {
            this.trace("--<finalSubmit ");
         }
      }
   }

   protected void sendErrorEvent(PrintErrorEvent pee) {
      if (this.traceOn()) {
         this.trace("generateErrorEvent " + this.handle.toString());
      }

      PrintCmd pc = null;
      if (0 < this.handle.getEventHelper().getListenerCount()) {
         this.handle.getEventHelper().fireErrorEvent(pee);

         try {
            if (this.traceOn()) {
               this.trace("---->>>>>pee.waitForResponse() ");
            }

            pee.waitForResponse();
            if (this.traceOn()) {
               this.trace("<<<<<----pee.waitForResponse() ");
            }

            DefaultPOSPrinterCmd dpc = (DefaultPOSPrinterCmd)pee.getOffender();
            if (dpc != null) {
               pc = dpc.getPrintCmd();
               if (null != pc) {
                  pc.setParent(this);
                  if (pc.waitToFinish()) {
                     pc.setBlockKey(this.inputQ.getBlockKey());
                  }
               }
            }
         } catch (Exception var6) {
            if (this.traceOn()) {
               PrinterWriter.getTracer().print(var6);
            }
         }
      }

      if (this.traceOn()) {
         this.trace("generateErrorEvent process response " + pee.getResponse());
      }

      switch (pee.getResponse()) {
         case 0:
         case 3:
         default:
            this.inputQ.processErrorResponse(pc, (byte)0);
            break;
         case 1:
            this.inputQ.processErrorResponse(pc, (byte)1);
            break;
         case 2:
            DefaultPOSPrinterCmd.Factory f = (DefaultPOSPrinterCmd.Factory)this.handle.getPOSPrinterCmdFactory();

            try {
               DefaultPOSPrinterCmd reset = (DefaultPOSPrinterCmd)f.createPrinterResetCmd();
               pc = reset.getPrintCmd();
            } catch (Exception var5) {
               if (this.traceOn()) {
                  this.trace(var5);
               }
            }

            this.inputQ.processErrorResponse(pc, (byte)2);
            break;
         case 4:
            this.inputQ.processErrorResponse(pc, (byte)4);
      }
   }

   public DefaultPOSPrinterCmd.Factory getPrintCmdFactory() {
      return this.factory;
   }

   protected void submitList(DefaultPOSPrinterCmd dpc) {
      List l = dpc.getCmdList();
      this.slCount = false;
      if (this.traceOn()) {
         this.trace(">>submitList appended " + l.size() + " dpc-" + dpc);
      }

      int it = 0;
      DefaultPOSPrinterCmd temp = null;

      while (it < l.size()) {
         if (this.clearOutput) {
            if (this.traceOn()) {
               this.trace(">>submitList clearOutput? " + this.clearOutput);
            }

            if (!dpc.getOwnerCmd().isSettingsCmd()) {
               if (this.traceOn()) {
                  this.trace("sList clearOutput");
               }
               break;
            }
         }

         temp = (DefaultPOSPrinterCmd)l.get(it++);
         if (this.traceOn()) {
            this.trace("sList cmd--" + temp);
            this.trace(temp.print());
         }

         if (temp.hasListCmds() && temp != dpc) {
            this.submitDefaultCmd(temp);
            this.slCount = false;
         } else {
            boolean adjusted1 = this.adjusted(temp);
            if (adjusted1) {
               if (this.traceOn()) {
                  this.trace("sList adjust>> " + adjusted1);
               }

               this.submitList(temp);
               if (temp == dpc) {
                  break;
               }

               this.slCount = false;
            } else {
               PrintCmd printCmd = temp.getPrintCmd();
               this.slCount = false;
               this.finalSubmit(printCmd);
            }
         }
      }

      this.slCount = true;
      if (this.traceOn()) {
         this.trace("<<submitList ");
      }
   }

   protected void addToPrintCmdList(PrintCmd cmd) {
      if (cmd.getDataSize() <= 0) {
         cmd.setSucceeded(true);
         this.handleCmdComplete(cmd);
      } else {
         cmd.setParent(this);
         if (!cmd.waitToFinish() && !cmd.waitToStart()) {
            this.preAddToCmdList(cmd);
            if (!this.addToOutgoingList(cmd)) {
               if (this.traceOn()) {
                  this.trace("add2outgoing 2 failed? " + cmd);
               }
            }
         } else {
            if (cmd.waitToFinish()) {
               cmd.setBlockKey(this.inputQ.getBlockKey());
            }

            if (cmd.waitToStart() && this.getPendingList().hasCommands()) {
               this.writeSubmit();
            }

            this.preAddToCmdList(cmd);
            if (!this.addToOutgoingList(cmd) && this.traceOn()) {
               this.trace("add2outgoing 1 failed? " + cmd);
            }

            this.writeSubmit();
         }
      }
   }

   protected boolean addToOutgoingList(PrintCmd cmd) {
      if (this.isClearOutput() && !cmd.getHandleCmd().getOwnerCmd().isSettingsCmd()) {
         this.trace("failed clearOutput");
         return false;
      } else if (!this.getPendingList().addCommand(cmd)) {
         this.writeSubmit();
         this.preAddToCmdList(cmd);
         boolean added = this.getPendingList().addCommand(cmd);
         if (this.traceOn()) {
            this.trace("cmd added 2nd time " + added);
         }

         return added;
      } else {
         return true;
      }
   }

   protected void writeSubmit() {
      if (this.clearOutput) {
         this.getPendingList().recycle();
         this.setPendingList(this.createPrintCmdList());
      } else {
         this.hasBeenAsked = false;
         if (!this.getPendingList().hasCommands()) {
            if (this.traceOn()) {
               this.trace("writesubmit failed");
            }
         } else {
            this.preSubmit(this.getPendingList());
            PrintCmdList pcl = this.getPendingList();
            this.setPendingList(this.createPrintCmdList());
            pcl.setParent(this);
            if (this.traceOn()) {
               this.trace("writesubmit pcl " + pcl + " pcl.getDataSize: " + pcl.getDataSize());
            }

            if (this.getTracer().isOn()) {
               this.getHandle().timeStamp().addToDevQ(pcl, pcl.getDataSize());
            }

            if (this.m_immediateMode) {
               this.inputQ.addImmediateCmdList(pcl);
            } else {
               this.inputQ.addCmdList(pcl);
            }
         }
      }
   }

   protected Tracer getTracer() {
      return tracer;
   }

   protected boolean traceOn() {
      return tracer.isOn();
   }

   protected void trace(String msg) {
      this.preTrace(1 + msg.length());
      this.getTracer().println(" " + msg);
   }

   protected void trace(String methodName, String msg) {
      this.preTrace(methodName.length() + 3 + msg.length());
      this.traceBuffer.append('.').append(methodName).append(' ').append(msg);
      this.getTracer().println(this.traceBuffer.toString());
   }

   protected void trace(Exception e) {
      this.getTracer().print(e);
   }

   private void preTrace(int len) {
      if (null == this.traceBuffer) {
         this.traceBuffer = new StringBuffer(len);
      } else {
         this.traceBuffer.delete(0, this.traceBuffer.length());
         this.traceBuffer.ensureCapacity(len);
      }
   }

   public void setPrinterStateId(int printerType, int printerId) {
      IBMPrinterState ibmState = (IBMPrinterState)this.getPrinterHandleState();
      String busName = this.getHandle().getDevBus().getName();
      switch (printerType) {
         case 1:
            ibmState.setPrinterType(3804);
            ibmState.setPrinterID(3836);
            break;
         case 2:
            ibmState.setPrinterType(3804);
            ibmState.setPrinterID(3836);
            break;
         case 48:
            ibmState.setPrinterType(3801);
            if (busName.equals("USB")) {
               this.printerId4610USB(printerId);
            } else if (busName.equals("RS485")) {
               this.printerId4610Rs485(printerId);
            } else if (busName.equals("RS232")) {
               this.printerId4610Rs232(printerId);
            }
            break;
         case 49:
            ibmState.setPrinterType(3802);
            if (busName.equals("USB")) {
               this.printerId4610USB(printerId);
            } else if (busName.equals("RS485")) {
               this.printerId4610Rs485(printerId);
            } else if (busName.equals("RS232")) {
               this.printerId4610Rs232(printerId);
            }
            break;
         case 50:
            ibmState.setPrinterType(3803);
            if (busName.equals("USB")) {
               this.printerId4689USB(printerId);
            } else if (busName.equals("RS485")) {
               this.printerId4689Rs485(printerId);
            }
      }
   }

   private void printerId4610Rs485(int printerId) {
      IBMPrinterState ibmState = (IBMPrinterState)this.getPrinterHandleState();
      switch (printerId) {
         case 0:
            ibmState.setPrinterID(3819);
            break;
         case 1:
            ibmState.setPrinterID(3820);
            break;
         case 2:
            ibmState.setPrinterID(3821);
            break;
         case 3:
            ibmState.setPrinterID(3823);
            break;
         case 4:
            ibmState.setPrinterID(3824);
            break;
         case 5:
            ibmState.setPrinterID(3825);
         case 6:
         case 8:
         default:
            break;
         case 7:
            ibmState.setPrinterID(3826);
            break;
         case 9:
            ibmState.setPrinterID(3822);
            break;
         case 10:
            ibmState.setPrinterID(3838);
      }
   }

   private void printerId4610USB(int printerId) {
      IBMPrinterState ibmState = (IBMPrinterState)this.getPrinterHandleState();
      switch (printerId) {
         case 1:
            ibmState.setPrinterID(3827);
            break;
         case 2:
            ibmState.setPrinterID(3828);
            break;
         case 3:
            ibmState.setPrinterID(3830);
            break;
         case 4:
            ibmState.setPrinterID(3831);
            break;
         case 5:
            ibmState.setPrinterID(3832);
         case 6:
         case 8:
         default:
            break;
         case 7:
            ibmState.setPrinterID(3833);
            break;
         case 9:
            ibmState.setPrinterType(3802);
            ibmState.setPrinterID(3829);
            break;
         case 10:
            ibmState.setPrinterType(3802);
            ibmState.setPrinterID(3837);
      }
   }

   private void printerId4610Rs232(int printerId) {
      IBMPrinterState ibmState = (IBMPrinterState)this.getPrinterHandleState();
      switch (printerId) {
         case 0:
            ibmState.setPrinterID(3811);
            break;
         case 1:
            ibmState.setPrinterID(3812);
            break;
         case 2:
            ibmState.setPrinterID(3813);
            break;
         case 3:
            ibmState.setPrinterID(3815);
            break;
         case 4:
            ibmState.setPrinterID(3816);
            break;
         case 5:
            ibmState.setPrinterID(3817);
         case 6:
         case 8:
         default:
            break;
         case 7:
            ibmState.setPrinterID(3818);
            break;
         case 9:
            ibmState.setPrinterType(3802);
            ibmState.setPrinterID(3814);
            break;
         case 10:
            ibmState.setPrinterType(3802);
            ibmState.setPrinterID(3839);
      }
   }

   private void printerId4689Rs485(int printerId) {
      if (printerId == 17) {
         IBMPrinterState ibmState = (IBMPrinterState)this.getPrinterHandleState();
         ibmState.setPrinterID(3834);
      }
   }

   private void printerId4689USB(int printerId) {
      if (printerId == 17) {
         IBMPrinterState ibmState = (IBMPrinterState)this.getPrinterHandleState();
         ibmState.setPrinterID(3835);
      }
   }

   class ImpSubmissionPolicy implements SubmissionPolicy {
      public ImpSubmissionPolicy() {
         try {
            String min = PosSystemManager.getInstance().getProperties().getPropertyString(IBMPrinterImp.this.getMinimumPropertyText());
            int mini = Integer.valueOf(min);
            mini = mini > IBMPrinterImp.this.getMaxCmdLen() ? IBMPrinterImp.this.getMaxCmdLen() : mini;
            this.setMinimumData(mini);
            this.setMaxData(IBMPrinterImp.this.getMaxCmdLen());
         } catch (Exception var4) {
            this.setMinimumData(IBMPrinterImp.this.getMaxCmdLen());
         }
      }

      public PrintCmdList askForData() {
         if (IBMPrinterImp.this.processing) {
            IBMPrinterImp.this.hasBeenAsked = true;
            return null;
         } else {
            IBMPrinterImp.this.ready.set(false);

            PrintCmdList pcl;
            try {
               if (null == IBMPrinterImp.this.getPendingList() || !IBMPrinterImp.this.getPendingList().hasCommands()) {
                  return null;
               }

               if (!IBMPrinterImp.this.isClearOutput()) {
                  pcl = null;
                  IBMPrinterImp.this.preSubmit(IBMPrinterImp.this.getPendingList());
                  if (IBMPrinterImp.this.traceOn()) {
                     IBMPrinterImp.this.trace("Successfully ask for data " + IBMPrinterImp.this.getPendingList());
                  }

                  pcl = IBMPrinterImp.this.getPendingList();
                  IBMPrinterImp.this.setPendingList(null);
                  pcl.setParent(IBMPrinterImp.this);
                  IBMPrinterImp.this.hasBeenAsked = false;
                  return pcl;
               }

               IBMPrinterImp.this.getPendingList().recycle();
               IBMPrinterImp.this.setPendingList(IBMPrinterImp.this.createPrintCmdList());
               pcl = null;
            } catch (Exception var6) {
               return null;
            } finally {
               IBMPrinterImp.this.ready.set(true);
            }

            return pcl;
         }
      }

      public void setMaxData(int max) {
         IBMPrinterImp.this.maximumData = max;
      }

      public void setMinimumData(int min) {
         IBMPrinterImp.this.minimumData = min;
      }
   }

   protected class PostDaemon extends AbstractActiveObject {
      private List list = Collections.synchronizedList(new ArrayList(4));
      BooleanMonitor d = new BooleanMonitor(false);
      boolean flag = false;
      boolean clear = false;

      public boolean isEmpty() {
         return this.list.isEmpty();
      }

      public synchronized void post(HandleCmd a, boolean settings) {
         this.flag = this.isEmpty();
         this.list.add(new IBMPrinterImp.PostDaemon.Wrap(a, settings));
         if (this.flag) {
            this.d.set(true);
         }
      }

      public synchronized void postImmediate(HandleCmd a, boolean settings) {
         this.flag = this.isEmpty();
         this.list.add(0, new IBMPrinterImp.PostDaemon.Wrap(a, settings));
         if (this.flag) {
            this.d.set(true);
         }
      }

      public void clearOutput() {
         if (IBMPrinterImp.this.getTracer().isOn()) {
            IBMPrinterImp.this.getTracer().println("clearOutput ");
         }

         this.list.clear();
      }

      public String toString() {
         return this.list.toString();
      }

      protected void runActiveObject() {
         while (true) {
            if (this.isEmpty()) {
               try {
                  this.d.set(false);
                  this.d.waitForTrue(3250);
               } catch (Exception var3) {
               }
            }

            try {
               if (!this.isEmpty()) {
                  IBMPrinterImp.PostDaemon.Wrap x = null;
                  x = (IBMPrinterImp.PostDaemon.Wrap)this.list.remove(0);
                  if (null != x.cmd) {
                     IBMPrinterImp.this.submit(x.cmd);
                  }
               }
            } catch (Exception var2) {
               IBMPrinterImp.this.getTracer().print(var2);
            }
         }
      }

      private class Wrap {
         boolean settings = false;
         Object cmd = null;

         public Wrap(Object x, boolean y) {
            this.settings = y;
            this.cmd = x;
         }

         public String toString() {
            return null == this.cmd ? "null" : this.cmd.toString();
         }
      }
   }
}
