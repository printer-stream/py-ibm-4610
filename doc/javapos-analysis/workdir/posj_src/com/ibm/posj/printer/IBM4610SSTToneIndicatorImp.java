package com.ibm.posj.printer;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.ToneIndicatorCmd;
import com.ibm.posj.bus.IBMPrinterComposite;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.printer.event.PrintErrorEvent;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCats;

public class IBM4610SSTToneIndicatorImp implements IBMPrinterToneIndicatorImp {
   private DeviceInputQueue iQueue = null;
   private IBM4610PrinterCmd.Factory defFactory;
   private IBMPrinterComposite ipc = null;
   private Object lockobj = new Object();
   private Object lockUntilCmdReceived = new Object();
   private int toneDuration = 0;
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevCats.TONEINDICATOR_DEVCAT.toString(), "IBM4610SSTToneIndicatorImp");
   public static final int WAIT_TIMEOUT_VALUE = 3000;
   private static final int IBM4610SSTTONEINDICATOR_MAX_DURATION = 25400;

   public IBM4610SSTToneIndicatorImp(IBMPrinterComposite ptrComposite) {
      this.ipc = ptrComposite;
   }

   public void init() throws HandleException {
      this.getIBMPrinterComposite().init();
      this.defFactory = (IBM4610PrinterCmd.Factory)this.getIBMPrinterComposite().getPrintCmdFactory();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (this.tracer.isOn()) {
         this.tracer.println(2, "-->submit(" + cmd.getName() + ")");
      }

      if (null == this.iQueue) {
         this.iQueue = new DefaultDeviceInputQueue(this, "Tone");
         this.getPrinterWriter().registerDeviceQueue((RetryInputQueue)this.iQueue);
      }

      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            if (cmd.getCode() == 1000) {
               PrintCmd tiCmd = this.createBeeperCmd(cmd);
               ToneIndicatorCmd.ToneCmd sound = (ToneIndicatorCmd.ToneCmd)cmd;
               if (sound.getDuration() <= 25400) {
                  this.toneDuration = sound.getDuration();
               } else {
                  this.toneDuration = 25400;
               }

               Handle.EventHelper eventHelper = this.ipc.getHandle().getEventHelper();
               this.iQueue.submit(tiCmd);
               this.waitForCmdReceivedMsg();
               eventHelper.fireStatusEvent(new StatusEvent(this, 0));
               this.lockUntilSoundCompleted(this.toneDuration);
               eventHelper.fireStatusEvent(new StatusEvent(this, 1));
            } else {
               if (!(cmd instanceof SystemCmd)) {
                  throw new HandleException("Invalid POSPrinterToneIndicatorCmd object submitted!");
               }

               if (cmd.getCode() == 103) {
                  this.submitDevInfoCmd((SystemCmd.DeviceInfoRequestCmd)cmd);
               }
            }
         } catch (HandleException var8) {
            cmd.getResult().setInError(true);
            throw var8;
         } finally {
            cmd.setCompleted(true);
         }

         if (this.tracer.isOn()) {
            this.tracer.println(2, "<--submit(" + cmd.getName() + ")");
         }
      }
   }

   public PrintCmd createBeeperCmd(HandleCmd cmd) throws HandleException {
      DefaultPOSPrinterCmd ppc = null;
      PrintCmd ret = null;

      try {
         ToneIndicatorCmd.ToneCmd sound = (ToneIndicatorCmd.ToneCmd)cmd;
         ppc = (DefaultPOSPrinterCmd)this.defFactory
            .createBeeperCmd(sound.getOn(), sound.getTimed(), sound.getDuration(), sound.getFrequency(), sound.getVolume());
         ret = ppc.getPrintCmd();
         ret.setParent(this);
         return ret;
      } catch (Exception var5) {
         throw new HandleException("Error while creating beeperCmd ");
      }
   }

   public void handleCmdComplete(PrintCmd done) {
      if (done.getHandleCmd() instanceof IBM4610PrinterCmd.BeeperCmd) {
         synchronized (this.lockUntilCmdReceived) {
            this.lockUntilCmdReceived.notifyAll();
         }

         if (null != done.getHandleCmd()) {
            done.getHandleCmd().setCompleted(true);
         }
      }
   }

   public void handleError(PrintCmd err, PrintErrorEvent status) {
      err.getHandleCmd().getResult().setInError(true);
   }

   public boolean handleImmediateError(PrintCmd err, PrintErrorEvent event) {
      return false;
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
      cmd.setFirmwareLevel(this.getIBMPrinterComposite().getPrinterID_microcodeLevel() & 0xFF);
      cmd.setSerialNumber(this.getIBMPrinterComposite().getDeviceSerialNumber());
      if (this.getIBMPrinterComposite().getDevBus().equals(DevBuses.RS232_DEVBUS)) {
         cmd.setDeviceId(4446);
      } else if (this.getIBMPrinterComposite().getDevBus().equals(DevBuses.RS485_DEVBUS)) {
         cmd.setDeviceId(4445);
      } else {
         cmd.setDeviceId(4447);
      }

      if (this.tracer.isOn()) {
         this.tracer.println(2, "Using Device ID : " + cmd.getDeviceId());
      }
   }

   protected void submitSystemCmd(SystemCmd systemCmd) throws HandleException {
   }

   protected IBMPrinterComposite getIBMPrinterComposite() {
      return this.ipc;
   }

   protected void lockUntilSoundCompleted(int msecs) {
      synchronized (this.lockobj) {
         try {
            this.lockobj.wait((long)(msecs + 50));
         } catch (InterruptedException var5) {
         }
      }
   }

   protected void waitForCmdReceivedMsg() {
      synchronized (this.lockUntilCmdReceived) {
         try {
            this.lockUntilCmdReceived.wait(20000L);
         } catch (InterruptedException var4) {
         }
      }
   }

   protected PrinterWriter getPrinterWriter() {
      return this.ipc.getWriter();
   }
}
