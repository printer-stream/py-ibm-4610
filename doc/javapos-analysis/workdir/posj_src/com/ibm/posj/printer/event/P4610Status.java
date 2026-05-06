package com.ibm.posj.printer.event;

import com.ibm.jutil.Util;
import com.ibm.jutil.patterns.factory.RecyclableObject;
import com.ibm.jutil.patterns.factory.RecycleFactory;
import com.ibm.jutil.patterns.factory.RecycleFactoryException;
import com.ibm.jutil.patterns.factory.RecyclableObject.CtorArg;
import com.ibm.jutil.patterns.factory.RecycleFactory.CreateMethod;
import com.ibm.posj.IBM4610PrinterConst;
import com.ibm.posj.printer.PrinterWriter;

public class P4610Status extends PrintStatus implements IBM4610PrinterConst {
   private boolean slipPaper = false;
   private boolean slipNonPaperErr = false;
   private int lineCnt;
   private static PrintStatus.PrintStatusFactory factory = null;
   public static final int BITMASK = 255;
   public static int lastline = 0;

   public P4610Status(Object src, byte[] a) {
      super(src, a);
   }

   public String toString() {
      String head = "4610Status@" + Integer.toHexString(this.hashCode()) + "-";
      if (null != this.getData()) {
         head = head + Util.toFormatedHexString(this.getData());
      }

      return head;
   }

   public Object clone() {
      return getPrintStatusFactory(this.getSource()).createPrintStatus(this.getData(), this.getLogHelper());
   }

   public int getLineCnt() {
      return this.lineCnt;
   }

   public void updateStatus() {
      byte[] data = super.getData();
      this.slipNonPaperErr = false;
      this.slipPaper = false;
      if (data[0] == data[2] && data[5] == data[7] && data[0] == data[7] && data[0] == 1) {
         this.log(1010, 4, "");
         this.setStatus(16, true);
         this.turnErrorOn(-1, (byte)2);
         this.cacheError((byte)-1, (byte)2, (short)16);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Communication Error!");
         }
      } else {
         this.parseByte1(data[0]);
         this.parseByte2(data[1]);
         this.parseByte3(data[2]);
         this.parseByte4(data[3]);
         this.parseByte5(data[4]);
         this.parseByte6(data[5]);
         this.parseByte7(data[6]);
         this.parseByte8(data[7]);
      }
   }

   protected void setLineCnt(int x) {
      this.lineCnt = x;
      this.turnCmdCompleteOn();
      lastline = this.lineCnt;
   }

   public PrintErrorEvent getCachedError(byte station) {
      if (20 == station) {
         station = 4;
      }

      return super.getCachedError(station);
   }

   public boolean isSlipPaperPresent() {
      return this.slipPaper;
   }

   public boolean isSlipNonPaperError() {
      return this.slipNonPaperErr;
   }

   public CtorArg createCtorArg(String name, Object value) throws IllegalArgumentException {
      throw new IllegalArgumentException("Not supported");
   }

   private void parseByte1(byte data) {
      if ((data & this.extractStatusID(320)) != 0) {
         this.log(3014, 3, "Receipt cover is open or no paper is present");
         this.setStatus(5, true);
         this.setStatus(0, true);
         this.turnErrorOn(2, (byte)2);
         this.cacheError((byte)2, (byte)2, (short)1);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Receipt cover ERROR!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(257)) != 0) {
         this.setStatus(11, true);
         this.turnCmdCompleteOn();
         this.setReady(true);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Cmd Loaded");
         }
      }

      if ((data & this.extractStatusID(384)) != 0) {
         this.log(3001, 4, "");
         this.setStatus(12, true);
         this.turnErrorOn(-1, (byte)1);
         this.cacheError((byte)-1, (byte)1, (short)1);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Command Reject!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(288)) != 0) {
         this.slipNonPaperErr = true;
         this.setStatus(6, true);
         this.turnErrorOn(4, (byte)2);
         this.turnErrorOn(20, (byte)2);
         this.cacheError((byte)4, (byte)2, (short)2);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Slp ERROR!!!!!");
         }
      }
   }

   private void parseByte2(byte data) {
      if ((data & this.extractStatusID(513)) != 0) {
         this.setStatus(2, false);
         this.turnErrorOn(4, (byte)2);
         this.turnErrorOn(20, (byte)2);
         this.cacheError((byte)4, (byte)2, (short)16);
      } else {
         this.slipPaper = true;
         this.setStatus(2, true);
      }

      if ((data & this.extractStatusID(514)) != 0) {
         this.setStatus(4, false);
      } else {
         this.slipPaper = true;
         this.setStatus(4, true);
      }

      if ((data & this.extractStatusID(516)) != 0) {
         this.setStatus(3, false);
      } else {
         this.slipPaper = true;
         this.setStatus(3, true);
      }

      if ((data & this.extractStatusID(528)) != 0) {
      }

      if ((data & this.extractStatusID(576)) != 0) {
         this.setStatus(9, true);
         this.turnCmdCompleteOn();
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Buffer Empty ---");
         }
      } else {
         this.setStatus(9, false);
      }

      if ((data & this.extractStatusID(640)) != 0) {
         this.log(3016, 3, "");
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Buffer Full +++");
         }
      }
   }

   private void parseByte3(byte data) {
      if ((data & this.extractStatusID(769)) != 0) {
         this.turnErrorOn(-1, (byte)1);
         this.cacheError((byte)-1, (byte)1, (short)32);
      }

      if ((data & this.extractStatusID(770)) != 0) {
         this.log(3002, 4, "");
         this.turnErrorOn(2, (byte)2);
         this.cacheError((byte)4, (byte)2, (short)128);
         this.cacheError((byte)20, (byte)2, (short)128);
      }

      if ((data & this.extractStatusID(772)) != 0) {
         this.slipNonPaperErr = true;
         this.log(3008, 4, "");
         this.turnErrorOn(4, (byte)2);
         this.turnErrorOn(20, (byte)2);
         this.cacheError((byte)4, (byte)2, (short)16);
      }

      if ((data & this.extractStatusID(776)) != 0) {
         this.turnErrorOn(-1, (byte)1);
         this.cacheError((byte)-1, (byte)1, (short)4);
      }

      if ((data & this.extractStatusID(800)) != 0) {
         this.turnErrorOn(-1, (byte)1);
         this.cacheError((byte)-1, (byte)1, (short)4);
      }

      if ((data & this.extractStatusID(832)) != 0) {
         this.turnErrorOn(-1, (byte)1);
         this.cacheError((byte)-1, (byte)1, (short)4);
      }

      if ((data & this.extractStatusID(896)) != 0) {
         this.turnCmdCompleteOn();
      }
   }

   private void parseByte4(byte data) {
      this.setECLevel((byte)(255 & data));
   }

   private void parseByte5(byte data) {
      if ((data & this.extractStatusID(1282)) != 0) {
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("ECLEVELDATA");
         }

         this.turnCmdCompleteOn();
      }

      if ((data & this.extractStatusID(1281)) != 0) {
         super.setExtendedData(true);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("PRINTERID");
         }
      }

      if ((data & this.extractStatusID(1284)) != 0) {
         this.setStatus(10, true);
         super.setExtendedData(true);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("MICRDATA");
         }
      }

      if ((data & this.extractStatusID(1288)) != 0) {
         super.setExtendedData(true);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("MCTDATA");
         }
      }

      if ((data & this.extractStatusID(1296)) != 0) {
         super.setExtendedData(true);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("FLASH READ DATA");
         }
      }

      if ((data & this.extractStatusID(1344)) != 0) {
         super.setExtendedData(true);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Image Scan Done");
         }
      }

      if ((data & this.extractStatusID(1408)) != 0) {
         super.setExtendedData(true);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("ImageData");
         }
      }
   }

   private void parseByte6(byte data) {
      int newLnCnt = 255 & data;
      this.setLineCnt(newLnCnt);
   }

   private void parseByte7(byte data) {
      if ((data & this.extractStatusID(1284)) != 0) {
         this.turnErrorOn(2, (byte)1);
         this.cacheError((byte)2, (byte)1, (short)8);
      }

      if ((data & this.extractStatusID(1288)) != 0) {
         this.setStatus(13, false);
      } else {
         this.setStatus(13, true);
      }

      if ((data & this.extractStatusID(1296)) != 0) {
         this.log(3009, 2, "");
         this.setStatus(9, false);
      }

      if ((data & this.extractStatusID(1344)) != 0) {
      }

      if ((data & this.extractStatusID(1408)) != 0) {
         this.slipNonPaperErr = true;
         this.log(3012, 4, "");
         this.turnErrorOn(4, (byte)2);
         this.turnErrorOn(20, (byte)2);
         this.cacheError((byte)4, (byte)2, (short)0);
      }
   }

   private void parseByte8(byte data) {
      if ((data & this.extractStatusID(384)) != 0 && PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("Head Overheat!!!");
      }
   }

   public static PrintStatus.PrintStatusFactory getPrintStatusFactory(Object src) {
      if (null == factory) {
         factory = new PrintStatus.PrintStatusFactory(new P4610Status.Creator(src));
      }

      return factory;
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
         return new P4610Status(this.getSrc(), PrintStatus.PrintStatusFactory.dummy);
      }

      public RecyclableObject newRecyclableObject(CtorArg[] ctorArgs, RecycleFactory recycleFactory) throws RecycleFactoryException {
         throw new RecycleFactoryException("Parameters not supported");
      }

      public Object getSrc() {
         return this.src;
      }
   }
}
