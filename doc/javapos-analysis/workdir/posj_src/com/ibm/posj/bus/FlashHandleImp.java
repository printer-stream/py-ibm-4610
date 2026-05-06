package com.ibm.posj.bus;

import com.ibm.posj.Handle;
import com.ibm.posj.flash.FlashException;
import com.ibm.posj.flash.FlashFile;
import com.ibm.posj.flash.FlashRequest;
import java.util.ArrayList;
import java.util.Vector;

public interface FlashHandleImp extends HandleImp {
   void setFlashRequest(FlashRequest var1);

   void flash() throws FlashException;

   void flashPOSPrinter(Handle var1, Vector var2, ArrayList var3) throws FlashException;

   void reset() throws FlashException;

   void flashPOSPrinterSDICC(Handle var1, String var2, String var3) throws FlashException;

   short getProductID() throws FlashException;

   String[] getFlashFileNames(Handle var1);

   void setPrtFlashFile(FlashFile var1);

   FlashFile getPrtFlashFile();
}
