package com.ibm.hid.util;

import com.ibm.hid.HidAsync;
import com.ibm.hid.HidException;

public abstract class DefaultHidAsync implements HidAsync {
   private HidException hidException = null;
   private boolean completed = false;

   private DefaultHidAsync() {
   }

   public HidException getHidException() {
      return this.hidException;
   }

   public void setHidException(HidException exception) {
      this.hidException = exception;
   }

   public boolean isCompleted() {
      return this.completed;
   }

   public void setCompleted(boolean c) {
      this.completed = c;
   }

   public void setCompleted() {
      this.completed = true;
   }

   public abstract void accept(HidAsync.AsyncVisitor var1);

   public abstract static class DefaultAsyncDescriptor extends DefaultHidAsync implements HidAsync.AsyncDescriptor {
      private byte descriptorType = 0;
      private byte descriptorIndex = 0;
      private byte[] descriptor = null;
      private short descriptorLength = 0;

      public byte getDescriptorType() {
         return this.descriptorType;
      }

      public void setDescriptorType(byte type) {
         this.descriptorType = type;
      }

      public byte getDescriptorIndex() {
         return this.descriptorIndex;
      }

      public void setDescriptorIndex(byte index) {
         this.descriptorIndex = index;
      }

      public byte[] getDescriptor() {
         return this.descriptor;
      }

      public void setDescriptor(byte[] d) {
         this.descriptor = d;
      }

      public short getDescriptorLength() {
         return this.descriptorLength;
      }

      public void setDescriptorLength(short length) {
         this.descriptorLength = length;
      }
   }

   public static class DefaultAsyncGetDescriptor extends DefaultHidAsync.DefaultAsyncDescriptor implements HidAsync.AsyncGetDescriptor {
      public void accept(HidAsync.AsyncVisitor asyncVisitor) {
         asyncVisitor.visitAsyncGetDescriptor(this);
      }
   }

   public static class DefaultAsyncGetIdle extends DefaultHidAsync.DefaultAsyncIdle implements HidAsync.AsyncGetIdle {
      public void accept(HidAsync.AsyncVisitor asyncVisitor) {
         asyncVisitor.visitAsyncGetIdle(this);
      }
   }

   public static class DefaultAsyncGetProtocol extends DefaultHidAsync.DefaultAsyncProtocol implements HidAsync.AsyncGetProtocol {
      public void accept(HidAsync.AsyncVisitor asyncVisitor) {
         asyncVisitor.visitAsyncGetProtocol(this);
      }
   }

   public static class DefaultAsyncGetReport extends DefaultHidAsync.DefaultAsyncReport implements HidAsync.AsyncGetReport {
      public void accept(HidAsync.AsyncVisitor asyncVisitor) {
         asyncVisitor.visitAsyncGetReport(this);
      }
   }

   public abstract static class DefaultAsyncIdle extends DefaultHidAsync implements HidAsync.AsyncIdle {
      private byte reportID = 0;
      private byte idleSetting = 0;

      public byte getReportID() {
         return this.reportID;
      }

      public void setReportID(byte id) {
         this.reportID = id;
      }

      public byte getIdleSetting() {
         return this.idleSetting;
      }

      public void setIdleSetting(byte setting) {
         this.idleSetting = setting;
      }
   }

   public abstract static class DefaultAsyncProtocol extends DefaultHidAsync implements HidAsync.AsyncProtocol {
      private byte protocol = 0;

      public byte getProtocol() {
         return this.protocol;
      }

      public void setProtocol(byte protocol) {
         this.protocol = protocol;
      }
   }

   public abstract static class DefaultAsyncReport extends DefaultHidAsync implements HidAsync.AsyncReport {
      private byte reportType = 0;
      private byte reportID = 0;
      private byte[] report = null;
      private short reportLength = 0;

      public byte getReportType() {
         return this.reportType;
      }

      public void setReportType(byte type) {
         this.reportType = type;
      }

      public byte getReportID() {
         return this.reportID;
      }

      public void setReportID(byte id) {
         this.reportID = id;
      }

      public byte[] getReport() {
         return this.report;
      }

      public void setReport(byte[] r) {
         this.report = r;
      }

      public short getReportLength() {
         return this.reportLength;
      }

      public void setReportLength(short length) {
         this.reportLength = length;
      }
   }

   public static class DefaultAsyncSetDescriptor extends DefaultHidAsync.DefaultAsyncDescriptor implements HidAsync.AsyncSetDescriptor {
      public void accept(HidAsync.AsyncVisitor asyncVisitor) {
         asyncVisitor.visitAsyncSetDescriptor(this);
      }
   }

   public static class DefaultAsyncSetIdle extends DefaultHidAsync.DefaultAsyncIdle implements HidAsync.AsyncSetIdle {
      public void accept(HidAsync.AsyncVisitor asyncVisitor) {
         asyncVisitor.visitAsyncSetIdle(this);
      }
   }

   public static class DefaultAsyncSetProtocol extends DefaultHidAsync.DefaultAsyncProtocol implements HidAsync.AsyncSetProtocol {
      public void accept(HidAsync.AsyncVisitor asyncVisitor) {
         asyncVisitor.visitAsyncSetProtocol(this);
      }
   }

   public static class DefaultAsyncSetReport extends DefaultHidAsync.DefaultAsyncReport implements HidAsync.AsyncSetReport {
      public void accept(HidAsync.AsyncVisitor asyncVisitor) {
         asyncVisitor.visitAsyncSetReport(this);
      }
   }

   public static class DefaultAsyncVisitor implements HidAsync.AsyncVisitor {
      public void visitAsyncGetDescriptor(HidAsync.AsyncGetDescriptor asyncGetDescriptor) {
      }

      public void visitAsyncSetDescriptor(HidAsync.AsyncSetDescriptor asyncSetDescriptor) {
      }

      public void visitAsyncGetReport(HidAsync.AsyncGetReport asyncGetReport) {
      }

      public void visitAsyncSetReport(HidAsync.AsyncSetReport asyncSetReport) {
      }

      public void visitAsyncGetIdle(HidAsync.AsyncGetIdle asyncGetIdle) {
      }

      public void visitAsyncSetIdle(HidAsync.AsyncSetIdle asyncSetIdle) {
      }

      public void visitAsyncGetProtocol(HidAsync.AsyncGetProtocol asyncGetProtocol) {
      }

      public void visitAsyncSetProtocol(HidAsync.AsyncSetProtocol asyncSetProtocol) {
      }
   }
}
