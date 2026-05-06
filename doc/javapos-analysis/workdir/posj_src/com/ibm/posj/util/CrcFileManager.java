package com.ibm.posj.util;

import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.PosSystem;
import com.ibm.posj.PosSystemManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class CrcFileManager {
   private static Tracer tracer = TracerFactory.getInstance().createTracer("CrcFileManager");
   private String checkSum;
   private String serialNumber;
   private String fileName;
   private BufferedReader inFile;
   private BufferedWriter outFile;
   private Hashtable bmpStored = new Hashtable();
   public static final String BITMAP_FILE_PROP_NAME = "com.ibm.posj.util.CrcFileLocation";
   private static final String DEFAULT_BITMAP_FILE_LOCATION = "~/.ibmjpos";
   private static final String BITMAP_FILE_NAME = "ibmjposcrc";
   private static final int NUMBER_OF_ELEMENTS = 8;

   public CrcFileManager(String sNumber) {
      tracer.println("CrcFileManager  -> serialNumber" + sNumber);
      if (sNumber != null && sNumber.length() > 1) {
         this.serialNumber = sNumber.trim().replace(' ', '_');
         this.fileName = this.getLocation() + "ibmjposcrc" + sNumber + ".dat";
      } else {
         this.serialNumber = "";
         this.fileName = this.getLocation() + "ibmjposcrc" + ".dat";
      }

      tracer.println("filename " + this.fileName);

      try {
         File f = new File(this.getLocation());
         boolean exist = f.exists();
         if (!exist) {
            tracer.println(this.getLocation() + " does not exist, create it");
            f.mkdir();
         }

         new File(this.fileName).createNewFile();
         this.bmpStored.clear();
      } catch (IOException var4) {
         tracer.println("Could not create the CrcFileManager File \n" + var4);
      } catch (SecurityException var5) {
         tracer.println("Could not create the CrcFileManager File \n" + var5);
      }

      this.loadFile();
   }

   public boolean hasElement(CrcFile crcFile) {
      return crcFile == null ? false : this.bmpStored.containsValue(crcFile);
   }

   public boolean hasElementAt(int number, int station) {
      String key = station + "" + number;
      return this.bmpStored.containsKey(key);
   }

   public void addElement(CrcFile crcFile, byte[] ckSum) {
      if (tracer.isOn()) {
         tracer.println("addElement -->" + crcFile);
      }

      if (crcFile != null && ckSum != null) {
         this.checkSum = Util.toFormatedHexString(ckSum);
         String key = crcFile.getStation() + "" + crcFile.getNumber();
         this.bmpStored.put(key, crcFile);
         this.saveFile();
         if (tracer.isOn()) {
            tracer.println("<--addElement ");
         }
      } else {
         if (tracer.isOn()) {
            tracer.println("element null, not added");
         }
      }
   }

   public void removeElement(int number, int station, byte[] ckSum) {
      tracer.println("removeElement");
      if (ckSum == null) {
         tracer.println("element not added");
      } else {
         this.checkSum = Util.toFormatedHexString(ckSum);
         String key = station + "" + number;
         this.bmpStored.remove(key);
         this.saveFile();
         tracer.println(this.toString());
      }
   }

   public boolean isTotalsCrcEqual(byte[] ckSum) {
      return this.checkSum != null && ckSum != null && this.checkSum.equalsIgnoreCase(Util.toFormatedHexString(ckSum));
   }

   public void reset() {
      this.removeFile();
      this.bmpStored.clear();
   }

   public int getSize() {
      return this.bmpStored.size();
   }

   public Enumeration getElements() {
      return this.bmpStored.elements();
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("Checksum -");
      sb.append(this.checkSum);
      sb.append("\n");
      sb.append("SerialNumber -");
      sb.append(this.serialNumber);
      sb.append("\n");
      Enumeration crcFiles = this.bmpStored.elements();

      while (crcFiles.hasMoreElements()) {
         sb.append(crcFiles.nextElement().toString()).append("\n");
      }

      return sb.toString();
   }

   public String getLocation() {
      PosSystem.Properties prop = PosSystemManager.getInstance().getProperties();
      if (!prop.isLoaded()) {
         prop.loadProperties();
      }

      String location;
      if (prop.isPropertyDefined("com.ibm.posj.util.CrcFileLocation")) {
         location = prop.getPropertyString("com.ibm.posj.util.CrcFileLocation");
      } else {
         location = "~/.ibmjpos";
      }

      if (location.startsWith("~")) {
         location = System.getProperty("user.home") + location.substring(1);
      }

      if (!location.endsWith(File.separator)) {
         location = location + File.separator;
      }

      return location;
   }

   private void loadFile() {
      try {
         tracer.println("loadFile->");
         this.inFile = new BufferedReader(new InputStreamReader(new FileInputStream(this.fileName)));
         this.checkSum = this.inFile.readLine();

         String s;
         while ((s = this.inFile.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(s, ",");
            if (st.countTokens() == 8) {
               CrcFile crcFile = new CrcFile();

               while (st.hasMoreTokens()) {
                  String pair = st.nextToken();
                  int index = pair.lastIndexOf("=");
                  if (index != -1) {
                     String name = pair.substring(0, index);
                     String value = pair.substring(index + 1);
                     if (name.equals("Number")) {
                        crcFile.setNumber(Integer.parseInt(value));
                     } else if (name.equals("Crc")) {
                        crcFile.setCrc(Integer.parseInt(value));
                     } else if (name.equals("Station")) {
                        crcFile.setStation(Integer.parseInt(value));
                     } else if (name.equals("Width")) {
                        crcFile.setWidth(Integer.parseInt(value));
                     } else if (name.equals("Alignment")) {
                        crcFile.setAlignment(Integer.parseInt(value));
                     } else if (name.equals("TabAligment")) {
                        crcFile.setTabAlignment(Integer.parseInt(value));
                     } else if (name.equals("FileName")) {
                        crcFile.setFileName(value.trim());
                     } else if (name.equals("FileSize")) {
                        crcFile.setFileSize(Integer.parseInt(value));
                     }
                  }
               }

               String key = crcFile.getStation() + "" + crcFile.getNumber();
               this.bmpStored.put(key, crcFile);
            } else if (tracer.isOn()) {
               tracer.println("line in file does not match the format " + st.toString());
            }
         }

         this.inFile.close();
         if (tracer.isOn()) {
            tracer.println("<--loadFile" + this);
         }
      } catch (FileNotFoundException var9) {
         tracer.println(this.fileName + " not found " + var9.getCause());
      } catch (IOException var10) {
         tracer.println("IOException while loadinf file " + var10.getCause());
      }
   }

   private void saveFile() {
      this.removeFile();

      try {
         this.outFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.fileName)));
         this.writeLine(this.checkSum);
         Enumeration crcFiles = this.bmpStored.elements();

         while (crcFiles.hasMoreElements()) {
            this.writeLine(crcFiles.nextElement().toString());
         }

         this.outFile.flush();
         this.outFile.close();
      } catch (FileNotFoundException var2) {
         tracer.println(this.fileName + " not found " + var2);
      } catch (IOException var3) {
         tracer.println("IOError saving file" + var3.getCause());
      }
   }

   private void writeLine(String s) {
      try {
         this.outFile.write(s, 0, s.length());
         this.outFile.newLine();
      } catch (IOException var3) {
         tracer.println(" error writing line to file " + var3);
      }
   }

   private void removeFile() {
      try {
         new File(this.fileName).delete();
      } catch (SecurityException var2) {
         tracer.println("Could not delete the " + this.fileName + " \n" + var2);
      }
   }
}
