package com.alomari.slaypuzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

/**
 * واجهة اللعب: سحب الكرة بالإصبع، تراجع، إعادة، شاشة الفوز بالنجوم
 * برمجة وتطوير: محمد العمري  730215825
 */
public class GameView extends View implements Board.Listener {
    static final int B_BACK = 0, B_RESTART = 1, B_UNDO = 2, B_NEXT = 3, B_HOME = 4, B_REPLAY = 5;

    final GameActivity act;
    final Board board;
    final Sfx sfx;
    final Particles conf = new Particles();
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    final RectF[] br = new RectF[6];
    final RectF panel = new RectF();
    final RectF bar = new RectF();
    final Handler handler = new Handler(Looper.getMainLooper());

    int level, stars, starShown, down = -1;
    boolean won, swiped;
    float winT, W, H, topPad, downX, downY;
    long last;

    public GameView(GameActivity a, int lvl) {
        super(a);
        act = a;
        Ui.init(a);
        sfx = Sfx.get(a);
        board = new Board(this);
        for (int i = 0; i < 6; i++) br[i] = new RectF();
        load(lvl);
    }

    void load(int n) {
        level = n;
        board.load(LevelGen.make(n));
        won = false;
        winT = 0;
        stars = 0;
        starShown = 0;
        conf.list.clear();
        if (W > 0) relayout();
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        W = w;
        H = h;
        topPad = Ui.dp(24);
        relayout();
    }

    void setRound(int id, float cx, float cy, float r) {
        br[id].set(cx - r, cy - r, cx + r, cy + r);
    }

    void relayout() {
        board.layout(Ui.dp(14), topPad + Ui.dp(104), W - Ui.dp(14), H - Ui.dp(146));
        setRound(B_BACK, Ui.dp(42), topPad + Ui.dp(32), Ui.dp(26));
        setRound(B_RESTART, W / 2f - Ui.dp(64), H - Ui.dp(86), Ui.dp(30));
        setRound(B_UNDO, W / 2f + Ui.dp(64), H - Ui.dp(86), Ui.dp(30));
        float pw = Math.min(W * 0.86f, Ui.dp(360)), ph = Ui.dp(330);
        panel.set(W / 2f - pw / 2f, H / 2f - ph / 2f, W / 2f + pw / 2f, H / 2f + ph / 2f);
        float bw = pw * 0.72f;
        br[B_NEXT].set(W / 2f - bw / 2f, panel.top + Ui.dp(188), W / 2f + bw / 2f, panel.top + Ui.dp(188) + Ui.dp(64));
        setRound(B_HOME, W / 2f - Ui.dp(56), panel.top + Ui.dp(284), Ui.dp(28));
        setRound(B_REPLAY, W / 2f + Ui.dp(56), panel.top + Ui.dp(284), Ui.dp(28));
        bar.set(W * 0.2f, topPad + Ui.dp(64), W * 0.8f, topPad + Ui.dp(78));
    }

    // ---------- Board.Listener ----------
    @Override public void onStart() { sfx.swoosh(); }
    @Override public void onPaint(int combo) { sfx.pop(combo); }
    @Override public void onArrive() {
        sfx.thud();
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }
    @Override public void onBlocked() { sfx.block(); }
    @Override public void onWin() {
        won = true;
        winT = 0;
        starShown = 0;
        int par = board.lv.par;
        int m = board.moves;
        stars = m <= par ? 3 : (m <= par + Math.max(2, par / 2) ? 2 : 1);
        Progress.complete(act, level, stars);
        sfx.winDelayed(150);
        conf.confetti(W, H, 90);
    }

    // ---------- الرسم ----------
    @Override
    protected void onDraw(Canvas c) {
        long now = System.nanoTime();
        float dt = last == 0 ? 0.016f : Math.min(0.033f, (now - last) / 1e9f);
        last = now;
        board.update(dt);
        conf.update(dt);
        if (won) {
            winT += dt;
            for (int i = 0; i < 3; i++) {
                if (i < stars && i == starShown && winT > 0.95f + 0.28f * i) {
                    sfx.pop(2 + i * 2, 0.8f);
                    starShown++;
                }
            }
        }
        Ui.drawBg(c, (int) W, (int) H);
        board.draw(c);
        drawHud(c);
        if (won) drawWin(c);
        conf.draw(c);
        postInvalidateOnAnimation();
    }

    void drawHud(Canvas c) {
        Ui.outlineText(c, "Level " + level, W / 2f, topPad + Ui.dp(46), Ui.dp(38), 0xFFFFFFFF, 0xFF7A4020, Ui.dp(7));
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x66000000);
        c.drawRoundRect(bar, Ui.dp(7), Ui.dp(7), p);
        float ratio = board.lv == null ? 0 : board.paintedCount / (float) board.lv.floor;
        p.setColor(Ui.ORANGE);
        c.drawRoundRect(bar.left, bar.top, bar.left + bar.width() * ratio, bar.bottom, Ui.dp(7), Ui.dp(7), p);
        Ui.text(c, "Moves " + board.moves + "   •   Par " + board.lv.par, W / 2f, bar.bottom + Ui.dp(22), Ui.dp(13), 0xFFDCE8FF);
        Ui.roundBtn(c, br[B_BACK].centerX(), br[B_BACK].centerY(), br[B_BACK].width() / 2f, down == B_BACK, Ui.I_BACK);
        Ui.roundBtn(c, br[B_RESTART].centerX(), br[B_RESTART].centerY(), br[B_RESTART].width() / 2f, down == B_RESTART, Ui.I_RESTART);
        Ui.roundBtn(c, br[B_UNDO].centerX(), br[B_UNDO].centerY(), br[B_UNDO].width() / 2f, down == B_UNDO, Ui.I_UNDO);
        Ui.credit(c, W / 2f, H - Ui.dp(36));
    }

    void drawWin(Canvas c) {
        float t = winT - 0.55f;
        if (t <= 0) return;
        float a = Math.min(1f, t / 0.3f);
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb((int) (150 * a), 0, 8, 30));
        c.drawRect(0, 0, W, H, p);
        float s = Ui.easeBack(Math.min(1f, t / 0.45f));
        c.save();
        c.scale(s, s, panel.centerX(), panel.centerY());
        Ui.panel(c, panel);
        Ui.outlineText(c, "Level Complete!", panel.centerX(), panel.top + Ui.dp(66), Ui.dp(30), 0xFFFFFFFF, 0xFFD2700C, Ui.dp(6));
        for (int i = 0; i < 3; i++) {
            float st = Ui.easeBack(Math.max(0f, Math.min(1f, (winT - (0.95f + 0.28f * i)) / 0.3f)));
            float cx = panel.centerX() + (i - 1) * Ui.dp(64);
            float cy = panel.top + Ui.dp(124) - (i == 1 ? Ui.dp(10) : 0f);
            Ui.star(c, cx, cy, Ui.dp(28), false);
            if (i < stars && st > 0f) Ui.star(c, cx, cy, Ui.dp(28) * st, true);
        }
        Ui.bigButton(c, br[B_NEXT], level < LevelGen.TOTAL ? "NEXT" : "LEVELS", down == B_NEXT, true, Ui.dp(28));
        Ui.roundBtn(c, br[B_HOME].centerX(), br[B_HOME].centerY(), br[B_HOME].width() / 2f, down == B_HOME, Ui.I_HOME);
        Ui.roundBtn(c, br[B_REPLAY].centerX(), br[B_REPLAY].centerY(), br[B_REPLAY].width() / 2f, down == B_REPLAY, Ui.I_RESTART);
        c.restore();
    }

    // ---------- اللمس ----------
    int hit(float x, float y) {
        float slop = Ui.dp(8);
        if (won) {
            if (winT < 0.9f) return -1;
            int[] ids = {B_NEXT, B_HOME, B_REPLAY};
            for (int id : ids)
                if (x >= br[id].left - slop && x <= br[id].right + slop && y >= br[id].top - slop && y <= br[id].bottom + slop) return id;
            return -1;
        }
        int[] ids = {B_BACK, B_RESTART, B_UNDO};
        for (int id : ids)
            if (x >= br[id].left - slop && x <= br[id].right + slop && y >= br[id].top - slop && y <= br[id].bottom + slop) return id;
        return -1;
    }

    void click(int id) {
        sfx.click();
        switch (id) {
            case B_BACK:
            case B_HOME:
                act.finish();
                break;
            case B_RESTART:
            case B_REPLAY:
                load(level);
                break;
            case B_UNDO:
                board.undo();
                break;
            case B_NEXT:
                if (level < LevelGen.TOTAL) load(level + 1); else act.finish();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX(), y = e.getY();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                swiped = false;
                down = hit(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (down < 0 && !swiped && !won) {
                    float dx = x - downX, dy = y - downY;
                    float thr = Ui.dp(22);
                    if (Math.max(Math.abs(dx), Math.abs(dy)) > thr) {
                        int d = Math.abs(dx) > Math.abs(dy) ? (dx > 0 ? 3 : 2) : (dy > 0 ? 1 : 0);
                        swiped = true;
                        board.swipe(d);
                    }
                } else if (down >= 0 && hit(x, y) != down) {
                    down = -1;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (down >= 0 && hit(x, y) == down) {
                    int id = down;
                    down = -1;
                    click(id);
                }
                down = -1;
                break;
            case MotionEvent.ACTION_CANCEL:
                down = -1;
                break;
            default:
                break;
        }
        return true;
    }
}
