package fr.oqom.ouquonmange;

import android.content.Context;
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;
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
    private String communityUuid;

    private LocationManager locationManager;
    private Location location;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_points);

        progressBar = (ProgressBar) findViewById(R.id.progress);

        eventUuid = getIntent().getStringExtra(Constants.EVENT_UUID);
        communityUuid = getIntent().getStringExtra(Constants.COMMUNITY_UUID);

        initNav();
        checkAuth();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        initInterestPointList();
        fetchInterestPoints();
    }

    private void initInterestPointList() {

        final Callback2<Throwable, JSONObject> errorCallback = new Callback2<Throwable, JSONObject>() {
            @Override
            public void apply(Throwable throwable, JSONObject jsonObject) {
                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                if (jsonObject != null) {
                    Toast.makeText(getApplicationContext(), jsonObject.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        interestPointsAdapter = new InterestPointsAdapter(getApplicationContext(), interestPoints, new Callback<InterestPoint>() {
            @Override
            public void apply(final InterestPoint interestPoint) {
                if (interestPoint.isJoin) {
                    api.quitGroup(communityUuid, eventUuid, interestPoint.foursquareId, new Callback<JSONObject>() {
                        @Override
                        public void apply(JSONObject jsonObject) {
                            for (InterestPoint ip : interestPoints) {
                                if (ip.foursquareId.equals(interestPoint.foursquareId)) {
                                    ip.isJoin = false;
                                    Log.i(LOG_TAG, " quit = " + ip.members+" - "+interestPoint.members);
                                    interestPointsAdapter.notifyDataSetChanged();

                                }
                            }
                        }
                    }, errorCallback);
                } else {
                    api.joinGroup(communityUuid, eventUuid, interestPoint.foursquareId, new Callback<JSONObject>() {
                        @Override
                        public void apply(JSONObject jsonObject) {
                            for (InterestPoint ip : interestPoints) {
                                if (ip.foursquareId.equals(interestPoint.foursquareId)) {
                                    ip.isJoin = true;
                                    Log.i(LOG_TAG, " join T = " + ip.members+" - "+interestPoint.members);
                                } else {
                                    ip.isJoin = false;
                                    Log.i(LOG_TAG, " join F = " + ip.members+" - "+interestPoint.members);
                                }
                            }
                            interestPointsAdapter.notifyDataSetChanged();
                        }
                    }, errorCallback);
                }
            }
        }, new Callback<InterestPoint>() {
            @Override
            public void apply(InterestPoint interestPoint) {
                Intent intent = new Intent(getApplicationContext(), InterestPointDetailsActivity.class);
                intent.putExtra(Constants.INTEREST_POINT_ID, interestPoint.foursquareId);
                intent.putExtra(Constants.EVENT_UUID, eventUuid);
                intent.putExtra(Constants.COMMUNITY_UUID, communityUuid);
                intent.putExtra(Constants.INTEREST_POINT, interestPoint);
                startActivity(intent);
            }
        }, new Callback<InterestPoint>() {
            @Override
            public void apply(final InterestPoint interestPoint) {
                if (interestPoint.isVote) {
                    api.unvoteGroup(communityUuid, eventUuid, interestPoint.foursquareId, new Callback<JSONObject>() {
                        @Override
                        public void apply(JSONObject jsonObject) {
                            for (InterestPoint ip : interestPoints) {
                                if (ip.foursquareId.equals(interestPoint.foursquareId)) {
                                    ip.isVote = false;
                                    Log.i(LOG_TAG, " unvote = " + ip.votes+" - "+interestPoint.votes);

                                    interestPointsAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }, errorCallback);
                } else {
                    api.voteGroup(communityUuid, eventUuid, interestPoint.foursquareId, new Callback<JSONObject>() {
                        @Override
                        public void apply(JSONObject jsonObject) {
                            for (InterestPoint ip : interestPoints) {
                                if (ip.foursquareId.equals(interestPoint.foursquareId)) {
                                    ip.isVote = true;
                                    Log.i(LOG_TAG, " vote T = " + ip.votes+" - "+interestPoint.votes);
                                } else {
                                    ip.isVote = false;
                                    Log.i(LOG_TAG, " vote F = " + ip.votes+" - "+interestPoint.votes);
                                }
                            }
                            interestPointsAdapter.notifyDataSetChanged();
                        }
                    }, errorCallback);
                }
            }
        });

        interestPointsRecyclerView = (RecyclerView) findViewById(R.id.interest_points_list);
        interestPointsLayoutManager = new LinearLayoutManager(this);
        interestPointsRecyclerView.setHasFixedSize(true);
        interestPointsRecyclerView.setLayoutManager(interestPointsLayoutManager);
        interestPointsRecyclerView.setAdapter(interestPointsAdapter);
    }

    private Callback2<Throwable, JSONObject> apiErrorCallback = new Callback2<Throwable, JSONObject>() {
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
    };

    private Callback<JSONObject> apiSuccessCallback = new Callback<JSONObject>() {
        @Override
        public void apply(JSONObject jsonObject) {
            try {
                JSONArray interestPointsJson = jsonObject.getJSONArray("interestPoints");
                JSONArray othersInterestPointsJson = jsonObject.getJSONArray("othersInterestPoints");
                List<InterestPoint> interestPointsWithGroup = InterestPoint.fromJson(interestPointsJson);
                List<InterestPoint> interestPointsByLocation = InterestPoint.fromJson(othersInterestPointsJson);
                interestPoints.clear();
                interestPoints.addAll(interestPointsWithGroup);
                interestPoints.addAll(interestPointsByLocation);
                interestPointsAdapter.notifyDataSetChanged();
                Log.i(LOG_TAG, "Fetch InterestPoint = " + jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Fetch InterestPoint = " + e.getMessage());
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            progressBar.setVisibility(View.GONE);
        }
    };

    private void fetchInterestPoints() {
        api.getInterestPoints(eventUuid, communityUuid, apiSuccessCallback, apiErrorCallback);
    }

    private void fetchInterestPoints(Location location) {
        api.getInterestPointsByLocation(location, eventUuid, communityUuid, apiSuccessCallback, apiErrorCallback);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        eventUuid = savedInstanceState.getString(Constants.EVENT_UUID);
        communityUuid = savedInstanceState.getString(Constants.COMMUNITY_UUID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_interest_point_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_location:
                progressBar.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "TODO Location Interest Points", Toast.LENGTH_SHORT).show();
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    String[] permissions = new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                    };

                    ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_ASK_PERMISSIONS);
                    return true;
                }

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

                return true;
            case R.id.menu_action_search:
                Toast.makeText(getApplicationContext(), "TODO Search Interest Points", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.EVENT_UUID, eventUuid);
        outState.putString(Constants.COMMUNITY_UUID, communityUuid);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "Location Changed");

        if (this.location == null) {
            this.location = location;
            Toast.makeText(getApplicationContext(), location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_LONG).show();
            fetchInterestPoints(location);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        } else {
            locationManager.removeUpdates(InterestPointsActivity.this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(LOG_TAG, "onStatusChanged + " + provider);
        Toast.makeText(getApplicationContext(), "onStatusChanged", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(LOG_TAG, "Location Enabled = " + provider);
        Toast.makeText(getApplicationContext(), "Location Enabled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(LOG_TAG, "Location Disabled = " + provider);
        Toast.makeText(getApplicationContext(), "Location Disabled", Toast.LENGTH_LONG).show();
    }
}
