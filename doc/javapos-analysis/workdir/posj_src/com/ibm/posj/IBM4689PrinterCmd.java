package com.ibm.posj;

import com.ibm.jutil.ByteBuffer;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;
import com.ibm.posj.bus.printer.cmds.ibm4689.Font4689CmdFactory;
import com.ibm.posj.bus.printer.cmds.ibm4689.Gen4689CmdFactory;
import com.ibm.posj.bus.printer.cmds.ibm4689.Grap4689CmdFactory;
import com.ibm.posj.printer.StatusVisitor;

public abstract class IBM4689PrinterCmd extends DefaultPOSPrinterCmd {
   public IBM4689PrinterCmd(HandleCmd.Factory factory, String name, byte station) {
      super(factory, name, station);
   }

   public IBM4689PrinterCmd(HandleCmd.Factory factory, String name) {
      super(factory, name);
   }

   public static class Continuation4689Cmd extends DefaultPOSPrinterCmd.ByteArrayStoringCmd {
      private boolean simple = false;
      private ByteBuffer header = null;

      public Continuation4689Cmd(HandleCmd.Factory factory, String name, byte station, ByteBuffer head, byte[] data) {
         super(factory, name, station, data, (byte)0);
         this.setPreWrittenBuffer(head);
         this.setOutputCmd();
      }

      public byte[] getData() {
         return super.getArray();
      }

      public void setData(byte[] data) {
         super.setArray(data);
      }

      public boolean isSimple() {
         return this.simple;
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         if (this.simple) {
            visitor.visit(this);
         } else {
            ((IBM4689PrinterCmdVisitor)visitor).visitContinuation4689Cmd(this);
         }
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4689CmdFactory)this.factory.getGeneralCmdFactory()).createContinuation4689Cmd(this);
      }
   }

   public static class DownloadDBCSFontCmd extends IBM4689PrinterCmd {
      private byte size;
      private byte cnum;
      private ByteBuffer data;

      DownloadDBCSFontCmd(HandleCmd.Factory factory, String name, byte size, byte cnum, ByteBuffer buffer) {
         super(factory, name);
         this.setSize(size);
         this.setcNum(cnum);
         this.setData(buffer);
      }

      public void accept(IBM4689PrinterCmdVisitor visitor) {
         visitor.visitDownloadDBCSFontCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitDownloadDBCSFontCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitDownloadDBCSFontCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Font4689CmdFactory)this.factory.getFontCmdFactory()).createDownloadDBCSFontCmd(this);
      }

      protected void setSize(byte b) {
         this.size = b;
      }

      protected void setcNum(byte b) {
         this.cnum = b;
      }

      protected void setData(ByteBuffer b) {
         this.data = b;
      }

      public byte getSize() {
         return this.size;
      }

      public byte getcNum() {
         return this.cnum;
      }

      public byte[] getData() {
         return this.data.getBytes();
      }
   }

   public static class DownloadFontCmd extends IBM4689PrinterCmd {
      private byte size;
      private byte badd;
      private byte eadd;
      private ByteBuffer data;

      DownloadFontCmd(HandleCmd.Factory factory, String name, byte size, byte badd, byte eadd, ByteBuffer buffer) {
         super(factory, name);
         this.setSize(size);
         this.setbAdd(badd);
         this.seteAdd(eadd);
         this.setData(buffer);
      }

      public void accept(IBM4689PrinterCmdVisitor visitor) {
         visitor.visitDownloadFontCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitDownloadFontCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitDownloadFontCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Font4689CmdFactory)this.factory.getFontCmdFactory()).createDownloadFontCmd(this);
      }

      protected void setSize(byte b) {
         this.size = b;
      }

      protected void setbAdd(byte b) {
         this.badd = b;
      }

      protected void seteAdd(byte b) {
         this.eadd = b;
      }

      protected void setData(ByteBuffer b) {
         this.data = b;
      }

      public byte getSize() {
         return this.size;
      }

      public byte getbAdd() {
         return this.badd;
      }

      public byte geteAdd() {
         return this.eadd;
      }

      public byte[] getData() {
         return this.data.getBytes();
      }
   }

   public static class ECLevelRequestCmd extends IBM4689PrinterCmd {
      public ECLevelRequestCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4689CmdFactory)this.factory.getGeneralCmdFactory()).createECLevelRequestCmd(this);
      }

      public void accept(IBM4689PrinterCmdVisitor visitor) {
         visitor.visitECLevelRequestCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitECLevelRequestCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitECLevelRequestCmd(this);
      }
   }

   public static class Factory extends DefaultPOSPrinterCmd.Factory {
      public Factory(PrintCmdFactory lowerFactory) {
         this.setPrintCmdFactory(lowerFactory);
      }

      public POSPrinterCmd createContinuation4689Cmd(byte station, ByteBuffer head, byte[] data) {
         return new IBM4689PrinterCmd.Continuation4689Cmd(this, "Continuation4689Cmd", station, head, data);
      }

      public SystemCmd.StatusRequestCmd createStatusRequestCmd() {
         return new GeneralPOSPrinterCmd.StatusRequestCmd(this, "StatusRequestCmd");
      }

      public POSPrinterCmd createECLevelRequestCmd(boolean buffered) {
         return new IBM4689PrinterCmd.ECLevelRequestCmd(this, "ECLevelRequestCmd");
      }

      public POSPrinterCmd createPrintNormalCmd(byte station, String data, byte lines) {
         return new IBM4689PrinterCmd.PrintNormalCmd(this, "PrintNormalCmd", station, data, lines);
      }

      public POSPrinterCmd createPrintNormalCmd(byte station, byte[] array, int index, int count, byte appendByte, byte lines) {
         return new IBM4689PrinterCmd.PrintNormalCmd(this, "PrintNormalCmd", station, array, index, count, appendByte, lines);
      }

      public POSPrinterCmd createPrintNormalCmd(byte station, ByteBuffer byteBuffer, int index, int count, byte appendByte, byte lines) {
         return new IBM4689PrinterCmd.PrintNormalCmd(this, "PrintNormalCmd", station, byteBuffer, index, count, appendByte, lines);
      }

      public POSPrinterCmd createLoadUDCBufferCmd(byte block, ByteBuffer byteBuffer) {
         return new IBM4689PrinterCmd.LoadUDCBufferCmd(this, "LoadUDCBufferCmd", block, byteBuffer);
      }

      public POSPrinterCmd createInitPrinterCmd() {
         return new IBM4689PrinterCmd.InitPrinterCmd(this, "InitPrinterCmd");
      }

      public POSPrinterCmd createFeedCmd(byte station, byte lines, ByteBuffer buffer) {
         return new IBM4689PrinterCmd.FeedCmd(this, "FeedCmd", station, lines);
      }

      public POSPrinterCmd createPartialCutCmd() {
         return new IBM4689PrinterCmd.PartialCutCmd(this, "PartialCutCmd");
      }

      public POSPrinterCmd createPrintFullCutCmd() {
         return new IBM4689PrinterCmd.PrintFullCutCmd(this, "PrintFullCutCmd");
      }

      public POSPrinterCmd createFeedUnitsBackCmd(byte lines) {
         return new IBM4689PrinterCmd.FeedUnitsBackCmd(this, "FeedUnitsBackCmd", lines);
      }

      public POSPrinterCmd createCutPaperCmd(byte station) {
         return new DefaultPOSPrinterCmd.CutPaperCmd(this, "CutPaperCmd", (short)station);
      }

      public POSPrinterCmd createDownloadFontCmd(byte size, byte badd, byte eadd, ByteBuffer buffer) {
         return new IBM4689PrinterCmd.DownloadFontCmd(this, "DownloadFontCmd", size, badd, eadd, buffer);
      }

      public POSPrinterCmd createDownloadDBCSFontCmd(byte size, byte cnum, ByteBuffer buffer) {
         return new IBM4689PrinterCmd.DownloadDBCSFontCmd(this, "DownloadDBCSFontCmd", size, cnum, buffer);
      }

      public POSPrinterCmd createSelectHSize(byte size) {
         return new IBM4689PrinterCmd.SelectHSizeCmd(this, "SelectHSizeCmd", size);
      }

      public POSPrinterCmd createSelectVSize(byte size) {
         return new IBM4689PrinterCmd.SelectVSizeCmd(this, "SelectVSizeCmd", size);
      }

      public POSPrinterCmd createSelectHRIPosition(byte pos) {
         return new IBM4689PrinterCmd.SelectHRIPositionCmd(this, "SelectHRIPositionCmd", pos);
      }

      public POSPrinterCmd createSendCheckSumCmd(byte area) {
         return new IBM4689PrinterCmd.SendCheckSumCmd(this, "SendCheckSumCmd", area);
      }
   }

   public static class FeedCmd extends IBM4689PrinterCmd {
      private byte lines;

      FeedCmd(HandleCmd.Factory factory, String name, byte station, byte lines) {
         super(factory, name, station);
         this.setLines(lines);
      }

      public void accept(IBM4689PrinterCmdVisitor visitor) {
         visitor.visitFeedCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitFeedCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitFeedCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4689CmdFactory)this.factory.getGeneralCmdFactory()).createFeedCmd(this);
      }

      public void setLines(byte l) {
         this.lines = l;
      }

      public byte getLines() {
         return this.lines;
      }
   }

   public static class FeedUnitsBackCmd extends IBM4689PrinterCmd {
      private byte lines;

      FeedUnitsBackCmd(HandleCmd.Factory factory, String name, byte lines) {
         super(factory, name);
         this.setLines(lines);
      }

      public void accept(IBM4689PrinterCmdVisitor visitor) {
         visitor.visitFeedUnitsBackCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitFeedUnitsBackCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitFeedUnitsBackCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4689CmdFactory)this.factory.getGeneralCmdFactory()).createFeedUnitsBackCmd(this);
      }

      public void setLines(byte l) {
         this.lines = l;
      }

      public byte getLines() {
         return this.lines;
      }
   }

   public static class InitPrinterCmd extends IBM4689PrinterCmd {
      InitPrinterCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBM4689PrinterCmdVisitor visitor) {
         visitor.visitInitPrinterCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitInitPrinterCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitInitPrinterCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4689CmdFactory)this.factory.getGeneralCmdFactory()).createInitPrinterCmd(this);
      }
   }

   public static class LoadUDCBufferCmd extends IBM4689PrinterCmd {
      private ByteBuffer data;
      private byte block;

      LoadUDCBufferCmd(HandleCmd.Factory factory, String name, byte block, ByteBuffer buffer) {
         super(factory, name);
         this.setBlock(block);
         this.setData(buffer);
      }

      public void accept(IBM4689PrinterCmdVisitor visitor) {
         visitor.visitLoadUDCBufferCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitLoadUDCBufferCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitLoadUDCBufferCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Grap4689CmdFactory)this.factory.getGraphicCmdFactory()).createLoadUDCBufferCmd(this);
      }

      public void setBlock(byte b) {
         this.block = b;
      }

      public byte getBlock() {
         return this.block;
      }

      public void setData(ByteBuffer b) {
         this.data = b;
      }

      public byte[] getData() {
         return this.data.getBytes();
      }
   }

   public static class PartialCutCmd extends IBM4689PrinterCmd {
      PartialCutCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBM4689PrinterCmdVisitor visitor) {
         visitor.visitPartialCutCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitPartialCutCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitPartialCutCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4689CmdFactory)this.factory.getGeneralCmdFactory()).createPartialCutCmd(this);
      }
   }

   public static class PrintFullCutCmd extends IBM4689PrinterCmd {
      PrintFullCutCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBM4689PrinterCmdVisitor visitor) {
         visitor.visitPrintFullCutCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitPrintFullCutCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitPrintFullCutCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Grap4689CmdFactory)this.factory.getGraphicCmdFactory()).createPrintFullCutCmd(this);
      }
   }

   public static class PrintNormalCmd extends GeneralPOSPrinterCmd.PrintNormalCmd {
      private byte linesFeed;

      PrintNormalCmd(HandleCmd.Factory factory, String name, byte station, String data, byte lines) {
         super(factory, name, station, data);
         this.setOutputCmd();
         this.setLinesFeed(lines);
      }

      PrintNormalCmd(HandleCmd.Factory factory, String name, byte station, String data, boolean immediate, byte lines) {
         super(factory, name, station, data, immediate);
         this.setOutputCmd();
         this.setLinesFeed(lines);
      }

      PrintNormalCmd(HandleCmd.Factory factory, String name, byte station, byte[] array, int index, int count, byte appendByte, byte lines) {
         super(factory, name, station, array, index, count, appendByte);
         this.setLinesFeed(lines);
      }

      PrintNormalCmd(HandleCmd.Factory factory, String name, byte station, ByteBuffer byteBuffer, int index, int count, byte appendByte, byte lines) {
         super(factory, name, station, byteBuffer, index, count, appendByte);
         this.setImmediate(false);
         this.setLinesFeed(lines);
      }

      public void setLinesFeed(byte lines) {
         this.linesFeed = lines;
      }

      public byte getLinesFeed() {
         return this.linesFeed;
      }
   }

   public static class ResetCmd extends GeneralPOSPrinterCmd.ResetCmd implements POSPrinterCmd.ResetCmd {
      ResetCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }
   }

   public static class SelectHRIPositionCmd extends IBM4689PrinterCmd {
      private byte pos;

      SelectHRIPositionCmd(HandleCmd.Factory factory, String name, byte pos) {
         super(factory, name);
         this.setPos(pos);
      }

      public void accept(IBM4689PrinterCmdVisitor visitor) {
         visitor.visitSelectHRIPositionCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitSelectHRIPositionCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitSelectHRIPositionCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Grap4689CmdFactory)this.factory.getGraphicCmdFactory()).createSelectHRIPositionCmd(this);
      }

      protected void setPos(byte b) {
         this.pos = b;
      }

      public byte getPos() {
         return this.pos;
      }
   }

   public static class SelectHSizeCmd extends IBM4689PrinterCmd {
      private byte size;

      SelectHSizeCmd(HandleCmd.Factory factory, String name, byte size) {
         super(factory, name);
         this.setSize(size);
      }

      public void accept(IBM4689PrinterCmdVisitor visitor) {
         visitor.visitSelectHSizeCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitSelectHSizeCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitSelectHSizeCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Grap4689CmdFactory)this.factory.getGraphicCmdFactory()).createSelectHSizeCmd(this);
      }

      protected void setSize(byte b) {
         this.size = b;
      }

      public byte getSize() {
         return this.size;
      }
   }

   public static class SelectVSizeCmd extends IBM4689PrinterCmd {
      private byte size;

      SelectVSizeCmd(HandleCmd.Factory factory, String name, byte size) {
         super(factory, name);
         this.setSize(size);
      }

      public void accept(IBM4689PrinterCmdVisitor visitor) {
         visitor.visitSelectVSizeCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitSelectVSizeCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitSelectVSizeCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Grap4689CmdFactory)this.factory.getGraphicCmdFactory()).createSelectVSizeCmd(this);
      }

      protected void setSize(byte b) {
         this.size = b;
      }

      public byte getSize() {
         return this.size;
      }
   }

   public static class SendCheckSumCmd extends IBM4689PrinterCmd {
      private byte area;

      SendCheckSumCmd(HandleCmd.Factory factory, String name, byte area) {
         super(factory, name);
         this.setArea(area);
      }

      public void accept(IBM4689PrinterCmdVisitor visitor) {
         visitor.visitSendCheckSumCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitSendCheckSumCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4689PrinterCmdVisitor)visitor).visitSendCheckSumCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4689CmdFactory)this.factory.getGeneralCmdFactory()).createSendCheckSumCmd(this);
      }

      protected void setArea(byte b) {
         this.area = b;
      }

      public byte getArea() {
         return this.area;
      }
   }

   public static class StatusRequestCmd extends GeneralPOSPrinterCmd.StatusRequestCmd implements POSPrinterCmd.StatusRequestCmd {
      public StatusRequestCmd(HandleCmd.Factory f, String name) {
         super(f, name);
      }
   }

   public static class TestReqCmd extends GeneralPOSPrinterCmd.TestReqCmd implements POSPrinterCmd.TestReqCmd {
      TestReqCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }
   }
}
