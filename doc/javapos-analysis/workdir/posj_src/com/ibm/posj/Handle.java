package com.ibm.posj;

import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.event.DataEvent;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
import com.ibm.posj.event.OutputCompleteEvent;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.flash.FlashException;
import com.ibm.posj.flash.FlashRequest;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevCat;
import java.util.EventListener;

public interface Handle {
   int HANDLE_BUS_FAILURE_ERROR_CODE = -100;
   int HANDLE_CMD_REJECTED_ERROR_CODE = -101;
   int HANDLE_BUSY_ERROR_CODE = -102;
   int HANDLE_PROCESS_DATA_ERROR_CODE = -103;

   String getName();

   DevCat getDevCat();

   DevBus getDevBus();

   HandleKey getHandleKey();

   HandleCmd.Factory getHandleCmdFactory();

   SystemCmd.Factory getSystemCmdFactory();

   boolean isAsyncMode();

   void setDirectIOMode(boolean var1);

   boolean isDirectIOMode();

   void setAsyncMode(boolean var1) throws HandleException;

   boolean isOnline();

   boolean isInit() throws HandleException;

   void init() throws HandleException;

   void submit(HandleCmd var1) throws HandleException;

   int getSubmissionCount();

   int getPendingSubmissionCount();

   void accept(HandleVisitor var1);

   void addHandleListener(Handle.Listener var1);

   void removeHandleListener(Handle.Listener var1);

   void flash(FlashRequest var1) throws FlashException;

   boolean isFlashable();

   void setHandleImp(HandleImp var1) throws HandleException;

   HandleImp getHandleImp();

   Handle.EventHelper getEventHelper();

   Handle.State getState();

   public interface EventHelper {
      Handle getHandle();

      void fireOutputCompleteEvent(OutputCompleteEvent var1);

      void fireDataEvent(DataEvent var1);

      void fireDirectIOEvent(DirectIOEvent var1);

      void fireErrorEvent(ErrorEvent var1);

      void fireStatusEvent(StatusEvent var1);

      void fireOnlineEvent(OnlineEvent var1);

      void fireOfflineEvent(OfflineEvent var1);

      void addHandleListener(Handle.Listener var1);

      void removeHandleListener(Handle.Listener var1);

      int getListenerCount();
   }

   public interface Listener extends EventListener {
      void outputCompleteEventOccurred(OutputCompleteEvent var1);

      void dataEventOccurred(DataEvent var1);

      void directIOEventOccurred(DirectIOEvent var1);

      void statusEventOccurred(StatusEvent var1);

      void errorEventOccurred(ErrorEvent var1);

      void onlineEventOccurred(OnlineEvent var1);

      void offlineEventOccurred(OfflineEvent var1);
   }

   public interface State {
      boolean isOnline();

      void setOnline(boolean var1);
   }
}
