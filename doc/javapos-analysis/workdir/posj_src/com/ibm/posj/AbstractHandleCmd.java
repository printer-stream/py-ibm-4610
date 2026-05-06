package com.ibm.posj;

public abstract class AbstractHandleCmd implements HandleCmd {
   private HandleCmd.Result result = null;
   private HandleCmd.Factory factory = null;
   private HandleCmd.Callback callback = null;
   private boolean completed = false;
   private Object lock = new Object();
   private int waiters = 0;

   AbstractHandleCmd(HandleCmd.Factory factory) {
      this.factory = factory;
   }

   public abstract String getName();

   public abstract int getCode();

   public abstract byte[] toBytes();

   public abstract void accept(HandleCmdVisitor var1);

   public boolean isCompleted() {
      return this.completed;
   }

   public void setCompleted(boolean b) {
      this.completed = b;
      if (this.isCompleted() && 0 < this.waiters) {
         synchronized (this.lock) {
            this.lock.notifyAll();
         }
      }
   }

   public HandleCmd.Result getResult() {
      if (this.result == null) {
         this.result = new AbstractHandleCmd.DefaultResult();
      }

      return this.result;
   }

   public void setResult(HandleCmd.Result result) {
      this.result = result;
   }

   public void waitUntilCompleted() {
      if (!this.isCompleted()) {
         synchronized (this.lock) {
            this.waiters++;

            while (!this.isCompleted()) {
               try {
                  this.lock.wait();
               } catch (InterruptedException var4) {
               }
            }

            this.waiters--;
         }
      }
   }

   public void waitUntilCompleted(long msec) {
      if (!this.isCompleted()) {
         synchronized (this.lock) {
            long now = System.currentTimeMillis();
            long start = now;
            this.waiters++;

            while (!this.isCompleted() && msec > 0L) {
               try {
                  this.lock.wait(msec);
               } catch (InterruptedException var10) {
               }

               now = System.currentTimeMillis();
               msec -= now - start;
               start = now;
            }

            this.waiters--;
         }
      }
   }

   public void setCallback(HandleCmd.Callback callback) {
      this.callback = callback;
   }

   public HandleCmd.Callback getCallback() {
      return this.callback;
   }

   public void recycle() {
      this.factory.recycle(this);
   }

   public void clean() {
   }

   public static class DefaultResult implements HandleCmd.Result {
      private HandleException handleException = null;
      private int errorCode = 0;
      private boolean inError = false;

      public HandleException getHandleException() {
         return this.handleException;
      }

      public int getErrorCode() {
         return this.errorCode;
      }

      public boolean isInError() {
         return this.inError;
      }

      public void setHandleException(HandleException hE) {
         this.handleException = hE;
      }

      public void setErrorCode(int eCode) {
         this.errorCode = eCode;
      }

      public void setInError(boolean b) {
         this.inError = b;
      }
   }

   static class Factory implements HandleCmd.Factory {
      public void recycle(HandleCmd cmd) {
         cmd.clean();
      }
   }
}
