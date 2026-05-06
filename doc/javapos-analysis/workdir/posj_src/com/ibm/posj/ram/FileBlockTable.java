package com.ibm.posj.ram;

import java.util.HashMap;
import java.util.Iterator;

class FileBlockTable {
   private HashMap blockHashTable = new HashMap();
   private boolean fragmentedMemory = false;
   private int allocatedData = 0;
   public static final boolean BLOCKTABLE_INITIAL_FRAGMENTED_MEMORY_VALUE = false;
   public static final int BLOCKTABLE_INITIAL_POSITION_POSITION_VALUE = 0;
   public static final int BLOCKTABLE_INITIAL_ALLOCATED_DATA = 0;
   public static final String BLOCKTABLE_ERROR_INVALID_POSITION_VALUE = "Invalid position value";
   public static final String BLOCKTABLE_ERROR_NOT_ENOUGH_MEMORY = "Memory could not be allocated";

   protected FileBlockTable() {
   }

   protected synchronized void clear() {
      this.blockHashTable.clear();
      this.allocatedData = 0;
   }

   protected synchronized int getAllocatedData() {
      return this.allocatedData;
   }

   protected synchronized int getNumberOfBlocks() {
      return this.blockHashTable.size();
   }

   protected synchronized void add(int position, int size, boolean available, int nextPosition, boolean endOfChain) {
      Integer key = new Integer(position);
      FileBlock fileBlock = new FileBlock(position, size, available, nextPosition, endOfChain);
      this.blockHashTable.put(key, fileBlock);
   }

   protected FileBlock get(int position) throws IllegalArgumentException {
      Integer key = new Integer(position);
      return (FileBlock)this.blockHashTable.get(key);
   }

   protected synchronized void delete(int position) {
      this.get(position).delete();
   }

   protected synchronized void remove(int position) {
      Integer key = new Integer(position);
      this.blockHashTable.remove(key);
   }

   protected synchronized boolean isFragmented() {
      return this.fragmentedMemory;
   }

   protected synchronized int allocate(int size) throws IllegalAccessException {
      FileBlock fileBlock = null;
      FileBlock preFileBlock = null;
      int blockChainSize = 0;
      int bytesRequired = 0;
      int bytesNotRequired = 0;
      int initialPosition = 0;
      boolean fileCompleted = false;
      Iterator blockList = this.blockHashTable.values().iterator();

      while (blockList.hasNext() && !fileCompleted) {
         fileBlock = (FileBlock)blockList.next();
         if (fileBlock.isAvailable()) {
            if (preFileBlock == null) {
               initialPosition = fileBlock.getPosition();
               this.setFragmentation(false);
            } else {
               preFileBlock.setNextBlockPosition(fileBlock.getPosition());
               this.setFragmentation(true);
            }

            preFileBlock = fileBlock;
            bytesRequired = size - blockChainSize;
            if (bytesRequired > fileBlock.getSize()) {
               bytesRequired = fileBlock.getSize();
            }

            bytesNotRequired = fileBlock.getSize() - bytesRequired;
            blockChainSize += bytesRequired;
            fileCompleted = blockChainSize >= size;
            fileBlock.update(fileBlock.getPosition(), bytesRequired, false, -1, fileCompleted);
            if (fileCompleted && bytesNotRequired > 0) {
               this.add(fileBlock.getPosition() + bytesRequired, bytesNotRequired, true, -1, true);
            }
         }
      }

      if (fileCompleted) {
         this.incAllocatedData(size);
         return initialPosition;
      } else {
         boolean allFilesDeleted = false;
         int positionValue = initialPosition;

         while (!allFilesDeleted) {
            fileBlock = this.get(positionValue);
            fileBlock.setAvailable(false);
            positionValue = fileBlock.getNextBlockPosition();
            allFilesDeleted = fileBlock.isEndOfChain() || positionValue == -1;
         }

         throw new IllegalAccessException("Memory could not be allocated");
      }
   }

   protected synchronized void free(int position) {
      FileBlock fileBlock = null;
      int blockPositionIterator = position;
      int freedData = 0;
      boolean endOfBlocks = false;
      boolean checkNextAvailableBlock = false;

      while (!endOfBlocks) {
         fileBlock = this.get(blockPositionIterator);
         if (fileBlock != null) {
            blockPositionIterator = fileBlock.getNextBlockPosition();
            endOfBlocks = fileBlock.isEndOfChain();
            freedData += fileBlock.getSize();
            fileBlock.delete();
         } else {
            endOfBlocks = true;
         }
      }

      this.decAllocatedData(freedData);
   }

   protected synchronized void defragment() {
      FileBlock currFileBlock = null;
      FileBlock nextFileBlock = null;
      int currPosition = 0;
      int nextPosition = 0;
      boolean endOfTable = false;

      while (!endOfTable) {
         currFileBlock = this.get(currPosition);
         if (currFileBlock != null) {
            nextPosition = currFileBlock.getPosition() + currFileBlock.getSize();
            if (currFileBlock.isAvailable()) {
               nextFileBlock = this.get(nextPosition);
               if (nextFileBlock != null) {
                  if (nextFileBlock.isAvailable()) {
                     int newBlockSize = currFileBlock.getSize() + nextFileBlock.getSize();
                     currFileBlock.setSize(newBlockSize);
                     this.remove(nextPosition);
                  } else {
                     currPosition = nextFileBlock.getPosition() + nextFileBlock.getSize();
                  }
               } else {
                  endOfTable = true;
               }
            } else {
               currPosition = nextPosition;
            }
         } else {
            endOfTable = true;
         }
      }
   }

   protected synchronized HashMap getHashTable() {
      return this.blockHashTable;
   }

   private void setFragmentation(boolean fragmentedMemory) {
      this.fragmentedMemory = fragmentedMemory;
   }

   private void incAllocatedData(int size) {
      this.allocatedData += size;
   }

   private void decAllocatedData(int size) {
      this.allocatedData -= size;
   }
}
