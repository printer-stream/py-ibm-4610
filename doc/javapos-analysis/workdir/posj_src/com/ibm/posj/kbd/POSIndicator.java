package com.ibm.posj.kbd;

public interface POSIndicator {
   void setWait(boolean var1);

   boolean getWait();

   void setOffline(boolean var1);

   boolean getOffline();

   void setMsgPendOrSysMsg(boolean var1);

   boolean getMsgPendOrSysMsg();

   void setBlankOrReady(boolean var1);

   boolean getBlankOrReady();
}
