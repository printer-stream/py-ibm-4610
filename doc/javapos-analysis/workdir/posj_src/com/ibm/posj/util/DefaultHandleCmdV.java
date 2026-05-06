package com.ibm.posj.util;

import com.ibm.posj.CashDrawerCmd;
import com.ibm.posj.CheckScannerCmd;
import com.ibm.posj.FiscalPrinterCmd;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleCmdVisitor;
import com.ibm.posj.HardTotalsCmd;
import com.ibm.posj.KeylockCmd;
import com.ibm.posj.LineDisplayCmd;
import com.ibm.posj.MICRCmd;
import com.ibm.posj.MSRCmd;
import com.ibm.posj.MotionSensorCmd;
import com.ibm.posj.POSKeyboardCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.ScaleCmd;
import com.ibm.posj.ScannerCmd;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.ToneIndicatorCmd;

public class DefaultHandleCmdV implements HandleCmdVisitor {
   protected void visitHandleCmd(HandleCmd cmd) {
   }

   public void visitSystemCmd(SystemCmd cmd) {
      this.visitHandleCmd(cmd);
   }

   public void visitCashDrawerCmd(CashDrawerCmd cmd) {
      this.visitHandleCmd(cmd);
   }

   public void visitCheckScannerCmd(CheckScannerCmd cmd) {
      this.visitHandleCmd(cmd);
   }

   public void visitFiscalPrinterCmd(FiscalPrinterCmd cmd) {
      this.visitHandleCmd(cmd);
   }

   public void visitHardTotalsCmd(HardTotalsCmd cmd) {
      this.visitHandleCmd(cmd);
   }

   public void visitKeylockCmd(KeylockCmd cmd) {
      this.visitHandleCmd(cmd);
   }

   public void visitLineDisplayCmd(LineDisplayCmd cmd) {
      this.visitHandleCmd(cmd);
   }

   public void visitMICRCmd(MICRCmd cmd) {
      this.visitHandleCmd(cmd);
   }

   public void visitMSRCmd(MSRCmd cmd) {
      this.visitHandleCmd(cmd);
   }

   public void visitMotionSensorCmd(MotionSensorCmd cmd) {
      this.visitHandleCmd(cmd);
   }

   public void visitPOSKeyboardCmd(POSKeyboardCmd cmd) {
      this.visitHandleCmd(cmd);
   }

   public void visitPOSPrinterCmd(POSPrinterCmd cmd) {
      this.visitHandleCmd(cmd);
   }

   public void visitScaleCmd(ScaleCmd cmd) {
      this.visitHandleCmd(cmd);
   }

   public void visitScannerCmd(ScannerCmd cmd) {
      this.visitHandleCmd(cmd);
   }

   public void visitToneIndicatorCmd(ToneIndicatorCmd cmd) {
      this.visitHandleCmd(cmd);
   }
}
