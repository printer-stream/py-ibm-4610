package com.ibm.posj.bus.rs232;

import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Timer;
import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.bus.rs232.javaxcomm.Rs232PortCommAdapter;
import com.ibm.rs232.Rs232Port;
import java.io.IOException;
import java.io.InputStream;

public class Rs232MSRReader implements Rs232PortCommAdapter.DataReaderStrategy {
   private ByteBuffer byteBuffer = new ByteBuffer();
   private boolean timerExpired = true;
   private Tracer tracer = TracerFactory.getInstance().createTracer("MSR", "Rs232MSRReader");
   public static int TIMEOUT = 350;

   public void dataAvailable(InputStream is, Rs232Port.EventHelper helper) {
      try {
         Thread.currentThread();
         Thread.sleep((long)TIMEOUT);
      } catch (InterruptedException var6) {
      }

      try {
         for (int bytesAvailable = is.available(); bytesAvailable > 0; bytesAvailable = is.available()) {
            byte[] data = new byte[bytesAvailable];

            for (int i = 0; i < data.length; i++) {
               data[i] = (byte)is.read();
            }

            if (this.tracer.isOn()) {
               this.tracer.println("Data available [" + data.length + "]" + Util.toFormatedHexString(data));
            }

            this.process(data, helper);
         }
      } catch (IOException var7) {
         if (this.tracer.isOn()) {
            this.tracer.println("Error Occurred while getting MSR data :");
         }

         this.tracer.println(var7);
      }
   }

   protected void process(byte[] data, Rs232Port.EventHelper helper) {
      if (this.timerExpired) {
         if (this.tracer.isOn()) {
            this.tracer.println("time expired start again");
         }

         this.timerExpired = false;
         this.byteBuffer.reset();
         this.byteBuffer.append(data);
         Timer timer = new Timer(new Rs232MSRReader$1(this, helper));
         timer.setTime(TIMEOUT + 100);
         timer.start();
      } else {
         if (this.tracer.isOn()) {
            this.tracer.println("add to previous data");
         }

         this.byteBuffer.append(data);
      }
   }
}
