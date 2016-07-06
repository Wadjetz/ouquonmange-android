package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.oqom.ouquonmange.adapters.SearchCommunitiesAdapter;
import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.User;
import fr.oqom.ouquonmange.utils.Callback;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class SearchCommunityActivity extends BaseActivity {

    private static final String LOG_TAG = "SearchActivity";

    @BindView(R.id.progress_community) ProgressBar progressBar;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.coordinatorSearchCommunityLayout) CoordinatorLayout coordinatorLayout;

    private List<Community> communitiesSearch = new ArrayList<>();
    private SearchCommunitiesAdapter searchCommunitiesAdapter;
    private String query;

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextSubmit(String query) {
            searchView.clearFocus();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String q) {
            query = q;
            if(query.isEmpty()) {
                searchAllCommunities();
            } else {
                searchCommunitiesByQuery(query);
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_community);
        ButterKnife.bind(this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(LOG_TAG, "onRefresh");
                if(query == null || query.isEmpty()) {
                    searchAllCommunities();
                } else {
                    searchCommunitiesByQuery(query);
                }
            }
        });

        initNav();
        toolbar.setSubtitle(R.string.search_communities);
        initCommunitySearchList();
        checkAuth();
        searchAllCommunities();
    }

    private void searchCommunitiesByQuery(String query) {
        ouQuOnMangeService.searchCommunities(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Community>>() {
                    @Override
                    public void call(List<Community> communityList) {
                        Log.i(LOG_TAG, "Fetch Communities = " + communitiesSearch.size());
                        communitiesSearch.clear();
                        communitiesSearch.addAll(communityList);
                        searchCommunitiesAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(LOG_TAG, "Fetch Communities = " + throwable.getMessage());
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void searchAllCommunities() {
        ouQuOnMangeService.searchCommunities()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Community>>() {
                    @Override
                    public void call(List<Community> communityList) {
                        Log.i(LOG_TAG, "Fetch Communities = " + communitiesSearch.size());
                        communitiesSearch.clear();
                        communitiesSearch.addAll(communityList);
                        searchCommunitiesAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(LOG_TAG, "Fetch Communities = " + throwable.getMessage());
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void initCommunitySearchList() {
        searchCommunitiesAdapter = new SearchCommunitiesAdapter(communitiesSearch, getApplicationContext(), new Callback<Community>() {
            @Override
            public void apply(final Community community) {
                Log.i(LOG_TAG, "Community uuid = " + community.uuid);

                ouQuOnMangeService.joinCommunity(community.uuid)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<User>() {
                            @Override
                            public void call(User user) {
                                Log.i(LOG_TAG, "Community Join ok : " + user);
                                showErrorSnackBar(getText(R.string.member_join_community));
                                int i = communitiesSearch.indexOf(community);
                                communitiesSearch.remove(i);
                                searchCommunitiesAdapter.notifyDataSetChanged();
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                                if (throwable instanceof HttpException) {
                                    HttpException response = (HttpException) throwable;
                                    switch (response.code()) {
                                        case 400:
                                            Log.e(LOG_TAG, "Join Community 400 Bad Request");
                                            showErrorSnackBar(getText(R.string.error_invalid_fields));
                                            break;
                                        case 409:
                                            Log.e(LOG_TAG, "Join Community 409 Conflict Community Already Join");
                                            showErrorSnackBar(getText(R.string.error_already_join));
                                    }
                                } else {
                                    showErrorSnackBar(throwable.getMessage());
                                }
                            }
                        });
            }
        }, new Callback<Community>() {
            @Override
            public void apply(Community community) {
                Intent intent = new Intent(getApplicationContext(), CommunityDetailsActivity.class);
                intent.putExtra(Constants.COMMUNITY, community);
                startActivity(intent);
            }
        });
        RecyclerView communitiesRecyclerView = (RecyclerView) findViewById(R.id.community_searched_list);
        communitiesRecyclerView.setHasFixedSize(true);
        communitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        communitiesRecyclerView.setAdapter(searchCommunitiesAdapter);
    }

    private void showErrorSnackBar(CharSequence message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_search_communities_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }

        if (searchView != null) {
            searchView.setOnQueryTextListener(queryTextListener);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Log.d(LOG_TAG, "action_search");
                return false;
            default:
                break;
        }
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onOptionsItemSelected(item);
    }
}
