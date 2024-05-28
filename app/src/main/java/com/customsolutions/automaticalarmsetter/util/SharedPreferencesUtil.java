package com.customsolutions.automaticalarmsetter.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * Utility class for handling shared preferences in the application.
 * Only works with preferences who's value is a String
 * Provides methods to serialize/deserialize java classes to JSON objects and write them to the shared preferences
 */
public class SharedPreferencesUtil {

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesUtil(Context context, String sharedPreferencesName) {
        sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
    }

    /**
     * Serializes a JAVA object to Json
     * @param object object to serialize
     * @return Json string
     */
    private <T> String serializeToJson(T object) {
        return new Gson().toJson(object);
    }

    /**
     * Deserializes Json string to JAVA object
     * @param jsonString string to deserialize. Must be a valid string that can be parsed to a JAVA object of specified type.
     * @param typeToken typeToken of the JAVA object type
     * @return JAVA object
     */
    private <T> T deserializeFromJson(String jsonString, TypeToken<T> typeToken) {
        return new Gson().fromJson(jsonString, typeToken.getType());
    }

    /**
     * Writes Json string to shared preferences
     * @param key preference of who's value to set
     * @param jsonString json string to write to preferences
     */
    private void writeJsonToPreferences(String key, String jsonString) {
        Editor editor = sharedPreferences.edit();
        editor.putString(key, jsonString);
        editor.apply();
    }

    /**
     * Removes a value from shared preferences
     * @param key preference who's value to remove
     */
    protected void removeValueFromPreferences(String key) {
        Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * Writes a JAVA object to shared preferences
     * @param key preference who's value to set
     * @param object object to serialize
     */
    protected <T> void writeObjectToPreferences(String key, T object) {
        String jsonString = serializeToJson(object);
        writeJsonToPreferences(key, jsonString);
    }

    /**
     * Gets JAVA object from shared preferences
     * @param key key who's value to get
     * @param typeToken typeToken of the JAVA object's type to receive
     * @return JAVA object stored in preferences, null if it does not exist
     */
    protected <T> T getObjectFromPreferences(String key, TypeToken<T> typeToken) {
        String jsonString = sharedPreferences.getString(key, "");
        if (!jsonString.isEmpty()) {
            return deserializeFromJson(jsonString, typeToken);
        }
        return null;
    }

    /**
     * Checks if a preference value exists
     * @param key key who's value's existence to check
     * @return boolean indicating whether the value exists or not
     */
    protected Boolean valueExists(String key) {
        String fieldValue = sharedPreferences.getString(key, "");
        return !fieldValue.isEmpty();
    }

}
