package com.ibm.posj;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;

public class StatisticDefinition {
   private String precomputedValue = "";
   private boolean hasPCV = false;
   private String name;
   private String categoryName;
   private float factor;
   private boolean isSupported;
   private int[] requestBytes;
   private boolean hasRemainder;
   Tracer tracer = TracerFactory.getInstance().createGlobalTracer();

   public StatisticDefinition(String name, String categoryName, float factor, boolean isSupported, int[] requestBytes, boolean hasRemainder) {
      this.name = name;
      this.categoryName = categoryName;
      this.factor = factor;
      this.isSupported = isSupported;
      this.requestBytes = requestBytes;
      this.hasRemainder = hasRemainder;
   }

   public StatisticDefinition(String name, String categoryName, float factor, boolean isSupported, int[] requestBytes) {
      this.name = name;
      this.categoryName = categoryName;
      this.factor = factor;
      this.isSupported = isSupported;
      this.requestBytes = requestBytes;
   }

   public StatisticDefinition(String name, String categoryName, float factor, boolean isSupported, boolean hasRemainder) {
      this.name = name;
      this.categoryName = categoryName;
      this.factor = factor;
      this.isSupported = isSupported;
      this.hasRemainder = hasRemainder;
   }

   public StatisticDefinition(String name, String categoryName, float factor, boolean isSupported) {
      this.name = name;
      this.categoryName = categoryName;
      this.factor = factor;
      this.isSupported = isSupported;
   }

   public StatisticDefinition(String name, String categoryName, float factor, boolean isSupported, String pcv) {
      this.name = name;
      this.categoryName = categoryName;
      this.factor = factor;
      this.isSupported = isSupported;
      this.hasPCV = true;
      if (pcv != null) {
         if (pcv.indexOf(0) != -1) {
            StringBuffer tmp = new StringBuffer();
            String[] toSet = pcv.split("\u0000");

            for (int i = 0; i < toSet.length; i++) {
               tmp.append(toSet[i]);
            }

            this.precomputedValue = tmp.toString();
            if (this.tracer.isOn()) {
               this.tracer.println("While loading statistics property '" + name + "', a null " + "character was found. It is being removed.");
            }
         } else {
            this.precomputedValue = pcv;
         }
      }
   }

   public float getFactor() {
      return this.factor;
   }

   public boolean isSupported() {
      return this.isSupported;
   }

   public String getName() {
      return this.name;
   }

   public String getCategoryName() {
      return this.categoryName;
   }

   public int[] getRequestBytes() {
      return this.requestBytes;
   }

   public boolean hasRemainder() {
      return this.hasRemainder;
   }

   public String getPrecomputedValue() {
      return this.precomputedValue;
   }

   public boolean hasPrecomputedValue() {
      return this.hasPCV;
   }
}
