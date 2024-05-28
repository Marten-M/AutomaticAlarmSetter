package com.customsolutions.automaticalarmsetter.util;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class containing static functions meant to
 */
public class TimeToStringFormatterUtil {
    public static final String SECOND = "second";
    public static final String MINUTE = "minute";
    public static final String HOUR = "hour";
    // Empty private constructor so a class could never be initiated
    private TimeToStringFormatterUtil() {

    }

    /**
     * Converts a time in milliseconds to a string indicating how many hours, minutes and seconds that is.
     * If time cannot be converted to seconds without a remainder then the remainder will be ignored
     * @param time Time to convert in milliseconds
     * @return String in the format 'X hours, Y minutes and Z seconds' (will not display fields that are 0
     */
    public static String convertTimeInMillisToHumanreadableString(int time) {
        int hours = time / 1000 / 3600;
        int minutes = (time - hours * 3600 * 1000) / 1000 / 60;
        int seconds = (time - hours * 3600 * 1000 - minutes * 60 * 1000) / 1000;
        Log.d("TimeToStringFormatterUtil", String.format(Locale.getDefault(), "Converted %d milliseconds to %d hours, %d minutes and %d seconds", time, hours, minutes, seconds));

        boolean andRequired = false;

        String returnString = "";
        if (hours > 0) {
            returnString += convertTimeStringToCorrectPluralForm(hours, HOUR);
            andRequired = true;
        }
        if (minutes > 0) {
            if (!returnString.isEmpty()) {
                if (seconds > 0)
                    returnString += " ";
                else
                    returnString += " and "; // No seconds to display, end the string with minutes
            }
            returnString += convertTimeStringToCorrectPluralForm(minutes, MINUTE);
            andRequired = true;
        }
        if (seconds > 0) {
            if (andRequired) returnString += " and ";
            returnString += convertTimeStringToCorrectPluralForm(seconds, SECOND);
        }
        return returnString;
    }

    /**
     * Get a time string in correct plurality.
     * Use the class attributes SECOND, MINUTE and HOUR as the unit.
     * @param timeValue The multiple of the time unit
     * @param unit The unit of time as a string ("hour", "minute" or "second")
     * @return the time string in the correct plurality
     */
    private static String convertTimeStringToCorrectPluralForm(int timeValue, String unit) {
        if (timeValue == 1) {
            return String.format(Locale.getDefault(),"%d %s", timeValue, unit);
        } else {
            return String.format(Locale.getDefault(),"%d %ss", timeValue, unit);
        }
    }

    /**
     * Converts an epoch time in milliseconds to 24 hour time format time
     * @param timeMillis epoch time in milliseconds
     * @return 24hour formatted string of the time
     */
    public static String convertTimeMillisTo24HourTime(long timeMillis) {
        Date date = new Date(timeMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

}
