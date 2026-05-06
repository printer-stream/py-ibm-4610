package com.ibm.hid.util;

import com.ibm.hid.HidAsync;
import com.ibm.hid.HidDevice;
import com.ibm.hid.HidException;
import com.ibm.hid.HidListener;

public class HidUtil {
   private HidUtil() {
   }

   public static HidDevice getSynchronizedHidDevice(HidDevice hidDevice) {
      return new HidUtil.SynchronizedHidDevice(hidDevice);
   }

   public static class SynchronizedHidDevice implements HidDevice {
      private HidDevice hidDevice = null;

      public SynchronizedHidDevice(HidDevice hidDevice) {
         this.hidDevice = hidDevice;
      }

      public synchronized void connect() throws HidException {
         this.hidDevice.connect();
      }

      public synchronized void disconnect() {
         this.hidDevice.disconnect();
      }

      public synchronized boolean isConnected() {
         return this.hidDevice.isConnected();
      }

      public synchronized byte[] getDescriptor(byte descriptorType, byte descriptorIndex) throws HidException {
         return this.hidDevice.getDescriptor(descriptorType, descriptorIndex);
      }

      public synchronized void setDescriptor(byte descriptorType, byte descriptorIndex, short descriptorLength, byte[] descriptor) throws HidException {
         this.hidDevice.setDescriptor(descriptorType, descriptorIndex, descriptorLength, descriptor);
      }

      public synchronized byte[] getReport(byte reportType, byte reportID) throws HidException {
         return this.hidDevice.getReport(reportType, reportID);
      }

      public synchronized void setReport(byte reportType, byte reportID, short reportLength, byte[] report) throws HidException {
         this.hidDevice.setReport(reportType, reportID, reportLength, report);
      }

      public synchronized byte getIdle(byte reportID) throws HidException {
         return this.hidDevice.getIdle(reportID);
      }

      public synchronized void setIdle(byte reportID, byte duration) throws HidException {
         this.hidDevice.setIdle(reportID, duration);
      }

      public synchronized byte getProtocol() throws HidException {
         return this.hidDevice.getProtocol();
      }

      public synchronized void setProtocol(byte protocol) throws HidException, IllegalArgumentException {
         this.hidDevice.setProtocol(protocol);
      }

      public synchronized void async(HidAsync hidAsync) {
         this.hidDevice.async(hidAsync);
      }

      public synchronized void addHidListener(HidListener listener) {
         this.hidDevice.addHidListener(listener);
      }

      public synchronized void removeHidListener(HidListener listener) {
         this.hidDevice.removeHidListener(listener);
      }

      public synchronized HidDevice getSynchronizedHidDevice() {
         return this;
      }

      public synchronized HidDevice getUnsynchronizedHidDevice() {
         return this.hidDevice;
      }
   }
}
