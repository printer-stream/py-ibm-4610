package com.ibm.posj.printer.parser.ibm4689;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;

public class Printer4689PropertiesManager {
   private boolean bold = false;
   private boolean underline = false;
   private boolean doubleHigh = false;
   private boolean doubleWide = false;
   private boolean reverseVideo = false;
   private byte wideMagnification = 1;
   private byte lineHighMagnification = 1;
   private boolean normalMode = true;
   private static Tracer tracer = TracerFactory.getInstance().createTracer("POSPrinter", "4689PropertiesManager");

   public void reset() {
      if (tracer.isOn()) {
         this.trace("reseting properties");
      }

      this.bold = false;
      this.underline = false;
      this.doubleHigh = false;
      this.doubleWide = false;
      this.reverseVideo = false;
      this.normalMode = true;
      this.lineHighMagnification = 1;
      this.wideMagnification = 1;
   }

   public void setProperties(Printer4689PropertiesManager propMngr) {
      this.bold = propMngr.bold;
      this.doubleHigh = propMngr.doubleHigh;
      this.doubleWide = propMngr.doubleWide;
      this.reverseVideo = propMngr.reverseVideo;
      this.underline = propMngr.underline;
      this.normalMode = propMngr.normalMode;
   }

   public boolean isBold() {
      return this.bold;
   }

   public void setBold(boolean bold) {
      if (tracer.isOn()) {
         this.trace("seting bold");
      }

      this.setNormalMode(false);
      this.bold = bold;
   }

   public boolean isDoubleHigh() {
      return this.doubleHigh;
   }

   public void setDoubleHigh(boolean doubleHigh) {
      if (tracer.isOn()) {
         this.trace("seting DoubleHigh to " + doubleHigh);
      }

      this.setLineHighMagnification((byte)3);
      this.setNormalMode(false);
      this.doubleHigh = doubleHigh;
   }

   public boolean isDoubleWide() {
      return this.doubleWide;
   }

   public void setDoubleWide(boolean doubleWide) {
      if (tracer.isOn()) {
         this.trace("seting DoubleWide to = " + doubleWide);
      }

      this.setWideMagnification((byte)2);
      this.setNormalMode(false);
      this.doubleWide = doubleWide;
   }

   public boolean isDoubleHighWide() {
      return this.isDoubleHigh() && this.isDoubleWide();
   }

   public boolean isUnderline() {
      return this.underline;
   }

   public void setUnderline(boolean underline) {
      if (tracer.isOn()) {
         this.trace("seting underline");
      }

      this.setNormalMode(false);
      this.underline = underline;
   }

   public boolean isReverseVideo() {
      return this.reverseVideo;
   }

   public void setReverseVideo(boolean reverseVideo) {
      if (tracer.isOn()) {
         this.trace("seting reverseVideo");
      }

      this.setNormalMode(false);
      this.reverseVideo = reverseVideo;
   }

   public boolean isNormalMode() {
      return this.normalMode;
   }

   public void setNormalMode(boolean normalMode) {
      if (tracer.isOn()) {
         this.trace("seting normalMode to " + normalMode);
      }

      this.normalMode = normalMode;
   }

   public byte getLineHighMagnification() {
      return this.lineHighMagnification;
   }

   public void setLineHighMagnification(byte lineHighMagnification) {
      this.lineHighMagnification = lineHighMagnification;
   }

   public byte getWideMagnification() {
      return this.wideMagnification;
   }

   public void setWideMagnification(byte wideMagnification) {
      this.wideMagnification = wideMagnification;
   }

   protected void trace(String s) {
      tracer.println(3, s);
   }
}
