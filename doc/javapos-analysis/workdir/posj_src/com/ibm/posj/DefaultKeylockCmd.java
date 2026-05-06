package com.ibm.posj;

abstract class DefaultKeylockCmd extends AbstractHandleCmd implements KeylockCmd {
   private String name = "";
   private int code = 0;
   protected byte[] byteArray = new byte[0];
   public static final int DEFAULT_STATUS_REQUEST_SLEEP_TIME = 75;

   DefaultKeylockCmd(HandleCmd.Factory factory, String name, int code) {
      super(factory);
      this.name = name;
      this.code = code;
   }

   public String getName() {
      return this.name;
   }

   public int getCode() {
      return this.code;
   }

   public byte[] toBytes() {
      return this.byteArray;
   }

   public void accept(HandleCmdVisitor visitor) {
      visitor.visitKeylockCmd(this);
   }

   static class Factory extends DefaultSystemCmd.Factory implements KeylockCmd.Factory {
      public KeylockCmd createGetKeyPositionCmd() {
         return new DefaultKeylockCmd.GetKeyPositionCmd(this);
      }
   }

   static class GetKeyPositionCmd extends DefaultKeylockCmd implements KeylockCmd.GetKeyPositionCmd {
      GetKeyPositionCmd(HandleCmd.Factory factory) {
         super(factory, "GET_KEY_POSITION", 300);
      }
   }
}
