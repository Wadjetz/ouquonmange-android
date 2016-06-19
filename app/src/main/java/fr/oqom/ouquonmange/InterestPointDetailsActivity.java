package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.InterestPoint;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class InterestPointDetailsActivity extends BaseActivity {

    private static final String LOG_TAG = "InterestPointsActivity";

    private TextView interestPointName;
    private TextView interestPointAddress;

    private ImageView interestPointPhoto;

    private String eventUuid;
    private String communityUuid;
    private String interestPointId;

    private ProgressBar progressBar;
    private InterestPoint interestPoint;

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
        checkAuth();
        // fetchInterestPointDetails();

        interestPointName.setText(interestPoint.name);
        interestPointAddress.setText(interestPoint.address);

        progressBar.setVisibility(View.GONE);
    }

    private void initView() {
        progressBar = (ProgressBar) findViewById(R.id.progress);
        interestPointName = (TextView) findViewById(R.id.interest_point_name);
        interestPointAddress = (TextView) findViewById(R.id.interest_point_address);
    }

    private void fetchInterestPointDetails() {
        api.getInterestPointDetails(interestPointId, eventUuid, communityUuid, new Callback<JSONObject>() {
            @Override
            public void apply(JSONObject jsonObject) {
                Log.d(LOG_TAG, "fetch Interest PointDetails " + jsonObject.toString());
            }
        }, new Callback2<Throwable, JSONObject>() {
            @Override
            public void apply(Throwable throwable, JSONObject jsonObject) {

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.EVENT_UUID, eventUuid);
        outState.putString(Constants.COMMUNITY_UUID, communityUuid);
        outState.putString(Constants.INTEREST_POINT_ID, interestPointId);
        outState.putParcelable(Constants.INTEREST_POINT, interestPoint);
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
