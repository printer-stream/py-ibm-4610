package com.ibm.embedded.event;

import java.util.EventListener;

public interface EmbeddedListener extends EventListener {
   void statusEventOccurred(EmbeddedEvent var1);
}
