package com.ibm.posj.bus.printer.cmds.ibm4610;

import com.ibm.posj.bus.printer.cmds.AbstractByteCmd;
import com.ibm.posj.bus.printer.cmds.CheckScannerCmdFactory;
import com.ibm.posj.bus.printer.cmds.DefaultCmdList;
import com.ibm.posj.bus.printer.cmds.FontCmdFactory;
import com.ibm.posj.bus.printer.cmds.GeneralCmdFactory;
import com.ibm.posj.bus.printer.cmds.GraphicCmdFactory;
import com.ibm.posj.bus.printer.cmds.MICRCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;

public class Print4610CmdFactory extends PrintCmdFactory {
   private General4610CmdFactory general;
   private boolean oldModel = false;
   private GraphicCmdFactory graphic;
   private FontCmdFactory font;
   private MICRCmdFactory micr;
   private CheckScannerCmdFactory checkScanner;
   private AbstractByteCmd cmdBytes = null;
   public static final int INITIAL_CMDCNT = 10;

   public Print4610CmdFactory(int maxSize, AbstractByteCmd abCmd, boolean useLineCnt) {
      super(10, maxSize);
      this.general = new Gen4610CmdFactory(this);
      ((Gen4610CmdFactory)this.general).setLineCnt(useLineCnt);
      this.graphic = new Grap4610CmdFactory(this);
      this.font = new Font4610CmdFactory(this);
      this.micr = new MICR4610CmdFactory(this);
      this.checkScanner = new CheckScanner4610CmdFactory(this);
      this.cmdBytes = abCmd;
   }

   public void setCmdsStation(PrintCmd ppiCmd, byte station) {
      switch (station) {
         case 2:
            ppiCmd.appendCmd(this.getCmdBytes().CR_COMM);
            ppiCmd.appendCmd(((Cmd4610)this.getCmdBytes()).CR_SETTINGS);
            break;
         case 4:
            ppiCmd.appendCmd(this.getCmdBytes().DIP_COMM);
            ppiCmd.appendCmd(((Cmd4610)this.getCmdBytes()).DIP_SETTINGS);
            break;
         case 20:
            ppiCmd.appendCmd(this.getCmdBytes().DIL_COMM);
      }
   }

   public AbstractByteCmd getCmdBytes() {
      if (null == this.cmdBytes) {
         this.cmdBytes = new Cmd4610();
      }

      return this.cmdBytes;
   }

   public PrintCmdList createPrintCmdList() {
      return DefaultCmdList.getFactory().createPrintCmdList(this.getMaxCmdLen());
   }

   public boolean isOlderModel() {
      return this.oldModel;
   }

   public void setOlderModel(boolean b) {
      this.oldModel = b;
   }

   public GeneralCmdFactory getGeneralFactory() {
      return this.general;
   }

   public GraphicCmdFactory getGraphicFactory() {
      return this.graphic;
   }

   public FontCmdFactory getFontFactory() {
      return this.font;
   }

   public MICRCmdFactory getMICRFactory() {
      return this.micr;
   }

   public CheckScannerCmdFactory getCheckScannerFactory() {
      return this.checkScanner;
   }
}
