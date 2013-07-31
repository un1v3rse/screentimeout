package com.bywrights.screentimeout.event;

import com.bywrights.screentimeout.Controller;

/**
 * Created by chris on 2013-07-29.
 */
public class Event {

    private final String
        destination_;

    public Event( String destination ) {
        destination_ = destination;
    }

    public String destination() { return destination_; }

    public boolean cancelled() { return false; }

    public void enqueue() {
        Controller.sharedInstance().router().enqueue( this );
    }
}
