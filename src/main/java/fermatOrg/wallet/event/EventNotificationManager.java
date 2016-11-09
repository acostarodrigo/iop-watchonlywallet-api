package fermatOrg.wallet.event;

import com.google.common.base.Preconditions;
import fermatOrg.wallet.event.IncomingTransactionEvent;
import fermatOrg.wallet.event.IncomingTransactionListener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rodrigo on 11/7/16.
 */
public class EventNotificationManager {
    //class variables
    private List<IncomingTransactionListener> listeners;
    private List<IncomingTransactionEvent> events;
    private EventNotificationAgent agent;

    //class constants
    private final File eventsDB; // the file that will save all events.
    private final String eventsDBName = "events"; // the file name in which we are writting to.
    private static int ITERATION_DELAY = 60; //defaults to 60 seconds.


    /**
     * Sets the amount of seconds of delay for the iterations of the agent. The amoun of seconds specified here
     * will be the delay of each iteration to notify pending events.
     * @param seconds the amount of seconds to wait for each iteration
     */
    public static void setIterationDelay(int seconds){
        ITERATION_DELAY = seconds;
    }

    /**
     * default constructor
     */
    public EventNotificationManager() {
        this.eventsDB = new File(eventsDBName);

        //initialize objects
        this.events = new ArrayList<>();

        // stats the monitoring agent on a new thread if the file exists.
        agent = new EventNotificationAgent(ITERATION_DELAY);
        startMonitoring();
    }

    /**
     * Starts the agent monitoring on a new thread if the file already exists.
     * The file exists only after an event has been saved.
     */
    private void startMonitoring() {
        if (!eventsDB.exists())
            return;

        // will make the initial load of events from disk.
        try {
            events = loadEvents();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread agentThread = new Thread(agent);
        agentThread.start();
    }

    /**
     * Adds a new listener. The agent monitoring pending notification events will let these listener know there are events.
     * @param newListener the new listener to add
     */
    public void addIncomingTransactionListener(IncomingTransactionListener newListener){
        if (listeners == null)
            listeners = new ArrayList<>();

        listeners.add(newListener);
    }


    /**
     * Removes an existing listener. Removed listener won't be notified of new or pending events.
     * @param listener the listener to remove
     */
    public void removeIncomingTransactionListener(IncomingTransactionListener listener){
        Preconditions.checkNotNull(listener);

        if (listeners == null)
            listeners = new ArrayList<>();


        listeners.remove(listener);

    }

    /**
     * Adds a new event to the event db and triggers notification to listeners.
     * @param event
     * @throws IOException
     */
    public void addNewEvent(IncomingTransactionEvent event) throws IOException {
        this.events.add(event);
        saveEvents();

        //once added, let's notify it
        triggerEvent(event);
    }

    /**
     * saves the events list of events to the file.
     * @throws IOException an IO error during save
     */
    private void saveEvents() throws IOException {
        ObjectOutputStream oos = null;
        FileOutputStream fout = null;
        try{
            fout = new FileOutputStream(eventsDB);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(events);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if(oos  != null){
                oos.close();
            }
        }

        /**
         * In case this is the first save, will start the agent if it is not running
         */
        if (!agent.isRunning)
            startMonitoring();
    }

    /**
     * Triggers the incoming event method of each registered listener.
     * @param event the event to notify
     */
    private void triggerEvent(IncomingTransactionEvent event){
        // no listeners registered, don't neet to trigger
        if (listeners == null)
            return;


        for (IncomingTransactionListener listener : listeners){
            listener.incomingEvent(event);
        }
    }

    /**
     * Loads the list of events from the file.
     * @return the list of events stored on the file.
     * @throws IOException in case there is an IO error during loading.
     */
    private List<IncomingTransactionEvent> loadEvents() throws IOException {
        ObjectInputStream objectinputstream = null;
        try {
            FileInputStream streamIn = new FileInputStream(eventsDB);
            objectinputstream = new ObjectInputStream(streamIn);
            List<IncomingTransactionEvent> readCase = (List<IncomingTransactionEvent>) objectinputstream.readObject();
            return readCase;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(objectinputstream != null){
                objectinputstream .close();
            }
        }

        return null;
    }

    /**
     * gets the list of events stored on file and returns only the pending ones.
     * @return the list of pending notification events.
     */
    public List<IncomingTransactionEvent> getPendingNotificationEvents() {
        // filter only pending
        List<IncomingTransactionEvent> pendingEvents = new ArrayList<>();
        for (IncomingTransactionEvent event : this.events){
            if (event.getStatus() == IncomingTransactionEvent.Status.PENDING_NOTIFICATION)
                pendingEvents.add(event);
        }

        return pendingEvents;
    }

    /**
     * marks an event as notified, so the agent won't trigger any notifications from this event.
     * @param event the event to mark as notified.
     */
    public void confirmEventNotification(IncomingTransactionEvent event){
        if (this.events.contains(event)){
            events.remove(event);
            event.setStatus(IncomingTransactionEvent.Status.NOTIFICATION_COMPLETED);
            events.add(event);
            try {
                this.saveEvents();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Agent that, on a schedulled basis, will get the list of pending events to notify and raise
     * new notification to registered listeners.
     */
    private class EventNotificationAgent implements Runnable{
        private final int seconds;
        private boolean isRunning;

        /**
         * constructor
         * @param seconds the time in seconds to iterate the pending notification events.
         */
        public EventNotificationAgent(int seconds) {
            this.seconds = seconds;
            this.isRunning = false;
        }

        @Override
        public void run() {
            // sets the running flag
            isRunning = true;

            // for each listener, I raise the event.
            for (IncomingTransactionEvent pendingEvent : getPendingNotificationEvents())
                triggerEvent(pendingEvent);

            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                isRunning = false;
                e.printStackTrace();
            }
        }

        /**
         * returns true if the agent is running.
         * @return true if running. False otherwise.
         */
        public boolean isRunning() {
            return isRunning;
        }
    }
}
