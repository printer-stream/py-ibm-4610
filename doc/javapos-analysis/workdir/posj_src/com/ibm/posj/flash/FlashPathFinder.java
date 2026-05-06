package com.ibm.posj.flash;

import com.ibm.jutil.FileUtil;
import com.ibm.jutil.UtilProperties;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.util.DefaultProperties;
import java.io.File;

public class FlashPathFinder {
   private Tracer tracer = TracerFactory.getInstance().createTracer("FLASH", "FlashPathFinder");
   private String fileName = null;
   private String fs = System.getProperty("file.separator");
   private final String _USB_FLASHDIR = "usb";
   private final String _485_FLASHDIR = "485";
   private final String WINDOWS_FLASH_DIR = this.fs + "ibmjpos" + this.fs + "res" + this.fs + "flash";
   private final String LINUX_FLASH_DIR = this.fs + "javapos" + this.fs + "flash";
   private String[] flashFileNames = new String[]{
      "res" + this.fs + "flash" + this.fs + "usb",
      "res" + this.fs + "flash" + this.fs + "485",
      "flash" + this.fs + "usb",
      "flash" + this.fs + "485",
      "com" + this.fs + "ibm" + this.fs + "posj" + this.fs + "res" + this.fs + "flash" + this.fs + "usb",
      "com" + this.fs + "ibm" + this.fs + "posj" + this.fs + "res" + this.fs + "flash" + this.fs + "485"
   };
   protected static FlashPathFinder instance = null;
   protected String filePath = null;

   public static FlashPathFinder getInstance() {
      if (instance == null) {
         instance = new FlashPathFinder();
         instance.setFilePath(instance.getFlashDir());
      }

      return instance;
   }

   protected FlashPathFinder() {
   }

   public String getFilePath() {
      return this.filePath;
   }

   void setFilePath(String path) {
      if (this.tracer.isOn()) {
         this.tracer.println(".setFilePath() : Flash directory to use: <" + path + ">");
      }

      this.filePath = path;
   }

   protected String getFlashDir() {
      String FlashDir = this.getFlashDirFromProperties();
      if (FlashDir == null) {
         FlashDir = this.getFlashDirFromInstallDir();
         if (!this.validateFlashDir(FlashDir)) {
            FlashDir = null;
         }

         if (FlashDir == null) {
            FlashDir = this.getFlashDirFromClasspath();
            if (!this.validateFlashDir(FlashDir)) {
               FlashDir = null;
            }
         }
      } else if (!this.validateFlashDir(FlashDir)) {
         FlashDir = null;
      }

      if (FlashDir != null) {
         FlashDir = FlashDir.replace('/', this.fs.charAt(0));
      }

      return FlashDir;
   }

   protected String getFlashDirFromInstallDir() {
      String opSys = System.getProperty("os.name");
      String path = PosSystemManager.getInstallDir();
      if (path != null) {
         if (opSys.equalsIgnoreCase("Linux")) {
            path = path + this.LINUX_FLASH_DIR;
         } else {
            path = path + this.WINDOWS_FLASH_DIR;
         }
      }

      return path;
   }

   protected String getFlashDirFromProperties() {
      UtilProperties prop = new DefaultProperties();
      prop.loadProperties();
      String opSys = System.getProperty("os.name");
      String path;
      if (opSys.equalsIgnoreCase("Linux")) {
         path = prop.getPropertyString("com.ibm.posj.flash.flashDirectory.linux");
      } else {
         path = prop.getPropertyString("com.ibm.posj.flash.flashDirectory.windows");
      }

      return path;
   }

   protected boolean validateFlashDir(String path) {
      File f = new File(path + this.fs + "usb");
      if (f.isAbsolute() && f.exists()) {
         return true;
      } else {
         f = new File(path + this.fs + "485");
         return f.isAbsolute() && f.exists();
      }
   }

   protected String getFlashDirFromClasspath() {
      String path = "";

      for (int i = 0; i < this.flashFileNames.length; i++) {
         if (FileUtil.locateFile(this.flashFileNames[i], true, false)) {
            this.fileName = this.flashFileNames[i];
            break;
         }
      }

      if (this.fileName != null) {
         File f1 = FileUtil.findFile(this.fileName, true);

         for (path = f1.getParent(); !path.endsWith("flash"); f1 = new File(path)) {
            path = f1.getParent();
         }
      }

      return path;
   }
}
