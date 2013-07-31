package com.bywrights.screentimeout.model;

import java.util.Vector;

/**
 * Created by chris on 2013-07-29.
 */
public class AppCollection {
    private final Vector<String>
        app_uids_;

    AppCollection( Vector<String> app_uids ) {
        app_uids_ = app_uids;
    }

    public int count() {
        return app_uids_.size();
    }

    public int position( String uid ) {
        int count = app_uids_.size();
        for (int ii = 0; ii < count; ++ii) {
            if (app_uids_.get(ii).equals(uid))
                return ii;
        }
        return -1;
    }

    public int position( App app ) {
        return position( app.uid() );
    }

    public App app( int position ) {
        return position < app_uids_.size() ? Model.sharedInstance().app( app_uids_.get(position) ) : null;
    }

    public App app( String uid ) {
        return Model.sharedInstance().app( uid );
    }

}
