package com.ibm.posj.ram;

public class FileBlock {
   private int position = 0;
   private int size = 0;
   private boolean available = true;
   private int nextBlockPosition = -1;
   private boolean endOfChain = true;
   public static final int BLOCK_INITIAL_POSITION = 0;
   public static final int BLOCK_INITIAL_SIZE = 0;
   public static final boolean BLOCK_INITIAL_AVAILABILITY = true;
   public static final boolean BLOCK_INITIAL_EOC = true;
   public static final int BLOCK_POSITION_EMPTY = -1;
   public static final String BLOCK_ERROR_INVALID_POSITION = "Invalid block position value";
   public static final String BLOCK_ERROR_INVALID_SIZE = "Invalid block size value";

   protected FileBlock(int position, int size, boolean available, int nextBlockPosition, boolean endOfChain) throws IllegalArgumentException {
      this.setPosition(position);
      this.setSize(size);
      this.setAvailable(available);
      this.setNextBlockPosition(nextBlockPosition);
      this.setEndOfChain(endOfChain);
   }

   public String toString() {
      return "Position: "
         + this.getPosition()
         + " "
         + "Size: "
         + this.getSize()
         + " "
         + "Available: "
         + this.isAvailable()
         + " "
         + "EndOfChain: "
         + this.isEndOfChain()
         + " "
         + "Next Block: "
         + this.getNextBlockPosition();
   }

   protected int getPosition() {
      return this.position;
   }

   protected int getSize() {
      return this.size;
   }

   protected int getNextBlockPosition() {
      return this.nextBlockPosition;
   }

   protected boolean isAvailable() {
      return this.available;
   }

   protected boolean isEndOfChain() {
      return this.endOfChain;
   }

   protected void setAvailable(boolean available) {
      this.available = available;
   }

   protected void setEndOfChain(boolean endOfChain) {
      this.endOfChain = endOfChain;
   }

   protected void setSize(int size) throws IllegalArgumentException {
      this.checkSizeArgument(size);
      this.size = size;
   }

   protected void setNextBlockPosition(int nextBlockPosition) throws IllegalArgumentException {
      this.checkNextPositionBlockArgument(nextBlockPosition);
      this.nextBlockPosition = nextBlockPosition;
   }

   protected void setPosition(int position) throws IllegalArgumentException {
      this.checkPositionArgument(position);
      this.position = position;
   }

   protected void reset() {
      this.position = 0;
      this.size = 0;
      this.available = true;
      this.nextBlockPosition = -1;
      this.endOfChain = true;
   }

   protected void delete() {
      this.available = true;
      this.nextBlockPosition = -1;
      this.endOfChain = true;
   }

   protected void update(int position, int size, boolean available, int nextBlockPosition, boolean endOfChain) throws IllegalArgumentException {
      this.setPosition(position);
      this.setSize(size);
      this.setAvailable(available);
      this.setNextBlockPosition(nextBlockPosition);
      this.setEndOfChain(endOfChain);
   }

   private void checkNextPositionBlockArgument(int position) throws IllegalArgumentException {
      if (this.nextBlockPosition < 0 && this.nextBlockPosition != -1) {
         throw new IllegalArgumentException("Invalid block position value");
      }
   }

   private void checkPositionArgument(int position) throws IllegalArgumentException {
      if (position < 0) {
         throw new IllegalArgumentException("Invalid block position value");
      }
   }

   private void checkSizeArgument(int size) throws IllegalArgumentException {
      if (size < 0) {
         throw new IllegalArgumentException("Invalid block size value");
      }
   }
}
