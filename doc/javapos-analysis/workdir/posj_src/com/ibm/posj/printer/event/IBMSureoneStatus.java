package com.ibm.posj.printer.event;

import com.ibm.jutil.Util;
import com.ibm.jutil.patterns.factory.RecyclableObject;
import com.ibm.jutil.patterns.factory.RecycleFactory;
import com.ibm.jutil.patterns.factory.RecycleFactoryException;
import com.ibm.jutil.patterns.factory.RecyclableObject.CtorArg;
import com.ibm.jutil.patterns.factory.RecycleFactory.CreateMethod;
import com.ibm.posj.SureonePrinterConst;
import com.ibm.posj.printer.PrinterWriter;

public class IBMSureoneStatus extends PrintStatus implements SureonePrinterConst {
   private static PrintStatus.PrintStatusFactory factory = null;

   public IBMSureoneStatus(Object src, byte[] a) {
      super(src, a);
   }

   public String toString() {
      return "SureoneStatus@" + this.hashCode() + "-" + Util.toFormatedHexString(this.getData());
   }

   public Object clone() {
      return getPrintStatusFactory(this.getSource()).createPrintStatus(this.getData(), this.getLogHelper());
   }

   public void updateStatus() {
      byte[] data = super.getData();
      this.parseByte1(data[0]);
   }

   public CtorArg createCtorArg(String name, Object value) throws IllegalArgumentException {
      throw new IllegalArgumentException("Not supported");
   }

   private void parseByte1(byte data) {
      if ((data & this.extractStatusID(260)) != 0) {
         this.log(3014, 3, "Receipt cover is open or no paper is present");
         this.setStatus(5, true);
         this.turnErrorOn(2, (byte)2);
         this.cacheError((byte)2, (byte)2, (short)1);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("CR Open!!!!!");
         }
      }

      if ((data & this.extractStatusID(288)) != 0) {
         this.setStatus(9, true);
         this.turnCmdCompleteOn();
         this.setReady(true);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Cmd Loaded/complete");
         }
      }

      if ((data & this.extractStatusID(264)) != 0) {
         this.setStatus(0, true);
         this.turnErrorOn(2, (byte)2);
         this.cacheError((byte)2, (byte)2, (short)8);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Rec empty ERROR!!!!!");
         }
      }
   }

   public static PrintStatus.PrintStatusFactory getPrintStatusFactory(Object src) {
      if (null == factory) {
         factory = new PrintStatus.PrintStatusFactory(new IBMSureoneStatus.Creator(src));
      }

      return factory;
   }

   public boolean isSlipPaperPresent() {
      return false;
   }

   public boolean isSlipNonPaperError() {
      return false;
   }

   public RecycleFactory getRecycleFactory() {
      return getPrintStatusFactory(this.getSource());
   }

   public static class Creator implements CreateMethod {
      Object src = null;

      public Creator(Object src) {
         this.src = src;
      }

      public RecyclableObject newRecyclableObject(RecycleFactory recycleFactory) {
         return new IBMSureoneStatus(this.getSrc(), PrintStatus.PrintStatusFactory.dummy);
      }

      public RecyclableObject newRecyclableObject(CtorArg[] ctorArgs, RecycleFactory recycleFactory) throws RecycleFactoryException {
         throw new RecycleFactoryException("Parameters not supported");
      }

      public Object getSrc() {
         return this.src;
      }
   }
}
