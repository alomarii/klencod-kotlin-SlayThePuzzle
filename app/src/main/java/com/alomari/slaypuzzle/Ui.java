package com.alomari.slaypuzzle;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;
import java.util.Random;

/**
 * أدوات الرسم المشتركة: خشب، أزرار، نصوص، أيقونات
 * برمجة وتطوير: محمد العمري  730215825
 */
public class Ui {
    public static float D = 1f;
    public static Bitmap floorTex, wallTex;
    public static final Paint floorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static final Paint wallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    static final Paint bgPaint = new Paint();
    static final Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
    static final Paint fp = new Paint(Paint.ANTI_ALIAS_FLAG);
    static final Paint ip = new Paint(Paint.ANTI_ALIAS_FLAG);
    static final Paint cp = new Paint(Paint.ANTI_ALIAS_FLAG);
    static final Path path = new Path();
    static final RectF tmp = new RectF();
    static int bgW, bgH;

    public static final Typeface TITLE = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC);
    public static final Typeface BOLD = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    public static final int ORANGE = 0xFFEE8A14, ORANGE_D = 0xFFC2640A, ORANGE_L = 0xFFFFB347;
    public static final int BROWN = 0xFF5A3217, CREAM = 0xFFFFF3DA;

    public static final int I_BACK = 0, I_RESTART = 1, I_UNDO = 2, I_HOME = 3, I_SOUND = 4, I_MUTE = 5, I_LOCK = 6;

    public static float dp(float v) { return v * D; }

    public static void init(Context c) {
        D = c.getResources().getDisplayMetrics().density;
        if (floorTex == null) {
            floorTex = makeWood(0xFFF3E3C0, 0xFFB98C4E, 11, 2);
            wallTex = makeWood(0xFF6B4229, 0xFF24120A, 23, 3);
            floorPaint.setShader(new BitmapShader(floorTex, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
            wallPaint.setShader(new BitmapShader(wallTex, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        }
    }

    public static void immersive(Activity a) {
        a.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    static Bitmap makeWood(int base, int grain, long seed, int knots) {
        int S = 256;
        Bitmap b = Bitmap.createBitmap(S, S, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawColor(base);
        Random r = new Random(seed);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        Path pa = new Path();
        for (int i = 0; i < 95; i++) {
            float y0 = r.nextFloat() * S;
            float amp = 2 + r.nextFloat() * 7;
            int k = 1 + r.nextInt(3);
            float ph = r.nextFloat() * 6.28f;
            p.setStrokeWidth(0.6f + r.nextFloat() * 2.2f);
            boolean light = i % 4 == 0;
            int a = 18 + r.nextInt(48);
            p.setColor(light ? 0x26FFFFFF : ((grain & 0x00FFFFFF) | (a << 24)));
            for (int off = -S; off <= S; off += S) {
                pa.reset();
                for (int x = 0; x <= S; x += 8) {
                    float y = y0 + off + amp * (float) Math.sin(2 * Math.PI * k * x / S + ph);
                    if (x == 0) pa.moveTo(x, y); else pa.lineTo(x, y);
                }
                c.drawPath(pa, p);
            }
        }
        for (int k = 0; k < knots; k++) {
            float kx = r.nextFloat() * S, ky = r.nextFloat() * S;
            for (int j = 1; j <= 4; j++) {
                p.setColor((grain & 0x00FFFFFF) | (0x30 << 24));
                p.setStrokeWidth(1.2f);
                c.drawOval(new RectF(kx - j * 9, ky - j * 4, kx + j * 9, ky + j * 4), p);
            }
        }
        return b;
    }

    public static float easeBack(float t) {
        float c1 = 1.70158f, c3 = c1 + 1f;
        float u = t - 1f;
        return 1f + c3 * u * u * u + c1 * u * u;
    }

    public static void drawBg(Canvas c, int w, int h) {
        if (bgW != w || bgH != h) {
            bgPaint.setShader(new RadialGradient(w / 2f, h * 0.42f, Math.max(w, h) * 0.75f,
                    new int[]{0xFF1C55B8, 0xFF123A8C, 0xFF071A47}, new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
            bgW = w;
            bgH = h;
        }
        c.drawRect(0, 0, w, h, bgPaint);
    }

    public static float textW(String s, float size) {
        tp.setTypeface(TITLE);
        tp.setTextSize(size);
        return tp.measureText(s);
    }

    public static void outlineText(Canvas c, String s, float cx, float baseY, float size, int fill, int stroke, float sw) {
        tp.setShader(null);
        tp.setTypeface(TITLE);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setTextSize(size);
        tp.setStrokeJoin(Paint.Join.ROUND);
        tp.setStyle(Paint.Style.STROKE);
        tp.setStrokeWidth(sw);
        tp.setColor(0x66000000);
        c.drawText(s, cx, baseY + sw * 0.7f, tp);
        tp.setColor(stroke);
        c.drawText(s, cx, baseY, tp);
        tp.setStyle(Paint.Style.FILL);
        tp.setColor(fill);
        c.drawText(s, cx, baseY, tp);
    }

    public static void text(Canvas c, String s, float cx, float baseY, float size, int color) {
        tp.setShader(null);
        tp.setTypeface(BOLD);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setTextSize(size);
        tp.setStyle(Paint.Style.FILL);
        tp.setColor(color);
        c.drawText(s, cx, baseY, tp);
    }

    public static void credit(Canvas c, float cx, float baseY) {
        cp.setTypeface(BOLD);
        cp.setTextAlign(Paint.Align.CENTER);
        cp.setStyle(Paint.Style.FILL);
        cp.setTextSize(dp(14));
        cp.setColor(0xFFFFE9C0);
        c.drawText("برمجة وتطوير محمد العمري", cx, baseY, cp);
        cp.setTextSize(dp(12));
        cp.setColor(0xFFBFD4FF);
        c.drawText("730215825", cx, baseY + dp(17), cp);
    }

    public static void bigButton(Canvas c, RectF r, String label, boolean pressed, boolean primary, float size) {
        c.save();
        if (pressed) c.scale(0.94f, 0.94f, r.centerX(), r.centerY());
        float rad = r.height() * 0.32f;
        fp.setShader(null);
        fp.setStyle(Paint.Style.FILL);
        fp.setColor(0x55000000);
        c.drawRoundRect(r.left, r.top + dp(6), r.right, r.bottom + dp(6), rad, rad, fp);
        c.drawRoundRect(r, rad, rad, wallPaint);
        float in = dp(5);
        tmp.set(r.left + in, r.top + in, r.right - in, r.bottom - in);
        if (primary) {
            fp.setColor(ORANGE);
            c.drawRoundRect(tmp, rad - in * 0.6f, rad - in * 0.6f, fp);
            fp.setColor(0x33FFFFFF);
            RectF hi = new RectF(tmp.left + dp(3), tmp.top + dp(3), tmp.right - dp(3), tmp.top + tmp.height() * 0.45f);
            c.drawRoundRect(hi, rad * 0.6f, rad * 0.6f, fp);
            fp.setColor(0x22000000);
            c.drawRect(tmp.left + rad * 0.4f, tmp.bottom - dp(6), tmp.right - rad * 0.4f, tmp.bottom - dp(2), fp);
            outlineText(c, label, r.centerX(), r.centerY() + size * 0.35f, size, 0xFFFFFFFF, 0xFF8A4A12, dp(5));
        } else {
            c.drawRoundRect(tmp, rad - in * 0.6f, rad - in * 0.6f, floorPaint);
            text(c, label, r.centerX(), r.centerY() + size * 0.35f, size, BROWN);
        }
        c.restore();
    }

    public static void roundBtn(Canvas c, float cx, float cy, float r, boolean pressed, int icon) {
        c.save();
        if (pressed) c.scale(0.92f, 0.92f, cx, cy);
        fp.setShader(null);
        fp.setStyle(Paint.Style.FILL);
        fp.setColor(0x55000000);
        c.drawCircle(cx, cy + dp(4), r, fp);
        c.drawCircle(cx, cy, r, wallPaint);
        c.drawCircle(cx, cy, r - dp(4), floorPaint);
        icon(c, icon, cx, cy, r * 0.5f, BROWN);
        c.restore();
    }

    public static void panel(Canvas c, RectF r) {
        float rad = dp(26);
        fp.setShader(null);
        fp.setStyle(Paint.Style.FILL);
        fp.setColor(0x66000000);
        c.drawRoundRect(r.left, r.top + dp(8), r.right, r.bottom + dp(8), rad, rad, fp);
        c.drawRoundRect(r, rad, rad, wallPaint);
        float in = dp(9);
        tmp.set(r.left + in, r.top + in, r.right - in, r.bottom - in);
        c.drawRoundRect(tmp, rad - dp(6), rad - dp(6), floorPaint);
        fp.setStyle(Paint.Style.STROKE);
        fp.setStrokeWidth(dp(3));
        fp.setColor(0x33000000);
        c.drawRoundRect(tmp, rad - dp(6), rad - dp(6), fp);
        fp.setStyle(Paint.Style.FILL);
    }

    public static void star(Canvas c, float cx, float cy, float R, boolean on) {
        path.reset();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5;
            float rr = (i % 2 == 0) ? R : R * 0.47f;
            float x = cx + (float) Math.cos(a) * rr, y = cy + (float) Math.sin(a) * rr;
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }
        path.close();
        fp.setShader(null);
        fp.setStyle(Paint.Style.FILL);
        fp.setColor(on ? 0xFFFFC21A : 0x44000000);
        c.drawPath(path, fp);
        if (on) {
            fp.setStyle(Paint.Style.STROKE);
            fp.setStrokeWidth(R * 0.16f);
            fp.setStrokeJoin(Paint.Join.ROUND);
            fp.setColor(0xFFB8660A);
            c.drawPath(path, fp);
            fp.setStyle(Paint.Style.FILL);
        }
    }

    static void arcArrow(Canvas c, float cx, float cy, float a, float start, float sweep) {
        tmp.set(cx - a, cy - a, cx + a, cy + a);
        c.drawArc(tmp, start, sweep, false, ip);
        double th = Math.toRadians(start + sweep);
        float px = cx + a * (float) Math.cos(th), py = cy + a * (float) Math.sin(th);
        float sg = sweep > 0 ? 1f : -1f;
        float tx = -sg * (float) Math.sin(th), ty = sg * (float) Math.cos(th);
        float nx = (float) Math.cos(th), ny = (float) Math.sin(th);
        float hl = a * 0.7f, hw = a * 0.5f;
        path.reset();
        path.moveTo(px + tx * hl, py + ty * hl);
        path.lineTo(px + nx * hw, py + ny * hw);
        path.lineTo(px - nx * hw, py - ny * hw);
        path.close();
        ip.setStyle(Paint.Style.FILL);
        c.drawPath(path, ip);
        ip.setStyle(Paint.Style.STROKE);
    }

    public static void icon(Canvas c, int id, float cx, float cy, float s, int col) {
        ip.setShader(null);
        ip.setStyle(Paint.Style.STROKE);
        ip.setStrokeCap(Paint.Cap.ROUND);
        ip.setStrokeJoin(Paint.Join.ROUND);
        ip.setStrokeWidth(s * 0.28f);
        ip.setColor(col);
        switch (id) {
            case I_BACK:
                path.reset();
                path.moveTo(cx + s * 0.3f, cy - s * 0.65f);
                path.lineTo(cx - s * 0.35f, cy);
                path.lineTo(cx + s * 0.3f, cy + s * 0.65f);
                c.drawPath(path, ip);
                break;
            case I_RESTART:
                arcArrow(c, cx, cy, s * 0.62f, -60f, 290f);
                break;
            case I_UNDO:
                arcArrow(c, cx, cy + s * 0.05f, s * 0.62f, -20f, -250f);
                break;
            case I_HOME:
                path.reset();
                path.moveTo(cx - s * 0.8f, cy - s * 0.05f);
                path.lineTo(cx, cy - s * 0.75f);
                path.lineTo(cx + s * 0.8f, cy - s * 0.05f);
                path.moveTo(cx - s * 0.55f, cy - s * 0.1f);
                path.lineTo(cx - s * 0.55f, cy + s * 0.7f);
                path.lineTo(cx + s * 0.55f, cy + s * 0.7f);
                path.lineTo(cx + s * 0.55f, cy - s * 0.1f);
                c.drawPath(path, ip);
                break;
            case I_SOUND:
            case I_MUTE:
                ip.setStyle(Paint.Style.FILL);
                path.reset();
                path.moveTo(cx - s * 0.75f, cy - s * 0.3f);
                path.lineTo(cx - s * 0.4f, cy - s * 0.3f);
                path.lineTo(cx + s * 0.05f, cy - s * 0.7f);
                path.lineTo(cx + s * 0.05f, cy + s * 0.7f);
                path.lineTo(cx - s * 0.4f, cy + s * 0.3f);
                path.lineTo(cx - s * 0.75f, cy + s * 0.3f);
                path.close();
                c.drawPath(path, ip);
                ip.setStyle(Paint.Style.STROKE);
                ip.setStrokeWidth(s * 0.2f);
                if (id == I_SOUND) {
                    tmp.set(cx - s * 0.25f, cy - s * 0.45f, cx + s * 0.35f, cy + s * 0.45f);
                    c.drawArc(tmp, -50, 100, false, ip);
                    tmp.set(cx - s * 0.45f, cy - s * 0.8f, cx + s * 0.75f, cy + s * 0.8f);
                    c.drawArc(tmp, -50, 100, false, ip);
                } else {
                    c.drawLine(cx + s * 0.35f, cy - s * 0.35f, cx + s * 0.9f, cy + s * 0.35f, ip);
                    c.drawLine(cx + s * 0.9f, cy - s * 0.35f, cx + s * 0.35f, cy + s * 0.35f, ip);
                }
                break;
            case I_LOCK:
                ip.setStyle(Paint.Style.FILL);
                tmp.set(cx - s * 0.6f, cy - s * 0.1f, cx + s * 0.6f, cy + s * 0.75f);
                c.drawRoundRect(tmp, s * 0.15f, s * 0.15f, ip);
                ip.setStyle(Paint.Style.STROKE);
                ip.setStrokeWidth(s * 0.18f);
                tmp.set(cx - s * 0.36f, cy - s * 0.78f, cx + s * 0.36f, cy + s * 0.1f);
                c.drawArc(tmp, 180, 180, false, ip);
                c.drawLine(cx - s * 0.36f, cy - s * 0.34f, cx - s * 0.36f, cy - s * 0.05f, ip);
                c.drawLine(cx + s * 0.36f, cy - s * 0.34f, cx + s * 0.36f, cy - s * 0.05f, ip);
                break;
            default:
                break;
        }
    }
}
