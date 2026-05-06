package com.ibm.posj.bus.poskbd;

import com.ibm.jutil.Util;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.POSKeyboardCmd;
import com.ibm.posj.POSKeyboardConst;
import com.ibm.posj.POSKeyboardHandle;
import com.ibm.posj.RuntimePosException;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.POSKeyboardHandleImp;
import com.ibm.posj.event.DataEvent;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.kbd.DefaultPOSKeyboardIndicator;
import com.ibm.posj.kbd.POSIndicator;
import com.ibm.posj.kbd.POSKeyboardConfig;
import com.ibm.posj.kbd.POSKeyboardIndicator;
import com.ibm.posj.kbd.PS2Indicator;
import com.ibm.posj.util.DefaultPOSKeyboardCmdV;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.POSKeyboardUtil;
import com.ibm.poskbd.ClickCmd;
import com.ibm.poskbd.Keyboard;
import com.ibm.poskbd.KeyboardDevInfo;
import com.ibm.poskbd.KeyboardFunction;
import com.ibm.poskbd.LedFunction;
import com.ibm.poskbd.Leds;
import com.ibm.poskbd.event.KeyboardEvent;
import com.ibm.poskbd.event.KeyboardListener;
import com.ibm.poskbd.event.ScancodeEvent;
import com.ibm.poskbd.util.LedCmdFactory;

class PosKbdPOSKeyboardHandleImp extends AbstractPosKbdHandleImp implements POSKeyboardHandleImp, POSKeyboardConst {
   protected KeyboardFunction keyboardFunction = null;
   protected LedFunction ledFunction = null;
   protected Keyboard keyboard = null;
   private KeyboardListener listener = new PosKbdPOSKeyboardHandleImp.KbdListener();
   private PosKbdPOSKeyboardHandleImp.SubmitPosKbdCmdV submitPosKbdCmdV = new PosKbdPOSKeyboardHandleImp.SubmitPosKbdCmdV();
   private PosKbdPOSKeyboardStrategy strategy = null;

   public PosKbdPOSKeyboardHandleImp(HandleKey key, KeyboardFunction keyboard, PosKbdPOSKeyboardStrategy strategy) {
      super(key);
      this.strategy = strategy;
      this.keyboardFunction = keyboard;
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitPOSKeyboard(this);
   }

   public DevCat getDevCat() {
      return DevCats.POSKEYBOARD_DEVCAT;
   }

   public void setLedFunction(LedFunction led) {
      this.ledFunction = led;
   }

   public void setKeyboard(Keyboard kbd) {
      this.keyboard = kbd;
   }

   public PS2Indicator getPS2IndicatorState() {
      PS2Indicator ps2Indicator = new DefaultPOSKeyboardIndicator();
      ps2Indicator.setCapsLock(this.keyboard.getLeds().getCapsLock());
      ps2Indicator.setNumLock(this.keyboard.getLeds().getNumLock());
      ps2Indicator.setScrollLock(this.keyboard.getLeds().getScrollLock());
      return ps2Indicator;
   }

   public void init() throws HandleException {
      this.keyboard.addKeyboardListener(this.listener);
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd instanceof POSKeyboardCmd) {
         ((POSKeyboardCmd)cmd).accept(this.submitPosKbdCmdV);
      } else {
         if (!(cmd instanceof SystemCmd)) {
            throw new HandleException("Invalid POSKeybpoardCmd object submitted!");
         }

         ((SystemCmd)cmd).accept(this.submitPosKbdCmdV);
      }
   }

   protected POSKeyboardHandle getPOSKeyboardHandle() {
      return (POSKeyboardHandle)this.getHandle();
   }

   protected void handleScanCodeReceived(ScancodeEvent event) {
      if (this.isTracerOn()) {
         this.traceMaximum("ScancodeEvent.getScancode() : " + Util.toHexString(event.getScancode()));
      }

      this.getHandle().getEventHelper().fireDataEvent(new DataEvent(this.getClass(), new byte[]{event.getScancode()}));
   }

   protected void handleKeyboardDisconnected(KeyboardEvent event) {
      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
      eventHelper.fireErrorEvent(new ErrorEvent(this, -100));
   }

   protected void visitTestSystemCmd(SystemCmd.TestRequestCmd cmd) {
   }

   protected void visitStatusSystemCmd(SystemCmd.StatusRequestCmd cmd) {
   }

   protected void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
      KeyboardDevInfo devInfo = this.keyboardFunction.getDeviceInfo();
      if (this.isTracerOn()) {
         this.traceNormal("->visitDevInfoSystemCmd : kbd subtype -> " + devInfo.getKeyboardSubtype());
      }

      cmd.setDeviceId(POSKeyboardUtil.getPs2POSKeyboardID(devInfo.getKeyboardSubtype()));
      if (this.isTracerOn()) {
         this.traceNormal("<-visitDevInfoSystemCmd : DevInfoCmd.getDeviceId(): " + cmd.getDeviceId());
      }
   }

   protected void visitResetSystemCmd(SystemCmd.ResetRequestCmd cmd) {
   }

   protected void visitConfigCmd(POSKeyboardCmd.ConfigCmd cmd) throws RuntimePosException {
      POSKeyboardConfig config = cmd.getConfig();
      ClickCmd clickCmd;
      if (config.getClick() == 2) {
         clickCmd = this.keyboardFunction.getClickCmdFactory().createClickCmd(2);
      } else if (config.getClick() == 1) {
         clickCmd = this.keyboardFunction.getClickCmdFactory().createClickCmd(1);
      } else {
         if (config.getClick() != 0) {
            throw new RuntimePosException("Invalid Click Configuration received");
         }

         clickCmd = this.keyboardFunction.getClickCmdFactory().createClickCmd(0);
      }

      this.keyboardFunction.setClick(clickCmd);
      this.keyboardFunction.setTypematic(config.getTypematic());
      this.keyboardFunction.setEnabled(config.getKbdScanning());
   }

   protected void visitIndicatorCmd(POSKeyboardCmd.IndicatorCmd cmd) {
      POSKeyboardIndicator kbdI = this.getCurrentSettings();
      int led = cmd.getIndicator();
      boolean action = cmd.getAction();
      if (1 == led) {
         kbdI.setWait(action);
      } else if (2 == led) {
         kbdI.setOffline(action);
      } else if (3 == led) {
         kbdI.setMsgPendOrSysMsg(action);
      } else if (4 == led) {
         kbdI.setBlankOrReady(action);
      } else if (5 == led) {
         kbdI.setNumLock(action);
      } else if (6 == led) {
         kbdI.setCapsLock(action);
      } else if (7 == led) {
         kbdI.setScrollLock(action);
      } else {
         if (0 != led) {
            throw new RuntimePosException("Illegal indicator received");
         }

         this.turnAll(kbdI, action);
      }

      LedCmdFactory ledFactory = this.ledFunction.getLedCmdFactory();
      this.ledFunction.setLeds(ledFactory.createLedCmd(this.getStrategy().createIndicatorConfig(kbdI)));
      this.keyboard.setLeds(this.convertTo(kbdI));
      this.getPOSKeyboardHandle().setPOSIndicatorState(kbdI);
   }

   protected void visitEnableCmd(POSKeyboardCmd.EnableCmd cmd) {
      this.keyboard.setEnable(cmd.getEnable());
   }

   protected POSKeyboardIndicator getCurrentSettings() {
      POSKeyboardIndicator kbdI = new DefaultPOSKeyboardIndicator();
      POSIndicator posIndicator = this.getPOSKeyboardHandle().getPOSIndicatorState();
      Leds leds = this.keyboard.getLeds();
      kbdI.setWait(posIndicator.getWait());
      kbdI.setOffline(posIndicator.getOffline());
      kbdI.setMsgPendOrSysMsg(posIndicator.getMsgPendOrSysMsg());
      kbdI.setBlankOrReady(posIndicator.getBlankOrReady());
      kbdI.setNumLock(leds.getNumLock());
      kbdI.setCapsLock(leds.getCapsLock());
      kbdI.setScrollLock(leds.getScrollLock());
      return kbdI;
   }

   protected void turnAll(POSKeyboardIndicator indicator, boolean action) {
      indicator.setWait(action);
      indicator.setOffline(action);
      indicator.setMsgPendOrSysMsg(action);
      indicator.setBlankOrReady(action);
      indicator.setNumLock(action);
      indicator.setCapsLock(action);
      indicator.setScrollLock(action);
   }

   protected PosKbdPOSKeyboardStrategy getStrategy() {
      return this.strategy;
   }

   protected Leds convertTo(POSKeyboardIndicator indicator) {
      PosKbdPOSKeyboardHandleImp.NormalLeds leds = new PosKbdPOSKeyboardHandleImp.NormalLeds();
      leds.setCapsLock(indicator.getCapsLock());
      leds.setNumLock(indicator.getNumLock());
      leds.setScrollLock(indicator.getScrollLock());
      return leds;
   }

   protected class KbdListener implements KeyboardListener {
      public void scancodeReceived(ScancodeEvent event) {
         PosKbdPOSKeyboardHandleImp.this.handleScanCodeReceived(event);
      }

      public void keyboardDisconnected(KeyboardEvent event) {
         PosKbdPOSKeyboardHandleImp.this.handleKeyboardDisconnected(event);
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

   protected class SubmitPosKbdCmdV extends DefaultPOSKeyboardCmdV {
      public void visitTestSystemCmd(SystemCmd.TestRequestCmd cmd) {
         PosKbdPOSKeyboardHandleImp.this.visitTestSystemCmd(cmd);
      }

      public void visitStatusSystemCmd(SystemCmd.StatusRequestCmd cmd) {
         PosKbdPOSKeyboardHandleImp.this.visitStatusSystemCmd(cmd);
      }

      public void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
         PosKbdPOSKeyboardHandleImp.this.visitDevInfoSystemCmd(cmd);
      }

      public void visitResetSystemCmd(SystemCmd.ResetRequestCmd cmd) {
         PosKbdPOSKeyboardHandleImp.this.visitResetSystemCmd(cmd);
      }

      public void visitConfigCmd(POSKeyboardCmd.ConfigCmd cmd) {
         PosKbdPOSKeyboardHandleImp.this.visitConfigCmd(cmd);
      }

      public void visitIndicatorCmd(POSKeyboardCmd.IndicatorCmd cmd) {
         PosKbdPOSKeyboardHandleImp.this.visitIndicatorCmd(cmd);
      }

      public void visitEnableCmd(POSKeyboardCmd.EnableCmd cmd) {
         PosKbdPOSKeyboardHandleImp.this.visitEnableCmd(cmd);
      }
   }
}
