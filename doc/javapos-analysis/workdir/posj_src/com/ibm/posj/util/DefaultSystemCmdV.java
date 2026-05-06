package com.ibm.posj.util;

import com.ibm.posj.SystemCmd;
import com.ibm.posj.SystemCmdVisitor;

public class DefaultSystemCmdV implements SystemCmdVisitor {
   public void visitStatisticCmd(SystemCmd.StatisticCmd cmd) {
   }

   public void visitTestSystemCmd(SystemCmd.TestRequestCmd cmd) {
   }

   public void visitStatusSystemCmd(SystemCmd.StatusRequestCmd cmd) {
   }

   public void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
   }

   public void visitResetSystemCmd(SystemCmd.ResetRequestCmd cmd) {
   }

   public void visitDirectWriteCmd(SystemCmd.DirectWriteCmd cmd) {
   }
}
