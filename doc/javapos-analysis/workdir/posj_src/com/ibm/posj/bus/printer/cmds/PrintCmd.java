package com.ibm.posj.bus.printer.cmds;

import com.ibm.jutil.AsyncBitSet;
import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.patterns.factory.RecyclableObject;
import com.ibm.jutil.patterns.factory.RecycleFactory;
import com.ibm.jutil.patterns.factory.RecyclableObject.CtorArg;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.PrintStatus;

public class PrintCmd implements RecyclableObject {
   private byte station = 0;
   private AsyncBitSet bools = new AsyncBitSet(10);
   private ByteBuffer data = null;
   private PrinterWriter.PrinterWriterUser parent = null;
   private POSPrinterCmd internal = null;
   private HandleCmd otherCmd = null;
   private int futureBytecount = 0;
   private PrintCmdFactory factory = null;
   private PrintStatus eStatus = null;
   private int continueIdx = 0;
   private int offset = 0;
   private BooleanMonitor monitor = null;
   public static final int WAIT_TO_START = 0;
   public static final int WAIT_TO_FINISH = 1;
   public static final int IS_BUFFERED = 2;
   public static final int IS_CLEANED = 3;
   public static final int IS_RECYCLED = 4;
   public static final int ERROR_RESPONSE = 5;
   public static final int SUCCEEDED = 6;
   public static final int RETRY = 7;
   public static final int AUTOLOAD = 8;
   public static final int AUTOLOAD_KNOWN = 9;
   public static final int END = 10;

   public PrintCmd(PrintCmdFactory f) {
      this.factory = f;
   }

   public boolean isDryRun() {
      return this.data == null;
   }

   public String toString() {
      StringBuffer s = new StringBuffer("PrintCmd");
      s.append(Integer.toHexString(this.hashCode()));
      if (null == this.internal) {
         s.append("No handle cmd?");
      } else {
         s.append(this.internal.getName()).append("@");
         s.append(Integer.toHexString(this.internal.hashCode())).append('(');
         s.append(this.internal.getStation()).append(')');
      }

      return s.toString();
   }

   public PrintCmdFactory getFactory() {
      return this.factory;
   }

   public PrintCmdList wrapCmd() {
      PrintCmdList l = this.factory.createPrintCmdList();
      l.addCommand(this);
      return l;
   }

   public void setStation(byte s) {
      this.station = s;
   }

   public byte getStation() {
      return this.internal == null ? this.station : this.internal.getStation();
   }

   public void setParent(PrinterWriter.PrinterWriterUser p) {
      this.parent = p;
   }

   public PrinterWriter.PrinterWriterUser getParent() {
      return this.parent;
   }

   public void clean() {
      this.parent = null;
      this.internal = null;
      this.otherCmd = null;
      this.eStatus = null;
      this.station = 0;
      this.bools.clearAll();
      this.bools.set(3);
      this.bools.set(6);
      this.futureBytecount = 0;
      this.data = null;
      this.continueIdx = 0;
      if (null != this.monitor) {
         this.monitor.set(false);
         this.monitor = null;
      }
   }

   public void setHandleCmd(POSPrinterCmd userData) {
      this.internal = userData;
   }

   public void setOtherHandleCmd(HandleCmd other) {
      this.otherCmd = other;
   }

   public POSPrinterCmd getHandleCmd() {
      return this.internal;
   }

   public HandleCmd getOtherHandleCmd() {
      return this.otherCmd;
   }

   public void setContinueIdx(int continueIdx) {
      this.continueIdx = continueIdx;
   }

   public void clearFutureByteCnt() {
      this.futureBytecount = 0;
   }

   public int getFutureByteCnt() {
      return this.futureBytecount;
   }

   public int getDataSize() {
      return null == this.data ? this.getFutureByteCnt() : this.data.getByteCount();
   }

   public boolean waitToStart() {
      return this.bools.get(0);
   }

   public boolean waitToFinish() {
      return this.bools.get(1);
   }

   public boolean isBuffered() {
      return this.bools.get(2);
   }

   public boolean isSucceeded() {
      return this.bools.get(6);
   }

   public boolean getRetry() {
      return this.bools.get(7);
   }

   public void setWaitToStart(boolean b) {
      this.setBitSet(0, b);
   }

   public void setWaitToFinish(boolean b) {
      this.setBitSet(1, b);
   }

   public void setErrorStatus(PrintStatus eStatus) {
      this.eStatus = eStatus;
   }

   public PrintStatus getErrorStatus() {
      return this.eStatus;
   }

   public void setBlockKey(BooleanMonitor key) {
      this.monitor = key;
   }

   public void retry() {
      if (null != this.monitor) {
         this.monitor.set(false);
      }
   }

   public void setBuffered(boolean b) {
      this.setBitSet(2, b);
   }

   public void setSucceeded(boolean b) {
      if (null != this.monitor) {
         this.monitor.set(false);
      }

      this.setBitSet(6, b);
   }

   public void setRetry(boolean b) {
      this.setBitSet(7, b);
   }

   public int appendCmd(byte[] sdata, int length) {
      if (length < 0) {
         return 0;
      } else {
         if (sdata.length < length) {
            length = sdata.length;
         }

         this.offset = 0;
         if (this.futureBytecount + length > this.continueIdx && 0 != this.continueIdx) {
            this.offset = length + this.futureBytecount - this.continueIdx;
            length -= this.offset;
         }

         this.futureBytecount += length;
         if (this.isDryRun()) {
            return length;
         } else {
            length = this.fitData(length);
            if (length <= 0) {
               return 0;
            } else {
               this.data.append(sdata, this.offset, length);
               return length;
            }
         }
      }
   }

   public int appendCmd(byte[] sdata, int startIndex, int length) throws IllegalArgumentException {
      if (length < 0) {
         return 0;
      } else if (startIndex > sdata.length) {
         return 0;
      } else {
         if (sdata.length < length + startIndex) {
            length = sdata.length - startIndex;
         }

         this.offset = 0;
         if (this.futureBytecount + length > this.continueIdx && 0 != this.continueIdx) {
            this.offset = length + this.futureBytecount - this.continueIdx;
            length -= this.offset;
         }

         this.futureBytecount += length;
         if (this.isDryRun()) {
            return length;
         } else {
            length = this.fitData(length);
            if (length <= 0) {
               return 0;
            } else {
               this.data.append(sdata, startIndex + this.offset, length);
               return length;
            }
         }
      }
   }

   public int appendCmd(ByteBuffer byteBuffer) {
      int length = byteBuffer.getByteCount();
      this.offset = 0;
      if (this.futureBytecount + length > this.continueIdx && 0 != this.continueIdx) {
         this.offset = length + this.futureBytecount - this.continueIdx;
         length -= this.offset;
      }

      this.futureBytecount = this.futureBytecount + byteBuffer.getByteCount();
      if (this.isDryRun()) {
         return byteBuffer.getByteCount();
      } else {
         length = this.fitData(length);
         if (length <= 0) {
            return 0;
         } else {
            this.appendCmd(byteBuffer.getBytesRef(), this.offset, length);
            return length;
         }
      }
   }

   public int appendCmd(byte[] sdata) {
      int length = sdata.length;
      this.offset = 0;
      if (this.futureBytecount + length > this.continueIdx && 0 != this.continueIdx) {
         this.offset = length + this.futureBytecount - this.continueIdx;
         length -= this.offset;
      }

      this.futureBytecount += length;
      if (this.isDryRun()) {
         return length;
      } else {
         length = this.fitData(length);
         if (length == 0) {
            return 0;
         } else {
            this.data.append(sdata, this.offset, length);
            return length;
         }
      }
   }

   public int appendCmd(byte sdata) {
      if (this.isDryRun()) {
         this.futureBytecount++;
         return 1;
      } else {
         this.data.append(sdata);
         return 1;
      }
   }

   public int fitData(int addLen) {
      if (addLen < 0) {
         addLen = 0;
      }

      return addLen;
   }

   public void setBytes(ByteBuffer data) {
      this.data = data;
   }

   public void setMaxSize(int maxSize) {
   }

   public byte[] getBytes() {
      return this.data.getBytesRef();
   }

   public ByteBuffer getByteBuffer() {
      return this.data;
   }

   public void recycle() {
      if (this.factory != null) {
         this.factory.recycle(this);
      }

      this.bools.set(4);
   }

   public boolean isCleaned() {
      return this.bools.get(3);
   }

   public boolean isRecycled() {
      return this.bools.get(4);
   }

   public RecycleFactory getRecycleFactory() {
      return this.factory.getBaseFactory();
   }

   public CtorArg createCtorArg(String name, Object value) throws IllegalArgumentException {
      return null;
   }

   private void setBitSet(int idx, boolean onOff) {
      if (onOff) {
         this.bools.set(idx);
      } else {
         this.bools.clear(idx);
      }
   }

   public void setAutoLoad(boolean a, boolean known) {
      this.bools.clear(8);
      if (a) {
         this.bools.set(8);
      }

      this.bools.clear(9);
      if (known) {
         this.bools.set(9);
      }
   }

   public boolean isAutoLoad() {
      return this.bools.get(8);
   }

   public boolean isAutoLoadKnown() {
      return this.bools.get(9);
   }
}
