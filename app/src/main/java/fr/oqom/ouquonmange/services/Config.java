package fr.oqom.ouquonmange.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import fr.oqom.ouquonmange.R;

public class Config {

    private static final String GCM_TOKEN_KEY = "GCM_TOKEN_KEY";
    private static final String DEFAULT_COMMUNITY_KEY = "DEFAULT_COMMUNITY";

    public static String getGcmToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences("fr.oqom.ouquonmange", Context.MODE_PRIVATE);
        return sp.getString(GCM_TOKEN_KEY, null);
    }

    public static boolean saveGcmToken(String gcmToken, Context context) {
        SharedPreferences sp = context.getSharedPreferences("fr.oqom.ouquonmange", Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(GCM_TOKEN_KEY, gcmToken);
        return spe.commit();
    }

    public static String getDefaultCommunity(Context context) {
        SharedPreferences sp = context.getSharedPreferences("fr.oqom.ouquonmange", Context.MODE_PRIVATE);
        return sp.getString(DEFAULT_COMMUNITY_KEY, null);
    }

    public static boolean setDefaultCommunity(String communityUuid, Context context) {
        SharedPreferences sp = context.getSharedPreferences("fr.oqom.ouquonmange", Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(DEFAULT_COMMUNITY_KEY, communityUuid);
        return spe.commit();
    }

    public static boolean isNotificationEnabled(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.key_notifications_enabled), true);
    }
}
