package com.bywrights.screentimeout.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bywrights.screentimeout.service.ForegroundListeningService;

/**
 * Created by chris on 2013-07-30.
 */
public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context arg0, Intent arg1)
    {
        Log.i("BootReceiver", "received boot");

        ForegroundListeningService.start( arg0 );
    }
}
