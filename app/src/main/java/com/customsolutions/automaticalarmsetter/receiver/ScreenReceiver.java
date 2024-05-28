package com.customsolutions.automaticalarmsetter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.customsolutions.automaticalarmsetter.handler.AlarmHandler;
import com.customsolutions.automaticalarmsetter.util.ForegroundNotificationTextUtil;

/**
 * Receiver for handling actions whenever the screen is turned on or off
 */
public class ScreenReceiver extends BroadcastReceiver {

    /**
     * Called whenever the ForegroundAlarmSetterService is running and a change in the state of the screen occurs
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ScreenReceiver", "ScreenReceiver received hit!");
        // Classes
        AlarmHandler alarmHandler = AlarmHandler.getInstance(context);
        ForegroundNotificationTextUtil foregroundNotificationTextUtil = ForegroundNotificationTextUtil.getInstance(context);

        String action = intent.getAction();

        if (action != null) {
            switch(action) {
                case Intent.ACTION_SCREEN_OFF:
                    Log.d("ScreenReceiver", "Screen turned off!");
                    // Schedule alarms
                    alarmHandler.scheduleAlarmsByFutureAlarmTimes(context);
                    // Update foreground notification text
                    foregroundNotificationTextUtil.updateNotificationContents(context);
                    break;
                case Intent.ACTION_SCREEN_ON:
                    Log.d("ScreenReceiver", "Screen turned on!");
                    // TODO - Cancel the alarm smartly. Maybe ask if they want to keep the alarm
                    break;
                default:
                    break;
            }
        }
    }
}
