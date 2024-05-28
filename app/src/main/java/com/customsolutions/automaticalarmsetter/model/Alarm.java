package com.customsolutions.automaticalarmsetter.model;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.customsolutions.automaticalarmsetter.receiver.AlarmReceiver;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class Alarm {

    private final int requestCode;
    private final long epochTriggerTimeMillis;

    public Alarm(int requestCode, long epochTriggerTimeMillis) {
        this.requestCode = requestCode;
        this.epochTriggerTimeMillis = epochTriggerTimeMillis;
    }

    /**
     * Get an alarm object that will trigger after specified amount of time
     * Alarm's request code will be random and the intent will be directed at AlarmHandler service
     * @param triggerAfterTime time after which the alarm should trigger in milliseconds
     * @return Alarm object that will trigger after specified time
     */
    public static Alarm getAlarm(int triggerAfterTime) {
        long triggerEpochTime = System.currentTimeMillis() + triggerAfterTime;
        // Generate random request code. Apply ostrich algorithm to collusions
        int requestCode = ThreadLocalRandom.current().nextInt(1, 100000);
        return new Alarm(requestCode, triggerEpochTime);
    }


    public static Intent getIntent(Context context) {
        return new Intent(context, AlarmReceiver.class);
    }

    public static PendingIntent getPendingIntent(Context context, Alarm alarm) {
        return PendingIntent.getBroadcast(context, alarm.getRequestCode(), getIntent(context), PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alarm otherAlarm = (Alarm) o;
        // They are the same alarm if request code and trigger time are the same
        return requestCode == otherAlarm.requestCode &&
                epochTriggerTimeMillis == otherAlarm.epochTriggerTimeMillis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestCode, epochTriggerTimeMillis);
    }
    public long getEpochTriggerTimeMillis() {
        return epochTriggerTimeMillis;
    }

    public int getRequestCode() {
        return requestCode;
    }
}
