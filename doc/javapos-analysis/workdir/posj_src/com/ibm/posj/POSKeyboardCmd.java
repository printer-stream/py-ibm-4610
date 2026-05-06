package com.ibm.posj;

import com.ibm.posj.kbd.POSKeyboardConfig;

public interface POSKeyboardCmd extends HandleCmd {
   void accept(POSKeyboardCmdVisitor var1);

   void setCmdBytes(byte[] var1);

   public interface ConfigCmd extends POSKeyboardCmd {
      POSKeyboardConfig getConfig();
   }

   public interface EnableCmd extends POSKeyboardCmd {
      boolean getEnable();
   }

   public interface Factory extends SystemCmd.Factory {
      POSKeyboardCmd.ConfigCmd createConfigCmd(POSKeyboardConfig var1);

      POSKeyboardCmd.IndicatorCmd createIndicatorCmd(byte var1, boolean var2);

      POSKeyboardCmd.EnableCmd createEnableCmd(boolean var1);
   }

   public interface IndicatorCmd extends POSKeyboardCmd {
      byte getIndicator();

      boolean getAction();
   }
}
