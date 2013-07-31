package com.bywrights.screentimeout.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.bywrights.screentimeout.util.BuildOptions;
import com.bywrights.screentimeout.util.SqliteDB;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by chris on 13-07-25.
 */
public class App extends DataObject {

    private static final String
        TAG = "App";

    private String uid_;
    private String name_;
    private Hashtable<String,AppPreference> preferences_;
    private Drawable icon_;
    private boolean hydrated_;
    private boolean deleted_;

    App( String uid ) {
        super();
        uid_ = uid;
    }

    App( String uid, String name ) {
        this( uid );

        // hydrate now so we can apply the name after
        hydrate();

        name_ = name;
    }

    public String toString() {
        return uid_;
    }

    static void reset( Model model ) throws Exception {
        SqliteDB db = model.db();
        db.execute( "DROP TABLE IF EXISTS app" );
    }


    static void migrate( Model model ) throws Exception {
        SqliteDB db = model.db();
        if (!db.table_exists("app")) {
            db.execute( "CREATE TABLE app ("
                + " uid VARCHAR NOT NULL PRIMARY KEY,"
                + " name VARCHAR DEFAULT NULL"
                + ")" );
            db.execute( "CREATE INDEX app_name_index ON app( name )" );
        }
    }

    public static Hashtable<String,String> keys( SqliteDB db ) {
        Hashtable<String,String> result = new Hashtable<String,String>();
        Cursor cursor = null;
        try {
            cursor = db.query("SELECT uid FROM app");
            while (cursor.moveToNext()) {
                String key = cursor.getString(0);
                result.put( key, key );
            }
        }
        catch (Exception e) {
            Log.e( TAG, "ordered_keys", e );
        }
        finally {
            if (cursor != null)
                try { cursor.close(); } catch (Exception e) {}
        }
        return result;
    }

    public static Vector<String> ordered_uids( SqliteDB db ) {
        Vector<String> result = new Vector<String>();
        Cursor cursor = null;
        try {
            cursor = db.query("SELECT uid FROM app WHERE name NOT NULL ORDER BY name");
            while (cursor.moveToNext()) {
                result.add( cursor.getString( 0 ) );
            }
        }
        catch (Exception e) {
            Log.e( TAG, "ordered_uids", e );
        }
        finally {
            if (cursor != null)
                try { cursor.close(); } catch (Exception e) {}
        }
        return result;
    }

    static Hashtable<String,App> load( Model model ) {
        Hashtable<String,App> result = new Hashtable<String,App>();
        Cursor cursor = null;
        try {
            cursor = model.query("SELECT uid FROM app", null );
            while (cursor.moveToNext()) {
                String key = cursor.getString( 0 );
                result.put(key, new App(key) );
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
        if (!hydrated_) {
            Cursor cursor = null;
            try {
                cursor = query("SELECT name FROM app WHERE uid = ?", new String[] { uid_ } );
                if (cursor.moveToNext()) {
                    name_ = cursor.getString( 0 );
                }
            }
            catch (Exception e) {
                Log.e( TAG, "hydrate", e );
            }
            finally {
                if (cursor != null)
                    try { cursor.close(); } catch (Exception e) {}
            }

            preferences_ = AppPreference.load( this );

            hydrated_ = true;
        }
    }

    public void save() {
        hydrate();

        try {
            if (deleted_) {
                Object[] uid = new Object[]{ uid_ };
                execute("DELETE FROM app WHERE uid = ?", uid );
                execute("DELETE FROM app_preference WHERE app = ?", uid );

            } else {
                execute("REPLACE INTO app( uid, name ) VALUES( ?, ? )", new Object[]{
                    uid_,
                    name_,
                });

                AppPreference.save( this );
            }
        }
        catch (Exception e) {
            Log.e( TAG, "save", e );
        }
    }

    void delete() {
        deleted_ = true;
        save();
    }

    public String uid() {
        return uid_;
    }

    public String package_name(){
        return uid_;
    }

    public String name(){
        hydrate();
        return name_;
    }

    void set_name( String name ) {
        hydrate();
        name_ = name;
    }

    public Drawable icon(){
        return icon_;
    }

    void set_icon( Drawable icon ) {
        icon_ = icon;
    }

    public Hashtable<String,AppPreference> preferences() {
        hydrate();
        return preferences_;
    }

    public void update_preference( AppPreference preference ) {
        hydrate();
        preferences_.put( preference.uid(), preference );
    }

    public void apply_preferences( Context context ) {
        boolean applied_any = false;
        Hashtable<String,AppPreference> preferences = preferences();
        Collection<AppPreference> prefs = preferences.values();
        for (AppPreference pref : prefs) {
            applied_any |= pref.override_system_value( context, !applied_any );
        }
        if (!applied_any) {
            if (BuildOptions.DEV)
                Toast.makeText(context, name(), Toast.LENGTH_SHORT).show();
        }
    }

    public void remove_preferences( Context context ) {
        Hashtable<String,AppPreference> preferences = preferences();
        Collection<AppPreference> prefs = preferences.values();
        for (AppPreference pref : prefs) {
            pref.restore_system_value( context, false );
        }
    }

}
