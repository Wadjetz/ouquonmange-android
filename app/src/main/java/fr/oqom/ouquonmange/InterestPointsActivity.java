package fr.oqom.ouquonmange;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.oqom.ouquonmange.adapters.InterestPointsAdapter;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.InterestPoint;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class InterestPointsActivity extends BaseActivity implements LocationListener, OnMapReadyCallback {

    private static final String LOG_TAG = "InterestPointsActivity";
    final private int REQUEST_LOCATION_ASK_PERMISSIONS = 123;


    private RecyclerView interestPointsRecyclerView;
    private RecyclerView.Adapter interestPointsAdapter;
    private RecyclerView.LayoutManager interestPointsLayoutManager;

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<InterestPoint> interestPoints = new ArrayList<>();
    private String eventUuid;
    private String communityUuid;

    private LocationManager locationManager;
    private Location location;
    private GoogleMap map;
    private Snackbar snackbar;


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        final GoogleMap map = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);

        Log.d(LOG_TAG, "onMapReady interestPoints = " + interestPoints);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_points);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(LOG_TAG, "onRefresh");
                interestPoints.clear();
                if (location != null) {
                    fetchInterestPoints(location);
                } else {
                    fetchInterestPoints();
                }

            }
        });

        eventUuid = getIntent().getStringExtra(Constants.EVENT_UUID);
        communityUuid = getIntent().getStringExtra(Constants.COMMUNITY_UUID);

        initNav();
        checkAuth();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (savedInstanceState == null) {
            fetchInterestPoints();
        } else {
            this.interestPoints = savedInstanceState.getParcelableArrayList(Constants.INTEREST_POINTS_LIST);
            progressBar.setVisibility(View.GONE);
            Log.d(LOG_TAG, "onCreate savedInstanceState = " + this.interestPoints.size());
        }

        initInterestPointList();

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorInterestPointsLayout);
        snackbar = Snackbar.make(coordinatorLayout,"Error !",Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close),closeSnackBarInterestPoints);
    }

    private View.OnClickListener closeSnackBarInterestPoints = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            snackbar.dismiss();
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.EVENT_UUID, eventUuid);
        outState.putString(Constants.COMMUNITY_UUID, communityUuid);
        outState.putParcelableArrayList(Constants.INTEREST_POINTS_LIST, this.interestPoints);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        eventUuid = savedInstanceState.getString(Constants.EVENT_UUID);
        communityUuid = savedInstanceState.getString(Constants.COMMUNITY_UUID);
    }

    private void initInterestPointList() {

        final Callback2<Throwable, JSONObject> errorCallback = new Callback2<Throwable, JSONObject>() {
            @Override
            public void apply(Throwable throwable, JSONObject jsonObject) {
            if (jsonObject != null) {
                Log.e(LOG_TAG, jsonObject.toString());
                String err = "";
                try {
                    err = jsonObject.getString("error");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                snackbar.setText(err).setActionTextColor(Color.parseColor("#D32F2F")).show();
            }
            }
        };

        interestPointsAdapter = new InterestPointsAdapter(getApplicationContext(), interestPoints, new Callback<InterestPoint>() {
            @Override
            public void apply(final InterestPoint interestPoint) {
                if (interestPoint.isJoin) {
                    api.quitGroup(communityUuid, eventUuid, interestPoint, new Callback<JSONObject>() {
                        @Override
                        public void apply(JSONObject jsonObject) {
                            for (InterestPoint ip : interestPoints) {
                                if(ip.isJoin){
                                    Log.i(LOG_TAG, "Group joined = " + ip.name);
                                    ip.members = (ip.members>0)?(ip.members-1):ip.members;
                                }
                                if (ip.apiId.equals(interestPoint.apiId)) {
                                    ip.isJoin = false;
                                    interestPointsAdapter.notifyDataSetChanged();

                                }
                            }
                        }
                    }, errorCallback);
                } else {
                    api.joinGroup(communityUuid, eventUuid, interestPoint, new Callback<JSONObject>() {
                        @Override
                        public void apply(JSONObject jsonObject) {
                            for (InterestPoint ip : interestPoints) {
                                if(ip.isJoin){
                                    Log.i(LOG_TAG, "Group unjoined = " + ip.name);
                                    ip.members = (ip.members>0)?ip.members-1:ip.members;
                                }

                                if (ip.apiId.equals(interestPoint.apiId)) {
                                    ip.isJoin = true;
                                    Log.i(LOG_TAG, "Group joining = " + ip.name);
                                    ip.members++;
                                } else {
                                    ip.isJoin = false;
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
                intent.putExtra(Constants.INTEREST_POINT_ID, interestPoint.apiId);
                intent.putExtra(Constants.EVENT_UUID, eventUuid);
                intent.putExtra(Constants.COMMUNITY_UUID, communityUuid);
                intent.putExtra(Constants.INTEREST_POINT, interestPoint);
                startActivity(intent);
            }
        }, new Callback<InterestPoint>() {
            @Override
            public void apply(final InterestPoint interestPoint) {
                if (interestPoint.isVote) {
                    api.unvoteGroup(communityUuid, eventUuid, interestPoint, new Callback<JSONObject>() {
                        @Override
                        public void apply(JSONObject jsonObject) {
                            for (InterestPoint ip : interestPoints) {
                                if (ip.apiId.equals(interestPoint.apiId)) {
                                    ip.isVote = false;
                                    Log.i(LOG_TAG, "Group unvoting = " + ip.name);
                                    ip.votes--;
                                    interestPointsAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }, errorCallback);
                } else {
                    api.voteGroup(communityUuid, eventUuid, interestPoint, new Callback<JSONObject>() {
                        @Override
                        public void apply(JSONObject jsonObject) {
                            for (InterestPoint ip : interestPoints) {
                                if (ip.apiId.equals(interestPoint.apiId)) {
                                    ip.isVote = true;
                                    Log.i(LOG_TAG, "Group voting = " + ip.name);
                                    ip.votes++;
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
                Log.e(LOG_TAG, jsonObject.toString());
                String err = "";
                try {
                    err = jsonObject.getString("error");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                snackbar.setText(err).setActionTextColor(Color.parseColor("#D32F2F")).show();
            }else{
                snackbar.setText(R.string.error_exception).setActionTextColor(Color.parseColor("#D32F2F")).show();
            }
            Log.e(LOG_TAG, "Fetch InterestPoint = " + throwable.getMessage());
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    private Callback<JSONArray> apiSuccessCallback = new Callback<JSONArray>() {
        @Override
        public void apply(JSONArray json) {
            try {
                interestPoints.clear();
                interestPoints.addAll(InterestPoint.fromJson(json));

                    boolean setCamera = false;
                    for (InterestPoint interestPoint : interestPoints) {
                        if (map != null) {
                            LatLng position = new LatLng(Double.valueOf(interestPoint.lat), Double.valueOf(interestPoint.lng));
                            if (setCamera) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
                                setCamera = true;
                            }
                            map.addMarker(new MarkerOptions()
                                    .position(position)
                                    .title(interestPoint.name));
                        } else {
                            snackbar.setText(R.string.map_not_ready).setActionTextColor(Color.parseColor("#D32F2F")).show();
                            Log.e(LOG_TAG, "Map not ready");
                        }
                    }

                    interestPointsAdapter.notifyDataSetChanged();
                    Log.i(LOG_TAG, "Fetch InterestPoint = " + json.length());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Fetch InterestPoint = " + e.getMessage());
                    snackbar.setText(getText(R.string.error_exception)).setActionTextColor(Color.parseColor("#D32F2F")).show();
                }

            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    private void fetchInterestPoints() {
        api.getInterestPoints(eventUuid, communityUuid, apiSuccessCallback, apiErrorCallback);
    }

    private void fetchInterestPoints(Location location) {
        api.getInterestPointsByLocation(location, eventUuid, communityUuid, apiSuccessCallback, apiErrorCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_interest_point_menu, menu);
        return true;
    }

    private boolean getLocation() {
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d(LOG_TAG, "getLocation isGPSEnabled=" + isGPSEnabled + " isNetEnabled=" + isNetEnabled);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            };

            ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_ASK_PERMISSIONS);
            return true;
        }

        if (isGPSEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 1000 * 60, this);
            Log.d(LOG_TAG, "requestLocationUpdates GPS_PROVIDER");
        }

        if (isNetEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 1000 * 60, this);
            Log.d(LOG_TAG, "requestLocationUpdates NETWORK_PROVIDER");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_location:
                getLocation();
                return true;
            case R.id.menu_action_search:
                Toast.makeText(getApplicationContext(), "TODO Search Interest Points", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "Location Changed GPS_PROVIDER : " + " provider = " + location.getProvider() +  "  " + ((this.location != null) ? location.distanceTo(this.location) : "null") + " loc = " + location.getLatitude() + " " + location.getLongitude());
        this.location = location;

        if (this.map != null) {
            this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
        }

        Toast.makeText(getApplicationContext(), "GPS : " + location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_LONG).show();
        fetchInterestPoints(location);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Location Changed GPS_PROVIDER checkSelfPermission");
        }
        //locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(LOG_TAG, "onStatusChanged provider = " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(LOG_TAG, "onProviderEnabled provider = " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(LOG_TAG, "onProviderDisabled provider = " + provider);
    }

    @Override
    protected void onStop() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "onStop checkSelfPermission");
        }
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        super.onStop();
    }
}
