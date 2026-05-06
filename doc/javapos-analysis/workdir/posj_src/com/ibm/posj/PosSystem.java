package com.ibm.posj;

import com.ibm.jutil.logging.LogHelper;
import com.ibm.posj.bus.HandlePopulator;
import com.ibm.posj.event.PosSystemEvent;
import com.ibm.posj.event.PosSystemListener;
import com.ibm.posj.util.DevBus;
import java.util.Enumeration;
import java.util.Iterator;

public interface PosSystem {
   HandleRegistry getHandleRegistry();

   void addPosSystemListener(PosSystemListener var1);

   void removePosSystemListener(PosSystemListener var1);

   void start() throws PosException;

   boolean isStarted();

   void startPopulator(DevBus var1) throws PosException;

   boolean isPopulatorStarted(DevBus var1);

   LogHelper getLogHelper();

   PosSystem.PopulatorRegistry getPopulatorRegistry();

   PosSystem.EventHelper getEventHelper();

   PosSystem.Properties getProperties();

   int getListenerCount();

   public interface EventHelper {
      void addPosSystemListener(PosSystemListener var1);

      void removePosSystemListener(PosSystemListener var1);

      void firePosDeviceAttached(PosSystemEvent var1);

      void firePosDeviceDetached(PosSystemEvent var1);

      int getListenerCount();
   }

   public interface PopulatorRegistry {
      void addPopulator(HandlePopulator var1);

      void removePopulator(HandlePopulator var1);

      boolean containsPopulator(HandlePopulator var1);

      boolean containsPopulator(String var1);

      Iterator getPopulators();

      boolean isEmpty();

      int getSize();
   }

   public interface Properties {
      String POSJ_PROPERTIES_FILENAME = "posj.properties";
      String POSJ_HANDLEPOPULATOR_PROP_NAME = "posj.bus.handle.populator.class";
      String POSJ_HANDLEPOPULATOR_LINUX_PROP_NAME = "posj.bus.handle.populator.linux.class";
      String POSJ_HANDLEPOPULATOR_QNX_PROP_NAME = "posj.bus.handle.populator.qnx.class";

      void loadProperties();

      boolean isLoaded();

      Exception getLastException();

      String getPropertyString(String var1);

      boolean isPropertyDefined(String var1);

      Enumeration getPropertyNames();

      Enumeration getPropertyValuesWithPattern(String var1);
   }
}
