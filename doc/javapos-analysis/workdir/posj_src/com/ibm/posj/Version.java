package com.ibm.posj;

public final class Version {
   public static String VERSION_STRING = null;

   public static void main(String[] args) {
      System.out.println(getVersionString());
   }

   public static void makeVersion(String parse, String dparse) {
      int idx = parse.lastIndexOf(32);
      String RELEASE = 'v' + parse.substring(parse.lastIndexOf(32, idx - 2) + 5);
      RELEASE = RELEASE.substring(0, RELEASE.indexOf(44)) + '-';
      String tVERSION = null;

      try {
         tVERSION = parse.substring(idx + 1, parse.length());
         Integer.parseInt(tVERSION);
      } catch (Exception var9) {
         tVERSION = "22";
      }

      String DATE = null;
      int dplen = dparse.length();

      try {
         DATE = "20" + dparse.substring(0, 2) + dparse.substring(dplen - 5, dplen - 3) + dparse.substring(dplen - 2, dplen);
      } catch (Exception var8) {
         DATE = "20050107";
      }

      VERSION_STRING = RELEASE + DATE + "L" + tVERSION;
   }

   public static String getVersionString() {
      makeVersion("src/com/ibm/posj/Version.java, posj.src, posj1.9.2.1, 33", "06/10/12");
      VERSION_STRING = "POSj (Point of Sale System for Java) " + VERSION_STRING;
      return VERSION_STRING;
   }
}
