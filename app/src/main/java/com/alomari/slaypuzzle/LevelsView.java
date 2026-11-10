package com.alomari.slaypuzzle;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

/**
 * شاشة اختيار المستويات: شبكة خشبية قابلة للتمرير مع نجوم وأقفال
 * برمجة وتطوير: محمد العمري  730215825
 */
public class LevelsView extends View {
    final LevelsActivity act;
    final Sfx sfx;
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    final RectF r = new RectF();
    final RectF back = new RectF();
    int cols = 4, unlocked;
    float W, H, tile, gap, margin, top, scrollY, maxScroll, vel, t, downY, lastY, downX;
    boolean dragging, backDown;
    long last;
    VelocityTracker vt;

    public LevelsView(LevelsActivity a) {
        super(a);
        act = a;
        Ui.init(a);
        sfx = Sfx.get(a);
        unlocked = Progress.unlocked(a);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        W = w;
        H = h;
        margin = Ui.dp(20);
        gap = Ui.dp(14);
        tile = (W - margin * 2 - gap * (cols - 1)) / cols;
        top = Ui.dp(150);
        int rows = (LevelGen.TOTAL + cols - 1) / cols;
        float content = top + rows * (tile + gap) + Ui.dp(100);
        maxScroll = Math.max(0, content - H);
        float cx = Ui.dp(42), cy = Ui.dp(24) + Ui.dp(32);
        back.set(cx - Ui.dp(26), cy - Ui.dp(26), cx + Ui.dp(26), cy + Ui.dp(26));
        // ابدأ التمرير قرب المستوى الحالي
        int row = (Math.min(unlocked, LevelGen.TOTAL) - 1) / cols;
        scrollY = Math.max(0, Math.min(maxScroll, top + row * (tile + gap) - H * 0.4f));
    }

    RectF tileRect(int i) {
        int row = i / cols, col = i % cols;
        float x = margin + col * (tile + gap);
        float y = top + row * (tile + gap) - scrollY;
        r.set(x, y, x + tile, y + tile);
        return r;
    }

    @Override
    protected void onDraw(Canvas c) {
        long now = System.nanoTime();
        float dt = last == 0 ? 0.016f : Math.min(0.033f, (now - last) / 1e9f);
        last = now;
        t += dt;
        unlocked = Progress.unlocked(act);
        if (!dragging && Math.abs(vel) > 5f) {
            scrollY = Math.max(0, Math.min(maxScroll, scrollY - vel * dt));
            vel *= (float) Math.pow(0.04, dt);
        }
        Ui.drawBg(c, (int) W, (int) H);
        p.setStyle(Paint.Style.FILL);
        for (int i = 0; i < LevelGen.TOTAL; i++) {
            RectF q = tileRect(i);
            if (q.bottom < Ui.dp(100) || q.top > H) continue;
            int n = i + 1;
            float appear = Ui.easeBack(Math.max(0f, Math.min(1f, (t - 0.04f * (i / cols)) / 0.4f)));
            if (appear <= 0f) continue;
            c.save();
            c.scale(appear, appear, q.centerX(), q.centerY());
            drawTile(c, q, n);
            c.restore();
        }
        // الشريط العلوي
        p.setShader(null);
        p.setColor(0xEE0A2461);
        c.drawRect(0, 0, W, Ui.dp(112), p);
        p.setColor(0x55000000);
        c.drawRect(0, Ui.dp(112), W, Ui.dp(116), p);
        Ui.outlineText(c, "Levels", W / 2f, Ui.dp(72), Ui.dp(42), 0xFFFFFFFF, 0xFF7A4020, Ui.dp(8));
        Ui.roundBtn(c, back.centerX(), back.centerY(), back.width() / 2f, backDown, Ui.I_BACK);
        // الشارة في الأسفل
        float cy = Ui.dp(150) + ((LevelGen.TOTAL + cols - 1) / cols) * (tile + gap) - scrollY + Ui.dp(30);
        Ui.credit(c, W / 2f, cy);
        postInvalidateOnAnimation();
    }

    void drawTile(Canvas c, RectF q, int n) {
        boolean locked = n > unlocked;
        int stars = Progress.stars(act, n);
        float rad = tile * 0.22f;
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x55000000);
        c.drawRoundRect(q.left, q.top + Ui.dp(5), q.right, q.bottom + Ui.dp(5), rad, rad, p);
        c.drawRoundRect(q, rad, rad, Ui.wallPaint);
        float in = Ui.dp(5);
        RectF inner = new RectF(q.left + in, q.top + in, q.right - in, q.bottom - in);
        if (locked) {
            p.setColor(0x55000000);
            c.drawRoundRect(inner, rad * 0.7f, rad * 0.7f, p);
            Ui.icon(c, Ui.I_LOCK, q.centerX(), q.centerY(), tile * 0.2f, 0xFFD9B98A);
            return;
        }
        if (stars > 0) {
            p.setColor(Ui.ORANGE);
            c.drawRoundRect(inner, rad * 0.7f, rad * 0.7f, p);
            p.setColor(0x33FFFFFF);
            c.drawRoundRect(inner.left + Ui.dp(3), inner.top + Ui.dp(3), inner.right - Ui.dp(3), inner.top + inner.height() * 0.42f, rad * 0.5f, rad * 0.5f, p);
            Ui.outlineText(c, String.valueOf(n), q.centerX(), q.centerY() + tile * 0.04f, tile * 0.36f, 0xFFFFFFFF, 0xFF8A4A12, Ui.dp(4));
            for (int i = 0; i < 3; i++)
                Ui.star(c, q.centerX() + (i - 1) * tile * 0.22f, q.bottom - tile * 0.17f - (i == 1 ? Ui.dp(2) : 0), tile * 0.09f, i < stars);
        } else {
            c.drawRoundRect(inner, rad * 0.7f, rad * 0.7f, Ui.floorPaint);
            Ui.text(c, String.valueOf(n), q.centerX(), q.centerY() + tile * 0.13f, tile * 0.36f, Ui.BROWN);
            if (n == unlocked) {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(Ui.dp(3));
                p.setColor(Ui.ORANGE);
                p.setAlpha((int) (140 + 115 * Math.sin(t * 4)));
                c.drawRoundRect(q.left - Ui.dp(3), q.top - Ui.dp(3), q.right + Ui.dp(3), q.bottom + Ui.dp(3), rad, rad, p);
                p.setStyle(Paint.Style.FILL);
                p.setAlpha(255);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX(), y = e.getY();
        if (vt == null) vt = VelocityTracker.obtain();
        vt.addMovement(e);
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                lastY = y;
                dragging = false;
                vel = 0;
                backDown = back.contains(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!dragging && Math.abs(y - downY) > Ui.dp(10)) {
                    dragging = true;
                    backDown = false;
                }
                if (dragging) {
                    scrollY = Math.max(0, Math.min(maxScroll, scrollY - (y - lastY)));
                    lastY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
                vt.computeCurrentVelocity(1000);
                if (dragging) {
                    vel = vt.getYVelocity();
                } else if (backDown && back.contains(x, y)) {
                    sfx.click();
                    act.finish();
                } else if (y > Ui.dp(116)) {
                    for (int i = 0; i < LevelGen.TOTAL; i++) {
                        if (tileRect(i).contains(x, y)) {
                            if (i + 1 <= unlocked) {
                                sfx.click();
                                Intent it = new Intent(act, GameActivity.class);
                                it.putExtra("level", i + 1);
                                act.startActivity(it);
                            } else {
                                sfx.block();
                            }
                            break;
                        }
                    }
                }
                dragging = false;
                backDown = false;
                vt.recycle();
                vt = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                backDown = false;
                if (vt != null) { vt.recycle(); vt = null; }
                break;
            default:
                break;
        }
        return true;
    }
}
