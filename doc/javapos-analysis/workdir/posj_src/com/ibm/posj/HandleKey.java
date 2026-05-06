package com.ibm.posj;

import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevCat;
import java.io.Serializable;

public interface HandleKey extends Serializable {
   String toString();

   int hashCode();

   boolean equals(Object var1);

   DevCat getDevCat();

   DevBus getDevBus();

   void accept(HandleKeyVisitor var1);
}
