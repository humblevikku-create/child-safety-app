package com.childsafety.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import okhttp3.*;

public class MainActivity extends Activity {
    private TextView statusText;
    private OkHttpClient http = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statusText = new TextView(this);
        statusText.setTextSize(18);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(50, 50, 50, 50);
        statusText.setTextColor(Color.parseColor("#6366f1"));
        setContentView(statusText);

        if (PairingHelper.isPaired(this)) {
            statusText.setText("✅ Device Connected\n\nProtecting your child 24/7");
            startAllServices();
        } else {
            statusText.setText("🔗 Activating...");
            requestPermissions();
            performPairing();
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] perms = {
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.READ_CALL_LOG,
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            };
            requestPermissions(perms, 1001);
        }
    }

    private void performPairing() {
        new Thread(() -> {
            try {
                JSONObject pairingData = PairingHelper.loadPairingData(this);
                if (pairingData == null) {
                    runOnUiThread(() -> statusText.setText("❌ Invalid APK"));
                    return;
                }
                String pairingCode = pairingData.getString("code");
                String serverUrl = pairingData.getString("serverUrl");
                JSONObject body = PairingHelper.buildDeviceInfo(this);
                body.put("pairingCode", pairingCode);

                Request req = new Request.Builder()
                    .url(serverUrl + "/api/device/pair")
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .build();
                Response resp = http.newCall(req).execute();
                String respStr = resp.body().string();

                if (resp.isSuccessful()) {
                    JSONObject json = new JSONObject(respStr);
                    PairingHelper.saveConfig(this,
                        json.getString("deviceToken"),
                        json.getString("deviceId"),
                        serverUrl);
                    runOnUiThread(() -> {
                        statusText.setText("✅ Activated!\n\nAll services running.");
                        Toast.makeText(this, "Paired successfully", Toast.LENGTH_LONG).show();
                    });
                    startAllServices();
                } else {
                    runOnUiThread(() -> statusText.setText("❌ Pairing failed\n\n" + respStr));
                }
            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("❌ Error\n\n" + e.getMessage()));
            }
        }).start();
    }

    private void startAllServices() {
        Intent loc = new Intent(this, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(loc);
        else startService(loc);

        startService(new Intent(this, SocketClient.class));
        startService(new Intent(this, ScreenTimeService.class));
    }
}
