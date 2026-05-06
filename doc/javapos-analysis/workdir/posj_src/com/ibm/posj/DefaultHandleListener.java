package com.ibm.posj;

import com.ibm.posj.event.DataEvent;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
import com.ibm.posj.event.OutputCompleteEvent;
import com.ibm.posj.event.StatusEvent;

public class DefaultHandleListener implements Handle.Listener {
   public void outputCompleteEventOccurred(OutputCompleteEvent event) {
   }

   public void dataEventOccurred(DataEvent event) {
   }

   public void directIOEventOccurred(DirectIOEvent event) {
   }

   public void statusEventOccurred(StatusEvent event) {
   }

   public void errorEventOccurred(ErrorEvent event) {
   }

   public void onlineEventOccurred(OnlineEvent event) {
   }

   public void offlineEventOccurred(OfflineEvent event) {
   }
}
