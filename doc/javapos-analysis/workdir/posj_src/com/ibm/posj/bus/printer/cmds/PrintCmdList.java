package com.ibm.posj.bus.printer.cmds;

import com.ibm.jutil.AsyncBitSet;
import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.patterns.factory.RecyclableObject;
import com.ibm.jutil.patterns.factory.RecycleFactory;
import com.ibm.jutil.patterns.factory.RecyclableObject.CtorArg;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.CmdCompleteStatusVisitor;
import com.ibm.posj.printer.CmdLoadedStatusVisitor;
import com.ibm.posj.printer.PrinterWriter;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

public abstract class PrintCmdList implements RecyclableObject {
   private boolean iOutput = false;
   private ByteBuffer writeBuffer = null;
   private Object parent = null;
   private boolean[] finisher = new boolean[]{false};
   private List listOCmds = new ArrayList(10);
   protected int completeIdx = 0;
   private int maxLen = 0;
   private int totalSize = 0;
   private BooleanMonitor blockKey = null;
   private AsyncBitSet bools = new AsyncBitSet(6);
   private byte uStations = 0;
   private boolean cleaned = true;
   protected boolean recycled = true;
   private boolean autoLoad = false;
   public static final int CMDLIST_INIT_SIZE = 10;
   public static final int WAIT_TO_START = 0;
   public static final int WAIT_TO_FINISH = 1;
   public static final int ERROR_FLAG = 2;
   public static final int DIRTY = 3;
   public static final int IMMEDIATE = 4;
   public static final int SINGLE_PRINT = 5;
   public static final int END = 6;

   protected PrintCmdList(int maxLen) {
      this.setMaxLength(maxLen);
   }

   public String getName() {
      return "PrintCmdList";
   }

   public byte getUsedStations() {
      return this.uStations;
   }

   public void setParent(Object o) {
      this.parent = o;
   }

   public Object getParent() {
      return this.parent;
   }

   public boolean hasPreDependency() {
      if (0 >= this.listOCmds.size()) {
         return false;
      } else {
         DefaultPOSPrinterCmd cmd = (DefaultPOSPrinterCmd)((PrintCmd)this.listOCmds.get(0)).getHandleCmd();
         return null == cmd ? false : cmd.isSlave();
      }
   }

   public void setWriteToBuffer(ByteBuffer w) {
      if (null != w) {
         if (null != this.writeBuffer) {
            this.writeBuffer.recycle();
         }

         this.writeBuffer = w;
         this.bools.set(3);
      }
   }

   public boolean isOutputList() {
      return this.iOutput;
   }

   public ByteBuffer getWriteBuffer() {
      if (this.isDirty()) {
         this.buildData();
      }

      return this.writeBuffer;
   }

   public boolean compareStations(byte stations) {
      if ((this.uStations & stations) == 0) {
         return false;
      } else {
         int size = this.listOCmds.size();
         boolean b = false;

         for (int i = 0; i < size; i++) {
            PrintCmd pcmd = (PrintCmd)this.listOCmds.get(i);
            POSPrinterCmd cmd = pcmd.getHandleCmd();
            if (null != cmd) {
               b = cmd.compareStations(stations);
               if (b) {
                  break;
               }
            }
         }

         return b;
      }
   }

   public PrintCmd removeLastCmd() {
      if (this.listOCmds.isEmpty()) {
         return null;
      } else {
         PrintCmd cmd = (PrintCmd)this.listOCmds.remove(this.listOCmds.size() - 1);
         this.totalSize = this.totalSize - cmd.getDataSize();
         this.bools.set(3);
         this.bools.clear(5);
         int size = this.listOCmds.size();

         for (int i = 0; i < size; i++) {
            this.copyPrintCmd((PrintCmd)this.listOCmds.get(i));
         }

         return cmd;
      }
   }

   public boolean addCommand(PrintCmd cmd) {
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("curr size " + this.getDataSize() + " adding dataSize " + cmd.getDataSize() + " of " + cmd);
      }

      if ((this.maxLen >= this.getDataSize() + cmd.getDataSize() || 0 >= this.listOCmds.size()) && null != cmd) {
         if (!this.testPreWriteBuffer((DefaultPOSPrinterCmd)cmd.getHandleCmd())) {
            return false;
         } else {
            this.iOutput = this.iOutput | cmd.getHandleCmd().isOutputCmd();
            boolean flag = true;

            while (flag) {
               try {
                  this.listOCmds.add(cmd);
                  flag = false;
               } catch (ArrayIndexOutOfBoundsException var4) {
                  PrinterWriter.getTracer().println("" + this.listOCmds.size());
                  PrinterWriter.getTracer().print(var4);
               }
            }

            this.totalSize = this.totalSize + cmd.getDataSize();
            this.copyPrintCmd(cmd);
            DefaultPOSPrinterCmd f = (DefaultPOSPrinterCmd)cmd.getHandleCmd();
            f.addToManager(this);
            this.bools.set(3);
            this.bools.clear(5);
            if (cmd.isAutoLoad()) {
               this.setAutoLoad(true);
            } else if (cmd.isAutoLoadKnown()) {
               this.setAutoLoad(false);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public void forceImmediateCmd(PrintCmd force) {
      this.listOCmds.add(0, force);
      this.totalSize = this.totalSize + force.getDataSize();
      this.copyPrintCmd(force);
      DefaultPOSPrinterCmd f = (DefaultPOSPrinterCmd)force.getHandleCmd();
      if (null != f) {
      }

      f.addToManager(this);
      this.bools.set(3);
      this.bools.clear(5);
   }

   public boolean addImmediateCommand(PrintCmd cmd) {
      if (this.maxLen >= this.getDataSize() + cmd.getDataSize() && null != cmd) {
         int i;
         for (i = 0; i < this.listOCmds.size(); i++) {
            POSPrinterCmd pa = ((PrintCmd)this.listOCmds.get(i)).getHandleCmd();
            if (pa == pa.getMasterCmd() || null == pa.getMasterCmd()) {
               break;
            }
         }

         this.iOutput = this.iOutput | cmd.getHandleCmd().isOutputCmd();
         DefaultPOSPrinterCmd dpc = (DefaultPOSPrinterCmd)cmd.getHandleCmd();
         if (null != dpc.getPreWrittenBuffer()) {
            if (i > 0) {
               return false;
            }

            if (this.listOCmds.size() > 0) {
               DefaultPOSPrinterCmd dpc2 = (DefaultPOSPrinterCmd)((PrintCmd)this.listOCmds.get(0)).getHandleCmd();
               if (null != dpc2.getPreWrittenBuffer()) {
                  return false;
               }
            }
         }

         if (i >= this.listOCmds.size()) {
            this.listOCmds.add(cmd);
         } else {
            this.listOCmds.add(i, cmd);
         }

         this.totalSize = this.totalSize + cmd.getDataSize();
         this.copyPrintCmd(cmd);
         DefaultPOSPrinterCmd f = (DefaultPOSPrinterCmd)cmd.getHandleCmd();
         if (null != f) {
            f.addToManager(this);
         }

         this.bools.set(3);
         this.bools.clear(5);
         if (cmd.isAutoLoad()) {
            this.setAutoLoad(true);
         } else if (cmd.isAutoLoadKnown()) {
            this.setAutoLoad(false);
         }

         return true;
      } else {
         return false;
      }
   }

   public void accept(CmdCompleteStatusVisitor ccsv) {
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("Acception cmdcmplete visitor");
      }

      if (this.completeIdx < this.listOCmds.size() && !this.listOCmds.isEmpty()) {
         int i = this.completeIdx;
         int origSize = this.listOCmds.size();

         do {
            if (origSize != this.listOCmds.size()) {
               i--;
               origSize = this.listOCmds.size();
            }

            PrintCmd tcmd = (PrintCmd)this.listOCmds.get(i++);
            POSPrinterCmd cmd = tcmd.getHandleCmd();
            if (cmd == null) {
               i--;
            } else {
               cmd.accept(ccsv);
            }
         } while (i < this.listOCmds.size() && ccsv.isSuccessful());

         this.completeIdx = i - 1;
      }
   }

   public void accept(CmdLoadedStatusVisitor clsv) {
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("Acception cmdloaded visitor");
      }

      int i = 0;
      if (i < this.listOCmds.size() && !this.listOCmds.isEmpty()) {
         POSPrinterCmd cmd = ((PrintCmd)this.listOCmds.get(i)).getHandleCmd();
         if (null != cmd) {
            cmd.accept(clsv);
         }
      }
   }

   public void setBlockToFinishKey(BooleanMonitor blockKey) {
      this.blockKey = blockKey;
   }

   public boolean hasCommands() {
      return !this.listOCmds.isEmpty();
   }

   public void reset() {
      this.completeIdx = 0;
      this.setError(false);

      for (PrintCmd p : this.listOCmds) {
         DefaultPOSPrinterCmd cmd = (DefaultPOSPrinterCmd)p.getHandleCmd();
         if (null != cmd) {
            cmd.retry();
         }
      }

      this.setError(false);
   }

   public void restructure(PrintCmdList safe, byte estations) {
      PrintCmd pc = (PrintCmd)this.listOCmds.get(0);
      POSPrinterCmd owner = pc.getHandleCmd();
      safe.setParent(this.getParent());

      while (!owner.compareStations(estations) || !owner.getOwnerCmd().isOutputCmd()) {
         safe.addCommand(pc);
         this.listOCmds.remove(pc);
         this.bools.set(3);
         this.bools.clear(5);
         if (0 >= this.listOCmds.size()) {
            break;
         }

         pc = (PrintCmd)this.listOCmds.get(0);
         owner = pc.getHandleCmd();
      }
   }

   public void remove(PrintCmd cmd) {
      this.bools.set(3);
      this.bools.clear(5);

      while (this.listOCmds.contains(cmd)) {
         try {
            this.listOCmds.remove(cmd);
            break;
         } catch (ConcurrentModificationException var3) {
         }
      }
   }

   public int buildData() {
      int flag = 0;
      Iterator it = this.listOCmds.iterator();
      DefaultPOSPrinterCmd cmd = null;

      while (it.hasNext()) {
         PrintCmd t = (PrintCmd)it.next();
         cmd = (DefaultPOSPrinterCmd)t.getHandleCmd();
         if (cmd == null) {
            return 0;
         }

         if (0 == flag && null != cmd.getPreWrittenBuffer()) {
            this.setWriteToBuffer(cmd.getPreWrittenBuffer());
            flag = 1;
            this.totalSize = this.writeBuffer.getByteCount();
         } else {
            cmd.setOutboundPacket(this.writeBuffer);
            cmd.generatePrintCmds();
            cmd.setOutboundPacket(null);
            flag++;
         }
      }

      this.bools.clear(3);
      if (null == cmd) {
         return flag;
      } else {
         DefaultPOSPrinterCmd f = (DefaultPOSPrinterCmd)cmd.getMasterCmd();
         if (null == f) {
            f = cmd;
         }

         f.setCompleteMonitor(this.finisher);
         this.finisher[0] = false;
         return flag;
      }
   }

   public boolean isPartialComplete() {
      if (0 >= this.listOCmds.size()) {
         return true;
      } else {
         DefaultPOSPrinterCmd cmd = (DefaultPOSPrinterCmd)((PrintCmd)this.listOCmds.get(this.listOCmds.size() - 1)).getHandleCmd();
         return null == cmd ? true : cmd.isPartiallyComplete() || cmd.isCompleted();
      }
   }

   public boolean isFullyComplete() {
      boolean flag = true;

      for (int i = 0; i < this.listOCmds.size(); i++) {
         PrintCmd pc = (PrintCmd)this.listOCmds.get(i);
         if (null == pc) {
            this.listOCmds.remove(i);
            i--;
         } else {
            if (null != pc.getHandleCmd()) {
               flag = ((DefaultPOSPrinterCmd)pc.getHandleCmd()).isFullyComplete();
            }

            if (!flag) {
               break;
            }
         }
      }

      return flag;
   }

   public PrintCmd commandComplete() {
      for (int i = 0; i < this.listOCmds.size(); i++) {
         PrintCmd cmd = (PrintCmd)this.listOCmds.remove(i);
         DefaultPOSPrinterCmd hcmd = (DefaultPOSPrinterCmd)cmd.getHandleCmd();
         if (!hcmd.isCompleted()) {
            return cmd;
         }
      }

      return null;
   }

   public String toString() {
      StringBuffer tstr = new StringBuffer(this.getName());
      tstr.append("@").append(Integer.toHexString(this.hashCode()));
      if (!this.bools.get(5)) {
         this.bools.set(5);
         if (!this.hasCommands()) {
            tstr.append(" list empty");
         } else {
            tstr.append(" ");

            try {
               tstr.append(this.listOCmds.toString());
            } catch (Exception var3) {
               PrinterWriter.getTracer().print(var3);
               tstr.append("-list failure-");
            }
         }
      }

      return tstr.toString();
   }

   public PrintCmd getPending() {
      return this.listOCmds.size() > 0 ? (PrintCmd)this.listOCmds.get(0) : null;
   }

   public PrintCmd getResponsePending() {
      for (int i = 0; i < this.listOCmds.size(); i++) {
         PrintCmd cmd = (PrintCmd)this.listOCmds.get(i);
         if (null != cmd.getHandleCmd()) {
            DefaultPOSPrinterCmd dc = (DefaultPOSPrinterCmd)cmd.getHandleCmd();
            if (!dc.isFullyComplete()) {
               return cmd;
            }
         }
      }

      return null;
   }

   public PrintCmd getErrorResponsePending() {
      int i = 0;

      PrintCmd pc;
      for (pc = null; i < this.listOCmds.size(); i++) {
         pc = (PrintCmd)this.listOCmds.get(i);
         if (pc.getHandleCmd().isOutputCmd()) {
            break;
         }

         pc = null;
      }

      return pc;
   }

   public int getCmdCount() {
      return this.listOCmds.size();
   }

   public int getDataSize() {
      if (0 >= this.getCmdCount() && this.totalSize != 0) {
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("Cmdcnt " + this.getCmdCount() + " totalsize " + this.totalSize + "????");
         }

         return 0;
      } else {
         return this.totalSize;
      }
   }

   public void setError(boolean e) {
      this.setBitSet(2, e);
   }

   public void setWaitToStart(boolean b) {
      this.setBitSet(0, b);
   }

   public void setImmediate(boolean b) {
      this.setBitSet(4, b);
   }

   public void setWaitToFinish(boolean b) {
      this.setBitSet(1, b);
   }

   public boolean isInError() {
      return this.bools.get(2);
   }

   public boolean waitToStart() {
      return this.bools.get(0);
   }

   public boolean waitToFinish() {
      return this.bools.get(1);
   }

   public boolean isDirty() {
      return this.bools.get(3);
   }

   public boolean isImmediate() {
      return this.bools.get(4);
   }

   public void clean() {
      this.parent = null;
      this.bools.clearAll();
      this.autoLoad = false;

      while (this.listOCmds.size() > 0) {
         PrintCmd pc = (PrintCmd)this.listOCmds.remove(0);
         if (null != pc.getHandleCmd()) {
            pc.getHandleCmd().recycle();
         } else {
            pc.recycle();
         }
      }

      this.completeIdx = 0;
      this.totalSize = 0;
      this.uStations = 0;
      if (null != this.writeBuffer) {
         this.writeBuffer.recycle();
         this.writeBuffer = null;
      }
   }

   public void setMaxLength(int max) {
      this.maxLen = max;
   }

   protected boolean testPreWriteBuffer(DefaultPOSPrinterCmd dpc) {
      ByteBuffer preWrite = dpc.getPreWrittenBuffer();
      return 0 == this.listOCmds.size() || null == preWrite;
   }

   protected List getCmdList() {
      return this.listOCmds;
   }

   private void copyPrintCmd(PrintCmd cmd) {
      this.uStations = (byte)(this.uStations | cmd.getHandleCmd().getImpliedStations());
      if (cmd.waitToStart()) {
         this.setWaitToStart(cmd.waitToStart());
      }

      if (cmd.waitToFinish()) {
         this.setWaitToFinish(cmd.waitToFinish());
      }

      if (cmd.getHandleCmd().getOwnerCmd().isImmediate()) {
         this.setImmediate(cmd.getHandleCmd().isImmediate());
      }
   }

   private void clearPrintCmds() {
      this.uStations = 0;
      this.setWaitToStart(false);
      this.setWaitToFinish(false);
      this.setImmediate(false);
   }

   private void setBitSet(int idx, boolean onOff) {
      if (onOff) {
         this.bools.set(idx);
      } else {
         this.bools.clear(idx);
      }
   }

   public CtorArg createCtorArg(String name, Object value) throws IllegalArgumentException {
      return null;
   }

   public boolean isCleaned() {
      return this.cleaned;
   }

   public boolean isRecycled() {
      return this.recycled;
   }

   public void recycle() {
      this.getRecycleFactory().recycle(this);
      this.recycled = true;
   }

   public void setAutoLoad(boolean a) {
      this.autoLoad = a;
   }

   public boolean isAutoLoad() {
      return this.autoLoad;
   }

   public interface Factory extends RecycleFactory {
      PrintCmdList createPrintCmdList(int var1);
   }
}
