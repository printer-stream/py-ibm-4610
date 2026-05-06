package com.ibm.posj.bus.rs232;

import com.ibm.posj.HandleKey;
import com.ibm.posj.HandleKeyVisitor;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.rs232.Rs232Config;

public class Rs232HandleKey implements HandleKey {
   private DevCat devCat = null;
   private int number = 0;
   private String name = "";
   private Rs232Config rs232Config = null;

   public Rs232HandleKey(DevCat devCat, int number, Rs232Config config) {
      this.devCat = devCat;
      this.number = number;
      this.rs232Config = config;
      StringBuffer sb = new StringBuffer();
      sb.append("<Rs232HandleKey:");
      sb.append("\n\t devCat=\"" + devCat.toString());
      if (devCat.equals(DevCats.CASHDRAWER_DEVCAT)) {
         sb.append("\n\t number = " + number);
      }

      sb.append("\n\t portName=" + config.getPortName());
      sb.append("\"/>\n");
      this.name = sb.toString();
   }

   public Rs232HandleKey(DevCat devCat, int number, String portName, int baudRate, int dataBits, int parity, int stopBits, int flowControl) {
      this(devCat, number, new Rs232Config(portName, baudRate, dataBits, parity, stopBits, flowControl));
   }

   public Rs232HandleKey(DevCat devCat, int number, String portName) {
      this(devCat, number, new Rs232Config(portName, 0, 0, 0, 0, 0));
   }

   public void accept(HandleKeyVisitor visitor) {
      visitor.visitRs232HandleKey(this);
   }

   public DevCat getDevCat() {
      return this.devCat;
   }

   public int getNumber() {
      return this.number;
   }

   public String getPortName() {
      return this.rs232Config.getPortName();
   }

   public Rs232Config getRs232Config() {
      return this.rs232Config;
   }

   public DevBus getDevBus() {
      return DevBuses.RS232_DEVBUS;
   }

   public String toString() {
      return this.name;
   }

   public int hashCode() {
      return this.toString().hashCode();
   }

   public boolean equals(Object obj) {
      try {
         return ((Rs232HandleKey)obj).toString().equals(this.toString());
      } catch (NullPointerException var3) {
         return false;
      } catch (ClassCastException var4) {
         return false;
      }
   }
}
