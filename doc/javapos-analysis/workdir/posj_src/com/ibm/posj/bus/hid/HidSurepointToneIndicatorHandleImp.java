package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.posj.HandleKey;

public class HidSurepointToneIndicatorHandleImp extends HidToneIndicatorHandleImp {
   public HidSurepointToneIndicatorHandleImp(HandleKey key, HidDevice device, HidToneIndicatorStrategy strategy) {
      super(key, device, strategy);
   }

   public int[][] getFrecuencyValues() {
      int[][] var10000 = new int[][]{{600, 1010, 1603}, null};
      int[] var10003 = new int[3];
      this.getGenTICmd();
      var10003[0] = 0;
      this.getGenTICmd();
      var10003[1] = 8;
      this.getGenTICmd();
      var10003[2] = 16;
      var10000[1] = var10003;
      return var10000;
   }
}
