package com.ibm.posj.flash;

import java.io.File;

public class DefaultFlashFileFactory implements FlashFileFactory {
   public FlashFile createFlashFile(String fileName) throws FlashException {
      File mf = new File(fileName);
      if (mf.isFile()) {
         String devBus = mf.getParent().toLowerCase();
         if (devBus.endsWith("usb")) {
            return new UsbFlashFile(fileName);
         }

         if (devBus.endsWith("rs485")) {
            String x = fileName.toLowerCase();
            if (x.endsWith("dat")) {
               return new PrinterFlashFile4689(fileName);
            }

            return new Rs485FlashFile(fileName);
         }
      }

      throw new FlashException(fileName + " is not a valid filename");
   }
}
