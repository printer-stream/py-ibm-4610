package com.ibm.posj.bus.poskbd;

import com.ibm.posj.AbstractToneIndicatorMapping;

public class PosKbdToneIndicatorMappingImp extends AbstractToneIndicatorMapping {
   private int[][] ti_frequency_values = new int[][]{{875, 1300, 2000}, {3, 4, 5}};
   private int[][] ti_volume_values = new int[][]{{1, 100}, {1, 2}};
   private int[] ti_duration_values = new int[256];
   private int duration = 0;
   private static final int TI_VOLUME_LOW = 1;
   private static final int TI_VOLUME_HIGH = 100;
   private static final int TI_MIN_DURATION = 0;
   private static final int TI_MAX_DURATION = 256;
   private static final int TONEIND_SOUND_DURATION_UNITS = 100;

   public PosKbdToneIndicatorMappingImp() {
      this.fillToneInd_duration();
      this.toneIndicator_duration_units = 100;
      this.toneInd_frequency_values = this.ti_frequency_values;
      this.toneInd_volume_values = this.ti_volume_values;
   }

   private void fillToneInd_duration() {
      this.duration = 0;

      while (this.duration < 256) {
         this.ti_duration_values[this.duration] = this.duration++;
      }

      this.toneInd_duration_values = this.ti_duration_values;
   }
}
