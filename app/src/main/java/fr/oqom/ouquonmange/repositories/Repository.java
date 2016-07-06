package fr.oqom.ouquonmange.repositories;

import android.content.Context;

import java.util.List;

import fr.oqom.ouquonmange.models.Auth;
import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.models.Profile;
import fr.oqom.ouquonmange.utils.Callback;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class Repository {

    private Realm realm;

    public Repository(Context context) {
        Context context1 = context;
        RealmConfiguration realmConfig = new RealmConfiguration
                .Builder(context)
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(realmConfig);
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
        RealmResults<Auth> auths = realm.where(Auth.class).findAll();
        if (auths.size() == 0) {
            return null;
        } else {
            return auths.first().getToken();
        }
    }

    public void clearData(final Callback<Boolean> success) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Auth.class)
                        .findAll()
                        .deleteAllFromRealm();

                realm.where(Profile.class)
                        .findAll()
                        .deleteAllFromRealm();

                realm.where(Community.class)
                        .findAll()
                        .deleteAllFromRealm();

                success.apply(true);
            }
        });
    }

    public void deleteToken(final Callback<Boolean> success) {
        realm.executeTransactionAsync(new Realm.Transaction() {

            @Override
            public void execute(Realm realm) {
                RealmResults<Auth> auths = realm.where(Auth.class).findAll();
                success.apply(auths.deleteAllFromRealm());
            }

        });
    }

    public void saveProfile(final Profile profile, final Callback<Void> success) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(profile);
            }
        });
    }

    public void getProfile(final Callback<Profile> success, final Callback<Void> notFound) {
        RealmResults<Profile> profiles = realm.where(Profile.class).findAll();
        if (profiles.size() == 0) {
            notFound.apply(null);
        } else {
            success.apply(profiles.first());
        }
    }

    public void deleteProfile(final Callback<Boolean> success) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Profile> profiles = realm.where(Profile.class).findAll();
                success.apply(profiles.deleteAllFromRealm());
            }
        });
    }

    public void saveMyCommunities(final List<Community> communities, final Callback<Void> success) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(communities);
            }
        });
    }

    public List<Community> getMyCommunities() {
        return realm.where(Community.class).findAll();
    }

    public void deleteMyCommunities(final Callback<Boolean> success) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Community> communities = realm.where(Community.class).findAll();
                success.apply(communities.deleteAllFromRealm());
            }
        });
    }

}
