package com.ibm.posj.bus.poskbd;

import com.ibm.jutil.IntBuffer;
import com.ibm.jutil.Util;
import com.ibm.jutil.UtilProperties;
import com.ibm.jutil.UtilProperties.MultiProperty;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.util.DefaultProperties;
import java.util.HashSet;
import java.util.Iterator;

public class PoskbdIDUtil {
   private static PoskbdIDUtil instance = null;
   private IntBuffer validkbdsVersion = IntBuffer.getIntBufferFactory().createIntBuffer();
   private IntBuffer validkbdsVendor = IntBuffer.getIntBufferFactory().createIntBuffer();
   private IntBuffer validkbdsProduct = IntBuffer.getIntBufferFactory().createIntBuffer();
   private HashSet knownKeyboards = new HashSet();
   private Tracer tracer = TracerFactory.getInstance().createTracer("POSKBD", "PoskbdIDUtil");
   public static final String KNOWN_PS2_KEYBOARD_PROP = "com.ibm.posj.bus.poskbd.PoskbdHandlePopulator.knownPS2Keyboard";
   public static final String USB_AS_SYSTEM_KEYBOARD_PROP = "com.ibm.posj.bus.hid.javaxusb.factory.kbdAsSystemKbd";

   protected PoskbdIDUtil() {
   }

   public static synchronized PoskbdIDUtil getInstance() {
      if (instance == null) {
         instance = new PoskbdIDUtil();
         instance.init();
      }

      return instance;
   }

   public boolean isKnownVersionID(int versionId) {
      return this.knownKeyboards.contains(String.valueOf(versionId));
   }

   public int[] getValidKbds() {
      return this.validkbdsVersion.getInts();
   }

   public int[] getValidKbdsByVendor() {
      return this.validkbdsVendor.getInts();
   }

   public int[] getValidKbdsByProduct() {
      return this.validkbdsProduct.getInts();
   }

   protected void init() {
      UtilProperties prop = new DefaultProperties();
      prop.loadProperties();
      this.loadValidKbdVersionProperties(prop, this.knownKeyboards);
      this.loadValidKbdVendorProductProperties(prop);
   }

   protected void loadValidKbdVendorProductProperties(UtilProperties prop) {
      String key = "com.ibm.posj.bus.hid.javaxusb.factory.kbdAsSystemKbd";
      if (prop.hasMultiProperty(key)) {
         MultiProperty multiProp = prop.getMultiProperty(key);
         if (this.tracer.isOn()) {
            this.tracer.println("Loading " + multiProp.getNumberOfProperties() + " properties for " + key);
         }

         Iterator iterator = multiProp.getPropertyValues();
         int vendorId = 0;
         int productId = 0;
         int count = 1;
         int posOfCol = 0;
         String tracerText = "";

         while (iterator.hasNext()) {
            String valueAll = (String)iterator.next();

            try {
               posOfCol = valueAll.indexOf(44);
               if (posOfCol == -1) {
                  throw new Exception();
               }

               String value1 = valueAll.substring(0, posOfCol);
               String value2 = valueAll.substring(posOfCol + 1, valueAll.length());
               vendorId = Integer.decode(value1);
               productId = Integer.decode(value2);
               this.validkbdsVendor.append(vendorId);
               this.validkbdsProduct.append(productId);
               if (this.tracer.isOn()) {
                  tracerText = tracerText + "{0x" + Util.toHexString(vendorId) + ",0x" + Util.toHexString(productId) + "} ";
                  if (++count % 7 == 1) {
                     tracerText = tracerText + "\n";
                  }
               }
            } catch (Exception var14) {
               if (this.tracer.isOn()) {
                  this.tracer.println("Unable to load \"" + valueAll + "\" value.");
               }
            }
         }

         if (this.tracer.isOn()) {
            this.tracer.print("<" + key + " <vendorID,productID>>\n" + tracerText + "\n</" + key + ">\n");
         }
      } else if (this.tracer.isOn()) {
         this.tracer.println("No " + key + " properties found");
      }
   }

   protected void loadValidKbdVersionProperties(UtilProperties prop, HashSet hashSet) {
      String key = "com.ibm.posj.bus.poskbd.PoskbdHandlePopulator.knownPS2Keyboard";
      if (prop.hasMultiProperty(key)) {
         MultiProperty multiProp = prop.getMultiProperty(key);
         if (this.tracer.isOn()) {
            this.tracer.println("Loading " + multiProp.getNumberOfProperties() + " properties for " + key);
         }

         Iterator iterator = multiProp.getPropertyValues();
         int versionId = 0;
         int count = 1;
         String value = "";
         String tracerText = "";

         while (iterator.hasNext()) {
            value = (String)iterator.next();

            try {
               versionId = Integer.decode(value);
               this.validkbdsVersion.append(versionId);
               hashSet.add(String.valueOf(versionId));
               if (this.tracer.isOn()) {
                  tracerText = tracerText + "{0x" + Util.toHexString(versionId) + "} ";
                  if (++count % 7 == 1) {
                     tracerText = tracerText + "\n";
                  }
               }
            } catch (Exception var11) {
               if (this.tracer.isOn()) {
                  this.tracer.println("Unable to load \"" + value + "\" value.");
               }
            }
         }

         if (this.tracer.isOn()) {
            this.tracer.print("<" + key + " <versionID>>\n" + tracerText + "\n</" + key + ">\n");
         }
      } else if (this.tracer.isOn()) {
         this.tracer.println("No " + key + " properties found");
      }
   }
}
