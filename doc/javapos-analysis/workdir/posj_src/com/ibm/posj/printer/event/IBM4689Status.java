package com.ibm.posj.printer.event;

import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Util;
import com.ibm.jutil.patterns.factory.RecyclableObject;
import com.ibm.jutil.patterns.factory.RecycleFactory;
import com.ibm.jutil.patterns.factory.RecycleFactoryException;
import com.ibm.jutil.patterns.factory.RecyclableObject.CtorArg;
import com.ibm.jutil.patterns.factory.RecycleFactory.CreateMethod;
import com.ibm.posj.IBM4689PrinterConst;
import com.ibm.posj.printer.PrinterWriter;

public class IBM4689Status extends PrintStatus implements IBM4689PrinterConst {
   private byte id = 0;
   private boolean slipPaper = false;
   private boolean slipNonPaperErr = false;
   private byte lineCnt;
   private static PrintStatus.PrintStatusFactory factory = null;
   private ByteBuffer MCTbytes = ByteBuffer.getByteBufferFactory().createByteBuffer();
   private ByteBuffer Flashbytes = ByteBuffer.getByteBufferFactory().createByteBuffer();
   private ByteBuffer ECbytes = ByteBuffer.getByteBufferFactory().createByteBuffer();
   public static final int BITMASK = 255;
   public static int lastline = 0;

   public IBM4689Status(Object src, byte[] a) {
      super(src, a);
   }

   public String toString() {
      return "4689Status@" + this.hashCode() + "-" + Util.toFormatedHexString(this.getData());
   }

   public Object clone() {
      return getPrintStatusFactory(this.getSource()).createPrintStatus(this.getData(), this.getLogHelper());
   }

   public byte getLineCnt() {
      return this.lineCnt;
   }

   public void updateStatus() {
      byte[] data = super.getData();
      if (data[0] == data[2] && data[5] == data[7] && data[0] == data[7] && data[0] == 1) {
         this.log(1010, 4, "");
         this.setStatus(16, true);
         this.turnErrorOn(-1, (byte)2);
         this.cacheError((byte)-1, (byte)2, (short)16);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Communication Error!");
         }
      } else {
         this.parseByte0(data[0]);
         this.parseByte1(data[1]);
         this.parseByte2(data[2]);
         this.parseByte3(data[3]);
         this.parseByte4(data[4]);
         this.parseByte5(data[5]);
         this.parseByte6(data[6]);
         this.parseByte7(data[7]);
      }
   }

   protected void setLineCnt(byte x) {
      this.lineCnt = x;
      this.turnCmdCompleteOn();
      lastline = this.lineCnt;
   }

   protected void setMCTBytes(byte Hb, byte Lb) {
      this.MCTbytes.append(Hb);
      this.MCTbytes.append(Lb);
   }

   public ByteBuffer getMCTBytes() {
      return this.MCTbytes;
   }

   protected void setFlashBytes(byte Hb, byte Lb) {
      this.Flashbytes.append(Hb);
      this.Flashbytes.append(Lb);
   }

   public ByteBuffer getFlashBytes() {
      return this.Flashbytes;
   }

   protected void setECBytes(byte Hb, byte Lb) {
      this.ECbytes.append(Hb);
      this.ECbytes.append(Lb);
   }

   public ByteBuffer getECBytes() {
      return this.ECbytes;
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

   public void setStatusID(byte id) {
      this.id = id;
   }

   public byte getStatusID() {
      return this.id;
   }

   private void parseByte0(byte data) {
      if ((data & this.extractStatusID(257)) != 0) {
         this.log(3001, 4, "");
         this.setStatus(12, true);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Command Reject!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(258)) != 0) {
         byte[] tmp = super.getData();
         this.setMCTBytes(tmp[2], tmp[3]);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("MCT Bytes filled !!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(260)) != 0) {
         byte[] tmp = super.getData();
         this.setFlashBytes(tmp[2], tmp[3]);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Flash Bytes filled !!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(288)) != 0) {
         this.setStatus(9, true);
         this.turnCmdCompleteOn();
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("No Recoverable Error Response Bytes filled !!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(384)) != 0) {
         this.setStatus(9, true);
         this.turnCmdCompleteOn();
         this.setReady(true);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("No Error End Response Bytes filled !!!!!!!!!");
         }
      }
   }

   private void parseByte1(byte data) {
      if ((data & this.extractStatusID(514)) != 0) {
         byte[] tmp = super.getData();
         this.setECBytes(tmp[2], tmp[3]);
         this.turnCmdCompleteOn();
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("EC Bytes Filled!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(516)) != 0) {
         super.setExtendedData(true);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("UDC User Flash Data Filled!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(520)) != 0) {
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Near End Receipt!!!!!!!!!!");
         }

         this.setStatus(17, true);
      }

      if ((data & this.extractStatusID(528)) != 0) {
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Near End Journal!!!!!!!!!!");
         }

         this.setStatus(18, true);
      }

      if ((data & this.extractStatusID(544)) != 0) {
         super.setExtendedData(true);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Expaned Status Bytes!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(576)) != 0) {
         this.log(3014, 3, "Receipt cover is open");
         this.setStatus(5, true);
         this.turnErrorOn(2, (byte)2);
         this.cacheError((byte)2, (byte)2, (short)1);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("RECEIPT COVER OPEN ERROR!!!!!!!!!!");
         }

         this.log(3022, 3, "Journal cover is open");
         this.setStatus(7, true);
         this.turnErrorOn(8, (byte)2);
         this.cacheError((byte)8, (byte)2, (short)3);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("JOURNAL COVER OPEN ERROR!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(640)) != 0) {
         super.setExtendedData(true);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Device Info!!!!!!!!!!");
         }
      }
   }

   private void parseByte2(byte data) {
   }

   private void parseByte3(byte data) {
      this.setStatusID(data);
   }

   private void parseByte4(byte data) {
      if ((data & this.extractStatusID(1025)) != 0) {
         this.turnErrorOn(2, (byte)1);
         this.cacheError((byte)2, (byte)1, (short)4);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Flash EPROM Error!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(1028)) != 0) {
         this.log(3014, 3, "BAT Mechanical error.");
         this.turnErrorOn(2, (byte)2);
         this.cacheError((byte)2, (byte)2, (short)64);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("BAT Mechanical Error!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(1032)) != 0) {
         this.log(3014, 3, "BAT Electrical error.");
         this.turnErrorOn(2, (byte)2);
         this.cacheError((byte)2, (byte)2, (short)64);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("BAT Electrical Error!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(1040)) != 0) {
         this.log(3014, 3, "Printer Disabled.");
         this.turnErrorOn(2, (byte)2);
         this.cacheError((byte)2, (byte)2, (short)64);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Printer Disabled!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(1088)) != 0) {
         this.log(3014, 3, "Inoperable.");
         this.turnErrorOn(2, (byte)2);
         this.cacheError((byte)2, (byte)2, (short)64);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Inoperable Condition!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(1152)) != 0) {
         this.log(1014, 3, "Invalid Command to Device");
         this.turnErrorOn(2, (byte)1);
         this.cacheError((byte)2, (byte)1, (short)1);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Invalid Command to Device!!!!!!!!!!");
         }
      }
   }

   private void parseByte5(byte data) {
      if ((data & this.extractStatusID(1281)) != 0) {
         this.turnErrorOn(2, (byte)1);
         this.cacheError((byte)2, (byte)1, (short)4);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("BAT EEPROM Error!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(1282)) != 0) {
         this.turnErrorOn(2, (byte)1);
         this.cacheError((byte)2, (byte)1, (short)4);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("BAT Image RAM Error!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(1288)) != 0) {
         this.log(3019, 3, "Cutter error.");
         this.turnErrorOn(2, (byte)2);
         this.cacheError((byte)2, (byte)2, (short)64);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("CUT Error!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(1296)) != 0) {
         this.log(3020, 3, "CR Head Overheated.");
         this.turnErrorOn(2, (byte)2);
         this.cacheError((byte)2, (byte)2, (short)200);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Receipt Head Overheated!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(1312)) != 0) {
         this.log(3021, 3, "Journal Head Overheated.");
         this.turnErrorOn(8, (byte)2);
         this.cacheError((byte)8, (byte)2, (short)200);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Journal Head Overheated!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(1344)) != 0) {
         this.log(3014, 3, "CR Head Overheated.");
         this.turnErrorOn(2, (byte)2);
         this.cacheError((byte)2, (byte)2, (short)200);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Head Interlock Error!!!!!!!!!");
         }
      }
   }

   private void parseByte6(byte data) {
      if ((data & this.extractStatusID(1537)) != 0 && PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("UDC Image Exist!!!!!!!!!");
      }

      if ((data & this.extractStatusID(1538)) != 0 && PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("LOGO Image Exist!!!!!!!!!");
      }

      if ((data & this.extractStatusID(1544)) != 0 && PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("Cutter Home!!!!!!!!!");
      }

      if ((data & this.extractStatusID(1552)) != 0) {
         this.log(3014, 3, "Receipt Head is open");
         this.setStatus(5, true);
         this.turnErrorOn(2, (byte)2);
         this.cacheError((byte)2, (byte)2, (short)1);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("RECEIPT HEAD OPEN ERROR!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(1568)) != 0) {
         this.log(3014, 3, "Journal Head is open");
         this.setStatus(5, true);
         this.turnErrorOn(8, (byte)2);
         this.cacheError((byte)8, (byte)2, (short)1);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("JOURNAL HEAD OPEN ERROR!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(1600)) != 0) {
         this.log(3014, 3, "No paper on Receipt");
         this.setStatus(0, true);
         this.turnErrorOn(2, (byte)2);
         this.cacheError((byte)2, (byte)2, (short)8);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("No Paper on Receipt!!!!!!!!!!");
         }
      }

      if ((data & this.extractStatusID(1664)) != 0) {
         this.log(3022, 3, "No paper on Journal");
         this.setStatus(8, true);
         this.turnErrorOn(8, (byte)2);
         this.cacheError((byte)8, (byte)2, (short)32);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("No Paper on Journal!!!!!!!!!!");
         }
      }
   }

   private void parseByte7(byte data) {
      if ((data & this.extractStatusID(1794)) != 0 && PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("Test mode switch status!!!!!!!!!!");
      }

      if ((data & this.extractStatusID(1796)) != 0 && PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("Pause mode status!!!!!!!!!!");
      }

      if ((data & this.extractStatusID(1800)) != 0 && PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("Tategaki mode status!!!!!!!!!!");
      }

      if ((data & this.extractStatusID(1920)) != 0) {
         this.setStatus(11, true);
         this.setReady(true);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Printer Enabled!!!!!!!!!!");
         }
      }
   }

   public static PrintStatus.PrintStatusFactory getPrintStatusFactory(Object src) {
      if (null == factory) {
         factory = new PrintStatus.PrintStatusFactory(new IBM4689Status.Creator(src));
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
         return new IBM4689Status(this.getSrc(), PrintStatus.PrintStatusFactory.dummy);
      }

      public RecyclableObject newRecyclableObject(CtorArg[] ctorArgs, RecycleFactory recycleFactory) throws RecycleFactoryException {
         throw new RecycleFactoryException("Parameters not supported");
      }

      public Object getSrc() {
         return this.src;
      }
   }
}
