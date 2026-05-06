package com.ibm.posj.flash;

import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import java.util.Hashtable;

public class FlashDevCatsTable {
   private Hashtable filenameHashtable = new Hashtable();
   protected static FlashDevCatsTable instance = null;
   public static final String PRINTER_4689_FILENAME = "aip4537.dat";

   protected FlashDevCatsTable() {
   }

   public static FlashDevCatsTable getInstance() {
      if (instance == null) {
         instance = new FlashDevCatsTable();
         instance.init();
      }

      return instance;
   }

   public DevCat getDevCat(String FlashFilename) {
      DevCat devCat = (DevCat)this.filenameHashtable.get(FlashFilename);
      if (devCat == null) {
         devCat = DevCats.UNKNOWN_DEVCAT;
      }

      return devCat;
   }

   protected void init() {
      this.filenameHashtable.put("aip4535.dat", DevCats.POSPRINTER_DEVCAT);
      this.filenameHashtable.put("aip4537.dat", DevCats.POSPRINTER_DEVCAT);
      this.filenameHashtable.put("aip4524.dat", DevCats.LINEDISPLAY_DEVCAT);
      this.filenameHashtable.put("aip4525.dat", DevCats.LINEDISPLAY_DEVCAT);
      this.filenameHashtable.put("aip4526.dat", DevCats.LINEDISPLAY_DEVCAT);
      this.filenameHashtable.put("aip452A.dat", DevCats.LINEDISPLAY_DEVCAT);
      this.filenameHashtable.put("aip4601.dat", DevCats.POSKEYBOARD_DEVCAT);
      this.filenameHashtable.put("aip4602.dat", DevCats.POSKEYBOARD_DEVCAT);
      this.filenameHashtable.put("aip4603.dat", DevCats.POSKEYBOARD_DEVCAT);
      this.filenameHashtable.put("aip4604.dat", DevCats.POSKEYBOARD_DEVCAT);
      this.filenameHashtable.put("aip4605.dat", DevCats.POSKEYBOARD_DEVCAT);
      this.filenameHashtable.put("aip4606.dat", DevCats.POSKEYBOARD_DEVCAT);
      this.filenameHashtable.put("aip4607.dat", DevCats.POSKEYBOARD_DEVCAT);
      this.filenameHashtable.put("aip46mch.hex", DevCats.POSPRINTER_DEVCAT);
      this.filenameHashtable.put("aip46mc.hex", DevCats.POSPRINTER_DEVCAT);
      this.filenameHashtable.put("aip46mcd.hex", DevCats.POSPRINTER_DEVCAT);
   }
}
