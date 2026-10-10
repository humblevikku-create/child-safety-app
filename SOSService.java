package com.childsafety.app;

import android.app.*;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class SOSService extends Service {
    private static final String CH = "cs_sos";
    private static final int NID = 2001;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForegroundInternal();
        FusedLocationProviderClient fused = LocationServices.getFusedLocationProviderClient(this);
        try {
            fused.getLastLocation().addOnSuccessListener(loc -> {
                double lat = loc != null ? loc.getLatitude() : 0;
                double lng = loc != null ? loc.getLongitude() : 0;
                ApiClient.triggerSOS(SOSService.this, lat, lng, 50);
            });
        } catch (SecurityException ignored) {}
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CH, "SOS",
                NotificationManager.IMPORTANCE_HIGH);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    private void startForegroundInternal() {
        Notification notif = new NotificationCompat.Builder(this, CH)
            .setContentTitle("🚨 SOS ACTIVE")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NID, notif);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return START_NOT_STICKY; }
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
