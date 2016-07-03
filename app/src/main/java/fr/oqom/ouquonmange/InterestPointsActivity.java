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
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.oqom.ouquonmange.adapters.InterestPointsAdapter;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.InterestPoint;
import fr.oqom.ouquonmange.services.ThrowableWithJson;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.NetConnectionUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class InterestPointsActivity extends BaseActivity implements LocationListener, OnMapReadyCallback {

    private static final String LOG_TAG = "InterestPointsActivity";
    private static final String IS_COLLAPSED = "IS_COLLAPSED";
    private static final String INTEREST_POINT_ITEM_HEIGHT = "INTEREST_POINT_ITEM_HEIGHT";
    private final int REQUEST_LOCATION_ASK_PERMISSIONS = 123;


    private RecyclerView interestPointsRecyclerView;
    private RecyclerView.Adapter interestPointsAdapter;

    //private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<InterestPoint> interestPoints = new ArrayList<>();
    private String eventUuid;
    private String communityUuid;

    private LocationManager locationManager;
    private Location location;
    private GoogleMap map;
    private Snackbar snackbar;
    private CoordinatorLayout coordinatorLayout;

    private Button containerCollapseAction;
    private View interestPointItem;
    private View mapContainer;
    private LinearLayout rootContainer;
    private View listContainer;
    private SupportMapFragment mapFragment;
    int interestPointItemHeight = 0;

    private boolean isCollapsed = false;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(LOG_TAG, "onWindowFocusChanged");

        if (hasFocus) {
            int height = rootContainer.getHeight();
            interestPointItemHeight = interestPointItem.getHeight() != 0 ? interestPointItem.getHeight() : interestPointItemHeight;
            if (!isCollapsed) {
                collapseEnable(height);
            } else {
                collapseDisable(height);
            }
        }
    }

    private void collapseEnable(int height) {
        interestPointsRecyclerView.setVisibility(View.VISIBLE);
        interestPointItem.setVisibility(View.GONE);
        containerCollapseAction.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_black), null, null, null);

        mapContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (height * 0.2)));
        mapFragment.getView().setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (height * 0.2)));
        listContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (height * 0.8)));
    }

    private void collapseDisable(int height) {
        interestPointsRecyclerView.setVisibility(View.GONE);
        interestPointItem.setVisibility(View.VISIBLE);
        containerCollapseAction.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_black), null, null, null);


        mapContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height - (interestPointItemHeight + containerCollapseAction.getHeight())));
        mapFragment.getView().setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height - (interestPointItemHeight + containerCollapseAction.getHeight())));
        listContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (interestPointItemHeight + containerCollapseAction.getHeight())));
    }

    private void showInterestPointItem(final InterestPoint interestPoint) {
        CardView interestPointCardView = (CardView) interestPointItem.findViewById(R.id.interest_point_cardView);
        TextView interestPointName = (TextView) interestPointItem.findViewById(R.id.interest_point_name);
        TextView interestPointAddress = (TextView) interestPointItem.findViewById(R.id.interest_point_address);
        final Button joinAction = (Button) interestPointItem.findViewById(R.id.action_join_group);
        Button detailsAction = (Button) interestPointItem.findViewById(R.id.action_details);
        Button voteAction = (Button) interestPointItem.findViewById(R.id.action_vote_group);

        interestPointName.setText(interestPoint.name);
        interestPointAddress.setText(interestPoint.address);
        String buttonText = (interestPoint.isJoin ? getApplicationContext().getString(R.string.quit_group) : getApplicationContext().getString(R.string.join_group)) + " (" + interestPoint.members + ")";
        joinAction.setText(buttonText);
        String buttonVote = (interestPoint.isVote ? getApplicationContext().getString(R.string.unvote_group) : getApplicationContext().getString(R.string.vote_group)) + " (" + interestPoint.votes + ")";
        voteAction.setText(buttonVote);

        joinAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (interestPoint.isJoin) {
                    api.quitGroup(communityUuid, eventUuid, interestPoint)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<JSONObject>() {
                                @Override
                                public void call(JSONObject response) {
                                    updateListAfterQuitGroup(interestPoint);
                                }
                            }, errorCallback);
                } else {
                    api.joinGroup(communityUuid, eventUuid, interestPoint)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<JSONObject>() {
                                @Override
                                public void call(JSONObject response) {
                                    updateListAfterJoinGroup(interestPoint);
                                }
                            }, errorCallback);
                }
            }
        });

        detailsAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInterestPointDetailsActivity(interestPoint);
            }
        });
        voteAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (interestPoint.isVote) {
                    api.unvoteGroup(communityUuid, eventUuid, interestPoint)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<JSONObject>() {
                                @Override
                                public void call(JSONObject jsonObject) {
                                    updateListAfterUnvote(interestPoint);
                                }
                            }, errorCallback);
                } else {
                    api.voteGroup(communityUuid, eventUuid, interestPoint)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<JSONObject>() {
                                @Override
                                public void call(JSONObject jsonObject) {
                                    updateListAfterVote(interestPoint);
                                }
                            }, errorCallback);
                }
            }
        });
    }

    private void initView() {
        containerCollapseAction = (Button) findViewById(R.id.container_collapse_action);
        interestPointItem = findViewById(R.id.interest_point_item);
        mapContainer = findViewById(R.id.map_container);
        rootContainer = (LinearLayout) findViewById(R.id.root_container);
        listContainer = findViewById(R.id.list_container);

        containerCollapseAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int height = rootContainer.getHeight();
                int interestPointItemHeight = interestPointItem.getHeight();

                Log.d(LOG_TAG, "height = " + height + " ipItemHeight = " + interestPointItemHeight + " isCollapsed = " + isCollapsed);

                if (isCollapsed) {
                    isCollapsed = false;
                    collapseEnable(height);

                } else {
                    isCollapsed = true;
                    collapseDisable(height);

                    /*
                    Animation animation = new Animation() {
                        @Override
                        public boolean willChangeBounds() {
                            return true;
                        }

                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            final int initialHeight = listContainer.getMeasuredHeight();

                            if(interpolatedTime == 1){
                                listContainer.setVisibility(View.GONE);
                            } else {
                                listContainer.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                                listContainer.requestLayout();
                            }
                        }
                    };
                    animation.setDuration(1000);
                    listContainer.startAnimation(animation);*/


                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        this.map.getUiSettings().setZoomControlsEnabled(false);

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
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                int position = 1;
                for (InterestPoint interestPoint : interestPoints) {
                    if (interestPoint.name.equals(marker.getTitle())) {
                        Log.d(LOG_TAG, marker.getTitle() + " position=" + position);
                        interestPointsRecyclerView.scrollToPosition(position);
                        showInterestPointItem(interestPoint);
                    }
                    position++;
                }

                return false;
            }
        });

        showMarkers();

        Log.d(LOG_TAG, "onMapReady interestPoints = " + interestPoints);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_points);
        Log.d(LOG_TAG, "onCreate");

        //progressBar = (ProgressBar) findViewById(R.id.progress);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorInterestPointsLayout);
        snackbar = Snackbar.make(coordinatorLayout, "Error !", Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close), closeSnackBarInterestPoints);

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
        initView();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (savedInstanceState == null) {
            fetchInterestPoints();
        } else {
            this.interestPoints = savedInstanceState.getParcelableArrayList(Constants.INTEREST_POINTS_LIST);
            //progressBar.setVisibility(View.GONE);
            Log.d(LOG_TAG, "onCreate savedInstanceState = " + this.interestPoints.size());
        }

        initInterestPointList();

    }

    private View.OnClickListener closeSnackBarInterestPoints = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            snackbar.dismiss();
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState");
        outState.putString(Constants.EVENT_UUID, eventUuid);
        outState.putString(Constants.COMMUNITY_UUID, communityUuid);
        outState.putParcelableArrayList(Constants.INTEREST_POINTS_LIST, this.interestPoints);
        outState.putBoolean(IS_COLLAPSED, isCollapsed);
        outState.putInt(INTEREST_POINT_ITEM_HEIGHT, interestPointItemHeight);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(LOG_TAG, "onRestoreInstanceState");
        eventUuid = savedInstanceState.getString(Constants.EVENT_UUID);
        communityUuid = savedInstanceState.getString(Constants.COMMUNITY_UUID);
        isCollapsed = savedInstanceState.getBoolean(IS_COLLAPSED);
        interestPointItemHeight = savedInstanceState.getInt(INTEREST_POINT_ITEM_HEIGHT);
    }

    private void updateListAfterJoinGroup(final InterestPoint joinedInterestPoint) {
        for (InterestPoint ip : interestPoints) {
            if (ip.isJoin) {
                Log.i(LOG_TAG, "Group unjoined = " + ip.name);
                ip.members = (ip.members > 0) ? (ip.members - 1) : ip.members;
            }

            if (ip.apiId.equals(joinedInterestPoint.apiId)) {
                ip.isJoin = true;
                Log.i(LOG_TAG, "Group joining = " + ip.name);
                ip.members++;
                showInterestPointItem(ip);
            } else {
                ip.isJoin = false;
            }
        }
        interestPointsAdapter.notifyDataSetChanged();
    }

    private void updateListAfterQuitGroup(final InterestPoint quitedInterestPoint) {
        for (InterestPoint ip : interestPoints) {
            if (ip.isJoin) {
                Log.i(LOG_TAG, "Group joined = " + ip.name);
                ip.members = (ip.members > 0) ? (ip.members - 1) : ip.members;
            }
            if (ip.apiId.equals(quitedInterestPoint.apiId)) {
                ip.isJoin = false;
                interestPointsAdapter.notifyDataSetChanged();
                showInterestPointItem(ip);
            }
        }
    }

    private void updateListAfterVote(final InterestPoint interestPoint) {
        for (InterestPoint ip : interestPoints) {
            if (ip.apiId.equals(interestPoint.apiId)) {
                ip.isVote = true;
                Log.i(LOG_TAG, "Group voting = " + ip.name);
                ip.votes++;
                showInterestPointItem(ip);
            }
        }
        interestPointsAdapter.notifyDataSetChanged();
    }

    private void updateListAfterUnvote(final InterestPoint interestPoint) {
        for (InterestPoint ip : interestPoints) {
            if (ip.apiId.equals(interestPoint.apiId)) {
                ip.isVote = false;
                Log.i(LOG_TAG, "Group unvoting = " + ip.name);
                ip.votes--;
                showInterestPointItem(ip);
                interestPointsAdapter.notifyDataSetChanged();
            }
        }
    }

    private void startInterestPointDetailsActivity(final InterestPoint interestPoint) {
        Intent intent = new Intent(getApplicationContext(), InterestPointDetailsActivity.class);
        intent.putExtra(Constants.INTEREST_POINT_ID, interestPoint.apiId);
        intent.putExtra(Constants.EVENT_UUID, eventUuid);
        intent.putExtra(Constants.COMMUNITY_UUID, communityUuid);
        intent.putExtra(Constants.INTEREST_POINT, interestPoint);
        startActivity(intent);
    }

    private void showApiError(ThrowableWithJson throwableWithJson) {
        if (throwableWithJson.getJson() != null) {
            try {
                String serverError = throwableWithJson.getJson().getString("error");
                snackbar.setText(serverError).setActionTextColor(Color.parseColor("#D32F2F")).show();

            } catch (JSONException e) {
                snackbar.setText(e.getMessage()).setActionTextColor(Color.parseColor("#D32F2F")).show();
            }
        } else {
            snackbar.setText(throwableWithJson.getThrowable().getMessage()).setActionTextColor(Color.parseColor("#D32F2F")).show();
        }
    }

    private final Action1<Throwable> errorCallback = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            ThrowableWithJson throwableWithJson = (ThrowableWithJson) throwable;
            showApiError(throwableWithJson);
        }
    };


    private void initInterestPointList() {
        interestPointsAdapter = new InterestPointsAdapter(getApplicationContext(), interestPoints, new Callback<InterestPoint>() {
            @Override
            public void apply(final InterestPoint interestPoint) {
                if (NetConnectionUtils.isConnected(getApplicationContext())) {
                    if (interestPoint.isJoin) {
                        api.quitGroup(communityUuid, eventUuid, interestPoint)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<JSONObject>() {
                                    @Override
                                    public void call(JSONObject jsonObject) {
                                        updateListAfterQuitGroup(interestPoint);
                                    }
                                }, errorCallback);
                    } else {
                        api.joinGroup(communityUuid, eventUuid, interestPoint)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<JSONObject>() {
                                    @Override
                                    public void call(JSONObject response) {
                                        updateListAfterJoinGroup(interestPoint);
                                    }
                                }, errorCallback);
                    }
                } else {
                    showNoConnexionSnackBar(coordinatorLayout);
                }
            }
        }, new Callback<InterestPoint>() {
            @Override
            public void apply(InterestPoint interestPoint) {
                startInterestPointDetailsActivity(interestPoint);
            }
        }, new Callback<InterestPoint>() {
            @Override
            public void apply(final InterestPoint interestPoint) {
                if (NetConnectionUtils.isConnected(getApplicationContext())) {
                    if (interestPoint.isVote) {
                        api.unvoteGroup(communityUuid, eventUuid, interestPoint)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<JSONObject>() {
                                    @Override
                                    public void call(JSONObject jsonObject) {
                                        updateListAfterUnvote(interestPoint);
                                    }
                                }, errorCallback);
                    } else {
                        api.voteGroup(communityUuid, eventUuid, interestPoint)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<JSONObject>() {
                                    @Override
                                    public void call(JSONObject jsonObject) {
                                        updateListAfterVote(interestPoint);
                                    }
                                }, errorCallback);
                    }
                } else {
                    showNoConnexionSnackBar(coordinatorLayout);
                }
            }
        });

        interestPointsRecyclerView = (RecyclerView) findViewById(R.id.interest_points_list);
        RecyclerView.LayoutManager interestPointsLayoutManager = new LinearLayoutManager(this);
        interestPointsRecyclerView.setHasFixedSize(true);
        interestPointsRecyclerView.setLayoutManager(interestPointsLayoutManager);
        interestPointsRecyclerView.setAdapter(interestPointsAdapter);
    }

    private Action1<Throwable> apiErrorCallback = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            throwable.printStackTrace();
            Log.e(LOG_TAG, "Fetch InterestPoint = " + throwable.getMessage());
            snackbar.setText(getText(R.string.error_exception)).setActionTextColor(Color.parseColor("#D32F2F")).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    private Action1<List<InterestPoint>> apiSuccessCallback = new Action1<List<InterestPoint>>() {
        @Override
        public void call(List<InterestPoint> ips) {
            interestPoints.clear();
            interestPoints.addAll(ips);
            showMarkers();
            interestPointsAdapter.notifyDataSetChanged();
            Log.i(LOG_TAG, "Fetch InterestPoint = " + interestPoints.size());
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    private void fetchInterestPoints() {
        if (NetConnectionUtils.isConnected(getApplicationContext())) {
            api.getInterestPoints(eventUuid, communityUuid)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(apiSuccessCallback, apiErrorCallback);
        } else {
            NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void fetchInterestPoints(Location location) {
        if (NetConnectionUtils.isConnected(getApplicationContext())) {
            api.getInterestPointsByLocation(location, eventUuid, communityUuid)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(apiSuccessCallback, apiErrorCallback);
        } else {
            NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
            swipeRefreshLayout.setRefreshing(false);
        }
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
        Log.d(LOG_TAG, "Location Changed GPS_PROVIDER : " + " provider = " + location.getProvider() + "  " + ((this.location != null) ? location.distanceTo(this.location) : "null") + " loc = " + location.getLatitude() + " " + location.getLongitude());
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

    private void showMarkers() {
        if (map != null) {
            boolean setCamera = false;
            for (InterestPoint interestPoint : interestPoints) {
                LatLng position = new LatLng(Double.valueOf(interestPoint.lat), Double.valueOf(interestPoint.lng));
                if (!setCamera) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 13));
                    setCamera = true;
                    showInterestPointItem(interestPoint);
                }
                map.addMarker(new MarkerOptions()
                        .position(position)
                        .title(interestPoint.name));
            }
        } else {
            snackbar.setText(R.string.map_not_ready).setActionTextColor(Color.parseColor("#D32F2F")).show();
            Log.e(LOG_TAG, "Map not ready");
        }
    }

    public void showNoConnexionSnackBar(final CoordinatorLayout coordinatorLayout) {
        Snackbar.make(coordinatorLayout, R.string.no_internet, Snackbar.LENGTH_LONG)
                .setAction(R.string.activate, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NetConnectionUtils.createAlertSetting(getApplicationContext());
                    }
                })
                .show();
    }
}
