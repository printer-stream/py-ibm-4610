package com.ibm.posj.bus.printer.cmds.ibm4689;

import com.ibm.posj.bus.printer.cmds.AbstractByteCmd;
import com.ibm.posj.bus.printer.cmds.DefaultCmdList;
import com.ibm.posj.bus.printer.cmds.FontCmdFactory;
import com.ibm.posj.bus.printer.cmds.GeneralCmdFactory;
import com.ibm.posj.bus.printer.cmds.GraphicCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;

public class Print4689CmdFactory extends PrintCmdFactory {
   private Gen4689CmdFactory general;
   private GraphicCmdFactory graphic;
   private FontCmdFactory font;
   private AbstractByteCmd cmdBytes = null;
   public static final int INITIAL_CMDCNT = 10;

   public Print4689CmdFactory(int maxSize, AbstractByteCmd abCmd) {
      super(10, maxSize);
      this.general = new Gen4689CmdFactory(this);
      this.graphic = new Grap4689CmdFactory(this);
      this.font = new Font4689CmdFactory(this);
      this.cmdBytes = abCmd;
   }

   public void setCmdsStation(PrintCmd ppiCmd, byte station) {
      switch (station) {
         case 2:
            ppiCmd.appendCmd(this.getCmdBytes().CR_COMM);
            break;
         case 8:
            ppiCmd.appendCmd(((Cmd4689)this.getCmdBytes()).JR_COMM);
            break;
         case 32:
            ppiCmd.appendCmd(((Cmd4689)this.getCmdBytes()).JR_CR_COMM);
      }
   }

   public AbstractByteCmd getCmdBytes() {
      if (null == this.cmdBytes) {
         this.cmdBytes = new Cmd4689();
      }

      return this.cmdBytes;
   }

   public PrintCmdList createPrintCmdList() {
      return DefaultCmdList.getFactory().createPrintCmdList(this.getMaxCmdLen());
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
}
