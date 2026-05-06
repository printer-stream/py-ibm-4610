package com.ibm.posj.util;

import com.ibm.posj.HandleCmd;

public class RetryHelper {
   private HandleCmd lastCmd = null;
   private boolean isCmdPending = false;
   private Object cmdPending = null;
   private byte[] lastResponse = new byte[0];

   public void setLastCmd(HandleCmd cmd) {
      this.lastCmd = cmd;
   }

   public HandleCmd getLastCmd() {
      return this.lastCmd;
   }

   public void setCmdPending(boolean value) {
      this.isCmdPending = value;
   }

   public boolean isCmdPending() {
      return this.isCmdPending;
   }

   public void waitCmdPending(long timeout) throws InterruptedException {
      synchronized (this.getLockCmdPending()) {
         this.getLockCmdPending().wait(timeout);
      }
   }

   public void notifyAllCmdPending() {
      synchronized (this.getLockCmdPending()) {
         this.getLockCmdPending().notifyAll();
      }
   }

   public void setLastResponse(byte[] response) {
      this.lastResponse = response;
   }

   public byte[] getLastResponse() {
      return this.lastResponse;
   }

   private Object getLockCmdPending() {
      if (this.cmdPending == null) {
         this.cmdPending = new Object();
      }

      return this.cmdPending;
   }
}
