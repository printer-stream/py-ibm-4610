package com.ibm.posj;

public interface ScaleCmdVisitor {
   void visitClearDisplayCmd(ScaleCmd.ClearDisplayCmd var1);

   void visitConfigScaleCmd(ScaleCmd.ConfigScaleCmd var1);

   void visitEnableExtendedStatusCmd(ScaleCmd.EnableExtendedStatusCmd var1);

   void visitReportConfigCmd(ScaleCmd.ReportConfigCmd var1);

   void visitWeightReqCmd(ScaleCmd.WeightReqCmd var1);

   void visitZeroScaleCmd(ScaleCmd.ZeroScaleCmd var1);
}
