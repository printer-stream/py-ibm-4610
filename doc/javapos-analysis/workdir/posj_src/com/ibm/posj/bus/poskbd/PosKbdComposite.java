package com.ibm.posj.bus.poskbd;

import com.ibm.posj.AbstractHandle;
import com.ibm.posj.bus.AbstractCompositeHandleImp;
import com.ibm.posj.bus.HandleImp;
import com.ibm.poskbd.Keyboard;
import com.ibm.poskbd.event.KeyboardEvent;
import com.ibm.poskbd.event.KeyboardListener;
import com.ibm.poskbd.event.ScancodeEvent;
import java.util.Iterator;
import java.util.Map;

public class PosKbdComposite extends AbstractCompositeHandleImp {
   private KeyboardListener listener = new PosKbdComposite.CompositeKeyboardListener();

   public PosKbdComposite(Keyboard keyboard) {
      keyboard.addKeyboardListener(this.listener);
   }

   public PosKbdComposite(Keyboard keyboard, Iterator handleImps) {
      super(handleImps);
      keyboard.addKeyboardListener(this.listener);
   }

   public void disconnect() {
      Iterator iterator = this.getSecondaryHandleImps();

      while (iterator.hasNext()) {
         ((AbstractHandle)((HandleImp)iterator.next()).getHandle()).setOnline(false);
      }
   }

   public Map getDevStatisticDefinitions() {
      return null;
   }

   public String getDeviceSerialNumber() {
      return null;
   }

   public boolean isSubDevice() {
      return false;
   }

   private class CompositeKeyboardListener implements KeyboardListener {
      private CompositeKeyboardListener() {
      }

      public void scancodeReceived(ScancodeEvent sE) {
      }

      public void keyboardDisconnected(KeyboardEvent kE) {
      }
   }
}
