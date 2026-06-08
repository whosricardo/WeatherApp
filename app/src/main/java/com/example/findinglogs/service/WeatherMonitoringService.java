package com.example.findinglogs.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.findinglogs.R;
import com.example.findinglogs.view.MainActivity;

public class WeatherMonitoringService extends Service {

    public static final String ACTION_STOP = "com.example.findinglogs.action.STOP_WEATHER_MONITORING";

    public static final String EXTRA_CITY = "extra_city";
    public static final String EXTRA_TEMPERATURE = "extra_temperature";
    public static final String EXTRA_CONDITION = "extra_condition";

    private static final String CHANNEL_ID = "weather_monitoring_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopWeatherMonitoring();
            return START_NOT_STICKY;
        }

        String city = intent != null ? intent.getStringExtra(EXTRA_CITY) : "Unknown city";
        String temperature = intent != null ? intent.getStringExtra(EXTRA_TEMPERATURE) : "--";
        String condition = intent != null ? intent.getStringExtra(EXTRA_CONDITION) : "Unknown condition";

        Notification notification = buildNotification(city, temperature, condition);

        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    private Notification buildNotification(String city, String temperature, String condition) {
        Intent openAppIntent = new Intent(this, MainActivity.class);

        PendingIntent openAppPendingIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, WeatherMonitoringService.class);
        stopIntent.setAction(ACTION_STOP);

        PendingIntent stopPendingIntent = PendingIntent.getService(
                this,
                1,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE);

        String title = "Monitoring weather";
        String content = city + " - " + temperature + " - " + condition;

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(openAppPendingIntent)
                .addAction(
                        R.drawable.ic_launcher_foreground,
                        "Stop",
                        stopPendingIntent)
                .setOngoing(true)
                .build();
    }

    private void stopWeatherMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }

        stopSelf();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Weather monitoring",
                NotificationManager.IMPORTANCE_LOW);

        channel.setDescription("Shows weather monitoring status");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
