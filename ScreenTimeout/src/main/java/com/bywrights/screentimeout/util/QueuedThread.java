package com.bywrights.screentimeout.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chris on 2013-07-29.
 */
public abstract class QueuedThread {

    private final String
        name_;

    private final Object
        exec_sync_ = new Object();
    private ExecutorService
        executor_;

    public QueuedThread( String name ) {
        name_ = name;
        executor_ = Executors.newSingleThreadExecutor();
    }

    public void add( Callable<Object> task ) {
        synchronized( exec_sync_ ) {
            executor_.submit( task );
        }
    }

    public void reset() {
        synchronized( exec_sync_ ) {
            executor_.shutdownNow();
            executor_ = Executors.newSingleThreadExecutor();
        }
    }

    public void shutdown() {
        synchronized( exec_sync_ ) {
            executor_.shutdownNow();
        }
    }
}
