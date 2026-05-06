package com.ibm.posj.printer;

import com.ibm.jutil.JUtilException;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SubmissionContainer {
   List internalList = Collections.synchronizedList(new ArrayList(5));

   public int size() {
      return this.internalList.size();
   }

   public String toString() {
      try {
         return "\n\tSentQ - " + this.internalList.toString();
      } catch (Exception var2) {
         return "Exception";
      }
   }

   public void removeList(PrintCmdList cmd) {
      this.internalList.remove(cmd);
   }

   public Iterator iterator() {
      return this.internalList.iterator();
   }

   public void addToContainer(PrintCmdList cmd) {
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("Adding 2 sq " + cmd);
      }

      this.internalList.add(cmd);
   }

   public PrintCmdList getLastSent() {
      try {
         PrintCmdList last = null;

         do {
            if (null != last) {
               this.internalList.remove(last);
            }

            last = (PrintCmdList)this.internalList.get(this.internalList.size() - 1);
         } while (!last.hasCommands());

         return last;
      } catch (Exception var2) {
         return null;
      }
   }

   public void format() {
      if (!this.internalList.isEmpty()) {
         try {
            for (int i = 0; i < this.internalList.size(); i++) {
               PrintCmdList list = (PrintCmdList)this.internalList.get(i);
               if (list.isFullyComplete()) {
                  this.internalList.remove(list);
                  list.recycle();
               }
            }
         } catch (Exception var3) {
         }
      }
   }

   public PrintCmdList getPendingCmdList() {
      if (this.internalList.isEmpty()) {
         return null;
      } else {
         try {
            return (PrintCmdList)this.internalList.get(0);
         } catch (Exception var2) {
            return null;
         }
      }
   }

   public PrintCmdList removePendingCmdList() {
      if (this.internalList.isEmpty()) {
         return null;
      } else {
         PrintCmdList ret = null;

         try {
            return (PrintCmdList)this.internalList.remove(0);
         } catch (Exception var3) {
            return null;
         }
      }
   }

   public PrintCmdList getNextCmdListToComplete() {
      return this.getCmdListToComplete(0);
   }

   public PrintCmdList getCmdListToComplete(int offset) {
      int posi = offset;
      if (!this.internalList.isEmpty() && offset < this.internalList.size()) {
         PrintCmdList item = null;

         try {
            item = (PrintCmdList)this.internalList.get(posi);
         } catch (Exception var5) {
            return null;
         }

         while (true) {
            try {
               if (!item.isPartialComplete()) {
                  return item;
               }

               if (item.isFullyComplete()) {
                  this.internalList.remove(item);
                  posi--;
               }

               if (posi + 1 >= this.internalList.size()) {
                  return null;
               }

               item = (PrintCmdList)this.internalList.get(++posi);
               if (null == item) {
                  return null;
               }
            } catch (Exception var6) {
               if (PrinterWriter.getTracer().isOn()) {
                  PrinterWriter.getTracer().print(var6);
               }

               return null;
            }
         }
      } else {
         return null;
      }
   }

   public void resendCmds() throws JUtilException {
      while (!this.internalList.isEmpty()) {
         PrintCmdList pcl = (PrintCmdList)this.internalList.remove(this.internalList.size() - 1);
         if (!pcl.hasCommands()) {
            pcl.recycle();
         } else {
            if (pcl.isFullyComplete()) {
               return;
            }

            pcl.reset();
            RetryInputQueue riq = (RetryInputQueue)pcl.getParent();
            riq.retryOutput(pcl);
         }
      }
   }
}
