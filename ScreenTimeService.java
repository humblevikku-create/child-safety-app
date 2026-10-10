package com.childsafety.app;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.List;

public class ScreenTimeService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override
    public IBinder onBind(Intent intent) { return null; }

    public static void uploadUsage(Context ctx) {
        try {
            UsageStatsManager usm = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usm == null) return;
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, -1);
            List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                cal.getTimeInMillis(), System.currentTimeMillis());
            if (stats == null) return;
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(new java.util.Date());
            for (UsageStats stat : stats) {
                long total = stat.getTotalTimeInForeground();
                if (total > 0) {
                    JSONObject data = new JSONObject();
                    data.put("appPackage", stat.getPackageName());
                    data.put("minutes", total / 60000);
                    data.put("date", date);
                    data.put("timestamp", System.currentTimeMillis());
                    ApiClient.uploadData(ctx, "screentime", data);
                }
            }
        } catch (Exception ignored) {}
    }
}
