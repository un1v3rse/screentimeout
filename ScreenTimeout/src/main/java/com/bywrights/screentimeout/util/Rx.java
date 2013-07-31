package com.bywrights.screentimeout.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by chris on 13-07-25.
 */
public class Rx {
    public static final String
        COLON = ": ",
        ELLIPSIS = "...",
        EOL = "\r\n";

    private static Context
        CONTEXT;

    public static final void init( Context context ) {
        CONTEXT = context;
    }

    public static final String s( int resId ) {
        return CONTEXT.getString(resId);
    }

    public static final String fmt( int resId, Object... args ) {
        return String.format( s( resId ), args );
    }

    public static final InputStream asset( String path ) throws IOException {
        return CONTEXT.getAssets().open( path );
    }
}
