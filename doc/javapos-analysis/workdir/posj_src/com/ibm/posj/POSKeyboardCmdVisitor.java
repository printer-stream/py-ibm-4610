package com.ibm.posj;

public interface POSKeyboardCmdVisitor {
   void visitConfigCmd(POSKeyboardCmd.ConfigCmd var1);

   void visitIndicatorCmd(POSKeyboardCmd.IndicatorCmd var1);

   void visitEnableCmd(POSKeyboardCmd.EnableCmd var1);
}
