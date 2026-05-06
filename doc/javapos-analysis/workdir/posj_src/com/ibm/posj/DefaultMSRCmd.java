package com.ibm.posj;

abstract class DefaultMSRCmd extends AbstractHandleCmd implements MSRCmd {
   private String name = "";
   private int code = 0;
   protected byte[] byteArray = new byte[0];

   DefaultMSRCmd(HandleCmd.Factory factory, String name, int code) {
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
      visitor.visitMSRCmd(this);
   }

   static class ConfigMSRCmd extends DefaultMSRCmd implements MSRCmd.ConfigMSRCmd {
      boolean enableISOtrack1 = true;
      boolean enableISOtrack2 = true;
      boolean enableISOtrack3 = true;
      boolean enableJIS_IItrack = false;
      int timeout = 0;

      ConfigMSRCmd(HandleCmd.Factory factory, boolean enabletrack1, boolean enabletrack2, boolean enabletrack3, boolean enabletrack4, int timeout) {
         super(factory, "Config MSR", 600);
         this.enableISOtrack1 = enabletrack1;
         this.enableISOtrack2 = enabletrack2;
         this.enableISOtrack3 = enabletrack3;
         this.enableJIS_IItrack = enabletrack4;
         this.timeout = timeout;
      }

      public boolean isEnabledISOtrack1() {
         return this.enableISOtrack1;
      }

      public boolean isEnabledISOtrack2() {
         return this.enableISOtrack2;
      }

      public boolean isEnabledISOtrack3() {
         return this.enableISOtrack3;
      }

      public boolean isEnabledJIS_IItrack() {
         return this.enableJIS_IItrack;
      }

      public int getTimeout() {
         return this.timeout;
      }
   }

   static class Factory extends DefaultSystemCmd.Factory implements MSRCmd.Factory {
      public MSRCmd createConfigMSRCmd(boolean enableISOtrack1, boolean enableISOtrack2, boolean enableISOtrack3, boolean enableJIS_IItrack, int timeout) {
         return new DefaultMSRCmd.ConfigMSRCmd(this, enableISOtrack1, enableISOtrack2, enableISOtrack3, enableJIS_IItrack, timeout);
      }

      public MSRCmd createWriteMSRCmd(byte[] data) {
         return new DefaultMSRCmd.WriteMSRCmd(this, data);
      }
   }

   static class WriteMSRCmd extends DefaultMSRCmd implements MSRCmd.WriteMSRCmd {
      private byte[] data = new byte[69];

      WriteMSRCmd(HandleCmd.Factory factory, byte[] data) {
         super(factory, "Write MSR", 601);
         this.data = data;
      }

      public byte[] getData() {
         return this.data;
      }

      public void setData(byte[] b) throws IllegalArgumentException {
         this.data = b;
      }
   }
}
