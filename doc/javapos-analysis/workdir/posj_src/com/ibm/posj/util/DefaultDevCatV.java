package com.ibm.posj.util;

public class DefaultDevCatV implements DevCatVisitor {
   protected void defaultVisit(DevCat devCat) {
   }

   public void visitUnknown(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitComposite(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitBumpBar(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitCashDrawer(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitCheckScanner(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitCAT(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitCoinDispenser(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitFiscalPrinter(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitHardTotals(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitKeylock(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitLineDisplay(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitMICR(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitMSR(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitMotionSensor(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitPinpad(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitPOSKeyboard(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitPOSPower(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitPOSPrinter(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitRemoteOrderDisplay(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitScale(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitScanner(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitSignatureCapture(DevCat devCat) {
      this.defaultVisit(devCat);
   }

   public void visitToneIndicator(DevCat devCat) {
      this.defaultVisit(devCat);
   }
}
