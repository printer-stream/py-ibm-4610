package com.ibm.posj.util;

public interface DevBus {
   String EMBEDDED_BUS_NAME = "Embedded";
   String POSKBD_BUS_NAME = "PosKbd";
   String USB_BUS_NAME = "USB";
   String HID_BUS_NAME = "HID";
   String RS232_BUS_NAME = "RS232";
   String RS485_BUS_NAME = "RS485";
   String UNKNOWN_BUS_NAME = "Unknown";

   String toString();

   String getName();

   boolean isProprietary();

   void accept(DevBusVisitor var1);

   public interface Embedded extends DevBus {
   }

   public interface Hid extends DevBus {
   }

   public interface PosKbd extends DevBus {
   }

   public interface RS232 extends DevBus {
   }

   public interface RS485 extends DevBus {
   }

   public interface Unknown extends DevBus {
   }

   public interface Usb extends DevBus {
   }
}
