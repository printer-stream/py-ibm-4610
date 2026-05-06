package com.ibm.posj.kbd;

public class DefaultPOSIndicator implements POSIndicator {
   private boolean wait = false;
   private boolean offline = false;
   private boolean msgPendOrSysMsg = false;
   private boolean blankOrReady = false;

   public void setWait(boolean on) {
      this.wait = on;
   }

   public boolean getWait() {
      return this.wait;
   }

   public void setOffline(boolean on) {
      this.offline = on;
   }

   public boolean getOffline() {
      return this.offline;
   }

   public void setMsgPendOrSysMsg(boolean on) {
      this.msgPendOrSysMsg = on;
   }

   public boolean getMsgPendOrSysMsg() {
      return this.msgPendOrSysMsg;
   }

   public void setBlankOrReady(boolean on) {
      this.blankOrReady = on;
   }

   public boolean getBlankOrReady() {
      return this.blankOrReady;
   }
}
