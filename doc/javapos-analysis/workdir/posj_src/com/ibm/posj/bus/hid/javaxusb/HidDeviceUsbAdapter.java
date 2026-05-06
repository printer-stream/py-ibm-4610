package com.ibm.posj.bus.hid.javaxusb;

import com.ibm.hid.DisconnectEvent;
import com.ibm.hid.HidAsync;
import com.ibm.hid.HidDevice;
import com.ibm.hid.HidException;
import com.ibm.hid.HidExceptionEvent;
import com.ibm.hid.HidListener;
import com.ibm.hid.ReportEvent;
import com.ibm.hid.util.HidUtil;
import com.ibm.jutil.RunnableManager;
import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.event.EventListenerImp;
import java.util.List;
import java.util.Vector;
import javax.usb.UsbClaimException;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbPipe;
import javax.usb.UsbStallException;
import javax.usb.event.UsbDeviceDataEvent;
import javax.usb.event.UsbDeviceErrorEvent;
import javax.usb.event.UsbDeviceEvent;
import javax.usb.event.UsbDeviceListener;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;
import javax.usb.util.StandardRequest;

public class HidDeviceUsbAdapter implements HidDevice {
   protected boolean connected = false;
   private RunnableManager asyncManager = new RunnableManager();
   protected UsbDevice usbDevice = null;
   protected UsbInterface usbInterface = null;
   protected List usbEndpoints = new Vector();
   protected UsbEndpoint outEndpoint = null;
   protected short idVendor = 0;
   protected short idProduct = 0;
   protected String idVendorString = null;
   protected String idProductString = null;
   protected String idString = null;
   protected byte bInterfaceNumber = 0;
   protected String bInterfaceNumberString = null;
   private UsbDeviceListener disconnectListener = new HidDeviceUsbAdapter.UsbDeviceDisconnectListener(null);
   private HidDeviceUsbAdapter.UsbPipeErrorListener outListener = new HidDeviceUsbAdapter.UsbPipeErrorListener();
   private HidDeviceUsbAdapter.ReportListener inListener = new HidDeviceUsbAdapter.ReportListener(null);
   private HidDeviceUsbAdapter.HidListenerHelper hlHelper = new HidDeviceUsbAdapter.HidListenerHelper(null);
   private HidDeviceUsbAdapter.AsyncVisitor asyncVisitor = new HidDeviceUsbAdapter.AsyncVisitor(null);
   private HidDevice synchronizedHidDevice = null;
   protected Tracer tracer;
   private static int instanceNumber = 1;
   private int usbStallExceptionRetry = 0;
   public static final int INT_BUFFERING = 1;
   public static final short DESCRIPTOR_MAX_LENGTH = 255;
   public static final short REPORT_INITIAL_LENGTH = 511;
   public static final short REPORT_INCREMENT_LENGTH = 255;
   public static final int MAX_ERROR_EVENTS = 10;
   protected static int MAX_RETRIES = 3;

   public HidDeviceUsbAdapter(UsbInterface uI) {
      this.init(uI);
   }

   public synchronized void connect() throws HidException {
      if (!this.connected) {
         if (this.tracer.isOn()) {
            this.tracer
               .println(
                  "Connecting HidDevice (hashCode "
                     + this.hashCode()
                     + ") to UsbDevice "
                     + this.idString
                     + " (hashCode "
                     + this.usbDevice.hashCode()
                     + ") UsbInterface "
                     + this.bInterfaceNumberString
               );
         }

         try {
            this.usbInterface.claim(DefaultUsbInterfacePolicy.getInstance());
         } catch (UsbClaimException var3) {
            String errMsg = "UsbInterface is already claimed : " + var3.getMessage();
            if (this.tracer.isOn()) {
               this.tracer.println(errMsg);
            }

            throw new HidException(errMsg, var3);
         } catch (UsbException var4) {
            String errMsg = "Got UsbException while claiming : " + var4.getMessage();
            if (this.tracer.isOn()) {
               this.tracer.println(errMsg);
               this.tracer.print(var4);
            }

            throw new HidException(errMsg, var4);
         } catch (UsbDisconnectedException var5) {
            String errMsg = "UsbDevice has been disconnected : " + var5.getMessage();
            if (this.tracer.isOn()) {
               this.tracer.println(errMsg);
            }

            throw new HidException(errMsg, var5);
         } catch (UsbNotActiveException var6) {
            String errMsg = "This UsbInterface is not active : " + var6.getMessage();
            if (this.tracer.isOn()) {
               this.tracer.println(errMsg);
               this.tracer.print(var6);
            }

            throw new HidException(errMsg, var6);
         }

         this.usbDevice.addUsbDeviceListener(this.disconnectListener);

         for (UsbEndpoint ep : this.usbInterface.getUsbEndpoints()) {
            if (3 == ep.getType()) {
               if (-128 == ep.getDirection()) {
                  this.addUsbEndpointIn(ep);
               } else if (0 == ep.getDirection()) {
                  this.addUsbEndpointOut(ep);
               }
            }
         }

         if (this.tracer.isOn()) {
            this.tracer
               .println(
                  "Connected HidDevice (hashCode "
                     + this.hashCode()
                     + ") to UsbDevice "
                     + this.idString
                     + " (hashCode "
                     + this.usbDevice.hashCode()
                     + " ) successfully."
               );
         }

         this.connected = true;
      }
   }

   public void disconnect() {
      this.disconnect(null);
   }

   public boolean isConnected() {
      return this.connected;
   }

   public byte[] getDescriptor(byte descriptorType, byte descriptorIndex) throws HidException {
      return this.getDescriptor(descriptorType, descriptorIndex, true);
   }

   public byte[] getDescriptor(byte descriptorType, byte descriptorIndex, boolean sync) throws HidException {
      this.checkForConnection();
      if (this.tracer.isOn()) {
         this.tracer.println(3, "-->getDescriptor( " + Util.toHexString(descriptorType) + ", " + Util.toHexString(descriptorIndex) + ", " + sync + ")");
      }

      byte[] data = new byte[255];
      int length = this.submitRequest((byte)-127, (byte)6, (short)(descriptorType << 8 | descriptorIndex), data, sync);
      byte[] descriptor = new byte[length];
      System.arraycopy(data, 0, descriptor, 0, descriptor.length);
      if (this.tracer.isOn()) {
         this.tracer.println(3, "<--getDescriptor() = " + Util.toFormatedHexString(descriptor));
      }

      return descriptor;
   }

   public void setDescriptor(byte descriptorType, byte descriptorIndex, short descriptorLength, byte[] descriptor) throws HidException {
      this.setDescriptor(descriptorType, descriptorIndex, descriptorLength, descriptor, true);
   }

   public void setDescriptor(byte descriptorType, byte descriptorIndex, short descriptorLength, byte[] descriptor, boolean sync) throws HidException {
      this.checkForConnection();
      byte[] data = new byte[Util.unsignedInt(descriptorLength)];
      System.arraycopy(descriptor, 0, data, 0, Util.unsignedInt(descriptorLength));
      this.submitRequest((byte)1, (byte)7, (short)(descriptorType << 8 | descriptorIndex), data, sync);
   }

   public byte[] getReport(byte reportType, byte reportID) throws HidException {
      return this.getReport(reportType, reportID, true);
   }

   public byte[] getReport(byte reportType, byte reportID, boolean sync) throws HidException {
      this.checkForConnection();
      if (this.tracer.isOn()) {
         this.tracer.println(3, "-->getReport( " + Util.toHexString(reportType) + ", " + Util.toHexString(reportID) + ", " + sync + ")");
      }

      int attemptLen = 256;
      int length = 0;
      byte[] data = null;

      do {
         attemptLen += 255;
         if (attemptLen > 65535) {
            attemptLen = 65535;
         }

         data = new byte[attemptLen];
         length = this.submitRequest((byte)-95, (byte)1, (short)(reportType << 8 | reportID), data, sync);
      } while (length == attemptLen && attemptLen < 65535);

      byte[] report = new byte[length];
      System.arraycopy(data, 0, report, 0, report.length);
      if (this.tracer.isOn()) {
         this.tracer.println(3, "<--getReport() = " + Util.toFormatedHexString(report));
      }

      return report;
   }

   public void setReport(byte reportType, byte reportID, short reportLength, byte[] report) throws HidException {
      this.setReport(reportType, reportID, reportLength, report, true);
   }

   public void setReport(byte reportType, byte reportID, short reportLength, byte[] report, boolean sync) throws HidException {
      this.checkForConnection();
      byte[] data = new byte[Util.unsignedInt(reportLength)];
      System.arraycopy(report, 0, data, 0, Util.unsignedInt(reportLength));
      this.submitRequest((byte)33, (byte)9, (short)(reportType << 8 | reportID), data, sync);
   }

   public byte getIdle(byte reportID) throws HidException {
      return this.getIdle(reportID, true);
   }

   public byte getIdle(byte reportID, boolean sync) throws HidException {
      this.checkForConnection();
      byte[] data = new byte[1];
      int length = this.submitRequest((byte)-95, (byte)2, (short)reportID, data, sync);
      if (1 > length) {
         String errMsg = "UsbDevice did not return any data for getIdle request (length = " + length + ")";
         this.tracer.println(errMsg);
         throw new HidException(errMsg);
      } else {
         return data[0];
      }
   }

   public void setIdle(byte reportID, byte duration) throws HidException {
      this.setIdle(reportID, duration, true);
   }

   public void setIdle(byte reportID, byte duration, boolean sync) throws HidException {
      this.checkForConnection();
      byte[] data = new byte[0];
      this.submitRequest((byte)33, (byte)10, (short)(duration << 8 | reportID), data, sync);
   }

   public byte getProtocol() throws HidException {
      return this.getProtocol(true);
   }

   public byte getProtocol(boolean sync) throws HidException {
      this.checkForConnection();
      byte[] data = new byte[1];
      int length = this.submitRequest((byte)-95, (byte)3, (short)0, data, sync);
      if (1 > length) {
         String errMsg = "UsbDevice did not return any data for getProtocol request (length = " + length + ")";
         this.tracer.println(errMsg);
         throw new HidException(errMsg);
      } else {
         return data[0];
      }
   }

   public void setProtocol(byte protocol) throws HidException, IllegalArgumentException {
      this.setProtocol(protocol, true);
   }

   public void setProtocol(byte protocol, boolean sync) throws HidException, IllegalArgumentException {
      this.checkForConnection();
      if (0 != protocol && 1 != protocol) {
         throw new IllegalArgumentException("Protocol must be 0 (boot mode) or 1 (non-boot mode)");
      } else {
         byte[] data = new byte[0];
         this.submitRequest((byte)33, (byte)11, (short)protocol, data, sync);
      }
   }

   public void async(HidAsync hidAsync) {
      Runnable asyncRunner = new HidDeviceUsbAdapter$1(this, hidAsync);
      this.asyncManager.add(asyncRunner);
   }

   public void addHidListener(HidListener listener) {
      this.hlHelper.addHidListener(listener);
   }

   public void removeHidListener(HidListener listener) {
      this.hlHelper.removeHidListener(listener);
   }

   public synchronized HidDevice getSynchronizedHidDevice() {
      if (null == this.synchronizedHidDevice) {
         this.synchronizedHidDevice = HidUtil.getSynchronizedHidDevice(this);
      }

      return this.synchronizedHidDevice;
   }

   public synchronized HidDevice getUnsynchronizedHidDevice() {
      return this;
   }

   protected void init(UsbInterface uI) {
      this.usbInterface = uI;
      this.usbDevice = this.usbInterface.getUsbConfiguration().getUsbDevice();
      this.idVendor = this.usbDevice.getUsbDeviceDescriptor().idVendor();
      this.idProduct = this.usbDevice.getUsbDeviceDescriptor().idProduct();
      this.idVendorString = "0x" + Util.toHexString(this.idVendor);
      this.idProductString = "0x" + Util.toHexString(this.idProduct);
      this.idString = "<" + this.idVendorString + "," + this.idProductString + ">";
      this.bInterfaceNumber = this.usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber();
      this.bInterfaceNumberString = Integer.toString(Util.unsignedInt(this.bInterfaceNumber));
      this.tracer = TracerFactory.getInstance().createTracer("HID" + instanceNumber++, "HidDeviceUsbAdapter" + this.idString);
   }

   protected int submitRequest(byte bmRequestType, byte bRequest, short wValue, byte[] data, boolean sync) throws HidException {
      short wIndex = (short)(255 & this.bInterfaceNumber);
      if (3 == this.tracer.getTraceLevel()) {
         String traceMsg = sync ? "sync" : "async";
         traceMsg = traceMsg + " submitRequest(";
         traceMsg = traceMsg + "0x" + Util.toHexString(bmRequestType) + ",";
         traceMsg = traceMsg + "0x" + Util.toHexString(bRequest) + ",";
         traceMsg = traceMsg + "0x" + Util.toHexString(wValue) + ",";
         traceMsg = traceMsg + "0x" + Util.toHexString(wIndex) + ",";
         traceMsg = traceMsg + Integer.toString(data.length) + ",";
         traceMsg = traceMsg + Util.toFormatedHexString(data) + ")";
         if (-127 == bmRequestType) {
            traceMsg = "HID_GET_DESCRIPTOR : " + traceMsg;
         }

         if (-95 == bmRequestType) {
            traceMsg = "HID_GET_REPORT : " + traceMsg;
         }

         this.tracer.println(traceMsg);
      }

      UsbControlIrp usbControlIrp = this.usbDevice.createUsbControlIrp(bmRequestType, bRequest, wValue, wIndex);
      usbControlIrp.setData(data);

      try {
         if (sync) {
            this.usbDevice.syncSubmit(usbControlIrp);
         } else {
            this.usbDevice.asyncSubmit(usbControlIrp);
         }
      } catch (UsbException var10) {
         String errMsg = "Could not submit UsbControlIrp (UsbException) : " + var10.getMessage();
         this.tracer.println(errMsg);
         this.tracer.print(var10);
         throw new HidException(errMsg);
      } catch (IllegalArgumentException var11) {
         String errMsgx = "Could not submit illegal UsbControlIrp : " + var11.getMessage();
         this.tracer.println(errMsgx);
         this.tracer.print(var11);
         throw new HidException(errMsgx);
      } catch (UsbDisconnectedException var12) {
         String errMsgxx = "UsbDevice has been disconnected.";
         this.tracer.println(errMsgxx);
         this.disconnectAndThrow(new HidException(errMsgxx));
      }

      if (!sync) {
         usbControlIrp.waitUntilComplete();
      }

      if (usbControlIrp.isUsbException()) {
         UsbException uE = usbControlIrp.getUsbException();
         String errMsg = "UsbControlIrp submission resulted in UsbException : " + uE.getMessage();
         this.tracer.println(errMsg);
         this.tracer.print(uE);
         throw new HidException(errMsg);
      } else {
         return usbControlIrp.getActualLength();
      }
   }

   protected synchronized void disconnect(HidException hidException) {
      this.connected = false;
      this.tracer
         .println(
            "Disconnecting HidDevice (hashCode " + this.hashCode() + ") from UsbDevice " + this.idString + " (hashCode " + this.usbDevice.hashCode() + ")"
         );
      this.usbDevice.removeUsbDeviceListener(this.disconnectListener);
      synchronized (this.usbEndpoints) {
         for (UsbEndpoint ep : this.usbEndpoints) {
            UsbPipe pipe = ep.getUsbPipe();
            pipe.removeUsbPipeListener(this.inListener);

            try {
               pipe.abortAllSubmissions();
            } catch (Exception var12) {
            }

            try {
               pipe.close();
            } catch (Exception var11) {
            }
         }

         this.usbEndpoints.clear();
      }

      this.hlHelper.hidDeviceDisconnected(new DisconnectEvent(this, hidException));
      if (null != this.outEndpoint) {
         UsbPipe pipe = this.outEndpoint.getUsbPipe();
         pipe.removeUsbPipeListener(this.outListener);

         try {
            pipe.abortAllSubmissions();
         } catch (Exception var10) {
         }

         try {
            pipe.close();
         } catch (Exception var9) {
         }
      }

      this.outEndpoint = null;

      try {
         this.usbInterface.release();
      } catch (Exception var8) {
      }
   }

   protected void addUsbEndpointIn(UsbEndpoint ep) throws HidException {
      UsbPipe pipe = ep.getUsbPipe();
      if (pipe.isOpen()) {
         this.disconnectAndThrow(new HidException("Could not access interrupt in pipe (already open)"));
      }

      pipe.addUsbPipeListener(this.inListener);
      synchronized (this.usbEndpoints) {
         this.usbEndpoints.add(ep);
      }

      try {
         pipe.open();
      } catch (Exception var5) {
         String errMsg = "Could not open UsbPipe : " + var5.getMessage();
         this.tracer.println(errMsg);
         this.tracer.print(var5);
         this.disconnectAndThrow(new HidException(errMsg));
      }

      try {
         for (int i = 0; i < 1; i++) {
            this.submitBuffer(pipe);
         }
      } catch (HidException var7) {
         this.disconnectAndThrow(var7);
      }
   }

   protected void addUsbEndpointOut(UsbEndpoint ep) throws HidException {
      if (null == this.outEndpoint) {
         UsbPipe pipe = ep.getUsbPipe();
         if (pipe.isOpen()) {
            this.disconnectAndThrow(new HidException("Could not access interrupt out pipe (already open)"));
         }

         pipe.addUsbPipeListener(this.outListener);
         this.outEndpoint = ep;

         try {
            pipe.open();
         } catch (Exception var5) {
            String errMsg = "Could not open UsbPipe : " + var5.getMessage();
            this.tracer.println(errMsg);
            this.tracer.print(var5);
            this.disconnectAndThrow(new HidException(errMsg));
         }
      }
   }

   protected void disconnectAndThrow(HidException hE) throws HidException {
      this.disconnect(hE);
      throw hE;
   }

   protected void checkForConnection() throws HidException {
      if (!this.isConnected()) {
         throw new HidException("Not connected");
      }
   }

   protected void submitBuffer(UsbPipe pipe) throws HidException {
      try {
         int bufferSize = Util.unsignedInt(pipe.getUsbEndpoint().getUsbEndpointDescriptor().wMaxPacketSize());
         if (this.tracer.isOn()) {
            this.tracer
               .println(
                  "submitBuffer on endpoint " + Util.toHexString(pipe.getUsbEndpoint().getUsbEndpointDescriptor().bEndpointAddress()) + " length " + bufferSize
               );
         }

         pipe.asyncSubmit(new byte[bufferSize]);
      } catch (UsbStallException var4) {
         this.clearFeature();
         if (this.usbStallExceptionRetry >= 3) {
            if (this.tracer.isOn()) {
               this.tracer.println("submitBuffer Retry Limit reached" + this.usbStallExceptionRetry);
               this.tracer.println("Could not resubmit buffer after UsbStallException");
               this.tracer.print(var4);
            }

            throw new HidException("Could not resubmit buffer after UsbStallException");
         }

         this.usbStallExceptionRetry++;
         if (this.tracer.isOn()) {
            this.tracer.println("submitBuffer - Retry" + this.usbStallExceptionRetry);
         }

         this.submitBuffer(pipe);
      } catch (UsbException var5) {
         String errMsg = "Could not submit UsbIrp(s) : " + var5.getMessage();
         if (this.tracer.isOn()) {
            this.tracer.println(errMsg);
            this.tracer.print(var5);
         }

         throw new HidException(errMsg);
      } catch (UsbNotActiveException var6) {
         String errMsg = "This pipe is not active : " + var6.getMessage();
         if (this.tracer.isOn()) {
            this.tracer.println(errMsg);
         }

         throw new HidException(errMsg);
      } catch (UsbNotOpenException var7) {
         String errMsg = "This pipe is not open : " + var7.getMessage();
         if (this.tracer.isOn()) {
            this.tracer.println(errMsg);
         }

         throw new HidException(errMsg);
      } catch (UsbDisconnectedException var8) {
         String errMsg = "This UsbDevice has been disconnected : " + var8.getMessage();
         if (this.tracer.isOn()) {
            this.tracer.println(errMsg);
         }

         throw new HidException(errMsg);
      } catch (IllegalArgumentException var9) {
         String errMsg = "Illegal buffer submitted to UsbPipe : " + var9.getMessage();
         if (this.tracer.isOn()) {
            this.tracer.println(errMsg);
            this.tracer.print(var9);
         }

         throw new HidException(errMsg);
      }
   }

   protected void clearFeature() throws HidException {
      try {
         if (this.tracer.isOn()) {
            this.tracer.println("clearing feature for interface : " + this.usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber());
         }

         StandardRequest.clearFeature(
            this.usbInterface.getUsbConfiguration().getUsbDevice(), (byte)1, (short)1, (short)this.usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber()
         );
         if (this.tracer.isOn()) {
            this.tracer.println(2, "<--clearFeature complete.");
         }
      } catch (IllegalArgumentException var2) {
         if (this.tracer.isOn()) {
            this.tracer.println("IllegalArgumentException when clearing feature, disconnect interface");
            this.tracer.print(var2);
         }

         this.disconnect();
         throw new HidException("IllegalArgumentException when clearing feature, interface disconnected ");
      } catch (UsbException var3) {
         if (this.tracer.isOn()) {
            this.tracer.println("UsbException when clearing feature, disconnect interface");
            this.tracer.print(var3);
         }

         this.disconnect();
         throw new HidException("UsbException when clearing feature, interface disconnected ");
      }
   }

   private class AsyncVisitor implements HidAsync.AsyncVisitor {
      private AsyncVisitor() {
      }

      public void visitAsyncGetDescriptor(HidAsync.AsyncGetDescriptor async) {
         try {
            byte[] descriptor = HidDeviceUsbAdapter.this.getDescriptor(async.getDescriptorType(), async.getDescriptorIndex(), false);
            async.setDescriptor(descriptor);
            async.setDescriptorLength((short)descriptor.length);
         } catch (HidException var6) {
            async.setHidException(var6);
         } finally {
            async.setCompleted();
         }
      }

      public void visitAsyncSetDescriptor(HidAsync.AsyncSetDescriptor async) {
         try {
            HidDeviceUsbAdapter.this.setDescriptor(
               async.getDescriptorType(), async.getDescriptorIndex(), async.getDescriptorLength(), async.getDescriptor(), false
            );
         } catch (HidException var6) {
            async.setHidException(var6);
         } finally {
            async.setCompleted();
         }
      }

      public void visitAsyncGetReport(HidAsync.AsyncGetReport async) {
         try {
            byte[] report = HidDeviceUsbAdapter.this.getReport(async.getReportType(), async.getReportID(), false);
            async.setReport(report);
            async.setReportLength((short)report.length);
         } catch (HidException var6) {
            async.setHidException(var6);
         } finally {
            async.setCompleted();
         }
      }

      public void visitAsyncSetReport(HidAsync.AsyncSetReport async) {
         try {
            HidDeviceUsbAdapter.this.setReport(async.getReportType(), async.getReportID(), async.getReportLength(), async.getReport(), false);
         } catch (HidException var6) {
            async.setHidException(var6);
         } finally {
            async.setCompleted();
         }
      }

      public void visitAsyncGetIdle(HidAsync.AsyncGetIdle async) {
         try {
            byte idle = HidDeviceUsbAdapter.this.getIdle(async.getReportID(), false);
            async.setIdleSetting(idle);
         } catch (HidException var6) {
            async.setHidException(var6);
         } finally {
            async.setCompleted();
         }
      }

      public void visitAsyncSetIdle(HidAsync.AsyncSetIdle async) {
         try {
            HidDeviceUsbAdapter.this.setIdle(async.getReportID(), async.getIdleSetting(), false);
         } catch (HidException var6) {
            async.setHidException(var6);
         } finally {
            async.setCompleted();
         }
      }

      public void visitAsyncGetProtocol(HidAsync.AsyncGetProtocol async) {
         try {
            byte protocol = HidDeviceUsbAdapter.this.getProtocol(false);
            async.setProtocol(protocol);
         } catch (HidException var6) {
            async.setHidException(var6);
         } finally {
            async.setCompleted();
         }
      }

      public void visitAsyncSetProtocol(HidAsync.AsyncSetProtocol async) {
         try {
            HidDeviceUsbAdapter.this.setProtocol(async.getProtocol(), false);
         } catch (HidException var6) {
            async.setHidException(var6);
         } finally {
            async.setCompleted();
         }
      }
   }

   private class HidListenerHelper extends EventListenerImp implements HidListener {
      private HidListenerHelper() {
      }

      public void reportEventOccurred(ReportEvent event) {
         if (HidDeviceUsbAdapter.this.tracer.isOn()) {
            HidDeviceUsbAdapter.this.tracer.println("-->reportEventOccurred() : sending to " + this.listeners.size() + " listeners.");
            HidDeviceUsbAdapter.this.tracer.println("ReportEvent data : " + Util.toFormatedHexString(event.getData()));
         }

         if (this.isEmpty()) {
            if (HidDeviceUsbAdapter.this.tracer.isOn()) {
               HidDeviceUsbAdapter.this.tracer.println("No listeners to send event to.");
            }
         } else {
            synchronized (this.listeners) {
               for (EventListenerImp.EventListenerRunnableManager elrM : this.listeners.values()) {
                  HidListener hL = (HidListener)elrM.getEventListener();
                  Runnable r = new HidDeviceUsbAdapter$2(this, hL, event);
                  elrM.add(r);
               }
            }

            if (HidDeviceUsbAdapter.this.tracer.isOn()) {
               HidDeviceUsbAdapter.this.tracer.println(2, "<--reportEventOccurred() : Sent ReportEvent to all Listeners (" + this.listeners.size() + ")");
            }
         }
      }

      public void hidExceptionEventOccurred(HidExceptionEvent event) {
         if (HidDeviceUsbAdapter.this.tracer.isOn()) {
            HidDeviceUsbAdapter.this.tracer.println("-->hidExceptionEventOccurred() : sending to " + this.listeners.size() + " listeners.");
         }

         if (this.isEmpty()) {
            if (HidDeviceUsbAdapter.this.tracer.isOn()) {
               HidDeviceUsbAdapter.this.tracer.println("No listeners to send event to.");
            }
         } else {
            synchronized (this.listeners) {
               for (EventListenerImp.EventListenerRunnableManager elrM : this.listeners.values()) {
                  HidListener hL = (HidListener)elrM.getEventListener();
                  Runnable r = new HidDeviceUsbAdapter$3(this, hL, event);
                  elrM.add(r);
               }
            }

            if (HidDeviceUsbAdapter.this.tracer.isOn()) {
               HidDeviceUsbAdapter.this.tracer.println(2, "<--hidExceptionEventOccurred() : Sent ExceptionEvent to all Listeners");
            }
         }
      }

      public void hidDeviceDisconnected(DisconnectEvent event) {
         if (HidDeviceUsbAdapter.this.tracer.isOn()) {
            HidDeviceUsbAdapter.this.tracer.println(2, "-->hidDeviceDisconnected() : sending to " + this.listeners.size() + " listeners.");
         }

         if (this.isEmpty()) {
            if (HidDeviceUsbAdapter.this.tracer.isOn()) {
               HidDeviceUsbAdapter.this.tracer.println("No listeners to send event to.");
            }
         } else {
            synchronized (this.listeners) {
               for (EventListenerImp.EventListenerRunnableManager elrM : this.listeners.values()) {
                  HidListener hL = (HidListener)elrM.getEventListener();
                  Runnable r = new HidDeviceUsbAdapter$4(this, hL, event);
                  elrM.add(r);
               }
            }

            if (HidDeviceUsbAdapter.this.tracer.isOn()) {
               HidDeviceUsbAdapter.this.tracer.println(2, "<--hidDeviceDisconnected() : Sent DisconnectEvent to all Listeners");
            }
         }
      }

      public void addHidListener(HidListener listener) {
         this.addEventListener(listener);
      }

      public void removeHidListener(HidListener listener) {
         this.removeEventListener(listener);
      }
   }

   private class ReportListener extends HidDeviceUsbAdapter.UsbPipeErrorListener implements UsbPipeListener {
      private ReportListener() {
      }

      public void dataEventOccurred(UsbPipeDataEvent event) {
         super.dataEventOccurred(event);
         HidDeviceUsbAdapter.this.tracer.println("ReportListener");
         byte bEndpointAddress = event.getUsbPipe().getUsbEndpoint().getUsbEndpointDescriptor().bEndpointAddress();
         String bEndpointAddressString = "0x" + Util.toHexString(bEndpointAddress);
         if (HidDeviceUsbAdapter.this.tracer.isOn() && 3 == HidDeviceUsbAdapter.this.tracer.getTraceLevel()) {
            HidDeviceUsbAdapter.this.tracer
               .println(
                  3,
                  "-->dataEventOccurred() on UsbEndpoint "
                     + bEndpointAddressString
                     + " (length "
                     + event.getData().length
                     + ") : "
                     + Util.toFormatedHexString(event.getData())
               );
         }

         UsbPipe pipe = event.getUsbPipe();
         byte[] data = new byte[event.getData().length];
         System.arraycopy(event.getData(), 0, data, 0, event.getData().length);
         HidDeviceUsbAdapter.this.hlHelper.reportEventOccurred(new ReportEvent(HidDeviceUsbAdapter.this, data));

         try {
            HidDeviceUsbAdapter.this.submitBuffer(pipe);
         } catch (HidException var7) {
            HidDeviceUsbAdapter.this.disconnect(var7);
         }

         if (HidDeviceUsbAdapter.this.tracer.isOn()) {
            HidDeviceUsbAdapter.this.tracer.println(3, "<--dataEventOccurred() on UsbEndpoint " + bEndpointAddressString + " : Resubmitted buffer, finished.");
         }
      }
   }

   private class UsbDeviceDisconnectListener implements UsbDeviceListener {
      private UsbDeviceDisconnectListener() {
      }

      public void dataEventOccurred(UsbDeviceDataEvent uddE) {
      }

      public void errorEventOccurred(UsbDeviceErrorEvent udeE) {
      }

      public void usbDeviceDetached(UsbDeviceEvent udE) {
         HidDeviceUsbAdapter.this.disconnect(new HidException("UsbDevice disconnected"));
      }
   }

   class UsbPipeErrorListener implements UsbPipeListener {
      protected int errorCount = 0;

      public void errorEventOccurred(UsbPipeErrorEvent event) {
         if (HidDeviceUsbAdapter.this.isConnected()) {
            this.errorCount++;
            UsbPipe pipe = event.getUsbPipe();
            UsbEndpoint ep = pipe.getUsbEndpoint();
            String bEndpointAddressString = null;
            if (HidDeviceUsbAdapter.this.tracer.isOn()) {
               bEndpointAddressString = "0x" + Util.toHexString(ep.getUsbEndpointDescriptor().bEndpointAddress());
               HidDeviceUsbAdapter.this.tracer.println("-->errorEventOccurred() on UsbEndpoint " + bEndpointAddressString);
            }

            HidException hidException = new HidException(
               "Error (" + this.errorCount + "/" + 10 + ") on pipe 0x" + bEndpointAddressString + " : " + event.getUsbException()
            );
            HidDeviceUsbAdapter.this.hlHelper.hidExceptionEventOccurred(new HidExceptionEvent(HidDeviceUsbAdapter.this, hidException));
            if (10 <= this.errorCount) {
               String errMsg = "Too many consecutive error events (" + this.errorCount + "), disconnecting from UsbDevice.";
               if (HidDeviceUsbAdapter.this.tracer.isOn()) {
                  HidDeviceUsbAdapter.this.tracer.println(errMsg);
               }

               HidDeviceUsbAdapter.this.disconnect(new HidException(errMsg));
            }

            try {
               HidDeviceUsbAdapter.this.submitBuffer(pipe);
            } catch (HidException var7) {
               HidDeviceUsbAdapter.this.disconnect(var7);
            }

            if (HidDeviceUsbAdapter.this.tracer.isOn()) {
               HidDeviceUsbAdapter.this.tracer.println("<--errorEventOccurred() on UsbEndpoint " + bEndpointAddressString + " : Resubmitted buffer, finished.");
            }
         }
      }

      public void dataEventOccurred(UsbPipeDataEvent event) {
         this.errorCount = 0;
      }
   }
}
