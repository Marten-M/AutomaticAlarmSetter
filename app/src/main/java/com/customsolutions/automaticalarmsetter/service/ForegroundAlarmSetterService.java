package com.customsolutions.automaticalarmsetter.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.customsolutions.automaticalarmsetter.MainActivity;
import com.customsolutions.automaticalarmsetter.R;
import com.customsolutions.automaticalarmsetter.receiver.ScreenReceiver;
import com.customsolutions.automaticalarmsetter.util.AlarmPreferencesUtil;
import com.customsolutions.automaticalarmsetter.util.TimeToStringFormatterUtil;

/**
 * Foreground Service that sets an alarm once the screen turns off
 */
public class ForegroundAlarmSetterService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "AlarmSetterServiceChannel";
    private ScreenReceiver screenReceiver;

    /**
     * Starts foreground service and dynamically sets up a ScreenReceiver to capture SCREEN_OFF broadcasts
     */
    @Override
    public void onCreate() {
        Log.d("AlarmSetterService", "Alarm Setter Service Created!");
        // Register the receiver
        screenReceiver = new ScreenReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, intentFilter);
        Log.d("AlarmSetterService", "Receiver set!");

        // Create notification channel
        createNotificationChannel();

        // Get the notifications text
        String notificationTitleText = getString(R.string.alarm_will_be_set_notification_title);
        // Get the content text. Content shows how long after turning off the screen the alarm will ring
        String alarmWillBeSetNotificationContent = getString(R.string.alarm_will_be_set_format_text);
        // Get the time when the alarm will go off from shared preferences
        AlarmPreferencesUtil alarmPreferencesUtil = AlarmPreferencesUtil.getInstance(this);
        String timeString = TimeToStringFormatterUtil.convertTimeInMillisToHumanreadableString(alarmPreferencesUtil.getFutureAlarmTimes().get(0));
        String notificationContentString = String.format(alarmWillBeSetNotificationContent, timeString);

        // Start the foreground service
        startForeground(NOTIFICATION_ID, buildNotification(this, notificationTitleText, notificationContentString));
        Log.d("AlarmSetterService", "Foreground service started!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AlarmSetterService", "Alarm Setter Service onStartCommand called!");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("AlarmSetterService", "Alarm Setter Service destroyed!");
        super.onDestroy();

        // Remove the Receiver
        unregisterReceiver(screenReceiver);
    }

    /**
     * Creates a notification channel. Required for Android > 8.0
     */
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Alarm Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    /**
     * Builds the notification object for the foreground service
     * @return notification containing information about the intents of the alarm
     */
    private static Notification buildNotification(Context context, String notificationTitle, String notificationContent) {
        Log.d("ForegroundAlarmSetterService", "Building notification...");
        // Create intent
        PendingIntent pendingIntent = getPendingIntent(context);

        return new Notification.Builder(context, CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setContentText(notificationContent)
                .setSmallIcon(R.drawable.alarm_icon_small)
                .setContentIntent(pendingIntent)
                .build();
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );
    }

    /**
     * Updates the permanent notification's text
     * @param context Context of the application
     * @param newTitleText Text to set the notification's title to
     * @param newContentText Text to set the notification's content to
     */
    public static void updateNotificationText(Context context, String newTitleText, String newContentText) {
        Notification notification = buildNotification(context, newTitleText, newContentText);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
