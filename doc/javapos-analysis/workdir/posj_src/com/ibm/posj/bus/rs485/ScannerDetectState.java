package com.ibm.posj.bus.rs485;

public class ScannerDetectState {
   private int state = 0;
   private int devType = 0;
   private int nextStep = 0;
   public final int INITIAL_STATE = 0;
   public final int FIRST_TRY_STATE = 1;
   public final int SECOND_TRY_STATE = 2;
   public final int DEV_RESETED_STATE = 3;
   public final int CONFIG_UPDATED_STATE = 4;
   public final int DEV_TYPE_FOUND_STATE = 5;

   public void reset() {
      this.state = 0;
      this.nextStep = 1;
   }

   public void setState(int state) {
      this.state = state;
   }

   public int getState() {
      return this.state;
   }

   public void setDevType(int devType) {
      this.devType = devType;
      this.setState(5);
      this.setNextStep(0);
   }

   public int getDevType() {
      return this.devType;
   }

   public void setNextStep(int nextStep) {
      this.nextStep = nextStep;
   }

   public int getNextStep() {
      return this.nextStep;
   }
}
