package com.ibm.posj.bus.rs232;

import com.ibm.jutil.Timerable;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.util.DevCats;

public abstract class Rs232FiscalPrinterState implements Timerable {
   protected Rs232FiscalPrinterProtocol protocol;
   protected long started = 0L;
   protected long stoped = 0L;
   protected Tracer tracer = TracerFactory.getInstance().createTracer(DevCats.FISCALPRINTER_DEVCAT.toString(), "Rs232FiscalPrinterState");
   protected boolean waitingForCompletion = false;

   public Rs232FiscalPrinterState(Rs232FiscalPrinterProtocol fpp) {
      this.waitingForCompletion = false;
      this.protocol = fpp;
   }

   public int write(byte[] buffer, int length) {
      return 3;
   }

   public void receivePacket(byte[] packet, int length) {
      this.protocol.stopTimer();
   }
}
