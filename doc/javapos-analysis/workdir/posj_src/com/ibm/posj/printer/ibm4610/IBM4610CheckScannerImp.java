package com.ibm.posj.printer.ibm4610;

import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Util;
import com.ibm.posj.AbstractHandleCmd;
import com.ibm.posj.CheckScannerCmd;
import com.ibm.posj.CheckScannerHandle;
import com.ibm.posj.CheckScannerHandleCmdFilterV;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.SystemCmdVisitor;
import com.ibm.posj.bus.IBMPrinterComposite;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.event.CheckScannerDataEvent;
import com.ibm.posj.event.CheckScannerStatusEvent;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.printer.IBMPrinterCheckScannerLinker;
import com.ibm.posj.printer.IBMPrinterState;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintErrorEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.PosjUtil;

public class IBM4610CheckScannerImp extends AbstractSubDeviceImp implements IBMPrinterCheckScannerLinker {
   private IBM4610CheckScannerImp.CheckScannerCmdVisitor visitor = new IBM4610CheckScannerImp.CheckScannerCmdVisitor(this);
   private CheckScannerHandleCmdFilterV handleCmdFilterV = new CheckScannerHandleCmdFilterV();
   private boolean isOutOfPaper = false;
   private boolean isHeaderRequest = false;
   private boolean isInitImageMemory = false;
   private boolean isCmdRejected = false;
   private Object initImageMemoryLock = new Object();
   private Object waitNextImgLocationLock = new Object();
   private Object scannerCalibCompleteLock = new Object();
   private Object eraseCompleteLock = new Object();
   private Object waitBufferEmptyLock = new Object();
   private boolean isEraseImgComplete = false;
   private boolean waitScannerCalibComplete = false;
   private short waitNextImgLocation = -1;
   private PrintStatus lastPrintStatus = null;
   private short lastImgLocation = -1;
   private AbstractHandleCmd.DefaultResult defaultResult = new AbstractHandleCmd.DefaultResult();
   private HandleException statisticException = null;
   public static final int TIMEOUT_GET_NEXT_IMG_LOCATION = 30000;
   public static final int TIMEOUT_ERASE_IMAGES = 60000;
   public static final int TIMEOUT_CALIBRATION_CMD = 60000;

   public IBM4610CheckScannerImp(HandleKey key, IBMPrinterComposite printerParent) {
      super(key, printerParent);
   }

   public void init() throws HandleException {
      super.init();
      this.submitStatusCmd();
      this.initImageMemory();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.statisticException = null;
      if (this.isTracerOn()) {
         this.traceNormal("-->submit(" + cmd.getName() + ")");
      }

      this.defaultResult.setInError(false);
      this.handleCmdFilterV.reset();
      cmd.accept(this.handleCmdFilterV);
      if (this.handleCmdFilterV.isCheckScannerCmd()) {
         ((CheckScannerCmd)cmd).accept(this.visitor);
      } else {
         if (!this.handleCmdFilterV.isSystemCmd()) {
            throw new HandleException("Invalid CheckScannerCmd object submitted!");
         }

         ((SystemCmd)cmd).accept(this.visitor);
      }

      if (this.statisticException != null) {
         throw this.statisticException;
      } else {
         if (this.isTracerOn()) {
            this.traceNormal("<--submit(" + cmd.getName() + ")");
         }
      }
   }

   public void receivePrintStatus(PrintStatus ps) {
      if (ps != null && this.isInit()) {
         byte[] status = ps.getData();
         this.lastPrintStatus = ps;
         if (this.isTracerOn()) {
            if (status.length < 60) {
               this.traceMaximum(Util.toFormatedHexString(status));
            } else {
               this.traceMaximum("Status 1..60 :" + Util.toFormatedHexString(status, 0, 59));
               this.traceMaximum("Status with lenght : " + status.length);
            }
         }

         if (PosjUtil.isBitSelected(status[0], 7)) {
            this.isCmdRejected = true;
         }

         this.updateStatus(ps);
         if (this.isStartScanReadError(status)) {
            this.getEventHelper().fireErrorEvent(new ErrorEvent(this, 9));
         }

         if (this.isEraseFlashComplete(status)) {
            this.isEraseImgComplete = true;
            synchronized (this.eraseCompleteLock) {
               this.eraseCompleteLock.notifyAll();
            }
         }

         if (PosjUtil.isBitSelected(status[1], 6)) {
            synchronized (this.waitBufferEmptyLock) {
               this.waitBufferEmptyLock.notifyAll();
            }
         }
      }
   }

   public void receivePrintDataEvent(PrintDataEvent pde) {
      if (pde != null && this.isInit()) {
         byte[] data = pde.getData();
         if (this.isTracerOn()) {
            this.traceMaximum("-->receivePrintDataEvent() PrintDataEvent.getType() : " + pde.getType());
            if (data != null && data.length < 60) {
               this.traceMaximum("PrintDataEvent.pde.getData() :" + Util.toFormatedHexString(data));
            } else {
               this.traceMaximum("PrintDataEvent.pde.getData().length :" + data.length);
            }
         }

         if (pde.getType() == 4) {
            this.waitNextImgLocation = Util.toShort(data[0], data[1]);
            synchronized (this.waitNextImgLocationLock) {
               this.waitNextImgLocationLock.notifyAll();
            }
         }

         if (pde.getType() == 64) {
            byte dataCode = 0;
            if (this.isHeaderRequest) {
               dataCode = 8;
            } else {
               dataCode = 7;
            }

            ByteBuffer buffer = pde.getDataBuffer();
            CheckScannerDataEvent csEvent = new CheckScannerDataEvent(this, buffer.getBytes(), dataCode, this.lastImgLocation);
            if (csEvent.getFileLocation() != 0 && !this.getHandle().getImageMemory().existImageHeader(csEvent.getFileLocation())) {
               CheckScannerHandle.ImageHeader ih = this.getHandle().createImageHeader(csEvent.getFileLocation(), csEvent.getData());
               this.getHandle().getImageMemory().addImage(ih);
               this.getHandle().getImageMemory().calculateSize();
               if (this.isTracerOn() && this.isInitImageMemory) {
                  this.traceMaximum(this.getHandle().getImageMemory().toString());
               }
            }

            if (this.isInitImageMemory) {
               this.getEventHelper().fireDataEvent(csEvent);
               if (this.isTracerOn()) {
                  if (csEvent.getDataEventCode() == 7) {
                     this.traceMaximum("Data Event Fired length :" + csEvent.getData().length);
                  } else {
                     this.traceMaximum("Data Event Fired : " + csEvent);
                  }
               }
            }

            if (!this.isInitImageMemory) {
               synchronized (this.initImageMemoryLock) {
                  this.initImageMemoryLock.notifyAll();
               }
            }
         }

         if (pde.getType() == 32) {
            byte[] status = pde.getData();
            CheckScannerStatusEvent stsEvt = new CheckScannerStatusEvent(this, 5, Util.toShort(status[0], status[1]), Util.toShort(status[2], status[3]));
            if (this.isTracerOn()) {
               this.traceNormal(
                  "Firing StatusEvent : STATUS_IMAGE_READ, getDocHeight() "
                     + stsEvt.getDocumentHeight()
                     + " 0x"
                     + Util.toHexString(stsEvt.getDocumentHeight())
                     + ", getDocWidth() "
                     + stsEvt.getDocumentWidth()
                     + " 0x"
                     + Util.toHexString(stsEvt.getDocumentWidth())
               );
            }

            this.getEventHelper().fireStatusEvent(stsEvt);
         }

         if (this.isTracerOn()) {
            this.traceMaximum("<--receivePrintDataEvent()");
         }
      }
   }

   public void handleCmdComplete(PrintCmd printCmd) {
      if (this.isTracerOn()) {
         this.traceMaximum("-->handleCmdComplete() : " + printCmd.getHandleCmd().getName() + "<-- " + "printCmd.isSucceeded()" + printCmd.isSucceeded());
      }

      if (printCmd.getHandleCmd() instanceof IBM4610PrinterCmd.ChangePrintSideCmd) {
         this.completeChangePrintSidecmd();
      }

      printCmd.getOtherHandleCmd().setResult(this.defaultResult);
      printCmd.getOtherHandleCmd().setCompleted(true);
      printCmd.getHandleCmd().setCompleted(true);
      printCmd.getHandleCmd().recycle();
   }

   public void handleError(PrintCmd err, PrintErrorEvent event) {
      if (this.isTracerOn()) {
         this.traceNormal(
            "-->handleError - event.getErrorType() : "
               + event.getErrorType()
               + ", event.getErrorCode() : "
               + event.getErrorCode()
               + ", getOffender() :"
               + event.getOffender().getName()
               + ", getPrinterErrorCode() :"
               + event.getPrinterErrorCode()
               + " instance: "
               + err.getOtherHandleCmd()
         );
      }

      if (event.getPrinterErrorCode() != 32 && event.getPrinterErrorCode() != 4) {
         this.defaultResult.setErrorCode(event.getPrinterErrorCode());
      } else {
         this.defaultResult.setErrorCode(10);
      }

      this.defaultResult.setInError(true);
      err.getOtherHandleCmd().setResult(this.defaultResult);
      err.getOtherHandleCmd().setCompleted(true);
      this.getDevQueue().processErrorResponse(err, (byte)0);
      if (this.isTracerOn()) {
         this.traceNormal("<--handleError");
      }
   }

   public synchronized byte getPrintSide() {
      return ((IBMPrinterState)this.getPrinterParent().getPrinterHandleState()).getSlpPrintSide();
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
      int id = ((DefaultPOSPrinterHandle)this.getPrinterParent().getHandle()).getPrinterHandleState().getPrinterID();
      int ecLevel = ((DefaultPOSPrinterHandle)this.getPrinterParent().getHandle()).getPrinterHandleState().getPrinterEC();
      cmd.setFirmwareLevel(ecLevel);

      try {
         switch (id) {
            case 3814:
               cmd.setDeviceId(2501);
               break;
            case 3822:
               cmd.setDeviceId(2502);
               break;
            case 3829:
               cmd.setDeviceId(2503);
               break;
            case 3837:
               cmd.setDeviceId(2506);
               break;
            case 3838:
               cmd.setDeviceId(2505);
               break;
            case 3839:
               cmd.setDeviceId(2504);
               break;
            default:
               cmd.setDeviceId(2500);
         }

         cmd.setSerialNumber(this.getPrinterParent().getPrinterHandleState().getSerialNumber());
         cmd.setFirmwareLevel(this.getPrinterParent().getPrinterHandleState().getPrinterEC());
      } catch (Exception var8) {
      } finally {
         cmd.setCompleted(true);
      }

      if (this.isTracerOn()) {
         this.traceNormal("Using Device ID : " + cmd.getDeviceId());
      }
   }

   protected void initImageMemory() throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("--> initImageMemory");
      }

      CheckScannerCmd.GetNextImgLocCmd cmd = this.getFactory().createGetNextImgLocCmd();
      this.submit(cmd);
      short nextImg = cmd.getNextImg();
      short HEADER = 0;

      while (nextImg > 1) {
         this.submit(this.getFactory().createGetScannedImgCmd(--nextImg, HEADER, HEADER));
         synchronized (this.initImageMemoryLock) {
            try {
               this.initImageMemoryLock.wait(10000L);
            } catch (InterruptedException var7) {
            }
         }
      }

      this.getHandle().getImageMemory().calculateSize();
      this.isInitImageMemory = true;
      if (this.isTracerOn()) {
         this.traceNormal(this.getHandle().getImageMemory().toString());
      }

      if (this.isTracerOn()) {
         this.traceNormal("<-- initImageMemory");
      }
   }

   protected CheckScannerHandle getHandle() {
      return (CheckScannerHandle)this.getHandleImp().getHandle();
   }

   protected CheckScannerCmd.Factory getFactory() {
      return ((CheckScannerHandle)this.getHandleImp().getHandle()).getCheckScannerCmdFactory();
   }

   protected void submitBeginInsertionCmd(HandleCmd cmd) throws HandleException {
      if (this.getState().isDocInserted()) {
         DefaultPOSPrinterCmd dpc = (DefaultPOSPrinterCmd)this.get4610PrinterCmdFactory().createRegisterSlipCmd(true);
         this.submitPrintCmd(cmd, dpc);
      } else {
         throw new HandleException("The document is not present at the front");
      }
   }

   protected void submitBeginRemovalCmd(HandleCmd cmd) throws HandleException {
      DefaultPOSPrinterCmd dpc = (DefaultPOSPrinterCmd)this.get4610PrinterCmdFactory().createEjectSlipCmd();
      this.submitPrintCmd(cmd, dpc);
   }

   protected void submitEndRemovalCmd(HandleCmd cmd) {
   }

   protected void submitScannerCalibCmd(HandleCmd cmd) throws HandleException {
      if (this.getState().isDocRegistered()) {
         IBM4610PrinterCmd pc = (IBM4610PrinterCmd)this.get4610PrinterCmdFactory().createScannerCalibCmd();
         this.submitPrintCmd(cmd, pc);
         this.waitCalibComplete(60000);
      } else {
         throw new HandleException("A white document is not ready to calibrate");
      }
   }

   protected void submitChangePrintSideCmd(HandleCmd cmd) throws HandleException {
      if (this.getState().isDocRegistered()) {
         DefaultPOSPrinterCmd dpc = (DefaultPOSPrinterCmd)this.get4610PrinterCmdFactory().createChangePrintSideCmd();
         this.submitPrintCmd(cmd, dpc);
      } else {
         throw new HandleException("Error - No check was detected");
      }
   }

   protected void submitStartScanCmd(CheckScannerCmd.StartScanCmd cmd) throws HandleException {
      if (this.getState().isDocRegistered()) {
         IBM4610PrinterCmd pc = (IBM4610PrinterCmd)this.get4610PrinterCmdFactory().createStartScanCmd(cmd.getMode());
         this.submitPrintCmd(cmd, pc);
      } else {
         throw new HandleException("The document is not ready to scan");
      }
   }

   protected void submitPrintScannedImgCmd(CheckScannerCmd.PrintScannedImgCmd cmd) throws HandleException {
      if (!this.isOutOfPaper) {
         IBM4610PrinterCmd pc = (IBM4610PrinterCmd)this.get4610PrinterCmdFactory()
            .createPrintScannedImgCmd(cmd.getLocation(), cmd.getCorners(), cmd.getOffset(), cmd.getScale(), cmd.rotateImage());
         this.submitPrintCmd(cmd, pc);
      } else {
         throw new HandleException("The printer is out of paper");
      }
   }

   protected void submitGetNextImgLocCmd(CheckScannerCmd.GetNextImgLocCmd cmd) throws HandleException {
      IBM4610PrinterCmd pc = (IBM4610PrinterCmd)this.get4610PrinterCmdFactory().createGetNextImgLocCmd();
      this.waitNextImgLocation = -1;
      this.submitPrintCmd(cmd, pc);
      cmd.setNextImg(this.waitNextImgLocation(30000));
   }

   protected void submitEraseImagesCmd(HandleCmd cmd) throws HandleException {
      this.checkBufferEmpty(this.lastPrintStatus.getData());
      DefaultPOSPrinterCmd dpc = (DefaultPOSPrinterCmd)this.get4610PrinterCmdFactory().createEraseFlashSectorCmd((byte)8);
      this.isEraseImgComplete = false;
      this.submitPrintCmd(cmd, dpc);
      this.waitEraseComplete(60000);
      this.getHandle().getImageMemory().clearMemory();
   }

   protected void submitGetFirstUnreadImgLocCmd(CheckScannerCmd.GetFirstUnreadImgLocCmd cmd) throws HandleException {
      IBM4610PrinterCmd pc = (IBM4610PrinterCmd)this.get4610PrinterCmdFactory().createGetFirstUnreadImgLocCmd();
      this.waitNextImgLocation = -1;
      this.submitPrintCmd(cmd, pc);
      cmd.setFirstUnreadImg(this.waitNextImgLocation(30000));
   }

   protected void submitSelCompressionFormatCmd(CheckScannerCmd.SelCompressionFormatCmd cmd) throws HandleException {
      this.checkArgs(cmd);
      IBM4610PrinterCmd pc = (IBM4610PrinterCmd)this.get4610PrinterCmdFactory().createSelCompressionFormatCmd(cmd.getFormat(), cmd.getContrast());
      this.submitPrintCmd(cmd, pc);
   }

   protected void submitStoreScannedImgCmd(CheckScannerCmd.StoreScannedImgCmd cmd) throws HandleException {
      IBM4610PrinterCmd pc = (IBM4610PrinterCmd)this.get4610PrinterCmdFactory()
         .createStoreScannedImgCmd(cmd.getStorageMethod(), cmd.getCorner(), cmd.getOffset(), cmd.getCornerSub(), cmd.getOffsetSub(), cmd.getTagData());
      this.submitPrintCmd(cmd, pc);
      pc.waitUntilCompleted();
      if (!cmd.getResult().isInError()) {
         this.waitBufferEmpty(60000);
      }
   }

   protected void submitStatisticCmd(SystemCmd.StatisticCmd cmd) {
      try {
         DefaultPOSPrinterCmd dpc = (DefaultPOSPrinterCmd)this.get4610PrinterCmdFactory().createStatisticCmd(cmd.getStatisticType());
         this.submitPrintCmd(cmd, dpc);
         dpc.waitUntilCompleted(60000L);
         cmd.setStatisticResult(dpc.getRequestData().getBytes());
      } catch (HandleException var3) {
         HandleException e = this.statisticException;
      }
   }

   protected void submitGetScannedImgCmd(CheckScannerCmd.GetScannedImgCmd cmd) throws HandleException {
      if (cmd.getOffset() == 0 && cmd.getNumBytes() == 0) {
         this.isHeaderRequest = true;
      } else {
         this.isHeaderRequest = false;
      }

      this.lastImgLocation = cmd.getLocation();
      IBM4610PrinterCmd pc = (IBM4610PrinterCmd)this.get4610PrinterCmdFactory().createGetScannedImgCmd(cmd.getLocation(), cmd.getOffset(), cmd.getNumBytes());
      this.submitPrintCmd(cmd, pc);
   }

   protected void submitPrintCmd(HandleCmd cmd, DefaultPOSPrinterCmd dpc) throws IllegalArgumentException, HandleException {
      this.isCmdRejected = false;
      PrintCmd pc = dpc.getPrintCmd();
      pc.setParent(this);
      pc.setOtherHandleCmd(cmd);
      this.getDevQueue().submit(pc);
      if (this.isCmdRejected) {
         throw new HandleException("Error when sending cmd : " + cmd.getName() + ", errorCode : REJECTED");
      }
   }

   protected boolean isImageScanComplete(byte[] status) {
      return status.length < 11 ? false : PosjUtil.isBitSelected(status[4], 6);
   }

   protected boolean isStartScanReadError(byte[] status) {
      return PosjUtil.isBitSelected(status[7], 7);
   }

   protected void checkBufferEmpty(byte[] status) throws HandleException {
      int retry = 0;

      while (retry++ < 10 && !PosjUtil.isBitSelected(status[1], 6)) {
         try {
            Thread.sleep(1000L);
         } catch (InterruptedException var4) {
         }

         this.submitStatusCmd();
         status = this.getLastPrintStatus().getData();
      }

      if (retry > 10) {
         throw new HandleException("Buffer is not empty ");
      }
   }

   protected boolean isEraseFlashComplete(byte[] status) {
      return PosjUtil.isBitSelected(status[2], 7);
   }

   protected boolean isImageDataAttached(byte[] status) {
      return PosjUtil.isBitSelected(status[4], 7);
   }

   protected void checkArgs(CheckScannerCmd.SelCompressionFormatCmd cmd) throws HandleException {
      switch (cmd.getFormat()) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
         case 16:
         case 17:
         case 18:
         case 20:
            if (cmd.getContrast() < 0 && cmd.getContrast() > 255) {
               throw new HandleException("Invalid Contrast Range :" + cmd.getContrast());
            }

            return;
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 19:
         default:
            throw new HandleException("Invalid Image Format Received :" + cmd.getFormat());
      }
   }

   protected void waitCalibComplete(int timeout) throws HandleException {
      this.waitScannerCalibComplete = true;
      synchronized (this.scannerCalibCompleteLock) {
         try {
            this.scannerCalibCompleteLock.wait((long)timeout);
         } catch (InterruptedException var5) {
         }
      }

      this.waitScannerCalibComplete = false;
      if (this.getState().isDocInserted() || this.getState().isDocRegistered()) {
         throw new HandleException("Unable to get a success completion for ScannerCalibCmd ( timeout = " + timeout + " msecs)");
      }
   }

   protected short waitNextImgLocation(int timeout) throws HandleException {
      synchronized (this.waitNextImgLocationLock) {
         if (this.waitNextImgLocation == -1) {
            try {
               this.waitNextImgLocationLock.wait((long)timeout);
            } catch (InterruptedException var5) {
            }
         }
      }

      if (this.waitNextImgLocation == -1) {
         throw new HandleException("Unable to get a response for GetNextImageLocation");
      } else {
         return this.waitNextImgLocation;
      }
   }

   protected void waitBufferEmpty(int timeout) {
      synchronized (this.waitBufferEmptyLock) {
         try {
            this.waitBufferEmptyLock.wait((long)timeout);
         } catch (InterruptedException var5) {
         }
      }
   }

   protected void waitEraseComplete(int timeout) throws HandleException {
      synchronized (this.eraseCompleteLock) {
         if (!this.isEraseImgComplete) {
            try {
               this.eraseCompleteLock.wait((long)timeout);
            } catch (InterruptedException var5) {
            }
         }
      }

      if (!this.isEraseImgComplete) {
         throw new HandleException("Unable to get a success response for EraseImagedCmd ( timeout = " + timeout + " msecs)");
      }
   }

   protected PrintStatus getLastPrintStatus() {
      return this.lastPrintStatus;
   }

   protected void updateStatus(PrintStatus ps) {
      boolean statusUpdated = false;
      if (ps.getStatus(4) != this.getState().isDocInserted()) {
         this.getState().setDocInserted(ps.getStatus(4));
         this.fireStatus(2, this.getState().isDocInserted());
         statusUpdated = true;
      }

      if (ps.getStatus(2) != this.getState().isDocRegistered()) {
         this.getState().setDocRegistered(ps.getStatus(2));
         this.fireStatus(3, this.getState().isDocRegistered());
         if (this.waitScannerCalibComplete && !this.getState().isDocRegistered() && !this.getState().isDocInserted()) {
            synchronized (this.scannerCalibCompleteLock) {
               this.scannerCalibCompleteLock.notifyAll();
            }
         }

         statusUpdated = true;
      }

      if (ps.getStatus(6) != this.getState().isCoverOpen()) {
         this.getState().setCoverOpen(ps.getStatus(6));
         this.fireStatus(4, this.getState().isCoverOpen());
         statusUpdated = true;
      }

      if (ps.getStatus(0) != this.isOutOfPaper) {
         this.isOutOfPaper = ps.getStatus(0);
         this.fireStatus(6, this.isOutOfPaper);
         statusUpdated = true;
      }

      if (statusUpdated & this.isTracerOn()) {
         this.traceMaximum(
            "isDocumentInserted = "
               + this.getState().isDocInserted()
               + " isDocumentRegistered = "
               + this.getState().isDocRegistered()
               + " isCoverOpen = "
               + this.getState().isCoverOpen()
               + " isOutOfPaper = "
               + this.isOutOfPaper
         );
      }
   }

   protected CheckScannerHandle.State getState() {
      return ((CheckScannerHandle)this.getHandleImp().getHandle()).getCheckScannerState();
   }

   protected void fireStatus(int id, boolean b) {
      this.getEventHelper().fireStatusEvent(new CheckScannerStatusEvent(this, id, b));
   }

   private void completeChangePrintSidecmd() {
      IBM4610Imp.IBM4610PrinterState pState = (IBM4610Imp.IBM4610PrinterState)this.getPrinterParent().getPrinterHandleState();
      byte side = pState.getSlpPrintSide();
      if (side == 1) {
         side = 2;
      } else {
         side = 1;
      }

      pState.setSlpPrintSide(side);
   }

   public class CheckScannerCmdVisitor implements com.ibm.posj.CheckScannerCmdVisitor, SystemCmdVisitor {
      private IBM4610CheckScannerImp parent;

      public CheckScannerCmdVisitor(IBM4610CheckScannerImp parent) {
         this.parent = parent;
      }

      public void visitBeginInsertionCmd(CheckScannerCmd.BeginInsertionCmd cmd) throws HandleException {
         this.parent.submitBeginInsertionCmd(cmd);
      }

      public void visitBeginRemovalCmd(CheckScannerCmd.BeginRemovalCmd cmd) throws HandleException {
         this.parent.submitBeginRemovalCmd(cmd);
      }

      public void visitEndRemovalCmd(CheckScannerCmd.EndRemovalCmd cmd) throws HandleException {
         this.parent.submitEndRemovalCmd(cmd);
      }

      public void visitStartScanCmd(CheckScannerCmd.StartScanCmd cmd) throws HandleException {
         this.parent.submitStartScanCmd(cmd);
      }

      public void visitPrintScannedImgCmd(CheckScannerCmd.PrintScannedImgCmd cmd) throws HandleException {
         this.parent.submitPrintScannedImgCmd(cmd);
      }

      public void visitGetNextImgLocCmd(CheckScannerCmd.GetNextImgLocCmd cmd) throws HandleException {
         this.parent.submitGetNextImgLocCmd(cmd);
      }

      public void visitSelCompressionFormatCmd(CheckScannerCmd.SelCompressionFormatCmd cmd) throws HandleException {
         this.parent.submitSelCompressionFormatCmd(cmd);
      }

      public void visitEraseImagesCmd(CheckScannerCmd.EraseImagesCmd cmd) throws HandleException {
         this.parent.submitEraseImagesCmd(cmd);
      }

      public void visitStoreScannedImgCmd(CheckScannerCmd.StoreScannedImgCmd cmd) throws HandleException {
         this.parent.submitStoreScannedImgCmd(cmd);
      }

      public void visitGetScannedImgCmd(CheckScannerCmd.GetScannedImgCmd cmd) throws HandleException {
         this.parent.submitGetScannedImgCmd(cmd);
      }

      public void visitGetFirstUnreadImgLocCmd(CheckScannerCmd.GetFirstUnreadImgLocCmd cmd) throws HandleException {
         this.parent.submitGetFirstUnreadImgLocCmd(cmd);
      }

      public void visitScannerCalibCmd(CheckScannerCmd.ScannerCalibCmd cmd) throws HandleException {
         this.parent.submitScannerCalibCmd(cmd);
      }

      public void visitChangePrintSideCmd(CheckScannerCmd.ChangePrintSideCmd cmd) throws HandleException {
         this.parent.submitChangePrintSideCmd(cmd);
      }

      public void visitStatisticCmd(SystemCmd.StatisticCmd cmd) {
         this.parent.submitStatisticCmd(cmd);
      }

      public void visitTestSystemCmd(SystemCmd.TestRequestCmd cmd) {
      }

      public void visitStatusSystemCmd(SystemCmd.StatusRequestCmd cmd) {
         IBM4610CheckScannerImp.this.submitStatusCmd();
      }

      public void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
         IBM4610CheckScannerImp.this.submitDevInfoCmd(cmd);
      }

      public void visitResetSystemCmd(SystemCmd.ResetRequestCmd cmd) {
      }

      public void visitDirectWriteCmd(SystemCmd.DirectWriteCmd cmd) {
      }
   }
}
