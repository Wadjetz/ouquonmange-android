package fr.oqom.ouquonmange.models;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

    public static final String OUQUONMANGE_PREFERENCES = "OUQUONMANGE_PREFERENCES";
    public static final String KEY_USER_LEARNED = "KEY_USER_LEARNED";
    public static final String FALSE = "false";
    public static final String TRUE = "true";

    public static void setUserLearned(Context context, String prefName, String prefValue) {
        SharedPreferences.Editor sharedPreferencesEditor = context.getSharedPreferences(OUQUONMANGE_PREFERENCES, Context.MODE_PRIVATE).edit();
        sharedPreferencesEditor.putString(prefName, prefValue);
        sharedPreferencesEditor.apply();
    }

    public static String hasUserLearned(Context context, String prefName, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(OUQUONMANGE_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(prefName, defaultValue);
    }
}
