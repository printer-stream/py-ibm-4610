package com.ibm.posj.ram;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

public class RamFileTable {
   private Hashtable fileTable = new Hashtable();
   public static final String FILETABLE_ERROR_NAME_NOT_VALID = "Invalid file name value";
   public static final String FILETABLE_ERROR_NAME_DOES_NOT_EXIST = "File name does not exist";
   public static final String FILETABLE_ERROR_NAME_ALREADY_EXISTS = "File name already exists";
   public static final String FILETABLE_ERROR_INVALID_INDEX = "Invalid file index value";
   public static final String FILETABLE_ERROR_INVALID_HANDLE = "Invalid file handle value";
   public static final String FILETABLE_ERROR_INVALID_POSITION = "Invalid file position value";

   protected RamFileTable() {
   }

   protected synchronized int getNumberOfFiles() {
      return this.fileTable.size();
   }

   protected synchronized void add(int handle, String name, int size, int position, boolean errorDetection) throws IllegalArgumentException, UnsupportedOperationException {
      this.checkUniqueNameArgument(name);
      Integer key = new Integer(handle);
      RamFile file = new RamFile(name, size, position, errorDetection);
      this.fileTable.put(key, file);
   }

   protected synchronized void remove(int handle) throws IllegalArgumentException {
      this.checkHandleArgument(handle);
      Integer key = new Integer(handle);
      this.fileTable.remove(key);
   }

   protected synchronized void clear() {
      this.fileTable.clear();
   }

   protected synchronized int getHandleByName(String name) throws IllegalArgumentException {
      this.checkNameArgument(name);
      boolean fileFound = false;
      int handle = 0;
      RamFile fileElement = null;
      Integer key = null;
      Iterator keyList = this.fileTable.keySet().iterator();

      while (keyList.hasNext() && !fileFound) {
         key = (Integer)keyList.next();
         fileElement = (RamFile)this.fileTable.get(key);
         if (fileFound = name.equals(fileElement.getName())) {
            handle = key;
         }
      }

      if (!fileFound) {
         throw new IllegalArgumentException("File name does not exist");
      } else {
         return handle;
      }
   }

   protected synchronized int getHandleByIndex(int index) throws IllegalArgumentException {
      this.checkIndexArgument(index);
      int handle = 0;
      Integer key = null;
      int elementPosition = this.getNumberOfFiles() - 1 - index;
      Iterator keyList = this.fileTable.keySet().iterator();

      for (int fileNumber = 0; fileNumber <= elementPosition; fileNumber++) {
         key = (Integer)keyList.next();
      }

      return key;
   }

   protected synchronized RamFile getFile(int fileHandle) throws IllegalArgumentException {
      this.checkHandleArgument(fileHandle);
      Integer key = new Integer(fileHandle);
      RamFile ramFile = (RamFile)this.fileTable.get(key);
      if (ramFile == null) {
         throw new IllegalArgumentException("Invalid file handle value");
      } else {
         return ramFile;
      }
   }

   protected synchronized boolean claim(int fileHandle, long timeout) throws IllegalArgumentException {
      this.checkHandleArgument(fileHandle);
      boolean claimed = false;
      RamFile ramFile = this.getFile(fileHandle);
      return ramFile.claim(timeout);
   }

   protected synchronized boolean release(int fileHandle) {
      this.checkHandleArgument(fileHandle);
      boolean released = false;
      RamFile ramFile = this.getFile(fileHandle);
      return ramFile.release();
   }

   protected synchronized int getClaimedFiles() {
      Enumeration e = this.fileTable.elements();
      int claimed = 0;

      while (e.hasMoreElements()) {
         if (((RamFile)e.nextElement()).isClaimed()) {
            claimed++;
         }
      }

      return claimed;
   }

   protected void checkUniqueNameArgument(String name) throws UnsupportedOperationException {
      this.checkNameArgument(name);
      boolean repeatedName = false;
      RamFile fileElement = null;
      Iterator fileList = this.fileTable.values().iterator();

      while (fileList.hasNext() && !repeatedName) {
         fileElement = (RamFile)fileList.next();
         repeatedName = name.equals(fileElement.getName());
      }

      if (repeatedName) {
         throw new UnsupportedOperationException("File name already exists");
      }
   }

   private void checkNameArgument(String name) throws UnsupportedOperationException {
      if (name == null) {
         throw new IllegalArgumentException("Invalid file name value");
      }
   }

   private void checkIndexArgument(int index) {
      if (index < 0 || index >= this.getNumberOfFiles()) {
         throw new IllegalArgumentException("Invalid file index value");
      }
   }

   private void checkHandleArgument(int handle) {
      if (handle < 0) {
         throw new IllegalArgumentException("Invalid file handle value");
      }
   }
}
