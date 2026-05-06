package com.ibm.posj.util;

import com.ibm.jutil.tracing.Tracing;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class CommandManager {
   private static Hashtable clients = new Hashtable(5);
   private static List freeLists = new ArrayList(7);
   private static CommandManager instance = null;

   public String toString() {
      return super.toString() + " " + clients;
   }

   public void monitorCommand(Object client, Object command) {
      if (null != client && null != command) {
         List v;
         if (clients.containsKey(client)) {
            v = (List)clients.get(client);
         } else {
            v = this.getList();
            clients.put(client, v);
         }

         v.add(command);
         if (Tracing.isOn()) {
            Tracing.println("Client size " + clients.size());
            Tracing.println(client + " list size " + v.size());
         }
      }
   }

   public void clearCommands(Object client, List related) {
      List v = null;
      if (clients.containsKey(client)) {
         v = (List)clients.get(client);
      }

      if (null != v) {
         related.removeAll(v);
         v.clear();
         this.removeClient(client);
      }
   }

   public void removeCommand(Object client, Object command) {
      if (null != client) {
         if (null != command) {
            if (clients.containsKey(client)) {
               List v = (List)clients.get(client);
               v.remove(command);
               if (v.size() <= 0) {
                  this.removeClient(client);
               }

               if (Tracing.isOn()) {
               }
            }
         }
      }
   }

   public void removeClient(Object client) {
      if (clients.containsKey(client)) {
         List l = (List)clients.get(client);
         l.clear();
         this.recycleList(l);
         clients.remove(client);
      }
   }

   public void listRemove(Object client) {
      if (clients.containsKey(client)) {
         List l = (List)clients.get(client);

         try {
            for (List tl : l) {
               tl.remove(client);
            }
         } catch (Exception var5) {
         }

         l.clear();
         this.recycleList(l);
         clients.remove(client);
      }
   }

   public boolean hasClient(Object client) {
      return clients.containsKey(client);
   }

   public static CommandManager getInstance() {
      if (instance == null) {
         instance = new CommandManager();
      }

      return instance;
   }

   public void selfRemove(CommandManager.SelfRemover sr, Object client) {
      if (this.hasClient(client) && null != sr) {
         List l = (List)clients.get(client);
         sr.doRemove(client, l);
      }
   }

   protected synchronized List getList() {
      List list;
      if (0 >= freeLists.size()) {
         list = new ArrayList(20);
      } else {
         list = (List)freeLists.get(0);
      }

      if (null == list) {
         list = new ArrayList(20);
      }

      return list;
   }

   protected void recycleList(List r) {
      r.clear();
      freeLists.add(r);
   }

   public interface SelfRemover {
      void doRemove(Object var1, List var2);
   }
}
