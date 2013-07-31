package com.bywrights.screentimeout.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.bywrights.screentimeout.Controller;

/**
 * Created by chris on 2013-07-29.
 */
public class Prefs {

    private static final String
        TAG = "Prefs",
        PREFS_NAME = Controller.AUTHORITY + ".prefs";

    private static final String
        ACTIVE_APP_UID = "ACTIVE_APP_UID",
        DEBUG_UI = "DEBUG_UI";

    private static SharedPreferences
        PREFS;

    static void init( Context context ) {
        PREFS = context.getSharedPreferences( PREFS_NAME, Context.MODE_PRIVATE );
    }

    public static final void save() {
        // do nothing, saves are incremental
    }

    private static final String get( String id, String def ) {
        return PREFS.getString( id, def );
    }

    private static final boolean get( String id, boolean def ) {
        return PREFS.getBoolean( id, def );
    }

    private static final int get( String id, int def ) {
        return PREFS.getInt( id, def );
    }

    private static final long get( String id, long def ) {
        return PREFS.getLong( id, def );
    }

    private static final void put( String id, String value ) {
        SharedPreferences.Editor editor = PREFS.edit();
        editor.putString( id, value );
        editor.commit();
    }

    private static final void put( String id, boolean value ) {
        SharedPreferences.Editor editor = PREFS.edit();
        editor.putBoolean( id, value );
        editor.commit();
    }

    private static final void put( String id, int value ) {
        SharedPreferences.Editor editor = PREFS.edit();
        editor.putInt( id, value );
        editor.commit();
    }

    private static final void put( String id, long value ) {
        SharedPreferences.Editor editor = PREFS.edit();
        editor.putLong( id, value );
        editor.commit();
    }

    public static final String active_app_uid() { return get( ACTIVE_APP_UID, null ); }
    public static final void active_app_uid( long value ) { put( ACTIVE_APP_UID, value ); }

    public static final boolean debug_ui() { return get( DEBUG_UI, false ); }
    public static final void debug_ui( boolean value ) { put( DEBUG_UI, value ); }

}
