package com.ibm.posj.bus;

import java.util.Iterator;

public interface CompositeHandleImp extends HandleImp {
   HandleImp getMainHandleImp();

   Iterator getSecondaryHandleImps();
}
