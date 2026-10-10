package com.childsafety.app;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import java.io.File;
import okhttp3.*;

public class ApiClient {
    private static final OkHttpClient http = new OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build();

    public static void uploadData(Context ctx, String type, JSONObject data) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("deviceId", PairingHelper.getDeviceIdFromConfig(ctx));
                body.put("type", type);
                body.put("data", data);
                Request req = new Request.Builder()
                    .url(PairingHelper.getServerUrl(ctx) + "/api/data/upload")
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .build();
                http.newCall(req).execute();
            } catch (Exception e) { Log.e("ApiClient", "Upload fail: " + e.getMessage()); }
        }).start();
    }

    public static void uploadFile(Context ctx, String type, File file, JSONObject meta) {
        new Thread(() -> {
            try {
                MultipartBody.Builder mb = new MultipartBody.Builder().setType(MultipartBody.FORM);
                mb.addFormDataPart("deviceId", PairingHelper.getDeviceIdFromConfig(ctx));
                mb.addFormDataPart("type", type);
                if (meta != null) mb.addFormDataPart("meta", meta.toString());
                mb.addFormDataPart("file", file.getName(),
                    RequestBody.create(file, MediaType.parse("application/octet-stream")));
                Request req = new Request.Builder()
                    .url(PairingHelper.getServerUrl(ctx) + "/api/data/upload-file")
                    .post(mb.build())
                    .build();
                http.newCall(req).execute();
            } catch (Exception e) { Log.e("ApiClient", "File fail: " + e.getMessage()); }
        }).start();
    }

    public static void sendHeartbeat(Context ctx, int battery) {
        try {
            JSONObject body = new JSONObject();
            body.put("deviceId", PairingHelper.getDeviceIdFromConfig(ctx));
            body.put("battery", battery);
            Request req = new Request.Builder()
                .url(PairingHelper.getServerUrl(ctx) + "/api/device/heartbeat")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();
            http.newCall(req).execute();
        } catch (Exception ignored) {}
    }

    public static void triggerSOS(Context ctx, double lat, double lng, int battery) {
        try {
            JSONObject body = new JSONObject();
            body.put("deviceId", PairingHelper.getDeviceIdFromConfig(ctx));
            body.put("deviceToken", PairingHelper.getDeviceToken(ctx));
            body.put("lat", lat);
            body.put("lng", lng);
            body.put("battery", battery);
            body.put("triggerType", "button");
            Request req = new Request.Builder()
                .url(PairingHelper.getServerUrl(ctx) + "/api/alerts/sos")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();
            http.newCall(req).execute();
        } catch (Exception ignored) {}
    }
}
