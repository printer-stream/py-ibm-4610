package com.ibm.posj.util;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import jpos.config.JposEntry;
import jpos.config.JposEntryRegistry;
import jpos.loader.JposServiceLoader;

public class JclHelper {
   private JposEntryRegistry jposEntryRegistry = null;
   private Tracer tracer = TracerFactory.getInstance().createTracer("JclHelper");
   private static JclHelper instance = null;

   JclHelper() {
   }

   public static JclHelper getInstance() {
      if (instance == null) {
         instance = new JclHelper();
         instance.init();
      }

      return instance;
   }

   protected void init() {
      this.jposEntryRegistry = JposServiceLoader.getManager().getEntryRegistry();
   }

   public void reloadRegistry() {
      this.jposEntryRegistry.load();
   }

   public DevCat[] getConfiguredDevCats() {
      JposEntry[] entries = this.getConfiguredJposEntries();
      HashMap devCatMap = new HashMap();

      for (int i = 0; i < entries.length; i++) {
         String devCatString = (String)entries[i].getPropertyValue("deviceCategory");
         DevCat devCat = DevCats.getDevCatForName(devCatString);
         if (!devCat.equals(DevCats.UNKNOWN_DEVCAT)) {
            devCatMap.put(devCat.toString(), devCat);
         } else {
            this.tracer.println("Found UnknowDevCat in JCL");
         }
      }

      DevCat[] devCats = new DevCat[devCatMap.size()];
      Iterator iterator = devCatMap.values().iterator();
      int ix = 0;

      while (iterator.hasNext()) {
         devCats[ix++] = (DevCat)iterator.next();
      }

      return devCats;
   }

   public DevCat[] getConfiguredDevCats(String devBus) {
      JposEntry[] entries = this.getConfiguredJposEntries();
      HashMap devCatMap = new HashMap();

      for (int i = 0; i < entries.length; i++) {
         if (entries[i].hasPropertyWithValue(devBus)) {
            String devCatString = (String)entries[i].getPropertyValue("deviceCategory");
            DevCat devCat = DevCats.getDevCatForName(devCatString);
            devCatMap.put(devCat.toString(), devCat);
         }
      }

      DevCat[] devCats = new DevCat[devCatMap.size()];
      Iterator iterator = devCatMap.values().iterator();
      int ix = 0;

      while (iterator.hasNext()) {
         devCats[ix++] = (DevCat)iterator.next();
      }

      return devCats;
   }

   public String[] getLogicalNames(DevCat devCat) {
      JposEntry[] entries = this.getConfiguredJposEntries();
      List lNameList = new ArrayList();

      for (int i = 0; i < entries.length; i++) {
         String devCatString = (String)entries[i].getPropertyValue("deviceCategory");
         if (DevCats.getDevCatForName(devCatString).equals(devCat)) {
            lNameList.add(entries[i].getLogicalName());
         }
      }

      String[] lNames = new String[lNameList.size()];

      for (int ix = 0; ix < lNameList.size(); ix++) {
         lNames[ix] = (String)lNameList.get(ix);
      }

      return lNames;
   }

   public String[] getAllConfiguredLogicalNames() {
      JposEntry[] entries = this.getConfiguredJposEntries();
      String[] lNames = new String[entries.length];

      for (int i = 0; i < entries.length; i++) {
         lNames[i] = entries[i].getLogicalName();
      }

      return lNames;
   }

   public JposEntry[] getConfiguredJposEntries() {
      Enumeration entries = this.jposEntryRegistry.getEntries();
      List list = new ArrayList();

      while (entries.hasMoreElements()) {
         list.add(entries.nextElement());
      }

      JposEntry[] entryArray = new JposEntry[list.size()];

      for (int i = 0; i < list.size(); i++) {
         entryArray[i] = (JposEntry)list.get(i);
      }

      return entryArray;
   }

   public JposEntry[] getConfiguredJposEntries(String devBus) {
      JposEntry[] entries = this.getConfiguredJposEntries();
      HashMap jposEntryMap = new HashMap();

      for (int i = 0; i < entries.length; i++) {
         if (entries[i].hasPropertyWithValue(devBus) && this.isIbmJposEntry(entries[i])) {
            jposEntryMap.put(entries[i].getLogicalName(), entries[i]);
         }
      }

      JposEntry[] jposEntries = new JposEntry[jposEntryMap.size()];
      Iterator iterator = jposEntryMap.values().iterator();
      int ix = 0;

      while (iterator.hasNext()) {
         jposEntries[ix++] = (JposEntry)iterator.next();
      }

      return jposEntries;
   }

   public JposEntry[] getConfiguredJposEntries(String devBus, DevCat devCat) {
      JposEntry[] entries = this.getConfiguredJposEntries();
      HashMap jposEntryMap = new HashMap();

      for (int i = 0; i < entries.length; i++) {
         if (entries[i].hasPropertyWithValue(devBus) && entries[i].hasPropertyWithValue(devCat.toString())) {
            jposEntryMap.put(entries[i].getLogicalName(), entries[i]);
         }
      }

      JposEntry[] jposEntries = new JposEntry[jposEntryMap.size()];
      Iterator iterator = jposEntryMap.values().iterator();
      int ix = 0;

      while (iterator.hasNext()) {
         jposEntries[ix++] = (JposEntry)iterator.next();
      }

      return jposEntries;
   }

   public JposEntry getConfiguredJposEntry(String logicalName) {
      return this.jposEntryRegistry.getJposEntry(logicalName);
   }

   public static DevCat getDevCat(JposEntry entry) {
      Object obj = entry.getPropertyValue("deviceCategory");
      return DevCats.getDevCatForName((String)obj);
   }

   private boolean isIbmJposEntry(JposEntry entry) {
      return entry.getPropertyValue("vendorName").equals("IBM");
   }
}
