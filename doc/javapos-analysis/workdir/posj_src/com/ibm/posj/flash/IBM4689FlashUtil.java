package com.ibm.posj.flash;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import java.util.Iterator;
import java.util.List;

public class IBM4689FlashUtil {
   private static Tracer tracer = TracerFactory.getInstance().createTracer("FLASH", "IBM4689FlashUtil");
   private static final String PRINTER_4689_FILENAME = "aip4689.hex";
   private static final String PRINTER_4689_USB_FILENAME = "aip4537.dat";
   private static final String PRINTER_4689_2_USB_FILENAME = "aip45372.dat";

   public static FlashFile findFlashFile(int printerType, int printerID, int printerFeatureByte1, int printerFeatureByte2, List flashFileList) throws FlashException {
      if (tracer.isOn()) {
         tracer.println("findFlashFile");
      }

      Iterator iterator = flashFileList.iterator();
      if (is4689Printer(printerID)) {
         while (iterator.hasNext()) {
            FlashFile currFlashFile = (FlashFile)iterator.next();
            if (currFlashFile instanceof UsbFlashFile
               && (currFlashFile.getFilename().endsWith("aip45372.dat") || currFlashFile.getFilename().endsWith("aip4537.dat"))) {
               return currFlashFile;
            }

            if (currFlashFile.getFilename().endsWith("aip4689.hex")) {
               return currFlashFile;
            }
         }
      }

      return null;
   }

   public static boolean is4689Printer(int id) {
      switch (id) {
         case 3834:
         case 3835:
            return true;
         default:
            return false;
      }
   }
}
