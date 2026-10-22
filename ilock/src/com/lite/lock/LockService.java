package com.lite.lock;

import android.app.*;
import android.content.*;
import android.os.*;

public class LockService extends Service {
    private final Handler h = new Handler(Looper.getMainLooper());
    private final BroadcastReceiver r = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) {
            // small delay so the system keyguard is already locked when we appear
            h.postDelayed(new Runnable() {
                @Override public void run() {
                    startActivity(new Intent(LockService.this, LockActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }, 400);
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("lock", "Lockscreen", NotificationManager.IMPORTANCE_MIN));
        Notification n = new Notification.Builder(this, "lock")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("iLock is running").build();
        startForeground(1, n);
        registerReceiver(r, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    @Override public int onStartCommand(Intent i, int f, int id) { return START_STICKY; }
    @Override public void onDestroy() { try { unregisterReceiver(r); } catch (Exception e) {} super.onDestroy(); }
    @Override public IBinder onBind(Intent i) { return null; }
}
