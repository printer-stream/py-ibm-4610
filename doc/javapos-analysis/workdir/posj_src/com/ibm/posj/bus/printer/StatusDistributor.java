package com.ibm.posj.bus.printer;

import com.ibm.jutil.tasks.SubmitTaskScheduler.Submitter;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.PrintStatus;

public class StatusDistributor implements Submitter {
   private int minLength = 2;
   StatusDistributor.Distributee recver = null;

   public StatusDistributor(StatusDistributor.Distributee d, byte[] data) {
      this.recver = d;
   }

   public void submit(Object o) {
      this.run((byte[])o);
   }

   public void setMinimumLength(int l) {
      this.minLength = l;
   }

   public void run(byte[] data) {
      PrintStatus ps = null;
      if (this.minLength <= data.length) {
         try {
            ps = this.recver.createPrintStatus(data);
         } catch (Exception var4) {
            PrinterWriter.getTracer().print(var4);
         }

         if (null != ps) {
            if (PrinterWriter.getTracer().isOn()) {
               PrinterWriter.getTracer().print("Status Post <- " + ps);
            }

            try {
               this.recver.distributeStatus(ps);
            } catch (Exception var5) {
               if (PrinterWriter.getTracer().isOn()) {
                  PrinterWriter.getTracer().print(var5);
               }
            }
         }
      }
   }

   public interface Distributee {
      PrintStatus createPrintStatus(byte[] var1);

      void distributeStatus(PrintStatus var1);
   }
}
