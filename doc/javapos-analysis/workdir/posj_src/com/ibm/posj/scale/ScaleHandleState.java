package com.ibm.posj.scale;

public interface ScaleHandleState {
   byte SCAL_WU_POUND = 0;
   byte SCAL_WU_KILOGRAM = 1;
   byte SCAL_WU_OUNCE = 2;
   byte SCAL_WU_GRAM = 3;

   boolean getCapDisplay();

   boolean getCapDisplayText();

   boolean getCapPriceCalculating();

   boolean getCapTareWeight();

   boolean getCapZeroScale();

   int getMaxDisplayTextChars();

   double getMaxWeight();

   byte getWeightUnit();

   boolean getConfigured();

   boolean isConfigured();

   void setMaxWeight(double var1);

   void setWeightUnit(byte var1);

   void setMaxWeightConversion(boolean var1);

   void setConfigured(boolean var1);
}
