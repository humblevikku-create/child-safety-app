package com.childsafety.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PairingHelper {
    public static JSONObject loadPairingData(Context ctx) {
        try {
            InputStream is = ctx.getAssets().open("pairing.json");
            byte[] buf = new byte[is.available()];
            is.read(buf);
            is.close();
            return new JSONObject(new String(buf, StandardCharsets.UTF_8));
        } catch (Exception e) { return null; }
    }

    public static String getDeviceId(Context ctx) {
        String id = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (id == null) id = "unknown_" + System.currentTimeMillis();
        return id;
    }

    public static JSONObject buildDeviceInfo(Context ctx) {
        JSONObject info = new JSONObject();
        try {
            info.put("deviceId", getDeviceId(ctx));
            info.put("model", Build.MODEL);
            info.put("manufacturer", Build.MANUFACTURER);
            info.put("androidVersion", Build.VERSION.RELEASE);
            info.put("sdkVersion", Build.VERSION.SDK_INT);
            info.put("childName", Build.MODEL);
            try {
                TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
                info.put("simOperator", tm.getNetworkOperatorName());
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
        return info;
    }

    public static void saveConfig(Context ctx, String token, String deviceId, String url) {
        ctx.getSharedPreferences("cs_config", Context.MODE_PRIVATE).edit()
            .putString("deviceToken", token)
            .putString("deviceId", deviceId)
            .putString("serverUrl", url)
            .putBoolean("paired", true)
            .apply();
    }

    public static String getServerUrl(Context ctx) {
        return ctx.getSharedPreferences("cs_config", Context.MODE_PRIVATE)
            .getString("serverUrl", "http://localhost:3000");
    }

    public static String getDeviceToken(Context ctx) {
        return ctx.getSharedPreferences("cs_config", Context.MODE_PRIVATE)
            .getString("deviceToken", "");
    }

    public static String getDeviceIdFromConfig(Context ctx) {
        return ctx.getSharedPreferences("cs_config", Context.MODE_PRIVATE)
            .getString("deviceId", "");
    }

    public static boolean isPaired(Context ctx) {
        return ctx.getSharedPreferences("cs_config", Context.MODE_PRIVATE)
            .getBoolean("paired", false);
    }
}
