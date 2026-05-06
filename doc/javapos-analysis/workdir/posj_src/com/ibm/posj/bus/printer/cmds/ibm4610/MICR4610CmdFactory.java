package com.ibm.posj.bus.printer.cmds.ibm4610;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.bus.printer.cmds.MICRCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;

public class MICR4610CmdFactory implements MICRCmdFactory {
   PrintCmdFactory factory;

   public MICR4610CmdFactory(PrintCmdFactory base) {
      this.factory = base;
   }

   private PrintCmd createCmd(DefaultPOSPrinterCmd cmd) {
      if (cmd != null && !cmd.isPrintCmdNull()) {
         PrintCmd ret = cmd.getPrintCmd();
         ret.setBytes(cmd.getOutboundPacket());
         return ret;
      } else {
         return this.factory.createCmd();
      }
   }

   private Cmd4610 getEscBytes() {
      return (Cmd4610)this.factory.getCmdBytes();
   }

   public PrintCmd createEndRegisterSlipCmd(DefaultPOSPrinterCmd.EndRegisterSlipCmd gsc) {
      PrintCmd ret = this.createCmd(gsc);
      ret.appendCmd(this.getEscBytes().END_REGISTER_DOC);
      return ret;
   }

   public PrintCmd createReadSlipCmd(DefaultPOSPrinterCmd.ReadSlipCmd rsc) {
      PrintCmd ret = this.createCmd(rsc);
      ret.appendCmd(this.getEscBytes().MICR_READ);
      return ret;
   }

   public PrintCmd createEjectSlipCmd(DefaultPOSPrinterCmd.EjectSlipCmd esc) {
      PrintCmd ret = this.createCmd(esc);
      ret.appendCmd(this.getEscBytes().DI_EJECT);
      return ret;
   }

   public PrintCmd createRegisterSlipCmd(DefaultPOSPrinterCmd.RegisterSlipCmd rsc) {
      PrintCmd ret = this.createCmd(rsc);
      ret.appendCmd(this.getEscBytes().DIP_COMM);
      if (rsc.isGrabber()) {
         ret.appendCmd(this.getEscBytes().GRAB_SLIP);
      } else {
         ret.appendCmd(this.getEscBytes().REGISTER_DOC);
      }

      return ret;
   }
}
