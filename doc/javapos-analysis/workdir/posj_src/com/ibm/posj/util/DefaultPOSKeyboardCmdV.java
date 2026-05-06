package com.ibm.posj.util;

import com.ibm.posj.POSKeyboardCmd;
import com.ibm.posj.POSKeyboardCmdVisitor;

public class DefaultPOSKeyboardCmdV extends DefaultSystemCmdV implements POSKeyboardCmdVisitor {
   public void visitConfigCmd(POSKeyboardCmd.ConfigCmd cmd) {
   }

   public void visitIndicatorCmd(POSKeyboardCmd.IndicatorCmd cmd) {
   }

   public void visitEnableCmd(POSKeyboardCmd.EnableCmd cmd) {
   }
}
