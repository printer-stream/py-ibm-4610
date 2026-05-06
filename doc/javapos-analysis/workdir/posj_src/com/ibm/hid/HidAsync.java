package com.ibm.hid;

public interface HidAsync {
   HidException getHidException();

   void setHidException(HidException var1);

   boolean isCompleted();

   void setCompleted(boolean var1);

   void setCompleted();

   void accept(HidAsync.AsyncVisitor var1);

   public interface AsyncDescriptor {
      byte getDescriptorType();

      void setDescriptorType(byte var1);

      byte getDescriptorIndex();

      void setDescriptorIndex(byte var1);

      byte[] getDescriptor();

      void setDescriptor(byte[] var1);

      short getDescriptorLength();

      void setDescriptorLength(short var1);
   }

   public interface AsyncGetDescriptor extends HidAsync, HidAsync.AsyncDescriptor {
   }

   public interface AsyncGetIdle extends HidAsync, HidAsync.AsyncIdle {
   }

   public interface AsyncGetProtocol extends HidAsync, HidAsync.AsyncProtocol {
   }

   public interface AsyncGetReport extends HidAsync, HidAsync.AsyncReport {
   }

   public interface AsyncIdle {
      byte getReportID();

      void setReportID(byte var1);

      byte getIdleSetting();

      void setIdleSetting(byte var1);
   }

   public interface AsyncProtocol {
      byte getProtocol();

      void setProtocol(byte var1);
   }

   public interface AsyncReport {
      byte getReportType();

      void setReportType(byte var1);

      byte getReportID();

      void setReportID(byte var1);

      byte[] getReport();

      void setReport(byte[] var1);

      short getReportLength();

      void setReportLength(short var1);
   }

   public interface AsyncSetDescriptor extends HidAsync, HidAsync.AsyncDescriptor {
   }

   public interface AsyncSetIdle extends HidAsync, HidAsync.AsyncIdle {
   }

   public interface AsyncSetProtocol extends HidAsync, HidAsync.AsyncProtocol {
   }

   public interface AsyncSetReport extends HidAsync, HidAsync.AsyncReport {
   }

   public interface AsyncVisitor {
      void visitAsyncGetDescriptor(HidAsync.AsyncGetDescriptor var1);

      void visitAsyncSetDescriptor(HidAsync.AsyncSetDescriptor var1);

      void visitAsyncGetReport(HidAsync.AsyncGetReport var1);

      void visitAsyncSetReport(HidAsync.AsyncSetReport var1);

      void visitAsyncGetIdle(HidAsync.AsyncGetIdle var1);

      void visitAsyncSetIdle(HidAsync.AsyncSetIdle var1);

      void visitAsyncGetProtocol(HidAsync.AsyncGetProtocol var1);

      void visitAsyncSetProtocol(HidAsync.AsyncSetProtocol var1);
   }
}
