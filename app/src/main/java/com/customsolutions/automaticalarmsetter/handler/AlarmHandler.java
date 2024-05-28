package com.customsolutions.automaticalarmsetter.handler;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.customsolutions.automaticalarmsetter.model.Alarm;
import com.customsolutions.automaticalarmsetter.util.AlarmPreferencesUtil;

import java.util.List;

/**
 * Singleton class for scheduling and removing alarms
 */
public class AlarmHandler {

    private static AlarmHandler instance;
    private final AlarmPreferencesUtil alarmPreferencesUtil;

    private AlarmHandler(Context context) {
        this.alarmPreferencesUtil = AlarmPreferencesUtil.getInstance(context);
    }

    public static synchronized AlarmHandler getInstance(Context context) {
        if (instance == null) {
            instance = new AlarmHandler(context);
        }
        return instance;
    }

    /**
     * Schedules and sets a single alarm that will trigger after given amount of time
     * @param context context of the application
     * @param triggerAfterMillis time after which to trigger the alarm in milliseconds
     */
    public void scheduleAlarmAfterTimeMillis(Context context, int triggerAfterMillis) {
        Alarm alarm = Alarm.getAlarm(triggerAfterMillis);
        scheduleAlarm(context, alarm);
    }

    /**
     * Schedules all the alarms based on future alarm times found in shared preferences
     * @param context context of the application
     */
    public void scheduleAlarmsByFutureAlarmTimes(Context context) {
        // Iterate over alarm times if any alarms are set
        List<Integer> futureAlarmTimes = alarmPreferencesUtil.getFutureAlarmTimes();
        int numberOfAlarms = futureAlarmTimes.size();
        if (numberOfAlarms > 0) {
            Log.d("AlarmHandler", String.format("Setting %d alarms...", numberOfAlarms));
            for (Integer alarmTime : futureAlarmTimes) {
                Alarm alarm = Alarm.getAlarm(alarmTime);
                scheduleAlarm(context, alarm);
            }
            // Remove future alarm times from shared preferences
            alarmPreferencesUtil.removeFutureAlarmTimes();
            Log.d("AlarmHandler", "Alarms set!");
            return;
        }
        Log.d("AlarmHandler", "No alarms to set!");
    }

    /**
     * Schedules an alarm and adds it to shared preferences
     * @param context context of the application
     * @param alarm alarm to schedule
     */
    public void scheduleAlarm(Context context, Alarm alarm) {
        // Configure intent to hit the alarm receiver class once the alarm goes off
        PendingIntent pendingIntent = Alarm.getPendingIntent(context, alarm);

        long triggerTime = alarm.getEpochTriggerTimeMillis();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(triggerTime, pendingIntent);

        // Check which build the phone has and use a function to set an alarm based on that
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Permissions to set exact alarms granted.
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
            } else {
                // Something is very wrong
                Log.d("AlarmHandler", "Don't have permissions to set an alarm!");
            }
        } else {
            // For older versions of Android. Not tested, might have unintended behaviour.
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
        // Write the alarm to the shared preferences
        alarmPreferencesUtil.addAlarm(alarm);
    }

    /**
     * Cancel all alarms that have been set and remove them from shared preferences
     * @param context context of the application
     */
    public void cancelAlarms(Context context) {
        List<Alarm> alarms = alarmPreferencesUtil.getAlarms();
        int alarmsToCancel = alarms.size();
        if (alarmsToCancel > 0) {
            Log.d("AlarmHandler", String.format("Cancelling %d alarms...", alarmsToCancel));
            for (Alarm alarm : alarms) {
                cancelAlarm(context, alarm);
            }
            Log.d("AlarmHandler", "Alarms canceled!");
            return;
        }
        Log.d("AlarmHandler", "No alarms to cancel!");
    }

    /**
     * Cancel an alarm and remove it from shared preferences
     * @param context context of the application
     * @param alarm alarm to cancel
     */
    public void cancelAlarm(Context context, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = Alarm.getPendingIntent(context, alarm);
        alarmManager.cancel(pendingIntent);
        alarmPreferencesUtil.removeAlarm(alarm);
    }
}
