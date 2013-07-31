package com.bywrights.screentimeout.event;

import android.util.Log;

import com.bywrights.screentimeout.Controller;

/**
 * Created by chris on 2013-07-29.
 */
public class UIEventQueue extends EventQueue implements EventRouter.Queue {

    private static final String
        TAG = ".UIEventQueue";
    public static String
        KEY = TAG;

    public UIEventQueue() {
        super( TAG );
    }

    @Override
    public void process( Event event ) {
        try {
            if (event instanceof UIEvent) {
                Controller.sharedInstance().handle_event( (UIEvent)event );
            } else {
                Log.e(TAG, "invalid event: " + event);
            }
        }
        catch (Throwable t) {
            Log.e(TAG, "process: " + event, t);
        }
    }
}
