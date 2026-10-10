package com.childsafety.app;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;
import org.json.JSONArray;

public class AppLockService extends AccessibilityService {
    private JSONArray lockedApps = new JSONArray();

    @Override
    public void onServiceConnected() {
        refreshLockedApps();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        try {
            String pkg = event.getPackageName() != null ? event.getPackageName().toString() : "";
            if (pkg.isEmpty() || pkg.equals(getPackageName())) return;
            if (isLocked(pkg)) LockScreenActivity.show(this, pkg);
        } catch (Exception ignored) {}
    }

    private boolean isLocked(String pkg) {
        for (int i = 0; i < lockedApps.length(); i++) {
            if (pkg.equals(lockedApps.optString(i))) return true;
        }
        return false;
    }

    public void refreshLockedApps() {
        try {
            SharedPreferences sp = getSharedPreferences("cs_locked", MODE_PRIVATE);
            lockedApps = new JSONArray(sp.getString("apps", "[]"));
        } catch (Exception ignored) {}
    }

    @Override
    public void onInterrupt() {}
}
