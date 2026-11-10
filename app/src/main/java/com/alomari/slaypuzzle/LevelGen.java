package com.alomari.slaypuzzle;

import java.util.Random;

/**
 * Slay the puzzle - توليد المستويات
 * برمجة وتطوير: محمد العمري  730215825
 *
 * كل مستوى يُولَّد بمحاكاة حركات انزلاق حقيقية، ثم تتحول الخلايا التي لم تمر عليها الكرة إلى جدران،
 * وبذلك يكون كل مستوى قابلاً للحل دائماً (والحل محفوظ في sol).
 */
public class LevelGen {
    public static final int TOTAL = 120;
    static final int[] DX = {0, 0, -1, 1};
    static final int[] DY = {-1, 1, 0, 0};

    public static class Level {
        public int w, h, sx, sy, floor, par;
        public boolean[][] wall;
        public int[] sol;
    }

    public static Level make(int n) {
        int tier = n - 1;
        int w = Math.min(5 + tier / 6, 10);
        int h = Math.min(6 + tier / 4, 14);
        int maxMoves = Math.min(4 + n / 3, 16);
        int minMoves = Math.min(maxMoves, 2 + n / 4);
        float dens = Math.min(0.10f + n * 0.004f, 0.28f);
        float needRatio = 0.42f + Math.min(0.12f, n * 0.002f);

        Level best = null;
        int bestScore = -1;
        for (int attempt = 0; attempt < 400; attempt++) {
            Random r = new Random(n * 100003L + attempt * 7919L + 12345L);
            boolean[][] wall = new boolean[w][h];
            for (int x = 0; x < w; x++)
                for (int y = 0; y < h; y++)
                    wall[x][y] = r.nextFloat() < dens;
            int x = -1, y = -1;
            for (int t = 0; t < 60; t++) {
                int rx = r.nextInt(w), ry = r.nextInt(h);
                if (!wall[rx][ry]) { x = rx; y = ry; break; }
            }
            if (x < 0) continue;
            int sx = x, sy = y;
            boolean[][] vis = new boolean[w][h];
            vis[x][y] = true;
            int[] sol = new int[maxMoves];
            int moves = 0, last = -1;
            for (int m = 0; m < maxMoves; m++) {
                int[] opts = new int[4];
                int cnt = 0;
                for (int d = 0; d < 4; d++) {
                    if (last >= 0 && d == (last ^ 1)) continue;
                    int nx = x + DX[d], ny = y + DY[d];
                    if (nx >= 0 && ny >= 0 && nx < w && ny < h && !wall[nx][ny]) opts[cnt++] = d;
                }
                if (cnt == 0) break;
                int d = opts[r.nextInt(cnt)];
                while (true) {
                    int nx = x + DX[d], ny = y + DY[d];
                    if (nx < 0 || ny < 0 || nx >= w || ny >= h || wall[nx][ny]) break;
                    x = nx; y = ny;
                    vis[x][y] = true;
                }
                sol[moves++] = d;
                last = d;
            }
            int count = 0;
            for (int i = 0; i < w; i++)
                for (int j = 0; j < h; j++)
                    if (vis[i][j]) count++;
            int score = count + moves * 3;
            boolean ok = moves >= minMoves && count >= (int) (w * h * needRatio) && count >= 7;
            if (ok || score > bestScore) {
                Level L = new Level();
                L.w = w; L.h = h; L.sx = sx; L.sy = sy; L.floor = count; L.par = moves;
                L.wall = new boolean[w][h];
                for (int i = 0; i < w; i++)
                    for (int j = 0; j < h; j++)
                        L.wall[i][j] = !vis[i][j];
                L.sol = new int[moves];
                System.arraycopy(sol, 0, L.sol, 0, moves);
                if (ok) return L;
                best = L;
                bestScore = score;
            }
        }
        return best;
    }
}
