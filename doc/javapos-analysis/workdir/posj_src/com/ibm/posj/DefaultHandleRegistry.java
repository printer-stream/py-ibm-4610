package com.ibm.posj;

import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DefaultHandleRegistry implements HandleRegistry {
   private List handleList = new ArrayList();
   private HashMap devBusMap = new HashMap();
   private HashMap handleMap = new HashMap();

   public DefaultHandleRegistry() {
      this.initDevBusMap();
      this.initDevCatMap();
   }

   private void initDevBusMap() {
      for (int i = 0; i < DevBuses.DEVBUS_ARRAY.length; i++) {
         this.devBusMap.put(DevBuses.DEVBUS_ARRAY[i], new HashMap());
      }
   }

   private void initDevCatMap() {
      for (HashMap devCatsMap : this.devBusMap.values()) {
         for (int i = 0; i < DevCats.DEVCAT_ARRAY.length; i++) {
            devCatsMap.put(DevCats.DEVCAT_ARRAY[i], new ArrayList());
         }
      }
   }

   private void add(Handle handle) {
      List devCatList = (List)((HashMap)this.devBusMap.get(handle.getDevBus())).get(handle.getDevCat());
      devCatList.add(handle);
      this.handleMap.put(handle.getHandleKey(), handle);
   }

   private void remove(Handle handle) {
      List devCatList = (List)((HashMap)this.devBusMap.get(handle.getDevBus())).get(handle.getDevCat());
      devCatList.remove(handle);
      this.handleMap.remove(handle.getHandleKey());
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("<HandleRegistry size=\"" + this.getSize() + "\">\n");

      for (int i = 0; i < DevCats.DEVCAT_ARRAY.length; i++) {
         DevCat devCat = DevCats.DEVCAT_ARRAY[i];
         if (this.getNumberOfHandles(devCat) > 0) {
            Iterator handles = this.getHandles(devCat);
            int count = 0;
            sb.append("\t<DevCat name=\"" + devCat + "\" numberOfHandlesForDevCat=\"" + this.getNumberOfHandles(devCat) + "\">\n");

            while (handles.hasNext()) {
               Handle handle = (Handle)handles.next();
               sb.append("\t    <Handle number=\"" + count + "\" key=\"" + handle.getHandleKey() + "\"/>\n");
               count++;
            }

            sb.append("\t</DevCat>\n");
         }
      }

      sb.append("</HandleRegistry>\n");
      return sb.toString();
   }

   public void addHandle(Handle handle) {
      if (!this.containsHandle(handle)) {
         this.handleList.add(handle);
         this.add(handle);
      }
   }

   public void removeHandle(Handle handle) {
      this.handleList.remove(handle);
      this.remove(handle);
   }

   public void removeHandle(HandleKey handleKey) {
      this.removeHandle((Handle)this.handleMap.get(handleKey));
   }

   public boolean containsHandle(Handle handle) {
      return this.handleList.contains(handle);
   }

   public boolean containsHandle(HandleKey handleKey) {
      return this.containsHandle((Handle)this.handleMap.get(handleKey));
   }

   public Handle getHandle(HandleKey handleKey) {
      return (Handle)this.handleMap.get(handleKey);
   }

   public Iterator getHandles() {
      List newList = new LinkedList(this.handleList);
      return newList.iterator();
   }

   public int getSize() {
      return this.handleList.size();
   }

   public boolean isEmpty() {
      return this.handleList.isEmpty();
   }

   public Iterator getHandles(DevCat devCat) {
      List list = new ArrayList();

      for (int i = 0; i < DevBuses.DEVBUS_ARRAY.length; i++) {
         List devCatList = (List)((HashMap)this.devBusMap.get(DevBuses.DEVBUS_ARRAY[i])).get(devCat);
         list.addAll(devCatList);
      }

      return list.iterator();
   }

   public Iterator getHandles(DevBus devBus) {
      List list = new ArrayList();
      HashMap devCatsMap = (HashMap)this.devBusMap.get(devBus);
      if (devCatsMap == null) {
         return null;
      } else {
         Iterator devCatLists = devCatsMap.values().iterator();

         while (devCatLists.hasNext()) {
            list.addAll((List)devCatLists.next());
         }

         return list.iterator();
      }
   }

   public Iterator getHandles(DevCat devCat, DevBus devBus) {
      HashMap devCatsMap = (HashMap)this.devBusMap.get(devBus);
      List devCatList = (List)devCatsMap.get(devCat);
      if (devCatList == null) {
         return null;
      } else {
         List newList = new LinkedList(devCatList);
         return newList.iterator();
      }
   }

   public int getNumberOfHandles(DevCat devCat) {
      int totalSize = 0;

      for (int i = 0; i < DevBuses.DEVBUS_ARRAY.length; i++) {
         List devCatList = (List)((HashMap)this.devBusMap.get(DevBuses.DEVBUS_ARRAY[i])).get(devCat);
         totalSize += devCatList.size();
      }

      return totalSize;
   }

   public int getNumberOfHandles(DevBus devBus) {
      int totalSize = 0;
      Iterator devCatLists = ((HashMap)this.devBusMap.get(devBus)).values().iterator();

      while (devCatLists.hasNext()) {
         totalSize += ((List)devCatLists.next()).size();
      }

      return totalSize;
   }

   public int getNumberOfHandles(DevCat devCat, DevBus devBus) {
      return ((List)((HashMap)this.devBusMap.get(devBus)).get(devCat)).size();
   }

   public Iterator getHandlesDevCats() {
      List list = new ArrayList();

      for (int i = 0; i < DevCats.DEVCAT_ARRAY.length; i++) {
         if (this.getNumberOfHandles(DevCats.DEVCAT_ARRAY[i]) > 0) {
            list.add(DevCats.DEVCAT_ARRAY[i]);
         }
      }

      return list.iterator();
   }

   public Iterator getHandlesDevBuses() {
      List list = new ArrayList();

      for (int i = 0; i < DevBuses.DEVBUS_ARRAY.length; i++) {
         if (this.getNumberOfHandles(DevBuses.DEVBUS_ARRAY[i]) > 0) {
            list.add(DevBuses.DEVBUS_ARRAY[i]);
         }
      }

      return list.iterator();
   }
}
