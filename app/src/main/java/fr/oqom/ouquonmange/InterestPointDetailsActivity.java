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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.oqom.ouquonmange.adapters.MembersAdapter;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.InterestPoint;
import fr.oqom.ouquonmange.models.User;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class InterestPointDetailsActivity extends BaseActivity {

    private static final String LOG_TAG = "InterestPtsActivityDet";

    private TextView interestPointName;
    private TextView interestPointAddress;
    private TextView interestPointNameMembers;
    //private TextView interestPointNameMembersNumbers;
    private RecyclerView.LayoutManager membersLayoutManager;
    //private ImageView interestPointPhoto;
    private String eventUuid;
    private String communityUuid;
    private String interestPointId;
    private ArrayList<User> members = new ArrayList<>();

    private ProgressBar progressBar;
    private InterestPoint interestPoint;
    private RecyclerView membersRecyclerView;
    private RecyclerView.Adapter membersAdapter;
    private Snackbar snackbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_point_details);
        Intent intent = getIntent();
        eventUuid = intent.getStringExtra(Constants.EVENT_UUID);
        communityUuid = intent.getStringExtra(Constants.COMMUNITY_UUID);
        interestPointId = intent.getStringExtra(Constants.INTEREST_POINT_ID);
        interestPoint = intent.getParcelableExtra(Constants.INTEREST_POINT);

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
        //interestPointNameMembersNumbers = (TextView) findViewById(R.id.interest_point_members_numbers);
        //interestPointNameMembersNumbers.setText(Constants.DEFAULT_NUMBERS_OF_MEMBERS);
    }

    private void InitMembersList() {

        membersAdapter = new MembersAdapter(getApplicationContext(),this.members);

        membersRecyclerView = (RecyclerView) findViewById(R.id.members_list);
        membersLayoutManager = new LinearLayoutManager(this);
        membersRecyclerView.setHasFixedSize(true);
        membersRecyclerView.setLayoutManager(membersLayoutManager);
        membersRecyclerView.setAdapter(membersAdapter);


        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorInterestPointsDetailsLayout);
        snackbar = Snackbar.make(coordinatorLayout,"Error !",Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close),closeSnackBarLogin);
    }

    private View.OnClickListener closeSnackBarLogin = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            snackbar.dismiss();
        }
    };

    private void initView() {
        progressBar = (ProgressBar) findViewById(R.id.progress);
        interestPointName = (TextView) findViewById(R.id.interest_point_detail_name);
        interestPointAddress = (TextView) findViewById(R.id.interest_point_detail_address);
    }

    private void fetchInterestPointDetails(String communityUuid, String eventUuid, InterestPoint interestPoint) {
        api.getInterestPointDetails(interestPoint, eventUuid, communityUuid, apiSuccessCallback,apiErrorCallback );
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

    private Callback<JSONObject> apiSuccessCallback = new Callback<JSONObject>() {
        @Override
        public void apply(JSONObject jsonObject) {
            if(jsonObject !=null) {
                try {
                    JSONArray membersJson = jsonObject.getJSONArray("members");
                    List<User> membersList = User.fromJson(membersJson);
                    members.clear();
                    members.addAll(membersList);
                    membersAdapter.notifyDataSetChanged();
                    Log.i(LOG_TAG, "Fetch members = " + members.size() + " members list :" + membersList.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Fetch members error : " + e.getMessage());
                    snackbar.setText(R.string.error_exception).setActionTextColor(Color.parseColor("#D32F2F")).show();
                }
            }else{
                snackbar.setText(R.string.error_exception).setActionTextColor(Color.parseColor("#D32F2F")).show();
            }
            progressBar.setVisibility(View.GONE);
        }
    };

    private Callback2<Throwable, JSONObject> apiErrorCallback = new Callback2<Throwable, JSONObject>() {
        @Override
        public void apply(Throwable throwable, JSONObject jsonObject) {
            Log.e(LOG_TAG, "fetch Interest PointDetails Error " + jsonObject.toString());
            String err = "";
            if(jsonObject != null) {
                try {
                    err = jsonObject.getString("error");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                snackbar.setText(err).setActionTextColor(Color.parseColor("#D32F2F")).show();
            }else{
                snackbar.setText(R.string.error_exception).setActionTextColor(Color.parseColor("#D32F2F")).show();
            }
        }
    };
}
