package fr.oqom.ouquonmange;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import fr.oqom.ouquonmange.adapters.CommunitiesAdapter;
import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.services.Config;
import fr.oqom.ouquonmange.services.OuQuOnMangeService;
import fr.oqom.ouquonmange.services.Service;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;
import fr.oqom.ouquonmange.utils.NetConnectionUtils;
import fr.oqom.ouquonmange.utils.OnCalendarSelected;
import fr.oqom.ouquonmange.utils.OnRedirectFragmentWithCommunity;
import fr.oqom.ouquonmange.utils.OnRedirectFragmentWithString;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MyCommunitiesFragment extends Fragment {

    private static final String LOG_TAG = "MyCommunitiesFgmt";

    private OnRedirectFragmentWithString onRedirectFragmentWithString;
    private OnRedirectFragmentWithCommunity onRedirectFragmentWithCommunity;
    private OnCalendarSelected onCalendarSelected;

    private SwipeRefreshLayout swipeRefreshLayout;
    //private ProgressBar progressBar;
    private CoordinatorLayout coordinatorLayout;

    private RecyclerView communitiesRecyclerView;
    private RecyclerView.Adapter communitiesAdapter;
    private RecyclerView.LayoutManager communitiesLayoutManager;

    private ArrayList<Community> communities = new ArrayList<>();

    private OuQuOnMangeService ouQuOnMangeService;

    private Subscription fetchCommunitiesSubscription;

    public static MyCommunitiesFragment newInstance() {
        MyCommunitiesFragment myCommunitiesFragment = new MyCommunitiesFragment();
        return myCommunitiesFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_communities, container, false);
        initView(view);
        initMyCommunityList(view);

        ouQuOnMangeService = Service.getInstance(getContext());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchCommunities();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onRedirectFragmentWithString = (OnRedirectFragmentWithString) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnRedirectFragmentWithString");
        }

        try {
            onRedirectFragmentWithCommunity = (OnRedirectFragmentWithCommunity) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnRedirectFragmentWithCommunity");
        }

        try {
            onCalendarSelected = (OnCalendarSelected) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCalendarSelected");
        }
    }

    private void initView(View view) {
        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinatorMainLayout);
        //progressBar = (ProgressBar) view.findViewById(R.id.progress);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(LOG_TAG, "onRefresh");
                fetchCommunities();
            }
        });
    }

    private void initMyCommunityList(View view) {
        // Creating list view
        communitiesAdapter = new CommunitiesAdapter(communities, new Callback<Community>() {
            @Override
            public void apply(Community community) {
                Log.d(LOG_TAG, "Go To CalendarActivity");
                onRedirectFragmentWithString.onRedirectFragmentWithString(Constants.CALENDAR_FRAGMENT, community.uuid);
            }
        }, new Callback<Community>() {
            @Override
            public void apply(Community community) {
                Log.d(LOG_TAG, "Go To CommunityDetailsActivity");
                onRedirectFragmentWithCommunity.onRedirectFragmentWithCommunity(Constants.COMMUNITY_DETAILS_FRAGMENT, community);
            }
        }, new Callback2<Community, Boolean>() {
            @Override
            public void apply(Community community, Boolean isChecked) {
                final Community communityFinal = community;
                final boolean isCheckedFinal = isChecked;
                Handler handler = new Handler();
                final Runnable r = new Runnable() {
                    public void run() {
                        String defaultCommunityUuid = Config.getDefaultCommunity(getContext());
                        for (Community c : communities) {
                            if (c.uuid.equals(communityFinal.uuid)) {
                                c.isDefault = isCheckedFinal;
                                if (isCheckedFinal) {
                                    if (defaultCommunityUuid != null && !defaultCommunityUuid.isEmpty()) {
                                        FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/" + defaultCommunityUuid);
                                    }

                                    Config.setDefaultCommunity(communityFinal.uuid, getContext());
                                    Log.d(LOG_TAG, "new topic = " + communityFinal.uuid + " default topic deleted" + defaultCommunityUuid);

                                    if (Config.isNotificationEnabled(getContext())) {
                                        FirebaseMessaging.getInstance().subscribeToTopic("/topics/" + communityFinal.uuid);
                                    }
                                } else {
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/" + defaultCommunityUuid);
                                    Config.setDefaultCommunity("", getContext());
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
        communitiesRecyclerView = (RecyclerView) view.findViewById(R.id.communities_list);
        communitiesLayoutManager = new LinearLayoutManager(getContext());
        communitiesRecyclerView.setHasFixedSize(true);
        communitiesRecyclerView.setLayoutManager(communitiesLayoutManager);
        communitiesRecyclerView.setAdapter(communitiesAdapter);
    }

    private void fetchCommunities() {
        if (NetConnectionUtils.isConnected(getContext())) {
            fetchCommunitiesSubscription = ouQuOnMangeService.getMyCommunities()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Community>>() {
                        @Override
                        public void call(List<Community> communityList) {
                            communities.clear();
                            communities.addAll(communityList);

                            for (Community c : communities) {
                                String defaultCommunityUuid = Config.getDefaultCommunity(getContext());
                                if (c.uuid.equals(defaultCommunityUuid)) {
                                    c.isDefault = true;
                                }
                            }

                            communitiesAdapter.notifyDataSetChanged();
                            Log.i(LOG_TAG, "Fetch Communities = " + communityList.size());
                            //progressBar.setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                            Log.e(LOG_TAG, "Fetch Communities error" + throwable.getMessage());
                            //progressBar.setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);
                            showErrorSnackBar(throwable.getMessage());
                        }
                    });
        } else {
            swipeRefreshLayout.setRefreshing(false);
            NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, getContext());
        }
    }

    private void showErrorSnackBar(CharSequence message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Constants.COMMUNITIES_LIST, this.communities);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (fetchCommunitiesSubscription != null) {
            fetchCommunitiesSubscription.unsubscribe();
        }
        super.onDestroy();
    }
}
