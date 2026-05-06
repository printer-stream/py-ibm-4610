package com.ibm.posj.scale;

public class OEMScaleHandleState extends DefaultScaleHandleState {
   public static final boolean DEFAULT_CAP_ZERO_SCALE = true;
   public static final double DEFAULT_MAX_WEIGHT_IN_KILOGRAMS = 15.0;
   public static final double DEFAULT_MAX_WEIGHT_IN_POUNDS = 30.0;

   public OEMScaleHandleState() {
      this.setCapZeroScale(true);
      this.setCapDisplay(true);
   }

   protected double getMaxWeightInUnits() {
      double maxWeightInUnits = 0.0;
      byte weightUnit = this.getWeightUnit();
      switch (weightUnit) {
         case 0:
            maxWeightInUnits = 30.0;
            break;
         case 1:
            maxWeightInUnits = 15.0;
            break;
         case 2:
            maxWeightInUnits = 480.0;
            break;
         case 3:
            maxWeightInUnits = 15000.0;
            break;
         default:
            maxWeightInUnits = 30.0;
      }

      return maxWeightInUnits;
   }
}
