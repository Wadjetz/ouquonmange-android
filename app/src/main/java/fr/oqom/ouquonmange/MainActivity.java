package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class MainActivity extends BaseActivity {

    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initNav();
        toolbar.setSubtitle(R.string.my_communities);
        initFloatingButton();
        checkAuth();
        checkGcm();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        CommunitiesFragment communitiesFragment = new CommunitiesFragment();
        fragmentTransaction.replace(R.id.fragment_container, communitiesFragment);
        fragmentTransaction.commit();
    }

    private void initFloatingButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.create_community);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CreateCommunityActivity.class));
                finish();
            }
        });
    }

    private void checkGcm() {
        String gcmToken = FirebaseInstanceId.getInstance().getToken();
        if (gcmToken != null) {
            api.addGcmToken(gcmToken, new Callback<JSONObject>() {
                @Override
                public void apply(JSONObject jsonObject) {
                    Log.e(LOG_TAG, jsonObject.toString());
                }
            }, new Callback2<Throwable, JSONObject>() {
                @Override
                public void apply(Throwable throwable, JSONObject jsonObject) {
                    if (jsonObject != null) {
                        Log.e(LOG_TAG, jsonObject.toString());
                    }
                    Log.e(LOG_TAG, throwable.getMessage());
                }
            });
        }

        Log.d(LOG_TAG, "GCM Token " + gcmToken);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_search:
                startActivity(new Intent(getApplicationContext(), SearchCommunityActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

