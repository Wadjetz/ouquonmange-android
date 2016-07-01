package fr.oqom.ouquonmange;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.oqom.ouquonmange.adapters.CommunitiesAdapter;
import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.services.Config;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class MainActivity extends BaseActivity {

    private static final String LOG_TAG = "MainActivity";

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView communitiesRecyclerView;
    private RecyclerView.Adapter communitiesAdapter;
    private RecyclerView.LayoutManager communitiesLayoutManager;

    private ArrayList<Community> communities = new ArrayList<>();
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String fromMenu = intent.getStringExtra(Constants.FROM_MENU);

        String defaultCommunityUuid = Config.getDefaultCommunity(getApplicationContext());

        Log.d(LOG_TAG, "fromMenu = " + fromMenu + " defaultCommunityUuid = " + defaultCommunityUuid);



        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorMainLayout);
        snackbar = Snackbar.make(coordinatorLayout,"Error !",Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close), closeSnackBarMain);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        if (defaultCommunityUuid != null && !defaultCommunityUuid.isEmpty() && !Constants.FROM_MENU.equals(fromMenu)) {
            if (Config.isNotificationEnabled(getApplicationContext())) {
                FirebaseMessaging.getInstance().subscribeToTopic("/topics/" + defaultCommunityUuid);
            }
            Intent i = new Intent(getApplicationContext(), CalendarActivity.class);
            i.putExtra(Constants.COMMUNITY_UUID, defaultCommunityUuid);
            startActivity(i);
            finish();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(LOG_TAG, "onRefresh");
                communities.clear();
                fetchCommunities();
            }
        });

        initNav();
        toolbar.setSubtitle(R.string.my_communities);
        initFloatingButton();

        if (savedInstanceState == null) {
            checkAuth();
            if(checkConnection(getApplicationContext())) {
                fetchCommunities();
                checkGcm();
            }else {
                Log.e(LOG_TAG, "NOT INTERNET");
                refreshSnackBar();
            }
        } else {
            this.communities = savedInstanceState.getParcelableArrayList(Constants.COMMUNITIES_LIST);
            progressBar.setVisibility(View.GONE);
        }

        initCommunityList();

    }


    private View.OnClickListener closeSnackBarMain = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            snackbar.dismiss();
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Constants.COMMUNITIES_LIST, this.communities);
        super.onSaveInstanceState(outState);
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
        if (checkConnection(getApplicationContext())) {
            api.getCommunities(new Callback<JSONArray>() {
                @Override
                public void apply(JSONArray value) {
                    if (value != null) {
                        try {
                            communities.addAll(Community.fromJson(value));

                            for (Community c : communities) {
                                String defaultCommunityUuid = Config.getDefaultCommunity(getApplicationContext());
                                if (c.uuid.equals(defaultCommunityUuid)) {
                                    c.isDefault = true;
                                }
                            }

                            communitiesAdapter.notifyDataSetChanged();
                            Log.i(LOG_TAG, "Fetch Communities = " + communities.size());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(LOG_TAG, "Fetch Communities = " + e.getMessage());
                            snackbar.setText(R.string.error_exception).setActionTextColor(Color.parseColor("#D32F2F")).show();
                        }
                    } else {
                        snackbar.setText(getText(R.string.error_exception)).setActionTextColor(Color.parseColor("#D32F2F")).show();
                    }
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, new Callback2<Throwable, JSONObject>() {
                @Override
                public void apply(Throwable throwable, JSONObject jsonObject) {
                    if (jsonObject != null) {
                        Log.e(LOG_TAG, "Fetch Communities = " + jsonObject.toString());
                        String err = "";
                        try {
                            err = jsonObject.getString("error");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        snackbar.setText(err).setActionTextColor(Color.parseColor("#D32F2F")).show();
                    }
                    Log.e(LOG_TAG, "Fetch Communities = " + throwable.getMessage());
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }else{
            refreshSnackBar();
        }
    }

    private void initCommunityList() {
        // Creating list view
        communitiesAdapter = new CommunitiesAdapter(communities, getApplicationContext(), new Callback<Community>() {
            @Override
            public void apply(Community community) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                intent.putExtra(Constants.COMMUNITY_UUID, community.uuid);
                startActivity(intent);
                finish();
            }
        }, new Callback2<Community, Boolean>() {
            @Override
            public void apply(Community community, Boolean isChecked) {
                final Community communityFinal = community;
                final boolean isCheckedFinal = isChecked;
                Handler handler = new Handler();
                final Runnable r = new Runnable() {
                    public void run() {
                        String defaultCommunityUuid = Config.getDefaultCommunity(getApplicationContext());
                        for(Community c: communities) {
                            if (c.uuid.equals(communityFinal.uuid)) {
                                c.isDefault = isCheckedFinal;
                                if (isCheckedFinal) {
                                    if (defaultCommunityUuid != null && !defaultCommunityUuid.isEmpty()) {
                                        FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/" + defaultCommunityUuid);
                                    }

                                    Config.setDefaultCommunity(communityFinal.uuid, getApplicationContext());
                                    Log.d(LOG_TAG, "new topic = " + communityFinal.uuid + " default topic deleted" + defaultCommunityUuid);

                                    if (Config.isNotificationEnabled(getApplicationContext())) {
                                        FirebaseMessaging.getInstance().subscribeToTopic("/topics/" + communityFinal.uuid);
                                    }
                                } else {
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/" + defaultCommunityUuid);
                                    Config.setDefaultCommunity("", getApplicationContext());
                                }
                            } else {
                                c.isDefault = false;
                            }
                        }

                        communitiesAdapter.notifyDataSetChanged();

                    }
                };
                handler.post(r);
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
            }
        });
    }

    private void checkGcm() {
        String gcmToken = FirebaseInstanceId.getInstance().getToken();
        if (gcmToken != null) {
            if(checkConnection(getApplicationContext())) {
                api.addGcmToken(gcmToken, new Callback<JSONObject>() {
                    @Override
                    public void apply(JSONObject jsonObject) {
                        if (jsonObject != null) {
                            Log.e(LOG_TAG, jsonObject.toString());
                        }
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
            }else{
              refreshSnackBar();
            }
        }

        Log.d(LOG_TAG, "GCM Token " + gcmToken);
    }

    public void refreshSnackBar(){
        snackbar.setText(R.string.no_internet)
                .setActionTextColor(Color.parseColor("#D32F2F"))
                .setDuration(Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.refresh, refreshSnackBarMain)
                .show();
        progressBar.setVisibility(View.GONE);

    }
    private View.OnClickListener refreshSnackBarMain = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = getIntent();
            intent.putExtra(Constants.FROM_MENU, Constants.FROM_MENU);
            finish();
            startActivity(intent);
        }
    };

}

