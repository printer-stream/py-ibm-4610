package com.ibm.posj.bus.rs232.javaxcomm;

import com.ibm.rs232.Rs232Config;
import java.util.Enumeration;
import jpos.config.JposEntry;
import jpos.config.simple.xml.SimpleXmlRegPopulator;

public class Rs232Util {
   public static final String DEFAULT_PORTNAME = "";
   public static final int DEFAULT_BAUDRATE = 9600;
   public static final int DEFAULT_DATABITS = 8;
   public static final int DEFAULT_PARITY = 0;
   public static final int DEFAULT_STOPBITS = 1;
   public static final int DEFAULT_FLOWCONTROL = 0;

   public static Rs232Config createRs232Config(JposEntry jposEntry) {
      Object obj = jposEntry.getPropertyValue("portName");
      String portName;
      if (obj == null) {
         portName = "";
      } else {
         portName = (String)obj;
      }

      obj = jposEntry.getPropertyValue("baudRate");
      int baudRate;
      if (obj == null) {
         baudRate = 9600;
      } else {
         baudRate = Integer.valueOf((String)obj);
      }

      obj = jposEntry.getPropertyValue("dataBits");
      int dataBits;
      if (obj == null) {
         dataBits = 8;
      } else {
         dataBits = Integer.valueOf((String)obj);
      }

      obj = jposEntry.getPropertyValue("parity");
      int parity;
      if (obj == null) {
         parity = 0;
      } else {
         parity = mapParity((String)obj);
      }

      obj = jposEntry.getPropertyValue("stopBits");
      int stopBits;
      if (obj == null) {
         stopBits = 1;
      } else {
         stopBits = mapStopBits((String)obj);
      }

      obj = jposEntry.getPropertyValue("flowControl");
      int flowControl;
      if (obj == null) {
         flowControl = 0;
      } else {
         flowControl = mapFlowControl((String)obj);
      }

      return new Rs232Config(portName, baudRate, dataBits, parity, stopBits, flowControl);
   }

   public static Enumeration getJposEntries(String fileName) {
      SimpleXmlRegPopulator xmlPop = new SimpleXmlRegPopulator();
      xmlPop.load(fileName);
      return xmlPop.getEntries();
   }

   protected static int mapParity(String jclParity) {
      int parity;
      if (jclParity.equals("None")) {
         parity = 0;
      } else if (jclParity.equals("Even")) {
         parity = 2;
      } else if (jclParity.equals("Mark")) {
         parity = 3;
      } else if (jclParity.equals("Odd")) {
         parity = 1;
      } else if (jclParity.equals("Space")) {
         parity = 4;
      } else {
         parity = 0;
      }

      return parity;
   }

   protected static int mapStopBits(String jclStopBits) {
      int stopBits;
      if (jclStopBits.equals("1")) {
         stopBits = 1;
      } else if (jclStopBits.equals("1.5")) {
         stopBits = 3;
      } else if (jclStopBits.equals("2")) {
         stopBits = 2;
      } else {
         stopBits = 1;
      }

      return stopBits;
   }

   protected static int mapFlowControl(String jclFlowControl) {
      int flowControl;
      if (jclFlowControl.equals("None")) {
         flowControl = 0;
      } else if (jclFlowControl.equals("Xon/Xoff")) {
         flowControl = 12;
      } else if (jclFlowControl.equals("Hardware")) {
         flowControl = 3;
      } else {
         flowControl = 0;
      }

      return flowControl;
   }
}
