package fr.oqom.ouquonmange;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import fr.oqom.ouquonmange.adapters.CommunityMembersAdapter;
import fr.oqom.ouquonmange.adapters.EmptyRecyclerViewAdapter;
import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.models.CommunityDetails;
import fr.oqom.ouquonmange.models.CommunityMember;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.Message;
import fr.oqom.ouquonmange.services.OuQuOnMangeService;
import fr.oqom.ouquonmange.services.Service;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.NetConnectionUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class CommunityDetailsFragment extends Fragment {

    private static final String LOG_TAG = "CommunityDetailsFgmt";

    private OuQuOnMangeService ouQuOnMangeService;

    private CommunityDetails communityDetails;
    private Community community;

    private TextView communityName, communityDescription, communityType;
    private ProgressBar progressBar;
    //private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<CommunityMember> members = new ArrayList<>();
    private RecyclerView.LayoutManager membersLayoutManager;
    private RecyclerView membersRecyclerView;
    private RecyclerView.Adapter membersAdapter;
    private RecyclerView.Adapter membersEmptyAdapter;

    public static CommunityDetailsFragment newInstance(Community community) {
        CommunityDetailsFragment communityDetailsFragment = new CommunityDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.COMMUNITY, community);
        communityDetailsFragment.setArguments(args);
        return communityDetailsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        community = getArguments().getParcelable(Constants.COMMUNITY);

        View view = inflater.inflate(R.layout.fragment_community_details, container, false);
        initView(view);

        Log.d(LOG_TAG, "onCreateView");

        ouQuOnMangeService = Service.getInstance(getContext());

        setView();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated");
        fetchCommunityDetails();
    }

    private void initView(View view) {
        communityName = (TextView) view.findViewById(R.id.community_name);
        communityDescription = (TextView) view.findViewById(R.id.community_description);
        communityType = (TextView) view.findViewById(R.id.community_type);
        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        //swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        membersAdapter = new CommunityMembersAdapter(this.members, getContext(), callbackAcceptMember, callbackRefuseMember);

        membersEmptyAdapter = new EmptyRecyclerViewAdapter();

        membersRecyclerView = (RecyclerView) view.findViewById(R.id.communities_members_list);
        membersLayoutManager = new LinearLayoutManager(getContext());
        membersRecyclerView.setHasFixedSize(true);
        membersRecyclerView.setLayoutManager(membersLayoutManager);
        if(members.size() > 0) {
            membersRecyclerView.setAdapter(membersAdapter);
        }else{
            membersRecyclerView.setAdapter(membersEmptyAdapter);
        }
    }

    private Callback<CommunityMember> callbackAcceptMember = new Callback<CommunityMember>() {
        @Override
        public void apply(CommunityMember communityMember) {
            Log.d(LOG_TAG, "Community Member Accepting " + communityMember);
        }
    };

    private Callback<CommunityMember> callbackRefuseMember = new Callback<CommunityMember>() {
        @Override
        public void apply(CommunityMember communityMember) {
            Log.d(LOG_TAG, "Community Member Deleting " + communityMember);
        }
    };


    private void setView() {
        communityName.setText(community.name);
        communityDescription.setText(community.description);
        communityType.setText(community.getCommunityType(getContext()));

        /*
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(LOG_TAG, "onRefresh");
                fetchCommunityDetails();
            }
        });
        */
    }

    private void fetchCommunityDetails() {
        Log.d(LOG_TAG, "fetch Community Details");
        if (NetConnectionUtils.isConnected(getContext())) {
            progressBar.setVisibility(View.VISIBLE);
            ouQuOnMangeService.getCommunityDetails(community.uuid)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<CommunityDetails>() {
                        @Override
                        public void call(CommunityDetails details) {
                            communityDetails = details;
                            Log.d(LOG_TAG, "fetched Community Details ok " + communityDetails);
                            members.clear();
                            if(communityDetails.members.size() > 0) {
                                members.addAll(communityDetails.members);
                                for (CommunityMember cm : members) {
                                    Log.d(LOG_TAG, "Set role member : " + cm.uuid + " username : " + cm.username);
                                }
                                membersAdapter.notifyDataSetChanged();
                                membersRecyclerView.setAdapter(membersAdapter);
                            }else{
                                membersEmptyAdapter.notifyDataSetChanged();
                                membersRecyclerView.setAdapter(membersEmptyAdapter);
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                            //swipeRefreshLayout.setRefreshing(false);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.d(LOG_TAG, "fetched Community Details error " + throwable.getMessage());
                            progressBar.setVisibility(View.INVISIBLE);
                            //swipeRefreshLayout.setRefreshing(false);
                        }
                    });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.COMMUNITY, community);
    }
}
