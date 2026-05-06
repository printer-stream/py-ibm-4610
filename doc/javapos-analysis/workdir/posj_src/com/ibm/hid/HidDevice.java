package com.ibm.hid;

public interface HidDevice {
   void connect() throws HidException;

   void disconnect();

   boolean isConnected();

   byte[] getDescriptor(byte var1, byte var2) throws HidException;

   void setDescriptor(byte var1, byte var2, short var3, byte[] var4) throws HidException;

   byte[] getReport(byte var1, byte var2) throws HidException;

   void setReport(byte var1, byte var2, short var3, byte[] var4) throws HidException;

   byte getIdle(byte var1) throws HidException;

   void setIdle(byte var1, byte var2) throws HidException;

   byte getProtocol() throws HidException;

   void setProtocol(byte var1) throws HidException, IllegalArgumentException;

   void async(HidAsync var1);

   void addHidListener(HidListener var1);

   void removeHidListener(HidListener var1);

   HidDevice getSynchronizedHidDevice();

   HidDevice getUnsynchronizedHidDevice();
}
