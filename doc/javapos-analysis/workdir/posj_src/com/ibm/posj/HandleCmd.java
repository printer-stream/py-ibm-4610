package com.ibm.posj;

public interface HandleCmd {
   String getName();

   int getCode();

   boolean isCompleted();

   void setCompleted(boolean var1);

   HandleCmd.Result getResult();

   void setResult(HandleCmd.Result var1);

   byte[] toBytes();

   void waitUntilCompleted();

   void waitUntilCompleted(long var1);

   void setCallback(HandleCmd.Callback var1);

   HandleCmd.Callback getCallback();

   void recycle();

   void accept(HandleCmdVisitor var1);

   void clean();

   public interface Callback {
      void execute(HandleCmd var1);
   }

   public interface Factory {
      void recycle(HandleCmd var1);
   }

   public interface Result {
      HandleException getHandleException();

      int getErrorCode();

      boolean isInError();

      void setInError(boolean var1);
   }
}
