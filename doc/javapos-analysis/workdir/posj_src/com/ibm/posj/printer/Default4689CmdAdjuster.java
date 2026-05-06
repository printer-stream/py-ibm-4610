package com.ibm.posj.printer;

import com.ibm.jutil.ByteBuffer;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.IBM4689PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.ibm4689.Cmd4689;

public class Default4689CmdAdjuster implements CmdAdjuster {
   private int appendSize = 0;
   private int accum = 0;
   private int maxSize = 0;
   private int head = 0;
   DefaultPOSPrinterCmd.Factory factory = null;
   private PrinterWriter format = null;
   private int MAX_4689_RS485_BUS = 1400;
   private boolean isUsb = false;
   private boolean rotate = false;
   private boolean journal = false;
   private int i;
   private int rest;
   private int position;
   private int lastPosition = 0;
   private Cmd4689 cmd = new Cmd4689();
   private static byte[] PRINT_REC_ROTATE_CMD = new byte[]{1, 0, 1, 0, 0, 0, 1, 27, 96, 27, 82};
   private static byte[] PRINT_REC_NORMAL_CMD = new byte[]{1, 0, 1, 0, 0, 0, 1, 27, 96, 27, 64, 0, 27, 83};
   private static byte[] PRINT_JRN_ROTATE_CMD = new byte[]{1, 0, 2, 0, 0, 0, 1, 27, 96, 27, 82};
   private static byte[] PRINT_JRN_NORMAL_CMD = new byte[]{1, 0, 2, 0, 0, 0, 1, 27, 96, 27, 64, 0, 27, 83};

   public Default4689CmdAdjuster(DefaultPOSPrinterCmd.Factory factory, int maxBusSize, PrinterWriter pw) {
      this.format = pw;
      this.factory = factory;
      ByteBuffer b = ByteBuffer.getByteBufferFactory().createByteBuffer();
      if (maxBusSize == this.MAX_4689_RS485_BUS) {
         this.appendSize = 0;
         this.isUsb = false;
      } else {
         this.appendSize = 1;
         this.isUsb = true;
      }

      this.maxSize = maxBusSize - this.appendSize;
   }

   public POSPrinterCmd adjustPOSPrinterCmd(DefaultPOSPrinterCmd adjust) {
      PrintCmd cmd = adjust.getPrintCmd();
      if (this.maxSize < cmd.getDataSize()) {
         ByteBuffer b = this.format.getFormattedBuffer();
         this.head = b.getByteCount();
         adjust.setPreWrittenBuffer(b);
         adjust.setOutboundPacket(b);
         adjust.generatePrintCmds();
         this.simpleSplit(adjust);
      }

      return adjust;
   }

   protected void simpleSplit(DefaultPOSPrinterCmd masterCmd) {
      PrinterWriter.getTracer().println("Splitting ->" + masterCmd);
      ByteBuffer large = masterCmd.getOutboundPacket();
      int largeLen = masterCmd.getPrintCmd().getDataSize();
      this.accum = 0;
      boolean flag = false;
      DefaultPOSPrinterCmd mainContCmd = null;
      this.setRotateJrnCmd(large.getBytes());

      for (this.rest = largeLen; this.accum < largeLen; flag = true) {
         try {
            if (null == mainContCmd) {
               mainContCmd = this.createContinuation4689Cmd(masterCmd.getStation(), large, flag, largeLen);
               masterCmd.appendSplitMembers(mainContCmd);
            } else {
               mainContCmd.appendPOSPrinterCmd(this.createContinuation4689Cmd(masterCmd.getStation(), large, flag, largeLen));
            }
         } catch (Exception var7) {
            var7.printStackTrace();
         }
      }

      this.cleanRotateJrnCmd();
      masterCmd.getPrintCmd().getByteBuffer().setByteCount(this.maxSize, true);
   }

   private DefaultPOSPrinterCmd createContinuation4689Cmd(byte station, ByteBuffer data, boolean escNeeded, int len) {
      ByteBuffer lastCmd = new ByteBuffer();
      int numberCmds = len / this.format.getMaxCmdLen();
      boolean lastCmdToSend = false;
      if (this.i < numberCmds) {
         this.position = this.separateData(data.getBytes(), this.i);
      } else {
         lastCmdToSend = true;
      }

      if (escNeeded) {
         lastCmd.clean();
         if (this.rotate) {
            if (this.journal) {
               lastCmd.append(PRINT_JRN_ROTATE_CMD);
            } else {
               lastCmd.append(PRINT_REC_ROTATE_CMD);
            }
         } else if (this.journal) {
            lastCmd.append(PRINT_JRN_NORMAL_CMD);
         } else {
            lastCmd.append(PRINT_REC_NORMAL_CMD);
         }

         if (!lastCmdToSend) {
            if (this.rotate) {
               lastCmd.append(data.getBytes(), this.lastPosition + 1, this.position - this.lastPosition);
            } else {
               lastCmd.append(data.getBytes(), this.lastPosition + 7, this.position - this.lastPosition - 6);
            }

            lastCmd.append(this.cmd.END_OF_DATA);
         } else if (this.rotate) {
            if (this.isUsb) {
               lastCmd.append(data.getBytes(), this.lastPosition + 1, this.rest - 8);
            } else {
               lastCmd.append(data.getBytes(), this.lastPosition + 1, this.rest - 7);
            }
         } else if (this.isUsb) {
            lastCmd.append(data.getBytes(), this.lastPosition + 7, this.rest - 8);
         } else {
            lastCmd.append(data.getBytes(), this.lastPosition + 7, this.rest - 7);
         }
      } else {
         lastCmd.clean();
         if (this.isUsb) {
            lastCmd.append(data.getBytes(), 1, this.position + 1);
         } else {
            lastCmd.append(data.getBytes(), 0, this.position + 1);
         }

         lastCmd.append(this.cmd.END_OF_DATA);
      }

      if (this.position - this.lastPosition > 0) {
         this.rest = this.rest - (this.position - this.lastPosition);
      } else {
         this.rest = 0;
      }

      this.lastPosition = this.position;
      POSPrinterCmd cont = ((IBM4689PrinterCmd.Factory)this.factory).createContinuation4689Cmd(station, null, lastCmd.getBytesRef());
      this.accum = len - this.rest;
      this.i++;
      PrinterWriter.getTracer().println("splitAccum-->" + this.accum + "  i-->" + this.i + "   rest-->" + this.rest);
      return (DefaultPOSPrinterCmd)cont;
   }

   private void setRotateJrnCmd(byte[] data) {
      if (this.isUsb) {
         if (data.length >= 11 && data[11] == 82) {
            this.rotate = true;
         }

         if (data[3] == 2) {
            this.journal = true;
         }
      } else {
         if (data.length >= 10 && data[10] == 82) {
            this.rotate = true;
         }

         if (data[2] == 2) {
            this.journal = true;
         }
      }
   }

   private void cleanRotateJrnCmd() {
      this.rotate = false;
      this.journal = false;
      this.i = this.position = this.lastPosition = this.rest = 0;
   }

   private int separateData(byte[] array, int i) {
      int initPosition = this.maxSize * (i + 1);
      byte[] patron = new byte[]{27, 64, 0, 27, 65, 0};
      boolean flag = true;
      int x = 0;

      while (flag) {
         int z = 5;
         if (array[initPosition] == patron[z]) {
            for (x = initPosition; x > initPosition - patron.length; x--) {
               if (array[x] == patron[z]) {
                  flag = false;
                  z--;
               } else {
                  initPosition--;
                  flag = true;
               }
            }
         } else {
            initPosition--;
         }

         if (initPosition == 0) {
            x = -1;
            flag = false;
         }
      }

      return x;
   }
}
