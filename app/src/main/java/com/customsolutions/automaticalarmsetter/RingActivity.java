package com.customsolutions.automaticalarmsetter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

import com.customsolutions.automaticalarmsetter.handler.AlarmHandler;
import com.customsolutions.automaticalarmsetter.model.Alarm;
import com.customsolutions.automaticalarmsetter.service.ForegroundAlarmSetterService;
import com.customsolutions.automaticalarmsetter.util.AlarmPreferencesUtil;
import com.customsolutions.automaticalarmsetter.util.ForegroundNotificationTextUtil;

import java.util.List;

public class RingActivity extends AppCompatActivity {

    private static final int SNOOZE_TIME_MINUTES = 10;
    private static final long[] vibrationPattern = {0, 1000, 500}; // delay before starting, vibration duration, time before next vibration
    private AlarmHandler alarmHandler;
    private AlarmPreferencesUtil alarmPreferencesUtil;
    private ForegroundNotificationTextUtil foregroundNotificationTextUtil;
    private Vibrator vibrator;
    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("RingActivity", "Ring Activity started!");

        super.onCreate(savedInstanceState);
        // Show it on lock screen
        setShowWhenLocked(true);

        setContentView(R.layout.activity_ring);

        // Get class instances
        alarmHandler = AlarmHandler.getInstance(this);
        alarmPreferencesUtil = AlarmPreferencesUtil.getInstance(this);
        foregroundNotificationTextUtil = ForegroundNotificationTextUtil.getInstance(this);
        ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Start the alarm
        startAlarm();
    }

    /**
     * Snoozes the alarm by making it come back in SNOOZE_TIME_MINUTES minutes and exits the activity
     * @param view view object of the button
     */
    public void onSnoozeButtonClick(View view) {
        Log.d("RingActivity", "Alarm snoozed!");
        removeAlarmFromPreferences();
        // Set new alarm
        int timeInMillis = SNOOZE_TIME_MINUTES * 60 * 1000;
        alarmHandler.scheduleAlarmAfterTimeMillis(this, timeInMillis);

        // Update notification text
        foregroundNotificationTextUtil.updateNotificationContents(this);

        // Stop the alarm and exit
        stopAlarm();
        finish();
    }

    /**
     * Stops and removes the alarm and exits the activity
     * @param view view object of the button
     */
    public void onStopButtonClick(View view) {
        Log.d("RingActivity", "Alarm stopped!");
        removeAlarmFromPreferences();

        Intent serviceIntent = new Intent(this, ForegroundAlarmSetterService.class);

        // Check if this was the last alarm that was meant to ring
        if (alarmPreferencesUtil.alarmSet()) {
            // Alarm set
            Log.d("RingActivity", String.format("%d more alarms set! Updating foreground notification text!", alarmPreferencesUtil.getAlarms().size()));
            foregroundNotificationTextUtil.updateNotificationContents(this);
        } else {
            Log.d("RingActivity", "No future alarms set! Stopping foreground service.");
            // No alarms set, stop the foreground service
            stopService(serviceIntent);
        }

        // Stop the alarm and exit
        stopAlarm();
        finish();
    }

    /**
     * Removes the alarm that was set off from shared preferences
     */
    private void removeAlarmFromPreferences() {
        Alarm alarm = getAlarm();
        alarmPreferencesUtil.removeAlarm(alarm);
    }

    /**
     * Gets the alarm that was set off
     * @return Alarm object that caused the alarm
     */
    private Alarm getAlarm() {
        List<Alarm> alarms = alarmPreferencesUtil.getAlarms();
        return alarms.get(0);
    }

    /**
     * Makes the phone vibrate and play the default ringtone
     */
    private void startAlarm() {
        Log.d("RingActivity", "Starting the alarm!");
        // Play the ringtone
        ringtone.play();

        // Vibrate the phone
        if (vibrator != null && vibrator.hasVibrator()) {
            // Vibrator is available and permissions are granted
            vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0));
        }
    }

    /**
     * Stops the phone's vibration and ringtone
     */
    private void stopAlarm() {
        Log.d("RingActivity", "Stopping the alarm!");
        // Stop the ringtone
        ringtone.stop();

        // Stop the vibration
        vibrator.cancel();
    }
}