package fr.oqom.ouquonmange.models;

import android.content.Context;

import fr.oqom.ouquonmange.utils.Callback;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class AuthRepository {

    private RealmConfiguration realmConfig;
    private Realm realm;
    private Context context;
    private RealmQuery<Auth> auth;

    public AuthRepository(Context context) {
        this.context = context;
        realmConfig = new RealmConfiguration.Builder(context).build();
        realm = Realm.getInstance(realmConfig);
        auth = realm.where(Auth.class);
    }

    public void save(final String token, final Callback<Void> success, final Callback<Throwable> failure) {
        realm.executeTransactionAsync(new Realm.Transaction() {

            @Override
            public void execute(Realm realm) {
                Auth auth = realm.createObject(Auth.class);
                auth.setToken(token);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                success.apply(null);
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                failure.apply(error);
            }
        });
    }

    public String getToken() {
        RealmResults<Auth> auths = auth.findAll();
        if (auths.size() == 0) {
            return null;
        } else {
            return auths.first().getToken();
        }
    }

    public void deleteToken(final Callback<Void> success) {
        realm.executeTransaction(new Realm.Transaction() {

            @Override
            public void execute(Realm realm) {
                RealmResults<Auth> auths = auth.findAll();
                if (auths.size() > 0) {
                    auths.remove(0);
                }
                success.apply(null);
            }

        });
    }
}
