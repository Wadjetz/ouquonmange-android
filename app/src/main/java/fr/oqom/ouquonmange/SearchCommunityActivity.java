package fr.oqom.ouquonmange;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import fr.oqom.ouquonmange.adapters.SearchCommunitiesAdapter;
import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class SearchCommunityActivity extends BaseActivity {
    private static final String LOG_TAG = "SearchActivity";
    private List<Community> communitiesSearch = new ArrayList<>();
    private SearchCommunitiesAdapter searchCommunitiesAdapter;
    private RecyclerView communitiesRecyclerView;
    private RecyclerView.LayoutManager communitiesLayoutManager;
    private ProgressBar progressBar;
    private SearchView searchView;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_community);
        progressBar = (ProgressBar) findViewById(R.id.progress_community);
        searchView = (SearchView) findViewById(R.id.community_searched_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                communitiesSearch.clear();
                if(query.length()>0) {
                    searchCommunitiesByQuery(query);
                }else if(query.length()==0){
                    searchAllCommunities();
                }
                return true;
            }
        });
        communitiesSearch.clear();
        initNavSearch();
        toolbar.setSubtitle(R.string.search_communities);
        initCommunitySearchList();
        checkAuth();
        searchAllCommunities();

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorSearchCommunityLayout);
        snackbar = Snackbar.make(coordinatorLayout,"Error !",Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close), closeSnackBarSearchCommunity);
    }

    private View.OnClickListener closeSnackBarSearchCommunity = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            snackbar.dismiss();
        }
    };

    private void searchCommunitiesByQuery(String query) {
        hiddenVirtualKeyboard();
        api.getCommunitiesByQuery(query,new Callback<JSONArray>() {
            @Override
            public void apply(JSONArray value) {
                if(value != null) {
                    try {
                        communitiesSearch.addAll(Community.fromJson(value));
                        searchCommunitiesAdapter.notifyDataSetChanged();
                        Log.i(LOG_TAG, "Fetch Communities = " + communitiesSearch.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(LOG_TAG, "Fetch Communities = " + e.getMessage());
                    }
                    progressBar.setVisibility(View.GONE);
                }else{
                    snackbar.setText(getText(R.string.error_exception)).setActionTextColor(Color.parseColor("#D32F2F")).show();
                }
            }
        }, new Callback2<Throwable, JSONObject>() {
            @Override
            public void apply(Throwable throwable, JSONObject jsonObject) {
                if (jsonObject != null) {
                    Log.e(LOG_TAG, "Fetch Communities = " + jsonObject.toString());
                }
                Log.e(LOG_TAG, "Fetch Communities = " + throwable.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void searchAllCommunities() {
        communitiesSearch.clear();
        api.getAllCommunities(new Callback<JSONArray>() {
            @Override
            public void apply(JSONArray value) {
                if(value != null) {
                    try {
                        communitiesSearch.addAll(Community.fromJson(value));
                        searchCommunitiesAdapter.notifyDataSetChanged();
                        Log.i(LOG_TAG, "Fetch Communities = " + communitiesSearch.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(LOG_TAG, "Fetch Communities = " + e.getMessage());
                    }
                    progressBar.setVisibility(View.GONE);
                }else {
                    snackbar.setText(getText(R.string.error_exception)).setActionTextColor(Color.parseColor("#D32F2F")).show();
                }
            }
        }, new Callback2<Throwable, JSONObject>() {
            @Override
            public void apply(Throwable throwable, JSONObject jsonObject) {
                if (jsonObject != null) {
                    Log.e(LOG_TAG, "Fetch Communities = " + jsonObject.toString());
                }
                Log.e(LOG_TAG, "Fetch Communities = " + throwable.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    private void initCommunitySearchList() {
        // Creating list view
        searchCommunitiesAdapter = new SearchCommunitiesAdapter(communitiesSearch, new Callback<Community>() {
            @Override
            public void apply(Community community) {
                Log.i(LOG_TAG, "Community uuid = " + community.uuid);
                api.addMemberInCommunity(community.uuid, new Callback<JSONObject>() {
                    @Override
                    public void apply(JSONObject jsonObject) {
                        if (jsonObject != null) {
                            Log.i(LOG_TAG, "JOIN : " + jsonObject.toString());
                            snackbar.setText(R.string.member_join_community).setActionTextColor(Color.parseColor("#D32F2F")).show();
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }else {
                            snackbar.setText(getText(R.string.error_exception)).setActionTextColor(Color.parseColor("#D32F2F")).show();
                        }
                    }
                }, new Callback2<Throwable, JSONObject>() {
                    @Override
                    public void apply(Throwable throwable, JSONObject jsonObject) {
                        if (jsonObject != null) {
                            Log.e(LOG_TAG, "JOIN Error : " + jsonObject.toString());
                            String err = "";
                            try {
                                err = jsonObject.getString("error");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            snackbar.setText(err).setActionTextColor(Color.parseColor("#D32F2F")).show();

                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }else {
                            snackbar.setText(getText(R.string.error_exception)).setActionTextColor(Color.parseColor("#D32F2F")).show();
                        }
                    }
                });
            }
        });
        communitiesRecyclerView = (RecyclerView) findViewById(R.id.community_searched_list);
        communitiesLayoutManager = new LinearLayoutManager(this);
        communitiesRecyclerView.setHasFixedSize(true);
        communitiesRecyclerView.setLayoutManager(communitiesLayoutManager);
        communitiesRecyclerView.setAdapter(searchCommunitiesAdapter);
    }
}
