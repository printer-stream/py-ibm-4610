package com.ibm.posj.bus.rs232;

import com.ibm.jutil.Timerable;
import com.ibm.jutil.Util;
import com.ibm.posj.HandleException;
import com.ibm.posj.bus.rs232.javaxcomm.Rs232PortCommAdapter;
import com.ibm.rs232.Rs232Port;
import java.io.IOException;
import java.io.InputStream;

public class Rs232IntegratedLineScannerProtocol extends Rs232IntegratedScannerProtocol implements Rs232PortCommAdapter.DataReaderStrategy {
   public static final byte STX = 2;
   public static final byte ETX = 3;
   public static final byte DLE = 16;
   public static final byte SETUP_READ = 64;
   public static final byte SETUP_WRITE = 65;
   public static final byte CONTROL_COMMAND = 66;
   public static final byte STATUS_READ = 67;
   public static final byte SETUP_PERMISSION_READ = 68;
   public static final byte SETUP_PERMISSION_WRITE = 69;
   public static final byte SETUP_REPLY = 80;
   public static final byte RESULT = 81;
   public static final byte STATUS_REPLY = 83;
   public static final byte SETUP_PERMISSION_REPLY = 84;
   public static final byte BARCODE_DATA = 96;
   public static final byte EVENT_NOTIFICATION = 97;
   public static final byte SETUP_BARCODE_DATA = 98;
   public static final byte ACK = 6;
   public static final byte NAK = 21;
   public static final byte BUSY = 27;
   public static final byte RESEND = 5;
   public static final byte AUTOSYNC = 22;
   public static final byte NO_RTS_CTS = 0;
   public static final byte AUTOSYNCD = 12;
   public static final byte UNKNOWN_SCANNER_FRAME_NUMBER = -1;
   public static final int MINIMUM_HIGH_LEVEL_FRAME_LENGTH = 5;
   public static final int MAX_LOW_LEVEL_FRAME_LENGTH = 128;
   public static final int MARGIN_HIGH_TO_LOW_LENGTH = 40;
   public static final int MAX_ATTEMPTS = 5;
   public static final int MAX_SYNC_ATTEMPTS = 10;
   public static final int TIME_BETWEEN_BUSY_RETRIES = 400;
   public static final int LOW_LEVEL_FRAME_TIMEOUT = 1000;
   public static final int HIGH_LEVEL_FRAME_TIMEOUT = 5000;
   private Rs232IntegratedLineScannerStateManager stateManager = null;
   private Rs232FiscalPrinterTimer timer = null;
   private int attemptsCounter = 1;
   private byte[] rxBuffer = new byte[1024];
   private byte[] lastHighLevelFrame;
   private int rxPosition = 0;
   private byte hostFrameNumber = 0;
   private byte scannerFrameNumber = -1;
   private byte multiFrame = 3;
   private byte error = 0;
   private byte restart = 1;
   private final byte sequenceNumber = 0;
   private boolean frameInProgress = false;
   private boolean dleInProgress = false;

   public Rs232IntegratedLineScannerProtocol(
      Rs232IntegratedScannerProtocolListener protocolListener, Rs232PortCommAdapter serialPort, int devicePollTimeInSeconds
   ) {
      super(protocolListener, devicePollTimeInSeconds);
      this.timer = new Rs232FiscalPrinterTimer(null);
      serialPort.setReaderStrategy(this);
      serialPort.setDTR(false);
      this.stateManager = new Rs232IntegratedLineScannerStateManager(this);
   }

   protected void insideDataAvailable(InputStream is, Rs232Port.EventHelper arg1) throws IOException {
      int bytesAvailable = 0;
      int newData = 0;
      bytesAvailable = is.available();
      if (bytesAvailable <= 0) {
         if (this.tracer.isOn()) {
            this.tracer.println("call to dataAvailable with 0 bytes available");
         }
      } else if (this.stateManager.getCurrentState() == null) {
         if (this.tracer.isOn()) {
            this.tracer.println("state == null inside dataAvailable");
         }

         this.flushInputStream(is);
      } else {
         for (int i = 1; i <= bytesAvailable; i++) {
            try {
               newData = is.read();
               if (newData == -1) {
                  break;
               }

               if (newData == 2) {
                  this.rxPosition = 0;
                  this.frameInProgress = true;
                  this.dleInProgress = false;
               } else if (this.frameInProgress) {
                  if (this.dleInProgress) {
                     newData &= 191;
                     this.dleInProgress = false;
                     this.rxBuffer[this.rxPosition] = (byte)(newData & 0xFF);
                     this.rxPosition++;
                  } else if (newData == 16) {
                     this.dleInProgress = true;
                  } else {
                     if (newData == 3) {
                        this.frameInProgress = false;
                        if (i < bytesAvailable) {
                           this.flushInputStream(is);
                        }
                        break;
                     }

                     this.rxBuffer[this.rxPosition] = (byte)(newData & 0xFF);
                     this.rxPosition++;
                  }
               }
            } catch (Exception var7) {
               this.flushInputStream(is);
               this.tracer.print(var7);
               return;
            }
         }

         if (newData != 3) {
            if (this.tracer.isOn()) {
               this.tracer
                  .println("Partial frame received on (" + System.currentTimeMillis() + ") : " + Util.toFormatedHexString(this.rxBuffer, 0, this.rxPosition));
            }
         } else if (this.rxPosition < 5) {
            byte[] lowLevelFrame = new byte[this.rxPosition];
            System.arraycopy(this.rxBuffer, 0, lowLevelFrame, 0, this.rxPosition);
            if (this.tracer.isOn()) {
               this.tracer.println("Low level frame received (" + System.currentTimeMillis() + ") : " + Util.toFormatedHexString(lowLevelFrame));
            }

            this.stateManager.getCurrentState().receiveLowLevelFrame(lowLevelFrame);
         } else {
            byte[] highLevelFrame = new byte[this.rxPosition - 2];
            System.arraycopy(this.rxBuffer, 0, highLevelFrame, 0, this.rxPosition - 2);
            if (!Rs232IntegratedLineScannerPacketManager.checkCheckSum(
               highLevelFrame, ((this.rxBuffer[this.rxPosition - 2] & 255) << 8) + (this.rxBuffer[this.rxPosition - 1] & 255)
            )) {
               if (this.tracer.isOn()) {
                  this.tracer.println("HLF received with wrong checksum: " + Util.toFormatedHexString(highLevelFrame));
               }
            } else {
               if (this.tracer.isOn()) {
                  this.tracer.println("High level frame received on (" + System.currentTimeMillis() + ") : " + Util.toFormatedHexString(highLevelFrame));
               }

               this.stateManager.getCurrentState().receiveHighLevelFrame(highLevelFrame);
               if (this.isRecoveringEnabled() && this.isRecovering()) {
                  this.stopRecovering();
               }
            }
         }
      }
   }

   protected void restartTimer(int time, Timerable t) {
      this.timer.setTime(time);
      this.timer.setTimerable(t);
      this.timer.start();
      if (this.tracer.isOn()) {
         this.tracer.println("-->restartTimer " + time + " -- " + System.currentTimeMillis() + " restartTimer<--");
      }
   }

   protected void stopTimer() {
      this.timer.stop();
      this.timer.setTimerable(null);
      if (this.tracer.isOn()) {
         this.tracer.println("-->stopTimer" + System.currentTimeMillis() + "stopTimer<--");
      }
   }

   protected void finalize() {
      this.stopTimer();
   }

   protected Rs232IntegratedScannerState getCurrentState() {
      return this.stateManager.getCurrentState();
   }

   protected void pollDevice() {
      if (this.tracer.isOn()) {
         this.tracer.println(3, "Polling device...");
      }

      byte[] temp = new byte[Rs232IntegratedLineScannerInfoHelper.CHECK_UPCA_ENABLED.length + 1];
      temp[0] = 64;
      System.arraycopy(Rs232IntegratedLineScannerInfoHelper.CHECK_UPCA_ENABLED, 0, temp, 1, Rs232IntegratedLineScannerInfoHelper.CHECK_UPCA_ENABLED.length);

      try {
         this.write(Rs232IntegratedLineScannerPacketManager.createHighLevelFrame(this.getSequenceNumber(), temp, this.getFrameManagement()));
      } catch (HandleException var3) {
      }
   }

   protected void stop() {
      this.stateManager = null;
   }

   protected void start() {
      this.stateManager = new Rs232IntegratedLineScannerStateManager(this);
   }

   public void dataAvailable(InputStream is, Rs232Port.EventHelper arg1) throws IOException {
      if (this.tracer.isOn()) {
         this.tracer.println("-->dataAvailable");
      }

      this.insideDataAvailable(is, arg1);
      if (this.tracer.isOn()) {
         this.tracer.println("<--dataAvailable");
      }
   }

   public Rs232IntegratedLineScannerStateManager getStateManager() {
      return this.stateManager;
   }

   public int getAttemptsCounter() {
      return this.attemptsCounter;
   }

   public void setAttemptsCounter(int i) {
      this.attemptsCounter = i;
   }

   public void fireIPLEndEvent() {
      if (this.tracer.isOn()) {
         this.tracer.println("-->fireIPLEndEvent");
      }

      if (this.tracer.isOn()) {
         this.tracer.println("<--fireIPLEndEvent");
      }
   }

   public void fireDataEvent(byte[] highLevelFrame) {
      byte[] data = new byte[highLevelFrame.length - 2];
      System.arraycopy(highLevelFrame, 1, data, 0, highLevelFrame.length - 2);
      super.fireDataEvent(data);
   }

   public void incrementHostFrameNumber() {
      this.hostFrameNumber = (byte)((this.hostFrameNumber + 1) % 8);
   }

   public byte getHostFrameNumber() {
      return this.hostFrameNumber;
   }

   public byte getScannerFrameNumber() {
      return this.scannerFrameNumber;
   }

   public void incrementScannerFrameNumber() {
      this.scannerFrameNumber = (byte)((this.scannerFrameNumber + 1) % 8);
   }

   public void setScannerFrameNumber(byte sfn) {
      this.scannerFrameNumber = sfn;
   }

   public byte getError() {
      return this.error;
   }

   public byte getRestart() {
      return this.restart;
   }

   public void clearRestart() {
      this.restart = 0;
   }

   public byte getMultiFrame() {
      return this.multiFrame;
   }

   public byte getFrameManagement() {
      return (byte)((this.getMultiFrame() << 5) + (this.getError() << 4) + (this.getRestart() << 3) + this.getHostFrameNumber());
   }

   public byte getSequenceNumber() {
      return 0;
   }

   public byte[] getLastHighLevelFrame() {
      return this.lastHighLevelFrame;
   }

   public void setLastHighLevelFrame(byte[] lastHighLevelFrame) {
      this.lastHighLevelFrame = lastHighLevelFrame;
   }
}
