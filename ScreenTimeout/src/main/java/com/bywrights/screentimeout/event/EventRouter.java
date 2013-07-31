package com.bywrights.screentimeout.event;

import android.util.Log;

import com.bywrights.screentimeout.Controller;
import com.bywrights.screentimeout.util.BuildOptions;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by chris on 2013-07-29.
 */
public class EventRouter extends EventQueue {

    private static final String
        TAG = Controller.AUTHORITY + ".EventRouter";

    public interface Queue {

        public void process( Event event );

        public void reset();

        public void shutdown();
    }

    private Hashtable<String, Queue>
        routes_ = new Hashtable<String, Queue>();

    public EventRouter() {
        super( TAG );

        register( new UIEventQueue(), UIEventQueue.KEY );
    }


    private Queue _queue( String key ) {
        return (Queue)routes_.get(key);
    }

    public static Queue queue( String key ) {
        EventRouter router = Controller.sharedInstance().router();
        return (router == null) ? null : router._queue(key);
    }

    private void register( Queue queue, String key ) {
        routes_.put(key, queue);
    }

    public final void shutdown() {
        Enumeration<String> keys = routes_.keys();
        while (keys.hasMoreElements()) {
            ((Queue)routes_.get(keys.nextElement())).shutdown();
        }
        super.shutdown();
    }

    public void process( Event event ) {

        if (event == null)
            return;
        String destination = event.destination();

        try {

            Queue queue = queue(destination);
            if (queue != null) {
                Log.d(TAG, "sendMsg: " + event);
                queue.process(event);
            } else {
                if (BuildOptions.UNIT_TEST) {
                    if (event instanceof UIEvent) {
                        // unit tests don't hit the UI
                        return;
                    }
                }
                Log.e( TAG, "message to invalid queue: " + event );
            }
        }
        catch (Throwable t) {
            Log.e( TAG, "_enqueue", t );
        }
    }

    public static void enqueue( Event event ) {

        if (event != null) {
            EventRouter router = Controller.sharedInstance().router();
            if (router == null) {
                Log.e( TAG, "Sending event before router initialized: " + event  );
            } else {
                router.add(event);
            }
        }
    }
}

