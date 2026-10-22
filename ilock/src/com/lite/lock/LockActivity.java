package com.lite.lock;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.*;
import android.graphics.Color;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;

public class LockActivity extends Activity {
    private LockView view;
    private KeyguardManager km;
    private boolean torch, registered;
    private final BroadcastReceiver tick = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) { view.invalidate(); }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setShowWhenLocked(true);
        Window w = getWindow();
        w.setStatusBarColor(Color.TRANSPARENT);
        w.setNavigationBarColor(Color.TRANSPARENT);
        w.getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        view = new LockView(this);
        setContentView(view);
    }

    @Override protected void onResume() {
        super.onResume();
        if (!km.isKeyguardLocked()) { finish(); return; }
        view.reload();
        if (!registered) { registerReceiver(tick, new IntentFilter(Intent.ACTION_TIME_TICK)); registered = true; }
    }

    @Override protected void onPause() {
        super.onPause();
        if (registered) { unregisterReceiver(tick); registered = false; }
    }

    // Swipe up -> Android's own PIN/pattern prompt -> straight to the home screen
    void unlock() {
        km.requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {
            @Override public void onDismissSucceeded() { finish(); }
        });
    }

    void torch() {
        try {
            CameraManager cm = (CameraManager) getSystemService(CAMERA_SERVICE);
            torch = !torch;
            cm.setTorchMode(cm.getCameraIdList()[0], torch);
            view.setTorch(torch);
        } catch (Exception e) { torch = false; }
    }

    void camera() {
        try {
            startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {}
    }

    @Override public void onBackPressed() {}
}
