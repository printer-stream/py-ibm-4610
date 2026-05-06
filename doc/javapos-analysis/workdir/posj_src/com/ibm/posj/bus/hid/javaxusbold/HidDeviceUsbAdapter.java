package com.ibm.posj.bus.hid.javaxusbold;

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
import java.util.List;
import java.util.Vector;
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
   protected StringBuffer traceBuffer = null;
   private StringBuffer submitTraceBuffer = null;
   private boolean connected = false;
   private RunnableManager asyncManager = new RunnableManager();
   private UsbInterface usbInterface = null;
   private List usbEndpoints = new Vector();
   private UsbEndpoint outEndpoint = null;
   private UsbDeviceListener disconnectListener = new HidDeviceUsbAdapter.UsbDeviceDisconnectListener(null);
   private HidDeviceUsbAdapter.UsbPipeErrorListener outListener = new HidDeviceUsbAdapter.UsbPipeErrorListener();
   private HidDeviceUsbAdapter.ReportListener inListener = new HidDeviceUsbAdapter.ReportListener(null);
   private HidDeviceUsbAdapter.HidListenerHelper hlHelper = new HidDeviceUsbAdapter.HidListenerHelper();
   private HidDeviceUsbAdapter.AsyncVisitor asyncVisitor = new HidDeviceUsbAdapter.AsyncVisitor(null);
   private HidDevice synchronizedHidDevice = null;
   private Tracer tracer;
   private static int instanceNumber = 0;
   protected static final int INT_BUFFERING = 1;
   protected static final short DESCRIPTOR_MAX_LENGTH = 255;
   protected static final short REPORT_INITIAL_LENGTH = 511;
   protected static final short REPORT_INCREMENT_LENGTH = 255;
   public static final int MAX_ERROR_RETRY = 3;

   public HidDeviceUsbAdapter(UsbInterface usbInterface) {
      this.tracer = this.tracer = TracerFactory.getInstance().createTracer("HID" + instanceNumber, "HidDeviceUsbAdapter");
      this.usbInterface = usbInterface;
      instanceNumber++;
   }

   public synchronized void connect() throws HidException {
      if (!this.connected) {
         if (this.tracer.isOn()) {
            this.tracer
               .println(
                  "-->Connecting HidDevice to UsbDevice "
                     + Util.toHexString(this.usbInterface.getUsbConfiguration().getUsbDevice().getUsbDeviceDescriptor().idProduct())
                     + "<--"
               );
         }

         try {
            this.usbInterface.claim(DefaultUsbInterfacePolicy.getInstance());
         } catch (UsbException var4) {
            this.tracer.println("Got UsbException claiming");
            this.tracer.print(var4);
            if (0 > var4.getMessage().indexOf("Device removed")) {
               throw new HidException("Could not claim USB interface", var4);
            }

            throw new UsbNotActiveException("Device removed");
         } catch (UsbDisconnectedException var5) {
            if (this.tracer.isOn()) {
               this.tracer.println("a UsbDisconnectException occurred");
            }

            throw new UsbDisconnectedException("a UsbDisconnectException occurred");
         }

         this.usbInterface.getUsbConfiguration().getUsbDevice().addUsbDeviceListener(this.disconnectListener);

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
            this.tracer.println("<--Connecting HidDevice to UsbDevice ");
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
      if (this.tracer.isOn() && this.tracer.getTraceLevel() == 3) {
         this.tracer.println("<-getDescriptor() = " + Util.toFormatedHexString(data));
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
      if (this.tracer.isOn() && this.tracer.getTraceLevel() == 3) {
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
      if (this.tracer.isOn() && this.tracer.getTraceLevel() == 3) {
         this.tracer.println("<-getReport() = " + Util.toFormatedHexString(data));
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
      this.submitRequest((byte)-95, (byte)2, (short)reportID, data, sync);
      return data[0];
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
      this.submitRequest((byte)-95, (byte)3, (short)0, data, sync);
      return data[0];
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

   protected int submitRequest(byte bmRequestType, byte bRequest, short wValue, byte[] data, boolean sync) throws HidException {
      if (this.tracer.isOn()) {
         if (null == this.traceBuffer) {
            this.buildTraceBuffers();
         }

         if (this.tracer.getTraceLevel() == 3) {
            this.tracer
               .println(
                  3,
                  "submitRequest( "
                     + Util.toHexString(bmRequestType)
                     + ", "
                     + Util.toHexString(bRequest)
                     + ", "
                     + Util.toHexString(wValue)
                     + ", data.length= "
                     + data.length
                     + ", "
                     + sync
                     + ")"
               );
            if (bmRequestType != -127 && bmRequestType != -95) {
               int len = this.submitTraceBuffer.length();
               int wlen = data.length > 350 ? 350 : data.length;
               this.submitTraceBuffer.append(Util.toFormatedHexString(data, 0, wlen)).append("<--");
               this.tracer.println(3, this.submitTraceBuffer.toString());
               this.submitTraceBuffer.setLength(len);
            } else {
               int len = this.submitTraceBuffer.length();
               if (bmRequestType == -127) {
                  this.submitTraceBuffer.append("HID_GET_DESCRIPTOR_REQUESTTYPE<--");
               } else {
                  this.submitTraceBuffer.append("HID_GET_REPORT_REQUESTTYPE<--");
               }

               this.tracer.println(3, this.submitTraceBuffer.toString());
               this.submitTraceBuffer.setLength(len);
            }
         }
      }

      short wIndex = (short)this.usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber();
      UsbControlIrp controlIrp = this.usbInterface.getUsbConfiguration().getUsbDevice().createUsbControlIrp(bmRequestType, bRequest, wValue, wIndex);
      controlIrp.setData(data);

      try {
         if (sync) {
            this.usbInterface.getUsbConfiguration().getUsbDevice().syncSubmit(controlIrp);
         } else {
            this.usbInterface.getUsbConfiguration().getUsbDevice().asyncSubmit(controlIrp);
         }
      } catch (UsbException var9) {
         throw new HidException(var9);
      } catch (UsbDisconnectedException var10) {
         if (this.tracer.isOn()) {
            this.tracer.println("a UsbDisconnectException occurred");
         }

         throw new UsbDisconnectedException("a UsbDisconnectException occurred");
      }

      if (!sync) {
         controlIrp.waitUntilComplete();
      }

      return controlIrp.getActualLength();
   }

   protected void buildTraceBuffers() {
      this.traceBuffer = new StringBuffer();
      this.submitTraceBuffer = new StringBuffer();
      UsbDevice ub = this.usbInterface.getUsbConfiguration().getUsbDevice();
      short devId = ub.getUsbDeviceDescriptor().idProduct();
      this.traceBuffer.append("-->").append(Util.toHexString(devId));

      try {
         this.traceBuffer.append(this.usbInterface.getInterfaceString());
      } catch (Exception var4) {
         this.traceBuffer.append("getIStr failed ").append(var4.getMessage());
      }

      this.submitTraceBuffer.append(this.traceBuffer.toString()).append(".submitRequest() Data : ");
   }

   protected synchronized void disconnect(HidException hidException) {
      this.connected = false;
      if (this.tracer.isOn()) {
         this.tracer
            .println(
               2,
               "-->Disconnecting HidDevice from UsbDevice "
                  + Util.toHexString(this.usbInterface.getUsbConfiguration().getUsbDevice().getUsbDeviceDescriptor().idProduct())
                  + "<--"
            );
      }

      this.usbInterface.getUsbConfiguration().getUsbDevice().removeUsbDeviceListener(this.disconnectListener);

      for (UsbEndpoint ep : this.usbEndpoints) {
         ep.getUsbPipe().removeUsbPipeListener(this.inListener);

         try {
            ep.getUsbPipe().abortAllSubmissions();
            ep.getUsbPipe().close();
         } catch (UsbException var9) {
         } catch (UsbDisconnectedException var10) {
         }
      }

      this.usbEndpoints.clear();
      this.hlHelper.hidDeviceDisconnected(new DisconnectEvent(this, hidException));
      if (null != this.outEndpoint) {
         this.outEndpoint.getUsbPipe().removeUsbPipeListener(this.outListener);

         try {
            if (this.outEndpoint.getUsbPipe().isOpen()) {
               this.outEndpoint.getUsbPipe().close();
            }
         } catch (UsbException var7) {
         } catch (UsbDisconnectedException var8) {
         }
      }

      this.outEndpoint = null;

      try {
         this.usbInterface.release();
      } catch (UsbException var5) {
      } catch (UsbDisconnectedException var6) {
      }
   }

   protected void addUsbEndpointIn(UsbEndpoint ep) throws HidException {
      UsbPipe pipe = ep.getUsbPipe();
      if (pipe.isOpen()) {
         this.disconnectAndThrow(new HidException("Could not access interrupt in pipe (already open)"));
      }

      pipe.addUsbPipeListener(this.inListener);
      this.usbEndpoints.add(pipe.getUsbEndpoint());

      try {
         pipe.open();

         for (int i = 0; i < 1; i++) {
            pipe.asyncSubmit(new byte[pipe.getUsbEndpoint().getUsbEndpointDescriptor().wMaxPacketSize()]);
         }
      } catch (UsbException var4) {
         this.disconnectAndThrow(new HidException(var4));
      } catch (UsbDisconnectedException var5) {
         if (this.tracer.isOn()) {
            this.tracer.println("a UsbDisconnectException occurred");
         }

         this.disconnect(null);
         throw new UsbDisconnectedException("a UsbDisconnectException occurred");
      }
   }

   protected void addUsbEndpointOut(UsbEndpoint ep) throws HidException {
      if (null == this.outEndpoint) {
         UsbPipe pipe = ep.getUsbPipe();
         if (pipe.isOpen()) {
            this.disconnectAndThrow(new HidException("Could not access interrupt out pipe (already open)"));
         }

         pipe.addUsbPipeListener(this.outListener);
         this.outEndpoint = pipe.getUsbEndpoint();

         try {
            pipe.open();
         } catch (UsbException var4) {
            this.disconnectAndThrow(new HidException(var4));
         } catch (UsbDisconnectedException var5) {
            if (this.tracer.isOn()) {
               this.tracer.println("a UsbDisconnectException occurred");
            }

            this.disconnect(null);
            throw new UsbDisconnectedException("a UsbDisconnectException occurred");
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

   protected short getWMaxPacketSize(UsbPipe pipe) {
      return pipe.getUsbEndpoint().getUsbEndpointDescriptor().wMaxPacketSize();
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

   private class HidListenerHelper implements HidListener {
      private Vector listeners = new Vector();
      private RunnableManager runnableManager = new RunnableManager();

      public HidListenerHelper() {
         this.runnableManager.setMaxSize(20L);
      }

      public void reportEventOccurred(ReportEvent reportEvent) {
         List list = (List)this.listeners.clone();
         if (HidDeviceUsbAdapter.this.tracer.isOn()) {
            HidDeviceUsbAdapter.this.tracer.println(2, "-->reportEventOccurred(),getData() : " + Util.toFormatedHexString(reportEvent.getData()));
         }

         for (int i = 0; i < list.size(); i++) {
            HidListener hL = (HidListener)list.get(i);
            this.runnableManager.add(new HidDeviceUsbAdapter$2(this, hL, reportEvent));
         }

         if (HidDeviceUsbAdapter.this.tracer.isOn()) {
            HidDeviceUsbAdapter.this.tracer.println(2, "<--reportEventOccurred() : Sent ReportEvent to all Listeners (" + list.size() + ")");
         }
      }

      public void hidExceptionEventOccurred(HidExceptionEvent hidExceptionEvent) {
         List list = (List)this.listeners.clone();
         if (HidDeviceUsbAdapter.this.tracer.isOn()) {
            HidDeviceUsbAdapter.this.tracer.println(2, "-->hidExceptionEventOccurred() : sending to " + list.size() + " listeners.");
         }

         for (int i = 0; i < list.size(); i++) {
            HidListener hL = (HidListener)list.get(i);
            this.runnableManager.add(new HidDeviceUsbAdapter$3(this, hL, hidExceptionEvent));
         }

         if (HidDeviceUsbAdapter.this.tracer.isOn()) {
            HidDeviceUsbAdapter.this.tracer.println(2, "<--hidExceptionEventOccurred() : Sent ExceptionEvent to all Listeners");
         }
      }

      public void hidDeviceDisconnected(DisconnectEvent disconnectEvent) {
         List list = (List)this.listeners.clone();
         if (HidDeviceUsbAdapter.this.tracer.isOn()) {
            HidDeviceUsbAdapter.this.tracer.println(2, "-->hidDeviceDisconnected() : sending to " + list.size() + " listeners.");
         }

         for (int i = 0; i < list.size(); i++) {
            HidListener hL = (HidListener)list.get(i);
            this.runnableManager.add(new HidDeviceUsbAdapter$4(this, hL, disconnectEvent));
         }

         if (HidDeviceUsbAdapter.this.tracer.isOn()) {
            HidDeviceUsbAdapter.this.tracer.println(2, "<--hidDeviceDisconnected() : Sent DisconnectEvent to all Listeners");
         }
      }

      public void addHidListener(HidListener listener) {
         this.listeners.add(listener);
      }

      public void removeHidListener(HidListener listener) {
         this.listeners.remove(listener);
      }
   }

   private class ReportListener extends HidDeviceUsbAdapter.UsbPipeErrorListener implements UsbPipeListener {
      private ReportListener() {
      }

      public void dataEventOccurred(UsbPipeDataEvent event) {
         if (HidDeviceUsbAdapter.this.tracer.isOn()) {
            HidDeviceUsbAdapter.this.tracer.println(3, "-->dataEventOccurred() : " + event.getData().length);
         }

         UsbPipe pipe = event.getUsbPipe();
         byte[] data = new byte[event.getData().length];
         System.arraycopy(event.getData(), 0, data, 0, event.getData().length);
         HidDeviceUsbAdapter.this.hlHelper.reportEventOccurred(new ReportEvent(HidDeviceUsbAdapter.this, data));

         try {
            pipe.asyncSubmit(new byte[pipe.getUsbEndpoint().getUsbEndpointDescriptor().wMaxPacketSize()]);
         } catch (UsbException var5) {
            HidDeviceUsbAdapter.this.tracer.print(var5);
            HidDeviceUsbAdapter.this.tracer.println(3, "CRITICAL : Coult not resubmit buffer!");
            HidDeviceUsbAdapter.this.disconnect(new HidException("Could not resubmit UsbPipe listener", var5));
         } catch (UsbDisconnectedException var6) {
            if (HidDeviceUsbAdapter.this.tracer.isOn()) {
               HidDeviceUsbAdapter.this.tracer.print(var6);
            }

            HidDeviceUsbAdapter.this.disconnect(new HidException("UsbDisconnectedException occurred", var6));
         } catch (UsbNotOpenException var7) {
            if (HidDeviceUsbAdapter.this.tracer.isOn()) {
               HidDeviceUsbAdapter.this.tracer.print(var7);
            }

            HidDeviceUsbAdapter.this.disconnect(new HidException("UsbNotOpenException occurred", var7));
         }

         if (HidDeviceUsbAdapter.this.tracer.isOn()) {
            HidDeviceUsbAdapter.this.tracer.println(3, "<--dataEventOccurred() : Resubmitted buffer, finished.");
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
      private int errorCounter = 0;
      private long timeFirstError = 0L;

      public void errorEventOccurred(UsbPipeErrorEvent event) {
         if (++this.errorCounter == 1) {
            this.timeFirstError = System.currentTimeMillis();
         }

         long deltaErrorTime = System.currentTimeMillis() - this.timeFirstError;
         if (HidDeviceUsbAdapter.this.tracer.isOn()) {
            if (null == HidDeviceUsbAdapter.this.traceBuffer) {
               HidDeviceUsbAdapter.this.buildTraceBuffers();
            }

            HidDeviceUsbAdapter.this.tracer
               .println(2, HidDeviceUsbAdapter.this.traceBuffer.toString() + "-->errorEventOccurred() : " + event.getUsbException());
            HidDeviceUsbAdapter.this.tracer.println(2, "error class : " + event.getUsbException().getClass().getName());
            HidDeviceUsbAdapter.this.tracer.println(2, "time between 1st and current error event (" + this.errorCounter + ") : " + deltaErrorTime);
         }

         if (event.getUsbException() instanceof UsbStallException) {
            try {
               if (HidDeviceUsbAdapter.this.tracer.isOn()) {
                  HidDeviceUsbAdapter.this.tracer
                     .println("clearing feature for interface : " + HidDeviceUsbAdapter.this.usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber());
               }

               StandardRequest.clearFeature(
                  HidDeviceUsbAdapter.this.usbInterface.getUsbConfiguration().getUsbDevice(),
                  (byte)1,
                  (short)1,
                  (short)HidDeviceUsbAdapter.this.usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber()
               );
               if (HidDeviceUsbAdapter.this.tracer.isOn()) {
                  HidDeviceUsbAdapter.this.tracer.println(2, "<--errorEventOccurred() : clearFeature complete return.");
               }

               return;
            } catch (IllegalArgumentException var10) {
               if (HidDeviceUsbAdapter.this.tracer.isOn()) {
                  HidDeviceUsbAdapter.this.tracer.println(2, "Exception when clearing feature, disconnect interface");
                  HidDeviceUsbAdapter.this.tracer.print(var10);
               }

               HidDeviceUsbAdapter.this.disconnect();
            } catch (UsbException var11) {
               if (HidDeviceUsbAdapter.this.tracer.isOn()) {
                  HidDeviceUsbAdapter.this.tracer.println(2, "Exception when clearing feature, disconnect interface");
                  HidDeviceUsbAdapter.this.tracer.print(var11);
               }

               HidDeviceUsbAdapter.this.disconnect();
            }
         }

         if (deltaErrorTime > 5000L) {
            this.errorCounter = 0;
         }

         UsbPipe pipe = event.getUsbPipe();
         HidException hE = new HidException("UsbPipeErrorEvent occurred " + event.getUsbException());
         String errStr = event.getUsbException().toString();
         if (HidDeviceUsbAdapter.this.isConnected() && -1 == errStr.indexOf("Device removed (or no such device)")) {
            if (!event.getUsbPipe().isOpen()) {
               HidDeviceUsbAdapter.this.disconnect(hE);
            } else {
               HidDeviceUsbAdapter.this.hlHelper.hidExceptionEventOccurred(new HidExceptionEvent(HidDeviceUsbAdapter.this, hE));
               if (this.errorCounter >= 3) {
                  if (HidDeviceUsbAdapter.this.tracer.isOn()) {
                     HidDeviceUsbAdapter.this.tracer.println(2, "CRITICAL :Maximum error retry reached, disconnect device");
                  }

                  HidDeviceUsbAdapter.this.disconnect();
               } else {
                  if (HidDeviceUsbAdapter.this.tracer.isOn()) {
                     HidDeviceUsbAdapter.this.tracer.println(2, "Resubmitting buffer, retry " + this.errorCounter + " of " + 3);
                  }

                  try {
                     pipe.asyncSubmit(new byte[HidDeviceUsbAdapter.this.getWMaxPacketSize(pipe)]);
                  } catch (UsbException var8) {
                     HidDeviceUsbAdapter.this.tracer.println("CRITICAL : Coult not resubmit buffer!");
                     HidDeviceUsbAdapter.this.disconnect(new HidException("Could not resubmit UsbPipe listener", var8));
                  } catch (UsbDisconnectedException var9) {
                     if (HidDeviceUsbAdapter.this.tracer.isOn()) {
                        HidDeviceUsbAdapter.this.tracer.println("a UsbDisconnectException occurred");
                     }

                     HidDeviceUsbAdapter.this.disconnect(new HidException("UsbDisconnectedException occurred", var9));
                  }
               }

               if (HidDeviceUsbAdapter.this.tracer.isOn()) {
                  HidDeviceUsbAdapter.this.tracer.println(2, "<--errorEventOccurred() : Resubmitted buffer, finished.");
               }
            }
         } else {
            if (HidDeviceUsbAdapter.this.tracer.isOn()) {
               if (null == HidDeviceUsbAdapter.this.traceBuffer) {
                  HidDeviceUsbAdapter.this.buildTraceBuffers();
               }

               HidDeviceUsbAdapter.this.tracer.println(2, HidDeviceUsbAdapter.this.traceBuffer.toString() + "calling disconnect method<--");
            }

            HidDeviceUsbAdapter.this.disconnect(hE);
         }
      }

      public void dataEventOccurred(UsbPipeDataEvent event) {
      }
   }
}
