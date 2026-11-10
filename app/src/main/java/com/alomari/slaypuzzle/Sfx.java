package com.alomari.slaypuzzle;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

/**
 * المؤثرات الصوتية - تُولَّد برمجياً (لا حاجة لملفات صوت خارجية)
 * برمجة وتطوير: محمد العمري  730215825
 */
public class Sfx {
    static Sfx inst;
    static final int RATE = 22050;
    static final int[] PENTA = {0, 2, 4, 7, 9, 12, 14};

    SoundPool pool;
    int sPop, sThud, sClick, sWin, sSwoosh;
    public boolean on = true;
    final Handler handler = new Handler(Looper.getMainLooper());

    public static Sfx get(Context c) {
        if (inst == null) inst = new Sfx(c.getApplicationContext());
        return inst;
    }

    private Sfx(Context c) {
        on = Progress.sound(c);
        AudioAttributes aa = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        pool = new SoundPool.Builder().setMaxStreams(10).setAudioAttributes(aa).build();
        try {
            sPop = load(c, "pop", marimba(740f, 0.24f, 0.8f, 7f));
            sClick = load(c, "click", marimba(1500f, 0.08f, 0.6f, 40f));
            sThud = load(c, "thud", thud());
            sSwoosh = load(c, "swoosh", swoosh());
            sWin = load(c, "win", fanfare());
        } catch (Exception ignored) {
        }
    }

    public void setOn(Context c, boolean v) {
        on = v;
        Progress.setSound(c, v);
    }

    void play(int id, float vol, float rate) {
        if (!on || id == 0) return;
        pool.play(id, vol, vol, 1, 0, Math.max(0.5f, Math.min(2f, rate)));
    }

    public void pop(int combo) {
        pop(combo, 0.7f);
    }

    public void pop(int combo, float vol) {
        int i = Math.max(0, combo - 1);
        int semi = PENTA[Math.min(i, PENTA.length - 1)];
        play(sPop, vol, (float) (0.84 * Math.pow(2, semi / 12.0)));
    }

    public void thud() { play(sThud, 1f, 1f); }
    public void block() { play(sThud, 0.5f, 1.4f); }
    public void click() { play(sClick, 0.8f, 1f); }
    public void swoosh() { play(sSwoosh, 0.7f, 1f); }
    public void win() { play(sWin, 1f, 1f); }
    public void splash() { play(sPop, 0.4f, 0.8f + new Random().nextFloat() * 1.0f); }

    public void winDelayed(long ms) {
        handler.postDelayed(new Runnable() {
            public void run() { win(); }
        }, ms);
    }

    // ---------- توليد الأصوات ----------
    int load(Context c, String name, short[] pcm) throws Exception {
        File f = new File(c.getCacheDir(), "sfx_" + name + ".wav");
        FileOutputStream o = new FileOutputStream(f);
        o.write(wav(pcm));
        o.close();
        return pool.load(f.getAbsolutePath(), 1);
    }

    static byte[] wav(short[] pcm) {
        int dataLen = pcm.length * 2;
        ByteBuffer b = ByteBuffer.allocate(44 + dataLen).order(ByteOrder.LITTLE_ENDIAN);
        b.put("RIFF".getBytes()).putInt(36 + dataLen).put("WAVE".getBytes());
        b.put("fmt ".getBytes()).putInt(16).putShort((short) 1).putShort((short) 1)
                .putInt(RATE).putInt(RATE * 2).putShort((short) 2).putShort((short) 16);
        b.put("data".getBytes()).putInt(dataLen);
        for (short s : pcm) b.putShort(s);
        return b.array();
    }

    static short[] marimba(float f, float dur, float vol, float decay) {
        int n = (int) (RATE * dur);
        short[] s = new short[n];
        for (int i = 0; i < n; i++) {
            double t = i / (double) RATE;
            double v = Math.sin(2 * Math.PI * f * t) * Math.exp(-t * decay)
                    + 0.35 * Math.sin(2 * Math.PI * f * 3.9 * t) * Math.exp(-t * 28)
                    + 0.15 * Math.sin(2 * Math.PI * f * 2 * t) * Math.exp(-t * 12);
            double a = Math.min(1.0, t / 0.002);
            s[i] = (short) (v * a * vol * 18000);
        }
        return s;
    }

    static short[] thud() {
        int n = (int) (RATE * 0.24f);
        short[] s = new short[n];
        Random r = new Random(5);
        double ph = 0;
        for (int i = 0; i < n; i++) {
            double t = i / (double) RATE;
            double f = 55 + 95 * Math.exp(-t * 25);
            ph += 2 * Math.PI * f / RATE;
            double v = Math.sin(ph) * Math.exp(-t * 14) + (r.nextDouble() * 2 - 1) * 0.45 * Math.exp(-t * 90);
            s[i] = (short) (v * 0.9 * 21000);
        }
        return s;
    }

    static short[] swoosh() {
        int n = (int) (RATE * 0.24f);
        short[] s = new short[n];
        Random r = new Random(9);
        double lp = 0;
        for (int i = 0; i < n; i++) {
            double t = i / (double) n;
            double env = Math.pow(Math.sin(Math.PI * t), 2);
            lp += ((r.nextDouble() * 2 - 1) - lp) * (0.12 + 0.3 * t);
            s[i] = (short) (lp * env * 0.7 * 20000);
        }
        return s;
    }

    static short[] fanfare() {
        float[] notes = {523.25f, 659.25f, 783.99f, 1046.5f, 1318.5f};
        float[] at = {0f, 0.12f, 0.24f, 0.36f, 0.5f};
        short[] out = new short[(int) (RATE * 1.3f)];
        for (int k = 0; k < notes.length; k++) {
            short[] m = marimba(notes[k], k == 4 ? 0.8f : 0.4f, 0.7f, k == 4 ? 4f : 6f);
            int off = (int) (at[k] * RATE);
            for (int i = 0; i < m.length && off + i < out.length; i++) {
                int v = out[off + i] + m[i];
                out[off + i] = (short) Math.max(-32000, Math.min(32000, v));
            }
        }
        return out;
    }
}
