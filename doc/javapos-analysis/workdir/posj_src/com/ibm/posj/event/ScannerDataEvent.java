package com.ibm.posj.event;

import com.ibm.posj.ScannerConfig;

public class ScannerDataEvent extends DataEvent {
   private int type = 0;
   private ScannerConfig config = null;

   public ScannerDataEvent(Object source, byte[] data, int type) {
      super(source, data);
      this.type = type;
   }

   public ScannerDataEvent(Object source, ScannerConfig config) {
      super(source);
      this.config = config;
   }

   public int getType() {
      return this.type;
   }

   public ScannerConfig getScannerConfig() {
      return this.config;
   }
}
