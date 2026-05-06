package com.ibm.rs232;

public class Rs232Config {
   private String name = "";
   private String portName = "";
   private int baudRate = -1;
   private int dataBits = -1;
   private int parity = -1;
   private int stopBits = -1;
   private int flowControl = -1;

   public Rs232Config(String portName, int baudRate, int dataBits, int parity, int stopBits, int flowControl) {
      this.portName = portName;
      this.baudRate = baudRate;
      this.dataBits = dataBits;
      this.parity = parity;
      this.stopBits = stopBits;
      this.flowControl = flowControl;
      StringBuffer sb = new StringBuffer();
      sb.append("<Rs232Config:");
      sb.append(" portName=" + this.getPortName());
      sb.append(" baudRate=" + this.getBaudRate());
      sb.append(" dataBits=" + this.getDataBits());
      sb.append(" parity=" + this.getParity());
      sb.append(" stopBits=" + this.getStopBits());
      sb.append(" flowControl=" + this.getFlowControl() + "\"/>");
      this.name = sb.toString();
   }

   public String getPortName() {
      return this.portName;
   }

   public int getBaudRate() {
      return this.baudRate;
   }

   public int getDataBits() {
      return this.dataBits;
   }

   public int getParity() {
      return this.parity;
   }

   public int getStopBits() {
      return this.stopBits;
   }

   public int getFlowControl() {
      return this.flowControl;
   }

   public String toString() {
      return this.name;
   }

   public boolean equals(Object obj) {
      try {
         return ((Rs232Config)obj).toString().equals(this.toString());
      } catch (NullPointerException var3) {
         return false;
      } catch (ClassCastException var4) {
         return false;
      }
   }
}
