package com.ibm.posj;

public class DefaultFiscalPrinterCmd extends AbstractHandleCmd implements FiscalPrinterCmd {
   private String name = "";
   private int code = 0;
   protected HandleCmd.Result result = null;
   protected byte[] byteArray = new byte[0];
   public static final int FISCAL_PRINTER_WRITE_CODE = 1201;
   public static final String FISCAL_PRINTER_WRITE_NAME = "FISCAL_PRINTER_WRITE";

   DefaultFiscalPrinterCmd(HandleCmd.Factory factory, String name, int code) {
      super(factory);
      this.name = name;
      this.code = code;
   }

   public String getName() {
      return this.name;
   }

   public int getCode() {
      return this.code;
   }

   public byte[] toBytes() {
      return this.byteArray;
   }

   public void accept(HandleCmdVisitor visitor) {
      visitor.visitFiscalPrinterCmd(this);
   }

   public HandleCmd.Result getResult() {
      if (this.result == null) {
         this.result = new AbstractHandleCmd.DefaultResult();
      }

      return this.result;
   }

   static class Factory extends DefaultSystemCmd.Factory implements FiscalPrinterCmd.Factory {
      public FiscalPrinterCmd.FiscalWriteCmd createFiscalWriteCmd(byte[] data) {
         return new DefaultFiscalPrinterCmd.FiscalWriteCmd(this, data);
      }

      public SystemCmd.DeviceInfoRequestCmd createDeviceInfoRequestCmd() {
         byte[] getDeviceInfoCmd = new byte[]{27, 102, -8, 1};
         SystemCmd.DeviceInfoRequestCmd cmd = super.createDeviceInfoRequestCmd();
         cmd.setCmdBytes(getDeviceInfoCmd);
         return cmd;
      }
   }

   static class FiscalWriteCmd extends DefaultFiscalPrinterCmd implements FiscalPrinterCmd.FiscalWriteCmd {
      FiscalWriteCmd(HandleCmd.Factory factory, byte[] data) {
         super(factory, "FISCAL_PRINTER_WRITE", 1201);
         this.byteArray = data;
      }
   }
}
