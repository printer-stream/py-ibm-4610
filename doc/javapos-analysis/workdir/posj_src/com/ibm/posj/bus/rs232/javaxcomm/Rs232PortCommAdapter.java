package com.ibm.posj.bus.rs232.javaxcomm;

import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.util.DevBuses;
import com.ibm.rs232.DefaultRs232PortEventHelper;
import com.ibm.rs232.Rs232Config;
import com.ibm.rs232.Rs232Exception;
import com.ibm.rs232.Rs232Port;
import com.ibm.rs232.event.Rs232ControlLineEvent;
import com.ibm.rs232.event.Rs232DataEvent;
import com.ibm.rs232.event.Rs232OfflineEvent;
import com.ibm.rs232.event.Rs232OnlineEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TooManyListenersException;
import javax.comm.CommPortIdentifier;
import javax.comm.CommPortOwnershipListener;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

public class Rs232PortCommAdapter implements Rs232Port {
   private boolean isOpen = false;
   private boolean isInit = false;
   private Rs232Config rs232Config = null;
   private CommPortIdentifier portIdentifier = null;
   private SerialPort serialPort = null;
   private SerialPortEventListener serialPortListener = new Rs232PortCommAdapter.SerialPortListener();
   private CommPortOwnershipListener commListener = new Rs232PortCommAdapter.OwnershipListener();
   private HashSet appsSet = new HashSet();
   private Rs232PortCommAdapter.DataReaderStrategy reader = new Rs232PortCommAdapter.DefaultDataReaderStrategy();
   private Rs232Port.OnlineWatcher onlineWatcher = null;
   private Rs232Port.EventHelper eventHelper = new DefaultRs232PortEventHelper(this);
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevBuses.RS232_DEVBUS.toString(), "Rs232PortCommAdapter");
   public static final int OPEN_TIMEOUT = 2000;

   public Rs232PortCommAdapter(Rs232Config config) throws Rs232Exception {
      this.rs232Config = config;
      this.checkPortName(this.getRs232config().getPortName());
   }

   public synchronized void init() throws Rs232Exception {
   }

   public synchronized void open(String appName) throws Rs232Exception {
      if (this.isOpen()) {
         this.appsSet.add(appName);
      } else {
         this.openSerialPort();
         this.appsSet.add(appName);
         this.isOpen = true;
      }
   }

   public synchronized void close(String appName) {
      this.appsSet.remove(appName);
      if (this.isOpen && this.appsSet.isEmpty() && this.serialPort != null) {
         this.closeSerialPort();
      }

      this.isOpen = false;
   }

   public synchronized boolean isOpen() {
      return this.isOpen;
   }

   public synchronized Iterator iterator() {
      return this.appsSet.iterator();
   }

   public void setFlowControlMode(int control) throws UnsupportedCommOperationException {
      this.serialPort.setFlowControlMode(control);
   }

   public void setReaderStrategy(Rs232PortCommAdapter.DataReaderStrategy reader) {
      if (null != reader) {
         this.reader = reader;
      }
   }

   public synchronized void submit(byte[] data) throws Rs232Exception {
      this.submit(data, 0, data.length);
   }

   public synchronized void submit(byte[] data, int offset, int length) throws Rs232Exception {
      if (this.traceOn()) {
         this.trace(">>Submit thread-" + Thread.currentThread().getName() + "- " + Util.toFormatedHexString(data, offset, length));
      }

      try {
         this.serialPort.getOutputStream().write(data, offset, length);
      } catch (Exception var5) {
         if (this.traceOn()) {
            this.trace("<<Submit Exception!");
            this.tracer.print(var5);
         }

         throw new Rs232Exception("Error when writting bytes to the port", var5);
      }

      if (this.traceOn()) {
         this.trace("<<Submit");
      }
   }

   public synchronized Rs232Config getRs232config() {
      return this.rs232Config;
   }

   public synchronized void addRs232PortListener(Rs232Port.Listener listener) {
      this.eventHelper.addRs232PortListener(listener);
   }

   public synchronized void removeRs232PortListener(Rs232Port.Listener listener) {
      this.eventHelper.removeRs232PortListener(listener);
   }

   public void setDTR(boolean enabled) {
      this.serialPort.setDTR(enabled);
   }

   public void setRTS(boolean active) {
      this.serialPort.setRTS(active);
   }

   public boolean isCTS() {
      return this.serialPort.isCTS();
   }

   public Rs232Port.OnlineWatcher getOnlineWatcher() {
      if (this.onlineWatcher == null) {
         this.onlineWatcher = new Rs232OnlineWatcher(this, this.eventHelper);
      }

      return this.onlineWatcher;
   }

   protected void checkPortName(String portName) throws Rs232Exception {
      try {
         this.portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
      } catch (NoSuchPortException var3) {
         throw new Rs232Exception("The port \"" + portName + "\" does not exist ", var3);
      }

      if (this.portIdentifier.getPortType() != 1) {
         throw new Rs232Exception("The port \"" + portName + "\" is not a Serial port");
      } else {
         this.portIdentifier.addPortOwnershipListener(this.commListener);
      }
   }

   protected void openSerialPort() throws Rs232Exception {
      if (this.traceOn()) {
         this.trace("-->openSerialPort()");
      }

      try {
         String appName = "Rs232Port-" + this.getRs232config().getPortName();
         this.serialPort = (SerialPort)this.portIdentifier.open(appName, 2000);
         this.serialPort.addEventListener(this.serialPortListener);
         this.serialPort
            .setSerialPortParams(
               this.getRs232config().getBaudRate(), this.getRs232config().getDataBits(), this.getRs232config().getStopBits(), this.getRs232config().getParity()
            );
         this.setFlowControlMode(this.getRs232config().getFlowControl());
         this.serialPort.notifyOnFramingError(true);
         this.serialPort.notifyOnOverrunError(true);
         this.serialPort.notifyOnParityError(true);
         this.serialPort.notifyOnDSR(true);
         this.serialPort.notifyOnCTS(true);
         this.serialPort.notifyOnDataAvailable(true);
      } catch (UnsupportedCommOperationException var7) {
         this.closeSerialPort();
         throw new Rs232Exception("UnsupportedCommOperationException", var7);
      } catch (PortInUseException var8) {
         this.closeSerialPort();
         throw new Rs232Exception("PortInUseException", var8);
      } catch (TooManyListenersException var9) {
         this.closeSerialPort();
         throw new Rs232Exception("TooManyListenersException", var9);
      } finally {
         ;
      }

      if (this.traceOn()) {
         this.trace("<--openSerialPort()");
      }
   }

   protected void closeSerialPort() {
      if (this.traceOn()) {
         this.trace("-->closeSerialPort()");
      }

      try {
         this.serialPort.notifyOnFramingError(false);
         this.serialPort.notifyOnOverrunError(false);
         this.serialPort.notifyOnParityError(false);
         this.serialPort.notifyOnDSR(false);
         this.serialPort.notifyOnCTS(false);
         this.serialPort.notifyOnDataAvailable(false);
         this.serialPort.notifyOnOutputEmpty(false);
         this.serialPort.removeEventListener();
         this.serialPort.close();
      } catch (Exception var2) {
      }

      if (this.traceOn()) {
         this.trace("<--closeSerialPort()");
      }
   }

   protected void ownershipChange(int type) {
      if (type == 1) {
         if (this.traceOn()) {
            this.trace("ownershipEvent-> PORT_OWNED");
         }
      } else if (type == 3) {
         if (this.traceOn()) {
            this.trace("ownershipEvent-> PORT_OWNERSHIP_REQUESTED");
         }
      } else {
         this.trace("ownershipEvent-> PORT_UNOWNED");
      }
   }

   protected void serialEvent(SerialPortEvent evt) {
      switch (evt.getEventType()) {
         case 1:
            try {
               this.reader.dataAvailable(this.serialPort.getInputStream(), this.eventHelper);
            } catch (IOException var3) {
               if (this.traceOn()) {
                  this.trace("Error when reading data : " + var3.toString());
               }
            }
            break;
         case 2:
            if (this.traceOn()) {
               this.trace("serialEvent() OUTBUF_EMPTY received");
            }

            this.eventHelper.fireControlLineEvent(new Rs232ControlLineEvent(this, 3, evt.getNewValue()));
            break;
         case 3:
            if (this.traceOn()) {
               this.trace("serialEvent() CTS " + (evt.getNewValue() ? "active" : "inactive") + " received");
            }

            this.eventHelper.fireControlLineEvent(new Rs232ControlLineEvent(this, 1, evt.getNewValue()));
            break;
         case 4:
            if (this.serialPort.isDSR()) {
               this.eventHelper.fireOnlineEvent(new Rs232OnlineEvent(this));
            } else {
               this.eventHelper.fireOfflineEvent(new Rs232OfflineEvent(this));
            }

            if (this.traceOn()) {
               this.trace("serialEvent() DSR received " + this.serialPort.isDSR());
            }

            this.eventHelper.fireControlLineEvent(new Rs232ControlLineEvent(this, 2, evt.getNewValue()));
            break;
         case 5:
            if (this.traceOn()) {
               this.trace("serialEvent() RI received");
            }
            break;
         case 6:
            if (this.traceOn()) {
               this.trace("serialEvent() CD received");
            }
            break;
         case 7:
            if (this.traceOn()) {
               this.trace("serialEvent() OE received");
            }
            break;
         case 8:
            if (this.traceOn()) {
               this.trace("serialEvent() PE received");
            }
            break;
         case 9:
            if (this.traceOn()) {
               this.trace("serialEvent() FE received");
            }
            break;
         case 10:
            if (this.traceOn()) {
               this.trace("serialEvent() BI received");
            }
      }
   }

   Rs232Port.EventHelper getEventhelper() {
      return this.eventHelper;
   }

   private boolean traceOn() {
      return this.tracer.isOn();
   }

   private void trace(String msg) {
      this.tracer.println(this.getRs232config().getPortName() + " " + msg);
   }

   public interface DataReaderStrategy {
      void dataAvailable(InputStream var1, Rs232Port.EventHelper var2) throws IOException;
   }

   private class DefaultDataReaderStrategy implements Rs232PortCommAdapter.DataReaderStrategy {
      private DefaultDataReaderStrategy() {
      }

      public void dataAvailable(InputStream is, Rs232Port.EventHelper helper) throws IOException {
         int bytesAvailable = Rs232PortCommAdapter.this.serialPort.getInputStream().available();
         if (bytesAvailable > 0) {
            byte[] data = new byte[bytesAvailable];

            for (int i = 0; i < data.length; i++) {
               data[i] = (byte)Rs232PortCommAdapter.this.serialPort.getInputStream().read();
            }

            if (Rs232PortCommAdapter.this.traceOn()) {
               Rs232PortCommAdapter.this.trace("Data available [" + data.length + "]" + Util.toFormatedHexString(data));
            }

            helper.fireDataEvent(new Rs232DataEvent(this, data));
         }
      }
   }

   private class OwnershipListener implements CommPortOwnershipListener {
      private OwnershipListener() {
      }

      public void ownershipChange(int type) {
         Rs232PortCommAdapter.this.ownershipChange(type);
      }
   }

   private class SerialPortListener implements SerialPortEventListener {
      private SerialPortListener() {
      }

      public void serialEvent(SerialPortEvent evt) {
         Rs232PortCommAdapter.this.serialEvent(evt);
      }
   }
}
