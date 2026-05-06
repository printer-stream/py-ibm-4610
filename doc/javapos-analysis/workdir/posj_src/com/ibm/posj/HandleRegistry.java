package com.ibm.posj;

import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevCat;
import java.util.Iterator;

public interface HandleRegistry {
   void addHandle(Handle var1);

   void removeHandle(Handle var1);

   void removeHandle(HandleKey var1);

   boolean containsHandle(Handle var1);

   boolean containsHandle(HandleKey var1);

   Handle getHandle(HandleKey var1);

   Iterator getHandles();

   int getSize();

   boolean isEmpty();

   Iterator getHandles(DevCat var1);

   Iterator getHandles(DevBus var1);

   Iterator getHandles(DevCat var1, DevBus var2);

   int getNumberOfHandles(DevCat var1);

   int getNumberOfHandles(DevBus var1);

   int getNumberOfHandles(DevCat var1, DevBus var2);

   Iterator getHandlesDevCats();

   Iterator getHandlesDevBuses();
}
