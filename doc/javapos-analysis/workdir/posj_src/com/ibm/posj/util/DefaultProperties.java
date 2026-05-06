package com.ibm.posj.util;

import com.ibm.jutil.DefaultUtilProperties;
import com.ibm.jutil.UtilProperties.MultiProperty;
import com.ibm.posj.PosSystem;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

public class DefaultProperties extends DefaultUtilProperties implements PosSystem.Properties {
   public DefaultProperties() {
      super("posj.properties");
   }

   public DefaultProperties(String propFileName) {
      super(propFileName);
   }

   public Enumeration getPropertyValuesWithPattern(String propNamePattern) {
      Vector vector = new Vector();
      if (this.hasMultiProperty(propNamePattern)) {
         MultiProperty multiProp = this.getMultiProperty(propNamePattern);
         Iterator values = multiProp.getPropertyValues();

         while (values.hasNext()) {
            vector.addElement(values.next().toString());
         }
      }

      return vector.elements();
   }
}
