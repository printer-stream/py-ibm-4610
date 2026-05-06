package com.ibm.posj.printer;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;
import com.ibm.posj.printer.event.PrintErrorEvent;
import java.util.ArrayList;
import java.util.List;

public class DefaultDeviceInputQueue implements RetryInputQueue {
   private short outCnt = 0;
   private PrintCmd saved_errCmd = null;
   private PrintErrorEvent saved_event = null;
   private List tempErrLst = null;
   private boolean errorPending = false;
   private SubmissionPolicy policy = null;
   private PrinterWriter.WriterInputQueue masterQ = null;
   private PrinterWriter.PrinterWriterUser user = null;
   private List immediatQ = new ArrayList(3);
   private List pendingQueue = new ArrayList(10);
   private String tracerName = null;
   private Tracer tracer = null;
   private StringBuffer traceBuffer = null;

   public DefaultDeviceInputQueue(PrinterWriter.PrinterWriterUser user, String deviceName) {
      this.user = user;
      this.tracerName = deviceName + "-DevInputQueue";
      this.tracer = TracerFactory.getInstance().createTracer(this.tracerName);
   }

   public String toString() {
      return this.tracerName;
   }

   public String toFullString() {
      StringBuffer sb = new StringBuffer(300);
      sb.append(this.tracerName).append("\n\timmed ").append(this.immediatQ.toString());
      sb.append("\n\t").append(this.getPendingQueue().toString());
      return sb.toString();
   }

   public void addCmdList(PrintCmdList p) {
      if (this.traceOn()) {
         this.trace("add ", p.toString());
      }

      if (p.hasCommands()) {
         this.getPendingQueue().add(p);
         if (p.isOutputList()) {
            this.outCnt++;
         }

         this.getMasterQ().flagQueue();
      }
   }

   public void addImmediateCmdList(PrintCmdList p) {
      if (this.traceOn()) {
         this.trace("immediate add " + p);
      }

      if (p.hasCommands()) {
         this.immediatQ.add(p);
         if (p.isOutputList()) {
            this.outCnt++;
         }

         this.getMasterQ().flagQueue();
      }
   }

   public void submit(PrintCmd pc) {
      if (pc.waitToFinish()) {
         pc.setBlockKey(this.getBlockKey());
      }

      PrintCmdList l = pc.wrapCmd();
      this.addCmdList(l);
   }

   public PrintCmdList getNextList() {
      int retry = 0;
      boolean exception = false;
      PrintCmdList nlst = null;

      do {
         try {
            if (this.traceOn()) {
               this.trace(
                  "--> getNextList immed "
                     + this.immediatQ.size()
                     + "-"
                     + this.immediatQ
                     + "\n pending "
                     + this.getPendingQueue().size()
                     + "-"
                     + this.getPendingQueue()
               );
            }

            if (this.errorPending && this.immediatQ.isEmpty()) {
               return null;
            }

            if (this.immediatQ.isEmpty()) {
               if (this.getPendingQueue().isEmpty()) {
                  if (null != this.policy && this.doPolicy()) {
                     nlst = (PrintCmdList)this.getPendingQueue().remove(0);
                     if (nlst.isOutputList()) {
                        this.outCnt--;
                     }
                  }
               } else {
                  nlst = (PrintCmdList)this.getPendingQueue().remove(0);
                  if (nlst.isOutputList()) {
                     this.outCnt--;
                  }
               }
            } else {
               nlst = (PrintCmdList)this.immediatQ.remove(0);
               if (nlst.isOutputList()) {
                  this.outCnt--;
               }
            }

            if (null != nlst) {
               nlst.setParent(this);
            }

            exception = false;
         } catch (Exception var5) {
            if (this.traceOn()) {
               this.trace(var5);
            }

            exception = true;
            retry++;
         }
      } while (exception && retry < 11);

      return nlst;
   }

   public void processErrorResponse(PrintCmd errorCmd, byte response) {
      if (this.traceOn()) {
         this.trace("prcesserrorresponse<< " + this.getPendingQueue().size());
      }

      this.saved_errCmd = null;
      this.saved_event = null;
      switch (response) {
         case 1:
            if (this.traceOn()) {
               this.trace("retry error ");
            }

            this.errorPending = false;
            this.getMasterQ().flagQueue();
            break;
         case 2:
            if (this.traceOn()) {
               this.trace("reset error ");
            }
            break;
         case 3:
         default:
            if (this.traceOn()) {
               this.trace("Clear error?");
            }

            this.errorPending = false;
            if (null != errorCmd) {
               if (this.traceOn()) {
                  this.trace("Clear error " + errorCmd);
               }

               errorCmd.setSucceeded(false);
               if (null != errorCmd.getHandleCmd()) {
                  ((DefaultPOSPrinterCmd)errorCmd.getHandleCmd()).setEventHandler(null);
               }

               if (null != errorCmd.getParent()) {
                  errorCmd.getParent().handleCmdComplete(errorCmd);
               }

               return;
            }
            break;
         case 4:
            this.errorPending = false;
            errorCmd.setSucceeded(false);
            if (null != errorCmd.getParent()) {
               errorCmd.getParent().handleCmdComplete(errorCmd);
            }

            return;
      }
   }

   public void retryOutput(PrintCmdList err) {
      if (this.traceOn()) {
         this.trace(">>retryOutput " + err);
      }

      if (err.hasCommands()) {
         err.reset();
         this.addFirst(err);
         if (this.traceOn()) {
            this.trace("<<retryOutput");
         }
      }
   }

   public void handleError(PrintCmd err, PrintErrorEvent event) {
      this.errorPending = true;
      this.saved_errCmd = err;
      this.saved_event = event;
   }

   public void setInputQueue(PrinterWriter.WriterInputQueue q) {
      this.masterQ = q;
   }

   public void setSubmissionPolicy(SubmissionPolicy policy) {
      this.policy = policy;
   }

   public BooleanMonitor getBlockKey() {
      return this.getMasterQ().getBlockKey();
   }

   public boolean isEmpty() {
      return this.getPendingQueue().isEmpty() && this.immediatQ.isEmpty();
   }

   public boolean isDataPending() {
      if (this.outCnt < 0) {
         this.outCnt = 0;
      }

      return this.getMasterQ().isDataPending() || this.outCnt != 0;
   }

   public void execute() {
      this.errorPending = true;
      this.user.handleError(this.saved_errCmd, this.saved_event);
   }

   protected void addFirst(PrintCmdList c) {
      if (c.isOutputList()) {
         this.outCnt++;
      }

      if (c.isImmediate()) {
         if (this.immediatQ.isEmpty()) {
            this.immediatQ.add(c);
         } else {
            this.immediatQ.add(0, c);
         }

         this.getMasterQ().flagQueue();
      } else {
         if (this.getPendingQueue().isEmpty()) {
            this.getPendingQueue().add(c);
         } else {
            this.getPendingQueue().add(0, c);
         }

         if (null == this.saved_errCmd) {
            this.getMasterQ().flagQueue();
         }
      }
   }

   protected PrinterWriter.WriterInputQueue getMasterQ() {
      return this.masterQ;
   }

   protected List getPendingQueue() {
      return this.pendingQueue;
   }

   protected void incrementOutCnt() {
      this.outCnt++;
   }

   private boolean doPolicy() {
      if (null == this.policy) {
         return false;
      } else {
         PrintCmdList ask = this.policy.askForData();
         if (null != ask && ask.hasCommands()) {
            this.addCmdList(ask);
            return true;
         } else {
            return false;
         }
      }
   }

   protected Tracer getTracer() {
      return this.tracer;
   }

   protected boolean traceOn() {
      return this.tracer.isOn();
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
}
