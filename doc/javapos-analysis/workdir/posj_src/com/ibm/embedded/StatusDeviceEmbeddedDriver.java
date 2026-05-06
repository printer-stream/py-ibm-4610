package com.ibm.embedded;

import com.ibm.embedded.event.EmbeddedListener;

public interface StatusDeviceEmbeddedDriver extends EmbeddedDriver {
   byte getStatus() throws EmbeddedException;

   void addEmbeddedEventListener(EmbeddedListener var1) throws EmbeddedException;

   void removeEmbeddedEventListener(EmbeddedListener var1) throws EmbeddedException;
}
