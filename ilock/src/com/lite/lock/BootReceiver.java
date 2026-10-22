package com.lite.lock;

import android.content.*;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        c.startForegroundService(new Intent(c, LockService.class));
    }
}
