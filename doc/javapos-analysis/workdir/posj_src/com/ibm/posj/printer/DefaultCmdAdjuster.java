package com.ibm.posj.printer;

import com.ibm.jutil.ByteBuffer;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmd;

public class DefaultCmdAdjuster implements CmdAdjuster {
   private int appendSize = 0;
   private int accum = 0;
   private int maxSize = 0;
   private int head = 0;
   DefaultPOSPrinterCmd.Factory factory = null;
   private PrinterWriter format = null;

   public DefaultCmdAdjuster(DefaultPOSPrinterCmd.Factory factory, int maxBusSize, PrinterWriter pw) {
      this.format = pw;
      this.factory = factory;
      byte[] test = new byte[]{1};
      ByteBuffer b = ByteBuffer.getByteBufferFactory().createByteBuffer();
      DefaultPOSPrinterCmd cont = (DefaultPOSPrinterCmd)((IBM4610PrinterCmd.Factory)factory).createContinuationCmd((byte)0, b, test, true, 0, 2);
      this.appendSize = cont.getPrintCmd().getDataSize() - 1;
      this.maxSize = maxBusSize - this.appendSize;
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println(">>DefaultCmdAdjuster maxBusSize-->" + maxBusSize + " maxSize-->" + this.maxSize);
      }

      cont.recycle();
   }

   public POSPrinterCmd adjustPOSPrinterCmd(DefaultPOSPrinterCmd adjust) {
      PrintCmd cmd = adjust.getPrintCmd();
      if (this.maxSize < cmd.getDataSize()) {
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println(">>adjustPOSPrinterCmd cmd.getDataSize()-->" + cmd.getDataSize());
         }

         ByteBuffer b = this.format.getFormattedBuffer();
         this.head = b.getByteCount();
         adjust.setPreWrittenBuffer(b);
         adjust.setOutboundPacket(b);
         adjust.generatePrintCmds();
         this.simpleSplit(adjust);
      }

      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println(">>cmdAdjusted-->" + adjust + " #cmdsInside-->" + adjust.getAppendCnt());
         PrinterWriter.getTracer().println(">>cmdAdjusted.getDataSize()-->" + adjust.getPrintCmd().getDataSize());
      }

      return adjust;
   }

   protected void simpleSplit(DefaultPOSPrinterCmd masterCmd) {
      PrintCmd cont = null;
      ByteBuffer large = masterCmd.getOutboundPacket();
      int largeLen = masterCmd.getPrintCmd().getDataSize();
      this.accum = this.head;
      boolean flag = false;

      for (DefaultPOSPrinterCmd mainContCmd = null; this.accum < largeLen; flag = true) {
         try {
            if (null == mainContCmd) {
               mainContCmd = this.createContinuationCmd(masterCmd.getStation(), large, !flag ? false : masterCmd.isContinueNeeded(), largeLen);
               masterCmd.appendSplitMembers(mainContCmd);
            } else {
               mainContCmd.appendPOSPrinterCmd(
                  this.createContinuationCmd(masterCmd.getStation(), large, !flag ? false : masterCmd.isContinueNeeded(), largeLen)
               );
            }
         } catch (Exception var8) {
            PrinterWriter.getTracer().print(var8);
         }
      }

      masterCmd.getPrintCmd().getByteBuffer().setByteCount(this.maxSize, true);
   }

   private DefaultPOSPrinterCmd createContinuationCmd(byte station, ByteBuffer data, boolean escNeeded, int length) {
      int tMaxSize = this.maxSize - (escNeeded ? this.appendSize : 0);
      int tdata;
      if (this.accum + tMaxSize <= length) {
         tdata = tMaxSize;
      } else {
         tdata = length - this.accum;
      }

      POSPrinterCmd cont = ((IBM4610PrinterCmd.Factory)this.factory).createContinuationCmd(station, null, data.getBytesRef(), escNeeded, this.accum, tdata);
      this.accum += tdata;
      return (DefaultPOSPrinterCmd)cont;
   }
}
