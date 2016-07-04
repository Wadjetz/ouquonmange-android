package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.oqom.ouquonmange.adapters.EventsSectionedAdapter;
import fr.oqom.ouquonmange.dialogs.DatePickerDialogs;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.Event;
import fr.oqom.ouquonmange.services.OuQuOnMangeService;
import fr.oqom.ouquonmange.services.Service;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback3;
import fr.oqom.ouquonmange.utils.NetConnectionUtils;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class EventsFragment extends Fragment {
    private static String LOG_TAG = "EventsFgmt";

    private SwipeRefreshLayout swipeRefreshLayout;
    private CoordinatorLayout coordinatorLayout;

    private RecyclerView eventsRecyclerView;
    private EventsSectionedAdapter eventsSectionedAdapter;
    private RecyclerView.LayoutManager eventsLayoutManager;

    private OuQuOnMangeService ouQuOnMangeService;

    private ArrayList<Event> events = new ArrayList<>();

    private String communityUuid;
    private Calendar day = Calendar.getInstance();

    public static EventsFragment newInstance(String communityUuid) {
        EventsFragment eventsFragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putString(Constants.COMMUNITY_UUID, communityUuid);
        eventsFragment.setArguments(args);
        return eventsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        initView(view);
        initSectionedEventList(view);

        ouQuOnMangeService = Service.getInstance(getContext());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchEvents(communityUuid, day);
    }

    private void initView(View view) {
        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinatorCalendarLayout);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(LOG_TAG, "onRefresh");
                events.clear();
                fetchEvents(communityUuid, day);
            }
        });
    }

    private void initSectionedEventList(View view) {
        eventsSectionedAdapter = new EventsSectionedAdapter(events, getContext(), new Callback<Event>() {
            @Override
            public void apply(Event event) {
                Intent intent = new Intent(getContext(), InterestPointsActivity.class);
                intent.putExtra(Constants.EVENT_UUID, event.uuid);
                intent.putExtra(Constants.COMMUNITY_UUID, communityUuid);
                startActivity(intent);
            }
        });
        eventsRecyclerView = (RecyclerView) view.findViewById(R.id.events_list);
        eventsLayoutManager = new LinearLayoutManager(getContext());
        eventsRecyclerView.setHasFixedSize(true);
        eventsRecyclerView.setLayoutManager(eventsLayoutManager);
        eventsRecyclerView.setAdapter(eventsSectionedAdapter);
    }

    private void fetchEvents(final String communityUuid, final Calendar calendar) {
        Log.d(LOG_TAG, "fetchEvents communityUuid=" + communityUuid + " calendar" + calendar);
        if (NetConnectionUtils.isConnected(getContext())) {
            ouQuOnMangeService.getEvents(communityUuid, calendar.getTimeInMillis() + "")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Event>>() {
                        @Override
                        public void call(List<Event> eventsList) {
                            events.clear();
                            events.addAll(eventsList);
                            Log.d(LOG_TAG, "Fetch Events of " + communityUuid + " at " + calendar.getTime().toString() + " = " + events.size());
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
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    });
        } else {
            swipeRefreshLayout.setRefreshing(false);
            NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, getContext());
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
                //progressBar.setVisibility(View.VISIBLE);
                //toolbar.setSubtitle(getString(R.string.events) + " at " + Constants.dateFormat.format(day.getTime()));
                fetchEvents(communityUuid, day);
            }
        });
        pickerDialogs.show(getActivity().getFragmentManager(), "date_picker");
    }
}
