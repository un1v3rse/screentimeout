package com.bywrights.screentimeout.controls;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import java.util.Hashtable;

/**
 * Created by chris on 2013-07-29.
 */
public class Toaster {

    private static final Hashtable<Object,Long>
            displayed_toasts_ = new Hashtable<Object,Long>();
    private static long
            last_show_;

    public static void show( Context context, CharSequence message, int duration ) {
        long now = System.currentTimeMillis();
        String key = message.toString();
        if (last_show_ < now) {
            displayed_toasts_.clear();
        } else {
            Long time = displayed_toasts_.get( key );
            if (time != null && time.longValue() > now) {
                return;
            }
        }

        last_show_ = new Long( now + (duration == Toast.LENGTH_LONG ? 5000 : 2000) );
        displayed_toasts_.put( key, new Long( last_show_ ) );
        Toast toast = Toast.makeText( context, message, duration );
        toast.setGravity(Gravity.TOP, 0, 30 );
        toast.show();
    }


    public static void show( Context context, int resId, int duration ) {
        long now = System.currentTimeMillis();
        Integer key = new Integer( resId );
        if (last_show_ < now) {
            displayed_toasts_.clear();
        } else {
            Long time = displayed_toasts_.get( key );
            if (time != null && time.longValue() > now) {
                return;
            }
        }

        last_show_ = new Long( now + (duration == Toast.LENGTH_LONG ? 5000 : 2000) );
        displayed_toasts_.put( key, new Long( last_show_ ) );
        Toast toast = Toast.makeText( context, resId, duration );
        toast.setGravity(Gravity.TOP, 0, 30 );
        toast.show();
    }
}
