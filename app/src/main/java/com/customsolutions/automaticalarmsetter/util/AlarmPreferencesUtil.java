package com.customsolutions.automaticalarmsetter.util;


import android.content.Context;
import android.util.Log;

import com.customsolutions.automaticalarmsetter.model.Alarm;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton utility class for handling alarms with shared storage
 * Provides methods for setting/getting/deleting alarms and future alarm times from the shared storage
 */
public class AlarmPreferencesUtil extends SharedPreferencesUtil {
    private static AlarmPreferencesUtil instance;
    private static final String PREFERENCES_NAME = "AlarmPreferences";
    private static final String KEY_ALARMS = "alarms"; // Alarms that are actually set
    private static final String KEY_ALARMS_TO_SET_IN_FUTURE = "alarmsToSetInFuture"; // Alarms that should be set after a given amount of time

    // Singleton instance
    private AlarmPreferencesUtil(Context context) {
        super(context, PREFERENCES_NAME);
    }

    public static synchronized AlarmPreferencesUtil getInstance(Context context) {
        if (instance == null) {
            instance = new AlarmPreferencesUtil(context);
        }
        return instance;
    }

    /**
     * Gets the times after which alarms should be set from the shared preferences
     * @return List of Integers indicating the time after which alarms should be set in milliseconds
     */
    public List<Integer> getFutureAlarmTimes() {
        List<Integer> alarmTimes = getObjectFromPreferences(KEY_ALARMS_TO_SET_IN_FUTURE, new TypeToken<List<Integer>>() {});
        if (alarmTimes == null) {
            alarmTimes = new ArrayList<>();
        }
        return alarmTimes;
    }

    /**
     * Adds a duration after which an alarm should be set in to the shared preferences
     * @param time The time after which the alarm should sound once the screen has been turned off in milliseconds
     */
    public void addFutureAlarmTime(Integer time) {
        List<Integer> currentTimes = getFutureAlarmTimes();
        currentTimes.add(time);

        writeObjectToPreferences(KEY_ALARMS_TO_SET_IN_FUTURE, currentTimes);
        Log.d("AlarmPreferencesUtil", "Alarm set to trigger in " + time + " milliseconds!");
    }

    /**
     * Adds multiple durations after which an alarm should be set in to the shared preferences
     * @param newTimes List of integers indicating the time after which an alarm should sound once the screen has been turned off in milliseconds
     */
    public void addFutureAlarmTimes(List<Integer> newTimes) {
        List<Integer> times = getFutureAlarmTimes();
        times.addAll(newTimes);

        writeObjectToPreferences(KEY_ALARMS_TO_SET_IN_FUTURE, times);
    }

    /**
     * Removes all future alarm times
     */
    public void removeFutureAlarmTimes() {
        removeValueFromPreferences(KEY_ALARMS_TO_SET_IN_FUTURE);
    }

    /**
     * Checks if an alarm will be set in the future
     * @return Boolean indicating whether an alarm will be set in the future
     */
    public Boolean futureAlarmWillBeSet() {
        List<Integer> alarmList = getFutureAlarmTimes();
        return alarmList.size() > 0;
    }

    /**
     * Get actual alarms that have been set
     * @return List of alarms
     */
    public List<Alarm> getAlarms() {
        List<Alarm> alarms = getObjectFromPreferences(KEY_ALARMS, new TypeToken<List<Alarm>>() {});
        if (alarms == null) {
            alarms = new ArrayList<>();
        }
        return alarms;
    }

    /**
     * Add an actual alarm to the shared preferences
     * @param alarm Alarm to add to shared preferences
     */
    public void addAlarm(Alarm alarm) {
        List<Alarm> alarms = getAlarms();
        alarms.add(alarm);

        writeObjectToPreferences(KEY_ALARMS, alarms);
    }

    /**
     * Adds multiple alarms to the shared preferences
     * @param newAlarms List of Alarms to add
     */
    public void addAlarms(List<Alarm> newAlarms) {
        List<Alarm> alarms = getAlarms();
        alarms.addAll(newAlarms);

        writeObjectToPreferences(KEY_ALARMS, alarms);
    }

    /**
     * Removes all alarms from the shared preferences
     */
    public void removeAlarms() {
        removeValueFromPreferences(KEY_ALARMS);
    }

    /**
     * Removes a specific alarm from the shared preferences
     * @param alarm alarm to remove
     */
    public void removeAlarm(Alarm alarm) {
        List<Alarm> alarms = getAlarms();
        alarms.remove(alarm);
        writeObjectToPreferences(KEY_ALARMS, alarms);
    }

    /**
     * Checks whether an alarm has been set
     * @return Boolean indicating whether an alarm exists in the shared preferences
     */
    public Boolean alarmSet() {
        List<Alarm> alarms = getAlarms();
        return alarms.size() > 0;
    }
}
