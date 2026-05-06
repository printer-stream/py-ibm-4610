package com.ibm.posj.bus;

import com.ibm.posj.RuntimePosException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractKbdCompositeHandleImp extends AbstractCompositeHandleImp implements KbdCompositeHandleImp {
   protected boolean keylockPresent = false;
   protected boolean lineDisplayPresent = false;
   protected boolean msrPresent = false;
   protected boolean toneIndicatorPresent = false;
   private KeylockHandleImp keylockHandleImp = null;
   private LineDisplayHandleImp lineDisplayHandleImp = null;
   private MSRHandleImp msrHandleImp = null;
   private ToneIndicatorHandleImp toneIndicatorHandleImp = null;

   protected AbstractKbdCompositeHandleImp(POSKeyboardHandleImp posKeyboardHandleImp, Iterator otherHandleImps) {
      super(posKeyboardHandleImp, otherHandleImps);
   }

   protected AbstractKbdCompositeHandleImp(
      POSKeyboardHandleImp posKeyboardHandleImp,
      KeylockHandleImp keylockHandleImp,
      LineDisplayHandleImp lineDisplayHandleImp,
      MSRHandleImp msrHandleImp,
      ToneIndicatorHandleImp toneIndicatorHandleImp
   ) {
      super(posKeyboardHandleImp);
      List list = this.getSecondaryHandleImpList();
      list.add(keylockHandleImp);
      list.add(lineDisplayHandleImp);
      list.add(msrHandleImp);
      list.add(toneIndicatorHandleImp);
      this.setKeylockHandleImp(keylockHandleImp);
      this.setLineDisplayHandleImp(lineDisplayHandleImp);
      this.setMSRHandleImp(msrHandleImp);
      this.setToneIndicatorHandleImp(toneIndicatorHandleImp);
   }

   protected AbstractKbdCompositeHandleImp(
      POSKeyboardHandleImp posKeyboardHandleImp, KeylockHandleImp keylockHandleImp, MSRHandleImp msrHandleImp, ToneIndicatorHandleImp toneIndicatorHandleImp
   ) {
      super(posKeyboardHandleImp);
      List list = this.getSecondaryHandleImpList();
      list.add(keylockHandleImp);
      list.add(msrHandleImp);
      list.add(toneIndicatorHandleImp);
      this.setKeylockHandleImp(keylockHandleImp);
      this.setMSRHandleImp(msrHandleImp);
      this.setToneIndicatorHandleImp(toneIndicatorHandleImp);
   }

   protected AbstractKbdCompositeHandleImp(
      POSKeyboardHandleImp posKeyboardHandleImp, KeylockHandleImp keylockHandleImp, ToneIndicatorHandleImp toneIndicatorHandleImp
   ) {
      super(posKeyboardHandleImp);
      List list = this.getSecondaryHandleImpList();
      list.add(keylockHandleImp);
      list.add(toneIndicatorHandleImp);
      this.setKeylockHandleImp(keylockHandleImp);
      this.setToneIndicatorHandleImp(toneIndicatorHandleImp);
   }

   protected void setKeylockHandleImp(KeylockHandleImp handleImp) {
      this.keylockHandleImp = handleImp;
      this.keylockPresent = true;
   }

   protected void setLineDisplayHandleImp(LineDisplayHandleImp handleImp) {
      this.lineDisplayHandleImp = handleImp;
      this.lineDisplayPresent = true;
   }

   protected void setMSRHandleImp(MSRHandleImp handleImp) {
      this.msrHandleImp = handleImp;
      this.msrPresent = true;
   }

   protected void setToneIndicatorHandleImp(ToneIndicatorHandleImp handleImp) {
      this.toneIndicatorHandleImp = handleImp;
      this.toneIndicatorPresent = true;
   }

   public POSKeyboardHandleImp getPOSKeyboardHandleImp() {
      return (POSKeyboardHandleImp)this.getMainHandleImp();
   }

   public KeylockHandleImp getKeylockHandleImp() throws RuntimePosException {
      if (!this.hasKeylock()) {
         throw new RuntimePosException("This KbdCompositeHandleImp does not have a Keylock");
      } else {
         return this.keylockHandleImp;
      }
   }

   public LineDisplayHandleImp getLineDisplayHandleImp() throws RuntimePosException {
      if (!this.hasLineDisplay()) {
         throw new RuntimePosException("This KbdCompositeHandleImp does not have a LineDisplay");
      } else {
         return this.lineDisplayHandleImp;
      }
   }

   public MSRHandleImp getMSRHandleImp() throws RuntimePosException {
      if (!this.hasMSR()) {
         throw new RuntimePosException("This KbdCompositeHandleImp does not have an MSR");
      } else {
         return this.msrHandleImp;
      }
   }

   public ToneIndicatorHandleImp getToneIndicatorHandleImp() throws RuntimePosException {
      if (!this.hasToneIndicator()) {
         throw new RuntimePosException("This KbdCompositeHandleImp does not have a ToneIndicator");
      } else {
         return this.toneIndicatorHandleImp;
      }
   }

   public Iterator getSecondaryHandleImps() {
      List handleImps = new ArrayList();
      if (this.hasKeylock()) {
         handleImps.add(this.getKeylockHandleImp());
      }

      if (this.hasToneIndicator()) {
         handleImps.add(this.getToneIndicatorHandleImp());
      }

      if (this.hasMSR()) {
         handleImps.add(this.getMSRHandleImp());
      }

      if (this.hasLineDisplay()) {
         handleImps.add(this.getLineDisplayHandleImp());
      }

      return handleImps.iterator();
   }

   public boolean hasKeylock() {
      return this.keylockHandleImp == null ? false : this.keylockPresent;
   }

   public boolean hasLineDisplay() {
      return this.lineDisplayHandleImp == null ? false : this.lineDisplayPresent;
   }

   public boolean hasMSR() {
      return this.msrHandleImp == null ? false : this.msrPresent;
   }

   public boolean hasToneIndicator() {
      return this.toneIndicatorHandleImp == null ? false : this.toneIndicatorPresent;
   }
}
