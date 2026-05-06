package com.ibm.posj.bus.printer.cmds;

import com.ibm.jutil.patterns.factory.RecyclableObject;
import com.ibm.jutil.patterns.factory.RecycleFactory;
import com.ibm.jutil.patterns.factory.RecycleFactoryException;
import com.ibm.jutil.patterns.factory.RecyclableObject.CtorArg;
import com.ibm.jutil.patterns.factory.RecycleFactory.CreateMethod;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import java.util.ConcurrentModificationException;

public abstract class PrintCmdFactory {
   private int maxSize = 0;
   private static PrinterRecycleFactory rFactory = null;
   private byte id = 1;

   public PrintCmdFactory(int CommandCnt, int maxSize) {
      if (rFactory == null) {
         rFactory = new PrinterRecycleFactory(new PrintCmdFactory.Creator(this));
      }

      this.maxSize = maxSize;
   }

   public abstract PrintCmdList createPrintCmdList();

   public abstract AbstractByteCmd getCmdBytes();

   public int getMaxCmdLen() {
      return this.maxSize;
   }

   public PrinterRecycleFactory getBaseFactory() {
      return rFactory;
   }

   public abstract void setCmdsStation(PrintCmd var1, byte var2);

   public PrintCmd createCmd(POSPrinterCmd ocmd) {
      DefaultPOSPrinterCmd cmd = (DefaultPOSPrinterCmd)ocmd;
      if (cmd != null && !cmd.isPrintCmdNull()) {
         PrintCmd ret = cmd.getPrintCmd();
         ret.setBytes(cmd.getOutboundPacket());
         return ret;
      } else {
         return this.createCmd();
      }
   }

   public byte generateID() {
      if (this.id == 0) {
         this.id++;
      }

      return this.id++;
   }

   public PrintCmd createCmd() {
      PrintCmd ret = null;

      while (null == ret) {
         try {
            ret = rFactory.getNewPrintCmd();
         } catch (ConcurrentModificationException var3) {
         }
      }

      ret.setMaxSize(this.maxSize);
      return ret;
   }

   public void recycle(RecyclableObject cmd) {
      if (cmd instanceof PrintCmd) {
         rFactory.recycle(cmd);
      }
   }

   public abstract GeneralCmdFactory getGeneralFactory();

   public abstract GraphicCmdFactory getGraphicFactory();

   public abstract FontCmdFactory getFontFactory();

   class Creator implements CreateMethod {
      PrintCmdFactory factory = null;

      Creator(PrintCmdFactory f) {
         this.factory = f;
      }

      public RecyclableObject newRecyclableObject(CtorArg[] ctorArgs, RecycleFactory recycleFactory) throws RecycleFactoryException {
         throw new RecycleFactoryException("Command not Supported");
      }

      public RecyclableObject newRecyclableObject(RecycleFactory recycleFactory) {
         return new PrintCmd(this.factory);
      }
   }
}
