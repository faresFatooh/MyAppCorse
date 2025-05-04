package com.hrtrack.app.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hrtrack.app.R;

public class NotificationReceiver extends BroadcastReceiver {
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context ctx, Intent i) {
        String title = i.getStringExtra("title");
        String desc  = i.getStringExtra("desc");
        int id       = i.getIntExtra("nid", 0);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, "task_chan")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(desc)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(ctx).notify(id, b.build());
    }
}
