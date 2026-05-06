package com.ibm.posj.bus.rs485;

import com.ibm.jutil.UtilProperties;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.util.DefaultProperties;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public final class Rs485Util {
   private static Rs485Util instance = null;
   private HashMap waitTime = new HashMap();
   private final Tracer tracer = TracerFactory.getInstance().createTracer("RS485", "Rs485Util");
   public static final String WAIT_TIME_PROP = "com.ibm.posj.bus.rs485.waitTime.";

   private Rs485Util() {
   }

   private static synchronized Rs485Util getInstance() {
      if (instance == null) {
         instance = new Rs485Util();
         instance.init();
      }

      return instance;
   }

   public static boolean hasCustomWaitTime(byte number) {
      return getMap().containsKey(getMapKey(number));
   }

   public static byte getCustomAdressWaitTime(byte number) {
      if (!hasCustomWaitTime(number)) {
         return -1;
      } else {
         Rs485Util.UtilSioDevice dev = (Rs485Util.UtilSioDevice)getMap().get(getMapKey(number));
         return dev.getAddressWaitTime();
      }
   }

   public static byte getCustomInterByteWaitTime(byte number) {
      if (!hasCustomWaitTime(number)) {
         return -1;
      } else {
         Rs485Util.UtilSioDevice dev = (Rs485Util.UtilSioDevice)getMap().get(getMapKey(number));
         return dev.getInterByteWaitTime();
      }
   }

   private void init() {
      UtilProperties prop = new DefaultProperties();
      prop.loadProperties();
      this.loadProperties(prop, this.waitTime);
   }

   private void loadProperties(UtilProperties prop, Map map) {
      if (this.tracer.isOn()) {
         this.trace("-->loadProperties");
      }

      Enumeration e = prop.getPropertyNames();
      String property = "";
      String value = "";
      String devId = "";
      StringBuffer traceText = new StringBuffer();
      int pos = 0;
      byte aWait = 0;
      byte bWait = 0;

      while (true) {
         while (true) {
            if (!e.hasMoreElements()) {
               if (this.tracer.isOn()) {
                  this.trace(map.size() + " properties loaded for " + "com.ibm.posj.bus.rs485.waitTime.");
                  this.trace(
                     "<com.ibm.posj.bus.rs485.waitTime. Format :<SioDeviceNumber>,<Address wait>,<InterByte wait>>\n"
                        + traceText.toString()
                        + "\n</"
                        + "com.ibm.posj.bus.rs485.waitTime."
                        + ">"
                  );
                  this.trace("<--loadProperties");
               }

               return;
            }

            property = (String)e.nextElement();
            if (property.startsWith("com.ibm.posj.bus.rs485.waitTime.")) {
               value = prop.getStringProperty(property);
               pos = value.indexOf(44);

               try {
                  aWait = Integer.decode(value.substring(0, pos)).byteValue();
                  bWait = Integer.decode(value.substring(pos + 1)).byteValue();
                  break;
               } catch (NumberFormatException var12) {
                  if (this.tracer.isOn()) {
                     this.tracer.print(var12);
                     this.trace("Unable to load \"" + property + "=" + value + "\" property : " + var12.toString());
                  }
               }
            }
         }

         pos = property.lastIndexOf(46);
         devId = property.substring(pos + 1);
         if (this.tracer.isOn() && !map.containsKey(devId)) {
            traceText.append("{" + devId + "=" + value + "} ");
            if (map.size() % 5 == 4) {
               traceText.append("\n");
            }
         }

         map.put(devId, new Rs485Util.UtilSioDevice(aWait, bWait));
      }
   }

   private static Map getMap() {
      return getInstance().waitTime;
   }

   private static Object getMapKey(byte number) {
      return Integer.toHexString(number).toUpperCase();
   }

   private void trace(String s) {
      this.tracer.println(2, s);
   }

   private class UtilSioDevice {
      private byte aWait;
      private byte iWait;

      public UtilSioDevice(byte a, byte i) {
         this.aWait = a;
         this.iWait = i;
      }

      public byte getAddressWaitTime() {
         return this.aWait;
      }

      public byte getInterByteWaitTime() {
         return this.iWait;
      }
   }
}
