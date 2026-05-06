package com.ibm.posj.bus.rs232;

import com.ibm.jutil.Timerable;
import com.ibm.jutil.Util;
import com.ibm.posj.HandleException;
import com.ibm.posj.bus.rs232.javaxcomm.Rs232PortCommAdapter;
import com.ibm.rs232.DefaultRs232PortListener;
import com.ibm.rs232.Rs232Port;
import com.ibm.rs232.event.Rs232DataEvent;
import java.io.IOException;
import java.io.InputStream;

public class Rs232IntegratedOmniScannerProtocol extends Rs232IntegratedScannerProtocol {
   public static final int DEFAULT_TIMEOUT_BETWEEN_CHARACTERS_IN_MILLIS = 1000;
   public static final int DEFAULT_SERIAL_RESPONSE_TIMEOUT_IN_MILLIS = 2000;
   private static final int INIT_RTS_MAX_ATTEMPTS = 5;
   public static final int MAX_PACKET_SEND_ATTEMPTS = 5;
   private static final int BUFFER_SIZE = 600;
   private static final int MINIMUM_TRANSMISSION_DELAY = 10;
   private int packetSendAttemptsCounter = 0;
   private byte[] lastPacketReceived = null;
   private byte[] lastPacketAccepted = null;
   private byte[] lastPacketSent = null;
   private Rs232PortCommAdapter serialPort = null;
   private Rs232IntegratedOmniScannerStateManager stateManager = null;
   private Rs232IntegratedOmniScannerProtocol.OmniScannerReaderStrategy omniScannerReaderStrategy = null;
   private Rs232IntegratedOmniScannerProtocol.OmniScannerRs232PortListener omniScannerRs232PortListener = null;
   private boolean transmissionPending = false;
   private int timeToTransmitAByte = 2;

   public Rs232IntegratedOmniScannerProtocol(
      Rs232IntegratedScannerProtocolListener protocolListener, Rs232PortCommAdapter serialPort, int devicePollTimeInSeconds
   ) {
      super(protocolListener, devicePollTimeInSeconds);
      this.serialPort = serialPort;
      this.timeToTransmitAByte = this.calculateTimeToTransmitAByte(this.serialPort);
      this.start();
   }

   public void setRTS(boolean active) {
      if (this.tracer.isOn()) {
         this.tracer.println(3, "Setting RTS " + (active ? "up" : "down") + ".");
      }

      this.serialPort.setRTS(active);
   }

   public void pollForCTS(boolean value) throws HandleException {
      try {
         Thread.sleep(50L);
      } catch (InterruptedException var3) {
      }
   }

   public void submitToAdapter(byte[] packet) {
      super.submitToAdapter(packet);
      this.waitForBytesToBeSent(packet.length);
      this.packetSendAttemptsCounter++;
   }

   public byte[] getLastPacketReceived() {
      return this.lastPacketReceived;
   }

   public void setLastPacketWritten(byte[] packet) {
      this.lastPacketSent = packet;
   }

   public byte[] getLastPacketWritten() {
      return this.lastPacketSent;
   }

   public boolean maxPacketSendAttemptsReached() {
      return this.packetSendAttemptsCounter >= 5;
   }

   public void setFirstPacketSendAttempt() {
      this.packetSendAttemptsCounter = 0;
   }

   public void flush() {
      if (this.omniScannerReaderStrategy.getInputStream() != null) {
         super.flushInputStream(this.omniScannerReaderStrategy.getInputStream());
      }

      this.omniScannerReaderStrategy.resetBuffer();
   }

   public boolean isTransmissionPending() {
      return this.transmissionPending;
   }

   public void setTransmissionPending(boolean desynchronized) {
      this.transmissionPending = desynchronized;
   }

   public byte[] getLastPacketAccepted() {
      return this.lastPacketAccepted;
   }

   public void setLastPacketAccepted(byte[] lastPacketAccepted) {
      this.lastPacketAccepted = lastPacketAccepted;
   }

   public int getTimeToTransmitAByte() {
      return this.timeToTransmitAByte;
   }

   protected void stop() {
      this.serialPort.removeRs232PortListener(this.omniScannerRs232PortListener);
      this.omniScannerReaderStrategy = null;
      this.omniScannerRs232PortListener = null;
      this.stateManager = null;
   }

   protected void start() {
      this.initCTSRTS();
      this.stateManager = new Rs232IntegratedOmniScannerStateManager(this);
      this.omniScannerReaderStrategy = new Rs232IntegratedOmniScannerProtocol.OmniScannerReaderStrategy();
      this.omniScannerRs232PortListener = new Rs232IntegratedOmniScannerProtocol.OmniScannerRs232PortListener();
      this.serialPort.addRs232PortListener(this.omniScannerRs232PortListener);
      this.serialPort.setReaderStrategy(this.omniScannerReaderStrategy);
   }

   protected Rs232IntegratedScannerState getCurrentState() {
      return this.stateManager.getCurrentState();
   }

   protected void pollDevice() throws HandleException {
      if (this.tracer.isOn()) {
         this.tracer.println(3, "Polling device...");
      }

      this.write(Rs232IntegratedOmniScannerPacketManager.getInstance().createPacket((byte)-57, (byte)1));
   }

   private void initCTSRTS() {
      int attemptCounter = 0;
      if (this.tracer.isOn()) {
         this.tracer.println(3, "Waiting for CTS down... ");
      }

      while (this.serialPort.isCTS() && attemptCounter < 5) {
         this.serialPort.setRTS(false);

         try {
            this.pollForCTS(false);
            if (this.tracer.isOn()) {
               this.tracer.println(3, "CTS is down.");
            }
         } catch (HandleException var3) {
            attemptCounter++;
         }
      }

      if (attemptCounter >= 5 && this.tracer.isOn()) {
         this.tracer.println(3, "CTS is still up!");
      }
   }

   private int calculateTimeToTransmitAByte(Rs232PortCommAdapter pca) {
      float timeTXByte = 10000.0F / (float)pca.getRs232config().getBaudRate();
      int result = (int)timeTXByte + 1;
      if (this.tracer.isOn()) {
         this.tracer.println(3, "Time to transmit a byte = " + result);
      }

      return result;
   }

   private void waitForBytesToBeSent(int quantityOfBytes) {
      try {
         Thread.sleep((long)(quantityOfBytes * this.getTimeToTransmitAByte() + 10));
      } catch (InterruptedException var3) {
      }
   }

   private boolean isCTS() {
      return this.serialPort.isCTS();
   }

   private synchronized void packetArrived(byte[] packet) {
      if (this.tracer.isOn()) {
         this.tracer.println(3, "--> packetArrived");
      }

      this.lastPacketReceived = packet;

      while (this.lastPacketReceived != null) {
         switch (((Rs232IntegratedOmniScannerState)this.getCurrentState()).packetArrived()) {
            case 1:
               if (this.tracer.isOn()) {
                  this.tracer.println(3, "<-- packetArrived");
               }

               return;
            case 2:
               if (this.tracer.isOn()) {
                  this.tracer.println("waiting for state ready to receive packet...");
               }

               try {
                  Thread.sleep(50L);
               } catch (Exception var4) {
               }
               break;
            case 3:
               try {
                  this.tracer.println(3, "Disabling protocol tracer...");
                  this.tracer.setOn(false);
                  Thread.sleep(5000L);
               } catch (Exception var3) {
               }
         }
      }

      if (this.tracer.isOn()) {
         this.tracer.println(3, "Packet discarded by flush.");
         this.tracer.println(3, "<-- packetArrived.");
      }
   }

   private class OmniScannerReaderStrategy implements Rs232PortCommAdapter.DataReaderStrategy, Timerable {
      private byte[] buffer1 = new byte[600];
      private byte[] buffer2 = new byte[600];
      private byte[] currentBuffer = this.buffer1;
      private int bytesAlreadyReceived = 0;
      private InputStream lastInputStream = null;
      private Rs232FiscalPrinterTimer timer = new Rs232FiscalPrinterTimer(this);

      public OmniScannerReaderStrategy() {
         this.timer.setTime(1000);
      }

      private void switchCurrentBuffer() {
         this.currentBuffer = this.getSecondaryBuffer();
      }

      private byte[] getCurrentBuffer() {
         return this.currentBuffer;
      }

      private byte[] getSecondaryBuffer() {
         return this.currentBuffer == this.buffer1 ? this.buffer2 : this.buffer1;
      }

      private void clearBuffer(byte[] buffer) {
         for (int i = 0; i < 257; i++) {
            buffer[i] = 0;
         }
      }

      private void concatenateData(byte[] data) {
         if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
            Rs232IntegratedOmniScannerProtocol.this.tracer.println(3, "--> concatenateData()");
         }

         for (int i = 0; i < data.length; i++) {
            this.getCurrentBuffer()[this.bytesAlreadyReceived + i] = data[i];
         }

         this.bytesAlreadyReceived += data.length;
         if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
            Rs232IntegratedOmniScannerProtocol.this.tracer.println(3, "<-- concatenateData()");
         }
      }

      private byte[] cutPacketFromBeginning(int packetSize) {
         if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
            Rs232IntegratedOmniScannerProtocol.this.tracer.println(3, "--> cutPacketFromBeginning()");
         }

         byte[] packet = new byte[packetSize];
         this.clearBuffer(this.getSecondaryBuffer());
         System.arraycopy(this.getCurrentBuffer(), 0, packet, 0, packetSize);
         System.arraycopy(this.getCurrentBuffer(), packetSize, this.getSecondaryBuffer(), 0, this.bytesAlreadyReceived - packetSize);
         if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
            Rs232IntegratedOmniScannerProtocol.this.tracer.println(3, "Cutted packet from the beginning of buffer: " + Util.toFormatedHexString(packet));
         }

         this.bytesAlreadyReceived -= packetSize;
         if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
            Rs232IntegratedOmniScannerProtocol.this.tracer
               .println(3, "Current buffer: " + Util.toFormatedHexString(this.getSecondaryBuffer(), 0, this.bytesAlreadyReceived));
         }

         this.switchCurrentBuffer();
         if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
            Rs232IntegratedOmniScannerProtocol.this.tracer.println(3, "<-- cutPacketFromBeginning()");
         }

         return packet;
      }

      private void processArrivedData(byte[] data, Rs232Port.EventHelper helper) {
         if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
            Rs232IntegratedOmniScannerProtocol.this.tracer.println(3, "--> processArrivedData()");
         }

         this.timer.stop();
         this.timer.start();
         this.concatenateData(data);
         int potentialPacketSize = Util.unsignedInt(this.getCurrentBuffer()[0]) + 2;
         if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
            Rs232IntegratedOmniScannerProtocol.this.tracer.println(3, "Potential packet size: " + potentialPacketSize);
         }

         if (potentialPacketSize <= this.bytesAlreadyReceived) {
            if (Rs232IntegratedOmniScannerPacketManager.getInstance().isCompletePacket(this.getCurrentBuffer(), 0, potentialPacketSize)) {
               this.timer.stop();
               helper.fireDataEvent(new Rs232DataEvent(this, this.cutPacketFromBeginning(potentialPacketSize)));
               if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
                  Rs232IntegratedOmniScannerProtocol.this.tracer.println(3, "Rs232DataEvent fired.");
               }
            } else {
               this.resetBuffer();
            }
         }

         if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
            Rs232IntegratedOmniScannerProtocol.this.tracer.println(3, "<-- processArrivedData()");
         }
      }

      public void resetBuffer() {
         this.bytesAlreadyReceived = 0;
         this.clearBuffer(this.getCurrentBuffer());
         if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
            Rs232IntegratedOmniScannerProtocol.this.tracer.println(3, "Buffer resetted.");
         }
      }

      public InputStream getInputStream() {
         return this.lastInputStream;
      }

      public void dataAvailable(InputStream is, Rs232Port.EventHelper helper) throws IOException {
         try {
            this.lastInputStream = is;

            for (int bytesAvailable = is.available(); bytesAvailable > 0; bytesAvailable = is.available()) {
               byte[] data = new byte[bytesAvailable];

               for (int i = 0; i < data.length; i++) {
                  data[i] = (byte)is.read();
               }

               if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
                  Rs232IntegratedOmniScannerProtocol.this.tracer.println("Data available [" + data.length + "]" + Util.toFormatedHexString(data));
               }

               this.processArrivedData(data, helper);
            }
         } catch (IOException var6) {
            if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
               Rs232IntegratedOmniScannerProtocol.this.tracer.println("Error Occurred while getting data :");
            }

            Rs232IntegratedOmniScannerProtocol.this.tracer.println(var6);
         }
      }

      public void timerExpired() {
         if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
            Rs232IntegratedOmniScannerProtocol.this.tracer.println(3, "Timeout while receiving packet");
         }

         this.resetBuffer();
      }
   }

   private class OmniScannerRs232PortListener extends DefaultRs232PortListener {
      private OmniScannerRs232PortListener() {
      }

      public void dataEventOccurred(Rs232DataEvent evt) {
         if (Rs232IntegratedOmniScannerProtocol.this.tracer.isOn()) {
            Rs232IntegratedOmniScannerProtocol.this.tracer.println(3, "Rs232DataEvent Occurred!");
         }

         Rs232IntegratedOmniScannerProtocol.this.packetArrived(evt.getData());
         if (Rs232IntegratedOmniScannerProtocol.this.isRecoveringEnabled() && Rs232IntegratedOmniScannerProtocol.this.isRecovering()) {
            Rs232IntegratedOmniScannerProtocol.this.flush();
            Rs232IntegratedOmniScannerProtocol.this.stopRecovering();
         }
      }
   }
}
