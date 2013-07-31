package com.bywrights.screentimeout.model;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.bywrights.screentimeout.Controller;
import com.bywrights.screentimeout.R;
import com.bywrights.screentimeout.controls.Toaster;
import com.bywrights.screentimeout.util.Rx;
import com.bywrights.screentimeout.util.SqliteDB;

import java.util.Collection;
import java.util.Hashtable;

/**
 * Created by chris on 2013-07-30.
 */
public class AppPreference extends DataObject {

    private static final String
        TAG = Controller.AUTHORITY + ".AppPreference";

    public static final int
        NULL_TYPE = -1,
        SCREEN_TIMEOUT = 0,
        MUSIC_VOLUME = 1,
        COUNT = 2;

    private static final int[] NAME_IDS = {
        R.string.screen_timeout,
        R.string.music_volume,
    };

    public static final String
        UID_SCREEN_TIMEOUT = "screen_timeout",
        UID_MUSIC_VOLUME = "music_volume";


    public static final String[] UIDS = {
         UID_SCREEN_TIMEOUT,
         UID_MUSIC_VOLUME,
    };

    private final String
        uid_;
    private final int
        type_;
    private boolean
        enabled_;
    private String
        value_,
        overridden_system_value_;

    private static final int type( String uid ) {
        for (int ii = 0; ii < COUNT; ++ii) {
            if (UIDS[ii].equals(uid))
                return ii;
        }
        return NULL_TYPE;
    }

    public AppPreference( String uid ) {
        super();
        uid_ = uid;
        type_ = type( uid );
    }

    public String toString() {
        return uid_;
    }

    static void reset( Model model ) throws Exception {
        SqliteDB db = model.db();
        db.execute( "DROP TABLE IF EXISTS app_preference" );
    }

    static void migrate( Model model ) throws Exception {
        SqliteDB db = model.db();
        if (!db.table_exists("app_preference")) {
            db.execute( "CREATE TABLE app_preference ("
                    + " app VARCHAR NOT NULL,"
                    + " preference VARCHAR NOT NULL,"
                    + " value VARCHAR DEFAULT NULL,"
                    + " enabled VARCHAR DEFAULT 0"
                    + ")" );
            db.execute( "CREATE UNIQUE INDEX app_preference_index ON app_preference( app, preference )" );
        }
    }

    static Hashtable<String,AppPreference> load( App app ) {
        Hashtable<String,AppPreference> result = new Hashtable<String,AppPreference>();
        Cursor cursor = null;
        try {
            cursor = app.query("SELECT preference, value, enabled FROM app_preference WHERE app = ?", new String[]{app.uid()});
            while (cursor.moveToNext()) {
                String uid = cursor.getString( 0 );
                AppPreference pref = new AppPreference(uid);
                pref.value_ = cursor.getString( 1 );
                pref.enabled_ = cursor.getInt( 2 ) != 0;
                result.put( uid, pref );
            }
        }
        catch (Exception e) {
            Log.e( TAG, "load", e );
        }
        finally {
            if (cursor != null)
                try { cursor.close(); } catch (Exception e) {}
        }
        return result;
    }

    void hydrate() {
        // no hydration, object is fully loaded in load()
    }

    public void save() {
        // no save, list is saved atomically in save( App )
    }

    void delete() {
        // no save, list elements deleted atomically in save( App )
    }

    static void save( App app ) throws Exception {
        app.execute("DELETE FROM app_preference WHERE app = ?", new Object[]{app.uid()});
        Hashtable<String,AppPreference> preferences = app.preferences();
        Collection<AppPreference> prefs = preferences.values();
        for (AppPreference preference : prefs) {

            app.execute("REPLACE INTO app_preference( app, preference, value, enabled ) VALUES( ?, ?, ?, ? )", new Object[]{
                    app.uid(),
                    preference.uid(),
                    preference.value(),
                    new Integer(preference.enabled() ? 1 : 0)
            });
        }
    }


    public String uid() { return uid_; }

    public int name_id() {
        return type_ == NULL_TYPE ? R.string.null_type : NAME_IDS[type_];
    }

    public String name( Context context ) {
        return context.getString( name_id() );
    }


    public String value() {
        hydrate();
        return value_;
    }
    public void set_value( String value ) {
        value_ = value;
    }

    public boolean enabled() {
        return enabled_;
    }

    public void set_enabled( boolean enabled ) {
        enabled_ = enabled;
    }

    public String min( Context context ) {
        String result = null;
        switch (type_) {
            case SCREEN_TIMEOUT:
                result = "0";
                break;

            case MUSIC_VOLUME:
                result = "0";
                break;
        }
        return result;
    }

    public String max( Context context ) {
        String result = null;
        switch (type_) {
            case SCREEN_TIMEOUT:
                result = "30"; // minutes
                break;

            case MUSIC_VOLUME:
                try {
                    AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                    int volume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    result = Integer.toString( volume );
                } catch (Exception e) {
                    Log.e( TAG, "max: " + name(context), e );
                }
                break;
        }
        return result;
    }

    private static String set_screen_timeout( Context context, String value, boolean show_ui ) {
        String result = null;
        try {
            int millis = android.provider.Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
            String saved = Integer.toString( millis / 60000 );

            if (value != null) {
                int minutes = Integer.parseInt( value );

                android.provider.Settings.System.putInt(context.getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, minutes <= 0 ? -1 : (minutes * 60000) );

                if (show_ui) {
                    Toaster.show( context, minutes <= 0 ? Rx.s(R.string.disabled_screen_timeout) : Rx.fmt(R.string.fmt_screen_timeout_x_minutes, minutes ) , Toast.LENGTH_SHORT );
                }
            }

            result = saved;
        } catch (Exception e) {
            Log.e( TAG, "set_screen_timeout: " + value, e );
        }
        return result;
    }

    private static String set_music_volume( Context context, String value, boolean show_ui ) {
        String result = null;
        try {
            AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            int volume = am.getStreamVolume( AudioManager.STREAM_MUSIC );
            String saved = Integer.toString(volume);

            if (value != null) {
                volume = Integer.parseInt( value );
                am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, show_ui ? AudioManager.FLAG_SHOW_UI : 0);
            }

            result = saved;
        } catch (Exception e) {
            Log.e( TAG, "set_music_volume: " + value, e );
        }
        return result;
    }

    public String current_system_value( Context context ) {
        // bit of a hack, rely on setting the value to null returning the old/current value
        switch (type_) {
            case SCREEN_TIMEOUT:
                return set_screen_timeout( context, null, false );

            case MUSIC_VOLUME:
                return set_music_volume( context, null, false );
        }
        return null;
    }

    public boolean override_system_value( Context context, boolean show_ui ) {
        hydrate();
        overridden_system_value_ = null;

        if (enabled_) {
            switch (type_) {
                case SCREEN_TIMEOUT:
                    overridden_system_value_ = value_ == null ? null : set_screen_timeout( context, value_, show_ui );
                    break;

                case MUSIC_VOLUME:
                    overridden_system_value_ = value_ == null ? null : set_music_volume( context, value_, show_ui );
                    break;
            }
        }
        return overridden_system_value_ != null;
    }

    public boolean restore_system_value( Context context, boolean show_ui ) {

        boolean result = overridden_system_value_ != null;
        switch (type_) {
            case SCREEN_TIMEOUT:
                set_screen_timeout( context, overridden_system_value_, show_ui );
                break;

            case MUSIC_VOLUME:
                set_music_volume( context, overridden_system_value_, show_ui );
                break;
        }

        overridden_system_value_ = null;
        return result;
    }

}
