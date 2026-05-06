package com.ibm.posj.bus;

import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;

public interface CompositeSubmitter {
   void submit(HandleCmd var1) throws HandleException;

   void submitCmd(String var1, byte[] var2) throws HandleException;
}
