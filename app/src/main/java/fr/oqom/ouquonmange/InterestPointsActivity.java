package fr.oqom.ouquonmange;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.oqom.ouquonmange.adapters.InterestPointsAdapter;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.InterestPoint;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class InterestPointsActivity extends BaseActivity implements LocationListener {

    private static final String LOG_TAG = "InterestPointsActivity";
    final private int REQUEST_LOCATION_ASK_PERMISSIONS = 123;


    private RecyclerView interestPointsRecyclerView;
    private RecyclerView.Adapter interestPointsAdapter;
    private RecyclerView.LayoutManager interestPointsLayoutManager;

    private ProgressBar progressBar;

    private List<InterestPoint> interestPoints = new ArrayList<>();
    private String eventUuid;

    private LocationManager locationManager;
    private Location location;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_points);

        progressBar = (ProgressBar) findViewById(R.id.progress);

        eventUuid = getIntent().getStringExtra(Constants.EVENT_UUID);
        Toast.makeText(getApplicationContext(), eventUuid, Toast.LENGTH_LONG).show();
        initNav();
        checkAuth();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            String[] permissions = new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            };

            ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_ASK_PERMISSIONS);

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        initInterestPointList();
    }

    private void initInterestPointList() {
        interestPointsAdapter = new InterestPointsAdapter(interestPoints, new Callback<InterestPoint>() {
            @Override
            public void apply(InterestPoint interestPoint) {
                Toast.makeText(getApplicationContext(), interestPoint.name, Toast.LENGTH_SHORT).show();
            }
        });
        interestPointsRecyclerView = (RecyclerView) findViewById(R.id.interest_points_list);
        interestPointsLayoutManager = new LinearLayoutManager(this);
        interestPointsRecyclerView.setHasFixedSize(true);
        interestPointsRecyclerView.setLayoutManager(interestPointsLayoutManager);
        interestPointsRecyclerView.setAdapter(interestPointsAdapter);
    }

    private void fetchInterestPoints(Location location) {
        api.getInterestPoints(location, new Callback<JSONArray>() {
            @Override
            public void apply(JSONArray jsonObject) {
                try {
                    interestPoints.addAll(InterestPoint.fromJson(jsonObject));
                    interestPointsAdapter.notifyDataSetChanged();
                    Log.i(LOG_TAG, "Fetch InterestPoint = " + interestPoints.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Fetch InterestPoint = " + e.getMessage());
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.GONE);
            }
        }, new Callback2<Throwable, JSONObject>() {
            @Override
            public void apply(Throwable throwable, JSONObject jsonObject) {
                if (jsonObject != null) {
                    Log.e(LOG_TAG, "Fetch InterestPoint = " + jsonObject.toString());
                    Toast.makeText(getApplicationContext(), jsonObject.toString(), Toast.LENGTH_SHORT).show();
                }
                Log.e(LOG_TAG, "Fetch InterestPoint = " + throwable.getMessage());
                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        eventUuid = savedInstanceState.getString(Constants.EVENT_UUID);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.EVENT_UUID, eventUuid);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "Location Changed");
        this.location = location;
        Toast.makeText(getApplicationContext(), location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_LONG).show();
        fetchInterestPoints(location);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(InterestPointsActivity.this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(LOG_TAG, "onStatusChanged");
        Toast.makeText(getApplicationContext(), "onStatusChanged", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(LOG_TAG, "Location Enabled");
        Toast.makeText(getApplicationContext(), "Location Enabled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(LOG_TAG, "Location Disabled");
        Toast.makeText(getApplicationContext(), "Location Disabled", Toast.LENGTH_LONG).show();
    }
}
