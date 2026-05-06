package com.ibm.posj.printer;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;
import com.ibm.posj.printer.event.PrintErrorEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.printer.ibm4610.IBM4610SlipCmdVisitor;

public class OutputProcessor {
   private SlipCmdVisitor slpCmdV = new IBM4610SlipCmdVisitor();
   private OutputProcessor.WriterInterface writer = null;
   private static Tracer tracer = TracerFactory.getInstance().createTracer("OutProc");

   public OutputProcessor(OutputProcessor.WriterInterface pw) {
      this.writer = pw;
   }

   public void processStatus(PrintStatus ps, PrintCmdList currentCmd) {
      if (this.traceOn()) {
         this.trace("proc statusevent<< error? " + ps.isHardwareError());
         this.trace("current cmd? " + currentCmd);
      }

      SubmissionContainer sentQueue = this.getWriter().getSentQueue();
      if (this.traceOn()) {
         this.trace("sq size " + sentQueue.size() + " cc " + currentCmd);
      }

      if (0 >= sentQueue.size() && null == currentCmd) {
         if (this.traceOn()) {
            this.trace("<<-- nothing to process -->");
         }
      } else {
         this.getWriter().getCmdLoadVisitor().setCurrentStatus(ps);
         this.getWriter().getCmdCompleteVisitor().setCurrentStatus(ps);
         if (this.traceOn()) {
            this.trace("CheckErrors " + ps.checkErrors());
         }

         if (null != currentCmd && currentCmd.hasCommands()) {
            currentCmd.accept(this.getWriter().getCmdLoadVisitor());
         }

         if (ps.checkErrors()) {
            this.errorTest(ps, currentCmd);
         }

         if (null != currentCmd) {
            if (!currentCmd.hasCommands()) {
               this.getWriter().forceLoad();
               this.getWriter().sendNext();
            } else {
               if (this.traceOn()) {
                  this.trace("sendNext??? " + !this.getWriter().getErrorPending() + " succeed? " + this.getWriter().getCmdLoadVisitor().isSuccessful());
               }

               if (this.getWriter().getCmdLoadVisitor().isSuccessful() && !this.getWriter().getErrorPending()) {
                  this.getWriter().sendNext();
               }
            }
         }

         this.getWriter().getCmdLoadVisitor().reset();
         if (0 < this.getWriter().getSentQueue().size()) {
            if (this.traceOn()) {
               this.trace(">>checkcmdcomplete");
            }

            this.checkCmdComplete(ps);
            if (this.traceOn()) {
               this.trace("<<checkcmdcomplete");
            }
         }
      }
   }

   public PrintErrorEvent findErrorByStation(PrintStatus status, PrintCmdList currentCmd) {
      if (this.traceOn()) {
         this.trace(">>febs ");
      }

      PrintErrorEvent err = null;
      if (!status.checkErrors()) {
         return null;
      } else {
         int i = 0;
         PrintCmdList pcl = null;

         while (null == err) {
            pcl = this.getWriter().getSentQueue().getCmdListToComplete(i++);
            if (pcl == null) {
               break;
            }

            err = status.getCachedError(pcl.getUsedStations());
         }

         if (null == err) {
            err = status.getCachedError(currentCmd.getUsedStations());
         }

         if (this.traceOn()) {
            this.trace("err found " + err);
         }

         return err;
      }
   }

   protected void checkCmdComplete(PrintStatus lastStatus) {
      if (this.traceOn()) {
         this.trace("\t" + this.getWriter().getSentQueue().toString() + " " + lastStatus + " " + lastStatus.checkCmdComplete());
      }

      if (lastStatus.checkCmdComplete()) {
         PrintCmdList next = this.getWriter().getSentQueue().getNextCmdListToComplete();
         if (this.traceOn()) {
            this.trace("\t1.  find done " + next);
         }

         PrintCmdList tNext = null;
         int off = 0;

         while (null != next) {
            if (this.traceOn()) {
               this.trace("\tfind done " + next);
            }

            if (tNext == next) {
               if (this.getWriter().getSentQueue().size() <= 1) {
                  break;
               }

               next = this.getWriter().getSentQueue().getNextCmdListToComplete();
               if (null == next) {
                  continue;
               }
            }

            tNext = next;
            if (next.getCmdCount() == 1 && next.isInError()) {
               if (this.traceOn()) {
                  this.trace("isinError return");
               }

               return;
            }

            if (next.isPartialComplete()) {
               if (this.traceOn()) {
                  this.trace("partial continue");
               }

               next = this.getWriter().getSentQueue().getNextCmdListToComplete();
            } else {
               if (this.traceOn()) {
                  this.trace("cmdcomplete visit " + next);
               }

               next.accept(this.getWriter().getCmdCompleteVisitor());
               if (!this.getWriter().getCmdCompleteVisitor().isSuccessful()) {
                  break;
               }

               next = this.getWriter().getSentQueue().getNextCmdListToComplete();
            }
         }
      }

      this.getWriter().getSentQueue().format();
      this.getWriter().getCmdCompleteVisitor().reset();
   }

   private void errorTest(PrintStatus ps, PrintCmdList currentCmd) {
      PrintStatus tps = ps;
      if (this.traceOn()) {
         this.trace(">>errorTest " + currentCmd + " sw test " + ps.getSWError());
      }

      if (this.getWriter().getSentQueue().size() > 0) {
         if (null == ps.getSWError()) {
            try {
               SubmissionContainer sentQ = this.getWriter().getSentQueue();
               PrintCmdList pcl = sentQ.getNextCmdListToComplete();
               int i = 1;

               while (null == pcl.getResponsePending()) {
                  pcl = sentQ.getCmdListToComplete(i++);
                  if (this.traceOn()) {
                     this.trace("resp " + pcl);
                  }

                  if (null == pcl) {
                     break;
                  }
               }

               if (this.traceOn()) {
                  this.trace("errtest hwerror found " + pcl);
               }

               PrintErrorEvent cpee = this.extendedErrorTest(ps, currentCmd, pcl);
               if (null == cpee) {
                  tps = null;
               }
            } catch (Exception var8) {
               tps = null;
            }
         } else if (null != currentCmd) {
            currentCmd.setError(true);
         }

         if (null != tps) {
            this.trace("-->setError");
            this.getWriter().setError(tps);
            this.trace("<--setError");
         }
      }
   }

   private PrintErrorEvent extendedErrorTest(PrintStatus ps, PrintCmdList currentCmd, PrintCmdList nextList) {
      PrintCmd cmd = nextList.getResponsePending();
      POSPrinterCmd handleCmd = cmd.getHandleCmd();
      handleCmd.accept(this.slpCmdV);
      PrintErrorEvent e;
      if (this.slpCmdV.isSlpManipulator()) {
         if (ps.isSlipPaperPresent() && !ps.isSlipNonPaperError()) {
            e = null;
         } else {
            e = ps.getCachedError(handleCmd.getOwnerCmd().getStation());
         }
      } else {
         e = this.findErrorByStation(ps, currentCmd);
      }

      return e;
   }

   protected OutputProcessor.WriterInterface getWriter() {
      return this.writer;
   }

   private boolean traceOn() {
      return tracer.isOn();
   }

   private void trace(String msg) {
      tracer.println(" " + msg);
   }

   private void trace(String methodName, String msg) {
      tracer.println(methodName + " " + msg);
   }

   private void trace(Exception e) {
      tracer.print(e);
   }

   public interface WriterInterface {
      void forceLoad();

      CmdCompleteStatusVisitor getCmdCompleteVisitor();

      CmdLoadedStatusVisitor getCmdLoadVisitor();

      SubmissionContainer getSentQueue();

      void setError(PrintStatus var1);

      boolean getErrorPending();

      void sendNext();

      boolean isStatusPending();
   }
}
