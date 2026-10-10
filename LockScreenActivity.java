package com.childsafety.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LockScreenActivity extends Activity {
    public static void show(Context ctx, String pkg) {
        Intent i = new Intent(ctx, LockScreenActivity.class);
        i.putExtra("package", pkg);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#1e293b"));

        TextView t1 = new TextView(this);
        t1.setText("🔒"); t1.setTextSize(72); t1.setGravity(Gravity.CENTER);
        layout.addView(t1);

        TextView t2 = new TextView(this);
        t2.setText("App Locked by Parent");
        t2.setTextSize(20); t2.setTextColor(Color.WHITE); t2.setGravity(Gravity.CENTER);
        layout.addView(t2);

        setContentView(layout);
    }

    @Override
    public void onBackPressed() { moveTaskToBack(true); }
}
