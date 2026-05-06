package com.ibm.posj;

import com.ibm.jutil.ByteBuffer;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.StatusVisitor;
import com.ibm.posj.printer.event.PrintStatus;
import java.util.List;

public class GeneralPOSPrinterCmd {
   public static class DevInfoCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd implements POSPrinterCmd.DevInfoCmd {
      private int id = 0;
      private int type = 0;
      private int firmwareLevel = -1;
      private String serialNumber = "";

      DevInfoCmd(HandleCmd.Factory factory, String name, boolean buffered) {
         super(factory, name, (byte)0, buffered, (byte)0);
      }

      public boolean isBuffered() {
         return this.getBoolean();
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitDevInfoCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         visitor.visitDevInfoCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return this.factory.getPrintCmdFactory().getGeneralFactory().createDevInfoCmd(this);
      }

      public void accept(SystemCmdVisitor visitor) {
      }

      public byte getCommandSet() {
         return 0;
      }

      public int getDeviceId() {
         return this.id;
      }

      public int getDeviceType() {
         return this.type;
      }

      public byte getLanguage() {
         return 0;
      }

      public void setCmdBytes(byte[] cmd) {
      }

      public void setDeviceType(int type) {
         this.type = type;
      }

      public void setDeviceId(int id) {
         this.id = id;
      }

      public void setDeviceType(byte type) {
         this.type = type;
      }

      public int getFirmwareLevel() {
         return this.firmwareLevel;
      }

      public String getSerialNumber() {
         return this.serialNumber;
      }

      public void setFirmwareLevel(int fl) {
         this.firmwareLevel = fl;
      }

      public void setSerialNumber(String sn) {
         this.serialNumber = sn;
      }

      public int getCode() {
         return 103;
      }
   }

   public static class DualPrintCmd extends DefaultPOSPrinterCmd implements POSPrinterCmd.DualPrintCmd {
      DualPrintCmd(HandleCmd.Factory factory, String name, byte stations, POSPrinterCmd cmd1, POSPrinterCmd cmd2) {
         super(factory, name, stations);
         this.appendSplitMembers((DefaultPOSPrinterCmd)cmd1);
         this.appendSplitMembers((DefaultPOSPrinterCmd)cmd2);
      }

      private POSPrinterCmd get(int i) {
         List l = this.getCmdList();
         if (null == l) {
            return null;
         } else {
            try {
               return (POSPrinterCmd)l.get(i);
            } catch (Exception var4) {
               return null;
            }
         }
      }

      public PrintCmd getPrintCmd() {
         return ((DefaultPOSPrinterCmd)this.getCmd1()).getPrintCmd();
      }

      public POSPrinterCmd getCmd1() {
         return this.get(0);
      }

      public POSPrinterCmd getCmd2() {
         return this.get(1);
      }

      protected PrintCmd buildPrintCmd() {
         return null;
      }

      protected void completeCleanup() {
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println(">completeCleanUp " + this);
         }

         List innerList = this.getCmdList();
         if (null != innerList && 0 <= innerList.size()) {
            for (int i = 0; i < innerList.size(); i++) {
               DefaultPOSPrinterCmd dcmd = (DefaultPOSPrinterCmd)innerList.get(i);
               if (null != dcmd && !this.equals(dcmd)) {
                  dcmd.completeCleanup();
               }
            }
         }
      }
   }

   public static class PrintNormalCmd extends DefaultPOSPrinterCmd.ByteArrayStoringCmd implements POSPrinterCmd.PrintNormalCmd {
      short vscale = 0;
      int index = 0;
      int count = 0;
      byte appendByte = 0;
      boolean lineCompleted = false;
      ByteBuffer byteBuffer = null;

      PrintNormalCmd(HandleCmd.Factory factory, String name, byte station, String data) {
         super(factory, name, station, data.getBytes(), (byte)0);
         byte[] dataArray = this.getArray();
         this.count = dataArray.length;
         this.byteBuffer = null;
         this.setMaxCharCnt(this.count);
         byte tl = 0;

         for (int i = 0; i < this.count; i++) {
            if (dataArray[i] == 10) {
               tl++;
            }
         }

         if (0 < tl) {
            this.completeLine();
         }

         this.setOutputCmd();
      }

      PrintNormalCmd(HandleCmd.Factory factory, String name, byte station, String data, boolean immediate) {
         super(factory, name, station, data.getBytes(), (byte)0);
         byte[] dataArray = this.getArray();
         this.setImmediate(immediate);
         this.count = dataArray.length;
         this.byteBuffer = null;
         byte tl = 0;
         this.setMaxCharCnt(this.count);
         this.setOutputCmd();
      }

      PrintNormalCmd(HandleCmd.Factory factory, String name, byte station, byte[] array, int index, int count, byte appendByte) {
         super(factory, name, station, array, (byte)0);
         this.index = index;
         this.count = count;
         this.appendByte = appendByte;
         this.byteBuffer = null;
         this.setImmediate(false);
         this.setOutputCmd();
         byte tl = 0;
         this.setMaxCharCnt(count);
      }

      PrintNormalCmd(HandleCmd.Factory factory, String name, byte station, ByteBuffer byteBuffer, int index, int count, byte appendByte) {
         super(factory, name, station, byteBuffer.getBytesRef(), (byte)0);
         this.index = index;
         this.count = count;
         this.appendByte = appendByte;
         this.byteBuffer = byteBuffer;
         this.setImmediate(false);
         this.setOutputCmd();
         this.setMaxCharCnt(count);
      }

      public void setData(String data) {
         this.setArray(data.getBytes());
         this.count = data.length();
      }

      public byte[] getData() {
         return this.getArray();
      }

      public int getIndex() {
         return this.index;
      }

      public int getCount() {
         return this.count;
      }

      public void setAppendByte(byte appendByte) {
         this.appendByte = appendByte;
      }

      public byte appendByte() {
         return this.appendByte;
      }

      public boolean isLineCompleted() {
         return this.lineCompleted;
      }

      public void incompleteLine() {
         this.lineCompleted = false;
      }

      public void completeLine() {
         this.lineCompleted = true;
      }

      protected PrintCmd buildPrintCmd() {
         if (10 == this.appendByte()) {
            this.completeLine();
         }

         return this.factory.getPrintCmdFactory().getGeneralFactory().createPrintNormalCmd(this);
      }

      public synchronized void recycle() {
         super.recycle();
         if (this.byteBuffer != null && !this.byteBuffer.isRecycled()) {
            this.byteBuffer.recycle();
         }
      }

      public void accept(StatusVisitor visitor) {
         visitor.visitPrintNormalCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitPrintNormalCmd(this);
      }

      public short getVerticalScale() {
         return this.vscale;
      }

      public void setVerticalScale(short v) {
         this.vscale = this.vscale > v ? this.vscale : v;
      }
   }

   public static class ResetCmd extends DefaultPOSPrinterCmd implements POSPrinterCmd.ResetCmd {
      ResetCmd(HandleCmd.Factory factory, String name) {
         super(factory, name, (byte)0);
      }

      public void accept(SystemCmdVisitor visitor) {
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitResetCmd(this);
      }

      public void setCmdBytes(byte[] cmd) {
      }

      protected PrintCmd buildPrintCmd() {
         return this.factory.getPrintCmdFactory().getGeneralFactory().createResetCmd(this);
      }
   }

   public static class SelectStationCmd extends DefaultPOSPrinterCmd implements POSPrinterCmd.SelectStationCmd {
      public SelectStationCmd(HandleCmd.Factory factory, String name, byte station) {
         super(factory, name, station);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitSelectStationCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return this.factory.getPrintCmdFactory().getGeneralFactory().createSelectStationCmd(this);
      }
   }

   public static class StatisticCmd extends DefaultPOSPrinterCmd implements POSPrinterCmd.StatisticCmd {
      private String statisticType;
      private Object statisticResult = null;

      public StatisticCmd(HandleCmd.Factory factory, String name, String statisticType) {
         super(factory, name);
         this.statisticType = statisticType;
      }

      protected PrintCmd buildPrintCmd() {
         return this.factory.getPrintCmdFactory().getGeneralFactory().createStatisticCmd(this);
      }

      public String getStatisticType() {
         return this.statisticType;
      }

      public void setStatisticResult(Object statisticResult) {
         this.statisticResult = statisticResult;
      }

      public Object getStatisticResult() {
         return this.statisticResult;
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitStatisticCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         visitor.visitStatisticCmd(this);
      }

      public void accept(SystemCmdVisitor visitor) {
      }

      public void setCmdBytes(byte[] cmd) {
      }
   }

   public static class StatusRequestCmd extends DefaultPOSPrinterCmd implements POSPrinterCmd.StatusRequestCmd {
      private PrintStatus status = null;

      public StatusRequestCmd(HandleCmd.Factory f, String name) {
         super(f, name);
      }

      public void accept(SystemCmdVisitor visitor) {
      }

      protected PrintCmd buildPrintCmd() {
         return this.factory.getPrintCmdFactory().getGeneralFactory().createStatusRequestCmd(this);
      }

      public void setStatus(PrintStatus ps) {
         if (null == this.status) {
            this.status.recycle();
         }

         this.status = (PrintStatus)ps.clone();
      }

      public PrintStatus getStatus() {
         return this.status;
      }

      public void clean() {
         if (null != this.status) {
            this.status.recycle();
         }

         super.clean();
      }

      public void setCmdBytes(byte[] cmd) {
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitStatusRequestCmd(this);
      }
   }

   public static class TestReqCmd extends DefaultPOSPrinterCmd implements POSPrinterCmd.TestReqCmd {
      TestReqCmd(HandleCmd.Factory factory, String name) {
         super(factory, name, (byte)0);
         this.impliedStations = 2;
         this.setOutputCmd();
      }

      public boolean compareStations(byte stations) {
         return (stations & 2) > 0 && (stations & 4) > 0;
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitTestReqCmd(this);
      }

      public void accept(SystemCmdVisitor visitor) {
      }

      protected PrintCmd buildPrintCmd() {
         return this.factory.getPrintCmdFactory().getGeneralFactory().createTestRequestCmd(this);
      }

      public void setCmdBytes(byte[] cmd) {
      }
   }
}
