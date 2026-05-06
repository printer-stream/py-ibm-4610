package com.ibm.posj.bus;

import com.ibm.posj.RuntimePosException;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractSurepointCompositeHandleImp extends AbstractCompositeHandleImp implements SurepointCompositeHandleImp {
   protected boolean posKeyboardPresent = false;
   protected boolean msrPresent = false;
   protected boolean toneIndicatorPresent = false;
   private POSKeyboardHandleImp posKeyboardHandleImp = null;
   private MSRHandleImp msrHandleImp = null;
   private ToneIndicatorHandleImp toneIndicatorHandleImp = null;

   protected AbstractSurepointCompositeHandleImp(KeylockHandleImp keylockHandleImp, Iterator otherHandleImps) {
      super(keylockHandleImp, otherHandleImps);
   }

   protected AbstractSurepointCompositeHandleImp(
      KeylockHandleImp keylockHandleImp, MSRHandleImp msrHandleImp, POSKeyboardHandleImp posKeyboardHandleImp, ToneIndicatorHandleImp toneIndicatorHandleImp
   ) {
      super(keylockHandleImp);
      List list = this.getSecondaryHandleImpList();
      list.add(msrHandleImp);
      list.add(posKeyboardHandleImp);
      list.add(toneIndicatorHandleImp);
      this.setMSRHandleImp(msrHandleImp);
      this.setPOSKeyboardHandleImp(posKeyboardHandleImp);
      this.setToneIndicatorHandleImp(toneIndicatorHandleImp);
   }

   protected AbstractSurepointCompositeHandleImp(KeylockHandleImp keylockHandleImp, MSRHandleImp msrHandleImp, ToneIndicatorHandleImp toneIndicatorHandleImp) {
      super(keylockHandleImp);
      List list = this.getSecondaryHandleImpList();
      list.add(msrHandleImp);
      list.add(toneIndicatorHandleImp);
      this.setMSRHandleImp(msrHandleImp);
      this.setToneIndicatorHandleImp(toneIndicatorHandleImp);
   }

   protected AbstractSurepointCompositeHandleImp(KeylockHandleImp keylockHandleImp, ToneIndicatorHandleImp toneIndicatorHandleImp) {
      super(keylockHandleImp);
      List list = this.getSecondaryHandleImpList();
      list.add(toneIndicatorHandleImp);
      this.setToneIndicatorHandleImp(toneIndicatorHandleImp);
   }

   protected void setMSRHandleImp(MSRHandleImp handleImp) {
      this.msrHandleImp = handleImp;
      this.msrPresent = true;
   }

   protected void setPOSKeyboardHandleImp(POSKeyboardHandleImp handleImp) {
      this.posKeyboardHandleImp = handleImp;
      this.posKeyboardPresent = true;
   }

   protected void setToneIndicatorHandleImp(ToneIndicatorHandleImp handleImp) {
      this.toneIndicatorHandleImp = handleImp;
      this.toneIndicatorPresent = true;
   }

   public POSKeyboardHandleImp getPOSKeyboardHandleImp() throws RuntimePosException {
      if (!this.hasPOSKeyboard()) {
         throw new RuntimePosException("This SurepointCompositeHandleImp does not have a Keypad");
      } else {
         return this.posKeyboardHandleImp;
      }
   }

   public KeylockHandleImp getKeylockHandleImp() {
      return (KeylockHandleImp)this.getMainHandleImp();
   }

   public MSRHandleImp getMSRHandleImp() throws RuntimePosException {
      if (!this.hasMSR()) {
         throw new RuntimePosException("This SurepointCompositeHandleImp does not have an MSR");
      } else {
         return this.msrHandleImp;
      }
   }

   public ToneIndicatorHandleImp getToneIndicatorHandleImp() throws RuntimePosException {
      if (!this.hasToneIndicator()) {
         throw new RuntimePosException("This SurepointCompositeHandleImp does not have a ToneIndicator");
      } else {
         return this.toneIndicatorHandleImp;
      }
   }

   public boolean hasPOSKeyboard() {
      return this.posKeyboardHandleImp == null ? false : this.posKeyboardPresent;
   }

   public boolean hasMSR() {
      return this.msrHandleImp == null ? false : this.msrPresent;
   }

   public boolean hasToneIndicator() {
      return this.toneIndicatorHandleImp == null ? false : this.toneIndicatorPresent;
   }
}
