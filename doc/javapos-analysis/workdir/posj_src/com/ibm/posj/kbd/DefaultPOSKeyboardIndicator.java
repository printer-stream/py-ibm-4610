package com.ibm.posj.kbd;

public class DefaultPOSKeyboardIndicator extends DefaultPOSIndicator implements POSKeyboardIndicator {
   private boolean numLock = false;
   private boolean capsLock = false;
   private boolean scrollLock = false;

   public void setNumLock(boolean on) {
      this.numLock = on;
   }

   public boolean getNumLock() {
      return this.numLock;
   }

   public void setCapsLock(boolean on) {
      this.capsLock = on;
   }

   public boolean getCapsLock() {
      return this.capsLock;
   }

   public void setScrollLock(boolean on) {
      this.scrollLock = on;
   }

   public boolean getScrollLock() {
      return this.scrollLock;
   }
}
