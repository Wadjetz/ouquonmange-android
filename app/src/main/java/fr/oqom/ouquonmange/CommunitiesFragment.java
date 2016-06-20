package fr.oqom.ouquonmange;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.oqom.ouquonmange.adapters.CommunitiesAdapter;
import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class CommunitiesFragment extends Fragment {

    private static final String LOG_TAG = "CommunitiesFragment";

    private RecyclerView communitiesRecyclerView;
    private RecyclerView.Adapter communitiesAdapter;
    private RecyclerView.LayoutManager communitiesLayoutManager;

    private ProgressBar progressBar;

    private List<Community> communities = new ArrayList<>();

    protected OuquonmangeApi api;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(LOG_TAG, "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");

        View layoutInflater = inflater.inflate(R.layout.fragment_communities, container, false);

        api = new OuquonmangeApi(getContext());

        progressBar = (ProgressBar) layoutInflater.findViewById(R.id.progress);

        initCommunityList(layoutInflater);
        fetchCommunities();

        return layoutInflater;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(LOG_TAG, "onDestroyView");
        communities.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(LOG_TAG, "onDetach");
    }

    private void initCommunityList(View v) {
        // Creating list view
        communitiesAdapter = new CommunitiesAdapter(communities, new Callback<Community>() {
            @Override
            public void apply(Community community) {
                Intent intent = new Intent(getContext(), CalendarActivity.class);
                intent.putExtra(Constants.COMMUNITY_UUID, community.uuid);
                startActivity(intent);
            }
        });
        communitiesRecyclerView = (RecyclerView) v.findViewById(R.id.communities_list_2);
        communitiesLayoutManager = new LinearLayoutManager(getContext());
        communitiesRecyclerView.setHasFixedSize(true);
        communitiesRecyclerView.setLayoutManager(communitiesLayoutManager);
        communitiesRecyclerView.setAdapter(communitiesAdapter);
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
                }
                progressBar.setVisibility(View.GONE);
            }
        }, new Callback2<Throwable, JSONObject>() {
            @Override
            public void apply(Throwable throwable, JSONObject jsonObject) {
                if (jsonObject != null) {
                    Log.e(LOG_TAG, "Fetch Communities = " + jsonObject.toString());
                }
                throwable.printStackTrace();
                Log.e(LOG_TAG, "Fetch Communities = " + throwable.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
