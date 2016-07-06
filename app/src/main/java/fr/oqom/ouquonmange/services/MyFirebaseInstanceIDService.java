package fr.oqom.ouquonmange.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import fr.oqom.ouquonmange.repositories.Repository;
import fr.oqom.ouquonmange.models.GSMToken;
import fr.oqom.ouquonmange.models.Message;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIDService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        Log.d(TAG, "Send token to server: " + token);
        Config.saveGcmToken(token, getApplicationContext());
        String authToken = new Repository(getApplicationContext()).getToken();
        if (authToken != null) {
            Service.getInstance(getApplicationContext()).addGcmToken(new GSMToken(token))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Message>() {
                        @Override
                        public void call(Message message) {
                            Log.d(TAG, "Send token to server: Ok " + message);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.d(TAG, "Send token to server: Error " + throwable.getMessage());
                        }
                    });
        }
    }

}
