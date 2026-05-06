package com.ibm.posj;

abstract class DefaultMICRCmd extends AbstractHandleCmd implements MICRCmd {
   private String name = "";
   private int code = 0;
   protected byte[] byteArray = new byte[0];
   private DefaultMICRCmd.Factory factory = new DefaultMICRCmd.Factory();

   DefaultMICRCmd(HandleCmd.Factory factory, String name, int code) {
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
      visitor.visitMICRCmd(this);
   }

   public abstract void accept(MICRCmdVisitor var1) throws HandleException;

   public static class BeginInsertionCmd extends DefaultMICRCmd implements MICRCmd.BeginInsertionCmd {
      public BeginInsertionCmd(HandleCmd.Factory factory, String name, int code) {
         super(factory, "BeginInsertion", 502);
      }

      public void accept(MICRCmdVisitor v) throws HandleException {
         v.visitBeginInsertionCmd(this);
      }
   }

   public static class BeginRemovalCmd extends DefaultMICRCmd implements MICRCmd.BeginRemovalCmd {
      public BeginRemovalCmd(HandleCmd.Factory factory, String name, int code) {
         super(factory, "BeginRemoval", 504);
      }

      public void accept(MICRCmdVisitor v) throws HandleException {
         v.visitBeginRemovalCmd(this);
      }
   }

   public static class EndInsertionCmd extends DefaultMICRCmd implements MICRCmd.EndInsertionCmd {
      public EndInsertionCmd(HandleCmd.Factory factory, String name, int code) {
         super(factory, "EndInsertion", 503);
      }

      public void accept(MICRCmdVisitor v) throws HandleException {
         v.visitEndInsertionCmd(this);
      }
   }

   public static class EndRemovalCmd extends DefaultMICRCmd implements MICRCmd.EndRemovalCmd {
      public EndRemovalCmd(HandleCmd.Factory factory, String name, int code) {
         super(factory, "EndRemoval", 505);
      }

      public void accept(MICRCmdVisitor v) throws HandleException {
         v.visitEndRemovalCmd(this);
      }
   }

   public static class Factory extends DefaultSystemCmd.Factory implements MICRCmd.Factory, HandleConst {
      public MICRCmd createBeginInsertionCmd() {
         return new DefaultMICRCmd.BeginInsertionCmd(this, "BeginInsertion", 502);
      }

      public MICRCmd createEndInsertionCmd() {
         return new DefaultMICRCmd.EndInsertionCmd(this, "EndInsertion", 503);
      }

      public MICRCmd createBeginRemovalCmd() {
         return new DefaultMICRCmd.BeginRemovalCmd(this, "BeginRemoval", 504);
      }

      public MICRCmd createEndRemovalCmd() {
         return new DefaultMICRCmd.EndRemovalCmd(this, "EndInsertion", 505);
      }

      public void recycle(HandleCmd cmd) {
         cmd.clean();
      }
   }
}
