package com.lite.lock;

import android.content.Context;
import android.graphics.*;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class LockView extends View {
    // ---- tweak the look here ----
    static final float CLOCK_TOP = 0.14f;     // clock top, fraction of screen height
    static final float CLOCK_HEIGHT = 0.27f;  // clock digit height, fraction of screen height
    static final String CLOCK_FONT = "sans-serif-light"; // try sans-serif-thin / sans-serif-medium

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float d;
    private Bitmap bg, fg;
    private boolean loaded, torchOn;
    private float dragY, downX, downY;

    LockView(Context c) { super(c); d = c.getResources().getDisplayMetrics().density; }

    void reload() { loaded = false; invalidate(); }
    void setTorch(boolean on) { torchOn = on; invalidate(); }

    private Bitmap load(String name) {
        File f = new File(getContext().getFilesDir(), name);
        if (!f.exists()) return null;
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getPath(), o);
        int s = 1;
        while (o.outWidth / (s * 2) >= getWidth()) s *= 2;
        o = new BitmapFactory.Options();
        o.inSampleSize = s;
        o.inPreferredConfig = name.endsWith("png") ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(f.getPath(), o);
    }

    private void fill(Canvas c, Bitmap b, int w, int h) {
        float sc = Math.max((float) w / b.getWidth(), (float) h / b.getHeight());
        int sw = (int) (w / sc), sh = (int) (h / sc);
        int sx = (b.getWidth() - sw) / 2, sy = (b.getHeight() - sh) / 2;
        c.drawBitmap(b, new Rect(sx, sy, sx + sw, sy + sh), new Rect(0, 0, w, h), null);
    }

    @Override protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        if (!loaded) { bg = load("bg.jpg"); fg = load("fg.png"); loaded = true; }
        c.save();
        c.translate(0, -dragY);

        // 1) wallpaper
        if (bg != null) fill(c, bg, w, h);
        else {
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0, 0, w, h, new int[]{0xFFD8C7A6, 0xFF8C7A66, 0xFF3A3548}, null, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, p);
            p.setShader(null);
        }

        // 2) date + clock
        String t = new SimpleDateFormat(DateFormat.is24HourFormat(getContext()) ? "H:mm" : "h:mm", Locale.getDefault()).format(new Date());
        String date = new SimpleDateFormat("EEE MMM d", Locale.getDefault()).format(new Date());
        float top = h * CLOCK_TOP, ch = h * CLOCK_HEIGHT;

        p.setTextAlign(Paint.Align.CENTER);
        p.setTextScaleX(1f);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xE6FFFFFF);
        p.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        p.setTextSize(17 * d);
        c.drawText(date, w / 2f, top - 16 * d, p);

        p.setTypeface(Typeface.create(CLOCK_FONT, Typeface.NORMAL));
        p.setTextSize(ch / 0.71f);                       // digit height ~ 0.71 em
        p.setTextScaleX(Math.min(1f, w * 0.9f / p.measureText(t))); // squeeze to a tall, narrow look
        p.setShader(new LinearGradient(0, top, 0, top + ch,
            new int[]{0xF2FFEBC8, 0xD9F4F1EC, 0xA6C9C9D6}, null, Shader.TileMode.CLAMP));
        c.drawText(t, w / 2f, top + ch, p);
        p.setShader(null);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.2f * d);
        p.setColor(0x70FFFFFF);
        c.drawText(t, w / 2f, top + ch, p);
        p.setStyle(Paint.Style.FILL);
        p.setTextScaleX(1f);

        // 3) foreground cutout drawn OVER the clock = the "clock behind wallpaper" depth effect
        if (fg != null) fill(c, fg, w, h);
        else if (bg == null) {
            Path q = new Path();
            q.moveTo(0, h * 0.42f);
            q.cubicTo(w * 0.35f, h * 0.30f, w * 0.7f, h * 0.50f, w, h * 0.34f);
            q.lineTo(w, h); q.lineTo(0, h); q.close();
            p.setShader(new LinearGradient(0, h * 0.3f, w, h, 0xFFB9A88F, 0xFF2F2B3D, Shader.TileMode.CLAMP));
            c.drawPath(q, p);
            p.setShader(null);
        }

        // 4) buttons + home indicator
        button(c, 56 * d, h - 92 * d, true);
        button(c, w - 56 * d, h - 92 * d, false);
        p.setColor(0xCCFFFFFF);
        c.drawRoundRect(new RectF(w / 2f - 67 * d, h - 18 * d, w / 2f + 67 * d, h - 13 * d), 3 * d, 3 * d, p);
        c.restore();
    }

    private void button(Canvas c, float cx, float cy, boolean flash) {
        boolean on = flash && torchOn;
        p.setStyle(Paint.Style.FILL);
        p.setColor(on ? 0xE6FFFFFF : 0x40FFFFFF);
        c.drawCircle(cx, cy, 26 * d, p);
        p.setColor(on ? 0xFF222222 : 0xFFFFFFFF);
        if (flash) {
            c.drawRoundRect(new RectF(cx - 6 * d, cy - 13 * d, cx + 6 * d, cy - 4 * d), 2 * d, 2 * d, p);
            c.drawRoundRect(new RectF(cx - 4 * d, cy - 3 * d, cx + 4 * d, cy + 13 * d), 2 * d, 2 * d, p);
        } else {
            c.drawRoundRect(new RectF(cx - 12 * d, cy - 8 * d, cx + 12 * d, cy + 10 * d), 4 * d, 4 * d, p);
            p.setColor(0x99000000);
            c.drawCircle(cx, cy + d, 5 * d, p);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        LockActivity a = (LockActivity) getContext();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN: downX = e.getX(); downY = e.getY(); return true;
            case MotionEvent.ACTION_MOVE: dragY = Math.max(0, downY - e.getY()); invalidate(); return true;
            case MotionEvent.ACTION_UP:
                float dy = downY - e.getY();
                dragY = 0; invalidate();
                if (dy > getHeight() * 0.12f) a.unlock();
                else if (Math.abs(dy) < 12 * d && Math.abs(e.getX() - downX) < 12 * d) {
                    float cy = getHeight() - 92 * d, r = 34 * d;
                    if (Math.hypot(e.getX() - 56 * d, e.getY() - cy) < r) a.torch();
                    else if (Math.hypot(e.getX() - (getWidth() - 56 * d), e.getY() - cy) < r) a.camera();
                }
                return true;
            case MotionEvent.ACTION_CANCEL: dragY = 0; invalidate(); return true;
        }
        return true;
    }
}
