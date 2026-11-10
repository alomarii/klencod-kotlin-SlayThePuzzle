package com.alomari.slaypuzzle;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import java.util.Random;

/**
 * الواجهة الرئيسية التفاعلية: عنوان متحرك، لوحة تعرض الحل تلقائياً،
 * فقاعات تتجاوب مع اللمس، وأزرار خشبية.
 * برمجة وتطوير: محمد العمري  730215825
 */
public class MenuView extends View implements Board.Listener {
    static final int B_PLAY = 0, B_LEVELS = 1, B_SOUND = 2;

    final MenuActivity act;
    final Sfx sfx;
    final Board demo;
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    final RectF[] br = {new RectF(), new RectF(), new RectF()};
    final Random rnd = new Random(7);
    final Particles touchParts = new Particles();

    static final int N = 18;
    final float[] bx = new float[N], by = new float[N], bs = new float[N], bv = new float[N], bph = new float[N];
    final float[] ox = new float[N], oy = new float[N];
    final int[] bc = new int[N];

    float W, H, t, titleSquash, touchX = -1000, touchY = -1000, demoTimer = 1f, winWait;
    int down = -1, demoIdx;
    long last;
    LevelGen.Level demoLevel;

    public MenuView(MenuActivity a) {
        super(a);
        act = a;
        Ui.init(a);
        sfx = Sfx.get(a);
        demo = new Board(this);
        demoLevel = LevelGen.make(6);
        demo.load(demoLevel);
        int[] cols = {0x33FFB347, 0x33FFE9C0, 0x2266AAFF, 0x33EE8A14};
        for (int i = 0; i < N; i++) {
            bs[i] = 0.5f + rnd.nextFloat();
            bv[i] = 0.02f + rnd.nextFloat() * 0.05f;
            bph[i] = rnd.nextFloat() * 6.28f;
            bc[i] = cols[rnd.nextInt(cols.length)];
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        W = w;
        H = h;
        for (int i = 0; i < N; i++) {
            bx[i] = rnd.nextFloat() * W;
            by[i] = rnd.nextFloat() * H;
        }
        demo.layout(Ui.dp(40), H * 0.31f, W - Ui.dp(40), H * 0.60f);
        float bw = Math.min(W * 0.66f, Ui.dp(300));
        br[B_PLAY].set(W / 2f - bw / 2f, H * 0.665f, W / 2f + bw / 2f, H * 0.665f + Ui.dp(76));
        br[B_LEVELS].set(W / 2f - bw / 2f, H * 0.665f + Ui.dp(100), W / 2f + bw / 2f, H * 0.665f + Ui.dp(100) + Ui.dp(66));
        float r = Ui.dp(26);
        br[B_SOUND].set(W - Ui.dp(40) - r, Ui.dp(24) + Ui.dp(32) - r, W - Ui.dp(40) + r, Ui.dp(24) + Ui.dp(32) + r);
    }

    // ---------- Board.Listener (العرض التلقائي) ----------
    @Override public void onStart() { sfx.swoosh(); }
    @Override public void onPaint(int combo) { sfx.pop(combo, 0.3f); }
    @Override public void onArrive() { sfx.thud(); demoTimer = 0.5f; }
    @Override public void onBlocked() { }
    @Override public void onWin() { winWait = 0; }

    @Override
    protected void onDraw(Canvas c) {
        long now = System.nanoTime();
        float dt = last == 0 ? 0.016f : Math.min(0.033f, (now - last) / 1e9f);
        last = now;
        t += dt;
        titleSquash = Math.max(0f, titleSquash - dt * 3f);
        update(dt);

        Ui.drawBg(c, (int) W, (int) H);
        drawBlobs(c);
        // العنوان
        float bounce = (float) Math.sin(t * 2f) * Ui.dp(4);
        float sq = 1f + 0.08f * titleSquash * (float) Math.sin(t * 25);
        float s1 = Math.min(Ui.dp(92), (W * 0.62f) / Ui.textW("Slay", 1f));
        float s2 = Math.min(Ui.dp(60), (W * 0.84f) / Ui.textW("the puzzle", 1f));
        c.save();
        c.scale(sq, 1f / sq, W / 2f, H * 0.14f);
        Ui.outlineText(c, "Slay", W / 2f, H * 0.14f + bounce, s1, 0xFFFFFFFF, 0xFF8A4A1C, Ui.dp(12));
        Ui.outlineText(c, "the puzzle", W / 2f, H * 0.14f + s2 * 1.05f - bounce, s2, 0xFFFFFFFF, 0xFF8A4A1C, Ui.dp(10));
        c.restore();
        // اللوحة التجريبية
        demo.draw(c);
        touchParts.draw(c);
        // الأزرار
        int cur = Progress.unlocked(act);
        Ui.bigButton(c, br[B_PLAY], "PLAY", down == B_PLAY, true, Ui.dp(38));
        Ui.text(c, "Level " + cur, W / 2f, br[B_PLAY].bottom + Ui.dp(22), Ui.dp(13), 0xFFDCE8FF);
        Ui.bigButton(c, br[B_LEVELS], "LEVELS", down == B_LEVELS, false, Ui.dp(26));
        Ui.roundBtn(c, br[B_SOUND].centerX(), br[B_SOUND].centerY(), br[B_SOUND].width() / 2f, down == B_SOUND,
                sfx.on ? Ui.I_SOUND : Ui.I_MUTE);
        Ui.credit(c, W / 2f, H - Ui.dp(36));
        postInvalidateOnAnimation();
    }

    void update(float dt) {
        demo.update(dt);
        touchParts.update(dt);
        if (!demo.moving && !demo.won) {
            demoTimer -= dt;
            if (demoTimer <= 0f && demoIdx < demoLevel.sol.length) {
                demoTimer = 99f;
                demo.swipe(demoLevel.sol[demoIdx++]);
            }
        }
        if (demo.won) {
            winWait += dt;
            if (winWait > 1.7f) {
                demo.reset();
                demoIdx = 0;
                demoTimer = 0.8f;
                winWait = 0;
            }
        }
        for (int i = 0; i < N; i++) {
            by[i] -= bv[i] * H * dt;
            if (by[i] < -Ui.dp(80)) {
                by[i] = H + Ui.dp(80);
                bx[i] = rnd.nextFloat() * W;
            }
            float dx = bx[i] + ox[i] - touchX, dy = by[i] + oy[i] - touchY;
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d < Ui.dp(130) && d > 1f) {
                ox[i] += dx / d * Ui.dp(260) * dt;
                oy[i] += dy / d * Ui.dp(260) * dt;
            }
            ox[i] *= (1f - 1.5f * dt);
            oy[i] *= (1f - 1.5f * dt);
        }
    }

    void drawBlobs(Canvas c) {
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        for (int i = 0; i < N; i++) {
            float x = bx[i] + ox[i] + (float) Math.sin(t * 0.8f + bph[i]) * Ui.dp(14);
            float y = by[i] + oy[i];
            p.setColor(bc[i]);
            c.drawCircle(x, y, Ui.dp(14) * bs[i] * 2.2f, p);
        }
    }

    int hit(float x, float y) {
        float slop = Ui.dp(6);
        for (int i = 0; i < 3; i++)
            if (x >= br[i].left - slop && x <= br[i].right + slop && y >= br[i].top - slop && y <= br[i].bottom + slop) return i;
        return -1;
    }

    void splash(float x, float y) {
        touchParts.burst(x, y, 12, Ui.dp(320), Ui.dp(4));
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX(), y = e.getY();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                down = hit(x, y);
                touchX = x;
                touchY = y;
                if (down < 0) {
                    splash(x, y);
                    sfx.splash();
                    if (y < H * 0.28f) titleSquash = 1f;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                touchX = x;
                touchY = y;
                if (down >= 0 && hit(x, y) != down) down = -1;
                if (down < 0 && rnd.nextInt(4) == 0) splash(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touchX = -1000;
                touchY = -1000;
                if (down >= 0 && hit(x, y) == down) {
                    int id = down;
                    down = -1;
                    sfx.click();
                    if (id == B_PLAY) {
                        Intent it = new Intent(act, GameActivity.class);
                        it.putExtra("level", Progress.unlocked(act));
                        act.startActivity(it);
                    } else if (id == B_LEVELS) {
                        act.startActivity(new Intent(act, LevelsActivity.class));
                    } else {
                        sfx.setOn(act, !sfx.on);
                        if (sfx.on) sfx.click();
                    }
                }
                down = -1;
                break;
            case MotionEvent.ACTION_CANCEL:
                touchX = -1000;
                touchY = -1000;
                down = -1;
                break;
            default:
                break;
        }
        return true;
    }
}
