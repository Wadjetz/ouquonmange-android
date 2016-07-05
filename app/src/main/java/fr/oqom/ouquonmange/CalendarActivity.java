package fr.oqom.ouquonmange;

import android.content.Intent;
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

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.oqom.ouquonmange.adapters.EventsSectionedAdapter;
import fr.oqom.ouquonmange.dialogs.DatePickerDialogs;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.Event;
import fr.oqom.ouquonmange.services.OuQuOnMangeService;
import fr.oqom.ouquonmange.services.Service;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback3;
import fr.oqom.ouquonmange.utils.DateTimeUtils;
import fr.oqom.ouquonmange.utils.NetConnectionUtils;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class CalendarActivity extends BaseActivity {
    private static String LOG_TAG = "CalendarActivity";

    @BindView(R.id.progress_calendar) ProgressBar progressBar;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.coordinatorCalendarLayout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.events_list) RecyclerView eventsRecyclerView;

    private EventsSectionedAdapter eventsSectionedAdapter;

    private OuQuOnMangeService ouQuOnMangeService;

    private ArrayList<Event> events = new ArrayList<>();

    private String communityUuid;
    private DateTime day = DateTimeUtils.now();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        communityUuid = intent.getStringExtra(Constants.COMMUNITY_UUID);

        Log.d(LOG_TAG, "onCreate = " + communityUuid);

        ouQuOnMangeService = Service.getInstance(getApplicationContext());

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
        toolbar.setSubtitle(getString(R.string.events) + " at " + Constants.dateFormat.format(day.toDate()));
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
                this.day = new DateTime(dayFromIntent);
            }
        }

        if (communityUuid == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        initSectionedEventList();
    }

    private void initSectionedEventList() {
        eventsSectionedAdapter = new EventsSectionedAdapter(events, getApplicationContext(), new Callback<Event>() {
            @Override
            public void apply(Event event) {
                Intent intent = new Intent(getApplicationContext(), InterestPointsActivity.class);
                intent.putExtra(Constants.EVENT_UUID, event.uuid);
                intent.putExtra(Constants.COMMUNITY_UUID, communityUuid);
                startActivity(intent);
            }
        });
        eventsRecyclerView.setHasFixedSize(true);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        eventsRecyclerView.setAdapter(eventsSectionedAdapter);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        communityUuid = savedInstanceState.getString(Constants.COMMUNITY_UUID);
        day = new DateTime(savedInstanceState.getLong(Constants.EVENT_DATE));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(Constants.COMMUNITY_UUID, communityUuid);
        outState.putLong(Constants.EVENT_DATE, day.getMillis());
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
                day = day.withYear(year).withMonthOfYear(monthOfYear).withDayOfMonth(dayOfMonth);
                events.clear();
                eventsSectionedAdapter.setItemList(events);
                eventsSectionedAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.VISIBLE);
                toolbar.setSubtitle(getString(R.string.events) + " at " + Constants.dateFormat.format(day.toDate()));
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
                intent.putExtra(Constants.EVENT_DATE, day.getMillis());
                startActivity(intent);
            }
        });
    }

    private void fetchEvents(final String communityUuid, final DateTime calendar) {
        Log.d(LOG_TAG, "fetchEvents communityUuid=" + communityUuid + " calendar" + calendar);
        if (NetConnectionUtils.isConnected(getApplicationContext())) {
            ouQuOnMangeService.getEvents(communityUuid, calendar.getMillis() + "")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Event>>() {
                        @Override
                        public void call(List<Event> eventsList) {
                            events.clear();
                            events.addAll(eventsList);
                            checkDefaultEvents();
                            Log.d(LOG_TAG, "Fetch Events of " + communityUuid + " at " + calendar.toString() + " = " + events.size());
                            progressBar.setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);
                            eventsSectionedAdapter.setItemList(events);
                            eventsSectionedAdapter.notifyDataSetChanged();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                            if (throwable instanceof HttpException) {
                                HttpException response = (HttpException) throwable;
                                int code = response.code();
                                Log.e(LOG_TAG, "RETROFIT ERROR code = " + code);
                                progressBar.setVisibility(View.GONE);
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
        }
    }

    private void checkDefaultEvents() {
        boolean flag = false;
        Log.d(LOG_TAG, "checkDefaultEvents " + events.size());
        for (Event e : events) {

            Log.d(LOG_TAG, "checkDefaultEvents " + e.dateStart.getHourOfDay() + " = " + 12);

            if (e.dateStart.getHourOfDay() == 12) {
                flag = true;
            }
        }
        if (!flag) {
            DateTime defaultMidiStartDate = day.withHourOfDay(12).withMinuteOfHour(0);
            DateTime defaultMidiEndDate = day.withHourOfDay(13).withMinuteOfHour(0);
            ouQuOnMangeService.createEvent(communityUuid, new Event(
                    getString(R.string.default_12h_event_name),
                    getString(R.string.default_12h_event_description),
                    defaultMidiStartDate.getMillis(),
                    defaultMidiEndDate.getMillis()
            )).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Event>() {
                @Override
                public void call(Event event) {
                    events.add(event);
                    eventsSectionedAdapter.setItemList(events);
                    eventsSectionedAdapter.notifyDataSetChanged();

                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    Log.d(LOG_TAG, "checkDefaultEvents create default 12h event error = " + throwable.getMessage());
                }
            });
        }
    }
}
