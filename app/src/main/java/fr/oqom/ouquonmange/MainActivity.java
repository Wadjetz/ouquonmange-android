package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.oqom.ouquonmange.adapters.CommunitiesAdapter;
import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.GSMToken;
import fr.oqom.ouquonmange.models.Message;
import fr.oqom.ouquonmange.services.Config;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;
import fr.oqom.ouquonmange.utils.NetConnectionUtils;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends BaseActivity {

    private static final String LOG_TAG = "MainActivity";

    @BindView(R.id.progress)              ProgressBar progressBar;
    @BindView(R.id.swipeRefreshLayout)    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.coordinatorMainLayout) CoordinatorLayout coordinatorLayout;

    private RecyclerView communitiesRecyclerView;
    private RecyclerView.Adapter communitiesAdapter;
    private RecyclerView.LayoutManager communitiesLayoutManager;

    private ArrayList<Community> communities = new ArrayList<>();

    private Subscription fetchCommunitiesSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String fromMenu = intent.getStringExtra(Constants.FROM_MENU);

        String defaultCommunityUuid = Config.getDefaultCommunity(getApplicationContext());

        Log.d(LOG_TAG, "fromMenu = " + fromMenu + " defaultCommunityUuid = " + defaultCommunityUuid);

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
            if (NetConnectionUtils.isConnected(getApplicationContext())) {
                fetchCommunities();
                checkGcm();
            } else {
                Log.e(LOG_TAG, "NOT INTERNET");
                NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
            }
        } else {
            this.communities = savedInstanceState.getParcelableArrayList(Constants.COMMUNITIES_LIST);
            progressBar.setVisibility(View.GONE);
        }

        initCommunityList();

    }

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
        if (NetConnectionUtils.isConnected(getApplicationContext())) {
            fetchCommunitiesSubscription = ouQuOnMangeService.getMyCommunities()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Community>>() {
                        @Override
                        public void call(List<Community> communityList) {
                            communities.addAll(communityList);

                            for (Community c : communities) {
                                String defaultCommunityUuid = Config.getDefaultCommunity(getApplicationContext());
                                if (c.uuid.equals(defaultCommunityUuid)) {
                                    c.isDefault = true;
                                }
                            }

                            communitiesAdapter.notifyDataSetChanged();
                            Log.i(LOG_TAG, "Fetch Communities = " + communityList.size());
                            progressBar.setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                            Log.e(LOG_TAG, "Fetch Communities error" + throwable.getMessage());
                            progressBar.setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);
                            showErrorSnackBar(throwable.getMessage());
                        }
                    });
        } else {
            NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
        }
    }

    private void initCommunityList() {
        // Creating list view
        communitiesAdapter = new CommunitiesAdapter(communities, new Callback<Community>() {
            @Override
            public void apply(Community community) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                intent.putExtra(Constants.COMMUNITY_UUID, community.uuid);
                startActivity(intent);
                finish();
            }
        }, new Callback<Community>() {
            @Override
            public void apply(Community community) {
                Intent intent = new Intent(getApplicationContext(), CommunityDetailsActivity.class);
                intent.putExtra(Constants.COMMUNITY, community);
                startActivity(intent);
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
                        for (Community c : communities) {
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
            if (NetConnectionUtils.isConnected(getApplicationContext())) {
                ouQuOnMangeService.addGcmToken(new GSMToken(gcmToken))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Message>() {
                            @Override
                            public void call(Message message) {
                                Log.d(LOG_TAG, "addGcmToken ok = " +  message);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.e(LOG_TAG, "addGcmToken error = " + throwable.getMessage());
                            }
                        });
            } else {
                NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
            }
        }
        Log.d(LOG_TAG, "GCM Token " + gcmToken);
    }

    private void showErrorSnackBar(CharSequence message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onStop() {
        if (fetchCommunitiesSubscription != null) {
            fetchCommunitiesSubscription.unsubscribe();
        }
        super.onStop();
    }
}

