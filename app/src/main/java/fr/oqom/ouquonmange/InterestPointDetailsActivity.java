package fr.oqom.ouquonmange;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import fr.oqom.ouquonmange.adapters.MembersAdapter;
import fr.oqom.ouquonmange.models.CommunityMember;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.InterestPoint;
import fr.oqom.ouquonmange.models.InterestPointDetails;
import fr.oqom.ouquonmange.services.OuQuOnMangeService;
import fr.oqom.ouquonmange.services.Service;
import fr.oqom.ouquonmange.utils.NetConnectionUtils;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class InterestPointDetailsActivity extends BaseActivity {

    private static final String LOG_TAG = "InterestPtsActivityDet";

    private TextView interestPointName;
    private TextView interestPointAddress;
    private TextView interestPointNameMembers;
    private RecyclerView.LayoutManager membersLayoutManager;
    private String eventUuid;
    private String communityUuid;
    private String interestPointId;
    private ArrayList<CommunityMember> members = new ArrayList<>();

    private ProgressBar progressBar;
    private InterestPoint interestPoint;
    private RecyclerView membersRecyclerView;
    private RecyclerView.Adapter membersAdapter;
    private Snackbar snackbar;
    private CoordinatorLayout coordinatorLayout;

    private OuQuOnMangeService ouQuOnMangeService;

    private InterestPointDetails interestPointDetails;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_point_details);
        Intent intent = getIntent();
        eventUuid = intent.getStringExtra(Constants.EVENT_UUID);
        communityUuid = intent.getStringExtra(Constants.COMMUNITY_UUID);
        interestPointId = intent.getStringExtra(Constants.INTEREST_POINT_ID);
        interestPoint = intent.getParcelableExtra(Constants.INTEREST_POINT);

        Log.d(LOG_TAG, "onCreate communityUuid=" + communityUuid + " eventUuid=" + eventUuid + " interestPointId=" + interestPointId + " interestPoint=" + interestPoint);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorInterestPointsDetailsLayout);
        snackbar = Snackbar.make(coordinatorLayout, "Error !", Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close), closeSnackBarLogin);

        ouQuOnMangeService = Service.getInstance(getApplicationContext());

        initView();
        initNav();
        initText();
        checkAuth();

        if (savedInstanceState == null) {
            fetchInterestPointDetails(communityUuid, eventUuid, interestPoint);
        } else {
            this.members = savedInstanceState.getParcelableArrayList(Constants.MEMBERS_LIST);
            progressBar.setVisibility(View.GONE);
            Log.d(LOG_TAG, "onCreate savedInstanceState = " + this.members.size());
        }

        InitMembersList();
    }

    private void initText() {
        interestPointName = (TextView) findViewById(R.id.interest_point_detail_name);
        interestPointName.setText(interestPoint.name);
        interestPointAddress = (TextView) findViewById(R.id.interest_point_detail_address);
        interestPointAddress.setText(interestPoint.address);
        interestPointNameMembers = (TextView) findViewById(R.id.interest_point_members_name);
        interestPointNameMembers.setText(Constants.NAME_MEMBERS);
    }

    private void InitMembersList() {

        membersAdapter = new MembersAdapter(getApplicationContext(), this.members);

        membersRecyclerView = (RecyclerView) findViewById(R.id.members_list);
        membersLayoutManager = new LinearLayoutManager(this);
        membersRecyclerView.setHasFixedSize(true);
        membersRecyclerView.setLayoutManager(membersLayoutManager);
        membersRecyclerView.setAdapter(membersAdapter);

    }

    private View.OnClickListener closeSnackBarLogin = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            snackbar.dismiss();
        }
    };

    private void initView() {
        progressBar = (ProgressBar) findViewById(R.id.progress_interest_point_detail);
        interestPointName = (TextView) findViewById(R.id.interest_point_detail_name);
        interestPointAddress = (TextView) findViewById(R.id.interest_point_detail_address);
    }

    private void fetchInterestPointDetails(String communityUuid, String eventUuid, InterestPoint interestPoint) {
        if (NetConnectionUtils.isConnected(getApplicationContext())) {
            ouQuOnMangeService.getInterestPointDetails(communityUuid, eventUuid, interestPoint.apiId, interestPoint.type)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<InterestPointDetails>() {
                        @Override
                        public void call(InterestPointDetails ipd) {
                            Log.i(LOG_TAG, "Fetch InterestPointDetails = ipd=" + ipd);
                            interestPointDetails = ipd;
                            members.clear();
                            members.addAll(ipd.members);
                            membersAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                            if (throwable instanceof HttpException) {
                                HttpException response = (HttpException) throwable;
                                switch (response.code()) {
                                    case 400:
                                        Log.e(LOG_TAG, "Login 400 Bad Request");
                                        snackbar.setText(R.string.login_error).setActionTextColor(Color.parseColor("#D32F2F")).show();
                                        break;
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                            } else {
                                snackbar.setText(throwable.getMessage()).setActionTextColor(Color.parseColor("#D32F2F")).show();
                            }
                        }
                    });


        } else {
            NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(Constants.EVENT_UUID, eventUuid);
        outState.putString(Constants.COMMUNITY_UUID, communityUuid);
        outState.putString(Constants.INTEREST_POINT_ID, interestPointId);
        outState.putParcelable(Constants.INTEREST_POINT, interestPoint);
        outState.putParcelableArrayList(Constants.MEMBERS_LIST, this.members);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        eventUuid = savedInstanceState.getString(Constants.EVENT_UUID);
        communityUuid = savedInstanceState.getString(Constants.COMMUNITY_UUID);
        interestPointId = savedInstanceState.getString(Constants.INTEREST_POINT_ID);
        interestPoint = savedInstanceState.getParcelable(Constants.INTEREST_POINT);
    }
}
