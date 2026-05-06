package com.ibm.posj;

import com.ibm.jutil.AsyncBitSet;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Util;
import com.ibm.posj.bus.printer.cmds.CheckScannerCmdFactory;
import com.ibm.posj.bus.printer.cmds.FontCmdFactory;
import com.ibm.posj.bus.printer.cmds.GeneralCmdFactory;
import com.ibm.posj.bus.printer.cmds.GraphicCmdFactory;
import com.ibm.posj.bus.printer.cmds.MICRCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;
import com.ibm.posj.event.OutputCompleteEvent;
import com.ibm.posj.printer.AbstractCmdCompleteVisitor;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.StatusVisitor;
import com.ibm.posj.util.DefaultPOSPrinterCmdV;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class DefaultPOSPrinterCmd extends AbstractHandleCmd implements POSPrinterCmd, POSPrinterCmd.IDCmd {
   protected DefaultPOSPrinterCmd.Factory factory = null;
   protected int outputid = -1;
   protected String name;
   protected byte station = 0;
   protected byte impliedStations = 0;
   protected PrintCmd printCmd = null;
   protected boolean cleaned = false;
   private POSPrinterCmd owner = null;
   private POSPrinterCmd master = null;
   private POSPrinterCmd crucialCmd = null;
   private POSPrinterCmd splitCrucialCmd = null;
   private DefaultPOSPrinterCmd stationCmd = null;
   private int code = -1;
   private byte id = -1;
   private List associated = null;
   private List innerList = null;
   private AsyncBitSet bools = new AsyncBitSet();
   private int splitCnt = 0;
   private ByteBuffer outPacket = null;
   private ByteBuffer preWrite = null;
   private ByteBuffer requestedData = null;
   private boolean[] cMonitor = null;
   private PrintCmdList manager = null;
   private Handle.EventHelper eventHelper = null;
   private int maxCharLen = 0;
   public static final int DEFAULTID = -1;
   public static final byte ONE = 1;
   public static final byte[] DUMMYBYTES = new byte[]{0};
   public static final int SETTINGCMD = 0;
   public static final int PARTIALLYCOMPLETE = 1;
   public static final int CHILD = 2;
   public static final int IMMEDIATE = 3;
   public static final int IS_OUTPUTCMD = 4;
   public static final int ASYNC = 5;
   public static final int LOADING = 6;
   public static final int FULLYCOMPLETE = 7;
   public static final int END = 8;
   public static final byte ZERO = 0;
   private static DefaultPOSPrinterCmd.BoldVisitor bVisit = new DefaultPOSPrinterCmd.BoldVisitor();

   public DefaultPOSPrinterCmd(HandleCmd.Factory f, String name, byte station) {
      super(f);
      this.factory = (DefaultPOSPrinterCmd.Factory)f;
      this.name = name;
      this.impliedStations = this.station = station;
      this.setRoleAsSettings(true);
      this.crucialCmd = this;
      this.owner = this;
   }

   public DefaultPOSPrinterCmd(HandleCmd.Factory f, String name) {
      super(f);
      this.factory = (DefaultPOSPrinterCmd.Factory)f;
      this.name = name;
      this.setRoleAsSettings(true);
      this.crucialCmd = this;
      this.owner = this;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer(50);
      sb.append(this.getName()).append("@").append(Integer.toHexString(this.hashCode()));
      return sb.toString();
   }

   public String print() {
      return this.innerList != null ? this.innerList.toString() : "empty";
   }

   public boolean compareStations(byte stations) {
      return !this.isOutputCmd() ? false : (stations & this.getImpliedStations()) > 0;
   }

   public void setMemoryCmd() {
      this.bools.clear(4);
   }

   public boolean isMemoryCmd() {
      return !this.bools.get(4);
   }

   public boolean isAsyncCmd() {
      return this.isSlave() && this.owner != this ? this.getOwnerCmd().isAsyncCmd() : this.bools.get(5);
   }

   public void setAsyncCmd(boolean a) {
      if (a) {
         this.bools.set(5);
      } else {
         this.bools.clear(5);
      }
   }

   public ByteBuffer getRequestData() {
      return this.requestedData;
   }

   public void setRequestedData(ByteBuffer d) {
      this.requestedData = d;
   }

   public void setRoleAsSettings(boolean b) {
      if (b) {
         this.bools.set(0);
      } else {
         this.bools.clear(0);
      }
   }

   public boolean isSettingsCmd() {
      return this.bools.get(0);
   }

   public void setImmediate(boolean immediate) {
      if (immediate) {
         this.bools.set(3);
      } else {
         this.bools.clear(3);
      }
   }

   public void loading() {
      this.bools.set(6);
      if (null != this.getMasterCmd()) {
         ((DefaultPOSPrinterCmd)this.getMasterCmd()).loading();
      }
   }

   public boolean isOutputCmd() {
      return this.bools.get(4);
   }

   public void addToManager(PrintCmdList l) {
      this.manager = l;
   }

   public void setCompleteMonitor(boolean[] m) {
      this.cMonitor = m;
   }

   public boolean isFullyComplete() {
      return this.getOwnerCmd() == this ? this.crucialCmd.isCompleted() : ((DefaultPOSPrinterCmd)this.getOwnerCmd()).isFullyComplete();
   }

   public boolean isPartiallyComplete() {
      return super.isCompleted() || this.bools.get(1);
   }

   public void setEventHandler(Handle.EventHelper e) {
      this.eventHelper = e;
   }

   public void forceComplete() {
      if (null != this.getMasterCmd()) {
         this.getMasterCmd().setResult(this.getResult());
         ((DefaultPOSPrinterCmd)this.getMasterCmd()).forceComplete();
      } else {
         this.crucialCmd = this;
         super.setCompleted(true);
         this.completeCleanup();
      }
   }

   private void subCmdCompleted(POSPrinterCmd p) {
      if (p == this.crucialCmd || p == this.splitCrucialCmd) {
         this.setCompleted(true);
      }
   }

   public void setCompleted(boolean b) {
      if (this.isCompleted() != b) {
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("SetCompleted( " + this + ") my crucial =" + this.crucialCmd);
         }

         if (!b) {
            super.setCompleted(b);
         } else {
            if (this == this.crucialCmd && null != this.splitCrucialCmd) {
               this.crucialCmd = this.splitCrucialCmd;
            }

            if (this == this.crucialCmd) {
               super.setCompleted(true);
            }

            if (null != this.getResult() && this.getResult().isInError() && null != this.master) {
               this.master.setResult(this.getResult());
               this.forceComplete();
            }

            if (this.crucialCmd.isCompleted()) {
               if (PrinterWriter.getTracer().isOn()) {
                  PrinterWriter.getTracer().println(this.getOutputID() + " is outputid Really setCompleted " + this);
                  PrinterWriter.getTracer().println("Eventhelper=  " + this.eventHelper + " inError? " + this.getResult().isInError());
               }

               if (this.getOutputID() > -1 && null != this.eventHelper && !this.getResult().isInError()) {
                  OutputCompleteEvent oe = new OutputCompleteEvent(this, this.getOutputID());
                  this.eventHelper.fireOutputCompleteEvent(oe);
                  this.eventHelper = null;
               }

               super.setCompleted(true);
               if (null != this.cMonitor) {
                  this.cMonitor[0] = true;
                  this.cMonitor = null;
               }

               if (this.master != null) {
                  ((DefaultPOSPrinterCmd)this.master).subCmdCompleted(this);
               } else {
                  this.completeCleanup();
               }
            } else {
               this.bools.set(1);
            }
         }
      }
   }

   public void retry() {
      this.crucialCmd.setCompleted(false);
      this.bools.clear(1);
      this.setCompleted(false);
      this.getPrintCmd().retry();
   }

   public void acceptCompletionVisitor(AbstractCmdCompleteVisitor acv) {
      this.accept(acv);
      if (null != this.innerList) {
         for (int i = 0; i < this.innerList.size(); i++) {
            DefaultPOSPrinterCmd pc = (DefaultPOSPrinterCmd)this.innerList.get(i);
            if (this != pc) {
               pc.acceptCompletionVisitor(acv);
            }
         }
      }
   }

   public boolean isContinueNeeded() {
      return false;
   }

   public byte[] toBytes() {
      return this.printCmd.getBytes();
   }

   public void setPreWrittenBuffer(ByteBuffer b) {
      this.preWrite = b;
   }

   public ByteBuffer getPreWrittenBuffer() {
      return this.preWrite;
   }

   public void setOutboundPacket(ByteBuffer pData) {
      this.outPacket = pData;
   }

   public ByteBuffer getOutboundPacket() {
      return this.outPacket;
   }

   public boolean isPrintCmdNull() {
      return null == this.printCmd;
   }

   public PrintCmd getPrintCmd() {
      this.cleaned = false;
      if (this.isPrintCmdNull()) {
         this.generatePrintCmds();
      }

      return this.printCmd;
   }

   public void clean() {
      try {
         this.completeCleanup();
         if (null != this.cMonitor) {
            this.cMonitor[0] = true;
            this.cMonitor = null;
         }
      } catch (Exception var3) {
      }

      this.setResult(null);
      this.bools.clearAll();
      if (this.printCmd != null) {
         this.printCmd.recycle();
         this.printCmd = null;
      }

      if (null != this.innerList && this.innerList.size() > 0) {
         for (int idx = 0; idx < this.innerList.size(); idx++) {
            POSPrinterCmd p = (POSPrinterCmd)this.innerList.get(idx);
            if (!this.equals(p)) {
               p.recycle();
            }
         }
      }

      this.master = null;
      this.setOutputID(-1);
      this.setOutboundPacket(null);
      this.impliedStations = this.station = 0;
      this.eventHelper = null;
      this.maxCharLen = this.splitCnt = 0;
      this.crucialCmd = this;
      this.splitCrucialCmd = null;
      this.stationCmd = null;
      super.clean();
      this.cleaned = true;
   }

   public String getName() {
      return this.name;
   }

   public int getCode() {
      return this.code;
   }

   public void setCode(byte c) {
      this.code = Util.unsignedInt(c);
   }

   public void setOutputID(int id) {
      this.outputid = id;
   }

   public int getOutputID() {
      return this.outputid;
   }

   public void setMaxCharCnt(int max) {
      if (max > this.maxCharLen) {
         this.maxCharLen = max;
      }
   }

   public int charCnt() {
      return this.maxCharLen;
   }

   public byte getImpliedStations() {
      return this.getOwnerCmd() == this ? this.impliedStations : this.getOwnerCmd().getImpliedStations();
   }

   public byte getStation() {
      return this.station;
   }

   public POSPrinterCmd getStationCmd() {
      if (null == this.stationCmd) {
         this.stationCmd = (DefaultPOSPrinterCmd)this.factory.createSelectStationCmd(this.getStation());
         this.stationCmd.setOwner(this.getOwnerCmd());
         this.stationCmd.setMasterCmd((DefaultPOSPrinterCmd)this.getOwnerCmd());
         this.stationCmd.setOutputCmd();
      }

      return this.stationCmd;
   }

   public void setStation(byte station) {
      this.station = station;
   }

   public boolean isImmediate() {
      return this.bools.get(3);
   }

   public void accept(HandleCmdVisitor visitor) {
      visitor.visitPOSPrinterCmd(this);
   }

   public void accept(POSPrinterCmdVisitor visitor) {
      visitor.visit(this);
   }

   public void accept(StatusVisitor visitor) {
      visitor.visit(this);
   }

   public void acceptMultiVisitor(POSPrinterCmdVisitor fv) {
      if (null != this.innerList && this.innerList.size() != 0) {
         DefaultPOSPrinterCmd xcmd = null;

         for (int i = 0; i < this.innerList.size(); i++) {
            xcmd = (DefaultPOSPrinterCmd)this.innerList.get(i);
            if (this == xcmd) {
               this.accept(fv);
            } else {
               xcmd.acceptMultiVisitor(fv);
            }
         }
      } else {
         this.accept(fv);
      }
   }

   public void recycle() {
      this.factory.recycle(this);
   }

   public void appendPOSPrinterCmd(POSPrinterCmd add) throws IllegalStateException {
      if (null != add) {
         if (((DefaultPOSPrinterCmd)add).isSlave()) {
            IllegalStateException te = new IllegalStateException(add.toString());
            if (PrinterWriter.getTracer().isOn()) {
               PrinterWriter.getTracer().print(te);
            }

            throw te;
         } else {
            DefaultPOSPrinterCmd these = (DefaultPOSPrinterCmd)add;
            if (this.getCmdList() != null && !these.hasListCmds()) {
               synchronized (bVisit) {
                  try {
                     bVisit.reset();
                     add.accept(bVisit);
                     if (bVisit.isTrue()) {
                        bVisit.reset();
                        POSPrinterCmd c = (POSPrinterCmd)this.getCmdList().get(this.getCmdList().size() - 1);
                        c.accept(bVisit);
                        if (bVisit.isTrue()) {
                           this.getCmdList().remove(c);
                        }
                     }
                  } catch (ClassCastException var6) {
                     if (PrinterWriter.getTracer().isOn()) {
                        PrinterWriter.getTracer().print(var6);
                     }
                  }
               }
            }

            if (null == this.innerList) {
               this.innerList = new ArrayList(20);
               this.innerList.add(this);
            }

            this.innerList.add(these);
            these.setMasterCmd(this);
            add.setAsyncCmd(this.isAsyncCmd());
            if (add.isOutputCmd()) {
               ((DefaultPOSPrinterCmd)this.getOwnerCmd()).setOutputCmd();
            }

            this.impliedStations = (byte)(this.impliedStations | add.getStation());
            these.setOwner(this.getOwnerCmd());
            this.crucialCmd = add;
            this.setMaxCharCnt(add.charCnt());
         }
      }
   }

   public void appendSplitMembers(DefaultPOSPrinterCmd split) throws IllegalStateException {
      if (split.isSlave()) {
         IllegalStateException te = new IllegalStateException(split.toString());
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().print(te);
         }

         throw te;
      } else {
         if (null == this.innerList) {
            this.innerList = new ArrayList(20);
         }

         split.setMasterCmd(this);
         int idx = -1;
         if ((idx = this.innerList.indexOf(this)) == -1) {
            this.innerList.add(this.splitCnt, split);
         } else {
            this.innerList.add(idx, split);
            this.innerList.remove(this);
         }

         this.splitCrucialCmd = split;
         if (this.isOutputCmd()) {
            split.setOutputCmd();
         }

         this.impliedStations = (byte)(this.impliedStations | split.getStation());
         split.setAsyncCmd(this.isAsyncCmd());
         this.splitCnt++;
         split.setOwner(this.getOwnerCmd());
         if (split.isOutputCmd()) {
            ((DefaultPOSPrinterCmd)this.getOwnerCmd()).setOutputCmd();
         }

         this.setMaxCharCnt(split.charCnt());
      }
   }

   public void addAssociatedCmd(POSPrinterCmd ac) {
      if (null != ac) {
         if (null == this.associated) {
            this.associated = new LinkedList();
         }

         this.associated.add(ac);
         ((DefaultPOSPrinterCmd)ac).setMasterCmd(this);
         ((DefaultPOSPrinterCmd)ac).setOwner(this);
      }
   }

   public POSPrinterCmd getOwnerCmd() {
      return this.getMasterCmd() != null ? ((DefaultPOSPrinterCmd)this.getMasterCmd()).getOwnerCmd() : this.owner;
   }

   public POSPrinterCmd getMasterCmd() {
      return this.master;
   }

   public boolean isSlave() {
      return this.bools.get(2);
   }

   public boolean hasListCmds() {
      return null == this.innerList ? false : !this.innerList.isEmpty();
   }

   public List getCmdList() {
      return this.innerList;
   }

   public Iterator iterator() throws IllegalStateException {
      return this.innerList.iterator();
   }

   public int getAppendCnt() {
      return null != this.innerList ? this.innerList.size() : 0;
   }

   public DefaultPOSPrinterCmd generatePrintCmds() {
      PrintCmd outCmd = this.buildPrintCmd();
      this.setHandlePrintCmd(outCmd);
      return this;
   }

   public boolean isClean() {
      return this.cleaned;
   }

   public void setID(byte id) {
      this.id = id;
   }

   public byte getID() {
      return this.id;
   }

   public void setOutputCmd() {
      this.bools.set(4);
   }

   protected void setPrintCmd(PrintCmd pcmd) {
      this.printCmd = pcmd;
   }

   protected void setMasterCmd(DefaultPOSPrinterCmd nmaster) {
      this.master = nmaster;
      this.bools.set(2);
   }

   protected void setOwner(POSPrinterCmd owner) {
      this.owner = owner;
   }

   protected abstract PrintCmd buildPrintCmd();

   protected void setHandlePrintCmd(PrintCmd p) {
      p.setHandleCmd(this);
      this.setPrintCmd(p);
   }

   protected void completeCleanup() {
      try {
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println(">cmdCleanup " + this);
         }

         this.bools.set(7);
         if (null == this.manager) {
            return;
         }

         this.manager.remove(this.getPrintCmd());
         if (null != this.stationCmd) {
            this.stationCmd.completeCleanup();
         }

         if (null == this.manager) {
            return;
         }

         if (0 == this.manager.getCmdCount()) {
            this.manager.recycle();
         }

         this.manager = null;
         if (null != this.innerList && 0 <= this.innerList.size()) {
            for (int i = 0; i < this.innerList.size(); i++) {
               DefaultPOSPrinterCmd dcmd = (DefaultPOSPrinterCmd)this.innerList.get(i);
               if (null != dcmd && !this.equals(dcmd)) {
                  dcmd.completeCleanup();
               }
            }
         }

         if (null != this.associated) {
            for (int ix = 0; ix < this.associated.size(); ix++) {
               DefaultPOSPrinterCmd dcmd = (DefaultPOSPrinterCmd)this.associated.remove(ix);
               if (null != dcmd && !this.equals(dcmd)) {
                  dcmd.completeCleanup();
               }
            }
         }
      } catch (Exception var3) {
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println(var3);
         }
      }
   }

   public static class AlignPositionCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.AlignPositionCmd {
      AlignPositionCmd(HandleCmd.Factory factory, String name, byte station, byte option) {
         super(factory, name, station, option);
      }

      public byte getOption() {
         return this.getNumber();
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitAlignPositionCmd(this);
      }

      public PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createAlignPositionCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class AlignmentCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.AlignmentCmd {
      AlignmentCmd(HandleCmd.Factory factory, String name, byte station, byte type) {
         super(factory, name, station, type);
      }

      public byte getAlignment() {
         return this.getNumber();
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createAlignmentCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class AlterWideHighCmd extends DefaultPOSPrinterCmd.ShortStoringCmd implements POSPrinterCmd.AlterWideHighCmd {
      AlterWideHighCmd(HandleCmd.Factory factory, String name, short option) {
         super(factory, name, (byte)0, option);
      }

      public short getOption() {
         return this.getNumber();
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitAlterWideHighCmd(this);
      }

      public PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createAlterWideHighCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class BoldCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd implements POSPrinterCmd.BoldCmd {
      BoldCmd(HandleCmd.Factory factory, String name, boolean active) {
         super(factory, name, active);
      }

      public boolean isActive() {
         return this.getBoolean();
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitBoldCmd(this);
      }

      public PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getFontCmdFactory().createBoldCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class BoldVisitor extends DefaultPOSPrinterCmdV {
      boolean flag = false;

      public void reset() {
         this.flag = false;
      }

      public boolean isTrue() {
         return this.flag;
      }

      public void visit(POSPrinterCmd x) {
         this.flag = false;
      }

      public void visitBoldCmd(POSPrinterCmd.BoldCmd boldCmd) {
         this.flag = true;
      }
   }

   public abstract static class BooleanStoringCmd extends DefaultPOSPrinterCmd.ByteStoringCmd {
      protected boolean bool;

      public BooleanStoringCmd(HandleCmd.Factory factory, String name, byte station, boolean bool, byte idata) {
         super(factory, name, station, idata);
         this.bool = bool;
      }

      public BooleanStoringCmd(HandleCmd.Factory factory, String name, boolean bool) {
         super(factory, name, (byte)0, (byte)0);
         this.bool = bool;
      }

      public boolean getBoolean() {
         return this.bool;
      }
   }

   public abstract static class BufferStoringCmd extends DefaultPOSPrinterCmd.ByteStoringCmd {
      protected ByteBuffer buffer;

      public BufferStoringCmd(HandleCmd.Factory factory, String name, byte station, ByteBuffer buff, byte idata) {
         super(factory, name, station, idata);
         this.buffer = buff;
      }

      public ByteBuffer getByteBuffer() {
         return this.buffer;
      }

      public void clean() {
         this.buffer.recycle();
         super.clean();
      }
   }

   public abstract static class ByteArrayStoringCmd extends DefaultPOSPrinterCmd.ByteStoringCmd {
      protected byte[] array;

      public ByteArrayStoringCmd(HandleCmd.Factory factory, String name, byte station, byte[] array, byte idata) {
         super(factory, name, station, idata);
         this.array = array;
      }

      public byte[] getArray() {
         return this.array;
      }

      public void setArray(byte[] array) {
         this.array = array;
      }
   }

   public abstract static class ByteStoringCmd extends DefaultPOSPrinterCmd {
      protected byte data;

      public ByteStoringCmd(HandleCmd.Factory factory, String name, byte station, byte x) {
         super(factory, name, station);
         this.setNumber(x);
      }

      public byte getNumber() {
         return this.data;
      }

      public void setNumber(byte b) {
         this.data = b;
      }
   }

   public static class CutPaperCmd extends DefaultPOSPrinterCmd.ShortStoringCmd implements POSPrinterCmd.CutPaperCmd {
      short linesToPaperCut = 0;

      CutPaperCmd(HandleCmd.Factory factory, String name, short percent) {
         super(factory, name, (byte)2, percent);
         this.setOutputCmd();
      }

      CutPaperCmd(HandleCmd.Factory factory, String name, short percent, short lines) {
         super(factory, name, (byte)2, percent);
         this.linesToPaperCut = lines;
         this.setOutputCmd();
      }

      public short getPercentage() {
         return this.getNumber();
      }

      public short getLinesToPaperCut() {
         return this.linesToPaperCut;
      }

      public PrintCmd buildPrintCmd() {
         return this.factory.getPrintCmdFactory().getGeneralFactory().createCutPaperCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitCutPaperCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         visitor.visitCutPaperCmd(this);
      }
   }

   public static class DotSpacingCmd extends DefaultPOSPrinterCmd.IntStoringCmd implements POSPrinterCmd.DotSpacingCmd {
      public boolean dbcs = false;

      DotSpacingCmd(HandleCmd.Factory factory, String name, byte station, int dots, boolean dbcs) {
         super(factory, name, station, dots);
         this.dbcs = dbcs;
      }

      public void setDotSpacing(int dots) {
         this.setNumber(dots);
      }

      public int getDotSpacing() {
         return this.getNumber();
      }

      public boolean isDBCS() {
         return this.dbcs;
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitDotSpacingCmd(this);
      }

      public PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createDotSpacingCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class EjectSlipCmd extends DefaultPOSPrinterCmd implements POSPrinterCmd.EjectSlipCmd {
      EjectSlipCmd(HandleCmd.Factory factory, String name, byte station) {
         super(factory, name, station);
      }

      protected PrintCmd buildPrintCmd() {
         return this.factory.getMICRCmdFactory().createEjectSlipCmd(this);
      }
   }

   public static class EnableColorModeCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd implements POSPrinterCmd.EnableColorModeCmd {
      EnableColorModeCmd(HandleCmd.Factory factory, String name, byte station, boolean active) {
         super(factory, name, station, active, (byte)0);
      }

      public boolean isActive() {
         return this.getBoolean();
      }

      public PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createEnableColorModeCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class EndRegisterSlipCmd extends DefaultPOSPrinterCmd implements POSPrinterCmd.EndRegisterSlipCmd {
      EndRegisterSlipCmd(HandleCmd.Factory factory, String name, byte station) {
         super(factory, name, station);
      }

      protected PrintCmd buildPrintCmd() {
         return this.factory.getMICRCmdFactory().createEndRegisterSlipCmd(this);
      }
   }

   public static class EraseFlashSectorCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.EraseFlashSectorCmd {
      public EraseFlashSectorCmd(HandleCmd.Factory factory, String name, byte sector) {
         super(factory, name, (byte)0, sector);
      }

      public byte getSector() {
         return this.getNumber();
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitEraseFlashSectorCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         visitor.visitEraseFlashSectorCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getGeneralFactory().createEraseFlashSectorCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class Factory extends DefaultSystemCmd.Factory implements POSPrinterCmd.Factory {
      protected PrintCmdFactory pcFactory = null;

      protected void setPrintCmdFactory(PrintCmdFactory c) {
         this.pcFactory = c;
      }

      public PrintCmdFactory getPrintCmdFactory() {
         return this.pcFactory;
      }

      public GeneralCmdFactory getGeneralCmdFactory() {
         return this.pcFactory.getGeneralFactory();
      }

      public MICRCmdFactory getMICRCmdFactory() {
         return null;
      }

      public CheckScannerCmdFactory getCheckScannerCmdFactory() {
         return null;
      }

      public GraphicCmdFactory getGraphicCmdFactory() {
         return this.pcFactory.getGraphicFactory();
      }

      public FontCmdFactory getFontCmdFactory() {
         return this.pcFactory.getFontFactory();
      }

      public POSPrinterCmd createSelectStationCmd(byte station) {
         return new GeneralPOSPrinterCmd.SelectStationCmd(this, "SelectStationCmd", station);
      }

      public POSPrinterCmd createMarkFeedCmd(byte type) {
         return new DefaultPOSPrinterCmd.MarkFeedCmd(this, "MarkFeedCmd", type);
      }

      public POSPrinterCmd createPrintBarCodeCmd(byte station, String data, int symbol, byte h, byte w, byte align, byte textPos) {
         return new DefaultPOSPrinterCmd.PrintBarCodeCmd(this, "PrintBarCodeCmd", station, data, symbol, h, w, align, textPos);
      }

      public POSPrinterCmd createPrintBitmapCmd(byte station, String fileName, byte[] bitmapStream, byte width, byte height, byte density, byte alignment) {
         return new DefaultPOSPrinterCmd.PrintBitmapCmd(this, "PrintBitmapCmd", station, fileName, bitmapStream, width, height, density, alignment);
      }

      public POSPrinterCmd createPrintBitmapCmd(
         byte station, String fileName, byte[] bitmapStream, int blockIndex, byte width, byte height, byte density, byte alignment
      ) {
         return new DefaultPOSPrinterCmd.PrintBitmapCmd(this, "PrintBitmapCmd", station, fileName, bitmapStream, blockIndex, width, height, density, alignment);
      }

      public POSPrinterCmd createPrintSetBitmapCmd(byte station, byte bitmapNo, byte density, byte width, byte height, byte alignment, int margin) {
         return new DefaultPOSPrinterCmd.PrintSetBitmapCmd(this, "PrintSetBitmapCmd", station, bitmapNo, density, width, height, alignment, margin);
      }

      public POSPrinterCmd createSetBitmapCmd(
         byte station, String fileName, byte[] bitmapStream, byte bitmapNo, byte width, byte height, byte alignment, int margin
      ) {
         return new DefaultPOSPrinterCmd.SetBitmapCmd(this, "SetBitmapCmd", station, fileName, bitmapStream, bitmapNo, width, height, alignment, margin);
      }

      public POSPrinterCmd createSetBitmapCmd(
         byte station, String fileName, byte[] bitmapStream, byte bitmapNo, int width, byte height, byte alignment, int margin
      ) {
         return new DefaultPOSPrinterCmd.SetBitmapCmd(this, "SetBitmapCmd", station, fileName, bitmapStream, bitmapNo, width, height, alignment, margin);
      }

      public POSPrinterCmd createPrintSetLogoCmd(byte station, byte location) {
         return new DefaultPOSPrinterCmd.PrintSetLogoCmd(this, "PrintSetLogoCmd", station, location);
      }

      public POSPrinterCmd createSetLogoCmd(byte location, String data) {
         return new DefaultPOSPrinterCmd.SetLogoCmd(this, "SetLogoCmd", location, data);
      }

      public POSPrinterCmd createDualPrintCmd(byte stations, POSPrinterCmd cmd1, POSPrinterCmd cmd2) {
         return new GeneralPOSPrinterCmd.DualPrintCmd(this, "DualPrintCmd", stations, cmd1, cmd2);
      }

      public POSPrinterCmd createPrintNormalCmd(byte station, String data) {
         return new GeneralPOSPrinterCmd.PrintNormalCmd(this, "PrintNormalCmd", station, data);
      }

      public POSPrinterCmd createPrintNormalCmd(byte station, byte[] array, int index, int count, byte appendByte) {
         return new GeneralPOSPrinterCmd.PrintNormalCmd(this, "PrintNormalCmd", station, array, index, count, appendByte);
      }

      public POSPrinterCmd createPrintNormalCmd(byte station, ByteBuffer byteBuffer, int index, int count, byte appendByte) {
         return new GeneralPOSPrinterCmd.PrintNormalCmd(this, "PrintNormalCmd", station, byteBuffer, index, count, appendByte);
      }

      public POSPrinterCmd createPrintImmediateCmd(byte station, String data) {
         return new GeneralPOSPrinterCmd.PrintNormalCmd(this, "PrintImmediateCmd", station, data, true);
      }

      public POSPrinterCmd createRotatePrintCmd(byte station, int rotation) {
         return new DefaultPOSPrinterCmd.RotatePrintCmd(this, "RotatePrintCmd", station, rotation);
      }

      public POSPrinterCmd createDevInfoCmd(boolean buffered) {
         return new GeneralPOSPrinterCmd.DevInfoCmd(this, "DevInfoCmd", buffered);
      }

      public POSPrinterCmd createTextAttribCmd(byte station, byte attrib, boolean enable) {
         return new DefaultPOSPrinterCmd.TextAttribCmd(this, "TextAttribCmd", station, attrib, enable);
      }

      public POSPrinterCmd createAlignmentCmd(byte station, byte type) {
         return new DefaultPOSPrinterCmd.AlignmentCmd(this, "AlignmentCmd", station, type);
      }

      public POSPrinterCmd createScaleFontCmd(byte station, byte width, byte height) {
         return new DefaultPOSPrinterCmd.ScaleFontCmd(this, "ScaleFontCmd", station, width, height);
      }

      public POSPrinterCmd createPrinterResetCmd() {
         return new GeneralPOSPrinterCmd.ResetCmd(this, "ResetCmd");
      }

      public POSPrinterCmd createSetUserDefinedCharCmd(byte station, byte codePage, byte start, byte end, byte[] data) {
         return new DefaultPOSPrinterCmd.SetUserDefinedCharCmd(this, "SetUserDefinedCharCmd", station, codePage, start, end, data);
      }

      public POSPrinterCmd createSetProportionalCharCmd(byte station, byte codePage, byte start, byte end, byte[] data) {
         return new DefaultPOSPrinterCmd.SetProportionalCharCmd(this, "SetProportionalCharCmd", station, codePage, start, end, data);
      }

      public POSPrinterCmd createEraseFlashSectorCmd(byte sector) {
         return new DefaultPOSPrinterCmd.EraseFlashSectorCmd(this, "EraseFlashCmd", sector);
      }

      public POSPrinterCmd createSelectCodePageCmd(byte codePage) {
         return new DefaultPOSPrinterCmd.SelectCodePageCmd(this, "SelectCodePageCmd", codePage);
      }

      public POSPrinterCmd createLineSpacingCmd(byte station, byte dots) {
         return new DefaultPOSPrinterCmd.LineSpacingCmd(this, "LineSpacingCmd", station, dots);
      }

      public POSPrinterCmd createFormFeedLengthCmd(byte station, byte len) {
         return new DefaultPOSPrinterCmd.FormFeedLengthCmd(this, "FeedLengthCmd", station, len);
      }

      public POSPrinterCmd createSetTabStopsCmd(byte station, int[] tabStops, boolean center) {
         return new DefaultPOSPrinterCmd.SetTabStopsCmd(this, "SetTabStopsCmd", station, tabStops, center);
      }

      public POSPrinterCmd createLeftMarginCmd(byte station, byte dots) {
         return new DefaultPOSPrinterCmd.LeftMarginCmd(this, "LeftMarginCmd", station, dots);
      }

      public POSPrinterCmd createRelativePositionCmd(byte station, byte offLeftMargin) {
         return new DefaultPOSPrinterCmd.RelativePositionCmd(this, "RelativePositionCmd", station, offLeftMargin);
      }

      public POSPrinterCmd createRegisterSlipCmd() {
         return new DefaultPOSPrinterCmd.RegisterSlipCmd(this, "RegisterSlipCmd", (byte)4, false);
      }

      public POSPrinterCmd createRegisterSlipCmd(boolean t) {
         return new DefaultPOSPrinterCmd.RegisterSlipCmd(this, "RegisterSlipCmd", (byte)4, t);
      }

      public POSPrinterCmd createEjectSlipCmd() {
         return new DefaultPOSPrinterCmd.EjectSlipCmd(this, "EjectSlipCmd", (byte)4);
      }

      public POSPrinterCmd createReadSlipCmd() {
         return new DefaultPOSPrinterCmd.ReadSlipCmd(this, "ReadSlipCmd", (byte)4);
      }

      public POSPrinterCmd createEndRegisterSlipCmd() {
         return new DefaultPOSPrinterCmd.EndRegisterSlipCmd(this, "EndRegisterSlipCmd", (byte)4);
      }

      public POSPrinterCmd createMCTValueCmd(byte matrixValue, byte byteWideV, byte byteHeightV) {
         return new DefaultPOSPrinterCmd.MCTValueCmd(this, "MCTValueCmd", matrixValue, byteWideV, byteHeightV);
      }

      public POSPrinterCmd createMCTReadCmd(byte mct) {
         return new DefaultPOSPrinterCmd.MCTCmd(this, "MCTReadCmd", mct);
      }

      public POSPrinterCmd createPrintQualityCmd(byte station, boolean quality) {
         return new DefaultPOSPrinterCmd.PrintQualityCmd(this, "PrintQualityCmd", station, quality);
      }

      public POSPrinterCmd createUnderlineCmd(short thickness) {
         return new DefaultPOSPrinterCmd.UnderlineCmd(this, "UnderlineCmd", thickness);
      }

      public POSPrinterCmd createBoldCmd(boolean active) {
         return new DefaultPOSPrinterCmd.BoldCmd(this, "BoldCmd", active);
      }

      public POSPrinterCmd createCutPaperCmd(short percent) {
         return new DefaultPOSPrinterCmd.CutPaperCmd(this, "CutPaperCmd", percent);
      }

      public POSPrinterCmd createCutPaperCmd(short percent, short linesToCut) {
         return new DefaultPOSPrinterCmd.CutPaperCmd(this, "CutPaperCmd", percent, linesToCut);
      }

      public POSPrinterCmd createReverseVideoCmd(boolean active) {
         return new DefaultPOSPrinterCmd.ReverseVideoCmd(this, "ReverseVideoCmd", active);
      }

      public POSPrinterCmd createAlterWideHighCmd(short option) {
         return new DefaultPOSPrinterCmd.AlterWideHighCmd(this, "AlterWideHighCmd", option);
      }

      public POSPrinterCmd createFeedLinesCmd(short lines) {
         return new DefaultPOSPrinterCmd.FeedLinesCmd(this, "FeedLinesCmd", lines);
      }

      public POSPrinterCmd createFeedLinesCmd(short lines, byte station) {
         return new DefaultPOSPrinterCmd.FeedLinesCmd(this, "FeedLinesCmd", lines, station);
      }

      public POSPrinterCmd createFeedUnitsCmd(short lines) {
         return new DefaultPOSPrinterCmd.FeedUnitsCmd(this, "FeedUnitsCmd", lines);
      }

      public POSPrinterCmd createFeedReverseCmd(short lines) {
         return new DefaultPOSPrinterCmd.FeedReverseCmd(this, "FeedReverseCmd", lines);
      }

      public POSPrinterCmd createNormalModeCmd(boolean active) {
         return new DefaultPOSPrinterCmd.NormalModeCmd(this, "NormalModeCmd", active);
      }

      public POSPrinterCmd createAlignPositionCmd(byte station, byte option) {
         return new DefaultPOSPrinterCmd.AlignPositionCmd(this, "AlignPositionCmd", station, option);
      }

      public POSPrinterCmd createFontTypeCmd(byte station, byte type) {
         return new DefaultPOSPrinterCmd.FontTypeCmd(this, "FontTypeCmd", station, type);
      }

      public POSPrinterCmd createFontColorCmd(byte color) {
         return new DefaultPOSPrinterCmd.FontColorCmd(this, "FontTypeCmd", color);
      }

      public POSPrinterCmd createDotSpacingCmd(byte station, int dots) {
         return new DefaultPOSPrinterCmd.DotSpacingCmd(this, "DotSpacingCmd", station, dots, false);
      }

      public POSPrinterCmd createDotSpacingCmd(byte station, int dots, boolean db) {
         return new DefaultPOSPrinterCmd.DotSpacingCmd(this, "DotSpacingCmd", station, dots, db);
      }

      public POSPrinterCmd createSetLeftMarginCmd(byte station, int margin) {
         return new DefaultPOSPrinterCmd.SetLeftMarginCmd(this, "SetLeftMarginCmd", station, margin);
      }

      public POSPrinterCmd createEnableColorModeCmd(byte station, boolean active) {
         return new DefaultPOSPrinterCmd.EnableColorModeCmd(this, "EnableColorModeCmd", station, active);
      }

      public POSPrinterCmd createPassThruCmd(byte[] data) {
         return new DefaultPOSPrinterCmd.PassThruCmd(this, "PassThruCmd", data);
      }

      public SystemCmd.TestRequestCmd createTestRequestCmd() {
         return new GeneralPOSPrinterCmd.TestReqCmd(this, "TestReqCmd");
      }

      public SystemCmd.StatusRequestCmd createStatusRequestCmd() {
         return new GeneralPOSPrinterCmd.StatusRequestCmd(this, "StatusRequestCmd");
      }

      public SystemCmd.DeviceInfoRequestCmd createDeviceInfoRequestCmd() {
         return new GeneralPOSPrinterCmd.DevInfoCmd(this, "DeviceInfoCmd", true);
      }

      public SystemCmd.ResetRequestCmd createResetCmd() {
         return new GeneralPOSPrinterCmd.ResetCmd(this, "PrinterResetCmd");
      }

      public SystemCmd.StatisticCmd createStatisticCmd(String statisticType) {
         return new GeneralPOSPrinterCmd.StatisticCmd(this, "StatisticCmd", statisticType);
      }
   }

   public static class FeedLinesCmd extends DefaultPOSPrinterCmd.ShortStoringCmd implements POSPrinterCmd.FeedLinesCmd {
      FeedLinesCmd(HandleCmd.Factory factory, String name, short lines) {
         super(factory, name, (byte)0, lines);
         this.setOutputCmd();
      }

      FeedLinesCmd(HandleCmd.Factory factory, String name, short lines, byte station) {
         super(factory, name, station, lines);
         this.setOutputCmd();
      }

      public short getLines() {
         return this.getNumber();
      }

      public PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getGeneralFactory().createFeedLinesCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }

      public void accept(StatusVisitor visitor) {
         visitor.visitFeedLinesCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitFeedLinesCmd(this);
      }
   }

   public static class FeedReverseCmd extends DefaultPOSPrinterCmd.ShortStoringCmd implements POSPrinterCmd.FeedReverseCmd {
      FeedReverseCmd(HandleCmd.Factory factory, String name, short lines) {
         super(factory, name, (byte)4, lines);
         this.setOutputCmd();
      }

      public short getLines() {
         return this.getNumber();
      }

      public PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getGeneralFactory().createFeedReverseCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class FeedUnitsCmd extends DefaultPOSPrinterCmd.ShortStoringCmd implements POSPrinterCmd.FeedUnitsCmd {
      FeedUnitsCmd(HandleCmd.Factory factory, String name, short units) {
         super(factory, name, (byte)0, units);
         this.setOutputCmd();
      }

      public short getUnits() {
         return this.getNumber();
      }

      public PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getGeneralFactory().createFeedUnitsCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }

      public void accept(StatusVisitor visitor) {
         visitor.visitFeedUnitsCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitFeedUnitsCmd(this);
      }
   }

   public static class FontColorCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.FontColorCmd {
      FontColorCmd(HandleCmd.Factory factory, String name, byte color) {
         super(factory, name, (byte)2, color);
      }

      public byte getFontColor() {
         return this.getNumber();
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitFontColorCmd(this);
      }

      public PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createFontColorCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class FontTypeCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.FontTypeCmd {
      FontTypeCmd(HandleCmd.Factory factory, String name, byte station, byte type) {
         super(factory, name, station, type);
      }

      public void setFontType(byte type) {
         this.setNumber(type);
      }

      public byte getFontType() {
         return this.getNumber();
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitFontTypeCmd(this);
      }

      public PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createFontTypeCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class FormFeedLengthCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.FormFeedLengthCmd {
      FormFeedLengthCmd(HandleCmd.Factory factory, String name, byte station, byte len) {
         super(factory, name, station, len);
         this.setOutputCmd();
      }

      public byte getFormFeedLength() {
         return this.getNumber();
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getGeneralFactory().createFormFeedLengthCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public abstract static class IntStoringCmd extends DefaultPOSPrinterCmd {
      protected int data;

      public IntStoringCmd(HandleCmd.Factory factory, String name, byte station, int x) {
         super(factory, name, station);
         this.data = x;
      }

      public int getNumber() {
         return this.data;
      }

      public void setNumber(int n) {
         this.data = n;
      }
   }

   public static class LeftMarginCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.LeftMarginCmd {
      LeftMarginCmd(HandleCmd.Factory factory, String name, byte station, byte dots) {
         super(factory, name, station, dots);
      }

      public byte getDots() {
         return this.getNumber();
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createLeftMarginCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class LineSpacingCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.LineSpacingCmd {
      LineSpacingCmd(HandleCmd.Factory factory, String name, byte station, byte dots) {
         super(factory, name, station, dots);
      }

      public byte getDots() {
         return this.getNumber();
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createLineSpacingCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class MCTCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.MCTCmd {
      public MCTCmd(HandleCmd.Factory factory, String name, byte mctNo) {
         super(factory, name, (byte)0, mctNo);
      }

      public byte getMCT() {
         return this.getNumber();
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitDataRequesterCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         visitor.visitDataRequesterCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getGeneralFactory().createMCTCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class MCTValueCmd extends DefaultPOSPrinterCmd implements POSPrinterCmd.MCTValueCmd {
      private byte matrixValue = 0;
      private byte byteWideV = 0;
      private byte byteHeightV = 0;

      public MCTValueCmd(HandleCmd.Factory factory, String name, byte value, byte bw, byte bh) {
         super(factory, name);
         this.matrixValue = value;
         this.byteWideV = bw;
         this.byteHeightV = bh;
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getGeneralCmdFactory().createMCTValueCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }

      public byte getMatrixValue() {
         return this.matrixValue;
      }

      public byte getByteHighValue() {
         return this.byteWideV;
      }

      public byte getByteLowValue() {
         return this.byteHeightV;
      }
   }

   public static class MarkFeedCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.MarkFeedCmd {
      MarkFeedCmd(HandleCmd.Factory factory, String name, byte type) {
         super(factory, name, (byte)2, type);
      }

      public byte getType() {
         return this.getNumber();
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getGeneralFactory().createMarkFeedCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class NormalModeCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd implements POSPrinterCmd.NormalModeCmd {
      AsyncBitSet settings = new AsyncBitSet(7);
      boolean active = false;
      public static final int WIDE_HIGH = 6;
      public static final int BOLD = 5;
      public static final int UNDERLINE = 4;
      public static final int REVERSE_VIDEO = 3;
      public static final int SCALING = 2;
      public static final int ALIGNMENT = 1;
      public static final int COLOR = 0;

      NormalModeCmd(HandleCmd.Factory factory, String name, boolean active) {
         super(factory, name, active);
      }

      public boolean isActive() {
         return this.getBoolean();
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitNormalModeCmd(this);
      }

      public PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createNormalModeCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }

      public void setDoubleWideHighOn(boolean b) {
         if (b) {
            this.settings.set(6);
         } else {
            this.settings.clear(6);
         }

         this.active |= b;
      }

      public void setBoldOn(boolean b) {
         if (b) {
            this.settings.set(5);
         } else {
            this.settings.clear(5);
         }

         this.active |= b;
      }

      public void setUnderlineOn(boolean b) {
         if (b) {
            this.settings.set(4);
         } else {
            this.settings.clear(4);
         }

         this.active |= b;
      }

      public void setReverseVideoOn(boolean b) {
         if (b) {
            this.settings.set(3);
         } else {
            this.settings.clear(3);
         }

         this.active |= b;
      }

      public void setScaleOn(boolean b) {
         if (b) {
            this.settings.set(2);
         } else {
            this.settings.clear(2);
         }

         this.active |= b;
      }

      public void setAlignmentOn(boolean b) {
         if (b) {
            this.settings.set(1);
         } else {
            this.settings.clear(1);
         }

         this.active |= b;
      }

      public void setColorOn(boolean b) {
         if (b) {
            this.settings.set(0);
         } else {
            this.settings.clear(0);
         }

         this.active |= b;
      }

      public boolean isBoldOn() {
         return this.settings.get(5);
      }

      public boolean isUnderlineOn() {
         return this.settings.get(4);
      }

      public boolean isReverseVideoOn() {
         return this.settings.get(3);
      }

      public boolean isScaleOn() {
         return this.settings.get(2);
      }

      public boolean isDoubleWideHighOn() {
         return this.settings.get(6);
      }

      public boolean isAlignmentOn() {
         return this.settings.get(1);
      }

      public boolean isColorOn() {
         return this.settings.get(0);
      }
   }

   public static class PassThruCmd extends DefaultPOSPrinterCmd.ByteArrayStoringCmd implements POSPrinterCmd.PassThruCmd {
      public PassThruCmd(HandleCmd.Factory factory, String name, byte[] data) {
         super(factory, name, (byte)0, data, (byte)0);
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getGeneralFactory().createPassThruCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }

      public Iterator iterator() {
         return null;
      }

      public byte[] toBytes() {
         return super.getArray();
      }
   }

   public static class PrintBarCodeCmd extends DefaultPOSPrinterCmd.StationStringStoringCmd implements POSPrinterCmd.PrintBarCodeCmd {
      protected byte h;
      protected byte align;
      protected byte textPos;
      protected int symbol;
      protected int rotation;

      PrintBarCodeCmd(HandleCmd.Factory factory, String name, byte station, String data, int symbol, byte h, byte w, byte align, byte textPos) {
         super(factory, name, station, data, w);
         this.symbol = symbol;
         this.h = h;
         this.align = align;
         this.textPos = textPos;
         this.setOutputCmd();
      }

      public int getSymbol() {
         return this.symbol;
      }

      public byte getHeight() {
         return this.h;
      }

      public byte getWidth() {
         return this.getNumber();
      }

      public byte getAlignment() {
         return this.align;
      }

      public byte getTextPosition() {
         return this.textPos;
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getGraphicFactory().createPrintBarCodeCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }

      public boolean isContinueNeeded() {
         return true;
      }

      public int getRotation() {
         return this.rotation;
      }

      public void setRotation(int rotation) {
         this.rotation = rotation;
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitPrintBarCodeCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         visitor.visitPrintBarCodeCmd(this);
      }
   }

   public static class PrintBitmapCmd extends DefaultPOSPrinterCmd.StationStringStoringCmd implements POSPrinterCmd.PrintBitmapCmd {
      protected byte align;
      protected byte bNo;
      protected byte height;
      protected byte density;
      protected byte[] stream;
      protected int blockIndex = 0;
      protected int rotation = 0;

      PrintBitmapCmd(
         HandleCmd.Factory factory,
         String name,
         byte station,
         String fileName,
         byte[] bitmapStream,
         int blockIndex,
         byte width,
         byte h,
         byte density,
         byte alignment
      ) {
         super(factory, name, station, fileName, width);
         this.align = alignment;
         this.stream = bitmapStream;
         this.height = h;
         this.density = density;
         this.blockIndex = blockIndex;
         this.setOutputCmd();
      }

      PrintBitmapCmd(
         HandleCmd.Factory factory, String name, byte station, String fileName, byte[] bitmapStream, byte width, byte h, byte density, byte alignment
      ) {
         super(factory, name, station, fileName, width);
         this.align = alignment;
         this.stream = bitmapStream;
         this.height = h;
         this.density = density;
         this.blockIndex = 0;
         this.setOutputCmd();
      }

      public String getFileName() {
         return this.getData();
      }

      public byte getWidth() {
         return this.getNumber();
      }

      public byte getHeight() {
         return this.height;
      }

      public byte getAlignment() {
         return this.align;
      }

      public void setBitmapStream(byte[] bs) {
         this.stream = bs;
      }

      public byte[] getBitmapStream() {
         return this.stream;
      }

      public byte getDensity() {
         return this.density;
      }

      public int getRotation() {
         return this.rotation;
      }

      public void setRotation(int rotation) {
         this.rotation = rotation;
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getGraphicFactory().createPrintBitmapCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }

      public boolean isContinueNeeded() {
         return true;
      }

      public int getBlockIndex() {
         return this.blockIndex;
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitPrintBitmapCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         visitor.visitPrintBitmapCmd(this);
      }
   }

   public static class PrintQualityCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd implements POSPrinterCmd.PrintQualityCmd {
      PrintQualityCmd(HandleCmd.Factory factory, String name, byte station, boolean quality) {
         super(factory, name, station, quality, (byte)0);
      }

      public boolean getQuality() {
         return this.getBoolean();
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getFontCmdFactory().createPrintQualityCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class PrintSetBitmapCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.PrintSetBitmapCmd {
      private byte density = 0;
      private byte alignment = 0;
      private int mrgn = 0;

      PrintSetBitmapCmd(HandleCmd.Factory factory, String name, byte station, byte bitmapNo, byte density, byte width, byte height, byte alignment, int margin) {
         super(factory, name, station, bitmapNo);
         this.density = density;
         this.alignment = alignment;
         this.mrgn = margin;
         this.setOutputCmd();
      }

      public byte getDensity() {
         return this.density;
      }

      public byte getBitmapNo() {
         return this.getNumber();
      }

      public byte getAlignment() {
         return this.alignment;
      }

      public int getMargin() {
         return this.mrgn;
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getGraphicFactory().createPrintSetBitmapCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitPrintSetBitmapCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         visitor.visitPrintSetBitmapCmd(this);
      }
   }

   public static class PrintSetLogoCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.PrintSetLogoCmd {
      PrintSetLogoCmd(HandleCmd.Factory factory, String name, byte station, byte location) {
         super(factory, name, station, location);
         this.setOutputCmd();
      }

      public byte getLocation() {
         return this.getNumber();
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getGraphicFactory().createPrintSetLogoCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitPrintSetLogoCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         visitor.visitPrintSetLogoCmd(this);
      }
   }

   public static class ReadSlipCmd extends DefaultPOSPrinterCmd implements POSPrinterCmd.ReadSlipCmd {
      ReadSlipCmd(HandleCmd.Factory factory, String name, byte station) {
         super(factory, name, station);
         this.setOutputCmd();
      }

      protected PrintCmd buildPrintCmd() {
         return this.factory.getMICRCmdFactory().createReadSlipCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitReadSlipCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         visitor.visitReadSlipCmd(this);
      }
   }

   public static class RegisterSlipCmd extends DefaultPOSPrinterCmd implements POSPrinterCmd.RegisterSlipCmd {
      boolean grab;

      RegisterSlipCmd(HandleCmd.Factory factory, String name, byte station, boolean grab) {
         super(factory, name, station);
         this.grab = grab;
         if (!grab) {
            this.setMemoryCmd();
         }
      }

      public boolean isGrabber() {
         return this.grab;
      }

      protected PrintCmd buildPrintCmd() {
         return this.factory.getMICRCmdFactory().createRegisterSlipCmd(this);
      }
   }

   public static class RelativePositionCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.RelativePositionCmd {
      RelativePositionCmd(HandleCmd.Factory factory, String name, byte station, byte offLeftMargin) {
         super(factory, name, station, offLeftMargin);
      }

      public byte getOffset() {
         return this.getNumber();
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createRelativePositionCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class ReverseVideoCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd implements POSPrinterCmd.ReverseVideoCmd {
      ReverseVideoCmd(HandleCmd.Factory factory, String name, boolean active) {
         super(factory, name, active);
      }

      public boolean isActive() {
         return this.getBoolean();
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitReverseVideoCmd(this);
      }

      public PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createReverseVideoCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class RotatePrintCmd extends DefaultPOSPrinterCmd.IntStoringCmd implements POSPrinterCmd.RotatePrintCmd {
      RotatePrintCmd(HandleCmd.Factory factory, String name, byte station, int rotation) {
         super(factory, name, station, rotation);
      }

      public void appendPOSPrinterCmd(POSPrinterCmd c) {
         if (((DefaultPOSPrinterCmd)c).isOutputCmd()) {
            this.setRoleAsSettings(false);
         }

         super.appendPOSPrinterCmd(c);
      }

      public void appendSplitMembers(DefaultPOSPrinterCmd d) {
         if (d.isOutputCmd()) {
            this.setRoleAsSettings(false);
         }

         super.appendSplitMembers(d);
      }

      public int getRotation() {
         return this.getNumber();
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getFontCmdFactory().createRotatePrintCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitRotatePrintCmd(this);
      }
   }

   public static class ScaleFontCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.ScaleFontCmd {
      private byte height;

      ScaleFontCmd(HandleCmd.Factory factory, String name, byte station, byte width, byte height) {
         super(factory, name, station, width);
         this.height = height;
      }

      public byte getHeight() {
         return this.height;
      }

      public byte getWidth() {
         return this.getNumber();
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitScaleFontCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createScaleFontCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class SelectCodePageCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.SelectCodePageCmd {
      SelectCodePageCmd(HandleCmd.Factory factory, String name, byte codePage) {
         super(factory, name, (byte)0, codePage);
      }

      public byte getSelectedCodePage() {
         return this.getNumber();
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createSelectCodePageCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class SetBitmapCmd extends DefaultPOSPrinterCmd implements POSPrinterCmd.SetBitmapCmd {
      private byte bNo = 0;
      private byte align = 0;
      private byte wdth = 0;
      private byte hght = 0;
      private String fName = "";
      private byte[] streamData = new byte[]{0};
      private int mrgn = 0;
      private int intWidth = 0;

      public SetBitmapCmd(
         HandleCmd.Factory factory,
         String name,
         byte station,
         String fileName,
         byte[] bitmapStream,
         byte bitmapNo,
         byte width,
         byte h,
         byte alignment,
         int margin
      ) {
         super(factory, name, station);
         this.bNo = bitmapNo;
         this.fName = fileName;
         this.wdth = width;
         this.hght = h;
         this.align = alignment;
         this.streamData = bitmapStream;
         this.mrgn = margin;
      }

      public SetBitmapCmd(
         HandleCmd.Factory factory,
         String name,
         byte station,
         String fileName,
         byte[] bitmapStream,
         byte bitmapNo,
         int width,
         byte h,
         byte alignment,
         int margin
      ) {
         super(factory, name, station);
         this.bNo = bitmapNo;
         this.fName = fileName;
         this.intWidth = width;
         this.hght = h;
         this.align = alignment;
         this.streamData = bitmapStream;
         this.mrgn = margin;
      }

      public byte getBitmapNo() {
         return this.bNo;
      }

      public String getFileName() {
         return this.fName;
      }

      public byte getWidth() {
         return this.wdth;
      }

      public byte getHeight() {
         return this.hght;
      }

      public byte getAlignment() {
         return this.align;
      }

      public byte[] getBitmapStream() {
         return this.streamData;
      }

      public int getMargin() {
         return this.mrgn;
      }

      public int getIntegerWidth() {
         return this.intWidth;
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitSetBitmapCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getGraphicFactory().createSetBitmapCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }

      public boolean isContinueNeeded() {
         return true;
      }

      public void appendPOSPrinterCmd(POSPrinterCmd c) {
         if (((DefaultPOSPrinterCmd)c).isOutputCmd()) {
            c.setMemoryCmd();
         }

         super.appendPOSPrinterCmd(c);
      }

      public void appendSplitMembers(DefaultPOSPrinterCmd d) {
         if (d.isOutputCmd()) {
            d.setMemoryCmd();
         }

         super.appendSplitMembers(d);
      }
   }

   public static class SetLeftMarginCmd extends DefaultPOSPrinterCmd.IntStoringCmd implements POSPrinterCmd.SetLeftMarginCmd {
      SetLeftMarginCmd(HandleCmd.Factory factory, String cmdName, byte station, int margin) {
         super(factory, cmdName, station, margin);
      }

      public void setLeftMarginValue(int margin) {
         this.setNumber(margin);
      }

      public int getLeftMarginValue() {
         return this.getNumber();
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitSetLeftMarginCmd(this);
      }

      public PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createSetLeftMarginCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class SetLogoCmd extends DefaultPOSPrinterCmd.ByteStoringCmd implements POSPrinterCmd.SetLogoCmd {
      private String data;

      SetLogoCmd(HandleCmd.Factory factory, String name, byte location, String data) {
         super(factory, name, (byte)0, location);
         this.data = data;
      }

      public byte getLocation() {
         return this.getNumber();
      }

      public String getData() {
         return this.data;
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitSetLogoCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getGraphicFactory().createSetLogoCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }

      public boolean isContinueNeeded() {
         return true;
      }

      public void appendPOSPrinterCmd(POSPrinterCmd c) {
         if (((DefaultPOSPrinterCmd)c).isOutputCmd()) {
            c.setMemoryCmd();
         }

         super.appendPOSPrinterCmd(c);
      }

      public void appendSplitMembers(DefaultPOSPrinterCmd d) {
         if (d.isOutputCmd()) {
            d.setMemoryCmd();
         }

         super.appendSplitMembers(d);
      }
   }

   public static class SetProportionalCharCmd extends DefaultPOSPrinterCmd.SetUserDefinedCharCmd implements POSPrinterCmd.SetProportionalCharCmd {
      SetProportionalCharCmd(HandleCmd.Factory factory, String name, byte station, byte codePage, byte start, byte end, byte[] data) {
         super(factory, name, station, codePage, start, end, data);
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createSetProportionalCharCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class SetTabStopsCmd extends DefaultPOSPrinterCmd implements POSPrinterCmd.SetTabStopsCmd {
      private int[] tabStops = new int[]{0, 0, 0, 0, 0};
      private int tabSize = 0;
      boolean center = false;

      SetTabStopsCmd(HandleCmd.Factory factory, String name, byte station, int[] tabStops, boolean ctr) {
         super(factory, name, station);
         int i = 0;

         while (i < tabStops.length) {
            this.tabStops[i] = tabStops[i++];
         }

         this.tabSize = tabStops.length;
         this.center = ctr;
      }

      public int[] getTabStops() {
         return this.tabStops;
      }

      public int getTabSize() {
         return this.tabSize;
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createSetTabStopsCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }

      public boolean isCenter() {
         return this.center;
      }
   }

   public static class SetUserDefinedCharCmd extends DefaultPOSPrinterCmd.ByteArrayStoringCmd implements POSPrinterCmd.SetUserDefinedCharCmd {
      protected byte start = 0;
      protected byte end = 0;

      SetUserDefinedCharCmd(HandleCmd.Factory factory, String name, byte station, byte codePage, byte start, byte end, byte[] data) {
         super(factory, name, station, data, codePage);
         this.start = start;
         this.end = end;
         this.setMemoryCmd();
      }

      public byte getStart() {
         return this.start;
      }

      public byte[] getCharData() {
         return this.getArray();
      }

      public byte getCodePage() {
         return this.getNumber();
      }

      public byte getEnd() {
         return this.end;
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createSetUserDefinedCharCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }

      public void accept(StatusVisitor visitor) {
         visitor.visitFontDownloadCmd(this);
      }
   }

   public abstract static class ShortStoringCmd extends DefaultPOSPrinterCmd {
      private short data;

      public ShortStoringCmd(HandleCmd.Factory factory, String name, byte station, short x) {
         super(factory, name, station);
         this.data = x;
      }

      public short getNumber() {
         return this.data;
      }
   }

   public abstract static class StationStringStoringCmd extends DefaultPOSPrinterCmd.ByteStoringCmd {
      protected String data;

      public StationStringStoringCmd(HandleCmd.Factory factory, String name, byte station, String sdata, byte x) {
         super(factory, name, station, x);
         this.data = sdata;
      }

      public String getData() {
         return this.data;
      }
   }

   public static class TextAttribCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd implements POSPrinterCmd.TextAttribCmd {
      TextAttribCmd(HandleCmd.Factory factory, String name, byte station, byte attrib, boolean enable) {
         super(factory, name, station, enable, attrib);
      }

      public boolean enabled() {
         return this.getBoolean();
      }

      public byte getAttribute() {
         return this.getNumber();
      }

      protected PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getPrintCmdFactory().getFontFactory().createTextAttribCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }

   public static class UnderlineCmd extends DefaultPOSPrinterCmd.ShortStoringCmd implements POSPrinterCmd.UnderlineCmd {
      UnderlineCmd(HandleCmd.Factory factory, String name, short thickness) {
         super(factory, name, (byte)2, thickness);
      }

      public short getThickness() {
         return this.getNumber();
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         visitor.visitUnderlineCmd(this);
      }

      public PrintCmd buildPrintCmd() {
         PrintCmd pc = null;

         try {
            pc = this.factory.getFontCmdFactory().createUnderlineCmd(this);
         } catch (Exception var3) {
         }

         return pc;
      }
   }
}
