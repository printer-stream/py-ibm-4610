package com.ibm.posj.bus.rs232.javaxcomm;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.util.DevBuses;
import com.ibm.rs232.Rs232Config;
import com.ibm.rs232.Rs232Exception;
import com.ibm.rs232.Rs232Port;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.comm.CommPortIdentifier;

class Rs232PortManager {
   private static Rs232PortManager instance = null;
   private Rs232PortCommFactory portCommFactory = null;
   private Hashtable portsTable = new Hashtable();
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevBuses.RS232_DEVBUS.toString(), "Rs232PortManager");

   private Rs232PortManager() {
      Enumeration e = CommPortIdentifier.getPortIdentifiers();
      String ports = "";

      while (e.hasMoreElements()) {
         ports = ports + ((CommPortIdentifier)e.nextElement()).getName() + " ";
      }

      if (this.tracer.isOn()) {
         this.tracer.println("Ports detected : " + ports);
      }
   }

   public static synchronized Rs232PortManager getInstance() {
      if (instance == null) {
         instance = new Rs232PortManager();
      }

      return instance;
   }

   public Rs232Port createRs232Port(Rs232Config config) throws Rs232Exception {
      return this.getRs232PortCommFactory().createRs232Port(config);
   }

   public boolean addRs232Port(Rs232Port rs232Port) {
      String portName = rs232Port.getRs232config().getPortName();
      if (this.hasRs232Port(portName)) {
         if (!this.getRs232Port(portName).getRs232config().equals(rs232Port.getRs232config())) {
            return false;
         }

         if (this.tracer.isOn()) {
            this.tracer.println("Rs232Port < " + portName + "> exists in portManager use the previous one");
         }
      } else {
         this.portsTable.put(portName, rs232Port);
         if (this.tracer.isOn()) {
            this.tracer.println("Rs232Port with " + rs232Port.getRs232config() + " added to the manager");
         }
      }

      return true;
   }

   public void removeRs232Port(String portName) {
      if (this.portsTable.remove(portName) != null && this.tracer.isOn()) {
         this.tracer.println("Rs232Port " + portName + " removed from manager");
      }
   }

   public Rs232Port getRs232Port(String portName) {
      return (Rs232Port)this.portsTable.get(portName);
   }

   public boolean hasRs232Port(String portName) {
      return this.portsTable.containsKey(portName);
   }

   protected Rs232PortCommFactory getRs232PortCommFactory() {
      if (this.portCommFactory == null) {
         this.portCommFactory = new Rs232PortCommFactory();
      }

      return this.portCommFactory;
   }
}
