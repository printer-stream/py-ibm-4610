package com.ibm.embedded;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.util.DevBuses;
import java.util.ArrayList;
import java.util.Iterator;

public class PciInitializer {
   private static GetSlotInfo gsi;
   private static ArrayList listeners;
   private static byte status;
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevBuses.EMBEDDED_DEVBUS.getName(), "PciInitializer");

   public Iterator init() throws EmbeddedException {
      ArrayList al = new ArrayList();

      try {
         gsi = nativeInit();
      } catch (Exception var8) {
         this.tracer.println("Exception when initializing PCI native code :" + var8.toString());
         this.tracer.println("Let's return an empty iterator");
         return al.iterator();
      } catch (Error var9) {
         this.tracer.println("Error when initializing PCI native code :" + var9.toString());
         this.tracer.println("Let's return an empty iterator");
         return al.iterator();
      }

      if (null != gsi) {
         long numberOfDrawers = gsi.getSlotDrawers();
         long sizeOfNVRAM = gsi.getSlotNVRAM();
         long adapterID = gsi.getAdapterID();
         if (numberOfDrawers > 0L) {
            al.add(new CashDrawerPciDriverImp(numberOfDrawers, adapterID));
         }

         if (sizeOfNVRAM > 0L) {
            al.add(new HardTotalsPciDriverImp(sizeOfNVRAM));
         }

         if (al.isEmpty()) {
            return al.iterator();
         }
      }

      return al.iterator();
   }

   private static native GetSlotInfo nativeInit() throws EmbeddedException;

   static {
      try {
         System.loadLibrary("aipposembedded");
      } catch (UnsatisfiedLinkError var1) {
      }

      gsi = null;
      listeners = null;
      status = 0;
   }
}
