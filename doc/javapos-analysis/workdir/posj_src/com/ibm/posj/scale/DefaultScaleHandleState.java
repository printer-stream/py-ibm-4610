package com.ibm.posj.scale;

public class DefaultScaleHandleState implements ScaleHandleState {
   private boolean maxWeightConversion = true;
   private boolean capDisplay = false;
   private boolean capDisplayText = false;
   private boolean capPriceCalculating = false;
   private boolean capTareWeight = false;
   private boolean capZeroScale = false;
   private int maxDisplayTextChars = 0;
   private double maxWeight = 0.0;
   private byte weightUnit = 0;
   private boolean scaleConfigured = false;
   public static final double DEFAULT_MAX_WEIGHT_IN_KILOGRAMS = 15.0;
   public static final double DEFAULT_MAX_WEIGHT_IN_POUNDS = 30.0;

   public boolean getCapDisplay() {
      return this.capDisplay;
   }

   public boolean getCapDisplayText() {
      return this.capDisplayText;
   }

   public boolean getCapPriceCalculating() {
      return this.capPriceCalculating;
   }

   public boolean getCapTareWeight() {
      return this.capTareWeight;
   }

   public boolean getCapZeroScale() {
      return this.capZeroScale;
   }

   public int getMaxDisplayTextChars() {
      return this.maxDisplayTextChars;
   }

   public double getMaxWeight() {
      double maxWeightValue = 0.0;
      if (this.maxWeightConversion) {
         maxWeightValue = this.getMaxWeightInUnits();
      } else {
         maxWeightValue = this.maxWeight;
      }

      return maxWeightValue;
   }

   public byte getWeightUnit() {
      return this.weightUnit;
   }

   public boolean getConfigured() {
      return this.scaleConfigured;
   }

   public boolean isConfigured() {
      return this.getConfigured();
   }

   public void setMaxWeight(double maxWeight) {
      this.maxWeight = maxWeight;
   }

   public void setWeightUnit(byte weighUnit) {
      this.weightUnit = weighUnit;
   }

   public void setMaxWeightConversion(boolean maxWeightConversion) {
      this.maxWeightConversion = maxWeightConversion;
   }

   public void setCapDisplay(boolean capDisplay) {
      this.capDisplay = capDisplay;
   }

   public void setCapDisplayText(boolean capDisplayText) {
      this.capDisplayText = capDisplayText;
   }

   public void setCapPriceCalculating(boolean capPriceCalculating) {
      this.capPriceCalculating = capPriceCalculating;
   }

   public void setCapTareWeight(boolean capTareWeight) {
      this.capTareWeight = capTareWeight;
   }

   public void setCapZeroScale(boolean capZeroScale) {
      this.capZeroScale = capZeroScale;
   }

   protected void setMaxDisplayTextChars(int maxDisplayTextChars) {
      this.maxDisplayTextChars = maxDisplayTextChars;
   }

   public void setConfigured(boolean deviceConfigured) {
      this.scaleConfigured = deviceConfigured;
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
