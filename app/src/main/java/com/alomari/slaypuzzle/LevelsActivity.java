package com.alomari.slaypuzzle;

import android.app.Activity;
import android.os.Bundle;

/** قائمة المستويات - برمجة وتطوير: محمد العمري  730215825 */
public class LevelsActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.init(this);
        setContentView(new LevelsView(this));
        Ui.immersive(this);
    }

    @Override
    public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);
        if (f) Ui.immersive(this);
    }
}
