package com.ibm.posj;

import com.ibm.posj.kbd.POSKeyboardConfig;

abstract class DefaultPOSKeyboardCmd extends AbstractHandleCmd implements POSKeyboardCmd, POSKeyboardConst {
   private String name = "";
   private int code = 0;
   protected byte[] byteArray = new byte[0];

   DefaultPOSKeyboardCmd(HandleCmd.Factory factory, String name, int code) {
      super(factory);
      this.name = name;
      this.code = code;
   }

   public void accept(HandleCmdVisitor visitor) {
      visitor.visitPOSKeyboardCmd(this);
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

   public void setCmdBytes(byte[] cmd) {
      this.byteArray = cmd;
   }

   static class ConfigCmd extends DefaultPOSKeyboardCmd implements POSKeyboardCmd.ConfigCmd {
      private POSKeyboardConfig kbdConfig = null;

      ConfigCmd(HandleCmd.Factory factory, POSKeyboardConfig config) {
         super(factory, "CONFIG_POSKEYBOARD_CMD_NAME", 701);
         this.kbdConfig = config;
      }

      public void accept(POSKeyboardCmdVisitor visitor) {
         visitor.visitConfigCmd(this);
      }

      public POSKeyboardConfig getConfig() {
         return this.kbdConfig;
      }
   }

   static class EnableCmd extends DefaultPOSKeyboardCmd implements POSKeyboardCmd.EnableCmd {
      private boolean enable = false;

      EnableCmd(HandleCmd.Factory factory, boolean enable) {
         super(factory, "ENABLE_POSKEYBOARD_CMD_NAME", 703);
         this.enable = enable;
      }

      public void accept(POSKeyboardCmdVisitor visitor) {
         visitor.visitEnableCmd(this);
      }

      public boolean getEnable() {
         return this.enable;
      }
   }

   static class Factory extends DefaultSystemCmd.Factory implements POSKeyboardCmd.Factory {
      public POSKeyboardCmd.ConfigCmd createConfigCmd(POSKeyboardConfig config) {
         return new DefaultPOSKeyboardCmd.ConfigCmd(this, config);
      }

      public POSKeyboardCmd.IndicatorCmd createIndicatorCmd(byte indicator, boolean on) {
         return new DefaultPOSKeyboardCmd.IndicatorCmd(this, indicator, on);
      }

      public POSKeyboardCmd.EnableCmd createEnableCmd(boolean enable) {
         return new DefaultPOSKeyboardCmd.EnableCmd(this, enable);
      }
   }

   static class IndicatorCmd extends DefaultPOSKeyboardCmd implements POSKeyboardCmd.IndicatorCmd {
      private byte indicator = 0;
      private boolean action = false;

      IndicatorCmd(HandleCmd.Factory factory, byte indicator, boolean on) {
         super(factory, "INDICATOR_POSKEYBOARD_CMD_NAME", 702);
         this.indicator = indicator;
         this.action = on;
      }

      public void accept(POSKeyboardCmdVisitor visitor) {
         visitor.visitIndicatorCmd(this);
      }

      public byte getIndicator() {
         return this.indicator;
      }

      public boolean getAction() {
         return this.action;
      }
   }
}
