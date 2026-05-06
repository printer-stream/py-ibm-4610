package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.POSKeyboardCmd;
import com.ibm.posj.event.DataEvent;
import com.ibm.posj.kbd.DefaultPOSKeyboardIndicator;
import com.ibm.posj.kbd.PS2Indicator;
import com.ibm.poskbd.Keyboard;
import com.ibm.poskbd.Leds;
import com.ibm.poskbd.PosKbdManager;
import com.ibm.poskbd.event.KeyboardEvent;
import com.ibm.poskbd.event.KeyboardListener;
import com.ibm.poskbd.event.ScancodeEvent;
import java.util.Iterator;

public class HidBootModePOSKeyboardHandleImp extends HidPOSKeyboardHandleImp {
   private Keyboard keyboard = null;
   private HidBootModePOSKeyboardHandleImp.NormalLeds normalLeds = new HidBootModePOSKeyboardHandleImp.NormalLeds();
   private KeyboardListener listener = new HidBootModePOSKeyboardHandleImp.KbdListener();

   public HidBootModePOSKeyboardHandleImp(HandleKey key, HidDevice device, HidPOSKeyboardStrategy strategy) {
      super(key, device, strategy);
   }

   public void init() throws HandleException {
      super.init();
      Iterator iterator = PosKbdManager.getInstance().getKeyboards();
      if (iterator.hasNext()) {
         this.keyboard = (Keyboard)iterator.next();
         this.keyboard.addKeyboardListener(this.listener);
      } else {
         throw new HandleException("Unable to add listener to the Handle, Keyboard obj not found");
      }
   }

   protected void submitEnableCmd(POSKeyboardCmd.EnableCmd enableCmd) {
      this.keyboard.setEnable(enableCmd.getEnable());
   }

   public PS2Indicator getPS2IndicatorState() {
      PS2Indicator ps2Indicator = new DefaultPOSKeyboardIndicator();
      ps2Indicator.setCapsLock(this.getNormalLeds().getCapsLock());
      ps2Indicator.setNumLock(this.getNormalLeds().getNumLock());
      ps2Indicator.setScrollLock(this.getNormalLeds().getScrollLock());
      return ps2Indicator;
   }

   protected void submitIndicatorCmd(POSKeyboardCmd.IndicatorCmd indicatorCmd) throws HandleException {
      switch (indicatorCmd.getIndicator()) {
         case 0:
            this.submitCmd(indicatorCmd.getName(), indicatorCmd.toBytes());
            this.keyboard.setLeds(this.getNormalLeds(indicatorCmd));
            break;
         case 1:
         case 2:
         case 3:
         case 4:
            this.submitCmd(indicatorCmd.getName(), indicatorCmd.toBytes());
            break;
         case 5:
         case 6:
         case 7:
            this.keyboard.setLeds(this.getNormalLeds(indicatorCmd));
      }

      this.getPOSKeyboardHandle().setPOSIndicatorState(this.getPOSKeyboardIndicator());
   }

   protected HidBootModePOSKeyboardHandleImp.NormalLeds getNormalLeds() {
      this.normalLeds.setCapsLock(this.keyboard.getLeds().getCapsLock());
      this.normalLeds.setNumLock(this.keyboard.getLeds().getNumLock());
      this.normalLeds.setScrollLock(this.keyboard.getLeds().getScrollLock());
      return this.normalLeds;
   }

   protected Leds getNormalLeds(POSKeyboardCmd.IndicatorCmd indicatorCmd) {
      HidBootModePOSKeyboardHandleImp.NormalLeds leds = this.getNormalLeds();
      switch (indicatorCmd.getIndicator()) {
         case 0:
            leds.setCapsLock(indicatorCmd.getAction());
            leds.setNumLock(indicatorCmd.getAction());
            leds.setScrollLock(indicatorCmd.getAction());
         case 1:
         case 2:
         case 3:
         case 4:
         default:
            break;
         case 5:
            leds.setNumLock(indicatorCmd.getAction());
            break;
         case 6:
            leds.setCapsLock(indicatorCmd.getAction());
            break;
         case 7:
            leds.setScrollLock(indicatorCmd.getAction());
      }

      return leds;
   }

   protected void handleScanCodeReceived(ScancodeEvent event) {
      if (this.getTracer().isOn()) {
         this.getTracer().println("handleScanCodeReceived() : Event-> 0x" + Integer.toHexString(event.getScancode()));
      }

      this.getHandle().getEventHelper().fireDataEvent(new DataEvent(this.getClass(), new byte[]{event.getScancode()}));
   }

   protected class KbdListener implements KeyboardListener {
      public void scancodeReceived(ScancodeEvent event) {
         HidBootModePOSKeyboardHandleImp.this.handleScanCodeReceived(event);
      }

      public void keyboardDisconnected(KeyboardEvent event) {
      }
   }

   protected class NormalLeds implements Leds {
      private boolean caps = false;
      private boolean num = false;
      private boolean scroll = false;

      public void setCapsLock(boolean on) {
         this.caps = on;
      }

      public boolean getCapsLock() {
         return this.caps;
      }

      public void setNumLock(boolean on) {
         this.num = on;
      }

      public boolean getNumLock() {
         return this.num;
      }

      public void setScrollLock(boolean on) {
         this.scroll = on;
      }

      public boolean getScrollLock() {
         return this.scroll;
      }
   }
}
