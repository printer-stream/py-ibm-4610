package com.ibm.embedded;

import java.util.Iterator;

public class EmbeddedManager {
   private Iterator drivers = null;
   private static EmbeddedManager instance = null;

   protected EmbeddedManager() {
   }

   public static synchronized EmbeddedManager getInstance() {
      if (instance == null) {
         instance = new EmbeddedManager();
      }

      return instance;
   }

   public Iterator getDrivers() {
      if (this.drivers == null) {
         EmbeddedInitializer initializer = new EmbeddedInitializer();
         this.drivers = initializer.init();
      }

      return this.drivers;
   }

   public GetSlotInfo getSlotInfo() throws UnsatisfiedLinkError, EmbeddedException {
      return EmbeddedInitializer.getSlotInfo();
   }
}
