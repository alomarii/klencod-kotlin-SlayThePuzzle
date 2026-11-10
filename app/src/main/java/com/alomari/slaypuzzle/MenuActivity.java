package com.alomari.slaypuzzle;

import android.app.Activity;
import android.os.Bundle;

/** الواجهة الرئيسية - برمجة وتطوير: محمد العمري  730215825 */
public class MenuActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.init(this);
        setContentView(new MenuView(this));
        Ui.immersive(this);
    }

    @Override
    public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);
        if (f) Ui.immersive(this);
    }
}
