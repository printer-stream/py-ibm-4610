package com.ibm.posj.util;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;

public class PrinterTimeStamper {
   Tracer tracer = null;
   long start = 0L;
   StringBuffer[] buff = new StringBuffer[3];

   public PrinterTimeStamper(String title) {
      this.tracer = TracerFactory.getInstance().createTracer("PrtTime", title);
   }

   public boolean isOn() {
      return this.tracer.isOn();
   }

   public void printerIdle() {
      if (this.isOn()) {
         StringBuffer s = this.getBuffer();
         s.append(Long.toString(this.getTime()));
         s.append(" Printer IDLE!!!");
         this.tracer.println(s.toString());
         this.releaseBuffer(s);
      }
   }

   public void onHandleQueue(Object pospcmd) {
      if (this.isOn()) {
         StringBuffer s = this.getBuffer();
         s.append(Long.toString(this.getTime()));
         s.append(" Onto HandleQ - ");
         s.append(pospcmd);
         this.tracer.println(s.toString());
         this.releaseBuffer(s);
      }
   }

   public void offHandleQueue(Object pospcmd) {
      if (this.isOn()) {
         StringBuffer s = this.getBuffer();
         s.append(Long.toString(this.getTime()));
         s.append(" Off of HandleQ - ");
         s.append(pospcmd);
         this.tracer.println(s.toString());
         this.releaseBuffer(s);
      }
   }

   public void addToCmdList(Object pospcmd, Object list) {
      if (this.isOn()) {
         StringBuffer s = this.getBuffer();
         s.append(Long.toString(this.getTime()));
         s.append(" adding - ");
         s.append(pospcmd);
         s.append("to - ");
         s.append(list);
         this.tracer.println(s.toString());
         this.releaseBuffer(s);
      }
   }

   public void addToDevQ(Object list, int len) {
      if (this.isOn()) {
         StringBuffer s = this.getBuffer();
         s.append(Long.toString(this.getTime()));
         s.append(" toDevQ - ");
         s.append(list);
         s.append(" size =").append(len);
         this.tracer.println(s.toString());
         this.releaseBuffer(s);
      }
   }

   public void flush(Object list, Object buffer) {
      if (this.isOn()) {
         StringBuffer s = this.getBuffer();
         s.append(Long.toString(this.getTime()));
         s.append(" flush - ");
         s.append(list);
         s.append(" to - ");
         s.append(buffer);
         this.tracer.println(s.toString());
         this.releaseBuffer(s);
      }
   }

   public void sendToLowerLayer(Object buffer) {
      if (this.isOn()) {
         StringBuffer s = this.getBuffer();
         s.append(Long.toString(this.getTime()));
         s.append(" to HW - ");
         s.append(buffer);
         this.tracer.println(s.toString());
         this.releaseBuffer(s);
      }
   }

   public void cmdLoaded(Object list) {
      if (this.isOn()) {
         StringBuffer s = this.getBuffer();
         s.append(Long.toString(this.getTime()));
         s.append(" loaded - ");
         s.append(list);
         this.tracer.println(s.toString());
         this.releaseBuffer(s);
      }
   }

   public void cmdComplete(Object cmd) {
      if (this.isOn()) {
         StringBuffer s = this.getBuffer();
         s.append(Long.toString(this.getTime()));
         s.append(" complete - ");
         s.append(cmd);
         this.tracer.println(s.toString());
         this.releaseBuffer(s);
      }
   }

   private long getTime() {
      if (this.start == 0L) {
         this.start = System.currentTimeMillis();
         return 0L;
      } else {
         return System.currentTimeMillis() - this.start;
      }
   }

   private StringBuffer getBuffer() {
      StringBuffer s = null;

      for (int i = 0; i < this.buff.length; i++) {
         if (null != this.buff[i]) {
            s = this.buff[i];
            this.buff[i] = null;
            break;
         }
      }

      if (null == s) {
         s = new StringBuffer(300);
      }

      return s;
   }

   private void releaseBuffer(StringBuffer s) {
      s.delete(0, s.length());

      for (int i = 0; i < this.buff.length; i++) {
         if (null == this.buff[i]) {
            this.buff[i] = s;
            break;
         }
      }
   }
}
