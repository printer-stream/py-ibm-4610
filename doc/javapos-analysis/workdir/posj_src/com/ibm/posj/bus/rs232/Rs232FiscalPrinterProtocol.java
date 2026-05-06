package com.ibm.posj.bus.rs232;

import com.ibm.jutil.Timerable;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.HandleException;
import com.ibm.posj.bus.rs232.javaxcomm.Rs232PortCommAdapter;
import com.ibm.posj.util.DevCats;
import com.ibm.rs232.Rs232Port;
import java.io.IOException;
import java.io.InputStream;

public class Rs232FiscalPrinterProtocol implements Rs232PortCommAdapter.DataReaderStrategy, Timerable {
   public static final byte PACKET_SNRM = 4;
   public static final byte PACKET_ROL = 5;
   public static final byte PACKET_NSA = 6;
   public static final byte PACKET_RESET = 7;
   public static final byte PACKET_IF0 = 0;
   public static final byte PACKET_IF1 = 1;
   public static final int RC_232_INVALID_LENGTH = 14;
   public static final int RC_232_IN_IPL = 15;
   public static final int RC_232_PLD = 16;
   public static final int RC_232_TIMEOUT = 17;
   public static final int RC_232_BAD_PACKET_READ = 18;
   public static final int RC_232_OK = 19;
   public static final int RC_232_CANNOT_INIT_COM = 20;
   public static final int RC_232_INTERM_STATUS_RECV = 21;
   public static final int RC_232_NOTHING_RECV = 22;
   static final int MINIMUM_PACKET_LENGTH = 5;
   public static final int STATUS_LENGTH = 15;
   public static final int MAX_ATTEMPTS = 10;
   public static final int RX_TIMEOUT = 200;
   public static final int DETECT_TIMEOUT = 300;
   public static final int RESET_TIME = 250;
   public static final int POLL_TIME = 100;
   public static final int POLL_AFTERDATATIME = 45;
   public static final byte[] CMD_FISCALGETVERSION = new byte[]{27, 102, -4, 0};
   public static final byte WRITE_OK = 1;
   public static final byte WRITE_WAIT = 2;
   public static final byte WRITE_ERROR = 3;
   private Rs232FiscalPrinterProtocolListener handleImp = null;
   private Rs232FiscalPrinterState state = null;
   private Rs232FiscalPrinterPacketFactory factory = new Rs232FiscalPrinterPacketFactory();
   private Rs232FiscalPrinterTimer timer = null;
   private int checkJustResetCounter = 0;
   private int attemptsCounter = 0;
   private int currIFPacket = 0;
   private long lastWrite = 0L;
   private byte[] rxBuffer = new byte[1024];
   private int rx_position = 0;
   private boolean waiting_response = false;
   private boolean processingRead = false;
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevCats.FISCALPRINTER_DEVCAT.toString(), "Rs232FiscalPrinterProtocol");

   public Rs232FiscalPrinterProtocol() {
      this.timer = new Rs232FiscalPrinterTimer(null);
      this.timer.setTime(200);
   }

   public void dataAvailable(InputStream is, Rs232Port.EventHelper arg1) throws IOException {
      int bytesAvailable = 0;
      int i = 0;
      int newData = 0;
      bytesAvailable = is.available();
      if (bytesAvailable <= 0) {
         if (this.tracer.isOn()) {
            this.tracer.println("no data available");
         }

         if (this.tracer.isOn()) {
            this.tracer.println("<--dataAvailable");
         }
      } else {
         this.processingRead = true;
         this.stopTimer();
         if (this.tracer.isOn()) {
            this.tracer.println("-->dataAvailable");
         }

         if (!this.isWaiting_response()) {
            if (this.tracer.isOn()) {
               this.tracer.println("not running?");
            }

            this.flushInputStream(is);
            this.restartTimer(200);
            if (this.tracer.isOn()) {
               this.tracer.println("<--dataAvailable");
            }

            this.processingRead = false;
         } else if (this.state == null) {
            if (this.tracer.isOn()) {
               this.tracer.println("not waiting a response?");
            }

            this.flushInputStream(is);
            this.restartTimer(200);
            if (this.tracer.isOn()) {
               this.tracer.println("<--dataAvailable");
            }

            this.processingRead = false;
         } else {
            for (int var11 = 1; var11 <= bytesAvailable; var11++) {
               try {
                  newData = is.read();
                  if (newData == -1) {
                     break;
                  }

                  this.rxBuffer[this.rx_position] = (byte)(newData & 0xFF);
                  this.rx_position++;
               } catch (Exception var9) {
                  this.flushInputStream(is);
                  if (this.tracer.isOn()) {
                     this.tracer.println("min packet length exception raised");
                  }

                  this.tracer.print(var9);
                  this.restartTimer(200);
                  if (this.tracer.isOn()) {
                     this.tracer.println("<--dataAvailable");
                  }

                  this.processingRead = false;
                  return;
               }
            }

            if (this.rx_position < 5) {
               if (this.tracer.isOn()) {
                  this.tracer.println("waiting header");
               }

               this.restartTimer(200);
               if (this.tracer.isOn()) {
                  this.tracer.println("<--dataAvailable");
               }

               this.processingRead = false;
            } else {
               int uiRemainingBytes = Rs232FiscalPrinterPacketFactory.getDataLength(this.rxBuffer);
               if (uiRemainingBytes > this.rxBuffer.length - 5) {
                  this.flushInputStream(is);
                  if (this.tracer.isOn()) {
                     this.tracer.println("buffer overflow");
                  }

                  this.restartTimer(200);
                  if (this.tracer.isOn()) {
                     this.tracer.println("<--dataAvailable");
                  }

                  this.processingRead = false;
               } else if (!Rs232FiscalPrinterPacketFactory.checkHeader(this.rxBuffer)) {
                  this.flushInputStream(is);
                  if (this.tracer.isOn()) {
                     this.tracer.println("wrong header");
                  }

                  this.restartTimer(200);
                  if (this.tracer.isOn()) {
                     this.tracer.println("<--dataAvailable");
                  }

                  this.processingRead = false;
               } else if (this.rx_position < uiRemainingBytes + 5) {
                  if (this.tracer.isOn()) {
                     this.tracer.println("waiting data");
                  }

                  this.restartTimer(200);
                  if (this.tracer.isOn()) {
                     this.tracer.println("<--dataAvailable");
                  }

                  this.processingRead = false;
               } else if (!Rs232FiscalPrinterPacketFactory.checkCRC(this.rxBuffer)) {
                  if (this.tracer.isOn()) {
                     this.tracer.println("wrong CRC");
                  }

                  this.flushInputStream(is);
                  this.restartTimer(200);
                  if (this.tracer.isOn()) {
                     this.tracer.println("<--dataAvailable");
                  }

                  this.processingRead = false;
               } else {
                  if (this.tracer.isOn()) {
                     this.tracer.println("CRC OK, packet received");
                  }

                  this.setWaiting_response(false);
                  if (this.handleImp.isDirectIOMode() && Rs232FiscalPrinterPacketFactory.getDataLength(this.rxBuffer) >= 15) {
                     int datalen = Rs232FiscalPrinterPacketFactory.getDataLength(this.rxBuffer);
                     byte[] data = new byte[datalen];
                     System.arraycopy(this.rxBuffer, 3, data, 0, datalen);
                     this.handleImp.fireDirectIOEvent(data);
                  }

                  this.state.receivePacket(this.rxBuffer, this.rx_position);
                  this.initRxBuffer();
                  if (this.tracer.isOn()) {
                     this.tracer.println("<--dataAvailable");
                  }

                  this.processingRead = false;
               }
            }
         }
      }
   }

   public void flushInputStream(InputStream is) {
      try {
         int val = 0;

         while (is.available() > 0) {
            val = is.read();
         }

         this.initRxBuffer();
      } catch (IOException var3) {
         this.tracer.print(var3);
      }
   }

   public void initRxBuffer() {
      this.rx_position = 0;
      this.rxBuffer[0] = 0;
      this.rxBuffer[1] = 0;
      this.rxBuffer[2] = 0;
      this.rxBuffer[3] = 0;
   }

   public void write(byte[] buffer, int length) throws HandleException {
      if (this.tracer.isOn()) {
         this.tracer.println("-->write");
      }

      while (true) {
         switch (this.state.write(buffer, length)) {
            case 1:
               if (this.tracer.isOn()) {
                  this.tracer.println("write ok");
                  this.tracer.println("<--write");
               }

               return;
            case 2:
               if (this.tracer.isOn()) {
                  this.tracer.println("waiting...");
               }

               try {
                  Thread.sleep(50L);
               } catch (Exception var4) {
               }
               break;
            case 3:
               if (this.tracer.isOn()) {
                  this.tracer.println("write exception");
                  this.tracer.println("<--write");
               }

               throw new HandleException("wrong state");
         }
      }
   }

   public void submitToAdapter(byte[] data) {
      if (this.tracer.isOn()) {
         this.tracer.println("-->submitToAdapter");
      }

      this.handleImp.writeToPort(data, data.length);
      if (this.tracer.isOn()) {
         this.tracer.println("Data Length:" + data.length);
         this.tracer.println("<--submiteToAdapter");
      }
   }

   public Rs232FiscalPrinterPacketFactory getFactory() {
      return this.factory;
   }

   public void changeState(Rs232FiscalPrinterState state) {
      this.state = state;
   }

   public Rs232FiscalPrinterState getState() {
      return this.state;
   }

   public int getAttemptsCounter() {
      return this.attemptsCounter;
   }

   public void setAttemptsCounter(int i) {
      this.attemptsCounter = i;
   }

   public void changeCurrIFPacket() {
      this.currIFPacket = this.currIFPacket == 0 ? 1 : 0;
   }

   public int getCurrIFPacket() {
      return this.currIFPacket;
   }

   public void setCurrIFPacket(int i) {
      this.currIFPacket = i;
   }

   public void fireIPLEndEvent(byte[] packet, int length) {
      if (this.tracer.isOn()) {
         this.tracer.println("-->fireIPLEndEvent");
      }

      byte[] data = Rs232FiscalPrinterPacketFactory.getData(packet);
      Rs232FiscalPrinterInfoHelper.getInstance().setFiscalInformation(data);
      this.handleImp.fireStatusEvent(2);
      if (this.tracer.isOn()) {
         this.tracer.println("<--fireIPLEndEvent");
      }
   }

   public void fireDataEvent(byte[] packet, int length) {
      if (this.tracer.isOn()) {
         this.tracer.println("-->fireDataEvent");
      }

      if (length < 5) {
         if (this.tracer.isOn()) {
            this.tracer.println("packet length error");
            this.tracer.println("<--fireDataEvent");
         }
      } else {
         byte[] data = Rs232FiscalPrinterPacketFactory.getData(packet);
         this.handleImp.fireDataEvent(data);
         if (this.tracer.isOn()) {
            this.tracer.println("data length:" + data.length);
            this.tracer.println("<--fireDataEvent");
         }
      }
   }

   public void fireErrorEvent(int type) {
      if (this.tracer.isOn()) {
         this.tracer.println("-->fireErrorEvent");
      }

      this.handleImp.fireErrorEvent(type);
      if (this.tracer.isOn()) {
         this.tracer.println("<--fireErrorEvent");
      }
   }

   public void setHandleImp(Rs232FiscalPrinterProtocolListener connection) {
      if (connection != null) {
         this.handleImp = connection;
         this.changeState(new StateReset(this));
      } else {
         this.stopTimer();
         this.timer.pleaseEnd();
      }
   }

   public void resetPrinter() {
      this.stopTimer();
      this.changeState(new StateReset(this));
   }

   public void detectFiscalPrinter(Rs232FiscalPrinterProtocolListener connection) {
      this.handleImp = connection;
      this.changeState(new StateDetect(this));
   }

   public void timerExpired() {
      if (this.timer != null) {
         if (this.tracer.isOn()) {
            this.tracer.println("-->timerExpired " + System.currentTimeMillis());
         }

         if (System.currentTimeMillis() - this.lastWrite >= this.timer.getTime() && !this.processingRead) {
            this.state.timerExpired();
         } else {
            if (this.tracer.isOn()) {
               this.tracer.println("-->timer rejected " + System.currentTimeMillis());
            }

            this.restartTimer((int)this.timer.getTime());
         }

         if (this.tracer.isOn()) {
            this.tracer.println("<--timerExpired");
         }
      }
   }

   public void restartTimer(int time) {
      this.setLastWrite();
      this.timer.setTime(time);
      this.timer.setTimerable(this);
      this.timer.start();
      if (this.tracer.isOn()) {
         this.tracer.println("-->restartTimer " + time + " -- " + System.currentTimeMillis() + " restartTimer<--");
      }
   }

   public void stopTimer() {
      this.timer.stop();
      this.timer.setTimerable(null);
      if (this.tracer.isOn()) {
         this.tracer.println("-->stopTimer" + System.currentTimeMillis() + "stopTimer<--");
      }
   }

   public void setLastWrite() {
      this.lastWrite = System.currentTimeMillis();
   }

   protected void finalize() throws Throwable {
      super.finalize();
      this.stopTimer();
      this.timer = null;
   }

   public boolean isWaiting_response() {
      return this.waiting_response;
   }

   public void setWaiting_response(boolean b) {
      this.waiting_response = b;
   }
}
