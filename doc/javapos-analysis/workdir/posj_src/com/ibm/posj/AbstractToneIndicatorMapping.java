package com.ibm.posj;

public class AbstractToneIndicatorMapping implements ToneIndicatorMapping {
   protected int[] toneInd_duration_values;
   protected int[][] toneInd_frequency_values;
   protected int[][] toneInd_volume_values;
   protected int toneIndicator_duration_units;
   private int[] array_duration;
   private int[][] array_frequency;
   private int[][] array_volume;
   private int position = 0;
   private int element = 0;
   private int next_element = 0;
   private boolean valueMappedFound = false;
   private static final int ARRAY_EMPTY = 0;
   private static final int UNIQUE_VALUE = 1;
   private static final int START = 0;
   private static final int DEVICE_FREQUENCY_ARRAY = 0;
   private static final int BUS_FREQUENCY_ARRAY = 1;
   private static final int DEVICE_VOLUME_ARRAY = 0;
   private static final int BUS_VOLUME_ARRAY = 1;
   private static final int DEVICE_PROPERTY_ARRAY = 0;
   private static final int BUS_PROPERTY_ARRAY = 1;

   public int mapToneDuration(int toneDuration) {
      this.array_duration = this.getArrayDuration();
      if (this.array_duration.length == 0) {
         throw new IllegalArgumentException("There is not any device duration value to match with, array is empty");
      } else {
         toneDuration /= this.getDurationUnits();
         if (this.array_duration.length == 1) {
            toneDuration = this.array_duration[this.element];
         } else if (toneDuration <= this.array_duration[0]) {
            toneDuration = this.array_duration[this.position];
         } else if (toneDuration >= this.array_duration[this.array_duration.length - 1]) {
            toneDuration = this.array_duration[this.array_duration.length - 1];
         } else {
            for (this.position = 0; this.position < this.array_duration.length && !this.valueMappedFound; this.position++) {
               if (this.position + 1 < this.array_duration.length) {
                  this.element = this.array_duration[this.position];
                  this.next_element = this.array_duration[this.position + 1];
                  if (toneDuration > this.element && toneDuration <= this.next_element) {
                     toneDuration = toneDuration <= (this.next_element - this.element) / 2 + this.element
                        ? this.array_duration[this.position]
                        : this.array_duration[this.position + 1];
                     this.valueMappedFound = true;
                  }
               }
            }
         }

         this.restart_Flag_ArrayCounter();
         return toneDuration;
      }
   }

   public int mapToneFrequency(int toneFrequency) {
      this.array_frequency = this.getArrayFrequencies();
      return this.mappingArgument(this.array_frequency, toneFrequency);
   }

   public int mapToneVolume(int toneVolume) {
      this.array_volume = this.getArrayVolume();
      return this.mappingArgument(this.array_volume, toneVolume);
   }

   private int[] getArrayDuration() {
      return this.toneInd_duration_values;
   }

   private int[][] getArrayFrequencies() {
      return this.toneInd_frequency_values;
   }

   private int[][] getArrayVolume() {
      return this.toneInd_volume_values;
   }

   private int getDurationUnits() {
      return this.toneIndicator_duration_units;
   }

   private int mappingArgument(int[][] array_device_property, int toneArgument) {
      if (array_device_property.length == 0) {
         throw new IllegalArgumentException("There is not any device argument value to match with, array is empty");
      } else {
         if (array_device_property.length == 1) {
            toneArgument = array_device_property[1][this.element];
         } else if (toneArgument <= array_device_property[0][0]) {
            toneArgument = array_device_property[1][this.position];
         } else if (toneArgument >= array_device_property[0][array_device_property[0].length - 1]) {
            toneArgument = array_device_property[1][array_device_property[1].length - 1];
         } else {
            for (this.position = 0; this.position < array_device_property[0].length && !this.valueMappedFound; this.position++) {
               if (this.position + 1 < array_device_property[0].length) {
                  this.element = array_device_property[0][this.position];
                  this.next_element = array_device_property[0][this.position + 1];
                  if (toneArgument > this.element && toneArgument <= this.next_element) {
                     toneArgument = toneArgument <= (this.next_element - this.element) / 2 + this.element
                        ? array_device_property[1][this.position]
                        : array_device_property[1][this.position + 1];
                     this.valueMappedFound = true;
                  }
               }
            }
         }

         this.restart_Flag_ArrayCounter();
         return toneArgument;
      }
   }

   private void restart_Flag_ArrayCounter() {
      this.valueMappedFound = false;
      this.position = 0;
   }
}
