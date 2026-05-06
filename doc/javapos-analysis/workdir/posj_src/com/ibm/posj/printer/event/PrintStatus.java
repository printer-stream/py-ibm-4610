package com.ibm.posj.printer.event;

import com.ibm.jutil.AsyncBitSet;
import com.ibm.jutil.ByteArrayCollector;
import com.ibm.jutil.Util;
import com.ibm.jutil.logging.LogHelper;
import com.ibm.jutil.patterns.factory.AbstractPoolFactory;
import com.ibm.jutil.patterns.factory.RecyclableObject;
import com.ibm.jutil.patterns.factory.RecycleFactory;
import com.ibm.jutil.patterns.factory.RecycleFactory.CreateMethod;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.posj.POSPrinterConst;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.printer.PrinterWriter;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

public abstract class PrintStatus extends StatusEvent implements POSPrinterConst, RecyclableObject {
   private boolean offlineStat = false;
   private boolean printerResponding = true;
   private AsyncBitSet status;
   private int size;
   private byte ec;
   private byte[] rawStatus = null;
   private boolean anyError = false;
   private boolean anyCmdComplete = false;
   private boolean extendedData = false;
   private boolean pause = false;
   protected LogHelper logger = null;
   private boolean rec = true;
   private boolean cleaned = true;
   private boolean pReady = false;
   protected ArrayList errorTable = new ArrayList(3);
   protected byte errorStation = 0;
   boolean anyHWError = false;
   private boolean disableSlipStatus = false;

   public PrintStatus(Object src, byte[] a) {
      super(src, 0);
   }

   public void setOfflineStatus() {
      this.offlineStat = true;
      this.turnErrorOn(-1, (byte)2);
      this.cacheError((byte)-1, (byte)2, (short)256);
   }

   public boolean isOffline() {
      return this.offlineStat;
   }

   public abstract Object clone();

   public RecycleFactory getRecycleFactory() {
      return null;
   }

   public void init(byte[] a) {
      if (null != this.rawStatus) {
         ByteArrayCollector.getCollector().collect(this.rawStatus);
      }

      this.rawStatus = a;
      this.anyError = false;
      if (null == this.status) {
         this.createBitSets();
      }

      if (a != null && !this.isOffline()) {
         this.updateStatus();
      }
   }

   public boolean isReady() {
      return this.pReady;
   }

   public boolean isPrinterResponding() {
      return this.printerResponding;
   }

   public void setPrinterResponding(boolean responding) {
      this.printerResponding = responding;
   }

   public boolean checkErrors() {
      return this.anyError;
   }

   public byte getECLevel() {
      return this.ec;
   }

   public PrintErrorEvent getCachedError(byte station) {
      if (this.traceOn()) {
         this.getTracer().println("getCachedError station " + Util.toHexString(station));
      }

      PrintStatus.ErrorStruct es = null;

      for (int i = 0; i < this.errorTable.size(); i++) {
         es = (PrintStatus.ErrorStruct)this.errorTable.get(i);
         if ((es.station & station) > 0) {
            break;
         }

         es = null;
      }

      PrintErrorEvent p = null;
      if (null != es) {
         p = PrintErrorEvent.createErrorEvent(this.getSource(), station, es);
      }

      if (this.traceOn()) {
         this.getTracer().println("getCachedError return " + p);
      }

      return p;
   }

   public PrintErrorEvent getSWError() {
      PrintStatus.ErrorStruct es = null;

      for (Iterator it = this.errorTable.iterator(); it.hasNext(); es = null) {
         es = (PrintStatus.ErrorStruct)it.next();
         if (es.type == 1) {
            break;
         }
      }

      return null == es ? null : PrintErrorEvent.createErrorEvent(this.getSource(), es.station, es);
   }

   public void cacheError(byte station, byte type, short code) {
      PrintStatus.ErrorStruct es = new PrintStatus.ErrorStruct();
      if (2 == type) {
         this.anyHWError = true;
      }

      es.code = code;
      es.type = type;
      es.station = station;
      this.errorTable.add(es);
      this.errorStation |= station;
      this.anyError = true;
   }

   public boolean errorCompare(PrintStatus ps) {
      boolean flag = false;
      if (ps.errorTable.size() != this.errorTable.size()) {
         return false;
      } else {
         for (int i = 0; i < this.errorTable.size(); i++) {
            PrintStatus.ErrorStruct tEs = (PrintStatus.ErrorStruct)this.errorTable.get(i);

            for (int x = 0; x < ps.errorTable.size(); x++) {
               PrintStatus.ErrorStruct psEs = (PrintStatus.ErrorStruct)ps.errorTable.get(x);
               if (psEs.compare(tEs)) {
                  flag = true;
                  break;
               }

               flag = false;
            }

            if (!flag) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean checkCmdComplete() {
      return this.anyCmdComplete;
   }

   public boolean checkForPause() {
      return this.pause;
   }

   public boolean isExtendedData() {
      return this.extendedData;
   }

   public byte getErrorStation() {
      return this.errorStation;
   }

   public boolean isHardwareError() {
      return this.anyHWError;
   }

   public boolean getStatus(int statusID) {
      if (19 < statusID) {
         byte id = this.extractStatusID(statusID);
         byte no = (byte)(this.extractByteNo(statusID) - 1);
         return (this.rawStatus[no] & id) != 0;
      } else {
         return this.status.get(statusID);
      }
   }

   public byte extractByteNo(int stat) {
      return (byte)(stat >> 8);
   }

   public byte extractStatusID(int stat) {
      return (byte)(0xFF & stat);
   }

   public void clean() {
      this.logger = null;
      this.anyError = false;
      this.anyCmdComplete = false;
      this.pause = false;
      this.status.clearAll();
      this.printerResponding = true;
      this.errorStation = 0;
      this.cleaned = true;
      this.extendedData = false;
      this.anyHWError = false;
      this.errorTable.clear();
      this.pReady = false;
      this.offlineStat = false;
      this.disableSlipStatus = false;
   }

   public boolean isCleaned() {
      return this.cleaned;
   }

   public abstract boolean isSlipPaperPresent();

   public abstract boolean isSlipNonPaperError();

   public void setDisableSlipStatus(boolean disable) {
      this.disableSlipStatus = disable;
   }

   public boolean ignoreSlipStatus() {
      return this.disableSlipStatus;
   }

   public void setReady(boolean p) {
      this.pReady = p;
   }

   protected LogHelper getLogHelper() {
      return this.logger;
   }

   protected void setECLevel(byte x) {
      this.ec = x;
   }

   protected void log(int ID, int priority, String msg) {
      if (null != this.logger) {
         try {
            this.logger.addLogEntry(ID, msg, "POSPrinter", priority);
         } catch (Exception var5) {
            if (this.traceOn()) {
               this.getTracer().print(var5);
            }
         }
      }
   }

   protected abstract void updateStatus();

   protected void setStatus(int statusID, boolean value) {
      if (this.size > statusID) {
         if (value) {
            this.status.set(statusID);
         } else if (!value) {
            this.status.clear(statusID);
         }
      }
   }

   public byte[] getData() {
      return this.rawStatus;
   }

   private void createBitSets() {
      int totalbits = 19;
      this.status = new AsyncBitSet();
      this.size = totalbits;
   }

   protected void turnErrorOn(int station, byte errorType) {
      this.errorStation = (byte)(this.errorStation | station);
      this.anyError = true;
   }

   protected void turnCmdCompleteOn() {
      this.anyCmdComplete = true;
   }

   protected void turnPauseOn() {
      this.pause = true;
   }

   protected void setExtendedData(boolean x) {
      this.extendedData = x;
   }

   public void recycle() {
      this.getRecycleFactory().recycle(this);
      this.rec = true;
   }

   public boolean isRecycled() {
      return this.rec;
   }

   public Tracer getTracer() {
      return PrinterWriter.getTracer();
   }

   public boolean traceOn() {
      return this.getTracer().isOn();
   }

   public static class ErrorStruct {
      public byte station;
      public byte type;
      public short code;

      public boolean compare(PrintStatus.ErrorStruct e) {
         return e.station == this.station && this.type == e.type && e.code == this.code;
      }

      public String toString() {
         return " #S " + this.station + " t " + this.type + " c " + this.code + " " + this.hashCode();
      }
   }

   public static class PrintStatusFactory extends AbstractPoolFactory {
      public static byte[] dummy = new byte[]{0};
      public static final int INIT_FACTORY_SIZE = 9;
      public static final int INCREMENTAL_SIZE = 3;

      protected PrintStatusFactory(CreateMethod creator) {
         super(creator, 9, 3, 0.5F);
      }

      public PrintStatus createPrintStatus(byte[] data, LogHelper logger) {
         PrintStatus ret = null;

         while (ret == null) {
            try {
               ret = (PrintStatus)super.takeFromPool();
            } catch (ConcurrentModificationException var5) {
            }
         }

         ret.logger = logger;
         byte[] copy = ByteArrayCollector.getCollector().getArray(data.length);
         System.arraycopy(data, 0, copy, 0, data.length);
         ret.init(copy);
         ret.rec = false;
         ret.cleaned = false;
         return ret;
      }
   }
}
