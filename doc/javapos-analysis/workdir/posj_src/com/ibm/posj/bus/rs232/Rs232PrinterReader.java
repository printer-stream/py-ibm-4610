package com.ibm.posj.bus.rs232;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.ByteArrayCollector;
import com.ibm.jutil.SleepPolicy;
import com.ibm.jutil.Util;
import com.ibm.jutil.tasks.AbstractActiveObject;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.posj.bus.rs232.javaxcomm.Rs232PortCommAdapter;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.rs232.Rs232Port;
import com.ibm.rs232.event.Rs232DataEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class Rs232PrinterReader implements Rs232PortCommAdapter.DataReaderStrategy {
   public static final int DLE = 16;
   public static final int XON = 17;
   public static final int XOFF = 19;
   private Rs232PrinterReader reader = null;
   private boolean flowXONXOFF = false;
   private boolean firstCall = true;
   private Rs232PrinterReader.DataHandler dataHandle = new Rs232PrinterReader.DataHandler();
   private ByteArrayCollector collector = ByteArrayCollector.getCollector();
   public PushbackInputStream pbis = null;
   int len = -1;
   long lastRead = 0L;
   byte[] lenBuffer = new byte[]{0, 0};

   public Rs232PrinterReader(Rs232Port port) {
      this.configFlowControl(port.getRs232config().getFlowControl());
   }

   private void configFlowControl(int flowControl) {
      if (this.traceOn()) {
         this.trace().println("config Flow Control to-->" + flowControl);
      }

      int XON_XOFF = 12;
      if (flowControl == XON_XOFF) {
         this.flowXONXOFF = true;
      }
   }

   public void kill() {
      this.dataHandle.die();
   }

   public boolean isReceiving() {
      boolean ret = false;
      if (null != this.pbis) {
         try {
            int total = (int)(System.currentTimeMillis() - this.lastRead);
            int avail = this.pbis.available();
            if (avail > 0 || total < 2500) {
               ret = true;
            }
         } catch (Exception var4) {
            this.trace().print(var4);
         }
      }

      return ret;
   }

   public void dataAvailable(InputStream isr, Rs232Port.EventHelper helper) throws IOException {
      if (this.firstCall) {
         this.dataHandle.setStreamHelper(isr, helper);
         this.firstCall = false;
      }

      if (!this.dataHandle.isStarted()) {
         this.dataHandle.start();
      }

      this.dataHandle.increment();
   }

   private void handleData(InputStream isr, Rs232Port.EventHelper helper) throws IOException {
      if (-1 == this.len) {
         if (this.pbis.available() < 8) {
            return;
         }

         if (this.traceOn()) {
            this.trace().println(">>doRead length");
         }

         try {
            this.doRead(2, this.lenBuffer);
         } catch (IllegalArgumentException var5) {
            if (this.traceOn()) {
               this.trace().print(var5);
            }

            this.len = -1;
            return;
         }

         if (this.traceOn()) {
            this.trace().println("<<doRead length " + Util.toFormatedHexString(this.lenBuffer));
         }

         if (this.lenBuffer[0] == 0 && this.lenBuffer[1] == 0) {
            this.pbis.unread(0);
            this.len = -1;
            return;
         }

         int high = Util.unsignedInt(this.lenBuffer[0]);
         int low = Util.unsignedInt(this.lenBuffer[1]);
         this.len = (high << 8) + low - 2;
         if (this.traceOn()) {
            this.trace().println("Proposed length " + this.len + " available " + this.pbis.available());
         }
      }

      if (0 >= this.len) {
         this.len = -1;
      } else {
         byte[] data = this.collector.getArray(this.len);

         try {
            if (this.trace().isOn()) {
               this.trace().print("-->Status doRead");
            }

            this.doRead(this.len, data);
            if (this.trace().isOn()) {
               this.trace().print("<--Status doRead");
            }
         } catch (IllegalArgumentException var6) {
            this.trace().print(var6);
            this.len = -1;
            return;
         }

         this.trace().println("fire " + Util.toFormatedHexString(data));
         helper.fireDataEvent(new Rs232DataEvent(this, data));
         this.len = -1;
      }
   }

   protected void doRead(int len, byte[] data) throws IllegalArgumentException {
      int read = 0;
      int jread = 0;

      while (read != len) {
         try {
            this.trace().println(">> doread " + Thread.currentThread().getName());
            jread = this.pbis.read();
            if (this.trace().isOn()) {
               this.trace().print(3, Util.toHexString(jread));
            }

            if (this.flowXONXOFF && 16 == jread) {
               jread = this.pbis.read();
               jread = this.unstuff(jread);
            }

            data[read++] = (byte)jread;
            this.lastRead = System.currentTimeMillis();
            if (len >= 256 && read >= 5 && (data[4] & 128) <= 0) {
               throw new IllegalArgumentException("Length > 256 and not a image read");
            }
         } catch (Exception var6) {
            this.trace().print(var6);
            this.waitForData();
         }
      }

      this.trace().println("<< doread " + Thread.currentThread().getName());
   }

   protected void waitForData() {
      if (this.traceOn()) {
         this.trace().println("BAD: Waiting for data");
      }

      while (true) {
         try {
            if (this.pbis.available() > 0) {
               return;
            }
         } catch (Exception var2) {
            this.trace().print(var2);
         }

         SleepPolicy.sleep(200L);
      }
   }

   private byte unstuff(int start) {
      if (this.trace().isOn()) {
         this.trace().print("unstuff " + Util.toHexString(start));
      }

      byte uns;
      switch (start) {
         case 48:
            uns = 17;
            break;
         case 49:
            uns = 16;
            break;
         case 50:
            uns = 19;
            break;
         default:
            throw new IllegalArgumentException("INVALID: Status from the printer");
      }

      return uns;
   }

   public boolean traceOn() {
      return PrinterWriter.getTracer().isOn();
   }

   public Tracer trace() {
      return PrinterWriter.getTracer();
   }

   public class DataHandler extends AbstractActiveObject {
      private boolean infinite = true;
      private int dataAvailCnt = 0;
      private BooleanMonitor lock = new BooleanMonitor(false);
      private InputStream isr;
      private Rs232Port.EventHelper helper;

      public void setStreamHelper(InputStream i, Rs232Port.EventHelper eh) {
         this.isr = i;
         this.helper = eh;
      }

      public void increment() {
         this.dataAvailCnt++;
         if (this.lock.isTrue()) {
            this.lock.set(false);
         }
      }

      public void die() {
         this.infinite = false;
      }

      protected void runActiveObject() {
         while (this.infinite) {
            try {
               if (Rs232PrinterReader.this.pbis != null && Rs232PrinterReader.this.pbis.available() < 7) {
                  Rs232PrinterReader.this.trace().println("Using reader " + Rs232PrinterReader.this.pbis.available());
                  this.lock.set(true);

                  try {
                     this.lock.waitForFalse(6000);
                  } catch (Exception var2) {
                     continue;
                  }
               }

               if (null == Rs232PrinterReader.this.pbis) {
                  Rs232PrinterReader.this.pbis = new PushbackInputStream(this.isr, 50);
               }

               if (Rs232PrinterReader.this.traceOn()) {
                  Rs232PrinterReader.this.trace()
                     .println(this.dataAvailCnt + "-->handleData Data availables - size = " + Rs232PrinterReader.this.pbis.available());
               }

               Rs232PrinterReader.this.handleData(this.isr, this.helper);
               if (Rs232PrinterReader.this.traceOn()) {
                  Rs232PrinterReader.this.trace().println("<--handleData dataAvailable available " + Rs232PrinterReader.this.pbis.available());
               }

               if (Rs232PrinterReader.this.pbis.available() > 8) {
                  Rs232PrinterReader.this.dataAvailable(Rs232PrinterReader.this.pbis, this.helper);
               }

               this.dataAvailCnt--;
            } catch (IOException var3) {
               Rs232PrinterReader.this.trace().print(var3);
            }
         }

         Rs232PrinterReader.this.trace().println("-->!!! Exiting reader thread");
      }
   }
}
