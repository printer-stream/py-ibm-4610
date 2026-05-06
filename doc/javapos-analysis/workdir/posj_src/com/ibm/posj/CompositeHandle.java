package com.ibm.posj;

import java.util.List;

public interface CompositeHandle extends Handle {
   Handle getMainHandle();

   List getHandleList();
}
