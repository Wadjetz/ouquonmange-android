package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    private RecyclerView.Adapter eventsAdapter;
    private List<Event> eventOfCommunities = new ArrayList<>();
    private RecyclerView eventsRecyclerView;
    private LinearLayoutManager eventsLayoutManager;

    private String communityUuid;
    private Calendar calendar = Calendar.getInstance();

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Intent intent = getIntent();
        communityUuid =  intent.getStringExtra(Constants.COMMUNITY_UUID);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        Log.d(LOG_TAG, "Community UUID - " + communityUuid);
        initNav();
        toolbar.setSubtitle(getString(R.string.events) + " at " + dateFormat.format(calendar.getTime()));
        initEventList();
        checkAuth();
        initFloatingButton();
        fetchEvents(communityUuid, calendar);
    }

    private void initEventList() {
        eventsAdapter = new EventsAdapter(eventOfCommunities, new Callback<Event>() {
            @Override
            public void apply(Event event) {
                Intent intent = new Intent(getApplicationContext(), InterestPointsActivity.class);
                intent.putExtra(Constants.EVENT_UUID, event.uuid);
                intent.putExtra(Constants.COMMUNITY_UUID, communityUuid);
                startActivity(intent);
            }
        });

        eventsRecyclerView = (RecyclerView) findViewById(R.id.events_list);
        eventsLayoutManager = new LinearLayoutManager(this);
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
                Toast.makeText(getApplicationContext(), "Add Event TODO", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), CreateEventActivity.class);
                intent.putExtra(Constants.COMMUNITY_UUID, communityUuid);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        communityUuid = savedInstanceState.getString(Constants.COMMUNITY_UUID);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.COMMUNITY_UUID, communityUuid);
    }

    private void fetchEvents(final String communityUuid, final Calendar calendar) {
        api.getEventsByUUID(communityUuid, calendar, new Callback<JSONArray>() {
            @Override
            public void apply(JSONArray value) {
                try {
                    eventOfCommunities.addAll(Event.fromJson(value));
                    eventsAdapter.notifyDataSetChanged();
                    Log.e(LOG_TAG, "Fetch Events of " + communityUuid + " at " + calendar.toString() + " = " + eventOfCommunities.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage());
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.i(LOG_TAG, value.toString());
                progressBar.setVisibility(View.GONE);
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
            }
        });

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
                calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth);
                eventOfCommunities.clear();
                eventsAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.VISIBLE);
                toolbar.setSubtitle(getString(R.string.events) + " at " + dateFormat.format(calendar.getTime()));
                fetchEvents(communityUuid, calendar);
            }
        });
        pickerDialogs.show(getFragmentManager(), "date_picker");
    }
}
