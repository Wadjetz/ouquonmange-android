package fr.oqom.ouquonmange;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorInterestPointsDetailsLayout);
        snackbar = Snackbar.make(coordinatorLayout,"Error !",Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close),closeSnackBarLogin);

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
        if(checkConnection(getApplicationContext())) {
            api.getInterestPointDetails(interestPoint, eventUuid, communityUuid, apiSuccessCallback, apiErrorCallback);
        }else{
            refreshSnackBar();
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
    public void refreshSnackBar(){
        snackbar.setText(R.string.no_internet)
                .setActionTextColor(Color.parseColor("#D32F2F"))
                .setDuration(Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.activate, activateSnackBarInterestPointsDetails)
                .show();
        progressBar.setVisibility(View.GONE);

    }
    private View.OnClickListener activateSnackBarInterestPointsDetails = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            CreateAlertSetting();
        }
    };

    private void CreateAlertSetting() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.setting_info)
                .setMessage(R.string.message_internet_not_available)
                .setCancelable(false)
                .setPositiveButton(R.string.activate_wifi_message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final WifiManager wifi =(WifiManager)getSystemService(getApplicationContext().WIFI_SERVICE);
                        wifi.setWifiEnabled(true);
                        if(checkConnection(getApplicationContext())) {
                            reloadActivity();
                        }else {
                            refreshSnackBar();
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.activate_data_mobile_message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setEnableDataMobile(true);
                        if(checkConnection(getApplicationContext())) {
                            reloadActivity();
                        }else {
                            refreshSnackBar();
                            dialog.dismiss();
                        }
                    }
                })
                .setNeutralButton(R.string.cancel_message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refreshSnackBar();
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    private void reloadActivity() {
        Intent intent = getIntent();
        intent.putExtra(Constants.EVENT_UUID,eventUuid);
        intent.putExtra(Constants.COMMUNITY_UUID,communityUuid);
        intent.putExtra(Constants.INTEREST_POINT_ID,interestPointId);
        intent.putExtra(Constants.INTEREST_POINT,interestPoint);
        finish();
        startActivity(intent);
    }

    public void setEnableDataMobile(boolean enable){
        // Enable data
        ConnectivityManager dataManager;
        dataManager  = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        Method dataMtd = null;
        try {
            dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
            dataMtd.setAccessible(true);
            dataMtd.invoke(dataManager, enable);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}