package com.ibm.posj.bus.poskbd;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.HandleFactory;
import com.ibm.posj.HandleFactoryException;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCats;
import com.ibm.poskbd.Function;
import com.ibm.poskbd.FunctionVisitor;
import com.ibm.poskbd.Keyboard;
import com.ibm.poskbd.KeyboardFunction;
import com.ibm.poskbd.LedFunction;
import com.ibm.poskbd.MsrFunction;
import com.ibm.poskbd.StatusFunction;
import com.ibm.poskbd.ToneFunction;
import com.ibm.poskbd.util.DefaultFunctionV;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

public class PosKbdHandleImpFactory {
   private HandleFactory handleFactory = null;
   private PosKbdPOSKeyboardStrategy.Factory kbdStrategyFactory = null;
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevBuses.POSKBD_DEVBUS.getName(), "PosKbdHandleImpFactory");

   public PosKbdHandleImpFactory(HandleFactory factory) {
      this.handleFactory = factory;
   }

   public ListIterator createHandleImps(Keyboard keyboard, int kbdNum) throws HandleFactoryException {
      Iterator i = keyboard.getFunctions();
      PosKbdHandleImpFactory.FactoryFunctionV visitor = new PosKbdHandleImpFactory.FactoryFunctionV(keyboard, kbdNum);

      while (i.hasNext()) {
         ((Function)i.next()).accept(visitor);
      }

      if (null != visitor.getException()) {
         throw visitor.getException();
      } else {
         visitor.connectFunctions();
         return visitor.getHandleImps();
      }
   }

   protected PosKbdPOSKeyboardStrategy.Factory getPOSKeyboardStrategyFactory() {
      if (this.kbdStrategyFactory == null) {
         this.kbdStrategyFactory = new DefaultPosKbdPOSKeyboardStrategy.Factory();
      }

      return this.kbdStrategyFactory;
   }

   private void traceNormal(String msg) {
      this.tracer.println(2, msg);
   }

   private class FactoryFunctionV extends DefaultFunctionV implements FunctionVisitor {
      private List handleImps = new Vector();
      private PosKbdPOSKeyboardHandleImp posKbdPOSKeyboardHandleImp = null;
      private PosKbdMSRHandleImp msrImp = null;
      private PosKbdToneIndicatorHandleImp toneImp = null;
      private PosKbdKeylockHandleImp keylockImp = null;
      private LedFunction ledFunction = null;
      private StatusFunction statusFunction = null;
      private KeyboardFunction keyboardFunction = null;
      private Keyboard keyboard = null;
      private int keyboardNumber = 0;
      private int posKeyboardNumber = 0;
      private int msrNumber = 0;
      private int keylockNumber = 0;
      private int toneNumber = 0;
      private HandleFactoryException hfE = null;

      public FactoryFunctionV(Keyboard keyboard, int keyboardNumber) {
         this.keyboard = keyboard;
         this.keyboardNumber = keyboardNumber;
      }

      public void visitKeyboardFunction(KeyboardFunction keyboard) {
         if (PosKbdHandleImpFactory.this.tracer.isOn()) {
            PosKbdHandleImpFactory.this.traceNormal("-->visitKeyboardFunction() : \n" + keyboard.getDeviceInfo() + "<--");
         }

         try {
            PosKbdHandleKey handleKey = new PosKbdHandleKey(DevCats.POSKEYBOARD_DEVCAT, this.keyboardNumber, this.posKeyboardNumber);
            short var10000 = keyboard.getDeviceInfo().getKeyboardSubtype();
            keyboard.getDeviceInfo();
            if (var10000 == 2) {
               this.posKbdPOSKeyboardHandleImp = new PosKbdPOSKeyboardHandleImp(
                  handleKey, keyboard, PosKbdHandleImpFactory.this.getPOSKeyboardStrategyFactory().createDBCSPOSKeyboardStrategy()
               );
            } else {
               this.posKbdPOSKeyboardHandleImp = new PosKbdPOSKeyboardHandleImp(
                  handleKey, keyboard, PosKbdHandleImpFactory.this.getPOSKeyboardStrategyFactory().createSBCSPOSKeyboardStrategy()
               );
            }

            this.posKbdPOSKeyboardHandleImp.setHandle(PosKbdHandleImpFactory.this.handleFactory.createPOSKeyboardHandle(this.posKbdPOSKeyboardHandleImp));
            this.handleImps.add(this.posKbdPOSKeyboardHandleImp);
            this.posKeyboardNumber++;
         } catch (HandleFactoryException var3) {
            this.hfE = var3;
         }

         this.keyboardFunction = keyboard;
      }

      public void visitMsrFunction(MsrFunction msr) {
         try {
            PosKbdHandleKey handleKey = new PosKbdHandleKey(DevCats.MSR_DEVCAT, this.keyboardNumber, this.msrNumber);
            this.msrImp = new PosKbdMSRHandleImp(handleKey, msr);
            this.msrImp.setHandle(PosKbdHandleImpFactory.this.handleFactory.createMSRHandle(this.msrImp));
            this.handleImps.add(this.msrImp);
            this.msrNumber++;
         } catch (HandleFactoryException var3) {
            this.hfE = var3;
         }
      }

      public void visitLedFunction(LedFunction led) {
         this.ledFunction = led;
      }

      public void visitStatusFunction(StatusFunction status) {
         try {
            this.statusFunction = status;
            PosKbdHandleKey handleKey = new PosKbdHandleKey(DevCats.KEYLOCK_DEVCAT, this.keyboardNumber, this.keylockNumber);
            this.keylockImp = new PosKbdKeylockHandleImp(handleKey, status);
            this.keylockImp.setHandle(PosKbdHandleImpFactory.this.handleFactory.createKeylockHandle(this.keylockImp));
            this.handleImps.add(this.keylockImp);
            this.keylockNumber++;
         } catch (HandleFactoryException var3) {
            this.hfE = var3;
         }
      }

      public void visitToneFunction(ToneFunction tone) {
         try {
            PosKbdHandleKey handleKey = new PosKbdHandleKey(DevCats.TONEINDICATOR_DEVCAT, this.keyboardNumber, this.toneNumber);
            this.toneImp = new PosKbdToneIndicatorHandleImp(handleKey, tone);
            this.toneImp.setHandle(PosKbdHandleImpFactory.this.handleFactory.createToneIndicatorHandle(this.toneImp));
            this.handleImps.add(this.toneImp);
            this.toneNumber++;
         } catch (HandleFactoryException var3) {
            this.hfE = var3;
         }
      }

      public void connectFunctions() {
         if (null != this.posKbdPOSKeyboardHandleImp) {
            if (null != this.ledFunction) {
               this.posKbdPOSKeyboardHandleImp.setLedFunction(this.ledFunction);
            }

            this.posKbdPOSKeyboardHandleImp.setKeyboard(this.keyboard);
         }

         if (null != this.toneImp && null != this.statusFunction) {
            this.toneImp.setStatusFunction(this.statusFunction);
            this.toneImp.setKbdType(this.keyboardFunction.getDeviceInfo().getKeyboardSubtype());
         }

         if (null != this.msrImp && null != this.keyboardFunction) {
            this.msrImp.setKeyboardFunction(this.keyboardFunction);
         }

         if (null != this.keylockImp && null != this.keyboardFunction) {
            this.keylockImp.setKbdType(this.keyboardFunction.getDeviceInfo().getKeyboardSubtype());
         }
      }

      public ListIterator getHandleImps() {
         return this.handleImps.listIterator();
      }

      public HandleFactoryException getException() {
         return this.hfE;
      }

      public void clear() {
         this.handleImps.clear();
         this.posKbdPOSKeyboardHandleImp = null;
         this.ledFunction = null;
         this.statusFunction = null;
         this.keyboard = null;
         this.keyboardNumber = 0;
         this.posKeyboardNumber = 0;
         this.msrNumber = 0;
         this.keylockNumber = 0;
         this.toneNumber = 0;
         this.hfE = null;
      }
   }
}
