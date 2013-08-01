package com.bywrights.screentimeout.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.util.Log;

import com.bywrights.screentimeout.Controller;
import com.bywrights.screentimeout.event.UIEvent;
import com.bywrights.screentimeout.util.FileUtil;
import com.bywrights.screentimeout.util.QueuedThread;
import com.bywrights.screentimeout.util.SqliteDB;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by chris on 13-07-25.
 */
public final class Model {
    private static final String
        TAG = Controller.AUTHORITY + "model.Model";
    private static Model
        INSTANCE = null;
    private final Context
        context_;
    private final Queue
        queue_;
    private final SqliteDB
        db_;
    private Hashtable<String, App>
        apps_ = new Hashtable<String, App>();


    public static Model sharedInstance() {
        if (INSTANCE == null)
            throw new IllegalStateException("Application not created yet!");
        return INSTANCE;
    }

    public static void init( Context context ) {
        INSTANCE = new Model( context );
    }

    private Model( Context context ) {
        context_ = context;
        queue_ = new Queue();
        db_ = new SqliteDB( FileUtil.app_data_folder(context_) + "databases/", "db.sqlite" );
    }

    Cursor query( String sql, String[] args ) throws Exception {
        return db_.query( sql, args );
    }

    void execute( String sql, Object[] args ) throws Exception {
        db_.execute( sql, args );
    }

    public App app( String uid ) {
        App app = apps_.get( uid );
        if (app == null) {
            app = new App( uid, null );
            apps_.put( uid, app );
        }
        return app;
    }

    public App find_app( String uid ) {
        return apps_.get( uid );
    }

    public AppCollection ordered_apps() {
        AppCollection result = new AppCollection( App.ordered_uids(db_) );
        if (result.count() == 0) {
            update_apps_from_system();
            result = new AppCollection( App.ordered_uids(db_) );
        }
        return result;
    }

    public void onCreate() throws Exception {

        db_.open(null);

        load( false );

        queue_.add( new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                update_apps_from_system();
                return null;
            }
        });
    }

    SqliteDB db() { return db_; }

    private void reset() throws Exception {
        App.reset(this);
        AppPreference.reset(this);
    }

    private void migrate() throws Exception {
        App.migrate(this);
        AppPreference.migrate(this);
    }



    private void load( boolean reset ) throws Exception {

        try {
            if (reset) {
                reset();
            }

            migrate();

            apps_ = App.load( this );

        } catch (Exception e) {
            Log.e(TAG,"migrate",e);

            // if we fail to load, try resetting, we lose settings but at least we'll run
            if (!reset)
                load( true );
            else
                throw e;
        }
    }

    private void save() {
    }


    private void update_apps_from_system() {
        boolean changed = false;

        // do a mark and sweep to update the list of app_list_activity
        Hashtable<String,App> before = new Hashtable<String,App>( apps_ );

        final PackageManager pm = context_.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo p : packages) {

            String uid = p.packageName;
            String name = p.loadLabel(pm).toString();

            // if the name matches the app, it's probably a system process, don't load it
            if (uid.equals( name ))
                continue;

            App app = before.get( uid );
            if (name != null) {
                if (app == null) {
                    app = new App( uid, name );
                    apps_.put( uid, app );
                    changed = true;
                } else {
                    if (!name.equals( app.name() )) {
                        app.set_name( name );
                        changed = true;
                    }
                    before.remove( uid );
                }
                app.save();
            }

            app.set_icon(p.loadIcon(pm));
        }

        // anything left over should be removed
        if (before.size() > 0) {
            Collection<App> values = before.values();
            for (App app : values) {
                apps_.remove( app.uid() );
                app.delete();
                app.save();
                changed = true;
            }
        }

        if (changed) {
            new UIEvent(UIEvent.RELOAD, null).enqueue();
        }
    }

    private static final class Queue extends QueuedThread {
        public Queue() {
            super("Model");
        }
    }
}
