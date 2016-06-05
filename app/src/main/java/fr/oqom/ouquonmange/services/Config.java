package fr.oqom.ouquonmange.services;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {

    private static final String GCM_TOKEN_KEY = "GCM_TOKEN_KEY";

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
}
