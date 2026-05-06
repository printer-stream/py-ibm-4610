package com.ibm.posj.flash;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.Handle;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.bus.AbstractHandleImp;
import com.ibm.posj.bus.FlashHandleImp;
import com.ibm.posj.bus.hid.AbstractHidHandleImp;
import com.ibm.posj.util.DevCats;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class FlashManager {
   public ArrayList microcodeFilesList = null;
   protected static FlashManager instance = null;
   private Iterator handles = null;
   private FlashFilesRegistry flashFilesRegistry = new FlashFilesRegistry();
   private FlashFileFactory flashFileFactory;
   private ArrayList usbFlashFileList;
   private ArrayList rs485FlashFileList;
   private String usbFlashFilePath;
   private String rs485FlashFilePath;
   private int printerID = 0;
   private int printerEC = 0;
   private int printerType = 0;
   private boolean bootSectorFlag = false;
   private Tracer tracer = TracerFactory.getInstance().createTracer("FLASH", "FlashManager");
   public static final short IBM_4610_PRINTER_PID = 17717;
   public static final short IBM_4689_PRINTER_PID = 17719;
   private String T1_OR_T2_PRINTER_FILENAME = "aip46mc.hex";
   private String T3_OR_T4_PRINTER_FILENAME = "aip46mch.hex";
   private String P4610_SST_PRINTER_FILENAME = "aip46mch.hex";
   private String T5_PRINTER_FILENAME = "aip46mcd.hex";
   private String P4610_DB_SST_PRINTER_FILENAME = "aip46mcd.hex";

   public static FlashManager getInstance() {
      if (instance == null) {
         instance = new FlashManager();
         instance.init();
      }

      return instance;
   }

   protected void init() {
      this.flashFileFactory = new DefaultFlashFileFactory();
   }

   public void flash(Handle handle) throws FlashException {
      this.tracer.println(".flash() " + handle.getDevCat().toString());
      FlashHandleImp flashHandleImp = null;
      if (handle.getHandleImp() instanceof AbstractHandleImp) {
         flashHandleImp = ((AbstractHandleImp)handle.getHandleImp()).getFlashHandleImp();
         if (null == flashHandleImp) {
            throw new FlashException("No FlashHandleImp!");
         }
      }

      if (handle.isFlashable()) {
         if (handle.getDevCat().toString().equals(DevCats.POSPRINTER_DEVCAT.toString())) {
            this.tracer.println(".flash() flash printer");
            List l = this.getFlashFiles(handle);
            flashHandleImp.flashPOSPrinter(handle, this.buildFlashRequest(handle), new ArrayList(l));
         } else {
            FlashRequest fr = new FlashRequest(handle, this.getFlashFile(handle));
            if (fr.getFlashFile() == null) {
               throw new FlashException("No respective FlashFile for the " + handle.getDevBus().toString() + " " + handle.getDevCat().toString() + " handle");
            }

            handle.flash(fr);
            flashHandleImp.reset();
            this.tracer.println(".flash() USB device - DONE!!!");
         }
      } else {
         throw new FlashException("Trying to flash non-flashable device: " + handle.getDevCat().toString());
      }
   }

   public void flashAll() throws FlashException {
      Iterator handles = PosSystemManager.getInstance().getHandleRegistry().getHandles();

      while (handles.hasNext()) {
         Handle currHandle = (Handle)handles.next();
         if (currHandle.isFlashable()) {
            this.flash(currHandle);
         }
      }
   }

   public void start() throws FlashException {
      this.handles = PosSystemManager.getInstance().getHandleRegistry().getHandles();
      String fs = System.getProperty("file.separator");
      this.usbFlashFilePath = this.getFlashFilePath() + fs + "usb" + fs;
      this.rs485FlashFilePath = this.getFlashFilePath() + fs + "rs485" + fs;
   }

   public FlashFileFactory getFlashFileFactory() {
      return new DefaultFlashFileFactory();
   }

   public String getFlashFilePath() {
      String flashFilePath = FlashPathFinder.getInstance().getFilePath();
      this.tracer.println(".getFlashFilePath() flashFilePath=" + flashFilePath);
      return FlashPathFinder.getInstance().getFilePath();
   }

   public ArrayList getUsbFlashFileList() {
      return this.usbFlashFileList;
   }

   public ArrayList getRs485FlashFileList() {
      return this.rs485FlashFileList;
   }

   public FlashRequest getFlashRequest(Handle handle) throws FlashException {
      for (FlashRequest flashRequest : this.getFlashRequests(handle)) {
         if (flashRequest.getHandle().getHandleKey().equals(handle.getHandleKey())) {
            return flashRequest;
         }
      }

      return null;
   }

   public void loadHeaders(ArrayList fileList) throws FlashException {
      Iterator flashFiles = fileList.iterator();

      while (flashFiles.hasNext()) {
         ((FlashFile)flashFiles.next()).loadHeader();
      }
   }

   public int getPrinterID() {
      return this.printerID;
   }

   public int getPrinterEC() {
      return this.printerEC;
   }

   public int getPrinterType() {
      return this.printerType;
   }

   public void flashUsbDevice(Handle handle, String fileName) throws FlashException {
      this.tracer.println(".flashUsbDevice()- fileName: " + fileName);
      File f = new File(fileName);
      if (!f.exists()) {
         this.tracer
            .println(".flashUsbDevice()-No respective FlashFile for the " + handle.getDevBus().toString() + " " + handle.getDevCat().toString() + " handle");
         throw new FlashException("No respective FlashFile for the " + handle.getDevBus().toString() + " " + handle.getDevCat().toString() + " handle");
      } else {
         UsbFlashFile flashFile = new UsbFlashFile(fileName);
         int productId = -1;
         FlashHandleImp flashHandleImp = null;
         flashFile.loadHeader();
         if (handle.getHandleImp() instanceof AbstractHandleImp) {
            flashHandleImp = ((AbstractHandleImp)handle.getHandleImp()).getFlashHandleImp();
            if (null == flashHandleImp) {
               this.tracer.println(".flashUsbDevice()-No FlashHandleImp!");
               throw new FlashException("No FlashHandleImp!");
            }

            productId = flashHandleImp.getProductID();
         }

         if (!flashFile.isProductIDMatched(productId)) {
            this.tracer.println(".flashUsbDevice()-ProductID not matched!");
            throw new FlashException("ProductID not matched");
         } else {
            FlashRequest flashRequest = new FlashRequest(handle, flashFile);
            handle.flash(flashRequest);
            flashHandleImp.reset();
            this.tracer.println(".flashUSBDevice -> flash() USB device - DONE!!!");
         }
      }
   }

   public void flashPrinterDevice(Handle handle, String usbFileName, String rs485FileName) throws FlashException {
      this.tracer.println(".flashPrinterDevice() usbFilename: " + usbFileName + " rs485FileName: " + rs485FileName);
      FlashHandleImp flashHandleImp = null;
      if (handle.getHandleImp() instanceof AbstractHandleImp) {
         flashHandleImp = ((AbstractHandleImp)handle.getHandleImp()).getFlashHandleImp();
         if (null == flashHandleImp) {
            throw new FlashException("No FlashHandleImp!");
         }
      }

      if (handle.isFlashable()) {
         this.tracer.println(".flashPrinterDevice() flash printer");
         flashHandleImp.flashPOSPrinterSDICC(handle, usbFileName, rs485FileName);
      }
   }

   public void setRs485PrinterInfo(int printerID, int printerEC, int printerType) {
      this.printerID = printerID;
      this.printerEC = printerEC;
      this.printerType = printerType;
   }

   public FlashFile getFlashFile(Handle handle) throws FlashException {
      String devBus = handle.getDevBus().toString();
      String devCat = handle.getDevCat().toString();
      Iterator flashFiles = this.flashFilesRegistry.getFlashFiles(handle).iterator();
      boolean hasNext = flashFiles.hasNext();
      if (hasNext) {
         FlashFile currFlashFile = (FlashFile)flashFiles.next();
         if (currFlashFile instanceof UsbFlashFile) {
            int productId = -1;
            if (handle.getHandleImp() instanceof AbstractHandleImp) {
               FlashHandleImp flashHandleImp = ((AbstractHandleImp)handle.getHandleImp()).getFlashHandleImp();
               if (null == flashHandleImp) {
                  throw new FlashException("No FlashHandleImp!");
               }

               int var9 = flashHandleImp.getProductID();
            }

            while (hasNext) {
               int hwLevel = ((AbstractHidHandleImp)handle.getHandleImp()).getBCDLevel();
               if (currFlashFile.isUSBHardwareLevelMatched(hwLevel)) {
                  this.tracer.println(".getFlashFile() -find Matched!");
                  return currFlashFile;
               }

               hasNext = flashFiles.hasNext();
               if (hasNext) {
                  currFlashFile = (UsbFlashFile)flashFiles.next();
               }
            }
         } else if (currFlashFile instanceof Rs485FlashFile) {
            while (hasNext) {
               if (devCat.equals(currFlashFile.getDevCat().toString())) {
                  return currFlashFile;
               }

               hasNext = flashFiles.hasNext();
               if (hasNext) {
                  currFlashFile = (UsbFlashFile)flashFiles.next();
               }
            }
         }
      }

      this.tracer.println(".getFlashFile(), return FlashFile=null");
      return null;
   }

   public FlashFile findPrinterFlashFile(int printerID) throws FlashException {
      Iterator flashFiles = this.rs485FlashFileList.iterator();
      if (printerID == 3819) {
         while (flashFiles.hasNext()) {
            FlashFile currFlashFile = (Rs485FlashFile)flashFiles.next();
            if (currFlashFile.getFilename().endsWith(this.T1_OR_T2_PRINTER_FILENAME)) {
               this.tracer.println(".findPrinterFlashFile()-find printer file: " + currFlashFile.getFilename());
               return currFlashFile;
            }
         }
      } else if (printerID == 3820) {
         while (flashFiles.hasNext()) {
            FlashFile currFlashFile = (Rs485FlashFile)flashFiles.next();
            if (currFlashFile.getFilename().endsWith(this.T3_OR_T4_PRINTER_FILENAME)) {
               this.tracer.println(".findPrinterFlashFile() - find printer file: " + currFlashFile.getFilename());
               return currFlashFile;
            }
         }
      } else if (printerID == 3821) {
         while (flashFiles.hasNext()) {
            FlashFile currFlashFile = (Rs485FlashFile)flashFiles.next();
            if (currFlashFile.getFilename().endsWith(this.T5_PRINTER_FILENAME)) {
               this.tracer.println(".findPrinterFlashFile() - find printer file: " + currFlashFile.getFilename());
               return currFlashFile;
            }
         }
      } else if (printerID == 3823) {
         while (flashFiles.hasNext()) {
            FlashFile currFlashFile = (Rs485FlashFile)flashFiles.next();
            if (currFlashFile.getFilename().endsWith(this.P4610_SST_PRINTER_FILENAME)) {
               this.tracer.println(".findPrinterFlashFile() - find printer file: " + currFlashFile.getFilename());
               return currFlashFile;
            }
         }
      } else if (printerID == 3826) {
         while (flashFiles.hasNext()) {
            FlashFile currFlashFile = (Rs485FlashFile)flashFiles.next();
            if (currFlashFile.getFilename().endsWith(this.P4610_DB_SST_PRINTER_FILENAME)) {
               this.tracer.println(".findPrinterFlashFile() - find printer file: " + currFlashFile.getFilename());
               return currFlashFile;
            }
         }
      }

      this.tracer.println(".findFlashFile()-find printer file - No Printer File for printerID, " + printerID);
      return null;
   }

   public void setBootSectorFlag(boolean bootSectorFlag) {
      this.bootSectorFlag = bootSectorFlag;
   }

   public boolean isBootSectorFlag() {
      return this.bootSectorFlag;
   }

   private Vector getFlashRequests(Handle handle) throws FlashException {
      Vector flashRequests = new Vector();
      Iterator flashFilesIterator = this.getFlashFiles(handle).iterator();

      while (flashFilesIterator.hasNext()) {
         flashRequests.add(new FlashRequest(handle, (FlashFile)flashFilesIterator.next()));
      }

      return flashRequests;
   }

   private List getFlashFiles(Handle handle) throws FlashException {
      return this.flashFilesRegistry.getFlashFiles(handle);
   }

   protected Vector buildFlashRequest(Handle handle) throws FlashException {
      Vector vector = new Vector();
      boolean usbFound = false;
      boolean rs485Found = false;
      this.tracer.println(".buildUsbBridgeFlashRequest()");
      this.tracer.println("dev bus = " + handle.getDevBus().toString());
      Vector flashFiles = (Vector)this.flashFilesRegistry.getFlashFiles(handle);
      Iterator flashFilesIterator = flashFiles.iterator();

      while (flashFilesIterator.hasNext()) {
         FlashFile currFlashFile = (FlashFile)flashFilesIterator.next();
         if (currFlashFile instanceof UsbFlashFile) {
            if (!usbFound) {
               if (handle.isFlashable()) {
                  vector.add(new FlashRequest(handle, currFlashFile));
                  return vector;
               }

               flashFilesIterator.remove();
            }
         } else if (currFlashFile instanceof Rs485FlashFile && !rs485Found && currFlashFile.getDevCat().toString().equals(DevCats.POSPRINTER_DEVCAT.toString())
            )
          {
            FlashRequest rs485fr = new FlashRequest(handle, currFlashFile);
            this.tracer.println(".buildRs485FlashRequest() - Building Rs485 FlashRequest() - SUCCESS!!");
            vector.add(rs485fr);
            rs485Found = true;
         }
      }

      return vector;
   }

   public void buildMicrocodeFlashFileList() throws FlashException {
      if (this.microcodeFilesList == null) {
         this.microcodeFilesList = new ArrayList();
         File mf = new File(this.rs485FlashFilePath);
         if (mf.isDirectory()) {
            String[] files = mf.list();

            for (int i = 0; i < files.length; i++) {
               String flashFileName = this.rs485FlashFilePath + files[i];
               this.microcodeFilesList.add(flashFileName);
            }
         } else {
            this.tracer.println("error while attempt to get the folder directory");
         }
      }
   }

   public List getMicrocodeFilesList() {
      return this.microcodeFilesList;
   }
}
