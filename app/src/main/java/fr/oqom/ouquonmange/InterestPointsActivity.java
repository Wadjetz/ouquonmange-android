package fr.oqom.ouquonmange;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.oqom.ouquonmange.adapters.EmptyRecyclerView;
import fr.oqom.ouquonmange.adapters.InterestPointsAdapter;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.Group;
import fr.oqom.ouquonmange.models.InterestPoint;
import fr.oqom.ouquonmange.models.JoinGroup;
import fr.oqom.ouquonmange.models.Message;
import fr.oqom.ouquonmange.models.MessageEvent;
import fr.oqom.ouquonmange.models.Vote;
import fr.oqom.ouquonmange.models.VoteGroup;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.NetConnectionUtils;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class InterestPointsActivity extends BaseActivity implements LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private static final String LOG_TAG = "InterestPointsActivity";
    private static final String IS_COLLAPSED = "IS_COLLAPSED";
    private static final String INTEREST_POINT_ITEM_HEIGHT = "INTEREST_POINT_ITEM_HEIGHT";
    private final int REQUEST_LOCATION_ASK_PERMISSIONS = 123;

    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.coordinatorInterestPointsLayout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.container_collapse_action) Button containerCollapseAction;
    @BindView(R.id.interest_point_item) View interestPointItem;
    @BindView(R.id.map_container) View mapContainer;
    @BindView(R.id.root_container) LinearLayout rootContainer;
    @BindView(R.id.list_container) View listContainer;
    @BindView(R.id.interest_points_list) EmptyRecyclerView interestPointsRecyclerView;

    private InterestPointsAdapter interestPointsAdapter;
    private ArrayList<InterestPoint> interestPoints = new ArrayList<>();
    private String eventUuid;
    private String communityUuid;
    private String searchQuery = "";

    private LocationManager locationManager;
    private LatLng targetLocation;
    private LatLng userLocation;
    private GoogleMap map;


    private SupportMapFragment mapFragment;
    int interestPointItemHeight = 0;

    private boolean isCollapsed = false;
    private boolean isCollapsedInterestPointStetted = false;

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_points);
        ButterKnife.bind(this);
        Log.d(LOG_TAG, "onCreate");

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(LOG_TAG, "onRefresh");
                interestPoints.clear();
                if (targetLocation != null) {
                    fetchInterestPoints(targetLocation.latitude, targetLocation.longitude);
                } else {
                    fetchInterestPoints();
                }
            }
        });

        queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(LOG_TAG, "onQueryTextSubmit query=" + query);
                searchQuery = query;
                searchView.clearFocus();
                if (targetLocation != null) {
                    fetchInterestPoints(targetLocation, searchQuery);
                } else {
                    showErrorSnackBar(getString(R.string.interest_point_search_error_no_location));
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String q) {
                Log.d(LOG_TAG, "onQueryTextChange q=" + q);
                searchQuery = q;
                return true;
            }
        };

        eventUuid = getIntent().getStringExtra(Constants.EVENT_UUID);
        communityUuid = getIntent().getStringExtra(Constants.COMMUNITY_UUID);

        initNav();
        checkAuth();
        initInterestPointList();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (savedInstanceState == null) {
            fetchInterestPoints();
        } else {
            List<InterestPoint> interestPointsFromInstance = savedInstanceState.getParcelableArrayList(Constants.INTEREST_POINTS_LIST);
            this.interestPoints.addAll(interestPointsFromInstance);
            interestPointsAdapter.notifyDataSetChanged();
            Log.d(LOG_TAG, "onCreate savedInstanceState = " + this.interestPoints.size());
        }

        interestPointItem.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (this.targetLocation != null) {
            fetchInterestPoints(targetLocation, (searchQuery != null) ? searchQuery : null);
        } else {
            fetchInterestPoints();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(LOG_TAG, "onWindowFocusChanged");

        if (hasFocus && !isLandscape()) {
            int height = rootContainer.getHeight();
            interestPointItemHeight = interestPointItem.getHeight() != 0 ? interestPointItem.getHeight() : interestPointItemHeight;
            if (!isCollapsed) {
                collapseEnable(height);
            } else {
                collapseDisable(height);
            }
        }
    }

    @OnClick(R.id.container_collapse_action) void collapseContainer() {
        int height = rootContainer.getHeight();
        int interestPointItemHeight = interestPointItem.getHeight();

        Log.d(LOG_TAG, "height = " + height + " ipItemHeight = " + interestPointItemHeight + " isCollapsed = " + isCollapsed);

        if (isCollapsed) {
            isCollapsed = false;
            collapseEnable(height);

        } else {
            isCollapsed = true;
            collapseDisable(height);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.map = googleMap;
        this.map.getUiSettings().setZoomControlsEnabled(true);
        this.map.setOnMarkerDragListener(this);

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

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                targetLocation = latLng;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                fetchInterestPoints(latLng.latitude, latLng.longitude);
            }
        });

        if (userLocation != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.latitude, userLocation.longitude), 13));
        }

        showMarkers();

        Log.d(LOG_TAG, "onMapReady interestPoints = " + interestPoints);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState");
        outState.putString(Constants.EVENT_UUID, eventUuid);
        outState.putString(Constants.COMMUNITY_UUID, communityUuid);
        outState.putParcelableArrayList(Constants.INTEREST_POINTS_LIST, this.interestPoints);
        outState.putBoolean(IS_COLLAPSED, isCollapsed);
        outState.putInt(INTEREST_POINT_ITEM_HEIGHT, interestPointItemHeight);
        outState.putString("searchQuery", searchQuery);
        outState.putParcelable("targetLocation", targetLocation);
        outState.putBoolean("isCollapsedInterestPointStetted", isCollapsedInterestPointStetted);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(LOG_TAG, "onRestoreInstanceState");
        eventUuid = savedInstanceState.getString(Constants.EVENT_UUID);
        communityUuid = savedInstanceState.getString(Constants.COMMUNITY_UUID);
        isCollapsed = savedInstanceState.getBoolean(IS_COLLAPSED);
        interestPointItemHeight = savedInstanceState.getInt(INTEREST_POINT_ITEM_HEIGHT);
        searchQuery = savedInstanceState.getString("searchQuery");
        targetLocation = savedInstanceState.getParcelable("targetLocation");
        isCollapsedInterestPointStetted = savedInstanceState.getBoolean("isCollapsedInterestPointStetted");
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
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

        if (isCollapsedInterestPointStetted) {
            interestPointItem.setVisibility(View.VISIBLE);
        }

        containerCollapseAction.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_black), null, null, null);

        mapContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height - (interestPointItemHeight + containerCollapseAction.getHeight())));
        mapFragment.getView().setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height - (interestPointItemHeight + containerCollapseAction.getHeight())));
        listContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (interestPointItemHeight + containerCollapseAction.getHeight())));
    }

    private void showInterestPointItem(final InterestPoint interestPoint) {
        isCollapsedInterestPointStetted = true;

        if (!isLandscape()) {
            interestPointItem.setVisibility(View.VISIBLE);
        }
        InterestPointsAdapter.InterestPointViewHolder holder = new InterestPointsAdapter.InterestPointViewHolder(
                interestPointItem,
                callbackGroup,
                callbackDetails,
                callbackVote,
                callbackCardAction
        );
        InterestPointsAdapter.InterestPointViewHolder.setView(getApplicationContext(), holder, interestPoint);
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
        Collections.sort(interestPoints);
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
                Collections.sort(interestPoints);
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
        Collections.sort(interestPoints);
        interestPointsAdapter.notifyDataSetChanged();
    }

    private void updateListAfterUnvote(final InterestPoint interestPoint) {
        for (InterestPoint ip : interestPoints) {
            if (ip.apiId.equals(interestPoint.apiId)) {
                ip.isVote = false;
                Log.i(LOG_TAG, "Group unvoting = " + ip.name);
                ip.votes--;
                showInterestPointItem(ip);
                Collections.sort(interestPoints);
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

    private void showErrorSnackBar(CharSequence message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private Callback<InterestPoint> callbackGroup = new Callback<InterestPoint>() {
        @Override
        public void apply(final InterestPoint interestPoint) {
            if (NetConnectionUtils.isConnected(getApplicationContext())) {
                if (interestPoint.isJoin) {
                    quitGroup(interestPoint);
                } else {
                    joinGroup(interestPoint);
                }
            } else {
                showNoConnexionSnackBar(coordinatorLayout);
            }
        }
    };

    private Callback<InterestPoint> callbackDetails = new Callback<InterestPoint>() {
        @Override
        public void apply(InterestPoint interestPoint) {
            startInterestPointDetailsActivity(interestPoint);
        }
    };

    private Callback<InterestPoint> callbackVote = new Callback<InterestPoint>() {
        @Override
        public void apply(final InterestPoint interestPoint) {
            if (NetConnectionUtils.isConnected(getApplicationContext())) {
                if (interestPoint.isVote) {
                    unvoteGroup(interestPoint);
                } else {
                    voteGroup(interestPoint);
                }
            } else {
                showNoConnexionSnackBar(coordinatorLayout);
            }
        }
    };


    private Callback<InterestPoint> callbackCardAction = new Callback<InterestPoint>() {
        @Override
        public void apply(InterestPoint interestPoint) {
            LatLng position = new LatLng(Double.valueOf(interestPoint.lat), Double.valueOf(interestPoint.lng));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15.5f));
        }
    };

    private void initInterestPointList() {
        interestPointsAdapter = new InterestPointsAdapter(
                getApplicationContext(),
                interestPoints,
                callbackGroup,
                callbackDetails,
                callbackVote,
                callbackCardAction
        );
        interestPointsRecyclerView.setHasFixedSize(true);
        View emptyView = findViewById(R.id.interest_points_list_empty_view);
        interestPointsRecyclerView.setEmptyView(emptyView);
        interestPointsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        interestPointsRecyclerView.setAdapter(interestPointsAdapter);
    }

    private Action1<Throwable> apiErrorCallback = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            throwable.printStackTrace();
            Log.e(LOG_TAG, "Fetch InterestPoint = " + throwable.getMessage());
            if (throwable instanceof HttpException) {
                HttpException response = (HttpException) throwable;
                switch (response.code()) {
                    case 400:
                        Log.e(LOG_TAG, "400 Bad Request");
                        showErrorSnackBar(getText(R.string.error_technical));
                        break;
                    case 409:
                        Log.e(LOG_TAG, "409 Conflict Community Already Exist");
                        showErrorSnackBar(getText(R.string.conflict_error));
                    case 401:
                        Log.e(LOG_TAG, "401 Unauthorized");
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                }
            } else {
                showErrorSnackBar(throwable.getMessage());
            }
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
        fetchInterestPoints(null, null, null);
    }

    private void fetchInterestPoints(double latitude, double longitude) {
        fetchInterestPoints(latitude + "", longitude + "", null);
    }

    private void fetchInterestPoints(LatLng location, String query) {
        fetchInterestPoints(location.latitude + "", location.longitude + "", query);
    }

    private void fetchInterestPoints(String latitude, String longitude, String address) {
        if (NetConnectionUtils.isConnected(getApplicationContext())) {
            ouQuOnMangeService.getInterestPoints(communityUuid, eventUuid, latitude, longitude, address)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(apiSuccessCallback, apiErrorCallback);
        } else {
            NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    private void voteGroup(final InterestPoint interestPointToVote) {
        ouQuOnMangeService.voteGroup(communityUuid, new VoteGroup(eventUuid, interestPointToVote.apiId, interestPointToVote.type))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Vote>() {
                    @Override
                    public void call(Vote message) {
                        updateListAfterVote(interestPointToVote);
                    }
                }, apiErrorCallback);
    }

    private void unvoteGroup(final InterestPoint interestPointToUnVote) {
        ouQuOnMangeService.unvoteGroup(communityUuid, eventUuid, interestPointToUnVote.apiId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        updateListAfterUnvote(interestPointToUnVote);
                    }
                }, apiErrorCallback);
    }

    private void joinGroup(final InterestPoint interestPointToJoin) {
        ouQuOnMangeService.joinGroup(communityUuid, new JoinGroup(eventUuid, interestPointToJoin.apiId, interestPointToJoin.type))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Group>() {
                    @Override
                    public void call(Group group) {
                        updateListAfterJoinGroup(interestPointToJoin);
                    }
                }, apiErrorCallback);
    }

    private void quitGroup(final InterestPoint interestPointToQuit) {
        ouQuOnMangeService.quitGroup(communityUuid, eventUuid, interestPointToQuit.apiId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        updateListAfterQuitGroup(interestPointToQuit);
                    }
                }, apiErrorCallback);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_interest_point_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_action_search);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }

        if (searchView != null) {
            searchView.setOnQueryTextListener(queryTextListener);
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
                return false;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.targetLocation = new LatLng(location.getLatitude(), location.getLongitude());
        this.userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (this.map != null) {
            this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
        }

        Toast.makeText(getApplicationContext(), "GPS : " + location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_LONG).show();
        fetchInterestPoints(location.getLatitude(), location.getLongitude());
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Location Changed GPS_PROVIDER checkSelfPermission");
        }
        locationManager.removeUpdates(this);
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

        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    private void showMarkers() {
        if (map != null) {
            boolean setCamera = false;
            map.clear();
            for (InterestPoint interestPoint : interestPoints) {
                LatLng position = new LatLng(Double.valueOf(interestPoint.lat), Double.valueOf(interestPoint.lng));
                if (!setCamera) {
                    setCamera = true;
                    showInterestPointItem(interestPoint);
                }
                map.addMarker(new MarkerOptions()
                        .position(position)
                        .title(interestPoint.name));
            }
            if (targetLocation != null) {
                map.addMarker(new MarkerOptions().draggable(true).position(targetLocation).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)).title(getString(R.string.map_target)));
            }
        } else {
            Log.e(LOG_TAG, "Map not ready");
            showErrorSnackBar(getText(R.string.map_not_ready));
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

    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.d(LOG_TAG, "onMarkerDragStart = " + marker.getTitle());
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Log.d(LOG_TAG, "onMarkerDrag = " + marker.getTitle());
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        targetLocation = marker.getPosition();
        fetchInterestPoints(targetLocation, searchQuery.isEmpty() ? null: searchQuery);
    }
}
