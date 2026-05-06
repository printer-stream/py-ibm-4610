package com.ibm.posj.bus;

import com.ibm.posj.HandleRegistry;
import com.ibm.posj.PosException;
import com.ibm.posj.util.DevBus;

public interface HandlePopulator {
   String getName();

   HandleRegistry getHandleRegistry();

   DevBus getDevBus();

   void stop() throws PosException;

   void start() throws PosException;

   boolean isStarted();

   Exception getLastException();
}
