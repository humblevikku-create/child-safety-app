package com.childsafety.app;

import android.app.*;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.*;
import org.json.JSONObject;

public class LocationService extends Service {
    private static final String CH = "cs_location";
    private static final int NID = 1001;
    private FusedLocationProviderClient fused;
    private LocationCallback callback;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForegroundInternal();
        fused = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest req = LocationRequest.create()
            .setInterval(60000)
            .setFastestInterval(30000)
            .setSmallestDisplacement(10)
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc == null) return;
                try {
                    JSONObject data = new JSONObject();
                    data.put("lat", loc.getLatitude());
                    data.put("lng", loc.getLongitude());
                    data.put("accuracy", loc.getAccuracy());
                    data.put("timestamp", System.currentTimeMillis());
                    ApiClient.uploadData(LocationService.this, "location", data);
                } catch (Exception ignored) {}
            }
        };

        try {
            fused.requestLocationUpdates(req, callback, getMainLooper());
        } catch (SecurityException ignored) {}
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CH, "Location",
                NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    private void startForegroundInternal() {
        Notification notif = new NotificationCompat.Builder(this, CH)
            .setContentTitle("Child Safety Active")
            .setContentText("Protection running")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true).build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NID, notif);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        if (fused != null && callback != null) fused.removeLocationUpdates(callback);
        super.onDestroy();
    }
}
