package com.ibm.posj.bus.poskbd;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.HandleFactory;
import com.ibm.posj.HandleFactoryException;
import com.ibm.posj.HandleRegistry;
import com.ibm.posj.PosException;
import com.ibm.posj.PosSystem;
import com.ibm.posj.bus.AbstractHandlePopulator;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.poskbd.Keyboard;
import com.ibm.poskbd.PosKbdManager;
import com.ibm.poskbd.event.PosKbdEvent;
import com.ibm.poskbd.event.PosKbdListener;
import java.util.Hashtable;
import java.util.ListIterator;

public class PosKbdHandlePopulator extends AbstractHandlePopulator {
   private PosKbdManager manager = null;
   private int keyboardNumber = 0;
   private PosKbdListener listener = new PosKbdHandlePopulator.PopulatorPosKbdListener();
   private PosKbdHandleImpFactory handleImpFactory = new PosKbdHandleImpFactory(this.getHandleFactory());
   private Hashtable keyboardTable = new Hashtable();
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevBuses.POSKBD_DEVBUS.getName(), "PosKbdPopulator");

   public PosKbdHandlePopulator(HandleRegistry registry, HandleFactory factory, PosSystem.EventHelper eventHelper) {
      super(registry, factory, eventHelper);
   }

   public void start() throws PosException {
      if (this.tracer.isOn()) {
         this.trace("-->Start()");
      }

      super.start();
      PosKbdManager.setValidKeyboards(PoskbdIDUtil.getInstance().getValidKbds());
      PosKbdManager.setValidKeyboardsByVendorProduct(PoskbdIDUtil.getInstance().getValidKbdsByVendor(), PoskbdIDUtil.getInstance().getValidKbdsByProduct());

      try {
         this.manager = PosKbdManager.getInstance();
      } catch (Throwable var2) {
         if (this.tracer.isOn()) {
            this.trace("-->PosKbdManager.getInstance():Error loading JNI drivers: " + var2.toString() + "<--");
            this.trace("<--Start()");
         }

         return;
      }

      this.initKeyboards();
      this.manager.addPosKbdListener(this.listener);
      if (this.tracer.isOn()) {
         this.trace("<--Start()");
      }
   }

   public void stop() throws PosException {
      this.manager.removePosKbdListener(this.listener);
      super.stop();
   }

   public String getName() {
      return "PosKbdHandlePopulator";
   }

   public DevBus getDevBus() {
      return DevBuses.POSKBD_DEVBUS;
   }

   private void initKeyboards() throws PosException {
      ListIterator keyboards = this.manager.getKeyboards();

      while (keyboards.hasNext()) {
         this.createKeyboard((Keyboard)keyboards.next());
      }
   }

   private void createKeyboard(Keyboard keyboard) throws PosException {
      if (this.tracer.isOn()) {
         this.trace("-->createKeyboard()");
      }

      synchronized (this.keyboardTable) {
         if (!this.keyboardTable.contains(keyboard)) {
            ListIterator imps = null;

            try {
               imps = this.handleImpFactory.createHandleImps(keyboard, this.keyboardNumber);
            } catch (HandleFactoryException var6) {
               throw new PosException("Could not populate", var6);
            }

            if (this.tracer.isOn()) {
               this.trace("<--createKeyboard() ");
            }

            PosKbdComposite composite = new PosKbdComposite(keyboard, imps);
            this.keyboardTable.put(keyboard, composite);

            while (imps.hasPrevious()) {
               imps.previous();
            }

            while (imps.hasNext()) {
               this.getHandleRegistry().addHandle(((HandleImp)imps.next()).getHandle());
            }

            this.keyboardNumber++;
         }
      }
   }

   private void removeKeyboard(Keyboard keyboard) {
      PosKbdComposite composite = null;
      synchronized (this.keyboardTable) {
         composite = (PosKbdComposite)this.keyboardTable.remove(keyboard);
         if (null == composite) {
            return;
         }

         this.keyboardNumber--;
      }

      composite.disconnect();
   }

   private void trace(String msg) {
      this.tracer.println(2, msg);
   }

   protected class PopulatorPosKbdListener implements PosKbdListener {
      public void keyboardConnected(PosKbdEvent pkE) {
         try {
            PosKbdHandlePopulator.this.createKeyboard(pkE.getKeyboard());
         } catch (PosException var3) {
         }
      }

      public void keyboardDisconnected(PosKbdEvent pkE) {
         PosKbdHandlePopulator.this.removeKeyboard(pkE.getKeyboard());
      }
   }
}
