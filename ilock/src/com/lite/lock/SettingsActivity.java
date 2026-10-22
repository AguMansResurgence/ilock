package com.lite.lock;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.*;
import java.io.*;

public class SettingsActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        l.setPadding(pad, pad, pad, pad);
        add(l, "Start lockscreen", new Runnable() { public void run() { startForegroundService(new Intent(SettingsActivity.this, LockService.class)); } });
        add(l, "Stop lockscreen", new Runnable() { public void run() { stopService(new Intent(SettingsActivity.this, LockService.class)); } });
        add(l, "Choose wallpaper", new Runnable() { public void run() { pick(1); } });
        add(l, "Choose cutout PNG (optional)", new Runnable() { public void run() { pick(2); } });
        add(l, "Reset to default look", new Runnable() { public void run() {
            new File(getFilesDir(), "bg.jpg").delete(); new File(getFilesDir(), "fg.png").delete(); } });
        add(l, "Allow background running", new Runnable() { public void run() {
            startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName()))); } });
        TextView t = new TextView(this);
        t.setPadding(0, pad, 0, 0);
        t.setText("Also turn on Auto-start for iLock in the phone's app settings, so ColorOS does not kill it.");
        l.addView(t);
        setContentView(l);
    }

    private void add(LinearLayout l, String s, final Runnable r) {
        Button b = new Button(this);
        b.setText(s);
        b.setOnClickListener(new android.view.View.OnClickListener() { public void onClick(android.view.View v) { r.run(); } });
        l.addView(b);
    }

    private void pick(int code) {
        startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), code);
    }

    @Override protected void onActivityResult(int req, int res, Intent d) {
        if (res != RESULT_OK || d == null || d.getData() == null) return;
        try (InputStream in = getContentResolver().openInputStream(d.getData());
             OutputStream out = new FileOutputStream(new File(getFilesDir(), req == 1 ? "bg.jpg" : "fg.png"))) {
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
        }
    }
}
