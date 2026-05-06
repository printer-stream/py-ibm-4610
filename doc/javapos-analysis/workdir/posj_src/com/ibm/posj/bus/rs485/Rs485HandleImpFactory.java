package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jutil.Util;
import com.ibm.posj.HandleFactory;
import com.ibm.posj.HandleFactoryException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.bus.IBM4610PrinterHandleImp;
import com.ibm.posj.bus.IBMPrinterComposite;
import com.ibm.posj.printer.IBM4610SSTToneIndicatorImp;
import com.ibm.posj.printer.IBMPrinterToneIndicatorImp;
import com.ibm.posj.printer.ibm4610.IBM4610CashDrawerImp;
import com.ibm.posj.printer.ibm4610.IBM4610CheckScannerImp;
import com.ibm.posj.printer.ibm4610.IBM4610MICRImp;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import java.util.Hashtable;

public class Rs485HandleImpFactory {
   private Hashtable numberTable = new Hashtable();
   private HandleFactory handleFactory = null;

   public Rs485HandleImpFactory(HandleFactory factory) {
      this.handleFactory = factory;
   }

   public HandleImp createCashDrawer(SioDevice device, int devNumber, int cdNumber) throws HandleFactoryException {
      DevCat devCat = DevCats.CASHDRAWER_DEVCAT;
      HandleKey hk = new Rs485HandleKey(devCat, devNumber, device.getSioSlot().getSioSlotNumber(), device.getPortNumber(), device.getDeviceNumber());
      Rs485CashDrawerHandleImp hi = new Rs485CashDrawerHandleImp(hk, device, cdNumber);
      hi.setHandle(this.handleFactory.createCashDrawerHandle(hi));
      return hi;
   }

   public HandleImp createFiscalPrinter(SioDevice device) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.FISCALPRINTER_DEVCAT, device);
      Rs485FiscalPrinterHandleImp hi = new Rs485FiscalPrinterHandleImp(hk, device);
      hi.setHandle(this.handleFactory.createFiscalPrinterHandle(hi));
      return hi;
   }

   public HandleImp createLineDisplay(SioDevice device) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.LINEDISPLAY_DEVCAT, device);
      Rs485LineDisplayHandleImp hi = new Rs485LineDisplayHandleImp(hk, device);
      hi.setHandle(this.handleFactory.createLineDisplayHandle(hi));
      return hi;
   }

   public HandleImp createAPALineDisplay(SioDevice device) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.LINEDISPLAY_DEVCAT, device);
      Rs485APALineDisplayHandleImp hi = new Rs485APALineDisplayHandleImp(hk, device);
      hi.setHandle(this.handleFactory.createLineDisplayHandle(hi));
      return hi;
   }

   public HandleImp createMSR(SioDevice device) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.MSR_DEVCAT, device);
      Rs485MSRHandleImp hi = new Rs485MSRHandleImp(hk, device);
      hi.setHandle(this.handleFactory.createMSRHandle(hi));
      return hi;
   }

   public HandleImp createPOSKbd(SioDevice device) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.POSKEYBOARD_DEVCAT, device);
      Rs485POSKeyboardHandleImp hi = new Rs485POSKeyboardHandleImp(hk, device);
      hi.setHandle(this.handleFactory.createPOSKeyboardHandle(hi));
      return hi;
   }

   public HandleImp createKeylock(SioDevice device) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.KEYLOCK_DEVCAT, device);
      Rs485KeylockHandleImp hi = new Rs485KeylockHandleImp(hk, device);
      hi.setHandle(this.handleFactory.createKeylockHandle(hi));
      return hi;
   }

   public HandleImp createToneIndicator(SioDevice device) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.TONEINDICATOR_DEVCAT, device);
      Rs485ToneIndicatorHandleImp hi = new Rs485ToneIndicatorHandleImp(hk, device);
      hi.setHandle(this.handleFactory.createToneIndicatorHandle(hi));
      return hi;
   }

   public HandleImp createPOSPrinterMod4(SioDevice device) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.POSPRINTER_DEVCAT, device);
      Rs485POSPrinterMod4HandleImp hi = new Rs485POSPrinterMod4HandleImp(hk, device);
      hi.setHandle(this.handleFactory.createPOSPrinterHandle(hi));
      return hi;
   }

   public HandleImp createScale(SioDevice device, int scaleType) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.SCALE_DEVCAT, device);
      Rs485ScaleHandleImp hi = new Rs485ScaleHandleImp(hk, device, scaleType);
      hi.setHandle(this.handleFactory.createScaleHandle(hi));
      return hi;
   }

   public HandleImp createScanner(SioDevice device, int devId) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.SCANNER_DEVCAT, device);
      Rs485ScannerHandleImp hi = new Rs485ScannerHandleImp(hk, device, devId);
      hi.setHandle(this.handleFactory.createScannerHandle(hi));
      return hi;
   }

   public HandleImp create4610CDHandleImp(SioDevice device, IBMPrinterComposite printerComposite, int number) throws HandleFactoryException {
      DevCat devCat = DevCats.CASHDRAWER_DEVCAT;
      HandleKey hk = new Rs485HandleKey(
         devCat, this.getDeviceNumber(devCat, device) + 4, device.getSioSlot().getSioSlotNumber(), device.getPortNumber(), device.getDeviceNumber()
      );
      IBM4610CashDrawerImp cdLinker = new IBM4610CashDrawerImp(hk, printerComposite, number);
      Rs485POSPrinterCashDrawerHandleImp hi = new Rs485POSPrinterCashDrawerHandleImp(hk, device, cdLinker);
      cdLinker.setHandleImp(hi);
      hi.setHandle(this.handleFactory.createCashDrawerHandle(hi));
      printerComposite.addDevice(hk, cdLinker);
      return hi;
   }

   public HandleImp create4610CSHandleImp(SioDevice device, IBMPrinterComposite printerComposite) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.CHECKSCANNER_DEVCAT, device);
      IBM4610CheckScannerImp csLinker = new IBM4610CheckScannerImp(hk, printerComposite);
      Rs485POSPrinterCheckScannerHandleImp hi = new Rs485POSPrinterCheckScannerHandleImp(hk, device, csLinker);
      csLinker.setHandleImp(hi);
      hi.setHandle(this.handleFactory.createCheckScannerHandle(hi));
      printerComposite.addDevice(hk, csLinker);
      return hi;
   }

   public HandleImp create4610MICRHandleImp(SioDevice device, IBMPrinterComposite printerComposite) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.MICR_DEVCAT, device);
      IBM4610MICRImp mLinker = new IBM4610MICRImp(hk, printerComposite);
      Rs485POSPrinterMICRHandleImp hi = new Rs485POSPrinterMICRHandleImp(hk, device, mLinker);
      mLinker.setHandleImp(hi);
      hi.setHandle(this.handleFactory.createMICRHandle(hi));
      printerComposite.addDevice(hk, mLinker);
      return hi;
   }

   public HandleImp create4610TIHandleImp(SioDevice device, IBMPrinterComposite printerComposite) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.TONEINDICATOR_DEVCAT, device);
      IBMPrinterToneIndicatorImp ti = new IBM4610SSTToneIndicatorImp(printerComposite);
      Rs485POSPrinterToneIndicatorHandleImp hi = new Rs485POSPrinterToneIndicatorHandleImp(hk, device, ti);
      hi.setHandle(this.handleFactory.createToneIndicatorHandle(hi));
      printerComposite.addDevice(hk, hi);
      return hi;
   }

   public Rs4854610PrinterHandleImp create4610POSPrinter(SioDevice device, IBM4610PrinterHandleImp.PrinterInfo type) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.POSPRINTER_DEVCAT, device);
      Rs4854610PrinterHandleImp hi = new Rs4854610PrinterHandleImp(hk, device, type);
      hi.setHandle(this.handleFactory.createPOSPrinterHandle(hi));
      return hi;
   }

   public Rs4854689PrinterHandleImp create4689POSPrinter(SioDevice device) throws HandleFactoryException {
      HandleKey hk = this.createRs485HandleKey(DevCats.POSPRINTER_DEVCAT, device);
      Rs4854689PrinterHandleImp hi = new Rs4854689PrinterHandleImp(hk, device);
      hi.setHandle(this.handleFactory.createPOSPrinterHandle(hi));
      return hi;
   }

   protected HandleKey createRs485HandleKey(DevCat devCat, SioDevice device) {
      return new Rs485HandleKey(
         devCat, this.getDeviceNumber(devCat, device), device.getSioSlot().getSioSlotNumber(), device.getPortNumber(), device.getDeviceNumber()
      );
   }

   protected int getDeviceNumber(DevCat devCat, SioDevice device) {
      String key = devCat.toString() + Util.toHexString(device.getDeviceNumber());
      int number = 0;
      if (this.numberTable.containsKey(key)) {
         number = (Integer)this.numberTable.get(key);
         number++;
      }

      this.numberTable.put(key, new Integer(number));
      return number;
   }
}
