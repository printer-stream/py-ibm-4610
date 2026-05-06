package com.ibm.hid.util;

import com.ibm.hid.HidAsync;

public interface HidAsyncFactory {
   HidAsync.AsyncGetDescriptor getAsyncGetDescriptor();

   HidAsync.AsyncSetDescriptor getAsyncSetDescriptor();

   HidAsync.AsyncGetReport getAsyncGetReport();

   HidAsync.AsyncSetReport getAsyncSetReport();

   HidAsync.AsyncGetIdle getAsyncGetIdle();

   HidAsync.AsyncSetIdle getAsyncSetIdle();

   HidAsync.AsyncGetProtocol getAsyncGetProtocol();

   HidAsync.AsyncSetProtocol getAsyncSetProtocol();
}
