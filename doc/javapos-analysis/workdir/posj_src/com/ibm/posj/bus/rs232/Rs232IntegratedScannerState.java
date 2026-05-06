package com.ibm.posj.bus.rs232;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.util.DevCats;

public abstract class Rs232IntegratedScannerState {
   protected Tracer tracer = TracerFactory.getInstance().createTracer(DevCats.SCANNER_DEVCAT.toString(), this.getClass().getName());

   public abstract void set();

   public synchronized int write(byte[] data) {
      return 2;
   }
}
