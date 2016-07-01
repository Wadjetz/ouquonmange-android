package fr.oqom.ouquonmange;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import fr.oqom.ouquonmange.models.AuthRepository;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.Profile;
import fr.oqom.ouquonmange.services.Config;
import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.utils.Callback;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = "BaseActivity";

    protected OuquonmangeApi api;
    protected AuthRepository authRepository;

    protected NavigationView navigationView;
    protected DrawerLayout drawer;
    protected Toolbar toolbar;
    protected View navigationViewHeader;
    protected TextView navigationViewHeaderUserName;
    protected TextView navigationViewHeaderEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = new OuquonmangeApi(getApplicationContext());
        authRepository = new AuthRepository(getApplicationContext());
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
                                authRepository.deleteToken(new Callback<Void>() {
                                    @Override
                                    public void apply(Void aVoid) {
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
        api.getProfile()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Profile>() {
                    @Override
                    public void call(Profile profile) {
                        navigationViewHeaderUserName.setText(profile.username);
                        navigationViewHeaderEmail.setText(profile.email);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(LOG_TAG, throwable.getMessage());
                    }
                });
    }

    protected void checkAuth() {
        if(authRepository.getToken() == null){
            Intent intentLogin = new Intent(this, LoginActivity.class);
            startActivity(intentLogin);
            finish();
        }
    }

    protected void initNavSearch() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_search);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_search);
        navigationView = (NavigationView) findViewById(R.id.nav_view_search);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    protected void hiddenVirtualKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

    }

    public boolean checkConnection(Context context){
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

        if (activeNetworkInfo != null) { // connected to the internet
            Toast.makeText(context, activeNetworkInfo.getTypeName(), Toast.LENGTH_SHORT).show();

            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI ) {
                // connected to wifi
                if(activeNetworkInfo.isAvailable() && activeNetworkInfo.isConnected()) {
                    Log.i(LOG_TAG,"type wifi");
                    return true;
                }

            } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                if(activeNetworkInfo.isAvailable() && activeNetworkInfo.isConnected()) {
                    Log.i(LOG_TAG, "type data");
                    return true;
                }
            }
        }
        return false;
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
