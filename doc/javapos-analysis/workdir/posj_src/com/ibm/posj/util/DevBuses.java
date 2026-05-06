package com.ibm.posj.util;

import java.util.Hashtable;

public class DevBuses {
   private static final Hashtable DEVBUS_TABLE = new Hashtable();
   public static final DevBus EMBEDDED_DEVBUS = DevBuses.Embedded.getInstance();
   public static final DevBus POSKBD_DEVBUS = DevBuses.PosKbd.getInstance();
   public static final DevBus USB_DEVBUS = DevBuses.Usb.getInstance();
   public static final DevBus HID_DEVBUS = DevBuses.Hid.getInstance();
   public static final DevBus RS232_DEVBUS = DevBuses.RS232.getInstance();
   public static final DevBus RS485_DEVBUS = DevBuses.RS485.getInstance();
   public static final DevBus UNKNOWN_DEVBUS = DevBuses.Unknown.getInstance();
   public static final DevBus[] DEVBUS_ARRAY = new DevBus[]{POSKBD_DEVBUS, USB_DEVBUS, HID_DEVBUS, RS232_DEVBUS, RS485_DEVBUS, UNKNOWN_DEVBUS, EMBEDDED_DEVBUS};

   public static DevBus getDevBusForName(String devBusName) {
      return DEVBUS_TABLE.containsKey(devBusName) ? (DevBus)DEVBUS_TABLE.get(devBusName) : DevBuses.Unknown.getInstance();
   }

   static {
      DEVBUS_TABLE.put(EMBEDDED_DEVBUS.toString(), EMBEDDED_DEVBUS);
      DEVBUS_TABLE.put(POSKBD_DEVBUS.toString(), POSKBD_DEVBUS);
      DEVBUS_TABLE.put(USB_DEVBUS.toString(), USB_DEVBUS);
      DEVBUS_TABLE.put(HID_DEVBUS.toString(), HID_DEVBUS);
      DEVBUS_TABLE.put(RS232_DEVBUS.toString(), RS232_DEVBUS);
      DEVBUS_TABLE.put(RS485_DEVBUS.toString(), RS485_DEVBUS);
      DEVBUS_TABLE.put(UNKNOWN_DEVBUS.toString(), UNKNOWN_DEVBUS);
   }

   public abstract static class AbstractDevBus implements DevBus {
      public abstract String toString();

      public boolean isProprietary() {
         return false;
      }

      public int hashCode() {
         return this.toString().hashCode();
      }

      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         } else {
            return !(obj instanceof DevBus) ? false : this.toString().equals(obj.toString());
         }
      }
   }

   public static class Embedded extends DevBuses.AbstractDevBus implements DevBus.Embedded {
      private static DevBus instance = null;

      Embedded() {
      }

      public static DevBus getInstance() {
         if (instance == null) {
            instance = new DevBuses.Embedded();
         }

         return instance;
      }

      public String toString() {
         return "Embedded";
      }

      public String getName() {
         return this.toString();
      }

      public boolean isProprietary() {
         return true;
      }

      public void accept(DevBusVisitor visitor) {
         visitor.visitEmbedded(this);
      }
   }

   public static class Hid extends DevBuses.AbstractDevBus implements DevBus.Hid {
      private static DevBus instance = null;

      Hid() {
      }

      public static DevBus getInstance() {
         if (instance == null) {
            instance = new DevBuses.Hid();
         }

         return instance;
      }

      public String toString() {
         return "HID";
      }

      public String getName() {
         return this.toString();
      }

      public void accept(DevBusVisitor visitor) {
         visitor.visitHid(this);
      }
   }

   public static class PosKbd extends DevBuses.AbstractDevBus implements DevBus.PosKbd {
      private static DevBus instance = null;

      PosKbd() {
      }

      public static DevBus getInstance() {
         if (instance == null) {
            instance = new DevBuses.PosKbd();
         }

         return instance;
      }

      public String toString() {
         return "PosKbd";
      }

      public String getName() {
         return this.toString();
      }

      public boolean isProprietary() {
         return true;
      }

      public void accept(DevBusVisitor visitor) {
         visitor.visitPosKbd(this);
      }
   }

   public static class RS232 extends DevBuses.AbstractDevBus implements DevBus.RS232 {
      private static DevBus instance = null;

      RS232() {
      }

      public static DevBus getInstance() {
         if (instance == null) {
            instance = new DevBuses.RS232();
         }

         return instance;
      }

      public String toString() {
         return "RS232";
      }

      public String getName() {
         return this.toString();
      }

      public void accept(DevBusVisitor visitor) {
         visitor.visitRS232(this);
      }
   }

   public static class RS485 extends DevBuses.AbstractDevBus implements DevBus.RS485 {
      private static DevBus instance = null;

      RS485() {
      }

      public static DevBus getInstance() {
         if (instance == null) {
            instance = new DevBuses.RS485();
         }

         return instance;
      }

      public String toString() {
         return "RS485";
      }

      public String getName() {
         return this.toString();
      }

      public void accept(DevBusVisitor visitor) {
         visitor.visitRS485(this);
      }
   }

   public static class Unknown extends DevBuses.AbstractDevBus implements DevBus.Unknown {
      private static DevBus instance = null;

      Unknown() {
      }

      public static DevBus getInstance() {
         if (instance == null) {
            instance = new DevBuses.Unknown();
         }

         return instance;
      }

      public String toString() {
         return "Unknown";
      }

      public String getName() {
         return this.toString();
      }

      public void accept(DevBusVisitor visitor) {
         visitor.visitUnknown(this);
      }
   }

   public static class Usb extends DevBuses.AbstractDevBus implements DevBus.Usb {
      private static DevBus instance = null;

      Usb() {
      }

      public static DevBus getInstance() {
         if (instance == null) {
            instance = new DevBuses.Usb();
         }

         return instance;
      }

      public String toString() {
         return "USB";
      }

      public String getName() {
         return this.toString();
      }

      public void accept(DevBusVisitor visitor) {
         visitor.visitUsb(this);
      }
   }
}
