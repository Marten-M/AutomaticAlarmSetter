package com.customsolutions.automaticalarmsetter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.customsolutions.automaticalarmsetter.handler.AlarmHandler;
import com.customsolutions.automaticalarmsetter.service.ForegroundAlarmSetterService;
import com.customsolutions.automaticalarmsetter.util.AlarmPreferencesUtil;
import com.customsolutions.automaticalarmsetter.util.TimeToStringFormatterUtil;

public class MainActivity extends AppCompatActivity {

    private NumberPicker hourPicker;
    private NumberPicker minutePicker;
    private NumberPicker secondPicker;

    private AlarmPreferencesUtil alarmPreferencesUtil;
    private AlarmHandler alarmHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate called!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get object's references
        hourPicker = findViewById(R.id.hourPicker);
        minutePicker = findViewById(R.id.minutePicker);
        secondPicker = findViewById(R.id.secondPicker);

        // Set the appropriate value ranges for the elements
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);

        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);

        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);

        alarmHandler = AlarmHandler.getInstance(this);
        alarmPreferencesUtil = AlarmPreferencesUtil.getInstance(this);
        updateActivityContents();

        // Set listeners for the number pickers
        hourPicker.setOnValueChangedListener(setNumberPickerValueChangedListener());
        minutePicker.setOnValueChangedListener(setNumberPickerValueChangedListener());
        secondPicker.setOnValueChangedListener(setNumberPickerValueChangedListener());
    }

    /**
     * Updates the contents whenever the activity is opened
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateActivityContents();
    }

    /**
     * Sets the text displayed on the screen depending on if an future alarm or an actual alarm has been set.
     * Also updates the content user can interact with (disables/enables buttons etc.)
     */
    private void updateActivityContents() {
        Button button = findViewById(R.id.setAlarmButton);
        TextView text = findViewById(R.id.alarmsSetText);

        boolean futureAlarmWillBeSet = alarmPreferencesUtil.futureAlarmWillBeSet();
        boolean alarmIsSet = alarmPreferencesUtil.alarmSet();

        if (alarmIsSet) {
            // An alarm is already set

            // Get epoch time when next alarm will go off
            long epochTime = alarmPreferencesUtil.getAlarms().get(0).getEpochTriggerTimeMillis();

            // Update text to display when the next alarm goes off
            String alarmIsSetFormatString = getString(R.string.alarm_is_set_format_text);
            String ringTimeString = TimeToStringFormatterUtil.convertTimeMillisTo24HourTime(epochTime);
            String ringTimeFullString = String.format(alarmIsSetFormatString, ringTimeString);
            text.setText(ringTimeFullString);

            // Update button to give the option to cancel the alarm
            String cancelAlarmButtonText = getString(R.string.cancel_alarm_button_text);
            button.setText(cancelAlarmButtonText);
        } else if (futureAlarmWillBeSet) {
            // Future alarm is set

            // Disable number pickers and enable the button
            setNumberPickersEnabled(false);
            button.setEnabled(true);

            // Change the button text to display cancel alarm
            String cancelAlarmButtonText = getString(R.string.cancel_alarm_button_text);
            button.setText(cancelAlarmButtonText);

            // Change the text below the button to show how long after turning off the screen the alarm will ring
            String alarmWillBeSetFormatString = getString(R.string.alarm_will_be_set_format_text);
            int timeMillis = alarmPreferencesUtil.getFutureAlarmTimes().get(0);
            String timeString = TimeToStringFormatterUtil.convertTimeInMillisToHumanreadableString(timeMillis);
            String alarmWillBeSetText = String.format(alarmWillBeSetFormatString, timeString);
            text.setText(alarmWillBeSetText);
        } else {
            // No alarms will be set

            setNumberPickersEnabled(true);

            if (hourPicker.getValue() != 0 || minutePicker.getValue() != 0 || secondPicker.getValue() != 0) {
                // One of the values is not 0, an alarm can be set.
                button.setEnabled(true);
                // Display set alarm text on button
                String setAlarmButtonText = getString(R.string.set_alarm_button_text);
                button.setText(setAlarmButtonText);

                // Display no alarms have been set on regular text below the button
                String noAlarmsHaveBeenSetText = getString(R.string.no_alarm_set_text);
                text.setText(noAlarmsHaveBeenSetText);
            } else {
                // All values are zero, disable the button
                button.setEnabled(false);
            }
        }
    }

    /**
     * Function that runs when the button below the number picker is clicked.
     * Clears current alarms, writes a future alarm time to shared preferences and starts the ForegroundAlarmSetterService.
     * @param view view object that function was called from (Button below number picker)
     */
    public void onSetAlarmButtonClick(View view) {
        Log.d("MainActivity", "Set Alarm button clicked!");
        Intent serviceIntent = new Intent(this, ForegroundAlarmSetterService.class);

        boolean futureAlarmsWillBeSet = alarmPreferencesUtil.futureAlarmWillBeSet();
        boolean alarmIsSet = alarmPreferencesUtil.alarmSet();
        if (alarmIsSet) {
            // At least one alarm is set, cancel them
            Log.d("MainActivity", "Cancelling alarms...");
            alarmHandler.cancelAlarms(this);
            Log.d("MainActivity", "Alarms cancelled!. Stopping foreground service...");
            stopService(serviceIntent);
        } else if (!futureAlarmsWillBeSet) {
            // No alarms set, set the alarm
            // Get chosen values
            int hours = hourPicker.getValue();
            int minutes = minutePicker.getValue();
            int seconds = secondPicker.getValue();

            // Add future alarm time to shared preferences
            Log.d("MainActivity", "Adding future alarm time to shared preferences");
            Integer time = (hours * 3600 + minutes * 60 + seconds) * 1000; // Time in milliseconds
            alarmPreferencesUtil.addFutureAlarmTime(time);

            // Start Alarm Setter Service
            Log.d("MainActivity", "Starting ForegroundAlarmSetterService...");
            startService(serviceIntent);
            Log.d("MainActivity", "ForegroundAlarmSetterService started!");
        } else { // Future alarms will be set, cancel them
            // Cancel old future alarm times
            Log.d("MainActivity", "Cancelling old future alarms and stopping foreground service...");
            alarmPreferencesUtil.removeFutureAlarmTimes();
            stopService(serviceIntent);
            Log.d("MainActivity", "Old alarms cancelled and foreground service stopped!");
        }
        // Update the text on the screen;
        updateActivityContents();
    }

    /**
     * Set a number pickers value changed listener
     * @return Class that contains a method that updates the text on the screen every time a value is changed on the number picker.
     */
    private NumberPicker.OnValueChangeListener setNumberPickerValueChangedListener() {
        return (picker, oldVal, newVal) -> {
            updateActivityContents(); // Update the text on the screen
        };
    }

    /**
     * Sets the enabled status of the number pickers
     * @param enabled Whether the number pickers should be enabled or not
     */
    private void setNumberPickersEnabled(boolean enabled) {
        hourPicker.setEnabled(enabled);
        minutePicker.setEnabled(enabled);
        secondPicker.setEnabled(enabled);
    }
}