package fr.oqom.ouquonmange.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONObject;

import fr.oqom.ouquonmange.models.AuthRepository;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIDService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        Log.d(TAG, "Send token to server: " + token);
        Config.saveGcmToken(token, getApplicationContext());
        String authToken = new AuthRepository(getApplicationContext()).getToken();
        final String gcmToken = token;

        if (authToken != null) {
            new OuquonmangeApi(getApplicationContext()).addGcmToken(token, new Callback<JSONObject>() {
                @Override
                public void apply(JSONObject jsonObject) {
                    Log.d(TAG, "Send token to server: Ok " + gcmToken);
                }
            }, new Callback2<Throwable, JSONObject>() {
                @Override
                public void apply(Throwable throwable, JSONObject jsonObject) {
                    Log.d(TAG, "Send token to server: Error " + gcmToken);
                    Log.d(TAG, "Send token to server: Error " + throwable.getMessage());
                    Log.d(TAG, "Send token to server: Error " + jsonObject.toString());
                }
            });
        }
    }

}
