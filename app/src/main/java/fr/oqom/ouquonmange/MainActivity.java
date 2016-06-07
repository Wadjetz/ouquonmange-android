package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.oqom.ouquonmange.adapters.CommunitiesAdapter;
import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class MainActivity extends BaseActivity {

    private static final String LOG_TAG = "MainActivity";

    private ProgressBar progressBar;

    private RecyclerView communitiesRecyclerView;
    private RecyclerView.Adapter communitiesAdapter;
    private RecyclerView.LayoutManager communitiesLayoutManager;

    private List<Community> communities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progress);

        initNav();
        toolbar.setSubtitle(R.string.my_communities);
        initCommunityList();
        initFloatingButton();
        checkAuth();
        checkGcm();
        fetchCommunities();
    }

    private void initCommunityList() {
        // Creating list view
        communitiesAdapter = new CommunitiesAdapter(communities, new Callback<Community>() {
            @Override
            public void apply(Community community) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                intent.putExtra(Constants.COMMUNITY_UUID, community.uuid);
                startActivity(intent);
            }
        });
        communitiesRecyclerView = (RecyclerView) findViewById(R.id.communities_list);
        communitiesLayoutManager = new LinearLayoutManager(this);
        communitiesRecyclerView.setHasFixedSize(true);
        communitiesRecyclerView.setLayoutManager(communitiesLayoutManager);
        communitiesRecyclerView.setAdapter(communitiesAdapter);
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

    private void fetchCommunities() {
        api.getCommunities(new Callback<JSONArray>() {
            @Override
            public void apply(JSONArray value) {
                try {
                    communities.addAll(Community.fromJson(value));
                    communitiesAdapter.notifyDataSetChanged();
                    Log.i(LOG_TAG, "Fetch Communities = " + communities.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Fetch Communities = " + e.getMessage());
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        }, new Callback2<Throwable, JSONObject>() {
            @Override
            public void apply(Throwable throwable, JSONObject jsonObject) {
                if (jsonObject != null) {
                    Log.e(LOG_TAG, "Fetch Communities = " + jsonObject.toString());
                }
                Log.e(LOG_TAG, "Fetch Communities = " + throwable.getMessage());
                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}

