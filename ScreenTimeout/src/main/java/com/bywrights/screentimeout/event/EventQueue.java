package com.bywrights.screentimeout.event;

import com.bywrights.screentimeout.util.QueuedThread;

import java.util.concurrent.Callable;

/**
 * Created by chris on 2013-07-29.
 */
public abstract class EventQueue extends QueuedThread {

    public EventQueue( String name ) {
        super( name );
    }

    private class Task implements Callable<Object> {

        private final Event
            event_;

        public Task( Event event ) {
            event_ = event;
        }

        public Object call() {

            process( event_ );
            return null;
        }
    }

    abstract public void process( Event event );

    public void add( Event event ) {
        add( new Task( event ) );
    }
}
