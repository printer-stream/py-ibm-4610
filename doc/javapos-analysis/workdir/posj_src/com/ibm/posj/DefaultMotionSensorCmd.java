package com.ibm.posj;

public class DefaultMotionSensorCmd extends AbstractHandleCmd implements MotionSensorCmd {
   private String name = "";
   private int code = 0;
   protected HandleCmd.Result result = null;
   protected byte[] byteArray = new byte[0];

   DefaultMotionSensorCmd(HandleCmd.Factory factory, String name, int code) {
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
      visitor.visitMotionSensorCmd(this);
   }

   public HandleCmd.Result getResult() {
      if (this.result == null) {
         this.result = new AbstractHandleCmd.DefaultResult();
      }

      return this.result;
   }

   static class Factory extends DefaultSystemCmd.Factory implements MotionSensorCmd.Factory {
   }
}
