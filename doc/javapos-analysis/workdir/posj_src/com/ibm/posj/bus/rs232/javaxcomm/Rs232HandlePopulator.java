package com.ibm.posj.bus.rs232.javaxcomm;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.HandleFactory;
import com.ibm.posj.HandleRegistry;
import com.ibm.posj.PosException;
import com.ibm.posj.PosSystem;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.bus.AbstractHandlePopulator;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import java.util.Enumeration;
import java.util.Iterator;
import jpos.config.JposEntry;

public class Rs232HandlePopulator extends AbstractHandlePopulator {
   private Rs232HandleImpFactory hiFactory = null;
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevBuses.RS232_DEVBUS.toString(), "Rs232HandlePopulator");
   public static final String RS232_HANDLE_POPULATOR_NAME_STRING = "RS232 Handle Populator";
   public static final String JPOSENTRIES_FILENAME = "com.ibm.posj.bus.rs232.jposEntriesFileName";

   public Rs232HandlePopulator(HandleRegistry registry, HandleFactory factory, PosSystem.EventHelper eventHelper) {
      super(registry, factory, eventHelper);
   }

   public void start() throws PosException {
      if (!this.isStarted()) {
         super.start();
         this.init();
      }
   }

   public void stop() {
      if (this.isStarted()) {
         ;
      }
   }

   public String getName() {
      return "RS232 Handle Populator";
   }

   public DevBus getDevBus() {
      return DevBuses.RS232_DEVBUS;
   }

   public void addRs232Device(JposEntry jposEntry) {
      Iterator imps = this.getRs232HandleImpFactory().createHandleImps(jposEntry).iterator();

      while (imps.hasNext()) {
         this.getHandleRegistry().addHandle(((HandleImp)imps.next()).getHandle());
      }
   }

   public String getJposEntriesFileName() {
      PosSystem.Properties p = PosSystemManager.getInstance().getProperties();
      String fn = "jpos.xml";
      if (p.isPropertyDefined("com.ibm.posj.bus.rs232.jposEntriesFileName")) {
         try {
            fn = p.getPropertyString("com.ibm.posj.bus.rs232.jposEntriesFileName");
         } catch (Exception var4) {
            this.tracer.print(var4);
         }
      }

      return fn;
   }

   protected void init() {
      Enumeration e = Rs232Util.getJposEntries(this.getJposEntriesFileName());
      if (!e.hasMoreElements()) {
         if (this.tracer.isOn()) {
            this.tracer.println(" There are no JposEntries to populate");
         }
      } else {
         while (e.hasMoreElements()) {
            JposEntry element = (JposEntry)e.nextElement();
            this.addRs232Device(element);
         }
      }
   }

   protected Rs232HandleImpFactory getRs232HandleImpFactory() {
      if (this.hiFactory == null) {
         this.hiFactory = new Rs232HandleImpFactory(this.getHandleFactory());
      }

      return this.hiFactory;
   }
}
