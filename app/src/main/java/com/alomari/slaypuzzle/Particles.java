package com.alomari.slaypuzzle;

import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.Random;

/** نظام الجسيمات (رذاذ الطلاء والقصاصات) - برمجة وتطوير: محمد العمري */
public class Particles {
    public static class P {
        float x, y, vx, vy, life, max, size, rot, vr, g, drag;
        int color;
        boolean rect;
    }

    public final ArrayList<P> list = new ArrayList<>();
    final Random r = new Random();
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static final int[] COLORS = {0xFFEE8A14, 0xFFFFB347, 0xFFF6D9A8, 0xFFFFE9C0, 0xFFD9731A};
    static final int[] CONF = {0xFFFF5A5F, 0xFFFFC21A, 0xFF3DDC84, 0xFF4DA3FF, 0xFFFF8A00, 0xFFB16CFF, 0xFFFFFFFF};

    public void add(float x, float y, float vx, float vy, float size, float life, int color, float g, float drag, boolean rect) {
        if (list.size() > 600) return;
        P q = new P();
        q.x = x; q.y = y; q.vx = vx; q.vy = vy; q.size = size;
        q.life = life; q.max = life; q.color = color; q.g = g; q.drag = drag; q.rect = rect;
        q.rot = r.nextFloat() * 360f;
        q.vr = (r.nextFloat() - 0.5f) * 600f;
        list.add(q);
    }

    public void burst(float x, float y, int n, float speed, float size) {
        for (int i = 0; i < n; i++) {
            double a = r.nextDouble() * Math.PI * 2;
            float s = speed * (0.3f + r.nextFloat());
            add(x, y, (float) Math.cos(a) * s, (float) Math.sin(a) * s,
                    size * (0.5f + r.nextFloat()), 0.4f + r.nextFloat() * 0.4f,
                    COLORS[r.nextInt(COLORS.length)], 0f, 3f, false);
        }
    }

    public void confetti(float w, float h, int n) {
        for (int i = 0; i < n; i++) {
            add(r.nextFloat() * w, -Ui.dp(20) - r.nextFloat() * h * 0.35f,
                    (r.nextFloat() - 0.5f) * Ui.dp(140), Ui.dp(60) + r.nextFloat() * Ui.dp(220),
                    Ui.dp(4) + r.nextFloat() * Ui.dp(5), 3f + r.nextFloat() * 1.5f,
                    CONF[r.nextInt(CONF.length)], Ui.dp(240), 0.4f, true);
        }
    }

    public void update(float dt) {
        for (int i = list.size() - 1; i >= 0; i--) {
            P q = list.get(i);
            q.life -= dt;
            if (q.life <= 0) {
                list.remove(i);
                continue;
            }
            q.vy += q.g * dt;
            float k = Math.max(0f, 1f - q.drag * dt);
            q.vx *= k;
            if (!q.rect) q.vy *= k;
            q.x += q.vx * dt;
            q.y += q.vy * dt;
            q.rot += q.vr * dt;
        }
    }

    public void draw(Canvas c) {
        p.setStyle(Paint.Style.FILL);
        for (int i = 0; i < list.size(); i++) {
            P q = list.get(i);
            p.setColor(q.color);
            p.setAlpha((int) (255 * Math.min(1f, q.life / q.max * 2f)));
            if (q.rect) {
                c.save();
                c.translate(q.x, q.y);
                c.rotate(q.rot);
                c.drawRect(-q.size, -q.size * 0.5f, q.size, q.size * 0.5f, p);
                c.restore();
            } else {
                c.drawCircle(q.x, q.y, q.size, p);
            }
        }
    }
}
