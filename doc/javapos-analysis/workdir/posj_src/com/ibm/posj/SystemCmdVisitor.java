package com.ibm.posj;

public interface SystemCmdVisitor {
   void visitTestSystemCmd(SystemCmd.TestRequestCmd var1);

   void visitStatusSystemCmd(SystemCmd.StatusRequestCmd var1);

   void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd var1);

   void visitResetSystemCmd(SystemCmd.ResetRequestCmd var1);

   void visitDirectWriteCmd(SystemCmd.DirectWriteCmd var1);

   void visitStatisticCmd(SystemCmd.StatisticCmd var1);
}
