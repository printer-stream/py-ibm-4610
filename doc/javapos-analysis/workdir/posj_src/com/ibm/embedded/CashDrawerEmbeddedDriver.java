package com.ibm.embedded;

import com.ibm.embedded.event.EmbeddedListener;

public interface CashDrawerEmbeddedDriver extends EmbeddedDriver {
   void disableAsyncCDComm() throws EmbeddedException;

   void openCD(int var1) throws EmbeddedException;

   byte getCDStatus() throws EmbeddedException;

   byte getNumberOfDrawers() throws EmbeddedException;

   void addEmbeddedEventListener(EmbeddedListener var1) throws EmbeddedException;

   void removeEmbeddedEventListener(EmbeddedListener var1) throws EmbeddedException;

   long getAdapterID();
}
