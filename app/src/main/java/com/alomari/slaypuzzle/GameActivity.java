package com.alomari.slaypuzzle;

import android.app.Activity;
import android.os.Bundle;

/** شاشة اللعب - برمجة وتطوير: محمد العمري  730215825 */
public class GameActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.init(this);
        int lvl = getIntent().getIntExtra("level", 1);
        setContentView(new GameView(this, lvl));
        Ui.immersive(this);
    }

    @Override
    public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);
        if (f) Ui.immersive(this);
    }
}
