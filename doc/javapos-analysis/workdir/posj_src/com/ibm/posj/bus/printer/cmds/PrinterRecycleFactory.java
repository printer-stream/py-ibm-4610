package com.ibm.posj.bus.printer.cmds;

import com.ibm.jutil.patterns.factory.AbstractPoolFactory;
import com.ibm.jutil.patterns.factory.RecycleFactory.CreateMethod;
import java.util.ConcurrentModificationException;

public class PrinterRecycleFactory extends AbstractPoolFactory {
   public static final int INIT_FACTORY_SIZE = 10;
   public static final int INCREMENTAL_SIZE = 2;

   public PrinterRecycleFactory(CreateMethod method) {
      super(method, 10, 2, 0.5F);
   }

   public PrintCmd getNewPrintCmd() {
      PrintCmd ret = null;

      while (null == ret) {
         try {
            ret = (PrintCmd)this.takeFromPool();
         } catch (ConcurrentModificationException var3) {
         }
      }

      return ret;
   }
}
