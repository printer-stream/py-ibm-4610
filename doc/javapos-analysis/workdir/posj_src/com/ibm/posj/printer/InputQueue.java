package com.ibm.posj.printer;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.tasks.AbstractActiveObject;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;
import java.util.ArrayList;
import java.util.List;

public class InputQueue extends AbstractActiveObject implements PrinterWriter.WriterInputQueue {
   private static final int WAIT_TIME = 12000;
   private short outCnt = 0;
   private InputReceiver writer = null;
   private BooleanMonitor send = new BooleanMonitor(true);
   private BooleanMonitor devQFlag = new BooleanMonitor(false);
   private int devIdx = 0;
   private List deviceQ = new ArrayList(4);
   private List pendingQueue = new ArrayList(10);
   private boolean waitToFinishFlag = false;
   private BooleanMonitor blockKey = new BooleanMonitor(false);
   private static Tracer tracer = TracerFactory.getInstance().createTracer("InputQueue");
   private StringBuffer traceBuffer = null;

   public InputQueue(InputReceiver iwriter) {
      this.writer = iwriter;
   }

   public String toString() {
      return this.pendingQueue.toString();
   }

   public boolean isStarted() {
      return super.isStarted();
   }

   public void start() {
      super.start();
   }

   public boolean isEmpty() {
      return this.pendingQueue.isEmpty();
   }

   public void quickRetry(PrintCmdList l) {
      if (l.isOutputList()) {
         this.outCnt++;
      }

      this.pendingQueue.add(0, l);
   }

   public void flagQueue() {
      if (this.traceOn()) {
         this.trace(">>flagQueue");
      }

      this.devQFlag.set(true);
   }

   public boolean isQueueFlagged() {
      return this.devQFlag.isTrue();
   }

   public void addDeviceQueue(DeviceInputQueue diq) {
      if (!this.deviceQ.contains(diq)) {
         this.deviceQ.add(diq);
      }
   }

   public void removeDeviceQueue(DeviceInputQueue diq) {
      if (this.deviceQ.contains(diq)) {
         this.deviceQ.remove(diq);
      }
   }

   public PrintCmdList getNextList() {
      PrintCmdList pcl = (PrintCmdList)this.pendingQueue.remove(0);
      if (pcl.isOutputList()) {
         this.outCnt--;
      }

      if (pcl.waitToFinish()) {
         if (this.traceOn()) {
            this.trace("set WTF " + pcl);
         }

         BooleanMonitor m = this.getBlockKey();
         this.waitToFinishFlag = true;
         m.set(true);
      }

      return pcl;
   }

   public void resendCmds() {
      if (this.traceOn()) {
         this.trace("-->resendCmds");
      }

      boolean sendT = this.send.isTrue();
      this.send.set(false);

      while (!this.pendingQueue.isEmpty()) {
         int x = this.pendingQueue.size() - 1;
         PrintCmdList lt = (PrintCmdList)this.pendingQueue.remove(x);
         if (lt.isOutputList()) {
            this.outCnt--;
         }

         RetryInputQueue riq = (RetryInputQueue)lt.getParent();
         riq.retryOutput(lt);
      }

      this.send.set(sendT);
      if (this.traceOn()) {
         this.trace("<--resendCmds " + sendT);
      }
   }

   public boolean isDataPending() {
      if (this.outCnt < 0) {
         this.outCnt = 0;
      }

      return this.outCnt > 0;
   }

   public PrintCmdList peekNextList() {
      return 0 >= this.pendingQueue.size() ? null : (PrintCmdList)this.pendingQueue.get(0);
   }

   public void removeList(PrintCmdList c) {
      if (c.isOutputList()) {
         this.outCnt--;
      }

      this.pendingQueue.remove(c);
   }

   public void sendNextCmd() {
      if (this.traceOn()) {
         this.trace(">>sendNextCmd()");
      }

      this.send.set(true);
      if (this.traceOn()) {
         this.trace("<<sendNextCmd()");
      }
   }

   public void writeNormalCmd() {
      PrintCmdList p33k = this.peekNextList();
      if (!p33k.hasCommands()) {
         this.removeList(p33k);

         try {
            p33k.recycle();
         } catch (Exception var4) {
         }
      } else {
         if (this.waitToFinishFlag) {
            if (this.traceOn()) {
               this.trace("IQ wtF " + p33k);
            }

            BooleanMonitor m = this.getBlockKey();

            try {
               m.waitForFalse(30000);
            } catch (RuntimeException var6) {
               return;
            }

            this.waitToFinishFlag = false;
            if (this.traceOn()) {
               this.trace("<<IQ wtF>>");
            }
         }

         if (p33k.waitToStart()) {
            if (this.traceOn()) {
               this.trace("IQ wts " + p33k);
            }

            BooleanMonitor m = this.writer.getIdleKey();

            try {
               m.waitForTrue(30000);
            } catch (RuntimeException var5) {
               return;
            }

            if (this.traceOn()) {
               this.trace("<<IQ wts>>");
            }
         }

         this.send.set(false);
         this.writer.writePrintCmds();
      }
   }

   public BooleanMonitor getBlockKey() {
      return this.blockKey;
   }

   protected void runActiveObject() {
      if (this.traceOn()) {
         this.trace("Starting inputQueue");
      }

      try {
         while (true) {
            while (!this.send.isTrue()) {
               try {
                  if (this.isQueueFlagged()) {
                     this.traverseDevices();
                  }

                  this.send.waitForTrue(12000);
                  break;
               } catch (Exception var4) {
               }
            }

            if (!this.pendingQueue.isEmpty()) {
               try {
                  if (this.traceOn()) {
                     this.trace(">>writeNormalCmd()<<");
                  }

                  this.writeNormalCmd();
                  if (this.traceOn()) {
                     this.trace("<<writeNormalCmd()>>");
                  }
               } catch (IndexOutOfBoundsException var3) {
               }
            } else if (!this.isQueueFlagged()) {
               try {
                  this.devQFlag.waitForTrue(12000);
               } catch (Exception var2) {
               }
            } else {
               this.traverseDevices();
            }
         }
      } catch (Exception var5) {
         if (this.traceOn()) {
            this.trace(var5);
         }

         if (this.traceOn()) {
            this.trace("Exiting execution loop!!!!!!!!!!!!!!!!!!");
         }
      }
   }

   private void traverseDevices() {
      if (this.traceOn()) {
         this.trace("-->traverseDevices " + this.deviceQ.size());
      }

      this.devQFlag.set(false);
      if (this.deviceQ.size() > 0) {
         if (this.devIdx >= this.deviceQ.size()) {
            this.devIdx = 0;
         }

         DeviceInputQueue diq = (DeviceInputQueue)this.deviceQ.get(this.devIdx++);
         PrintCmdList lst = null;

         DeviceInputQueue curr;
         do {
            if (this.deviceQ.size() <= this.devIdx) {
               this.devIdx = 0;
            }

            curr = (DeviceInputQueue)this.deviceQ.get(this.devIdx++);
            if (this.traceOn()) {
               this.trace("testing devQ " + curr);
            }

            while ((lst = curr.getNextList()) != null) {
               if (this.traceOn()) {
                  this.trace("using devQ " + curr);
               }

               this.addList(lst);
            }
         } while (diq != curr);
      }

      if (this.traceOn()) {
         this.trace("<--traverseDevices");
      }
   }

   private void addList(PrintCmdList l) {
      if (this.traceOn()) {
         this.trace("-->addList " + l);
      }

      if (l.hasCommands()) {
         if (l.isOutputList()) {
            this.outCnt++;
         }

         this.pendingQueue.add(l);
         if (this.traceOn()) {
            this.trace("<--addList " + l);
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
}
