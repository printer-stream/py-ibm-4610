package com.ibm.posj.printer.event;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.Util;
import com.ibm.jutil.patterns.factory.AbstractPoolFactory;
import com.ibm.jutil.patterns.factory.RecyclableObject;
import com.ibm.jutil.patterns.factory.RecycleFactory;
import com.ibm.jutil.patterns.factory.RecycleFactoryException;
import com.ibm.jutil.patterns.factory.RecyclableObject.CtorArg;
import com.ibm.jutil.patterns.factory.RecycleFactory.CreateMethod;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.event.ErrorEvent;

public class PrintErrorEvent extends ErrorEvent implements RecyclableObject {
   private StringBuffer toString = new StringBuffer(95);
   private POSPrinterCmd offender = null;
   private POSPrinterCmd errCmd = null;
   private byte response = 0;
   BooleanMonitor responded = new BooleanMonitor(false);
   private boolean recycled = true;
   private boolean cleaned = true;
   private Object src = null;
   private static PrintErrorEvent.Factory factory = new PrintErrorEvent.Factory();
   private byte station = 0;
   private PrintStatus.ErrorStruct data = null;

   protected PrintErrorEvent(Object source, int i, byte station) {
      super(source, i);
      this.src = source;
      this.station = station;
   }

   public String toString() {
      try {
         if (0 == this.toString.length()) {
            this.toString.append("PrintErrorEvent@").append(Util.toHexString(this.hashCode())).append(" type-");
            this.toString.append(this.getErrorType());
            this.toString.append(" code-").append(this.getPrinterErrorCode()).append(" station ");
            this.toString.append(this.getErrorStation()).append(" offender ").append(this.getOffender());
         }
      } catch (Exception var2) {
         return super.toString();
      }

      return this.toString.toString();
   }

   public Object clone() {
      return createErrorEvent(this.getSource(), this.getErrorStation(), this.data);
   }

   public void setOffender(POSPrinterCmd o) {
      this.offender = o;
   }

   public POSPrinterCmd getOffender() {
      return this.offender;
   }

   public void setErrorStation(byte station) {
      this.station = station;
   }

   public Object getSource() {
      return this.src;
   }

   public byte getErrorType() {
      return this.data.type;
   }

   public short getPrinterErrorCode() {
      return this.data.code;
   }

   public byte getErrorStation() {
      return this.station;
   }

   public void waitForResponse() {
      this.responded.waitForTrue(-1);
   }

   public void setResponseCmd(POSPrinterCmd pc) {
      this.errCmd = pc;
   }

   public POSPrinterCmd getResponseCmd() {
      return this.errCmd;
   }

   public void setResponse(byte response) {
      this.response = response;
      this.responded.set(true);
   }

   public byte getResponse() {
      return this.response;
   }

   public void recycle() {
      this.getRecycleFactory().recycle(this);
   }

   public boolean isRecycled() {
      return this.recycled;
   }

   public boolean isCleaned() {
      return this.cleaned;
   }

   public RecycleFactory getRecycleFactory() {
      if (null == factory) {
         factory = new PrintErrorEvent.Factory();
      }

      return factory;
   }

   public static PrintErrorEvent createErrorEvent(Object src, byte station, PrintStatus.ErrorStruct es) {
      PrintErrorEvent pee = factory.createErrorEvent();
      pee.src = src;
      pee.station = station;
      pee.data = es;
      return pee;
   }

   public CtorArg createCtorArg(String name, Object value) throws IllegalArgumentException {
      throw new IllegalArgumentException();
   }

   public void clean() {
      this.errCmd = null;
      this.offender = null;
      this.response = 0;
      this.responded.set(false);
      this.cleaned = true;
      this.recycled = true;
      this.station = 0;
      this.data = null;
      this.toString.setLength(0);
   }

   private static class Creator implements CreateMethod {
      private Creator() {
      }

      public RecyclableObject newRecyclableObject(RecycleFactory recycleFactory) {
         return new PrintErrorEvent(this, 0, (byte)0);
      }

      public RecyclableObject newRecyclableObject(CtorArg[] ctorArgs, RecycleFactory recycleFactory) throws RecycleFactoryException {
         return new PrintErrorEvent(this, 0, (byte)0);
      }
   }

   public static class Factory extends AbstractPoolFactory {
      public Factory() {
         super(new PrintErrorEvent.Creator());
      }

      public PrintErrorEvent createErrorEvent() {
         return (PrintErrorEvent)super.takeFromPool();
      }
   }
}
