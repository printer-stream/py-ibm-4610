package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.printer.cmds.ibm4689.Cmd4689;
import com.ibm.posj.flash.FlashException;
import com.ibm.posj.flash.FlashFile;
import com.ibm.posj.flash.FlashFormat;
import com.ibm.posj.flash.FlashRecord;
import com.ibm.posj.flash.FlashRequest;
import com.ibm.posj.printer.event.IBM4689Status;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.DevCats;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class Rs485Flash4689PrtHandleImp extends Rs485FlashPrinterHandleImp {
   Cmd4689 cmds = new Cmd4689();
   private Handle printerHandle;

   public Rs485Flash4689PrtHandleImp(HandleKey key, SioDevice device) {
      super(key, device);
      this.setTracer(TracerFactory.getInstance().createTracer("FLASH", "Rs485Flash4689PrtHandleImp"));
   }

   public void flash(FlashRequest flashRequest) throws FlashException {
      try {
         this.lockSubDevices();
         this.getTracer().println(".flash() - flashfilename:" + flashRequest.getFlashFile().getFilename());
         this.getTracer().println(".flash() - deviceECLevel(): " + this.getECLevel());
         flashRequest.setDeviceECLevel(this.getECLevel());
         this.getTracer().println(".flash() - preparing to flash " + this.getDevCat().toString());
         this.prep();
         this.downloadFirmware(flashRequest.getFlashFile());
         this.finish();
      } catch (FlashException var6) {
         throw var6;
      } finally {
         this.unlockSubDevices();
      }
   }

   public void getPrinterInfo(Handle handle, FlashFile file) throws FlashException {
      if (handle.getDevCat().toString().equals(DevCats.POSPRINTER_DEVCAT.toString())) {
         ;
      }
   }

   public void flashPOSPrinter(Handle handle, Vector flashRequests, ArrayList flashFileList) throws FlashException {
      this.getTracer().println("-->Rs485Flash4689Prt");
      this.initPrinter(handle, flashRequests, flashFileList);
      this.getTracer().println("<--Rs485Flash4689Prt");
   }

   protected void handleStatus(PrintStatus event) {
      this.getTracer().println("4689 statusEventOccurred() ->Got Flash Data BACK!!!");
      IBM4689Status pStatus = (IBM4689Status)event;
      this.cmdCompleted.set(true);
   }

   protected void initPrinter(Handle handle, Vector flashRequests, ArrayList rs485FlashFileList) throws FlashException {
      this.getTracer().println("initPrinter");
      this.printerHandle = handle;
      this.initPrinterRequests(handle, flashRequests);
      this.getTracer().println("buildRs485FlashRequest() getPrinterInfo");
      FlashFile file = this.findFlash(rs485FlashFileList);
      if (null != file) {
         this.setRs485PrinterFlashRequest(new FlashRequest(handle, file));
         this.getTracer().println("flash() - printer flash file: " + file);

         try {
            handle.init();
         } catch (Exception var6) {
            this.getTracer().print(var6);
            throw new FlashException("init failed", var6);
         }

         handle.flash(this.getRs485PrinterFlashRequest());
      }
   }

   private void prep() throws FlashException {
      this.formatDwnLdCmd(this.printerHandle, this.cmds.ERASE_FIRMWARE);
   }

   private void downloadFirmware(FlashFile file) throws FlashException {
      FlashFormat format = file.getFlashFormat();
      file.load();
      FlashFormat ff = file.getFlashFormat();
      dlSequence = 0;

      for (int i = 0; i < ff.size(); i++) {
         FlashRecord fr = ff.get(i);
         byte[] flashRecordData = fr.getRecordData();
         this.getTracer().println("executeFlash -> flashRecordData length = " + flashRecordData.length + " seq: " + dlSequence++);
         this.cmdCompleted.set(false);
         this.formatDwnLdCmd(this.printerHandle, flashRecordData);
      }
   }

   private void finish() throws FlashException {
      ByteBuffer doReset = ByteBuffer.getByteBufferFactory().createByteBuffer(this.cmds.RESET.length + 1);
      doReset.append(0);
      this.formatDwnLdCmd(this.printerHandle, doReset.getBytesRef());
      doReset.recycle();
   }

   private FlashFile findFlash(ArrayList fileList) throws FlashException {
      Iterator it = fileList.iterator();

      FlashFile ret;
      for (ret = null; it.hasNext(); ret = null) {
         ret = (FlashFile)it.next();
         if (ret.getFilename().endsWith("aip4537.dat")) {
            break;
         }
      }

      return ret;
   }
}
