package com.ibm.embedded;

import com.ibm.embedded.event.EmbeddedListener;

public interface MotionSensorEmbeddedDriver extends EmbeddedDriver {
   byte MS_ABSENCE = 0;
   byte MS_PRESENCE = 1;
   byte MS_NOT_SET = 2;

   byte getStatus() throws EmbeddedException;

   void addEmbeddedEventListener(EmbeddedListener var1) throws EmbeddedException;

   void removeEmbeddedEventListener(EmbeddedListener var1) throws EmbeddedException;
}
