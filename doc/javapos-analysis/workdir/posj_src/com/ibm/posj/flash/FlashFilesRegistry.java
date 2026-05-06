package com.ibm.posj.flash;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.Handle;
import com.ibm.posj.bus.FlashHandleImp;
import com.ibm.posj.util.DevCats;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class FlashFilesRegistry {
   private Map registry = new HashMap();
   private Map flashFileNames;
   private FlashFileFactory flashFileFactory;
   private String[] buses = new String[]{"RS485", "RS232", "USB"};
   private String fs = System.getProperty("file.separator");
   private String flashFilesRootPath;
   private Tracer tracer = TracerFactory.getInstance().createTracer("FLASHING", "FlashFileRegistry");

   public FlashFilesRegistry() {
      this.flashFileFactory = new DefaultFlashFileFactory();
      this.flashFilesRootPath = FlashPathFinder.getInstance().getFilePath();
   }

   public List getFlashFiles(Handle handle) throws FlashException {
      if (!this.registry.containsKey(handle)) {
         FlashHandleImp flashHandleImp = handle.getHandleImp().getFlashHandleImp();
         this.registry.put(handle, this.createFiles(flashHandleImp.getFlashFileNames(handle), handle));
      }

      return (List)this.registry.get(handle);
   }

   private List createFiles(String[] flashFileNames, Handle handle) throws FlashException {
      List files = new Vector();

      for (int i = 0; i < flashFileNames.length; i++) {
         String absolutePath = this.flashFilesRootPath + this.fs + this.getBusDirectory(handle, flashFileNames[i]) + this.fs + flashFileNames[i];

         try {
            files.add(this.flashFileFactory.createFlashFile(absolutePath));
         } catch (Exception var7) {
            if (this.tracer.isOn()) {
               this.tracer.print(flashFileNames[i] + "not found");
            }
         }
      }

      return files;
   }

   private String getBusDirectory(Handle handle, String flashFileName) {
      if (handle.getDevBus().getName().equalsIgnoreCase("usb")) {
         return flashFileName.endsWith(".hex") && handle.getDevCat().equals(DevCats.POSPRINTER_DEVCAT) ? "rs485" : "usb";
      } else {
         return "rs485";
      }
   }

   private Map getRegistry() {
      if (this.registry == null) {
         this.registry = new HashMap();
      }

      return this.registry;
   }

   private Object getFlashFileNames(Object[] flashFileNames) {
      Map map = new HashMap();

      for (int i = 0; i < this.buses.length; i++) {
         map.put(this.buses[i], flashFileNames[i]);
      }

      return map;
   }
}
