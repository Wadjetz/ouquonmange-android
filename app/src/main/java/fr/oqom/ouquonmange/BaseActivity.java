package fr.oqom.ouquonmange;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;

import fr.oqom.ouquonmange.repositories.Repository;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.Profile;
import fr.oqom.ouquonmange.services.Config;
import fr.oqom.ouquonmange.services.OuQuOnMangeService;
import fr.oqom.ouquonmange.services.Service;
import fr.oqom.ouquonmange.utils.Callback;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = "BaseActivity";

    protected OuQuOnMangeService ouQuOnMangeService;
    protected Repository repository;

    protected NavigationView navigationView;
    protected DrawerLayout drawer;
    protected Toolbar toolbar;
    protected View navigationViewHeader;
    protected TextView navigationViewHeaderUserName;
    protected TextView navigationViewHeaderEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new Repository(getApplicationContext());
        ouQuOnMangeService = Service.getInstance(getApplicationContext());
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        switch (id) {
            case R.id.nav_calendar:
                String defaultCommunityUuid = Config.getDefaultCommunity(getApplicationContext());
                if (defaultCommunityUuid != null && !defaultCommunityUuid.isEmpty()) {
                    Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                    intent.putExtra(Constants.COMMUNITY_UUID, defaultCommunityUuid);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.nav_communities:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra(Constants.FROM_MENU, Constants.FROM_MENU);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_search_communities:
                startActivity(new Intent(getApplicationContext(), SearchCommunityActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                finish();
                break;
            case R.id.nav_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.logout_confirm_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes_message, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                repository.clearData(new Callback<Boolean>() {
                                    @Override
                                    public void apply(Boolean isDeleted) {
                                        Config.setDefaultCommunity(null, getApplicationContext());
                                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                        finish();
                                    }
                                });
                            }
                        })
                        .setNegativeButton(R.string.no_message, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
                break;
        }

        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    protected void initNav() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationViewHeader = navigationView.inflateHeaderView(R.layout.nav_header_main);
        navigationViewHeaderUserName = (TextView) navigationViewHeader.findViewById(R.id.profile_name);
        navigationViewHeaderUserName.setText(getText(R.string.app_name));
        navigationViewHeaderEmail = (TextView) navigationViewHeader.findViewById(R.id.profile_email);
        navigationViewHeaderEmail.setText(getText(R.string.app_name));

        initProfile();
    }

    private void initProfile() {
        repository.getProfile(new Callback<Profile>() {
            @Override
            public void apply(Profile profile) {
                Log.d(LOG_TAG, "Get profile from repository = " + profile);
                navigationViewHeaderUserName.setText(profile.username);
                navigationViewHeaderEmail.setText(profile.email);
            }
        }, new Callback<Void>() {
            @Override
            public void apply(Void aVoid) {
                ouQuOnMangeService.getProfile()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Profile>() {
                            @Override
                            public void call(final Profile profile) {
                                Log.d(LOG_TAG, "Fetch profile from API = " + profile);
                                navigationViewHeaderUserName.setText(profile.username);
                                navigationViewHeaderEmail.setText(profile.email);
                                repository.saveProfile(profile, new Callback<Void>() {
                                    @Override
                                    public void apply(Void aVoid) {
                                        Log.d(LOG_TAG, "Save Profile profile = " + profile);
                                    }
                                });
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.d(LOG_TAG, throwable.getMessage());
                            }
                        });
            }
        });
    }

    protected void checkAuth() {
        if (repository.getToken() == null) {
            Intent intentLogin = new Intent(this, LoginActivity.class);
            startActivity(intentLogin);
            finish();
        }
    }

    protected void hiddenVirtualKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        if(getCurrentFocus().getWindowToken() != null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(LOG_TAG, "onSharedPreferenceChanged key = " + key);
        if (key.equals(getString(R.string.key_notifications_enabled))) {
            String defaultCommunityUuid = Config.getDefaultCommunity(getApplicationContext());
            Log.d(LOG_TAG, "onSharedPreferenceChanged defaultCommunityUuid = " + defaultCommunityUuid);
            if (sharedPreferences.getBoolean(getString(R.string.key_notifications_enabled), true)) {
                if (defaultCommunityUuid != null) {
                    Log.d(LOG_TAG, "onSharedPreferenceChanged subscribeToTopic = " + defaultCommunityUuid);
                    FirebaseMessaging.getInstance().subscribeToTopic("/topics/" + defaultCommunityUuid);
                }
            } else {
                if (defaultCommunityUuid != null) {
                    Log.d(LOG_TAG, "onSharedPreferenceChanged unsubscribeFromTopic = " + defaultCommunityUuid);
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/" + defaultCommunityUuid);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).unregisterOnSharedPreferenceChangeListener(this);
    }
}
