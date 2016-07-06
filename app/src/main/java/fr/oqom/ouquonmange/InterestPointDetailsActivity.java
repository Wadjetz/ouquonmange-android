package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
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

public class InterestPointDetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "InterestPtsActivityDet";

    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.backdrop) ImageView backdropImageView;
    @BindView(R.id.interest_point_detail_name) TextView interestPointName;
    @BindView(R.id.interest_point_detail_address) TextView interestPointAddress;
    @BindView(R.id.members_list) RecyclerView membersRecyclerView;
    @BindView(R.id.coordinatorMainLayout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private String eventUuid;
    private String communityUuid;
    private String interestPointId;
    private ArrayList<CommunityMember> members = new ArrayList<>();
    private InterestPoint interestPoint;
    private InterestPointDetails interestPointDetails;
    private MembersAdapter membersAdapter;

    private OuQuOnMangeService ouQuOnMangeService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_point_details);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        eventUuid = intent.getStringExtra(Constants.EVENT_UUID);
        communityUuid = intent.getStringExtra(Constants.COMMUNITY_UUID);
        interestPointId = intent.getStringExtra(Constants.INTEREST_POINT_ID);
        interestPoint = intent.getParcelableExtra(Constants.INTEREST_POINT);

        Log.d(LOG_TAG, "onCreate communityUuid=" + communityUuid + " eventUuid=" + eventUuid + " interestPointId=" + interestPointId + " interestPoint=" + interestPoint);

        ouQuOnMangeService = Service.getInstance(getApplicationContext());

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            fetchInterestPointDetails(communityUuid, eventUuid, interestPoint);
        } else {
            members = savedInstanceState.getParcelableArrayList(Constants.MEMBERS_LIST);
            interestPointDetails = savedInstanceState.getParcelable(Constants.INTEREST_POINT_DETAILS);
            Log.d(LOG_TAG, "onCreate savedInstanceState = " + this.members.size());
        }

        initData();

        InitMembersList();
    }

    private void initData() {
        collapsingToolbar.setTitle(interestPoint.name);
        interestPointName.setText(interestPoint.name);
        interestPointAddress.setText(interestPoint.address);

        Log.d(LOG_TAG, "initData interestPointDetails=" + interestPointDetails);

        if (interestPointDetails != null && interestPointDetails.interestPoint.image.size() > 0) {
            Glide.with(getApplicationContext())
                    .load(interestPointDetails.interestPoint.image.get(0))
                    .into(backdropImageView);
        }
    }

    private void InitMembersList() {
        membersAdapter = new MembersAdapter(getApplicationContext(), this.members);
        membersRecyclerView.setHasFixedSize(true);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        membersRecyclerView.setAdapter(membersAdapter);
    }

    private void fetchInterestPointDetails(String communityUuid, String eventUuid, InterestPoint interestPoint) {
        if (NetConnectionUtils.isConnected(getApplicationContext())) {
            ouQuOnMangeService.getInterestPointDetails(communityUuid, eventUuid, interestPoint.apiId, interestPoint.type)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<InterestPointDetails>() {
                        @Override
                        public void call(InterestPointDetails ipd) {
                            Log.i(LOG_TAG, "Fetch InterestPointDetails ipd=" + ipd);
                            interestPointDetails = ipd;
                            Log.i(LOG_TAG, "Fetch InterestPointDetails images " + ipd.interestPoint.image);

                            if (ipd.interestPoint.image.size() > 0) {
                                Log.i(LOG_TAG, "Fetch InterestPointDetails image " + ipd.interestPoint.image.get(0));
                                Glide.with(getApplicationContext())
                                        .load(ipd.interestPoint.image.get(0))
                                        .into(backdropImageView);
                            }

                            members.clear();
                            members.addAll(ipd.members);
                            membersAdapter.notifyDataSetChanged();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                            if (throwable instanceof HttpException) {
                                HttpException response = (HttpException) throwable;
                                switch (response.code()) {
                                    case 400:
                                        Log.e(LOG_TAG, "400 Bad Request");
                                        showErrorSnackBar(getText(R.string.error_technical));
                                        break;
                                }
                            } else {
                                showErrorSnackBar(throwable.getMessage());
                            }
                        }
                    });


        } else {
            NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(Constants.EVENT_UUID, eventUuid);
        outState.putString(Constants.COMMUNITY_UUID, communityUuid);
        outState.putString(Constants.INTEREST_POINT_ID, interestPointId);
        outState.putParcelable(Constants.INTEREST_POINT, interestPoint);
        outState.putParcelableArrayList(Constants.MEMBERS_LIST, this.members);
        outState.putParcelable(Constants.INTEREST_POINT_DETAILS, interestPointDetails);
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

    private void showErrorSnackBar(CharSequence message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }
}
