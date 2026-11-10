package com.alomari.slaypuzzle;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import java.util.ArrayList;
import java.util.Random;

/**
 * لوحة اللعب: منطق الانزلاق + الرسم الخشبي + الجسيمات
 * برمجة وتطوير: محمد العمري  730215825
 */
public class Board {
    public interface Listener {
        void onStart();
        void onPaint(int combo);
        void onArrive();
        void onBlocked();
        void onWin();
    }

    static final int[] DX = {0, 0, -1, 1};
    static final int[] DY = {-1, 1, 0, 0};
    static final float VMAX = 26f, ACC = 130f;

    static class Snap {
        int cx, cy, moves, count;
        boolean[][] painted;
    }

    public LevelGen.Level lv;
    public boolean[][] painted;
    float[][] paintT;
    public int paintedCount, moves;
    public boolean moving, won;
    float bx, by;
    int cx, cy, dir, tx, ty, pcx, pcy, combo, pending = -1;
    float v, time, squash, shake;
    float cs, ox, oy, edgeW;
    public final Particles parts = new Particles();
    final ArrayList<Snap> undo = new ArrayList<>();
    final Listener lis;
    boolean[][] frame;
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint orange = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint ball = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint[] edge = new Paint[4];
    final Random rnd = new Random();

    public Board(Listener l) {
        lis = l;
        orange.setColor(Ui.ORANGE);
        for (int i = 0; i < 4; i++) edge[i] = new Paint();
    }

    public boolean isWall(int x, int y) {
        return x < 0 || y < 0 || x >= lv.w || y >= lv.h || lv.wall[x][y];
    }

    public void load(LevelGen.Level l) {
        lv = l;
        frame = new boolean[l.w + 2][l.h + 2];
        for (int gx = -1; gx <= l.w; gx++) {
            for (int gy = -1; gy <= l.h; gy++) {
                if (!isWall(gx, gy)) continue;
                boolean near = false;
                for (int a = -1; a <= 1 && !near; a++)
                    for (int b = -1; b <= 1; b++)
                        if (!isWall(gx + a, gy + b)) { near = true; break; }
                frame[gx + 1][gy + 1] = near;
            }
        }
        reset();
    }

    public void reset() {
        painted = new boolean[lv.w][lv.h];
        paintT = new float[lv.w][lv.h];
        cx = lv.sx;
        cy = lv.sy;
        bx = cx;
        by = cy;
        painted[cx][cy] = true;
        paintT[cx][cy] = -10f;
        paintedCount = 1;
        moves = 0;
        moving = false;
        won = false;
        pending = -1;
        squash = 0;
        undo.clear();
        parts.list.clear();
    }

    public void layout(float l, float t, float r, float b) {
        if (lv == null) return;
        int cols = lv.w + 2, rows = lv.h + 2;
        float aw = r - l, ah = b - t;
        cs = Math.min(Math.min(aw / cols, ah / rows), Ui.dp(76));
        ox = l + (aw - cs * cols) / 2f + cs;
        oy = t + (ah - cs * rows) / 2f + cs;
        edgeW = cs * 0.22f;
        edge[0].setShader(new LinearGradient(0, 0, 0, edgeW, 0x70000000, 0x00000000, Shader.TileMode.CLAMP));
        edge[1].setShader(new LinearGradient(0, 0, 0, -edgeW, 0x70000000, 0x00000000, Shader.TileMode.CLAMP));
        edge[2].setShader(new LinearGradient(0, 0, edgeW, 0, 0x70000000, 0x00000000, Shader.TileMode.CLAMP));
        edge[3].setShader(new LinearGradient(0, 0, -edgeW, 0, 0x70000000, 0x00000000, Shader.TileMode.CLAMP));
        float rr = cs * 0.34f;
        ball.setShader(new RadialGradient(-rr * 0.35f, -rr * 0.4f, rr * 1.5f,
                new int[]{0xFFFBD99A, 0xFFD9964A, 0xFF8A4F1E}, new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
    }

    // ---------- الحركة ----------
    public void swipe(int d) {
        if (won || lv == null) return;
        if (moving) {
            pending = d;
        } else {
            startMove(d);
        }
    }

    boolean startMove(int d) {
        int x = cx, y = cy;
        while (!isWall(x + DX[d], y + DY[d])) {
            x += DX[d];
            y += DY[d];
        }
        if (x == cx && y == cy) {
            shake = Math.max(shake, 0.35f);
            lis.onBlocked();
            return false;
        }
        Snap s = new Snap();
        s.cx = cx; s.cy = cy; s.moves = moves; s.count = paintedCount;
        s.painted = new boolean[lv.w][lv.h];
        for (int i = 0; i < lv.w; i++) System.arraycopy(painted[i], 0, s.painted[i], 0, lv.h);
        undo.add(s);
        moves++;
        moving = true;
        dir = d;
        tx = x;
        ty = y;
        v = 6f;
        combo = 0;
        pcx = cx;
        pcy = cy;
        lis.onStart();
        return true;
    }

    public boolean undo() {
        if (moving || won || undo.isEmpty()) return false;
        Snap s = undo.remove(undo.size() - 1);
        for (int i = 0; i < lv.w; i++) {
            System.arraycopy(s.painted[i], 0, painted[i], 0, lv.h);
            for (int j = 0; j < lv.h; j++) paintT[i][j] = -10f;
        }
        cx = s.cx; cy = s.cy; bx = cx; by = cy;
        moves = s.moves;
        paintedCount = s.count;
        pending = -1;
        return true;
    }

    void paintCell(int x, int y) {
        if (x < 0 || y < 0 || x >= lv.w || y >= lv.h) return;
        if (!painted[x][y]) {
            painted[x][y] = true;
            paintT[x][y] = time;
            paintedCount++;
            combo++;
            lis.onPaint(combo);
        }
    }

    public void update(float dt) {
        if (lv == null) return;
        time += dt;
        squash = Math.max(0f, squash - dt * 4.5f);
        shake = Math.max(0f, shake - dt * 5f);
        if (moving) {
            v = Math.min(VMAX, v + ACC * dt);
            float step = v * dt;
            float rem = Math.abs(tx - bx) + Math.abs(ty - by);
            boolean arrived = false;
            if (step >= rem) {
                bx = tx;
                by = ty;
                arrived = true;
            } else {
                bx += DX[dir] * step;
                by += DY[dir] * step;
            }
            int ncx = Math.round(bx), ncy = Math.round(by);
            while (pcx != ncx || pcy != ncy) {
                pcx += DX[dir];
                pcy += DY[dir];
                paintCell(pcx, pcy);
            }
            // رذاذ الطلاء خلف الكرة
            float px = ox + (bx + 0.5f) * cs, py = oy + (by + 0.5f) * cs;
            for (int i = 0; i < 2; i++) {
                float sideV = (rnd.nextFloat() - 0.5f) * cs * 6f;
                float backV = -(1f + rnd.nextFloat() * 3f) * cs;
                float vx = DX[dir] != 0 ? DX[dir] * backV : sideV;
                float vy = DY[dir] != 0 ? DY[dir] * backV : sideV;
                parts.add(px - DX[dir] * cs * 0.2f, py - DY[dir] * cs * 0.2f, vx, vy,
                        cs * (0.04f + rnd.nextFloat() * 0.07f), 0.35f + rnd.nextFloat() * 0.35f,
                        Particles.COLORS[rnd.nextInt(Particles.COLORS.length)], 0f, 3f, false);
            }
            if (arrived) arrive();
        }
        parts.update(dt);
    }

    void arrive() {
        moving = false;
        cx = tx;
        cy = ty;
        squash = 1f;
        shake = 0.5f;
        float px = ox + (bx + 0.5f) * cs, py = oy + (by + 0.5f) * cs;
        parts.burst(px + DX[dir] * cs * 0.3f, py + DY[dir] * cs * 0.3f, 16, cs * 6f, cs * 0.07f);
        lis.onArrive();
        if (paintedCount >= lv.floor) {
            won = true;
            pending = -1;
            lis.onWin();
        } else if (pending >= 0) {
            int d = pending;
            pending = -1;
            startMove(d);
        }
    }

    // ---------- الرسم ----------
    public void draw(Canvas c) {
        if (lv == null) return;
        c.save();
        if (shake > 0f) {
            c.translate((rnd.nextFloat() - 0.5f) * shake * Ui.dp(5), (rnd.nextFloat() - 0.5f) * shake * Ui.dp(5));
        }
        int W = lv.w, H = lv.h;
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        // ظل اللوحة
        for (int k = 3; k >= 0; k--) {
            p.setColor(0x14000000);
            float e = k * Ui.dp(3);
            for (int gx = -1; gx <= W; gx++)
                for (int gy = -1; gy <= H; gy++)
                    if (frame[gx + 1][gy + 1]) {
                        float x = ox + gx * cs, y = oy + gy * cs + Ui.dp(6);
                        c.drawRoundRect(x - e, y - e, x + cs + e, y + cs + e, cs * 0.2f, cs * 0.2f, p);
                    }
        }
        // الأرضية الخشبية + الطلاء البرتقالي
        for (int gx = 0; gx < W; gx++) {
            for (int gy = 0; gy < H; gy++) {
                if (lv.wall[gx][gy]) continue;
                float x = ox + gx * cs, y = oy + gy * cs;
                c.drawRect(x, y, x + cs + 0.6f, y + cs + 0.6f, Ui.floorPaint);
                if (painted[gx][gy]) {
                    float a = Math.min(1f, (time - paintT[gx][gy]) / 0.14f);
                    orange.setAlpha((int) (255 * Math.max(0f, a)));
                    c.drawRect(x, y, x + cs + 0.6f, y + cs + 0.6f, orange);
                }
            }
        }
        // ظلال داخلية بجانب الجدران
        for (int gx = 0; gx < W; gx++) {
            for (int gy = 0; gy < H; gy++) {
                if (lv.wall[gx][gy]) continue;
                float x = ox + gx * cs, y = oy + gy * cs;
                if (isWall(gx, gy - 1)) {
                    c.save(); c.translate(x, y);
                    c.drawRect(0, 0, cs, edgeW, edge[0]);
                    c.restore();
                }
                if (isWall(gx, gy + 1)) {
                    c.save(); c.translate(x, y + cs);
                    c.drawRect(0, -edgeW, cs, 0, edge[1]);
                    c.restore();
                }
                if (isWall(gx - 1, gy)) {
                    c.save(); c.translate(x, y);
                    c.drawRect(0, 0, edgeW, cs, edge[2]);
                    c.restore();
                }
                if (isWall(gx + 1, gy)) {
                    c.save(); c.translate(x + cs, y);
                    c.drawRect(-edgeW, 0, 0, cs, edge[3]);
                    c.restore();
                }
            }
        }
        // الجدران الخشبية الداكنة
        for (int gx = -1; gx <= W; gx++) {
            for (int gy = -1; gy <= H; gy++) {
                if (!frame[gx + 1][gy + 1]) continue;
                float x = ox + gx * cs, y = oy + gy * cs;
                c.drawRect(x, y, x + cs + 0.6f, y + cs + 0.6f, Ui.wallPaint);
            }
        }
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Ui.dp(1.8f));
        for (int gx = -1; gx <= W; gx++) {
            for (int gy = -1; gy <= H; gy++) {
                if (!frame[gx + 1][gy + 1]) continue;
                float x = ox + gx * cs, y = oy + gy * cs;
                float o = Ui.dp(0.9f);
                p.setColor(0x66FFDDA8);
                if (!isWall(gx, gy - 1)) c.drawLine(x, y + o, x + cs, y + o, p);
                if (!isWall(gx - 1, gy)) c.drawLine(x + o, y, x + o, y + cs, p);
                p.setColor(0x55000000);
                if (!isWall(gx, gy + 1)) c.drawLine(x, y + cs - o, x + cs, y + cs - o, p);
                if (!isWall(gx + 1, gy)) c.drawLine(x + cs - o, y, x + cs - o, y + cs, p);
            }
        }
        p.setStyle(Paint.Style.FILL);
        // الكرة الخشبية
        float px = ox + (bx + 0.5f) * cs, py = oy + (by + 0.5f) * cs, r = cs * 0.34f;
        p.setColor(0x44000000);
        c.drawOval(px - r * 0.9f, py + r * 0.45f, px + r * 0.9f, py + r * 1.15f, p);
        float along, perp;
        if (moving) {
            float st = 0.16f * Math.min(1f, v / VMAX);
            along = 1f + st;
            perp = 1f - st * 0.6f;
        } else {
            along = 1f - 0.24f * squash;
            perp = 1f + 0.14f * squash;
        }
        c.save();
        c.translate(px, py);
        if (dir >= 2) c.scale(along, perp); else c.scale(perp, along);
        c.drawCircle(0, 0, r, ball);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Ui.dp(1.2f));
        p.setColor(0x55402000);
        c.drawCircle(0, 0, r, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x77FFFFFF);
        c.drawOval(-r * 0.58f, -r * 0.68f, -r * 0.08f, -r * 0.32f, p);
        c.restore();
        parts.draw(c);
        c.restore();
    }
}
