package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleKey;
import com.ibm.posj.flash.FlashException;
import com.ibm.posj.flash.FlashFile;
import com.ibm.posj.flash.FlashFormat;
import com.ibm.posj.util.DevCats;

public class HidFlash4689PrtHandleImp extends HidFlashPrinterHandleImp {
   private static final String PRINTER_4689_FILENAME = "aip4689.hex";

   public HidFlash4689PrtHandleImp(HandleKey key, HidDevice device) {
      super(key, device);
      this.setTracer(TracerFactory.getInstance().createTracer("FLASH", "HidFlash4689PrtHandleImp"));
   }

   public void getPrinterInfo(Handle handle, FlashFile file) throws FlashException {
      if (handle.getDevCat().toString().equals(DevCats.POSPRINTER_DEVCAT.toString())) {
         ;
      }
   }

   class MCDownload4689 implements HidFlashPrinterHandleImp.MCDownload {
      public void doFlash() throws FlashException {
         if (HidFlash4689PrtHandleImp.this.getRs485FlashFile().getFilename().endsWith("aip4689.hex")) {
            HidFlash4689PrtHandleImp.this.getTracer()
               .println(
                  "MCDownload4689 - doFlash() firmware: "
                     + HidFlash4689PrtHandleImp.this.getRs485FlashFile().getFilename()
                     + " is not allowed for TI8 printers!"
               );
         } else {
            HidFlash4689PrtHandleImp.this.getRs485FlashFile().load();
            HidFlash4689PrtHandleImp.this.currentEraseCmd = HidFlash4689PrtHandleImp.this.eraseFirmwareHSCmd;
            HidFlash4689PrtHandleImp.this.currentLoadCmd = HidFlash4689PrtHandleImp.this.loadFirmwareHSCmd;
            FlashFormat ff = HidFlash4689PrtHandleImp.this.getRs485FlashFile().getFlashFormat();
            HidFlash4689PrtHandleImp.this.getTracer().println("MCDownload4689 - doFlash() -download4689Firmware()");
            HidFlash4689PrtHandleImp.this.downloadFirmware(ff);
            HidFlash4689PrtHandleImp.this.getTracer().println("MCDownload4689 - doFlash() -eraseCodeSector()");
            HidFlash4689PrtHandleImp.this.eraseCodeSector();
         }
      }
   }
}
