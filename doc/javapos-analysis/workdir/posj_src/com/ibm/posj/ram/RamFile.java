package com.ibm.posj.ram;

public class RamFile {
   private int size = 0;
   private String name = "";
   private boolean errorDetection = false;
   private int position = -1;
   private boolean claimed = false;
   public static final int FILE_CLAIM_WAIT_FOREVER = -1;
   public static final int FILE_INITIAL_SIZE = 0;
   public static final String FILE_INITIAL_NAME = "";
   public static final boolean FILE_INITIAL_ERROR_DETECTION = false;
   public static final boolean FILE_INITIAL_CLAIM_STATE = false;
   public static final int FILE_NOT_ALLOCATED = -1;
   public static final String FILE_ERROR_INVALID_SIZE = "Invalid file size value";
   public static final String FILE_ERROR_INVALID_POSITION = "Invalid file position value";
   public static final String FILE_ERROR_INVALID_NAME = "Invalid file name value";
   public static final String FILE_ERROR_INVALID_TIMEOUT = "Invalid timeout value";

   public RamFile(String name, int size, int position, boolean errorDetection) throws IllegalArgumentException {
      this.setName(name);
      this.setSize(size);
      this.setPosition(position);
      this.setErrorDetection(errorDetection);
   }

   public String getName() {
      return this.name;
   }

   public int getSize() {
      return this.size;
   }

   public int getPosition() {
      return this.position;
   }

   public boolean isErrorDetectionActive() {
      return this.errorDetection;
   }

   public boolean isClaimed() {
      return this.claimed;
   }

   public synchronized boolean claim(long timeOut) throws IllegalArgumentException {
      boolean claimed = false;
      this.checkTimeoutArgument(timeOut);
      if (this.isClaimed()) {
         try {
            if (timeOut > 0L) {
               this.wait(timeOut);
            } else if (timeOut == -1L) {
               this.wait(0L);
            }
         } catch (InterruptedException var5) {
         }
      }

      if (!this.isClaimed()) {
         this.setClaimStatus(true);
         claimed = true;
      }

      return claimed;
   }

   public synchronized boolean release() {
      boolean released = false;
      if (this.isClaimed()) {
         this.setClaimStatus(false);
         this.notify();
         released = true;
      }

      return released;
   }

   public synchronized void setName(String name) {
      this.checkNameArgument(name);
      this.name = name;
   }

   private void setSize(int size) throws IllegalArgumentException {
      this.checkSizeArgument(size);
      this.size = size;
   }

   private void setErrorDetection(boolean errorDetection) {
      this.errorDetection = errorDetection;
   }

   private void setClaimStatus(boolean claimed) {
      this.claimed = claimed;
   }

   private void setPosition(int position) throws IllegalArgumentException {
      this.checkPositionArgument(position);
      this.position = position;
   }

   private void checkSizeArgument(int size) throws IllegalArgumentException {
      if (size < 0) {
         throw new IllegalArgumentException("Invalid file size value");
      }
   }

   private void checkPositionArgument(int position) throws IllegalArgumentException {
      if (position < 0 && position != -1) {
         throw new IllegalArgumentException("Invalid file position value");
      }
   }

   private void checkNameArgument(String name) throws UnsupportedOperationException {
      if (name == null) {
         throw new IllegalArgumentException("Invalid file name value");
      }
   }

   private void checkTimeoutArgument(long timeout) throws IllegalArgumentException {
      if (timeout < 0L && timeout != -1L) {
         throw new IllegalArgumentException("Invalid timeout value");
      }
   }
}
