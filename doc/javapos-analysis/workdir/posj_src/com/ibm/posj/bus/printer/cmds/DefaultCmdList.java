package com.ibm.posj.bus.printer.cmds;

import com.ibm.jutil.patterns.factory.AbstractPoolFactory;
import com.ibm.jutil.patterns.factory.RecyclableObject;
import com.ibm.jutil.patterns.factory.RecycleFactory;
import com.ibm.jutil.patterns.factory.RecycleFactoryException;
import com.ibm.jutil.patterns.factory.RecyclableObject.CtorArg;
import com.ibm.jutil.patterns.factory.RecycleFactory.CreateMethod;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.printer.CmdCompleteStatusVisitor;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.StatusVisitor;
import java.util.List;

public class DefaultCmdList extends PrintCmdList {
   private static PrintCmdList.Factory pclFactory = new DefaultCmdList.DefaultFactory();

   protected DefaultCmdList(int maxLen) {
      super(maxLen);
   }

   public String getName() {
      return "IBMDefaultCmdList";
   }

   public static PrintCmdList.Factory getFactory() {
      return pclFactory;
   }

   public RecycleFactory getRecycleFactory() {
      return getFactory();
   }

   public void accept(CmdCompleteStatusVisitor ccsv) {
      Tracer tracer = PrinterWriter.getTracer();
      List list = this.getCmdList();
      if (0 > list.size()) {
         if (tracer.isOn()) {
            tracer.println(3, "DCL-> visit empty");
         }

         ccsv.succeeded();
      } else {
         boolean flag = true;
         int base = list.size();

         int x;
         for (x = 0; x < list.size(); x++) {
            PrintCmd tcmd = (PrintCmd)list.get(x);
            DefaultPOSPrinterCmd cmd = (DefaultPOSPrinterCmd)tcmd.getHandleCmd();
            if (tracer.isOn()) {
               tracer.println(3, "DCL->" + cmd);
            }

            if (null == cmd) {
               list.remove(tcmd);
               tcmd.recycle();
               x--;
            } else if (cmd.isPartiallyComplete()) {
               if (tracer.isOn()) {
                  tracer.println(3, "DCL-> partial continue");
               }
            } else {
               flag = false;
               cmd.accept((StatusVisitor)ccsv);
               if (!ccsv.isSuccessful()) {
                  break;
               }

               if (list.size() != base) {
                  base = list.size();
                  x = -1;
               }
            }
         }

         if (flag) {
            if (tracer.isOn()) {
               tracer.println(3, "DCL-> flag never set");
            }

            ccsv.succeeded();
         }

         this.completeIdx = x;
      }
   }

   private static class CmdMaker implements CreateMethod {
      private CmdMaker() {
      }

      public RecyclableObject newRecyclableObject(RecycleFactory recycleFactory) {
         return new DefaultCmdList(1);
      }

      public RecyclableObject newRecyclableObject(CtorArg[] ctorArgs, RecycleFactory recycleFactory) throws RecycleFactoryException {
         return new DefaultCmdList(1);
      }
   }

   public static class DefaultFactory extends AbstractPoolFactory implements PrintCmdList.Factory {
      public DefaultFactory() {
         super(new DefaultCmdList.CmdMaker(), 20);
      }

      public PrintCmdList createPrintCmdList(int maxLen) {
         PrintCmdList p = null;

         while (null == p) {
            try {
               p = (PrintCmdList)super.takeFromPool();
            } catch (Exception var4) {
            }
         }

         p.setMaxLength(maxLen);
         return p;
      }
   }
}
