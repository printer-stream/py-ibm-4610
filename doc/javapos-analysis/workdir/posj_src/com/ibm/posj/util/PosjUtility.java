package com.ibm.posj.util;

import com.ibm.jutil.FileUtil;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.swing.ImageIcon;

public class PosjUtility {
   public static final String IMAGES_PATH_STRING = "com/ibm/jpos/res/images/";

   private PosjUtility() {
   }

   public static ImageIcon getTreeImage(String imageFileName) {
      ImageIcon treeImageIcon = null;

      try {
         Vector v = new Vector();
         InputStream is = FileUtil.loadFile(imageFileName, true, true);
         if (is == null) {
            is = FileUtil.loadFile("com/ibm/jpos/res/images/unknown.gif", true, true);
         }

         InputStream var11 = new BufferedInputStream(is);

         while (var11.available() > 0) {
            byte[] buffer = new byte[var11.available()];
            var11.read(buffer);
            v.addElement(buffer);
         }

         int size = 0;

         for (int i = 0; i < v.size(); i++) {
            byte[] buffer = (byte[])v.elementAt(i);
            size += buffer.length;
         }

         byte[] bigBuffer = new byte[size];
         int dstPos = 0;

         for (int i = 0; i < v.size(); i++) {
            byte[] buffer = (byte[])v.elementAt(i);
            System.arraycopy(buffer, 0, bigBuffer, dstPos, buffer.length);
            dstPos += buffer.length;
         }

         treeImageIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(bigBuffer));
      } catch (IOException var9) {
         treeImageIcon = null;
      }

      return treeImageIcon;
   }
}
