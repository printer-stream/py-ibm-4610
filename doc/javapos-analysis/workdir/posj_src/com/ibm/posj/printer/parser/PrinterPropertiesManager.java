package com.ibm.posj.printer.parser;

import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterCmdVisitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PrinterPropertiesManager {
   boolean newElement = true;
   POSPrinterCmd masterPropertiesCmd = null;
   List propertiesList = new ArrayList(20);
   List rememberedCmdList = new ArrayList(20);
   PrinterEscCmdProcessor escCmdProc = null;

   public synchronized void setEscCmdProcessor(PrinterEscCmdProcessor escCmdProc) throws IllegalArgumentException {
      if (escCmdProc == null) {
         throw new IllegalArgumentException("Invalid PrinterEscCmdProcessor parameter");
      } else {
         this.escCmdProc = escCmdProc;
      }
   }

   public synchronized POSPrinterCmd.Factory getFactory() {
      return this.escCmdProc.getFactory();
   }

   public synchronized POSPrinterCmdVisitor getClonCmdVisitor() {
      return this.escCmdProc.getClonPOSPrinterCmdVisitor();
   }

   public synchronized void append(POSPrinterCmd printerCmd, byte type) {
      if (printerCmd instanceof POSPrinterCmd.NormalModeCmd) {
         this.propertiesList.clear();
         this.rememberedCmdList.clear();
      }

      this.propertiesList.add(printerCmd);
      if (type == 0) {
         this.rememberedCmdList.add(printerCmd);
      }
   }

   public synchronized void reset() {
      this.propertiesList.clear();
      Iterator it = this.rememberedCmdList.iterator();

      while (it.hasNext()) {
         this.propertiesList.add((POSPrinterCmd)it.next());
      }
   }

   public synchronized void clear() {
      this.propertiesList.clear();
      this.rememberedCmdList.clear();
   }

   public synchronized POSPrinterCmd masterPropertiesCmd() {
      return this.getFactory().createNormalModeCmd(true);
   }

   public synchronized boolean arePropertiesPresent() {
      return this.propertiesList.size() > 0;
   }

   public synchronized POSPrinterCmd getPOSMasterCmd(boolean clear) {
      this.newElement = true;
      this.masterPropertiesCmd = null;
      Iterator it = this.propertiesList.iterator();

      while (it.hasNext()) {
         this.appendToMasterProperties((POSPrinterCmd)it.next());
      }

      return this.masterPropertiesCmd;
   }

   public void appendToMasterProperties(POSPrinterCmd command) {
      POSPrinterCmd commandToAppend = null;
      this.getClonCmdVisitor().reset();
      command.accept(this.getClonCmdVisitor());
      commandToAppend = (POSPrinterCmd)this.getClonCmdVisitor().getData();
      if (commandToAppend != null) {
         if (this.masterPropertiesCmd == null) {
            this.masterPropertiesCmd = this.masterPropertiesCmd();
            if (!(commandToAppend instanceof POSPrinterCmd.NormalModeCmd)) {
               this.masterPropertiesCmd.appendPOSPrinterCmd(commandToAppend);
            }

            this.newElement = false;
         } else {
            this.masterPropertiesCmd.appendPOSPrinterCmd(commandToAppend);
         }
      }
   }
}
