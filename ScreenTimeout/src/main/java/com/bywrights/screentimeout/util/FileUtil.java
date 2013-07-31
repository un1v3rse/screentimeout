package com.bywrights.screentimeout.util;

import android.content.Context;

/**
 * Created by chris on 13-07-25.
 */
public class FileUtil {

    public static final String app_data_folder( Context context ) {
        return new StringBuilder("/data/data/").append(context.getPackageName()).append("/").toString();
    }
}
