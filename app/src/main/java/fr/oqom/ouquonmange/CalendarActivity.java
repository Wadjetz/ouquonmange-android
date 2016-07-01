package fr.oqom.ouquonmange;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.oqom.ouquonmange.adapters.EventsSectionedAdapter;
import fr.oqom.ouquonmange.dialogs.DatePickerDialogs;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.Event;
import fr.oqom.ouquonmange.services.ThrowableWithJson;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback3;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class CalendarActivity extends BaseActivity {
    private static String LOG_TAG = "CalendarActivity";

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView eventsRecyclerView;
    private EventsSectionedAdapter eventsSectionedAdapter;
    private RecyclerView.LayoutManager eventsLayoutManager;

    private ArrayList<Event> events = new ArrayList<>();

    private String communityUuid;
    private Calendar day = Calendar.getInstance();
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Intent intent = getIntent();
        communityUuid =  intent.getStringExtra(Constants.COMMUNITY_UUID);

        Log.d(LOG_TAG, "onCreate = " + communityUuid);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(LOG_TAG, "onRefresh");
                events.clear();
                fetchEvents(communityUuid, day);
            }
        });

        Log.d(LOG_TAG, "Community UUID - " + communityUuid);
        initNav();
        toolbar.setSubtitle(getString(R.string.events) + " at " + Constants.dateFormat.format(day.getTime()));
        checkAuth();
        initFloatingButton();

        if (savedInstanceState == null) {
            if (communityUuid == null) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }

            fetchEvents(communityUuid, day);
        } else {
            this.events = savedInstanceState.getParcelableArrayList(Constants.EVENTS_LIST);
            this.communityUuid = savedInstanceState.getString(Constants.COMMUNITY_UUID);
            progressBar.setVisibility(View.GONE);
            Log.d(LOG_TAG, "onCreate savedInstanceState = " + this.events.size());
            long dayFromIntent = intent.getLongExtra(Constants.EVENT_DATE, -1);

            if (dayFromIntent != -1) {
                this.day.setTimeInMillis(dayFromIntent);
            }
        }

        if (communityUuid == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        initSectionedEventList();

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorCalendarLayout);
        snackbar = Snackbar.make(coordinatorLayout,"Error !",Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close), closeSnackBarCalendar);
    }

    private void initSectionedEventList() {
        eventsSectionedAdapter = new EventsSectionedAdapter(events, new Callback<Event>() {
            @Override
            public void apply(Event event) {
                Intent intent = new Intent(getApplicationContext(), InterestPointsActivity.class);
                intent.putExtra(Constants.EVENT_UUID, event.uuid);
                intent.putExtra(Constants.COMMUNITY_UUID, communityUuid);
                startActivity(intent);
            }
        });
        eventsRecyclerView = (RecyclerView) findViewById(R.id.events_list);
        eventsLayoutManager = new LinearLayoutManager(getApplicationContext());
        eventsRecyclerView.setHasFixedSize(true);
        eventsRecyclerView.setLayoutManager(eventsLayoutManager);
        eventsRecyclerView.setAdapter(eventsSectionedAdapter);
    }

    private View.OnClickListener closeSnackBarCalendar = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            snackbar.dismiss();
        }
    };

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        communityUuid = savedInstanceState.getString(Constants.COMMUNITY_UUID);
        day = Calendar.getInstance();
        day.setTimeInMillis(savedInstanceState.getLong(Constants.EVENT_DATE));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(Constants.COMMUNITY_UUID, communityUuid);
        outState.putLong(Constants.EVENT_DATE, day.getTimeInMillis());
        outState.putParcelableArrayList(Constants.EVENTS_LIST, this.events);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_events_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_calendar:
                setDate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setDate() {
        final DatePickerDialogs pickerDialogs = new DatePickerDialogs();
        pickerDialogs.setCallback(new Callback3<Integer, Integer, Integer>() {
            @Override
            public void apply(Integer year, Integer monthOfYear, Integer dayOfMonth) {
                day = Calendar.getInstance();
                day.set(year, monthOfYear, dayOfMonth);
                events.clear();
                eventsSectionedAdapter.setItemList(events);
                eventsSectionedAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.VISIBLE);
                toolbar.setSubtitle(getString(R.string.events) + " at " + Constants.dateFormat.format(day.getTime()));
                fetchEvents(communityUuid, day);
            }
        });
        pickerDialogs.show(getFragmentManager(), "date_picker");
    }

    private void initFloatingButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.create_events_fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CreateEventActivity.class);
                intent.putExtra(Constants.COMMUNITY_UUID, communityUuid);
                intent.putExtra(Constants.EVENT_DATE, day.getTimeInMillis());
                startActivity(intent);
            }
        });
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

    private void fetchEvents(final String communityUuid, final Calendar calendar) {
        api.getEvents(communityUuid, calendar)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Event>>() {
                    @Override
                    public void call(List<Event> eventsList) {
                        events.clear();
                        events.addAll(eventsList);
                        Log.e(LOG_TAG, "Fetch Events of " + communityUuid + " at " + calendar.getTime().toString() + " = " + events.size());
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        eventsSectionedAdapter.setItemList(events);
                        eventsSectionedAdapter.notifyDataSetChanged();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        ThrowableWithJson throwableWithJson = (ThrowableWithJson) throwable;
                        showApiError(throwableWithJson);
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }
}
