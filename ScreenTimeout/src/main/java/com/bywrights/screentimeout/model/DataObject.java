package com.bywrights.screentimeout.model;

import android.database.Cursor;

/**
 * Created by chris on 2013-07-30.
 */
public abstract class DataObject {

    DataObject() {
    }

    Cursor query( String sql, String[] args ) throws Exception {
        return Model.sharedInstance().query( sql, args );
    }

    void execute( String sql, Object[] args ) throws Exception {
        Model.sharedInstance().execute( sql, args );
    }

    abstract void hydrate();
    abstract public void save();
    abstract void delete();

    abstract public Object uid();
}
