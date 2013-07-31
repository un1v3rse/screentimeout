package com.bywrights.screentimeout.event;

/**
 * Created by chris on 2013-07-29.
 */
public class UIEvent extends Event {

    public static final int
        GRACEFUL_EXIT = 1,
        FATAL_ERROR = 2,
        RELOAD = 3,
        RESHOW_DIALOG = 4;

    private final int
        type_;
    private final Object
        context_;

    public UIEvent( int type, Object context ) {
        super( UIEventQueue.KEY );
        type_ = type;
        context_ = context;
    }

    public int type() { return type_; }
    public Object context() { return context_; }

    public boolean cancelled() { return false; }

    public static interface Handler {
        public boolean handle_event( UIEvent event );
    }
}
