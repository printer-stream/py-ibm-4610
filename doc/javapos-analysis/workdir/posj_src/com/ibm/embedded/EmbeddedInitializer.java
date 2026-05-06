package com.ibm.embedded;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.util.DevBuses;
import java.util.ArrayList;
import java.util.Iterator;

public class EmbeddedInitializer {
   private ArrayList deviceList = null;
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevBuses.EMBEDDED_DEVBUS.getName(), "EmbeddedInitializer");
   public static final String ERROR_INITIALIZATION_CDHT = "Error when initializing the Cash Drawer or Hard Totals native code :";
   public static final String ERROR_INITIALIZATION_MS = "Error when initializing the Motion Sensor native code :";
   public static final String ERROR_JNI_LIBRARY_NOT_PRESENT = "The JNI Library is not present on this system :";

   static GetSlotInfo getSlotInfo() throws EmbeddedException, UnsatisfiedLinkError {
      return nativeInit();
   }

   public Iterator init() {
      if (this.deviceList == null) {
         this.deviceList = new ArrayList();
         this.createCDHTDEmbeddeDrivers(this.deviceList);
         this.createMotionSensorEmbeddedDriver(this.deviceList);
      }

      return this.deviceList.iterator();
   }

   private void createMotionSensorEmbeddedDriver(ArrayList deviceList) {
      long motionSensorPresent = 0L;

      try {
         GetSlotInfo slotInfo = nativeMSInitializer();
         if (slotInfo.getSlotMotionSensor() > 0L) {
            deviceList.add(new MotionSensorEmbeddedDriverImp());
         }
      } catch (EmbeddedException var5) {
         this.tracer.println("Error when initializing the Motion Sensor native code :" + var5.toString());
         this.tracer.print(var5);
      } catch (UnsatisfiedLinkError var6) {
         this.tracer.println("The JNI Library is not present on this system :" + var6.toString());
      }
   }

   private void createCDHTDEmbeddeDrivers(ArrayList deviceList) {
      long numberOfCashDrawer = 0L;
      long nvRamMemorySize = 0L;
      long adapterID = 0L;

      try {
         GetSlotInfo slotInfo = nativeInit();
         if (this.tracer.isOn()) {
            this.traceNormal(slotInfo.toString());
         }

         numberOfCashDrawer = slotInfo.getSlotDrawers();
         nvRamMemorySize = slotInfo.getSlotNVRAM();
         adapterID = slotInfo.getAdapterID();
         if (numberOfCashDrawer > 0L) {
            deviceList.add(new CashDrawerPciDriverImp(numberOfCashDrawer, adapterID));
         }

         if (nvRamMemorySize > 0L) {
            HardTotalsPciDriverImp ht = new HardTotalsPciDriverImp(adapterID);
            if (this.tracer.isOn()) {
               this.traceNormal(ht.getNVRAMInfo().toString());
            }

            deviceList.add(ht);
         }

         if (adapterID == 4614L) {
            deviceList.add(new KeylockEmbeddedDriverImp());
         }
      } catch (EmbeddedException var10) {
         this.tracer.println("Error when initializing the Cash Drawer or Hard Totals native code :" + var10.toString());
         this.tracer.print(var10);
      } catch (UnsatisfiedLinkError var11) {
         this.tracer.println("The JNI Library is not present on this system :" + var11.toString());
      }
   }

   private void traceNormal(String msg) {
      this.tracer.println(2, msg);
   }

   private static native GetSlotInfo nativeInit() throws EmbeddedException;

   private static native GetSlotInfo nativeMSInitializer() throws EmbeddedException;

   static {
      System.loadLibrary("aipposembedded");
   }
}
