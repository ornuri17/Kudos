package com.example.or.kudos;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Roy Oren on 14/06/2017.
 */

public class Kudos extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        createAlarm();
    }

    private void createAlarm() {
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationCreator.class);
        PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 10, 1000 * 10, pi);
    }
}
