package com.bywrights.screentimeout;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.bywrights.screentimeout.activity.AppDetailActivity;
import com.bywrights.screentimeout.activity.AppListActivity;
import com.bywrights.screentimeout.activity.BaseActivity;
import com.bywrights.screentimeout.controls.Toaster;
import com.bywrights.screentimeout.event.EventRouter;
import com.bywrights.screentimeout.event.UIEvent;
import com.bywrights.screentimeout.model.App;
import com.bywrights.screentimeout.model.Model;
import com.bywrights.screentimeout.service.ForegroundListeningService;
import com.bywrights.screentimeout.util.Rx;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * Created by chris on 2013-07-29.
 */


/*

    Program Structure (these concepts correspond to classes and packages)

    Controller

        This is where any central initialization that the app needs on the start of any Activity will
        happen.

        This is also where we wrap the activity dispatch methods.  Forming dispatch for activities can be
        complicated, and this gives us DRY for it.  It also enforces type safety on our intent params.

    Activity

        All user interface presentation is wrapped in a Activity, of course.  In most instances,
        little code should be in the activity, aside from deciding what fragments to present.

        This project contains two activities, AppListActivity and AppDetailActivity.  The former will
        present both list and detail fragments if screen space allows.

    Fragment

        The UI components of the app.  Cast as fragments so they can be positioned differently for
        different screen sizes, such as Master->Detail on a tablet.

        This project contains two fragments, AppListFragment and AppDetailFragment, which are either
        presented individually, or in master->detail on larger screens.

    Model

        This is where the business logic of the application resides, and persistence is managed.

        The model is shared between Services and Activities via a singleton pattern that is
        centralized in the Controller.

    Receiver

        This is where our external event receivers live.

        We only have one receiver, listening for the boot event, which starts our listening service.

    Service

        Services are a little more generic than the Android Service, in that they don't actually
        have to run as a service.  What I mean by Service is a compartmentalized piece of functionality
        that manages its own state.

        There is a single service for this app, and it is in fact a real Android service, that runs
        in the background listening for Applications to appear to trigger our events.


 */

public final class Controller extends Application {

    public static final String
        AUTHORITY = "com.bywrights.screentimeout",

        ALLOW_BACK = AUTHORITY + ".ALLOW_BACK",
        APP_UID = AUTHORITY + ".APP_UID";

    private static final String
        TAG = AUTHORITY + ".Controller";

    private static Controller
        INSTANCE = null;
    private final Vector<WeakReference<BaseActivity>>
        activities_ = new Vector<WeakReference<BaseActivity>>();
    private EventRouter
        router_;


    public static Controller sharedInstance() {
        if (INSTANCE == null)
            throw new IllegalStateException("Application not created yet!");
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable ex) {
            ex.printStackTrace();
            Log.e( TAG, thread.getName(), ex );
            }
        } );

        INSTANCE = this;
        router_ = new EventRouter();

        Rx.init(this);
        Model.init(this);

        try {
            Model.sharedInstance().onCreate();
        }
        catch (Exception e) {
            Log.e( TAG, "Model.onCreate", e );
            System.exit( -1 );
        }

        ForegroundListeningService.start( getBaseContext() );
    }

    public EventRouter router() { return router_; }

    public void register( BaseActivity activity ) {
        unregister( activity );
        activities_.add( new WeakReference<BaseActivity>( activity ) );
    }

    public void unregister( BaseActivity activity ) {
        for (int ii = activities_.size(); ii-->0;) {
            BaseActivity found = activities_.get( ii ).get();
            if (found == activity || found == null)
                activities_.remove( ii );
        }
    }

    public void finish_all( BaseActivity except ) {
        for (int ii = activities_.size(); ii-->0;) {
            BaseActivity activity = activities_.get( ii ).get();
            if (activity != null && activity != except) {
                activities_.remove( ii );
                activity.overridePendingTransition( 0, 0 );
                activity.finish();
            }
        }
    }

    public static String app_uid( Intent intent ) { return intent.getStringExtra( APP_UID ); }
    private static void app_uid( Intent intent, String value ) { intent.putExtra( APP_UID, value ); }

    public void show_app_list( Context context ) {
        Intent intent = new Intent( context, AppListActivity.class );
        context.startActivity(intent);
    }


    public void show_app( Context context, App app ) {
        Intent intent = new Intent( context, AppDetailActivity.class );
        app_uid( intent, app.uid() );
        context.startActivity(intent);
    }



    public static final void show_error( Context context, CharSequence error, boolean long_duration ) {
        if (error != null && error.length() > 0 ) {
            Toaster.show(context, error, long_duration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        }
    }

    public static final void show_error( Context context, int resId, boolean long_duration ) {
        Toaster.show(context, resId, long_duration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
    }

    public static final void show_message( Context context, CharSequence message, boolean long_duration ) {
        if (message != null && message.length() > 0 ) {
            Toaster.show(context, message, long_duration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        }
    }

    public static final void show_message( Context context, int resId, boolean long_duration ) {
        Toaster.show(context, resId, long_duration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
    }

    public boolean handle_event( final UIEvent event ) {

        if (!event.cancelled()) {
            switch (event.type()) {

                case UIEvent.GRACEFUL_EXIT:
                    finish_all( null );
                    break;

                case UIEvent.FATAL_ERROR:
                    new Handler(getBaseContext().getMainLooper()).post( new Runnable() { public void run() {
                        new AlertDialog.Builder(getBaseContext())
                                .setMessage((String) event.context())
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                        System.exit( -1 );
                    } } );
                    return true; // bail immediately, don't pass along

                case UIEvent.RELOAD:
                    if (event.context() instanceof String) {
                        final String msg = (String)event.context();
                        if (msg != null) {
                            new Handler(getBaseContext().getMainLooper()).post( new Runnable() { public void run() {
                                show_message( getBaseContext(), msg, false );
                            } } );
                        }
                    }
                    show_app_list( getBaseContext() );
                    break;
            }

            // pass along to activities
            new Handler(getBaseContext().getMainLooper()).post( new Runnable() { public void run() {
                for (WeakReference<BaseActivity> ref: activities_) {
                    BaseActivity activity = ref.get();
                    if (activity != null) {
                        activity.handle_event( event );
                    }
                }
            } } );
        }
        return true;
    }
}
