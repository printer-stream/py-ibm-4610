package com.ibm.posj.bus.hid.javaxusb;

import com.ibm.jutil.Util;
import com.ibm.jutil.UtilProperties;
import com.ibm.jutil.UtilProperties.MultiProperty;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.util.DefaultProperties;
import com.ibm.posj.util.PosjUtil;
import java.util.HashSet;
import java.util.Iterator;

public class UsbIDUtil {
   private static UsbIDUtil instance = null;
   private HashSet ignoreInterface = new HashSet();
   private HashSet knownDevices = new HashSet();
   private HashSet oemScanners = new HashSet();
   private HashSet kbdDevices = new HashSet();
   private HashSet kbdAsSystemKbd = new HashSet();
   private Tracer tracer = TracerFactory.getInstance().createTracer("HID", "UsbIDUtil");
   private static final byte USBCARD_TYPE_1 = 1;
   private static final byte USBCARD_TYPE_2 = 2;
   private static final byte USBCARD_VERSION_14 = 20;
   private static final byte USBCARD_VERSION_7 = 7;
   public static final String IGNORE_INTERFACE_PROP = "com.ibm.posj.bus.hid.javaxusb.factory.ignoreInterface";
   public static final String KNOWN_POS_DEVICES_PROP = "com.ibm.posj.bus.hid.javaxusb.factory.knownPosDevices";
   public static final String OEM_SCANNERS_PROTOCOL_PROP = "com.ibm.posj.bus.hid.javaxusb.factory.oemScannerProtocols";
   public static final String KBD_AS_LEGACY_MODE_PROP = "com.ibm.posj.bus.hid.javaxusb.factory.kbdAsLegacyMode";
   public static final String KBD_AS_SYSTEM_KEYBOARD_PROP = "com.ibm.posj.bus.hid.javaxusb.factory.kbdAsSystemKbd";

   protected UsbIDUtil() {
   }

   public static synchronized UsbIDUtil getInstance() {
      if (instance == null) {
         instance = new UsbIDUtil();
         instance.init();
      }

      return instance;
   }

   public boolean ignoreInterface(short vendorId, short productId, byte i) {
      String key = "0x" + Util.toHexString(vendorId) + ",0x" + Util.toHexString(productId) + "," + i;
      return this.ignoreInterface.contains(key);
   }

   public static boolean isIBM4610(short vendorId, short productId) {
      return 17717 == productId && 1203 == vendorId;
   }

   public static boolean isIBM4689(short vendorId, short productId) {
      return 17719 == productId && 1203 == vendorId;
   }

   public boolean isKnownPosDevice(short vendorId, short productId) {
      return this.knownDevices.contains(String.valueOf(PosjUtil.convertTo(vendorId, productId)));
   }

   public boolean isOEMScannerRenumerationRequired(short vendorId, short productId) {
      return this.oemScanners.contains(String.valueOf(PosjUtil.convertTo(vendorId, productId)));
   }

   public boolean isKbdRenumerationRequired(short vendorId, short productId) {
      return this.kbdDevices.contains(String.valueOf(PosjUtil.convertTo(vendorId, productId)));
   }

   public boolean isKbdAsSystemKeyboard(short vendorId, short productId) {
      return this.kbdAsSystemKbd.contains(String.valueOf(PosjUtil.convertTo(vendorId, productId)));
   }

   protected static boolean isPrinterAdapterResetNeeded(short bcd) {
      byte high = (byte)(bcd >> 8);
      byte low = (byte)(bcd & 255);
      return 1 == high && low < 7 || 2 == high && low < 20;
   }

   protected void init() {
      UtilProperties prop = new DefaultProperties();
      prop.loadProperties();
      this.loadProperties(prop, "com.ibm.posj.bus.hid.javaxusb.factory.knownPosDevices", this.knownDevices);
      this.loadProperties(prop, "com.ibm.posj.bus.hid.javaxusb.factory.oemScannerProtocols", this.oemScanners);
      this.loadProperties(prop, "com.ibm.posj.bus.hid.javaxusb.factory.kbdAsLegacyMode", this.kbdDevices);
      this.loadProperties(prop, "com.ibm.posj.bus.hid.javaxusb.factory.kbdAsSystemKbd", this.kbdAsSystemKbd);
      this.loadPropertiesString(prop, "com.ibm.posj.bus.hid.javaxusb.factory.ignoreInterface", this.ignoreInterface);
   }

   protected void loadProperties(UtilProperties prop, String key, HashSet hashSet) {
      if (!prop.hasMultiProperty(key)) {
         this.tracer.println("None " + key + " properties found");
      } else {
         MultiProperty multiProp = prop.getMultiProperty(key);
         if (this.tracer.isOn()) {
            this.tracer.println("Loading " + multiProp.getNumberOfProperties() + " properties for " + key);
         }

         Iterator iterator = multiProp.getPropertyValues();
         short vId = 0;
         short pId = 0;
         int commapos = 0;
         int count = 1;
         String value = "";
         String tracerText = "";

         while (iterator.hasNext()) {
            value = (String)iterator.next();
            commapos = value.indexOf(44);

            try {
               vId = Integer.decode(value.substring(0, commapos)).shortValue();
               pId = Integer.decode(value.substring(commapos + 1)).shortValue();
               hashSet.add(String.valueOf(PosjUtil.convertTo(vId, pId)));
               if (this.tracer.isOn()) {
                  tracerText = tracerText + "{0x" + Util.toHexString(vId) + ",0x" + Util.toHexString(pId) + "} ";
                  if (++count % 7 == 1) {
                     tracerText = tracerText + "\n";
                  }
               }
            } catch (Exception var13) {
               this.tracer.println("Unable to load \"" + value + "\" value.");
            }
         }

         if (this.tracer.isOn()) {
            this.tracer.print("<" + key + " <vendorID>,<product ID>>\n" + tracerText + "\n</" + key + ">\n");
         }
      }
   }

   protected void loadPropertiesString(UtilProperties prop, String key, HashSet hashSet) {
      if (!prop.hasMultiProperty(key)) {
         this.tracer.println("None " + key + " properties found");
      } else {
         MultiProperty multiProp = prop.getMultiProperty(key);
         if (this.tracer.isOn()) {
            this.tracer.println("Loading " + multiProp.getNumberOfProperties() + " properties for " + key);
         }

         Iterator iterator = multiProp.getPropertyValues();
         int count = 1;
         String value = "";
         String tracerText = "";

         while (iterator.hasNext()) {
            value = (String)iterator.next();
            hashSet.add(value);
            if (this.tracer.isOn()) {
               tracerText = tracerText + "{" + value + "} ";
               if (++count % 6 == 1) {
                  tracerText = tracerText + "\n";
               }
            }
         }

         if (this.tracer.isOn()) {
            this.tracer.print("<" + key + " <vendorID>,<product ID>,<interface number>\n" + tracerText + "\n</" + key + ">\n");
         }
      }
   }
}
