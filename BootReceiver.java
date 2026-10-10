package com.childsafety.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (!PairingHelper.isPaired(ctx)) return;

        Intent loc = new Intent(ctx, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(loc);
        else ctx.startService(loc);

        ctx.startService(new Intent(ctx, SocketClient.class));
        ctx.startService(new Intent(ctx, ScreenTimeService.class));
    }
}
