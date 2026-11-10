package com.alomari.slaypuzzle;

import android.content.Context;
import android.content.SharedPreferences;

/** حفظ التقدم - برمجة وتطوير: محمد العمري */
public class Progress {
    static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("slay_progress", Context.MODE_PRIVATE);
    }

    public static int unlocked(Context c) {
        return Math.min(LevelGen.TOTAL, Math.max(1, sp(c).getInt("unlocked", 1)));
    }

    public static int stars(Context c, int level) {
        return sp(c).getInt("s" + level, 0);
    }

    public static void complete(Context c, int level, int stars) {
        SharedPreferences s = sp(c);
        SharedPreferences.Editor e = s.edit();
        if (stars > s.getInt("s" + level, 0)) e.putInt("s" + level, stars);
        int next = Math.min(LevelGen.TOTAL, level + 1);
        if (next > s.getInt("unlocked", 1)) e.putInt("unlocked", next);
        e.apply();
    }

    public static boolean sound(Context c) {
        return sp(c).getBoolean("sound", true);
    }

    public static void setSound(Context c, boolean on) {
        sp(c).edit().putBoolean("sound", on).apply();
    }
}
