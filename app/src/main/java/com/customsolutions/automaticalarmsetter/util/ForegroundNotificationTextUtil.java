package com.customsolutions.automaticalarmsetter.util;

import android.content.Context;

import com.customsolutions.automaticalarmsetter.R;
import com.customsolutions.automaticalarmsetter.service.ForegroundAlarmSetterService;

/**
 * Singleton class containing methods meant to help set the contents of the foreground notification
 */
public class ForegroundNotificationTextUtil {
    private static ForegroundNotificationTextUtil instance;
    private final AlarmPreferencesUtil alarmPreferencesUtil;

    private ForegroundNotificationTextUtil(Context context) {
        alarmPreferencesUtil = AlarmPreferencesUtil.getInstance(context);
    }

    public static synchronized ForegroundNotificationTextUtil getInstance(Context context) {
        if (instance == null) {
            instance = new ForegroundNotificationTextUtil(context);
        }
        return instance;
    }

    /**
     * Automatically update the contents of the foreground notification
     */
    public void updateNotificationContents(Context context) {
        boolean alarmWillBeSet = alarmPreferencesUtil.futureAlarmWillBeSet();
        boolean alarmIsSet = alarmPreferencesUtil.alarmSet();
        if (alarmIsSet) {
            setNotificationContentsToNextAlarmTime(context);
        } else if (alarmWillBeSet) {
            setNotificationContentsToFutureAlarmTime(context);
        }
    }

    /**
     * Sets the contents of the notification to show when the next alarm will ring
     */
    private void setNotificationContentsToNextAlarmTime(Context context) {
        // Get epoch time when next alarm will go off
        long epochTime = alarmPreferencesUtil.getAlarms().get(0).getEpochTriggerTimeMillis();

        // Update notification text to display when the next one goes off
        String alarmIsSetFormatString = context.getString(R.string.alarm_is_set_format_text);
        String ringTimeString = TimeToStringFormatterUtil.convertTimeMillisTo24HourTime(epochTime);

        String notificationTextString = String.format(alarmIsSetFormatString, ringTimeString);
        String titleTextString = context.getString(R.string.alarm_is_set_notification_title_text);

        ForegroundAlarmSetterService.updateNotificationText(context, titleTextString, notificationTextString);
    }

    /**
     * Sets the contents of the notification to show when the alarm will ring after the screen is turned off
     */
    private void setNotificationContentsToFutureAlarmTime(Context context) {
        // Get the notifications text
        String notificationTitleText = context.getString(R.string.alarm_will_be_set_notification_title);
        // Get the content text. Content shows how long after turning off the screen the alarm will ring
        String alarmWillBeSetNotificationContent = context.getString(R.string.alarm_will_be_set_format_text);
        // Get the time when the alarm will go off from shared preferences
        String timeString = TimeToStringFormatterUtil.convertTimeInMillisToHumanreadableString(alarmPreferencesUtil.getFutureAlarmTimes().get(0));
        String notificationContentString = String.format(alarmWillBeSetNotificationContent, timeString);

        ForegroundAlarmSetterService.updateNotificationText(context, notificationTitleText, notificationContentString);
    }
}
