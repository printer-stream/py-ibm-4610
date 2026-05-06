package com.ibm.posj;

import com.ibm.jutil.DefaultUtilProperties;
import com.ibm.jutil.FileUtil;
import com.ibm.jutil.logging.DefaultLogHelper;
import com.ibm.jutil.logging.LogHelper;
import com.ibm.jutil.logging.NullLogHelper;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.bus.HandlePopulator;
import com.ibm.posj.event.PosSystemEvent;
import com.ibm.posj.event.PosSystemListener;
import com.ibm.posj.util.DefaultProperties;
import com.ibm.posj.util.DevBus;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class PosSystemManager implements PosSystem {
   private HandleRegistry handleRegistry = new PosSystemManager.PosSystemHandleRegistry();
   private HandleFactory handleFactory = new DefaultHandleFactory();
   private PosSystem.EventHelper eventHelper = new DefaultPosSystemEventHelper();
   private PosSystem.PopulatorRegistry populatorRegistry = new PosSystemManager.DefaultPopulatorRegistry();
   private PosSystem.Properties properties = new DefaultProperties();
   private LogHelper logHelper = null;
   private boolean started = false;
   private boolean isInit = false;
   private HashSet populatorSet = new HashSet();
   private static Tracer tracer = TracerFactory.getInstance().createTracer("POSJ", "PosSystemManager");
   private static String installDir = "";
   private static PosSystem instance = null;
   private static final LogHelper NULL_LOGHELPER = new NullLogHelper();

   protected PosSystemManager() {
   }

   protected void init() throws PosException {
      if (tracer.isOn()) {
         StringBuffer sb = new StringBuffer();
         sb.append("-->init()");
         sb.append("\n\tDate = " + DateFormat.getDateInstance(1).format(new Date()));
         sb.append("\n\tjava.fullversion = " + System.getProperty("java.fullversion"));
         sb.append("\n\t" + TracerFactory.getInstance());
         sb.append("\n\t<--init()");
         tracer.println(1, sb);
      }

      this.initProperties();
      this.initLogging();
      this.initHandlePopulatorsFromProperties();
      this.isInit = true;
   }

   protected boolean isInit() {
      return this.isInit;
   }

   protected void initProperties() {
      this.properties.loadProperties();
   }

   protected void initLogging() {
      DefaultUtilProperties props = new DefaultUtilProperties("Log.properties");
      props.loadProperties();
      if (props.isPropertyDefined("com.ibm.jutil.logging.LogStoreName")) {
         String logStoreValue = props.getPropertyString("com.ibm.jutil.logging.LogStoreName");
         if (!logStoreValue.equals("com.ibm.jutil.logging.JDKLogStore") && !logStoreValue.equals("com.ibm.jutil.logging.Log4jLogStore")) {
            if (tracer.isOn()) {
               tracer.println("no logger defined. Property com.ibm.jutil.logging.LogStoreName=" + logStoreValue);
            }

            this.logHelper = NULL_LOGHELPER;
         } else {
            this.logHelper = DefaultLogHelper.getInstance();
         }
      } else {
         this.logHelper = DefaultLogHelper.getInstance();
      }

      if (tracer.isOn()) {
         tracer.println("using loghelper = " + this.logHelper);
      }

      DefaultUtilProperties var3 = null;
   }

   protected void initHandlePopulatorsFromProperties() {
      try {
         List list = this.getHandlePopulatorClassNames(this.properties);
         String[] classNames = new String[list.size()];

         for (int i = 0; i < classNames.length; i++) {
            classNames[i] = (String)list.get(i);
         }

         HandlePopulator[] pops = this.createHandlePopulators(classNames);

         for (int i = 0; i < pops.length; i++) {
            HandlePopulator pop = pops[i];
            if (this.getPopulatorRegistry().containsPopulator(pop.getName())) {
               String msg = "PopulatorRegistry already contain HandlePopulator: " + pop.getName();
               throw new PosException(msg);
            }

            this.getPopulatorRegistry().addPopulator(pop);
         }
      } catch (Exception var7) {
         if (tracer.isOn()) {
            tracer.print(var7);
         }

         this.getLogHelper().addLogEntry(0, var7.getMessage(), "PosSystemManager");
      }
   }

   protected HandleFactory getHandleFactory() {
      return this.handleFactory;
   }

   private void startPopulators() throws PosException {
      if (tracer.isOn()) {
         this.trace("-->startPopulators()");
      }

      Iterator populators = this.getPopulatorRegistry().getPopulators();
      Exception lastException = null;
      HandlePopulator pop = null;

      while (populators.hasNext()) {
         try {
            pop = (HandlePopulator)populators.next();
            this.populatorSet.add(pop.getDevBus().toString());
            pop.start();
         } catch (PosException var5) {
            lastException = var5;
         }
      }

      if (lastException != null) {
         if (tracer.isOn()) {
            tracer.print(lastException);
         }

         this.getLogHelper().addLogEntry(1, lastException.getMessage(), "PosSystemManager");
         throw new PosException("Error while starting populators!", lastException);
      } else {
         if (tracer.isOn()) {
            this.trace("<--startPopulators()");
         }
      }
   }

   private void trace(String msg) {
      tracer.println(2, msg);
   }

   List getHandlePopulatorClassNames(PosSystem.Properties props) throws PosException {
      String opSys = System.getProperty("os.name");
      if (tracer.isOn()) {
         this.trace(".getHandlePopulatorClassNames(): using the OS: " + opSys);
      }

      Enumeration propValues;
      if (opSys.equalsIgnoreCase("Linux")) {
         if (tracer.isOn()) {
            this.trace(".getHandlePopulatorClassNames(): Loading Linux Populators");
         }

         propValues = this.properties.getPropertyValuesWithPattern("posj.bus.handle.populator.linux.class");
      } else if (opSys.equalsIgnoreCase("QNX")) {
         if (tracer.isOn()) {
            this.trace(".getHandlePopulatorClassNames(): Loading QNX Populators");
         }

         propValues = this.properties.getPropertyValuesWithPattern("posj.bus.handle.populator.qnx.class");
      } else {
         if (tracer.isOn()) {
            this.trace(".getHandlePopulatorClassNames(): Loading default Populators");
         }

         propValues = this.properties.getPropertyValuesWithPattern("posj.bus.handle.populator.class");
      }

      List list = new ArrayList();

      while (propValues.hasMoreElements()) {
         String className = (String)propValues.nextElement();
         if (className == null) {
            String msg = "Found a null HandlePopulator class name";
            if (tracer.isOn()) {
               this.trace(msg);
            }

            throw new PosException(msg);
         }

         if (className.equals("")) {
            String msg = "Found and empty HandlePopulator class name";
            if (tracer.isOn()) {
               this.trace(msg);
            }

            throw new PosException(msg);
         }

         list.add(className);
      }

      return list;
   }

   HandlePopulator[] createHandlePopulators(String[] classNames) throws PosException {
      if (classNames == null) {
         throw new PosException("Invalid HandlePopulator class names");
      } else {
         HandlePopulator[] handlePops = null;

         try {
            handlePops = new HandlePopulator[classNames.length];

            for (int i = 0; i < classNames.length; i++) {
               String className = classNames[i];
               HandlePopulator handlePop = this.createHandlePopulator(className);
               handlePops[i] = handlePop;
            }

            return handlePops;
         } catch (Exception var6) {
            String msg = "Error while creating HandlePopulator: Exception.msg =" + var6.getMessage();
            if (tracer.isOn()) {
               tracer.print(msg);
            }

            this.getLogHelper().addLogEntry(0, msg, "PosSystemManager");
            throw new PosException(msg, var6);
         }
      }
   }

   HandlePopulator createHandlePopulator(String className) throws PosException {
      HandlePopulator handlePop = null;

      try {
         Class handlePopClass = Class.forName(className);
         Class[] argsClasses = new Class[]{
            class$com$ibm$posj$HandleRegistry == null
               ? (class$com$ibm$posj$HandleRegistry = class$("com.ibm.posj.HandleRegistry"))
               : class$com$ibm$posj$HandleRegistry,
            class$com$ibm$posj$HandleFactory == null
               ? (class$com$ibm$posj$HandleFactory = class$("com.ibm.posj.HandleFactory"))
               : class$com$ibm$posj$HandleFactory,
            class$com$ibm$posj$PosSystem$EventHelper == null
               ? (class$com$ibm$posj$PosSystem$EventHelper = class$("com.ibm.posj.PosSystem$EventHelper"))
               : class$com$ibm$posj$PosSystem$EventHelper
         };
         Constructor threeArgCtor = handlePopClass.getConstructor(argsClasses);
         Object[] args = new Object[]{this.getHandleRegistry(), this.getHandleFactory(), this.getEventHelper()};
         return (HandlePopulator)threeArgCtor.newInstance(args);
      } catch (Exception var7) {
         throw new PosException("Could not create HandlePopulator instance from className=" + className, var7);
      }
   }

   void setPosSystemProperties(PosSystem.Properties props) throws PosException {
      if (this.isStarted()) {
         throw new PosException("PosSystemManager started cannot change!");
      } else if (props == null) {
         throw new PosException("PosSystem.Properties cannot be null!");
      } else {
         this.properties = props;
      }
   }

   void setHandleRegistry(HandleRegistry registry) throws PosException {
      if (this.isStarted()) {
         throw new PosException("PosSystemManager started cannot change!");
      } else if (registry == null) {
         throw new PosException("HandleRegistry cannot be null!");
      } else {
         this.handleRegistry = registry;
      }
   }

   void setHandleFactory(HandleFactory factory) throws PosException {
      if (this.isStarted()) {
         throw new PosException("PosSystemManager started cannot change!");
      } else if (factory == null) {
         throw new PosException("HandleFactory cannot be null!");
      } else {
         this.handleFactory = factory;
      }
   }

   void setEventHelper(PosSystem.EventHelper helper) throws PosException {
      if (this.isStarted()) {
         throw new PosException("PosSystemManager started cannot change!");
      } else if (helper == null) {
         throw new PosException("PosSystem.EventHelper cannot be null!");
      } else {
         this.eventHelper = helper;
      }
   }

   static void resetInstance() {
      instance = null;
   }

   public HandleRegistry getHandleRegistry() {
      return this.handleRegistry;
   }

   public synchronized void addPosSystemListener(PosSystemListener listener) {
      this.getEventHelper().addPosSystemListener(listener);
   }

   public synchronized void removePosSystemListener(PosSystemListener listener) {
      this.getEventHelper().removePosSystemListener(listener);
   }

   public int getListenerCount() {
      return this.getEventHelper().getListenerCount();
   }

   public PosSystem.PopulatorRegistry getPopulatorRegistry() {
      return this.populatorRegistry;
   }

   public PosSystem.EventHelper getEventHelper() {
      return this.eventHelper;
   }

   public PosSystem.Properties getProperties() {
      return this.properties;
   }

   public synchronized void start() throws PosException {
      if (!this.started) {
         if (!this.isInit()) {
            this.init();
         }

         this.startPopulators();
         this.started = true;
         if (tracer.isOn()) {
            this.trace(this.getPopulatorRegistry().toString());
            this.trace(this.getHandleRegistry().toString());
         }
      }
   }

   public boolean isStarted() {
      return this.started;
   }

   public synchronized void startPopulator(DevBus bus) throws PosException {
      if (tracer.isOn()) {
         this.trace("-->startPopulator(" + bus.toString() + ")");
      }

      if (!this.populatorSet.contains(bus.getName())) {
         if (!this.isInit()) {
            this.init();
         }

         Iterator populators = this.getPopulatorRegistry().getPopulators();
         HandlePopulator pop = null;
         boolean popStarted = true;

         while (populators.hasNext()) {
            try {
               pop = (HandlePopulator)populators.next();
               if (bus.getName().equalsIgnoreCase(pop.getDevBus().toString())) {
                  pop.start();
               }
            } catch (PosException var6) {
               if (tracer.isOn()) {
                  this.trace("Error while starting populators :" + pop.getName());
                  tracer.print(var6);
               }
            }

            if (!pop.isStarted() && popStarted) {
               popStarted = false;
            }
         }

         if (popStarted) {
            this.started = true;
         }

         this.populatorSet.add(bus.getName());
         if (tracer.isOn()) {
            this.trace(this.getPopulatorRegistry().toString());
            this.trace(this.getHandleRegistry().toString());
            this.trace("<--startPopulator(" + bus.toString() + ")");
         }
      }
   }

   public boolean isPopulatorStarted(DevBus bus) {
      return this.populatorSet.contains(bus.getName());
   }

   public LogHelper getLogHelper() {
      return this.logHelper == null ? NULL_LOGHELPER : this.logHelper;
   }

   public static String getInstallDir() {
      if (installDir == "") {
         if (tracer.isOn()) {
            tracer.println("-->getInstallDir() : searching for install dir...");
            tracer.println("using CLASSPATH = " + System.getProperty("java.class.path"));
         }

         if (FileUtil.isWindows()) {
            installDir = FileUtil.getClasspathDirectory("\\ibmjpos\\lib\\posj.jar");
         } else {
            installDir = FileUtil.getClasspathDirectory("/javapos/lib/posj.jar");
         }

         if (tracer.isOn()) {
            tracer.println("<--getInstallDir() : install dir = " + (installDir == null ? "null" : installDir));
         }
      }

      return installDir;
   }

   public static synchronized PosSystem getInstance() {
      if (instance == null) {
         instance = new PosSystemManager();
      }

      return instance;
   }

   class DefaultPopulatorRegistry implements PosSystem.PopulatorRegistry {
      private List populatorList = new ArrayList();

      public DefaultPopulatorRegistry() {
      }

      public synchronized String toString() {
         StringBuffer sb = new StringBuffer();
         Iterator pops = this.getPopulators();
         HandlePopulator pop = null;
         sb.append("<PosSystem.PopulatorRegistry>\n");

         while (pops.hasNext()) {
            pop = (HandlePopulator)pops.next();
            sb.append("\t<populator name=\"" + pop.getName() + "\", isStarted()== " + pop.isStarted() + "/>\n");
         }

         sb.append("\n<PosSystem.PopulatorRegistry>");
         return sb.toString();
      }

      public synchronized void addPopulator(HandlePopulator populator) {
         this.populatorList.add(populator);
      }

      public synchronized void removePopulator(HandlePopulator populator) {
         this.populatorList.remove(populator);
      }

      public synchronized boolean containsPopulator(HandlePopulator populator) {
         return this.populatorList.contains(populator);
      }

      public synchronized boolean containsPopulator(String populatorName) {
         for (HandlePopulator pop : this.populatorList) {
            if (pop.getName().equals(populatorName)) {
               return true;
            }
         }

         return false;
      }

      public synchronized Iterator getPopulators() {
         return this.populatorList.iterator();
      }

      public synchronized boolean isEmpty() {
         return this.populatorList.isEmpty();
      }

      public synchronized int getSize() {
         return this.populatorList.size();
      }
   }

   class PosSystemHandleRegistry extends DefaultHandleRegistry {
      public void addHandle(Handle handle) {
         super.addHandle(handle);
         PosSystemManager.this.getEventHelper().firePosDeviceAttached(new PosSystemEvent(handle));
      }

      public void removeHandle(Handle handle) {
         super.removeHandle(handle);
         PosSystemManager.this.getEventHelper().firePosDeviceDetached(new PosSystemEvent(handle));
      }

      public void removeHandle(HandleKey handleKey) {
         Handle handle = this.getHandle(handleKey);
         super.removeHandle(handleKey);
         if (null != handle) {
            PosSystemManager.this.getEventHelper().firePosDeviceDetached(new PosSystemEvent(handle));
         }
      }
   }
}
