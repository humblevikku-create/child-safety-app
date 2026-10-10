package com.childsafety.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;
import java.net.URISyntaxException;

public class SocketClient extends Service {
    private Socket socket;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            String url = PairingHelper.getServerUrl(this);
            String deviceId = PairingHelper.getDeviceIdFromConfig(this);
            socket = IO.socket(url);

            socket.on(Socket.EVENT_CONNECT, args -> {
                try {
                    JSONObject join = new JSONObject();
                    join.put("deviceId", deviceId);
                    join.put("role", "device");
                    socket.emit("join-device", join);
                } catch (Exception ignored) {}
            });

            socket.on("command", args -> {
                try {
                    JSONObject cmd = (JSONObject) args[0];
                    Log.d("Socket", "Command: " + cmd.getString("cmd"));
                } catch (Exception ignored) {}
            });

            socket.connect();
        } catch (URISyntaxException ignored) {}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        if (socket != null) { socket.disconnect(); socket.off(); }
        super.onDestroy();
    }
}
