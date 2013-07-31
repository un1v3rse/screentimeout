package com.bywrights.screentimeout.util;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by chris on 13-07-25.
 */
public class DateUtil {
    private static String
            TAG = "util.DataUtil";

    public static final SimpleDateFormat
            ISO_DATETIME_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );


    public static final String date( Context context, Date date ) {
        return date == null ? "" : android.text.format.DateFormat.getLongDateFormat( context ).format( date );
    }

    public static final String time( Context context, Date date ) {
        return date == null ? "" : android.text.format.DateFormat.getTimeFormat( context ).format( date );
    }

    public static final String datetime( Context context, Date date ) {
        return date == null ? "" : new StringBuilder( android.text.format.DateFormat.getLongDateFormat( context ).format( date ) ).append(" ").append( android.text.format.DateFormat.getTimeFormat( context ).format( date ) ).toString();
    }

    public static final Date midnight( Date date ) {
        return date == null ? null : new Date( midnight( date.getTime() ) );
    }

    public static final long midnight( long time ) {
        return time == 0L ? 0L : (midday( time ) - (12 * DateUtils.HOUR_IN_MILLIS));
    }

    public static final long midnight() {
        return midnight( System.currentTimeMillis() );
    }

    public static final Date midday( Date date ) {
        Calendar cal = Calendar.getInstance();
        cal.setTime( date );
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static final long midday( long time ) {
        return midday( new Date( time ) ).getTime();
    }

    public static final String now_iso() {
        return datetime_to_iso( new Date() );
    }

    public static final String datetime_to_iso( Date date ) { return date == null ? null : ISO_DATETIME_FORMAT.format( date ); }
    public static final Date datetime_from_iso( String date ) {
        try {
            return date == null ? null : ISO_DATETIME_FORMAT.parse( date );
        } catch (Throwable t) {
            Log.w(TAG, new StringBuilder("datetime_from_iso( ").append(date).append(" )").toString(), t);
        }
        return null;
    }

    public static final boolean is_today( Date date ) {
        return date == null ? false : is_today( date.getTime() );
    }
    public static final boolean is_today( long time ) {
        long midnight = midnight();
        return (time >= midnight) && (time < (midnight + DateUtils.DAY_IN_MILLIS));
    }

    public static final boolean is_yesterday( Date date ) {
        return date == null ? false : is_yesterday( date.getTime() );
    }
    public static final boolean is_yesterday( long time ) {
        long midnight = midnight();
        return (time < midnight) && (time >= (midnight - DateUtils.DAY_IN_MILLIS));
    }

    public static final String raw_ms( long start, long end ) {
        return end == 0 ? "" : Long.toString( end - start );
    }
}
