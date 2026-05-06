package com.ibm.hid.util;

import com.ibm.hid.HidAsync;

public class DefaultHidAsyncFactory implements HidAsyncFactory {
   public HidAsync.AsyncGetDescriptor getAsyncGetDescriptor() {
      return new DefaultHidAsync.DefaultAsyncGetDescriptor();
   }

   public HidAsync.AsyncSetDescriptor getAsyncSetDescriptor() {
      return new DefaultHidAsync.DefaultAsyncSetDescriptor();
   }

   public HidAsync.AsyncGetReport getAsyncGetReport() {
      return new DefaultHidAsync.DefaultAsyncGetReport();
   }

   public HidAsync.AsyncSetReport getAsyncSetReport() {
      return new DefaultHidAsync.DefaultAsyncSetReport();
   }

   public HidAsync.AsyncGetIdle getAsyncGetIdle() {
      return new DefaultHidAsync.DefaultAsyncGetIdle();
   }

   public HidAsync.AsyncSetIdle getAsyncSetIdle() {
      return new DefaultHidAsync.DefaultAsyncSetIdle();
   }

   public HidAsync.AsyncGetProtocol getAsyncGetProtocol() {
      return new DefaultHidAsync.DefaultAsyncGetProtocol();
   }

   public HidAsync.AsyncSetProtocol getAsyncSetProtocol() {
      return new DefaultHidAsync.DefaultAsyncSetProtocol();
   }
}
