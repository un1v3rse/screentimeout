package com.bywrights.screentimeout.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.bywrights.screentimeout.model.App;
import com.bywrights.screentimeout.model.Model;

import java.util.List;

/**
 * Created by chris on 2013-07-30.
 */
public final class ForegroundListeningService extends Service {

    private static final String
        TAG = "com.bywrights.service.ForegroundListeningService";

    private ListeningThread
        listening_thread_;
    private final Object
        active_app_package_name_sync_ = new Object();
    private String
        active_app_package_name_;
    private App
        active_app_;

    public static void start( Context context ) {
        Intent intent = new Intent(context,ForegroundListeningService.class);
        context.startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void onDestroy() {
        Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");

        listening_thread_.cancel();

        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        listening_thread_ = new ListeningThread();
        listening_thread_.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid)
    {
        int result = super.onStartCommand( intent, flags, startid );



        return result;
    }

    private String active_app_package() {
        String found = null;
        ActivityManager am = (ActivityManager)getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> running = am.getRunningTasks(1);
        if (running.size() == 1) {
            ActivityManager.RunningTaskInfo info = running.get( 0 );
            found = info.topActivity.getPackageName();
        }
        return found;
    }

    private void check_active_app() throws Exception {
        String found = active_app_package();

        if (found != null) {
            synchronized (active_app_package_name_sync_) {
                if (!found.equals(active_app_package_name_)) {
                    Message msg = new Message();
                    msg.obj=found;
                    handler_.sendMessage(msg);
                }
            }
        }
    }

    // handle app settings on main thread so we can show UI when needed
    private Handler handler_ = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            String found = (String)msg.obj;
            synchronized (active_app_package_name_sync_) {
                // don't do anything if the package is already active, or if the package changed again
                //  since the event fired
                if (found.equals(active_app_package_name_) || !found.equals(active_app_package())) {
                    return;
                } else {
                    active_app_package_name_ = found;
                }
            }


            if (active_app_ != null) {
                active_app_.remove_preferences( getBaseContext() );
            }

            active_app_ = Model.sharedInstance().find_app( found );

            if (active_app_ != null) {
                active_app_.apply_preferences( getBaseContext() );
            }

        }
    };

    private final class ListeningThread extends Thread {

        private final Object
            sync_ = new Object();
        private volatile boolean
            cancelled_;

        public ListeningThread() {
            super( TAG + ".ListeningThread" );
        }

        void cancel() {
            cancelled_ = true;
            sync_.notify();
        }

        public void run() {
            while (!cancelled_) {

                try {
                    check_active_app();
                }
                catch (Throwable t) {
                    Log.e( TAG, "run", t );
                }

                synchronized (sync_) {
                    try {
                        sync_.wait(1000);
                    }
                    catch (InterruptedException e) {
                        // we expect this
                    }
                }

            }
        }
    }

}
