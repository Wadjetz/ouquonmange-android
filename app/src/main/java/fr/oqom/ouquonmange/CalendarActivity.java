package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.util.Calendar;

import fr.oqom.ouquonmange.adapters.EventsAdapter;
import fr.oqom.ouquonmange.dialogs.DatePickerDialogs;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.Event;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;
import fr.oqom.ouquonmange.utils.Callback3;

public class CalendarActivity extends BaseActivity {
    private static String LOG_TAG = "CalendarActivity";

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView eventsRecyclerView;
    private RecyclerView.Adapter eventsAdapter;
    private RecyclerView.LayoutManager eventsLayoutManager;

    private ArrayList<Event> events = new ArrayList<>();

    private String communityUuid;
    private Calendar day = Calendar.getInstance();

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

        initEventList();
    }

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
                eventsAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.VISIBLE);
                toolbar.setSubtitle(getString(R.string.events) + " at " + Constants.dateFormat.format(day.getTime()));
                fetchEvents(communityUuid, day);
            }
        });
        pickerDialogs.show(getFragmentManager(), "date_picker");
    }

    private void initEventList() {
        eventsAdapter = new EventsAdapter(this.events, new Callback<Event>() {
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
        eventsRecyclerView.setAdapter(eventsAdapter);
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

    private void fetchEvents(final String communityUuid, final Calendar calendar) {

        Log.d(LOG_TAG, "fetchEvents communityUuid=" + communityUuid + " calendar" + calendar);

        api.getEventsByUUID(communityUuid, calendar, new Callback<JSONArray>() {
            @Override
            public void apply(JSONArray value) {
                try {
                    events.addAll(Event.fromJson(value));
                    eventsAdapter.notifyDataSetChanged();
                    Log.e(LOG_TAG, "Fetch Events of " + communityUuid + " at " + calendar.toString() + " = " + events.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage());
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.i(LOG_TAG, value.toString());
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        }, new Callback2<Throwable, JSONObject>() {
            @Override
            public void apply(Throwable throwable, JSONObject jsonObject) {
                if (jsonObject != null) {
                    Log.e(LOG_TAG, jsonObject.toString());
                    Toast.makeText(getApplicationContext(), jsonObject.toString(), Toast.LENGTH_SHORT).show();
                }
                Log.e(LOG_TAG, throwable.getMessage());
                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }
}
